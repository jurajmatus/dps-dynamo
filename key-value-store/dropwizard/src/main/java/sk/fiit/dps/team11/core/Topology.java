package sk.fiit.dps.team11.core;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientProperties;
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
import com.sleepycat.je.DatabaseException;

import sk.fiit.dps.team11.config.TopConfiguration;
import sk.fiit.dps.team11.models.PutRequest;
import sk.fiit.dps.team11.models.PutResponse;

public class Topology {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Topology.class);
	
	private final static HashFunction MD5 = Hashing.md5();
	
	ConsulClient consulClient;
	
	String myIpAddr;
	String hostname;

	Thread pollThread;
	
	@Inject
	private TopConfiguration conf;
	
	@Inject
	private DatabaseAdapter db;
	
	private final ObjectMapper MAPPER = new ObjectMapper();
	
	private DynamoNode self;
	private SortedSet<DynamoNode> nodes = new TreeSet<>();
	
	@PostConstruct
	private void init() {
		//Gets IP address of interface ethwe0
		String interfaceAddr = "ethwe0";
		this.myIpAddr = null;
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
	        		LOGGER.error("Invalid IP address '{}'. Continue to next.", inetAddress.getHostAddress());
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
			
	        if (myIpAddr == null) {
	        	LOGGER.error("Got no IP address from interfaces\n");
	        	System.exit(-1);
	        }
			LOGGER.info("Registering node with hostname {} and IP address {}.", this.hostname, myIpAddr);
			
			Long myPosition = new Long(new Random().nextLong());

			consulClient = new ConsulClient("consul-server");
			consulServiceRegister(myPosition);
			
			self = new DynamoNode(myIpAddr, myPosition);
			nodes.add(self);
		}
		
		pollThread = new Thread(poll);
		pollThread.start();

	}
	
	private long getPositionInChord(byte[] key) {
		return MD5.hashBytes(key).asLong();
	}
	
	private int numReplicas() {
		return conf.getReliability().getNumReplicas();
	}
	
	public boolean isMy(byte[] key) {
		long hash = getPositionInChord(key);
		
		DynamoNode responsibleNode = null;
		DynamoNode actualNode;
		
		synchronized (nodes) {
			Iterator<DynamoNode> it = this.nodes.iterator();
			while (it.hasNext()) {
				actualNode = it.next();
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
		
		LOGGER.info("isMy: Checking if key '{}' belongs to my node [{};{}]", hash,
				self.getIp(), self.getPosition());
		
		if ( responsibleNode.equals(this.self) ) {
			LOGGER.info("Key belongs to me");
			return true;
		}
		else {
			LOGGER.info("Key does not belong to me");
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
			LOGGER.info("nodesForKey: Responsible nodes for key {} are {}", hash, MAPPER.writeValueAsString(responsibleNodes));
		} catch (JsonProcessingException e) {}
		
		return responsibleNodes;
				
	}
	
	public void notifyFailedNode(DynamoNode node) {
		// The whole topology will be rescanned
		//this.poll();
	}

	Runnable poll = new Runnable() {
    	@Override
    	public void run() {
    		try {
				Thread.sleep(25000);
			} catch (InterruptedException e1) {
				LOGGER.error("", e1);
			}
    		while (true) {
    			//LOGGER.info("Polling consul");
				SortedSet<DynamoNode> activeDynamoNodes = new TreeSet<DynamoNode>();
		
				String uri = "http://consul-server:8500/v1/health/service/dynamo";
				WebTarget target = ClientBuilder.newClient()
						.target(uri);
				
				Response response = target.request().get();

				JsonNode responseJson;
				try {
					responseJson = MAPPER.readTree(response.readEntity(String.class));
					if (responseJson.isArray()) {
						responseJson.forEach(node -> {
							JsonNode service = node.get("Service");
							JsonNode hostname = service.get("ID");
							
							JsonNode ipAddr = service.get("Address");
							JsonNode position = service.get("Tags").get(0);
							
							JsonNode checks = node.get("Checks");
							JsonNode servReachability = checks.get(0).get("Status");
							JsonNode serfReachability = checks.get(1).get("Status");
							
							if ( (servReachability.textValue().compareToIgnoreCase("passing") == 0) && 
								(serfReachability.textValue().compareToIgnoreCase("passing") == 0) ) {
								activeDynamoNodes.add(
										new DynamoNode(ipAddr.textValue(), Long.valueOf(position.textValue())));
							}
							
							/*LOGGER.info("Topology Poll: services: HOSTNAME[{}] IP[{}] POS[{}] "
									+ "SERF_STATUS[{}] SERV_STATUS[{}]", hostname.textValue(), ipAddr.textValue(), 
									position.textValue(), serfReachability.textValue(), servReachability.textValue());*/
						});
					}
				} catch (IOException e) {
					LOGGER.error("", e);
				}
				
				try {
					LOGGER.info("Topology Poll: Active dynamo nodes are " + MAPPER.writeValueAsString(activeDynamoNodes));
				} catch (JsonProcessingException e) {
					LOGGER.error("", e);
				}
				
				if ( !activeDynamoNodes.isEmpty() )
					compareTopology(activeDynamoNodes);
				
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					LOGGER.error("", e);
				}
    		}
    	}
	};
	
	public void compareTopology(SortedSet<DynamoNode> healthyNodes) {
		
		TreeSet<DynamoNode> healthyNodesTset = (TreeSet<DynamoNode>) healthyNodes;

		SortedSet<String> healthyNodesIps = new TreeSet<String>();
		Iterator<DynamoNode> it = healthyNodesTset.iterator();
		while (it.hasNext()) {
			DynamoNode dn = it.next();
			healthyNodesIps.add(dn.getIp());
		}

		SortedSet<String> nodesIps = new TreeSet<String>();
		it = nodes.iterator();
		while (it.hasNext()) {
			DynamoNode dn = it.next();
			nodesIps.add(dn.getIp());
		}
		
		if ( nodesIps.equals(healthyNodesIps) ) {
			//LOGGER.info("Topology did not change, checking again");
			return;
		}
		else {
			LOGGER.info("Topology changed, initiating topology rebuild");
			updateTopology(healthyNodes);
		}
		
	}

	/**
	 * 
	 * Adds node to node list. If newNode is previous to self, move items for which the new node is responsible
	 * @param newNode
	 */
	private void addNode(DynamoNode newNode) {

		synchronized (nodes) {
			TreeSet<DynamoNode> nodesTmp = (TreeSet<DynamoNode>) nodes;
			nodesTmp.add(newNode);
			
			if ( nodesTmp.last().equals(newNode) ) {
				// if new node will be new last in set
				if ( nodesTmp.first().equals(self) ) {
					// and I am first 
					this.moveItems(self, newNode);
				}
			}
			else {
				// if new node is not last
				DynamoNode previousNode = nodesTmp.first();
				Iterator<DynamoNode> it = nodesTmp.iterator();
				if ( it.hasNext() ) 
					it.next();
				
				while ( it.hasNext() ) {
					// find if I have key overlap with new node
					DynamoNode dn = it.next();
					if ( newNode.equals(previousNode) ) {
						if (dn.equals(self)) {
							this.moveItems(self, newNode);
							break;
						}
					}
					previousNode = dn;
				}
			}
			nodes.add(newNode);
		}
	}
	
	private PutResponse put(PutRequest request, WebTarget target) throws Exception {
		Response response = target.request().buildPut(Entity.entity(request, MediaType.APPLICATION_JSON)).invoke();
		
		PutResponse resp = response.readEntity(PutResponse.class);
		
		return resp;
	}
	
	private void moveItems(DynamoNode resendingNode, DynamoNode newNode) {
		//resending node is one node after newNode
		
		int MIN_NUM_RW = 1;
		String URL = String.format("http://%s:8080/", newNode.getIp());
		WebTarget target = ClientBuilder.newClient()
				.property(ClientProperties.CONNECT_TIMEOUT, 5000)
				.property(ClientProperties.READ_TIMEOUT, 5000)
				.target(URL + "storage");
		

		try {
			db.forEach( (key, value) -> {
				ByteBuffer bb = ByteBuffer.wrap(key);
				if ( (bb.getLong() <= newNode.getPosition()) && (bb.getLong() > resendingNode.getPosition()) ) {
					LOGGER.info("Sending overlaped data[K:{};V:{}] to new node[{},{}]", 
							key, value.getValues().get(value.getValues().size() - 1).data, 
							newNode.getIp(), newNode.getPosition());
					PutResponse resp;
					try {
						resp = put(new PutRequest(key, 
								value.getValues().get(value.getValues().size() - 1).data, 
								value.getVersion(),	MIN_NUM_RW), 
								target);
					} catch (Exception e) {
						LOGGER.error("", e);
					}
				}
			});
		} catch (DatabaseException e) {
			LOGGER.error("", e);
		}
		
	}
		
	private void deleteNode(DynamoNode node) {
		synchronized (nodes) {
			nodes.remove(node);
		}
		
	}
	
	private void updateTopology(SortedSet<DynamoNode> newNodesSet) {

		//find deleted nodes
		Iterator<DynamoNode> it = nodes.iterator();
		while ( it.hasNext() ) {
			DynamoNode dn = it.next();
			String ip = dn.getIp();
			Long pos = dn.getPosition();
			if ( !newNodesSet.contains(dn) ) {
				//node was deleted
				LOGGER.info("Removing node with IP {} and position {} from topology", ip, pos);
				this.deleteNode(dn);
			}
			else {
				//LOGGER.info("OLD: Node[{};{}] is already in topology", ip, pos);
				;
			}
		}
		
		//find new nodes
		it = newNodesSet.iterator();
		while ( it.hasNext() ) {
			DynamoNode dn = it.next();
			String ip = dn.getIp();
			Long pos = dn.getPosition();
			if ( nodes.contains(dn) ) {
				//node is already in dynamo
				//LOGGER.info("NEW: Node[{};{}] is already in topology", ip, pos);
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
		
		List<String> tags = new ArrayList<String>();
		tags.add(position.toString());

		NewService newService = new NewService();
		
		newService.setId(this.hostname);
		newService.setName("dynamo");
		newService.setPort(8080);
		newService.setTags(tags);
		newService.setAddress(this.myIpAddr);

		NewService.Check serviceCheck = new NewService.Check();
		serviceCheck.setHttp(String.format("http://%s:8081/healthcheck", this.hostname));
		serviceCheck.setInterval("5s");
		serviceCheck.setTimeout("3s");
		newService.setCheck(serviceCheck);
		
		LOGGER.info("Consul deregistering ID: {}.", this.hostname);
		consulClient.agentServiceDeregister(this.hostname);
		consulClient.agentCheckDeregister(this.hostname);
		
		LOGGER.info("Consul register: 'http://{}:8081/healthcheck'.", this.hostname);
		consulClient.agentServiceRegister(newService);
		
	}
	
	public void printDynamoNodesToLogger() {
		try {
			LOGGER.info("Current nodes in topology: \n{}", MAPPER.writeValueAsString(nodes));
		} catch (JsonProcessingException e) {}
		
	}

}
