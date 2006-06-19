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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.core.cdi.ICDILocator;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
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
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.PreferenceConstants;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.IAbstractDebugger;
import org.eclipse.ptp.debug.core.IPDebugConstants;
import org.eclipse.ptp.debug.core.PCDIDebugModel;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.ProcessInputStream;
import org.eclipse.ptp.debug.core.cdi.IPCDIErrorInfo;
import org.eclipse.ptp.debug.core.cdi.IPCDILineLocation;
import org.eclipse.ptp.debug.core.cdi.IPCDISession;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIDebugDestroyedEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIErrorEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIEventListener;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIExitedEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIResumedEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDISuspendedEvent;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIBreakpoint;
import org.eclipse.ptp.debug.core.cdi.model.IPCDILocationBreakpoint;
import org.eclipse.ptp.debug.core.cdi.model.IPCDILocator;
import org.eclipse.ptp.debug.core.launch.IPLaunchEvent;
import org.eclipse.ptp.debug.core.launch.IPLaunchListener;
import org.eclipse.ptp.debug.core.launch.PDebugTargetRegisterEvent;
import org.eclipse.ptp.debug.core.launch.PDebugTargetUnRegisterEvent;
import org.eclipse.ptp.debug.core.launch.PLaunchStartedEvent;
import org.eclipse.ptp.debug.core.model.IPDebugTarget;
import org.eclipse.ptp.debug.core.model.IPVariableManager;
import org.eclipse.ptp.debug.external.core.cdi.BreakpointHitInfo;
import org.eclipse.ptp.debug.external.core.cdi.EndSteppingRangeInfo;
import org.eclipse.ptp.debug.external.core.cdi.event.BreakpointCreatedEvent;
import org.eclipse.ptp.debug.external.core.cdi.event.BreakpointHitEvent;
import org.eclipse.ptp.debug.external.core.cdi.event.EndSteppingRangeEvent;
import org.eclipse.ptp.debug.external.core.cdi.event.InferiorSignaledEvent;
import org.eclipse.ptp.debug.internal.ui.actions.UpdateVariablesActionDelegate;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.debug.ui.events.IDebugActionEvent;
import org.eclipse.ptp.debug.ui.events.ResumedDebugEvent;
import org.eclipse.ptp.debug.ui.events.SuspendedDebugEvent;
import org.eclipse.ptp.debug.ui.events.TerminatedDebugEvent;
import org.eclipse.ptp.debug.ui.listeners.IDebugActionUpdateListener;
import org.eclipse.ptp.debug.ui.listeners.IRegListener;
import org.eclipse.ptp.internal.ui.JobManager;
import org.eclipse.ptp.ui.OutputConsole;
import org.eclipse.ptp.ui.UIUtils;
import org.eclipse.ptp.ui.listeners.ISetListener;
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
public class UIDebugManager extends JobManager implements ISetListener, IBreakpointListener, IPCDIEventListener, IPLaunchListener {
	private final static int REG_TYPE = 1;
	private final static int UNREG_TYPE = 2;
	private List regListeners = new ArrayList();
	private List debugEventListeners = new ArrayList();
	private PAnnotationManager annotationMgr = null;
	private PCDIDebugModel debugModel = null;
	private Map consoleWindows = new HashMap();
	private IPVariableManager variableManager = null;

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

	/** Constructor
	 * 
	 */
	public UIDebugManager() {
		PTPDebugUIPlugin.getDefault().getPluginPreferences().addPropertyChangeListener(propertyChangeListener);
		addSetListener(this);
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
		debugModel = PTPDebugCorePlugin.getDebugModel();
		variableManager = PTPDebugCorePlugin.getPVariableManager();
		debugModel.addLaunchListener(this);
		annotationMgr = new PAnnotationManager(this);
		settingPreference();
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
		removeSetListener(this);
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
		regListeners.clear();
		debugModel.removeLaunchListener(this);
		annotationMgr.shutdown();
		super.shutdown();
	}
	/** Get variable manager
	 * @return
	 */
	public IPVariableManager getPVariableManager() {
		return variableManager;
	}
	/** Get value text for tooltip
	 * @param job
	 * @param taskID
	 * @return
	 */
	public String getValueText(IPJob job, int taskID) {
		return variableManager.getResultDisplay(job, taskID);
	}
	
	/** Register process 0 if default registeris true 
	 * @param session
	 */
	private void defaultRegister(IPCDISession session) { // register process 0 if the preference is checked
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
	 * @see org.eclipse.ptp.internal.ui.JobManager#fireJobChangeEvent(java.lang.String, java.lang.String)
	 */
	public void fireJobChangeEvent(String cur_jid, String pre_jid) {
		updateBreakpointMarker(IElementHandler.SET_ROOT_ID);
		try {
			removeAllRegisterElements(pre_jid);
		} catch (CoreException e) {
			PTPDebugUIPlugin.log(e);
		}
		super.fireJobChangeEvent(cur_jid, pre_jid);
	}
	/** Add debug event listener
	 * @param listener
	 */
	public void addDebugEventListener(IDebugActionUpdateListener listener) {
		if (!debugEventListeners.contains(listener))
			debugEventListeners.add(listener);
	}
	/** Remove debug listener
	 * @param listener
	 */
	public void removeDebugEventListener(IDebugActionUpdateListener listener) {
		if (debugEventListeners.contains(listener))
			debugEventListeners.remove(listener);
	}
	/** Fire debug event
	 * @param event
	 */
	public void fireDebugEvent(final IDebugActionEvent event) {
		for (Iterator i = debugEventListeners.iterator(); i.hasNext();) {
			final IDebugActionUpdateListener dListener = (IDebugActionUpdateListener) i.next();
			SafeRunner.run(new SafeNotifier() {
				public void run() {
					dListener.handleDebugActionEvent(event);
				}
			});
		}
	}
	/** Add register listener
	 * @param listener
	 */
	public void addRegListener(IRegListener listener) {
		if (!regListeners.contains(listener))
			regListeners.add(listener);
	}
	/** Remove register listener
	 * @param listener
	 */
	public void removeRegListener(IRegListener listener) {
		if (regListeners.contains(listener))
			regListeners.remove(listener);
	}
	/** Fire register listener
	 * @param type
	 * @param tasks
	 */
	public synchronized void fireRegListener(final int type, final BitList tasks) {
		Job uiJob = new Job("Firing register / unregister listener") {
			protected IStatus run(IProgressMonitor monitor) {
				for (Iterator i = regListeners.iterator(); i.hasNext();) {
					final IRegListener rListener = (IRegListener) i.next();
					switch (type) {
					case REG_TYPE:
						rListener.register(tasks);
					break;
					case UNREG_TYPE:
						rListener.unregister(tasks);
					}
				}
				return Status.OK_STATUS;
			}
		};
		uiJob.setPriority(Job.INTERACTIVE);
		uiJob.schedule();
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
		return debugModel.getPCDISession(job);
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
	 * Launch Listener
	 **************************************************************************************************************************************************************************************************/
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.launch.IPLaunchListener#handleLaunchEvent(org.eclipse.ptp.debug.core.launch.IPLaunchEvent)
	 */
	public synchronized void handleLaunchEvent(IPLaunchEvent event) {
		IPJob job = event.getJob();
		if (event instanceof PLaunchStartedEvent) {
			addJob(job);			
			getDebugSession(job).getEventManager().addEventListener(this);
			defaultRegister(getDebugSession(job));
		} else if (event instanceof PDebugTargetRegisterEvent) {
			IElementHandler elementHandler = getElementHandler(job.getIDString());
			BitList tasks = ((PDebugTargetRegisterEvent) event).getTasks();
			int[] processes = tasks.toArray();
			for (int j = 0; j < processes.length; j++) {
				IPProcess proc = job.findProcessByTaskId(processes[j]);
				IElement element = elementHandler.getSetRoot().get(proc.getIDString());
				element.setRegistered(true);
				elementHandler.addRegisterElement(element);
			}
			//focus on the registered debug target.  If registered targets are more than one, focus on the first one.
			if (processes.length > 0) {
				focusOnDebugTarget(job, processes[0]);				
			}
			fireRegListener(REG_TYPE, tasks);
		} else if (event instanceof PDebugTargetUnRegisterEvent) {
			IElementHandler elementHandler = getElementHandler(job.getIDString());
			BitList tasks = ((PDebugTargetUnRegisterEvent) event).getTasks();
			int[] processes = tasks.toArray();
			for (int j = 0; j < processes.length; j++) {
				IPProcess proc = job.findProcessByTaskId(processes[j]);
				IElement element = elementHandler.getSetRoot().get(proc.getIDString());
				element.setRegistered(false);
				elementHandler.removeRegisterElement(element);
			}
			fireRegListener(UNREG_TYPE, tasks);
		}
		// firePaintListener(null);
	}
	/** Focus on debug target
	 * @param job
	 * @param task_id
	 */
	public void focusOnDebugTarget(IPJob job, int task_id) {
		Object debugObj = getDebugObject(job, task_id);
		if (debugObj != null) {
			focusOnDebugView(debugObj);
		}
	}
	/** Get debug object
	 * @param job
	 * @param task_id
	 * @return
	 */
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
	private void doOnFocusDebugView(Object selection) {
		IViewPart part = UIUtils.findView(IDebugUIConstants.ID_DEBUG_VIEW);
		if (part != null && part instanceof IDebugView) {
			Viewer viewer = ((IDebugView)part).getViewer();
			if (viewer != null) {
				//viewer.setSelection(new StructuredSelection(selection), true);
				//Changed to TreeSelection since 3.2
				//viewer.setSelection(new TreeSelection(getFocusTreePath(selection)), true);
				part.setFocus();
			}
		}
	}
	private TreePath getFocusTreePath(Object selection) {
		if (selection instanceof IDebugTarget) {
			return new TreePath(new Object[] { selection} ); 
		}
		if (selection instanceof IStackFrame) {
			return new TreePath(new Object[] { ((IStackFrame)selection).getThread(), selection });
		}
		return new TreePath(new Object[0]);
	}
	/** Focus on debug view
	 * @param selection
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
				IPProcess process = findProcess(job, elements[i].getID());
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
				IPProcess process = findProcess(job, elements[i].getID());
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
	public void breakpointAdded(IBreakpoint breakpoint) {
		if (PTPDebugUIPlugin.isPTPDebugPerspective()) {
			if (breakpoint instanceof ICLineBreakpoint) {
				try {
					DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(breakpoint, true);
				} catch (CoreException e) {
					System.out.println("Err: " + e.getMessage());
				}
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
	public void updateBreakpointMarker(String cur_sid) {
		try {
			debugModel.updatePBreakpoints(cur_sid);
		} catch (CoreException e) {
			PTPDebugUIPlugin.log(e);
		}
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
			protected IStatus run(IProgressMonitor pmonitor) {
				IPCDISession session = getDebugSession(job_id);
				if (session == null)
					return Status.CANCEL_STATUS;
				IElement[] registerElements = elementHandler.getRegisteredElements();
				pmonitor.beginTask("Removing registering processes....", registerElements.length);
				for (int i = 0; i < registerElements.length; i++) {
					IPProcess proc = findProcess(job_id, registerElements[i].getID());
					if (proc != null) {
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
							IPProcess proc = findProcess(job_id, registerElements[i].getID());
							if (proc != null)
								registerProcess(session, proc, false);
						}
					} else {
						IPProcess proc = findProcess(job_id, registerElements[i].getID());
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
	 * @see org.eclipse.ptp.ui.listeners.ISetListener#changeSetEvent(org.eclipse.ptp.ui.model.IElementSet, org.eclipse.ptp.ui.model.IElementSet)
	 */
	public void changeSetEvent(IElementSet curSet, IElementSet preSet) {
		if (curSet == null)
			return;
		updateBreakpointMarker(curSet.getID());
		try {
			updateRegisterUnRegisterElements(curSet, preSet, getCurrentJobId());
			annotationMgr.updateAnnotation(curSet, preSet);
		} catch (CoreException e) {
			PTPDebugUIPlugin.log(e);
		}
		updateDebugVariables();
	}
	/* (non-Javadoc)
	 * Delete the set that will delete the all related breakpoint
	 * @see org.eclipse.ptp.ui.listeners.ISetListener#deleteSetEvent(org.eclipse.ptp.ui.model.IElementSet)
	 */
	public void deleteSetEvent(IElementSet set) {
		try {
			debugModel.deletePBreakpointBySet(getCurrentJobId(), set.getID());
		} catch (CoreException e) {
			PTPDebugUIPlugin.log(e);
		}
		try {
			debugModel.deleteSet(getCurrentJobId(), set.getID());
		} catch (CoreException e) {
			PTPDebugUIPlugin.log(e);
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.listeners.ISetListener#createSetEvent(org.eclipse.ptp.ui.model.IElementSet, org.eclipse.ptp.ui.model.IElement[])
	 */
	public void createSetEvent(IElementSet set, IElement[] elements) {
		BitList tasks = new BitList(set.getElementHandler().getSetRoot().size());
		for (int i = 0; i < elements.length; i++) {
			tasks.set(convertToInt(elements[i].getName()));
		}
		try {
			debugModel.createSet(getCurrentJobId(), set.getID(), tasks);
		} catch (CoreException e) {
			PTPDebugUIPlugin.log(e);
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.listeners.ISetListener#addElementsEvent(org.eclipse.ptp.ui.model.IElementSet, org.eclipse.ptp.ui.model.IElement[])
	 */
	public void addElementsEvent(IElementSet set, IElement[] elements) {
		BitList tasks = new BitList(set.getElementHandler().getSetRoot().size());
		for (int i = 0; i < elements.length; i++) {
			tasks.set(convertToInt(elements[i].getName()));
		}
		try {
			debugModel.addTasks(getCurrentJobId(), set.getID(), tasks);
		} catch (CoreException e) {
			PTPDebugUIPlugin.log(e);
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.listeners.ISetListener#removeElementsEvent(org.eclipse.ptp.ui.model.IElementSet, org.eclipse.ptp.ui.model.IElement[])
	 */
	public void removeElementsEvent(IElementSet set, IElement[] elements) {
		BitList tasks = new BitList(set.getElementHandler().getSetRoot().size());
		for (int i = 0; i < elements.length; i++) {
			tasks.set(convertToInt(elements[i].getName()));
		}
		try {
			debugModel.removeTasks(getCurrentJobId(), set.getID(), tasks);
		} catch (CoreException e) {
			PTPDebugUIPlugin.log(e);
		}
	}
	/***************************************************************************************************************************************************************************************************
	 * Event
	 **************************************************************************************************************************************************************************************************/
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.cdi.event.IPCDIEventListener#handleDebugEvents(org.eclipse.ptp.debug.core.cdi.event.IPCDIEvent[])
	 */
	public void handleDebugEvents(final IPCDIEvent[] events) {
		Job uiJob = new Job("Updating debug events") {
			protected IStatus run(IProgressMonitor monitor) {
				handleDebugEvents(events, monitor);
				return Status.OK_STATUS;
			}
		};
		uiJob.setPriority(Job.INTERACTIVE);
		uiJob.schedule();
	}
	/** Handle debug events
	 * @param events
	 * @param monitor
	 */
	private synchronized void handleDebugEvents(IPCDIEvent[] events, IProgressMonitor monitor) {
		for (int i = 0; i < events.length; i++) {
			Object condition = null;
			IPCDIEvent event = events[i];
			System.out.println("===================== event: " + event);
			// all events must be running under a job
			IPJob job = event.getDebugJob();
			if (job == null)
				continue;
			String workingDebugDir = (String) job.getAttribute(PreferenceConstants.JOB_DEBUG_DIR);
			/*
			if (event instanceof TargetRegisteredEvent) {
				IElementHandler elementHandler = getElementHandler(job.getIDString());
				int[] processes = event.getAllProcesses().toArray();
				for (int j = 0; j < processes.length; j++) {
					IPProcess proc = job.findProcessByTaskId(processes[j]);
					elementHandler.addRegisterElement(proc.getIDString());
					elementHandler.getSetRoot().get(proc.getIDString()).setRegistered(true);
				}
				fireRegListener(REG_TYPE, event.getAllRegisteredProcesses());
			} else if (event instanceof TargetUnregisteredEvent) {
				IElementHandler elementHandler = getElementHandler(job.getIDString());
				int[] processes = event.getAllProcesses().toArray();
				for (int j = 0; j < processes.length; j++) {
					IPProcess proc = job.findProcessByTaskId(processes[j]);
					elementHandler.removeRegisterElement(proc.getIDString());
					elementHandler.getSetRoot().get(proc.getIDString()).setRegistered(false);
				}
				fireRegListener(UNREG_TYPE, event.getAllUnregisteredProcesses());
			}
			*/
			if (event instanceof IPCDISuspendedEvent) {
				int lineNumber = 0;
				String fileName = workingDebugDir + "/";
				if (event instanceof BreakpointHitEvent) {
					IPCDIBreakpoint bpt = ((BreakpointHitInfo) ((BreakpointHitEvent) event).getReason()).getBreakpoint();
					if (bpt instanceof IPCDILocationBreakpoint) {
						IPCDILocator locator = ((IPCDILocationBreakpoint) bpt).getLocator();
						lineNumber = locator.getLineNumber();
						fileName += locator.getFile();
					}
				} 
				else if (event instanceof EndSteppingRangeEvent) {
					IPCDILineLocation lineLocation = ((EndSteppingRangeInfo) ((EndSteppingRangeEvent) event).getReason()).getLineLocation();
					if (lineLocation != null) {
						lineNumber = lineLocation.getLineNumber();
						fileName += lineLocation.getFile();
					}
				}
				else {
					ICDILocator locator = ((InferiorSignaledEvent) event).getLocator();
					if (locator != null) {
						lineNumber = locator.getLineNumber();
						fileName += locator.getFile();
					}
				}
				if (lineNumber == 0)
					lineNumber = 1;
				try {					
					annotationMgr.addAnnotation(job.getIDString(), fileName, lineNumber, event.getAllUnregisteredProcesses(), false);
					annotationMgr.addAnnotation(job.getIDString(), fileName, lineNumber, event.getAllRegisteredProcesses(), true);
				} catch (final CoreException e) {
					PTPDebugUIPlugin.getDisplay().asyncExec(new Runnable() {
						public void run() {
							PTPDebugUIPlugin.errorDialog("Error", e);
						}
					});
				}
				updateDebugVariablesOnSuspend(job);
				fireSuspendEvent(job, event.getAllProcesses());
			} else if (event instanceof IPCDIResumedEvent) {
				removeAnnotation(job.getIDString(), event.getAllProcesses());
				// System.out.println("-------------------- resume ------------------------");
				cleanupDebugVariables(job);
				fireResumeEvent(job, event.getAllProcesses());
			} else if (event instanceof IPCDIDebugDestroyedEvent) {
				condition = new Boolean(true);
				annotationMgr.removeAnnotationGroup(job.getIDString());
				//System.err.println("--- TESTING exit event and remove all annotation in job: " + job.getIDString());
				getDebugSession(job).getEventManager().removeEventListener(this);
				cleanupDebugVariables(job);
			} else if (event instanceof IPCDIExitedEvent) {
				removeAnnotation(job.getIDString(), event.getAllProcesses());
				// System.out.println("-------------------- terminate ------------------------");
				// annotationMgr.printBitList(event.getAllProcesses());
				cleanupDebugVariables(job);
				fireTerminatedEvent(job, event.getAllProcesses());
			} else if (event instanceof IPCDIErrorEvent) {
				final IPCDIErrorEvent errEvent = (IPCDIErrorEvent)event;
				if (errEvent.getErrorCode() != IPCDIErrorEvent.DBG_WARNING) {
					removeAnnotation(job.getIDString(), event.getAllProcesses());					
				}				
				PTPDebugUIPlugin.getDisplay().asyncExec(new Runnable() {
					public void run() {
						IPCDIErrorInfo info = (IPCDIErrorInfo)errEvent.getReason();
						PTPDebugUIPlugin.errorDialog("Error", new Exception(info.getMessage() + " on tasks: "+ PDebugUIUtils.showBitList(errEvent.getAllProcesses())));
					}
				});
				cleanupDebugVariables(job);
			} else if (event instanceof BreakpointCreatedEvent) {
				// do nothing in breakpoint created event
				continue;
			}
			firePaintListener(condition);
		}
	}
	/** Remove annotation
	 * @param job_id job ID
	 * @param tasks
	 */
	private void removeAnnotation(String job_id, BitList tasks) {
		try {
			annotationMgr.removeAnnotation(job_id, tasks);
			System.err.println("--- TESTING remove annotation of processes in this event: " + PDebugUIUtils.showBitList(tasks));
		} catch (final CoreException e) {
			PTPDebugUIPlugin.getDisplay().asyncExec(new Runnable() {
				public void run() {
					PTPDebugUIPlugin.errorDialog("Error", e);
				}
			});
		}
	}
	/** Fire suspend event
	 * @param job
	 * @param tasks
	 */
	public void fireSuspendEvent(IPJob job, BitList tasks) {
		fireDebugEvent(new SuspendedDebugEvent(job.getIDString(), (BitList) job.getAttribute(IAbstractDebugger.SUSPENDED_PROC_KEY), (BitList) job.getAttribute(IAbstractDebugger.TERMINATED_PROC_KEY)));
	}
	/** Fire resume event
	 * @param job
	 * @param tasks
	 */
	public void fireResumeEvent(IPJob job, BitList tasks) {
		fireDebugEvent(new ResumedDebugEvent(job.getIDString(), (BitList) job.getAttribute(IAbstractDebugger.SUSPENDED_PROC_KEY), (BitList) job.getAttribute(IAbstractDebugger.TERMINATED_PROC_KEY)));
	}
	/** Fire terminate event
	 * @param job
	 * @param tasks
	 */
	public void fireTerminatedEvent(IPJob job, BitList tasks) {
		fireDebugEvent(new TerminatedDebugEvent(job.getIDString(), (BitList) job.getAttribute(IAbstractDebugger.TERMINATED_PROC_KEY), (BitList) job.getAttribute(IAbstractDebugger.SUSPENDED_PROC_KEY)));
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
				debugModel.deletePBreakpointBySet(job.getIDString());
			} catch (CoreException e) {
				PTPDebugUIPlugin.log(e);
			}
			debugModel.deleteJob(job.getIDString());
			debugModel.shutdownSession(job);
		}
		super.removeJob(job);
	}
	
	/** Clean up debug variables in given job
	 * @param job
	 */
	protected void cleanupDebugVariables(IPJob job) {
		variableManager.cleanVariableResults(job);
	}
	/** Update debug variables in current job
	 * 
	 */
	protected void updateDebugVariables() {
		if (prefAutoUpdateVarOnChange) {
			updateDebugVariables(getCurrentJob());
		}
	}
	/** Update debug variables in given job
	 * @param job
	 */
	private void updateDebugVariables(IPJob job) {
		if (variableManager.hasVariable(job)) {
			PTPDebugUIPlugin.getDisplay().syncExec(new Runnable() {
				public void run() {
					UpdateVariablesActionDelegate.doAction(null);
				}
			});
		}
	}
	/** Update debug variable when the debugger is suspendeds
	 * @param job
	 */
	private void updateDebugVariablesOnSuspend(IPJob job) {
		if (prefAutoUpdateVarOnSuspend) {
			IPJob cur_job = getCurrentJob();
			if (cur_job != null && cur_job.equals(job)) {
				updateDebugVariables(job);
			}
		}
	}
}
