package org.eclipse.ptp.proxy.tests.lsf;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.eclipse.ptp.lsf.core.rtsystem.LSFProxyRuntimeClient;
import org.junit.Test;

public class LSFRemoteProxy {

	@Test public void start_stop() {
		boolean error = false;
		boolean launchManually = false;
		String proxy = "lsf/ptp_lsf_proxy";
		
		LSFProxyRuntimeClient client = new LSFProxyRuntimeClient(proxy, launchManually);
		
		if (client.startup(null)) {
			try {
				client.initialize();
			} catch (IOException e) {
				error = true;
				e.printStackTrace();
			}
			client.shutdown();
		} else {
			error = true;
		}
    	      
		assertEquals("LSFRemoteProxyTest: Proxy Client: FAILURE, unsuccessfull initialization",
    	              false,
    	              error);

	}
	
	@Test public void events() {
		boolean error = false;
		boolean launchManually = false;
		String proxy = "lsf/ptp_lsf_proxy";
		
		LSFProxyRuntimeClient client = new LSFProxyRuntimeClient(proxy, launchManually);
		
		if (client.startup(null)) {
			try {
				client.initialize();
				client.sendEvents();
				client.haltEvents();
			} catch (IOException e) {
				error = true;
				e.printStackTrace();
			}
			client.shutdown();
		} else {
			error = true;
		}
    	      
		assertEquals("LSFRemoteProxyTest: Proxy Client: FAILURE, unsuccessfull initialization",
    	              false,
    	              error);

	}
	
}
