package org.eclipse.ptp.rdt.sync.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.rdt.sync.core.RemoteSyncException;

/**
 * @since 1.0
 */
public interface IRemoteSyncConnection{

	void syncLocalToRemote(IProgressMonitor monitor) throws RemoteSyncException;

	void syncRemoteToLocal(IProgressMonitor monitor) throws RemoteSyncException;
	
	boolean pathFilter(String path);

}
