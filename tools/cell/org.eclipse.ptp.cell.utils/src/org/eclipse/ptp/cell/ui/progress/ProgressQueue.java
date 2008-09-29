/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.cell.ui.progress;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.cell.utils.debug.Debug;


/**
 * Allows easily managing and updating a {@link IProgressMonitor} by specifying
 * a queue of operations.
 * <p>
 * Solves several issues of common use of {@link IProgressMonitor}:
 * <ul>
 * <li>The {@link IProgressMonitor} needs to be initialized with the total
 * number of steps. Extra work is required to manually synchronized the total
 * number with the amount of steps for each operation. The {@link ProgressQueue}
 * automatically calculates the total amount.
 * <li>With {@link IProgressMonitor} The number of steps in each operation and
 * the description of the operation is specified when the operations are
 * started. The {@link ProgressQueue} allows specifying all operations and the
 * number of steps on each on a centralized place, where this information can be
 * easily managed.
 * <li>The {@link IProgressMonitor} needs to be notified whenever a new
 * operation is started. But semantics of the problem may notify on the end
 * operation only (and the {@link IProgressMonitor} needs to show the next
 * operation). {@link ProgressQueue} makes this issue transparent.
 * <li>{@link ProgressQueue} may be used as a listener registered to one or
 * more control classes. This isolates the control classes from the complexity
 * of handling the {@link IProgressMonitor}. Even better, this allows writing
 * control classes that do not depend on Eclipse.
 * </ul>
 * <p>
 * A {@link ProgressQueue} may stop its operation on several ways:
 * <ul>
 * <li>Finish gracefully because all operations were notified to be complete.
 * <li>Forced to finish by calling {@link #finish()}
 * <li>Become interrupted by calling {@link #interrupt()}
 * <li>Become canceled by calling {@link #cancel()}
 * <li>Become canceled by the user clicking on the "cancel" button.
 * </ul>
 * <p>
 * Once the {@link ProgressQueue} was notified that operations have started,
 * attributes cannot be changed anymore. On starting, the
 * {@link IProgressMonitor} is configured properly. On terminating, the
 * {@link IProgressMonitor} is set to a proper state (100% complete, cancelled
 * or interrupted).
 * 
 * @author Daniel Felix Ferber
 */
public class ProgressQueue {
	private ArrayList operations = new ArrayList();

	private IProgressMonitor monitor = null;

	private ICancelCallback cancelCallBack = null;

	int currentOperationIndex = 0;

	/**
	 * Indicates that the user has pressed the cancel button on the progress bar
	 * or that the {@link #cancel()} method was called.
	 */
	private boolean cancelled = false;

	/**
	 * Indicates that the queue was terminated by calling {@link #interrupt()},
	 * without first being canceled or having finished.
	 */
	private boolean interrupted = false;

	/**
	 * Indicates that the queue has already started the progress listening and
	 * cannot be reconfigured.
	 */
	private boolean started = false;

	/**
	 * Indicates that the last progress point was achieved.
	 */
	private boolean finished = false;

	/**
	 * The name that is shown on the top of the progress monitor.
	 */
	private String taskName = null;
	private String interruptMessage = Messages.ProgressQueue_Interrupted;
	private String cancelMessage = Messages.ProgressQueue_Canceling;

	/**
	 * Thread that polls the progress monitor the check if the cancel button was
	 * pressed.
	 */
	ProgressMonitorPoll progressMonitorPoll;

	class ProgressMonitorPoll extends Thread {
		public ProgressMonitorPoll() {
			super(Messages.ProgressQueue_CancelStatusPolling);
		}

		public void run() {
			Debug.POLICY.enter(Debug.DEBUG_PROGRESS);
			while (!this.isInterrupted()) {
				try {
					synchronized (this) {
						wait(500);
					}
				} catch (InterruptedException e) {
					break;
				}
				pollMonitor();
			}
			Debug.POLICY.exit(Debug.DEBUG_PROGRESS);
		}
	}

	/** Default constructor */
	public ProgressQueue() {
		this.monitor = null;
	}

	/** Create a {@link ProgressQueue} associated to a {@link IProgressMonitor}. */
	public ProgressQueue(IProgressMonitor monitor) {
			this.monitor = monitor;
	}

	/**
	 * Create a {@link ProgressQueue} associated to a {@link IProgressMonitor}
	 * and a task name.
	 */
	public ProgressQueue(IProgressMonitor monitor, String taskName) {
		this.taskName = taskName;
		if (monitor != null) {
			this.monitor = monitor;
		}
	}

	/**
	 * Add an operation to the {@link ProgressQueue}.
	 * 
	 * TODO: rename to addOperation
	 */
	public void addWait(int id, String description, int steps) {
		Assert.isTrue(!this.started,
				"Cannot add new steps after ProgressQueue was started"); //$NON-NLS-1$
		Assert.isTrue(! started && ! interrupted && ! cancelled && ! finished);
		operations.add(new ProgressInfo(id, description, steps));
	}

	/** Set the task name that is shown in {@link IProgressMonitor}. */
	public synchronized void setTaskName(String taskName) {
		Assert.isTrue(!this.started,
				"Cannot set task name after ProgressQueue was started"); //$NON-NLS-1$
		Assert.isTrue(! started && ! interrupted && ! cancelled && ! finished);
		this.taskName = taskName;
	}

	/** Set the progress monitor that is controlled by this object. */
	public synchronized void setMonitor(IProgressMonitor monitor) {
		Assert.isTrue(!this.started,
				"Cannot set monitor after ProgressQueue was started"); //$NON-NLS-1$
		Assert.isTrue(! started && ! interrupted && ! cancelled && ! finished);
		this.monitor = monitor;
	}

	/** Return the progress monitor controlled by this object. */
	public synchronized IProgressMonitor getMonitor() {
		return monitor;
	}

	/**
	 * The the cancel call back (or event listener) that is called when the
	 * "cancel" button is pressed or when the {@link #cancel()} method is
	 * called.
	 * 
	 * @param cancelCallBack
	 */
	public synchronized void setCancelCallBack(ICancelCallback cancelCallBack) {
		Assert.isTrue(!this.started,
				"Cannot set cancel callback after ProgressQueue was started"); //$NON-NLS-1$
		Assert.isTrue(! started && ! interrupted && ! cancelled && ! finished);
		this.cancelCallBack = cancelCallBack;
	}
	
	public synchronized String getCancelMessage() {
		return cancelMessage;
	}

	public synchronized void setCancelMessage(String cancelMessage) {
		Assert.isTrue(! started && ! interrupted && ! cancelled && ! finished);
		this.cancelMessage = cancelMessage;
	}

	public synchronized String getInterruptMessage() {
		return interruptMessage;
	}

	public synchronized void setInterruptMessage(String interruptMessage) {
		Assert.isTrue(! started && ! interrupted && ! cancelled && ! finished);
		this.interruptMessage = interruptMessage;
	}

	public synchronized boolean isCancelled() {
		return cancelled;
	}

	public synchronized boolean isInterrupted() {
		return interrupted;
	}

	public synchronized boolean isFinished() {
		return finished;
	}

	public synchronized boolean isStarted() {
		return started;
	}

	/**
	 * Start the progress monitor. Should be called after the progress infos
	 * have been added to the progress queue. Configures the progress monitor
	 * and adds the progress monitor polling.
	 */
	public synchronized void start() {
		Debug.POLICY.enter(Debug.DEBUG_PROGRESS);
		/*
		 * Pre conditions
		 */
		Assert.isTrue(! started && ! interrupted && ! cancelled && ! finished);
		Assert.isTrue(operations.size() > 0,
				"Must have at least one event in queue"); //$NON-NLS-1$
		Assert.isTrue(!started, "Must not already been started"); //$NON-NLS-1$

		/*
		 * Set up progress monitor and show first message.
		 */
		started = true;
		
		if (monitor != null) {
			if (Debug.DEBUG_PROGRESS) {
				Debug.POLICY.trace("Attaching to progress monitor. Total of {0} steps and {1} progress entries.", countAllSteps(), operations.size()); //$NON-NLS-1$
			}
			monitor.beginTask(taskName, countAllSteps());
//			ProgressInfo info = (ProgressInfo) operations.get(0);
//			if (info.description != null) {
//				monitor.subTask(info.description);
//			}
			showOperationMessage(0);
			progressMonitorPoll = new ProgressMonitorPoll();
			progressMonitorPoll.start();
		}
	}

	/**
	 * Waits until all enqueued events have passed, the user cancels the until
	 * the progress, or some other threads cancels the operation, whatever
	 * happens first.
	 * <p>
	 * This is useful to be called when the thread needs to wait until all operations have finished..
	 */
	public synchronized void waitProgress() {
		Debug.POLICY.enter(Debug.DEBUG_PROGRESS);
		Assert.isTrue(started && ! interrupted && ! cancelled && ! finished);
		waitProgressIndexed(Integer.MAX_VALUE);
	}

	/**
	 * Waits until all enqueued events have passed, the user cancels the until
	 * the progress, some other threads cancels the operation, or until the
	 * specified operation is reached, whatever happens first.
	 * <p>
	 * This is useful to be called when the thread needs to wait until some operation has finished.
	 * 
	 * 
	 * @param event
	 *            The event when to stop.
	 */
	public synchronized void waitProgress(int event) {
		Debug.POLICY.enter(Debug.DEBUG_PROGRESS, event);
		Assert.isTrue(started && ! interrupted && ! cancelled && ! finished);
		int eventIndex = seachIndex(event);
		waitProgressIndexed(eventIndex);
	}

	private synchronized void waitProgressIndexed(int referenceIndex) {
		/*
		 * Wait until the operation is canceled, interrupted, finished or the
		 * referenceIndex is achieved. Poll the progress monitor to check if
		 * user has canceled operation.
		 */
		while ((!cancelled) && (!interrupted) && (!finished)
				&& (currentOperationIndex < referenceIndex)) {
			try {
				Debug.POLICY.trace(Debug.DEBUG_PROGRESS, "Waiting for index {0}, currently at {1}.", referenceIndex, currentOperationIndex); //$NON-NLS-1$
				this.wait();
			} catch (InterruptedException e) {
				break;
			}
		}
	}

	/**
	 * Stops waiting for events and flags that the user canceled the operation.
	 */
	public void cancel() {
		Debug.POLICY.enter(Debug.DEBUG_PROGRESS);
		synchronized (this) {
			if (cancelled) return;
			Assert.isTrue(started && ! interrupted && ! cancelled && ! finished);			
		}
		doCancel(false);
	}

	public void interrupt() {
		Debug.POLICY.enter(Debug.DEBUG_PROGRESS);
		synchronized (this) {
			if (interrupted) return;
			if (cancelled) return;
			Assert.isTrue(started && ! interrupted && ! cancelled && ! finished);			
		}
		doInterrupt();
	}

	/**
	 * Notify the queue that all operations have finished.
	 * One may use this method to assure that the progress monitor
	 * is now showing progress of 100%.
	 */
	public void finish() {
		Debug.POLICY.enter(Debug.DEBUG_PROGRESS);
		synchronized (this) {
			Assert.isTrue(started && ! interrupted && ! cancelled && ! finished);
		}
		doFinish();
	}

	/**
	 * Notifies that an operation has just started. Automatically assumes that
	 * the previous operation just completed.
	 * <p>
	 * The monitor is updated with information about the steps done until the
	 * recent event and shows the task description for the next event.
	 * 
	 * @param id
	 *            The operation that just started.
	 */
	public synchronized void notifyOperationStarted(int id) {
		Debug.POLICY.enter(Debug.DEBUG_PROGRESS, id);
		
		if (interrupted || cancelled) return;
		Assert.isTrue(started && ! interrupted && ! cancelled && ! finished);

		int operationIndex = seachIndex(id);
		if (operationIndex == -1) {
			Debug.POLICY.error(Debug.DEBUG_PROGRESS, "Unknown id {0}", id); //$NON-NLS-1$
			return;
		}
		
		Assert.isTrue(currentOperationIndex <= operationIndex,
				"Operation out of order"); //$NON-NLS-1$

		/*
		 * Small performance improvement. If the iquality is true, this means
		 * that notifyOperationCompleted() was called for the previous operation
		 * an the ProgressQueue already automatically advanced to the current
		 * operation.
		 */
		if (currentOperationIndex == operationIndex) {
			return;
		}

		/*
		 * Update number of steps done to complete last operation.
		 */
		updateCompletedProgress(operationIndex - 1);

		/*
		 * Test if the last operation issues to interrupt or to finish. If
		 * finish is set for the last operation, the next (current) operation is
		 * not applied.
		 */
		checkFlags(operationIndex - 1);
		if (finished) {
			return;
		}

		/*
		 * Show the next (current) operation
		 */
		showOperationMessage(operationIndex);
		currentOperationIndex = operationIndex;
		this.notifyAll();
	}

	/**
	 * Notifies the queue that an operation got complete. Automatically assumes that the
	 * next operation (if available) has just started.
	 * <p>
	 * The monitor is updated with information about the steps done until the
	 * recent event and shows the task description for the next event.
	 * 
	 * @param id
	 *            The operation that just completed.
	 */
	public synchronized void notifyOperationCompleted(int id) {
		Debug.POLICY.enter(Debug.DEBUG_PROGRESS, id);
		if (interrupted || cancelled) return;
		Assert.isTrue(started && ! interrupted && ! cancelled && ! finished);

		int operationIndex = seachIndex(id);
		if (operationIndex == -1) {
			Debug.POLICY.error(Debug.DEBUG_PROGRESS, "Unknown id {0}", id); //$NON-NLS-1$
			return;
		}

		Assert.isTrue(currentOperationIndex <= operationIndex,
				"Operation out of order"); //$NON-NLS-1$

		/*
		 * Update number of steps done to complete last operation.
		 */
		updateCompletedProgress(operationIndex);

		/*
		 * Test if the last operation issues to interrupt or to finish. If
		 * finish is set for the last operation, the next (current) operation is
		 * not applied.
		 */
		checkFlags(operationIndex);
		if (finished) {
			return;
		}

		/*
		 * Show the next (current) operation
		 */
		currentOperationIndex = operationIndex + 1;
		showOperationMessage(currentOperationIndex);
		this.notifyAll();
	}

	private void showOperationMessage(int operationIndex) {
		// No synchronize needed. Reads/accesses immutable data.
		ProgressInfo newInfo = (ProgressInfo) operations.get(operationIndex);
		if (monitor != null) {
			Debug.POLICY.trace(Debug.DEBUG_PROGRESS, "Show new message for {0}: {1}.", operationIndex, newInfo.description); //$NON-NLS-1$
			if (newInfo.description != null) {
				monitor.subTask(newInfo.description);
			} else {
				/*
				 * TODO: Decide approach. Could be reverse seach to find the
				 * last not null message.
				 */
			}
		}
	}

	private void checkFlags(int operationIndex) {
		// No synchronize needed. Reads immutable data.
		/* Flags are not implemented anymore. They were not useful. */
		if (operationIndex >= (operations.size() - 1)) {
			doFinish();
		}
	}

	private void updateCompletedProgress(int lastOperationIndex) {
		// No synchronize needed. Reads/accesses immutable data.
		int steps = countStepsInterval(currentOperationIndex,
				lastOperationIndex);
		if (monitor != null) {
			Debug.POLICY.error(Debug.DEBUG_PROGRESS, "Complete progress from {0} to {1}: {2} steps.", lastOperationIndex, currentOperationIndex, steps); //$NON-NLS-1$
			if (steps > 0) {
				monitor.worked(steps);
			}
		}
	}

	private int countStepsInterval(int fromIndex, int toIndex) {
		// No synchronize needed. Reads immutable data.
		int steps = 0;
		for (int index = fromIndex; index <= toIndex; index++) {
			ProgressInfo info = (ProgressInfo) operations.get(index);
			steps += info.steps;
		}
		return steps;
	}

	private int seachIndex(int id) {
		// No synchronize needed. Reads immutable data.
		Iterator iterator = operations.iterator();
		int index = 0;
		while (iterator.hasNext()) {
			ProgressInfo info = (ProgressInfo) iterator.next();
			if (info.id == id) {
				return index;
			}
			index++;
		}

		return -1;
	}

	private int countAllSteps() {
		// No synchronize needed. Reads immutable data.
		return countStepsInterval(0, operations.size() - 1);
	}

	private void doCancel(boolean byUser) {
		Debug.POLICY.trace(Debug.DEBUG_PROGRESS, "ProgressQueue set to cancelled."); //$NON-NLS-1$
		// callback is forgotten after cleanUp(). Keep a temporary copy.
		ICancelCallback callbackCopy = null;
		synchronized (this) {
			monitor.subTask(Messages.ProgressQueue_Canceling);
			cancelled = true;
			callbackCopy = this.cancelCallBack;
			cleanUp();
		}
		if (callbackCopy != null) {
			// Never call a callback inside a sinchronized block. This may cause deadlocks.
			callbackCopy.cancel(byUser);
		}
	}

	private synchronized void doInterrupt() {
		Debug.POLICY.trace(Debug.DEBUG_PROGRESS, "ProgressQueue set to interrupted."); //$NON-NLS-1$
		interrupted = true;			
		cleanUp();
	}

	private synchronized void doFinish() {
		Debug.POLICY.trace(Debug.DEBUG_PROGRESS, "ProgressQueue set to finished."); //$NON-NLS-1$
		finished = true;
		/* Make sure that 100% have reached. whatever operation was current.  */
		updateCompletedProgress(operations.size() - 1);
		cleanUp();
	}

	private void cleanUp() {
		// No synchronize needed. Attributes are not written concurrently.
		// Anyway this method is always called within synchronized block.
		if (progressMonitorPoll != null) {
			progressMonitorPoll.interrupt();
			progressMonitorPoll = null;
		}
		cancelCallBack = null;
		monitor = null;
		if (Debug.DEBUG_PROGRESS) {
			Debug.POLICY.trace("Detached from progress monitor."); //$NON-NLS-1$
		}
		/*
		 * Do note close monitor, so that the user can see the last message
		 * The user has to call monitor.done() manually according to his convenience.
		 */
		this.notifyAll();
	}

	private void pollMonitor() {
		// No synchronize needed. Accesses immutable data.
		if (monitor != null) {
			if (monitor.isCanceled()) {
				doCancel(true);
			}
		}
	}


}
