/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.proxy.util.compression;

import java.nio.ByteBuffer;

/** Interface for all encoding methods. */

public interface IEncoder {
	/**
	 * Apply this encoder on a ByteBuffer
	 * 
	 * This method transforms the input ByteBuffer based on the specified
	 * encoder.
	 * 
	 * @param in
	 *            The input ByteBuffer to be encoded with limit set to the
	 *            number of input bytes.
	 * @return The encoded ByteBuffer with its limit set to the resulting number
	 *         of bytes.
	 */
	public ByteBuffer apply(ByteBuffer in);
}
