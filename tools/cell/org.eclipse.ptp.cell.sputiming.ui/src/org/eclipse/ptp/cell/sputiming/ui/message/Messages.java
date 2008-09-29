/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.cell.sputiming.ui.message;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.cell.sputiming.message.messages"; //$NON-NLS-1$

	public static String SpuTimingDiagramView_CreatePartControl_ShowViewError_CannotOpenFileMessage;

	public static String SpuTimingDiagramView_CreatePartControl_ShowViewError_NoTimingInfoAvailableMessage;

	public static String SpuTimingDiagramView_CreatePartControl_ShowViewError_Title;

	public static String SPUTimingObserver_FailedParseFile;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
