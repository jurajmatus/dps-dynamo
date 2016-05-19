package sk.fiit.dps.team11.core;

import java.util.function.BiFunction;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.codahale.metrics.MetricRegistry;

public class MetricsAdapter {
	
	@Inject
	private MetricRegistry metrics;
	
	@Inject
	private Topology topology;
	
	private String suffix = "";
	
	@PostConstruct
	public void init() {
		String[] ipChunks = topology.myIpAddr.split(".");
		if (ipChunks.length > 0) {
			suffix = "." + ipChunks[ipChunks.length - 1];
		}
	}
	
	public <T> T get(BiFunction<MetricRegistry, String, T> getter, String name) {
		String className = "";
		try {
			className = Thread.currentThread().getStackTrace()[1].getClassName() + ".";
		} catch (SecurityException e) {
			
		}
		
		return getter.apply(metrics, className + name + suffix);
	}

}
