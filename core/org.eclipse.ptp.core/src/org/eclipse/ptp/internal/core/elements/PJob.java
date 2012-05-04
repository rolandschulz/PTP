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
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.BooleanAttribute;
import org.eclipse.ptp.core.attributes.EnumeratedAttribute;
import org.eclipse.ptp.core.attributes.EnumeratedAttributeDefinition;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.attributes.IntegerAttribute;
import org.eclipse.ptp.core.attributes.StringAttribute;
import org.eclipse.ptp.core.attributes.StringAttributeDefinition;
import org.eclipse.ptp.core.elements.IPElement;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.attributes.AttributeIndexSet;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.elements.attributes.JobAttributes.State;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes;
import org.eclipse.ptp.core.elements.events.IChangedProcessEvent;
import org.eclipse.ptp.core.elements.events.INewProcessEvent;
import org.eclipse.ptp.core.elements.events.IRemoveProcessEvent;
import org.eclipse.ptp.core.elements.listeners.IJobChildListener;
import org.eclipse.ptp.core.util.ProcessOutput;
import org.eclipse.ptp.internal.core.elements.events.ChangedProcessEvent;
import org.eclipse.ptp.internal.core.elements.events.NewProcessEvent;
import org.eclipse.ptp.internal.core.elements.events.RemoveProcessEvent;

public class PJob extends Parent implements IPJob {

	private final ListenerList childListeners = new ListenerList();
	private final BitSet currentProcessJobRanks = new BitSet();
	private final ProcessOutput processOutput;
	private final String fControlId;
	private final Map<IAttributeDefinition<?, ?, ?>, AttributeIndexSet<?>> processAttributesMap = new HashMap<IAttributeDefinition<?, ?, ?>, AttributeIndexSet<?>>();

	public PJob(String id, String controlId, IPElement parent, IAttribute<?, ?, ?>[] attrs) {
		super(id, parent, attrs);

		fControlId = controlId;

		/*
		 * Create required attributes.
		 */
		EnumeratedAttribute<State> jobState = getAttribute(JobAttributes.getStateAttributeDefinition());
		if (jobState == null) {
			jobState = JobAttributes.getStateAttributeDefinition().create();
			addAttribute(jobState);
		}
		BooleanAttribute debugFlag = getAttribute(JobAttributes.getDebugFlagAttributeDefinition());
		if (debugFlag == null) {
			debugFlag = JobAttributes.getDebugFlagAttributeDefinition().create();
			addAttribute(debugFlag);
		}

		/*
		 * handle output
		 */

		processOutput = new ProcessOutput(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPJob#addChildListener(org.eclipse.ptp. core.elements.listeners.IJobProcessListener)
	 */
	public void addChildListener(IJobChildListener listener) {
		childListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elementcontrols.IPJobControl#addProcessAttributes (java.util.BitSet,
	 * org.eclipse.ptp.core.attributes.AttributeManager)
	 */
	public void addProcessAttributes(BitSet processIds, AttributeManager attributes) {
		synchronized (this) {
			// limit the addition of attributes to the current set of
			// child processes
			processIds = (BitSet) processIds.clone();
			processIds.and(currentProcessJobRanks);

			addAttributesForJobRanks(processIds, attributes);
		}
		fireChangedProcesses(processIds, attributes);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elementcontrols.IPJobControl#addProcesses(java.util .BitSet,
	 * org.eclipse.ptp.core.attributes.AttributeManager)
	 */
	public void addProcessesByJobRanks(BitSet newProcessJobRanks, AttributeManager attrs) {
		synchronized (this) {

			// add the processes to the existing set of processes (via BitSets
			// of
			// ids)
			currentProcessJobRanks.or(newProcessJobRanks);

			// add the attributes from the AttributeManager to these processes.
			addAttributesForJobRanks(newProcessJobRanks, attrs);

			// add some that may have been overlooked, that will be needed
			List<IAttribute<?, ?, ?>> requiredAttributes = new ArrayList<IAttribute<?, ?, ?>>(5);
			/*
			 * Create required attributes.
			 */
			EnumeratedAttribute<ProcessAttributes.State> procState = getAttribute(attrs,
					ProcessAttributes.getStateAttributeDefinition());
			if (procState == null) {
				procState = ProcessAttributes.getStateAttributeDefinition().create();
				requiredAttributes.add(procState);
			}
			IntegerAttribute exitCode = getAttribute(attrs, ProcessAttributes.getExitCodeAttributeDefinition());
			if (exitCode == null) {
				try {
					exitCode = ProcessAttributes.getExitCodeAttributeDefinition().create();
					requiredAttributes.add(exitCode);
				} catch (IllegalValueException e) {
				}
			}
			StringAttribute signalName = getAttribute(attrs, ProcessAttributes.getSignalNameAttributeDefinition());
			if (signalName == null) {
				signalName = ProcessAttributes.getSignalNameAttributeDefinition().create();
				requiredAttributes.add(signalName);
			}

			final IAttribute<?, ?, ?>[] requiredAttrs = requiredAttributes.toArray(new IAttribute<?, ?, ?>[0]);
			addAttributesForJobRanks(newProcessJobRanks, new AttributeManager(requiredAttrs));

		} // end synchronized (this)

		fireNewProcesses(newProcessJobRanks);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPJob#getProcessAttribute(org.eclipse.ptp .core.attributes.IAttributeDefinition, int)
	 */
	public synchronized <T, A extends IAttribute<T, A, D>, D extends IAttributeDefinition<T, A, D>> A getProcessAttribute(
			D attributeDefinition, int processJobRank) {
		final BitSet processJobRanks = new BitSet();
		processJobRanks.set(processJobRank);
		final Set<A> attrs = getProcessAttributes(attributeDefinition, processJobRanks);
		if (attrs.isEmpty()) {
			return null;
		}
		return attrs.iterator().next();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPJob#getProcessAttribute(java.lang.String, int)
	 */
	public synchronized IAttribute<?, ?, ?> getProcessAttribute(String attrDefId, int processJobRank) {
		final BitSet processJobRanks = new BitSet();
		processJobRanks.set(processJobRank);
		final Set<IAttribute<?, ?, ?>> attrs = getProcessAttributes(attrDefId, processJobRanks);
		if (attrs.isEmpty()) {
			return null;
		}
		return attrs.iterator().next();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPJob#getProcessAttributeKeys(java.util .BitSet)
	 */
	@SuppressWarnings("unchecked")
	public synchronized Set<IAttributeDefinition<?, ?, ?>> getProcessAttributeKeys(BitSet processJobRanks) {
		final Set<IAttributeDefinition<?, ?, ?>> results = new HashSet<IAttributeDefinition<?, ?, ?>>();
		for (AttributeIndexSet<?> ais : processAttributesMap.values()) {
			final Set<IAttribute<?, ?, ?>> attrs = (Set<IAttribute<?, ?, ?>>) ais.getSubset(processJobRanks).getAttributes();
			for (IAttribute<?, ?, ?> attr : attrs) {
				results.add(attr.getDefinition());
			}
		}
		return results;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPJob#getProcessAttributes(java.util.BitSet )
	 */
	@SuppressWarnings("unchecked")
	public synchronized Set<IAttribute<?, ?, ?>> getProcessAttributes(BitSet processJobRanks) {
		final Set<IAttribute<?, ?, ?>> results = new HashSet<IAttribute<?, ?, ?>>();
		for (AttributeIndexSet<?> ais : processAttributesMap.values()) {
			final Set<IAttribute<?, ?, ?>> attrs = (Set<IAttribute<?, ?, ?>>) ais.getSubset(processJobRanks).getAttributes();
			results.addAll(attrs);
		}
		return results;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPJob#getProcessAttributes(org.eclipse. ptp.core.attributes.IAttributeDefinition,
	 * java.util.BitSet)
	 */
	public synchronized <T, A extends IAttribute<T, A, D>, D extends IAttributeDefinition<T, A, D>> Set<A> getProcessAttributes(
			D attributeDefinition, BitSet processJobRanks) {
		AttributeIndexSet<A> jobRanksForAttr = getAttributeIndexSet(attributeDefinition);
		AttributeIndexSet<A> subSet = jobRanksForAttr.getSubset(processJobRanks);
		return subSet.getAttributes();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPJob#getProcessAttributes(java.lang.String , java.util.BitSet)
	 */
	public synchronized Set<IAttribute<?, ?, ?>> getProcessAttributes(String attrDefId, BitSet processJobRanks) {
		Set<IAttribute<?, ?, ?>> results = new HashSet<IAttribute<?, ?, ?>>();
		for (Entry<IAttributeDefinition<?, ?, ?>, AttributeIndexSet<?>> entry : processAttributesMap.entrySet()) {
			IAttributeDefinition<?, ?, ?> def = entry.getKey();
			if (def.getId().equals(attrDefId)) {
				AttributeIndexSet<?> jobRanksForAttr = entry.getValue();
				AttributeIndexSet<?> subSet = jobRanksForAttr.getSubset(processJobRanks);
				results.addAll(subSet.getAttributes());
				return results;
			}
		}
		return null;
	}

	public synchronized <T, A extends IAttribute<T, A, D>, D extends IAttributeDefinition<T, A, D>> T getProcessAttributeValue(
			int processJobRank, final D def) {
		AttributeIndexSet<A> attrIndexSet = getAttributeIndexSet(def);
		final A attr = attrIndexSet.getAttribute(processJobRank);
		if (attr == null) {
			return null;
		}
		return attr.getValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPJob#getProcessIds()
	 */
	public synchronized BitSet getProcessJobRanks() {
		return (BitSet) currentProcessJobRanks.clone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPJob#getProcessIds(org.eclipse.ptp.core .attributes.IAttribute)
	 */
	public synchronized <T, A extends IAttribute<T, A, D>, D extends IAttributeDefinition<T, A, D>> BitSet getProcessJobRanks(
			A attribute) {
		D def = attribute.getDefinition();
		AttributeIndexSet<A> attrJobRanks = getAttributeIndexSet(def);
		return attrJobRanks.getIndexSet(attribute);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPJob#getProcessName(int)
	 */
	public String getProcessName(int processJobRank) {
		return getName() + "." + processJobRank; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPJob#getProcessNode(int)
	 */
	public String getProcessNodeId(int processJobRank) {
		final StringAttributeDefinition def = ProcessAttributes.getNodeIdAttributeDefinition();
		return getProcessAttributeValue(processJobRank, def);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPJob#getProcessState(int)
	 */
	public ProcessAttributes.State getProcessState(int processJobRank) {
		final EnumeratedAttributeDefinition<ProcessAttributes.State> def = ProcessAttributes.getStateAttributeDefinition();
		return getProcessAttributeValue(processJobRank, def);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPJob#getControlId()
	 */
	public String getControlId() {
		return fControlId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPJob#getSavedOutput(int)
	 */
	public String getSavedOutput(int processJobRank) {
		return processOutput.getSavedOutput(processJobRank);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPJob#getState()
	 */
	public State getState() {
		return getAttribute(JobAttributes.getStateAttributeDefinition()).getValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPJob#hasProcessById(int)
	 */
	public synchronized boolean hasProcessByJobRank(int processJobRank) {
		return currentProcessJobRanks.get(processJobRank);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPJob#hasProcessesByIds(java.util.BitSet)
	 */
	public synchronized boolean hasProcessesByJobRanks(BitSet processJobRanks) {
		BitSet intersection = (BitSet) currentProcessJobRanks.clone();
		intersection.and(processJobRanks);
		boolean containsAll = intersection.equals(processJobRanks);
		return containsAll;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPJob#isDebug()
	 */
	public boolean isDebug() {
		return getAttribute(JobAttributes.getDebugFlagAttributeDefinition()).getValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPJob#removeChildListener(org.eclipse.ptp .core.elements.listeners.IJobProcessListener)
	 */
	public void removeChildListener(IJobChildListener listener) {
		childListeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elementcontrols.IPJobControl#removeProcesses(java .util.BitSet)
	 */
	public void removeProcessesByJobRanks(BitSet processJobRanks) {
		synchronized (this) {

			// remove these processes from the master set
			currentProcessJobRanks.andNot(processJobRanks);

			// remove these processes from each AttributeIndexSet
			final List<AttributeIndexSet<?>> values = new ArrayList<AttributeIndexSet<?>>(processAttributesMap.values());
			for (AttributeIndexSet<?> ais : values) {
				ais.clearIndices(processJobRanks);
			}

			// clear the output file for the processes
			clearOutput(processJobRanks);

		}

		fireRemoveProcesses(processJobRanks);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPJob#setDebug()
	 */
	public void setDebug() {
		BooleanAttribute debug = getAttribute(JobAttributes.getDebugFlagAttributeDefinition());
		debug.setValue(true);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void addAttributesForJobRanks(BitSet processJobRanks, AttributeManager attrs) {
		StringAttribute stdOutAttr = attrs.getAttribute(ProcessAttributes.getStdoutAttributeDefinition());
		if (stdOutAttr != null) {
			addOutput(stdOutAttr.getValue(), processJobRanks);
		}
		for (IAttribute<?, ?, ?> attr : attrs.getAttributes()) {
			// get the process set for the attributes
			// corresponding to attr's definition
			AttributeIndexSet attrIds = getAttributeIndexSet(attr.getDefinition());
			// add the procces' ranks to that attribute
			attrIds.addIndicesToAttribute(attr, processJobRanks);
		}
	}

	/**
	 * @param output
	 * @param processJobRanks
	 */
	private void addOutput(String output, BitSet processJobRanks) {
		processOutput.addOutput(output, processJobRanks);
	}

	/**
	 * @param processJobRanks
	 * 
	 */
	private void clearOutput(BitSet processJobRanks) {
		final AttributeIndexSet<StringAttribute> outAttrs = getAttributeIndexSet(ProcessAttributes.getStdoutAttributeDefinition());

		// if all of the processes have had their output cleared
		// then delete the output file
		outAttrs.clearIndices(processJobRanks);
		if (outAttrs.isEmpty()) {
			processOutput.delete();
		}
	}

	/**
	 * Send IChangedProcessEvent to registered listeners
	 * 
	 * @param processes
	 * @param attributes
	 */
	private void fireChangedProcesses(BitSet processes, AttributeManager attributes) {
		IChangedProcessEvent e = new ChangedProcessEvent(this, this, processes, attributes);

		for (Object listener : childListeners.getListeners()) {
			((IJobChildListener) listener).handleEvent(e);
		}
	}

	/**
	 * Notify listeners when a new processes are created.
	 * 
	 * @param processes
	 */
	private void fireNewProcesses(BitSet processes) {
		INewProcessEvent e = new NewProcessEvent(this, this, processes);

		for (Object listener : childListeners.getListeners()) {
			((IJobChildListener) listener).handleEvent(e);
		}
	}

	/**
	 * Notify listeners when the collection of processes are removed.
	 * 
	 * @param processes
	 *            to remove
	 */
	private void fireRemoveProcesses(BitSet processes) {
		IRemoveProcessEvent e = new RemoveProcessEvent(this, this, processes);

		for (Object listener : childListeners.getListeners()) {
			((IJobChildListener) listener).handleEvent(e);
		}
	}

	/**
	 * @param attrs
	 * @param def
	 * @return
	 */
	private <T, A extends IAttribute<T, A, D>, D extends IAttributeDefinition<T, A, D>> A getAttribute(AttributeManager attrs, D def) {
		return attrs.getAttribute(def);
	}

	/**
	 * @param def
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private synchronized <T, A extends IAttribute<T, A, D>, D extends IAttributeDefinition<T, A, D>> AttributeIndexSet<A> getAttributeIndexSet(
			D def) {
		AttributeIndexSet<A> attributeIndexSet = (AttributeIndexSet<A>) processAttributesMap.get(def);
		if (attributeIndexSet == null) {
			attributeIndexSet = new AttributeIndexSet<A>();
			processAttributesMap.put(def, attributeIndexSet);
		}
		return attributeIndexSet;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.core.elements.PElement#doAddAttributeHook(org .eclipse.ptp.core.attributes.AttributeManager)
	 */
	@Override
	protected void doAddAttributeHook(AttributeManager attrs) {
	}
}
