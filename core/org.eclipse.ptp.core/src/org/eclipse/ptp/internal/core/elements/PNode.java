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

import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.EnumeratedAttribute;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IntegerAttribute;
import org.eclipse.ptp.core.elements.IPElement;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPMachine;
import org.eclipse.ptp.core.elements.IPNode;
import org.eclipse.ptp.core.elements.attributes.NodeAttributes;
import org.eclipse.ptp.core.elements.attributes.NodeAttributes.State;
import org.eclipse.ptp.core.elements.events.IChangedProcessEvent;
import org.eclipse.ptp.core.elements.events.INewProcessEvent;
import org.eclipse.ptp.core.elements.events.INodeChangeEvent;
import org.eclipse.ptp.core.elements.events.IRemoveProcessEvent;
import org.eclipse.ptp.core.elements.listeners.IJobChildListener;
import org.eclipse.ptp.core.elements.listeners.INodeChildListener;
import org.eclipse.ptp.core.elements.listeners.INodeListener;
import org.eclipse.ptp.core.messages.Messages;
import org.eclipse.ptp.internal.core.elements.events.ChangedProcessEvent;
import org.eclipse.ptp.internal.core.elements.events.NewProcessEvent;
import org.eclipse.ptp.internal.core.elements.events.NodeChangeEvent;
import org.eclipse.ptp.internal.core.elements.events.RemoveProcessEvent;

public class PNode extends Parent implements IPNode, IJobChildListener {

	private final ListenerList childListeners = new ListenerList();
	private final ListenerList elementListeners = new ListenerList();
	// discover which job ranks for a given job are running on this node
	private final Map<IPJob, BitSet> jobProcessRanksMap = new HashMap<IPJob, BitSet>();

	public PNode(String id, IPMachine mac, IAttribute<?, ?, ?>[] attrs) {
		super(id, mac, attrs);
		/*
		 * Create required attributes.
		 */
		EnumeratedAttribute<State> nodeState = getAttribute(NodeAttributes.getStateAttributeDefinition());
		if (nodeState == null) {
			nodeState = NodeAttributes.getStateAttributeDefinition().create();
			addAttribute(nodeState);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.IPNode#addChildListener(org.eclipse.ptp
	 * .core.elements.listeners.INodeProcessListener)
	 */
	public void addChildListener(INodeChildListener listener) {
		childListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.IPNode#addElementListener(org.eclipse.ptp
	 * .core.elements.listeners.INodeListener)
	 */
	public void addElementListener(INodeListener listener) {
		elementListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elementcontrols.IPNodeControl#addJobProcessRanks
	 * (org.eclipse.ptp.core.elementcontrols.IPJob, java.util.BitSet)
	 */
	public void addJobProcessRanks(IPJob job, BitSet processRanks) {
		// add the incoming process indices to the
		// existing process indices for this job
		BitSet jobProcessRanks = jobProcessRanksMap.get(job);
		if (jobProcessRanks == null) {
			jobProcessRanks = new BitSet();
			jobProcessRanksMap.put(job, jobProcessRanks);
		}
		jobProcessRanks.or(processRanks);

		/*
		 * Add this node to the listeners for job child events. This is so we
		 * can forward IChangedProcess events to the INodeChildListers.
		 */
		job.addChildListener(this);

		/*
		 * Fire the INewProcess event for these processes
		 */
		fireNewProcesses(job, processRanks);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.core.elements.PElement#doAddAttributeHook(java
	 * .util.Map)
	 */
	@Override
	protected void doAddAttributeHook(AttributeManager attrs) {
		fireChangedNode(attrs);
	}

	/**
	 * Notify listeners when a node attribute has changed.
	 * 
	 * @param attrs
	 */
	private void fireChangedNode(AttributeManager attrs) {
		INodeChangeEvent e = new NodeChangeEvent(this, attrs);

		for (Object listener : elementListeners.getListeners()) {
			((INodeListener) listener).handleEvent(e);
		}
	}

	/**
	 * Send IChangedProcessEvent to registered listeners
	 * 
	 * @param job
	 * @param processes
	 * @param attrManager
	 */
	private void fireChangedProcesses(IPJob job, BitSet processes, AttributeManager attrManager) {
		IChangedProcessEvent e = new ChangedProcessEvent(this, job, processes, attrManager);

		for (Object listener : childListeners.getListeners()) {
			((INodeChildListener) listener).handleEvent(e);
		}
	}

	/**
	 * Send INewProcessEvent to registered listeners
	 * 
	 * @param job
	 *            the job that possesses these processes
	 * @param processes
	 *            indices of added processes
	 */
	private void fireNewProcesses(IPJob job, BitSet processes) {
		INewProcessEvent e = new NewProcessEvent(this, job, processes);

		for (Object listener : childListeners.getListeners()) {
			((INodeChildListener) listener).handleEvent(e);
		}
	}

	/**
	 * @param job
	 *            the job that used to posses these processes
	 * @param processes
	 *            indices of removed processes
	 */
	private void fireRemoveProcesses(IPJob job, BitSet processes) {
		IRemoveProcessEvent e = new RemoveProcessEvent(this, job, processes);

		for (Object listener : childListeners.getListeners()) {
			((INodeChildListener) listener).handleEvent(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.IPNode#getJobProcessRanks(org.eclipse.ptp
	 * .core.elements.IPJob)
	 */
	public BitSet getJobProcessRanks(IPJob job) {
		return (BitSet) jobProcessRanksMap.get(job).clone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPNode#getJobs()
	 */
	public Set<? extends IPJob> getJobs() {
		return Collections.unmodifiableSet(jobProcessRanksMap.keySet());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elementcontrols.IPNodeControl#getMachineControl()
	 */
	public IPMachine getMachine() {
		IPElement current = this;
		do {
			if (current instanceof IPMachine) {
				return (IPMachine) current;
			}
		} while ((current = current.getParent()) != null);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPNode#getNodeNumber()
	 */
	public String getNodeNumber() {
		IntegerAttribute num = getAttribute(NodeAttributes.getNumberAttributeDefinition());
		if (num != null) {
			return num.getValueAsString();
		}
		return ""; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPNode#getState()
	 */
	public State getState() {
		return getAttribute(NodeAttributes.getStateAttributeDefinition()).getValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.listeners.IJobChildListener#handleEvent
	 * (org.eclipse.ptp.core.elements.events.IChangedProcessEvent)
	 */
	public void handleEvent(IChangedProcessEvent e) {
		fireChangedProcesses((IPJob) e.getSource(), e.getProcesses(), e.getAttributes());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.listeners.IJobChildListener#handleEvent
	 * (org.eclipse.ptp.core.elements.events.INewProcessEvent)
	 */
	public void handleEvent(INewProcessEvent e) {
		// Do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.listeners.IJobChildListener#handleEvent
	 * (org.eclipse.ptp.core.elements.events.IRemoveProcessEvent)
	 */
	public void handleEvent(IRemoveProcessEvent e) {
		removeJobProcessRanks(e.getJob(), e.getProcesses());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.IPNode#removeChildListener(org.eclipse.
	 * ptp.core.elements.listeners.INodeProcessListener)
	 */
	public void removeChildListener(INodeChildListener listener) {
		childListeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.IPNode#removeElementListener(org.eclipse
	 * .ptp.core.elements.listeners.INodeListener)
	 */
	public void removeElementListener(INodeListener listener) {
		elementListeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elementcontrols.IPNodeControl#removeJobProcessRanks
	 * (org.eclipse.ptp.core.elementcontrols.IPJob, java.util.BitSet)
	 */
	public void removeJobProcessRanks(IPJob job, BitSet ranks) {
		// remove the removed process indices from the jobProcesses's set of
		// process indices for this job.

		final BitSet jobProcessRanks = jobProcessRanksMap.get(job);
		if (jobProcessRanks == null) {
			PTPCorePlugin.log(Messages.PNode_1 + job.getName());
		} else {
			jobProcessRanks.andNot(ranks);
		}

		fireRemoveProcesses(job, ranks);
	}

}
