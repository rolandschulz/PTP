/**********************************************************************
 * Copyright (c) 2013 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rm.jaxb.control.core.exceptions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.internal.rm.jaxb.control.core.JAXBControlCorePlugin;

/**
 * Exception raised when a stream parser fails
 */
public class StreamParserException extends CoreException {

	private static final long serialVersionUID = 6631830809450920459L;

	public StreamParserException(String message) {
		super(new Status(IStatus.ERROR, JAXBControlCorePlugin.getUniqueIdentifier(), message));
	}

	public StreamParserException(String message, Throwable exception) {
		super(new Status(IStatus.ERROR, JAXBControlCorePlugin.getUniqueIdentifier(), message, exception));
	}

}
