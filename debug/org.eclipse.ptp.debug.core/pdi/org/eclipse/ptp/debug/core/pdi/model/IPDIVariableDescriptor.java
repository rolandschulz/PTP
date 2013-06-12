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
 * 
 * @author clement
 * 
 */
public interface IPDIVariableDescriptor extends IPDISessionObject {
	/**
	 * Determines whether both descriptors are the same
	 * 
	 * @param variable
	 *            IPDIVariableDescriptor for comparing
	 * @return true if given descriptors is same as the current descriptors
	 */
	public boolean equalDescriptors(IPDIVariableDescriptor variable);

	/**
	 * Returns AIF representation of this variable
	 * 
	 * @return AIF representation of this variable
	 * @throws PDIException
	 *             on failure
	 */
	public IAIF getAIF() throws PDIException;

	/**
	 * Returns the end position
	 * 
	 * @return the end position
	 */
	public int getCastingArrayEnd();

	/**
	 * Returns the start position
	 * 
	 * @return the start position
	 */
	public int getCastingArrayStart();

	/**
	 * @return
	 */
	public String[] getCastingTypes();

	/**
	 * Returns the full name of this variable
	 * 
	 * @return the name of this variable
	 */
	public String getFullName();

	/**
	 * Returns the name of this variable
	 * 
	 * @return the name of this variable
	 */
	public String getName();

	/**
	 * Get position
	 * 
	 * @return
	 */
	public int getPosition();

	/**
	 * Return the qualified name of this variable descriptor
	 * 
	 * @return the qualified name of this variable descriptor
	 * @throws PDIException
	 *             on failure
	 */
	public String getQualifiedName() throws PDIException;

	/**
	 * Get stack depth
	 * 
	 * @return
	 */
	public int getStackDepth();

	/**
	 * Get stack frame
	 * 
	 * @return
	 * @throws PDIException
	 */
	public IPDIStackFrame getStackFrame() throws PDIException;

	/**
	 * Get thread
	 * 
	 * @return
	 * @throws PDIException
	 */
	public IPDIThread getThread() throws PDIException;

	/**
	 * Returns the type name of this variable descriptor
	 * 
	 * @return the type name of this variable descriptor
	 * @throws PDIException
	 *             on failure
	 */
	public String getTypeName() throws PDIException;

	/**
	 * Consider the variable object as an Array of type and rang [start, start + length - 1]
	 * 
	 * @param start
	 *            start index
	 * @param length
	 *            size of array
	 * @return IPDIVariableDescriptor
	 * @throws PDIException
	 *             on failure
	 */
	public IPDIVariableDescriptor getVariableDescriptorAsArray(int start, int length) throws PDIException;

	/**
	 * Consider the variable object as a type
	 * 
	 * @param type
	 *            type of variable
	 * @return IPDIVariableDescriptor
	 * @throws PDIException
	 *             on failure
	 */
	public IPDIVariableDescriptor getVariableDescriptorAsType(String type) throws PDIException;

	/**
	 * Returns a unique id for this variable. This id can be used for looking up the value of the variable.
	 * 
	 * @return a unique id for this variable
	 */
	public String getId();

	/**
	 * Sets IAIF to this variable
	 * 
	 * @param aif
	 *            AIF object
	 */
	public void setAIF(IAIF aif);

	/**
	 * Sets end position of casting array
	 * 
	 * @param end
	 *            position to end
	 */
	public void setCastingArrayEnd(int end);

	/**
	 * Sets start position of casting array
	 * 
	 * @param start
	 *            position to start
	 */
	public void setCastingArrayStart(int start);

	/**
	 * Returns the size of this variable descriptor
	 * 
	 * @return the size of this variable descriptor
	 * @throws PDIException
	 *             on failure
	 */
	public int sizeof() throws PDIException;
}
