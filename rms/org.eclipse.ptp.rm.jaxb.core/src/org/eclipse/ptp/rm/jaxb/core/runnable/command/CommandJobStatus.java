/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.core.runnable.command;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.ptp.remote.core.IRemoteProcess;
import org.eclipse.ptp.rm.jaxb.core.ICommandJob;
import org.eclipse.ptp.rm.jaxb.core.ICommandJobStatus;
import org.eclipse.ptp.rm.jaxb.core.ICommandJobStreamsProxy;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerControl;
import org.eclipse.ptp.rm.jaxb.core.JAXBCorePlugin;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeType;
import org.eclipse.ptp.rm.jaxb.core.data.PropertyType;
import org.eclipse.ptp.rm.jaxb.core.utils.FileUtils;
import org.eclipse.ptp.rm.jaxb.core.utils.RemoteServicesDelegate;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;
import org.eclipse.ptp.rmsystem.IJobStatus;

/**
 * Extension of the IJobStatus class to handle resource manager command jobs.
 * Also handles availability notification for remote stdout and stderr files.
 * 
 * @author arossi
 * 
 */
public class CommandJobStatus implements ICommandJobStatus {

	/**
	 * Checks for file existence, then waits 3 seconds to compare file length.
	 * If block is false, the listeners may be notified that the file is still
	 * not ready; else the listeners will receive a ready = true notification
	 * when the file does finally stabilize, provided this occurs within the
	 * block parameter (seconds).
	 * 
	 * @author arossi
	 */
	private class FileReadyChecker extends Thread {
		private boolean ready;
		private int block;
		private String path;

		@Override
		public void run() {
			ready = false;
			long timeout = block * 1000;
			RemoteServicesDelegate d = control.getRemoteServicesDelegate();
			long start = System.currentTimeMillis();
			while (!ready) {
				try {
					ready = FileUtils.isStable(d.getRemoteFileManager(), path, 3, new NullProgressMonitor());
				} catch (Throwable t) {
					JAXBCorePlugin.log(t);
				}

				if (System.currentTimeMillis() - start >= timeout) {
					break;
				}

				synchronized (this) {
					try {
						wait(IJAXBNonNLSConstants.STANDARD_WAIT);
					} catch (InterruptedException ignored) {
					}
				}
			}
		}

	}

	private final String rmUniqueName;
	private final IJAXBResourceManagerControl control;
	private final ICommandJob open;

	private String jobId;
	private ILaunchConfiguration launchConfig;
	private String state;
	private String stateDetail;
	private String remoteOutputPath;
	private String remoteErrorPath;
	private ICommandJobStreamsProxy proxy;
	private IRemoteProcess process;

	private boolean waitEnabled;
	private long lastUpdateRequest;
	private boolean dirty = false;
	private boolean fFilesChecked = false;

	/**
	 * @param rmUniqueName
	 *            owner resource manager
	 * @param control
	 *            resource manager control
	 */
	public CommandJobStatus(String rmUniqueName, ICommandJob open, IJAXBResourceManagerControl control) {
		this(rmUniqueName, null, UNDETERMINED, open, control);
	}

	/**
	 * @param rmUniqueName
	 *            owner resource manager
	 * @param jobId
	 * @param state
	 * @param control
	 *            resource manager control
	 */
	public CommandJobStatus(String rmUniqueName, String jobId, String state, ICommandJob open, IJAXBResourceManagerControl control) {
		this.rmUniqueName = rmUniqueName;
		this.jobId = jobId;
		setState(state);
		this.open = open;
		this.control = control;
		assert (null != control);
		waitEnabled = true;
		lastUpdateRequest = 0;
	}

	/**
	 * Closes the proxy and calls destroy on the process. Used for interactive
	 * job cancellation.
	 * 
	 * @return true if canceled during this call.
	 */
	public synchronized boolean cancel() {
		if (getStateRank(stateDetail) > 4) {
			return false;
		}

		/*
		 * If this process is persistent (open), call terminate on the job, as
		 * it may still be running; the process will be killed and the proxy
		 * closed inside the job
		 */
		if (open != null) {
			open.terminate();
			return true;
		}

		cancelWait();
		if (process != null && !process.isCompleted()) {
			process.destroy();
			if (proxy != null) {
				proxy.close();
			}
			return true;
		}
		return false;
	}

	/**
	 * Notifies all callers of <code>waitForId</code> to exit wait.
	 */
	public void cancelWait() {
		synchronized (this) {
			waitEnabled = false;
			notifyAll();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.ICommandJobStatus#getControl()
	 */
	public IJAXBResourceManagerControl getControl() {
		return control;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IJobStatus#getErrorPath()
	 */
	public String getErrorPath() {
		return remoteErrorPath;
	}

	/**
	 * @return jobId either internal UUID or resource-specific id
	 */
	public synchronized String getJobId() {
		return jobId;
	}

	public synchronized long getLastUpdateRequest() {
		return lastUpdateRequest;
	}

	/**
	 * @return configuration used for this submission.
	 */
	public ILaunchConfiguration getLaunchConfiguration() {
		return launchConfig;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IJobStatus#getOutputPath()
	 */
	public String getOutputPath() {
		return remoteOutputPath;
	}

	/**
	 * 
	 * @return owner resource manager id
	 */
	public String getRmUniqueName() {
		return rmUniqueName;
	}

	/**
	 * @return state of the job (not of the submission process).
	 */
	public synchronized String getState() {
		return state;
	}

	/**
	 * @return more specific state identifier.
	 */
	public synchronized String getStateDetail() {
		return stateDetail;
	}

	/**
	 * Wrapper containing monitoring functionality for the associated output and
	 * error streams.
	 */
	public IStreamsProxy getStreamsProxy() {
		return proxy;
	}

	/*
	 * NOTE: since the script/job attribute defining this path is generated
	 * prior to submission, @jobId cannot appear in the path; at the same time,
	 * a batch variable replacement will not work, as that would not be
	 * interpretable for the RM. One actually needs to configure two separate
	 * strings in this case, giving one to the script and one to the resource
	 * manager. We treat the path as requiring a possible substitution of the
	 * jobId tag.
	 * 
	 * @see
	 * org.eclipse.ptp.rm.jaxb.core.ICommandJobStatus#initialize(java.lang.String
	 * )
	 */
	public void initialize(String jobId) {
		this.jobId = jobId;
		String path = null;
		RMVariableMap rmVarMap = control.getEnvironment();
		Object o = rmVarMap.get(IJAXBNonNLSConstants.STDOUT_REMOTE_FILE);
		if (o != null) {
			if (o instanceof PropertyType) {
				path = (String) ((PropertyType) o).getValue();
			} else if (o instanceof AttributeType) {
				path = (String) ((PropertyType) o).getValue();
			}
			path = rmVarMap.getString(path);
			remoteOutputPath = path.replaceAll(IJAXBNonNLSConstants.JOB_ID_TAG, jobId);
		}
		o = rmVarMap.get(IJAXBNonNLSConstants.STDERR_REMOTE_FILE);
		if (o != null) {
			if (o instanceof PropertyType) {
				path = (String) ((PropertyType) o).getValue();
			} else if (o instanceof AttributeType) {
				path = (String) ((AttributeType) o).getValue();
			}
			path = rmVarMap.getString(path);
			remoteErrorPath = path.replaceAll(IJAXBNonNLSConstants.JOB_ID_TAG, jobId);
		}
	}

	/**
	 * @return whether a process object has been attached to this status object
	 *         (in which case the submission is not through an asynchronous job
	 *         scheduler).
	 */
	public boolean isInteractive() {
		return process != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.jaxb.core.ICommandJobStatus#maybeWaitForHandlerFiles
	 * (int)
	 */
	public void maybeWaitForHandlerFiles(int blockForSecs) {
		if (fFilesChecked) {
			return;
		}

		FileReadyChecker tout = null;
		FileReadyChecker terr = null;

		if (remoteOutputPath != null) {
			tout = checkForReady(remoteOutputPath, blockForSecs);

		}

		if (remoteErrorPath != null) {
			terr = checkForReady(remoteErrorPath, blockForSecs);
		}

		if (tout == null && terr == null) {
			fFilesChecked = true;
			return;
		}

		if (tout != null) {
			try {
				tout.join();
			} catch (InterruptedException ignored) {
			}
		}

		if (terr != null) {
			try {
				terr.join();
			} catch (InterruptedException ignored) {
			}
		}

		if ((tout == null || tout.ready) && (terr == null || terr.ready)) {
			setState(IJobStatus.JOB_OUTERR_READY);
		}

		fFilesChecked = true;
	}

	/**
	 * @param launchConfig
	 *            configuration used for this submission.
	 */
	public void setLaunchConfig(ILaunchConfiguration launchConfig) {
		this.launchConfig = launchConfig;
	}

	/**
	 * @param process
	 *            object (used for interactive cancellation)
	 */
	public void setProcess(IRemoteProcess process) {
		this.process = process;
	}

	/**
	 * We also immediately dereference any paths associated with the job by
	 * calling intialize, as the jobId property may not be in the environment
	 * after this initial call returns.
	 * 
	 * @param proxy
	 *            Wrapper containing monitoring functionality for the associated
	 *            output and error streams.
	 */
	public void setProxy(ICommandJobStreamsProxy proxy) {
		this.proxy = proxy;
		initialize(jobId);
	}

	/**
	 * @param state
	 *            of the job (not of the submission process).
	 */
	public synchronized void setState(String state) {
		if (!canUpdateState(state)) {
			return;
		}

		dirty = false;
		String previousDetail = stateDetail;

		if (UNDETERMINED.equals(state)) {
			this.state = UNDETERMINED;
			stateDetail = UNDETERMINED;
		} else if (SUBMITTED.equals(state)) {
			this.state = SUBMITTED;
			stateDetail = SUBMITTED;
		} else if (RUNNING.equals(state)) {
			this.state = RUNNING;
			stateDetail = RUNNING;
		} else if (SUSPENDED.equals(state)) {
			this.state = SUSPENDED;
			stateDetail = SUSPENDED;
		} else if (COMPLETED.equals(state)) {
			this.state = COMPLETED;
			stateDetail = COMPLETED;
		} else if (QUEUED_ACTIVE.equals(state)) {
			this.state = SUBMITTED;
			stateDetail = QUEUED_ACTIVE;
		} else if (SYSTEM_ON_HOLD.equals(state)) {
			this.state = SUBMITTED;
			stateDetail = SYSTEM_ON_HOLD;
		} else if (USER_ON_HOLD.equals(state)) {
			this.state = SUBMITTED;
			stateDetail = USER_ON_HOLD;
		} else if (USER_SYSTEM_ON_HOLD.equals(state)) {
			this.state = SUBMITTED;
			stateDetail = USER_SYSTEM_ON_HOLD;
		} else if (SYSTEM_SUSPENDED.equals(state)) {
			this.state = SUSPENDED;
			stateDetail = SYSTEM_SUSPENDED;
		} else if (USER_SUSPENDED.equals(state)) {
			this.state = SUSPENDED;
			stateDetail = USER_SUSPENDED;
		} else if (USER_SYSTEM_SUSPENDED.equals(state)) {
			this.state = SUSPENDED;
			stateDetail = USER_SYSTEM_SUSPENDED;
		} else if (FAILED.equals(state)) {
			this.state = COMPLETED;
			stateDetail = FAILED;
		} else if (CANCELED.equals(state)) {
			this.state = COMPLETED;
			stateDetail = CANCELED;
		} else if (JOB_OUTERR_READY.equals(state)) {
			this.state = COMPLETED;
			stateDetail = JOB_OUTERR_READY;
		}
		if (previousDetail == null || !previousDetail.equals(stateDetail)) {
			dirty = true;
		}
	}

	/**
	 * @param time
	 *            in milliseconds of last update request issued to remote
	 *            resource
	 */
	public synchronized void setUpdateRequestTime(long update) {
		lastUpdateRequest = update;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.ICommandJobStatus#stateChanged()
	 */
	public boolean stateChanged() {
		boolean changed = dirty;
		dirty = false;
		return changed;
	}

	/*
	 * Wait until the jobId has been set on the job id property in the
	 * environment.
	 * 
	 * The uuid key for the property containing as its name the
	 * resource-specific jobId and as its value the state.
	 * 
	 * The waitUntil state will usually be either SUBMITTED or RUNNING (for
	 * interactive)
	 * 
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.jaxb.core.ICommandJobStatus#waitForJobId(java.lang
	 * .String, java.lang.String,
	 * org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerControl)
	 */
	public void waitForJobId(String uuid, String waitUntil) {
		synchronized (this) {
			while (waitEnabled && (jobId == null || !waitUntil.equals(state))) {
				try {
					wait(1000);
				} catch (InterruptedException ignored) {
				}
				RMVariableMap env = control.getEnvironment();
				if (env == null) {
					break;
				}
				PropertyType p = (PropertyType) env.get(uuid);
				if (p != null) {
					jobId = p.getName();
					String v = (String) p.getValue();
					if (v != null) {
						setState(v);
					}
				}
				if (jobId != null && stateChanged()) {
					control.jobStateChanged(jobId);
				}
			}
		}
	}

	/**
	 * Implicitly describes the legal state transitions.
	 * 
	 * @param newState
	 * @return transition is legal
	 */
	private boolean canUpdateState(String newState) {
		int prevRank = getStateRank(stateDetail);
		int currRank = getStateRank(newState);
		if (prevRank >= currRank) {
			if (prevRank == 0) {
				return true;
			}
			if (prevRank != 4 || currRank != 3) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Checks for file existence, then waits 3 seconds to compare file length.
	 * If block is false, the listeners may be notified that the file is still
	 * not ready; else the listeners will receive a ready = true notification
	 * when the file does finally stabilize. (non-Javadoc)
	 * 
	 * @param path
	 * @param blockInSeconds
	 * @return thread running the check
	 */
	private FileReadyChecker checkForReady(final String path, final int block) {
		FileReadyChecker t = new FileReadyChecker();
		t.block = block;
		t.path = path;
		t.start();
		return t;
	}

	/**
	 * Gives ordering of states.
	 * 
	 * @param state
	 * @return the ordering of the state
	 */
	private int getStateRank(String state) {
		if (SUBMITTED.equals(state)) {
			return 1;
		} else if (RUNNING.equals(state)) {
			return 4;
		} else if (SUSPENDED.equals(state)) {
			return 3;
		} else if (COMPLETED.equals(state)) {
			return 5;
		} else if (QUEUED_ACTIVE.equals(state)) {
			return 2;
		} else if (SYSTEM_ON_HOLD.equals(state)) {
			return 3;
		} else if (USER_ON_HOLD.equals(state)) {
			return 3;
		} else if (USER_SYSTEM_ON_HOLD.equals(state)) {
			return 3;
		} else if (SYSTEM_SUSPENDED.equals(state)) {
			return 3;
		} else if (USER_SUSPENDED.equals(state)) {
			return 3;
		} else if (USER_SYSTEM_SUSPENDED.equals(state)) {
			return 3;
		} else if (FAILED.equals(state)) {
			return 6;
		} else if (CANCELED.equals(state)) {
			return 6;
		} else if (JOB_OUTERR_READY.equals(state)) {
			return 6;
		}
		return 0;
	}
}
