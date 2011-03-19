package org.eclipse.ptp.rm.jaxb.core.utils;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;

public class RemoteServicesDelegate implements IJAXBNonNLSConstants {
	private final IRemoteServices remoteServices;
	private final IRemoteServices localServices;
	private final IRemoteConnectionManager remoteConnectionManager;
	private final IRemoteConnectionManager localConnectionManager;
	private final IRemoteConnection remoteConnection;
	private final IRemoteConnection localConnection;
	private final IRemoteFileManager remoteFileManager;
	private final IRemoteFileManager localFileManager;

	public RemoteServicesDelegate(String remoteServicesId, String remoteConnectionName) {
		localServices = PTPRemoteCorePlugin.getDefault().getDefaultServices();
		assert (localServices != null);
		localConnectionManager = localServices.getConnectionManager();
		assert (localConnectionManager != null);
		/*
		 * Since it's a local service, it doesn't matter which parameter is
		 * passed
		 */
		localConnection = localConnectionManager.getConnection(ZEROSTR);
		assert (localConnection != null);
		localFileManager = localServices.getFileManager(localConnection);
		assert (localFileManager != null);
		remoteServices = PTPRemoteCorePlugin.getDefault().getRemoteServices(remoteServicesId, new NullProgressMonitor());
		assert (null != remoteServices);
		remoteConnectionManager = remoteServices.getConnectionManager();
		assert (null != remoteConnectionManager);
		remoteConnection = remoteConnectionManager.getConnection(remoteConnectionName);
		assert (null != remoteConnection);
		remoteFileManager = remoteServices.getFileManager(remoteConnection);
		assert (null != remoteFileManager);
	}

	public IRemoteConnection getLocalConnection() {
		return localConnection;
	}

	public IRemoteConnectionManager getLocalConnectionManager() {
		return localConnectionManager;
	}

	public IRemoteFileManager getLocalFileManager() {
		return localFileManager;
	}

	public IRemoteServices getLocalServices() {
		return localServices;
	}

	public IRemoteConnection getRemoteConnection() {
		return remoteConnection;
	}

	public IRemoteConnectionManager getRemoteConnectionManager() {
		return remoteConnectionManager;
	}

	public IRemoteFileManager getRemoteFileManager() {
		return remoteFileManager;
	}

	public IRemoteServices getRemoteServices() {
		return remoteServices;
	}

}
