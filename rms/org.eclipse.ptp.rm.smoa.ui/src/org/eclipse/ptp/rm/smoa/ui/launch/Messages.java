/*******************************************************************************
 * Copyright (c) 2010 Poznan Supercomputing and Networking Center
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jan Konczak (PSNC) - initial implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.smoa.ui.launch;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.rm.smoa.ui.launch.messages"; //$NON-NLS-1$
	public static String SMOARMLaunchConfigurationDynamicTab_AllMachines;
	public static String SMOARMLaunchConfigurationDynamicTab_APP_REGEX;
	public static String SMOARMLaunchConfigurationDynamicTab_CpuCountBounds;
	public static String SMOARMLaunchConfigurationDynamicTab_CustomMakeCommand;
	public static String SMOARMLaunchConfigurationDynamicTab_CustomMakeIsEmptyError;
	public static String SMOARMLaunchConfigurationDynamicTab_Description;
	public static String SMOARMLaunchConfigurationDynamicTab_MaxCpuCount;
	public static String SMOARMLaunchConfigurationDynamicTab_MinCpuCount;
	public static String SMOARMLaunchConfigurationDynamicTab_Name;
	public static String SMOARMLaunchConfigurationDynamicTab_NativeSpec;
	public static String SMOARMLaunchConfigurationDynamicTab_PreferredMachines;
	public static String SMOARMLaunchConfigurationDynamicTab_QueueName;
	public static String SMOARMLaunchConfigurationDynamicTab_RunMake;
	public static String SMOARMLaunchConfigurationDynamicTab_WrapperScript;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
