/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.rdt.core.activator.Activator;

/**
 * Convenience class for writing to the RDT core log.
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the RDT team.
 * 
 * @author Mike Kucera
 */
public class RDTLog {

	public static void log(IStatus status) {
		Activator.getDefault().getLog().log(status);
	}
	
	public static void logError(Throwable exception, String message) {
		if(exception instanceof CoreException)
			log(((CoreException)exception).getStatus());
		else
			log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.OK, message, exception));
	}
	
	public static void logError(Throwable exception) {
		logError(exception, exception.getMessage());
	}
	
	public static void logError(String message) {
		log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, message));
	}
	
	public static void logInfo(String message) {
		log(new Status(IStatus.INFO, Activator.PLUGIN_ID, message));
	}
	
}
