package org.eclipse.ptp.lsf.core.rtsystem;

import org.eclipse.ptp.rtsystem.proxy.AbstractProxyRuntimeClient;

public class LSFProxyRuntimeClient extends AbstractProxyRuntimeClient {
	
	public LSFProxyRuntimeClient(String proxyPath, int baseModelId, boolean launchManually) {
		super("LSF", proxyPath, baseModelId, launchManually);
	}

}
