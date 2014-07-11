/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.internal.rm.jaxb.control.core.runnable.command;

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
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.ptp.core.jobs.IJobStatus;
import org.eclipse.ptp.core.jobs.IPJobStatus;
import org.eclipse.ptp.core.jobs.JobManager;
import org.eclipse.ptp.core.util.CoreExceptionUtils;
import org.eclipse.ptp.internal.rm.jaxb.control.core.ICommandJob;
import org.eclipse.ptp.internal.rm.jaxb.control.core.ICommandJobStatus;
import org.eclipse.ptp.internal.rm.jaxb.control.core.ICommandJobStatusMap;
import org.eclipse.ptp.internal.rm.jaxb.control.core.ICommandJobStreamMonitor;
import org.eclipse.ptp.internal.rm.jaxb.control.core.ICommandJobStreamsProxy;
import org.eclipse.ptp.internal.rm.jaxb.control.core.JAXBControlConstants;
import org.eclipse.ptp.internal.rm.jaxb.control.core.JAXBControlCorePlugin;
import org.eclipse.ptp.internal.rm.jaxb.control.core.RemoteServicesDelegate;
import org.eclipse.ptp.internal.rm.jaxb.control.core.messages.Messages;
import org.eclipse.ptp.internal.rm.jaxb.control.core.runnable.JobStatusMap;
import org.eclipse.ptp.internal.rm.jaxb.core.JAXBCoreConstants;
import org.eclipse.ptp.rm.jaxb.control.core.ILaunchController;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeType;
import org.eclipse.remote.core.IRemoteProcess;

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
				d = RemoteServicesDelegate.getDelegate(fControl.getRemoteServicesId(), fControl.getConnectionName(),
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

	private final ILaunchController fControl;
	private final ICommandJob fOpen;
	private final String fLaunchMode;
	private final IVariableMap fVarMap;

	private String fJobId;
	private String fOwner;
	private String fQueue;
	private String fState;
	private String fStateDetail;
	private String fRemoteOutputPath;
	private String fRemoteErrorPath;
	private ICommandJobStreamsProxy fProxy;
	private IRemoteProcess fProcess;
	private PProcesses fPProcesses;
	private boolean fInitialized;
	private boolean fWaitEnabled;
	private boolean fDirty;
	private boolean fFilesChecked;
	private long fLastRequestedUpdate;

	/**
	 * @param rmUniqueName
	 *            owner resource manager
	 * @param control
	 *            resource manager control
	 */
	public CommandJobStatus(ICommandJob open, ILaunchController control, IVariableMap map, String mode) {
		this(null, UNDETERMINED, open, control, map, mode);
	}

	/**
	 * @param rmUniqueName
	 *            owner resource manager
	 * @param jobId
	 * @param state
	 * @param control
	 *            resource manager control
	 */
	public CommandJobStatus(String jobId, String state, ICommandJob open, ILaunchController control, IVariableMap map, String mode) {
		this.fJobId = jobId;
		normalizeState(state);
		this.fOpen = open;
		this.fControl = control;
		this.fVarMap = map;
		this.fLaunchMode = mode;
		fWaitEnabled = true;
		fLastRequestedUpdate = 0;
		fInitialized = false;
		fDirty = false;
		fFilesChecked = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rm.jaxb.control.core.ICommandJobStatus#cancel()
	 */
	@Override
	public boolean cancel() {
		synchronized (this) {
			if (getStateRank(fStateDetail) > 4) {
				return false;
			}

			fWaitEnabled = false;

			/*
			 * If this process is persistent (open), call terminate on the job, as it may still be running; the process will be
			 * killed and the proxy closed inside the job
			 */
			if (fOpen != null) {
				fOpen.terminate();
				fOpen.getJobStatus().setState(IJobStatus.CANCELED);
				return true;
			}

			notifyAll();

			if (fProcess != null && !fProcess.isCompleted()) {
				fProcess.destroy();
				if (fProxy != null) {
					fProxy.close();
				}
				return true;
			}
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rm.jaxb.control.core.ICommandJobStatus#cancelWait()
	 */
	@Override
	public void cancelWait() {
		synchronized (this) {
			fWaitEnabled = false;
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
		int prevRank = getStateRank(fStateDetail);
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
		FileReadyChecker t = new FileReadyChecker(Messages.CommandJobStatus_Checking_output_file + path);
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
		if (fProcess != null) {
			if (fProcess.isCompleted()) {
				setState(fProcess.exitValue() == 0 ? COMPLETED : FAILED);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		if (adapter == IPJobStatus.class) {
			if (fPProcesses != null) {
				return this;
			}
			if (fJobId != null && fControl != null) {
				AttributeType nProcsAttr = fVarMap.get(JAXBControlConstants.MPI_PROCESSES);
				/*
				 * Bug 412887 - make sure to check for linked attribute
				 */
				if (nProcsAttr != null) {
					String link = nProcsAttr.getLinkValueTo();
					if (link != null) {
						nProcsAttr = fVarMap.get(link);
					}
				}
				if (nProcsAttr != null) {
					int nProcs = 0;
					try {
						nProcs = Integer.parseInt(nProcsAttr.getValue().toString());
					} catch (Exception e) {
						// Ignore
					}
					if (nProcs > 0) {
						fPProcesses = new PProcesses(nProcs);
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
	@Override
	public String getControlId() {
		return fControl.getControlId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IJobStatus#getErrorPath()
	 */
	@Override
	public String getErrorPath() {
		return fRemoteErrorPath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.jobs.IJobStatus#getJobId()
	 */
	@Override
	public synchronized String getJobId() {
		return fJobId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rm.jaxb.control.core.ICommandJobStatus#getLastUpdateRequest()
	 */
	@Override
	public synchronized long getLastUpdateRequest() {
		return fLastRequestedUpdate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.jobs.IJobStatus#getLaunchMode()
	 */
	@Override
	public String getLaunchMode() {
		return fLaunchMode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.jobs.IPJobStatus#getNumberOfProcesses()
	 */
	@Override
	public int getNumberOfProcesses() {
		return fPProcesses != null ? fPProcesses.getNumberOfProcesses() : 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IJobStatus#getOutputPath()
	 */
	@Override
	public String getOutputPath() {
		return fRemoteOutputPath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IJobStatus#getOwner()
	 */
	@Override
	public String getOwner() {
		return fOwner;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.jobs.IPJobStatus#getProcessState(int)
	 */
	@Override
	public String getProcessState(int proc) {
		return fPProcesses != null ? fPProcesses.getProcessState(proc) : ""; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IJobStatus#getQueueName()
	 */
	@Override
	public String getQueueName() {
		return fQueue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.jobs.IJobStatus#getState()
	 */
	@Override
	public synchronized String getState() {
		if (fStateDetail != IJobStatus.CANCELED) {
			checkProcessStateForTermination();
		}
		return fState;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.jobs.IJobStatus#getStateDetail()
	 */
	@Override
	public synchronized String getStateDetail() {
		if (fStateDetail != IJobStatus.CANCELED) {
			checkProcessStateForTermination();
		}
		return fStateDetail;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.jobs.IJobStatus#getStreamsProxy()
	 */
	@Override
	public IStreamsProxy getStreamsProxy() {
		return fProxy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rm.jaxb.control.core.ICommandJobStatus#initialize(java.lang.String)
	 */
	@Override
	public void initialize(String jobId) {
		if (fInitialized) {
			return;
		}
		this.fJobId = jobId;
		String path = null;
		fRemoteOutputPath = null;
		fRemoteErrorPath = null;
		AttributeType a = fVarMap.get(JAXBControlConstants.STDOUT_REMOTE_FILE);
		if (a != null) {
			path = (String) a.getValue();
			if (path != null && !JAXBControlConstants.ZEROSTR.equals(path)) {
				fRemoteOutputPath = fVarMap.getString(jobId, path);
			}
		}
		a = fVarMap.get(JAXBControlConstants.STDERR_REMOTE_FILE);
		if (a != null) {
			path = (String) a.getValue();
			if (path != null && !JAXBControlConstants.ZEROSTR.equals(path)) {
				fRemoteErrorPath = fVarMap.getString(jobId, path);
			}
		}
		fInitialized = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.jobs.IJobStatus#isInteractive()
	 */
	@Override
	public boolean isInteractive() {
		return fProcess != null;
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
			w = fWaitEnabled;
		}
		return w;
	}

	@Override
	public void rerun() {
		if (getState().equals(IJobStatus.RUNNING) && isInteractive()) {
			fOpen.rerun();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.ICommandJobStatus#maybeWaitForHandlerFiles (int)
	 */
	@Override
	public void maybeWaitForHandlerFiles(int blockForSecs, IProgressMonitor monitor) {
		if (fFilesChecked) {
			return;
		}

		FileReadyChecker tout = null;
		FileReadyChecker terr = null;

		SubMonitor progress = SubMonitor.convert(monitor, 10);

		if (fRemoteOutputPath != null) {
			tout = checkForReady(fRemoteOutputPath, blockForSecs, progress.newChild(5));
		}

		if (fRemoteErrorPath != null) {
			terr = checkForReady(fRemoteErrorPath, blockForSecs, progress.newChild(5));
		}

		if (tout == null && terr == null) {
			fFilesChecked = true;
			return;
		}

		if (tout != null) {
			try {
				tout.join();
			} catch (InterruptedException ignored) {
				// Ignore
			}
		}

		if (terr != null) {
			try {
				terr.join();
			} catch (InterruptedException ignored) {
				// Ignore
			}
		}

		/*
		 * [414001] Set status if job has either an output or error file. This is a change from the previous check which set the
		 * status only if *both* files exist. I believe this was because Torque/PBS always generate both files, however other job
		 * schedulers don't necessarily do this.
		 */
		if ((tout != null && tout.ready) || (terr != null && terr.ready)) {
			setState(IJobStatus.JOB_OUTERR_READY);
		}

		fFilesChecked = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rm.jaxb.control.core.ICommandJobStatus#setOwner(java .lang.String)
	 */
	@Override
	public void setOwner(String owner) {
		this.fOwner = owner;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rm.jaxb.control.core.ICommandJobStatus#setProcess(org.eclipse.remote.core.IRemoteProcess)
	 */
	@Override
	public void setProcess(IRemoteProcess process) {
		this.fProcess = process;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.jobs.IPJobStatus#setProcessOutput(java.util.BitSet, java.lang.String)
	 */
	@Override
	public void setProcessOutput(BitSet procs, String output) {
		if (fPProcesses != null) {
			fPProcesses.setProcessOutput(procs, output);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.jobs.IPJobStatus#setProcessState(java.util.BitSet, java.lang.String)
	 */
	@Override
	public void setProcessState(BitSet procs, String state) {
		if (fPProcesses != null) {
			fPProcesses.setProcessState(procs, state);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rm.jaxb.control.core.ICommandJobStatus#setProxy(org.eclipse.ptp.internal.rm.jaxb.control.core.
	 * ICommandJobStreamsProxy)
	 */
	@Override
	public void setProxy(ICommandJobStreamsProxy proxy) {
		this.fProxy = proxy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rm.jaxb.control.core.ICommandJobStatus#setQueueName (java.lang.String)
	 */
	@Override
	public void setQueueName(String name) {
		this.fQueue = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rm.jaxb.control.core.ICommandJobStatus#setState(java.lang.String)
	 */
	@Override
	public synchronized void setState(String state) {
		if (!canUpdateState(state)) {
			return;
		}

		fDirty = false;
		String previousDetail = fStateDetail;

		normalizeState(state);

		if (previousDetail == null || !previousDetail.equals(fStateDetail)) {
			fDirty = true;
			JobManager.getInstance().fireJobChanged(this);
		}
	}

	private void normalizeState(String newState) {
		if (UNDETERMINED.equals(newState)) {
			fState = UNDETERMINED;
			fStateDetail = UNDETERMINED;
		} else if (SUBMITTED.equals(newState)) {
			fState = SUBMITTED;
			fStateDetail = SUBMITTED;
		} else if (RUNNING.equals(newState)) {
			fState = RUNNING;
			fStateDetail = RUNNING;
		} else if (SUSPENDED.equals(newState)) {
			fState = SUSPENDED;
			fStateDetail = SUSPENDED;
		} else if (COMPLETED.equals(newState)) {
			fState = COMPLETED;
			fStateDetail = COMPLETED;
		} else if (QUEUED_ACTIVE.equals(newState)) {
			fState = SUBMITTED;
			fStateDetail = QUEUED_ACTIVE;
		} else if (SYSTEM_ON_HOLD.equals(newState)) {
			fState = SUBMITTED;
			fStateDetail = SYSTEM_ON_HOLD;
		} else if (USER_ON_HOLD.equals(newState)) {
			fState = SUBMITTED;
			fStateDetail = USER_ON_HOLD;
		} else if (USER_SYSTEM_ON_HOLD.equals(newState)) {
			fState = SUBMITTED;
			fStateDetail = USER_SYSTEM_ON_HOLD;
		} else if (SYSTEM_SUSPENDED.equals(newState)) {
			fState = SUSPENDED;
			fStateDetail = SYSTEM_SUSPENDED;
		} else if (USER_SUSPENDED.equals(newState)) {
			fState = SUSPENDED;
			fStateDetail = USER_SUSPENDED;
		} else if (USER_SYSTEM_SUSPENDED.equals(newState)) {
			fState = SUSPENDED;
			fStateDetail = USER_SYSTEM_SUSPENDED;
		} else if (FAILED.equals(newState)) {
			fState = COMPLETED;
			fStateDetail = FAILED;
		} else if (CANCELED.equals(newState)) {
			fState = COMPLETED;
			fStateDetail = CANCELED;
		} else if (JOB_OUTERR_READY.equals(newState)) {
			fState = COMPLETED;
			fStateDetail = JOB_OUTERR_READY;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rm.jaxb.control.core.ICommandJobStatus#setUpdateRequestTime(long)
	 */
	@Override
	public synchronized void setUpdateRequestTime(long update) {
		fLastRequestedUpdate = update;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.ICommandJobStatus#stateChanged()
	 */
	@Override
	public synchronized boolean stateChanged() {
		boolean changed = fDirty && !UNDETERMINED.equals(fState);
		fDirty = false;
		return changed;
	}

	/**
	 * Gives the string field values in list form.
	 */
	@Override
	public String toString() {
		List<String> s = new ArrayList<String>();
		s.add(fJobId);
		s.add(fOwner);
		s.add(fQueue);
		s.add(fState);
		s.add(fStateDetail);
		s.add(fRemoteOutputPath);
		s.add(fRemoteErrorPath);
		return s.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rm.jaxb.control.core.ICommandJobStatus#waitForJobId(java.lang.String, java.lang.String,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void waitForJobId(String uuid, String waitUntil, IProgressMonitor monitor) throws CoreException {
		SubMonitor progress = SubMonitor.convert(monitor);
		while (!progress.isCanceled() && isWaitEnabled() && (fJobId == null || !isReached(fState, waitUntil))) {
			synchronized (this) {
				try {
					wait(1000);
				} catch (InterruptedException ignored) {
					// Ignore
				}
			}

			progress.setWorkRemaining(10);

			if (isInteractive() && isWaitEnabled() && fProcess.isCompleted() && fProcess.exitValue() != 0) {
				throw CoreExceptionUtils.newException(uuid + JAXBCoreConstants.CO + JAXBCoreConstants.SP + FAILED, null);
			}

			AttributeType a = fVarMap.get(uuid);
			if (a != null) {
				fJobId = a.getName();
				String v = (String) a.getValue();
				if (v != null) {
					setState(v);
				}

				if (fJobId != null) {
					if (stateChanged()) {
						/*
						 * guarantee the presence of intermediate state in the environment
						 */
						fVarMap.put(fJobId, a);

						/*
						 * Update status
						 */
						ICommandJobStatusMap map = JobStatusMap.getInstance(fControl);
						if (map != null) {
							map.addJobStatus(fJobId, this);
						}
					}
				}

				if (fStateDetail == FAILED) {
					throw CoreExceptionUtils.newException(uuid + JAXBCoreConstants.CO + JAXBCoreConstants.SP + FAILED, null);
				}
			}
		}
	}
}
