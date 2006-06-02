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

package org.eclipse.ptp.rtsystem.mpich2;

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
import org.eclipse.ptp.rtsystem.proxy.event.ProxyRuntimeNodeChangeEvent;
import org.eclipse.ptp.rtsystem.proxy.event.ProxyRuntimeProcessOutputEvent;

public class MPICH2MonitoringSystem implements IMonitoringSystem, IProxyRuntimeEventListener {

	protected List listeners = new ArrayList(2);
	
	private MPICH2ProxyRuntimeClient proxy = null;
	private boolean proxyDead = true;
	
	public boolean isHealthy() { return !proxyDead; }

	public MPICH2MonitoringSystem(MPICH2ProxyRuntimeClient proxy) {
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
		System.out.println("MPICH2MonitoringSystem startup()");
	}

	public void shutdown() {
		System.out.println("MPICH2MonitoringSystem shutdown()");
		listeners.clear();
		listeners = null;
	}
	
	public String[] getMachines() throws CoreException {
		if(proxyDead) {
			throw new CoreException(new Status(IStatus.ERROR, PTPCorePlugin.getUniqueIdentifier(), IStatus.ERROR, "Monitoring system is shut down", null));
		}
		
		System.out.println("JAVA OMPI: getMachines() called");

		String[] ne = new String[1];
		ne[0] = new String("machine0");

		return ne;
	}

	/* get the nodes pertaining to a certain machine */
	public String[] getNodes(IPMachine machine) throws CoreException {
		if(proxyDead) {
			throw new CoreException(new Status(IStatus.ERROR, PTPCorePlugin.getUniqueIdentifier(), IStatus.ERROR, "Monitoring system is shut down", null));
		}
		System.out.println("MPICH2MonitoringSystem: getNodes(" + machine.getElementName() + ") called");

		/* need to check if machineName is a valid machine name */
		
		int numNodes = 0;
		
		int machID = machine.getMachineNumberInt();
		
		try {
			numNodes = proxy.getNumNodesBlocking(machID);
		} catch(IOException e) {
			proxyDead = true;
			throw new CoreException(new Status(IStatus.ERROR, PTPCorePlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), null));
		}
		
		/* this is an error, so we'll return 1 empty node */
		if(numNodes <= 0) {
			String[] ne = new String[1];
			ne[0] = new String(machine.getElementName()+"_node0");
			return ne;
		}
		
		String[] ne = new String[numNodes];
		for(int i=0; i<numNodes; i++) {
			ne[i] = new String(machine.getElementName()+"_node"+i);
		}	
		
		return ne;
	}

	public String getNodeMachineName(String nodeName) throws CoreException {
		if(proxyDead) {
			throw new CoreException(new Status(IStatus.ERROR, PTPCorePlugin.getUniqueIdentifier(), IStatus.ERROR, "Monitoring system is shut down", null));
		}
		System.out.println("JAVA OMPI: getNodeMachineName(" + nodeName
				+ ") called");

		/* check nodeName . . . */

		return "machine0";
	}

	public String[] getNodeAttributes(IPNode node, String[] attribs) throws CoreException {
		if(proxyDead) {
			throw new CoreException(new Status(IStatus.ERROR, PTPCorePlugin.getUniqueIdentifier(), IStatus.ERROR, "Monitoring system is shut down", null));
		}
		System.out.println("ORTE Monitoring System: getNodeAttribute(" + node.getElementName() + ", "
				+ attribs + ") called");
		IPMachine machine = node.getMachine();
		int machID = machine.getMachineNumberInt();
		int nodeID = node.getNodeNumberInt();

		String[] values = null;
		
		try {
			values = proxy.getNodeAttributesBlocking(machID, nodeID, attribs);
		} catch(IOException e) {
			proxyDead = true;
			throw new CoreException(new Status(IStatus.ERROR, PTPCorePlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), null));
		}
		
		return values;
	}
	
	public String[] getAllNodesAttributes(IPMachine machine, String[] attribs) throws CoreException {
		if(proxyDead) {
			throw new CoreException(new Status(IStatus.ERROR, PTPCorePlugin.getUniqueIdentifier(), IStatus.ERROR, "Monitoring system is shut down", null));
		}
		int machID = machine.getMachineNumberInt();
		
		String[] values = null;
		
		try {
			values = proxy.getAllNodesAttributesBlocking(machID, attribs);
		} catch(IOException e) {
			proxyDead = true;
			throw new CoreException(new Status(IStatus.ERROR, PTPCorePlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), null));
		}
		
		if(values == null || values.length == 0) {
			System.out.println("NOTHING RETURNED FROM ORTE_SERVER, faking it with some blank data.");
			
			IPNode[] nodes = machine.getSortedNodes();
			
			int nlen = nodes.length;
			if(nodes == null || nodes.length == 0) nlen = 1;
			
			values = new String[attribs.length * nlen];
			
			for(int i=0; i<nlen; i++) {
				for(int j=0; j<attribs.length; j++) {
					String attrib = attribs[j];
					if(attrib.equals(AttributeConstants.ATTRIB_NODE_NAME))
						values[(i * attribs.length) + j] = new String(""+i+"");
					else if(attrib.equals(AttributeConstants.ATTRIB_NODE_USER))
						values[(i * attribs.length) + j] = System.getProperty("user.name");
					else if(attrib.equals(AttributeConstants.ATTRIB_NODE_GROUP))
						values[(i * attribs.length) + j] = new String("ptp");
					else if(attrib.equals(AttributeConstants.ATTRIB_NODE_STATE))
						values[(i * attribs.length) + j] = new String("up");
					else if(attrib.equals(AttributeConstants.ATTRIB_NODE_MODE))
						values[(i * attribs.length) + j] = new String("73");
				}
			}
		}
		
		return values;
	}

    public synchronized void handleEvent(IProxyRuntimeEvent e) {
        //System.out.println("MPICH2MonitoringSystem got event: " + e.toString());
        if(e instanceof ProxyRuntimeNodeChangeEvent) {
        		RuntimeEvent re = new RuntimeEvent(RuntimeEvent.EVENT_NODE_GENERAL_CHANGE);
        		String key = ((ProxyRuntimeNodeChangeEvent)e).getKey();
        		String val = ((ProxyRuntimeNodeChangeEvent)e).getValue();
        		int machID = ((ProxyRuntimeNodeChangeEvent)e).getMachineID();
        		String nodeName = ((ProxyRuntimeNodeChangeEvent)e).getNodeName();
        		
        		/* TODO
        		 * COME BACK HERE AND CONVERT CODES LIKE bproc-soh-state INTO ATTRIBUTE_CONSTANTS.NODE_STATE, ETC
        		 * To do this you gotta get the SoH crap working so you can bpctl it.
        		 */
        		/*
        		if(key.equals(""))
        		
        		public static final String ATTRIB_NODE_NAME = "ATTRIB_NODE_NAME";
        		public static final String ATTRIB_NODE_NUMBER = "ATTRIB_NODE_NUMBER";
        		public static final String ATTRIB_NODE_STATE = "ATTRIB_NODE_STATE";
        		public static final String ATTRIB_NODE_GROUP = "ATTRIB_NODE_GROUP";
        		public static final String ATTRIB_NODE_USER = "ATTRIB_NODE_USER";
        		public static final String ATTRIB_NODE_MODE = "ATTRIB_NODE_MODE";
        		*/
        		
        		String valid_key = null;
         		
        		if(valid_key != null) {
        			re.setText(valid_key);
        			re.setAltText(val);
        		
        			fireEvent("machine"+machID+"_node"+nodeName, re);
        		}
        		else {
        			System.out.println("UNKNOWN KEY '"+key+"', value '"+val+"' - IGNORING.");
        		}
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
    
	protected synchronized void fireEvent(String ID, RuntimeEvent event) {
		if (listeners == null)
			return;
		Iterator i = listeners.iterator();
		while (i.hasNext()) {
			IRuntimeListener listener = (IRuntimeListener) i.next();
			switch (event.getEventNumber()) {
			case RuntimeEvent.EVENT_NODE_GENERAL_CHANGE:
				listener.runtimeNodeGeneralChange(ID, event.getText(), event.getAltText());
				break;
			}
		}
	}
}

