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
package org.eclipse.ptp.debug.ui;

import java.util.BitSet;

import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointListener;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.core.jobs.IJobStatus;
import org.eclipse.ptp.core.jobs.IPJobStatus;
import org.eclipse.ptp.debug.core.IPDebugConstants;
import org.eclipse.ptp.debug.core.IPSession;
import org.eclipse.ptp.debug.core.PDebugModel;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.model.IPDebugTarget;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.ui.messages.Messages;
import org.eclipse.ptp.ui.IElementManager;
import org.eclipse.ptp.ui.managers.JobManager;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.ptp.ui.model.IElementSet;
import org.eclipse.ptp.ui.views.IToolTipProvider;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * @author clement chu
 * 
 */
public class UIDebugManager extends JobManager implements IBreakpointListener {
	private interface IDebugProgressMonitor {
		IStatus runDebugJob(IJobStatus job, IPSession session, IProgressMonitor monitor);
	}

	private class UIDebugWorkbenchJob extends WorkbenchJob {
		private IJobStatus job = null;
		private IPSession session = null;
		private IDebugProgressMonitor debugMonitor = null;

		public UIDebugWorkbenchJob(boolean runInDialog, String name, IJobStatus job, IDebugProgressMonitor debugMonitor) {
			super(name);
			this.job = job;
			this.debugMonitor = debugMonitor;
			if (runInDialog) {
				PlatformUI.getWorkbench().getProgressService().showInDialog(null, this);
			}
			schedule();
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			return debugMonitor.runDebugJob(job, session, monitor);
		}

		@Override
		public boolean shouldRun() {
			session = getDebugSession(job.getJobId());
			if (session == null) {
				return false;
			}
			if (!session.isReady()) {
				return false;
			}
			return super.shouldRun();
		}
	}

	private final PVariableManager jobVarMgr = new PVariableManager();
	private PDebugModel debugModel = null;
	private IPSession currentSession = null;
	private boolean prefAutoUpdateVarOnSuspend = false;
	private boolean prefAutoUpdateVarOnChange = false;
	private boolean prefRegisterProc0 = true;

	private final IPreferenceChangeListener fPreferenceChangeListener = new IPreferenceChangeListener() {
		public void preferenceChange(PreferenceChangeEvent event) {
			String preferenceType = event.getKey();
			String value = (String) event.getNewValue();
			if (preferenceType.equals(IPDebugConstants.PREF_DEBUG_REGISTER_PROC_0)) {
				prefRegisterProc0 = new Boolean(value).booleanValue();
			} else if (preferenceType.equals(IPDebugConstants.PREF_UPDATE_VARIABLES_ON_SUSPEND)) {
				prefAutoUpdateVarOnSuspend = new Boolean(value).booleanValue();
			} else if (preferenceType.equals(IPDebugConstants.PREF_UPDATE_VARIABLES_ON_CHANGE)) {
				prefAutoUpdateVarOnChange = new Boolean(value).booleanValue();
			} else if (preferenceType.equals(IPDebugConstants.PREF_DEBUG_COMM_TIMEOUT)) {
				for (IJobStatus job : getJobs()) {
					IPSession session = getDebugSession(job.getJobId());
					if (session != null) {
						session.getPDISession().setRequestTimeout(new Integer(value).longValue());
					}
				}
			}
		}
	};

	public UIDebugManager() {
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
		debugModel = PTPDebugCorePlugin.getDebugModel();
		initializePreferences();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.IBreakpointListener#breakpointAdded(org.eclipse .debug.core.model.IBreakpoint)
	 */
	public void breakpointAdded(final IBreakpoint breakpoint) {
		if (PTPDebugUIPlugin.isPTPDebugPerspective()) {
			if (breakpoint instanceof ICLineBreakpoint) {
				// delete c breakpoint if the ptp debug perspective is active
				WorkbenchJob uiJob = new WorkbenchJob(Messages.UIDebugManager_0) {
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						try {
							DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(breakpoint, true);
						} catch (CoreException e) {
							return e.getStatus();
						}
						return Status.OK_STATUS;
					}
				};
				uiJob.setSystem(true);
				uiJob.schedule();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.IBreakpointListener#breakpointChanged(org.eclipse .debug.core.model.IBreakpoint,
	 * org.eclipse.core.resources.IMarkerDelta)
	 */
	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.IBreakpointListener#breakpointRemoved(org.eclipse .debug.core.model.IBreakpoint,
	 * org.eclipse.core.resources.IMarkerDelta)
	 */
	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.managers.AbstractElementManager#fireJobChangedEvent (int, java.lang.String, java.lang.String)
	 */
	@Override
	public void fireJobChangedEvent(int type, String new_id, String old_id) {
		// TODO ?? updateBreakpointMarker
		updateBreakpointMarker(IElementHandler.SET_ROOT_ID);
		if (old_id != null) {
			removeAllRegisterElements(old_id);
		}
		super.fireJobChangedEvent(type, new_id, old_id);
	}

	/**
	 * @since 5.0
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.managers.AbstractElementManager#fireSetEvent(int, java.util.BitSet,
	 * org.eclipse.ptp.ui.model.IElementSet, org.eclipse.ptp.ui.model.IElementSet)
	 */
	@Override
	public synchronized void fireSetEvent(int eventType, BitSet elements, IElementSet cur_set, IElementSet pre_set) {
		IJobStatus job = getJob();
		if (job != null) {
			IPSession session = getDebugSession(job.getJobId());
			if (session != null) {
				switch (eventType) {
				case CREATE_SET_TYPE:
					TaskSet cTasks = convertElementsToBitList(session, elements);
					debugModel.createSet(session, cur_set.getName(), cTasks);
					break;
				case DELETE_SET_TYPE:
					debugModel.deleteSet(session, cur_set.getName());
					break;
				case CHANGE_SET_TYPE:
					if (cur_set != null) {
						// annotationMgr.updateAnnotation(cur_set, pre_set);
						updateBreakpointMarker(cur_set.getName());
						updateRegisterUnRegisterElements(cur_set, pre_set, getCurrentJobId());
					}
					break;
				case ADD_ELEMENT_TYPE:
					TaskSet aTasks = convertElementsToBitList(session, elements);
					debugModel.addTasks(session, cur_set.getName(), aTasks);
					break;
				case REMOVE_ELEMENT_TYPE:
					TaskSet rTasks = convertElementsToBitList(session, elements);
					debugModel.removeTasks(session, cur_set.getName(), rTasks);
					break;
				}
				super.fireSetEvent(eventType, elements, cur_set, pre_set);
			}
		}
	}

	/**
	 * @return
	 */
	public String getCurrentJobId() {
		IJobStatus job = getJob();
		if (job != null) {
			return job.getJobId();
		}
		return IElementManager.EMPTY_ID;
	}

	/**
	 * @return
	 */
	public IPSession getCurrentSession() {
		if (currentSession == null) {
			IJobStatus job = getJob();
			if (job != null) {
				currentSession = getDebugSession(job.getJobId());
			}
		}
		return currentSession;
	}

	/**
	 * Get debug session
	 * 
	 * @param jobId
	 *            Job ID
	 * @return
	 */
	public IPSession getDebugSession(String jobId) {
		if (isNoJob(jobId)) {
			return null;
		}
		return debugModel.getSession(jobId);
	}

	/**
	 * @return
	 */
	public PVariableManager getJobVariableManager() {
		return jobVarMgr;
	}

	/**
	 * @param obj
	 * @return
	 * @since 3.0
	 */
	public TaskSet getSelectedRegisteredTasks(Object obj) {
		IDebugTarget target = null;
		if (obj instanceof IStackFrame) {
			target = ((IStackFrame) obj).getDebugTarget();
		} else if (obj instanceof IThread) {
			target = ((IThread) obj).getDebugTarget();
		} else if (obj instanceof IDebugTarget) {
			target = (IDebugTarget) obj;
		}

		if (target instanceof IPDebugTarget) {
			return ((IPDebugTarget) target).getTasks();
		}
		return null;
	}

	/**
	 * Get tasks from given set
	 * 
	 * @param job_id
	 *            job ID
	 * @param set_id
	 *            set ID
	 * @return
	 * @throws CoreException
	 * @since 3.0
	 */
	public TaskSet getTasks(IPSession session, String set_id) throws CoreException {
		return debugModel.getTasks(session, set_id);
	}

	/**
	 * @param set_id
	 * @return
	 * @throws CoreException
	 * @since 3.0
	 */
	public TaskSet getTasks(String set_id) throws CoreException {
		return getTasks(getCurrentJobId(), set_id);
	}

	/**
	 * @param job_id
	 * @param set_id
	 * @return
	 * @throws CoreException
	 * @since 3.0
	 */
	public TaskSet getTasks(String job_id, String set_id) throws CoreException {
		IPSession session = getDebugSession(job_id);
		if (session == null) {
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR,
					Messages.UIDebugManager_1, null));
		}
		return getTasks(session, set_id);
	}

	/**
	 * Get value text for tooltip
	 * 
	 * @param taskID
	 * @return
	 */
	public String getValueText(int taskID, IToolTipProvider provider) {
		IJobStatus job = getJob();
		if (job != null) {
			if (job instanceof IAdaptable) {
				IPJobStatus pJob = (IPJobStatus) ((IAdaptable) job).getAdapter(IPJobStatus.class);
				if (pJob != null) {
					return getJobVariableManager().getValue(pJob, taskID, provider);
				}
			}
		}

		return Messages.UIDebugManager_2;
	}

	/**
	 * @return
	 */
	public boolean isAutoUpdateVarOnChange() {
		return prefAutoUpdateVarOnChange;
	}

	/**
	 * @return
	 */
	public boolean isAutoUpdateVarOnSuspend() {
		return prefAutoUpdateVarOnSuspend;
	}

	/**
	 * Is job in debug mode
	 * 
	 * @param job
	 * @return true if given job in debug mode
	 * @since 5.0
	 */
	public boolean isDebugMode(IJobStatus job) {
		if (job == null) {
			return false;
		}
		return job.getLaunchMode().equals(ILaunchManager.DEBUG_MODE);
	}

	/**
	 * Is Job in debug mode
	 * 
	 * @param job_id
	 *            Job ID
	 * @return true if given job in debug mode
	 */
	public boolean isDebugMode(String job_id) {
		if (isNoJob(job_id)) {
			return false;
		}
		return isDebugMode(findJobById(job_id));
	}

	/**
	 * @return
	 */
	public boolean isEnabledDefaultRegister() {
		return prefRegisterProc0;
	}

	/**
	 * Is job running
	 * 
	 * @param job
	 * @return true if job is running
	 * @since 5.0
	 */
	public boolean isRunning(IJobStatus job) {
		return (job != null && job.getState() != IJobStatus.COMPLETED);
	}

	/**
	 * Is job running
	 * 
	 * @param job_id
	 *            job ID
	 * @return true if job is running
	 */
	public boolean isRunning(String job_id) {
		if (isNoJob(job_id)) {
			return false;
		}
		return isRunning(findJobById(job_id));
	}

	/**
	 * Register elements. Assumes that elements are not registered
	 * 
	 * @param elements
	 * @since 5.0
	 */
	public void registerElements(final BitSet elements) {
		new UIDebugWorkbenchJob(false, Messages.UIDebugManager_3, getJob(), new IDebugProgressMonitor() {
			public IStatus runDebugJob(IJobStatus job, IPSession session, IProgressMonitor monitor) {
				if (job == null || session == null) {
					return Status.CANCEL_STATUS;
				}

				TaskSet tasks = session.getTasks(-1);
				tasks.or(elements);
				registerTasks(session, tasks);
				return Status.OK_STATUS;
			}
		});
	}

	/**
	 * @param session
	 * @param tasks
	 * @since 3.0
	 */
	public void registerTasks(IPSession session, TaskSet tasks) {
		session.createDebugTarget(tasks, true, true);
	}

	/**
	 * Remove all register elements
	 * 
	 * @param job_id
	 *            job ID
	 */
	public void removeAllRegisterElements(final String job_id) {
		new UIDebugWorkbenchJob(false, Messages.UIDebugManager_4, findJobById(job_id), new IDebugProgressMonitor() {
			public IStatus runDebugJob(IJobStatus job, IPSession session, IProgressMonitor monitor) {
				if (job == null || session == null) {
					return Status.CANCEL_STATUS;
				}

				session.deleteDebugTargets(false);
				monitor.done();
				return Status.OK_STATUS;
			}
		});
	}

	/**
	 * Resume debugger
	 */
	public void resume() throws CoreException {
		resume(getCurrentJobId(), getCurrentSetId());
	}

	/**
	 * Resume debugger
	 * 
	 * @param job_id
	 *            job ID
	 * @param set_id
	 *            set ID
	 */
	public void resume(final String jobId, final String set_id) throws CoreException {
		IPSession session = getDebugSession(jobId);
		if (session == null) {
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR,
					Messages.UIDebugManager_5, null));
		}
		try {
			session.getPDISession().resume(getTasks(session, set_id), false);
		} catch (PDIException e) {
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR,
					e.getMessage(), null));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.managers.JobManager#setJob(org.eclipse.ptp.core.elements .IPJob)
	 */
	@Override
	public void setJob(IJobStatus job) {
		if (job != null) {
			currentSession = getDebugSession(job.getJobId());
		} else {
			currentSession = null;
		}
		super.setJob(job);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.managers.JobManager#shutdown()
	 */
	@Override
	public void shutdown() {
		Preferences.removePreferenceChangeListener(PTPDebugCorePlugin.getUniqueIdentifier(), fPreferenceChangeListener);
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
		jobVarMgr.shutdown();
		super.shutdown();
	}

	/**
	 * Step into debugger
	 */
	public void stepInto() throws CoreException {
		stepInto(getCurrentJobId(), getCurrentSetId());
	}

	/**
	 * Step into debugger
	 * 
	 * @param job_id
	 * @param set_id
	 */
	public void stepInto(final String job_id, final String set_id) throws CoreException {
		IPSession session = getDebugSession(job_id);
		if (session == null) {
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR,
					Messages.UIDebugManager_6, null));
		}
		try {
			TaskSet tasks = getTasks(session, set_id);
			// filterRunningTasks(tasks, STEP_INTO);
			session.getPDISession().stepInto(tasks, 1);
		} catch (PDIException e) {
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR,
					e.getMessage(), null));
		}
	}

	/**
	 * Step over debugger
	 */
	public void stepOver() throws CoreException {
		stepOver(getCurrentJobId(), getCurrentSetId());
	}

	/**
	 * Step over debugger
	 * 
	 * @param job_id
	 * @param set_id
	 */
	public void stepOver(final String job_id, final String set_id) throws CoreException {
		IPSession session = getDebugSession(job_id);
		if (session == null) {
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR,
					Messages.UIDebugManager_7, null));
		}
		try {
			TaskSet tasks = getTasks(session, set_id);
			// filterRunningTasks(tasks, STEP_OVER);
			session.getPDISession().stepOver(tasks, 1);
		} catch (PDIException e) {
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR,
					e.getMessage(), null));
		}
	}

	/**
	 * Step return debugger
	 */
	public void stepReturn() throws CoreException {
		stepReturn(getCurrentJobId(), getCurrentSetId());
	}

	/**
	 * Step return debugger
	 * 
	 * @param job_id
	 * @param set_id
	 * @throws CoreException
	 */
	public void stepReturn(final String job_id, final String set_id) throws CoreException {
		IPSession session = getDebugSession(job_id);
		if (session == null) {
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR,
					Messages.UIDebugManager_8, null));
		}
		try {
			TaskSet tasks = getTasks(session, set_id);
			// filterRunningTasks(tasks, STEP_RETURN);
			session.getPDISession().stepReturn(tasks, 0);
		} catch (PDIException e) {
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR,
					e.getMessage(), null));
		}
	}

	/**
	 * Suspend debugger
	 */
	public void suspend() throws CoreException {
		suspend(getCurrentJobId(), getCurrentSetId());
	}

	/**
	 * Suspend debugger
	 * 
	 * @param job_id
	 * @param set_id
	 */
	public void suspend(final String job_id, final String set_id) throws CoreException {
		IPSession session = getDebugSession(job_id);
		if (session == null) {
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR,
					Messages.UIDebugManager_9, null));
		}
		try {
			session.getPDISession().suspend(getTasks(session, set_id));
		} catch (PDIException e) {
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR,
					e.getMessage(), null));
		}
	}

	/**
	 * Terminate debugger
	 */
	public void terminate() throws CoreException {
		terminate(getCurrentJobId(), getCurrentSetId());
	}

	/**
	 * Terminate debugger
	 * 
	 * @param job_id
	 * @param set_id
	 */
	public void terminate(final String jobId, final String set_id) throws CoreException {
		if (isDebugMode(jobId)) {
			IPSession session = getDebugSession(jobId);
			if (session == null) {
				throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR,
						Messages.UIDebugManager_10, null));
			}
			try {
				session.getPDISession().terminate(getTasks(session, set_id));
			} catch (PDIException e) {
				throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR,
						e.getMessage(), null));
			}
		}
	}

	/**
	 * Unregister elements. Assumes elements are already registered.
	 * 
	 * @param elements
	 * @since 5.0
	 */
	public void unregisterElements(final BitSet elements) {
		new UIDebugWorkbenchJob(false, Messages.UIDebugManager_11, getJob(), new IDebugProgressMonitor() {
			public IStatus runDebugJob(IJobStatus job, IPSession session, IProgressMonitor monitor) {
				if (job == null || session == null) {
					return Status.CANCEL_STATUS;
				}

				TaskSet tasks = session.getTasks(-1);
				tasks.or(elements);
				unregisterTasks(session, tasks);
				return Status.OK_STATUS;
			}
		});
	}

	/**
	 * @param session
	 * @param tasks
	 * @since 3.0
	 */
	public void unregisterTasks(IPSession session, TaskSet tasks) {
		session.deleteDebugTarget(tasks, true, true);
	}

	/**
	 * Update breakpoint marker
	 * 
	 * @param cur_sid
	 *            current set ID
	 */
	public void updateBreakpointMarker(final String cur_sid) {
		WorkbenchJob uiJob = new WorkbenchJob(Messages.UIDebugManager_12) {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				try {
					PDebugModel.updateBreakpoints(cur_sid, monitor);
				} catch (CoreException e) {
					return e.getStatus();
				}
				return Status.OK_STATUS;
			}
		};
		uiJob.setSystem(true);
		uiJob.schedule();
	}

	/**
	 * 
	 */
	public void updateCurrentJobVariableValues() {
		IJobStatus job = getJob();
		if (job != null) {
			getJobVariableManager().updateValues(job.getJobId());
		}
	}

	/**
	 * Update register and unregister elements
	 * 
	 * @param curSet
	 * @param preSet
	 * @param job_id
	 */
	public void updateRegisterUnRegisterElements(final IElementSet curSet, final IElementSet preSet, final String job_id) {
		new UIDebugWorkbenchJob(false, Messages.UIDebugManager_13, findJobById(job_id), new IDebugProgressMonitor() {
			public IStatus runDebugJob(IJobStatus job, IPSession session, IProgressMonitor monitor) {
				if (job == null || session == null) {
					return Status.CANCEL_STATUS;
				}

				session.reloadDebugTargets(debugModel.getTasks(session, curSet.getID()), true, false);
				monitor.done();
				return Status.OK_STATUS;
			}
		});
	}

	/**
	 * @param session
	 * @param elements
	 * @return
	 */
	private TaskSet convertElementsToBitList(IPSession session, BitSet elements) {
		TaskSet tasks = session.getTasks(-1);
		tasks.or(elements);
		return tasks;
	}

	/**
	 * Initialize preference settings
	 */
	private void initializePreferences() {
		Preferences.addPreferenceChangeListener(PTPDebugCorePlugin.getUniqueIdentifier(), fPreferenceChangeListener);
		prefRegisterProc0 = Preferences.getBoolean(PTPDebugCorePlugin.getUniqueIdentifier(),
				IPDebugConstants.PREF_DEBUG_REGISTER_PROC_0);
		prefAutoUpdateVarOnSuspend = Preferences.getBoolean(PTPDebugCorePlugin.getUniqueIdentifier(),
				IPDebugConstants.PREF_UPDATE_VARIABLES_ON_SUSPEND);
		prefAutoUpdateVarOnChange = Preferences.getBoolean(PTPDebugCorePlugin.getUniqueIdentifier(),
				IPDebugConstants.PREF_UPDATE_VARIABLES_ON_CHANGE);
	}

}
