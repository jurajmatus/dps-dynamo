package sk.fiit.dps.team11.core;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import sk.fiit.dps.team11.config.TopConfiguration;

public class Topology {
	
	private final static HashFunction MD5 = Hashing.md5();
	
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
		
		//zisti pocet uzlov
		
		//
		
		try {
			self = new DynamoNode(InetAddress.getLocalHost().getHostAddress(), new Random().nextLong());
			nodes.add(self);
		} catch (UnknownHostException e) {
			throw new RuntimeException("Cannot continue without having an IP address");
		}
		
		// TODO - register some repeated polling?
		// execService.scheduleAtFixedRate(this::poll, 0, 1, TimeUnit.SECONDS);
	}
	
	private void poll() {
		
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
		
		DynamoNode newNode = new DynamoNode(ip, 0);
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

}
