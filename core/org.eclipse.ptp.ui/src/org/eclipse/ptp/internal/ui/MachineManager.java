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
import org.eclipse.ptp.core.AttributeConstants;
import org.eclipse.ptp.core.INodeEvent;
import org.eclipse.ptp.core.INodeListener;
import org.eclipse.ptp.core.IPElement;
import org.eclipse.ptp.core.IPMachine;
import org.eclipse.ptp.core.IPNode;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.IPUniverse;
import org.eclipse.ptp.internal.ui.model.Element;
import org.eclipse.ptp.internal.ui.model.ElementHandler;
import org.eclipse.ptp.ui.IPTPUIConstants;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.ptp.ui.model.IElementSet;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableItem;

/**
 * @author clement chu
 * 
 */
public class MachineManager extends AbstractUIManager implements INodeListener {
	private Map machineList = new HashMap();
	protected String cur_machine_id = EMPTY_ID;

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#shutdown()
	 */
	public void shutdown() {
		clear();
		modelManager = null;
		super.shutdown();
	}
	/** Is no machine
	 * @return true if there is no machine
	 */
	public boolean isNoMachine() {
		return isNoMachine(cur_machine_id);
	}
	/** Is no machine
	 * @param machid machine ID
	 * @return true if there is no machine 
	 */
	public boolean isNoMachine(String machid) {
		return (machid == null || machid.length() == 0);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#getElementHandler(java.lang.String)
	 */
	public IElementHandler getElementHandler(String id) {
		return (IElementHandler) machineList.get(id);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#size()
	 */
	public int size() {
		return machineList.size();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#clear()
	 */
	public void clear() {
		if (machineList != null) {
			machineList.clear();
		}
	}
	/** Get machines
	 * @return machines
	 */
	public IPMachine[] getMachines() {
		IPUniverse universe = modelManager.getUniverse();
		if (universe == null) {
			return new IPMachine[0];
		}
		return universe.getSortedMachines();
	}
	/** Get current machine ID
	 * @return current machine ID
	 */
	public String getCurrentMachineId() {
		return cur_machine_id;
	}
	/** Set current machine ID
	 * @param machine_id machine ID
	 */
	public void setCurrentMachineId(String machine_id) {
		cur_machine_id = machine_id;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#getCurrentSetId()
	 */
	public String getCurrentSetId() {
		return cur_set_id;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#setCurrentSetId(java.lang.String)
	 */
	public void setCurrentSetId(String set_id) {
		cur_set_id = set_id;
	}
	/** Convert attribute key to display name
	 * 
	 * @param key
	 * @return name
	 */
	public String getNodeAttributeName(String key) {
		if (key.equals(AttributeConstants.ATTRIB_NODE_NAME))
			return "Name";
		if (key.equals(AttributeConstants.ATTRIB_NODE_NUMBER))
			return "Node #";
		if (key.equals(AttributeConstants.ATTRIB_NODE_STATE))
			return "State";
		if (key.equals(AttributeConstants.ATTRIB_NODE_GROUP))
			return "Group";
		if (key.equals(AttributeConstants.ATTRIB_NODE_USER))
			return "User";
		if (key.equals(AttributeConstants.ATTRIB_NODE_MODE))
			return "Mode";
			
		return "Unknown";
	}
	/** Get node status text
	 * @param node
	 * @return status text
	 */
	public String getNodeStatusText(IPNode node) {
		switch (getNodeStatus(node)) {
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
	/** Get node status text
	 * @param job_id job ID
	 * @param proc_id process ID
	 * @return status
	 */
	public String getNodeStatusText(String job_id, String proc_id) {
		return getNodeStatusText(findNode(job_id, proc_id));
	}
	/** Get process status
	 * @param p_state
	 * @return status
	 */
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
	/** Get node status
	 * @param node
	 * @return
	 */
	public int getNodeStatus(IPNode node) {
		if (node != null) {
			String nodeState = (String)node.getAttribute(AttributeConstants.ATTRIB_NODE_STATE);
			//System.out.println("nodestate = '"+nodeState+"'");
			if(nodeState == null) {
				System.out.println("null node state!");
				return IPTPUIConstants.NODE_UNKNOWN;
			}
			if (nodeState.equals(IPNode.NODE_STATE_UP)) {
				if (node.size() > 0)
					return (node.isAllStop() ? IPTPUIConstants.NODE_EXITED : IPTPUIConstants.NODE_RUNNING);
				if (node.getAttribute(AttributeConstants.ATTRIB_NODE_USER).equals(System.getProperty("user.name"))) {
					String mode = (String) node.getAttribute(AttributeConstants.ATTRIB_NODE_MODE);
					//System.out.println("Mode = '"+mode+"'");
					if (mode.equals("64"))
						return IPTPUIConstants.NODE_USER_ALLOC_EXCL;
					else if (mode.equals("72") || mode.equals("73") || mode.equals("65"))
						return IPTPUIConstants.NODE_USER_ALLOC_SHARED;
				} else if (!node.getAttribute(AttributeConstants.ATTRIB_NODE_USER).equals("")) {
					String mode = (String) node.getAttribute(AttributeConstants.ATTRIB_NODE_MODE);
					if (mode.equals("64"))
						return IPTPUIConstants.NODE_OTHER_ALLOC_EXCL;
					else if (mode.equals("72") || mode.equals("73") || mode.equals("65"))
						return IPTPUIConstants.NODE_OTHER_ALLOC_SHARED;
				}
				return IPTPUIConstants.NODE_UP;
			} else if (nodeState.equals(IPNode.NODE_STATE_DOWN))
				return IPTPUIConstants.NODE_DOWN;
			else if (nodeState.equals(IPNode.NODE_STATE_ERROR))
				return IPTPUIConstants.NODE_ERROR;
		}
		return IPTPUIConstants.NODE_UNKNOWN;
	}
	/** Get status 
	 * @param machine_id Machine ID
	 * @param node_id node ID
	 * @return status
	 */
	public int getStatus(String machine_id, String node_id) {
		return getNodeStatus(findNode(machine_id, node_id));
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#getStatus(java.lang.String)
	 */
	public int getStatus(String id) {
		return getStatus(getCurrentMachineId(), id);
	}
	
	/** Find node 
	 * @param machine_id machine ID
	 * @param node_id node ID
	 * @return null is not found
	 */
	public IPNode findNode(String machine_id, String node_id) {
		IPMachine machine = findMachineById(machine_id);
		if (machine == null)
			return null;
		return machine.findNode(node_id);
	}
	/** Find machine
	 * @param machine_name
	 * @return
	 */
	public IPMachine findMachine(String machine_name) {
		return (IPMachine) modelManager.getUniverse().findMachineByName(machine_name);
	}
	/** find machine by ID
	 * @param machine_id machine ID
	 * @return
	 */
	public IPMachine findMachineById(String machine_id) {
		IPElement element = modelManager.getUniverse().findChild(machine_id);
		if (element instanceof IPMachine)
			return (IPMachine) element;
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#getName(java.lang.String)
	 */
	public String getName(String id) {
		IPElement element = findMachineById(id);
		if (element == null)
			return "";
		return element.getElementName();
	}
	/** Add machine
	 * @param mac machine
	 */
	public void addMachine(IPMachine mac) {
		if (machineList.containsKey(mac.getIDString()))
			return;

		IPNode[] pNodes = mac.getSortedNodes();
		int total_element = pNodes.length;
		if (total_element > 0) {
			IElementHandler elementHandler = new ElementHandler();
			IElementSet set = elementHandler.getSetRoot();
			for (int i = 0; i < total_element; i++) {
				pNodes[i].addNodeListener(this);
				set.add(new Element(set, pNodes[i].getIDString(), pNodes[i].getElementName()));
			}
			elementHandler.add(set);
			machineList.put(mac.getIDString(), elementHandler);
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#initial()
	 */
	public String initial() {
		IPMachine[] macs = getMachines();
		if (macs.length > 0) {
			cur_machine_id = macs[0].getIDString();
			for (int j = 0; j < macs.length; j++) {
				//System.out.println("testing -- " + macs[j] + ", " + macs[j].getID());
				addMachine(macs[j]);
			}
			setCurrentSetId(IElementHandler.SET_ROOT_ID);
		}
		return cur_machine_id;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.INodeListener#nodeEvent(org.eclipse.ptp.core.INodeEvent)
	 */
	public void nodeEvent(INodeEvent event) {
		// only redraw if the current set contain the node
		if (isCurrentSetContainNode(event.getMachineID(), event.getNodeID())) {
			firePaintListener(null);
		}
	}
	/** Is current set contain node
	 * @param mid machine ID
	 * @param nodeID node iD
	 * @return true if current set contains node
	 */
	public boolean isCurrentSetContainNode(String mid, String nodeID) {
		if (!getCurrentMachineId().equals(mid))
			return false;
		IElementHandler elementHandler = getElementHandler(getCurrentMachineId());
		if (elementHandler == null)
			return false;
		IElementSet set = elementHandler.getSet(getCurrentSetId());
		if (set == null)
			return false;
		return set.contains(nodeID);
	}
}
