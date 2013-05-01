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
package org.eclipse.ptp.internal.debug.core;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
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
import org.eclipse.ptp.debug.core.IPDebugger;
import org.eclipse.ptp.debug.core.IPSession;
import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.event.IPDebugEvent;
import org.eclipse.ptp.debug.core.event.IPDebugInfo;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.debug.core.model.IPAddressBreakpoint;
import org.eclipse.ptp.debug.core.model.IPBreakpoint;
import org.eclipse.ptp.debug.core.model.IPDebugTarget;
import org.eclipse.ptp.debug.core.model.IPFunctionBreakpoint;
import org.eclipse.ptp.debug.core.model.IPLineBreakpoint;
import org.eclipse.ptp.debug.core.model.IPWatchpoint;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.model.IPDITarget;
import org.eclipse.ptp.internal.debug.core.breakpoint.PAddressBreakpoint;
import org.eclipse.ptp.internal.debug.core.breakpoint.PFunctionBreakpoint;
import org.eclipse.ptp.internal.debug.core.breakpoint.PLineBreakpoint;
import org.eclipse.ptp.internal.debug.core.breakpoint.PWatchpoint;
import org.eclipse.ptp.internal.debug.core.event.PDebugEvent;
import org.eclipse.ptp.internal.debug.core.event.PDebugRegisterInfo;
import org.eclipse.ptp.internal.debug.core.messages.Messages;
import org.eclipse.ptp.internal.debug.core.model.PDebugTarget;

/**
 * @author clement chu
 * 
 */
public class PDebugModel {
	/**
	 * Create an address breakpoint.
	 * 
	 * @param module
	 * @param sourceHandle
	 * @param resource
	 * @param lineNumber
	 * @param address
	 * @param enabled
	 * @param ignoreCount
	 * @param condition
	 * @param register
	 * @param setId
	 * @param jobId
	 * @param jobName
	 * @return
	 * @throws CoreException
	 */
	public static IPAddressBreakpoint createAddressBreakpoint(String module, String sourceHandle, IResource resource,
			int lineNumber, BigInteger address, boolean enabled, int ignoreCount, String condition, boolean register, String setId,
			String jobId, String jobName) throws CoreException {
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
		attributes.put(IPBreakpoint.CUR_SET_ID, setId);
		attributes.put(IPBreakpoint.JOB_NAME, jobName);
		return new PAddressBreakpoint(resource, attributes, jobId, setId, register);
	}

	/**
	 * Create a function breakpoint.
	 * 
	 * @param sourceHandle
	 * @param resource
	 * @param function
	 * @param charStart
	 * @param charEnd
	 * @param lineNumber
	 * @param enabled
	 * @param ignoreCount
	 * @param condition
	 * @param register
	 * @param setId
	 * @param jobId
	 * @param jobName
	 * @return
	 * @throws CoreException
	 */
	public static IPFunctionBreakpoint createFunctionBreakpoint(String sourceHandle, IResource resource, String function,
			int charStart, int charEnd, int lineNumber, boolean enabled, int ignoreCount, String condition, boolean register,
			String setId, String jobId, String jobName) throws CoreException {
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
		return new PFunctionBreakpoint(resource, attributes, jobId, setId, register);
	}

	/**
	 * @param sourceHandle
	 * @param resource
	 * @param lineNumber
	 * @param enabled
	 * @param ignoreCount
	 * @param condition
	 * @param register
	 * @param setId
	 * @param jobId
	 * @return
	 * @throws CoreException
	 * @since 7.0
	 */
	public static IPLineBreakpoint createLineBreakpoint(String sourceHandle, IResource resource, int lineNumber, boolean enabled,
			int ignoreCount, String condition, boolean register, String setId, String jobId) throws CoreException {
		HashMap<String, Object> attributes = new HashMap<String, Object>(10);
		attributes.put(IBreakpoint.ID, getPluginIdentifier());
		attributes.put(IMarker.LINE_NUMBER, new Integer(lineNumber));
		attributes.put(IBreakpoint.ENABLED, new Boolean(enabled));
		attributes.put(IPBreakpoint.SOURCE_HANDLE, sourceHandle);
		attributes.put(IPBreakpoint.IGNORE_COUNT, new Integer(ignoreCount));
		attributes.put(IPBreakpoint.CONDITION, condition);
		attributes.put(IPBreakpoint.CUR_SET_ID, setId);
		attributes.put(IPBreakpoint.JOB_NAME, jobId);
		return new PLineBreakpoint(resource, attributes, jobId, setId, register);
	}

	/**
	 * Create a watchpoint on an expression.
	 * 
	 * @param sourceHandle
	 * @param resource
	 * @param writeAccess
	 * @param readAccess
	 * @param expression
	 * @param enabled
	 * @param ignoreCount
	 * @param condition
	 * @param register
	 * @param setId
	 * @param jobId
	 * @param jobName
	 * @return
	 * @throws CoreException
	 */
	public static IPWatchpoint createWatchpoint(String sourceHandle, IResource resource, boolean writeAccess, boolean readAccess,
			String expression, boolean enabled, int ignoreCount, String condition, boolean register, String setId, String jobId,
			String jobName) throws CoreException {
		HashMap<String, Object> attributes = new HashMap<String, Object>(10);
		attributes.put(IBreakpoint.ID, getPluginIdentifier());
		attributes.put(IBreakpoint.ENABLED, Boolean.valueOf(enabled));
		attributes.put(IPBreakpoint.SOURCE_HANDLE, sourceHandle);
		attributes.put(IPBreakpoint.IGNORE_COUNT, new Integer(ignoreCount));
		attributes.put(IPBreakpoint.CONDITION, condition);
		attributes.put(IPWatchpoint.EXPRESSION, expression);
		attributes.put(IPWatchpoint.READ, Boolean.valueOf(readAccess));
		attributes.put(IPWatchpoint.WRITE, Boolean.valueOf(writeAccess));
		return new PWatchpoint(resource, attributes, jobId, setId, register);
	}

	/**
	 * Find the breakpoints for a particular function.
	 * 
	 * @param sourceHandle
	 * @param resource
	 * @param function
	 * @return
	 * @throws CoreException
	 */
	public static IPFunctionBreakpoint[] functionBreakpointExists(String sourceHandle, IResource resource, String function)
			throws CoreException {
		IBreakpoint[] breakpoints = getBreakpoints();
		List<IPFunctionBreakpoint> foundBreakpoints = new ArrayList<IPFunctionBreakpoint>();
		String markerType = PFunctionBreakpoint.getMarkerType();
		for (IBreakpoint breakpoint : breakpoints) {
			if (breakpoint instanceof IPFunctionBreakpoint) {
				IPFunctionBreakpoint funcBP = (IPFunctionBreakpoint) breakpoint;
				if (funcBP.getMarker().getType().equals(markerType)) {
					if (sameSourceHandle(sourceHandle, funcBP.getSourceHandle())) {
						if (funcBP.getMarker().getResource().equals(resource)) {
							if (funcBP.getFunction() != null && funcBP.getFunction().equals(function)) {
								foundBreakpoints.add(funcBP);
							}
						}
					}
				}
			}
		}
		return foundBreakpoints.toArray(new IPFunctionBreakpoint[0]);
	}

	/**
	 * Get all the breakpoints set by the debugger.
	 * 
	 * @return all breakpoints
	 * @since 5.0
	 */
	public static IBreakpoint[] getBreakpoints() {
		return DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(getPluginIdentifier());
	}

	/**
	 * @return
	 */
	public static String getPluginIdentifier() {
		return PTPDebugCorePlugin.getUniqueIdentifier();
	}

	/**
	 * Find the first global breakpoint or breakpoint on the supplied job.
	 * 
	 * @param breakpoints
	 * @param job
	 * @return
	 * @throws CoreException
	 * @since 7.0
	 */
	public static IPLineBreakpoint lineBreakpointExists(IPLineBreakpoint[] breakpoints, String jid) throws CoreException {
		for (IPLineBreakpoint breakpoint : breakpoints) {
			String jobId = breakpoint.getJobId();
			if (jobId.equals(IPBreakpoint.GLOBAL) || jobId.equals(jid)) {
				return breakpoint;
			}
		}
		return null;
	}

	/**
	 * Find the line breakpoints at a particular line.
	 * 
	 * @param sourceHandle
	 * @param resource
	 * @param lineNumber
	 * @return
	 * @throws CoreException
	 */
	public static IPLineBreakpoint[] lineBreakpointsExists(String sourceHandle, IResource resource, int lineNumber)
			throws CoreException {
		IBreakpoint[] breakpoints = getBreakpoints();
		List<IPLineBreakpoint> foundBreakpoints = new ArrayList<IPLineBreakpoint>();
		for (IBreakpoint breakpoint : breakpoints) {
			if (breakpoint instanceof IPLineBreakpoint) {
				IPLineBreakpoint lineBP = (IPLineBreakpoint) breakpoint;
				if (sameSourceHandle(sourceHandle, lineBP.getSourceHandle())) {
					if (lineBP.getMarker().getResource().equals(resource)) {
						if (lineBP.getLineNumber() == lineNumber) {
							foundBreakpoints.add(lineBP);
						}
					}
				}
			}
		}
		return foundBreakpoints.toArray(new IPLineBreakpoint[0]);
	}

	/**
	 * Update breakpoint when a set ID changes.
	 * 
	 * @param setId
	 * @param monitor
	 * @throws CoreException
	 * @since 5.0
	 */
	public static void updateBreakpoints(String setId, IProgressMonitor monitor) throws CoreException {
		try {
			IBreakpoint[] breakpoints = getBreakpoints();
			monitor.beginTask(Messages.PDebugModel_0, breakpoints.length);
			for (IBreakpoint bpt : breakpoints) {
				if (bpt instanceof IPBreakpoint) {
					((IPBreakpoint) bpt).setCurSetId(setId);
					monitor.worked(1);
				}
			}
		} finally {
			monitor.done();
		}
	}

	/**
	 * Check if a watchpoint exists.
	 * 
	 * @param sourceHandle
	 * @param resource
	 * @param expression
	 * @return
	 * @throws CoreException
	 */
	public static IPWatchpoint[] watchpointExists(String sourceHandle, IResource resource, String expression) throws CoreException {
		List<IPWatchpoint> foundBreakpoints = new ArrayList<IPWatchpoint>();
		String markerType = PWatchpoint.getMarkerType();
		for (IBreakpoint breakpoint : getBreakpoints()) {
			if (breakpoint instanceof IPWatchpoint) {
				IPWatchpoint wp = (IPWatchpoint) breakpoint;
				if (wp.getMarker().getType().equals(markerType)) {
					if (sameSourceHandle(sourceHandle, wp.getSourceHandle())) {
						if (wp.getMarker().getResource().equals(resource)) {
							if (wp.getExpression().equals(expression)) {
								foundBreakpoints.add(wp);
							}
						}
					}
				}
			}
		}
		return foundBreakpoints.toArray(new IPWatchpoint[0]);
	}

	/**
	 * Delete all breakpoints for a job.
	 * 
	 * @param jobId
	 * @throws CoreException
	 */
	private static void deleteBreakpoint(final String jobId) throws CoreException {
		IPBreakpoint[] breakpoints = findBreakpoints(jobId, false);
		if (breakpoints.length > 0) {
			DebugPlugin.getDefault().getBreakpointManager().removeBreakpoints(breakpoints, true);
		}
	}

	/**
	 * Delete all breakpoints for a set
	 * 
	 * @param jobId
	 * @param setId
	 * @throws CoreException
	 */
	private static void deleteBreakpoint(final String jobId, final String setId) throws CoreException {
		IPBreakpoint[] breakpoints = findBreakpoints(jobId, setId);
		if (breakpoints.length > 0) {
			DebugPlugin.getDefault().getBreakpointManager().removeBreakpoints(breakpoints, true);
		}
	}

	/**
	 * Find all breakpoints associated with the job.
	 * 
	 * @param jobId
	 * @param includeGlobal
	 *            include global breakpoints
	 * @return
	 * @throws CoreException
	 */
	private static IPBreakpoint[] findBreakpoints(String jobId, boolean includeGlobal) throws CoreException {
		List<IPBreakpoint> bptList = new ArrayList<IPBreakpoint>();
		IBreakpoint[] breakpoints = getBreakpoints();
		for (IBreakpoint bpt : breakpoints) {
			if (bpt instanceof IPBreakpoint) {
				IPBreakpoint breakpoint = (IPBreakpoint) bpt;
				String bpJobId = breakpoint.getJobId();
				if (bpJobId.equals(jobId) || (includeGlobal && bpJobId.equals(IPBreakpoint.GLOBAL))) {
					bptList.add(breakpoint);
				}
			}
		}
		return bptList.toArray(new IPBreakpoint[bptList.size()]);
	}

	/**
	 * Find breakpoints associated with the set.
	 * 
	 * @param jobId
	 * @param setId
	 * @return
	 * @throws CoreException
	 */
	private static IPBreakpoint[] findBreakpoints(String jobId, String setId) throws CoreException {
		List<IPBreakpoint> bptList = new ArrayList<IPBreakpoint>();
		IBreakpoint[] breakpoints = getBreakpoints();
		for (IBreakpoint bpt : breakpoints) {
			if (bpt instanceof IPBreakpoint) {
				IPBreakpoint breakpoint = (IPBreakpoint) bpt;
				if (breakpoint.getJobId().equals(jobId) && breakpoint.getSetId().equals(setId)) {
					bptList.add(breakpoint);
				}
			}
		}
		return bptList.toArray(new IPBreakpoint[0]);
	}

	/**
	 * Check if source handles are the same.
	 * 
	 * @param handle1
	 * @param handle2
	 * @return
	 */
	private static boolean sameSourceHandle(String handle1, String handle2) {
		if (handle1 == null || handle2 == null) {
			return false;
		}
		IPath path1 = new Path(handle1);
		IPath path2 = new Path(handle2);
		if (path1.isValidPath(handle1) && path2.isValidPath(handle2)) {
			return path1.equals(path2);
		}
		return handle1.equals(handle2);
	}

	private final PSessionManager sessionMgr = new PSessionManager();

	/**
	 * Add a new debug target to the lauch.
	 * 
	 * @param launch
	 * @param tasks
	 * @param pdiTargets
	 * @param refresh
	 * @param resumeTarget
	 * @since 4.0
	 */
	public void addNewDebugTargets(final IPLaunch launch, final TaskSet tasks, final IPDITarget[] pdiTargets,
			final boolean refresh, final boolean resumeTarget) {
		WorkspaceJob aJob = new WorkspaceJob(Messages.PDebugModel_1) {
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) {
				boolean allowTerminate = true;
				boolean allowDisconnect = false;
				IPSession session = getSession(launch.getJobId());
				if (session == null) {
					return Status.CANCEL_STATUS;
				}
				for (IPDITarget pdiTarget : pdiTargets) {
					IPDebugTarget target = new PDebugTarget(session, launch.getProcesses()[0], pdiTarget, allowTerminate,
							allowDisconnect);
					if (resumeTarget) {
						try {
							session.getPDISession().resume(target.getTasks(), false);
						} catch (PDIException e) {
							PTPDebugCorePlugin.log(e);
						}
					}
					launch.addDebugTarget(target);
				}
				fireRegisterEvent(launch, tasks, refresh);
				return Status.OK_STATUS;
			}
		};
		aJob.setSystem(true);
		aJob.schedule();
	}

	/**
	 * Add new tasks to a task set
	 * 
	 * @param session
	 *            current debug session
	 * @param setId
	 *            ID of the set to add the tasks to
	 * @param tasks
	 *            tasks to add to the set
	 * @since 4.0
	 */
	public void addTasks(IPSession session, String setId, TaskSet tasks) {
		TaskSet curSetTasks = getTasks(session, setId);
		session.getSetManager().addTasks(setId, tasks);
		tasks.andNot(curSetTasks);
		try {
			IPBreakpoint[] breakpoints = findBreakpoints(session.getLaunch().getJobId(), setId);
			session.getBreakpointManager().addSetBreakpoints(tasks, breakpoints);
		} catch (CoreException e) {
			PTPDebugCorePlugin.log(e);
		}
	}

	/**
	 * Helper method to create a new debug session.
	 * 
	 * @param timeout
	 *            timeout value for debug commands
	 * @param launch
	 *            debugger launch configuration
	 * @param monitor
	 *            progress monitor
	 * @return new debug session
	 * @throws CoreException
	 * @since 5.0
	 */
	public IPSession createDebugSession(IPDebugger debugger, IPLaunch launch, IProject project, IProgressMonitor monitor)
			throws CoreException {
		long timeout = PTPDebugCorePlugin.getDefault().getCommandTimeout();
		IPDISession pdiSession = debugger.createDebugSession(timeout, launch, monitor);
		IPSession session = new PSession(pdiSession, launch, project);
		sessionMgr.addSession(launch.getJobId(), session);
		return session;
	}

	/**
	 * Create a new set containing the given tasks.
	 * 
	 * @param session
	 * @param setId
	 * @param tasks
	 * @since 4.0
	 */
	public void createSet(IPSession session, String setId, TaskSet tasks) {
		session.getSetManager().createSet(setId, tasks);
	}

	/**
	 * Remove a set from the session.
	 * 
	 * @param session
	 *            current session
	 * @param setId
	 */
	public void deleteSet(IPSession session, String setId) {
		try {
			PDebugModel.deleteBreakpoint(session.getLaunch().getJobId(), setId);
		} catch (CoreException e) {
			PTPDebugCorePlugin.log(e);
		}
		// must delete breakpoint and then delete set
		session.getSetManager().deleteSets(setId);
	}

	/**
	 * Get the session associated with a job
	 * 
	 * @param job
	 * @return
	 * @since 5.0
	 */
	public IPSession getSession(String jobId) {
		return sessionMgr.getSession(jobId);
	}

	/**
	 * Find the tasks associated with a set in a debug sesison.
	 * 
	 * @param session
	 * @param setId
	 * @return
	 * @since 4.0
	 */
	public TaskSet getTasks(IPSession session, String setId) {
		TaskSet tasks = session.getSetManager().getTasks(setId);
		if (tasks != null) {
			return tasks.copy();
		}
		return null;
	}

	/**
	 * Remove a debug target from a launch. �
	 * 
	 * @param launch
	 * @param tasks
	 * @param refresh
	 * @since 4.0
	 */
	public void removeDebugTarget(final IPLaunch launch, final TaskSet tasks, final boolean refresh) {
		WorkspaceJob aJob = new WorkspaceJob(Messages.PDebugModel_2) {
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) {
				int[] taskArray = tasks.toArray();
				for (int task : taskArray) {
					IPDebugTarget target = launch.getDebugTarget(task);
					if (target != null) {
						target.dispose();
						launch.removeDebugTarget(target);
					}
				}
				fireUnregisterEvent(launch, tasks, refresh);
				return Status.OK_STATUS;
			}
		};
		aJob.setSystem(true);
		aJob.schedule();
	}

	/**
	 * Remove tasks from the set of tasks defined by setId. If there are any
	 * breakpoints on tasks in the set, the breakpoint need to be removed also.
	 * 
	 * @param session
	 *            debug session
	 * @param setId
	 *            id of set containing tasks to be removed
	 * @param tasks
	 *            tasks to be removed from the set
	 * @since 4.0
	 */
	public void removeTasks(IPSession session, String setId, TaskSet tasks) {
		TaskSet curSetTasks = getTasks(session, setId);
		session.getSetManager().removeTasks(setId, tasks);
		tasks.and(curSetTasks);
		try {
			IPBreakpoint[] breakpoints = findBreakpoints(session.getLaunch().getJobId(), setId);
			session.getBreakpointManager().deleteSetBreakpoints(tasks, breakpoints);
		} catch (CoreException e) {
			PTPDebugCorePlugin.log(e);
		}
	}

	/**
	 * Shutdown all debug sessions
	 */
	public void shutdown() {
		for (IPSession session : sessionMgr.getSessions()) {
			shutdownSession(session);
		}
		sessionMgr.shutdown();
	}

	/**
	 * Shutdown the debug session for the given job.
	 * 
	 * @param jobId
	 *            job ID for the debug session
	 * @since 5.0
	 */
	public void shutdownSession(String jobId) {
		if (jobId != null) {
			shutdownSession(getSession(jobId));
		}
	}

	/**
	 * Fire a register event for the given tasks.
	 * 
	 * @param job
	 * @param tasks
	 * @param refresh
	 * @since 4.0
	 */
	private void fireRegisterEvent(IPLaunch launch, TaskSet tasks, boolean refresh) {
		if (!tasks.isEmpty()) {
			IPSession session = getSession(launch.getJobId());
			if (session != null) {
				IPDebugInfo info = new PDebugRegisterInfo(launch, tasks, null, null, refresh);
				PTPDebugCorePlugin.getDefault().fireDebugEvent(
						new PDebugEvent(session, IPDebugEvent.CREATE, IPDebugEvent.REGISTER, info));
			}
		}
	}

	/**
	 * Fire an unregister event for the given tasks.
	 * 
	 * @param job
	 * @param tasks
	 * @param refresh
	 * @since 4.0
	 */
	private void fireUnregisterEvent(IPLaunch launch, TaskSet tasks, boolean refresh) {
		if (!tasks.isEmpty()) {
			IPSession session = getSession(launch.getJobId());
			if (session != null) {
				IPDebugInfo info = new PDebugRegisterInfo(launch, tasks, null, null, refresh);
				PTPDebugCorePlugin.getDefault().fireDebugEvent(
						new PDebugEvent(session, IPDebugEvent.TERMINATE, IPDebugEvent.REGISTER, info));
			}
		}
	}

	/**
	 * Shutdown a debug session.
	 * 
	 * @param session
	 */
	private void shutdownSession(IPSession session) {
		if (session != null) {
			try {
				deleteBreakpoint(session.getLaunch().getJobId());
			} catch (CoreException e) {
				PTPDebugCorePlugin.log(e);
			}
			session.dispose();
		}
	}
}
