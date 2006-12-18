package org.eclipse.ptp.proxy.tests.lsf;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.eclipse.ptp.rtsystem.proxy.ProxyRuntimeClient;
import org.junit.Test;

public class LSFRemoteProxy {

	@Test public void start_stop() {
		boolean error = false;
		
		ProxyRuntimeClient client = new ProxyRuntimeClient();
		
		try {
			client.sessionCreate(3333, 0);
			//wait(50000);
			//client.initialize();
		} catch(Exception e) {
            e.printStackTrace();
			error = true;

		} 
    	      
		assertEquals("Proxy Client: unsuccessfull initialization",
    	              false,
    	              error);

	}
	
}
