/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.internal.rdt.editor.preferences;

import org.eclipse.osgi.util.NLS;

public class PreferenceMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.ptp.internal.rdt.editor.preferences.PreferenceMessages";//$NON-NLS-1$

	private PreferenceMessages() {
		// Do not instantiate
	}
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, PreferenceMessages.class);
	}
	
	public static String Header;
	public static String Footer;
	public static String Left;
	public static String Right;
	public static String Center;
	public static String PageNumber;
	public static String CurrentDate;
	public static String CurrentTime;
	public static String FileName;
	public static String LineNumbers;
	public static String Font;
	
	
}
