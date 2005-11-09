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
import java.util.List;

import org.eclipse.ptp.core.AttributeConstants;
import org.eclipse.ptp.core.IPMachine;
import org.eclipse.ptp.rtsystem.IMonitoringSystem;
import org.eclipse.ptp.rtsystem.IRuntimeListener;

public class OMPIMonitoringSystem implements IMonitoringSystem {

	protected List listeners = new ArrayList(2);
	
	private OMPIProxyRuntimeClient proxy = null;

	public OMPIMonitoringSystem(OMPIProxyRuntimeClient proxy) {
		this.proxy = proxy;
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

	public String getNodeAttribute(String nodeName, String attrib) {
		System.out.println("JAVA OMPI: getNodeAttribute(" + nodeName + ", "
				+ attrib + ") called");
		String s = null;

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
	}
}
