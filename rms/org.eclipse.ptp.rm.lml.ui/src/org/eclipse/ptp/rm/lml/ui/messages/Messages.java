/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Claudia Knobloch,FZ Juelich
 */
package org.eclipse.ptp.rm.lml.ui.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.rm.lml.ui.messages.messages"; //$NON-NLS-1$
	public static String NodedisplayViewMaxlevelAdjust_0;
	public static String NodedisplayViewMaxlevelAdjust_1;
	public static String NodedisplayViewMaxlevelAdjust_2;
	public static String NodesView_0;
	public static String NodesView_1;
	public static String NodesView_2;
	public static String NodesView_3;
	public static String InfoView_Node;
	public static String InfoView_Show_message_of_the_day;
	public static String UIUtils_1;
	public static String UIUtils_2;
	public static String UIUtils_3;
	public static String UIUtils_4;

	public static String UsagebarPainter_NodeText;
	public static String UsagebarPainter_CPUText;

	public static String TableUpdate;

	public static String TableView_Alpha;
	public static String TableView_Filters;
	public static String TableView_Owner;
	public static String TableView_Show_only_my_jobs;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
