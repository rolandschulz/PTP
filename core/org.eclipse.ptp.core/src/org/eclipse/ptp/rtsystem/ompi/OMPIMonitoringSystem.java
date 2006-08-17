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

package org.eclipse.ptp.rtsystem.ompi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.core.AttributeConstants;
import org.eclipse.ptp.core.IPMachine;
import org.eclipse.ptp.core.IPNode;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.rtsystem.IMonitoringSystem;
import org.eclipse.ptp.rtsystem.IRuntimeListener;
import org.eclipse.ptp.rtsystem.RuntimeEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEventListener;
import org.eclipse.ptp.rtsystem.proxy.event.ProxyRuntimeErrorEvent;
import org.eclipse.ptp.rtsystem.proxy.event.ProxyRuntimeJobStateEvent;
import org.eclipse.ptp.rtsystem.proxy.event.ProxyRuntimeNodeAttributeEvent;
import org.eclipse.ptp.rtsystem.proxy.event.ProxyRuntimeNodeChangeEvent;
import org.eclipse.ptp.rtsystem.proxy.event.ProxyRuntimeProcessOutputEvent;

public class OMPIMonitoringSystem implements IMonitoringSystem, IProxyRuntimeEventListener {

	protected List listeners = new ArrayList(2);
	
	private OMPIProxyRuntimeClient proxy = null;
	private boolean proxyDead = true;
	
	public boolean isHealthy() { return !proxyDead; }

	public OMPIMonitoringSystem(OMPIProxyRuntimeClient proxy) {
		this.proxy = proxy;
		if(proxy != null) proxyDead = false;
		proxy.addRuntimeEventListener(this);
	}

	public void addRuntimeListener(IRuntimeListener listener) {
		listeners.add(listener);
	}

	public void removeRuntimeListener(IRuntimeListener listener) {
		listeners.remove(listener);
	}

	public void startup() {
		System.out.println("OMPIMonitoringSystem startup()");
	}

	public void shutdown() {
		System.out.println("OMPIMonitoringSystem shutdown()");
		listeners.clear();
		listeners = null;
	}
	
	public void initiateDiscovery() throws CoreException {
		if(proxyDead) {
			throw new CoreException(new Status(IStatus.ERROR, PTPCorePlugin.getUniqueIdentifier(), IStatus.ERROR, "Monitoring system is shut down", null));
		}
		System.out.println("OMPIMonitoringSystem: initiateDiscovery phase");
		try {
			proxy.initiateDiscovery();
		} catch(IOException e) {
			e.printStackTrace();
			throw new CoreException(new Status(IStatus.ERROR, PTPCorePlugin.getUniqueIdentifier(), IStatus.ERROR, "Proxy IO Exception", null));
		}
	}

    public synchronized void handleEvent(IProxyRuntimeEvent e) {
        //System.out.println("OMPIMonitoringSystem got event: " + e.toString());
    	if(e instanceof ProxyRuntimeNodeAttributeEvent) {
    		String[] keys = ((ProxyRuntimeNodeAttributeEvent)e).getKeys();
    		String[] vals = ((ProxyRuntimeNodeAttributeEvent)e).getValues();
    		/*
    		for(int i=0; i<vals.length; i++) {
    			System.out.println(i+": "+keys[i]+" = "+vals[i]);
    		}
    		*/
    		RuntimeEvent re = new RuntimeEvent(RuntimeEvent.EVENT_NODE_GENERAL_CHANGE);
    		re.setAttributeKeys(keys);
    		re.setAttributeValues(vals);
    		fireEvent(re);
    	}
        else if(e instanceof ProxyRuntimeErrorEvent) {
			System.err.println("Fatal error from proxy: '"+((ProxyRuntimeErrorEvent)e).getErrorMessage()+"'");
			int errorCode = ((ProxyRuntimeErrorEvent)e).getErrorCode();
			String errorMsg = ((ProxyRuntimeErrorEvent)e).getErrorMessage();
			PTPCorePlugin.errorDialog("Fatal PTP Monitoring System Error",
					"There was a fatal PTP Monitoring System error (ERROR CODE: "+errorCode+").\n"+
					"Error message: \""+errorMsg+"\"\n\n"+
					"Monitoring System is now disabled.", null);
			proxyDead = true;
		}
    }
    
	protected synchronized void fireEvent(RuntimeEvent event) {
		if (listeners == null)
			return;
		Iterator i = listeners.iterator();
		while (i.hasNext()) {
			IRuntimeListener listener = (IRuntimeListener) i.next();
			switch (event.getEventNumber()) {
			case RuntimeEvent.EVENT_NODE_GENERAL_CHANGE:
				listener.runtimeNodeGeneralChange(event.getAttributeKeys(), event.getAttributeValues());
				break;
			}
		}
	}
}

