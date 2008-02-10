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
package org.eclipse.ptp.debug.internal.ui;

import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPProcess;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.DebugJobStorage;
import org.eclipse.ptp.debug.core.IPDebugConstants;
import org.eclipse.ptp.debug.core.IPSession;
import org.eclipse.ptp.debug.core.PDebugModel;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.ProcessInputStream;
import org.eclipse.ptp.debug.core.model.IPDebugTarget;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.ui.IManager;
import org.eclipse.ptp.ui.OutputConsole;
import org.eclipse.ptp.ui.managers.JobManager;
import org.eclipse.ptp.ui.model.IElement;
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
		IStatus runDebugJob(IPJob job, IPSession session, IProgressMonitor monitor);
	}
	
	private class UIDebugWorkbenchJob extends WorkbenchJob {
		private IPJob job = null;
		private IPSession session = null;
		private IDebugProgressMonitor debugMonitor = null;
		public UIDebugWorkbenchJob(boolean runInDialog, String name, IPJob job, IDebugProgressMonitor debugMonitor) {
			super(name);
			this.job = job;
			this.debugMonitor = debugMonitor;
			if (runInDialog)
				PlatformUI.getWorkbench().getProgressService().showInDialog(null, this);
			schedule();
		}
		public IStatus runInUIThread(IProgressMonitor monitor) {
			return debugMonitor.runDebugJob(job, session, monitor);
		}
	    public boolean shouldRun() {
	    	session = getDebugSession(job);
	    	if (session == null)
	    		return false;
	    	if (!session.isReady())
	    		return false;
	    	return super.shouldRun();
	    }
	}
	
	private DebugJobStorage consoleStorage = new DebugJobStorage("Console");
	private PVariableManager jobVarMgr = new PVariableManager();
	private PAnnotationManager annotationMgr = null;

	private PDebugModel debugModel = null;
	private IPSession currentSession = null;
	private boolean prefAutoUpdateVarOnSuspend = false;
	
	private boolean prefAutoUpdateVarOnChange = false;
	
	private boolean prefRegisterProc0 = true;
	
	private IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			String preferenceType = event.getProperty();
			String value = (String)event.getNewValue();
			if (preferenceType.equals(IPDebugConstants.PREF_PTP_DEBUG_REGISTER_PROC_0)) {
				prefRegisterProc0 = new Boolean(value).booleanValue();
			}
			else if (preferenceType.equals(IPDebugConstants.PREF_UPDATE_VARIABLES_ON_SUSPEND)) {
				prefAutoUpdateVarOnSuspend = new Boolean(value).booleanValue();
			}
			else if (preferenceType.equals(IPDebugConstants.PREF_UPDATE_VARIABLES_ON_CHANGE)) {
				prefAutoUpdateVarOnChange = new Boolean(value).booleanValue();
			}
			else if (preferenceType.equals(IPDebugConstants.PREF_PTP_DEBUG_COMM_TIMEOUT)) {
				for (IPJob job : getJobs()) {
					IPSession session = getDebugSession(job);
					if (session != null) {
						session.getPDISession().setRequestTimeout(new Integer(value).longValue());
					}
				}
			}
		}
	};
	
	public UIDebugManager() {
		PTPDebugUIPlugin.getDefault().getPluginPreferences().addPropertyChangeListener(propertyChangeListener);
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
		debugModel = PTPDebugCorePlugin.getDebugModel();
		annotationMgr = new PAnnotationManager(this);
		initializePreferences();
	}
	
	/**
	 * Add process to console window if it is register into Debug View
	 * @param proc
	 */
	public void addConsoleWindow(IPJob job, IPProcess proc) {
		if (consoleStorage.getValue(job.getID(), proc.getID()) == null) {
			OutputConsole outputConsole = new OutputConsole(proc.getName(), new ProcessInputStream(proc));
			consoleStorage.addValue(job.getID(), proc.getID(), outputConsole);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointListener#breakpointAdded(org.eclipse.debug.core.model.IBreakpoint)
	 */
	public void breakpointAdded(final IBreakpoint breakpoint) {
		if (PTPDebugUIPlugin.isPTPDebugPerspective()) {
			if (breakpoint instanceof ICLineBreakpoint) {
				//delete c breakpoint if the ptp debug perspective is active
				WorkbenchJob uiJob = new WorkbenchJob("Removing C-Line breakpoint...") {
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointListener#breakpointChanged(org.eclipse.debug.core.model.IBreakpoint, org.eclipse.core.resources.IMarkerDelta)
	 */
	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointListener#breakpointRemoved(org.eclipse.debug.core.model.IBreakpoint, org.eclipse.core.resources.IMarkerDelta)
	 */
	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {}
	
	/**
	 * Convert string to integer
	 * @param id
	 * @return
	 */
	public int convertToInt(String id) {
		return Integer.parseInt(id);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.managers.AbstractUIManager#fireJobChangedEvent(int, java.lang.String, java.lang.String)
	 */
	public void fireJobChangedEvent(int type, String cur_jid, String pre_jid) {
		//TODO ?? updateBreakpointMarker
		updateBreakpointMarker(IElementHandler.SET_ROOT_ID);
		removeAllRegisterElements(pre_jid);
		super.fireJobChangedEvent(type, cur_jid, pre_jid);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.ui.AbstractUIManager#fireSetEvent(int, org.eclipse.ptp.ui.model.IElement[], org.eclipse.ptp.ui.model.IElementSet, org.eclipse.ptp.ui.model.IElementSet)
	 */
	public synchronized void fireSetEvent(int eventType, IElement[] elements, IElementSet cur_set, IElementSet pre_set) {
		IPSession session = getDebugSession(getJob());
		if (session != null) {
			switch (eventType) {
			case CREATE_SET_TYPE:
				BitList cTasks = convertElementsToBitList(session, elements);
				debugModel.createSet(session, cur_set.getName(), cTasks);
				break;
			case DELETE_SET_TYPE:
				debugModel.deleteSet(session, cur_set.getName());
				break;
			case CHANGE_SET_TYPE:
				if (cur_set != null) {
					//annotationMgr.updateAnnotation(cur_set, pre_set);
					updateBreakpointMarker(cur_set.getName());
					updateRegisterUnRegisterElements(cur_set, pre_set, getCurrentJobId());
				}
				break;
			case ADD_ELEMENT_TYPE:
				BitList aTasks = convertElementsToBitList(session, elements);
				debugModel.addTasks(session, cur_set.getName(), aTasks);
				break;
			case REMOVE_ELEMENT_TYPE:
				BitList rTasks = convertElementsToBitList(session, elements);
				debugModel.removeTasks(session, cur_set.getName(), rTasks);
				break;
			}
			super.fireSetEvent(eventType, elements, cur_set, pre_set);
		}
	}
	
	/**
	 * @return
	 */
	public String getCurrentJobId() {
		IPJob job = getJob();
		if (job != null) {
			return job.getID();
		}
		return IManager.EMPTY_ID;
	}
	
	/**
	 * @return
	 */
	public IPSession getCurrentSession() {
		if (currentSession == null)
			currentSession = getDebugSession(getJob());
		return currentSession;
	}
	
	/**
	 * Get debug session
	 * @param job
	 * @return
	 */
	public IPSession getDebugSession(IPJob job) {
		if (job == null)
			return null;
		return debugModel.getSession(job);
	}
	
	/**
	 * Get debug session
	 * @param job_id Job ID
	 * @return
	 */
	public IPSession getDebugSession(String job_id) {
		if (isNoJob(job_id))
			return null;
		return getDebugSession(findJobById(job_id));
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
	 */
	public BitList getSelectedRegisteredTasks(Object obj) {
		IDebugTarget target = null;
		if (obj instanceof IStackFrame) {
			target = ((IStackFrame)obj).getDebugTarget();
		}
		else if (obj instanceof IThread) {
			target = ((IThread)obj).getDebugTarget();
		}
		else if (obj instanceof IDebugTarget) {
			target = (IDebugTarget)obj;
		}
		
		if (target instanceof IPDebugTarget) {
			 return ((IPDebugTarget)target).getTasks();
		}
		return null;
	}
	
	/** 
	 * Get tasks from given set
	 * @param job_id job ID
	 * @param set_id set ID
	 * @return
	 * @throws CoreException
	 */
	public BitList getTasks(IPSession session, String set_id) throws CoreException {
		return debugModel.getTasks(session, set_id);
	}

	/**
	 * @param set_id
	 * @return
	 * @throws CoreException
	 */
	public BitList getTasks(String set_id) throws CoreException {
		return getTasks(getCurrentJobId(), set_id);
	}
	
	/**
	 * @param job_id
	 * @param set_id
	 * @return
	 * @throws CoreException
	 */
	public BitList getTasks(String job_id, String set_id) throws CoreException {
		IPSession session = getDebugSession(job_id);
		if (session == null)
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No session found", null));
		return getTasks(session, set_id);
	}
	
	/**
	 * Get value text for tooltip
	 * @param taskID
	 * @return
	 */
	public String getValueText(int taskID, IToolTipProvider provider) {
		IPJob job = getJob();
		if (job == null)
			return "No job found";
		
		return getJobVariableManager().getValue(getJob(), taskID, provider);
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
	 * @param job
	 * @return true if given job in debug mode
	 */
	public boolean isDebugMode(IPJob job) {
		if (job == null)
			return false;
		return job.isDebug();
	}
	
	/**
	 * Is Job in debug mode
	 * @param job_id Job ID
	 * @return true if given job in debug mode
	 */
	public boolean isDebugMode(String job_id) {
		if (isNoJob(job_id))
			return false;
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
	 * @param job
	 * @return true if job is running
	 */
	public boolean isRunning(IPJob job) {
		return (job != null && !job.isTerminated());
	}
	
	/**
	 * Is job running
	 * @param job_id job ID
	 * @return true if job is running
	 */
	public boolean isRunning(String job_id) {
		if (isNoJob(job_id))
			return false;
		return isRunning(findJobById(job_id));
	}
	
	/** 
	 * Register elements
	 * @param elements
	 */
	public void registerElements(final IElement[] elements) {
		new UIDebugWorkbenchJob(false, "Registered elements", getJob(), new IDebugProgressMonitor() {
			public IStatus runDebugJob(IPJob job, IPSession session, IProgressMonitor monitor) {
				if (job == null || session == null)
					return Status.CANCEL_STATUS;

				BitList tasks = session.getTasks(-1);
				for (IElement element : elements) {
					// only register some unregistered elements
					if (!element.isRegistered()) {
						try {
							tasks.set(Integer.parseInt(element.getName()));
						} catch (NumberFormatException e) {
							// The element name had better be the process number
							PTPDebugCorePlugin.log(e);
						}
					}
				}
				registerTasks(session, tasks);
				return Status.OK_STATUS;
			}
		});
	}
	
	/**
	 * @param session
	 * @param tasks
	 */
	public void registerTasks(IPSession session, BitList tasks) {
		session.createDebugTarget(tasks, true, true);
	}
	
	/** 
	 * Remove all register elements
	 * @param job_id job ID
	 */
	public void removeAllRegisterElements(final String job_id){
		new UIDebugWorkbenchJob(false, "Removing registered processes", findJobById(job_id), new IDebugProgressMonitor() {
			public IStatus runDebugJob(IPJob job, IPSession session, IProgressMonitor monitor) {
				if (job == null || session == null)
					return Status.CANCEL_STATUS;
				
				session.deleteDebugTargets(false);
				monitor.done();
				return Status.OK_STATUS;
			}
		});
	}
	
	/**
	 * Remove process from console list and close its output
	 * @param proc
	 */
	public void removeConsoleWindow(IPJob job, IPProcess proc) {
		OutputConsole outputConsole = (OutputConsole) consoleStorage.removeValue(job.getID(), proc.getID());
		if (outputConsole != null) {
			outputConsole.kill();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#removeJob(org.eclipse.ptp.core.IPJob)
	 */
	public void removeJob(IPJob job) {
		if (job.isDebug()) {
			debugModel.shutdownSession(job);
			removeConsoleWindows(job);
		}
		super.removeJob(job);
	}
	
	/** 
	 * Resume debugger
	 */
	public void resume() throws CoreException {
		resume(getCurrentJobId(), getCurrentSetId());
	}
	
	/** 
	 * Resume debugger
	 * @param job_id job ID
	 * @param set_id set ID
	 */
	public void resume(final String job_id, final String set_id) throws CoreException {
		IPSession session = getDebugSession(job_id);
		if (session == null)
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No session found", null));
		try {
			session.getPDISession().resume(getTasks(session, set_id), false);
		} catch (PDIException e) {
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), null));
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.managers.JobManager#setJob(org.eclipse.ptp.core.elements.IPJob)
	 */
	public void setJob(IPJob job) {
		currentSession = getDebugSession(job);
		super.setJob(job);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.managers.JobManager#shutdown()
	 */
	public void shutdown() {
		PTPDebugUIPlugin.getDefault().getPluginPreferences().removePropertyChangeListener(propertyChangeListener);
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
		annotationMgr.shutdown();
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
	 * @param job_id
	 * @param set_id
	 */
	public void stepInto(final String job_id, final String set_id) throws CoreException {
		IPSession session = getDebugSession(job_id);
		if (session == null)
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No session found", null));
		try {
			BitList tasks = getTasks(session, set_id);
			//filterRunningTasks(tasks, STEP_INTO);
			session.getPDISession().stepInto(tasks, 1);
		} catch (PDIException e) {
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), null));
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
	 * @param job_id
	 * @param set_id
	 */
	public void stepOver(final String job_id, final String set_id) throws CoreException {
		IPSession session = getDebugSession(job_id);
		if (session == null)
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No session found", null));
		try {
			BitList tasks = getTasks(session, set_id);
			//filterRunningTasks(tasks, STEP_OVER);
			session.getPDISession().stepOver(tasks, 1);
		} catch (PDIException e) {
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), null));
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
	 * @param job_id
	 * @param set_id
	 * @throws CoreException
	 */
	public void stepReturn(final String job_id, final String set_id) throws CoreException {
		IPSession session = getDebugSession(job_id);
		if (session == null)
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No session found", null));
		try {
			BitList tasks = getTasks(session, set_id);
			//filterRunningTasks(tasks, STEP_RETURN);
			session.getPDISession().stepReturn(tasks, 0);
		} catch (PDIException e) {
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), null));
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
	 * @param job_id
	 * @param set_id
	 */
	public void suspend(final String job_id, final String set_id) throws CoreException {
		IPSession session = getDebugSession(job_id);
		if (session == null)
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No session found", null));
		try {
			session.getPDISession().suspend(getTasks(session, set_id));
		} catch (PDIException e) {
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), null));
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
	 * @param job_id
	 * @param set_id
	 */
	public void terminate(final String job_id, final String set_id) throws CoreException {
		IPJob job = findJobById(job_id);
		if (isDebugMode(job)) {
			IPSession session = getDebugSession(job);
			if (session == null)
				throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No session found", null));
			try {
				session.getPDISession().terminate(getTasks(session, set_id));
			} catch (PDIException e) {
				throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), null));
			}
		}
	}
	
	/**
	 * Unregister elements
	 * @param elements
	 */
	public void unregisterElements(final IElement[] elements) {
		new UIDebugWorkbenchJob(false, "Unregistered elements", getJob(), new IDebugProgressMonitor() {
			public IStatus runDebugJob(IPJob job, IPSession session, IProgressMonitor monitor) {
				if (job == null || session == null)
					return Status.CANCEL_STATUS;
				
				BitList tasks = session.getTasks(-1);
				for (IElement element : elements) {
					if (element.isRegistered()) {
						// only unregister some registered elements
						try {
							tasks.set(Integer.parseInt(element.getName()));
						} catch (NumberFormatException e) {
							// The element name had better be the process number
							PTPDebugCorePlugin.log(e);
						}
					}
				}
				unregisterTasks(session, tasks);
				return Status.OK_STATUS;
			}
		});
	}
	
	/**
	 * @param session
	 * @param tasks
	 */
	public void unregisterTasks(IPSession session, BitList tasks) {
		session.deleteDebugTarget(tasks, true, true);
	}
	
	/**
	 * Update breakpoint marker
	 * @param cur_sid current set ID
	 */
	public void updateBreakpointMarker(final String cur_sid) {
		WorkbenchJob uiJob = new WorkbenchJob("Updating breakpoint marker...") {
			public IStatus runInUIThread(IProgressMonitor monitor) {
				try {
					PDebugModel.updatePBreakpoints(cur_sid,  monitor);
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
		getJobVariableManager().updateValues(getJob());
	}
	
	/** 
	 * Update register and unregister elements
	 * @param curSet
	 * @param preSet
	 * @param job_id
	 */
	public void updateRegisterUnRegisterElements(final IElementSet curSet, final IElementSet preSet, final String job_id) {
		new UIDebugWorkbenchJob(false, "Updating registered/unregistered processes", findJobById(job_id), new IDebugProgressMonitor() {
			public IStatus runDebugJob(IPJob job, IPSession session, IProgressMonitor monitor) {
				if (job == null || session == null)
					return Status.CANCEL_STATUS;
				
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
	private BitList convertElementsToBitList(IPSession session, IElement[] elements) {
		BitList tasks = session.getTasks(-1);
		for (IElement element : elements) {
			tasks.set(convertToInt(element.getName()));
		}
		return tasks;
	}

	/**
	 * @param job
	 */
	private void removeConsoleWindows(IPJob job) {
		for (OutputConsole outputConsole : consoleStorage.getValueCollection(job.getID()).toArray(new OutputConsole[0])) {
			if (outputConsole != null) {
				outputConsole.kill();
			}
		}
		consoleStorage.removeJobStorage(job.getID());
	}

	/**
	 * Initialize preference settings
	 */
	private void initializePreferences() {
		Preferences prefStore = PTPDebugUIPlugin.getDefault().getPluginPreferences();
		prefRegisterProc0 = prefStore.getBoolean(IPDebugConstants.PREF_PTP_DEBUG_REGISTER_PROC_0);
		prefAutoUpdateVarOnSuspend = prefStore.getBoolean(IPDebugConstants.PREF_UPDATE_VARIABLES_ON_SUSPEND);
		prefAutoUpdateVarOnChange = prefStore.getBoolean(IPDebugConstants.PREF_UPDATE_VARIABLES_ON_CHANGE);
	}

}
