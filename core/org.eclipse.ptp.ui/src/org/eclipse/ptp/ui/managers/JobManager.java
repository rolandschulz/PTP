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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
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
	 * @see org.eclipse.ptp.ui.IManager#clear()
	 */
	public void clear() {
		if (jobList != null) {
			jobList.clear();
		}
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
	/** Find process
	 * @param job_id job ID
	 * @param id process ID
	 * @return process
	 */
	public IPProcess findProcess(String job_id, int task_id) {
		return findProcess(findJobById(job_id), task_id);
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
		return (IElementHandler) jobList.get(id);
	}
	
	/** Get Jobs
	 * @return jobs
	 */
	public IPJob[] getJobs() {
		IPUniverse universe = getUniverse();
		if (universe == null) {
			return new IPJob[0];
		}
		return universe.getJobs();
	}
	
	public IPJob[] getJobsFromCurrentQueue() {
		IPQueue queue = getCurrentQueue();
		if (queue != null) {
			return queue.getJobs();
		}
		return new IPJob[0];
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
		final IPJob job = findJobById(id);
		final IPQueue queue;
		final String jobName;
		if (job != null) {
			jobName = job.getName();
			// get the queue from the selected job
			queue = job.getQueue();
		}
		else {
			jobName = "";
			// get the currently selected queue
			queue = getCurrentQueue();
		}
		final String queueName;
		if (queue != null) {
			final IResourceManager rm = queue.getResourceManager();
			queueName = rm.getName() + ": " + queue.getName() + ": ";
		}
		else {
			queueName = "";
		}
		return queueName + jobName;
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
	public String getQueueID() {
		return cur_queue_id;
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
	
	public String initial() {
		IPJob[] jobs = getJobs();
		for (int j = 0; j < jobs.length; j++) {
			addJob(jobs[j]);
		}
		
		String last_job_id = EMPTY_ID;
		final LinkedList queues = new LinkedList(Arrays.asList(getQueues()));
		final IPQueue curQueue = getCurrentQueue();
		// move the current Queue to the front of the list
		if (curQueue != null) {
			queues.remove(curQueue);
			queues.add(0, curQueue);
		}
		// loop until we find a job or run out of queues
		for (Iterator qit = queues.iterator(); qit.hasNext() && last_job_id.equals(EMPTY_ID); ) {
			final IPQueue q = (IPQueue) qit.next();
			jobs = q.getSortedJobs();
			if (jobs.length > 0) {
				// focus on last job
				final IPJob lastJob = jobs[jobs.length - 1];
				cur_queue_id = q.getIDString();
				last_job_id = lastJob.getIDString();
			}
		}
		setCurrentSetId(IElementHandler.SET_ROOT_ID);
		return last_job_id;
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
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#removeJob(org.eclipse.ptp.core.IPJob)
	 */
	public void removeJob(IPJob job) {
		jobList.remove(job.getIDString());
		super.removeJob(job);
	}
	
	/** Set current job ID
	 * @param job_id Job ID
	 */
	public void setCurrentJobId(String job_id) {
		String tmp_jod_id = cur_job_id;
		cur_job_id = job_id;		
		fireJobChangedEvent(IJobChangedListener.CHANGED, job_id, tmp_jod_id);
	}
	public void setCurrentQueueId(String id) {
		if (!cur_queue_id.equals(id)) {
			cur_queue_id = id;
			setCurrentJobId(EMPTY_ID);
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
		terminateAll(getCurrentJobId());
	}
	
	/** terminate all processes in given job
	 * @param job_id job ID
	 * @throws CoreException
	 */
	public void terminateAll(String job_id) throws CoreException {
		modelPresentation.abortJob(getName(job_id));
	}
	private IPQueue getCurrentQueue() {
		final IPQueue queue;
		if (cur_queue_id.equals(EMPTY_ID)) {
			IPUniverse universe = getUniverse();
			IPJob job = universe.findJobById(cur_job_id);
			if (job == null) {
				queue = null;
			}
			else {
				queue = job.getQueue();
			}
		}
		else {
			queue = getQueue(cur_queue_id);
		}
		if (queue == null) {
			cur_queue_id = EMPTY_ID;
		}
		else {
			cur_queue_id = queue.getIDString();
		}
		return queue;
	}
	
	private IPQueue getQueue(String id) {
		IPUniverse universe = getUniverse();
		if (universe == null) {
			return null;
		}
		return universe.findQueueById(id);
	}
	
	private IPUniverse getUniverse() {
		return modelPresentation.getUniverse();
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
	
}
