/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.jaxb.control.ui.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.rm.jaxb.control.ui.messages.messages"; //$NON-NLS-1$

	public static String JAXBUIPlugin_Exception_InternalError;
	public static String JAXBConnectionWizardPage_Description;
	public static String JAXBRMControlConfigurationWizardPage_Title;
	public static String JAXBRMConfigurationSelectionWizardPage_1;
	public static String ClearScript;
	public static String BatchScriptPath;
	public static String RemoteScriptPath;
	public static String DefaultDynamicTab_title;
	public static String ViewScript;
	public static String ViewConfig;
	public static String ErrorOnLoadTitle;
	public static String ErrorOnLoadFromStore;
	public static String WidgetSelectedError;
	public static String WidgetSelectedErrorTitle;
	public static String ModifyErrorTitle;
	public static String ModifyError;
	public static String DefaultValues;
	public static String ToggleShowHideSelectedAttributes;
	public static String DialogClose;
	public static String DisplayConfig;
	public static String DisplayScript;
	public static String DisplayError;
	public static String DisplayErrorTitle;
	public static String ViewerLabelProviderColumnError;
	public static String ScriptNotSupportedWarning;
	public static String ScriptNotSupportedWarning_title;
	public static String CreateControlConfigurableError;
	public static String ReadOnlyWarning;
	public static String ReadOnlyWarning_title;
	public static String ValidationError;
	public static String ValidationError_title;
	public static String EnableStdoutFetch;
	public static String EnableStderrFetch;
	public static String RemotePathTooltip;
	public static String VoidLaunchTabMessage;
	public static String VoidLaunchTabTitle;
	public static String CachedDefinitionFile;
	public static String IllegalVariableValueType;
	public static String TabInitialization;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		// Prevent instances.
	}
}
