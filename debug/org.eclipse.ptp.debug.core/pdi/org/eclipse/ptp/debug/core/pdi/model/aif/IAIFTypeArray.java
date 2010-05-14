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
 * Represents array type. Multi-dimensional arrays are represented by nested
 * IAIFTypeArray objects.
 * 
 * For example:
 * 
 * The descriptor "[r0..5is4][r0..9is4]f4" represents a two-dimensional array as
 * a 6-element array containing 10-element arrays of floats.
 * 
 * This array type contains the range information for only the first dimension
 * of the array, so getRanget.getLower() would return 0, getRange().getUpper()
 * would return 5.
 * 
 * The base type of this array is the sub-array, or "[r0..9is4]f4".
 * 
 * @author clement
 * 
 */
public interface IAIFTypeArray extends ITypeDerived {
	/**
	 * Returns base type of this array
	 * 
	 * @return base type of this array
	 */
	public IAIFType getBaseType();

	/**
	 * Returns the array range
	 * 
	 * @return array range
	 */
	public IAIFTypeRange getRange();
}
