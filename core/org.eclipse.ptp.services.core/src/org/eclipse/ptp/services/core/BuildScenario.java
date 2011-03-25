package org.eclipse.ptp.services.core;

/**
 * Class for build information that will be mapped to a specific service configuration. Utility methods for reading and writing
 * the information to an IConfiguration (a .cproject file) are also provided.
 * @since 2.1
 */
public class BuildScenario {
	final String syncProvider;
	final String remoteConnectionName;
	final String location;
	
	/**
	 * Create a new build scenario
	 * 
	 * @param sp
	 *           Name of sync provider
	 * @param rcn
	 * 			 Name of remote connection
	 * @param l
	 * 			 Location (directory) on remote host
	 */
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