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
package org.eclipse.ptp.remotetools.utils.network;

/**
 * @author Richard Maciel
 * @since 3.0
 */
public class MacAddressFormatException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 9098093943130065515L;

	public MacAddressFormatException(String errorMsg) {
		super(errorMsg);
	}
}
