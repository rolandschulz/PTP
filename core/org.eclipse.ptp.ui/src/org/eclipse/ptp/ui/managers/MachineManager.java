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
import org.eclipse.ptp.core.elements.IPUniverse;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.attributes.NodeAttributes;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes;
import org.eclipse.ptp.ui.IPTPUIConstants;
import org.eclipse.ptp.ui.model.Element;
import org.eclipse.ptp.ui.model.ElementHandler;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.ptp.ui.model.IElementSet;

/**
 * @author clement chu
 * 
 */
public class MachineManager extends AbstractUIManager {
	private Map<String, IElementHandler> machineElementHandlerList = new HashMap<String, IElementHandler>();
	private Map<String, IPMachine> machineList = new HashMap<String, IPMachine>();
	protected IPMachine cur_machine = null;
	protected final String DEFAULT_TITLE = "Please select a machine";
	
	/** 
	 * Add a new machine to machineList. Add any new nodes to the element
	 * handler for the machine.
	 * 
	 * @param mac machine
	 * @return true if the machine was added
	 */
	public void addMachine(IPMachine mac) {
		if (mac != null) {
			IElementHandler handler;
			if (!machineList.containsKey(mac.getID())) {
				handler = new ElementHandler();
				machineList.put(mac.getID(), mac);
				machineElementHandlerList.put(mac.getID(), handler);
			} else {
				handler = machineElementHandlerList.get(mac.getID());				
			}
			IElementSet set = handler.getSetRoot();
			for (IPNode node : mac.getNodes()) {
				String id = node.getID();
				if (set.getElement(id) == null) {
					set.add(new Element(set, id, node.getName()));
				}
			}
		}
	}
	
	/** Add machine
	 * @param mac machine
	 */
	public void addNode(IPNode node) {
		addMachine(node.getMachine());
		IElementHandler elementHandler = machineElementHandlerList.get(node.getMachine().getID());
		IElementSet set = elementHandler.getSetRoot();
		String id = node.getID();
		if (set.getElement(id) == null) {
			set.add(new Element(set, id, node.getName()));
		}
	}
	
	/**
	 * @param machine
	 */
	public void removeMachine(IPMachine machine) {
		machineList.remove(machine.getID());
		machineElementHandlerList.remove(machine.getID());
		IElementHandler handler = machineElementHandlerList.get(machine.getID());
		if (handler != null) {
			IElementSet set = handler.getSetRoot();
			for (IPNode node : machine.getNodes()) {
				IElement element = set.getElement(node.getID());
				if (element != null) {
					set.remove(element);
				}
			}
		}
		if (cur_machine == machine) {
			cur_machine = null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#clear()
	 */
	public void clear() {
		if (machineList != null) {
			machineList.clear();
			machineElementHandlerList.clear();
		}
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
	
	/** Get current machine ID
	 * @return current machine ID
	 */
	public IPMachine getCurrentMachine() {
		return cur_machine;
	}
	
	public IPMachine findMachineById(String id) {
		return (IPMachine) machineList.get(id);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#getCurrentSetId()
	 */
	public String getCurrentSetId() {
		return cur_set_id;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#getElementHandler(java.lang.String)
	 */
	public IElementHandler getElementHandler(String id) {
		return (IElementHandler) machineElementHandlerList.get(id);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#getFullyQualifiedName(java.lang.String)
	 */
	public String getFullyQualifiedName(String id) {
		if (id.equals(EMPTY_ID)) {
			return DEFAULT_TITLE;
		}
		//TODO check that this is what should happen. Can we just use cur_machine?
		IPMachine machine = getCurrentMachine();
		if (machine != null) {
			IResourceManager rm = machine.getResourceManager();
			if (rm != null) {
				return rm.getName() + ": " + machine.getName();
			}
		}
		return "";
	}
	
	/** Get machines
	 * @return machines
	 */
	public IPMachine[] getMachines() {
		return machineList.values().toArray(new IPMachine[0]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#getName(java.lang.String)
	 */
	public String getName(String id) {
		if (cur_machine == null)
			return "";
		return cur_machine.getName();
	}
	
	/** 
	 * Get node status.
	 * 
	 * Currently the node status is determined as follows:
	 * 	- if the node is up:
	 * 		- if there are *any* running processes on the node: NODE_RUNNING
	 * 		- if there are no running processes, but one or more exited processes on the node: NODE_EXITED
	 *  	- if there is extraState: extraState
	 *  	- if there is no extraState: NODE_UP
	 *  - if the node is down: NODE_DOWN
	 *  - if the node is error: NODE_ERROR
	 *  
	 *  TODO: in the future, the machine view should be linked to the jobs view. The node state should only
	 *  be shown as NODE_RUNNING if any processes belonging to the current job are running.
	 *  
	 * @param node
	 * @return
	 */
	public int getNodeStatus(IPNode node) {
		if (node != null) {
			EnumeratedAttribute<NodeAttributes.State> nodeStateAttr = 
				node.getAttribute(NodeAttributes.getStateAttributeDefinition());
			if(nodeStateAttr == null) {
				return IPTPUIConstants.NODE_UNKNOWN;
			}
			NodeAttributes.State nodeState = nodeStateAttr.getValue();
			
			switch (nodeState) {
			case UP:
				IPProcess[] procs = node.getProcesses();
				if (procs.length > 0) {
					for (IPProcess proc : procs) {
						if (!proc.isTerminated()) {
							return IPTPUIConstants.NODE_RUNNING;
						}
					}
					return IPTPUIConstants.NODE_EXITED;
				}
				
				EnumeratedAttribute<NodeAttributes.ExtraState> extraStateAttr =
					node.getAttribute(NodeAttributes.getExtraStateAttributeDefinition());
				NodeAttributes.ExtraState extraState = NodeAttributes.ExtraState.NONE;
				if (extraStateAttr != null) {
					extraState = extraStateAttr.getValue();
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
	
	/** Get node status text
	 * @param node
	 * @return status text
	 */
	public String getNodeStatusText(IPNode node) {
		if (node == null) {
			return "Unknown";
		}
		EnumeratedAttribute<NodeAttributes.State> nodeStateAttr =
			node.getAttribute(NodeAttributes.getStateAttributeDefinition());
		if(nodeStateAttr == null) {
			return "Unknown";
		}
		NodeAttributes.State nodeState = nodeStateAttr.getValue();
		
		if (nodeState == NodeAttributes.State.UP) {
			if (node.getProcesses().length > 0) {
				if (node.getProcesses()[0].getJob().isTerminated()) {
					return "Exited";
				}
				return "Running";
			}

			EnumeratedAttribute<NodeAttributes.ExtraState> extraStateAttr =
				node.getAttribute(NodeAttributes.getExtraStateAttributeDefinition());
			NodeAttributes.ExtraState extraState = NodeAttributes.ExtraState.NONE;
			if (extraStateAttr != null) {
				extraState = extraStateAttr.getValue();
			}
			
			if (extraState != NodeAttributes.ExtraState.NONE) {
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

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#initial()
	 */
	public IPElement initial(IPUniverse universe) {
		for (IResourceManager rm : universe.getResourceManagers()) {
			for (IPMachine machine : rm.getMachines()) {
				addMachine(machine);
			}
		}

		setCurrentSetId(IElementHandler.SET_ROOT_ID);
		return null;
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
	
	/** Is no machine
	 * @return true if there is no machine
	 */
	public boolean isNoMachine() {
		return cur_machine == null;
	}
	
	/** 
	 * Set current machine ID. If the machine has never been set before, add an entry to
	 * the machineList, and add the nodes to the element handler.
	 * 
	 * @param machine_id machine ID
	 */
	public void setMachine(IPMachine machine) {
		if (machine != cur_machine) {
			cur_machine = machine;
			addMachine(machine);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#setCurrentSetId(java.lang.String)
	 */
	public void setCurrentSetId(String set_id) {
		cur_set_id = set_id;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#shutdown()
	 */
	public void shutdown() {
		clear();
		modelPresentation = null;
		super.shutdown();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#size()
	 */
	public int size() {
		return machineList.size();
	}
}