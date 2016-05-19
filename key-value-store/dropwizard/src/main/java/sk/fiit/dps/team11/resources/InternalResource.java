package sk.fiit.dps.team11.resources;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;

import org.apache.commons.codec.binary.Base64;
import org.glassfish.jersey.client.ClientProperties;
import org.javatuples.Pair;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import sk.fiit.dps.team11.config.TopConfiguration;
import sk.fiit.dps.team11.core.DatabaseAdapter;
import sk.fiit.dps.team11.core.MetricsAdapter;
import sk.fiit.dps.team11.core.Topology;
import sk.fiit.dps.team11.models.VersionedValue;

@Path("/internal")
public class InternalResource {
	
	@Inject
	TopConfiguration conf;
	
	@Inject
	Topology topology;
	
	@Inject
	MetricsAdapter metrics;
	
	@Inject
	DatabaseAdapter db;
	
	private final static ObjectWriter MAPPER = new ObjectMapper().writerWithDefaultPrettyPrinter();
	
	private <T> Stream<Pair<String, String>> aggregate(String path) {
		String urlTpl = "http://%s:8080/internal/" + path;
		
		return topology.allNodes()
			.filter(node -> !node.equals(topology.self()))
			.map(node -> {
				try {
					return new Pair<>(node.getIp(),
						ClientBuilder.newClient()
							.property(ClientProperties.CONNECT_TIMEOUT, 1000)
							.property(ClientProperties.READ_TIMEOUT, 1000)
							.target(String.format(urlTpl, node.getIp()))
							.request()
							.get()
							.readEntity(String.class));
				} catch (Exception e) {
					return null;
				}
			}).filter(pair -> pair != null);
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("dump/my")
	public String dump() {
		try {
			Map<String, VersionedValue> all = new HashMap<>((int) db.numEntries() * 3);
			db.forEach((key, val) -> {
				all.put(Base64.encodeBase64String(key), val);
			});
			return MAPPER.writeValueAsString(all);
		} catch (Exception e) {
			return e.toString();
		}
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("dump/all")
	public String dumpAll() throws Exception {
		StringBuilder all = new StringBuilder();
		all.append(dump());
		all.append("\n\n");
		
		aggregate("dump/my").forEach(pair -> {
			all.append(pair.getValue0());
			all.append("\n");
			all.append("######################\n");
			all.append(pair.getValue1());
		});
		
		return all.toString();
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("clear/my")
	public String clear() {
		try {
			db.clear();
			return "true";
		} catch (Exception e) {
			return e.toString();
		}
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("clear/all")
	public String clearAll() {
		StringBuilder all = new StringBuilder();
		all.append(clear());
		
		aggregate("clear/my").forEach(pair -> {
			all.append(pair.getValue0());
			all.append("\n");
			all.append("######################\n");
			all.append(pair.getValue1());
		});
		
		return all.toString();
	}
	
}
