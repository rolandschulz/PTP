/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.internal.rm.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.rm.core.ResourceManagerPlugin;
import org.eclipse.ptp.rm.ui.RMUiPlugin;

/**
 * Eclipse plug-in logging convenience utility class.
 * 
 * @author rsqrd
 * 
 */
public class ResourceManagerUILog {
	public static IStatus createErrorStatus(String message, Throwable exception) {
		return createStatus(IStatus.ERROR, IStatus.OK, message, exception);
	}

	public static IStatus createInfoStatus(String message, Throwable exception) {
		return createStatus(IStatus.INFO, IStatus.OK, message, exception);
	}

	public static IStatus createStatus(int severity, int code, String message,
			Throwable exception) {
		return new Status(severity, RMUiPlugin.ID, code, message,
				exception);
	}

	public static void log(int severity, int code, String message,
			Throwable exception) {
		log(createStatus(severity, code, message, exception));
	}

	public static void log(IStatus status) {
		ResourceManagerPlugin.getDefault().getLog().log(status);
	}

	public static void logError(String message, Throwable exception) {
		log(createErrorStatus(message, exception));
	}

	public static void logError(Throwable exception) {
		logError("Unexpected exception", exception);
	}

	public static void logInfo(String message) {
		log(createInfoStatus(message, null));
	}
}
