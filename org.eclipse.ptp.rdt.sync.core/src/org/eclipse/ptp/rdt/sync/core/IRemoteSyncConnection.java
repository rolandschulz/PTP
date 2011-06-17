package org.eclipse.ptp.rdt.sync.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.rdt.sync.core.RemoteSyncException;

public interface IRemoteSyncConnection{

	void syncLocalToRemote(IProgressMonitor monitor) throws RemoteSyncException;

	void syncRemoteToLocal(IProgressMonitor monitor) throws RemoteSyncException;

}
