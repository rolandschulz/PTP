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
 * Represents array type. Multi-dimensional arrays are represented
 * by nested IAIFTypeArray objects. 
 * 
 * For example:
 * 
 * The descriptor "[r0..5is4][r0..9is4]f4" represents a two-dimensional 
 * array as a 6-element array containing 10-element arrays of floats.
 * 
 * This array type contains the range information for only the first
 * dimension of the array, so getLow() would return 0, getHigh()
 * would return 5, and getRange() would return 6. 
 * 
 * However some information is provided for the whole array, so getDimension() 
 * would return 2, getAIFTypeArray(0) would return a type with descriptor
 * "[r0..9is4]f4", and getAIFTypeArray(1) would return a type with descriptor "f4".
 * 
 * The base type of this array is the sub-array, or "[r0..9is4]f4".
 * 
 * @author clement
 *
 */
public interface IAIFTypeArray extends ITypeDerived {
	/**
	 * Returns the dimension of this array
	 * 
	 * @return dimension of this array
	 */
	public int getDimension();
	
	/**
	 * Returns an array containing the size of each dimension of the array type
	 * 
	 * TODO: rename to getDimensions()
	 * 
	 * @return an array of dimension sizes
	 */
	public int[] getDimensionDetails();
	
	/**
	 * Determines whether this array type has more than one dimension. i.e. contains
	 * a sub-array.
	 * 
	 * @return true if this array type has more than one dimension
	 * @deprecated use (getDimension() > 1)
	 */
	public boolean isDimensionArray();
	
	/**
	 * Returns sub-array type of the given dimension 
	 * 
	 * @param dim_pos dimension position
	 * @return array type
	 */
	public IAIFTypeArray getAIFTypeArray(int dim_pos);
	
	/**
	 * Returns string format in given range
	 * 
	 * @param range range number
	 * @return string format of the current type
	 */
	public String toString(int range);
	
	/**
	 * Returns the lowest index of this array
	 * 
	 * @return the lowest index of this array
	 * @deprecated use getRange().getLower()
	 */
	public int getLow();
	
	/**
	 * Returns the highest index of this array
	 * 
	 * @return the highest index of this array
	 * @deprecated use getRange().getUpper()
	 */
	public int getHigh();
	
	/**
	 * Returns the number of elements
	 * 
	 * TODO: change to return IAIFTypeRange
	 * 
	 * @return number of elements
	 */
	public int getRange();
	
	/**
	 * Returns base type of this array 
	 * 
	 * TODO: rename to getBaseType()
	 * 
	 * @return base type of this array
	 */
	public IAIFType getFoundationType();
}
