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

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.pdi.IPDISessionObject;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIF;

/**
 * Handles more than one expression for multi-processes
 * @author clement
 *
 */
public interface IPDIMultiExpressions extends IPDISessionObject {
	/**
	 * Returns status of this expression
	 * @return status of this expression
	 */
	boolean isEnabled();
	/**
	 * Returns expression text
	 * @return expression text
	 */
	String getExpressionText();
	/**
	 * Stores an expression
	 * @param expression Expression
	 */
	void addExpression(IPDIExpression expression);
	
	/**
	 * Remove an expression from store
	 * @param expression an expression going to be removed
	 */
	void removeExpression(IPDIExpression expression);
	
	/**
	 * Remove an expression from store
	 * @param tasks BitList
	 */
	void removeExpression(BitList tasks);
	
	/**
	 * Returns an array of expressions
	 * @return an array of expressions
	 */
	IPDIExpression[] getExpressions();

	/**
	 * Returns the value of this expression.
	 * @param expression an expression for getting value
	 * @return the value of this expression
	 * @throws PDIException on failure
	 */
	IAIF getAIF(IPDIExpression expression) throws PDIException;
}
