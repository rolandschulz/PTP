package org.eclipse.ptp.mpich2.core.rtsystem;

import org.eclipse.ptp.rtsystem.proxy.AbstractProxyRuntimeClient;

public class MPICH2ProxyRuntimeClient extends AbstractProxyRuntimeClient {
	
	public MPICH2ProxyRuntimeClient(String proxyPath, int baseModelId, boolean launchManually) {
		super("MPICH2", proxyPath, baseModelId,  launchManually);
	}

}
