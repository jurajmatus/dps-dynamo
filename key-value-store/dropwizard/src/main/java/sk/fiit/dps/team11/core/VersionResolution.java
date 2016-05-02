package sk.fiit.dps.team11.core;

import javax.inject.Inject;

import sk.fiit.dps.team11.models.VersionedValue;

public class VersionResolution {

	public static enum MergeMode {
		AGGRESIVE,
		DEFENSIVE
	}
	
	@Inject
	private Topology topology;
	
	public Version increment(Version version) {
		return version.increment(topology.self());
	}
	
	public VersionedValue merge(MergeMode mode, VersionedValue value1, VersionedValue value2) {
		// TODO
		return value1;
	}
	
}
