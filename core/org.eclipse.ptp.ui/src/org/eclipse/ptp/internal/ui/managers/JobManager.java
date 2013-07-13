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
package org.eclipse.ptp.internal.ui.managers;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ptp.core.jobs.IJobStatus;
import org.eclipse.ptp.core.jobs.IPJobStatus;
import org.eclipse.ptp.internal.ui.IJobManager;
import org.eclipse.ptp.internal.ui.ParallelImages;
import org.eclipse.ptp.internal.ui.listeners.IJobChangedListener;
import org.eclipse.ptp.internal.ui.messages.Messages;
import org.eclipse.ptp.internal.ui.model.ElementHandler;
import org.eclipse.ptp.internal.ui.model.IElementHandler;
import org.eclipse.ptp.internal.ui.model.IElementSet;
import org.eclipse.swt.graphics.Image;

/**
 * @author Clement chu
 * @since 4.0
 * 
 */

public class JobManager extends AbstractElementManager implements IJobManager {
	protected Map<String, IJobStatus> jobList = new HashMap<String, IJobStatus>();
	protected IJobStatus cur_job = null;
	protected final String DEFAULT_TITLE = Messages.JobManager_0;

	private final ListenerList jobListeners = new ListenerList();

	/**
	 * Add a new job to jobList. Check for any new processes and add these to the element handler for the job.
	 * 
	 * @param job
	 * @since 7.0
	 */
	private void addJob(IJobStatus job) {
		if (job != null) {
			IPJobStatus pJob = (IPJobStatus) job.getAdapter(IPJobStatus.class);
			if (pJob != null) {
				IElementSet set = createElementHandler(job).getSet(IElementHandler.SET_ROOT_ID);
				int nprocs = pJob.getNumberOfProcesses();
				BitSet elements = new BitSet(nprocs);
				elements.set(0, nprocs);
				set.addElements(elements);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IJobManager#addJobChangedListener(org.eclipse.ptp.internal.ui.listeners.IJobChangedListener)
	 */
	@Override
	public void addJobChangedListener(IJobChangedListener jobListener) {
		jobListeners.add(jobListener);
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

	/**
	 * @since 7.0
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IJobManager#createElementHandler(org.eclipse.ptp.core.jobs.IJobStatus)
	 */
	@Override
	public IElementHandler createElementHandler(IJobStatus job) {
		IElementHandler handler = getElementHandler(job.getJobId());
		if (handler == null) {
			handler = new ElementHandler();
			jobList.put(job.getJobId(), job);
			setElementHandler(job.getJobId(), handler);
		}
		return handler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IJobManager#findJobById(java.lang.String)
	 */
	/**
	 * @since 7.0
	 */
	@Override
	public IJobStatus findJobById(String job_id) {
		return jobList.get(job_id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IJobManager#fireJobChangedEvent(int, java.lang.String, java.lang.String)
	 */
	@Override
	public void fireJobChangedEvent(final int type, final String cur_job_id, final String pre_job_id) {
		Object[] array = jobListeners.getListeners();
		for (Object element : array) {
			final IJobChangedListener listener = (IJobChangedListener) element;
			SafeRunner.run(new SafeRunnable() {
				@Override
				public void run() {
					listener.jobChangedEvent(type, cur_job_id, pre_job_id);
				}
			});
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IElementManager#getFullyQualifiedName(java.lang.String )
	 */
	@Override
	public String getFullyQualifiedName(String id) {
		if (id.equals(EMPTY_ID)) {
			return DEFAULT_TITLE;
		}
		IJobStatus job = getJob();
		if (job != null) {
			return job.getJobId(); //$NON-NLS-1$
		}
		return ""; //$NON-NLS-1$
	}

	@Override
	public Image getImage(int index, boolean isSelected) {
		IJobStatus job = getJob();
		if (job != null) {
			IPJobStatus pJob = (IPJobStatus) job.getAdapter(IPJobStatus.class);
			if (pJob != null) {
				String state = pJob.getProcessState(index);
				if (state == IPJobStatus.RUNNING) {
					return ParallelImages.procImages[1][isSelected ? 1 : 0];
				}
				if (state == IPJobStatus.SUSPENDED) {
					return ParallelImages.procImages[2][isSelected ? 1 : 0];
				}
				if (state == IPJobStatus.COMPLETED) {
					return ParallelImages.procImages[3][isSelected ? 1 : 0];
				}
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IJobManager#getJob()
	 */
	/**
	 * @since 7.0
	 */
	@Override
	public IJobStatus getJob() {
		return cur_job;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IJobManager#getJobs()
	 */
	/**
	 * @since 7.0
	 */
	@Override
	public IJobStatus[] getJobs() {
		return jobList.values().toArray(new IJobStatus[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IElementManager#getName(java.lang.String)
	 */
	@Override
	public String getName(String id) {
		IJobStatus job = findJobById(id);
		if (job == null) {
			return ""; //$NON-NLS-1$
		}
		return job.getJobId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IJobManager#hasStoppedJob()
	 */
	@Override
	public boolean hasStoppedJob() {
		for (IJobStatus job : getJobs()) {
			if (job.getState().equals(IJobStatus.COMPLETED)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IJobManager#initialize()
	 */
	/**
	 * @since 7.0
	 */
	@Override
	public void initialize() {
		for (IJobStatus job : org.eclipse.ptp.core.jobs.JobManager.getInstance().getJobs()) {
			addJob(job);
		}

		setCurrentSetId(IElementHandler.SET_ROOT_ID);
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
	 * @see org.eclipse.ptp.ui.IJobManager#removeAllCompletedJobs()
	 */
	/**
	 * @since 7.0
	 */
	@Override
	public void removeAllCompletedJobs() {
		for (IJobStatus job : getJobs()) {
			if (job.getState().equals(IJobStatus.COMPLETED)) {
				jobList.remove(job.getJobId());
			}
		}
		fireJobChangedEvent(IJobChangedListener.REMOVED, null, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IJobManager#removeJobChangedListener(org.eclipse.ptp.internal.ui.listeners.IJobChangedListener)
	 */
	@Override
	public void removeJobChangedListener(IJobChangedListener jobListener) {
		jobListeners.remove(jobListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IElementManager#setCurrentSetId(java.lang.String)
	 */
	@Override
	public void setCurrentSetId(String set_id) {
		cur_set_id = set_id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IJobManager#setJob(org.eclipse.ptp.core.elements.IPJob )
	 */
	/**
	 * @since 7.0
	 */
	@Override
	public void setJob(IJobStatus job) {
		String old_id = null;
		if (cur_job != null) {
			old_id = cur_job.getJobId();
		}
		String new_id = null;
		if (job != null) {
			new_id = job.getJobId();
			addJob(job);
		}
		cur_job = job;
		fireJobChangedEvent(IJobChangedListener.CHANGED, new_id, old_id);
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
	@Override
	public int size() {
		return jobList.size();
	}
}
