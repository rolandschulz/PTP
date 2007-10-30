/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.remotetools.environment.launcher.core;

/**
 * A listener that is called each time the launch advances one step.
 * Useful for updating the progress bar.
 * @author Daniel Felix Ferber
 */
public interface ILaunchProgressListener {
	// Launch progress
	public static final int UNDEFINED = 0;
	public static final int WAIT = 1;
	public static final int PREPARE_WORKING_DIR1 = 2;
	public static final int UPLOAD_WORKING_DIR = 3;
	public static final int PREPARE_WORKING_DIR2 = 4;
	public static final int UPLOAD_APPLICATION = 5;
	public static final int PREPARE_APPLICATION = 6;
	public static final int RUNNING = 7;
	public static final int FINALIZE_APPLICATION = 8;
	public static final int DOWNLOAD_WORKING_DIR = 9;
	public static final int FINALIZE_WORKING_DIR2 = 10;
	public static final int CLEANUP = 11;
	public static final int FINALIZE_CLEANUP = 12;
	public static final int FINISHED = 13;
	void notifyProgress(int progress);
	void notifyInterrupt();
}
