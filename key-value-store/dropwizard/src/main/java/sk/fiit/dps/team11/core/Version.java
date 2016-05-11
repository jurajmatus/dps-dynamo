package sk.fiit.dps.team11.core;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.google.common.net.InetAddresses;

import sk.fiit.dps.team11.core.Version.Deserializer;
import sk.fiit.dps.team11.core.Version.Serializer;

@JsonSerialize(using = Serializer.class)
@JsonDeserialize(using = Deserializer.class)
public class Version {

	public static final Version INITIAL = new Version(Collections.emptyList());
	
	public static class Serializer extends JsonSerializer<Version> {

		@Override
		public void serialize(Version value, JsonGenerator gen, SerializerProvider serializers)
				throws IOException, JsonProcessingException {
			
			ByteBuffer buf = ByteBuffer.allocate(MAX_ENTRIES * 16);
			
			for (Entry entry : value.entries) {
				int ipAsInt = InetAddresses.coerceToInteger(InetAddresses.forString(entry.nodeIp));
				buf.putInt(ipAsInt);
				buf.putLong(entry.timestamp);
				buf.putInt(entry.versionNumber);
			}
			
			buf.flip();
			gen.writeBinary(buf.array(), 0, buf.limit());
		}
		
	}
	
	public static class Deserializer extends JsonDeserializer<Version> {

		@Override
		public Version deserialize(JsonParser p, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {
			
			ByteBuffer buf = ByteBuffer.wrap(p.getBinaryValue());
			
			List<Entry> entries = new ArrayList<>();
			
			while (buf.remaining() >= 16) {
				int ipAsInt = buf.getInt();
				Date timestamp = new Date(buf.getLong());
				int versionNumber = buf.getInt();
				entries.add(new Entry(InetAddresses.fromInteger(ipAsInt).getHostAddress(), versionNumber, timestamp));
			}
			
			return new Version(entries);
		}
		
	}
	
	public static enum Comp {
		FIRST_NEWER,
		SECOND_NEWER,
		CONCURRENT,
		EQUAL
	}
	
	private static int versNum(Map<String, Entry> map, String key) {
		return map.getOrDefault(key, new Entry(key, 0)).versionNumber;
	}
	
	public static Comp compare(Version v1, Version v2) {
		Map<String, Entry> entries1 = v1.entriesByNode();
		Map<String, Entry> entries2 = v2.entriesByNode();
		
		SetView<String> allKeys = Sets.union(entries1.keySet(), entries2.keySet());
		
		boolean test;
		
		test = allKeys.stream().allMatch(key -> versNum(entries1, key) == versNum(entries2, key));
		if (test) {		
			return Comp.EQUAL;
		}
		
		test = allKeys.stream().allMatch(key -> versNum(entries1, key) >= versNum(entries2, key));
		if (test) {		
			return Comp.FIRST_NEWER;
		}
		
		test = allKeys.stream().allMatch(key -> versNum(entries1, key) <= versNum(entries2, key));
		if (test) {		
			return Comp.SECOND_NEWER;
		}
		
		return Comp.CONCURRENT;
	}
	
	private static class Entry implements Comparable<Entry> {
		final String nodeIp;
		final long timestamp;
		final int versionNumber;
		
		Entry (String ip, int versionNumber, Date timestamp) {
			this.nodeIp = ip;
			this.versionNumber = versionNumber;
			this.timestamp = timestamp.getTime();
		}
		
		Entry (String ip, int versionNumber) {
			this(ip, versionNumber, new Date());
		}

		@Override
		public int compareTo(Entry o) {
			// The newest in front
			return Long.compare(o.timestamp, timestamp);
		}
		
		public Entry increase() {
			return new Entry(nodeIp, versionNumber + 1);
		}
	}
	
	private final static int MAX_ENTRIES = 10;
	
	private final List<Entry> entries;
	
	private Version(Collection<Entry> entries) {
		// Trimming to not exceed 10 entries - that would bloat the serialization
		this.entries = entries.stream().sorted().limit(MAX_ENTRIES).collect(toList());
	}
	
	public Version increment(DynamoNode atNode) {
		LinkedList<Entry> _entries = entries.stream()
			.filter(entry -> !entry.nodeIp.equals(atNode.getIp()))
			.collect(toCollection(LinkedList::new));
		
		Entry incremented = entries.stream()
			.filter(entry -> entry.nodeIp.equals(atNode.getIp()))
			.findFirst()
			.map(entry -> entry.increase())
			.orElse(new Entry(atNode.getIp(), 1));
		
		_entries.addFirst(incremented);
		
		return new Version(_entries);
	}
	
	private Map<String, Entry> entriesByNode() {
		return entries.stream().collect(toMap(entry -> entry.nodeIp, entry -> entry));
	}
	
	public Version merge(Version version) {
		Map<String, Entry> myEntries = entriesByNode();
		Map<String, Entry> otherEntries = version.entriesByNode();
		
		Map<String, Entry> highest = new HashMap<>(myEntries);
		otherEntries.entrySet().forEach(entry -> {
			if (!highest.containsKey(entry.getKey())
				|| highest.get(entry.getKey()).versionNumber < entry.getValue().versionNumber) {
				
				highest.put(entry.getKey(), entry.getValue());
			}
		});
		
		return new Version(highest.values());
	}
	
}
