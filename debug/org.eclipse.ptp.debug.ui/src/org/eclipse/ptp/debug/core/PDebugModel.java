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
package org.eclipse.ptp.debug.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.ptp.debug.core.model.IPBreakpoint;
import org.eclipse.ptp.debug.core.model.IPLineBreakpoint;
import org.eclipse.ptp.debug.internal.core.breakpoints.PLineBreakpoint;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;

/**
 * @author Clement chu
 *
 */
public class PDebugModel {
	private static PDebugModel instance = null;
	
	public static PDebugModel getDefault() {
		if (instance == null)
			instance = new PDebugModel();
		
		return instance;
	}
	
	public IBreakpointManager getBreakpointManager() {
		return DebugPlugin.getDefault().getBreakpointManager();
	}
	
	/*
	//TODO ONLY workwith CBreakpoint
	public static IBreakpoint addRemoveBreakpoint(ICLineBreakpoint breakpoint, String set_id, String job_id) throws CoreException {
		int lineNumber = breakpoint.getLineNumber();
		boolean enabled = breakpoint.isEnabled();
		IResource resource = breakpoint.getMarker().getResource();
		String sourceHandle = breakpoint.getSourceHandle();
		
		return addRemoveBreakpoint(sourceHandle, resource, lineNumber, enabled, true, set_id, job_id);
	}
	public static IBreakpoint addRemoveBreakpoint(String sourceHandle, IResource resource, int lineNumber, boolean enabled, boolean register, String set_id, String job_id) throws CoreException {
		IPLineBreakpoint breakpoint = lineBreakpointExists(sourceHandle, resource, lineNumber);
		if (breakpoint == null)
			return createLineBreakpoint(sourceHandle, resource, lineNumber, enabled, 0, "", register, set_id, job_id);

		getBreakpointManager().removeBreakpoint(breakpoint, true);
		return null;
	}
	*/
	public static IPLineBreakpoint lineBreakpointExists(String sourceHandle, IResource resource, int lineNumber, String set_id, String job_id) throws CoreException {
		IBreakpoint[] breakpoints = getPBreakpoints();
		for(int i=0; i<breakpoints.length; i++) {
			if (!(breakpoints[i] instanceof IPLineBreakpoint))
				continue;

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
	public static boolean sameSourceHandle(String handle1, String handle2) {
		if (handle1 == null || handle2 == null)
			return false;
		IPath path1 = new Path(handle1);
		IPath path2 = new Path(handle2);
		if (path1.isValidPath(handle1) && path2.isValidPath(handle2))
			return path1.equals(path2);

		return handle1.equals(handle2);
	}	

	public static IBreakpoint createLineBreakpoint(String sourceHandle, IResource resource, int lineNumber, boolean enabled, int ignoreCount, String condition, boolean register, String set_id, String job_id) throws CoreException {
		HashMap attributes = new HashMap(10);
		attributes.put(IBreakpoint.ID, PTPDebugUIPlugin.getUniqueIdentifier());
		attributes.put(IMarker.LINE_NUMBER, new Integer(lineNumber));
		attributes.put(IBreakpoint.ENABLED, new Boolean(enabled));
		attributes.put(IPBreakpoint.SOURCE_HANDLE, sourceHandle);
		attributes.put(IPBreakpoint.IGNORE_COUNT, new Integer(ignoreCount));
		attributes.put(IPBreakpoint.CONDITION, condition);
		attributes.put(IPBreakpoint.SET_ID, set_id);
		attributes.put(IPBreakpoint.CUR_SET_ID, set_id);
		attributes.put(IPBreakpoint.JOB_ID, job_id);
		
		return new PLineBreakpoint(resource, attributes, register);
	}
	
	public static IBreakpoint[] getPBreakpoints() {
		return PDebugModel.getDefault().getBreakpointManager().getBreakpoints(PTPDebugUIPlugin.getUniqueIdentifier());
	}
	
	public static IPBreakpoint isPBreakpointExisted(IPLineBreakpoint breakpoint) throws CoreException {
		int lineNumber = breakpoint.getLineNumber();
		IResource resource = breakpoint.getMarker().getResource();
		String sourceHandle = breakpoint.getSourceHandle();
		
		return lineBreakpointExists(sourceHandle, resource, lineNumber);
	}

	public static IPLineBreakpoint lineBreakpointExists(String sourceHandle, IResource resource, int lineNumber) throws CoreException {
		IBreakpoint[] breakpoints = getPBreakpoints();
		for(int i=0; i<breakpoints.length; i++) {
			if (!(breakpoints[i] instanceof IPLineBreakpoint))
				continue;

			IPLineBreakpoint breakpoint = (IPLineBreakpoint)breakpoints[i];
			if (sameSourceHandle(sourceHandle, breakpoint.getSourceHandle())) {
				if (breakpoint.getMarker().getResource().equals(resource)) {
					if (breakpoint.getLineNumber() == lineNumber) {
						return breakpoint;
					}
				}
			}
		}
		return null;
	}
	
	public static IBreakpoint[] findPBreakpointsBySet(String set_id) throws CoreException {
		List bptList = new ArrayList();
		IBreakpoint[] breakpoints = getPBreakpoints();
		for(int i=0; i<breakpoints.length; i++) {
			if (!(breakpoints[i] instanceof IPLineBreakpoint))
				continue;

			IPLineBreakpoint breakpoint = (IPLineBreakpoint)breakpoints[i];
			if (breakpoint.getSetId().equals(set_id)) {
				bptList.add(breakpoint);
			}
		}
		return (IBreakpoint[])bptList.toArray(new IBreakpoint[bptList.size()]);
	}
	
	public static void deletePBreakpointBySet(final String set_id) throws CoreException {
		IWorkspaceRunnable runnable= new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				final IBreakpoint[] breakpoints = findPBreakpointsBySet(set_id);
				if (breakpoints.length > 0) {
					new Job("Remove breakpoint") {
						protected IStatus run(IProgressMonitor pmonitor) {
							try {
								PDebugModel.getDefault().getBreakpointManager().removeBreakpoints(breakpoints, true);					
	                            return Status.OK_STATUS;
	                        } catch (CoreException e) {
	                            PTPDebugUIPlugin.log(e);
	                        }
	                        return Status.CANCEL_STATUS;
						}
					}.schedule();
				}
			}
		};
		ResourcesPlugin.getWorkspace().run(runnable, null, 0, null);
	}
	
	public static void updatePBreakpoints(final String job_id, final String set_id) throws CoreException {
		IWorkspaceRunnable runnable= new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				final IBreakpoint[] breakpoints = getPBreakpoints();
				new Job("Update breakpoint") {
					protected IStatus run(IProgressMonitor pmonitor) {
						try {				
							for (int i=0; i<breakpoints.length; i++) {
								if (!(breakpoints[i] instanceof IPBreakpoint))
									continue;
					
								IPBreakpoint breakpoint = (IPBreakpoint)breakpoints[i];
								if (breakpoint.getJobId().length() == 0)
									breakpoint.setJobId(job_id);
					
								breakpoint.setCurSetId(set_id);
							}
							return Status.OK_STATUS;
						} catch (CoreException e) {
							PTPDebugUIPlugin.log(e);
						}
						return Status.CANCEL_STATUS;
					}
				}.schedule();
			}
		};
		ResourcesPlugin.getWorkspace().run(runnable, null, 0, null);
	}
}
