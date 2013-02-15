/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.internal.rm.jaxb.control.core.runnable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.core.jobs.IJobControl;
import org.eclipse.ptp.core.jobs.IJobStatus;
import org.eclipse.ptp.core.jobs.JobManager;
import org.eclipse.ptp.internal.rm.jaxb.control.core.ICommandJobStatus;
import org.eclipse.ptp.internal.rm.jaxb.control.core.ICommandJobStatusMap;
import org.eclipse.ptp.internal.rm.jaxb.control.core.JAXBControlConstants;
import org.eclipse.ptp.internal.rm.jaxb.control.core.JAXBControlCorePlugin;

/**
 * Class for handling status of submitted jobs.
 * 
 * @author arossi
 * 
 */
public class JobStatusMap extends Thread implements ICommandJobStatusMap {

	private final IJobControl fControl;
	private final Map<String, ICommandJobStatus> fJobStatusMap = new HashMap<String, ICommandJobStatus>();
	private boolean running = false;

	private static final Map<IJobControl, ICommandJobStatusMap> fMaps = Collections
			.synchronizedMap(new HashMap<IJobControl, ICommandJobStatusMap>());

	public static ICommandJobStatusMap getInstance(IJobControl control) {
		ICommandJobStatusMap map = fMaps.get(control);
		if (map == null) {
			map = new JobStatusMap(control);
			fMaps.put(control, map);
		}
		return map;
	}

	private JobStatusMap(IJobControl control) {
		fControl = control;
	}

	/*
	 * @see org.eclipse.ptp.rm.jaxb.core.ICommandJobStatusMap#addJobStatus(java.lang .String,
	 * org.eclipse.ptp.rm.jaxb.core.ICommandJobStatus)
	 */
	public boolean addJobStatus(String jobId, ICommandJobStatus status) {
		boolean notifyAdd = false;
		boolean exists = false;
		synchronized (fJobStatusMap) {
			exists = fJobStatusMap.containsKey(jobId);
			notifyAdd = !exists && !IJobStatus.UNDETERMINED.equals(status.getState());
			fJobStatusMap.put(jobId, status);
		}
		if (notifyAdd) {
			try {
				status.initialize(jobId);
			} catch (CoreException e) {
				JAXBControlCorePlugin.log(e);
			}
			JobManager.getInstance().fireJobAdded(status);
		}
		return !exists;
	}

	/*
	 * Synchronized cancel. External calls are premature and thus should not block waiting for the remote files if any.(non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.ICommandJobStatusMap#cancel(java.lang.String )
	 */
	public ICommandJobStatus cancel(String jobId) {
		ICommandJobStatus status = null;
		synchronized (fJobStatusMap) {
			status = fJobStatusMap.get(jobId);
			if (status != null) {
				status.cancel();
				status.setState(IJobStatus.CANCELED);
			}
		}
		return status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rm.jaxb.control.core.ICommandJobStatusMap#dispose()
	 */
	public void dispose() {
		synchronized (fJobStatusMap) {
			running = false;
			fJobStatusMap.notifyAll();
		}
		fMaps.remove(fControl);
	}

	/**
	 * Must be called under synchronization.
	 * 
	 * @param jobId
	 *            either internal UUID or scheduler id for the job.
	 * @param block
	 *            wait for the remote files
	 * @param monitor
	 *            progress monitor
	 * @return job status
	 */
	private ICommandJobStatus doTerminated(String jobId, boolean block, IProgressMonitor monitor) {
		ICommandJobStatus status = fJobStatusMap.get(jobId);
		if (status != null) {
			String d = status.getStateDetail();
			if (!IJobStatus.JOB_OUTERR_READY.equals(d)) {
				block = block && !IJobStatus.CANCELED.equals(d) && !IJobStatus.FAILED.equals(d);
				if (!status.isInteractive()) {
					if (block) {
						status.maybeWaitForHandlerFiles(JAXBControlConstants.READY_FILE_BLOCK, monitor);
					} else {
						status.maybeWaitForHandlerFiles(0, monitor);
					}
				}
			}
			status.cancel();
		}
		return status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.ICommandJobStatusMap#getStatus(java.lang .String)
	 */
	public ICommandJobStatus getStatus(String jobId) {
		ICommandJobStatus status = null;
		synchronized (fJobStatusMap) {
			status = fJobStatusMap.get(jobId);
		}
		return status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rm.jaxb.control.core.ICommandJobStatusMap#initialize()
	 */
	public void initialize() {
		if (!isRunning()) {
			start();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rm.jaxb.control.core.ICommandJobStatusMap#isEmpty()
	 */
	public boolean isEmpty() {
		return fJobStatusMap.isEmpty();
	}

	/**
	 * FIXME Why not just return running?
	 * 
	 * @return whether the thread is active
	 */
	private boolean isRunning() {
		boolean b = false;
		synchronized (fJobStatusMap) {
			b = running;
		}
		return b;
	}

	/**
	 * Must be called under synchronization.
	 * 
	 * @param jobId
	 *            either internal UUID or scheduler id for the job.
	 * @param block
	 *            wait for the remote files
	 * @param monitor
	 *            progress monitor
	 * @return job status
	 */
	private ICommandJobStatus remove(String jobId, boolean block, IProgressMonitor monitor) {
		ICommandJobStatus status = doTerminated(jobId, block, monitor);
		fJobStatusMap.remove(jobId);
		return status;
	}

	/**
	 * Thread daemon for cleanup on the map. Eliminates stray completed state information, and also starts the stream proxies on
	 * jobs which have been submitted to a scheduler and have become active.
	 */
	@Override
	public void run() {
		Map<String, String> toPrune = new HashMap<String, String>();

		synchronized (fJobStatusMap) {
			running = true;
		}

		while (isRunning()) {
			synchronized (fJobStatusMap) {
				try {
					fJobStatusMap.wait(2 * JAXBControlConstants.MINUTE_IN_MS);
				} catch (InterruptedException ignored) {
					// Ignore
				}

				for (String jobId : fJobStatusMap.keySet()) {
					IJobStatus status = null;
					try {
						status = fControl.getJobStatus(jobId, true, null);
					} catch (CoreException e) {
						// Ignore
					}
					if (status == null || IJobStatus.COMPLETED.equals(status.getState())) {
						toPrune.put(jobId, jobId);
					}
				}

				for (String jobId : toPrune.keySet()) {
					remove(jobId, true, null);
				}
				toPrune.clear();
			}
		}

		synchronized (fJobStatusMap) {
			for (String jobId : fJobStatusMap.keySet()) {
				doTerminated(jobId, false, null);
			}
			fJobStatusMap.clear();
		}
	}

	/*
	 * Synchronized terminate. (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.ICommandJobStatusMap#terminated(java.lang .String)
	 */
	public ICommandJobStatus terminated(String jobId, IProgressMonitor monitor) {
		ICommandJobStatus status = null;
		synchronized (fJobStatusMap) {
			status = doTerminated(jobId, true, monitor);
		}
		return status;
	}
}
