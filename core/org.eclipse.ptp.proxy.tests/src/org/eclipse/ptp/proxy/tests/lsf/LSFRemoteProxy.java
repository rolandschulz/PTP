package org.eclipse.ptp.proxy.tests.lsf;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import org.eclipse.ptp.lsf.core.rtsystem.LSFProxyRuntimeClient;

import org.eclipse.ptp.rtsystem.proxy.ProxyRuntimeClient;
import org.junit.Test;

public class LSFRemoteProxy {

	@Test public void start_stop() {
		boolean error = false;
		String proxy = "../org.eclipse.ptp.lsf.proxy/ptp_lsf_proxy";
		
		LSFProxyRuntimeClient client = new LSFProxyRuntimeClient(proxy, false);
		
		if (client.startup(null)) {
			client.shutdown();
		} else {
			error = true;
		}
    	      
		assertEquals("Proxy Client: unsuccessfull initialization",
    	              false,
    	              error);

	}
	
}
