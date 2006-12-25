package org.eclipse.ptp.lsf.core.rtsystem;

import java.io.IOException;
import org.eclipse.ptp.rtsystem.IRuntimeProxy;
import org.eclipse.ptp.rtsystem.proxy.AbstractProxyRuntimeClient;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEventListener;

public class LSFProxyRuntimeClient extends AbstractProxyRuntimeClient implements IRuntimeProxy, IProxyRuntimeEventListener {
	
	public LSFProxyRuntimeClient(String proxyPath, boolean launchManually) {
		super(proxyPath, launchManually);
		super.addRuntimeEventListener(this);
	}
	
	public void runJob(String[] args) throws IOException {
		run(args);
	}

}
