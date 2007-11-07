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
package org.eclipse.ptp.debug.internal.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.elementcontrols.IPJobControl;
import org.eclipse.ptp.core.elementcontrols.IPProcessControl;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPProcess;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.IPSession;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.event.IPDebugEvent;
import org.eclipse.ptp.debug.core.event.IPDebugInfo;
import org.eclipse.ptp.debug.core.event.PDebugErrorInfo;
import org.eclipse.ptp.debug.core.event.PDebugEvent;
import org.eclipse.ptp.debug.core.event.PDebugExitInfo;
import org.eclipse.ptp.debug.core.event.PDebugInfo;
import org.eclipse.ptp.debug.core.event.PDebugSuspendInfo;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.debug.core.pdi.IPDIBreakpointInfo;
import org.eclipse.ptp.debug.core.pdi.IPDIEndSteppingRangeInfo;
import org.eclipse.ptp.debug.core.pdi.IPDIErrorInfo;
import org.eclipse.ptp.debug.core.pdi.IPDIExitInfo;
import org.eclipse.ptp.debug.core.pdi.IPDIFunctionFinishedInfo;
import org.eclipse.ptp.debug.core.pdi.IPDILocationReachedInfo;
import org.eclipse.ptp.debug.core.pdi.IPDILocator;
import org.eclipse.ptp.debug.core.pdi.IPDIMemoryBlockInfo;
import org.eclipse.ptp.debug.core.pdi.IPDIRegisterInfo;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.IPDISessionObject;
import org.eclipse.ptp.debug.core.pdi.IPDISharedLibraryInfo;
import org.eclipse.ptp.debug.core.pdi.IPDISignalInfo;
import org.eclipse.ptp.debug.core.pdi.IPDITaskManager;
import org.eclipse.ptp.debug.core.pdi.IPDIThreadInfo;
import org.eclipse.ptp.debug.core.pdi.IPDIVariableInfo;
import org.eclipse.ptp.debug.core.pdi.IPDIWatchpointScopeInfo;
import org.eclipse.ptp.debug.core.pdi.IPDIWatchpointTriggerInfo;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.event.IPDIChangedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIConnectedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDICreatedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIDestroyedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIDisconnectedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIErrorEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEventListener;
import org.eclipse.ptp.debug.core.pdi.event.IPDIRestartedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIResumedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIStartedEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDISuspendedEvent;
import org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDILocationBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDITarget;
import org.eclipse.ptp.debug.internal.core.model.PDebugTarget;

/**
 * @author greg
 *
 */
public class PSession implements IPSession, IPDIEventListener {
	private IPDISession pdiSession = null;
	private IPLaunch launch  = null;
	private IProject project = null;
	private PSignalManager signalMgr = null;
	private PGlobalVariableManager globalMgr = null;
	private PBreakpointManager bptMgr = null;
	private PMemoryManager memMgr = null;
	private PThreadManager threadMgr = null;
	private PVariableManager varMgr = null;
	private PExpressionManager expMgr = null;
	private PRegisterManager regMgr = null;
	private PSetManager setMgr = null;

	public PSession(IPDISession pdiSession, IPLaunch launch, IProject project, IProgressMonitor monitor) {
		this.pdiSession = pdiSession;
		this.launch = launch;
		this.project = project;
		initialize(monitor);
	}
	
	/**
	 * @param monitor
	 */
	private void initialize(IProgressMonitor monitor) {
		setMgr = new PSetManager(this);
		signalMgr = new PSignalManager(this);
		globalMgr = new PGlobalVariableManager(this);
		bptMgr = new PBreakpointManager(this);
		memMgr = new PMemoryManager(this);
		regMgr = new PRegisterManager(this);
		threadMgr = new PThreadManager(this);
		varMgr = new PVariableManager(this);
		expMgr = new PExpressionManager(this);
		setMgr.initialize(monitor);
		signalMgr.initialize(monitor);
		globalMgr.initialize(monitor);
		bptMgr.initialize(monitor);
		memMgr.initialize(monitor);
		regMgr.initialize(monitor);
		threadMgr.initialize(monitor);
		varMgr.initialize(monitor);
		expMgr.initialize(monitor);
		getPDISession().getEventManager().addEventListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IPSession#dispose()
	 */
	public void dispose() {
		dispose(false);
	}
	
	/**
	 * @param force
	 */
	private void dispose(final boolean force) {
		WorkspaceJob aJob = new WorkspaceJob("Disposing session...") {
			public IStatus runInWorkspace(IProgressMonitor monitor) {
				signalMgr.dispose(monitor);
				globalMgr.dispose(monitor);
				bptMgr.dispose(monitor);
				memMgr.dispose(monitor);
				regMgr.dispose(monitor);
				varMgr.dispose(monitor);
				threadMgr.dispose(monitor);
				expMgr.dispose(monitor);
				setMgr.dispose(monitor);
				deleteDebugTargets(true);
				getPDISession().getEventManager().removeEventListener(PSession.this);
				getPDISession().shutdown(force);
				return Status.OK_STATUS;
			}
		};
		aJob.setSystem(true);
		aJob.schedule();		
	}
	
	/**
	 * @param monitor
	 * @throws CoreException
	 */
	public void connectToDebugger(IProgressMonitor monitor) throws CoreException {
		boolean failed = false;
		IPJob job = launch.getPJob();
		String app = job.getAttribute(JobAttributes.getExecutableNameAttributeDefinition()).getValueAsString();
		String path = job.getAttribute(JobAttributes.getExecutablePathAttributeDefinition()).getValueAsString();
		String dir = job.getAttribute(JobAttributes.getWorkingDirectoryAttributeDefinition()).getValueAsString();
		List<String> args = job.getAttribute(JobAttributes.getProgramArgumentsAttributeDefinition()).getValue();
		try {
			getPDISession().connectToDebugger(monitor, app, path, dir, args.toArray(new String[0]));
		} 
		catch (PDIException e) {
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), null));			
		}
		finally {
			if ((failed || monitor.isCanceled()))
				dispose();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IPSession#getSetManager()
	 */
	public PSetManager getSetManager() {
		return setMgr;
	}
	
	/**
	 * @return
	 */
	public PSignalManager getSignalManager() {
		return signalMgr;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IPSession#getBreakpointManager()
	 */
	public PBreakpointManager getBreakpointManager() {
		return bptMgr;
	}
	
	/**
	 * @return
	 */
	public PExpressionManager getExpressionManager() {
		return expMgr;
	}
	
	/**
	 * @return
	 */
	public PRegisterManager getRegisterManager() {
		return regMgr;
	}
	
	/**
	 * @return
	 */
	public PThreadManager getThreadManager() {
		return threadMgr;
	}
	
	/**
	 * @return
	 */
	public PMemoryManager getMemoryManager() {
		return memMgr;
	}
	
	/**
	 * @return
	 */
	public PVariableManager getVariableManager() {
		return varMgr;
	}
	
	/**
	 * @return
	 */
	public PGlobalVariableManager getGlobalVariableManager() {
		return globalMgr;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IPDISession.class))
			return this;
		if (adapter.equals(PSignalManager.class))
			return getSignalManager();
		return null;
	}
	
	/***********************************************************************
	 * Debug Event
	 ***********************************************************************/
	/**
	 * @param eTasks
	 * @return
	 */
	protected IPDebugInfo getDebugInfo(BitList eTasks) {
		IPDITaskManager taskMgr = getPDISession().getTaskManager();
		return new PDebugInfo(getJob(), eTasks, taskMgr.getRegisteredTasks(eTasks.copy()), taskMgr.getUnregisteredTasks(eTasks.copy()));
	}
	
	/****************************************
	 * IPDIEventListener
	 ****************************************/
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.event.IPDIEventListener#handleDebugEvents(org.eclipse.ptp.debug.core.pdi.event.IPDIEvent[])
	 */
	public void handleDebugEvents(IPDIEvent[] events) {
		for (IPDIEvent event : events) {
			if (event instanceof IPDIConnectedEvent) {
				PTPDebugCorePlugin.getDefault().fireDebugEvent(new PDebugEvent(getSession(), IPDebugEvent.CREATE, IPDebugEvent.DEBUGGER, getDebugInfo(event.getTasks())));
			}
			else if (event instanceof IPDIStartedEvent) {
				//only call once
				bptMgr.setInitialBreakpoints();
				boolean stopInMain = true;
				try {
					stopInMain = getLaunch().getLaunchConfiguration().getAttribute(IPTPLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, false);		
				}
				catch (CoreException e) {
					//do nothing
				}
				try {
					if (stopInMain) {
						bptMgr.setStopInMain(getPDISession().getTasks());
					}
					getPDISession().start(getPDISession().getTasks());
					getPDISession().setStatus(IPDISession.STARTED);
					PTPDebugCorePlugin.getDefault().fireDebugEvent(new PDebugEvent(getSession(), IPDebugEvent.CHANGE, IPDebugEvent.PROCESS_SPECIFIC, getDebugInfo(event.getTasks())));
				} catch (PDIException e) {
					IPDebugInfo errInfo = new PDebugErrorInfo(getDebugInfo(event.getTasks()), "Starting processes error!", e.getMessage(), PTPDebugCorePlugin.INTERNAL_ERROR);
					PTPDebugCorePlugin.getDefault().fireDebugEvent(new PDebugEvent(this, IPDebugEvent.ERROR, IPDebugEvent.UNSPECIFIED, errInfo));		
				}
			}
			else if (event instanceof IPDIDisconnectedEvent) {
				PTPDebugCorePlugin.getDefault().fireDebugEvent(new PDebugEvent(getSession(), IPDebugEvent.TERMINATE, IPDebugEvent.DEBUGGER, getDebugInfo(event.getTasks())));				
				dispose();
			}
			else if (event instanceof IPDIChangedEvent) {
				fireChangeEvent((IPDIChangedEvent)event);
			}
			else if (event instanceof IPDICreatedEvent) {
				fireCreateEvent((IPDICreatedEvent)event);
			}
			else if (event instanceof IPDIDestroyedEvent) {
				fireDestroyEvent((IPDIDestroyedEvent)event);
			}
			else if (event instanceof IPDIErrorEvent) {
				fireErrorEvent((IPDIErrorEvent)event);
			}
			else if (event instanceof IPDIRestartedEvent) {
				//TODO
			}
			else if (event instanceof IPDIResumedEvent) {
				fireResumeEvent((IPDIResumedEvent)event);
			}
			else if (event instanceof IPDISuspendedEvent) {
				fireSuspendEvent((IPDISuspendedEvent)event);
			}
			else {
				IPDebugEvent debugEvent = new PDebugEvent(this, IPDebugEvent.UNSPECIFIED, IPDebugEvent.UNSPECIFIED, getDebugInfo(event.getTasks()));		
				PTPDebugCorePlugin.getDefault().fireDebugEvent(debugEvent);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IPSession#forceStoppedDebugger(org.eclipse.ptp.core.elements.attributes.ProcessAttributes.State)
	 */
	public void forceStoppedDebugger(ProcessAttributes.State state) {
		BitList tasks = getTasks();
		changeProcessState(tasks, state);
		PTPDebugCorePlugin.getDefault().fireDebugEvent(new PDebugEvent(getSession(), IPDebugEvent.TERMINATE, IPDebugEvent.DEBUGGER, getDebugInfo(tasks)));
		dispose(true);
	}
	
	/**
	 * @param tasks
	 * @param state
	 */
	private void changeProcessState(BitList tasks, ProcessAttributes.State state) {
		IPJob job = getJob();
		List<IPProcessControl> processes = new ArrayList<IPProcessControl>();
		for (int task : tasks.toArray()) {
			IPProcess p = job.getProcessByIndex(task);
			if (p instanceof IPProcessControl) {
				processes.add((IPProcessControl)p);
			}
		}
		((IPJobControl)job).addProcessAttributes(processes, new IAttribute[] { ProcessAttributes.getStateAttributeDefinition().create(state) });
	}
	
	/**
	 * @param event
	 */
	public void fireSuspendEvent(IPDISuspendedEvent event) {
		/*
		try {
			getPDISession().validateStepReturn(event.getTasks().copy());
		}
		catch (PDIException e) {
			IPDebugInfo errInfo = new PDebugErrorInfo(getDebugInfo(e.getTasks()), "Check step return error!", e.getMessage(), PTPDebugCorePlugin.INTERNAL_ERROR);
			PTPDebugCorePlugin.getDefault().fireDebugEvent(new PDebugEvent(this, IPDebugEvent.ERROR, IPDebugEvent.UNSPECIFIED, errInfo));		
		}
		*/
		IPDebugInfo baseInfo = getDebugInfo(event.getTasks());
		int detail = IPDebugEvent.UNSPECIFIED;
		
		int lineNumber = 0;
		int level = event.getLevel();
		int depth = event.getDepth();
		String fileName = "";
		IPDISessionObject reason = event.getReason();
		if (reason instanceof IPDIBreakpointInfo) {
			IPDIBreakpoint bpt = ((IPDIBreakpointInfo)reason).getBreakpoint();
			if (bpt instanceof IPDILocationBreakpoint) {
				IPDILocator locator = ((IPDILocationBreakpoint) bpt).getLocator();
				if (locator != null) {
					lineNumber = locator.getLineNumber();
					fileName += locator.getFile();
					detail = IPDebugEvent.BREAKPOINT;
				}
			}
		}
		else if (reason instanceof IPDIEndSteppingRangeInfo) {
			IPDILocator locator = ((IPDIEndSteppingRangeInfo)reason).getLocator();
			if (locator != null) {
				lineNumber = locator.getLineNumber();
				fileName += locator.getFile();
				detail = IPDebugEvent.STEP_END;
			}
		}
		else if (reason instanceof IPDILocationReachedInfo) {
			IPDILocator locator = ((IPDILocationReachedInfo)reason).getLocator();
			if (locator != null) {
				lineNumber = locator.getLineNumber();
				fileName += locator.getFile();
				detail = IPDebugEvent.CLIENT_REQUEST;
			}
		}
		else if (reason instanceof IPDISignalInfo) {
			IPDILocator locator = ((IPDISignalInfo)reason).getLocator();
			if (locator != null) {
				lineNumber = locator.getLineNumber();
				fileName += locator.getFile();
				detail = IPDebugEvent.CLIENT_REQUEST;
			}
		}
		else if (reason instanceof IPDIFunctionFinishedInfo) {
			//TODO
		}
		else if (reason instanceof IPDISharedLibraryInfo) {
			//TODO
		}
		else if (reason instanceof IPDIWatchpointScopeInfo) {
			//TODO
		}
		else if (reason instanceof IPDIWatchpointTriggerInfo) {
			//TODO
		}
		changeProcessState(event.getTasks(), ProcessAttributes.State.SUSPENDED);
		//if (lineNumber == 0)
			//lineNumber = 1;
		PTPDebugCorePlugin.getDefault().fireDebugEvent(new PDebugEvent(getSession(), IPDebugEvent.SUSPEND, detail, new PDebugSuspendInfo(baseInfo, fileName, lineNumber, level, depth)));
	}
	
	/**
	 * @param event
	 */
	public void fireResumeEvent(IPDIResumedEvent event) {
		IPDebugInfo baseInfo = getDebugInfo(event.getTasks());
		int detail = IPDebugEvent.UNSPECIFIED;

		switch (event.getType()) {
		case IPDIResumedEvent.STEP_INTO:
		case IPDIResumedEvent.STEP_INTO_INSTRUCTION:
			detail = IPDebugEvent.STEP_INTO;
			break;
		case IPDIResumedEvent.STEP_OVER:
		case IPDIResumedEvent.STEP_OVER_INSTRUCTION:
			detail = IPDebugEvent.STEP_OVER;
			break;
		case IPDIResumedEvent.STEP_RETURN:
			detail = IPDebugEvent.STEP_RETURN;
			break;
		}

		changeProcessState(event.getTasks(), ProcessAttributes.State.RUNNING);

		PTPDebugCorePlugin.getDefault().fireDebugEvent(new PDebugEvent(this, IPDebugEvent.RESUME, detail, baseInfo));
	}
	
	/**
	 * @param event
	 */
	public void fireDestroyEvent(IPDIDestroyedEvent event) {
		IPDebugEvent debugEvent = null;
		IPDebugInfo baseInfo = getDebugInfo(event.getTasks());
		IPDISessionObject reason = event.getReason();

		if (reason instanceof IPDIBreakpointInfo) {
		}
		else if (reason instanceof IPDIErrorInfo) {
			deleteDebugTarget(baseInfo.getAllRegisteredTasks().copy(), true, true);
			IPDebugInfo errInfo = new PDebugErrorInfo(baseInfo, ((IPDIErrorInfo)reason).getMessage(), ((IPDIErrorInfo)reason).getDetailMessage(), ((IPDIErrorInfo)reason).getCode());
			debugEvent = new PDebugEvent(this, IPDebugEvent.TERMINATE, IPDebugEvent.ERROR, errInfo);
			changeProcessState(event.getTasks(), ProcessAttributes.State.ERROR);
		}
		else if (reason instanceof IPDIExitInfo) {
			deleteDebugTarget(baseInfo.getAllRegisteredTasks().copy(), true, true);
			IPDebugInfo exitInfo = new PDebugExitInfo(baseInfo, ((IPDIExitInfo)reason).getCode(), "Exited", "Exited");
			debugEvent = new PDebugEvent(this, IPDebugEvent.TERMINATE, IPDebugEvent.PROCESS_SPECIFIC, exitInfo);
			changeProcessState(event.getTasks(), ProcessAttributes.State.EXITED);
		}
		else if (reason instanceof IPDISignalInfo) {
			deleteDebugTarget(baseInfo.getAllRegisteredTasks().copy(), true, true);
			IPDebugInfo exitInfo = new PDebugExitInfo(baseInfo, 0, ((IPDISignalInfo)reason).getDescription(), ((IPDISignalInfo)reason).getName());
			debugEvent = new PDebugEvent(this, IPDebugEvent.TERMINATE, IPDebugEvent.PROCESS_SPECIFIC, exitInfo);
			changeProcessState(event.getTasks(), ProcessAttributes.State.EXITED_SIGNALLED);
		}
		else if (reason instanceof IPDISharedLibraryInfo) {
			//TODO
		}
		else if (reason instanceof IPDIThreadInfo) {
			//TODO
		}
		else if (reason instanceof IPDIVariableInfo) {
			//TODO
		}
		else {
			debugEvent = new PDebugEvent(this, IPDebugEvent.TERMINATE, IPDebugEvent.UNSPECIFIED, baseInfo);		
		}
		PTPDebugCorePlugin.getDefault().fireDebugEvent(debugEvent);
	}
	
	/**
	 * @param event
	 */
	public void fireErrorEvent(IPDIErrorEvent event) {
		IPDebugInfo baseInfo = getDebugInfo(event.getTasks());
		int detail = IPDebugEvent.UNSPECIFIED;
		IPDISessionObject reason = event.getReason();
		if (reason instanceof IPDIErrorInfo) {
			int code = ((IPDIErrorInfo)reason).getCode();
			switch (code) {
			case IPDIErrorInfo.DBG_FATAL:
				detail = IPDebugEvent.ERR_FATAL;
				//only fatal error reports process error
				changeProcessState(event.getTasks(), ProcessAttributes.State.ERROR);
				break;
			case IPDIErrorInfo.DBG_WARNING:
				detail = IPDebugEvent.ERR_WARNING;
				break;
			case IPDIErrorInfo.DBG_IGNORE:
			case IPDIErrorInfo.DBG_NORMAL:
				detail = IPDebugEvent.ERR_NORMAL;
				break;
			}
			IPDebugInfo errInfo = new PDebugErrorInfo(baseInfo, ((IPDIErrorInfo)reason).getMessage(), ((IPDIErrorInfo)reason).getDetailMessage(), ((IPDIErrorInfo)reason).getCode());			
			PTPDebugCorePlugin.getDefault().fireDebugEvent(new PDebugEvent(this, IPDebugEvent.ERROR, detail, errInfo));
		}
		else {
			IPDebugInfo errInfo = new PDebugErrorInfo(baseInfo, "Internal Error!", "Unknown", PTPDebugCorePlugin.INTERNAL_ERROR);
			PTPDebugCorePlugin.getDefault().fireDebugEvent(new PDebugEvent(this, IPDebugEvent.ERROR, IPDebugEvent.UNSPECIFIED, errInfo));		
		}
	}
	
	/**
	 * @param event
	 */
	public void fireChangeEvent(IPDIChangedEvent event) {
		IPDebugInfo baseInfo = getDebugInfo(event.getTasks());
		int detail = IPDebugEvent.UNSPECIFIED;
		
		IPDISessionObject reason = event.getReason();
		if (reason instanceof IPDIBreakpointInfo) {
		}
		else if (reason instanceof IPDIMemoryBlockInfo) {
			//TODO
		}
		else if (reason instanceof IPDISignalInfo) {
			//TODO
		}
		else if (reason instanceof IPDIVariableInfo) {
			//TODO
		}
		PTPDebugCorePlugin.getDefault().fireDebugEvent(new PDebugEvent(this, IPDebugEvent.CHANGE, detail, baseInfo));
	}
	
	/**
	 * @param event
	 */
	public void fireCreateEvent(IPDICreatedEvent event) {
		IPDebugInfo baseInfo = getDebugInfo(event.getTasks());
		int detail = IPDebugEvent.UNSPECIFIED;

		IPDISessionObject reason = event.getReason();
		if (reason instanceof IPDIBreakpointInfo) {
			
		}
		else if (reason instanceof IPDIThreadInfo) {
			
		}
		else if (reason instanceof IPDIMemoryBlockInfo) {
			
		}
		else if (reason instanceof IPDIRegisterInfo) {
			
		}
		else if (reason instanceof IPDISharedLibraryInfo) {
			
		}
		else if (reason instanceof IPDIVariableInfo) {
			
		}
		else {
			
		}
		PTPDebugCorePlugin.getDefault().fireDebugEvent(new PDebugEvent(this, IPDebugEvent.CREATE, detail, baseInfo));
	}
	
	/**
	 * @param type
	 * @param details
	 * @param info
	 */
	public void fireDebugEvent(int type, int details, IPDebugInfo info) {
		PTPDebugCorePlugin.getDefault().fireDebugEvent(new PDebugEvent(this, type, details, info));
	}
	
	/**
	 * @param type
	 * @param details
	 * @param tasks
	 */
	public void fireDebugEvent(int type, int details, BitList tasks) {
		PTPDebugCorePlugin.getDefault().fireDebugEvent(new PDebugEvent(this, type, details, getDebugInfo(tasks)));
	}
	/*********************************************************************
	 * 
	 *********************************************************************/
	/**
	 * @return
	 */
	public IPSession getSession() {
		return this;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IPSession#getPDISession()
	 */
	public IPDISession getPDISession() {
		return pdiSession;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IPSession#getJob()
	 */
	public IPJob getJob() {
		return getLaunch().getPJob();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IPSession#getLaunch()
	 */
	public IPLaunch getLaunch() {
		return launch;
	}
	
	/**
	 * @return
	 */
	public IProject getProject() {
		return project;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IPSession#findDebugTarget(org.eclipse.ptp.core.util.BitList)
	 */
	public PDebugTarget findDebugTarget(BitList tasks) {
		return (PDebugTarget)launch.getDebugTarget(tasks);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IPSession#getTasks(int)
	 */
	public BitList getTasks(int id) {
		int max = getPDISession().getTotalTasks();
		BitList tasks = new BitList(max);
		if (id >= 0 && id <= max)
			tasks.set(id);
		return tasks;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IPSession#getTasks()
	 */
	public BitList getTasks() {
		return getPDISession().getTasks();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IPSession#isReady()
	 */
	public boolean isReady() {
		return (getPDISession().getStatus() == IPDISession.STARTED);
	}
	
	/****************************************
	 * register / unregister
	 ****************************************/
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IPSession#deleteDebugTargets(boolean)
	 */
	public void deleteDebugTargets(boolean register) {
		if (isReady()) {
			BitList tasks = getPDISession().getTaskManager().getRegisteredTasks().copy();
			deleteDebugTarget(tasks, true, register);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IPSession#reloadDebugTargets(org.eclipse.ptp.core.util.BitList, boolean, boolean)
	 */
	public void reloadDebugTargets(BitList tasks, boolean refresh, boolean register) {
		if (isReady()) {
			BitList curRegTasks = getPDISession().getTaskManager().getRegisteredTasks(tasks.copy());
			BitList othRegTasks = getPDISession().getTaskManager().getRegisteredTasks().copy();
			othRegTasks.andNot(curRegTasks);
			deleteDebugTarget(othRegTasks, refresh, register);
			createDebugTarget(curRegTasks, refresh, register);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IPSession#createDebugTarget(org.eclipse.ptp.core.util.BitList, boolean, boolean)
	 */
	public void createDebugTarget(BitList tasks, boolean refresh, boolean register) {
		if (isReady()) {
			List<IPDITarget> targets = new ArrayList<IPDITarget>();
			int[] task_array = tasks.toArray();
			for (int task_id : task_array) {
				IPDITarget target = getPDISession().getTargetManager().addTarget(getTasks(task_id));
				if (target != null) {
					targets.add(target);
				}
				else {
					tasks.clear(task_id);
				}
			}
			if (register)
				getPDISession().getTaskManager().setRegisterTasks(true, tasks);
			PTPDebugCorePlugin.getDebugModel().addNewDebugTargets(getLaunch(), tasks, targets.toArray(new IPDITarget[0]), refresh, false);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IPSession#deleteDebugTarget(org.eclipse.ptp.core.util.BitList, boolean, boolean)
	 */
	public void deleteDebugTarget(BitList tasks, boolean refresh, boolean register) {
		int[] task_array = tasks.toArray();
		for (int task_id : task_array) {
			if (!getPDISession().getTargetManager().removeTarget(getTasks(task_id))) {
				tasks.clear(task_id);
			}
		}
		if (register)
			getPDISession().getTaskManager().setRegisterTasks(false, tasks);
		PTPDebugCorePlugin.getDebugModel().removeDebugTarget(getLaunch(), tasks, refresh);
	}
}
