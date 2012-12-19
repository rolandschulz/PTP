/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.control.internal.runnable.command;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.ptp.core.jobs.IJobStatus;
import org.eclipse.ptp.core.jobs.IPJobStatus;
import org.eclipse.ptp.core.util.CoreExceptionUtils;
import org.eclipse.ptp.remote.core.IRemoteProcess;
import org.eclipse.ptp.remote.core.RemoteServicesDelegate;
import org.eclipse.ptp.rm.jaxb.control.IJobController;
import org.eclipse.ptp.rm.jaxb.control.JAXBControlConstants;
import org.eclipse.ptp.rm.jaxb.control.JAXBControlCorePlugin;
import org.eclipse.ptp.rm.jaxb.control.JAXBUtils;
import org.eclipse.ptp.rm.jaxb.control.internal.ICommandJob;
import org.eclipse.ptp.rm.jaxb.control.internal.ICommandJobStatus;
import org.eclipse.ptp.rm.jaxb.control.internal.ICommandJobStatusMap;
import org.eclipse.ptp.rm.jaxb.control.internal.ICommandJobStreamMonitor;
import org.eclipse.ptp.rm.jaxb.control.internal.ICommandJobStreamsProxy;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.JAXBCoreConstants;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeType;

/**
 * Extension of the IJobStatus class to handle resource manager command jobs. Also handles availability notification for remote
 * stdout and stderr files.
 * 
 * @author arossi
 * 
 */
public class CommandJobStatus implements ICommandJobStatus {

	/**
	 * Checks for file existence, then waits 3 seconds to compare file length. If block is false, the listeners may be notified that
	 * the file is still not ready; else the listeners will receive a ready = true notification when the file does finally
	 * stabilize, provided this occurs within the block parameter (seconds).
	 * 
	 * @author arossi
	 */
	private class FileReadyChecker extends Job {
		private boolean ready;
		private int block;
		private String path;
		private IProgressMonitor callerMonitor;

		/**
		 * @param name
		 */
		public FileReadyChecker(String name) {
			super(name);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime. IProgressMonitor)
		 */
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			ready = false;
			long timeout = block * 1000;
			RemoteServicesDelegate d = null;
			SubMonitor progress = SubMonitor.convert(monitor, 120);
			try {
				d = JAXBUtils.getRemoteServicesDelegate(control.getRemoteServicesId(), control.getConnectionName(),
						progress.newChild(20));
				if (d.getRemoteFileManager() == null) {
					/*
					 * could be a call initiated by closing of resource manager, the connection may be closed; just ignore and move
					 * on
					 */
					return Status.OK_STATUS;
				}
			} catch (CoreException ce) {
				return CoreExceptionUtils.getErrorStatus(ce.getMessage(), ce);
			} finally {
				if (monitor != null) {
					monitor.done();
				}
			}
			long start = System.currentTimeMillis();
			long last = 0;
			long elapsed = 0;
			double increment = 0;
			while (true) {
				try {
					ready = RemoteServicesDelegate.isStable(d.getRemoteFileManager(), path, 3, progress.newChild(20));
				} catch (Throwable t) {
					JAXBControlCorePlugin.log(t);
				}

				if (ready) {
					break;
				}

				elapsed = System.currentTimeMillis() - start;
				if (elapsed >= timeout) {
					break;
				}
				increment = ((double) (elapsed - last) / timeout) * 100;
				last = elapsed;
				progress.worked((int) increment);

				if (progress.isCanceled() || this.callerMonitor.isCanceled()) {
					break;
				}
			}
			if (monitor != null) {
				monitor.done();
			}
			return Status.OK_STATUS;
		}
	}

	private class PProcesses {
		private final Map<String, BitSet> fProcState = new HashMap<String, BitSet>();
		private final int fNumProcs;

		public PProcesses(int nprocs) {
			fNumProcs = nprocs;
			fProcState.put(IPJobStatus.COMPLETED, new BitSet(nprocs));
			fProcState.put(IPJobStatus.RUNNING, new BitSet(nprocs));
			fProcState.put(IPJobStatus.SUSPENDED, new BitSet(nprocs));
		}

		public int getNumberOfProcesses() {
			return fNumProcs;
		}

		public String getProcessState(int proc) {
			for (String state : fProcState.keySet()) {
				if (fProcState.get(state).get(proc)) {
					return state;
				}
			}
			return IPJobStatus.UNDETERMINED;
		}

		public void setProcessOutput(BitSet procs, String output) {
			ICommandJobStreamMonitor monitor = (ICommandJobStreamMonitor) getStreamsProxy().getOutputStreamMonitor();
			monitor.append(output);
		}

		public void setProcessState(BitSet procs, String newState) {
			for (String state : fProcState.keySet()) {
				if (state.equals(newState)) {
					fProcState.get(state).or(procs);
				} else {
					fProcState.get(state).andNot(procs);
				}
			}
		}
	}

	private final IJobController control;
	private final IVariableMap varMap;
	private final ICommandJob open;

	private String jobId;
	private String owner;
	private String queue;
	private ILaunch launch;
	private String state;
	private String stateDetail;
	private String remoteOutputPath;
	private String remoteErrorPath;
	private ICommandJobStreamsProxy proxy;
	private IRemoteProcess process;
	private PProcesses fProcesses;
	private boolean initialized;
	private boolean waitEnabled;
	private boolean dirty;
	private boolean fFilesChecked;
	private long lastRequestedUpdate;

	/**
	 * @param rmUniqueName
	 *            owner resource manager
	 * @param control
	 *            resource manager control
	 */
	public CommandJobStatus(ICommandJob open, IJobController control) {
		this(null, UNDETERMINED, open, control);
	}

	/**
	 * @param rmUniqueName
	 *            owner resource manager
	 * @param jobId
	 * @param state
	 * @param control
	 *            resource manager control
	 */
	public CommandJobStatus(String jobId, String state, ICommandJob open, IJobController control) {
		this.jobId = jobId;
		setState(state);
		this.open = open;
		this.control = control;
		this.varMap = control.getEnvironment();
		waitEnabled = true;
		lastRequestedUpdate = 0;
		initialized = false;
		dirty = false;
		fFilesChecked = false;
	}

	/**
	 * Closes the proxy and calls destroy on the process. Used for interactive job cancellation.
	 * 
	 * @return true if canceled during this call.
	 */
	public boolean cancel() {
		synchronized (this) {
			if (getStateRank(stateDetail) > 4) {
				return false;
			}

			waitEnabled = false;

			/*
			 * If this process is persistent (open), call terminate on the job, as it may still be running; the process will be
			 * killed and the proxy closed inside the job
			 */
			if (open != null) {
				open.terminate();
				open.getJobStatus().setState(IJobStatus.CANCELED);
				return true;
			}

			notifyAll();

			if (process != null && !process.isCompleted()) {
				process.destroy();
				if (proxy != null) {
					proxy.close();
				}
				return true;
			}
			return false;
		}
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
	 * Checks for file existence, then waits 3 seconds to compare file length. If block is false, the listeners may be notified that
	 * the file is still not ready; else the listeners will receive a ready = true notification when the file does finally
	 * stabilize. (non-Javadoc)
	 * 
	 * @param path
	 * @param blockInSeconds
	 * @param monitor
	 * @return thread running the check
	 */
	private FileReadyChecker checkForReady(String path, int block, IProgressMonitor monitor) {
		FileReadyChecker t = new FileReadyChecker(path);
		t.block = block;
		t.path = path;
		t.callerMonitor = monitor;
		t.schedule();
		return t;
	}

	/**
	 * If interactive, check to see if the process has completed.
	 */
	private void checkProcessStateForTermination() {
		if (process != null) {
			if (process.isCompleted()) {
				setState(process.exitValue() == 0 ? COMPLETED : FAILED);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		if (adapter == IPJobStatus.class) {
			if (fProcesses != null) {
				return this;
			}
			if (jobId != null && control != null) {
				AttributeType nProcsAttr = control.getEnvironment().get(JAXBControlConstants.MPI_PROCESSES);
				if (nProcsAttr != null) {
					int nProcs = 0;
					try {
						nProcs = Integer.parseInt(nProcsAttr.getValue().toString());
					} catch (Exception e) {
					}
					if (nProcs > 0) {
						fProcesses = new PProcesses(nProcs);
						return this;
					}
				}
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.jobs.IJobStatus#getControlId()
	 */
	public String getControlId() {
		return control.getControlId();
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

	/**
	 * for throttling requests.
	 */
	public synchronized long getLastUpdateRequest() {
		return lastRequestedUpdate;
	}

	/**
	 * @return launch used for this submission.
	 */
	public ILaunch getLaunch() {
		return launch;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.jobs.IPJobStatus#getNumberOfProcesses()
	 */
	public int getNumberOfProcesses() {
		return fProcesses != null ? fProcesses.getNumberOfProcesses() : 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IJobStatus#getOutputPath()
	 */
	public String getOutputPath() {
		return remoteOutputPath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IJobStatus#getOwner()
	 */
	public String getOwner() {
		return owner;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.jobs.IPJobStatus#getProcessState(int)
	 */
	public String getProcessState(int proc) {
		return fProcesses != null ? fProcesses.getProcessState(proc) : ""; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IJobStatus#getQueueName()
	 */
	public String getQueueName() {
		return queue;
	}

	/**
	 * @return state of the job (not of the submission process).
	 */
	public synchronized String getState() {
		if (stateDetail != IJobStatus.CANCELED) {
			checkProcessStateForTermination();
		}
		return state;
	}

	/**
	 * @return more specific state identifier.
	 */
	public synchronized String getStateDetail() {
		if (stateDetail != IJobStatus.CANCELED) {
			checkProcessStateForTermination();
		}
		return stateDetail;
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
			return 7;
		}
		return 0;
	}

	/**
	 * Wrapper containing monitoring functionality for the associated output and error streams.
	 */
	public IStreamsProxy getStreamsProxy() {
		return proxy;
	}

	/*
	 * NOTE: Initialize must be called immediately after the return of the submit.run() method while the property for the jobId is
	 * pinned and in the environment. Note also that batch variable replacement will not work, as that would not be interpretable
	 * for the RM. One actually needs to configure two separate strings in this case, giving one to the script and one to the
	 * resource manager.
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.ICommandJobStatus#initialize(java.lang.String )
	 */
	public void initialize(String jobId) {
		if (initialized) {
			return;
		}
		this.jobId = jobId;
		String path = null;
		remoteOutputPath = null;
		remoteErrorPath = null;
		AttributeType a = varMap.get(JAXBControlConstants.STDOUT_REMOTE_FILE);
		if (a != null) {
			path = (String) a.getValue();
			if (path != null && !JAXBControlConstants.ZEROSTR.equals(path)) {
				remoteOutputPath = varMap.getString(jobId, path);
			}
		}
		a = varMap.get(JAXBControlConstants.STDERR_REMOTE_FILE);
		if (a != null) {
			path = (String) a.getValue();
			if (path != null && !JAXBControlConstants.ZEROSTR.equals(path)) {
				remoteErrorPath = varMap.getString(jobId, path);
			}
		}
		initialized = true;
	}

	/**
	 * @return whether a process object has been attached to this status object (in which case the submission is not through an
	 *         asynchronous job scheduler).
	 */
	public boolean isInteractive() {
		return process != null;
	}

	/**
	 * @param state
	 *            current
	 * @param waitUntil
	 *            state to reach
	 * @return true if the current state has reached the indicated state
	 */
	private boolean isReached(String state, String waitUntil) {
		int i = getStateRank(state);
		int j = getStateRank(waitUntil);
		return i >= j;
	}

	/**
	 * @return under synchronization
	 */
	private boolean isWaitEnabled() {
		boolean w = true;
		synchronized (this) {
			w = waitEnabled;
		}
		return w;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.ICommandJobStatus#maybeWaitForHandlerFiles (int)
	 */
	public void maybeWaitForHandlerFiles(int blockForSecs, IProgressMonitor monitor) {
		if (fFilesChecked) {
			return;
		}

		FileReadyChecker tout = null;
		FileReadyChecker terr = null;

		SubMonitor progress = SubMonitor.convert(monitor, 10);

		if (remoteOutputPath != null) {
			tout = checkForReady(remoteOutputPath, blockForSecs, progress.newChild(5));
		}

		if (remoteErrorPath != null) {
			terr = checkForReady(remoteErrorPath, blockForSecs, progress.newChild(5));
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
	 * @param launch
	 *            launch used for this submission.
	 */
	public void setLaunch(ILaunch launch) {
		this.launch = launch;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.internal.ICommandJobStatus#setOwner(java .lang.String)
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}

	/**
	 * @param process
	 *            object (used for interactive cancellation)
	 */
	public void setProcess(IRemoteProcess process) {
		this.process = process;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.jobs.IPJobStatus#setProcessOutput(java.util.BitSet, java.lang.String)
	 */
	public void setProcessOutput(BitSet procs, String output) {
		if (fProcesses != null) {
			fProcesses.setProcessOutput(procs, output);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.jobs.IPJobStatus#setProcessState(java.util.BitSet, java.lang.String)
	 */
	public void setProcessState(BitSet procs, String state) {
		if (fProcesses != null) {
			fProcesses.setProcessState(procs, state);
		}
	}

	/**
	 * @param proxy
	 *            Wrapper containing monitoring functionality for the associated output and error streams.
	 */
	public void setProxy(ICommandJobStreamsProxy proxy) {
		this.proxy = proxy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.internal.ICommandJobStatus#setQueueName (java.lang.String)
	 */
	public void setQueueName(String name) {
		this.queue = name;
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
	 *            in milliseconds of last update request issued to remote resource
	 */
	public synchronized void setUpdateRequestTime(long update) {
		lastRequestedUpdate = update;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.ICommandJobStatus#stateChanged()
	 */
	public synchronized boolean stateChanged() {
		boolean changed = dirty && !UNDETERMINED.equals(state);
		dirty = false;
		return changed;
	}

	/**
	 * Gives the string field values in list form.
	 */
	@Override
	public String toString() {
		List<String> s = new ArrayList<String>();
		s.add(jobId);
		s.add(owner);
		s.add(queue);
		s.add(state);
		s.add(stateDetail);
		s.add(remoteOutputPath);
		s.add(remoteErrorPath);
		return s.toString();
	}

	/*
	 * Wait until the jobId has been set on the job id property in the environment.
	 * 
	 * The uuid key for the property containing as its name the resource-specific jobId and as its value the state.
	 * 
	 * The waitUntil state will usually be either SUBMITTED or RUNNING (for interactive)
	 * 
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.ICommandJobStatus#waitForJobId(java.lang .String, java.lang.String,
	 * org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerControl)
	 */
	public void waitForJobId(String uuid, String waitUntil, ICommandJobStatusMap map, IProgressMonitor monitor)
			throws CoreException {
		IVariableMap env = control.getEnvironment();
		if (env == null) {
			return;
		}

		while (!monitor.isCanceled() && isWaitEnabled() && (jobId == null || !isReached(state, waitUntil))) {
			synchronized (this) {
				try {
					wait(1000);
				} catch (InterruptedException ignored) {
				}
			}

			if (isInteractive()) {
				int exit = 0;
				try {
					exit = process.exitValue();
				} catch (Throwable t) {
				}
				if (exit != 0 && !monitor.isCanceled() && isWaitEnabled()) {
					throw CoreExceptionUtils.newException(uuid + JAXBCoreConstants.CO + JAXBCoreConstants.SP + FAILED, null);
				}
			}

			AttributeType a = env.get(uuid);
			if (a == null) {
				continue;
			}

			jobId = a.getName();
			String v = (String) a.getValue();
			if (v != null) {
				setState(v);
			}

			if (jobId == null) {
				if (stateDetail == FAILED) {
					throw CoreExceptionUtils.newException(uuid + JAXBCoreConstants.CO + JAXBCoreConstants.SP + FAILED, null);
				} else {
					continue;
				}
			}

			if (!stateChanged()) {
				continue;
			}

			/*
			 * guarantee the presence of intermediate state in the environment
			 */
			env.put(jobId, a);
			if (!map.addJobStatus(jobId, this)) {
				control.jobStateChanged(jobId, this);
			}
		}
	}
}
