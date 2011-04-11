package org.eclipse.ptp.services.core;

/*
 * Class for build information that will be mapped to a specific service configuration.
 */
public class BuildScenario {
	final String syncProvider;
	final String remoteConnectionName;
	final String location;
	
	public BuildScenario(String sp, String rcn, String l) {
		syncProvider = sp;
		remoteConnectionName = rcn;
		location = l;
	}

	public String getSyncProvider() {
		return syncProvider;
	}

	public String getRemoteConnectionName() {
		return remoteConnectionName;
	}

	public String getLocation() {
		return location;
	}
}