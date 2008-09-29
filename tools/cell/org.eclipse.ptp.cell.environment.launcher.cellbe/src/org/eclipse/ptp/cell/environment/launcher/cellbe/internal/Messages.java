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
package org.eclipse.ptp.cell.environment.launcher.cellbe.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.cell.environment.launcher.cellbe.internal.messages"; //$NON-NLS-1$

	public static String CellLaunchDelegate_LaunchLabel;

	public static String CellLaunchDelegate_TargetControl_ErrorInexistentTarget;

	public static String CellLaunchDelegate_TargetControl_ErrorInvalidState;

	public static String CellLaunchDelegate_TargetControl_ErrorNoTargetSelected;

	public static String CellLaunchDelegate_TargetControl_ErrorTargetNotReady;

	public static String CellLaunchDelegate_TargetControl_ErrorTargetNotStarted;

	public static String ProgressListener_PREPARE_APPLICATION;

	public static String ProgressListener_PREPARE_WORKING_DIR1;

	public static String ProgressListener_PREPARE_WORKING_DIR2;

	public static String ProgressListener_RUNNING;

	public static String ProgressListener_Title;

	public static String ProgressListener_UPLOAD_APPLICATION;

	public static String ProgressListener_UPLOAD_WORKING_DIR;

	public static String ProgressListener_WAIT;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
