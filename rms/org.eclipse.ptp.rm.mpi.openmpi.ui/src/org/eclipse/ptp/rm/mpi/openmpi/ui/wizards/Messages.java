/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.mpi.openmpi.ui.wizards;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Daniel Felix Ferber
 *
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.rm.mpi.openmpi.ui.wizards.messages"; //$NON-NLS-1$
	public static String OpenMPIConfigurationWizardPage_Description;
	public static String OpenMPIConfigurationWizardPage_Label_Version;
	public static String OpenMPIConfigurationWizardPage_Name;
	public static String OpenMPIConfigurationWizardPage_Title;
	public static String OpenMPIConfigurationWizardPage_Validation_NoVersionSelected;
	public static String OpenMPIConfigurationWizardPage_VersionCombo_Version12;
	public static String OpenMPIConfigurationWizardPage_VersionCombo_Version13;
	public static String OpenMPIRMConfigurationWizardPage_Description;
	public static String OpenMPIRMConfigurationWizardPage_Title;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		// Prevent instances.
	}
}
