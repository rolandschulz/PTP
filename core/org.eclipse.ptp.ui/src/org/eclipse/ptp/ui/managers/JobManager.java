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
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.IPQueue;
import org.eclipse.ptp.core.IPUniverse;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.ptp.ui.IPTPUIConstants;
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
	protected Map jobList = new HashMap();
	protected String cur_job_id = EMPTY_ID;
	protected String cur_queue_id = EMPTY_ID;

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#shutdown()
	 */
	public void shutdown() {
		clear();
		modelPresentation = null;
		super.shutdown();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#getElementHandler(java.lang.String)
	 */
	public IElementHandler getElementHandler(String id) {
		return (IElementHandler) jobList.get(id);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#size()
	 */
	public int size() {
		return jobList.size();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#clear()
	 */
	public void clear() {
		if (jobList != null) {
			jobList.clear();
		}
	}
	/** Get Jobs
	 * @return jobs
	 */
	public IPJob[] getJobs() {
		IPUniverse universe = modelPresentation.getUniverse();
		if (universe == null) {
			return new IPJob[0];
		}
		return universe.getJobs();
	}
	
	/** Get current job
	 * @return curretn job
	 */
	public IPJob getCurrentJob() {
		return findJobById(getCurrentJobId());
	}
	/** Get current job ID
	 * @return current job ID
	 */
	public String getCurrentJobId() {
		return cur_job_id;
	}
	/** Set current job ID
	 * @param job_id Job ID
	 */
	public void setCurrentJobId(String job_id) {
		String tmp_jod_id = cur_job_id;
		cur_job_id = job_id;		
		fireJobChangedEvent(IJobChangedListener.CHANGED, job_id, tmp_jod_id);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#getCurrentSetId()
	 */
	public String getCurrentSetId() {
		return cur_set_id;
	}
	
	public void setCurrentSetId(String set_id) {
		cur_set_id = set_id;
	}
	
	/** Get process status text
	 * @param proc process
	 * @return status
	 */
	public String getProcessStatusText(IPProcess proc) {
		switch (getProcessStatus(proc)) {
		case IPTPUIConstants.PROC_STARTING:
			return "Starting";
		case IPTPUIConstants.PROC_RUNNING:
			return "Running";
		case IPTPUIConstants.PROC_EXITED:
			return "Exited";
		case IPTPUIConstants.PROC_EXITED_SIGNAL:
			return "Exited Signal";
		case IPTPUIConstants.PROC_STOPPED:
			return "Stopped";
		case IPTPUIConstants.PROC_ERROR:
			return "Error";
		default:
			return "Error";
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#getStatus(java.lang.String)
	 */
	public int getStatus(String id) {
		return getStatus(getCurrentJobId(), Integer.parseInt(id));
	}	
	/** Get Status
	 * @param job_id job ID
	 * @param proc_id process ID
	 * @return status 
	 */
	public int getStatus(String job_id, int task_id) {
		return getProcessStatus(findProcess(job_id, task_id));
	}
	/** Get process status
	 * @param proc process
	 * @return status
	 */
	public int getProcessStatus(IPProcess proc) {
		if (proc != null) {
			String status = proc.getStatus();
			if (status.equals(IPProcess.STARTING))
				return IPTPUIConstants.PROC_STARTING;
			else if (status.equals(IPProcess.RUNNING))
				return IPTPUIConstants.PROC_RUNNING;
			else if (status.equals(IPProcess.EXITED))
				return IPTPUIConstants.PROC_EXITED;
			else if (status.equals(IPProcess.EXITED_SIGNALLED))
				return IPTPUIConstants.PROC_EXITED_SIGNAL;
			else if (status.equals(IPProcess.STOPPED))
				return IPTPUIConstants.PROC_STOPPED;
			else if (status.equals(IPProcess.ERROR))
				return IPTPUIConstants.PROC_ERROR;
		}
		return IPTPUIConstants.PROC_ERROR;
	}
	/** Find process
	 * @param job_id job ID
	 * @param id process ID
	 * @return process
	 */
	public IPProcess findProcess(String job_id, int task_id) {
		return findProcess(findJobById(job_id), task_id);
	}
	/** Find process
	 * @param job job
	 * @param id process ID
	 * @return
	 */
	public IPProcess findProcess(IPJob job, int task_id) {
		if (job == null)
			return null;
		return job.findProcessByTaskId(task_id);
		//return job.findProcess(id);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#getName(java.lang.String)
	 */
	public String getName(String id) {
		IPJob job = findJobById(id);
		if (job == null)
			return "";
		final IPQueue queue = job.getQueue();
		final IResourceManager rm = queue.getResourceManager();
		return rm.getName() + ": " + queue.getName() + ": " + job.getName();
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
	/** Add a job
	 * @param job
	 */
	public void addJob(IPJob job) {
		if (!jobList.containsKey(job.getIDString())) {
			IPProcess[] pProcesses = job.getSortedProcesses();
			int total_element = pProcesses.length;
			if (total_element > 0) {
				IElementHandler elementHandler = new ElementHandler();
				IElementSet set = elementHandler.getSetRoot();
				for (int i = 0; i < total_element; i++) {
					//task id for element key
					set.add(createElement(set, String.valueOf(pProcesses[i].getTaskId()), pProcesses[i].getIDString()));
				}
				elementHandler.add(set);
				jobList.put(job.getIDString(), elementHandler);
			}
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#initial()
	 */
	public String initial() {
		IPJob[] jobs = getJobs();
		for (int j = 0; j < jobs.length; j++) {
			addJob(jobs[j]);
		}
		
		String last_job_id = EMPTY_ID;
		if (cur_queue_id.equals(EMPTY_ID)) {
			IPQueue[] queues = getQueues();
			for (int iq=0; iq<queues.length; ++iq) {
				jobs = queues[iq].getJobs();
				if (jobs.length > 0) {
					// focus on last job
					final IPJob lastJob = jobs[jobs.length - 1];
					cur_queue_id = lastJob.getQueue().getIDString();
					last_job_id = lastJob.getIDString();
				}
			}
		}
		else
		{
			IPQueue queue = getQueue();
			jobs = queue.getJobs();
			if (jobs.length > 0) {
				// focus on last job
				final IPJob lastJob = jobs[jobs.length - 1];
				last_job_id = lastJob.getIDString();
			}
		}
		setCurrentSetId(IElementHandler.SET_ROOT_ID);
		return last_job_id;
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
	/** Is current set contain process
	 * @param jid job ID
	 * @param processID process ID
	 * @return true if job contains process
	 */
	public boolean isCurrentSetContainProcess(String jid, String processID) {
		if (!getCurrentJobId().equals(jid))
			return false;
		IElementHandler elementHandler = getElementHandler(getCurrentJobId());
		if (elementHandler == null)
			return false;
		IElementSet set = elementHandler.getSet(getCurrentSetId());
		if (set == null)
			return false;
		return set.contains(processID);
	}
	/*******************************************************************************************************************************************************************************************************************************************************************************************************
	 * terminate action
	 ******************************************************************************************************************************************************************************************************************************************************************************************************/
	/** terminate all processes in current job
	 * @throws CoreException
	 */
	public void terminateAll() throws CoreException {
		terminateAll(getCurrentJobId());
	}
	/** terminate all processes in given job
	 * @param job_id job ID
	 * @throws CoreException
	 */
	public void terminateAll(String job_id) throws CoreException {
		modelPresentation.abortJob(getName(job_id));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#removeJob(org.eclipse.ptp.core.IPJob)
	 */
	public void removeJob(IPJob job) {
		jobList.remove(job.getIDString());
		super.removeJob(job);
	}

	private IPQueue getQueue() {
		IPUniverse universe = modelPresentation.getUniverse();
		if (universe == null) {
			return null;
		}
		if (cur_queue_id.equals(EMPTY_ID)) {
			IPJob job = universe.findJobById(cur_job_id);
			if (job == null) {
				return null;
			}
			IPQueue queue = job.getQueue();
			cur_queue_id = queue.getIDString();
			return queue;
		}
		return universe.findQueueById(cur_queue_id);
	}
	
	public void setCurrentQueueId(String id) {
		if (!cur_queue_id.equals(id)) {
			cur_queue_id = id;
			setCurrentJobId(EMPTY_ID);
		}
	}
	public String getQueueID() {
		return cur_queue_id;
	}
	
	public IPJob[] getJobsFromCurrentQueue() {
		IPQueue queue = getQueue();
		if (queue != null) {
			return queue.getJobs();
		}
		return new IPJob[0];
	}
	
}
