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

package org.eclipse.ptp.cell.utils.vt100;

/**
 * Receives notifications about elements as they are decoded from a VT100
 * stream. The notifications are in the same sequence as the occurr in the VT100
 * stream.
 * <p>
 * Currently, only text sequences are supported. Escape codes related to
 * formatting or other terminal characteristics were not yet implemented.
 * 
 * @author Daniel Felix Ferber
 */
public interface VT100Listener {
	/**
	 * Notifies that the vt100 decoder has just received a sequence of
	 * characters.
	 * 
	 * @param bytes
	 *            The character sequence.
	 * @param length
	 *            The length of the character sequence.
	 */
	public void textSequence(byte[] bytes, int length);
}
