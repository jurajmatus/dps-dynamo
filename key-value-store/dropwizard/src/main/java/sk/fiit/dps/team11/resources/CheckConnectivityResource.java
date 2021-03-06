package sk.fiit.dps.team11.resources;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import sk.fiit.dps.team11.models.ConsulHealth;

@Path("/check_connectivity")
public class CheckConnectivityResource {
	
	private static final ObjectMapper MAPPER = new ObjectMapper();
	
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	@Timed(name = "check_connectivity")
	public Response check() throws JsonProcessingException, IOException {
		
		ObjectNode ret = JsonNodeFactory.instance.objectNode();
		
		WebTarget target = ClientBuilder.newClient()
				.target("http://consul-server:8500/v1/health/service/dynamo");

		Response response = target.request().get();
		
		JsonNode responseJson = MAPPER.readTree(response.readEntity(String.class));
		if (responseJson.isArray()) {
			responseJson.forEach(node -> {
				ConsulHealth health = MAPPER.convertValue(node, ConsulHealth.class);
				ret.put(health.getAddress(), health.isReachable() ? "reachable" : "unreachable");
			});
		}

		return Response.ok().entity(ret).build();
	}
	
}
