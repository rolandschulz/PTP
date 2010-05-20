/*******************************************************************************
 * Copyright (c) 2010 IBM Corp. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corp. - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.openmp.ui.pv.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.pldt.openmp.ui.pv.messages.messages"; //$NON-NLS-1$
	public static String OpenMPProblemsView_action1;
	public static String OpenMPProblemsView_action1executed;
	public static String OpenMPProblemsView_action1tooltip;
	public static String OpenMPProblemsView_Description;
	public static String OpenMPProblemsView_InFolder;
	public static String OpenMPProblemsView_Location;
	public static String OpenMPProblemsView_removeAllMarkers;
	public static String OpenMPProblemsView_removeMarkers;
	public static String OpenMPProblemsView_Resource;
	public static String OpenMPProblemsView_SampleView;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
