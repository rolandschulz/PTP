/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.debug.core.pdi.model.aif;

/**
 * Represents int value of IAIFValue
 * @author clement
 *
 */
public interface IAIFValueInt extends IValueIntegral {
	/**
	 * Determines whether this value is long
	 * @return true if this value if long
	 */
	boolean isLong();
	
	/**
	 * Determines whether this value is short
	 * @return true if this value is short
	 */
	boolean isShort();
	
	/**
	 * Determines whether this value is int
	 * @return true if this value is int
	 */
	boolean isInt();
	
	/**
	 * Returns byte array of this value
	 * @return bute array of this value 
	 * @throws AIFException on failure
	 */
	byte[] byteValue() throws AIFException;
	
	/**
	 * Returns value of this value
	 * @return value of this value
	 * @throws AIFException on failure
	 */
	long longValue() throws AIFException;
	
	/**
	 * Returns value of this value
	 * @return value of this value
	 * @throws AIFException on failure
	 */
	short shortValue() throws AIFException;

	/**
	 * Returns value of this value
	 * @return value of this value
	 * @throws AIFException on failure
	 */
	int intValue() throws AIFException;
}
