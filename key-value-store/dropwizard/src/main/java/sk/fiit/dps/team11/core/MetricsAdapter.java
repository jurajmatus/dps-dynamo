package sk.fiit.dps.team11.core;

import java.util.function.BiFunction;

import javax.inject.Inject;

import com.codahale.metrics.MetricRegistry;

public class MetricsAdapter {
	
	@Inject
	private MetricRegistry metrics;
	
	@Inject
	private Topology topology;
	
	private String getSuffix() {
		try {
			return ".node" + topology.self().getIp().replace('.', '_');
		} catch (Exception e) {
			return "";
		}
	}
	
	public <T> T get(BiFunction<MetricRegistry, String, T> getter, String name) {
		return getter.apply(metrics, "dynamo." + name + getSuffix());
	}

}
