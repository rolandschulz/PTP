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
package org.eclipse.ptp.debug.core.model;

import java.math.BigInteger;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrame;

/**
 * Represents a stack frame
 * 
 * @author Clement chu
 * 
 */
public interface IPStackFrame extends IRunToLine, IRunToAddress, IJumpToLine, IJumpToAddress, IPDebugElement, IStackFrame {
	/**
	 * Check if expressions can be evaluated in this stack frame
	 * 
	 * @return
	 */
	public boolean canEvaluate();

	/**
	 * Evaluate an expression in this stack frame
	 * 
	 * @param expression
	 * @return
	 * @throws DebugException
	 */
	public IValue evaluateExpression(String expression) throws DebugException;

	/**
	 * Evaluate an expression in this stack frame. Return the result as a string
	 * 
	 * @return
	 * @throws DebugException
	 */
	public String evaluateExpressionToString(String expression) throws DebugException;

	/**
	 * Get the address of this stack frame
	 * 
	 * @return
	 */
	public BigInteger getAddress();

	/**
	 * Get the file associated with this stack frame
	 * 
	 * @return
	 */
	public String getFile();

	/**
	 * Get the frame line number
	 * 
	 * @return
	 */
	public int getFrameLineNumber();

	/**
	 * Get the function
	 * 
	 * @return
	 */
	public String getFunction();

	/**
	 * Get the frame level
	 * 
	 * @return
	 */
	public int getLevel();

	/**
	 * Get the PDI stack frame
	 * 
	 * @return
	 */
	public IPDIStackFrame getPDIStackFrame();
}
