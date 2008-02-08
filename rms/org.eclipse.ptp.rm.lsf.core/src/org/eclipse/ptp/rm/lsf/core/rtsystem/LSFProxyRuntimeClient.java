package org.eclipse.ptp.rm.lsf.core.rtsystem;

import org.eclipse.ptp.rm.lsf.core.rmsystem.LSFResourceManagerConfiguration;
import org.eclipse.ptp.rm.remote.core.AbstractRemoteProxyRuntimeClient;

public class LSFProxyRuntimeClient extends AbstractRemoteProxyRuntimeClient {
	
	public LSFProxyRuntimeClient(LSFResourceManagerConfiguration config, int baseModelId) {
		super(config, baseModelId);
	}

}
