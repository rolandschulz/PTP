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
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIF;

/**
 * A register is special kind of variable that is contained in a register group.
 * Each register has a name and a AIF value
 * 
 * @author clement
 * 
 */
public interface IPDIRegister extends IPDIVariable, IPDIRegisterDescriptor {
	/**
	 * Returns whether the value of this variable could be changed
	 * 
	 * @return true if the value of this variable could be changed
	 * @throws PDIException
	 *             on failure
	 */
	public boolean isEditable() throws PDIException;

	/**
	 * Returns AIF value of this variable
	 * 
	 * @param context
	 *            StackFrame
	 * @return AIF value of this variable
	 * @throws PDIException
	 *             on failure
	 */
	public IAIF getAIF(IPDIStackFrame context) throws PDIException;

	/**
	 * Sets value of this variable by given expression
	 * 
	 * @param expression
	 *            an expression to generate a new AIF value
	 * @throws PDIException
	 *             on failure
	 */
	public void setValue(String expression) throws PDIException;

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
}
