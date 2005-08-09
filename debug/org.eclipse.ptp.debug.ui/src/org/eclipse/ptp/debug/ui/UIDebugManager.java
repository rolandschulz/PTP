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

import java.util.HashMap;

import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointListener;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.debug.core.DebugManager;
import org.eclipse.ptp.debug.core.PProcess;
import org.eclipse.ptp.debug.core.breakpoints.IPBreakpoint;
import org.eclipse.ptp.debug.core.breakpoints.IPLineBreakpoint;
import org.eclipse.ptp.debug.core.breakpoints.PLineBreakpoint;
import org.eclipse.ptp.debug.internal.core.model.PDebugTarget;
import org.eclipse.ptp.ui.JobManager;
import org.eclipse.ptp.ui.MachineManager;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.model.IElementSet;
import org.eclipse.ptp.ui.model.ISetManager;
import org.eclipse.ptp.ui.model.internal.Element;
import org.eclipse.ptp.ui.model.internal.SetManager;

/**
 * @author clement chu
 *
 */
public class UIDebugManager extends JobManager implements IBreakpointListener {
	public final static int PROC_SUSPEND = 6;
	public final static int PROC_HIT = 7;

	//FIXME dummy only
	public boolean dummy = false;
	
	public UIDebugManager() {
		getBreakpointManager().addBreakpointListener(this);
	}
	
	public void shutdown() {
		super.shutdown();
		getBreakpointManager().removeBreakpointListener(this);
	}
		
	public int getProcessStatus(String job_id, String proc_id) {
		//FIXME dummy only 
		if (dummy)
			return getDummyStatus(proc_id);

		return super.getProcessStatus(job_id, proc_id);
	}
	
	//FIXME dummy only 
	public int getDummyStatus(String id) {
		String status = DebugManager.getInstance().getProcess(id).getStatus();
		if (status.equals(IPProcess.STARTING))
			return MachineManager.PROC_STARTING;
		else if (status.equals(IPProcess.RUNNING))
			return MachineManager.PROC_RUNNING;
		else if (status.equals(IPProcess.EXITED))
			return MachineManager.PROC_EXITED;
		else if (status.equals(IPProcess.EXITED_SIGNALLED))
			return MachineManager.PROC_EXITED_SIGNAL;
		else if (status.equals(IPProcess.STOPPED))
			return MachineManager.PROC_STOPPED;
		else
			return MachineManager.PROC_ERROR;
	}
	
	//FIXME don't know whether it return machine or job
	public String getName(String id) {
		//FIXME dummy only
		if (dummy)
			return "dummy";
		
		return super.getName(id);
	}
	
	//FIXME dummy only
	private String dummyInitialProcess() {
		PProcess[] processes = DebugManager.getInstance().getProcesses();
		if (processes.length > 0) {
			ISetManager setManager = new SetManager();
			setManager.clearAll();
			IElementSet set = setManager.getSetRoot();
			for (int j=0; j<processes.length; j++) {
				set.add(new Element(processes[j].getID()));
			}
			jobList.put("dummy", setManager);
			return "dummy";
		}
		return "";
	}
	
	public String initial() {
		//FIXME dummy only
		if (dummy) {
			return dummyInitialProcess();
		}
		
		return super.initial();
	}	
	
	public void unregisterElements(ILaunch launch, PDebugTarget target, IElement[] elements) {
		for (int i=0; i<elements.length; i++) {
			//only unregister some registered elements
			if (elements[i].isRegistered()) {
				//TODO unregister in selected elements in debug view 
			}
		}
	}
	
	public void unregisterElements(IElement[] elements) {
		try {
			ILaunch launch = getLaunch();
			unregisterElements(launch, (PDebugTarget)launch.getDebugTarget(), elements);
		} catch (CoreException e) {
			System.out.println(e.getMessage());
		}
	}
	
	public void registerElements(ILaunch launch, PDebugTarget target, IElement[] elements) {
		//FIXME dummy only 
		if (dummy)
			return;
		
		for (int i=0; i<elements.length; i++) {
			//only register some unregistered elements
			if (!elements[i].isRegistered()) {
				//target.register(elements[i].getIDNum());
			}
		}
	}
	public void registerElements(IElement[] elements) {
		try {
			ILaunch launch = getLaunch();
			registerElements(launch, (PDebugTarget)launch.getDebugTarget(), elements);
		} catch (CoreException e) {
			System.out.println(e.getMessage());
		}
	}
	
	public ILaunch getLaunch() throws CoreException {
		ILaunch[] launches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
		for (int i=0; i<launches.length; i++) {
			if (launches[i].getDebugTarget() instanceof PDebugTarget)
				return launches[i];
		}
		throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "No launch found", null));
	}
	
	/*********************************************************************
	 * Breakpoint
	 *********************************************************************/
	
	public void breakpointAdded(IBreakpoint breakpoint) {
		if (PTPDebugUIPlugin.getDefault().getCurrentPerspectiveID().equals(IPTPDebugUIConstants.PERSPECTIVE_DEBUG)) {
			if (breakpoint instanceof ICLineBreakpoint) {
				IBreakpointManager breakpointManager = getBreakpointManager();
				try {
					addBreakpoint((ICLineBreakpoint)breakpoint);
					breakpointManager.removeBreakpoint(breakpoint, true);
				} catch (CoreException e) {
					System.out.println("Err: " + e.getMessage());
				}
			}
		}
	}
	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
		
	}
	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
		
	}
	
	public IBreakpointManager getBreakpointManager() {
		return DebugPlugin.getDefault().getBreakpointManager();
	}
	
	//TODO should move this method to debug.core
	public IBreakpoint createLineBreakpoint(String sourceHandle, IResource resource, int lineNumber, boolean enabled, boolean register, String set_id, String job_id) throws CoreException {
		HashMap attributes = new HashMap(10);
		attributes.put(IBreakpoint.ID, PTPDebugUIPlugin.getUniqueIdentifier());
		attributes.put(IMarker.LINE_NUMBER, new Integer(lineNumber));
		attributes.put(IBreakpoint.ENABLED, new Boolean(enabled));
		attributes.put(IPBreakpoint.SOURCE_HANDLE, sourceHandle);
		attributes.put(IPBreakpoint.SET_ID, set_id);
		attributes.put(IPBreakpoint.JOB_ID, job_id);
		return new PLineBreakpoint(resource, attributes, register);
	}
	//TODO ONLY workwith CBreakpoint
	public void addBreakpoint(ICLineBreakpoint lineBkpt) throws CoreException {
		int lineNumber = lineBkpt.getLineNumber();
		boolean enabled = lineBkpt.isEnabled();
		IProject project = lineBkpt.getMarker().getResource().getProject();
		String sourceHandle = lineBkpt.getSourceHandle();
		
		addBreakpoint(sourceHandle, project, lineNumber, enabled, true, getCurrentSetId(), getCurrentJobId());
	}
	
	public void addBreakpoint(String sourceHandle, IResource resource, int lineNumber, boolean enabled, boolean register, String set_id, String job_id) throws CoreException {
		IBreakpoint breakpoint = isBreakpointExisted(sourceHandle, resource, lineNumber, set_id, job_id);
		if (breakpoint != null) {
			getBreakpointManager().removeBreakpoint(breakpoint, true);
		} else
			createLineBreakpoint(sourceHandle, resource, lineNumber, enabled, register, set_id, job_id);
	}
	public IBreakpoint isBreakpointExisted(String sourceHandle, IResource resource, int lineNumber, String set_id, String job_id) throws CoreException {
		String modelId = PTPDebugUIPlugin.getUniqueIdentifier();
		IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
		IBreakpoint[] breakpoints = manager.getBreakpoints(modelId);
		for(int i=0; i<breakpoints.length; i++) {
			if (!(breakpoints[i] instanceof IPLineBreakpoint)) {
				continue;
			}
			IPLineBreakpoint breakpoint = (IPLineBreakpoint)breakpoints[i];
			if (sameSourceHandle(sourceHandle, breakpoint.getSourceHandle())) {
				if (breakpoint.getMarker().getResource().equals(resource)) {
					if (breakpoint.getLineNumber() == lineNumber && breakpoint.getSetId().equals(set_id) && breakpoint.getJobId().equals(job_id)) {
						return breakpoint;
					}
				}
			}
		}
		return null;		
	}
	private static boolean sameSourceHandle(String handle1, String handle2) {
		if (handle1 == null || handle2 == null)
			return false;
		IPath path1 = new Path(handle1);
		IPath path2 = new Path(handle2);
		if (path1.isValidPath(handle1) && path2.isValidPath(handle2)) {
			return path1.equals(path2);
		}
		return handle1.equals(handle2);
	}	
}
