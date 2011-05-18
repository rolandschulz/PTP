/*******************************************************************************
 * Copyright (c) 2008,2009 School of Computer Science, 
 * National University of Defense Technology, P.R.China
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	Peichang Shi <pcmutates@163.com>/<pcshi@nudt.edu.cn>
 *******************************************************************************/
package org.eclipse.ptp.rm.slurm.ui.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.rm.slurm.ui.messages.messages"; //$NON-NLS-1$

	public static String SLURMResourceManagerConfigurationWizardPage_name;
	public static String SLURMResourceManagerConfigurationWizardPage_title;
	public static String SLURMResourceManagerConfigurationWizardPage_description;

	public static String SLURMRMLaunchConfigurationDynamicTab_nprocs;
	public static String SLURMRMLaunchConfigurationDynamicTab_nnodes;
	public static String SLURMRMLaunchConfigurationDynamicTab_tlimit;
	public static String SLURMRMLaunchConfigurationDynamicTab_partition;
	public static String SLURMRMLaunchConfigurationDynamicTab_reqlist;
	public static String SLURMRMLaunchConfigurationDynamicTab_exclist;
	public static String SLURMRMLaunchConfigurationDynamicTab_nprocs_tip;
	public static String SLURMRMLaunchConfigurationDynamicTab_nnodes_tip;
	public static String SLURMRMLaunchConfigurationDynamicTab_tlimit_tip;
	public static String SLURMRMLaunchConfigurationDynamicTab_partition_tip;
	public static String SLURMRMLaunchConfigurationDynamicTab_reqlist_tip;
	public static String SLURMRMLaunchConfigurationDynamicTab_exclist_tip;

	public static String SLURMConfigurationWizardPage_name;
	public static String SLURMConfigurationWizardPage_title;
	public static String SLURMConfigurationWizardPage_description;
	public static String SLURMConfigurationWizardPage_defaultButton;
	public static String SLURMConfigurationWizardPage_browseButton;
	public static String SLURMConfigurationWizardPage_path;
	public static String SLURMConfigurationWizardPage_arguments;
	public static String SLURMConfigurationWizardPage_invalid;
	public static String SLURMConfigurationWizardPage_select;
	public static String SLURMConfigurationWizardPage_connection_error;
	public static String SLURMConfigurationWizardPage_connection_error_msg;

	public static String SLURMConfigurationWizardPage_numProcsInvalid;
	public static String SLURMConfigurationWizardPage_numNodesInvalid;
	public static String SLURMConfigurationWizardPage_timeLimitInvalid;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		// Prevent instances.
	}
}
