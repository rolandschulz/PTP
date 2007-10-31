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
 * Represents enum type in IAIFType
 * @author clement
 *
 */
public interface IAIFTypeEnum extends ITypeIntegral {
	/**
	 * Returns base type of the current type
	 * @return base type of the current type
	 */
	IAIFType getBaseType();
	
	/**
	 * Returns name of the current type
	 * @return name of the current type
	 */
	String getName();
	
	/**
	 * Returns a list of fields of the current type
	 * @return a list of fields of the current type
	 */
	String[] getFields();
	
	/**
	 * Returns a list of type names of the current type
	 * @return a list of type names of the current type
	 */
	String[] getTypes();
	
	/**
	 * Returns the name of field of the current type by given position
	 * @param index position of this enum type
	 * @return the name of field of the current type
	 */
	String getField(int index);
	
	/**
	 * Returns name of value of this type by given position
	 * @param index position of this type
	 * @return name of value of this type by given position
	 */
	String getValue(int index);
	
	/**
	 * Returns number of children of this type
	 * @return number of children of this type
	 */
	int getNumberOfChildren();
}
