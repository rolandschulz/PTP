/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.control.core.exceptions;

/**
 * Exception raised when a widget validator using a regular expression fails
 * 
 * @author arossi
 * 
 */
public class UnsatisfiedMatchException extends Exception {

	private static final long serialVersionUID = 4521238998263940220L;

	public UnsatisfiedMatchException() {
	}

	public UnsatisfiedMatchException(String message) {
		super(message);
	}

	public UnsatisfiedMatchException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnsatisfiedMatchException(Throwable cause) {
		super(cause);
	}
}
