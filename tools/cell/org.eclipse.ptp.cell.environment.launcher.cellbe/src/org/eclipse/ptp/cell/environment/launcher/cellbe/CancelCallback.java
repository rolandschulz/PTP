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

import org.eclipse.ptp.cell.ui.progress.ICancelCallback;
import org.eclipse.ptp.remotetools.environment.launcher.core.ILaunchProcess;


/**
 * Cancels the ongoing launch of a Cell application.
 * Used by {@link ProgressListener}.
 * When the user hits the cancel button, then the Cell application
 * is marked as canceled by the user, and the execution manager 
 * that controls the remote operations is canceled, too.
 * @author Daniel Felix Ferber
 *
 */
public class CancelCallback implements ICancelCallback {
	ILaunchProcess launchProcess;
	
	public CancelCallback(ILaunchProcess launchProcess) {
		super();
		this.launchProcess = launchProcess;
	}

	public void cancel(boolean byUser) {
		launchProcess.markAsCanceled();
	}
};
