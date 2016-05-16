import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.binary.Base64;
import org.glassfish.jersey.client.ClientProperties;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

import static org.junit.Assert.assertThat;

import static java.util.stream.Collectors.toList;

import sk.fiit.dps.team11.core.Version;
import sk.fiit.dps.team11.core.Version.Comp;
import sk.fiit.dps.team11.models.ByteArray;
import sk.fiit.dps.team11.models.GetResponse;
import sk.fiit.dps.team11.models.PutRequest;
import sk.fiit.dps.team11.models.PutResponse;

public class TestStorage {
	
	private static final String URL = "http://localhost:8080/";
	// private static final String URL = "http://10.32.0.2:8080/";

	private static final Logger LOGGER = LoggerFactory.getLogger(TestStorage.class);
	
	public final static List<byte[]> KEYS = Stream.of("SOME_KEY", "longer-key", "   -.,.,/-*/")
		.map(String::getBytes).collect(toList());
	
	public final static List<byte[]> VALUES = Stream.of("VALUE1",
			"Very long: ".concat(Collections.nCopies(5000, 'a').toString()),
			"0")
		.map(String::getBytes).collect(toList());
	
	private final static ObjectMapper MAPPER = new ObjectMapper();
	
	private final static int MIN_NUM_RW = 1;
	
	private WebTarget target;
	
	@BeforeClass
	public static void onlyRunIfServerRuns() {
		try {
			Response response = ClientBuilder.newClient()
				.property(ClientProperties.CONNECT_TIMEOUT, 100)
				.property(ClientProperties.READ_TIMEOUT, 100)
				.target(URL + "ping")
				.request()
				.get();
			Assume.assumeThat(response.getStatusInfo().getStatusCode(), equalTo(Status.OK.getStatusCode()));
		} catch (Exception e) {
			Assume.assumeNoException(e);
		}
	}
	
	@Before
	public void prepare() {
		target = ClientBuilder.newClient()
			.property(ClientProperties.CONNECT_TIMEOUT, 5000)
			.property(ClientProperties.READ_TIMEOUT, 5000)
			.target(URL + "storage");
	}
	
	private PutResponse put(PutRequest request) throws Exception {
		Response response = target.request().buildPut(Entity.entity(request, MediaType.APPLICATION_JSON)).invoke();
		
		PutResponse resp = response.readEntity(PutResponse.class);
		
		return resp;
	}
	
	private GetResponse get(byte[] key) throws Exception {
		Response response = target
			.path(Base64.encodeBase64String(key))
			.queryParam("minNumWrites", MIN_NUM_RW)
			.request()
			.get();
		
		GetResponse resp = response.readEntity(GetResponse.class);
		
		LOGGER.info("Get response: {}", MAPPER.writeValueAsString(resp));
		
		return resp;
	}

	@Test
	public void testPutAfterGet() throws Exception {
		
		byte[] key = KEYS.get(0);
		byte[] value = VALUES.get(0);
		
		// Retrieve current version
		GetResponse cur = get(key);
		
		// Put new value
		PutResponse resp = put(new PutRequest(key, value, cur.getValue().getVersion(), MIN_NUM_RW));
		
		assertThat(resp.isSuccess(), equalTo(true));
		
	}

	@Test
	public void testPutWithStaleVersion() throws Exception {
		
		byte[] key = KEYS.get(1);
		byte[] value = VALUES.get(1);
		
		// Retrieve current version
		GetResponse cur = get(key);
		// Put new value
		put(new PutRequest(key, value, cur.getValue().getVersion(), MIN_NUM_RW));

		PutResponse resp = put(new PutRequest(key, value, Version.INITIAL, MIN_NUM_RW));
		
		assertThat(resp.isSuccess(), equalTo(false));
		
	}

	@Test
	public void testGetAfterSuccessfulPut() throws Exception {
		
		byte[] key = KEYS.get(2);
		byte[] value = VALUES.get(2);
		
		// Retrieve current version
		GetResponse cur = get(key);
		
		// Put new value
		put(new PutRequest(key, value, cur.getValue().getVersion(), MIN_NUM_RW));
		
		// Retrieve the new version
		GetResponse resp = get(key);
		
		assertThat(resp.getValue().getValues(), equalTo(Arrays.asList(new ByteArray(value))));
		
	}

	@Test
	public void testGetAfterConcurrentPuts() throws Exception {
		
		byte[] key = KEYS.get(1);
		byte[] value1 = VALUES.get(1);
		byte[] value2 = VALUES.get(2);
		
		// Retrieve current version
		GetResponse cur = get(key);
		
		// Put new value two times concurrently
		Version version1 = cur.getValue().getVersion().increment(TestVersion.NODE_1);
		Version version2 = cur.getValue().getVersion().increment(TestVersion.NODE_2);
		put(new PutRequest(key, value1, version1, MIN_NUM_RW));
		put(new PutRequest(key, value2, version2, MIN_NUM_RW));
		
		// Retrieve the new version
		GetResponse resp = get(key);
		
		assertThat(resp.getValue().getValues(), contains(Arrays.asList(
			equalTo(new ByteArray(value1)),
			equalTo(new ByteArray(value2)))));
		
		assertThat(Version.compare(resp.getValue().getVersion(), version1), equalTo(Comp.FIRST_NEWER));
		assertThat(Version.compare(resp.getValue().getVersion(), version2), equalTo(Comp.FIRST_NEWER));
		
	}
	
}
