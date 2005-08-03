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
package org.eclipse.ptp.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ptp.core.IModelManager;
import org.eclipse.ptp.core.IPElement;
import org.eclipse.ptp.core.IPMachine;
import org.eclipse.ptp.core.IPNode;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.ui.model.IElementSet;
import org.eclipse.ptp.ui.model.ISetManager;
import org.eclipse.ptp.ui.model.internal.Element;
import org.eclipse.ptp.ui.model.internal.SetManager;

/**
 * @author clement chu
 *
 */
public class MachineManager {
	public final static int NODE_USER_ALLOC_EXCL = 0;
	public final static int NODE_USER_ALLOC_SHARED = 1;
	public final static int NODE_OTHER_ALLOC_EXCL = 2;
	public final static int NODE_OTHER_ALLOC_SHARED = 3;
	public final static int NODE_DOWN = 4;
	public final static int NODE_ERROR = 5;
	public final static int NODE_EXITED = 6;
	public final static int NODE_RUNNING = 7;
	public final static int NODE_UNKNOWN = 8;
	public final static int NODE_UP = 9;
	
	public final static int PROC_ERROR = 10;
	public final static int PROC_EXITED = 11;
	public final static int PROC_EXITED_SIGNAL = 12;
	public final static int PROC_RUNNING = 13;
	public final static int PROC_STARTING = 14;
	public final static int PROC_STOPPED = 15;
	
	protected IModelManager modelManager = null;
	protected UIManager uiManager = null;
	private Map machineList = new HashMap();

	public MachineManager() {
		modelManager = PTPCorePlugin.getDefault().getModelManager();
		uiManager = PTPUIPlugin.getDefault().getUIManager();
	}
		
	public ISetManager getSetManager(String machine_name) {
		return (ISetManager)machineList.get(machine_name);
	}
	
	public String getNodeStatusText(String id) {
		switch(getNodeStatus(id)) {
		case NODE_USER_ALLOC_EXCL:
			return "User Alloc Excl";
		case NODE_USER_ALLOC_SHARED:
			return "User Alloc Shared";
		case NODE_OTHER_ALLOC_EXCL:
			return "Other Alloc Excl";
		case NODE_OTHER_ALLOC_SHARED:
			return "Other Alloc Shared";
		case NODE_UP:
			return "Up";
		case NODE_DOWN:
			return "Down";
		case NODE_ERROR:
			return "Error";
		case NODE_UNKNOWN:
			return "Unknown";
		default:
			return "Unknown";
		}
	}
	
	public int getNodeStatus(String id) {
		IPNode node = findNode(id);
		if (node != null) {
			String nodeState = (String)node.getAttrib("state");
			if (nodeState.equals("up")) {
				if (node.getAttrib("user").equals(System.getProperty("user.name"))) {
					String mode = (String) node.getAttrib("mode");
					if (mode.equals("0100"))
						return NODE_USER_ALLOC_EXCL;
					else if (mode.equals("0110") || mode.equals("0111") || mode.equals("0101"))
						return NODE_USER_ALLOC_SHARED;
				}
				else if (!node.getAttrib("user").equals("")) {
					String mode = (String) node.getAttrib("mode");
					if (mode.equals("0100"))
						return NODE_OTHER_ALLOC_EXCL;
					else if (mode.equals("0110") || mode.equals("0111") || mode.equals("0101"))
						return NODE_OTHER_ALLOC_SHARED;
				}
				return NODE_UP;
			}
			else if (nodeState.equals("down"))
				return NODE_DOWN;
			else if (nodeState.equals("error"))
				return NODE_ERROR;
		}
		return NODE_UNKNOWN;		
	}

	//FIXME using id, or name
	public IPNode findNode(String id) {
		return modelManager.getUniverse().findNodeByName(id);
	}
	
	public void addMachine(IPMachine mac) {
		IPElement[] pElements = mac.getSortedNodes();
		int total_element = pElements.length;
		if (total_element > 0) {
			ISetManager setManager = new SetManager();
			setManager.clearAll();
			IElementSet set = setManager.getSetRoot();
			for (int i=0; i<total_element; i++) {
				//FIXME using id, or name
				set.add(new Element(pElements[i].getKeyString()));
			}
			setManager.add(set);
			machineList.put(mac.getElementName(), setManager);
		}
	}
	
	public void initialMachines() {
		IPMachine[] macs = modelManager.getUniverse().getSortedMachines();
		if (macs.length > 0) {
			for (int j=0; j<macs.length; j++) {
				if (!machineList.containsKey(macs[j].getElementName()))
					addMachine(macs[j]);
			}
		}
	}	
}
