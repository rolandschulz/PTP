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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.IPDISessionObject;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIF;

/**
 * Handles more than one expression for multi-processes
 * 
 * @author clement
 * 
 */
public interface IPDIMultiExpressions extends IPDISessionObject {
	/**
	 * Stores an expression
	 * 
	 * @param expression
	 *            Expression
	 */
	public void addExpression(IPDIExpression expression);

	/**
	 * @param tasks
	 * @param monitor
	 * @since 4.0
	 */
	public void cleanExpressionsValue(TaskSet tasks, IProgressMonitor monitor);

	/**
	 * Returns the value of this expression.
	 * 
	 * @param expression
	 *            an expression for getting value
	 * @return the value of this expression
	 * @throws PDIException
	 *             on failure
	 */
	public IAIF getAIF(IPDIExpression expression) throws PDIException;

	/**
	 * @param task
	 * @return
	 */
	public IPDIExpression getExpression(int task);

	/**
	 * Returns an array of expressions
	 * 
	 * @return an array of expressions
	 */
	public IPDIExpression[] getExpressions();

	/**
	 * Returns expression text
	 * 
	 * @return expression text
	 */
	public String getExpressionText();

	/**
	 * Returns status of this expression
	 * 
	 * @return status of this expression
	 */
	public boolean isEnabled();

	/**
	 * Remove an expression from store
	 * 
	 * @param tasks
	 *            TaskSet
	 * @since 4.0
	 */
	public void removeExpression(TaskSet tasks);

	/**
	 * Remove an expression from store
	 * 
	 * @param expression
	 *            an expression going to be removed
	 */
	public void removeExpression(IPDIExpression expression);

	/**
	 * Set enabled
	 * 
	 * @param enabled
	 */
	public void setEnabled(boolean enabled);

	/**
	 * Clean up resources
	 */
	public void shutdown();

	/**
	 * Update expression value
	 * 
	 * @param tasks
	 * @param monitor
	 * @throws PDIException
	 * @since 4.0
	 */
	public void updateExpressionsValue(TaskSet tasks, IProgressMonitor monitor) throws PDIException;
}
