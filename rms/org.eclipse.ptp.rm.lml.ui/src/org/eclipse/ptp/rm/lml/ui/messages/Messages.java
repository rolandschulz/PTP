/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Claudia Knobloch
 */
package org.eclipse.ptp.rm.lml.ui.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.rm.lml.ui.messages.messages"; //$NON-NLS-1$
	public static String AddLguiAction;
	public static String HideColumn;
	public static String OpenView;
	public static String RemoveLguiAction_0;
	public static String RemoveLguiAction_1;
	public static String UIUtils_1;
	public static String UIUtils_2;
	public static String UIUtils_3;
	public static String UIUtils_4;
	public static String UpdateLguiAction_0;
	public static String JAXBRMConfigurationSelectionWizardPage_0;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
