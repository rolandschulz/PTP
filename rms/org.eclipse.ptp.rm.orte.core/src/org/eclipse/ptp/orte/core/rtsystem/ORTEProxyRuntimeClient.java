package org.eclipse.ptp.orte.core.rtsystem;

import org.eclipse.ptp.orte.core.rmsystem.ORTEResourceManagerConfiguration;
import org.eclipse.ptp.remote.AbstractRemoteProxyRuntimeClient;

public class ORTEProxyRuntimeClient extends AbstractRemoteProxyRuntimeClient {
	public ORTEProxyRuntimeClient(ORTEResourceManagerConfiguration config, 
			int baseModelId) {
		super(config, baseModelId);
	}
}
