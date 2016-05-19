package sk.fiit.dps.team11.workers;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.codahale.metrics.MetricRegistry;

import sk.fiit.dps.team11.core.DatabaseAdapter;
import sk.fiit.dps.team11.core.MetricsAdapter;

public class GlobalMetricsWorker {

	@Inject
	private ScheduledExecutorService execService;
	
	@Inject
	private MetricsAdapter metrics;
	
	@Inject
	private DatabaseAdapter db;
	
	private final static int METRIC_INTERVAL_MILLISECONDS = 500;
	
	@PostConstruct
	private void init() {
		execService.scheduleAtFixedRate(this::tick, METRIC_INTERVAL_MILLISECONDS / 2,
				METRIC_INTERVAL_MILLISECONDS, TimeUnit.MILLISECONDS);
	}
	
	private void tick() {
		metrics.get(MetricRegistry::histogram, "database-num-entries").update(db.numEntries());
	}
	
}
