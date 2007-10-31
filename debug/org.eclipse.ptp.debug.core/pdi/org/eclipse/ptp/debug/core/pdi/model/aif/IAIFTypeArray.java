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
 * Represents array type in IAIFType
 * @author clement
 *
 */
public interface IAIFTypeArray extends ITypeDerived {
	/**
	 * Returns the dimension of this array
	 * @return dimension of this array
	 */
	int getDimension();
	
	/**
	 * Returns an array of dimension size
	 * @return an array of dimension size
	 */
	int[] getDimensionDetails();
	
	/**
	 * Determines whether this array type is dimension or not
	 * @return true if this array type has dimension
	 */
	boolean isDimensionArray();
	
	/**
	 * Returns array type in given dimension position 
	 * @param dim_pos dimension position of current array
	 * @return array type
	 */
	IAIFTypeArray getAIFTypeArray(int dim_pos);
	
	/**
	 * Returns string format in given range
	 * @param range range number
	 * @return string format of the current type
	 */
	String toString(int range);
	
	/**
	 * Returns the lowest index of this array
	 * @return the lowest index of this array
	 */
	int getLow();
	
	/**
	 * Returns the highest index of this array
	 * @return the highest index of this array
	 */
	int getHigh();
	
	/**
	 * Returns the range of this array
	 * @return the range of this array
	 */
	int getRange();
	
	/**
	 * Returns base type of this array 
	 * @return base type of this array
	 */
	IAIFType getFoundationType();
}
