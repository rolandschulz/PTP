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
package org.eclipse.ptp.cell.environment.launcher.cellbe;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.cell.environment.launcher.cellbe.internal.Messages;
import org.eclipse.ptp.cell.ui.progress.ICancelCallback;
import org.eclipse.ptp.cell.ui.progress.ProgressQueue;
import org.eclipse.ptp.remotetools.environment.launcher.core.ILaunchProgressListener;
import org.eclipse.ptp.remotetools.environment.launcher.data.ExecutionConfiguration;


/**
 * Listens the progress of a remote process and updates progress monitor.
 * Also used by the launch delegate to sleep until the launch starts the applicaiton.
 * @author Daniel Felix Ferber
 */
public class ProgressListener extends ProgressQueue implements ILaunchProgressListener {

	public ProgressListener(ExecutionConfiguration configuration, IProgressMonitor monitor, ICancelCallback callback) {
		super(monitor, Messages.ProgressListener_Title);

		addWait(ILaunchProgressListener.WAIT, Messages.ProgressListener_WAIT, 0);
		addWait(ILaunchProgressListener.PREPARE_WORKING_DIR1, Messages.ProgressListener_PREPARE_WORKING_DIR1, 1);
		if (configuration.getDoSynchronizeBefore()) {
			addWait(ILaunchProgressListener.UPLOAD_WORKING_DIR, Messages.ProgressListener_UPLOAD_WORKING_DIR, 10);
			addWait(ILaunchProgressListener.PREPARE_WORKING_DIR2, Messages.ProgressListener_PREPARE_WORKING_DIR2, 1);
		}
		addWait(ILaunchProgressListener.UPLOAD_APPLICATION, Messages.ProgressListener_UPLOAD_APPLICATION, 3);
		addWait(ILaunchProgressListener.PREPARE_APPLICATION, Messages.ProgressListener_PREPARE_APPLICATION, 1);
		addWait(ILaunchProgressListener.RUNNING, Messages.ProgressListener_RUNNING,0);
		
		setCancelCallBack(callback);
	}
	
	public void notifyProgress(int progress) {
		notifyOperationStarted(progress);
	}

	public synchronized void waitForLaunch() {
		waitProgress(ILaunchProgressListener.RUNNING);
	}

	public void notifyInterrupt() {
		interrupt();
	}
}