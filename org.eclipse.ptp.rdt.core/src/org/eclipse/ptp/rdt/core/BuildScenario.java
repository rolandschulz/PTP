package org.eclipse.ptp.rdt.core;

import org.eclipse.ptp.remote.core.IRemoteConnection;

/**
 * Class for build information that will be mapped to a specific service configuration. Utility methods for reading and writing
 * the information to an IConfiguration (a .cproject file) are also provided.
 * @since 3.1
 */
public class BuildScenario {
	final String syncProvider;
	final IRemoteConnection remoteConnection;
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
	public BuildScenario(String sp, IRemoteConnection rc, String l) {
		syncProvider = sp;
		remoteConnection = rc;
		location = l;
	}

	public String getSyncProvider() {
		return syncProvider;
	}

	public IRemoteConnection getRemoteConnection() {
		return remoteConnection;
	}

	public String getLocation() {
		return location;
	}
}