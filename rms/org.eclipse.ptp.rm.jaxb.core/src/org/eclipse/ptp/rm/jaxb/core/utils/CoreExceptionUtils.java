/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.core.utils;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.rm.jaxb.core.JAXBCorePlugin;

public class CoreExceptionUtils {

	private CoreExceptionUtils() {
	}

	public static IStatus getErrorStatus(String message, Throwable t) {
		if (t != null) {
			t.printStackTrace();
		}
		return new Status(Status.ERROR, JAXBCorePlugin.getUniqueIdentifier(), Status.ERROR, message, t);
	}

	public static CoreException newException(String message, Throwable t) {
		return new CoreException(getErrorStatus(message, t));
	}
}
