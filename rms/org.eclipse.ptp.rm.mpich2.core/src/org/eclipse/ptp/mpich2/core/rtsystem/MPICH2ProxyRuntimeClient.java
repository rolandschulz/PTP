package org.eclipse.ptp.mpich2.core.rtsystem;

import org.eclipse.ptp.mpich2.core.rmsystem.MPICH2ResourceManagerConfiguration;
import org.eclipse.ptp.rm.remote.core.AbstractRemoteProxyRuntimeClient;

public class MPICH2ProxyRuntimeClient extends AbstractRemoteProxyRuntimeClient {
	
	public MPICH2ProxyRuntimeClient(MPICH2ResourceManagerConfiguration config, int baseModelId) {
		super(config, baseModelId);
	}

}
