/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation 
 *     Albert L. Rossi (NCSA) - full implementation (bug 310188)
 *     			      - modifications 04/30/2010
 *                    - modifications 05/11/2010
 *                    - these now correctly reflect only the externally
 *                      exposed (through UI widgets) strings (09/14/2010)
 ******************************************************************************/
package org.eclipse.ptp.rm.pbs.ui.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	public static String DialogClose;

	public static String DynamicTabWizardPage_ATTRIBUTE;
	public static String DynamicTabWizardPage_DESCRIPTION;

	public static String DynamicTabWizardPage_VALUE;

	public static String PBSAttributeTemplateManager_rmState;
	public static String PBSAttributeTemplateManager_rmNotStartedMessage;
	public static String PBSAttributeTemplateManager_requestStartTitle;
	public static String PBSAttributeTemplateManager_requestStartMessage;
	public static String PBSAttributeTemplateManager_requestStartContinue;
	public static String PBSAttributeTemplateManager_requestStartCancel;
	public static String PBSAttributeTemplateManager_requestInitializeTitle;
	public static String PBSAttributeTemplateManager_requestInitializeMessage;

	public static String PBSBatchScriptDisplay;

	public static String PBSBatchScriptTemplateEditError_message;
	public static String PBSBatchScriptTemplateEditError_title;
	public static String PBSBatchScriptTemplateEditPostpend_title;
	public static String PBSBatchScriptTemplateEditPrepend_title;
	public static String PBSBatchScriptTemplateMPICommand;

	public static String PBSConfigurationWizardPage_description;
	public static String PBSConfigurationWizardPage_name;
	public static String PBSConfigurationWizardPage_title;

	public static String PBSResourceManagerConfigurationWizardPage_description;
	public static String PBSResourceManagerConfigurationWizardPage_name;
	public static String PBSResourceManagerConfigurationWizardPage_title;

	public static String PBSRMLaunchConfigDeleteButton_title;
	public static String PBSRMLaunchConfigDeleteError_message;
	public static String PBSRMLaunchConfigDeleteError_title;
	public static String PBSRMLaunchConfigEditButton_title;
	public static String PBSRMLaunchConfigEditChoose_message;
	public static String PBSRMLaunchConfigEditChoose_new;
	public static String PBSRMLaunchConfigEditChoose_new_name;
	public static String PBSRMLaunchConfigEditError_message;
	public static String PBSRMLaunchConfigEditError_title;
	public static String PBSRMLaunchConfigExportButton_title;
	public static String PBSRMLaunchConfigExportButton_message;
	public static String PBSRMLaunchConfigExportError_message;
	public static String PBSRMLaunchConfigExportError_title;
	public static String PBSRMLaunchConfigExportRename;
	public static String PBSRMLaunchConfigExportRename_new;
	public static String PBSRMLaunchConfigExportJobMessage0;
	public static String PBSRMLaunchConfigExportJobMessage1;
	public static String PBSRMLaunchConfigImportButton_title;
	public static String PBSRMLaunchConfigImportButton_message;
	public static String PBSRMLaunchConfigImportError_message;
	public static String PBSRMLaunchConfigImportError_title;
	public static String PBSRMLaunchConfigImportJobMessage;
	public static String PBSRMLaunchConfigGroup1_title;
	public static String PBSRMLaunchConfigGroup2_title;
	public static String PBSRMLaunchConfigPreferences_column_0;
	public static String PBSRMLaunchConfigPreferences_column_1;
	public static String PBSRMLaunchConfigPreferences_column_2;
	public static String PBSRMLaunchConfigPreferences_message;
	public static String PBSRMLaunchConfigTemplate_message;
	public static String PBSRMLaunchConfigTemplate_title;
	public static String PBSRMLaunchConfigViewScript_title;
	public static String PBSRMLaunchConfigEditTemplates_title;

	public static String PBSRMLaunchDataSource_ValueNotSet;

	public static String PBSProxyConfigComboTitle;

	private static final String BUNDLE_NAME = "org.eclipse.ptp.rm.pbs.ui.messages.messages"; //$NON-NLS-1$

	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
