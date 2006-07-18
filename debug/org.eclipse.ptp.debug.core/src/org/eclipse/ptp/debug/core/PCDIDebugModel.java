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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
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
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.ptp.core.AttributeConstants;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.PreferenceConstants;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.aif.AIFException;
import org.eclipse.ptp.debug.core.aif.IAIF;
import org.eclipse.ptp.debug.core.cdi.IPCDISession;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.model.IPCDITarget;
import org.eclipse.ptp.debug.core.cdi.model.IPCDITargetConfiguration;
import org.eclipse.ptp.debug.core.events.IPDebugEvent;
import org.eclipse.ptp.debug.core.events.IPDebugInfo;
import org.eclipse.ptp.debug.core.events.PDebugEvent;
import org.eclipse.ptp.debug.core.events.PDebugInfo;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.debug.core.model.IPBreakpoint;
import org.eclipse.ptp.debug.core.model.IPDebugTarget;
import org.eclipse.ptp.debug.core.model.IPLineBreakpoint;
import org.eclipse.ptp.debug.internal.core.breakpoint.PLineBreakpoint;
import org.eclipse.ptp.debug.internal.core.model.PDebugTarget;
import org.eclipse.ptp.debug.internal.core.model.PSession;

/**
 * @author clement chu
 * 
 */
public class PCDIDebugModel {
	private final String VALUE_UNKNOWN = "Unkwnon";
	private final String VALUE_ERROR = "Error in getting value";

	private Map jobSets = new HashMap();
	private Map sessions = new HashMap();

	public void shutdown() {
		for (Iterator i=sessions.values().iterator(); i.hasNext();) {
			IPSession pSession = (IPSession)i.next();
			if (pSession != null) {
				IPJob job = pSession.getJob();
				if(!job.isAllStop()) {
					job.removeAllProcesses();
					pSession.getPCDISession().shutdown();
					try {
						System.out.println("-----PCDIDebugModel - waiting debugger to stop");
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		sessions.clear();
		jobSets.clear();
	}
	public void shutdownSession(IPJob job) {
		if (job != null) {
			IPSession pSession = (IPSession)sessions.remove(job.getIDString());
			if (pSession != null) {
				pSession.getPCDISession().shutdown();
			}
		}
	}
	public IPCDISession getPCDISession(String jid) {
		IPSession pSession = (IPSession)sessions.get(jid);
		if (pSession != null) {
			return pSession.getPCDISession();
		}
		return null;
	}
	public IPSession createDebuggerSession(IAbstractDebugger debugger, IPLaunch launch, IBinaryObject exe, int timeout, IProgressMonitor monitor) throws CoreException {
		IPSession pSession = new PSession(debugger.createDebuggerSession(launch, exe, timeout, monitor));
		IPJob job = launch.getPJob();
		sessions.put(job.getIDString(), pSession);
		newJob(job, job.totalProcesses());
		fireSessionEvent(job, pSession.getPCDISession());
		pSession.getPCDISession().start(monitor);
		return pSession;
	}	
	public static String getPluginIdentifier() {
		return PTPDebugCorePlugin.getUniqueIdentifier();
	}
	public void fireSessionEvent(IPJob job, IPCDISession session) {
		IPDebugInfo info = new PDebugInfo(job, null, null, null);
		PTPDebugCorePlugin.getDefault().fireDebugEvent(new PDebugEvent(session, IPDebugEvent.CREATE, IPDebugEvent.DEBUGGER, info));
	}
	public void fireRegisterEvent(IPJob job, BitList tasks) {
		IPDebugInfo info = new PDebugInfo(job, tasks, tasks, null);
		PTPDebugCorePlugin.getDefault().fireDebugEvent(new PDebugEvent(getPCDISession(job.getIDString()), IPDebugEvent.CREATE, IPDebugEvent.REGISTER, info));
	}
	public void fireUnregisterEvent(IPJob job, BitList tasks) {
		IPDebugInfo info = new PDebugInfo(job, tasks, null, tasks);
		PTPDebugCorePlugin.getDefault().fireDebugEvent(new PDebugEvent(getPCDISession(job.getIDString()), IPDebugEvent.TERMINATE, IPDebugEvent.REGISTER, info));
	}
	public void removeDebugTarget(IPLaunch launch, BitList tasks, boolean sendEvent) {
		int[] taskArray = tasks.toArray();
		for (int i=0; i<taskArray.length; i++) {
			launch.getPJob().findProcessByTaskId(taskArray[i]).setAttribute(AttributeConstants.ATTRIB_ISREGISTERED, new Boolean(false));
			IPDebugTarget debugTarget = launch.getDebugTarget(taskArray[i]);
			if (debugTarget != null) {
				launch.removeDebugTarget(debugTarget);
				debugTarget.terminated();
			}
		}
		if (sendEvent)
			fireUnregisterEvent(launch.getPJob(), tasks);
	}
	public void addNewDebugTargets(IPLaunch launch, BitList tasks, IPCDITarget[] targets, IBinaryObject file, boolean resumeTarget, boolean sendEvent) {
		IPDebugTarget[] debugTargets = newDebugTargets(launch, targets, file, true, false, resumeTarget);
		for (int i=0; i<debugTargets.length; i++) {
			launch.getPJob().findProcessByTaskId(debugTargets[i].getTargetID()).setAttribute(AttributeConstants.ATTRIB_ISREGISTERED, new Boolean(true));
			launch.addDebugTarget(debugTargets[i]);
		}
		if (sendEvent)
			fireRegisterEvent(launch.getPJob(), tasks);
	}
	public IPDebugTarget[] newDebugTargets(final IPLaunch launch, final IPCDITarget[] cdiTargets, final IBinaryObject file, final boolean allowTerminate, final boolean allowDisconnect, final boolean resumeTarget) {
		final IPDebugTarget[] targets = new IPDebugTarget[cdiTargets.length];
		IWorkspaceRunnable r = new IWorkspaceRunnable() {
			public void run(IProgressMonitor m) throws CoreException {
				for (int i=0; i<targets.length; i++) {
					targets[i] = new PDebugTarget(launch, cdiTargets[i], null, file, allowTerminate, allowDisconnect);
					IPCDITargetConfiguration config = cdiTargets[i].getConfiguration();
					if (config.supportsResume() && resumeTarget) {
						targets[i].resume();
					}
				}
			}
		};
		try {
			ResourcesPlugin.getWorkspace().run(r, null);
		} catch (CoreException e) {
			PTPDebugCorePlugin.log(e);
			return new IPDebugTarget[0];
		}
		return targets;
	}
	public IPLineBreakpoint[] lineBreakpointsExists(String sourceHandle, IResource resource, int lineNumber) throws CoreException {
		IBreakpoint[] breakpoints = getPBreakpoints();
		List foundBreakpoints = new ArrayList(0);
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
		return (IPLineBreakpoint[]) foundBreakpoints.toArray(new IPLineBreakpoint[foundBreakpoints.size()]);
	}
	// remove global breapoint or the breakpoint same as job id given
	public IPLineBreakpoint lineBreakpointExists(IPLineBreakpoint[] breakpoints, String job_id) throws CoreException {
		for (int i = 0; i < breakpoints.length; i++) {
			String bpt_job_id = breakpoints[i].getJobId();
			if (bpt_job_id.equals(IPBreakpoint.GLOBAL) || bpt_job_id.equals(job_id))
				return breakpoints[i];
		}
		return null;
	}
	public boolean sameSourceHandle(String handle1, String handle2) {
		if (handle1 == null || handle2 == null)
			return false;
		IPath path1 = new Path(handle1);
		IPath path2 = new Path(handle2);
		if (path1.isValidPath(handle1) && path2.isValidPath(handle2))
			return path1.equals(path2);
		return handle1.equals(handle2);
	}
	public IBreakpoint createLineBreakpoint(String sourceHandle, IResource resource, int lineNumber, boolean enabled, int ignoreCount, String condition, boolean register, String set_id, String job_id, String jobName) throws CoreException {
		HashMap attributes = new HashMap(10);
		attributes.put(IBreakpoint.ID, PTPDebugCorePlugin.getUniqueIdentifier());
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
	public IBreakpoint[] getPBreakpoints() {
		return DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(getPluginIdentifier());
	}
	public IBreakpoint[] findPBreakpointsBySet(String job_id, String set_id) throws CoreException {
		List bptList = new ArrayList();
		IBreakpoint[] breakpoints = getPBreakpoints();
		for (int i = 0; i < breakpoints.length; i++) {
			if (!(breakpoints[i] instanceof IPLineBreakpoint))
				continue;
			IPLineBreakpoint breakpoint = (IPLineBreakpoint) breakpoints[i];
			if (breakpoint.getJobId().equals(job_id) && breakpoint.getSetId().equals(set_id)) {
				bptList.add(breakpoint);
			}
		}
		return (IBreakpoint[]) bptList.toArray(new IBreakpoint[bptList.size()]);
	}
	public IPBreakpoint[] findPBreakpointsByJob(String job_id, boolean includeGlobal) throws CoreException {
		List bptList = new ArrayList();
		IBreakpoint[] breakpoints = getPBreakpoints();
		for (int i = 0; i < breakpoints.length; i++) {
			if (!(breakpoints[i] instanceof IPBreakpoint))
				continue;
			IPBreakpoint breakpoint = (IPBreakpoint) breakpoints[i];
			String bp_job_id = breakpoint.getJobId();
			if (bp_job_id.equals(job_id) || (includeGlobal && bp_job_id.equals(IPBreakpoint.GLOBAL))) {
				bptList.add(breakpoint);
			}
		}
		return (IPBreakpoint[]) bptList.toArray(new IPBreakpoint[bptList.size()]);
	}
	public void deletePBreakpointBySet(final String job_id, final String set_id) throws CoreException {
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				final IBreakpoint[] breakpoints = findPBreakpointsBySet(job_id, set_id);
				if (breakpoints.length > 0) {
					new Job("Remove breakpoint") {
						protected IStatus run(IProgressMonitor pmonitor) {
							try {
								DebugPlugin.getDefault().getBreakpointManager().removeBreakpoints(breakpoints, true);
								return Status.OK_STATUS;
							} catch (CoreException e) {
								PTPDebugCorePlugin.log(e);
							}
							return Status.CANCEL_STATUS;
						}
					}.schedule();
				}
			}
		};
		ResourcesPlugin.getWorkspace().run(runnable, null);
	}
	public void deletePBreakpointBySet(final String job_id) throws CoreException {
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				final IBreakpoint[] breakpoints = findPBreakpointsByJob(job_id, false);
				if (breakpoints.length > 0) {
					new Job("Remove breakpoint") {
						protected IStatus run(IProgressMonitor pmonitor) {
							try {
								DebugPlugin.getDefault().getBreakpointManager().removeBreakpoints(breakpoints, true);
								return Status.OK_STATUS;
							} catch (CoreException e) {
								PTPDebugCorePlugin.log(e);
							}
							return Status.CANCEL_STATUS;
						}
					}.schedule();
				}
			}
		};
		ResourcesPlugin.getWorkspace().run(runnable, null);
	}
	public void updatePBreakpoints(String set_id, IProgressMonitor monitor) throws CoreException {
		try {
			IBreakpoint[] breakpoints = getPBreakpoints();
			monitor.beginTask("Updating parallel breakpoint...", breakpoints.length);
			for (int i = 0; i < breakpoints.length; i++) {
				if (!(breakpoints[i] instanceof IPBreakpoint))
					continue;
				((IPBreakpoint) breakpoints[i]).setCurSetId(set_id);
				monitor.worked(1);
			}
		} finally {
			monitor.done();
		}
	}
	public void newJob(IPJob job, int totalTasks) throws CoreException {
		BitList rootTasks = new BitList(totalTasks);
		rootTasks.set(0, totalTasks);
		createSet(job.getIDString(), PreferenceConstants.SET_ROOT_ID, rootTasks);
	}
	public void createSet(String job_id, String set_id, BitList tasks) throws CoreException {
		JobSet jobSet = getJobSet(job_id);
		jobSet.createSet(set_id, tasks);
	}
	public void addTasks(String job_id, String set_id, BitList tasks) throws CoreException {
		JobSet jobSet = getJobSet(job_id);
		jobSet.addTasks(set_id, tasks);
	}
	public void removeTasks(String job_id, String set_id, BitList tasks) throws CoreException {
		JobSet jobSet = getJobSet(job_id);
		jobSet.removeTasks(set_id, tasks);
	}
	public void deleteSet(String job_id, String set_id) throws CoreException {
		JobSet jobSet = getJobSet(job_id);
		jobSet.deleteSet(set_id);
	}
	public BitList getTasks(String job_id, String set_id) throws CoreException {
		JobSet jobSet = getJobSet(job_id);
		return jobSet.getTasks(set_id).copy();
	}
	public void deleteJob(IPJob job) {
		getJobSet(job.getIDString()).clearAllSets();
		jobSets.remove(job.getIDString());
	}
	private JobSet getJobSet(String job_id) {
		if (!jobSets.containsKey(job_id)) {
			jobSets.put(job_id, new JobSet());
		}
		return (JobSet)jobSets.get(job_id);
	}
	private class JobSet {
		private Map sets = new HashMap();
		public void clearAllSets() {
			sets.clear();
		}
		public void createSet(String set_id, BitList tasks) throws CoreException {
			if (sets.containsKey(set_id))
				throw new CoreException(new Status(IStatus.ERROR, getPluginIdentifier(), IStatus.ERROR, "The set [" + set_id + "] is already existed.", null));
				
			sets.put(set_id, tasks);
		}
		public void addTasks(String set_id, BitList tasks) throws CoreException {
			getTasks(set_id).or(tasks);
		}
		public void removeTasks(String set_id, BitList tasks) throws CoreException {
			getTasks(set_id).andNot(tasks);
		}
		public void deleteSet(String set_id) throws CoreException {
			if (!sets.containsKey(set_id))
				throw new CoreException(new Status(IStatus.ERROR, getPluginIdentifier(), IStatus.ERROR, "The set [" + set_id + "] is not found.", null));

			sets.remove(set_id);
		}
		public BitList getTasks(String set_id) throws CoreException {
			if (!sets.containsKey(set_id))
				throw new CoreException(new Status(IStatus.ERROR, getPluginIdentifier(), IStatus.ERROR, "The set [" + set_id + "] is not found.", null));

			return (BitList)sets.get(set_id);
		}
	}
	/***********************************************************************************************
	 * Expression
	 ***********************************************************************************************/
	public String getValue(IPCDISession session, int taskID, String var, IProgressMonitor monitor) {
		try {
			IAIF aif = session.getExpressionValue(taskID, var);
			if (aif == null) {
				return VALUE_UNKNOWN;
			}
			else {
				return aif.getValue().getValueString();
			}
		} catch (PCDIException pe) {
			return VALUE_ERROR;
		} catch (AIFException e) {
			return VALUE_ERROR;
		} finally {
			monitor.worked(1);
		}
	}
	public String[] getValues(IPCDISession session, int taskID, String[] vars, IProgressMonitor monitor) {
		String[] values = new String[vars.length];
		for (int i=0; i<vars.length; i++) {
			values[i] = getValue(session, taskID, vars[i], monitor);
		}
		return values;
	}
}
