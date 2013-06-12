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

import org.eclipse.ptp.debug.core.pdi.PDIException;

/**
 * Represents a data structure in the program.
 * 
 * @author clement
 * 
 */
public interface IPDIVariable extends IPDIVariableDescriptor {
	/**
	 * Remove the variable from the manager list
	 * 
	 * @throws PDIException
	 *             on failure
	 */
	public void dispose() throws PDIException;

	/**
	 * Determines whether both variables are the same
	 * 
	 * @param variable
	 *            IPDIVariable for comparing
	 * @return true if given variable is same as the current variable
	 */
	public boolean equals(IPDIVariable variable);

	/**
	 * gGet child
	 * 
	 * @param varid
	 * @return
	 */
	public IPDIVariable getChild(String varid);

	/**
	 * Returns an array of IPDIVariables of the children of current variable or empty array if nothing
	 * 
	 * @return an array of IPDIVariables of the children of current variable
	 * @throws PDIException
	 *             on failure
	 */
	public IPDIVariable[] getChildren() throws PDIException;

	/**
	 * Returns an array of IPDIVariables of the children of current variable by given a range or empty array if nothing
	 * 
	 * @param findex
	 *            first index of this variable
	 * @param psize
	 *            size of this variable
	 * @return an array of IPDIVariable of the children of current variable
	 * @throws PDIException
	 *             on failure
	 */
	public IPDIVariable[] getChildren(int findex, int psize) throws PDIException;

	/**
	 * Returns the number of children of this variable
	 * 
	 * @return the number of children of this variable
	 * @throws PDIException
	 *             on failure
	 */
	public int getChildrenNumber() throws PDIException;

	/**
	 * Determines whether the value of this variable could be edited
	 * 
	 * @return true if the value of this variable could be edited
	 * @throws PDIException
	 *             on failure
	 */
	public boolean isEditable() throws PDIException;

	/**
	 * Resets value of the current variable
	 */
	public void resetValue();

	/**
	 * Sets value of this variable by given expression
	 * 
	 * @param expression
	 *            an expression to generate a new AIF value
	 * @throws PDIException
	 *             on failure
	 */
	public void setValue(String expression) throws PDIException;
}
