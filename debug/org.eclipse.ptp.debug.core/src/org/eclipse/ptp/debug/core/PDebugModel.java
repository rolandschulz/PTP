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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.event.IPDebugEvent;
import org.eclipse.ptp.debug.core.event.IPDebugInfo;
import org.eclipse.ptp.debug.core.event.PDebugEvent;
import org.eclipse.ptp.debug.core.event.PDebugRegisterInfo;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.debug.core.model.IPAddressBreakpoint;
import org.eclipse.ptp.debug.core.model.IPBreakpoint;
import org.eclipse.ptp.debug.core.model.IPDebugTarget;
import org.eclipse.ptp.debug.core.model.IPFunctionBreakpoint;
import org.eclipse.ptp.debug.core.model.IPLineBreakpoint;
import org.eclipse.ptp.debug.core.model.IPWatchpoint;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.model.IPDITarget;
import org.eclipse.ptp.debug.internal.core.PSession;
import org.eclipse.ptp.debug.internal.core.PSessionManager;
import org.eclipse.ptp.debug.internal.core.breakpoint.PAddressBreakpoint;
import org.eclipse.ptp.debug.internal.core.breakpoint.PFunctionBreakpoint;
import org.eclipse.ptp.debug.internal.core.breakpoint.PLineBreakpoint;
import org.eclipse.ptp.debug.internal.core.breakpoint.PWatchpoint;
import org.eclipse.ptp.debug.internal.core.model.PDebugTarget;

/**
 * @author clement chu
 * 
 */
public class PDebugModel {
	//private final String SESSION_KEY = "session_key";
	//private DebugJobStorage jobStorage = new DebugJobStorage("Job");
	//private DebugJobStorage sessionStorage = new DebugJobStorage("Session");
	private PSessionManager sessionMgr = new PSessionManager();
	
	public IPSession getSession(IPJob job) {
		return sessionMgr.getSession(job);
	}
	public void shutdown() {
		for (IPSession session : sessionMgr.getSessions()) {
			shutdownnSession(session);
		}
		sessionMgr.shutdown();
	}
	public void shutdownSession(IPJob job) {
		if (job != null) {
			shutdownnSession(getSession(job));
		}
	}
	public void shutdownnSession(IPSession session) {
		if (session != null) {
			try {
				deletePBreakpoint(session.getJob().getID());
			}
			catch (CoreException e) {
				PTPDebugCorePlugin.log(e);
			}
			session.dispose();
		}
	}
	public IPSession createDebugSession(long timeout, IPTPDebugger debugger, IPLaunch launch, IFile exec, IPath coreFile, IProgressMonitor monitor) throws CoreException {
		PSession session = new PSession(debugger.createDebugSession(timeout, launch, exec, coreFile, monitor), launch, exec, monitor);
		sessionMgr.addSession(launch.getPJob(), session);
		session.connectToDebugger(monitor);
		return session;
	}
	public static String getPluginIdentifier() {
		return PTPDebugCorePlugin.getUniqueIdentifier();
	}
	public void fireRegisterEvent(IPJob job, BitList tasks, boolean refresh) {
		if (!tasks.isEmpty()) {
			IPSession session = getSession(job);
			if (session != null) {
				IPDebugInfo info = new PDebugRegisterInfo(job, tasks, null, null, refresh);
				PTPDebugCorePlugin.getDefault().fireDebugEvent(new PDebugEvent(session, IPDebugEvent.CREATE, IPDebugEvent.REGISTER, info));
			}
		}
	}
	public void fireUnregisterEvent(IPJob job, BitList tasks, boolean refresh) {
		if (!tasks.isEmpty()) {
			IPSession session = getSession(job);
			if (session != null) {
				IPDebugInfo info = new PDebugRegisterInfo(job, tasks, null, null, refresh);
				PTPDebugCorePlugin.getDefault().fireDebugEvent(new PDebugEvent(session, IPDebugEvent.TERMINATE, IPDebugEvent.REGISTER, info));
			}
		}
	}
	/**************************************************
	 * Register / Unregister
	 **************************************************/
	public void removeDebugTarget(final IPLaunch launch, final BitList tasks, final boolean refresh) {
		WorkspaceJob aJob = new WorkspaceJob("Removing the debug targets...") {
			public IStatus runInWorkspace(IProgressMonitor monitor) {
				int[] taskArray = tasks.toArray();
				for (int task : taskArray) {
					IPDebugTarget target = launch.getDebugTarget(task);
					if (target != null) {
						target.dispose();
						launch.removeDebugTarget(target);
					}
				}
				fireUnregisterEvent(launch.getPJob(), tasks, refresh);
				return Status.OK_STATUS;
			}
		};
		aJob.setSystem(true);
		aJob.schedule();		
	}
	public void addNewDebugTargets(final IPLaunch launch, final BitList tasks, final IPDITarget[] pdiTargets, final boolean refresh, final boolean resumeTarget) {
		WorkspaceJob aJob = new WorkspaceJob("Creating new debug targets...") {
			public IStatus runInWorkspace(IProgressMonitor monitor) {
				boolean allowTerminate = true;
				boolean allowDisconnect = false;
				IPSession session = getSession(launch.getPJob());
				if (session == null)
					return Status.CANCEL_STATUS;
				for (IPDITarget pdiTarget: pdiTargets) {
					IPDebugTarget target = new PDebugTarget((PSession)session, pdiTarget, allowTerminate, allowDisconnect);
					if (resumeTarget) {
						try {
							session.getPDISession().resume(target.getTasks(), false);
						}
						catch (PDIException e) {
							PTPDebugCorePlugin.log(e);
						}
					}
					launch.addDebugTarget(target);
				}
				fireRegisterEvent(launch.getPJob(), tasks, refresh);
				return Status.OK_STATUS;
			}
		};
		aJob.setSystem(true);
		aJob.schedule();
	}
	/**************************************************
	 * Breakpoint
	 **************************************************/
	public static IPLineBreakpoint createLineBreakpoint(String sourceHandle, IResource resource, int lineNumber, boolean enabled, int ignoreCount, String condition, boolean register, String set_id, IPJob job) throws CoreException {
		String job_id = IPBreakpoint.GLOBAL;
		String jobName = IPBreakpoint.GLOBAL;
		if (job != null && !job.isTerminated()) {
			IPSession session = PTPDebugCorePlugin.getDebugModel().getSession(job);
			if (session != null) {
				job_id = job.getID();
				jobName = job.getName();
			}
		}
		HashMap<String, Object> attributes = new HashMap<String, Object>(10);
		attributes.put(IBreakpoint.ID, getPluginIdentifier());
		attributes.put(IMarker.LINE_NUMBER, new Integer(lineNumber));
		attributes.put(IBreakpoint.ENABLED, new Boolean(enabled));
		attributes.put(IPBreakpoint.SOURCE_HANDLE, sourceHandle);
		attributes.put(IPBreakpoint.IGNORE_COUNT, new Integer(ignoreCount));
		attributes.put(IPBreakpoint.CONDITION, condition);
		attributes.put(IPBreakpoint.SET_ID, set_id);
		attributes.put(IPBreakpoint.CUR_SET_ID, set_id);
		attributes.put(IPBreakpoint.JOB_ID, job_id);
		attributes.put(IPBreakpoint.JOB_NAME, jobName);
		return new PLineBreakpoint(resource, attributes, register);
	}
	public static IPAddressBreakpoint createAddressBreakpoint(String module, String sourceHandle, IResource resource, BigInteger address, boolean enabled, int ignoreCount, String condition, boolean register, String set_id, String job_id, String jobName) throws CoreException {
		return createAddressBreakpoint(module, sourceHandle, resource, -1, address, enabled, ignoreCount, condition, register, set_id, job_id, jobName);
	}
	public static IPAddressBreakpoint createAddressBreakpoint(String module, String sourceHandle, IResource resource, int lineNumber, BigInteger address, boolean enabled, int ignoreCount, String condition, boolean register, String set_id, String job_id, String jobName) throws CoreException {
		HashMap<String, Object> attributes = new HashMap<String, Object>(10);
		attributes.put(IBreakpoint.ID, getPluginIdentifier());
		attributes.put(IMarker.CHAR_START, new Integer(-1));
		attributes.put(IMarker.CHAR_END, new Integer(-1));
		attributes.put(IMarker.LINE_NUMBER, new Integer(lineNumber));
		attributes.put(IBreakpoint.ENABLED, Boolean.valueOf(enabled));
		attributes.put(IPLineBreakpoint.ADDRESS, address.toString(10));
		attributes.put(IPBreakpoint.SOURCE_HANDLE, sourceHandle);
		attributes.put(IPBreakpoint.IGNORE_COUNT, new Integer(ignoreCount));
		attributes.put(IPBreakpoint.CONDITION, condition);
		//attributes.put(IPBreakpoint.MODULE, module);
		attributes.put(IPBreakpoint.SET_ID, set_id);
		attributes.put(IPBreakpoint.CUR_SET_ID, set_id);
		attributes.put(IPBreakpoint.JOB_ID, job_id);
		attributes.put(IPBreakpoint.JOB_NAME, jobName);
		return new PAddressBreakpoint(resource, attributes, register);
	}
	public static IPWatchpoint createWatchpoint(String sourceHandle, IResource resource, boolean writeAccess, boolean readAccess, String expression, boolean enabled, int ignoreCount, String condition, boolean register, String set_id, String job_id, String jobName) throws CoreException {
		HashMap<String, Object> attributes = new HashMap<String, Object>(10);
		attributes.put(IBreakpoint.ID, getPluginIdentifier());
		attributes.put(IBreakpoint.ENABLED, Boolean.valueOf(enabled));
		attributes.put(IPBreakpoint.SOURCE_HANDLE, sourceHandle);
		attributes.put(IPBreakpoint.IGNORE_COUNT, new Integer(ignoreCount));
		attributes.put(IPBreakpoint.CONDITION, condition);
		attributes.put(IPWatchpoint.EXPRESSION, expression );
		attributes.put(IPWatchpoint.READ, Boolean.valueOf(readAccess));
		attributes.put(IPWatchpoint.WRITE, Boolean.valueOf(writeAccess));
		return new PWatchpoint(resource, attributes, register);
	}
	public static IPWatchpoint createWatchpoint(String sourceHandle, IResource resource, int charStart, int charEnd, int lineNumber, boolean writeAccess, boolean readAccess, String expression, boolean enabled, int ignoreCount, String condition, boolean register, String set_id, String job_id, String jobName) throws CoreException {
		HashMap<String, Object> attributes = new HashMap<String, Object>(10);
		attributes.put(IBreakpoint.ID, getPluginIdentifier());
		attributes.put(IMarker.CHAR_START, new Integer(charStart));
		attributes.put(IMarker.CHAR_END, new Integer(charEnd));
		attributes.put(IMarker.LINE_NUMBER, new Integer(lineNumber));
		attributes.put(IBreakpoint.ENABLED, Boolean.valueOf(enabled));
		attributes.put(IPBreakpoint.SOURCE_HANDLE, sourceHandle);
		attributes.put(IPBreakpoint.IGNORE_COUNT, new Integer(ignoreCount));
		attributes.put(IPBreakpoint.CONDITION, condition);
		attributes.put(IPWatchpoint.EXPRESSION, expression );
		attributes.put(IPWatchpoint.READ, Boolean.valueOf(readAccess));
		attributes.put(IPWatchpoint.WRITE, Boolean.valueOf(writeAccess));
		return new PWatchpoint(resource, attributes, register);
	}
	public static IPFunctionBreakpoint createFunctionBreakpoint(String sourceHandle, IResource resource, String function, int charStart, int charEnd, int lineNumber, boolean enabled, int ignoreCount, String condition, boolean register, String set_id, String job_id, String jobName) throws CoreException {
		HashMap<String, Object> attributes = new HashMap<String, Object>(10);
		attributes.put(IBreakpoint.ID, getPluginIdentifier());
		attributes.put(IMarker.CHAR_START, new Integer(charStart));
		attributes.put(IMarker.CHAR_END, new Integer(charEnd));
		attributes.put(IMarker.LINE_NUMBER, new Integer(lineNumber));
		attributes.put(IBreakpoint.ENABLED, Boolean.valueOf(enabled));
		attributes.put(IPLineBreakpoint.FUNCTION, function);
		attributes.put(IPBreakpoint.SOURCE_HANDLE, sourceHandle);
		attributes.put(IPBreakpoint.IGNORE_COUNT, new Integer(ignoreCount));
		attributes.put(IPBreakpoint.CONDITION, condition);
		return new PFunctionBreakpoint(resource, attributes, register);
	}
	public static IPLineBreakpoint[] lineBreakpointsExists(String sourceHandle, IResource resource, int lineNumber) throws CoreException {
		IBreakpoint[] breakpoints = getPBreakpoints();
		List<IBreakpoint> foundBreakpoints = new ArrayList<IBreakpoint>(0);
		for (int i = 0; i < breakpoints.length; i++) {
			if (!(breakpoints[i] instanceof IPLineBreakpoint))
				continue;
			IPLineBreakpoint breakpoint = (IPLineBreakpoint) breakpoints[i];
			if (sameSourceHandle(sourceHandle, breakpoint.getSourceHandle())) {
				if (breakpoint.getMarker().getResource().equals(resource)) {
					if (breakpoint.getLineNumber() == lineNumber) {
						foundBreakpoints.add(breakpoint);
					}
				}
			}
		}
		return foundBreakpoints.toArray(new IPLineBreakpoint[0]);
	}
	// remove global breakpoint or the breakpoint same as job id given
	public static IPLineBreakpoint lineBreakpointExists(IPLineBreakpoint[] breakpoints, IPJob job) throws CoreException {
		for (int i = 0; i < breakpoints.length; i++) {
			String bpt_job_id = breakpoints[i].getJobId();
			if (bpt_job_id.equals(IPBreakpoint.GLOBAL) || (job != null && bpt_job_id.equals(job.getID())))
				return breakpoints[i];
		}
		return null;
	}
	public static IPWatchpoint[] watchpointExists(String sourceHandle, IResource resource, String expression) throws CoreException {
		IBreakpoint[] breakpoints = getPBreakpoints();
		List<IBreakpoint> foundBreakpoints = new ArrayList<IBreakpoint>(0);
		String markerType = PWatchpoint.getMarkerType();
		for (int i = 0; i < breakpoints.length; i++) {
			if (!(breakpoints[i] instanceof IPWatchpoint))
				continue;
			IPWatchpoint breakpoint = (IPWatchpoint) breakpoints[i];
			if (breakpoint.getMarker().getType().equals(markerType)) {
				if (sameSourceHandle(sourceHandle, breakpoint.getSourceHandle())) {
					if (breakpoint.getMarker().getResource().equals(resource)) {
						if (breakpoint.getExpression().equals(expression)) {
							foundBreakpoints.add(breakpoint);
						}
					}
				}
			}
		}
		return foundBreakpoints.toArray(new IPWatchpoint[0]);
	}
	public static IPFunctionBreakpoint[] functionBreakpointExists(String sourceHandle, IResource resource, String function) throws CoreException {
		IBreakpoint[] breakpoints = getPBreakpoints();
		List<IBreakpoint> foundBreakpoints = new ArrayList<IBreakpoint>(0);
		String markerType = PFunctionBreakpoint.getMarkerType();
		for (int i = 0; i < breakpoints.length; i++) {
			if (!(breakpoints[i] instanceof IPFunctionBreakpoint))
				continue;
			IPFunctionBreakpoint breakpoint = (IPFunctionBreakpoint) breakpoints[i];
			if (breakpoint.getMarker().getType().equals(markerType)) {
				if (sameSourceHandle(sourceHandle, breakpoint.getSourceHandle())) {
					if (breakpoint.getMarker().getResource().equals(resource)) {
						if (breakpoint.getFunction() != null && breakpoint.getFunction().equals(function)) {
							foundBreakpoints.add(breakpoint);
						}
					}
				}
			}
		}
		return foundBreakpoints.toArray(new IPFunctionBreakpoint[0]);
	}
	private static boolean sameSourceHandle(String handle1, String handle2) {
		if (handle1 == null || handle2 == null)
			return false;
		IPath path1 = new Path(handle1);
		IPath path2 = new Path(handle2);
		if (path1.isValidPath(handle1) && path2.isValidPath(handle2))
			return path1.equals(path2);
		return handle1.equals(handle2);
	}
	public static IBreakpoint[] getPBreakpoints() {
		return DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(getPluginIdentifier());
	}
	/***************************************
	 * Other breakpoint functions
	 ***************************************/
	public static IPBreakpoint[] findPBreakpoints(String job_id, String set_id) throws CoreException {
		List<IPBreakpoint> bptList = new ArrayList<IPBreakpoint>();
		IBreakpoint[] breakpoints = getPBreakpoints();
		for (IBreakpoint bpt : breakpoints) {
			if (!(bpt instanceof IPBreakpoint))
				continue;
			IPBreakpoint breakpoint = (IPBreakpoint) bpt;
			if (breakpoint.getJobId().equals(job_id) && breakpoint.getSetId().equals(set_id)) {
				bptList.add(breakpoint);
			}
		}
		return (IPBreakpoint[]) bptList.toArray(new IPBreakpoint[0]);
	}
	public static IPBreakpoint[] findPBreakpoints(String job_id, boolean includeGlobal) throws CoreException {
		List<IPBreakpoint> bptList = new ArrayList<IPBreakpoint>();
		IBreakpoint[] breakpoints = getPBreakpoints();
		for (IBreakpoint bpt : breakpoints) {
			if (!(bpt instanceof IPBreakpoint))
				continue;
			IPBreakpoint breakpoint = (IPBreakpoint) bpt;
			String bp_job_id = breakpoint.getJobId();
			if (bp_job_id.equals(job_id) || (includeGlobal && bp_job_id.equals(IPBreakpoint.GLOBAL))) {
				bptList.add(breakpoint);
			}
		}
		return (IPBreakpoint[]) bptList.toArray(new IPBreakpoint[bptList.size()]);
	}
	public static void deletePBreakpoint(final String job_id, final String set_id) throws CoreException {
		IPBreakpoint[] breakpoints = findPBreakpoints(job_id, set_id);
		if (breakpoints.length > 0)
			DebugPlugin.getDefault().getBreakpointManager().removeBreakpoints(breakpoints, true);		
	}
	public static void deletePBreakpoint(final String job_id) throws CoreException {
		IPBreakpoint[] breakpoints = findPBreakpoints(job_id, false);
		if (breakpoints.length > 0)
			DebugPlugin.getDefault().getBreakpointManager().removeBreakpoints(breakpoints, true);
	}
	public static void updatePBreakpoints(String set_id, IProgressMonitor monitor) throws CoreException {
		try {
			IBreakpoint[] breakpoints = getPBreakpoints();
			monitor.beginTask("Updating parallel breakpoint...", breakpoints.length);
			for (IBreakpoint bpt : breakpoints) {
				if (!(bpt instanceof IPBreakpoint))
					continue;
				((IPBreakpoint) bpt).setCurSetId(set_id);
				monitor.worked(1);
			}
		} finally {
			monitor.done();
		}
	}
	/**************************************************
	 * Debug Job
	 **************************************************/
	public void createSet(IPSession session, String set_id, BitList tasks) {
		session.getSetManager().createSet(set_id, tasks);
	}
	public void addTasks(IPSession session, String set_id, BitList tasks) {
		BitList curSetTasks = getTasks(session, set_id);
		session.getSetManager().addTasks(set_id, tasks);
		tasks.andNot(curSetTasks);
		try {
			IPBreakpoint[] breakpoints = findPBreakpoints(session.getJob().getID(), set_id);
			session.getBreakpointManager().addSetBreakpoints(tasks, breakpoints);
		}
		catch (CoreException e) {
			PTPDebugCorePlugin.log(e);
		}
	}
	public void removeTasks(IPSession session, String set_id, BitList tasks) {
		BitList curSetTasks = getTasks(session, set_id);
		session.getSetManager().removeTasks(set_id, tasks);
		tasks.and(curSetTasks);
		try {
			IPBreakpoint[] breakpoints = findPBreakpoints(session.getJob().getID(), set_id);
			session.getBreakpointManager().deleteSetBreakpoints(tasks, breakpoints);
		}
		catch (CoreException e) {
			PTPDebugCorePlugin.log(e);
		}
	}
	public void deleteSet(IPSession session, String set_id) {
		try {
			PDebugModel.deletePBreakpoint(session.getJob().getID(), set_id);			
		}
		catch (CoreException e) {
			PTPDebugCorePlugin.log(e);
		}
		//must delete breakpoint and then delete set
		session.getSetManager().deleteSets(set_id);
	}
	public BitList getTasks(IPSession session, String set_id) {
		BitList tasks = session.getSetManager().getTasks(set_id);
		if (tasks != null)
			return tasks.copy();
		return null;
	}
}
