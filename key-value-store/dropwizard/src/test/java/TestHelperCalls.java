import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.Random;

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

import static org.hamcrest.Matchers.equalTo;

import sk.fiit.dps.team11.core.Version;
import sk.fiit.dps.team11.models.GetRequest;
import sk.fiit.dps.team11.models.GetResponse;
import sk.fiit.dps.team11.models.PutRequest;
import sk.fiit.dps.team11.models.PutResponse;

public class TestHelperCalls {
	
	// private static final String URL = "http://localhost:8080/";
	private static final String URL = "http://10.32.0.3:8080/";
	
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
		Response response = target.request()
				.buildPut(Entity.entity(request, MediaType.APPLICATION_JSON))
				.invoke();
		
		PutResponse resp = response.readEntity(PutResponse.class);
		
		return resp;
	}
	
	private GetResponse get(byte[] key) throws Exception {
		GetRequest request = new GetRequest(Base64.encodeBase64String(key), MIN_NUM_RW);
		
		Response response = target.request()
			.buildPost(Entity.entity(request, MediaType.APPLICATION_JSON))
			.invoke();
		
		GetResponse resp = response.readEntity(GetResponse.class);
		
		return resp;
	}
	
	private void putToClipboard(String text) {
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
	}

	@Test
	public void testMakeConflict() throws Exception {
		byte[] key = "conflicted-key".getBytes();
		
		// Retrieve current version
		GetResponse cur = get(key);
		
		// Put new value two times concurrently
		Version version1 = cur.getValue().getVersion().increment(TestVersion.NODE_1);
		Version version2 = cur.getValue().getVersion().increment(TestVersion.NODE_2);
		
		put(new PutRequest(key, "Value 1 of conflict".getBytes(), version1, MIN_NUM_RW));
		put(new PutRequest(key, "Some other value with different length to see the difference".getBytes(),
				version2, MIN_NUM_RW));
		
		putToClipboard(new String(key));
	}

	@Test
	public void testRandomPut() throws Exception {
		
		Random r = new Random();
		byte[] key = new byte[10];
		r.nextBytes(key);
		
		byte[] value = new byte[25];
		r.nextBytes(value);
		
		put(new PutRequest(key, value, Version.INITIAL, MIN_NUM_RW));
		
	}
	
}
