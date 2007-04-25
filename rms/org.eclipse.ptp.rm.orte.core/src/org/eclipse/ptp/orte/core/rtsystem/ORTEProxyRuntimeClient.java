package org.eclipse.ptp.orte.core.rtsystem;

import org.eclipse.ptp.rtsystem.proxy.AbstractProxyRuntimeClient;

public class ORTEProxyRuntimeClient extends AbstractProxyRuntimeClient {
	
	public ORTEProxyRuntimeClient(String proxyPath, int baseModelId, boolean launchManually) {
		super("ORTE", proxyPath, baseModelId, launchManually);
	}

}
