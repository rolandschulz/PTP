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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.ptp.core.attributes.BooleanAttribute;
import org.eclipse.ptp.core.attributes.EnumeratedAttribute;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.elementcontrols.IPElementControl;
import org.eclipse.ptp.core.elementcontrols.IPJobControl;
import org.eclipse.ptp.core.elementcontrols.IPProcessControl;
import org.eclipse.ptp.core.elementcontrols.IPQueueControl;
import org.eclipse.ptp.core.elements.IPProcess;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.elements.attributes.JobAttributes.State;
import org.eclipse.ptp.core.elements.events.IJobChangedEvent;
import org.eclipse.ptp.core.elements.events.IJobChangedProcessEvent;
import org.eclipse.ptp.core.elements.events.IJobNewProcessEvent;
import org.eclipse.ptp.core.elements.events.IJobRemoveProcessEvent;
import org.eclipse.ptp.core.elements.events.IProcessChangedEvent;
import org.eclipse.ptp.core.elements.listeners.IJobListener;
import org.eclipse.ptp.core.elements.listeners.IJobProcessListener;
import org.eclipse.ptp.core.elements.listeners.IProcessListener;
import org.eclipse.ptp.internal.core.elements.events.JobChangedEvent;
import org.eclipse.ptp.internal.core.elements.events.JobChangedProcessEvent;
import org.eclipse.ptp.internal.core.elements.events.JobNewProcessEvent;
import org.eclipse.ptp.internal.core.elements.events.JobRemoveProcessEvent;

public class PJob extends Parent implements IPJobControl, IProcessListener {
	final public static int BASE_OFFSET = 10000;
	final public static int STATE_NEW = 5000;

	private final ListenerList elementListeners = new ListenerList();
	private final ListenerList childListeners = new ListenerList();
	private HashMap<String, IPProcessControl> numberMap = 
		new HashMap<String, IPProcessControl>();

	@SuppressWarnings("unchecked")
	public PJob(String id, IPQueueControl queue, IAttribute[] attrs) {
		super(id, queue, P_JOB, attrs);
		/*
		 * Create required attributes.
		 */
		EnumeratedAttribute<State> jobState = (EnumeratedAttribute<State>) getAttribute(
				JobAttributes.getStateAttributeDefinition());
		if (jobState == null) {
			jobState = JobAttributes.getStateAttributeDefinition().create();
			addAttribute(jobState);
		}
		BooleanAttribute debug = (BooleanAttribute) getAttribute(
				JobAttributes.getDebugFlagAttributeDefinition());
		if (debug == null) {
			debug = JobAttributes.getDebugFlagAttributeDefinition().create();
			addAttribute(debug);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPJob#addChildListener(org.eclipse.ptp.core.elements.listeners.IJobProcessListener)
	 */
	public void addChildListener(IJobProcessListener listener) {
		childListeners.add(listener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPJob#addElementListener(org.eclipse.ptp.core.elements.listeners.IJobListener)
	 */
	public void addElementListener(IJobListener listener) {
		elementListeners.add(listener);
	}

	public void addProcess(IPProcessControl process) {
		addChild(process);
		String num = process.getProcessNumber();
		if (num != null) {
			numberMap.put(num, process);
		}
		fireNewProcess(process);
		process.addElementListener(this);
	}
	
	public synchronized IPProcess getProcessById(String id) {
		IPElementControl element = findChild(id);
		if (element != null)
			return (IPProcessControl) element;
		return null;
	}
	
	public synchronized IPProcess getProcessByNumber(String number) {
		return numberMap.get(number);
	}

	
	public synchronized IPProcess getProcessByNumber(int number) {
		return numberMap.get(String.valueOf(number));
	}

	/*
	 * returns all the processes in this job, which are the children of the job
	 */
	public synchronized IPProcessControl[] getProcessControls() {
		return (IPProcessControl[]) getCollection().toArray(new IPProcessControl[size()]);
	}

	/*
	 * returns all the processes in this job, which are the children of the job
	 */
	public IPProcess[] getProcesses() {
		return getProcessControls();
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

	public synchronized IPProcess[] getSortedProcesses() {
		IPProcessControl[] processes = getProcessControls();
		sort(processes);
		return processes;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.listeners.IProcessListener#handleEvent(org.eclipse.ptp.core.elements.events.IProcessChangedEvent)
	 */
	public void handleEvent(IProcessChangedEvent e) {
		IJobChangedProcessEvent ne = 
			new JobChangedProcessEvent(this, e.getSource(), e.getAttributes());
		
		for (Object listener : childListeners.getListeners()) {
			((IJobProcessListener)listener).handleEvent(ne);
		}
	}

	public boolean isDebug() {
		BooleanAttribute debug = (BooleanAttribute) getAttribute(JobAttributes.getDebugFlagAttributeDefinition());
		return debug.getValue();
	}

	@SuppressWarnings("unchecked")
	public boolean isTerminated() {
		EnumeratedAttribute<State> jobState = (EnumeratedAttribute<State>) getAttribute(
				JobAttributes.getStateAttributeDefinition());
		State state = jobState.getValue();
		if (state == State.TERMINATED || state == State.ERROR) {
			return true;
		}
		return false;
	}

	public void removeAllProcesses() {
		for (IPProcessControl process : getProcessControls()) {
			removeProcess(process);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPJob#removeChildListener(org.eclipse.ptp.core.elements.listeners.IJobProcessListener)
	 */
	public void removeChildListener(IJobProcessListener listener) {
		childListeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPJob#removeElementListener(org.eclipse.ptp.core.elements.listeners.IJobListener)
	 */
	public void removeElementListener(IJobListener listener) {
		elementListeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elementcontrols.IPJobControl#removeProcess(org.eclipse.ptp.core.elementcontrols.IPProcessControl)
	 */
	public void removeProcess(IPProcessControl process) {
		process.removeElementListener(this);
		removeChild(process);
		process.removeNode();
		process.clearOutput();
		String num = process.getProcessNumber();
		if (num != null) {
			numberMap.remove(num);
		}
		fireRemoveProcess(process);
	}

	public void setDebug() {
		BooleanAttribute debug = (BooleanAttribute) getAttribute(JobAttributes.getDebugFlagAttributeDefinition());
		debug.setValue(true);
	}

	private void fireChangedJob(Collection<IAttribute> attrs) {
		IJobChangedEvent e = 
			new JobChangedEvent(this, attrs);
		
		for (Object listener : elementListeners.getListeners()) {
			((IJobListener)listener).handleEvent(e);
		}
	}

	private void fireNewProcess(IPProcess process) {
		IJobNewProcessEvent e = 
			new JobNewProcessEvent(this, process);
		
		for (Object listener : childListeners.getListeners()) {
			((IJobProcessListener)listener).handleEvent(e);
		}
	}

	private void fireRemoveProcess(IPProcess process) {
		IJobRemoveProcessEvent e = 
			new JobRemoveProcessEvent(this, process);
		
		for (Object listener : childListeners.getListeners()) {
			((IJobProcessListener)listener).handleEvent(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.core.elements.PElement#doAddAttributeHook(org.eclipse.ptp.core.attributes.IAttribute[])
	 */
	@Override
	protected void doAddAttributeHook(List<IAttribute> attribs) {
		fireChangedJob(attribs);
	}
}
