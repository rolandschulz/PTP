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
package org.eclipse.ptp.internal.debug.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.jobs.IJobStatus;
import org.eclipse.ptp.core.jobs.IPJobStatus;
import org.eclipse.ptp.core.jobs.JobManager;
import org.eclipse.ptp.debug.core.IPBreakpointManager;
import org.eclipse.ptp.debug.core.IPLocationSetManager;
import org.eclipse.ptp.debug.core.IPMemoryManager;
import org.eclipse.ptp.debug.core.IPRegisterManager;
import org.eclipse.ptp.debug.core.IPSession;
import org.eclipse.ptp.debug.core.IPSetManager;
import org.eclipse.ptp.debug.core.IPSignalManager;
import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.event.IPDebugEvent;
import org.eclipse.ptp.debug.core.event.IPDebugInfo;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.debug.core.pdi.IPDILocator;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.IPDISessionObject;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.event.IPDIBreakpointInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDIChangedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIConnectedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDICreatedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIDestroyedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIDisconnectedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEndSteppingRangeInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDIErrorEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIErrorInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEventListener;
import org.eclipse.ptp.debug.core.pdi.event.IPDIExitInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDIFunctionFinishedInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDILocationReachedInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDIMemoryBlockInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDIOutputEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIRegisterInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDIRestartedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIResumedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDISharedLibraryInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDISignalInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDIStartedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDISuspendedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIThreadInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDIVariableInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDIWatchpointScopeInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDIWatchpointTriggerInfo;
import org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager;
import org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDILocationBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDITarget;
import org.eclipse.ptp.internal.debug.core.event.PDebugErrorInfo;
import org.eclipse.ptp.internal.debug.core.event.PDebugEvent;
import org.eclipse.ptp.internal.debug.core.event.PDebugExitInfo;
import org.eclipse.ptp.internal.debug.core.event.PDebugInfo;
import org.eclipse.ptp.internal.debug.core.event.PDebugSuspendInfo;
import org.eclipse.ptp.internal.debug.core.messages.Messages;
import org.eclipse.ptp.internal.debug.core.model.PDebugTarget;

public class PSession implements IPSession, IPDIEventListener {
	private final IPDISession pdiSession;
	private final IPLaunch launch;
	private final IProject project;
	private final PSignalManager signalMgr;
	private final PBreakpointManager bptMgr;
	private final PMemoryManager memMgr;
	private final PRegisterManager regMgr;
	private final PSetManager setMgr;
	private final PLocationSetManager locMgr;

	public PSession(IPDISession pdiSession, IPLaunch launch, IProject project) {
		this.pdiSession = pdiSession;
		this.launch = launch;
		this.project = project;
		signalMgr = new PSignalManager(this);
		bptMgr = new PBreakpointManager(this);
		memMgr = new PMemoryManager(this);
		regMgr = new PRegisterManager(this);
		setMgr = new PSetManager(this);
		locMgr = new PLocationSetManager(this);
		getPDISession().getEventManager().addEventListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.IPSession#connectToDebugger(org.eclipse.core .runtime.IProgressMonitor, java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String[])
	 */
	public void connectToDebugger(IProgressMonitor monitor, String app, String path, String cwd, String[] args)
			throws CoreException {
		SubMonitor progress = SubMonitor.convert(monitor, 1);
		boolean failed = false;
		try {
			getPDISession().connectToDebugger(progress.newChild(1), app, path, cwd, args);
		} catch (PDIException e) {
			failed = true;
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), IStatus.ERROR,
					e.getMessage(), null));
		} finally {
			if (failed || progress.isCanceled()) {
				dispose();
			}
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.IPSession#createDebugTarget(org.eclipse.ptp .core.util.TaskSet, boolean, boolean)
	 */
	public void createDebugTarget(TaskSet tasks, boolean refresh, boolean register) {
		if (isReady()) {
			List<IPDITarget> targets = new ArrayList<IPDITarget>();
			int[] task_array = tasks.toArray();
			for (int task_id : task_array) {
				IPDITarget target = getPDISession().getTargetManager().addTarget(getTasks(task_id));
				if (target != null) {
					targets.add(target);
				} else {
					tasks.clear(task_id);
				}
			}
			if (register) {
				getPDISession().getTaskManager().setRegisterTasks(true, tasks);
			}
			PTPDebugCorePlugin.getDebugModel().addNewDebugTargets(getLaunch(), tasks, targets.toArray(new IPDITarget[0]), refresh,
					false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.IPSession#deleteDebugTarget(org.eclipse.ptp .core.util.TaskSet, boolean, boolean)
	 */
	public void deleteDebugTarget(TaskSet tasks, boolean refresh, boolean register) {
		int[] task_array = tasks.toArray();
		for (int task_id : task_array) {
			if (!getPDISession().getTargetManager().removeTarget(getTasks(task_id))) {
				tasks.clear(task_id);
			}
		}
		if (register) {
			getPDISession().getTaskManager().setRegisterTasks(false, tasks);
		}
		PTPDebugCorePlugin.getDebugModel().removeDebugTarget(getLaunch(), tasks, refresh);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.IPSession#deleteDebugTargets(boolean)
	 */
	public void deleteDebugTargets(boolean register) {
		if (isReady()) {
			TaskSet tasks = getPDISession().getTaskManager().getRegisteredTasks().copy();
			deleteDebugTarget(tasks, true, register);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.IPSession#dispose()
	 */
	public void dispose() {
		dispose(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.IPSession#findDebugTarget(org.eclipse.ptp. core.util.TaskSet)
	 */
	public PDebugTarget findDebugTarget(TaskSet tasks) {
		return (PDebugTarget) launch.getDebugTarget(tasks);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.IPSession#fireDebugEvent(int, int, org.eclipse.ptp.debug.core.event.IPDebugInfo)
	 */
	public void fireDebugEvent(int type, int details, IPDebugInfo info) {
		PTPDebugCorePlugin.getDefault().fireDebugEvent(new PDebugEvent(this, type, details, info));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.IPSession#forceStoppedDebugger(boolean)
	 */
	public void forceStoppedDebugger(boolean isError) {
		TaskSet tasks = getTasks();
		changeProcessState(tasks, IPJobStatus.COMPLETED);
		PTPDebugCorePlugin.getDefault().fireDebugEvent(
				new PDebugEvent(getSession(), IPDebugEvent.TERMINATE, IPDebugEvent.DEBUGGER, getDebugInfo(tasks)));
		dispose(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IPDISession.class)) {
			return this;
		}
		if (adapter.equals(PSignalManager.class)) {
			return getSignalManager();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.IPSession#getBreakpointManager()
	 */
	public IPBreakpointManager getBreakpointManager() {
		return bptMgr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.IPSession#getDebugInfo(org.eclipse.ptp.core .util.TaskSet)
	 */
	public IPDebugInfo getDebugInfo(TaskSet eTasks) {
		IPDITaskManager taskMgr = getPDISession().getTaskManager();
		return new PDebugInfo(getLaunch(), eTasks, taskMgr.getRegisteredTasks(eTasks.copy()), taskMgr.getUnregisteredTasks(eTasks
				.copy()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.IPSession#getLaunch()
	 */
	public IPLaunch getLaunch() {
		return launch;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.IPSession#getLocationSetManager()
	 */
	public IPLocationSetManager getLocationSetManager() {
		return locMgr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.IPSession#getMemoryManager()
	 */
	public IPMemoryManager getMemoryManager() {
		return memMgr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.IPSession#getPDISession()
	 */
	public IPDISession getPDISession() {
		return pdiSession;
	}

	/**
	 * @return
	 */
	public IProject getProject() {
		return project;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.IPSession#getRegisterManager()
	 */
	public IPRegisterManager getRegisterManager() {
		return regMgr;
	}

	/**
	 * @return
	 */
	public IPSession getSession() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.IPSession#getSetManager()
	 */
	public IPSetManager getSetManager() {
		return setMgr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.IPSession#getSignalManager()
	 */
	public IPSignalManager getSignalManager() {
		return signalMgr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.IPSession#getTasks()
	 */
	public TaskSet getTasks() {
		return getPDISession().getTasks();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.IPSession#getTasks(int)
	 */
	public TaskSet getTasks(int id) {
		int max = getPDISession().getTotalTasks();
		TaskSet tasks = new TaskSet(max);
		if (id >= 0 && id <= max) {
			tasks.set(id);
		}
		return tasks;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.event.IPDIEventListener#handleDebugEvents
	 * (org.eclipse.ptp.debug.core.pdi.event.IPDIEvent[])
	 */
	public void handleDebugEvents(IPDIEvent[] events) {
		for (IPDIEvent event : events) {
			if (event instanceof IPDIConnectedEvent) {
				PTPDebugCorePlugin.getDefault().fireDebugEvent(
						new PDebugEvent(getSession(), IPDebugEvent.CREATE, IPDebugEvent.DEBUGGER, getDebugInfo(event.getTasks())));
			} else if (event instanceof IPDIStartedEvent) {
				// only call once
				bptMgr.setInitialBreakpoints();
				boolean stopInMain = true;
				try {
					stopInMain = getLaunch().getLaunchConfiguration().getAttribute(
							IPTPLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, false);
				} catch (CoreException e) {
					// do nothing
				}
				try {
					if (stopInMain) {
						bptMgr.setStopInMain(getPDISession().getTasks());
					}
					getPDISession().start(getPDISession().getTasks());
					getPDISession().setStatus(IPDISession.STARTED);
					PTPDebugCorePlugin.getDefault().fireDebugEvent(
							new PDebugEvent(getSession(), IPDebugEvent.CHANGE, IPDebugEvent.PROCESS_SPECIFIC, getDebugInfo(event
									.getTasks())));
				} catch (PDIException e) {
					IPDebugInfo errInfo = new PDebugErrorInfo(getDebugInfo(event.getTasks()), Messages.PSession_1, e.getMessage(),
							PTPDebugCorePlugin.INTERNAL_ERROR);
					PTPDebugCorePlugin.getDefault().fireDebugEvent(
							new PDebugEvent(this, IPDebugEvent.ERROR, IPDebugEvent.UNSPECIFIED, errInfo));
				}
			} else if (event instanceof IPDIDisconnectedEvent) {
				PTPDebugCorePlugin.getDefault()
						.fireDebugEvent(
								new PDebugEvent(getSession(), IPDebugEvent.TERMINATE, IPDebugEvent.DEBUGGER, getDebugInfo(event
										.getTasks())));
				dispose();
			} else if (event instanceof IPDIChangedEvent) {
				fireChangeEvent((IPDIChangedEvent) event);
			} else if (event instanceof IPDICreatedEvent) {
				fireCreateEvent((IPDICreatedEvent) event);
			} else if (event instanceof IPDIDestroyedEvent) {
				fireDestroyEvent((IPDIDestroyedEvent) event);
			} else if (event instanceof IPDIErrorEvent) {
				fireErrorEvent((IPDIErrorEvent) event);
			} else if (event instanceof IPDIRestartedEvent) {
				// TODO
			} else if (event instanceof IPDIResumedEvent) {
				fireResumeEvent((IPDIResumedEvent) event);
			} else if (event instanceof IPDISuspendedEvent) {
				try {
					bptMgr.updatePendingBreakpoints();
				} catch (PDIException e) {
				}
				fireSuspendEvent((IPDISuspendedEvent) event);
			} else if (event instanceof IPDIOutputEvent) {
				setProcessOutput(event.getTasks(), ((IPDIOutputEvent) event).getOutput());
			} else {
				IPDebugEvent debugEvent = new PDebugEvent(this, IPDebugEvent.UNSPECIFIED, IPDebugEvent.UNSPECIFIED,
						getDebugInfo(event.getTasks()));
				PTPDebugCorePlugin.getDefault().fireDebugEvent(debugEvent);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.IPSession#isReady()
	 */
	public boolean isReady() {
		return (getPDISession().getStatus() == IPDISession.STARTED);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.IPSession#reloadDebugTargets(org.eclipse.ptp .core.util.TaskSet, boolean, boolean)
	 */
	public void reloadDebugTargets(TaskSet tasks, boolean refresh, boolean register) {
		if (isReady()) {
			TaskSet curRegTasks = getPDISession().getTaskManager().getRegisteredTasks(tasks.copy());
			TaskSet othRegTasks = getPDISession().getTaskManager().getRegisteredTasks().copy();
			othRegTasks.andNot(curRegTasks);
			deleteDebugTarget(othRegTasks, refresh, register);
			createDebugTarget(curRegTasks, refresh, register);
		}
	}

	/**
	 * Set the state of all processes in the BitList
	 * 
	 * @param tasks
	 * @param state
	 */
	private void changeProcessState(TaskSet tasks, String state) {
		IJobStatus job = JobManager.getInstance().getJob(getLaunch().getJobControl().getControlId(), getLaunch().getJobId());
		if (job != null) {
			IPJobStatus pJob = (IPJobStatus) job.getAdapter(IPJobStatus.class);
			if (pJob != null) {
				pJob.setProcessState(tasks, state);
			}
		}
	}

	/**
	 * @param force
	 */
	private void dispose(final boolean force) {
		WorkspaceJob aJob = new WorkspaceJob(Messages.PSession_0) {
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) {
				signalMgr.dispose(monitor);
				bptMgr.dispose(monitor);
				memMgr.dispose(monitor);
				regMgr.dispose(monitor);
				setMgr.dispose(monitor);
				locMgr.dispose(monitor);
				deleteDebugTargets(true);
				getPDISession().getEventManager().removeEventListener(PSession.this);
				getPDISession().shutdown(force);
				return Status.OK_STATUS;
			}
		};
		aJob.setSystem(true);
		aJob.schedule();
	}

	/**
	 * @param event
	 */
	private void fireChangeEvent(IPDIChangedEvent event) {
		IPDebugInfo baseInfo = getDebugInfo(event.getTasks());
		int detail = IPDebugEvent.UNSPECIFIED;

		IPDISessionObject reason = event.getReason();
		if (reason instanceof IPDIBreakpointInfo) {
			// Nothing currently required
		} else if (reason instanceof IPDIMemoryBlockInfo) {
			// Not currently implemented
		} else if (reason instanceof IPDISignalInfo) {
			// Not currently implemented
		} else if (reason instanceof IPDIVariableInfo) {
			// Not currently implemented
		}
		PTPDebugCorePlugin.getDefault().fireDebugEvent(new PDebugEvent(this, IPDebugEvent.CHANGE, detail, baseInfo));
	}

	/**
	 * @param event
	 */
	private void fireCreateEvent(IPDICreatedEvent event) {
		IPDebugInfo baseInfo = getDebugInfo(event.getTasks());
		int detail = IPDebugEvent.UNSPECIFIED;

		IPDISessionObject reason = event.getReason();
		if (reason instanceof IPDIBreakpointInfo) {
			// Nothing currently required
		} else if (reason instanceof IPDIThreadInfo) {
			// Nothing currently required
		} else if (reason instanceof IPDIMemoryBlockInfo) {
			// Not currently implemented
		} else if (reason instanceof IPDIRegisterInfo) {
			// Not currently implemented
		} else if (reason instanceof IPDISharedLibraryInfo) {
			// Not currently implemented
		} else if (reason instanceof IPDIVariableInfo) {
			// Not currently implemented
		}
		PTPDebugCorePlugin.getDefault().fireDebugEvent(new PDebugEvent(this, IPDebugEvent.CREATE, detail, baseInfo));
	}

	/**
	 * @param event
	 */
	private void fireDestroyEvent(IPDIDestroyedEvent event) {
		IPDebugEvent debugEvent = null;
		IPDebugInfo baseInfo = getDebugInfo(event.getTasks());
		IPDISessionObject reason = event.getReason();

		if (reason instanceof IPDIBreakpointInfo) {
		} else if (reason instanceof IPDIErrorInfo) {
			deleteDebugTarget(baseInfo.getAllRegisteredTasks().copy(), true, true);
			IPDebugInfo errInfo = new PDebugErrorInfo(baseInfo, ((IPDIErrorInfo) reason).getMessage(),
					((IPDIErrorInfo) reason).getDetailMessage(), ((IPDIErrorInfo) reason).getCode());
			debugEvent = new PDebugEvent(this, IPDebugEvent.TERMINATE, IPDebugEvent.ERROR, errInfo);
			changeProcessState(event.getTasks(), IPJobStatus.COMPLETED);
		} else if (reason instanceof IPDIExitInfo) {
			deleteDebugTarget(baseInfo.getAllRegisteredTasks().copy(), true, true);
			IPDebugInfo exitInfo = new PDebugExitInfo(baseInfo, ((IPDIExitInfo) reason).getCode(), Messages.PSession_2,
					Messages.PSession_3);
			debugEvent = new PDebugEvent(this, IPDebugEvent.TERMINATE, IPDebugEvent.PROCESS_SPECIFIC, exitInfo);
			changeProcessState(event.getTasks(), IPJobStatus.COMPLETED);
		} else if (reason instanceof IPDISignalInfo) {
			deleteDebugTarget(baseInfo.getAllRegisteredTasks().copy(), true, true);
			IPDebugInfo exitInfo = new PDebugExitInfo(baseInfo, 0, ((IPDISignalInfo) reason).getDescription(),
					((IPDISignalInfo) reason).getName());
			debugEvent = new PDebugEvent(this, IPDebugEvent.TERMINATE, IPDebugEvent.PROCESS_SPECIFIC, exitInfo);
			changeProcessState(event.getTasks(), IPJobStatus.COMPLETED);
		} else if (reason instanceof IPDISharedLibraryInfo) {
			// Nothing currently required
		} else if (reason instanceof IPDIThreadInfo) {
			// Nothing currently required
		} else if (reason instanceof IPDIVariableInfo) {
			// Nothing currently required
		} else {
			debugEvent = new PDebugEvent(this, IPDebugEvent.TERMINATE, IPDebugEvent.UNSPECIFIED, baseInfo);
		}
		PTPDebugCorePlugin.getDefault().fireDebugEvent(debugEvent);
	}

	/**
	 * @param event
	 */
	private void fireErrorEvent(IPDIErrorEvent event) {
		IPDebugInfo baseInfo = getDebugInfo(event.getTasks());
		int detail = IPDebugEvent.UNSPECIFIED;
		IPDISessionObject reason = event.getReason();
		if (reason instanceof IPDIErrorInfo) {
			int code = ((IPDIErrorInfo) reason).getCode();
			switch (code) {
			case IPDIErrorInfo.DBG_FATAL:
				detail = IPDebugEvent.ERR_FATAL;
				// only fatal error reports process error
				changeProcessState(event.getTasks(), IPJobStatus.COMPLETED); // TODO:
																				// how
																				// to
																				// report
																				// error?
				break;
			case IPDIErrorInfo.DBG_WARNING:
				detail = IPDebugEvent.ERR_WARNING;
				break;
			case IPDIErrorInfo.DBG_IGNORE:
			case IPDIErrorInfo.DBG_NORMAL:
				detail = IPDebugEvent.ERR_NORMAL;
				break;
			}
			IPDebugInfo errInfo = new PDebugErrorInfo(baseInfo, ((IPDIErrorInfo) reason).getMessage(),
					((IPDIErrorInfo) reason).getDetailMessage(), ((IPDIErrorInfo) reason).getCode());
			PTPDebugCorePlugin.getDefault().fireDebugEvent(new PDebugEvent(this, IPDebugEvent.ERROR, detail, errInfo));
		} else {
			IPDebugInfo errInfo = new PDebugErrorInfo(baseInfo, Messages.PSession_4, Messages.PSession_5,
					PTPDebugCorePlugin.INTERNAL_ERROR);
			PTPDebugCorePlugin.getDefault().fireDebugEvent(
					new PDebugEvent(this, IPDebugEvent.ERROR, IPDebugEvent.UNSPECIFIED, errInfo));
		}
	}

	/**
	 * @param event
	 */
	private void fireResumeEvent(IPDIResumedEvent event) {
		IPDebugInfo baseInfo = getDebugInfo(event.getTasks());
		int detail = IPDebugEvent.UNSPECIFIED;

		switch (event.getType()) {
		case IPDIResumedEvent.STEP_INTO:
		case IPDIResumedEvent.STEP_INTO_INSTRUCTION:
			detail = IPDebugEvent.STEP_INTO;
			break;
		case IPDIResumedEvent.STEP_OVER:
		case IPDIResumedEvent.STEP_OVER_INSTRUCTION:
			detail = IPDebugEvent.STEP_OVER;
			break;
		case IPDIResumedEvent.STEP_RETURN:
			detail = IPDebugEvent.STEP_RETURN;
			break;
		}

		changeProcessState(event.getTasks(), IPJobStatus.RUNNING);

		PTPDebugCorePlugin.getDefault().fireDebugEvent(new PDebugEvent(this, IPDebugEvent.RESUME, detail, baseInfo));
	}

	/**
	 * @param event
	 */
	private void fireSuspendEvent(IPDISuspendedEvent event) {
		IPDebugInfo baseInfo = getDebugInfo(event.getTasks());
		int detail = IPDebugEvent.UNSPECIFIED;

		int lineNumber = 0;
		int level = event.getLevel();
		int depth = event.getDepth();
		String fileName = ""; //$NON-NLS-1$
		IPDISessionObject reason = event.getReason();
		if (reason instanceof IPDIBreakpointInfo) {
			IPDIBreakpoint bpt = ((IPDIBreakpointInfo) reason).getBreakpoint();
			if (bpt instanceof IPDILocationBreakpoint) {
				IPDILocator locator = ((IPDILocationBreakpoint) bpt).getLocator();
				if (locator != null) {
					lineNumber = locator.getLineNumber();
					fileName += locator.getFile();
					detail = IPDebugEvent.BREAKPOINT;
				}
			}
		} else if (reason instanceof IPDIEndSteppingRangeInfo) {
			IPDILocator locator = ((IPDIEndSteppingRangeInfo) reason).getLocator();
			if (locator != null) {
				lineNumber = locator.getLineNumber();
				fileName += locator.getFile();
				detail = IPDebugEvent.STEP_END;
			}
		} else if (reason instanceof IPDILocationReachedInfo) {
			IPDILocator locator = ((IPDILocationReachedInfo) reason).getLocator();
			if (locator != null) {
				lineNumber = locator.getLineNumber();
				fileName += locator.getFile();
				detail = IPDebugEvent.CLIENT_REQUEST;
			}
		} else if (reason instanceof IPDISignalInfo) {
			IPDILocator locator = ((IPDISignalInfo) reason).getLocator();
			if (locator != null) {
				lineNumber = locator.getLineNumber();
				fileName += locator.getFile();
				detail = IPDebugEvent.CLIENT_REQUEST;
			}
		} else if (reason instanceof IPDIFunctionFinishedInfo) {
			// Not currently implemented
		} else if (reason instanceof IPDISharedLibraryInfo) {
			// Not currently implemented
		} else if (reason instanceof IPDIWatchpointScopeInfo) {
			// Not currently implemented
		} else if (reason instanceof IPDIWatchpointTriggerInfo) {
			// Not currently implemented
		}
		changeProcessState(event.getTasks(), IPJobStatus.SUSPENDED);
		PTPDebugCorePlugin.getDefault().fireDebugEvent(
				new PDebugEvent(getSession(), IPDebugEvent.SUSPEND, detail, new PDebugSuspendInfo(baseInfo, fileName, lineNumber,
						level, depth)));
	}

	/**
	 * Set the output attribute of all processes in the bitlist
	 * 
	 * @param tasks
	 * @param output
	 */
	private void setProcessOutput(TaskSet tasks, String output) {
		IJobStatus job = JobManager.getInstance().getJob(getLaunch().getJobControl().getControlId(), getLaunch().getJobId());
		if (job != null) {
			IPJobStatus pJob = (IPJobStatus) job.getAdapter(IPJobStatus.class);
			if (pJob != null) {
				pJob.setProcessOutput(tasks, output);
			}
		}
	}
}
