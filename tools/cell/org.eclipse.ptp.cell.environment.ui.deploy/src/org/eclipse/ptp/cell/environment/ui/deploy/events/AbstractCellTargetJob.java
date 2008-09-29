/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.cell.environment.ui.deploy.events;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionManager;
import org.eclipse.ptp.remotetools.environment.control.ITargetJob;


public abstract class AbstractCellTargetJob implements ITargetJob{
		
	protected IRemoteExecutionManager executionManager;
	/*
	 * This attribute does not exist anymore since the ITargetControlledJob class was removed.
	 */
	/*
	protected ITargetControlledJob controller;
	*/
	protected Exception exception;
	protected String errorMessage;
	protected boolean hadError = false;
		
	public String getErrorMessage(){ return errorMessage; }
	public boolean didHaveError(){ return hadError; }
	public Exception getException(){ return exception; }
	
	/*
	 * This method does not exist anymore in ITargetJob due to refactor and
	 * simplification of the interface. The run() method was also removed.
	 * Both were merged into only one run(manager) method that merges
	 * the functionalities of both methods.
	 * For compatibility purpose the new run(manager) method calls
	 * the previous abstract run() method.
	 * (changed by Daniel Felix Ferber)
	 */
	/*
	public void setRun(ITargetControlledJob control, IRemoteExecutionManager manager){
		this.controller = control;
		this.executionManager = manager;
	}
	*/
	
	public void run(IRemoteExecutionManager manager) {
		try {
			this.executionManager = manager;
			run();
		} finally {
			synchronized(this) {
				finished = true;
				notifyAll();
			}
		}
	}
		
	abstract public void run();

	protected boolean finished = false;
	public synchronized boolean isFinished() {
		return finished;
	}
	public synchronized void waitFor() throws InterruptedException {
		while (! finished) {
			this.wait();
		}
	}
	public synchronized void waitFor(IProgressMonitor monitor, String interruptMessage) throws InterruptedException {
		while (! finished) {
			if (monitor.isCanceled()) {
				throw new InterruptedException(interruptMessage);
			}
			this.wait(200);
		}
	}
	
	
}