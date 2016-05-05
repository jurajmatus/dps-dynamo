package sk.fiit.dps.team11.core;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.Random;
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
		} catch (UnknownHostException e) {
			throw new RuntimeException("Cannot continue without having an IP address");
		}
		
		// TODO - register some repeated polling?
		// execService.scheduleAtFixedRate(this::poll, 0, 1, TimeUnit.SECONDS);
	}
	
	private void poll() {
		
	}
	
	public boolean isMy(byte[] key) {
		// TODO
		long hash = getPositionInChord(key);
		
		// Stub implementation to allow testing redirect / non-redirect branches
		// return (new Random()).nextBoolean();
		return true;
	}
	
	public DynamoNode self() {
		return self;
	}
	
	/**
	 * 
	 * @return list of nodes responsible for the key, ordered counter-clockwise (except this one)
	 */
	public List<DynamoNode> nodesForKey(byte[] key) {
		// TODO
		long hash = getPositionInChord(key);
		
		// Stub implementation - redirect will be to loopback
		// Just for debugging - final implementation should not contain self
		return Collections.nCopies(numReplicas(), self);
	}
	
	// TODO - polling, notifications about new nodes / nodes removals
	
	public void notifyFailedNode(DynamoNode node) {
		// TODO - remove from internal database, update positional info
	}

}
