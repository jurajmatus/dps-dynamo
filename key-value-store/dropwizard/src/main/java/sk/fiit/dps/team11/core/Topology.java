package sk.fiit.dps.team11.core;

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

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.net.InetAddresses;

import sk.fiit.dps.team11.config.TopConfiguration;
import sk.fiit.dps.team11.workers.CheckTopologyChangeWorker;

public class Topology {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CheckTopologyChangeWorker.class);
	private final static HashFunction MD5 = Hashing.md5();
	
	ConsulClient consulClient;
	
	String hostname;
	
	@Inject
	private ScheduledExecutorService execService;
	
	@Inject
	private TopConfiguration conf;
	
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
			System.err.printf("Cannot get IP address of network interface. Returned IP is '%s'\n", myIpAddr);
			LOGGER.error("Cannot get IP address of network interface. Returned IP is '%s'\n", myIpAddr);
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
				System.err.printf("Cannot get IP address of localhost network interface\n.");
				LOGGER.error("Cannot get IP address of localhost network interface\n.");
				e.printStackTrace();
				System.exit(-1);
			}
		}
		else {
			Enumeration<InetAddress> addresses = interf.getInetAddresses();
			
	        for (InetAddress inetAddress : Collections.list(addresses)) {
	        	try {
	        		InetAddresses.forString(inetAddress.getHostAddress());
	        	} catch (IllegalArgumentException e) {
	        		System.err.printf("Invalid IP address '%s'. Continue to next.\n", inetAddress.getHostAddress());
	        		continue;
	        	}
	        	myIpAddr = inetAddress.getHostAddress();
	        	try {
					this.hostname = InetAddress.getLocalHost().getHostName();
				} catch (UnknownHostException e) {
					System.err.printf("Cannot get Hostname from Localhost\n");
					LOGGER.error("Cannot get Hostname from Localhost\n");
					e.printStackTrace();
				}
	        }
			
			System.out.printf("Registering node with hostname '%s' and IP address '%s'.\n", this.hostname, myIpAddr);
			LOGGER.info("Registering node with hostname " + this.hostname + " and IP address " + myIpAddr + ".");
			
			Long myPosition = new Long(new Random().nextLong());

			consulClient = new ConsulClient("consul-server");
			consulServiceRegister(myPosition);
			
			self = new DynamoNode(myIpAddr, myPosition);
			nodes.add(self);
		}
		
		// TODO - register some repeated polling?
		// execService.scheduleAtFixedRate(this::poll, 0, 1, TimeUnit.SECONDS);
	}
	
	private long getPositionInChord(byte[] key) {
		return MD5.hashBytes(key).asLong();
	}
	
	private int numReplicas() {
		return conf.getReliability().getNumReplicas();
	}
	
	private void poll() {
		//TODO
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
				responsibleNode = this.nodes.first();
			}
		}
		
		if ( responsibleNode.equals(this.self) ) {
			System.out.print("Checking if key '" + hash + "' belongs to my node [" + 
					self.getIp() + ";" + self.getPosition() + "] --- TRUE");
			LOGGER.info("Checking if key '" + hash + "' belongs to my node [" + 
					self.getIp() + ";" + self.getPosition() + "] --- TRUE");
			return true;
		}
		else {
			System.out.print("Checking if key '" + hash + "' belongs to my node [" + 
					self.getIp() + ";" + self.getPosition() + "] --- FALSE");
			LOGGER.info("Checking if key '" + hash + "' belongs to my node [" + 
					self.getIp() + ";" + self.getPosition() + "] --- FALSE");
			return false;
		}
		
	}
	
	public DynamoNode self() {
		return self;
	}
	
	private void addNode(DynamoNode node) {
		// TODO - initialization logic - poll for information, ...
		
		//Redistribute items from other nodes and also replicate them to appropriate nodes 
		
		synchronized (nodes) {
			nodes.add(node);
		}
	}
	
	private void deleteNode(DynamoNode node) {
		// TODO - initialization logic - poll for information, ...
		
		//Save items from deleted node replicas and redistribute them to appropriate nodes 
		
		synchronized (nodes) {
			nodes.remove(node);
		}
	}
	
	public DynamoNode nodeForIp(String ip) {
		
		//Finds first DynamoNode with given 'ip'
		Optional<DynamoNode> nodeWithIp;
		synchronized (nodes) {
			nodeWithIp = nodes.stream().filter(node -> node.getIp().equals(ip)).findFirst();
		}

		if (nodeWithIp.isPresent()) {
			return nodeWithIp.get();
		}
		
		DynamoNode newNode = new DynamoNode(ip, (long) 0);
		execService.execute(() -> addNode(newNode));
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
		
		StringBuilder sb = new StringBuilder();
		while (responsibleNodes.iterator().hasNext()) {
			DynamoNode dn = responsibleNodes.iterator().next();
			sb.append("\tNode: IP[" + dn.getIp() + "] Position[" + dn.getPosition() + "]\n");
		}
		System.out.print("Responsible nodes for key" + hash + " are: \n" + sb.toString());
		LOGGER.info("Responsible nodes for key" + hash + " are " + sb.toString());
		
		return responsibleNodes;
				
	}
	
	// TODO - polling, notifications about new nodes / nodes removals
	
	public void notifyFailedNode(DynamoNode node) {
		// TODO - remove from internal database, update positional info
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
			System.out.printf("Topology changed, initiating topology rebuild\n");
			LOGGER.info("Topology changed, initiating topology rebuild\n");
			updateTopology(healthyNodesTset);
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
				System.out.print("Removing node with IP " + ip + " and position " + pos + " from topology\n");
				LOGGER.info("Removing node with IP " + ip + " and position " + pos + " from topology\n");
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
				System.out.print("Removing node with IP " + ip + " and position " + pos + " from topology\n");
				LOGGER.info("Adding node with IP " + ip + " and position " + pos + " to topology\n");
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

		System.out.printf("Consul register: 'http://%s:8081/healthcheck'.\n", this.hostname);
		consulClient.agentServiceRegister(newService);
		
	}
	
	public void printDynamoNodesToLogger() {
		
		StringBuilder sb = new StringBuilder();
		int i = 0;
		while (nodes.iterator().hasNext()) {
			DynamoNode dn = nodes.iterator().next();
			sb.append(String.format(
					"Dynamo node[" + i + "] has IP " + dn.getIp() + " and Position " + dn.getPosition() + ".\n"));
			i++;
		}
		System.out.print(sb.toString());
		LOGGER.info(sb.toString());
		
	}

}
