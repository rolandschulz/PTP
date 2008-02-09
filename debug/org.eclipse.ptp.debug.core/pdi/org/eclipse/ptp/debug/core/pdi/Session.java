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
package org.eclipse.ptp.debug.core.pdi;

import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.PDebugUtils;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEventFactory;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIBreakpointManager;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIEventManager;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIEventRequestManager;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIExpressionManager;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIManagerFactory;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIMemoryManager;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIRegisterManager;
import org.eclipse.ptp.debug.core.pdi.manager.IPDISignalManager;
import org.eclipse.ptp.debug.core.pdi.manager.IPDISourceManager;
import org.eclipse.ptp.debug.core.pdi.manager.IPDITargetManager;
import org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIThreadManager;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIVariableManager;
import org.eclipse.ptp.debug.core.pdi.model.IPDIModelFactory;
import org.eclipse.ptp.debug.core.pdi.model.IPDISignal;
import org.eclipse.ptp.debug.core.pdi.model.IPDITarget;
import org.eclipse.ptp.debug.core.pdi.model.IPDIThread;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIF;
import org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory;
import org.eclipse.ptp.debug.core.pdi.request.IPDIStartDebuggerRequest;

/**
 * @author clement
 *
 */
public class Session implements IPDISession {
	private IPDIBreakpointManager breakpointManager;
	private IPDIEventManager eventManager;
	private IPDIEventRequestManager eventRequestManager;
	private IPDITaskManager taskManager;
	private IPDITargetManager targetManager; 
	private IPDIThreadManager threadManager; 
	private IPDIExpressionManager expressionManager;
	private IPDIVariableManager variableManager;
	private IPDISourceManager sourceManager;
	private IPDIMemoryManager memoryManager;
	private IPDISignalManager signalManager;
	private IPDIRegisterManager registerManager;
	private IPDIDebugger debugger = null;
	private String job_id;
	private int total_tasks = 0;
	private int status;
	private long timeout = 30000;
	private NotifyJob notifyJob = null;
	private IPDIRequestFactory requestFactory;
	private IPDIEventFactory eventFactory;
	private IPDIModelFactory modelFactory;
	private final ReentrantLock	waitLock = new ReentrantLock();
	private ILaunchConfiguration config = null;
	
	public Session(IPDIManagerFactory managerFactory,IPDIRequestFactory requestFactory, 
			IPDIEventFactory eventFactory, IPDIModelFactory modelFactory, 
			ILaunchConfiguration config, long timeout, IPDIDebugger debugger, 
			String job_id, int total_tasks) throws PDIException {
		this.config = config;
		this.timeout = timeout;
		this.debugger = debugger;
		this.job_id = job_id;
		this.total_tasks = total_tasks;
		this.notifyJob = new NotifyJob();
		this.requestFactory = requestFactory;
		this.eventFactory = eventFactory;
		this.modelFactory = modelFactory;
		
		setEventRequestManager(managerFactory.newEventRequestManager(this));
		setEventManager(managerFactory.newEventManager(this));
		setTaskManager(managerFactory.newTaskManager(this));
		setTargetManager(managerFactory.newTargetManager(this));
		setThreadManager(managerFactory.newThreadManager(this));
		setBreakpointManager(managerFactory.newBreakpointManager(this));
		setExpressionManager(managerFactory.newExpressionManager(this));
		setVariableManager(managerFactory.newVariableManager(this));
		setSourceManager(managerFactory.newSourceManager(this));
		setMemeoryManager(managerFactory.newMemoryManager(this));
		setSignalManager(managerFactory.newSignalManager(this));
		setRegisterManager(managerFactory.newRegisterManager(this));
	}
	
	/**
	 * @return
	 */
	public ILaunchConfiguration getConfiguration() {
		return config;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#getRequestFactory()
	 */
	public IPDIRequestFactory getRequestFactory() {
		return requestFactory;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#getTimeout()
	 */
	public long getTimeout() {
		return timeout;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#setRequestTimeout(long)
	 */
	public void setRequestTimeout(long timeout) {
		this.timeout = timeout;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#connectToDebugger(org.eclipse.core.runtime.IProgressMonitor, java.lang.String, java.lang.String, java.lang.String, java.lang.String[])
	 */
	public void connectToDebugger(IProgressMonitor monitor, String app, String path, String dir, String[] args) throws PDIException {
		setStatus(CONNECTING);
		if (!getDebugger().isConnected(monitor)) {
			throw new PDIException(getTasks(), "Cannot connect to debugger");
		}
		getDebugger().register(eventManager);
		monitor.beginTask("Initialising debug processes ("+total_tasks+")...", total_tasks);
		IPDIStartDebuggerRequest request = getRequestFactory().getStartDebuggerRequest(getTasks(), app, path, dir, args);
		eventRequestManager.addEventRequest(request);
		request.waitUntilCompleted(null, monitor);
		setStatus(CONNECTED);
		eventManager.fireEvent(getEventFactory().newStartedEvent(this, getTasks()));
	}
	
	/**
	 * @param manager
	 */
	protected void setEventRequestManager(IPDIEventRequestManager manager) {
		eventRequestManager = manager;
	}
	
	/**
	 * @param manager
	 */
	protected void setEventManager(IPDIEventManager manager) {
		eventManager = manager;
	}
	
	/**
	 * @param manager
	 */
	protected void setTaskManager(IPDITaskManager manager) {
		taskManager = manager;
	}
	
	/**
	 * @param manager
	 */
	protected void setTargetManager(IPDITargetManager manager) {
		targetManager = manager;
	}
	
	/**
	 * @param manager
	 */
	protected void setThreadManager(IPDIThreadManager manager) {
		threadManager = manager;
	}
	
	/**
	 * @param manager
	 */
	protected void setBreakpointManager(IPDIBreakpointManager manager) {
		breakpointManager = manager;
	}
	
	/**
	 * @param manager
	 */
	protected void setExpressionManager(IPDIExpressionManager manager) {
		expressionManager = manager;
	}
	
	/**
	 * @param manager
	 */
	protected void setVariableManager(IPDIVariableManager manager) {
		variableManager = manager;
	}
	
	/**
	 * @param manager
	 */
	protected void setSourceManager(IPDISourceManager manager) {
		sourceManager = manager;
	}
	
	/**
	 * @param manager
	 */
	protected void setMemeoryManager(IPDIMemoryManager manager) {
		memoryManager = manager;
	}
	
	/**
	 * @param manager
	 */
	protected void setSignalManager(IPDISignalManager manager) {
		signalManager = manager;
	}
	
	/**
	 * @param manager
	 */
	protected void setRegisterManager(IPDIRegisterManager manager) {
		registerManager = manager;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#getEventRequestManager()
	 */
	public IPDIEventRequestManager getEventRequestManager() {
		return eventRequestManager;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#getEventManager()
	 */
	public IPDIEventManager getEventManager() {
		return eventManager;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#getTaskManager()
	 */
	public IPDITaskManager getTaskManager() {
		return taskManager;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#getThreadManager()
	 */
	public IPDIThreadManager getThreadManager() {
		return threadManager;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#getTargetManager()
	 */
	public IPDITargetManager getTargetManager() {
		return targetManager;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#getBreakpointManager()
	 */
	public IPDIBreakpointManager getBreakpointManager() {
		return breakpointManager;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#getExpressionManager()
	 */
	public IPDIExpressionManager getExpressionManager() {
		return expressionManager;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#getVariableManager()
	 */
	public IPDIVariableManager getVariableManager() {
		return variableManager;
	}
	
	/**
	 * @return
	 */
	public IPDISourceManager getSourceManager() {
		return sourceManager;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#getMemoryManager()
	 */
	public IPDIMemoryManager getMemoryManager() {
		return memoryManager;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#getSignalManager()
	 */
	public IPDISignalManager getSignalManager() {
		return signalManager;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#getRegisterManager()
	 */
	public IPDIRegisterManager getRegisterManager() {
		return registerManager;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#shutdown(boolean)
	 */
	public void shutdown(boolean force) {
		try {
			if (!force) {
				exit();
			}
			else
				setStatus(EXITING);
			debugger.disconnect(eventManager);
		}
		catch (PDIException e) {
			e.printStackTrace();
		}
		finally {
			variableManager.shutdown();
			expressionManager.shutdown();
			breakpointManager.shutdown();
			eventManager.shutdown();
			sourceManager.shutdown();
			taskManager.shutdown();
			targetManager.shutdown();
			threadManager.shutdown();
			memoryManager.shutdown();
			signalManager.shutdown();
			eventRequestManager.shutdown();
			notifyJob.schedule();
		}
	}
	
	/*******************************************
	 * IPDIExecuteManagement
	 *******************************************/
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#restart(org.eclipse.ptp.core.util.BitList)
	 */
	public void restart(BitList tasks) throws PDIException {
		checkStatus();
		throw new PDIException(tasks, "Not implement restart() yet");
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#start(org.eclipse.ptp.core.util.BitList)
	 */
	public void start(BitList tasks) throws PDIException {
		checkStatus();
		getEventRequestManager().addEventRequest(getRequestFactory().getResumeRequest(tasks, false));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#resume(org.eclipse.ptp.core.util.BitList, boolean)
	 */
	public void resume(BitList tasks, boolean passSignal) throws PDIException {
		checkStatus();
		if (passSignal)
			throw new PDIException(tasks, "Not implment resume() - Pass Signal yet");
		taskManager.getSuspendedTasks(tasks);
		if (tasks.isEmpty())
			throw new PDIException(tasks, "No suspended processes found");
		getEventRequestManager().addEventRequest(getRequestFactory().getResumeRequest(tasks, passSignal));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#resume(org.eclipse.ptp.core.util.BitList, org.eclipse.ptp.debug.core.pdi.IPDILocation)
	 */
	public void resume(BitList tasks, IPDILocation location) throws PDIException {
		checkStatus();
		taskManager.getSuspendedTasks(tasks);
		if (tasks.isEmpty())
			throw new PDIException(tasks, "No suspended processes found");
		throw new PDIException(tasks, "Not implment resume(IPDILocation) yet");
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#resume(org.eclipse.ptp.core.util.BitList, org.eclipse.ptp.debug.core.pdi.model.IPDISignal)
	 */
	public void resume(BitList tasks, IPDISignal signal) throws PDIException {
		checkStatus();
		taskManager.getSuspendedTasks(tasks);
		if (tasks.isEmpty())
			throw new PDIException(tasks, "No suspended processes found");
		throw new PDIException(tasks, "Not implment resume(IPDISignal) yet");
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#stepInto(org.eclipse.ptp.core.util.BitList, int)
	 */
	public void stepInto(BitList tasks, int count) throws PDIException {
		checkStatus();
		taskManager.getSuspendedTasks(tasks);
		if (tasks.isEmpty())
			throw new PDIException(tasks, "No suspended processes found");
		getEventRequestManager().addEventRequest(getRequestFactory().getStepIntoRequest(tasks, count));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#stepIntoInstruction(org.eclipse.ptp.core.util.BitList, int)
	 */
	public void stepIntoInstruction(BitList tasks, int count) throws PDIException {
		checkStatus();
		taskManager.getSuspendedTasks(tasks);
		if (tasks.isEmpty())
			throw new PDIException(tasks, "No suspended processes found");
		throw new PDIException(tasks, "Not implment stepIntoInstruction() yet");
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#stepOver(org.eclipse.ptp.core.util.BitList, int)
	 */
	public void stepOver(BitList tasks, int count) throws PDIException {
		checkStatus();
		taskManager.getSuspendedTasks(tasks);
		if (tasks.isEmpty())
			throw new PDIException(tasks, "No suspended processes found");
		getEventRequestManager().addEventRequest(getRequestFactory().getStepOverRequest(tasks, count));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#stepOverInstruction(org.eclipse.ptp.core.util.BitList, int)
	 */
	public void stepOverInstruction(BitList tasks, int count) throws PDIException {
		checkStatus();
		taskManager.getSuspendedTasks(tasks);
		if (tasks.isEmpty())
			throw new PDIException(tasks, "No suspended processes found");
		throw new PDIException(tasks, "Not implment stepOverInstruction() yet");
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#stepReturn(org.eclipse.ptp.core.util.BitList, org.eclipse.ptp.debug.core.pdi.model.aif.IAIF)
	 */
	public void stepReturn(BitList tasks, IAIF aif) throws PDIException {
		checkStatus();
		taskManager.getSuspendedTasks(tasks);
		if (tasks.isEmpty())
			throw new PDIException(tasks, "No suspended processes found");
		throw new PDIException(tasks, "Not implment stepReturn(IAIF) yet");
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#stepReturn(org.eclipse.ptp.core.util.BitList, int)
	 */
	public void stepReturn(BitList tasks, int count) throws PDIException {
		checkStatus();
		taskManager.getCanStepReturnTasks(tasks);
		if (tasks.isEmpty())
			throw new PDIException(tasks, "No suspended processes found");
		getEventRequestManager().addEventRequest(getRequestFactory().getStepFinishRequest(tasks, count));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#stepUntil(org.eclipse.ptp.core.util.BitList, org.eclipse.ptp.debug.core.pdi.IPDILocation)
	 */
	public void stepUntil(BitList tasks, IPDILocation location) throws PDIException {
		checkStatus();
		taskManager.getSuspendedTasks(tasks);
		if (tasks.isEmpty())
			throw new PDIException(tasks, "No suspended processes found");
		throw new PDIException(tasks, "Not implment stepUntil(IPDILocation) yet");
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#suspend(org.eclipse.ptp.core.util.BitList)
	 */
	public void suspend(BitList tasks) throws PDIException {
		checkStatus();
		taskManager.getRunningTasks(tasks);
		if (tasks.isEmpty())
			throw new PDIException(tasks, "No running processes found");
		getEventRequestManager().addEventRequest(getRequestFactory().getSuspendRequest(tasks));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#terminate(org.eclipse.ptp.core.util.BitList)
	 */
	public void terminate(BitList tasks) throws PDIException {
		checkStatus();
		taskManager.getNonTerminatedTasks(tasks);
		if (tasks.isEmpty())
			throw new PDIException(tasks, "All processes have been terminated");

		BitList nonTerTasks = tasks.copy();
		taskManager.getRunningTasks(nonTerTasks);
		if (!nonTerTasks.isEmpty()) {
			getEventRequestManager().addEventRequest(getRequestFactory().getSuspendRequest(nonTerTasks, false));
		}
		getEventRequestManager().addEventRequest(getRequestFactory().getTerminateRequest(tasks));
		taskManager.setPendingTasks(true, tasks);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#validateStepReturn(org.eclipse.ptp.core.util.BitList)
	 */
	public void validateStepReturn(BitList tasks) throws PDIException {
		/*
		taskManager.getUnregisteredTasks(tasks);
		if (!tasks.isEmpty()) {
			getEventRequestManager().addEventRequest(getRequestFactory().getGetStackInfoDepthRequest(tasks));
			//FIXME: for testing
			//getEventRequestManager().addEventRequest(getRequestFactory().getTerminateRequest(tasks.copy()));
		}
		*/
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#exit()
	 */
	public void exit() throws PDIException {
		if (status != EXITING && status != EXITED) {
			setStatus(EXITING);
			eventRequestManager.cleanEventRequests();
			BitList tasks = getTasks();
			taskManager.getRunningTasks(tasks);
			if (!tasks.isEmpty()) {
				getEventRequestManager().addEventRequest(getRequestFactory().getSuspendRequest(tasks, false));
			}
			tasks = getTasks();
			taskManager.getNonTerminatedTasks(tasks);
			if (!tasks.isEmpty())
				getEventRequestManager().addEventRequest(getRequestFactory().getTerminateRequest(tasks));
			
			getEventRequestManager().addEventRequest(getRequestFactory().getStopDebuggerRequest(new BitList(total_tasks)));
			taskManager.setPendingTasks(true, tasks);
		}
	}
	
	/*******************************************
	 * IPDISession
	 *******************************************/
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#getDebugger()
	 */
	public IPDIDebugger getDebugger() {
		return debugger;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#getJobID()
	 */
	public String getJobID() {
		return job_id;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#isTerminated(org.eclipse.ptp.core.util.BitList)
	 */
	public boolean isTerminated(BitList tasks) {
		return taskManager.isAllTerminated(tasks);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#isSuspended(org.eclipse.ptp.core.util.BitList)
	 */
	public boolean isSuspended(BitList tasks) {
		return taskManager.isAllSuspended(tasks);
	}
	
	/**
	 * @param tasks
	 * @return
	 */
	public boolean isRunning(BitList tasks) {
		return taskManager.isAllRunning(tasks);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#getTasks()
	 */
	public BitList getTasks() {
		BitList tasks = new BitList(total_tasks);
		tasks.set(0, total_tasks);
		return tasks;
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#getTotalTasks()
	 */
	public int getTotalTasks() {
		return total_tasks;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISessionObject#getSession()
	 */
	public IPDISession getSession() {
		return this;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#setStatus(int)
	 */
	public void setStatus(int status) {
		waitLock.lock();
		try {
			this.status = status;
		}
		finally {
			waitLock.unlock();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#getStatus()
	 */
	public int getStatus() {
		return status;
	}
	
	/**
	 * @throws PDIException
	 */
	protected void checkStatus() throws PDIException {
		if (status == EXITING || status == EXITED)
			throw new PDIException(null, "Cannot process for your request due to session is exiting or exited.");
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#findTarget(org.eclipse.ptp.core.util.BitList)
	 */
	public IPDITarget findTarget(BitList qTasks) throws PDIException {
		IPDITarget target = targetManager.getTarget(qTasks);
		if (target == null)
			throw new PDIException(qTasks, "No target found.");
		return target;
	}
	
	/**
	 * @param qTasks
	 * @return
	 */
	public boolean isTarget(BitList qTasks) {
		return (targetManager.getTarget(qTasks) != null);
	}
	
	/**********************************************
	 * process on running or suspended 
	 **********************************************/
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#processRunningEvent(org.eclipse.ptp.core.util.BitList)
	 */
	public synchronized void processRunningEvent(BitList tasks) {
		IPDITarget[] targets = targetManager.getTargets();
		for (final IPDITarget target : targets) {
			if (target.getTasks().intersects(tasks)) {
				Runnable runnable = new Runnable() {
					public void run() {
						target.setSupended(false);
					}
				};
				queueRunnable(runnable);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#processSupsendedEvent(org.eclipse.ptp.core.util.BitList, int, java.lang.String[])
	 */
	public synchronized void processSupsendedEvent(BitList tasks, final int thread_id, final String[] vars) {
		IPDITarget[] targets = targetManager.getTargets();
		for (final IPDITarget target : targets) {
			if (target.getTasks().intersects(tasks)) {
				Runnable runnable = new Runnable() {
					public void run() {
						target.setSupended(true);
						target.updateState(thread_id);
						try {
							IPDIThread pthread = target.getCurrentThread();
							if (pthread == null)
								return;
							
							pthread.getCurrentStackFrame();
						} catch (PDIException e) {
							return;
						}

						try {
							if (variableManager.isAutoUpdate()) {
								variableManager.update(target.getTasks(), vars);
							}
							if (expressionManager.isAutoUpdate()) { 
								expressionManager.update(target.getTasks(), vars);
							}
							if (registerManager.isAutoUpdate()) {
								registerManager.update(target.getTasks());
							}
							if (memoryManager.isAutoUpdate()) {
								memoryManager.update(target.getTasks());
							}
							if (breakpointManager.isAutoUpdate()) {
								breakpointManager.update(target.getTasks());
							}
							if (signalManager.isAutoUpdate()) {
								signalManager.update(target.getTasks());
							}
							if (sourceManager.isAutoUpdate()) {
								sourceManager.update(target.getTasks());
							}
						} catch (PDIException e) {
							return;
						}
					}
				};
				queueRunnable(runnable);
			}
		}
	}
	
	/*************************************************
	 * Notify Job
	 *************************************************/
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#queueRunnable(java.lang.Runnable)
	 */
	public void queueRunnable(Runnable runnable) {
		notifyJob.addRunnable(runnable);
	}
	
	class NotifyJob extends Job {
		private Vector<Runnable> fRunnables;
		public NotifyJob() {
			super("PTP Notify Job");
			setSystem(true);
			fRunnables = new Vector<Runnable>(10);
		}
		public void addRunnable(Runnable runnable) {
			synchronized (fRunnables) {
				fRunnables.add(runnable);
			}
			schedule();
		}
		public boolean shouldRun() {
			return !fRunnables.isEmpty();
		}
		public IStatus run(IProgressMonitor monitor) {
			Runnable[] runnables;
			synchronized (fRunnables) {
				runnables = fRunnables.toArray(new Runnable[0]);
				fRunnables.clear();
			}
			MultiStatus failed = null;
			monitor.beginTask(getName(), runnables.length);
			PDebugUtils.println("Msg: NotifyJob - size of runnables: " + runnables.length);
			for (Runnable runnable : runnables) {
				try {
					runnable.run();
				}
				catch (Exception e) {
					if (failed == null)
						failed = new MultiStatus(PTPDebugCorePlugin.getUniqueIdentifier(), PTPDebugCorePlugin.INTERNAL_ERROR, "Event notify error", null);
					failed.add(new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), PTPDebugCorePlugin.INTERNAL_ERROR, "Event notify error", e));
				}
				monitor.worked(1);
			}
			monitor.done();
			if (failed == null)
				return Status.OK_STATUS;
			
			return failed;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#getEventFactory()
	 */
	public IPDIEventFactory getEventFactory() {
		return eventFactory;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#getModelFactory()
	 */
	public IPDIModelFactory getModelFactory() {
		return modelFactory;
	}
}
