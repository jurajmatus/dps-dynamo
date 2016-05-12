package sk.fiit.dps.team11.core;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
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
	
	
	
	private long getPositionInChord(byte[] key) {
		return MD5.hashBytes(key).asLong();
	}
	
	private int numReplicas() {
		return conf.getReliability().getNumReplicas();
	}
	
	
	
	@PostConstruct
	private void init() {
		
		//Gets IP address of interface ethwe0
		String interfaceAddr = "ethwe0";
		String myIpAddr = "";
		
		try {
			NetworkInterface interf = NetworkInterface.getByName(interfaceAddr);
			Enumeration<InetAddress> addresses = interf.getInetAddresses();
			
			int i = 0;
			if ( addresses.hasMoreElements() ) {
				InetAddress ia = addresses.nextElement();
				this.hostname = ia.getHostName(); 
				myIpAddr = ia.getHostAddress();
				System.out.printf("IP address '%d' of host '%s' interface is '%s'\n", i, ia.getHostName(), myIpAddr);
				LOGGER.info("Registering node with hostname '%s' and IP address '%s'\n", this.hostname, myIpAddr);
				i++;
			}
		}
		catch (SocketException e) {
			System.err.printf("Cannot get IP address of network interface. Returned IP is '%s'\n", myIpAddr);
			LOGGER.error("Cannot get IP address of network interface. Returned IP is '%s'\n", myIpAddr);
			System.exit(-1);
		}
		
		Long myPosition = new Long(new Random().nextLong());
		
		consulClient = new ConsulClient("ethwe0", 8080);
		consulServiceRegister(myPosition);
		
		self = new DynamoNode(myIpAddr, myPosition);
		nodes.add(self);
		
		// TODO - register some repeated polling?
		// execService.scheduleAtFixedRate(this::poll, 0, 1, TimeUnit.SECONDS);
	}
	
	private void poll() {
		//TODO
	}
	
	public boolean isMy(byte[] key) {
		long hash = getPositionInChord(key);
		
		// TODO
		// like nodesForKey(), but presence of this node is checked
		
		return true;
	}
	
	public DynamoNode self() {
		return self;
	}
	
	private void addNode(DynamoNode node) {
		// TODO - initialization logic - poll for information, ...
		// TODO - use value that came via some information protocol
		node.setPosition(0);
		
		synchronized (nodes) {
			nodes.add(node);
		}
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
		execService.execute(() -> addNode(newNode));
		return newNode;
		
	}
	
	/**
	 * 
	 * @return list of nodes responsible for the key, ordered counter-clockwise (except this one)
	 */
	public List<DynamoNode> nodesForKey(byte[] key) {
		// TODO
		long hash = getPositionInChord(key);
		
		synchronized (nodes) {
			// Iterate nodes
			// Find the one with highest lower position than key position (variable hash)
			// Take n-1 succeeding nodes (function numReplicas() - 1)
			// Remove self if present, to not confuse replication worker - we don't want to send
			// replication message to self
		}
		
		// Stub implementation - redirect will be to loopback
		// Just for debugging - final implementation should not contain self
		return Collections.nCopies(numReplicas(), self);
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
				LOGGER.info("Removing node with IP '%s' and position '%ld' from topology\n", ip, pos);
				nodes.remove(ip);
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
				LOGGER.info("Adding node with IP '%s' and position '%ld' to topology\n", ip, pos);
				synchronized (nodes) {
					nodes.add(new DynamoNode(ip, pos));
				}
			}
		}
	}
	
	private void consulServiceRegister(Long position) {
		
		// List<String> tags = new ArrayList<String>();
		// tags.add(position);
		
		// register new service with associated health check
		NewService newService = new NewService();
		
		newService.setId(position.toString());
		newService.setName("dynamo");
		newService.setPort(8080);
		//newService.setTags(tags);

		NewService.Check serviceCheck = new NewService.Check();
		serviceCheck.setHttp("http://localhost:8081/healthcheck");
		serviceCheck.setInterval("5s");
		serviceCheck.setTimeout("3s");
		newService.setCheck(serviceCheck);

		consulClient.agentServiceRegister(newService);
		
	}

}
