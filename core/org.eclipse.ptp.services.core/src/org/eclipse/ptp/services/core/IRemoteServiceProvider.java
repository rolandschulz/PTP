package org.eclipse.ptp.services.core;

import org.eclipse.ptp.remote.core.IRemoteConnection;

/**
 * Additional interface that must be implemented by service providers that operate on remote hosts
 * @since 2.1
 */
public interface IRemoteServiceProvider {
	public void changeRemoteInformation(IRemoteConnection remoteConnection, String location);

}
