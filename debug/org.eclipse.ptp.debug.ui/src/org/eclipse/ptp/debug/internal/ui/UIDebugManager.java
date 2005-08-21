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

import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.debug.core.IPDebugListener;
import org.eclipse.ptp.debug.core.PDebugModel;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.cdi.IPCDISession;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIEvent;
import org.eclipse.ptp.debug.core.utils.BitList;
import org.eclipse.ptp.debug.external.cdi.event.TargetRegisteredEvent;
import org.eclipse.ptp.debug.external.cdi.event.TargetUnregisteredEvent;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.debug.ui.listeners.IRegListener;
import org.eclipse.ptp.internal.ui.JobManager;
import org.eclipse.ptp.ui.PTPUIPlugin;
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
	private final static int REG_TYPE = 1;
	private final static int UNREG_TYPE = 2;
	
	private List regListeners = new ArrayList();
	private PAnnotationManager annotationMgr = null;
	
	public UIDebugManager() {
		PTPUIPlugin.getDefault().getUIManager().addSetListener(this);
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
		PTPDebugCorePlugin.getDefault().addDebugSessionListener(this);
		annotationMgr = new PAnnotationManager(this);
	}
	
	public void shutdown() {
		PTPUIPlugin.getDefault().getUIManager().removeSetListener(this);
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
		PTPDebugCorePlugin.getDefault().removeDebugSessionListener(this);
		regListeners.clear();
		annotationMgr.shutdown();
		super.shutdown();
	}
	
	public boolean isDebugging(String job_id) {
		if (isNoJob(job_id))
			return false;
		
		IPJob job = findJobById(job_id);
		return (job != null && !job.isAllStop() && job.isDebug());
	}
	
	public void jobChangedEvent(String cur_jid, String pre_jid) {
		super.jobChangedEvent(cur_jid, pre_jid);
		updateBreakpointMarker(IElementHandler.SET_ROOT_ID);
		createEventListener(cur_jid);
	}

	public void addRegListener(IRegListener listener) {
		if (!regListeners.contains(listener))
			regListeners.add(listener);
	}
	public void removeRegListener(IRegListener listener) {
		if (regListeners.contains(listener))
			regListeners.remove(listener);
	}
	public void fireRegListener(int type, BitList tasks) {
		for (Iterator i=regListeners.iterator(); i.hasNext();) {
			switch (type) {
			case REG_TYPE:
				((IRegListener)i.next()).register(tasks);
				break;
			case UNREG_TYPE:
				((IRegListener)i.next()).unregister(tasks);
			}
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
		return PTPDebugCorePlugin.getDefault().getDebugSession(job);
	}

	public int convertToInt(String id) {
		return Integer.parseInt(id);
	}
	public void registerProcess(IPCDISession session, int task_id, boolean isChanged) {
		session.registerTarget(task_id, isChanged);
	}
	public void unregisterProcess(IPCDISession session, int task_id, boolean isChanged) {
		session.unregisterTarget(task_id, isChanged);
	}
	
	public void unregisterElements(IElement[] elements) {
		if (isJobStop(getCurrentJobId()))
			return;
		
		IPCDISession session = (IPCDISession)getDebugSession(getCurrentJobId());
		if (session == null)
			return;		
		for (int i=0; i<elements.length; i++) {			
			//only unregister some registered elements
			if (elements[i].isRegistered()) {
				int taskId = convertToInt(elements[i].getName());
				unregisterProcess(session, taskId, true);
			}
		}
	}
	public void registerElements(IElement[] elements) {
		if (isJobStop(getCurrentJobId()))
			return;

		IPCDISession session = (IPCDISession)getDebugSession(getCurrentJobId());
		if (session == null)
			return;

		for (int i=0; i<elements.length; i++) {
			//only register some unregistered elements
			if (!elements[i].isRegistered()) {
				int taskId = convertToInt(elements[i].getName());
				registerProcess(session, taskId, true);
			}
		}
	}
	
	/******
	 * Breakpoint
	 ******/
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

	/*****
	 * Element Set
	 *****/
	public void updateBreakpointMarker(final String cur_sid) {
		try {
			PDebugModel.updatePBreakpoints(getCurrentJobId(), cur_sid);
		} catch (CoreException e) {
			PTPDebugUIPlugin.log(e);
		}				
	}
	public void changeSetEvent(final IElementSet curSet, final IElementSet preSet) {
		updateBreakpointMarker(curSet.getID());

		final IElementHandler elementHandler = getElementHandler(getCurrentJobId());
		if (elementHandler == null)
			return;
		
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				new Job("Updating") {
					protected IStatus run(IProgressMonitor pmonitor) {
						IPCDISession session = (IPCDISession)getDebugSession(getCurrentJobId());
						String[] registerElementsID = elementHandler.getRegisteredElementsID();
						for (int i=0; i<registerElementsID.length; i++) {
							if (curSet.contains(registerElementsID[i])) {
								if (!preSet.contains(registerElementsID[i])) {
									int taskID = convertToInt(elementHandler.getSetRoot().get(registerElementsID[i]).getName());				
									registerProcess(session, taskID, false);
									BitList tasks = new BitList();
									tasks.set(taskID);
									fireRegListener(UNREG_TYPE, tasks);
								}
							} 
							else {
								int taskID = convertToInt(elementHandler.getSetRoot().get(registerElementsID[i]).getName());				
								unregisterProcess(session, taskID, false);
								BitList tasks = new BitList();
								tasks.set(taskID);
								fireRegListener(UNREG_TYPE, tasks);
								
							}
						}
						//update annotation
						return Status.OK_STATUS;
					}
				}.schedule();
			}
		};
		try {
			ResourcesPlugin.getWorkspace().run(runnable, null, 0, null);
			annotationMgr.updateAnnotation(curSet, preSet);
		} catch (CoreException e) {
			PTPDebugUIPlugin.log(e);
		}
	}
	//Delete the set that will delete the all related breakpoint
	public void deleteSetEvent(IElementSet set) {
		try {
			PDebugModel.deletePBreakpointBySet(set.getID());
		} catch (CoreException e) {
			PTPDebugUIPlugin.log(e);
		}
		set.setData(BITSET_KEY, null);		
	}
	public void createSetEvent(IElementSet set, IElement[] elements) {
		BitList tasks = new BitList();
		for (int i=0; i<elements.length; i++) {
			tasks.set(convertToInt(elements[i].getName()));
		}
		set.setData(BITSET_KEY, tasks);
	}
	public void addElementsEvent(IElementSet set, IElement[] elements) {
		BitList tasks = (BitList)set.getData(BITSET_KEY);
		BitList addTasks = new BitList();
		for (int i=0; i<elements.length; i++) {
			tasks.set(convertToInt(elements[i].getName()));
		}
		tasks.or(addTasks);
	}
	public void removeElementsEvent(IElementSet set, IElement[] elements) {
		BitList tasks = (BitList)set.getData(BITSET_KEY);
		BitList addTasks = new BitList();
		for (int i=0; i<elements.length; i++) {
			tasks.set(convertToInt(elements[i].getName()));
		}
		tasks.andNot(addTasks);
	}

    	/*
    	 * Cannot unregister the extension
    	final String CDT_DEBUG_UI_ID = "org.eclipse.cdt.debug.ui";    		
		Bundle bundle = Platform.getBundle(CDT_DEBUG_UI_ID);
		if (bundle != null && bundle.getState() == Bundle.ACTIVE) {
			//ExtensionRegistry reg = (ExtensionRegistry) Platform.getExtensionRegistry();
			//reg.remove(bundle.getBundleId());
			try {
				Platform.getBundle(CDT_DEBUG_UI_ID).uninstall();
				System.out.println("Remove: " + bundle.getState());
			} catch (BundleException e) {
				System.out.println("Err: " + e.getMessage());
			}
		} 
		*/
	
	/*****
	 * Event
	 *****/
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
			ResourcesPlugin.getWorkspace().run(runnable, null, 0, null);
		} catch (CoreException e) {
			PTPDebugUIPlugin.log(e);
		}
	}
	
	private void handleDebugEvents(ICDIEvent[] events, IProgressMonitor monitor) {
		for (int i=0; i<events.length; i++) {
			IPCDIEvent event = (IPCDIEvent)events[i];
			if (event instanceof TargetRegisteredEvent) {
				IPJob job = event.getDebugJob();
				if (job == null)
					continue;
				
				IElementHandler elementHandler = getElementHandler(job.getIDString());
				int[] processes = event.getAllProcesses().toIntArray();
				for (int j=0; j<processes.length; j++) {
					IPProcess proc = job.findProcessByTaskId(processes[i]);
					elementHandler.addRegisterElement(proc.getIDString());
					elementHandler.getSetRoot().get(proc.getIDString()).setRegistered(true);
				}
				fireRegListener(REG_TYPE, event.getAllProcesses().toBitList());
			}
			else if (event instanceof TargetUnregisteredEvent) {
				IPJob job = event.getDebugJob();
				if (job == null)
					continue;
				
				IElementHandler elementHandler = getElementHandler(job.getIDString());
				int[] processes = event.getAllProcesses().toIntArray();
				for (int j=0; j<processes.length; j++) {
					IPProcess proc = job.findProcessByTaskId(processes[i]);
					elementHandler.removeRegisterElement(proc.getIDString());
					elementHandler.getSetRoot().get(proc.getIDString()).setRegistered(false);
				}
				fireRegListener(UNREG_TYPE, event.getAllProcesses().toBitList());
			}
			firePaintListener();
		}
	}
	
	//ONLY for detect the debug sesssion is created
	public void update(IPCDISession session) {
		createEventListener(getCurrentJobId());
	}
}
