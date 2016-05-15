package sk.fiit.dps.team11.core;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.InetAddresses;

import sk.fiit.dps.team11.KeyValueStoreService;

public class NetworkInterfaceUtility {

	private static final Logger LOGGER = LoggerFactory.getLogger(KeyValueStoreService.class);
	
	public NetworkInterfaceUtility() {
		;
	}
	
	public static String getInterfaceIpAddress(String netwInterfaceName) {
		
		NetworkInterface interf = null;
		String ip = null;
		
		try {
			interf = NetworkInterface.getByName(netwInterfaceName);
		}
		catch (SocketException e) {
			LOGGER.error("Cannot get IP address of network interface {}", netwInterfaceName, e);
		}
	
		if (interf == null) {
			//If interface ethwe0 does not exists, return localhost
			try {
				ip = InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e) {
				LOGGER.error("Cannot get IP address of localhost network interface.", e);
			}
		}
		else {
			Enumeration<InetAddress> addresses = interf.getInetAddresses();
		
			for (InetAddress inetAddress : Collections.list(addresses)) {
				try {
					InetAddresses.forString(inetAddress.getHostAddress());
				} catch (IllegalArgumentException e) {
					LOGGER.warn("Invalid IP address '{}'. Continue to next.", inetAddress.getHostAddress());
					continue;
				}
				
				ip = inetAddress.getHostAddress();
			}
        }
		
        if (ip == null) {
        	LOGGER.error("Got no IP address from interfaces\n");
        	System.exit(-1);
        }
        
        return ip;
        
	}
	
}
