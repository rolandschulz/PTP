/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.remotetools.internal.common;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.remotetools.RemotetoolsPlugin;

public class Debug {
	private static boolean fDebug = false;

	public static void setDebug(boolean debug) {
		fDebug = debug;
	}

	private static void log(IStatus status) {
		RemotetoolsPlugin.getDefault().getLog().log(status);
	}

	private static void log(int status, String msg) {
		log(new Status(IStatus.ERROR, RemotetoolsPlugin.getDefault().getBundle().getSymbolicName(), status, msg, null));
	}

	public static void println(String s) {
		if (fDebug) {
			log(IStatus.INFO, s);
		}
	}

	public static void printErrorln(String s) {
		if (fDebug) {
			log(IStatus.ERROR, s);
		}
	}

	public static void println2(String s) {
		if (fDebug) {
			log(IStatus.INFO, s);
		}
	}
}
