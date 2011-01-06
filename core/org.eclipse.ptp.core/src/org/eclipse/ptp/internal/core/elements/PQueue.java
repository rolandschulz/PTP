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
import java.util.List;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.EnumeratedAttribute;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.elementcontrols.IPElementControl;
import org.eclipse.ptp.core.elementcontrols.IPJobControl;
import org.eclipse.ptp.core.elementcontrols.IPQueueControl;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.core.elements.attributes.QueueAttributes;
import org.eclipse.ptp.core.elements.attributes.QueueAttributes.State;
import org.eclipse.ptp.core.elements.events.IChangedJobEvent;
import org.eclipse.ptp.core.elements.events.INewJobEvent;
import org.eclipse.ptp.core.elements.events.IQueueChangeEvent;
import org.eclipse.ptp.core.elements.events.IRemoveJobEvent;
import org.eclipse.ptp.core.elements.listeners.IQueueChildListener;
import org.eclipse.ptp.core.elements.listeners.IQueueListener;
import org.eclipse.ptp.internal.core.elements.events.ChangedJobEvent;
import org.eclipse.ptp.internal.core.elements.events.NewJobEvent;
import org.eclipse.ptp.internal.core.elements.events.QueueChangeEvent;
import org.eclipse.ptp.internal.core.elements.events.RemoveJobEvent;

public class PQueue extends Parent implements IPQueueControl {
	private final ListenerList elementListeners = new ListenerList();
	private final ListenerList childListeners = new ListenerList();

	public PQueue(String id, IResourceManagerControl rm, IAttribute<?, ?, ?>[] attrs) {
		super(id, rm, P_QUEUE, attrs);
		/*
		 * Create required attributes.
		 */
		EnumeratedAttribute<QueueAttributes.State> state = getAttribute(QueueAttributes.getStateAttributeDefinition());
		if (state == null) {
			state = QueueAttributes.getStateAttributeDefinition().create();
			addAttribute(state);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.IPQueue#addChildListener(org.eclipse.ptp
	 * .core.elements.listeners.IQueueJobListener)
	 */
	public void addChildListener(IQueueChildListener listener) {
		childListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.IPQueue#addElementListener(org.eclipse.
	 * ptp.core.elements.listeners.IQueueListener)
	 */
	public void addElementListener(IQueueListener listener) {
		elementListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elementcontrols.IPQueueControl#addJobAttributes(
	 * java.util.Collection,
	 * org.eclipse.ptp.core.attributes.IAttribute<?,?,?>[])
	 */
	public void addJobAttributes(Collection<IPJobControl> jobControls,
			IAttribute<?, ?, ?>[] attrs) {
		List<IPJob> jobs = new ArrayList<IPJob>(jobControls.size());

		for (IPJobControl job : jobControls) {
			job.addAttributes(attrs);
			jobs.add(job);
		}

		fireChangedJobs(jobs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elementcontrols.IPQueueControl#addJobs(java.util
	 * .Collection)
	 */
	public void addJobs(Collection<IPJobControl> jobControls) {
		List<IPJob> jobs = new ArrayList<IPJob>(jobControls.size());

		for (IPJobControl job : jobControls) {
			addChild(job);
			jobs.add(job);
		}

		fireNewJobs(jobs);
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
		fireChangedQueue(attrs);
	}

	/**
	 * Send IChangedJobEvent to registered listeners
	 * 
	 * @param jobs
	 *            jobs that have changed
	 */
	private void fireChangedJobs(Collection<IPJob> jobs) {
		IChangedJobEvent e =
				new ChangedJobEvent(this, jobs);

		for (Object listener : childListeners.getListeners()) {
			((IQueueChildListener) listener).handleEvent(e);
		}
	}

	/**
	 * Send IQueueChangeEvent to registered listeners
	 * 
	 * @param attrs
	 *            attributes that have changed
	 */
	private void fireChangedQueue(AttributeManager attrs) {
		IQueueChangeEvent e = new QueueChangeEvent(this, attrs);

		for (Object listener : elementListeners.getListeners()) {
			((IQueueListener) listener).handleEvent(e);
		}
	}

	/**
	 * Send INewJobEvent to registered listeners
	 * 
	 * @param jobs
	 *            new jobs
	 */
	private void fireNewJobs(Collection<IPJob> jobs) {
		INewJobEvent e =
				new NewJobEvent(this, jobs);

		for (Object listener : childListeners.getListeners()) {
			((IQueueChildListener) listener).handleEvent(e);
		}
	}

	/**
	 * Send IRemoveJobEvent to registered listeners
	 * 
	 * @param job
	 *            removed jobs
	 */
	private void fireRemoveJobs(Collection<IPJob> jobs) {
		IRemoveJobEvent e =
				new RemoveJobEvent(this, jobs);

		for (Object listener : childListeners.getListeners()) {
			((IQueueChildListener) listener).handleEvent(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPQueue#getJobById(java.lang.String)
	 */
	public IPJob getJobById(String job_id) {
		return getJobControl(job_id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elementcontrols.IPQueueControl#getJobControl(java
	 * .lang.String)
	 */
	public IPJobControl getJobControl(String job_id) {
		IPElementControl element = findChild(job_id);
		if (element != null) {
			return (IPJobControl) element;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elementcontrols.IPQueueControl#getJobControls()
	 */
	public Collection<IPJobControl> getJobControls() {
		IPElementControl[] children = getChildren();
		List<IPJobControl> jobs =
				new ArrayList<IPJobControl>(children.length);
		for (IPElementControl element : children) {
			jobs.add((IPJobControl) element);
		}
		return jobs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPQueue#getJobs()
	 */
	public IPJob[] getJobs() {
		Collection<IPJobControl> jobs = getJobControls();
		return jobs.toArray(new IPJobControl[jobs.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.IPMachine#getResourceManager()
	 */
	public IPResourceManager getResourceManager() {
		return (IPResourceManager) getParent();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPQueue#getState()
	 */
	public State getState() {
		return getAttribute(QueueAttributes.getStateAttributeDefinition()).getValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.IPQueue#removeChildListener(org.eclipse
	 * .ptp.core.elements.listeners.IQueueJobListener)
	 */
	public void removeChildListener(IQueueChildListener listener) {
		childListeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.IPQueue#removeElementListener(org.eclipse
	 * .ptp.core.elements.listeners.IQueueListener)
	 */
	public void removeElementListener(IQueueListener listener) {
		elementListeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elementcontrols.IPQueueControl#removeJobs(java.util
	 * .Collection)
	 */
	public void removeJobs(Collection<IPJobControl> jobControls) {
		List<IPJob> jobs = new ArrayList<IPJob>(jobControls.size());

		for (IPJobControl job : jobControls) {
			job.removeProcessesByJobRanks(job.getProcessJobRanks());
			removeChild(job);
			jobs.add(job);
		}

		fireRemoveJobs(jobs);
	}

}
