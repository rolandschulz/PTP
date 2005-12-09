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

import org.eclipse.ptp.core.AttributeConstants;
import org.eclipse.ptp.core.IPMachine;
import org.eclipse.ptp.core.IPNode;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.rtsystem.IMonitoringSystem;
import org.eclipse.ptp.rtsystem.IRuntimeListener;
import org.eclipse.ptp.rtsystem.RuntimeEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEventListener;
import org.eclipse.ptp.rtsystem.proxy.event.ProxyRuntimeJobStateEvent;
import org.eclipse.ptp.rtsystem.proxy.event.ProxyRuntimeNodeChangeEvent;
import org.eclipse.ptp.rtsystem.proxy.event.ProxyRuntimeProcessOutputEvent;

public class OMPIMonitoringSystem implements IMonitoringSystem, IProxyRuntimeEventListener {

	protected List listeners = new ArrayList(2);
	
	private OMPIProxyRuntimeClient proxy = null;

	public OMPIMonitoringSystem(OMPIProxyRuntimeClient proxy) {
		this.proxy = proxy;
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
	
	public String[] getMachines() {
		System.out.println("JAVA OMPI: getMachines() called");

		String[] ne = new String[1];
		ne[0] = new String("machine0");

		return ne;
	}

	/* get the nodes pertaining to a certain machine */
	public String[] getNodes(IPMachine machine) {
		System.out.println("OMPIMonitoringSystem: getNodes(" + machine.getElementName() + ") called");

		/* need to check if machineName is a valid machine name */

//		/* default to just returning 10 nodes on this machine */
//		int n = 10;
//		String[] ne = new String[n];
//
//		for (int i = 0; i < ne.length; i++) {
//			/* prepend this node name with the machine name */
//			ne[i] = new String(machineName + "_node" + i);
//		}
		
		int numNodes = 0;
		
		int machID = machine.getMachineNumberInt();
		
		try {
			numNodes = proxy.getNumNodesBlocking(machID);
		} catch(IOException e) {
			e.printStackTrace();
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

	public String getNodeMachineName(String nodeName) {
		System.out.println("JAVA OMPI: getNodeMachineName(" + nodeName
				+ ") called");

		/* check nodeName . . . */

		return "machine0";
	}

	public String[] getNodeAttributes(IPNode node, String attribs) {
		System.out.println("ORTE Monitoring System: getNodeAttribute(" + node.getElementName() + ", "
				+ attribs + ") called");
		IPMachine machine = node.getMachine();
		int machID = machine.getMachineNumberInt();
		int nodeID = node.getNodeNumberInt();

		String[] values = null;
		
		try {
			values = proxy.getNodeAttributesBlocking(machID, nodeID, attribs);
		} catch(IOException e) {
			e.printStackTrace();
		}

		/*
		if (attrib.equals(AttributeConstants.ATTRIB_NODE_STATE)) {
			s = "up";
		} else if (attrib.equals(AttributeConstants.ATTRIB_NODE_MODE)) {
			s = "0100";
		} else if (attrib.equals(AttributeConstants.ATTRIB_NODE_USER)) {
			s = System.getProperty("user.name");
		} else if (attrib.equals(AttributeConstants.ATTRIB_NODE_GROUP)) {
			s = "ptp";
		}
		return s;
		*/
		
		return values;
	}
	
	public String[] getAllNodesAttributes(IPMachine machine, String attribs) {
		int machID = machine.getMachineNumberInt();
		
		String[] values = null;
		
		try {
			values = proxy.getAllNodesAttributesBlocking(machID, attribs);
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		return values;
	}

    public synchronized void handleEvent(IProxyRuntimeEvent e) {
        System.out.println("OMPIMonitoringSystem got event: " + e.toString());
        if(e instanceof ProxyRuntimeNodeChangeEvent) {
        		RuntimeEvent re = new RuntimeEvent(RuntimeEvent.EVENT_NODE_GENERAL_CHANGE);
        		String key = ((ProxyRuntimeNodeChangeEvent)e).getKey();
        		String val = ((ProxyRuntimeNodeChangeEvent)e).getValue();
        		int machID = ((ProxyRuntimeNodeChangeEvent)e).getMachineID();
        		int nodeID = ((ProxyRuntimeNodeChangeEvent)e).getNodeID();
        		
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
        		
        		
        		re.setText(key);
        		re.setAltText(val);
        		
        		fireEvent("machine"+machID+"_node"+nodeID, re);
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

