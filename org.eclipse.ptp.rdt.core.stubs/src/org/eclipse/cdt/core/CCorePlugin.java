/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core;

import java.io.PrintStream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;


public class CCorePlugin {
	
	public static final int STATUS_CDTPROJECT_EXISTS = 1;
	public static final int STATUS_CDTPROJECT_MISMATCH = 2;
	public static final int CDT_PROJECT_NATURE_ID_MISMATCH = 3;
	/**
	 * Status code for core exception that is thrown if a pdom grew larger than the supported limit.
	 * @since 5.2
	 */
	public static final int STATUS_PDOM_TOO_LARGE = 4;

	public static final String PLUGIN_ID = "org.eclipse.cdt.core"; //$NON-NLS-1$
	
	
	public static void log(Throwable e) {
		if(e != null)
			e.printStackTrace();
	}
	
	public static void log(IStatus status) {
		PrintStream out = status.getSeverity() == IStatus.ERROR ? System.err : System.out;
		out.println(status.getMessage());
		log(status.getException());
	}
	
	
	public static IStatus createStatus(String msg) {
		return createStatus(msg, null);
	}
	
	public static IStatus createStatus(String msg, Throwable e) {
		return new Status(IStatus.ERROR, PLUGIN_ID, msg, e);
	}
	
	public static String getResourceString(String key) {
		return key;
	}
	
}
