package org.eclipse.ptp.lsf.core.rtsystem;

import org.eclipse.ptp.lsf.core.rmsystem.LSFResourceManagerConfiguration;
import org.eclipse.ptp.rm.remote.core.AbstractRemoteProxyRuntimeClient;

public class LSFProxyRuntimeClient extends AbstractRemoteProxyRuntimeClient {
	
	public LSFProxyRuntimeClient(LSFResourceManagerConfiguration config, int baseModelId) {
		super(config, baseModelId);
	}

}
