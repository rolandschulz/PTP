/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *****************************************************************************/
package org.eclipse.ptp.cell.examples.ui.internal;

import org.eclipse.osgi.util.NLS;

/**
 * @author laggarcia
 * @since 1.1.1
 * 
 */
public class ExampleMessages extends NLS {

	private static final String BUNDLE_ID = "org.eclipse.ptp.cell.examples.ui.internal.PluginResources"; //$NON-NLS-1$

	public static String creatingExampleProjects;

	public static String configuringProject;

	public static String wizardPageErrorAlreadyExists;

	public static String operationErrorTitle;

	public static String wizardPageWarningNameChanged;

	public static String overwriteQueryTitle;

	public static String overwriteQueryMessage;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_ID, ExampleMessages.class);
	}

	private ExampleMessages() {
		// cannot create new instance
	}

}
