package org.eclipse.ptp.orte.core.rtsystem;

import org.eclipse.ptp.orte.core.rmsystem.ORTEResourceManagerConfiguration;
import org.eclipse.ptp.proxy.runtime.event.ProxyRuntimeEventFactory;
import org.eclipse.ptp.rm.remote.core.AbstractRemoteProxyRuntimeClient;

public class ORTEProxyRuntimeClient extends AbstractRemoteProxyRuntimeClient {
	public ORTEProxyRuntimeClient(ORTEResourceManagerConfiguration config, 
			int baseModelId) {
		super(config, baseModelId, new ORTEProxyCommandFactory(config), new ProxyRuntimeEventFactory());
	}
}
