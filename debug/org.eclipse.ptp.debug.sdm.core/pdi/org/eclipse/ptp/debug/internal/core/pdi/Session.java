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
package org.eclipse.ptp.debug.internal.core.pdi;

import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.IRequestFactory;
import org.eclipse.ptp.debug.core.PDebugUtils;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.pdi.IPDIDebugger;
import org.eclipse.ptp.debug.core.pdi.IPDILocation;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.IPDISessionObject;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.model.IPDISignal;
import org.eclipse.ptp.debug.core.pdi.model.IPDITarget;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIF;
import org.eclipse.ptp.debug.external.core.RequestFactory;
import org.eclipse.ptp.debug.internal.core.pdi.event.StartedEvent;
import org.eclipse.ptp.debug.internal.core.pdi.model.Target;
import org.eclipse.ptp.debug.internal.core.pdi.model.Thread;
import org.eclipse.ptp.debug.internal.core.pdi.request.ResumeRequest;
import org.eclipse.ptp.debug.internal.core.pdi.request.StartDebuggerRequest;
import org.eclipse.ptp.debug.internal.core.pdi.request.StepFinishRequest;
import org.eclipse.ptp.debug.internal.core.pdi.request.StepIntoRequest;
import org.eclipse.ptp.debug.internal.core.pdi.request.StepOverRequest;
import org.eclipse.ptp.debug.internal.core.pdi.request.StopDebuggerRequest;
import org.eclipse.ptp.debug.internal.core.pdi.request.SuspendRequest;
import org.eclipse.ptp.debug.internal.core.pdi.request.TerminateRequest;

/**
 * @author clement
 *
 */
public class Session implements IPDISession, IPDISessionObject {
	private BreakpointManager breakpointManager;
	private EventManager eventManager;
	private EventRequestManager eventRequestManager;
	private TaskManager taskManager;
	private TargetManager targetManager; 
	private ThreadManager threadManager; 
	private ExpressionManager expressionManager;
	private VariableManager variableManager;
	private SourceManager sourceManager;
	private MemoryManager memoryManager;
	private SignalManager signalManager;
	private RegisterManager registerManager;
	private IPDIDebugger debugger = null;
	private String job_id;
	private int total_tasks = 0;
	private int status;
	private long timeout = 30000;
	private NotifyJob notifyJob = null;
	private IRequestFactory factory = new RequestFactory();
	private final ReentrantLock	waitLock = new ReentrantLock();
	private ILaunchConfiguration config = null;
	
	public Session(ILaunchConfiguration config, long timeout, IPDIDebugger debugger, String job_id, int total_tasks) throws PDIException {
		this.config = config;
		this.timeout = timeout;
		this.debugger = debugger;
		this.job_id = job_id;
		this.total_tasks = total_tasks;
		this.notifyJob = new NotifyJob();
		
		setEventRequestManager(new EventRequestManager(this));
		setEventManager(new EventManager(this));
		setTaskManager(new TaskManager(this));
		setTargetManager(new TargetManager(this));
		setThreadManager(new ThreadManager(this));
		setBreakpointManager(new BreakpointManager(this));
		setExpressionManager(new ExpressionManager(this));
		setVariableManager(new VariableManager(this));
		setSourceManager(new SourceManager(this));
		setMemeoryManager(new MemoryManager(this));
		setSignalManager(new SignalManager(this));
		setRegisterManager(new RegisterManager(this));
	}
	public ILaunchConfiguration getConfiguration() {
		return config;
	}
	public IRequestFactory getRequestFactory() {
		return factory;
	}
	public long getTimeout() {
		return timeout;
	}
	public void setRequestTimeout(long timeout) {
		this.timeout = timeout;
	}
	public void connectToDebugger(IProgressMonitor monitor, String app, String path, String dir, String[] args) throws PDIException {
		setStatus(CONNECTING);
		if (!getDebugger().isConnected(monitor)) {
			throw new PDIException(getTasks(), "Cannot connect to debugger");
		}
		getDebugger().register(eventManager);
		monitor.beginTask("Initialising debug processes ("+total_tasks+")...", total_tasks);
		StartDebuggerRequest request = new StartDebuggerRequest(getTasks(), app, path, dir, args);
		eventRequestManager.addEventRequest(request);
		request.waitUntilCompleted(null, monitor);
		setStatus(CONNECTED);
		eventManager.fireEvent(new StartedEvent(this, getTasks()));
	}
	protected void setEventRequestManager(EventRequestManager manager) {
		eventRequestManager = manager;
	}
	protected void setEventManager(EventManager manager) {
		eventManager = manager;
	}
	protected void setTaskManager(TaskManager manager) {
		taskManager = manager;
	}
	protected void setTargetManager(TargetManager manager) {
		targetManager = manager;
	}
	protected void setThreadManager(ThreadManager manager) {
		threadManager = manager;
	}
	protected void setBreakpointManager(BreakpointManager manager) {
		breakpointManager = manager;
	}
	protected void setExpressionManager(ExpressionManager manager) {
		expressionManager = manager;
	}
	protected void setVariableManager(VariableManager manager) {
		variableManager = manager;
	}
	protected void setSourceManager(SourceManager manager) {
		sourceManager = manager;
	}
	protected void setMemeoryManager(MemoryManager manager) {
		memoryManager = manager;
	}
	protected void setSignalManager(SignalManager manager) {
		signalManager = manager;
	}
	protected void setRegisterManager(RegisterManager manager) {
		registerManager = manager;
	}
	public EventRequestManager getEventRequestManager() {
		return eventRequestManager;
	}
	public EventManager getEventManager() {
		return eventManager;
	}
	public TaskManager getTaskManager() {
		return taskManager;
	}
	public ThreadManager getThreadManager() {
		return threadManager;
	}
	public TargetManager getTargetManager() {
		return targetManager;
	}
	public BreakpointManager getBreakpointManager() {
		return breakpointManager;
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
	public MemoryManager getMemoryManager() {
		return memoryManager;
	}
	public SignalManager getSignalManager() {
		return signalManager;
	}
	public RegisterManager getRegisterManager() {
		return registerManager;
	}
	public void shutdown(boolean force) {
		try {
			if (!force) {
				exit();
			}
			else
				setStatus(EXITING);
			debugger.disconnection(eventManager);
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
	public void restart(BitList tasks) throws PDIException {
		checkStatus();
		throw new PDIException(tasks, "Not implement restart() yet");
	}
	public void start(BitList tasks) throws PDIException {
		checkStatus();
		getEventRequestManager().addEventRequest(new ResumeRequest(tasks, false));
	}
	public void resume(BitList tasks, boolean passSignal) throws PDIException {
		checkStatus();
		if (passSignal)
			throw new PDIException(tasks, "Not implment resume() - Pass Signal yet");
		taskManager.getSuspendedTasks(tasks);
		if (tasks.isEmpty())
			throw new PDIException(tasks, "No suspended processes found");
		getEventRequestManager().addEventRequest(new ResumeRequest(tasks, passSignal));
	}
	public void resume(BitList tasks, IPDILocation location) throws PDIException {
		checkStatus();
		taskManager.getSuspendedTasks(tasks);
		if (tasks.isEmpty())
			throw new PDIException(tasks, "No suspended processes found");
		throw new PDIException(tasks, "Not implment resume(IPDILocation) yet");
	}
	public void resume(BitList tasks, IPDISignal signal) throws PDIException {
		checkStatus();
		taskManager.getSuspendedTasks(tasks);
		if (tasks.isEmpty())
			throw new PDIException(tasks, "No suspended processes found");
		throw new PDIException(tasks, "Not implment resume(IPDISignal) yet");
	}
	public void stepInto(BitList tasks, int count) throws PDIException {
		checkStatus();
		taskManager.getSuspendedTasks(tasks);
		if (tasks.isEmpty())
			throw new PDIException(tasks, "No suspended processes found");
		getEventRequestManager().addEventRequest(new StepIntoRequest(tasks, count));
	}
	public void stepIntoInstruction(BitList tasks, int count) throws PDIException {
		checkStatus();
		taskManager.getSuspendedTasks(tasks);
		if (tasks.isEmpty())
			throw new PDIException(tasks, "No suspended processes found");
		throw new PDIException(tasks, "Not implment stepIntoInstruction() yet");
	}
	public void stepOver(BitList tasks, int count) throws PDIException {
		checkStatus();
		taskManager.getSuspendedTasks(tasks);
		if (tasks.isEmpty())
			throw new PDIException(tasks, "No suspended processes found");
		getEventRequestManager().addEventRequest(new StepOverRequest(tasks, count));
	}
	public void stepOverInstruction(BitList tasks, int count) throws PDIException {
		checkStatus();
		taskManager.getSuspendedTasks(tasks);
		if (tasks.isEmpty())
			throw new PDIException(tasks, "No suspended processes found");
		throw new PDIException(tasks, "Not implment stepOverInstruction() yet");
	}
	public void stepReturn(BitList tasks, IAIF aif) throws PDIException {
		checkStatus();
		taskManager.getSuspendedTasks(tasks);
		if (tasks.isEmpty())
			throw new PDIException(tasks, "No suspended processes found");
		throw new PDIException(tasks, "Not implment stepReturn(IAIF) yet");
	}
	public void stepReturn(BitList tasks, int count) throws PDIException {
		checkStatus();
		taskManager.getCanStepReturnTasks(tasks);
		if (tasks.isEmpty())
			throw new PDIException(tasks, "No suspended processes found");
		getEventRequestManager().addEventRequest(new StepFinishRequest(tasks, count));
	}
	public void stepUntil(BitList tasks, IPDILocation location) throws PDIException {
		checkStatus();
		taskManager.getSuspendedTasks(tasks);
		if (tasks.isEmpty())
			throw new PDIException(tasks, "No suspended processes found");
		throw new PDIException(tasks, "Not implment stepUntil(IPDILocation) yet");
	}
	public void suspend(BitList tasks) throws PDIException {
		checkStatus();
		taskManager.getRunningTasks(tasks);
		if (tasks.isEmpty())
			throw new PDIException(tasks, "No running processes found");
		getEventRequestManager().addEventRequest(new SuspendRequest(tasks));
	}
	public void terminate(BitList tasks) throws PDIException {
		checkStatus();
		taskManager.getNonTerminatedTasks(tasks);
		if (tasks.isEmpty())
			throw new PDIException(tasks, "All processes have been terminated");

		BitList nonTerTasks = tasks.copy();
		taskManager.getRunningTasks(nonTerTasks);
		if (!nonTerTasks.isEmpty()) {
			getEventRequestManager().addEventRequest(new SuspendRequest(nonTerTasks, false));
		}
		getEventRequestManager().addEventRequest(new TerminateRequest(tasks));
		taskManager.setPendingTasks(true, tasks);
	}
	public void validateStepReturn(BitList tasks) throws PDIException {
		/*
		taskManager.getUnregisteredTasks(tasks);
		if (!tasks.isEmpty()) {
			getEventRequestManager().addEventRequest(new GetStackInfoDepthRequest(tasks));
			//FIXME: for testing
			//getEventRequestManager().addEventRequest(new TerminateRequest(tasks.copy()));
		}
		*/
	}
	public void exit() throws PDIException {
		if (status != EXITING && status != EXITED) {
			setStatus(EXITING);
			eventRequestManager.cleanEventRequests();
			BitList tasks = getTasks();
			taskManager.getRunningTasks(tasks);
			if (!tasks.isEmpty()) {
				getEventRequestManager().addEventRequest(new SuspendRequest(tasks, false));
			}
			tasks = getTasks();
			taskManager.getNonTerminatedTasks(tasks);
			if (!tasks.isEmpty())
				getEventRequestManager().addEventRequest(new TerminateRequest(tasks));
			
			getEventRequestManager().addEventRequest(new StopDebuggerRequest(new BitList(total_tasks)));
			taskManager.setPendingTasks(true, tasks);
		}
	}
	/*******************************************
	 * IPDISession
	 *******************************************/
	public IPDIDebugger getDebugger() {
		return debugger;
	}
	public String getJobID() {
		return job_id;
	}
	public boolean isTerminated(BitList tasks) {
		return taskManager.isAllTerminated(tasks);
	}
	public boolean isSuspended(BitList tasks) {
		return taskManager.isAllSuspended(tasks);
	}
	public boolean isRunning(BitList tasks) {
		return taskManager.isAllRunning(tasks);
	}
	public BitList getTasks() {
		BitList tasks = new BitList(total_tasks);
		tasks.set(0, total_tasks);
		return tasks;
	}	
	public int getTotalTasks() {
		return total_tasks;
	}
	public IPDISession getSession() {
		return this;
	}
	public void setStatus(int status) {
		waitLock.lock();
		try {
			this.status = status;
		}
		finally {
			waitLock.unlock();
		}
	}
	public int getStatus() {
		return status;
	}
	protected void checkStatus() throws PDIException {
		if (status == EXITING || status == EXITED)
			throw new PDIException(null, "Cannot process for your request due to session is exiting or exited.");
	}
	public IPDITarget findTarget(BitList qTasks) throws PDIException {
		Target target = targetManager.getTarget(qTasks);
		if (target == null)
			throw new PDIException(qTasks, "No target found.");
		return target;
	}
	public boolean isTarget(BitList qTasks) {
		return (targetManager.getTarget(qTasks) != null);
	}
	/**********************************************
	 * process on running or suspended 
	 **********************************************/
	public synchronized void processRunningEvent(BitList tasks) {
		Target[] targets = targetManager.getTargets();
		for (final Target target : targets) {
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
	public synchronized void processSupsendedEvent(BitList tasks, final int thread_id, final String[] vars) {
		Target[] targets = targetManager.getTargets();
		for (final Target target : targets) {
			if (target.getTasks().intersects(tasks)) {
				Runnable runnable = new Runnable() {
					public void run() {
						target.setSupended(true);
						target.updateState(thread_id);
						try {
							Thread pthread = (Thread)target.getCurrentThread();
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
}
