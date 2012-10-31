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
package org.eclipse.ptp.remotetools.utils.verification;

public class IllegalAttributeException extends Exception {

	private static final long serialVersionUID = -4569682337785839337L;

	String name;
	String value;

	public IllegalAttributeException() {
	}

	public IllegalAttributeException(String arg0) {
		super(arg0);
	}

	public IllegalAttributeException(Throwable arg0) {
		super(arg0);
	}

	public IllegalAttributeException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public IllegalAttributeException(String message, String name) {
		super(name + ": " + message); //$NON-NLS-1$
		this.name = name;
	}

	public IllegalAttributeException(String message, String name, String value) {
		super(name + ": " + message + "( " + value + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		this.value = value;
		this.name = name;
	}

	public IllegalAttributeException(Exception e, String name, String message, String value) {
		super(name + ": " + message + "( " + value + ")", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		this.value = value;
		this.name = name;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

}
