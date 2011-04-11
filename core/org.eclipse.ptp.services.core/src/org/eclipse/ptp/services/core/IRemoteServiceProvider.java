package org.eclipse.ptp.services.core;

/**
 * Additional interface that must be implemented by service providers that operate on remote hosts
 * @since 2.1
 */
public interface IRemoteServiceProvider {
	public void changeRemoteInformation(String remoteConnectionName, String location);

}
