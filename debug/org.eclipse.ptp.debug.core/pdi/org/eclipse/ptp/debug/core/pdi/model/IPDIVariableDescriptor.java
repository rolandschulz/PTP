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
package org.eclipse.ptp.debug.core.pdi.model;

import org.eclipse.ptp.debug.core.pdi.IPDISessionObject;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIF;

/**
 * Represents variable description
 * @author clement
 *
 */
public interface IPDIVariableDescriptor extends IPDISessionObject {
	/**
	 * Returns the name of this variable
	 * @return the name of this variable
	 */
	String getName();

	/**
	 * Returns AIF of this variable
	 * @return AIF of this variable
	 * @throws PDIException on failure
	 */
	IAIF getAIF() throws PDIException;
	
	/**
	 * Sets IAIF to this variable
	 * @param aif AIF object
	 */
	void setAIF(IAIF aif);
	
	/**
	 * Returns the type name of this variable descriptor
	 * @return the type name of this variable descriptor
	 * @throws PDIException on failure
	 */
	String getTypeName() throws PDIException;
	
	/**
	 * Returns the size of this variable descriptor
	 * @return the size of this variable descriptor
	 * @throws PDIException on failure
	 */
	int sizeof() throws PDIException;
	
	/**
	 * Return the qualified name of this variable descriptor
	 * @return the qualified name of this variable descriptor
	 * @throws PDIException on failure
	 */
	String getQualifiedName() throws PDIException;
	
	/**
	 * Consider the variable object as an Array of type and rang [start, start + length - 1]
	 * @param start start index
	 * @param length size of array
	 * @return IPDIVariableDescriptor
	 * @throws PDIException on failure
	 */
	IPDIVariableDescriptor getVariableDescriptorAsArray(int start, int length) throws PDIException;
	
	/**
	 *  Consider the variable object as a type
	 * @param type type of variable
	 * @return IPDIVariableDescriptor
	 * @throws PDIException on failure
	 */
	IPDIVariableDescriptor getVariableDescriptorAsType(String type) throws PDIException;
	
	/**
	 * Sets start position of casting array
	 * @param start position to start
	 */
	void setCastingArrayStart(int start);
	
	/**
	 * Returns the start position
	 * @return the start position
	 */
	int getCastingArrayStart();
	
	/**
	 * Sets end position of casting array
	 * @param end position to end
	 */
	void setCastingArrayEnd(int end);
	
	/**
	 * Returns the end position
	 * @return the end position
	 */
	int getCastingArrayEnd();

	/**
	 * Returns an unique id of this variable
	 * @return an unique id of this variable
	 */
	String getVarId();
}
