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

import java.net.*;
import java.io.InputStream;
import java.io.StringWriter;
import org.json.*;
import org.apache.commons.io.IOUtils;
import javax.json.*;

@Path("/check_connectivity")
public class CheckConnectivityResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(CheckConnectivityResource.class);
	
	@Inject
	TopConfiguration conf;
	
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	@Timed(name = "check_connectivity")
	public Sample check() {
		LOGGER.info(conf == null ? "Configuration wasn't injected" : "Configuration was properly injected");

		String healthResponse = "";
		String hostname = "";
		try {
			hostname = InetAddress.getLocalHost().getHostName();
		}
		catch (UnknownHostException e) {
			return new Sample(e.toString());
		}

		System.out.println("hostname: " + hostname );

		healthResponse = healthResponse + hostname;

		String ip = System.getenv("NODE1_IP");
		String urlStr = "http://" + ip + ":8500/v1/health/service/dynamo";
		
		System.out.println("consul health request: " + ip );

		URLConnection connection;
		try {
			connection = new URL(urlStr).openConnection();
		}
		catch (Exception e) {
			return new Sample(e.toString());
		}

		/*URL url = new URL("http://10.0.0.8:8500/v1/health/service/dynamo");
		HttpURLConnection connection = new HttpURLConnection();
		connection.setRequestMethod("GET");*/

		InputStream response;
		try {
			response = connection.getInputStream();
		}
		catch (java.io.IOException e) {
			return new Sample(e.toString());
		}

		String responseStr;
		try {
			StringWriter writer = new StringWriter();
			IOUtils.copy(response, writer);
			responseStr = writer.toString();
		}
		catch (java.io.IOException e) { 
			return new Sample(e.toString());
		}
	
		System.out.println("response: '" + responseStr + "'");
		JSONArray array = new JSONArray(responseStr);


		for (int i = 0; i < array.length(); i++) {
			JSONObject jsonNode = array.getJSONObject(i).getJSONObject("Node");
			JSONObject jsonServ = array.getJSONObject(i).getJSONArray("Checks").getJSONObject(0);
			JSONObject jsonSerf = array.getJSONObject(i).getJSONArray("Checks").getJSONObject(1);
		
			String servStatus = jsonServ.getString("Status");
			String serfStatus = jsonSerf.getString("Status");
			String nodeIpStr = jsonNode.getString("Address");
			System.out.println("servstatus: " + servStatus + "\nserfstatus: " + serfStatus + "\nnodeIp: " + nodeIpStr);
				
			if ( (serfStatus.compareToIgnoreCase("passing") == 0) && (servStatus.compareToIgnoreCase("passing") == 0) ) {
				//ping node
				InetAddress nodeInet;
				try {
					nodeInet = InetAddress.getByName(nodeIpStr);
				}
				catch (java.net.UnknownHostException e) {
					return new Sample(e.toString());
				}

				try {
					if ( nodeInet.isReachable(1000) ) {
						healthResponse = healthResponse + " -- IP address: " + nodeIpStr + " reachable";
					}
				}
				catch (java.io.IOException e) {
					return new Sample(e.toString()); 
				}
			}
			else {
				System.out.println("continue");
			}
		}

		return new Sample(healthResponse);
	}
	
}