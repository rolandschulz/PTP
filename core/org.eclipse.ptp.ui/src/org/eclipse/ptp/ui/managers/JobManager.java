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

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ptp.core.elementcontrols.IPJobControl;
import org.eclipse.ptp.core.elementcontrols.IPQueueControl;
import org.eclipse.ptp.core.elements.IPElement;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPMachine;
import org.eclipse.ptp.core.elements.IPProcess;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.attributes.ElementAttributes;
import org.eclipse.ptp.ui.IPTPUIConstants;
import org.eclipse.ptp.ui.PTPUIPlugin;
import org.eclipse.ptp.ui.listeners.IJobChangedListener;
import org.eclipse.ptp.ui.model.Element;
import org.eclipse.ptp.ui.model.ElementHandler;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.ptp.ui.model.IElementSet;
import org.eclipse.ui.PlatformUI;

/**
 * @author Clement chu
 * 
 */
public class JobManager extends AbstractUIManager {
	protected Map<String, IElementHandler> jobList = new HashMap<String, IElementHandler>();
	protected IPJob cur_job = null;
	protected IPQueue cur_queue = null;

	/** Add a job
	 * @param job
	 */
	public void addJob(IPJob job) {
		if (!jobList.containsKey(job.getID())) {
			IElementHandler elementHandler = new ElementHandler();
			jobList.put(job.getID(), elementHandler);
		}
	}
	
	public void addProcess(IPProcess proc) {
		addJob(proc.getJob());
		IElementHandler elementHandler = jobList.get(proc.getJob().getID());
		IElementSet set = elementHandler.getSetRoot();
		set.add(createElement(set, proc.getID(), proc.getProcessNumber()));
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
	public IPProcess findProcess(IPJob job, String task_id) {
		if (job == null)
			return null;
		return job.getProcessByNumber(task_id);
		//return job.findProcess(id);
	}
	
	public IPProcess findProcess(String proc_id) {
		IPJob job = getJob();
		if (job != null) {
			return job.getProcessById(proc_id);
		}
		return null;
	}
	
	/** Get current job ID
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
		return (IElementHandler) jobList.get(id);
	}
	
	/** Get Jobs
	 * @return jobs
	 */
	public IPJob[] getJobs() {
		IPQueue queue = getQueue();
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
		String name = "";
		final IPQueue queue = getQueue();
		if (queue != null) {
			IPJob job = queue.getJobById(id);
			if (job != null) {
				name = queue.getResourceManager().getName() + ":" + queue.getName() + ":" + job.getName();
			}
		}
		return name;
	}
	/** Get process status
	 * @param proc process
	 * @return status
	 */
	public int getProcessStatus(IPProcess proc) {
		if (proc != null) {
			switch (proc.getState()) {
			case STARTING:
				return IPTPUIConstants.PROC_STARTING;
			case RUNNING:
				return IPTPUIConstants.PROC_RUNNING;
			case EXITED:
				return IPTPUIConstants.PROC_EXITED;
			case EXITED_SIGNALLED:
				return IPTPUIConstants.PROC_EXITED_SIGNAL;
			case STOPPED:
				return IPTPUIConstants.PROC_STOPPED;
			case ERROR:
				return IPTPUIConstants.PROC_ERROR;
			}
		}
		return IPTPUIConstants.PROC_ERROR;
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
			return getProcessStatus(job.getProcessById(id));
		}
		return getProcessStatus(null);
	}
	
	public IPElement initial() {
		IPJob[] jobs = getJobs();
		for (int j = 0; j < jobs.length; j++) {
			addJob(jobs[j]);
		}
		
		IPJob last_job = null;
		final LinkedList<IPQueue> queues = new LinkedList<IPQueue>(Arrays.asList(getQueues()));
		final IPQueue curQueue = getQueue();
		// move the current Queue to the front of the list
		if (curQueue != null) {
			queues.remove(curQueue);
			queues.add(0, curQueue);
		}
		// loop until we find a job or run out of queues
		for (Iterator qit = queues.iterator(); qit.hasNext() && last_job == null; ) {
			final IPQueue q = (IPQueue) qit.next();
			jobs = q.getJobs();
			if (jobs != null && jobs.length > 0) {
				// focus on last job
				final IPJob lastJob = jobs[jobs.length - 1];
				cur_queue = q;
				last_job = lastJob;
			}
		}
		setCurrentSetId(IElementHandler.SET_ROOT_ID);
		return last_job;
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
		IPJob[] jobs = getQueue().getJobs();
		for (IPJob job : jobs) {
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
		if (job_id == null) {
			return null;
		}
		
		IPQueue queue = getQueue();
		if (queue != null) {
			return queue.getJobById(job_id);
		}
		return null;
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
		((IPQueueControl) (job.getQueue())).removeJob((IPJobControl) job);
		fireJobChangedEvent(IJobChangedListener.REMOVED, null, job.getID());
		jobList.remove(job.getID());
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#removeAllStoppedJobs()
	 */
	public void removeAllStoppedJobs() {
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor pmonitor) throws InvocationTargetException {
				if (pmonitor == null)
					pmonitor = new NullProgressMonitor();
				try {
					IPQueue queue = getQueue();
					if (queue != null) {
						IPJob[] jobs = queue.getJobs();
						if (jobs.length > 0) {
							pmonitor.beginTask("Removing stopped jobs...", jobs.length);
							for (IPJob job : jobs) {
								if (pmonitor.isCanceled())
									throw new InvocationTargetException(new Exception("Cancelled by user"));
								if (job.isTerminated())
									removeJob(job);
								pmonitor.worked(1);
							}
						}
					}
				} finally {
					pmonitor.done();
				}
			}
		};
		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(runnable);
		} catch (InterruptedException e) {
			PTPUIPlugin.log(e);
		} catch (InvocationTargetException e1) {
			PTPUIPlugin.log(e1);
		}
	}

}
