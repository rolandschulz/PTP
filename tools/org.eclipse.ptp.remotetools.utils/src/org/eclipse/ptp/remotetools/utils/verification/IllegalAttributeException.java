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
		super(name + ": " + message);
		this.name = name;
	}

	public IllegalAttributeException(String message, String name, String value) {
		super(name + ": " + message + "( " + value + ")");
		this.value = value;
		this.name = name;
	}

	public IllegalAttributeException(Exception e, String name, String message, String value) {
		super(name + ": " + message + "( " + value + ")", e);
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
