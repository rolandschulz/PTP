package org.eclipse.ptp.rm.ompi.core.rtsystem;

import org.eclipse.ptp.proxy.runtime.event.ProxyRuntimeEventFactory;
import org.eclipse.ptp.rm.ompi.core.rmsystem.OMPIResourceManagerConfiguration;
import org.eclipse.ptp.rm.remote.core.AbstractRemoteProxyRuntimeClient;

public class OMPIProxyRuntimeClient extends AbstractRemoteProxyRuntimeClient {
	public OMPIProxyRuntimeClient(OMPIResourceManagerConfiguration config, 
			int baseModelId) {
		super(config, baseModelId, new OMPIProxyCommandFactory(config), new ProxyRuntimeEventFactory());
	}
}
