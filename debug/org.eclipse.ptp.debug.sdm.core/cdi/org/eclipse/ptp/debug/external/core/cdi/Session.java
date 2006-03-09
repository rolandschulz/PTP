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
package org.eclipse.ptp.debug.external.core.cdi;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.IAbstractDebugger;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.aif.IAIF;
import org.eclipse.ptp.debug.core.cdi.IPCDIBreakpointManager;
import org.eclipse.ptp.debug.core.cdi.IPCDIEventManager;
import org.eclipse.ptp.debug.core.cdi.IPCDISession;
import org.eclipse.ptp.debug.core.cdi.IPCDISessionConfiguration;
import org.eclipse.ptp.debug.core.cdi.IPCDISessionObject;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.model.IPCDITarget;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.debug.external.core.PTPDebugExternalPlugin;
import org.eclipse.ptp.debug.external.core.cdi.model.Target;
import org.eclipse.ptp.debug.external.core.commands.GetAIFCommand;
import org.eclipse.ptp.debug.external.core.commands.GoCommand;
import org.eclipse.ptp.debug.external.core.commands.HaltCommand;
import org.eclipse.ptp.debug.external.core.commands.KillCommand;
import org.eclipse.ptp.debug.external.core.commands.StepFinishCommand;
import org.eclipse.ptp.debug.external.core.commands.StepIntoCommand;
import org.eclipse.ptp.debug.external.core.commands.StepOverCommand;

/**
 * @author Clement chu
 *
 */
public class Session implements IPCDISession, IPCDISessionObject {
	public final static Target[] EMPTY_TARGETS = {};
	Properties props;
	ProcessManager processManager;
	EventManager eventManager;
	BreakpointManager breakpointManager;
	ExpressionManager expressionManager;
	VariableManager variableManager;
	SourceManager sourceManager;
	IPCDISessionConfiguration configuration;
	IAbstractDebugger debugger = null;
	IPJob job = null;
	IPLaunch launch = null;
	IBinaryObject file;
	
	public Session(IAbstractDebugger debugger, IPJob job, IPLaunch launch, IBinaryObject file) throws CoreException {
		this.debugger = debugger;
		this.job = job;
		this.launch = launch;
		this.file = file;
		commonSetup();
		//job.setAttribute(PreferenceConstants.JOB_DEBUG_SESSION, this);
	}
	private void commonSetup() {
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
		try {
			debugger.exit();
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		DebugPlugin.getDefault().getLaunchManager().removeLaunch(launch);
		variableManager.shutdown();
		expressionManager.shutdown();
		breakpointManager.shutdown();
		eventManager.shutdown();
		sourceManager.shutdown();
		processManager.shutdown();
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
	public void start(IProgressMonitor monitor) throws CoreException {
		IWorkspaceRunnable r = new IWorkspaceRunnable() {
			public void run(IProgressMonitor m) throws CoreException {
				m.beginTask("", 3);
				m.setTaskName("Creating debugging session...");
				PTPDebugCorePlugin.getDebugModel().newJob(getJob().getIDString(), getTotalProcesses());				
				launch.launchedStarted();
				m.worked(1);
				
				boolean stopInMain = getLaunch().getLaunchConfiguration().getAttribute(IPTPLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, false);
				m.subTask("Initialing breakpoints...");
				breakpointManager.setInitialBreakpoints();
				if (stopInMain) {
					breakpointManager.setInternalTemporaryBreakpoint(createBitList(), breakpointManager.createFunctionLocation("", "main"));
				}
				m.worked(1);
				if (m.isCanceled()) {
					throw new CoreException(Status.CANCEL_STATUS);
				}
				try {
					resume(createBitList());
					m.worked(1);
				} catch (PCDIException e) {
					throw new CoreException(new Status(IStatus.ERROR, PTPDebugExternalPlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), null));			
				} finally {
					m.done();
				}
			}
		};
		ResourcesPlugin.getWorkspace().run(r, monitor);
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
		List targetList = new ArrayList();
		for (int i = 0; i < procNums.length; i++) {
			if (!processManager.containTarget(procNums[i])) {
				targetList.add(new Target(this, procNums[i]));
			}
		}
		IPCDITarget[] targets = (IPCDITarget[])targetList.toArray(new IPCDITarget[0]);
		if (targets.length > 0) {
			BitList regTasks = createEmptyBitList();
			processManager.addTargets(targets, regTasks);
			PTPDebugCorePlugin.getDebugModel().addNewDebugTargets(launch, regTasks, targets, file, resumeTarget, sendEvent);
		}
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
	public IPCDIBreakpointManager getBreakpointManager() {
		return breakpointManager;
	}
	public IPCDIEventManager getEventManager() {
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
	public IPCDITarget[] getTargets() {
		return getProcessManager().getCDITargets();
	}
	public BitList getRegisteredTargets() {
		return getProcessManager().getRegisteredTargets();
	}
	public IPCDITarget getTarget(int target_id) {
		return getProcessManager().getTarget(target_id);
	}
	public void setAttribute(String key, String value) {
		props.setProperty(key, value);
	}
	public IPCDISessionConfiguration getConfiguration() {
		return configuration;
	}
	public void setConfiguration(IPCDISessionConfiguration conf) {
		configuration = conf;
	}
	public IPCDISession getSession() {
		return this;
	}
	public Process getSessionProcess() throws PCDIException {
		return null;
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
		return job.totalProcesses();
	}

	public void terminate() throws PCDIException {
		stop(createBitList());
	}
	public void stop(BitList tasks) throws PCDIException {
		getDebugger().postCommand(new KillCommand(tasks));
	}
	public void resume(BitList tasks) throws PCDIException {
		getDebugger().postCommand(new GoCommand(tasks));
	}
	public void suspend(BitList tasks) throws PCDIException {
		getDebugger().postCommand(new HaltCommand(tasks));
	}
	public void steppingInto(BitList tasks) throws PCDIException {
		getDebugger().postCommand(new StepIntoCommand(tasks));
	}
	public void steppingOver(BitList tasks) throws PCDIException {
		getDebugger().postCommand(new StepOverCommand(tasks));
	}
	public void steppingReturn(BitList tasks) throws PCDIException {
		getDebugger().postCommand(new StepFinishCommand(tasks));
	}
	public IAIF getExpressionValue(BitList tasks, String variable) throws PCDIException {
		GetAIFCommand command = new GetAIFCommand(tasks, variable);
		getDebugger().postCommand(command);
		return command.getAIF();
	}
	public IAIF getExpressionValue(int task_id, String variable) throws PCDIException {
		return getExpressionValue(createBitList(task_id), variable);
	}	
}
