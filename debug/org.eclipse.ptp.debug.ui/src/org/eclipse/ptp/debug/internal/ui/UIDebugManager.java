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

import java.util.HashMap;
import java.util.Map;
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
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.internal.ui.viewers.AsynchronousTreeViewer;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.IPDebugConstants;
import org.eclipse.ptp.debug.core.PCDIDebugModel;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.ProcessInputStream;
import org.eclipse.ptp.debug.core.cdi.IPCDISession;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.model.IPDebugTarget;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.debug.ui.model.DebugElement;
import org.eclipse.ptp.internal.ui.JobManager;
import org.eclipse.ptp.ui.OutputConsole;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.ptp.ui.model.IElementSet;
import org.eclipse.swt.widgets.TreeItem;
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
	private PJobVariableManager jobMgr = new PJobVariableManager();	
	private PAnnotationManager annotationMgr = null;
	private PCDIDebugModel debugModel = null;
	private Map consoleWindows = new HashMap();

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
		PTPDebugUIPlugin.getDefault().getPluginPreferences().addPropertyChangeListener(propertyChangeListener);
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
		annotationMgr.shutdown();
		jobMgr.shutdown();
		super.shutdown();
	}
	protected IElement createElement(IElementSet set, String key, String name) {
		return new DebugElement(set, key, name);
	}
	
	/** Get value text for tooltip
	 * @param taskID
	 * @return
	 */
	public String getValueText(int taskID) {
		return getJobVariableManager().getResultDisplay(getCurrentJobId(), taskID);
	}
	
	/** Register process 0 if default registeris true 
	 * @param session
	 */
	public void defaultRegister(IPCDISession session) { // register process 0 if the preference is checked
		if (prefRegisterProc0) {
			IPProcess proc = session.getJob().findProcessByTaskId(0);
			if (proc != null)
				registerProcess(session, proc, true);
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
		return (job != null && !job.isAllStop());
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
		IPJob job = findJobById(job_id);
		if (job == null)
			return null;
		return getDebugSession(job);
	}
	/** Get debug session
	 * @param job
	 * @return
	 */
	public IPCDISession getDebugSession(IPJob job) {
		return debugModel.getPCDISession(job.getIDString());
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
	private void addConsoleWindow(IPProcess proc) {
		consoleWindows.put(proc, new OutputConsole(proc.getElementName(), new ProcessInputStream(proc)));
	}
	/** Remove process from console list and close its output
	 * @param proc
	 */
	private void removeConsoleWindow(IPProcess proc) {
		OutputConsole outputConsole = (OutputConsole) consoleWindows.remove(proc);
		if (outputConsole != null) {
			outputConsole.kill();
		}
	}
	/***************************************************************************************************************************************************************************************************
	 * Register / Unregister
	 **************************************************************************************************************************************************************************************************/
	/** Register process
	 * @param session
	 * @param proc
	 * @param isChanged
	 */
	public void registerProcess(IPCDISession session, IPProcess proc, boolean isChanged) {
		session.registerTarget(proc.getTaskId(), isChanged);
		addConsoleWindow(proc);
	}
	/** Unregister process
	 * @param session
	 * @param proc
	 * @param isChanged
	 */
	public void unregisterProcess(IPCDISession session, IPProcess proc, boolean isChanged) {
		session.unregisterTarget(proc.getTaskId(), isChanged);
		removeConsoleWindow(proc);
	}
	/** Unregister elements
	 * @param elements
	 * @throws CoreException
	 */
	public void unregisterElements(IElement[] elements) throws CoreException {
		IPJob job = getCurrentJob();
		if (job == null)
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No job found", null));
		IPCDISession session = getDebugSession(job);
		if (session == null)
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No session found", null));
		for (int i = 0; i < elements.length; i++) {
			// only unregister some registered elements
			if (elements[i].isRegistered()) {
				IPProcess process = findProcess(job, elements[i].getIDNum());
				if (process != null)
					unregisterProcess(session, process, true);
			}
		}
	}
	/** Register elements
	 * @param elements
	 * @throws CoreException
	 */
	public void registerElements(IElement[] elements) throws CoreException {
		IPJob job = getCurrentJob();
		if (job == null)
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No job found", null));
		IPCDISession session = getDebugSession(job);
		if (session == null)
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No session found", null));
		for (int i = 0; i < elements.length; i++) {
			// only register some unregistered elements
			if (!elements[i].isRegistered()) {
				IPProcess process = findProcess(job, elements[i].getIDNum());
				if (process != null && !process.isAllStop())
					registerProcess(session, process, true);
			}
		}
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
				Job uiJob = new Job("Removing CLine breakpoint...") {
					protected IStatus run(IProgressMonitor monitor) {
						try {
							DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(breakpoint, true);
						} catch (CoreException e) {
							PTPDebugUIPlugin.log(e.getStatus());
						}
						return Status.OK_STATUS;
					}
				};
				uiJob.setSystem(true);
				uiJob.setPriority(Job.BUILD);
				//set delete breakpoint job later to prevent the breakpoint didn't finished the completion of adding
				uiJob.schedule(100);
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
	public void removeAllRegisterElements(final String job_id) throws CoreException {
		final IElementHandler elementHandler = getElementHandler(job_id);
		if (elementHandler == null)
			return;
		Job uiJob = new Job("Removing registered processes") {
			protected IStatus run(IProgressMonitor monitor) {
				IPCDISession session = getDebugSession(job_id);
				if (session == null)
					return Status.CANCEL_STATUS;
				IElement[] registerElements = elementHandler.getRegisteredElements();
				monitor.beginTask("Removing registering processes....", registerElements.length);
				for (int i = 0; i < registerElements.length; i++) {
					IPProcess proc = findProcess(job_id, registerElements[i].getIDNum());
					if (proc != null) {
						unregisterProcess(session, proc, false);
					}
					monitor.worked(1);
				}
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
	public void updateRegisterUnRegisterElements(final IElementSet curSet, final IElementSet preSet, final String job_id) throws CoreException {
		final IElementHandler elementHandler = getElementHandler(job_id);
		if (elementHandler == null)
			return;
		Job uiJob = new Job("Updating registered/unregistered processes") {
			protected IStatus run(IProgressMonitor pmonitor) {
				IPCDISession session = getDebugSession(job_id);
				if (session == null)
					return Status.CANCEL_STATUS;
				
				IElement[] registerElements = elementHandler.getRegisteredElements();
				pmonitor.beginTask("Registering process....", registerElements.length);
				for (int i = 0; i<registerElements.length; i++) {
					if (curSet.contains(registerElements[i].getID())) {
						if (curSet.isRootSet() || (preSet != null && !curSet.equals(preSet) && !preSet.contains(registerElements[i].getID()))) {
							IPProcess proc = findProcess(job_id, registerElements[i].getIDNum());
							if (proc != null)
								registerProcess(session, proc, false);
						}
					} else {
						IPProcess proc = findProcess(job_id, registerElements[i].getIDNum());
						if (proc != null)
							unregisterProcess(session, proc, false);
					}
					pmonitor.worked(1);
				}
				pmonitor.done();
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
	public void fireSetEvent(int eventType, IElement[] elements, IElementSet cur_set, IElementSet pre_set) {
		switch (eventType) {
		case CREATE_SET_TYPE:
			BitList created_tasks = new BitList(cur_set.getElementHandler().getSetRoot().size());
			for (int i = 0; i < elements.length; i++) {
				created_tasks.set(convertToInt(elements[i].getID()));
			}
			try {
				debugModel.createSet(getCurrentJobId(), cur_set.getID(), created_tasks);
			} catch (CoreException e) {
				PTPDebugUIPlugin.log(e);
			}
			break;
		case DELETE_SET_TYPE:
			try {
				debugModel.deletePBreakpoint(getCurrentJobId(), cur_set.getID());
			} catch (CoreException e) {
				PTPDebugUIPlugin.log(e);
			}
			try {
				debugModel.deleteSet(getCurrentJobId(), cur_set.getID());
			} catch (CoreException e) {
				PTPDebugUIPlugin.log(e);
			}
			String cur_job_id = getCurrentJobId();
			if (cur_job_id != null && cur_job_id.length() > 0) {
				getJobVariableManager().deleteSet(cur_job_id, cur_set.getID());
			}		
			break;
		case CHANGE_SET_TYPE:
			if (cur_set == null) {
				break;
			}
			updateBreakpointMarker(cur_set.getID());
			try {
				updateRegisterUnRegisterElements(cur_set, pre_set, getCurrentJobId());
				annotationMgr.updateAnnotation(cur_set, pre_set);
			} catch (CoreException e) {
				PTPDebugUIPlugin.log(e);
			}

			updateVariableValueOnChange();
			break;
		case ADD_ELEMENT_TYPE:
			BitList added_tasks = new BitList(cur_set.getElementHandler().getSetRoot().size());
			for (int i = 0; i < elements.length; i++) {
				added_tasks.set(convertToInt(elements[i].getID()));
			}
			try {
				debugModel.addTasks(getCurrentJobId(), cur_set.getID(), added_tasks);
			} catch (CoreException e) {
				PTPDebugUIPlugin.log(e);
			}
			break;
		case REMOVE_ELEMENT_TYPE:
			BitList removed_tasks = new BitList(cur_set.getElementHandler().getSetRoot().size());
			for (int i = 0; i < elements.length; i++) {
				removed_tasks.set(convertToInt(elements[i].getID()));
			}
			try {
				debugModel.removeTasks(getCurrentJobId(), cur_set.getID(), removed_tasks);
			} catch (CoreException e) {
				PTPDebugUIPlugin.log(e);
			}
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
	 * @throws CoreException
	 */
	public void resume() throws CoreException {
		resume(getCurrentJobId(), getCurrentSetId());
	}
	/** Resume debugger
	 * @param job_id job ID
	 * @param set_id set ID
	 * @throws CoreException
	 */
	public void resume(String job_id, String set_id) throws CoreException {
		IPCDISession session = getDebugSession(job_id);
		if (session == null)
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No session found", null));
		try {
			session.resume(getTasks(job_id, set_id));
		} catch (PCDIException e) {
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), null));
		}
	}
	/** Suspend debugger
	 * @throws CoreException
	 */
	public void suspend() throws CoreException {
		suspend(getCurrentJobId(), getCurrentSetId());
	}
	/** Suspend debugger
	 * @param job_id
	 * @param set_id
	 * @throws CoreException
	 */
	public void suspend(String job_id, String set_id) throws CoreException {
		IPCDISession session = getDebugSession(job_id);
		if (session == null)
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No session found", null));
		try {
			session.suspend(getTasks(job_id, set_id));
		} catch (PCDIException e) {
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), null));
		}
	}
	/** Terminate debugger
	 * @throws CoreException
	 */
	public void terminate() throws CoreException {
		terminate(getCurrentJobId(), getCurrentSetId());
	}
	/** Terminate debugger
	 * @param job_id
	 * @param set_id
	 * @throws CoreException
	 */
	public void terminate(String job_id, String set_id) throws CoreException {
		IPJob job = findJobById(job_id);
		if (isDebugMode(job)) {
			IPCDISession session = (IPCDISession) getDebugSession(job);
			if (session == null)
				throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No session found", null));
			try {
				session.stop(getTasks(job_id, set_id));
			} catch (PCDIException e) {
				throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), null));
			}
		} else {
			super.terminateAll(job_id);
		}
	}
	/** Step into debugger
	 * @throws CoreException
	 */
	public void stepInto() throws CoreException {
		stepInto(getCurrentJobId(), getCurrentSetId());
	}
	/** Step into debugger
	 * @param job_id
	 * @param set_id
	 * @throws CoreException
	 */
	public void stepInto(String job_id, String set_id) throws CoreException {
		IPCDISession session = getDebugSession(job_id);
		if (session == null)
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No session found", null));
		try {
			session.steppingInto(getTasks(job_id, set_id));
		} catch (PCDIException e) {
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), null));
		}
	}
	/** Step over debugger
	 * @throws CoreException
	 */
	public void stepOver() throws CoreException {
		stepOver(getCurrentJobId(), getCurrentSetId());
	}
	/** Step over debugger
	 * @param job_id
	 * @param set_id
	 * @throws CoreException
	 */
	public void stepOver(String job_id, String set_id) throws CoreException {
		IPCDISession session = getDebugSession(job_id);
		if (session == null)
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No session found", null));
		try {
			session.steppingOver(getTasks(job_id, set_id));
		} catch (PCDIException e) {
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), null));
		}
	}
	/** Step return debugger
	 * @throws CoreException
	 */
	public void stepReturn() throws CoreException {
		stepReturn(getCurrentJobId(), getCurrentSetId());
	}
	/** Step return debugger
	 * @param job_id
	 * @param set_id
	 * @throws CoreException
	 */
	public void stepReturn(String job_id, String set_id) throws CoreException {
		IPCDISession session = getDebugSession(job_id);
		if (session == null)
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No session found", null));
		try {
			session.steppingReturn(getTasks(job_id, set_id));
		} catch (PCDIException e) {
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), null));
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IManager#removeJob(org.eclipse.ptp.core.IPJob)
	 */
	public void removeJob(IPJob job) {
		if (job.isDebug()) {
			try {
				debugModel.deletePBreakpoint(job.getIDString());
			} catch (CoreException e) {
				PTPDebugUIPlugin.log(e);
			}
			debugModel.deleteJob(job);
			debugModel.shutdownSession(job);
		}
		super.removeJob(job);
	}
	
	/**********************************************************
	 * Variable methods
	 **********************************************************/
	public void updateVariableValueOnSuspend() {
		updateVariableValue(isAutoUpdateVarOnSuspend());
	}
	public void updateVariableValueOnChange() {
		updateVariableValue(isAutoUpdateVarOnChange());
	}
	public void updateVariableValue(boolean force) {
		cleanVariableValue();
		if (force) {
			updateVariableValue();
		}
	}
	public void updateVariableValue() {
		updateVariableValue(getCurrentJobId(), getCurrentSetId());
	}
	public void updateVariableValue(final String jid, final String sid) {
		if (jid != null) {
			Job uiJob = new Job("Updating variables...") {
				protected IStatus run(IProgressMonitor monitor) {
					try {
						getJobVariableManager().updateJobVariableValues(jid, sid, monitor);
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
	public void cleanVariableValue() {
		getJobVariableManager().cleanupJobVariableValues();
	}
	
	/******************************************************
	 * TODO redevelop the focus on debug target on debug view 
	 ******************************************************/
	public void focusOnDebugTarget(IPJob job, int task_id) {
		Object debugObj = getDebugObject(job, task_id);
		if (debugObj != null) {
			focusOnDebugView(debugObj);
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
				} catch (DebugException e) {
					return debugTarget;
				}
				return debugTarget;
			}
		}
		return null;
	}
	private TreeItem getTreeItem(TreeItem[] items, IDebugTarget target, IStackFrame frame) {
		for (int i=0; i<items.length; i++) {
			if (!items[i].getExpanded()) {
				items[i].setExpanded(true);
			}
			Object obj = items[i].getData();
			if (obj instanceof ILaunch) {
				return getTreeItem(items[i].getItems(), target, frame);
			}
			else if (obj instanceof IDebugTarget) {
				if (obj.equals(target)) {
					return getTreeItem(items[i].getItems(), target, frame);
				}
				continue;
			}
			else if (obj instanceof IStackFrame) {
				if (obj.equals(frame)) {
					return items[i];
				}
			}
			else {
				return getTreeItem(items[i].getItems(), target, frame);
			}
			return items[i];
		}
		return null;
	}
	private void doOnFocusDebugView(Object selection) {
		IViewPart part = PTPDebugUIPlugin.getActiveWorkbenchWindow().getActivePage().findView(IDebugUIConstants.ID_DEBUG_VIEW);
		if (part != null && part instanceof IDebugView) {
			Viewer viewer = ((IDebugView)part).getViewer();
			if (viewer != null) {
				IDebugTarget target = null;
				IStackFrame frame = null;
				if (selection instanceof IDebugTarget) {
					target = (IDebugTarget)selection;
				}
				else if (selection instanceof IStackFrame) {
					frame = (IStackFrame)selection;
					target = frame.getDebugTarget();
				}
				TreeItem item = getTreeItem(((AsynchronousTreeViewer)viewer).getTree().getItems(), target, frame);
				if (item != null) {
					item.getParent().setSelection(item);
				}
				//viewer.setSelection(new StructuredSelection(selection), true);
				//Changed to TreeSelection since 3.2
				//((AsynchronousViewer)viewer).setSelection(new TreeSelection(getFocusTreePath(selection)), true, true);
				//part.setFocus();
			}
		}
	}
	/*
	private TreePath getFocusTreePath(Object selection) {
		if (selection instanceof IDebugTarget) {
			return new TreePath(new Object[] { selection} ); 
		}
		if (selection instanceof IStackFrame) {
			return new TreePath(new Object[] { ((IStackFrame)selection).getThread(), selection });
		}
		return new TreePath(new Object[0]);
	}
	*/
	public void focusOnDebugView(final Object selection) {
		if (selection == null) {
			return;
		}

		if (PTPDebugUIPlugin.getDisplay().getThread() == Thread.currentThread()) {
			doOnFocusDebugView(selection);
		} else {
			WorkbenchJob job = new WorkbenchJob("Focus on Debug View") {
				public IStatus runInUIThread(IProgressMonitor monitor) {
					doOnFocusDebugView(selection);
					return Status.OK_STATUS;
				}
			};
			job.setSystem(true);
			job.schedule();
		}
	}
}
