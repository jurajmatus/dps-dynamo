package sk.fiit.dps.team11.core;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class Topology {
	
	private final static HashFunction MD5 = Hashing.md5();
	
	private long getPositionInChord(byte[] key) {
		return MD5.hashBytes(key).asLong();
	}
	
	@Inject
	private ScheduledExecutorService execService;
	
	@PostConstruct
	private void init() {
		// TODO - register some repeated polling?
		// execService.scheduleAtFixedRate(this::poll, 0, 1, TimeUnit.SECONDS);
	}
	
	private void poll() {
		
	}
	
	/**
	 * 
	 * @return list of nodes responsible for the key (the first item in list should be the coordinator)
	 */
	public List<DynamoNode> nodesForKey(byte[] key) {
		// TODO
		long hash = getPositionInChord(key);
		
		return Collections.emptyList();
	}
	
	// TODO - polling, notifications about new nodes / nodes removals

}
