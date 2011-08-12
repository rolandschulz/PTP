package org.eclipse.ptp.rdt.sync.rsync.core;

import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @since 1.0
 */
public interface IRemoteSyncConnection{
	void syncLocalToRemote(IProgressMonitor monitor) throws RemoteSyncException;
	void syncRemoteToLocal(IProgressMonitor monitor) throws RemoteSyncException;
	boolean pathFilter(String path);
	public void pathChanged(IResourceDelta delta) throws RemoteSyncException;
}
