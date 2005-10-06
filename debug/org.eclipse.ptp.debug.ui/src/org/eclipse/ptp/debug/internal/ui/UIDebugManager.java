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
import java.util.Iterator;
import java.util.List;
import org.eclipse.cdt.debug.core.cdi.ICDILineLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILocator;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDILineBreakpoint;
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
import org.eclipse.ptp.debug.core.IPDebugListener;
import org.eclipse.ptp.debug.core.PCDIDebugModel;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.cdi.IPCDISession;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIEvent;
import org.eclipse.ptp.debug.external.cdi.BreakpointHitInfo;
import org.eclipse.ptp.debug.external.cdi.EndSteppingRangeInfo;
import org.eclipse.ptp.debug.external.cdi.event.BreakpointHitEvent;
import org.eclipse.ptp.debug.external.cdi.event.EndSteppingRangeEvent;
import org.eclipse.ptp.debug.external.cdi.event.ErrorEvent;
import org.eclipse.ptp.debug.external.cdi.event.InferiorExitedEvent;
import org.eclipse.ptp.debug.external.cdi.event.InferiorResumedEvent;
import org.eclipse.ptp.debug.external.cdi.event.TargetRegisteredEvent;
import org.eclipse.ptp.debug.external.cdi.event.TargetUnregisteredEvent;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.debug.ui.events.IDebugActionEvent;
import org.eclipse.ptp.debug.ui.events.ResumedDebugEvent;
import org.eclipse.ptp.debug.ui.events.SuspendedDebugEvent;
import org.eclipse.ptp.debug.ui.events.TerminatedDebugEvent;
import org.eclipse.ptp.debug.ui.listeners.IDebugActionUpdateListener;
import org.eclipse.ptp.debug.ui.listeners.IRegListener;
import org.eclipse.ptp.internal.ui.JobManager;
import org.eclipse.ptp.ui.listeners.ISetListener;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.ptp.ui.model.IElementSet;

/**
 * @author clement chu
 * 
 */
public class UIDebugManager extends JobManager implements ISetListener, IBreakpointListener, ICDIEventListener, IPDebugListener {
	public final static String BITSET_KEY = "bitset";
	public final static String TERMINATED_PROC_KEY = "terminated";
	public final static String SUSPENDED_PROC_KEY = "suspended";
	private final static int REG_TYPE = 1;
	private final static int UNREG_TYPE = 2;
	private List regListeners = new ArrayList();
	private List debugEventListeners = new ArrayList();
	private PAnnotationManager annotationMgr = null;

	public UIDebugManager() {
		addSetListener(this);
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
		PTPDebugCorePlugin.getDefault().addDebugSessionListener(this);
		annotationMgr = new PAnnotationManager(this);
	}
	public void shutdown() {
		removeSetListener(this);
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
		PTPDebugCorePlugin.getDefault().removeDebugSessionListener(this);
		regListeners.clear();
		annotationMgr.shutdown();
		super.shutdown();
	}
	public String initial() {
		String new_job_id = super.initial();
		IElementHandler elementHandler = getElementHandler(new_job_id);
		if (elementHandler != null) {
			if ((BitList) elementHandler.getData(TERMINATED_PROC_KEY) == null)
				elementHandler.setData(TERMINATED_PROC_KEY, new BitList(elementHandler.getSetRoot().size()));
			if ((BitList) elementHandler.getData(SUSPENDED_PROC_KEY) == null)
				elementHandler.setData(SUSPENDED_PROC_KEY, new BitList(elementHandler.getSetRoot().size()));
		}
		createEventListener(new_job_id);
		return new_job_id;
	}
	public boolean isDebugging(String job_id) {
		if (isNoJob(job_id))
			return false;
		IPJob job = findJobById(job_id);
		return (job != null && !job.isAllStop() && job.isDebug());
	}
	// change job
	public void setCurrentJobId(String job_id) {
		super.setCurrentJobId(job_id);
		updateBreakpointMarker(IElementHandler.SET_ROOT_ID);
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
	public void createEventListener(String job_id) {
		ICDISession session = getDebugSession(job_id);
		if (session != null) {
			session.getEventManager().addEventListener(this);
		}
	}
	public void removeEventListener(String job_id) {
		ICDISession session = getDebugSession(job_id);
		if (session != null)
			session.getEventManager().removeEventListener(this);
	}
	public ICDISession getDebugSession(String job_id) {
		if (isNoJob(job_id))
			return null;
		IPJob job = findJobById(job_id);
		if (job == null)
			return null;
		return PTPDebugCorePlugin.getDefault().getDebugSession(job);
	}
	public ICDISession getDebugSession(IPJob job) {
		return PTPDebugCorePlugin.getDefault().getDebugSession(job);
	}
	public int convertToInt(String id) {
		return Integer.parseInt(id);
	}
	/*******************************************************************************************************************************************************************************************************************************************************************************************************
	 * Register / Unregister
	 ******************************************************************************************************************************************************************************************************************************************************************************************************/
	public void registerProcess(IPCDISession session, int task_id, boolean isChanged) {
		session.registerTarget(task_id, isChanged);
	}
	public void unregisterProcess(IPCDISession session, int task_id, boolean isChanged) {
		session.unregisterTarget(task_id, isChanged);
	}
	public void unregisterElements(IElement[] elements) throws CoreException {
		IPJob job = findJobById(getCurrentJobId());
		if (job == null)
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No job found", null));
		IPCDISession session = (IPCDISession) getDebugSession(job);
		if (session == null)
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No session found", null));
		for (int i = 0; i < elements.length; i++) {
			// only unregister some registered elements
			if (elements[i].isRegistered()) {
				IPProcess process = findProcess(job, elements[i].getID());
				if (process != null)
					unregisterProcess(session, process.getTaskId(), true);
			}
		}
	}
	public void registerElements(IElement[] elements) throws CoreException {
		IPJob job = findJobById(getCurrentJobId());
		if (job == null)
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No job found", null));
		IPCDISession session = (IPCDISession) getDebugSession(job);
		if (session == null)
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No session found", null));
		for (int i = 0; i < elements.length; i++) {
			// only register some unregistered elements
			if (!elements[i].isRegistered()) {
				IPProcess process = findProcess(job, elements[i].getID());
				if (process != null && !process.isAllStop())
					registerProcess(session, process.getTaskId(), true);
			}
		}
	}
	/*******************************************************************************************************************************************************************************************************************************************************************************************************
	 * Breakpoint
	 ******************************************************************************************************************************************************************************************************************************************************************************************************/
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
	/*******************************************************************************************************************************************************************************************************************************************************************************************************
	 * Element Set
	 ******************************************************************************************************************************************************************************************************************************************************************************************************/
	public void updateBreakpointMarker(String cur_sid) {
		try {
			PCDIDebugModel.updatePBreakpoints(cur_sid);
		} catch (CoreException e) {
			PTPDebugUIPlugin.log(e);
		}
	}
	public void removeAllRegisterElements(final String job_id) throws CoreException {
		final IElementHandler elementHandler = getElementHandler(job_id);
		if (elementHandler == null)
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "Update register/unregister error", null));
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				new Job("Removing registered processes") {
					protected IStatus run(IProgressMonitor pmonitor) {
						IPCDISession session = (IPCDISession) getDebugSession(job_id);
						if (session == null)
							return Status.CANCEL_STATUS;
						String[] registerElementsID = elementHandler.getRegisteredElementsID();
						for (int i = 0; i < registerElementsID.length; i++) {
							int taskID = convertToInt(elementHandler.getSetRoot().get(registerElementsID[i]).getName());
							unregisterProcess(session, taskID, false);
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
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "Update register/unregister error", null));
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				new Job("Updating registered/unregistered processes") {
					protected IStatus run(IProgressMonitor pmonitor) {
						IPCDISession session = (IPCDISession) getDebugSession(job_id);
						if (session == null)
							return Status.CANCEL_STATUS;
						String[] registerElementsID = elementHandler.getRegisteredElementsID();
						for (int i = 0; i < registerElementsID.length; i++) {
							if (curSet.contains(registerElementsID[i])) {
								if (preSet != null && !preSet.contains(registerElementsID[i])) {
									int taskID = convertToInt(elementHandler.getSetRoot().get(registerElementsID[i]).getName());
									registerProcess(session, taskID, false);
								}
							} else {
								int taskID = convertToInt(elementHandler.getSetRoot().get(registerElementsID[i]).getName());
								unregisterProcess(session, taskID, false);
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
			PCDIDebugModel.deletePBreakpointBySet(getCurrentJobId(), set.getID());
		} catch (CoreException e) {
			PTPDebugUIPlugin.log(e);
		}
		set.setData(BITSET_KEY, null);
		IPCDISession session = (IPCDISession) getDebugSession(getCurrentJobId());
		if (session != null) {
			session.getModelManager().delProcessSet(set.getID());
		}
	}
	
	public void createSetEvent(IElementSet set, IElement[] elements) {
		BitList tasks = new BitList(set.getElementHandler().getSetRoot().size());
		for (int i = 0; i < elements.length; i++) {
			tasks.set(convertToInt(elements[i].getName()));
		}
		set.setData(BITSET_KEY, tasks);
		IPCDISession session = (IPCDISession) getDebugSession(getCurrentJobId());
		if (session != null) {
			session.getModelManager().newProcessSet(set.getID(), tasks);
		}
	}
	public void addElementsEvent(IElementSet set, IElement[] elements) {
		BitList tasks = (BitList) set.getData(BITSET_KEY);
		for (int i = 0; i < elements.length; i++) {
			tasks.set(convertToInt(elements[i].getName()));
		}
	}
	public void removeElementsEvent(IElementSet set, IElement[] elements) {
		BitList tasks = (BitList) set.getData(BITSET_KEY);
		for (int i = 0; i < elements.length; i++) {
			tasks.clear(convertToInt(elements[i].getName()));
		}
	}
	/*
	 * Cannot unregister the extension final String CDT_DEBUG_UI_ID = "org.eclipse.cdt.debug.ui"; Bundle bundle = Platform.getBundle(CDT_DEBUG_UI_ID); if (bundle != null && bundle.getState() == Bundle.ACTIVE) { //ExtensionRegistry reg = (ExtensionRegistry) Platform.getExtensionRegistry();
	 * //reg.remove(bundle.getBundleId()); try { Platform.getBundle(CDT_DEBUG_UI_ID).uninstall(); System.out.println("Remove: " + bundle.getState()); } catch (BundleException e) { System.out.println("Err: " + e.getMessage()); } }
	 */
	/*******************************************************************************************************************************************************************************************************************************************************************************************************
	 * Event
	 ******************************************************************************************************************************************************************************************************************************************************************************************************/
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
			if (event instanceof TargetRegisteredEvent) {
				IElementHandler elementHandler = getElementHandler(job.getIDString());
				int[] processes = event.getAllProcesses().toIntArray();
				for (int j = 0; j < processes.length; j++) {
					IPProcess proc = job.findProcessByTaskId(processes[i]);
					elementHandler.addRegisterElement(proc.getIDString());
					elementHandler.getSetRoot().get(proc.getIDString()).setRegistered(true);
				}
				fireRegListener(REG_TYPE, event.getAllRegisteredProcesses().toBitList());
			} else if (event instanceof TargetUnregisteredEvent) {
				IElementHandler elementHandler = getElementHandler(job.getIDString());
				int[] processes = event.getAllProcesses().toIntArray();
				for (int j = 0; j < processes.length; j++) {
					IPProcess proc = job.findProcessByTaskId(processes[i]);
					elementHandler.removeRegisterElement(proc.getIDString());
					elementHandler.getSetRoot().get(proc.getIDString()).setRegistered(false);
				}
				fireRegListener(UNREG_TYPE, event.getAllUnregisteredProcesses().toBitList());
			} else if (event instanceof BreakpointHitEvent) {
				BreakpointHitEvent bptHitEvent = (BreakpointHitEvent) event;
				ICDIBreakpoint bpt = ((BreakpointHitInfo) bptHitEvent.getReason()).getBreakpoint();
				if (bpt instanceof ICDILineBreakpoint) {
					ICDILocator locator = ((ICDILineBreakpoint) bpt).getLocator();
					int lineNumber = locator.getLineNumber();
					// FIXME: Hardcode the filename
					String fileName = PreferenceConstants.SIMULATION_PROJECT_NAME + "/" + PreferenceConstants.SIMULATION_FILE_NAME + ".c";
					try {
						annotationMgr.addAnnotation(job.getIDString(), fileName, lineNumber, event.getAllUnregisteredProcesses().toBitList(), false);
						annotationMgr.addAnnotation(job.getIDString(), fileName, lineNumber, event.getAllRegisteredProcesses().toBitList(), true);
					} catch (CoreException e) {
						PTPDebugUIPlugin.errorDialog(PTPDebugUIPlugin.getActiveWorkbenchShell(), "Error", "Cannot display annotation marker on editor", e);
					}
				}
				fireSuspendEvent(job, event.getAllProcesses().toBitList());
			} else if (event instanceof EndSteppingRangeEvent) {
				EndSteppingRangeEvent endStepEvent = (EndSteppingRangeEvent) event;
				ICDILineLocation lineLocation = ((EndSteppingRangeInfo)endStepEvent.getReason()).getLineLocation();
				if (lineLocation != null) {
					int lineNumber = lineLocation.getLineNumber();
					// FIXME: Hardcode the filename
					String fileName = PreferenceConstants.SIMULATION_PROJECT_NAME + "/" + PreferenceConstants.SIMULATION_FILE_NAME + ".c";
					try {
						annotationMgr.addAnnotation(job.getIDString(), fileName, lineNumber, event.getAllUnregisteredProcesses().toBitList(), false);
						annotationMgr.addAnnotation(job.getIDString(), fileName, lineNumber, event.getAllRegisteredProcesses().toBitList(), true);
					} catch (CoreException e) {
						PTPDebugUIPlugin.errorDialog(PTPDebugUIPlugin.getActiveWorkbenchShell(), "Error", "Cannot display annotation marker on editor", e);
					}
				}
				// System.out.println("-------------------- end stepping ------------------------");
				// annotationMgr.printBitList(event.getAllProcesses().toBitList());
				fireSuspendEvent(job, event.getAllProcesses().toBitList());
			} else if (event instanceof InferiorResumedEvent) {
				try {
					annotationMgr.removeAnnotation(job.getIDString(), event.getAllProcesses().toBitList());
				} catch (CoreException e) {
					PTPDebugUIPlugin.errorDialog(PTPDebugUIPlugin.getActiveWorkbenchShell(), "Error", "Cannot display annotation marker on editor", e);
				}
				// System.out.println("-------------------- resume ------------------------");
				// annotationMgr.printBitList(event.getAllProcesses().toBitList());
				fireResumeEvent(job, event.getAllProcesses().toBitList());
			} else if (event instanceof InferiorExitedEvent || event instanceof ErrorEvent) {
				try {
					annotationMgr.removeAnnotation(job.getIDString(), event.getAllProcesses().toBitList());
				} catch (CoreException e) {
					PTPDebugUIPlugin.errorDialog(PTPDebugUIPlugin.getActiveWorkbenchShell(), "Error", "Cannot display annotation marker on editor", e);
				}
				// System.out.println("-------------------- terminate ------------------------");
				// annotationMgr.printBitList(event.getAllProcesses().toBitList());
				fireTerminatedEvent(job, event.getAllProcesses().toBitList());
				if (job.isAllStop()) {
					condition = new Boolean(true);
					annotationMgr.removeAnnotationGroup(job.getIDString());
				}
			} else if (event instanceof BreakpointHitEvent) {
				// do nothing in breakpoint hit event
				continue;
			}
			firePaintListener(condition);
		}
	}
	private BitList addTasks(BitList curTasks, BitList newTasks) {
		if (curTasks.size() < newTasks.size()) {
			newTasks.or(curTasks);
			return newTasks.copy();
		} 
		curTasks.or(newTasks);
		return curTasks; 
	}
	private void removeTasks(BitList curTasks, BitList newTasks) {
		curTasks.andNot(newTasks);
	}
	public void fireSuspendEvent(IPJob job, BitList tasks) {
		IElementHandler elementHandler = getElementHandler(job.getIDString());
		BitList suspendedTasks = (BitList) elementHandler.getData(SUSPENDED_PROC_KEY);
		suspendedTasks = addTasks(suspendedTasks, tasks);
		fireDebugEvent(new SuspendedDebugEvent(job.getIDString(), suspendedTasks, (BitList) elementHandler.getData(TERMINATED_PROC_KEY)));
	}
	public void fireResumeEvent(IPJob job, BitList tasks) {
		IElementHandler elementHandler = getElementHandler(job.getIDString());
		BitList suspendedTasks = (BitList) elementHandler.getData(SUSPENDED_PROC_KEY);
		removeTasks(suspendedTasks, tasks);
		fireDebugEvent(new ResumedDebugEvent(job.getIDString(), suspendedTasks, (BitList) elementHandler.getData(TERMINATED_PROC_KEY)));
	}
	public void fireTerminatedEvent(IPJob job, BitList tasks) {
		IElementHandler elementHandler = getElementHandler(job.getIDString());
		BitList suspendedTasks = (BitList) elementHandler.getData(SUSPENDED_PROC_KEY);
		removeTasks(suspendedTasks, tasks);
		BitList terminatedTasks = (BitList) elementHandler.getData(TERMINATED_PROC_KEY);
		terminatedTasks = addTasks(terminatedTasks, tasks);
		fireDebugEvent(new TerminatedDebugEvent(job.getIDString(), terminatedTasks, suspendedTasks));
	}
	// ONLY for detect the debug sesssion is created
	public void update(IPCDISession session) {
		createEventListener(getCurrentJobId());
	}
	/*******************************************************************************************************************************************************************************************************************************************************************************************************
	 * debug actions
	 ******************************************************************************************************************************************************************************************************************************************************************************************************/
	public void resume() throws CoreException {
		resume(getCurrentJobId(), getCurrentSetId());
	}
	public void resume(String job_id, String set_id) throws CoreException {
		IPCDISession session = (IPCDISession) getDebugSession(job_id);
		if (session == null)
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No session found", null));
		session.resume(set_id);
	}
	public void suspend() throws CoreException {
		suspend(getCurrentJobId(), getCurrentSetId());
	}
	public void suspend(String job_id, String set_id) throws CoreException {
		IPCDISession session = (IPCDISession) getDebugSession(job_id);
		if (session == null)
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No session found", null));
		session.suspend(set_id);
	}
	public void terminate() throws CoreException {
		terminate(getCurrentJobId(), getCurrentSetId());
	}
	public void terminate(String job_id, String set_id) throws CoreException {
		IPCDISession session = (IPCDISession) getDebugSession(job_id);
		if (session == null)
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No session found", null));
		session.terminate(set_id);
	}
	public void stepInto() throws CoreException {
		stepInto(getCurrentJobId(), getCurrentSetId());
	}
	public void stepInto(String job_id, String set_id) throws CoreException {
		IPCDISession session = (IPCDISession) getDebugSession(job_id);
		if (session == null)
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No session found", null));
		session.stepInto(set_id);
	}
	public void stepOver() throws CoreException {
		stepOver(getCurrentJobId(), getCurrentSetId());
	}
	public void stepOver(String job_id, String set_id) throws CoreException {
		IPCDISession session = (IPCDISession) getDebugSession(job_id);
		if (session == null)
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No session found", null));
		session.stepOver(set_id);
	}
	public void stepReturn() throws CoreException {
		stepReturn(getCurrentJobId(), getCurrentSetId());
	}
	public void stepReturn(String job_id, String set_id) throws CoreException {
		IPCDISession session = (IPCDISession) getDebugSession(job_id);
		if (session == null)
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No session found", null));
		session.stepFinish(set_id);
	}
	
	public void removeJob(IPJob job) {
		super.removeJob(job);
		try {
			PCDIDebugModel.deletePBreakpointBySet(job.getIDString());
		} catch (CoreException e) {
			PTPDebugUIPlugin.log(e);
		}
	}
}
