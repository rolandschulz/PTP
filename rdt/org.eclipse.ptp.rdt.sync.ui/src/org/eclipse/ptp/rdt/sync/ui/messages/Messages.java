/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.ui.messages;

import org.eclipse.osgi.util.NLS;

@SuppressWarnings("javadoc")
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.rdt.sync.ui.messages.messages"; //$NON-NLS-1$
	public static String NewRemoteSyncProjectWizard_description;
	public static String NewRemoteSyncProjectWizard_title;
	public static String NewRemoteSyncProjectWizardPage_syncProvider;
	public static String WizardProjectConversion_windowLabel;
	public static String ConvertToRemoteWizardPage_0;
	public static String ConvertToSyncProjectWizardPage_0;
	public static String ConvertToSyncProjectWizardPage_convertingToSyncProject;
	public static String RemoteSyncWizardPage_0;
	public static String RemoteSyncWizardPage_description;
	public static String SynchronizeParticipantDescriptor_invalidClass;
	public static String BRPPage_RemoteProviderLabel;
	public static String BRPPage_ConnectionLabel;
	public static String BRPPage_RootLocation;
	public static String BRPPage_BrowseButton;
	public static String BRPPage_ConnectionButton;
	public static String GitParticipant_0;
	public static String GitParticipant_1;
	public static String GitParticipant_2;
	public static String GitParticipant_3;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
