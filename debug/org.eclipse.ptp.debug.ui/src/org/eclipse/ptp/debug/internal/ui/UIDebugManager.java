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

import java.util.Iterator;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IStep;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPProcess;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.DebugJobStorage;
import org.eclipse.ptp.debug.core.IPDebugConstants;
import org.eclipse.ptp.debug.core.PCDIDebugModel;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.ProcessInputStream;
import org.eclipse.ptp.debug.core.cdi.IPCDISession;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.model.IPDebugElement;
import org.eclipse.ptp.debug.core.model.IPDebugTarget;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.debug.ui.model.DebugElement;
import org.eclipse.ptp.ui.IManager;
import org.eclipse.ptp.ui.OutputConsole;
import org.eclipse.ptp.ui.managers.JobManager;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.ptp.ui.model.IElementSet;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * @author clement chu
 * 
 */
public class UIDebugManager extends JobManager implements IBreakpointListener {
	//private final static int REG_TYPE = 1;
	//private final static int UNREG_TYPE = 2;
	private DebugJobStorage consoleStorage = new DebugJobStorage("Console");
	private PJobVariableManager jobMgr = new PJobVariableManager();	
	private PAnnotationManager annotationMgr = null;
	private PCDIDebugModel debugModel = null;

	private boolean prefAutoUpdateVarOnSuspend = false;
	private boolean prefAutoUpdateVarOnChange = false;
	private boolean prefRegisterProc0 = true;
	
	private final int STEP_INTO = 1;
	private final int STEP_OVER = 2;
	private final int STEP_RETURN = 3;
		
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
		}
	};
	public boolean isAutoUpdateVarOnSuspend() {
		return prefAutoUpdateVarOnSuspend;
	}
	public boolean isAutoUpdateVarOnChange() {
		return prefAutoUpdateVarOnChange;
	}
	public boolean isRegProc0() {
		return prefRegisterProc0;
	}

	/** Constructor
	 * 
	 */
	public UIDebugManager() {
		PTPDebugUIPlugin.getDefault().getPluginPreferences().addPropertyChangeListener(propertyChangeListener);
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
		debugModel = PTPDebugCorePlugin.getDebugModel();
		annotationMgr = new PAnnotationManager(this);
		settingPreference();
	}
	public PJobVariableManager getJobVariableManager() {
		return jobMgr;
	}
	/** Initial preference settings
	 * 
	 */
	private void settingPreference() {
		IPreferenceStore prefStore = PTPDebugUIPlugin.getDefault().getPreferenceStore();
		prefRegisterProc0 = prefStore.getBoolean(IPDebugConstants.PREF_PTP_DEBUG_REGISTER_PROC_0);
		prefAutoUpdateVarOnSuspend = prefStore.getBoolean(IPDebugConstants.PREF_UPDATE_VARIABLES_ON_SUSPEND);
		prefAutoUpdateVarOnChange = prefStore.getBoolean(IPDebugConstants.PREF_UPDATE_VARIABLES_ON_CHANGE);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#shutdown()
	 */
	public void shutdown() {
		PTPDebugUIPlugin.getDefault().getPluginPreferences().removePropertyChangeListener(propertyChangeListener);
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
		annotationMgr.shutdown();
		jobMgr.shutdown();
		super.shutdown();
	}
	protected IElement createElement(IElementSet set, String key, String name) {
		return new DebugElement(set, key, name);
	}
	public String getCurrentJobId() {
		IPJob job = getJob();
		if (job != null) {
			return job.getID();
		}
		return IManager.EMPTY_ID;
	}
	/** Get value text for tooltip
	 * @param taskID
	 * @return
	 */
	public String getValueText(String taskIDStr) {
		try {
			int taskID = Integer.parseInt(taskIDStr);
			return getJobVariableManager().getResultDisplay(getCurrentJobId(), taskID);
		} catch (NumberFormatException e) {
			return "";
		}
	}
	
	/** Register process 0 if default registeris true 
	 * @param session
	 */
	public void defaultRegister(IPCDISession session) { // register process 0 if the preference is checked
		if (prefRegisterProc0) {
			IPProcess proc = session.getJob().getProcessByNumber("0");
			if (proc != null) {
				addConsoleWindow(session.getJob(), proc);
				registerProcess(session, session.createBitList(0), true);
			}
		}
	}
	/** Is Job in debug mode
	 * @param job_id Job ID
	 * @return true if given job in debug mode
	 */
	public boolean isDebugMode(String job_id) {
		if (isNoJob(job_id))
			return false;
		return isDebugMode(findJobById(job_id));
	}
	/** Is job in debug mode
	 * @param job
	 * @return true if given job in debug mode
	 */
	public boolean isDebugMode(IPJob job) {
		if (job == null)
			return false;
		return job.isDebug();
	}
	/** Is job running
	 * @param job
	 * @return true if job is running
	 */
	public boolean isRunning(IPJob job) {
		return (job != null && !job.isTerminated());
	}
	/** Is job running
	 * @param job_id job ID
	 * @return true if job is running
	 */
	public boolean isRunning(String job_id) {
		if (isNoJob(job_id))
			return false;
		return isRunning(findJobById(job_id));
	}
	// change job
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.ui.AbstractUIManager#fireJobChangedEvent(int, java.lang.String, java.lang.String)
	 */
	public void fireJobChangedEvent(int type, String cur_jid, String pre_jid) {
		updateBreakpointMarker(IElementHandler.SET_ROOT_ID);
		try {
			removeAllRegisterElements(pre_jid);
		} catch (CoreException e) {
			PTPDebugUIPlugin.log(e);
		}
		super.fireJobChangedEvent(type, cur_jid, pre_jid);
	}
	/** Get debug session
	 * @param job_id Job ID
	 * @return
	 */
	public IPCDISession getDebugSession(String job_id) {
		if (isNoJob(job_id))
			return null;
		return debugModel.getPCDISession(job_id);
		//return getDebugSession(findJobById(job_id));
	}
	/** Get debug session
	 * @param job
	 * @return
	 */
	public IPCDISession getDebugSession(IPJob job) {
		if (job == null)
			return null;
		return debugModel.getPCDISession(job.getID());
	}
	/** Convert string to integer
	 * @param id
	 * @return
	 */
	public int convertToInt(String id) {
		return Integer.parseInt(id);
	}
	/** Add process to console window if it is register into Debug View
	 * @param proc
	 */
	private void addConsoleWindow(IPJob job, IPProcess proc) {
		if (consoleStorage.getValue(job.getID(), proc.getID()) == null) {
			OutputConsole outputConsole = new OutputConsole(proc.getName(), new ProcessInputStream(proc));
			consoleStorage.addValue(job.getID(), proc.getID(), outputConsole);
		}
	}
	/** Remove process from console list and close its output
	 * @param proc
	 */
	private void removeConsoleWindow(IPJob job, IPProcess proc) {
		OutputConsole outputConsole = (OutputConsole) consoleStorage.removeValue(job.getID(), proc.getID());
		if (outputConsole != null) {
			outputConsole.kill();
		}
	}
	private void removeConsoleWindows(IPJob job) {
		for (Iterator i=consoleStorage.getValueIterator(job.getID()); i.hasNext();) {
			OutputConsole outputConsole = (OutputConsole)i.next();
			if (outputConsole != null) {
				outputConsole.kill();
			}
		}
		consoleStorage.removeJobStorage(job.getID());
	}
	/***************************************************************************************************************************************************************************************************
	 * Register / Unregister
	 **************************************************************************************************************************************************************************************************/
	/** Register process
	 * @param session
	 * @param proc
	 * @param isChanged
	 */
	public void registerProcess(IPCDISession session, BitList tasks, boolean isChanged) {
		if (!tasks.isEmpty()) {
			session.registerTargets(tasks, isChanged);
		}
	}
	/** Unregister process
	 * @param session
	 * @param proc
	 * @param isChanged
	 */
	public void unregisterProcess(IPCDISession session, BitList tasks, boolean isChanged) {
		if (!tasks.isEmpty()) {
			session.unregisterTargets(tasks, isChanged);
		}
	}
	/** Unregister elements
	 * @param elements
	 * @throws CoreException
	 */
	public void unregisterElements(final IElement[] elements) throws CoreException {
		WorkbenchJob uiJob = new WorkbenchJob("Unregistering elements...") {
			public IStatus runInUIThread(IProgressMonitor monitor) {
				IPJob job = getJob();
				if (job == null)
					return new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No job found", null);
				IPCDISession session = getDebugSession(job);
				if (session == null)
					return Status.CANCEL_STATUS;
				//return new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No session found", null);
		
				BitList tasks = session.createEmptyBitList();
				for (int i = 0; i < elements.length; i++) {
					// only unregister some registered elements
					if (elements[i].isRegistered()) {
						IPProcess proc = findProcess(job, elements[i].getID());
						if (proc != null) {
							removeConsoleWindow(job, proc);
							tasks.set(elements[i].getIDNum());
						}
					}
				}
				unregisterProcess(session, tasks, true);
				return Status.OK_STATUS;
			}
		};
		uiJob.setSystem(false);
		uiJob.setPriority(Job.INTERACTIVE);
		uiJob.schedule();
	}
	/** Register elements
	 * @param elements
	 * @throws CoreException
	 */
	public void registerElements(final IElement[] elements) throws CoreException {
		WorkbenchJob uiJob = new WorkbenchJob("registering elements...") {
			public IStatus runInUIThread(IProgressMonitor monitor) {
				IPJob job = getJob();
				if (job == null)
					return new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No job found", null);
		
				IPCDISession session = getDebugSession(job);
				if (session == null)
					return Status.CANCEL_STATUS;
				//return new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No session found", null);
				
				BitList tasks = session.createEmptyBitList();
				for (int i = 0; i < elements.length; i++) {
					// only register some unregistered elements
					if (!elements[i].isRegistered()) {
						IPProcess proc = findProcess(job, elements[i].getID());
						if (proc != null && !proc.isTerminated()) {
							addConsoleWindow(job, proc);
							tasks.set(elements[i].getIDNum());
						}
					}
				}
				registerProcess(session, tasks, true);
				return Status.OK_STATUS;
			}
		};
		uiJob.setSystem(false);
		uiJob.setPriority(Job.INTERACTIVE);
		uiJob.schedule();
	}
	/***************************************************************************************************************************************************************************************************
	 * Breakpoint
	 **************************************************************************************************************************************************************************************************/
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointListener#breakpointAdded(org.eclipse.debug.core.model.IBreakpoint)
	 */
	public void breakpointAdded(final IBreakpoint breakpoint) {
		if (PTPDebugUIPlugin.isPTPDebugPerspective()) {
			if (breakpoint instanceof ICLineBreakpoint) {
				//delete c breakpoint if the ptp debug perspective is active
				WorkbenchJob uiJob = new WorkbenchJob("Removing CLine breakpoint...") {
					public IStatus runInUIThread(IProgressMonitor monitor) {
						try {
							DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(breakpoint, true);
						} catch (CoreException e) {
							PTPDebugUIPlugin.log(e.getStatus());
						}
						return Status.OK_STATUS;
					}
				};
				uiJob.setSystem(true);
				uiJob.setPriority(Job.DECORATE);
				uiJob.schedule();
			}
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointListener#breakpointRemoved(org.eclipse.debug.core.model.IBreakpoint, org.eclipse.core.resources.IMarkerDelta)
	 */
	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointListener#breakpointChanged(org.eclipse.debug.core.model.IBreakpoint, org.eclipse.core.resources.IMarkerDelta)
	 */
	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {}
	/***************************************************************************************************************************************************************************************************
	 * Element Set
	 **************************************************************************************************************************************************************************************************/
	/** Update breakpoint marker
	 * @param cur_sid current set ID
	 */
	public void updateBreakpointMarker(final String cur_sid) {
		WorkbenchJob uiJob = new WorkbenchJob("Updating breakpoint marker...") {
			public IStatus runInUIThread(IProgressMonitor monitor) {
				try {
					debugModel.updatePBreakpoints(cur_sid,  monitor);
				} catch (CoreException e) {
					return e.getStatus();
				}
				return Status.OK_STATUS;
			}
		};
		uiJob.setSystem(true);
		uiJob.setPriority(Job.INTERACTIVE);
		uiJob.schedule();
	}
	/** Remove all register elements
	 * @param job_id job ID
	 * @throws CoreException
	 */
	public synchronized void removeAllRegisterElements(final String job_id) throws CoreException {
		final IElementHandler elementHandler = getElementHandler(job_id);
		if (elementHandler == null)
			return;
		WorkbenchJob uiJob = new WorkbenchJob("Removing registered processes") {
			public IStatus runInUIThread(IProgressMonitor monitor) {
				IPJob job = findJobById(job_id);
				if (job == null)
					return Status.CANCEL_STATUS;

				IPCDISession session = getDebugSession(job);
				if (session == null)
					return Status.CANCEL_STATUS;

				BitList tasks = session.createEmptyBitList();
				IElement[] registerElements = elementHandler.getRegisteredElements();
				monitor.beginTask("Removing registering processes....", registerElements.length);
				for (int i = 0; i < registerElements.length; i++) {
					IPProcess proc = findProcess(job, registerElements[i].getID());
					if (proc != null) {
						removeConsoleWindow(job, proc);
						tasks.set(registerElements[i].getIDNum());
					}
					monitor.worked(1);
				}
				unregisterProcess(session, tasks, false);
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		uiJob.setPriority(Job.INTERACTIVE);
		PlatformUI.getWorkbench().getProgressService().showInDialog(PTPDebugUIPlugin.getActiveWorkbenchShell(), uiJob);
		uiJob.schedule();
	}
	/** Update register and unregister elements
	 * @param curSet
	 * @param preSet
	 * @param job_id
	 * @throws CoreException
	 */
	public void updateRegisterUnRegisterElements(final IElementSet curSet, final IElementSet preSet, final String job_id) {
		WorkbenchJob uiJob = new WorkbenchJob("Updating registered/unregistered processes") {
			public IStatus runInUIThread(IProgressMonitor monitor) {
				final IElementHandler elementHandler = getElementHandler(job_id);
				if (elementHandler == null)
					return Status.CANCEL_STATUS;

				IPJob job = findJobById(job_id);
				if (job == null)
					return Status.CANCEL_STATUS;

				IPCDISession session = getDebugSession(job);
				if (session == null)
					return Status.CANCEL_STATUS;
				
				BitList regTasks = session.createEmptyBitList();
				BitList unregTasks = session.createEmptyBitList();
				
				IElement[] registerElements = elementHandler.getRegisteredElements();
				monitor.beginTask("Registering process....", registerElements.length);
				for (int i = 0; i<registerElements.length; i++) {
					if (curSet.contains(registerElements[i].getID())) {//check whether the current set contains the registered process or not
						if (curSet.isRootSet() || (preSet != null && !curSet.equals(preSet) && !preSet.contains(registerElements[i].getID()))) {
							IPProcess proc = findProcess(job, registerElements[i].getID());
							if (proc != null) {
								addConsoleWindow(job, proc);
								regTasks.set(registerElements[i].getIDNum());
							}
						}
					} else { //if not unregister it
						IPProcess proc = findProcess(job, registerElements[i].getID());
						if (proc != null) {
							removeConsoleWindow(job, proc);
							unregTasks.set(registerElements[i].getIDNum());
						}
					}
					monitor.worked(1);
				}
				registerProcess(session, regTasks, false);
				unregisterProcess(session, unregTasks, false);
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		uiJob.setPriority(Job.INTERACTIVE);
		PlatformUI.getWorkbench().getProgressService().showInDialog(PTPDebugUIPlugin.getActiveWorkbenchShell(), uiJob);
		uiJob.schedule();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.ui.AbstractUIManager#fireSetEvent(int, org.eclipse.ptp.ui.model.IElement[], org.eclipse.ptp.ui.model.IElementSet, org.eclipse.ptp.ui.model.IElementSet)
	 */
	public synchronized void fireSetEvent(int eventType, IElement[] elements, IElementSet cur_set, IElementSet pre_set) {
		switch (eventType) {
		case CREATE_SET_TYPE:
			BitList created_tasks = new BitList(cur_set.getElementHandler().getSetRoot().size());
			for (int i = 0; i < elements.length; i++) {
				created_tasks.set(convertToInt(elements[i].getID()));
			}
			debugModel.createSet(getCurrentJobId(), cur_set.getID(), created_tasks);
			break;
		case DELETE_SET_TYPE:
			debugModel.deletePBreakpoint(getCurrentJobId(), cur_set.getID());
			debugModel.deleteSet(getCurrentJobId(), cur_set.getID());
			String cur_job_id = getCurrentJobId();
			if (cur_job_id != null && cur_job_id.length() > 0) {
				getJobVariableManager().deleteSet(cur_job_id, cur_set.getID());
			}		
			break;
		case CHANGE_SET_TYPE:
			if (cur_set == null) {
				break;
			}
			annotationMgr.updateAnnotation(cur_set, pre_set);
			updateBreakpointMarker(cur_set.getID());
			updateRegisterUnRegisterElements(cur_set, pre_set, getCurrentJobId());
			updateVariableValueOnChange();
			break;
		case ADD_ELEMENT_TYPE:
			BitList added_tasks = new BitList(cur_set.getElementHandler().getSetRoot().size());
			for (int i = 0; i < elements.length; i++) {
				added_tasks.set(convertToInt(elements[i].getID()));
			}
			debugModel.addTasks(getCurrentJobId(), cur_set.getID(), added_tasks);
			break;
		case REMOVE_ELEMENT_TYPE:
			BitList removed_tasks = new BitList(cur_set.getElementHandler().getSetRoot().size());
			for (int i = 0; i < elements.length; i++) {
				removed_tasks.set(convertToInt(elements[i].getID()));
			}
			debugModel.removeTasks(getCurrentJobId(), cur_set.getID(), removed_tasks);
			break;
		}
		super.fireSetEvent(eventType, elements, cur_set, pre_set);
	}
	/***************************************************************************************************************************************************************************************************
	 * debug actions
	 **************************************************************************************************************************************************************************************************/
	/** Get tasks from given set
	 * @param job_id job ID
	 * @param set_id set ID
	 * @return
	 * @throws CoreException
	 */
	public BitList getTasks(String job_id, String set_id) throws CoreException {
		return debugModel.getTasks(job_id, set_id);
	}
	/** Resume debugger
	 */
	public void resume() {
		resume(getCurrentJobId(), getCurrentSetId());
	}
	/** Resume debugger
	 * @param job_id job ID
	 * @param set_id set ID
	 */
	public void resume(final String job_id, final String set_id) {
		WorkbenchJob uiJob = new WorkbenchJob("PTP Resume...") {
			public IStatus runInUIThread(IProgressMonitor monitor) {
				IPCDISession session = getDebugSession(job_id);
				if (session == null)
					return new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No session found", null);
				try {
					session.resume(getTasks(job_id, set_id));
				} catch (CoreException e) {
					return e.getStatus();
				} catch (PCDIException e) {
					return new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), null);
				}
				return Status.OK_STATUS;
			}
		};
		uiJob.setSystem(true);
		uiJob.setPriority(Job.INTERACTIVE);
		uiJob.schedule();
	}
	/** Suspend debugger
	 */
	public void suspend() {
		suspend(getCurrentJobId(), getCurrentSetId());
	}
	/** Suspend debugger
	 * @param job_id
	 * @param set_id
	 */
	public void suspend(final String job_id, final String set_id) {
		WorkbenchJob uiJob = new WorkbenchJob("PTP Suspend...") {
			public IStatus runInUIThread(IProgressMonitor monitor) {
				IPCDISession session = getDebugSession(job_id);
				if (session == null)
					return new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No session found", null);
				try {
					session.suspend(getTasks(job_id, set_id));
				} catch (CoreException e) {
					return e.getStatus();
				} catch (PCDIException e) {
					return new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), null);
				}
				return Status.OK_STATUS;
			}
		};
		uiJob.setSystem(true);
		uiJob.setPriority(Job.INTERACTIVE);
		uiJob.schedule();
	}
	/** Terminate debugger
	 */
	public void terminate() {
		terminate(getCurrentJobId(), getCurrentSetId());
	}
	/** Terminate debugger
	 * @param job_id
	 * @param set_id
	 */
	public void terminate(final String job_id, final String set_id) {
		WorkbenchJob uiJob = new WorkbenchJob("PTP Terminate...") {
			public IStatus runInUIThread(IProgressMonitor monitor) {
				IPJob job = findJobById(job_id);
				if (isDebugMode(job)) {
					IPCDISession session = getDebugSession(job);
					if (session == null)
						return new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No session found", null);
					try {
						session.stop(getTasks(job_id, set_id));
					} catch (CoreException e) {
						return e.getStatus();
					} catch (PCDIException e) {
						return new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), null);
					}
				} else {
					try {
						terminateAll();
					} catch (CoreException e) {
						return e.getStatus();
					}
				}
				return Status.OK_STATUS;
			}
		};
		uiJob.setSystem(true);
		uiJob.setPriority(Job.INTERACTIVE);
		uiJob.schedule();
	}
	/** Step into debugger
	 */
	public void stepInto() {
		stepInto(getCurrentJobId(), getCurrentSetId());
	}
	/** Step into debugger
	 * @param job_id
	 * @param set_id
	 */
	public void stepInto(final String job_id, final String set_id) {
		WorkbenchJob uiJob = new WorkbenchJob("PTP Step into...") {
			public IStatus runInUIThread(IProgressMonitor monitor) {
				IPCDISession session = getDebugSession(job_id);
				if (session == null)
					return new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No session found", null);
				try {
					BitList tasks = getTasks(job_id, set_id);
					filterRunningTasks(tasks, STEP_INTO);
					session.steppingInto(tasks);
				} catch (CoreException e) {
					return e.getStatus();
				} catch (PCDIException e) {
					return new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), null);
				}
				return Status.OK_STATUS;
			}
		};
		uiJob.setSystem(true);
		uiJob.setPriority(Job.INTERACTIVE);
		uiJob.schedule();
	}
	/** Step over debugger
	 */
	public void stepOver() {
		stepOver(getCurrentJobId(), getCurrentSetId());
	}
	/** Step over debugger
	 * @param job_id
	 * @param set_id
	 */
	public void stepOver(final String job_id, final String set_id) {
		WorkbenchJob uiJob = new WorkbenchJob("PTP Step over...") {
			public IStatus runInUIThread(IProgressMonitor monitor) {
				IPCDISession session = getDebugSession(job_id);
				if (session == null)
					return new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No session found", null);
				try {
					BitList tasks = getTasks(job_id, set_id);
					filterRunningTasks(tasks, STEP_OVER);
					session.steppingOver(tasks);
				} catch (CoreException e) {
					return e.getStatus();
				} catch (PCDIException e) {
					return new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), null);
				}
				return Status.OK_STATUS;
			}
		};
		uiJob.setSystem(true);
		uiJob.setPriority(Job.INTERACTIVE);
		uiJob.schedule();
	}
	/** Step return debugger
	 */
	public void stepReturn(BitList tasks) {
		stepReturn(tasks, getCurrentJobId(), getCurrentSetId());
	}
	/** Step return debugger
	 * @param job_id
	 * @param set_id
	 * @throws CoreException
	 */
	public void stepReturn(final BitList tasks, final String job_id, final String set_id) {
		WorkbenchJob uiJob = new WorkbenchJob("PTP Step over...") {
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (tasks == null) {
					return new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No task selected", null);
				}
				IPCDISession session = getDebugSession(job_id);
				if (session == null)
					return new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No session found", null);
				try {
					//BitList tasks = getTasks(job_id, set_id);
					filterRunningTasks(tasks, STEP_RETURN);
					session.steppingReturn(tasks);
				} catch (PCDIException e) {
					return new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), null);
				}
				return Status.OK_STATUS;
			}
		};
		uiJob.setSystem(true);
		uiJob.setPriority(Job.INTERACTIVE);
		uiJob.schedule();
	}
	public BitList[] filterStepReturnTasks(BitList tasks) {
		return filterStepReturnTasks(tasks, getCurrentJobId());
	}
	public BitList[] filterStepReturnTasks(BitList tasks, String job_id) {
		IPCDISession session = getDebugSession(job_id);
		if (session == null)
			return new BitList[0];
		try {
			return session.getStepReturnTasks(tasks);
		} catch (PCDIException e) {
			return new BitList[0];
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#removeJob(org.eclipse.ptp.core.IPJob)
	 */
	public void removeJob(IPJob job) {
		if (job.isDebug()) {
			debugModel.deletePBreakpoint(job.getID());
			debugModel.deleteJob(job);
			debugModel.shutdownSession(job);
			removeConsoleWindows(job);
		}
		super.removeJob(job);
	}
	
	/**********************************************************
	 * Variable methods
	 **********************************************************/
	public void updateVariableValueOnSuspend(BitList tasks) {
		updateVariableValue(isAutoUpdateVarOnSuspend(), tasks);
	}
	public void updateVariableValueOnChange() {
		updateVariableValue(isAutoUpdateVarOnChange(), null);
	}
	public void updateVariableValue(boolean force, BitList tasks) {
		cleanVariableValue(tasks);
		if (force) {
			updateVariableValue(tasks);
		}
	}
	public void cleanVariableValue(BitList tasks) {
		String jid = getCurrentJobId();
		String sid = getCurrentSetId();
		if (tasks == null) {
			tasks = debugModel.getTasks(jid, sid);
		}
		getJobVariableManager().cleanupJobVariableValues(jid, tasks);
	}
	public void updateVariableValue(BitList tasks) {
		String jid = getCurrentJobId();
		String sid = getCurrentSetId();
		if (tasks == null) {
			tasks = debugModel.getTasks(jid, sid);
		}
		updateVariableValue(jid, sid, tasks);
	}
	public void updateVariableValue(final String jid, final String sid, final BitList tasks) {
		if (jid != null) {
			Job uiJob = new Job("Updating variables...") {
				protected IStatus run(IProgressMonitor monitor) {
					try {
						getJobVariableManager().updateJobVariableValues(jid, sid, tasks, monitor);
					} catch (CoreException e) {
						return e.getStatus();
					}
					return Status.OK_STATUS;
				}
			};
			uiJob.setPriority(Job.INTERACTIVE);
			PlatformUI.getWorkbench().getProgressService().showInDialog(PTPDebugUIPlugin.getActiveWorkbenchShell(), uiJob);
			uiJob.schedule();
		}
	}
	public Object getDebugObject(IPJob job, int task_id) {
		IPCDISession session = getDebugSession(job); 
		if (session != null) {
			IPDebugTarget debugTarget = session.getLaunch().getDebugTarget(task_id);
			if (debugTarget != null) {
				try {
					IThread[] threads = debugTarget.getThreads();
					for (int i=0; i<threads.length; i++) {
						IStackFrame frame = threads[i].getTopStackFrame();
						if (frame != null)
							return frame;
					}
					if (threads.length > 0) {
						return threads[0];
					}
				} catch (DebugException e) {
					return debugTarget;
				}
				return debugTarget;
			}
		}
		return null;
	}
	
	public int getSelectedRegisteredTasks(Object obj) {
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
			 return ((IPDebugTarget)target).getTargetID();
		}
		return -1;
	}
	//For make sure the task in current set is not registered and is not focus on and it is not stepping, otherwise remove this task
	public void filterRunningTasks(BitList tasks, int step) {
		Viewer viewer = null;
		IViewPart part = PTPDebugUIPlugin.getActiveWorkbenchWindow().getActivePage().findView(IDebugUIConstants.ID_DEBUG_VIEW);
		if (part != null && part instanceof AbstractDebugView) {
			viewer = ((AbstractDebugView)part).getViewer();
		}
		if (viewer != null) {
			ISelection selection = viewer.getSelection();
			if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				if (obj instanceof IStep && obj instanceof IPDebugElement) {
					int taskID = ((IPDebugTarget)((IPDebugElement)obj).getDebugTarget()).getTargetID();
					if (tasks.get(taskID)) {
						switch (step) {
						case STEP_INTO:
							if (!((IStep)obj).canStepInto()) {
								tasks.clear(taskID);
							}
							break;
						case STEP_OVER:
							if (!((IStep)obj).canStepOver()) {
								tasks.clear(taskID);
							}
							break;
						case STEP_RETURN:
							if (!((IStep)obj).canStepReturn()) {
								tasks.clear(taskID);
							}
							break;
						}
					}
				}
			}
		}
	}
}
