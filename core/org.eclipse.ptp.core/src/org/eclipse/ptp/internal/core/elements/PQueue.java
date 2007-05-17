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
import java.util.List;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.elementcontrols.IPElementControl;
import org.eclipse.ptp.core.elementcontrols.IPJobControl;
import org.eclipse.ptp.core.elementcontrols.IPProcessControl;
import org.eclipse.ptp.core.elementcontrols.IPQueueControl;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.events.IJobChangedEvent;
import org.eclipse.ptp.core.elements.events.IQueueChangedEvent;
import org.eclipse.ptp.core.elements.events.IQueueChangedJobEvent;
import org.eclipse.ptp.core.elements.events.IQueueNewJobEvent;
import org.eclipse.ptp.core.elements.events.IQueueRemoveJobEvent;
import org.eclipse.ptp.core.elements.listeners.IJobListener;
import org.eclipse.ptp.core.elements.listeners.IQueueJobListener;
import org.eclipse.ptp.core.elements.listeners.IQueueListener;
import org.eclipse.ptp.internal.core.elements.events.QueueChangedEvent;
import org.eclipse.ptp.internal.core.elements.events.QueueChangedJobEvent;
import org.eclipse.ptp.internal.core.elements.events.QueueNewJobEvent;
import org.eclipse.ptp.internal.core.elements.events.QueueRemoveJobEvent;

public class PQueue extends Parent implements IPQueueControl, IJobListener {
	private final ListenerList elementListeners = new ListenerList();
	private final ListenerList childListeners = new ListenerList();

	public PQueue(String id, IResourceManagerControl rm, IAttribute[] attrs) {
		super(id, rm, P_QUEUE, attrs);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPQueue#addChildListener(org.eclipse.ptp.core.elements.listeners.IQueueJobListener)
	 */
	public void addChildListener(IQueueJobListener listener) {
		childListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPQueue#addElementListener(org.eclipse.ptp.core.elements.listeners.IQueueListener)
	 */
	public void addElementListener(IQueueListener listener) {
		elementListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elementcontrols.IPQueueControl#addJob(org.eclipse.ptp.core.elementcontrols.IPJobControl)
	 */
	public void addJob(IPJobControl job) {
		addChild(job);
		fireNewJob(job);
		job.addElementListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPQueue#getJobById(java.lang.String)
	 */
	public IPJob getJobById(String job_id) {
		return getJobControl(job_id);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elementcontrols.IPQueueControl#getJobControl(java.lang.String)
	 */
	public IPJobControl getJobControl(String job_id) {
		IPElementControl element = findChild(job_id);
		if (element != null)
			return (IPJobControl) element;
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elementcontrols.IPQueueControl#getJobControls()
	 */
	public IPJobControl[] getJobControls() {
		return (IPJobControl[]) getCollection().toArray(new IPJobControl[size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPQueue#getJobs()
	 */
	public IPJob[] getJobs() {
		return getJobControls();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.IPMachine#getResourceManager()
	 */
	public IResourceManager getResourceManager() {
		return (IResourceManager) getParent();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.listeners.IJobListener#handleEvent(org.eclipse.ptp.core.elements.events.IJobChangedEvent)
	 */
	public void handleEvent(IJobChangedEvent e) {
		IQueueChangedJobEvent qe = 
			new QueueChangedJobEvent(this, e.getSource(), e.getAttributes());
		
		for (Object listener : childListeners.getListeners()) {
			((IQueueJobListener)listener).handleEvent(qe);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPQueue#removeChildListener(org.eclipse.ptp.core.elements.listeners.IQueueJobListener)
	 */
	public void removeChildListener(IQueueJobListener listener) {
		childListeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.IPQueue#removeElementListener(org.eclipse.ptp.core.elements.listeners.IQueueListener)
	 */
	public void removeElementListener(IQueueListener listener) {
		elementListeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elementcontrols.IPQueueControl#removeJob(org.eclipse.ptp.core.elementcontrols.IPJobControl)
	 */
	public void removeJob(IPJobControl job) {
		job.removeElementListener(this);
		for (IPProcessControl proc : job.getProcessControls()) {
			job.removeProcess(proc);
		}
		removeChild(job);
		fireRemoveJob(job);
	}

	/**
	 * @param attrs
	 */
	private void fireChangedQueue(Collection<IAttribute> attrs) {
		IQueueChangedEvent e = 
			new QueueChangedEvent(this, attrs);
		
		for (Object listener : elementListeners.getListeners()) {
			((IQueueListener)listener).handleEvent(e);
		}
	}

	/**
	 * @param job
	 */
	private void fireNewJob(IPJob job) {
		IQueueNewJobEvent e = 
			new QueueNewJobEvent(this, job);
		
		for (Object listener : childListeners.getListeners()) {
			((IQueueJobListener)listener).handleEvent(e);
		}
	}

	/**
	 * @param job
	 */
	private void fireRemoveJob(IPJob job) {
		IQueueRemoveJobEvent e = 
			new QueueRemoveJobEvent(this, job);
		
		for (Object listener : childListeners.getListeners()) {
			((IQueueJobListener)listener).handleEvent(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.core.elements.PElement#doAddAttributeHook(java.util.List)
	 */
	@Override
	protected void doAddAttributeHook(List<IAttribute> attrs) {
		fireChangedQueue(attrs);
	}

}
