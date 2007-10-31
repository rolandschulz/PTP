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
 * Represents a type of aggregate
 * @author clement
 *
 */
public interface ITypeAggregate extends IAIFType {
	/**
	 * Returns name of this type
	 * @return name of this type
	 */
	String getName();
	
	/**
	 * Returns an array of field names of this type
	 * @return array of field names 
	 */
	String[] getFields();
	
	/**
	 * Returns an array of this type or empty of array if nothing
	 * @return array of this type
	 */
	IAIFType[] getTypes();
	
	/**
	 * Returns field name of this type by given position
	 * @param index position of this type
	 * @return field name of this type by given position
	 */
	String getField(int index);
	
	/**
	 * Returns IAIFType of this type by given position
	 * @param index position of type
	 * @return IAIFType of this type by given position
	 */
	IAIFType getType(int index);
	
	/**
	 * Returns number of children of this type
	 * @return number of children of this type
	 */
	int getNumberOfChildren();
}
