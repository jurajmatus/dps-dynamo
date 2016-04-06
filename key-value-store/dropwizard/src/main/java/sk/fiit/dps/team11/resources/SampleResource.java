package sk.fiit.dps.team11.resources;

import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kjetland.dropwizard.activemq.ActiveMQSender;
import com.sleepycat.je.DatabaseException;

import sk.fiit.dps.team11.annotations.MQSender;
import sk.fiit.dps.team11.models.Sample;
import sk.fiit.dps.team11.models.SimpleEntry;
import sk.fiit.dps.team11.providers.DatabaseAdapter;

@Path("/sample")
public class SampleResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(SampleResource.class);
	
	@Inject
	DatabaseAdapter db;
	
	@MQSender(topic = "sample")
	ActiveMQSender sender;
	
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	@Timed(name = "sample")
	public Sample justTest(@QueryParam("value") String value) {
		
		if (value != null) {
			LOGGER.info("Putting value {} into sample queue", value);
			sender.send(value);
		}
		
		return new Sample("Hello! The application works.");
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("put")
	public Response simplePut(SimpleEntry entry) throws DatabaseException {
		boolean success = db.put(entry.getKey().getBytes(), entry.getValue().getBytes());
		
		return success ? Response.ok().build() : Response.serverError().build();
	}
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("get")
	public JsonNode simpleGet(SimpleEntry entry) throws DatabaseException {
		Optional<byte[]> value = db.get(entry.getKey().getBytes());
		
		ObjectNode ret = JsonNodeFactory.instance.objectNode();
		ret.put("present", value.isPresent());
		if (value.isPresent()) {
			ret.put("value", value.get());
		}
		
		return ret;
	}
	
}
