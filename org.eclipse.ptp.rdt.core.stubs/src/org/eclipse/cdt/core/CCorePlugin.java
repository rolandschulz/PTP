/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
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
		return new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, msg, e);
	}
	
	public static String getResourceString(String key) {
		return key;
	}
	
}
