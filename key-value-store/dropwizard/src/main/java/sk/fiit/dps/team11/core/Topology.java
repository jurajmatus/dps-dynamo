package sk.fiit.dps.team11.core;

import static java.util.stream.Collectors.toList;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Stream;

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
	
	private InetAddress myAddress;
	
	private long myPosition;
	
	@PostConstruct
	private void init() {
		myPosition = new Random().nextLong();
		
		try {
			myAddress = InetAddress.getLocalHost();
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
		return (new Random()).nextBoolean();
	}
	
	/**
	 * 
	 * @return list of nodes responsible for the key, ordered counter-clockwise (except this one)
	 */
	public List<DynamoNode> nodesForKey(byte[] key) {
		// TODO
		long hash = getPositionInChord(key);
		
		// Stub implementation - redirect will be to loopback
		return Stream.of(myAddress)
			.map(addr -> new DynamoNode(addr.getHostAddress()))
			.collect(toList());
	}
	
	// TODO - polling, notifications about new nodes / nodes removals

}
