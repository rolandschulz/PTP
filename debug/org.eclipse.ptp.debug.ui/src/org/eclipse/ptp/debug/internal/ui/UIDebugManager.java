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
import org.eclipse.cdt.debug.core.cdi.ICDILineLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILocator;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocationBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.PreferenceConstants;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.PCDIDebugModel;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.ProcessInputStream;
import org.eclipse.ptp.debug.core.cdi.IPCDISession;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIEvent;
import org.eclipse.ptp.debug.core.launch.IPLaunchEvent;
import org.eclipse.ptp.debug.core.launch.IPLaunchListener;
import org.eclipse.ptp.debug.core.launch.PDebugTargetRegisterEvent;
import org.eclipse.ptp.debug.core.launch.PDebugTargetUnRegisterEvent;
import org.eclipse.ptp.debug.core.launch.PLaunchStartedEvent;
import org.eclipse.ptp.debug.external.IAbstractDebugger;
import org.eclipse.ptp.debug.external.cdi.BreakpointHitInfo;
import org.eclipse.ptp.debug.external.cdi.EndSteppingRangeInfo;
import org.eclipse.ptp.debug.external.cdi.event.BreakpointCreatedEvent;
import org.eclipse.ptp.debug.external.cdi.event.BreakpointHitEvent;
import org.eclipse.ptp.debug.external.cdi.event.DebuggerExitedEvent;
import org.eclipse.ptp.debug.external.cdi.event.EndSteppingRangeEvent;
import org.eclipse.ptp.debug.external.cdi.event.ErrorEvent;
import org.eclipse.ptp.debug.external.cdi.event.InferiorExitedEvent;
import org.eclipse.ptp.debug.external.cdi.event.InferiorResumedEvent;
import org.eclipse.ptp.debug.external.cdi.event.InferiorSignaledEvent;
import org.eclipse.ptp.debug.internal.ui.preferences.IPDebugPreferenceConstants;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.debug.ui.events.IDebugActionEvent;
import org.eclipse.ptp.debug.ui.events.ResumedDebugEvent;
import org.eclipse.ptp.debug.ui.events.SuspendedDebugEvent;
import org.eclipse.ptp.debug.ui.events.TerminatedDebugEvent;
import org.eclipse.ptp.debug.ui.listeners.IDebugActionUpdateListener;
import org.eclipse.ptp.debug.ui.listeners.IRegListener;
import org.eclipse.ptp.internal.ui.JobManager;
import org.eclipse.ptp.ui.OutputConsole;
import org.eclipse.ptp.ui.listeners.ISetListener;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.ptp.ui.model.IElementSet;

/**
 * @author clement chu
 * 
 */
public class UIDebugManager extends JobManager implements ISetListener, IBreakpointListener, ICDIEventListener, IPLaunchListener {
	private final static int REG_TYPE = 1;
	private final static int UNREG_TYPE = 2;
	private List regListeners = new ArrayList();
	private List debugEventListeners = new ArrayList();
	private PAnnotationManager annotationMgr = null;
	private PCDIDebugModel debugModel = null;
	private Map consoleWindows = new HashMap();

	public UIDebugManager() {
		addSetListener(this);
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
		debugModel = PTPDebugCorePlugin.getDebugModel();
		debugModel.addLaunchListener(this);
		annotationMgr = new PAnnotationManager(this);
	}
	public void shutdown() {
		removeSetListener(this);
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
		regListeners.clear();
		debugModel.removeLaunchListener(this);
		annotationMgr.shutdown();
		super.shutdown();
	}
	private void defaultRegister(IPCDISession session) { // register process 0 if the preference is checked
		if (PTPDebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IPDebugPreferenceConstants.PREF_PTP_DEBUG_REGISTER_PROC_0)) {
			IPProcess proc = session.getJob().findProcessByTaskId(0);
			if (proc != null)
				registerProcess(session, proc, true);
		}
	}
	public boolean isDebugMode(String job_id) {
		if (isNoJob(job_id))
			return false;
		return isDebugMode(findJobById(job_id));
	}
	public boolean isDebugMode(IPJob job) {
		if (job == null)
			return false;
		return job.isDebug();
	}
	public boolean isRunning(IPJob job) {
		if (job == null)
			return false;
		return (job != null && !job.isAllStop());
	}
	public boolean isRunning(String job_id) {
		if (isNoJob(job_id))
			return false;
		return isRunning(findJobById(job_id));
	}
	// change job
	public void fireJobChangeEvent(String cur_jid, String pre_jid) {
		updateBreakpointMarker(IElementHandler.SET_ROOT_ID);
		try {
			removeAllRegisterElements(pre_jid);
		} catch (CoreException e) {
			PTPDebugUIPlugin.log(e);
		}
		super.fireJobChangeEvent(cur_jid, pre_jid);
	}
	public void addDebugEventListener(IDebugActionUpdateListener listener) {
		if (!debugEventListeners.contains(listener))
			debugEventListeners.add(listener);
	}
	public void removeDebugEventListener(IDebugActionUpdateListener listener) {
		if (debugEventListeners.contains(listener))
			debugEventListeners.remove(listener);
	}
	public void fireDebugEvent(final IDebugActionEvent event) {
		for (Iterator i = debugEventListeners.iterator(); i.hasNext();) {
			final IDebugActionUpdateListener dListener = (IDebugActionUpdateListener) i.next();
			Platform.run(new SafeNotifier() {
				public void run() {
					dListener.handleDebugActionEvent(event);
				}
			});
		}
	}
	public void addRegListener(IRegListener listener) {
		if (!regListeners.contains(listener))
			regListeners.add(listener);
	}
	public void removeRegListener(IRegListener listener) {
		if (regListeners.contains(listener))
			regListeners.remove(listener);
	}
	public synchronized void fireRegListener(final int type, final BitList tasks) {
		for (Iterator i = regListeners.iterator(); i.hasNext();) {
			final IRegListener rListener = (IRegListener) i.next();
			Platform.run(new SafeNotifier() {
				public void run() {
					switch (type) {
					case REG_TYPE:
						rListener.register(tasks);
						break;
					case UNREG_TYPE:
						rListener.unregister(tasks);
					}
				}
			});
		}
	}
	public void removeEventListener(String job_id) {
		ICDISession session = getDebugSession(job_id);
		if (session != null)
			session.getEventManager().removeEventListener(this);
	}
	public IPCDISession getDebugSession(String job_id) {
		if (isNoJob(job_id))
			return null;
		IPJob job = findJobById(job_id);
		if (job == null)
			return null;
		return getDebugSession(job);
	}
	public IPCDISession getDebugSession(IPJob job) {
		return (IPCDISession) job.getAttribute(PreferenceConstants.JOB_DEBUG_SESSION);
	}
	public int convertToInt(String id) {
		return Integer.parseInt(id);
	}
	private void addConsoleWindow(IPProcess proc) {
		consoleWindows.put(proc, new OutputConsole(proc.getElementName(), new ProcessInputStream(proc)));
	}
	private void removeConsoleWindow(IPProcess proc) {
		OutputConsole outputConsole = (OutputConsole) consoleWindows.remove(proc);
		if (outputConsole != null) {
			outputConsole.kill();
		}
	}
	/***************************************************************************************************************************************************************************************************
	 * Launch Listener
	 **************************************************************************************************************************************************************************************************/
	public void handleLaunchEvent(IPLaunchEvent event) {
		IPJob job = event.getJob();
		if (event instanceof PLaunchStartedEvent) {
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
	/***************************************************************************************************************************************************************************************************
	 * Register / Unregister
	 **************************************************************************************************************************************************************************************************/
	public void registerProcess(IPCDISession session, IPProcess proc, boolean isChanged) {
		session.registerTarget(proc.getTaskId(), isChanged);
		addConsoleWindow(proc);
	}
	public void unregisterProcess(IPCDISession session, IPProcess proc, boolean isChanged) {
		session.unregisterTarget(proc.getTaskId(), isChanged);
		removeConsoleWindow(proc);
	}
	public void unregisterElements(IElement[] elements) throws CoreException {
		IPJob job = findJobById(getCurrentJobId());
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
	public void registerElements(IElement[] elements) throws CoreException {
		IPJob job = findJobById(getCurrentJobId());
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
	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {}
	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {}
	/***************************************************************************************************************************************************************************************************
	 * Element Set
	 **************************************************************************************************************************************************************************************************/
	public void updateBreakpointMarker(String cur_sid) {
		try {
			debugModel.updatePBreakpoints(cur_sid);
		} catch (CoreException e) {
			PTPDebugUIPlugin.log(e);
		}
	}
	public void removeAllRegisterElements(final String job_id) throws CoreException {
		final IElementHandler elementHandler = getElementHandler(job_id);
		if (elementHandler == null)
			return;
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				new Job("Removing registered processes") {
					protected IStatus run(IProgressMonitor pmonitor) {
						IPCDISession session = getDebugSession(job_id);
						if (session == null)
							return Status.CANCEL_STATUS;
						IElement[] registerElements = elementHandler.getRegisteredElements();
						for (int i = 0; i < registerElements.length; i++) {
							IPProcess proc = findProcess(job_id, registerElements[i].getID());
							if (proc != null)
								unregisterProcess(session, proc, false);
						}
						return Status.OK_STATUS;
					}
				}.schedule();
			}
		};
		ResourcesPlugin.getWorkspace().run(runnable, null);
	}
	public void updateRegisterUnRegisterElements(final IElementSet curSet, final IElementSet preSet, final String job_id) throws CoreException {
		final IElementHandler elementHandler = getElementHandler(job_id);
		if (elementHandler == null)
			return;
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				new Job("Updating registered/unregistered processes") {
					protected IStatus run(IProgressMonitor pmonitor) {
						IPCDISession session = getDebugSession(job_id);
						if (session == null)
							return Status.CANCEL_STATUS;
						IElement[] registerElements = elementHandler.getRegisteredElements();
						for (int i = 0; i < registerElements.length; i++) {
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
						}
						return Status.OK_STATUS;
					}
				}.schedule();
			}
		};
		ResourcesPlugin.getWorkspace().run(runnable, null);
	}
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
	}
	// Delete the set that will delete the all related breakpoint
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
	/*
	 * Cannot unregister the extension final String CDT_DEBUG_UI_ID = "org.eclipse.cdt.debug.ui"; Bundle bundle = Platform.getBundle(CDT_DEBUG_UI_ID); if (bundle != null && bundle.getState() ==
	 * Bundle.ACTIVE) { //ExtensionRegistry reg = (ExtensionRegistry) Platform.getExtensionRegistry(); //reg.remove(bundle.getBundleId()); try { Platform.getBundle(CDT_DEBUG_UI_ID).uninstall();
	 * System.out.println("Remove: " + bundle.getState()); } catch (BundleException e) { System.out.println("Err: " + e.getMessage()); } }
	 */
	/***************************************************************************************************************************************************************************************************
	 * Event
	 **************************************************************************************************************************************************************************************************/
	public void handleDebugEvents(final ICDIEvent[] events) {
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				new Job("Update Events") {
					protected IStatus run(IProgressMonitor pmonitor) {
						handleDebugEvents(events, pmonitor);
						return Status.OK_STATUS;
					}
				}.schedule();
			}
		};
		try {
			ResourcesPlugin.getWorkspace().run(runnable, null);
		} catch (CoreException e) {
			PTPDebugUIPlugin.log(e);
		}
	}
	private synchronized void handleDebugEvents(ICDIEvent[] events, IProgressMonitor monitor) {
		for (int i = 0; i < events.length; i++) {
			Object condition = null;
			IPCDIEvent event = (IPCDIEvent) events[i];
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
			if (event instanceof BreakpointHitEvent) {
				BreakpointHitEvent bptHitEvent = (BreakpointHitEvent) event;
				ICDIBreakpoint bpt = ((BreakpointHitInfo) bptHitEvent.getReason()).getBreakpoint();
				if (bpt instanceof ICDILocationBreakpoint) {
					ICDILocator locator = ((ICDILocationBreakpoint) bpt).getLocator();
					int lineNumber = locator.getLineNumber();
					if (lineNumber == 0)
						lineNumber = 1;
					String fileName = workingDebugDir + "/" + locator.getFile();
					try {
						annotationMgr.addAnnotation(job.getIDString(), fileName, lineNumber, event.getAllUnregisteredProcesses(), false);
						annotationMgr.addAnnotation(job.getIDString(), fileName, lineNumber, event.getAllRegisteredProcesses(), true);
					} catch (CoreException e) {
						PTPDebugUIPlugin.errorDialog(PTPDebugUIPlugin.getActiveWorkbenchShell(), "Error", "Cannot display annotation marker on editor", e);
					}
				}
				fireSuspendEvent(job, event.getAllProcesses());
			} else if (event instanceof EndSteppingRangeEvent) {
				EndSteppingRangeEvent endStepEvent = (EndSteppingRangeEvent) event;
				ICDILineLocation lineLocation = ((EndSteppingRangeInfo) endStepEvent.getReason()).getLineLocation();
				if (lineLocation != null) {
					int lineNumber = lineLocation.getLineNumber();
					if (lineNumber == 0)
						lineNumber = 1;
					String fileName = workingDebugDir + "/" + lineLocation.getFile();
					try {
						annotationMgr.addAnnotation(job.getIDString(), fileName, lineNumber, event.getAllUnregisteredProcesses(), false);
						annotationMgr.addAnnotation(job.getIDString(), fileName, lineNumber, event.getAllRegisteredProcesses(), true);
					} catch (CoreException e) {
						PTPDebugUIPlugin.errorDialog(PTPDebugUIPlugin.getActiveWorkbenchShell(), "Error", "Cannot display annotation marker on editor", e);
					}
				}
				// System.out.println("-------------------- end stepping ------------------------");
				// annotationMgr.printBitList(event.getAllProcesses().toBitList());
				fireSuspendEvent(job, event.getAllProcesses());
			} else if (event instanceof InferiorResumedEvent) {
				try {
					annotationMgr.removeAnnotation(job.getIDString(), event.getAllProcesses());
				} catch (CoreException e) {
					PTPDebugUIPlugin.errorDialog(PTPDebugUIPlugin.getActiveWorkbenchShell(), "Error", "Cannot display annotation marker on editor", e);
				}
				// System.out.println("-------------------- resume ------------------------");
				// annotationMgr.printBitList(event.getAllProcesses().toBitList());
				fireResumeEvent(job, event.getAllProcesses());
			} else if (event instanceof InferiorExitedEvent || event instanceof ErrorEvent) {
				try {
					annotationMgr.removeAnnotation(job.getIDString(), event.getAllProcesses());
				} catch (CoreException e) {
					PTPDebugUIPlugin.errorDialog(PTPDebugUIPlugin.getActiveWorkbenchShell(), "Error", "Cannot display annotation marker on editor", e);
				}
				// System.out.println("-------------------- terminate ------------------------");
				// annotationMgr.printBitList(event.getAllProcesses());
				fireTerminatedEvent(job, event.getAllProcesses());
			} else if (event instanceof InferiorSignaledEvent) {
				InferiorSignaledEvent signalEvent = (InferiorSignaledEvent) event;
				ICDILocator locator = signalEvent.getLocator();
				if (locator != null) {
					int lineNumber = locator.getLineNumber();
					if (lineNumber == 0)
						lineNumber = 1;
					String fileName = workingDebugDir + "/" + locator.getFile();
					try {
						annotationMgr.addAnnotation(job.getIDString(), fileName, lineNumber, event.getAllUnregisteredProcesses(), false);
						annotationMgr.addAnnotation(job.getIDString(), fileName, lineNumber, event.getAllRegisteredProcesses(), true);
					} catch (CoreException e) {
						PTPDebugUIPlugin.errorDialog(PTPDebugUIPlugin.getActiveWorkbenchShell(), "Error", "Cannot display annotation marker on editor", e);
					}
				}
				// System.out.println("-------------------- suspend ------------------------");
				// annotationMgr.printBitList(event.getAllProcesses().toBitList());
				fireSuspendEvent(job, event.getAllProcesses());
			} else if (event instanceof BreakpointCreatedEvent) {
				// do nothing in breakpoint created event
				continue;
			} else if (event instanceof DebuggerExitedEvent) {
				condition = new Boolean(true);
				annotationMgr.removeAnnotationGroup(job.getIDString());
				getDebugSession(job).getEventManager().addEventListener(this);
			}
			firePaintListener(condition);
		}
	}
	public void fireSuspendEvent(IPJob job, BitList tasks) {
		fireDebugEvent(new SuspendedDebugEvent(job.getIDString(), (BitList) job.getAttribute(IAbstractDebugger.SUSPENDED_PROC_KEY), (BitList) job.getAttribute(IAbstractDebugger.TERMINATED_PROC_KEY)));
	}
	public void fireResumeEvent(IPJob job, BitList tasks) {
		fireDebugEvent(new ResumedDebugEvent(job.getIDString(), (BitList) job.getAttribute(IAbstractDebugger.SUSPENDED_PROC_KEY), (BitList) job.getAttribute(IAbstractDebugger.TERMINATED_PROC_KEY)));
	}
	public void fireTerminatedEvent(IPJob job, BitList tasks) {
		fireDebugEvent(new TerminatedDebugEvent(job.getIDString(), (BitList) job.getAttribute(IAbstractDebugger.TERMINATED_PROC_KEY), (BitList) job.getAttribute(IAbstractDebugger.SUSPENDED_PROC_KEY)));
	}
	/***************************************************************************************************************************************************************************************************
	 * debug actions
	 **************************************************************************************************************************************************************************************************/
	public BitList getCurrentSetTasks(String job_id, String set_id) throws CoreException {
		return debugModel.getTasks(job_id, set_id);
	}
	public void resume() throws CoreException {
		resume(getCurrentJobId(), getCurrentSetId());
	}
	public void resume(String job_id, String set_id) throws CoreException {
		IPCDISession session = getDebugSession(job_id);
		if (session == null)
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No session found", null));
		try {
			session.resume(getCurrentSetTasks(job_id, set_id));
		} catch (PCDIException e) {
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), null));
		}
	}
	public void suspend() throws CoreException {
		suspend(getCurrentJobId(), getCurrentSetId());
	}
	public void suspend(String job_id, String set_id) throws CoreException {
		IPCDISession session = getDebugSession(job_id);
		if (session == null)
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No session found", null));
		try {
			session.suspend(getCurrentSetTasks(job_id, set_id));
		} catch (PCDIException e) {
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), null));
		}
	}
	public void terminate() throws CoreException {
		terminate(getCurrentJobId(), getCurrentSetId());
	}
	public void terminate(String job_id, String set_id) throws CoreException {
		IPJob job = findJobById(job_id);
		if (isDebugMode(job)) {
			IPCDISession session = (IPCDISession) getDebugSession(job);
			if (session == null)
				throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No session found", null));
			try {
				session.stop(getCurrentSetTasks(job_id, set_id));
			} catch (PCDIException e) {
				throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), null));
			}
		} else {
			super.terminateAll(job_id);
		}
	}
	public void stepInto() throws CoreException {
		stepInto(getCurrentJobId(), getCurrentSetId());
	}
	public void stepInto(String job_id, String set_id) throws CoreException {
		IPCDISession session = getDebugSession(job_id);
		if (session == null)
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No session found", null));
		try {
			session.steppingInto(getCurrentSetTasks(job_id, set_id));
		} catch (PCDIException e) {
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), null));
		}
	}
	public void stepOver() throws CoreException {
		stepOver(getCurrentJobId(), getCurrentSetId());
	}
	public void stepOver(String job_id, String set_id) throws CoreException {
		IPCDISession session = getDebugSession(job_id);
		if (session == null)
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No session found", null));
		try {
			session.steppingOver(getCurrentSetTasks(job_id, set_id));
		} catch (PCDIException e) {
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), null));
		}
	}
	public void stepReturn() throws CoreException {
		stepReturn(getCurrentJobId(), getCurrentSetId());
	}
	public void stepReturn(String job_id, String set_id) throws CoreException {
		IPCDISession session = getDebugSession(job_id);
		if (session == null)
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No session found", null));
		try {
			session.steppingReturn(getCurrentSetTasks(job_id, set_id));
		} catch (PCDIException e) {
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), null));
		}
	}
	public void removeJob(IPJob job) {
		try {
			debugModel.deletePBreakpointBySet(job.getIDString());
		} catch (CoreException e) {
			PTPDebugUIPlugin.log(e);
		}
		debugModel.deleteJob(job.getIDString());
		debugModel.shutdownSession(job);
		super.removeJob(job);
	}
}
