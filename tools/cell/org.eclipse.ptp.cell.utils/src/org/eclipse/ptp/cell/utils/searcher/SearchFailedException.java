/******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *****************************************************************************/
package org.eclipse.ptp.cell.utils.searcher;

/**
 * @author laggarcia
 * @since 3.0.0
 */
public class SearchFailedException extends Exception {

	private static final long serialVersionUID = 3037138929596778700L;

	/**
	 * 
	 */
	public SearchFailedException() {
	}

	/**
	 * @param message
	 */
	public SearchFailedException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public SearchFailedException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public SearchFailedException(String message, Throwable cause) {
		super(message, cause);
	}

}
