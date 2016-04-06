package sk.fiit.dps.team11.models;

import java.net.InetAddress;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/*[
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
]*/

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConsulHealth {

	public static class Node {
		
		@JsonProperty("Address")
		public String address;
		
		@JsonProperty("Node")
		public String name;
		
	}
	
	public static class Check {
		
		@JsonProperty("Status")
		public String status;
		
	}
	
	@JsonProperty("Node")
	private Node node;
	
	@JsonProperty("Checks")
	private List<Check> checks;
	
	public boolean isReachable() {
		if (!checks.stream().allMatch(c -> c.status.equalsIgnoreCase("passing"))) {
			return false;
		}
		try {
			if (InetAddress.getByName(node.address).isReachable(1000)) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}
	
	public String getAddress() {
		return node.address;
	}
	
}
