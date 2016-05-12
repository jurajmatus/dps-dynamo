package sk.fiit.dps.team11.workers;

import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kjetland.dropwizard.activemq.ActiveMQSender;

import sk.fiit.dps.team11.annotations.MQSender;
import sk.fiit.dps.team11.config.TopConfiguration;
import sk.fiit.dps.team11.core.DynamoNode;
import sk.fiit.dps.team11.core.RequestState;
import sk.fiit.dps.team11.core.RequestStates;
import sk.fiit.dps.team11.core.Topology;
import sk.fiit.dps.team11.workers.WrappedMethodWorker.MQListener;

/*
[
  {
    "Node": {
      "Node": "foobar",
      "Address": "10.1.10.12",
      "TaggedAddresses": {
        "wan": "10.1.10.12"
      }
    },
    "Service": {
      "ID": "redis",
      "Service": "redis",
      "Tags": null,
      "Address": "10.1.10.12",
      "Port": 8000
    },
    "Checks": [
      {
        "Node": "foobar",
        "CheckID": "service:redis",
        "Name": "Service 'redis' check",
        "Status": "passing",
        "Notes": "",
        "Output": "",
        "ServiceID": "redis",
        "ServiceName": "redis"
      },
      {
        "Node": "foobar",
        "CheckID": "serfHealth",
        "Name": "Serf Health Status",
        "Status": "passing",
        "Notes": "",
        "Output": "",
        "ServiceID": "",
        "ServiceName": ""
      }
    ]
  }
]
*/

public class CheckTopologyChangeWorker {

	private static final Logger LOGGER = LoggerFactory.getLogger(CheckTopologyChangeWorker.class);
	
	@Inject
	RequestStates states;
	
	@Inject
	ScheduledExecutorService execService;
	
	@Inject
	Topology topology;
	
	@Inject
	TopConfiguration conf;
	
	@MQSender(topic = "find-for-key")
	ActiveMQSender replicaFinderWorker;
	
	private final class Checker implements Runnable {
		
		private final UUID uuid;
		
		private final ObjectMapper MAPPER = new ObjectMapper();
		
		public Checker(UUID uuid) {
			this.uuid = uuid;
		}
		
		private void checkTopologyChange(RequestState<?> state) {
			if (state.isDone()) {
				return;
			}
			
			//SortedSet<String> activeDynamoNodes = new TreeSet<String>();
			SortedSet<DynamoNode> activeDynamoNodes = new TreeSet<DynamoNode>();

			WebTarget target = ClientBuilder.newClient()
					.target("http://consul-server:8500/v1/health/service/dynamo");

			Response response = target.request().get();
			
			JsonNode responseJson;
			try {
				responseJson = MAPPER.readTree(response.readEntity(String.class));
			if (responseJson.isArray()) {
				responseJson.forEach(node -> {

					JsonNode n = node.get("Node");
					JsonNode addr = n.get("Address");
					String ipAddr = addr.textValue();
					
					JsonNode service = node.get("Service");
					Long nodePosition = Long.getLong(service.get("ID").textValue());
					
					JsonNode checks = node.get("Checks");
					JsonNode servReachability = checks.get(0).get("Status");
					JsonNode serfReachability = checks.get(1).get("Status");
					
					if ( (servReachability.textValue().compareToIgnoreCase("passing") == 0) && 
						(serfReachability.textValue().compareToIgnoreCase("passing") == 0) ) {
						activeDynamoNodes.add(new DynamoNode(ipAddr, nodePosition));
					}
				});
			}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			topology.compareTopology(activeDynamoNodes);
			
		}

		@Override
		public void run() {
			states.withState(uuid, RequestState.class, this::checkTopologyChange, () -> {
				// No problem if state is not found - it means it's completed
				// before timeout
			});
		}
		
	}

	@MQListener(queue = "topology-timeout-check")
	public void registerCheck(UUID requestId) {
		execService.schedule(new Checker(requestId),
			conf.getReliability().getTopologyChangeTimeoutMillis(), TimeUnit.MILLISECONDS);
	}

}
