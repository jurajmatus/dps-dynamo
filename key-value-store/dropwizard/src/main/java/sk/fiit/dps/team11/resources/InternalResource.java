package sk.fiit.dps.team11.resources;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.ClientBuilder;

import org.apache.commons.codec.binary.Base64;
import org.glassfish.jersey.client.ClientProperties;
import org.javatuples.Pair;

import com.sleepycat.je.DatabaseException;

import static java.util.stream.Collectors.toMap;

import sk.fiit.dps.team11.config.TopConfiguration;
import sk.fiit.dps.team11.core.DatabaseAdapter;
import sk.fiit.dps.team11.core.MetricsAdapter;
import sk.fiit.dps.team11.core.Topology;
import sk.fiit.dps.team11.models.VersionedValue;

@Path("/internal")
public class InternalResource {
	
	public static class NodeDump extends HashMap<String, VersionedValue> {
		private static final long serialVersionUID = -2237772615396856586L;
	}
	
	@Inject
	TopConfiguration conf;
	
	@Inject
	Topology topology;
	
	@Inject
	MetricsAdapter metrics;
	
	@Inject
	DatabaseAdapter db;
	
	private <T> Stream<Pair<String, T>> aggregate(String path, Class<T> expectedClass) {
		String urlTpl = "http://%s:8080/internal/" + path;
		
		return topology.allNodes()
			.filter(node -> !node.equals(topology.self()))
			.map(node -> new Pair<>(node.getIp(),
				ClientBuilder.newClient()
					.property(ClientProperties.CONNECT_TIMEOUT, 1000)
					.property(ClientProperties.READ_TIMEOUT, 1000)
					.target(String.format(urlTpl, node.getIp()))
					.request()
					.get()
					.readEntity(expectedClass)));
	}
	
	@GET
	@Path("dump/my")
	public NodeDump dump() throws DatabaseException {
		NodeDump all = new NodeDump();
		db.forEach((key, val) -> {
			all.put(Base64.encodeBase64String(key), val);
		});
		return all;
	}
	
	@GET
	@Path("dump/all")
	public Map<String, NodeDump> dumpAll() throws DatabaseException {
		return Stream.concat(
				Stream.of(new Pair<>(topology.self().getIp() ,dump())),
				aggregate("dump/all", NodeDump.class)
		).collect(toMap(pair -> pair.getValue0(), pair -> pair.getValue1()));
	}
	
	@GET
	@Path("clear/my")
	public boolean clear() {
		try {
			db.clear();
			return true;
		} catch (DatabaseException e) {
			return false;
		}
	}
	
	@GET
	@Path("clear/all")
	public boolean clearAll() throws DatabaseException {
		return clear()
			&& aggregate("clear/all", Boolean.class)
				.allMatch(pair -> pair.getValue1().booleanValue());
	}
	
}
