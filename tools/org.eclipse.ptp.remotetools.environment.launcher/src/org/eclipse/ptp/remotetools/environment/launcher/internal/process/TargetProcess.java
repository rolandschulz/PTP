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
package org.eclipse.ptp.remotetools.environment.launcher.internal.process;

import java.io.OutputStream;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.ptp.remotetools.environment.launcher.core.ILaunchProcess;
import org.eclipse.ptp.remotetools.environment.launcher.core.ILaunchProgressListener;


public class TargetProcess extends AbstractProcess implements ILaunchProgressListener {

	final ILaunchProcess executionJob;
	boolean wasFinished = false;
	
	final MonitorOutputStream monitorOutputStream = new MonitorOutputStream();
	final MonitorOutputStream monitorErrorStream = new MonitorOutputStream();
	final ExecutionStreamsProxy streamsProxy = new ExecutionStreamsProxy(monitorErrorStream, monitorOutputStream);
	
	public TargetProcess(ILaunch launch, ILaunchProcess job) {
		super(launch, Messages.TargetProcess_Label);
		this.executionJob = job;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IProcess#getExitValue()
	 */
	public int getExitValue() throws DebugException {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IProcess#getStreamsProxy()
	 */
	public IStreamsProxy getStreamsProxy() {
		return streamsProxy;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#canTerminate()
	 */
	public boolean canTerminate() {
		return executionJob.getCurrentProgress() != ILaunchProgressListener.FINISHED;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#isTerminated()
	 */
	public boolean isTerminated() {
		return wasFinished || ! canTerminate();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#terminate()
	 */
	public void terminate() throws DebugException {
		executionJob.markAsCanceled();
	}

	public OutputStream getOutputStream() {
		return monitorOutputStream;
	}

	public OutputStream getErrorStream() {
		return monitorErrorStream;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.environment.launcher.core.ILaunchProgressListener#notifyProgress(int)
	 */
	public void notifyProgress(int progress) {
		fireChangeEvent();
		if (progress == ILaunchProgressListener.FINISHED) {
			fireTerminateEvent();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.environment.launcher.core.ILaunchProgressListener#notifyInterrupt()
	 */
	public void notifyInterrupt() {
		wasFinished = true;
		fireTerminateEvent();
	}

}
