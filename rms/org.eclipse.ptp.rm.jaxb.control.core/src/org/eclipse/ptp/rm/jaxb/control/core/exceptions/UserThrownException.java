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

/**
 * Exception raised by <throw> element
 */
public class UserThrownException extends StreamParserException {

	private static final long serialVersionUID = 6631830809450920459L;

	public UserThrownException(String message) {
		super(message);
	}

	public UserThrownException(String message, Throwable exception) {
		super(message, exception);
	}
}
