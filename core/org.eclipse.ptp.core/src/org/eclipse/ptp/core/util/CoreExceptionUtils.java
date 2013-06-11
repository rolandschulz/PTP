/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.core.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.internal.core.PTPCorePlugin;

/**
 * Convenience methods for handling CoreExceptions and Status.ERROR.
 * 
 * @author arossi
 * @since 5.0
 */
public class CoreExceptionUtils {

	/*
	 * For static use only.
	 */
	private CoreExceptionUtils() {
	}

	/**
	 * Obtain an IStatus instance with the given message and throwable.
	 * 
	 * @param message
	 * @param t
	 * @return error status object
	 */
	public static IStatus getErrorStatus(String message, Throwable t) {
		if (t != null) {
			PTPCorePlugin.log(t);
		}
		return new Status(Status.ERROR, PTPCorePlugin.getUniqueIdentifier(), Status.ERROR, message, t);
	}

	/**
	 * Obtain a CoreException instance with the given message and throwable.
	 * 
	 * @param message
	 * @param t
	 * @return exception
	 */
	public static CoreException newException(String message, Throwable t) {
		return new CoreException(getErrorStatus(message, t));
	}

	/**
	 * Obtain a CoreException instance with the given message.
	 * 
	 * @param message
	 * @return exception
	 * @since 7.0
	 */
	public static CoreException newException(String message) {
		return new CoreException(getErrorStatus(message, null));
	}
}
