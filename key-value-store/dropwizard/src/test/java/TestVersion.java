import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Random;

import org.hamcrest.Matchers;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.InetAddresses;

import sk.fiit.dps.team11.core.DynamoNode;
import sk.fiit.dps.team11.core.Version;

public class TestVersion {

	public static DynamoNode NODE_1 = new DynamoNode("128.192.156.20", 100L);
	public static DynamoNode NODE_2 = new DynamoNode("156.12.39.10", 100000L);
	
	public static ObjectMapper MAPPER = new ObjectMapper();
	
	@Test
	public void compareShouldReturnEqual() throws Exception {
		Version v1 = Version.INITIAL.increment(NODE_1);
		Version v2 = Version.INITIAL.increment(NODE_1);
		
		System.out.println(MAPPER.writeValueAsString(v1));
		
		assertThat(Version.compare(v1, v2), equalTo(Version.Comp.EQUAL));
	}
	
	@Test
	public void compareShouldReturnConcurrent() throws Exception {
		Version v1 = Version.INITIAL.increment(NODE_1);
		Version v2 = Version.INITIAL.increment(NODE_2);
		
		System.out.println(MAPPER.writeValueAsString(v1));
		
		assertThat(Version.compare(v1, v2), equalTo(Version.Comp.CONCURRENT));
	}
	
	@Test
	public void compareShouldReturnSecondNewer() throws Exception {
		Version v1 = Version.INITIAL.increment(NODE_1);
		Version v2 = v1.increment(NODE_2);
		
		System.out.println(MAPPER.writeValueAsString(v1));
		
		assertThat(Version.compare(v1, v2), equalTo(Version.Comp.SECOND_NEWER));
	}
	
	@Test
	public void compareShouldReturnFirstNewer() throws Exception {
		Version v2 = Version.INITIAL.increment(NODE_1);
		Version v1 = v2.increment(NODE_1);
		
		System.out.println(MAPPER.writeValueAsString(v1));
		
		assertThat(Version.compare(v1, v2), equalTo(Version.Comp.FIRST_NEWER));
	}
	
	@Test
	public void compareShouldReturnNewerForMerged() throws Exception {
		Version v1 = Version.INITIAL.increment(NODE_1);
		Version v2 = Version.INITIAL.increment(NODE_2);
		
		Version merged = v1.merge(v2);
		
		System.out.println(MAPPER.writeValueAsString(merged));
		
		assertThat(Version.compare(merged, v1), equalTo(Version.Comp.FIRST_NEWER));
	}
	
	@Test
	public void serializationCycle() throws Exception {
		Version v1 = Version.INITIAL.increment(NODE_1);
		Version v2 = Version.INITIAL.increment(NODE_2);
		Version merged = v1.merge(v2);
		
		String ser1 = MAPPER.writeValueAsString(merged);
		Version deser = MAPPER.readValue(ser1, Version.class);
		String ser2 = MAPPER.writeValueAsString(deser);

		assertThat(ser2.length(), Matchers.greaterThan(0));
		assertThat(ser1, equalTo(ser2));
	}
	
	@Test
	public void newVersionShouldBeTrimmedTo10Entries() throws Exception {
		Version v1 = Version.INITIAL;
		Version v2 = Version.INITIAL;
		Random r = new Random();
		
		for (int i = 0; i < 10; i++) {
			v1 = v1.increment(new DynamoNode(InetAddresses.fromInteger(r.nextInt()).getHostAddress(), r.nextLong()));
			v2 = v2.increment(new DynamoNode(InetAddresses.fromInteger(r.nextInt()).getHostAddress(), r.nextLong()));
		}
		
		Version merged = v1.merge(v2);
		Field _entries = Version.class.getDeclaredField("entries");
		_entries.setAccessible(true);
		Object entries = _entries.get(merged);
		
		assertThat(entries instanceof Collection<?>, is(true));
		assertThat(((Collection<?>) entries).size(), lessThan(11));
	}
	
}
