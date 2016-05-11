package sk.fiit.dps.team11.core;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.inject.Inject;

import sk.fiit.dps.team11.core.Version.Comp;
import sk.fiit.dps.team11.models.ByteArray;
import sk.fiit.dps.team11.models.VersionedValue;

public class VersionResolution {
	
	@Inject
	private Topology topology;
	
	public Version increment(Version version) {
		return version.increment(topology.self());
	}
	
	public VersionedValue resolve(VersionedValue oldValue, VersionedValue newValue, boolean incrementVersion,
		Consumer<VersionedValue> withChanged, Consumer<Boolean> withUnchanged) {
		
		Comp comparison = Version.compare(oldValue.getVersion(), newValue.getVersion());
		
		switch (comparison) {
		case FIRST_NEWER:
			withUnchanged.accept(false);
			return oldValue;
			
		case EQUAL:
			withUnchanged.accept(true);
			return oldValue;
			
		case CONCURRENT:
			
			Version newVersion = oldValue.getVersion().merge(newValue.getVersion());
			if (incrementVersion) {
				newVersion = increment(newVersion);
			}
			
			List<ByteArray> allValues = Stream.concat(
					oldValue.getValues().stream(), newValue.getValues().stream())
				.collect(toList());
			
			newValue = new VersionedValue(newVersion, allValues);
			
			withChanged.accept(newValue);
			return newValue;
			
		case SECOND_NEWER:
			
			if (incrementVersion) {
				newValue = new VersionedValue(increment(newValue.getVersion()), newValue.getValues());
			}
			
			withChanged.accept(newValue);
			return newValue;

		default:
			return newValue;
		}
		
	}
	
}
