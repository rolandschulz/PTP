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
package org.eclipse.ptp.debug.core.pdi;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.pdi.model.IPDIExpression;
import org.eclipse.ptp.debug.core.pdi.model.IPDIMultiExpressions;
import org.eclipse.ptp.debug.core.pdi.model.IPDITargetExpression;

/**
 * Represent expression manager to manage multiple expression
 * @author clement
 *
 */
public interface IPDIExpressionManager extends IPDISessionObject {
	/**
	 * Update status of multiple expressions
	 * @param exprText expression name
	 * @param enabled status of this expression
	 */
	void updateStatusMultiExpressions(String exprText, boolean enabled);
	/**
	 * Remove multiple expressions from store
	 * @param exprText expression name
	 */
	void removeMutliExpressions(String exprText);
	
	/**
	 * Remove multiple expressions from store
	 * @param qTasks task 
	 * @param exprText expression name
	 */
	void removeMutliExpressions(BitList tasks, String exprText);

	/**
	 * Returns an array of multiple expressions
	 * @return an array of multiple expressions
	 */
	IPDIMultiExpressions[] getMultiExpressions();
	
	/**
	 * Creates an expression for specify variable
	 * @param qTasks task 
	 * @param expr variable
	 * @return IPDIExpression expression
	 * @throws PDIException on failure
	 */
	IPDITargetExpression createExpression(BitList qTasks, String expr) throws PDIException;
	
	/**
	 * Creates mutliple expression object for a number of processes with one variable
	 * @param tasks task
	 * @param exprText variable
	 * @param enabled status of this expression
	 */
	void createMutliExpressions(BitList tasks, String exprText, boolean enabled);
	
	/**
	 * Returns IPDIMultiExpressions with specific expression value
	 * @param exprText expression value
	 * @return IPDIMultiExpressions
	 */
	IPDIMultiExpressions getMultiExpression(String exprText);
	
	/**
	 * Returns array of enabled IPDIExpression with specific task id
	 * @param task task id
	 * @return array of enabled IPDIExpression with specific task id
	 */
	IPDIExpression[] getMultiExpressions(int task);

	/**
	 * Update expression value
	 * @param tasks
	 * @param monitor
	 * @throws PDIException
	 */
	void updateMultiExpressions(BitList tasks, IProgressMonitor monitor) throws PDIException;
	
	/**
	 * Update expression value
	 * @param exprText
	 * @param tasks
	 * @param monitor
	 * @throws PDIException
	 */
	void updateMultiExpressions(String exprText, BitList tasks, IProgressMonitor monitor) throws PDIException;
	
	/**
	 * Clean expression value with given tasks
	 * @param tasks tasks
	 * @param monitor
	 * @throws PDIException
	 */
	void cleanMultiExpressions(BitList tasks, IProgressMonitor monitor) throws PDIException;
	
	/**
	 * Clean expression value with given tasks
	 * @param exprText
	 * @param tasks tasks
	 * @param monitor
	 * @throws PDIException
	 */
	void cleanMultiExpressions(String exprText, BitList tasks, IProgressMonitor monitor) throws PDIException;
}
