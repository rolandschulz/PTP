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
package org.eclipse.ptp.ui.managers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ptp.core.attributes.EnumeratedAttribute;
import org.eclipse.ptp.core.elements.IPElement;
import org.eclipse.ptp.core.elements.IPMachine;
import org.eclipse.ptp.core.elements.IPNode;
import org.eclipse.ptp.core.elements.IPProcess;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.attributes.NodeAttributes;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes;
import org.eclipse.ptp.core.elements.attributes.NodeAttributes.ExtraState;
import org.eclipse.ptp.core.elements.attributes.NodeAttributes.State;
import org.eclipse.ptp.ui.IPTPUIConstants;
import org.eclipse.ptp.ui.model.Element;
import org.eclipse.ptp.ui.model.ElementHandler;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.ptp.ui.model.IElementSet;

/**
 * @author clement chu
 * 
 */
public class MachineManager extends AbstractUIManager {
	private Map<String, IElementHandler> machineList = new HashMap<String, IElementHandler>();
	protected IPMachine cur_machine = null;
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#shutdown()
	 */
	public void shutdown() {
		clear();
		modelPresentation = null;
		super.shutdown();
	}
	/** Is no machine
	 * @return true if there is no machine
	 */
	public boolean isNoMachine() {
		return cur_machine == null;
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
		if (cur_machine != null) {
			return cur_machine.getResourceManager().getMachines();
		}
		return new IPMachine[] {};
	}
	
	/** Get current machine ID
	 * @return current machine ID
	 */
	public IPMachine getCurrentMachine() {
		return cur_machine;
	}
	/** Set current machine ID
	 * @param machine_id machine ID
	 */
	public void setCurrentMachine(IPMachine machine) {
		cur_machine = machine;
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
	
	/** Get node status text
	 * @param node
	 * @return status text
	 */
	public String getNodeStatusText(IPNode node) {
		if (node == null) {
			return "Unknown";
		}
		EnumeratedAttribute nodeStateAttr = (EnumeratedAttribute) node.getAttribute(NodeAttributes.getStateAttributeDefinition());
		if(nodeStateAttr == null) {
			return "Unknown";
		}
		NodeAttributes.State nodeState = (State) nodeStateAttr.getEnumValue();
		
		if (nodeState == State.UP) {
			if (node.getProcesses().length > 0) {
				if (node.getProcesses()[0].getJob().isTerminated()) {
					return "Exited";
				}
				return "Running";
			}

			EnumeratedAttribute extraStateAttr = (EnumeratedAttribute) node.getAttribute(NodeAttributes.getExtraStateAttributeDefinition());
			NodeAttributes.ExtraState extraState = ExtraState.NONE;
			if (extraStateAttr != null) {
				extraState = (ExtraState) extraStateAttr.getEnumValue();
			}
			
			if (extraState != ExtraState.NONE) {
				return extraState.toString();
			}
		}
		return nodeState.toString();
	}
	/** Get process status
	 * @param state
	 * @return status
	 */
	public int getProcStatus(ProcessAttributes.State state) {
		switch (state) {
		case STARTING:
			return IPTPUIConstants.PROC_STARTING;
		case RUNNING:
			return IPTPUIConstants.PROC_RUNNING;
		case EXITED:
			return IPTPUIConstants.PROC_EXITED;
		case EXITED_SIGNALLED:
			return IPTPUIConstants.PROC_EXITED_SIGNAL;
		case STOPPED:
			return IPTPUIConstants.PROC_STOPPED;
		case ERROR:
		default:
			return IPTPUIConstants.PROC_ERROR;
		}
	}
	/** Get node status
	 * @param node
	 * @return
	 */
	public int getNodeStatus(IPNode node) {
		if (node != null) {
			EnumeratedAttribute nodeStateAttr = (EnumeratedAttribute) node.getAttribute(NodeAttributes.getStateAttributeDefinition());
			if(nodeStateAttr == null) {
				return IPTPUIConstants.NODE_UNKNOWN;
			}
			NodeAttributes.State nodeState = (State) nodeStateAttr.getEnumValue();
			
			switch (nodeState) {
			case UP:
				IPProcess[] procs = node.getProcesses();
				if (node.getProcesses().length > 0) {
					if (node.getProcesses()[0].getJob().isTerminated()) {
						return IPTPUIConstants.NODE_EXITED;
					}
					return IPTPUIConstants.NODE_RUNNING;
				}
				
				EnumeratedAttribute extraStateAttr = (EnumeratedAttribute) node.getAttribute(NodeAttributes.getExtraStateAttributeDefinition());
				NodeAttributes.ExtraState extraState = ExtraState.NONE;
				if (extraStateAttr != null) {
					extraState = (ExtraState) extraStateAttr.getEnumValue();
				}
				
				switch (extraState) {
				case USER_ALLOC_EXCL:
					return IPTPUIConstants.NODE_USER_ALLOC_EXCL;
				case USER_ALLOC_SHARED:
					return IPTPUIConstants.NODE_USER_ALLOC_SHARED;
				case OTHER_ALLOC_EXCL:
					return IPTPUIConstants.NODE_OTHER_ALLOC_EXCL;
				case OTHER_ALLOC_SHARED:
					return IPTPUIConstants.NODE_OTHER_ALLOC_SHARED;
				}
				return IPTPUIConstants.NODE_UP;
			case DOWN:
				return IPTPUIConstants.NODE_DOWN;
			case ERROR:
				return IPTPUIConstants.NODE_ERROR;
			}
		}
		return IPTPUIConstants.NODE_UNKNOWN;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#getStatus(java.lang.String)
	 */
	public int getStatus(String id) {
		IPMachine machine = getCurrentMachine();
		if (machine != null) {
			return getNodeStatus(machine.getNodeById(id));
		}
		return getNodeStatus(null);
	}

	/** Find node 
	 * @param node_id node ID
	 * @return null is not found
	 */
	public IPNode findNode(String node_id) {
		IPMachine machine = getCurrentMachine();
		if (machine == null) {
			System.out.println("\t*** POSSIBLE ERROR: Unable to find machine");
			return null;
		}
		return machine.getNodeById(node_id);
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#getName(java.lang.String)
	 */
	public String getName(String id) {
		if (cur_machine == null)
			return "";
		return cur_machine.getName();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#getFullyQualifiedName(java.lang.String)
	 */
	public String getFullyQualifiedName(String id) {
		//TODO check that this is what should happen. Can we just use cur_machine?
		IPMachine machine = getCurrentMachine();
		if (machine != null) {
			IResourceManager rm = machine.getResourceManager();
			IPMachine machine2 = rm.getMachineById(id);
			if (machine2 != null) {
				return rm.getName() + ": " + machine2.getName();
			}
		}
		return "";
	}
	
	/** Add machine
	 * @param mac machine
	 */
	public void addMachine(IPMachine mac) {
		if (machineList.containsKey(mac.getID()))
			return;

		IElementHandler elementHandler = new ElementHandler();
		machineList.put(mac.getID(), elementHandler);
	}
	
	/** Add machine
	 * @param mac machine
	 */
	public void addNode(IPNode node) {
		addMachine(node.getMachine());
		IElementHandler elementHandler = machineList.get(node.getMachine().getID());
		IElementSet set = elementHandler.getSetRoot();
		set.add(new Element(set, node.getID(), node.getName()));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#initial()
	 */
	public IPElement initial() {
		IPMachine[] macs = getMachines();
		if (macs.length > 0) {
			cur_machine = macs[0];
			for (int j = 0; j < macs.length; j++) {
				addMachine(macs[j]);
			}
			setCurrentSetId(IElementHandler.SET_ROOT_ID);
		}
		return cur_machine;
	}
	/** Is current set contain node
	 * @param mid machine ID
	 * @param nodeID node iD
	 * @return true if current set contains node
	 */
	public boolean isCurrentSetContainNode(String mid, String nodeID) {
		IPMachine machine = getCurrentMachine();
		if (machine != null) {
			if (!machine.getID().equals(mid))
				return false;
			IElementHandler elementHandler = getElementHandler(machine.getID());
			if (elementHandler == null)
				return false;
			IElementSet set = elementHandler.getSet(getCurrentSetId());
			if (set == null)
				return false;
			return set.contains(nodeID);
		}
		return false;
	}
}