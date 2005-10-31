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
package org.eclipse.ptp.debug.external.cdi;

import java.util.Properties;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIEventManager;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.ICDISessionConfiguration;
import org.eclipse.cdt.debug.core.cdi.ICDISessionObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.PreferenceConstants;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.IAbstractDebugger;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.cdi.IPCDISession;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.debug.core.model.IPBreakpoint;
import org.eclipse.ptp.debug.external.PTPDebugExternalPlugin;
import org.eclipse.ptp.debug.external.cdi.model.Target;

public class Session implements IPCDISession, ICDISessionObject, IBreakpointListener {
	public final static Target[] EMPTY_TARGETS = {};
	Properties props;
	ProcessManager processManager;
	EventManager eventManager;
	BreakpointManager breakpointManager;
	ExpressionManager expressionManager;
	VariableManager variableManager;
	SourceManager sourceManager;
	ICDISessionConfiguration configuration;
	IAbstractDebugger debugger = null;
	IPJob job = null;
	IPLaunch launch = null;
	IBinaryObject file;
	
	public Session(IAbstractDebugger debugger, IPJob job, IPLaunch launch, IBinaryObject file) {
		this.debugger = debugger;
		this.job = job;
		this.launch = launch;
		this.file = file;
		this.debugger.setSession(this);
		commonSetup();
		job.setAttribute(PreferenceConstants.JOB_DEBUG_SESSION, this);
		
		start();
	}
	private void commonSetup() {
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
		props = new Properties();
		setConfiguration(new SessionConfiguration(this));
		processManager = new ProcessManager(this);
		breakpointManager = new BreakpointManager(this);
		eventManager = new EventManager(this);
		expressionManager = new ExpressionManager(this);
		variableManager = new VariableManager(this);
		sourceManager = new SourceManager(this);
		//add observer
		this.debugger.addDebuggerObserver(eventManager);
	}
	
	public void shutdown() {
		getDebugger().deleteAllObservers();
		processManager.shutdown();
		breakpointManager.shutdown();
		eventManager.shutdown();
		expressionManager.shutdown();
		variableManager.shutdown();
		sourceManager.shutdown();
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
	}
	public IPLaunch getLaunch() {
		return launch;
	}
	public IAbstractDebugger getDebugger() {
		return debugger;
	}
	public IPJob getJob() {
		return job;
	}
	public IBinaryObject getBinaryFile() {
		return file;
	}
	private void start() {
		IWorkspaceRunnable r = new IWorkspaceRunnable() {
			public void run(IProgressMonitor m) throws CoreException {
				PTPDebugCorePlugin.getDebugModel().newJob(getJob().getIDString(), getTotalProcesses());
				launch.launchedStarted();
				boolean stopInMain = getLaunch().getLaunchConfiguration().getAttribute(IPTPLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, false);
				getBreakpointManager().setInitialBreakpoints();
				if (stopInMain) {
					getBreakpointManager().setInternalTemporaryBreakpoint(createBitList(), getBreakpointManager().createFunctionLocation("", "main"));
				}
				try {
					resume(createBitList());
				} catch (PCDIException e) {
					throw new CoreException(new Status(IStatus.ERROR, PTPDebugExternalPlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), null));			
				}
			}
		};
		try {
			ResourcesPlugin.getWorkspace().run(r, null);
		} catch (CoreException e) {
			PTPDebugCorePlugin.log(e);
		}
	}	
	
	public void registerTarget(int procNum, boolean sendEvent) {
		registerTarget(procNum, sendEvent, false);
	}
	public void registerTargets(int[] procNums, boolean sendEvent) {
		registerTargets(procNums, sendEvent, false);
	}
	public void registerTarget(int procNum, boolean sendEvent, boolean resumeTarget) {
		registerTargets(new int[] { procNum }, sendEvent, resumeTarget);
	}
	public void registerTargets(int[] procNums, boolean sendEvent, boolean resumeTarget) {
		Target[] targets = new Target[procNums.length];
		for (int i = 0; i < targets.length; i++) {
			targets[i] = new Target(this, procNums[i]);
		}
		BitList regTasks = createEmptyBitList();
		processManager.addTargets(targets, regTasks);
		PTPDebugCorePlugin.getDebugModel().addNewDebugTargets(launch, regTasks, targets, file, resumeTarget, sendEvent);
	}
	public void unregisterTarget(int procNum, boolean sendEvent) {
		unregisterTargets(new int[] { procNum }, sendEvent);
	}
	public void unregisterTargets(int[] procNums, boolean sendEvent) {
		BitList unregTasks = createEmptyBitList();
		for (int i = 0; i < procNums.length; i++) {
			if (processManager.removeTarget(procNums[i])) {
				unregTasks.set(procNums[i]);
			}
		}
		PTPDebugCorePlugin.getDebugModel().removeDebugTarget(launch, unregTasks, sendEvent);
	}
	public String getAttribute(String key) {
		return props.getProperty(key);
	}
	public ProcessManager getProcessManager() {
		return processManager;
	}
	public BreakpointManager getBreakpointManager() {
		return breakpointManager;
	}
	public ICDIEventManager getEventManager() {
		return eventManager;
	}
	public ExpressionManager getExpressionManager() {
		return expressionManager;
	}
	public VariableManager getVariableManager() {
		return variableManager;
	}
	public SourceManager getSourceManager() {
		return sourceManager;
	}
	public ICDITarget[] getTargets() {
		return getProcessManager().getCDITargets();
	}
	public BitList getRegisteredTargets() {
		return getProcessManager().getRegisteredTargets();
	}
	public ICDITarget getTarget(int target_id) {
		return getProcessManager().getTarget(target_id);
	}
	public void setAttribute(String key, String value) {
		props.setProperty(key, value);
	}
	public ICDISessionConfiguration getConfiguration() {
		return configuration;
	}
	public void setConfiguration(ICDISessionConfiguration conf) {
		configuration = conf;
	}
	public ICDISession getSession() {
		return this;
	}
	public Process getSessionProcess() throws CDIException {
		return getDebugger().getDebuggerProcess();
	}
	public BitList createEmptyBitList() {
		return new BitList(getTotalProcesses());
	}
	public BitList createBitList() {
		BitList tasks = createEmptyBitList();
		tasks.set(0, getTotalProcesses());
		return tasks;
	}
	public BitList createBitList(int index) {
		BitList tasks = createEmptyBitList();
		tasks.set(index);
		return tasks;
	}
	public int getTotalProcesses() {
		return job.size();
	}

	public void breakpointAdded(IBreakpoint breakpoint) {
		String job_id = getJob().getIDString();
		if (breakpoint instanceof IPBreakpoint) {
			try {
				String bp_job_id = ((IPBreakpoint)breakpoint).getJobId(); 
				if (bp_job_id.equals(job_id) || bp_job_id.equals(IPBreakpoint.GLOBAL)) {
					getBreakpointManager().setBreakpoint(job_id, (IPBreakpoint)breakpoint);
				}
			} catch (CoreException e) {
			}
		}
	}
	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
		// TODO Auto-generated method stub
	}
	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
		// TODO Auto-generated method stub
	}
	public void terminate() throws CDIException {
		stop(createBitList());
	}
	public void stop(BitList tasks) throws PCDIException {
		getDebugger().stop(tasks);
	}
	public void resume(BitList tasks) throws PCDIException {
		getDebugger().resume(tasks);
	}
	public void suspend(BitList tasks) throws PCDIException {
		getDebugger().suspend(tasks);
	}
	public void steppingInto(BitList tasks, int count) throws PCDIException {
		getDebugger().steppingInto(tasks, count);
	}
	public void steppingInto(BitList tasks) throws PCDIException {
		getDebugger().steppingInto(tasks);
	}
	public void steppingOver(BitList tasks, int count) throws PCDIException {
		getDebugger().steppingOver(tasks, count);
	}
	public void steppingOver(BitList tasks) throws PCDIException {
		getDebugger().steppingOver(tasks);
	}
	public void steppingReturn(BitList tasks) throws PCDIException {
		getDebugger().steppingReturn(tasks);
	}
}
