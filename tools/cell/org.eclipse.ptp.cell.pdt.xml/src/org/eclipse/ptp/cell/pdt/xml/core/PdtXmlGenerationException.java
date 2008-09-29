/******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *

*****************************************************************************/
package org.eclipse.ptp.cell.pdt.xml.core;

/**
 * Represents an error when creating the XML structure
 * 
 * @author Richard Maciel
 *
 */
public class PdtXmlGenerationException extends RuntimeException {

	/**
	 * 
	 */
	public PdtXmlGenerationException() {
	}

	/**
	 * @param message
	 */
	public PdtXmlGenerationException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public PdtXmlGenerationException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public PdtXmlGenerationException(String message, Throwable cause) {
		super(message, cause);
	}

}
