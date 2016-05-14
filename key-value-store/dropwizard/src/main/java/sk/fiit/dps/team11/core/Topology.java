package sk.fiit.dps.team11.core;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.net.InetAddresses;

import sk.fiit.dps.team11.config.TopConfiguration;

public class Topology {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Topology.class);
	
	private final static HashFunction MD5 = Hashing.md5();
	
	ConsulClient consulClient;
	
	String hostname;
	
	@Inject
	private ScheduledExecutorService execService;
	
	@Inject
	private TopConfiguration conf;
	
	private final ObjectMapper MAPPER = new ObjectMapper();
	
	private DynamoNode self;
	private SortedSet<DynamoNode> nodes = new TreeSet<>();
	
	@PostConstruct
	private void init() {
		//Gets IP address of interface ethwe0
		String interfaceAddr = "ethwe0";
		String myIpAddr = "";
		NetworkInterface interf = null;
		
		try {
			interf = NetworkInterface.getByName(interfaceAddr);
		}
		catch (SocketException e) {
			LOGGER.error("Cannot get IP address of network interface. Returned IP is '{}'", myIpAddr, e);
			e.printStackTrace();
			System.exit(-1);
		}
		
		if (interf == null) {
			//If interface ethwe0 does not exists, bind to localhost
			try {
				self = new DynamoNode(InetAddress.getLocalHost().getHostAddress(), new Random().nextLong());
				this.hostname = InetAddress.getLocalHost().getHostName();
				nodes.add(self);
			} catch (UnknownHostException e) {
				LOGGER.error("Cannot get IP address of localhost network interface.", e);
				System.exit(-1);
			}
		}
		else {
			Enumeration<InetAddress> addresses = interf.getInetAddresses();
			
	        for (InetAddress inetAddress : Collections.list(addresses)) {
	        	try {
	        		InetAddresses.forString(inetAddress.getHostAddress());
	        	} catch (IllegalArgumentException e) {
	        		LOGGER.error("Invalid IP address '{}'. Continue to next.", inetAddress.getHostAddress(), e);
	        		continue;
	        	}
	        	myIpAddr = inetAddress.getHostAddress();
	        	try {
					this.hostname = InetAddress.getLocalHost().getHostName();
				} catch (UnknownHostException e) {
					LOGGER.error("Cannot get Hostname from Localhost", e);
					e.printStackTrace();
				}
	        }
			
			LOGGER.info("Registering node with hostname {} and IP address {}.", this.hostname, myIpAddr);
			
			Long myPosition = new Long(new Random().nextLong());

			consulClient = new ConsulClient("consul-server");
			consulServiceRegister(myPosition);
			
			self = new DynamoNode(myIpAddr, myPosition);
			nodes.add(self);
		}
		
		execService.scheduleAtFixedRate(this::poll, 0, conf.getReliability().getTopologyChangeTimeoutMillis(),
				TimeUnit.MILLISECONDS);
	}
	
	private long getPositionInChord(byte[] key) {
		return MD5.hashBytes(key).asLong();
	}
	
	private int numReplicas() {
		return conf.getReliability().getNumReplicas();
	}
	
	private void poll() {
		SortedSet<DynamoNode> activeDynamoNodes = new TreeSet<DynamoNode>();

		WebTarget target = ClientBuilder.newClient()
				.target("http://consul-server:8500/v1/health/service/dynamo");

		Response response = target.request().get();
		
		JsonNode responseJson;
		try {
			responseJson = MAPPER.readTree(response.readEntity(String.class));
		if (responseJson.isArray()) {
			responseJson.forEach(node -> {

				JsonNode n = node.get("Node");
				JsonNode addr = n.get("Address");
				String ipAddr = addr.textValue();
				
				JsonNode service = node.get("Service");
				Long nodePosition = Long.getLong(service.get("ID").textValue());
				
				JsonNode checks = node.get("Checks");
				JsonNode servReachability = checks.get(0).get("Status");
				JsonNode serfReachability = checks.get(1).get("Status");
				
				if ( (servReachability.textValue().compareToIgnoreCase("passing") == 0) && 
					(serfReachability.textValue().compareToIgnoreCase("passing") == 0) ) {
					activeDynamoNodes.add(new DynamoNode(ipAddr, nodePosition));
				}
			});
		}
		} catch (IOException e) {
			LOGGER.error("", e);
		}
		
		this.compareTopology(activeDynamoNodes);
	}
	
	public boolean isMy(byte[] key) {
		long hash = getPositionInChord(key);
		
		DynamoNode responsibleNode = null;
		DynamoNode actualNode;
		
		synchronized (nodes) {
			while (this.nodes.iterator().hasNext()) {
				actualNode = this.nodes.iterator().next();
				if ( actualNode.getPosition() > hash ) {
					//First node with higher position in sorted set is responsible for hash key
					responsibleNode = actualNode;
					break;
				}
			}
			if ( responsibleNode == null ) {
				responsibleNode = this.nodes.last();
			}
		}
		
		LOGGER.info("Checking if key '{}' belongs to my node [{}+ {}]", hash,
				self.getIp(), self.getPosition());
		
		if ( responsibleNode.equals(this.self) ) {
			return true;
		}
		else {
			return false;
		}
		
	}
	
	public DynamoNode self() {
		return self;
	}
	
	public DynamoNode nodeForIp(String ip) {
		
		Optional<DynamoNode> nodeWithIp;
		synchronized (nodes) {
			nodeWithIp = nodes.stream().filter(node -> node.getIp().equals(ip)).findFirst();
		}

		if (nodeWithIp.isPresent()) {
			return nodeWithIp.get();
		}
		
		DynamoNode newNode = new DynamoNode(ip, (long) 0);
		return newNode;
		
	}
	
	/**
	 * 
	 * @return list of nodes responsible for the key, ordered counter-clockwise (except this one)
	 */
	public List<DynamoNode> nodesForKey(byte[] key) {
		
		long hash = getPositionInChord(key);
		List<DynamoNode> responsibleNodes = new ArrayList<DynamoNode>();
		
		DynamoNode[] dynamoNodeArray = (DynamoNode[]) nodes.toArray();
		int firstResponsibleNodeIndex = 0;
		
		synchronized (nodes) {
			for (int i = 0; i < dynamoNodeArray.length; i++) {
				if ( dynamoNodeArray[i].getPosition() > hash ) {
					//First node with higher position in sorted set is responsible for hash key
					firstResponsibleNodeIndex = i;
					break;
				}
			}

			for (int i = 0; i < numReplicas() - 1 ; i++) {
				int circularIndex = (firstResponsibleNodeIndex - i) % dynamoNodeArray.length; 
				responsibleNodes.add(dynamoNodeArray[circularIndex]);
			}
		}
		
		try {
			LOGGER.info("Responsible nodes for key {} are {}", hash, MAPPER.writeValueAsString(responsibleNodes));
		} catch (JsonProcessingException e) {}
		
		return responsibleNodes;
				
	}
	
	public void notifyFailedNode(DynamoNode node) {
		// The whole topology will be rescanned
		this.poll();
	}
	
	public void compareTopology(SortedSet<DynamoNode> healthyNodes) {
		
		TreeSet<DynamoNode> healthyNodesTset = (TreeSet<DynamoNode>) healthyNodes;
		
		SortedSet<String> healthyNodesIps = new TreeSet<String>();
		while (healthyNodes.iterator().hasNext()) {
			healthyNodesIps.add(nodes.iterator().next().getIp());
		}
		
		SortedSet<String> nodesIps = new TreeSet<String>();
		while (nodes.iterator().hasNext()) {
			nodesIps.add(nodes.iterator().next().getIp());
		}
		
		if ( nodesIps.equals(healthyNodesIps) ) {
			return;
		}
		else {
			LOGGER.info("Topology changed, initiating topology rebuild");
			updateTopology(healthyNodesTset);
		}
		
	}
	
	private void addNode(DynamoNode node) {
		synchronized (nodes) {
			nodes.add(node);
		}
	}
	
	private void deleteNode(DynamoNode node) {
		synchronized (nodes) {
			nodes.remove(node);
		}
	}
	
	private void updateTopology(SortedSet<DynamoNode> newNodesIpsSet) {

		//find deleted nodes
		while ( nodes.iterator().hasNext() ) {
			DynamoNode dn = nodes.iterator().next();
			String ip = dn.getIp();
			Long pos = dn.getPosition();
			if ( !newNodesIpsSet.contains(ip) ) {
				//node was deleted
				LOGGER.info("Removing node with IP {} and position {} from topology", ip, pos);
				this.deleteNode(dn);
			}
		}
		//find new nodes
		while ( newNodesIpsSet.iterator().hasNext() ) {
			DynamoNode dn = newNodesIpsSet.iterator().next();
			String ip = dn.getIp();
			Long pos = dn.getPosition();
			if ( nodes.contains(ip) ) {
				//node is already in dynamo
				;
			}
			else {
				//new node
				LOGGER.info("Adding node with IP {} and position {} to topology", ip, pos);
				this.addNode(new DynamoNode(ip, pos));
			}
		}
		
		this.printDynamoNodesToLogger();
		
	}
	
	private void consulServiceRegister(Long position) {
		
		// List<String> tags = new ArrayList<String>();
		// tags.add(position);

		NewService newService = new NewService();
		
		newService.setId(position.toString());
		newService.setName("dynamo");
		newService.setPort(8080);
		//newService.setTags(tags);

		NewService.Check serviceCheck = new NewService.Check();
		serviceCheck.setHttp(String.format("http://%s:8081/healthcheck", this.hostname));
		serviceCheck.setInterval("5s");
		serviceCheck.setTimeout("3s");
		newService.setCheck(serviceCheck);

		LOGGER.info("Consul register: 'http://{}:8081/healthcheck'.", this.hostname);
		consulClient.agentServiceRegister(newService);
		
	}
	
	public void printDynamoNodesToLogger() {
		try {
			LOGGER.info("Nodes: {}", MAPPER.writeValueAsString(nodes));
		} catch (JsonProcessingException e) {}
		
	}

}
