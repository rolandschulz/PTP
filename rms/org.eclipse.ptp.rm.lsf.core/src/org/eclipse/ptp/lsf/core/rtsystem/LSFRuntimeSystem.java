/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/

package org.eclipse.ptp.lsf.core.rtsystem;

import org.eclipse.ptp.core.attributes.AttributeDefinitionManager;
import org.eclipse.ptp.rtsystem.AbstractProxyRuntimeSystem;

public class LSFRuntimeSystem extends AbstractProxyRuntimeSystem {
	public LSFRuntimeSystem(LSFProxyRuntimeClient proxy, AttributeDefinitionManager manager) {
		super(proxy, manager);
	}
	
/* TODO work out what to do with these errors!
	public void handleProxyDisconnectedEvent(IProxyDisconnectedEvent e) {
		boolean is_error = e.wasError();
		System.out.println("Proxy Disconnected.");
		proxyDead = true;
		if(is_error) {
			PTPCorePlugin.errorDialog("Fatal PTP Control System Error",
					"There was a fatal PTP Control System error.  The proxy "+
					"server disconnected with an error.\n\n"+
					"Control System is now disabled.", null);
		}
	}

	public void handleProxyErrorEvent(IProxyErrorEvent e) {
		System.err.println("Fatal error from proxy: '"+e.getErrorMessage()+"'");
		int errorCode = e.getErrorCode();
		String errorMsg = e.getErrorMessage();
		PTPCorePlugin.errorDialog("Fatal PTP Control System Error",
				"There was a fatal PTP Control System error (ERROR CODE: "+errorCode+").\n"+
				"Error message: \""+errorMsg+"\"\n\n"+
				"Control System is now disabled.", null);
		proxyDead = true;
	}
	*/
}
