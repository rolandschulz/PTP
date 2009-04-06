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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.ptp.core.attributes.EnumeratedAttribute;
import org.eclipse.ptp.core.elements.IPElement;
import org.eclipse.ptp.core.elements.IPMachine;
import org.eclipse.ptp.core.elements.IPNode;
import org.eclipse.ptp.core.elements.IPProcess;
import org.eclipse.ptp.core.elements.IPUniverse;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.attributes.NodeAttributes;
import org.eclipse.ptp.ui.messages.Messages;
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
	protected final String DEFAULT_TITLE = Messages.MachineManager_0;
	
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
			List<IElement> elements = new ArrayList<IElement>();
			IElementSet set = handler.getSetRoot();
			for (IPNode node : mac.getNodes()) {
				elements.add(createNodeElement(set, node.getID(), node.getName(), node));
			}
			set.addElements(elements.toArray(new IElement[0]));
		}
	}
	
	/** Add machine
	 * @param mac machine
	 */
	public void addNode(IPNode node) {
		addMachine(node.getMachine());
		IElementHandler elementHandler = machineElementHandlerList.get(node.getMachine().getID());
		IElementSet set = elementHandler.getSetRoot();
		set.addElements(new IElement[] { createNodeElement(set, node.getID(), node.getName(), node) });
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
	
	/**
	 * @param id
	 * @return
	 */
	public IPMachine findMachineById(String id) {
		return (IPMachine) machineList.get(id);
	}
	
	/** Find node 
	 * @param node_id node ID
	 * @return null is not found
	 */
	public IPNode findNode(String node_id) {
		IPMachine machine = getCurrentMachine();
		if (machine == null) {
			System.out.println(Messages.MachineManager_1);
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
				return rm.getName() + ": " + machine.getName(); //$NON-NLS-1$
			}
		}
		return ""; //$NON-NLS-1$
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
			return ""; //$NON-NLS-1$
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
			NodeAttributes.State nodeState = node.getState();
			
			EnumeratedAttribute<NodeAttributes.ExtraState> extraStateAttr = 
				node.getAttribute(NodeAttributes.getExtraStateAttributeDefinition());
			
			if (extraStateAttr != null) {
				return NodeAttributes.State.values().length + extraStateAttr.getValueIndex();
			}
			
			// FIXME: this should be provided by the proxy
			
			if (nodeState == NodeAttributes.State.UP) {
				IPProcess[] procs = node.getProcesses();
				if (procs.length > 0) {
					for (IPProcess proc : procs) {
						if (!proc.isTerminated()) {
							return NodeAttributes.State.values().length + NodeAttributes.ExtraState.RUNNING_PROCESS.ordinal();
						}
					}
					return NodeAttributes.State.values().length + NodeAttributes.ExtraState.EXITED_PROCESS.ordinal();
				}
				
			}
			
			return nodeState.ordinal();
		}
		return NodeAttributes.State.UNKNOWN.ordinal();
	}
	
	/** Get node status text
	 * @param node
	 * @return status text
	 */
	public String getNodeStatusText(IPNode node) {
		if (node == null) {
			return Messages.MachineManager_2;
		}
		EnumeratedAttribute<NodeAttributes.State> nodeStateAttr =
			node.getAttribute(NodeAttributes.getStateAttributeDefinition());
		if(nodeStateAttr == null) {
			return Messages.MachineManager_2;
		}
		NodeAttributes.State nodeState = nodeStateAttr.getValue();
		
		if (nodeState == NodeAttributes.State.UP) {
			if (node.getProcesses().length > 0) {
				if (node.getProcesses()[0].getJob().isTerminated()) {
					return Messages.MachineManager_3;
				}
				return Messages.MachineManager_4;
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
			IElementSet set = (IElementSet)elementHandler.getElementByID(getCurrentSetId());
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
	 * @param machine
	 */
	public void removeMachine(IPMachine machine) {
		machineList.remove(machine.getID());
		machineElementHandlerList.remove(machine.getID());
		IElementHandler handler = machineElementHandlerList.get(machine.getID());
		if (handler != null) {
			IElementSet set = handler.getSetRoot();
			for (IPNode node : machine.getNodes()) {
				IElement element = set.getElementByID(node.getID());
				if (element != null) {
					set.removeElement(node.getID());
				}
			}
		}
		if (cur_machine == machine) {
			cur_machine = null;
		}
	}	
	
	/** Add machine
	 * @param mac machine
	 */
	public void removeNode(IPNode node) {
		IElementHandler elementHandler = machineElementHandlerList.get(node.getMachine().getID());
		IElementSet set = elementHandler.getSetRoot();
		IElement element = set.getElementByID(node.getID());
		if (element != null) {
			set.removeElement(node.getID());
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#setCurrentSetId(java.lang.String)
	 */
	public void setCurrentSetId(String set_id) {
		cur_set_id = set_id;
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
	
	/**
	 * @param set
	 * @param key
	 * @param name
	 * @return
	 */
	protected IElement createNodeElement(IElementSet set, String key, String name, IPNode node) {
		return new Element(set, key, name, node) {
			public int compareTo(IElement e) {
				return getID().compareTo(e.getID());
			}
		};
	}
}