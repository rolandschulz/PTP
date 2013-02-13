/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/

package org.eclipse.ptp.internal.rm.jaxb.ui.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.internal.rm.jaxb.ui.messages.messages"; //$NON-NLS-1$

	public static String JAXBRMConfigurationImportWizard_createResourceManagersProject;

	public static String InvalidConfiguration;
	public static String InvalidConfiguration_title;
	public static String ConfigurationImportWizardTitle;
	public static String ConfigurationImportWizardPageTitle;
	public static String ConfigurationImportWizardPageDescription;
	public static String ConfigurationImportWizardPageTooltip;
	public static String ConfigurationImportWizardPageLabel;
	public static String ResourceManagersNotExist_title;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		// Prevent instances.
	}
}
