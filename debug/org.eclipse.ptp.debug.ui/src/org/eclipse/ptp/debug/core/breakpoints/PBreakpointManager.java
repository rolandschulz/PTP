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
package org.eclipse.ptp.debug.core.breakpoints;

import java.util.HashMap;

import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;

/**
 * @author Clement chu
 *
 */
public class PBreakpointManager {
	private static PBreakpointManager instance = null;
	
	public static PBreakpointManager getDefault() {
		if (instance == null)
			instance =  new PBreakpointManager();
		return instance;
	}
	
	public IBreakpointManager getBreakpointManager() {
		return DebugPlugin.getDefault().getBreakpointManager();
	}
	
	//TODO ONLY workwith CBreakpoint
	public IBreakpoint addRemoveBreakpoint(ICLineBreakpoint lineBkpt, String set_id, String job_id) throws CoreException {
		int lineNumber = lineBkpt.getLineNumber();
		boolean enabled = lineBkpt.isEnabled();
		IResource resource = lineBkpt.getMarker().getResource();
		String sourceHandle = lineBkpt.getSourceHandle();
		
		return addRemoveBreakpoint(sourceHandle, resource, lineNumber, enabled, true, set_id, job_id);
	}
	
	public IBreakpoint addRemoveBreakpoint(String sourceHandle, IResource resource, int lineNumber, boolean enabled, boolean register, String set_id, String job_id) throws CoreException {
		IBreakpoint breakpoint = isBreakpointExisted(sourceHandle, resource, lineNumber, set_id, job_id);
		if (breakpoint == null)
			return createLineBreakpoint(sourceHandle, resource, lineNumber, enabled, register, set_id, job_id);

		getBreakpointManager().removeBreakpoint(breakpoint, true);
		return null;
	}
	public IBreakpoint isBreakpointExisted(String sourceHandle, IResource resource, int lineNumber, String set_id, String job_id) throws CoreException {
		IBreakpoint[] breakpoints = getPBreakpoints();
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
	
	public IBreakpoint[] getPBreakpoints() {
		return getBreakpointManager().getBreakpoints(PTPDebugUIPlugin.getUniqueIdentifier());
	}
	
}
