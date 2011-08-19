/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.core.messages;

import org.eclipse.osgi.util.NLS;

@SuppressWarnings("javadoc")
public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.ptp.rdt.sync.core.messages.messages"; //$NON-NLS-1$

	public static String BCM_WorkspaceConfigDes;
	public static String BCM_ScenarioToServiceConfigError;

	public static String BCM_SetWorkspaceConfigDescriptionError;
	public static String BCM_InitError;

	public static String BCM_InitializationError;

	public static String BCM_TemplateError;
	public static String BCM_ProviderError;
	public static String BCM_BuildInfoError;
	public static String BCM_AncestorError;

	public static String BCM_CreateWorkspaceConfigError;

	public static String BCM_CreateWorkspaceConfigFailure;

	public static String BCM_LocalConnectionError;

	public static String BCM_LocalServiceError;
	public static String BCM_ProjectError;

	public static String ResourceChangeListener_0;

	public static String WorkspaceConfigName;

	public static String SyncBuildServiceProvider_configDir;

	public static String SyncBuildServiceProvider_name;
 
	public static String SyncManager_0;
 
	public static String SyncManager_1;
 
	public static String SyncManager_2;
 
	public static String SyncManager_4;

	public static String SyncManager_7;

	public static String SyncManager_8;
	
	static {
		initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
