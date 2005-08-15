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
package org.eclipse.ptp.internal.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ptp.core.IPElement;
import org.eclipse.ptp.core.IPMachine;
import org.eclipse.ptp.core.IPNode;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.ui.IPTPUIConstants;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.ptp.ui.model.IElementSet;
import org.eclipse.ptp.ui.model.internal.Element;
import org.eclipse.ptp.ui.model.internal.ElementHandler;

/**
 * @author clement chu
 *
 */
public class MachineManager extends AbstractUIManager {	
	private Map machineList = new HashMap();
	protected String cur_machine_id = "";

	public MachineManager() {
		modelManager = PTPCorePlugin.getDefault().getModelManager();
	}

	public void shutdown() {
		machineList.clear();
		machineList = null;
		modelManager = null;
	}

	public boolean isNoMachine() {
		return isNoMachine(cur_machine_id);
	}
	public boolean isNoMachine(String machid) {
		return (machid == null || machid.length() == 0);
	}

	public IElementHandler getElementHandler(String id) {
		return (IElementHandler)machineList.get(id);
	}
	public int size() {
		return machineList.size();
	}
	
	public IPMachine[] getMachines() {
		return modelManager.getUniverse().getSortedMachines();
	}
	
	public String getCurrentMachineId() {
		return cur_machine_id;
	}
	public void setCurrentMachineId(String machine_id) {
		cur_machine_id = machine_id;
	}
	
	public String getCurrentSetId() {
		return cur_set_id;
	}
	public void setCurrentSetId(String set_id) {
		cur_set_id = set_id;
	}	
	
	public String getNodeStatusText(IPNode node) {
		switch(getNodeStatus(node)) {
		case IPTPUIConstants.NODE_USER_ALLOC_EXCL:
			return "User Alloc Excl";
		case IPTPUIConstants.NODE_USER_ALLOC_SHARED:
			return "User Alloc Shared";
		case IPTPUIConstants.NODE_OTHER_ALLOC_EXCL:
			return "Other Alloc Excl";
		case IPTPUIConstants.NODE_OTHER_ALLOC_SHARED:
			return "Other Alloc Shared";
		case IPTPUIConstants.NODE_UP:
			return "Up";
		case IPTPUIConstants.NODE_DOWN:
			return "Down";
		case IPTPUIConstants.NODE_ERROR:
			return "Error";
		case IPTPUIConstants.NODE_UNKNOWN:
			return "Unknown";
		default:
			return "Unknown";
		}
	}
	public String getNodeStatusText(String job_id, String proc_id) {
		return getNodeStatusText(findNode(job_id, proc_id));
	}	
	public int getProcStatus(String p_state) {
		if (p_state.equals(IPProcess.STARTING))
			return IPTPUIConstants.PROC_STARTING;
		else if (p_state.equals(IPProcess.RUNNING))
			return IPTPUIConstants.PROC_RUNNING;
		else if (p_state.equals(IPProcess.EXITED))
			return IPTPUIConstants.PROC_EXITED;
		else if (p_state.equals(IPProcess.EXITED_SIGNALLED))
			return IPTPUIConstants.PROC_EXITED_SIGNAL;
		else if (p_state.equals(IPProcess.STOPPED))
			return IPTPUIConstants.PROC_STOPPED;
		else if (p_state.equals(IPProcess.ERROR))
			return IPTPUIConstants.PROC_ERROR;
		else
			return IPTPUIConstants.PROC_ERROR;
	}	
	public int getNodeStatus(IPNode node) {
		if (node != null) {
			String nodeState = (String)node.getAttrib("state");
			if (nodeState.equals("up")) {
				if (node.getAttrib("user").equals(System.getProperty("user.name"))) {
					String mode = (String) node.getAttrib("mode");
					if (mode.equals("0100"))
						return IPTPUIConstants.NODE_USER_ALLOC_EXCL;
					else if (mode.equals("0110") || mode.equals("0111") || mode.equals("0101"))
						return IPTPUIConstants.NODE_USER_ALLOC_SHARED;
				}
				else if (!node.getAttrib("user").equals("")) {
					String mode = (String) node.getAttrib("mode");
					if (mode.equals("0100"))
						return IPTPUIConstants.NODE_OTHER_ALLOC_EXCL;
					else if (mode.equals("0110") || mode.equals("0111") || mode.equals("0101"))
						return IPTPUIConstants.NODE_OTHER_ALLOC_SHARED;
				}
				return IPTPUIConstants.NODE_UP;
			}
			else if (nodeState.equals("down"))
				return IPTPUIConstants.NODE_DOWN;
			else if (nodeState.equals("error"))
				return IPTPUIConstants.NODE_ERROR;
		}
		return IPTPUIConstants.NODE_UNKNOWN;		
	}
	public int getNodeStatus(String machine_id, String node_id) {
		return getNodeStatus(findNode(machine_id, node_id));
	}

	//FIXME using id, or name
	public IPNode findNode(String machine_id, String node_id) {
		//FIXME HARDCODE
		return modelManager.getUniverse().findNodeByName(getName(machine_id) + "_node" + node_id);
	}
	
	public IPMachine findMachine(String machine_name) {
		return (IPMachine)modelManager.getUniverse().findMachineByName(machine_name);
	}	
	public IPMachine findMachineById(String machine_id) {
		IPElement element = modelManager.getUniverse().findChild(machine_id);
		if (element == null)
			return findMachineById2(machine_id);
		return (IPMachine)element;
	}
	
	public IPMachine findMachineById2(String machine_id) {
		IPMachine[] macs = modelManager.getUniverse().getMachines();
		for (int i=0; i<macs.length; i++) {
			if (macs[i].getIDString().equals(machine_id))
				return macs[i];
		}
		return null;
	}
	
	//FIXME don't know whether it return machine or job
	public String getName(String id) {
		IPElement element = findMachineById(id);
		if (element == null)
			return "";
		
		return element.getElementName();
	}
	
	public void addMachine(IPMachine mac) {
		IPElement[] pElements = mac.getSortedNodes();
		int total_element = pElements.length;
		if (total_element > 0) {
			IElementHandler elementHandler = new ElementHandler();
			elementHandler.clearAll();
			IElementSet set = elementHandler.getSetRoot();
			for (int i=0; i<total_element; i++) {
				//FIXME using id, or name
				set.add(new Element(pElements[i].getIDString(), pElements[i].getElementName()));
			}
			elementHandler.add(set);
			machineList.put(mac.getIDString(), elementHandler);
		}
	}
	
	public String initial() {
		IPMachine[] macs = getMachines();
		if (macs.length > 0) {
			cur_machine_id = macs[0].getIDString();
			for (int j=0; j<macs.length; j++) {
				System.out.println(macs[j]);
				if (!machineList.containsKey(macs[j].getIDString()))
					addMachine(macs[j]);
			}
		}
		return cur_machine_id;
	}	
}
