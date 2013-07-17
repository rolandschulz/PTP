/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/

package org.eclipse.ptp.internal.rm.jaxb.control.ui.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.internal.rm.jaxb.control.ui.messages.messages"; //$NON-NLS-1$

	public static String JAXBUIPlugin_Exception_InternalError;
	public static String JAXBConnectionWizardPage_Description;
	public static String JAXBRMControlConfigurationWizardPage_Title;
	public static String StdoutPath;
	public static String StderrPath;
	public static String QueueName;
	public static String ModifyErrorTitle;
	public static String ModifyError;
	public static String ToggleShowHideSelectedAttributes;
	public static String DialogClose;
	public static String ViewerLabelProviderColumnError;
	public static String EnableStdoutOverride;
	public static String EnableStderrOverride;
	public static String EnableQueue;
	public static String RemotePathTooltip;
	public static String QueueNameTooltip;
	public static String IllegalVariableValueType;
	public static String TabInitialization;
	public static String ControlStateListener_0;
	public static String ControlStateRuleUtil_0;
	public static String ControlStateRuleUtil_1;
	public static String ControlStateRuleUtil_2;
	public static String WidgetInstantiationError;
	public static String JAXBRMPreferencesPage_Preferences_options;
	public static String JAXBRMPreferencesPage_ParserDebug_options;
	public static String JAXBRMPreferencesPage_CommandDebug_options;
	public static String UpdateModelFactory_Browse_directory;
	public static String UpdateModelFactory_Browse_file;
	public static String UpdateModelFactory_Undefined_attribute_in_BrowseType;
	public static String UpdateModelFactory_Undefined_attribute_in_WidgetType;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		// Prevent instances.
	}
}
