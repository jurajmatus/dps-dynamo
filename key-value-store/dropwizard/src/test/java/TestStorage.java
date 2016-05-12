import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;
import org.glassfish.jersey.client.ClientProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;

import com.fasterxml.jackson.databind.ObjectMapper;

import sk.fiit.dps.team11.core.Version;
import sk.fiit.dps.team11.models.GetResponse;
import sk.fiit.dps.team11.models.PutRequest;
import sk.fiit.dps.team11.models.PutResponse;

public class TestStorage {
	
	public final static List<byte[]> KEYS = Stream.of("SOME_KEY", "longer-key", "   -.,.,/-*/")
		.map(String::getBytes).collect(toList());
	
	public final static List<byte[]> VALUES = Stream.of("VALUE1",
			"Very long: ".concat(Collections.nCopies(5000, 'a').toString()),
			"0")
		.map(String::getBytes).collect(toList());
	
	private final static ObjectMapper MAPPER = new ObjectMapper();
	
	private WebTarget target;
	
	@Before
	public void prepare() {
		target = ClientBuilder.newClient()
			.property(ClientProperties.CONNECT_TIMEOUT, 2000)
			.property(ClientProperties.READ_TIMEOUT, 2000)
			.target("http://localhost:8080/storage");
	}

	@Ignore @Test
	public void testPut() throws Exception {
		
		PutRequest entity = new PutRequest(KEYS.get(0), VALUES.get(0), Version.INITIAL, 1);
		
		Response response = target.request().buildPut(Entity.entity(entity, MediaType.APPLICATION_JSON)).invoke();
		
		PutResponse resp = response.readEntity(PutResponse.class);
		
		MAPPER.writeValue(System.out, resp);
		
		assertThat(resp.isSuccess(), is(true));
		
	}

	@Ignore @Test
	public void testGet() throws Exception {
		
		Response response = target
			.path(Base64.encodeBase64String(KEYS.get(0)))
			.queryParam("minNumWrites", 1)
			.request()
			.get();
		
		GetResponse resp = response.readEntity(GetResponse.class);
		
		MAPPER.writeValue(System.out, resp);
		
	}
	
}
