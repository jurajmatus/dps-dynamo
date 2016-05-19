package sk.fiit.dps.team11.core;

import java.util.function.BiFunction;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;

public class MetricsAdapter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MetricsAdapter.class);
	
	@Inject
	private MetricRegistry metrics;
	
	@Inject
	private Topology topology;
	
	private String getSuffix() {
		String suffix = "";
		String[] ipChunks = topology.myIpAddr.split(".");
		if (ipChunks.length > 0) {
			suffix = ".node" + ipChunks[ipChunks.length - 1];
		}
		return suffix;
	}
	
	public <T> T get(BiFunction<MetricRegistry, String, T> getter, String name) {
		String className = "";
		try {
			className = Thread.currentThread().getStackTrace()[1].getClassName() + ".";
		} catch (Exception e) {}
		
		String mname = className + name + getSuffix();
		LOGGER.info("Creating metric with name {}", mname);
		
		return getter.apply(metrics, mname);
	}

}
