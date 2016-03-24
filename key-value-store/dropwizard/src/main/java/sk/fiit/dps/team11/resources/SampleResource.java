package sk.fiit.dps.team11.resources;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;

import sk.fiit.dps.team11.config.TopConfiguration;
import sk.fiit.dps.team11.models.Sample;

@Path("/sample")
public class SampleResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(SampleResource.class);
	
	@Inject
	TopConfiguration conf;
	
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	@Timed(name = "sample")
	public Sample justTest() {
		LOGGER.info(conf == null ? "Configuration wasn't injected" : "Configuration was properly injected");
		return new Sample("Hello! The application works.");
	}
	
}
