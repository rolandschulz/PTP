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

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.ptp.core.ModelManager;
import org.eclipse.ptp.core.elements.IPElement;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.core.elements.IPUniverse;
import org.eclipse.ptp.core.elements.attributes.ElementAttributes;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes.State;
import org.eclipse.ptp.internal.ui.ParallelImages;
import org.eclipse.ptp.internal.ui.model.PProcessUI;
import org.eclipse.ptp.ui.IJobManager;
import org.eclipse.ptp.ui.listeners.IJobChangedListener;
import org.eclipse.ptp.ui.messages.Messages;
import org.eclipse.ptp.ui.model.Element;
import org.eclipse.ptp.ui.model.ElementHandler;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.ptp.ui.model.IElementSet;
import org.eclipse.ptp.utils.core.BitSetIterable;
import org.eclipse.swt.graphics.Image;

/**
 * @author Clement chu
 * @since 4.0
 * 
 */

public class JobManager extends AbstractElementManager implements IJobManager {
	private static String getProcessKey(IPJob job, int procJobRank) {
		return job.getProcessName(procJobRank);
	}

	protected Map<String, IPJob> jobList = new HashMap<String, IPJob>();
	protected IPJob cur_job = null;
	protected IPQueue cur_queue = null;
	protected final String DEFAULT_TITLE = Messages.JobManager_0;

	/**
	 * Add a new job to jobList. Check for any new processes and add these to the element handler for the job.
	 * 
	 * @param job
	 */
	public void addJob(IPJob job) {
		if (job != null) {
			IElementSet set = createElementHandler(job).getSetRoot();
			List<IElement> elements = new ArrayList<IElement>();
			for (Integer processJobRank : new BitSetIterable(job.getProcessJobRanks())) {
				final String key = getProcessKey(job, processJobRank);
				if (set.contains(key)) {
					continue;
				}
				elements.add(createProcessElement(set, key, job, processJobRank));
			}
			set.addElements(elements.toArray(new IElement[0]));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IJobManager#addProcess(org.eclipse.ptp.core.elements .IPJob, int)
	 */
	/**
	 * @since 4.0
	 */
	public void addProcess(IPJob job, int procJobRank) {
		IElementSet set = createElementHandler(job).getSetRoot();
		final String key = getProcessKey(job, procJobRank);
		if (!set.contains(key)) {
			set.addElements(new IElement[] { createProcessElement(set, key, job, procJobRank) });
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IElementManager#clear()
	 */
	@Override
	public void clear() {
		jobList.clear();
		super.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IJobManager#createElementHandler(org.eclipse.ptp.core .elements.IPJob)
	 */
	public IElementHandler createElementHandler(IPJob job) {
		IElementHandler handler = getElementHandler(job.getID());
		if (handler == null) {
			handler = new ElementHandler();
			jobList.put(job.getID(), job);
			setElementHandler(job.getID(), handler);
		}
		return handler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IJobManager#findJobById(java.lang.String)
	 */
	public IPJob findJobById(String job_id) {
		return jobList.get(job_id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IElementManager#getFullyQualifiedName(java.lang.String )
	 */
	public String getFullyQualifiedName(String id) {
		if (id.equals(EMPTY_ID)) {
			return DEFAULT_TITLE;
		}
		IPJob job = getJob();
		if (job != null) {
			return job.getName(); //$NON-NLS-1$
		}
		return ""; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IJobManager#getJob()
	 */
	public IPJob getJob() {
		return cur_job;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IJobManager#getJobs()
	 */
	public IPJob[] getJobs() {
		return jobList.values().toArray(new IPJob[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IElementManager#getName(java.lang.String)
	 */
	public String getName(String id) {
		IPJob job = findJobById(id);
		if (job == null) {
			return ""; //$NON-NLS-1$
		}
		return job.getName();
	}

	/**
	 * Get process status text
	 * 
	 * @param proc
	 *            process (PProcessUI goes away when we address UI scalability. See Bug 311057)
	 * @return status
	 * @since 4.0
	 */
	// FIXME PProcessUI goes away when we address UI scalability. See Bug 311057
	public String getProcessStatusText(PProcessUI proc) {
		if (proc != null) {
			return proc.getState().toString();
		}
		return Messages.JobManager_1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IJobManager#getQueue()
	 */
	public IPQueue getQueue() {
		return cur_queue;
	}

	/**
	 * @return
	 */
	public IPQueue[] getQueues() {
		if (cur_queue != null) {
			IPResourceManager rm = ModelManager.getInstance().getUniverse().getResourceManager(cur_queue.getControlId());
			if (rm != null) {
				return rm.getQueues();
			}
		}
		return new IPQueue[] {};
	}

	/**
	 * Return set id
	 * 
	 * @param jid
	 * @return
	 */
	public String[] getSets(String jid) {
		IElementHandler eHandler = getElementHandler(jid);
		if (eHandler == null) {
			return new String[0];
		}

		IElement[] elements = eHandler.getElements();
		String[] sets = new String[elements.length];
		for (int i = 1; i < sets.length + 1; i++) {
			String tmp = elements[i - 1].getID();
			if (tmp.equals(IElementHandler.SET_ROOT_ID)) {
				continue;
			}
			if (i == sets.length) {
				sets[i - 1] = tmp;
			} else {
				sets[i] = tmp;
			}
		}
		if (sets.length > 0) {
			sets[0] = IElementHandler.SET_ROOT_ID;
		}
		return sets;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IElementManager#getImage(org.eclipse.ptp.ui.model. IElement)
	 */
	@Override
	public Image getImage(IElement element) {
		IPJob job = getJob();
		if (job != null) {
			IPElement pElement = element.getPElement();
			// FIXME PProcessUI goes away when we address UI scalability. See
			// Bug 311057
			if (pElement instanceof PProcessUI) {
				State state = ((PProcessUI) pElement).getState();
				return ParallelImages.procImages[state.ordinal()][element.isSelected() ? 1 : 0];
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IJobManager#hasStoppedJob()
	 */
	public boolean hasStoppedJob() {
		for (IPJob job : getJobs()) {
			if (job.getState() == JobAttributes.State.COMPLETED) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IElementManager#initial()
	 */
	public IPElement initial(IPUniverse universe) {
		for (IPResourceManager rm : universe.getResourceManagers()) {
			for (IPJob job : rm.getJobs()) {
				addJob(job);
			}
		}

		setCurrentSetId(IElementHandler.SET_ROOT_ID);
		return null;
	}

	/**
	 * Is current set contain process
	 * 
	 * @param jid
	 *            job ID
	 * @param processID
	 *            process ID
	 * @return true if job contains process
	 */
	public boolean isCurrentSetContainProcess(String jid, String processID) {
		IPJob job = getJob();
		if (job != null) {
			if (!job.getID().equals(jid)) {
				return false;
			}
			IElementHandler elementHandler = getElementHandler(jid);
			if (elementHandler == null) {
				return false;
			}
			IElementSet set = (IElementSet) elementHandler.getElementByID(getCurrentSetId());
			if (set == null) {
				return false;
			}
			return set.contains(processID);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IElementManager#isJobStop(java.lang.String)
	 */
	public boolean isJobStop(String job_id) {
		if (isNoJob(job_id)) {
			return true;
		}
		IPJob job = findJobById(job_id);
		return (job == null || job.getState() == JobAttributes.State.COMPLETED);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IElementManager#isNoJob(java.lang.String)
	 */
	public boolean isNoJob(String jid) {
		return (jid == null || jid.length() == 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IJobManager#removeAllStoppedJobs()
	 */
	public void removeAllStoppedJobs() {
		Map<String, IPResourceManager> rms = new HashMap<String, IPResourceManager>();
		for (IPJob job : getJobs()) {
			IPResourceManager rm = ModelManager.getInstance().getUniverse().getResourceManager(job.getControlId());
			if (rm != null) {
				if (rm != null && !rms.containsKey(rm.getID())) {
					rm.removeTerminatedJobs();
					rms.put(rm.getID(), rm);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IJobManager#removeJob(org.eclipse.ptp.core.elements .IPJob)
	 */
	public void removeJob(IPJob job) {
		// remove launch from debug view
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		String jid = job.getID();
		for (ILaunch launch : launchManager.getLaunches()) {
			String launchedJobID = launch.getAttribute(ElementAttributes.getIdAttributeDefinition().getId());
			if (launchedJobID != null && launchedJobID.equals(jid)) {
				launchManager.removeLaunch(launch);
			}
		}
		jobList.remove(jid);
		removeElementHandler(job);
		fireJobChangedEvent(IJobChangedListener.REMOVED, null, jid);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IJobManager#removeProcess(org.eclipse.ptp.core.elements .IPJob, int)
	 */
	/**
	 * @since 4.0
	 */
	public void removeProcess(IPJob job, int procJobRank) {
		IElementHandler elementHandler = getElementHandler(job.getID());
		if (elementHandler != null) {
			IElementSet set = elementHandler.getSetRoot();
			final String key = getProcessKey(job, procJobRank);
			IElement element = set.getElementByID(key);
			if (element != null) {
				set.removeElement(key);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IElementManager#setCurrentSetId(java.lang.String)
	 */
	public void setCurrentSetId(String set_id) {
		cur_set_id = set_id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IJobManager#setJob(org.eclipse.ptp.core.elements.IPJob )
	 */
	public void setJob(IPJob job) {
		String old_id = null;
		if (cur_job != null) {
			old_id = cur_job.getID();
		}
		String new_id = null;
		if (job != null) {
			new_id = job.getID();
			addJob(job);
		}
		cur_job = job;
		fireJobChangedEvent(IJobChangedListener.CHANGED, new_id, old_id);
	}

	/**
	 * @param queue
	 */
	public void setQueue(IPQueue queue) {
		if (queue != cur_queue) {
			cur_queue = queue;
			setJob(null);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IElementManager#shutdown()
	 */
	@Override
	public void shutdown() {
		clear();
		super.shutdown();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IElementManager#size()
	 */
	public int size() {
		return jobList.size();
	}

	/**
	 * @param job
	 */
	private void removeElementHandler(IPJob job) {
		IElementHandler elementHandler = getElementHandler(job.getID());
		if (elementHandler != null) {
			elementHandler.removeAllRegistered();
			elementHandler.clean();
		}
		removeElementHandler(job.getID());
	}

	/**
	 * @param set
	 * @param key
	 * @param job
	 * @param processJobRank
	 * @return
	 * @since 4.0
	 */
	protected IElement createProcessElement(IElementSet set, String key, IPJob job, int processJobRank) {
		// FIXME PProcessUI goes away when we address UI scalability. See Bug
		// 311057
		final PProcessUI pelement = new PProcessUI(job, processJobRank);
		return new Element(set, key, Integer.toString(processJobRank), pelement) {
			@Override
			public int compareTo(IElement e) {
				return new Integer(getName()).compareTo(new Integer(e.getName()));
			}
		};
	}
}
