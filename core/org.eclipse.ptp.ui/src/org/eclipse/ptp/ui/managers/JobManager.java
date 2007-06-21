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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.ptp.core.elements.IPElement;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPProcess;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IPUniverse;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.attributes.ElementAttributes;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes;
import org.eclipse.ptp.ui.listeners.IJobChangedListener;
import org.eclipse.ptp.ui.model.Element;
import org.eclipse.ptp.ui.model.ElementHandler;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.ptp.ui.model.IElementSet;

/**
 * @author Clement chu
 * 
 */

public class JobManager extends AbstractUIManager {
	protected Map<String, IElementHandler> jobElementHandlerList = new HashMap<String, IElementHandler>();
	protected Map<String, IPJob> jobList = new HashMap<String, IPJob>();
	protected IPJob cur_job = null;
	protected IPQueue cur_queue = null;
	protected final String DEFAULT_TITLE = "Please select a job";

	/** 
	 * Add a new job to jobList. Check for any new processes and add these to
	 * the element handler for the job.
	 * 
	 * @param job
	 */
	public void addJob(IPJob job) {
		if (job != null) {
			IElementHandler handler;
			if (!jobList.containsKey(job.getID())) {
				handler = new ElementHandler();
				jobList.put(job.getID(), job);
				jobElementHandlerList.put(job.getID(), handler);
			} else {
				handler = jobElementHandlerList.get(job.getID());
			}
			IElementSet set = handler.getSetRoot();
			for (IPProcess proc : job.getProcesses()) {
				String id = proc.getID();
				if (set.getElement(id) == null) {
					set.add(createElement(set, id, proc.getProcessIndex()));
				}
			}
		}
	}
	
	public void addProcess(IPProcess proc) {
		addJob(proc.getJob());
		IElementHandler elementHandler = jobElementHandlerList.get(proc.getJob().getID());
		IElementSet set = elementHandler.getSetRoot();
		if (set.getElement(proc.getID()) == null) {
			set.add(createElement(set, proc.getID(), proc.getProcessIndex()));
		}
	}

	
	public void removeProcess(IPProcess proc) {
		IElementHandler elementHandler = jobElementHandlerList.get(proc.getJob().getID());
		IElementSet set = elementHandler.getSetRoot();
		set.remove(proc.getID());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#clear()
	 */
	public void clear() {
		if (jobList != null) {
			jobList.clear();
			jobElementHandlerList.clear();
		}
	}
	
	/** Find process
	 * @param job job
	 * @param id process ID
	 * @return
	 */
	public IPProcess findProcess(IPJob job, String task_id) {
		if (job == null)
			return null;
		return job.getProcessByIndex(task_id);
		//return job.findProcess(id);
	}
	
	public IPProcess findProcess(String proc_id) {
		IPJob job = getJob();
		if (job != null) {
			return job.getProcessById(proc_id);
		}
		return null;
	}
	
	/** 
	 * Get current job ID. This is the job that is currently selected by the view.
	 * 
	 * @return current job ID
	 */
	public IPJob getJob() {
		return cur_job;
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
		return (IElementHandler) jobElementHandlerList.get(id);
	}
	
	/** 
	 * Get Jobs that we know about. This is typically all jobs in the system.
	 * 
	 * @return jobs
	 */
	public IPJob[] getJobs() {
		return jobList.values().toArray(new IPJob[0]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#getName(java.lang.String)
	 */
	public String getName(String id) {
		IPJob job = findJobById(id);
		if (job == null)
			return "";
		return job.getName();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#getFullyQualifiedName(java.lang.String)
	 */
	public String getFullyQualifiedName(String id) {
		if (id.equals(EMPTY_ID)) {
			return DEFAULT_TITLE;
		}
		IPJob job = getJob();
		if (job != null) {
			IPQueue queue = job.getQueue();
			if (queue != null) {
				IResourceManager rm = queue.getResourceManager();
				if (rm != null) {
					return rm.getName() + ": " + queue.getName() + ":" + job.getName();
				}
			}
		}
		return "";
	}
	
	/** Get process status text
	 * @param proc process
	 * @return status
	 */
	public String getProcessStatusText(IPProcess proc) {
		if (proc != null) {
			return proc.getState().toString();
		}
		return "Error";
	}
	
	public IPQueue[] getQueues() {
		if (cur_queue != null) {
			return cur_queue.getResourceManager().getQueues();
		}
		return new IPQueue[] {};
	}
	
	/** Return set id
	 * @param jid
	 * @return
	 */
	public String[] getSets(String jid) {
		IElementHandler eHandler = getElementHandler(jid);
		if (eHandler == null)
			return new String[0];
		
		IElementSet[] eSets = eHandler.getSets();
		String[] sets = new String[eSets.length];
		for (int i=1; i<sets.length+1; i++) {
			String tmp = eSets[i-1].getID();
			if (tmp.equals(IElementHandler.SET_ROOT_ID)) {
				continue;
			}
			if (i == sets.length) {
				sets[i-1] = tmp;				
			}
			else {
				sets[i] = tmp;				
			}
		}
		if (sets.length > 0) {
			sets[0] = IElementHandler.SET_ROOT_ID;
		}
		return sets;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#getStatus(java.lang.String)
	 */
	public int getStatus(String id) {
		IPJob job = getJob();
		if (job != null) {
			IPProcess proc = job.getProcessById(id);
			if (proc != null) {
				return proc.getState().ordinal();
			}
		}
		return ProcessAttributes.State.UNKNOWN.ordinal();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#initial()
	 */
	public IPElement initial(IPUniverse universe) {
		for (IResourceManager rm : universe.getResourceManagers()) {
			for (IPQueue queue : rm.getQueues()) {
				for (IPJob job : queue.getJobs()) {
					addJob(job);
				}
			}
		}

		setCurrentSetId(IElementHandler.SET_ROOT_ID);
		return null;
	}
	
	/** Is current set contain process
	 * @param jid job ID
	 * @param processID process ID
	 * @return true if job contains process
	 */
	public boolean isCurrentSetContainProcess(String jid, String processID) {
		IPJob job = getJob();
		if (job != null) {
			if (!job.getID().equals(jid))
				return false;
			IElementHandler elementHandler = getElementHandler(job.getID());
			if (elementHandler == null)
				return false;
			IElementSet set = elementHandler.getSet(getCurrentSetId());
			if (set == null)
				return false;
			return set.contains(processID);
		}
		return false;
	}
	
	/** Set current job ID
	 * @param job_id Job ID
	 */
	public void setJob(IPJob job) {
		String new_id = EMPTY_ID;
		String old_id = EMPTY_ID;
		if (cur_job != null) {
			old_id = cur_job.getID();
		}
		if (job != null) {
			addJob(job);
			new_id = job.getID();
		}
		cur_job = job;
		fireJobChangedEvent(IJobChangedListener.CHANGED, new_id, old_id);
	}
	
	public void setQueue(IPQueue queue) {
		if (queue != cur_queue) {
			cur_queue = queue;
			setJob(null);
		}
	}
	
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
		return jobList.size();
	}

	/*******************************************************************************************************************************************************************************************************************************************************************************************************
	 * terminate action
	 ******************************************************************************************************************************************************************************************************************************************************************************************************/
	/** terminate all processes in current job
	 * @throws CoreException
	 */
	public void terminateAll() throws CoreException {
		IPJob job = getJob();
		if (job != null) {
			job.getQueue().getResourceManager().terminateJob(job);
		}
	}
	
	public IPQueue getQueue() {
		return cur_queue;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#hasStoppedJob()
	 */
	public boolean hasStoppedJob() {
		for (IPJob job : getJobs()) {
			if (job.isTerminated())
				return true;
		}
		return false;
	}
	
	/** Create Element
	 * @param set
	 * @param key
	 * @param name
	 * @return
	 */
	protected IElement createElement(IElementSet set, String key, String name) {
		return new Element(set, key, name);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#isNoJob(java.lang.String)
	 */
	public boolean isNoJob(String jid) {
		return (jid == null || jid.length() == 0);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#isJobStop(java.lang.String)
	 */
	public boolean isJobStop(String job_id) {
		if (isNoJob(job_id))
			return true;
		IPJob job = findJobById(job_id);
		return (job == null || job.isTerminated());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#findJobById(java.lang.String)
	 */
	public IPJob findJobById(String job_id) {
		return jobList.get(job_id);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#removeJob(org.eclipse.ptp.core.IPJob)
	 */
	public void removeJob(IPJob job) {
		//remove launch from debug view
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunch[] launches = launchManager.getLaunches();
		for (int i=0; i<launches.length; i++) {
			String launchedJobID = launches[i].getAttribute(ElementAttributes.getIdAttributeDefinition().getId());
			if (launchedJobID != null && launchedJobID.equals(job.getID())) {
				launchManager.removeLaunch(launches[i]);
			}
		}
		fireJobChangedEvent(IJobChangedListener.REMOVED, null, job.getID());
		jobList.remove(job.getID());
		jobElementHandlerList.remove(job.getID());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#removeAllStoppedJobs()
	 */
	public void removeAllStoppedJobs() {
		Map<String, IPQueue> queues = new HashMap<String, IPQueue>();
		for (IPJob job : jobList.values()) {
			IPQueue queue = job.getQueue();
			if (!queues.containsKey(queue.getID())) {
				queue.getResourceManager().removeTerminatedJobs(queue);
				queues.put(queue.getID(), queue);
			}
		}
	}
}
