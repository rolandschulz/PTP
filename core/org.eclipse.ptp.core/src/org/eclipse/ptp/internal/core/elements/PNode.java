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
package org.eclipse.ptp.internal.core.elements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.EnumeratedAttribute;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IntegerAttribute;
import org.eclipse.ptp.core.elementcontrols.IPElementControl;
import org.eclipse.ptp.core.elementcontrols.IPMachineControl;
import org.eclipse.ptp.core.elementcontrols.IPNodeControl;
import org.eclipse.ptp.core.elementcontrols.IPProcessControl;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPMachine;
import org.eclipse.ptp.core.elements.IPProcess;
import org.eclipse.ptp.core.elements.attributes.NodeAttributes;
import org.eclipse.ptp.core.elements.attributes.NodeAttributes.State;
import org.eclipse.ptp.core.elements.events.IChangedProcessEvent;
import org.eclipse.ptp.core.elements.events.INewProcessEvent;
import org.eclipse.ptp.core.elements.events.INodeChangeEvent;
import org.eclipse.ptp.core.elements.events.IRemoveProcessEvent;
import org.eclipse.ptp.core.elements.listeners.IJobChildListener;
import org.eclipse.ptp.core.elements.listeners.INodeChildListener;
import org.eclipse.ptp.core.elements.listeners.INodeListener;
import org.eclipse.ptp.internal.core.elements.events.ChangedProcessEvent;
import org.eclipse.ptp.internal.core.elements.events.NewProcessEvent;
import org.eclipse.ptp.internal.core.elements.events.NodeChangeEvent;
import org.eclipse.ptp.internal.core.elements.events.RemoveProcessEvent;

public class PNode extends Parent implements IPNodeControl, IJobChildListener {
	
	private final ListenerList elementListeners = new ListenerList();
	private final ListenerList childListeners = new ListenerList();

	public PNode(String id, IPMachineControl mac, IAttribute<?,?,?>[] attrs) {
		super(id, mac, P_NODE, attrs);
		/*
		 * Create required attributes.
		 */
		EnumeratedAttribute<State> nodeState = getAttribute(NodeAttributes.getStateAttributeDefinition());
		if (nodeState == null) {
			nodeState = NodeAttributes.getStateAttributeDefinition().create();
			addAttribute(nodeState);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPNode#addChildListener(org.eclipse.ptp.core.elements.listeners.INodeProcessListener)
	 */
	public void addChildListener(INodeChildListener listener) {
		childListeners.add(listener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPNode#addElementListener(org.eclipse.ptp.core.elements.listeners.INodeListener)
	 */
	public void addElementListener(INodeListener listener) {
		elementListeners.add(listener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elementcontrols.IPNodeControl#addProcess(org.eclipse.ptp.core.elementcontrols.IPProcessControl)
	 */
	public void addProcesses(Collection<IPProcessControl> processControls) {
		List<IPProcess> procs = new ArrayList<IPProcess>(processControls.size());
		Set<IPJob> jobs = new HashSet<IPJob>();
		
		for (IPProcessControl process : processControls) {
			/*
			 * Add the process as a child of the node
			 */
			addChild(process);
			
			/*
			 * Add this node to the process
			 */
			process.addNode(this);
			
			/*
			 * Find the set of jobs that started these processes
			 */
			jobs.add(process.getJob());
			
			/*
			 * Add the process to the event list
			 */
			procs.add(process);
		}
		
		/*
		 * Add this node to the listeners for job child events. This is so
		 * we can forward IChangedProcess events to the INodeChildListers.
		 */
		for (IPJob job : jobs) {
			job.addChildListener(this);
		}
		
		/*
		 * Fire the INewProcess event for these processes
		 */
		fireNewProcesses(procs);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elementcontrols.IPNodeControl#getMachineControl()
	 */
	public IPMachine getMachine() {
		return getMachineControl();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elementcontrols.IPNodeControl#getMachineControl()
	 */
	public IPMachineControl getMachineControl() {
		IPElementControl current = this;
		do {
			if (current instanceof IPMachineControl) {
				return (IPMachineControl) current;
			}
		} while ((current = current.getParent()) != null);
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPNode#getNodeNumber()
	 */
	public String getNodeNumber() {
		IntegerAttribute num = getAttribute(NodeAttributes.getNumberAttributeDefinition());
		if (num != null) {
			return num.getValueAsString();
		}
		return "";
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elementcontrols.IPNodeControl#getProcessControls()
	 */
	public Collection<IPProcessControl> getProcessControls() {
		IPElementControl[] children = getChildren();
		List<IPProcessControl> processes =
			new ArrayList<IPProcessControl>(children.length);
		for (IPElementControl element : children) {
			processes.add((IPProcessControl)element);
		}
		return processes;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPNode#getProcesses()
	 */
	public IPProcess[] getProcesses() {
		return getProcessControls().toArray(new IPProcess[getProcessControls().size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPNode#getState()
	 */
	public State getState() {
		return getAttribute(NodeAttributes.getStateAttributeDefinition()).getValue();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.listeners.IJobChildListener#handleEvent(org.eclipse.ptp.core.elements.events.IChangedProcessEvent)
	 */
	public void handleEvent(IChangedProcessEvent e) {
		fireChangedProcesses(e.getProcesses());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.listeners.IJobChildListener#handleEvent(org.eclipse.ptp.core.elements.events.IRemoveProcessEvent)
	 */
	public void handleEvent(IRemoveProcessEvent e) {
		for (IPProcess process : e.getProcesses()) {
			removeChild((IPProcessControl)process);
		}
		
		fireRemoveProcesses(e.getProcesses());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.listeners.IJobChildListener#handleEvent(org.eclipse.ptp.core.elements.events.INewProcessEvent)
	 */
	public void handleEvent(INewProcessEvent e) {
		// Do nothing
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPNode#removeChildListener(org.eclipse.ptp.core.elements.listeners.INodeProcessListener)
	 */
	public void removeChildListener(INodeChildListener listener) {
		childListeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPNode#removeElementListener(org.eclipse.ptp.core.elements.listeners.INodeListener)
	 */
	public void removeElementListener(INodeListener listener) {
		elementListeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elementcontrols.IPNodeControl#removeProcess(org.eclipse.ptp.core.elementcontrols.IPProcessControl)
	 */
	public void removeProcesses(Collection<IPProcessControl> processControls) {
		List<IPProcess> processes = new ArrayList<IPProcess>(processControls.size());
		
		for (IPProcessControl process : processControls) {
			removeChild(process);
			process.removeNode();
			processes.add(process);
		}
		
		fireRemoveProcesses(processes);
	}

	/**
	 * Notify listeners when a node attribute has changed.
	 * 
	 * @param attrs
	 */
	private void fireChangedNode(AttributeManager attrs) {
		INodeChangeEvent e = new NodeChangeEvent(this, attrs);
		
		for (Object listener : elementListeners.getListeners()) {
			((INodeListener)listener).handleEvent(e);
		}
	}
	
	/**
	 * Send INewProcessEvent to registered listeners
	 * 
	 * @param process
	 */
	private void fireNewProcesses(Collection<IPProcess> processes) {
		INewProcessEvent e = 
			new NewProcessEvent(this, processes);
		
		for (Object listener : childListeners.getListeners()) {
			((INodeChildListener)listener).handleEvent(e);
		}
	}

	/**
	 * @param process
	 */
	private void fireRemoveProcesses(Collection<IPProcess> processes) {
		IRemoveProcessEvent e = 
			new RemoveProcessEvent(this, processes);
		
		for (Object listener : childListeners.getListeners()) {
			((INodeChildListener)listener).handleEvent(e);
		}
	}

	/**
	 * Send IChangedProcessEvent to registered listeners
	 * 
	 * @param nodes
	 */
	private void fireChangedProcesses(Collection<IPProcess> processes) {
		IChangedProcessEvent e = 
			new ChangedProcessEvent(this, processes);
		
		for (Object listener : childListeners.getListeners()) {
			((INodeChildListener)listener).handleEvent(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.core.elements.PElement#doAddAttributeHook(java.util.Map)
	 */
	@Override
	protected void doAddAttributeHook(AttributeManager attrs) {
		fireChangedNode(attrs);
	}

}
