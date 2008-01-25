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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.core.attributes.BooleanAttribute;
import org.eclipse.ptp.core.attributes.EnumeratedAttribute;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.elementcontrols.IPElementControl;
import org.eclipse.ptp.core.elementcontrols.IPJobControl;
import org.eclipse.ptp.core.elementcontrols.IPProcessControl;
import org.eclipse.ptp.core.elementcontrols.IPQueueControl;
import org.eclipse.ptp.core.elements.IPProcess;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.elements.attributes.JobAttributes.State;
import org.eclipse.ptp.core.elements.events.IChangedProcessEvent;
import org.eclipse.ptp.core.elements.events.IJobChangeEvent;
import org.eclipse.ptp.core.elements.events.INewProcessEvent;
import org.eclipse.ptp.core.elements.events.IRemoveProcessEvent;
import org.eclipse.ptp.core.elements.listeners.IJobChildListener;
import org.eclipse.ptp.core.elements.listeners.IJobListener;
import org.eclipse.ptp.internal.core.elements.events.ChangedProcessEvent;
import org.eclipse.ptp.internal.core.elements.events.JobChangeEvent;
import org.eclipse.ptp.internal.core.elements.events.NewProcessEvent;
import org.eclipse.ptp.internal.core.elements.events.RemoveProcessEvent;

public class PJob extends Parent implements IPJobControl {
	private final ListenerList elementListeners = new ListenerList();
	private final ListenerList childListeners = new ListenerList();
	private HashMap<String, IPProcessControl> indexMap = 
		new HashMap<String, IPProcessControl>();
	private ILaunchConfiguration configuration;
	
	public PJob(String id, IPQueueControl queue, IAttribute<?,?,?>[] attrs) {
		super(id, queue, P_JOB, attrs);
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
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPJob#addChildListener(org.eclipse.ptp.core.elements.listeners.IJobProcessListener)
	 */
	public void addChildListener(IJobChildListener listener) {
		childListeners.add(listener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPJob#addElementListener(org.eclipse.ptp.core.elements.listeners.IJobListener)
	 */
	public void addElementListener(IJobListener listener) {
		elementListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elementcontrols.IPJobControl#addProcessAttributes(java.util.Collection, org.eclipse.ptp.core.attributes.IAttribute<?,?,?>[])
	 */
	public void addProcessAttributes(Collection<IPProcessControl> processControls,
			IAttribute<?, ?, ?>[] attrs) {
		List<IPProcess> processes = new ArrayList<IPProcess>(processControls.size());
		
		for (IPProcessControl process : processControls) {
			process.addAttributes(attrs);
			processes.add(process);
		}
		
		fireChangedProcesses(processes);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elementcontrols.IPJobControl#addProcesses(java.util.Collection)
	 */
	public void addProcesses(Collection<IPProcessControl> processControls) {
		List<IPProcess> processes = new ArrayList<IPProcess>(processControls.size());

		for (IPProcessControl process : processControls) {
			addChild(process);
			String idx = process.getProcessIndex();
			if (idx != null) {
				indexMap.put(idx, process);
			}
			processes.add(process);
		}
		
		fireNewProcesses(processes);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPJob#getLaunchConfiguration()
	 */
	public synchronized ILaunchConfiguration getLaunchConfiguration() {
		return configuration;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPJob#getProcessById(java.lang.String)
	 */
	public synchronized IPProcess getProcessById(String id) {
		IPElementControl element = findChild(id);
		if (element != null)
			return (IPProcessControl) element;
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPJob#getProcessByIndex(int)
	 */
	public synchronized IPProcess getProcessByIndex(int index) {
		return indexMap.get(String.valueOf(index));
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPJob#getProcessByIndex(java.lang.String)
	 */
	public synchronized IPProcess getProcessByIndex(String index) {
		return indexMap.get(index);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elementcontrols.IPJobControl#getProcessControls()
	 */
	public synchronized Collection<IPProcessControl> getProcessControls() {
		List<IPProcessControl> processes =
			new ArrayList<IPProcessControl>(getCollection().size());
		for (IPElementControl element : getCollection()) {
			processes.add((IPProcessControl)element);
		}
		return processes;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPJob#getProcesses()
	 */
	public IPProcess[] getProcesses() {
		return getProcessControls().toArray(new IPProcess[getProcessControls().size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPJob#getQueue()
	 */
	public IPQueue getQueue() {
		return getQueueControl();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elementcontrols.IPJobControl#getQueueControl()
	 */
	public IPQueueControl getQueueControl() {
		return (IPQueueControl) getParent();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPJob#getState()
	 */
	public State getState() {
		return getAttribute(JobAttributes.getStateAttributeDefinition()).getValue();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPJob#isDebug()
	 */
	public boolean isDebug() {
		return getAttribute(JobAttributes.getDebugFlagAttributeDefinition()).getValue();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPJob#isTerminated()
	 */
	public boolean isTerminated() {
		State state = getState();
		if (state == State.TERMINATED || state == State.ERROR) {
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPJob#removeChildListener(org.eclipse.ptp.core.elements.listeners.IJobProcessListener)
	 */
	public void removeChildListener(IJobChildListener listener) {
		childListeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPJob#removeElementListener(org.eclipse.ptp.core.elements.listeners.IJobListener)
	 */
	public void removeElementListener(IJobListener listener) {
		elementListeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elementcontrols.IPJobControl#removeProcesses(java.util.Collection)
	 */
	public void removeProcesses(Collection<IPProcessControl> processControls) {
		List<IPProcess> processes = new ArrayList<IPProcess>(processControls.size());
		
		for (IPProcessControl process : processControls) {
			removeChild(process);
			process.clearOutput();
			String idx = process.getProcessIndex();
			if (idx != null) {
				indexMap.remove(idx);
			}
			processes.add(process);
		}
		
		fireRemoveProcesses(processes);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPJob#setDebug()
	 */
	public void setDebug() {
		BooleanAttribute debug = getAttribute(JobAttributes.getDebugFlagAttributeDefinition());
		debug.setValue(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elementcontrols.IPJobControl#setLaunchConfiguration(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public synchronized void setLaunchConfiguration(ILaunchConfiguration configuration) {
		this.configuration = configuration;
	}

	/**
	 * Notify listeners when a job attribute has changed.
	 * 
	 * @param attrs
	 */
	private void fireChangedJob(Map<IAttributeDefinition<?,?,?>, IAttribute<?,?,?>> attrs) {
		IJobChangeEvent e = 
			new JobChangeEvent(this, attrs);
		
		for (Object listener : elementListeners.getListeners()) {
			((IJobListener)listener).handleEvent(e);
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
			((IJobChildListener)listener).handleEvent(e);
		}
	}

	/**
	 * Notify listeners when a new process is created.
	 * 
	 * @param process
	 */
	private void fireNewProcesses(Collection<IPProcess> processes) {
		INewProcessEvent e = 
			new NewProcessEvent(this, processes);
		
		for (Object listener : childListeners.getListeners()) {
			((IJobChildListener)listener).handleEvent(e);
		}
	}
	
	/**
	 * Notify listeners when the collection of processes are removed.
	 * 
	 * @param processes to remove
	 */
	private void fireRemoveProcesses(Collection<IPProcess> processes) {
		IRemoveProcessEvent e = 
			new RemoveProcessEvent(this, processes);
		
		for (Object listener : childListeners.getListeners()) {
			((IJobChildListener)listener).handleEvent(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.core.elements.PElement#doAddAttributeHook(java.util.Map)
	 */
	@Override
	protected void doAddAttributeHook(Map<IAttributeDefinition<?,?,?>, IAttribute<?,?,?>> attribs) {
		fireChangedJob(attribs);
	}
}
