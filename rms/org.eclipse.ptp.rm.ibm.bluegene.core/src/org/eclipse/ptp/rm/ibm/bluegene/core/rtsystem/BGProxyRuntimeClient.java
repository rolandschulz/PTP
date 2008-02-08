package org.eclipse.ptp.rm.ibm.bluegene.core.rtsystem;

import org.eclipse.ptp.rm.ibm.bluegene.core.rmsystem.BGResourceManagerConfiguration;
import org.eclipse.ptp.rm.remote.core.AbstractRemoteProxyRuntimeClient;

public class BGProxyRuntimeClient extends AbstractRemoteProxyRuntimeClient {
	public BGProxyRuntimeClient(BGResourceManagerConfiguration config, 
			int baseModelId) {
		super(config, baseModelId);
	}
}
