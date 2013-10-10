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
package org.eclipse.ptp.internal.debug.core.pdi;

import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.IPDIDebugger;
import org.eclipse.ptp.debug.core.pdi.IPDILocation;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.PDIException;
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
import org.eclipse.ptp.internal.debug.core.PDebugOptions;
import org.eclipse.ptp.internal.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.internal.debug.core.pdi.messages.Messages;

/**
 * @author clement
 * @since 5.0
 * 
 */
public class Session implements IPDISession {
	private class NotifyJob extends Job {
		private final Vector<Runnable> fRunnables;

		public NotifyJob() {
			super(Messages.Session_15);
			setSystem(true);
			fRunnables = new Vector<Runnable>(10);
		}

		public void addRunnable(Runnable runnable) {
			synchronized (fRunnables) {
				fRunnables.add(runnable);
			}
			schedule();
		}

		@Override
		public IStatus run(IProgressMonitor monitor) {
			Runnable[] runnables;
			synchronized (fRunnables) {
				runnables = fRunnables.toArray(new Runnable[0]);
				fRunnables.clear();
			}
			MultiStatus failed = null;
			monitor.beginTask(getName(), runnables.length);
			PDebugOptions.trace(Messages.Session_16 + runnables.length);
			for (Runnable runnable : runnables) {
				try {
					runnable.run();
				} catch (Exception e) {
					if (failed == null) {
						failed = new MultiStatus(PTPDebugCorePlugin.getUniqueIdentifier(), PTPDebugCorePlugin.INTERNAL_ERROR,
								Messages.Session_17, null);
					}
					failed.add(new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(),
							PTPDebugCorePlugin.INTERNAL_ERROR, Messages.Session_17, e));
				}
				monitor.worked(1);
			}
			monitor.done();
			if (failed == null) {
				return Status.OK_STATUS;
			}

			return failed;
		}

		@Override
		public boolean shouldRun() {
			return !fRunnables.isEmpty();
		}
	}

	private final IPDIBreakpointManager breakpointManager;
	private final IPDIEventManager eventManager;
	private final IPDIEventRequestManager eventRequestManager;
	private final IPDITaskManager taskManager;
	private final IPDITargetManager targetManager;
	private final IPDIThreadManager threadManager;
	private final IPDIExpressionManager expressionManager;
	private final IPDIVariableManager variableManager;
	private final IPDISourceManager sourceManager;
	private final IPDIMemoryManager memoryManager;
	private final IPDISignalManager signalManager;
	private final IPDIRegisterManager registerManager;
	private final IPDIDebugger debugger;
	private final IPDIRequestFactory requestFactory;
	private final IPDIEventFactory eventFactory;
	private final IPDIModelFactory modelFactory;
	private final ILaunchConfiguration config;
	private final ReentrantLock waitLock = new ReentrantLock();
	private final NotifyJob notifyJob = new NotifyJob();
	private final String job_id;

	private int total_tasks = 0;
	private int status = DISCONNECTED;
	private long timeout = 30000;

	public Session(IPDIManagerFactory managerFactory, IPDIRequestFactory requestFactory, IPDIEventFactory eventFactory,
			IPDIModelFactory modelFactory, ILaunchConfiguration config, long timeout, IPDIDebugger debugger, String job_id,
			int total_tasks) throws PDIException {
		this.config = config;
		this.timeout = timeout;
		this.debugger = debugger;
		this.job_id = job_id;
		this.total_tasks = total_tasks;
		this.requestFactory = requestFactory;
		this.eventFactory = eventFactory;
		this.modelFactory = modelFactory;

		this.eventRequestManager = managerFactory.newEventRequestManager(this);
		this.eventManager = managerFactory.newEventManager(this);
		this.taskManager = managerFactory.newTaskManager(this);
		this.targetManager = managerFactory.newTargetManager(this);
		this.threadManager = managerFactory.newThreadManager(this);
		this.breakpointManager = managerFactory.newBreakpointManager(this);
		this.expressionManager = managerFactory.newExpressionManager(this);
		this.variableManager = managerFactory.newVariableManager(this);
		this.sourceManager = managerFactory.newSourceManager(this);
		this.memoryManager = managerFactory.newMemoryManager(this);
		this.signalManager = managerFactory.newSignalManager(this);
		this.registerManager = managerFactory.newRegisterManager(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDISession#connectToDebugger(org.eclipse
	 * .core.runtime.IProgressMonitor, java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String[])
	 */
	@Override
	public void connectToDebugger(IProgressMonitor monitor, String app, String path, String dir, String[] args) throws PDIException {
		SubMonitor progress = SubMonitor.convert(monitor, total_tasks + 10);
		try {
			setStatus(CONNECTING);
			if (!getDebugger().isConnected(progress.newChild(10))) {
				setStatus(DISCONNECTED);
				throw new PDIException(getTasks(), Messages.Session_0);
			}
			getDebugger().addEventManager(eventManager);
			progress.subTask(NLS.bind(Messages.Session_1, total_tasks));
			IPDIStartDebuggerRequest request = getRequestFactory().getStartDebuggerRequest(getTasks(), app, path, dir, args);
			eventRequestManager.addEventRequest(request);
			request.waitUntilCompleted(null, progress.newChild(total_tasks));
			setStatus(CONNECTED);
			eventManager.fireEvent(getEventFactory().newStartedEvent(this, getTasks()));
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#exit()
	 */
	@Override
	public void exit() throws PDIException {
		if (status != EXITING && status != EXITED) {
			setStatus(EXITING);
			eventRequestManager.flushEventRequests();
			TaskSet tasks = getTasks();
			taskManager.getRunningTasks(tasks);
			if (!tasks.isEmpty()) {
				eventRequestManager.addEventRequest(getRequestFactory().getSuspendRequest(tasks, false));
			}
			tasks = getTasks();
			taskManager.getNonTerminatedTasks(tasks);
			if (!tasks.isEmpty()) {
				eventRequestManager.addEventRequest(getRequestFactory().getTerminateRequest(tasks));
			}
			eventRequestManager.addEventRequest(getRequestFactory().getStopDebuggerRequest(new TaskSet(total_tasks)));
			taskManager.setPendingTasks(true, tasks);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDISession#findTarget(org.eclipse.ptp
	 * .core.util.TaskSet)
	 */
	/**
	 * @since 4.0
	 */
	@Override
	public IPDITarget findTarget(TaskSet qTasks) throws PDIException {
		IPDITarget target = targetManager.getTarget(qTasks);
		if (target == null) {
			throw new PDIException(qTasks, Messages.Session_14);
		}
		return target;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#getBreakpointManager()
	 */
	@Override
	public IPDIBreakpointManager getBreakpointManager() {
		return breakpointManager;
	}

	/**
	 * @return
	 */
	public ILaunchConfiguration getConfiguration() {
		return config;
	}

	/*******************************************
	 * IPDISession
	 *******************************************/
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#getDebugger()
	 */
	@Override
	public IPDIDebugger getDebugger() {
		return debugger;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#getEventFactory()
	 */
	@Override
	public IPDIEventFactory getEventFactory() {
		return eventFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#getEventManager()
	 */
	@Override
	public IPDIEventManager getEventManager() {
		return eventManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#getEventRequestManager()
	 */
	@Override
	public IPDIEventRequestManager getEventRequestManager() {
		return eventRequestManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#getExpressionManager()
	 */
	@Override
	public IPDIExpressionManager getExpressionManager() {
		return expressionManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#getJobID()
	 */
	@Override
	public String getJobID() {
		return job_id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#getMemoryManager()
	 */
	@Override
	public IPDIMemoryManager getMemoryManager() {
		return memoryManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#getModelFactory()
	 */
	@Override
	public IPDIModelFactory getModelFactory() {
		return modelFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#getRegisterManager()
	 */
	@Override
	public IPDIRegisterManager getRegisterManager() {
		return registerManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#getRequestFactory()
	 */
	@Override
	public IPDIRequestFactory getRequestFactory() {
		return requestFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISessionObject#getSession()
	 */
	@Override
	public IPDISession getSession() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#getSignalManager()
	 */
	@Override
	public IPDISignalManager getSignalManager() {
		return signalManager;
	}

	/*
	 * ******************************************
	 * IPDIExecuteManagement******************************************
	 */

	/**
	 * @return
	 */
	@Override
	public IPDISourceManager getSourceManager() {
		return sourceManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#getStatus()
	 */
	@Override
	public int getStatus() {
		return status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#getTargetManager()
	 */
	@Override
	public IPDITargetManager getTargetManager() {
		return targetManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#getTaskManager()
	 */
	@Override
	public IPDITaskManager getTaskManager() {
		return taskManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#getTasks()
	 */
	/**
	 * @since 4.0
	 */
	@Override
	public TaskSet getTasks() {
		TaskSet tasks = new TaskSet(total_tasks);
		tasks.set(0, total_tasks);
		return tasks;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#getThreadManager()
	 */
	@Override
	public IPDIThreadManager getThreadManager() {
		return threadManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#getTimeout()
	 */
	@Override
	public long getTimeout() {
		return timeout;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#getTotalTasks()
	 */
	@Override
	public int getTotalTasks() {
		return total_tasks;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#getVariableManager()
	 */
	@Override
	public IPDIVariableManager getVariableManager() {
		return variableManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDISession#isSuspended(org.eclipse.ptp
	 * .core.util.TaskSet)
	 */
	/**
	 * @since 4.0
	 */
	@Override
	public boolean isSuspended(TaskSet tasks) {
		return taskManager.isAllSuspended(tasks);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDISession#isTerminated(org.eclipse.ptp
	 * .core.util.TaskSet)
	 */
	/**
	 * @since 4.0
	 */
	@Override
	public boolean isTerminated(TaskSet tasks) {
		return taskManager.isAllTerminated(tasks);
	}

	/**********************************************
	 * process on running or suspended
	 **********************************************/
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDISession#processRunningEvent(org.eclipse
	 * .ptp.core.util.TaskSet)
	 */
	/**
	 * @since 4.0
	 */
	@Override
	public synchronized void processRunningEvent(TaskSet tasks) {
		IPDITarget[] targets = targetManager.getTargets();
		for (final IPDITarget target : targets) {
			if (target.getTasks().intersects(tasks)) {
				Runnable runnable = new Runnable() {
					@Override
					public void run() {
						target.setSupended(false);
					}
				};
				queueRunnable(runnable);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDISession#processSupsendedEvent(org.
	 * eclipse.ptp.core.util.TaskSet, int, java.lang.String[])
	 */
	/**
	 * @since 4.0
	 */
	@Override
	public synchronized void processSupsendedEvent(TaskSet tasks, final int thread_id, final String[] vars) {
		IPDITarget[] targets = targetManager.getTargets();
		for (final IPDITarget target : targets) {
			if (target.getTasks().intersects(tasks)) {
				Runnable runnable = new Runnable() {
					@Override
					public void run() {
						target.setSupended(true);
						target.updateState(thread_id);
						try {
							IPDIThread pthread = target.getCurrentThread();
							if (pthread == null) {
								return;
							}

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
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDISession#queueRunnable(java.lang.Runnable
	 * )
	 */
	@Override
	public void queueRunnable(Runnable runnable) {
		notifyJob.addRunnable(runnable);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#restart(org.eclipse
	 * .ptp.core.util.TaskSet)
	 */
	/**
	 * @since 4.0
	 */
	@Override
	public void restart(TaskSet tasks) throws PDIException {
		checkStatus();
		throw new PDIException(tasks, Messages.Session_2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#resume(org.eclipse
	 * .ptp.core.util.TaskSet, boolean)
	 */
	/**
	 * @since 4.0
	 */
	@Override
	public void resume(TaskSet tasks, boolean passSignal) throws PDIException {
		checkStatus();
		if (passSignal) {
			throw new PDIException(tasks, Messages.Session_3);
		}
		taskManager.getSuspendedTasks(tasks);
		if (tasks.isEmpty()) {
			throw new PDIException(tasks, Messages.Session_4);
		}
		eventRequestManager.addEventRequest(getRequestFactory().getResumeRequest(tasks, passSignal));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#resume(org.eclipse
	 * .ptp.core.util.TaskSet, org.eclipse.ptp.debug.core.pdi.IPDILocation)
	 */
	/**
	 * @since 4.0
	 */
	@Override
	public void resume(TaskSet tasks, IPDILocation location) throws PDIException {
		checkStatus();
		taskManager.getSuspendedTasks(tasks);
		if (tasks.isEmpty()) {
			throw new PDIException(tasks, Messages.Session_4);
		}
		throw new PDIException(tasks, Messages.Session_5);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#resume(org.eclipse
	 * .ptp.core.util.TaskSet, org.eclipse.ptp.debug.core.pdi.model.IPDISignal)
	 */
	/**
	 * @since 4.0
	 */
	@Override
	public void resume(TaskSet tasks, IPDISignal signal) throws PDIException {
		checkStatus();
		taskManager.getSuspendedTasks(tasks);
		if (tasks.isEmpty()) {
			throw new PDIException(tasks, Messages.Session_4);
		}
		throw new PDIException(tasks, Messages.Session_6);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#setRequestTimeout(long)
	 */
	@Override
	public void setRequestTimeout(long timeout) {
		this.timeout = timeout;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#setStatus(int)
	 */
	@Override
	public void setStatus(int status) {
		waitLock.lock();
		try {
			this.status = status;
		} finally {
			waitLock.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISession#shutdown(boolean)
	 */
	@Override
	public void shutdown(boolean force) {
		try {
			if (!force) {
				exit();
			} else {
				setStatus(EXITING);
			}
			debugger.removeEventManager(eventManager);
			debugger.stopDebugger();
		} catch (PDIException e) {
			e.printStackTrace();
		} finally {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#start(org.eclipse
	 * .ptp.core.util.TaskSet)
	 */
	/**
	 * @since 4.0
	 */
	@Override
	public void start(TaskSet tasks) throws PDIException {
		checkStatus();
		eventRequestManager.addEventRequest(getRequestFactory().getResumeRequest(tasks, false));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#stepInto(org.eclipse
	 * .ptp.core.util.TaskSet, int)
	 */
	/**
	 * @since 4.0
	 */
	@Override
	public void stepInto(TaskSet tasks, int count) throws PDIException {
		checkStatus();
		taskManager.getSuspendedTasks(tasks);
		if (tasks.isEmpty()) {
			throw new PDIException(tasks, Messages.Session_4);
		}
		eventRequestManager.addEventRequest(getRequestFactory().getStepIntoRequest(tasks, count));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#stepIntoInstruction
	 * (org.eclipse.ptp.core.util.TaskSet, int)
	 */
	/**
	 * @since 4.0
	 */
	@Override
	public void stepIntoInstruction(TaskSet tasks, int count) throws PDIException {
		checkStatus();
		taskManager.getSuspendedTasks(tasks);
		if (tasks.isEmpty()) {
			throw new PDIException(tasks, Messages.Session_4);
		}
		throw new PDIException(tasks, Messages.Session_7);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#stepOver(org.eclipse
	 * .ptp.core.util.TaskSet, int)
	 */
	/**
	 * @since 4.0
	 */
	@Override
	public void stepOver(TaskSet tasks, int count) throws PDIException {
		checkStatus();
		taskManager.getSuspendedTasks(tasks);
		if (tasks.isEmpty()) {
			throw new PDIException(tasks, Messages.Session_4);
		}
		eventRequestManager.addEventRequest(getRequestFactory().getStepOverRequest(tasks, count));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#stepOverInstruction
	 * (org.eclipse.ptp.core.util.TaskSet, int)
	 */
	/**
	 * @since 4.0
	 */
	@Override
	public void stepOverInstruction(TaskSet tasks, int count) throws PDIException {
		checkStatus();
		taskManager.getSuspendedTasks(tasks);
		if (tasks.isEmpty()) {
			throw new PDIException(tasks, Messages.Session_4);
		}
		throw new PDIException(tasks, Messages.Session_8);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#stepReturn(org.eclipse
	 * .ptp.core.util.TaskSet, org.eclipse.ptp.debug.core.pdi.model.aif.IAIF)
	 */
	/**
	 * @since 4.0
	 */
	@Override
	public void stepReturn(TaskSet tasks, IAIF aif) throws PDIException {
		checkStatus();
		taskManager.getSuspendedTasks(tasks);
		if (tasks.isEmpty()) {
			throw new PDIException(tasks, Messages.Session_4);
		}
		throw new PDIException(tasks, Messages.Session_9);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#stepReturn(org.eclipse
	 * .ptp.core.util.TaskSet, int)
	 */
	/**
	 * @since 4.0
	 */
	@Override
	public void stepReturn(TaskSet tasks, int count) throws PDIException {
		checkStatus();
		taskManager.getCanStepReturnTasks(tasks);
		if (tasks.isEmpty()) {
			throw new PDIException(tasks, Messages.Session_4);
		}
		eventRequestManager.addEventRequest(getRequestFactory().getStepFinishRequest(tasks, count));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#stepUntil(org.eclipse
	 * .ptp.core.util.TaskSet, org.eclipse.ptp.debug.core.pdi.IPDILocation)
	 */
	/**
	 * @since 4.0
	 */
	@Override
	public void stepUntil(TaskSet tasks, IPDILocation location) throws PDIException {
		checkStatus();
		taskManager.getSuspendedTasks(tasks);
		if (tasks.isEmpty()) {
			throw new PDIException(tasks, Messages.Session_4);
		}
		throw new PDIException(tasks, Messages.Session_10);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#suspend(org.eclipse
	 * .ptp.core.util.TaskSet)
	 */
	/**
	 * @since 4.0
	 */
	@Override
	public void suspend(TaskSet tasks) throws PDIException {
		checkStatus();
		taskManager.getRunningTasks(tasks);
		if (tasks.isEmpty()) {
			throw new PDIException(tasks, Messages.Session_11);
		}
		eventRequestManager.addEventRequest(getRequestFactory().getSuspendRequest(tasks));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#terminate(org.eclipse
	 * .ptp.core.util.TaskSet)
	 */
	/**
	 * @since 4.0
	 */
	@Override
	public void terminate(TaskSet tasks) throws PDIException {
		checkStatus();
		taskManager.getNonTerminatedTasks(tasks);
		if (tasks.isEmpty()) {
			throw new PDIException(tasks, Messages.Session_12);
		}

		TaskSet nonTerTasks = tasks.copy();
		taskManager.getRunningTasks(nonTerTasks);
		if (!nonTerTasks.isEmpty()) {
			eventRequestManager.addEventRequest(getRequestFactory().getSuspendRequest(nonTerTasks, false));
		}
		eventRequestManager.addEventRequest(getRequestFactory().getTerminateRequest(tasks));
		taskManager.setPendingTasks(true, tasks);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDISession#validateStepReturn(org.eclipse
	 * .ptp.core.util.TaskSet)
	 */
	/**
	 * @since 4.0
	 */
	@Override
	public void validateStepReturn(TaskSet tasks) throws PDIException {
		/*
		 * taskManager.getUnregisteredTasks(tasks); if (!tasks.isEmpty()) {
		 * getEventRequestManager
		 * ().addEventRequest(getRequestFactory().getGetStackInfoDepthRequest
		 * (tasks)); //FIXME: for testing //eventRequestManager.addEventRequest
		 * (getRequestFactory().getTerminateRequest(tasks.copy())); }
		 */
	}

	/**
	 * @throws PDIException
	 */
	protected void checkStatus() throws PDIException {
		if (status == EXITING || status == EXITED) {
			throw new PDIException(null, Messages.Session_13);
		}
	}
}
