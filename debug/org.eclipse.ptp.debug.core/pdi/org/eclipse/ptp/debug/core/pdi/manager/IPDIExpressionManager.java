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
package org.eclipse.ptp.debug.core.pdi.manager;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.model.IPDIExpression;
import org.eclipse.ptp.debug.core.pdi.model.IPDIMultiExpressions;
import org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrame;
import org.eclipse.ptp.debug.core.pdi.model.IPDITargetExpression;
import org.eclipse.ptp.debug.core.pdi.model.IPDIVariable;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIF;

/**
 * Represent expression manager to manage multiple expression
 * @author clement
 *
 */
public interface IPDIExpressionManager extends IPDIManager {
	/**
	 * Clean expression value with given tasks
	 * @param tasks tasks
	 * @param monitor
	 * @throws PDIException
	 */
	public void cleanMultiExpressions(BitList tasks, IProgressMonitor monitor) throws PDIException;
	
	/**
	 * Clean expression value with given tasks
	 * @param exprText
	 * @param tasks tasks
	 * @param monitor
	 * @throws PDIException
	 */
	public void cleanMultiExpressions(String exprText, BitList tasks, IProgressMonitor monitor) throws PDIException;
	
	/**
	 * Creates an expression for specify variable
	 * @param qTasks task 
	 * @param expr variable
	 * @return IPDIExpression expression
	 * @throws PDIException on failure
	 */
	public IPDITargetExpression createExpression(BitList qTasks, String expr) throws PDIException;
	
	/**
	 * Creates mutliple expression object for a number of processes with one variable
	 * @param tasks task
	 * @param exprText variable
	 * @param enabled status of this expression
	 */
	public void createMutliExpressions(BitList tasks, String exprText, boolean enabled);
	
	/**
	 * @param frame
	 * @param expr
	 * @return
	 * @throws PDIException
	 */
	public IPDIVariable createVariable(IPDIStackFrame frame, String expr) throws PDIException;
	
	/**
	 * @param variable
	 * @throws PDIException
	 */
	public void deleteVariable(IPDIVariable variable) throws PDIException;
	
	/**
	 * @param qTasks
	 * @param expressions
	 * @throws PDIException
	 */
	public void destroyExpressions(BitList qTasks, IPDIExpression[] expressions) throws PDIException;
	
	/**
	 * @param qTasks
	 * @param expr
	 * @return
	 * @throws PDIException
	 */
	public IAIF getExpressionValue(BitList qTasks, String expr) throws PDIException;

	/**
	 * Returns IPDIMultiExpressions with specific expression value
	 * @param exprText expression value
	 * @return IPDIMultiExpressions
	 */
	public IPDIMultiExpressions getMultiExpression(String exprText);
	
	/**
	 * Returns an array of multiple expressions
	 * @return an array of multiple expressions
	 */
	public IPDIMultiExpressions[] getMultiExpressions();
	
	/**
	 * Returns array of enabled IPDIExpression with specific task id
	 * @param task task id
	 * @return array of enabled IPDIExpression with specific task id
	 */
	public IPDIExpression[] getMultiExpressions(int task);
	
	/**
	 * Remove multiple expressions from store
	 * @param qTasks task 
	 * @param exprText expression name
	 */
	public void removeMutliExpressions(BitList tasks, String exprText);
	
	/**
	 * Remove multiple expressions from store
	 * @param exprText expression name
	 */
	public void removeMutliExpressions(String exprText);

	/**
	 * @param qTasks
	 * @param varList
	 * @throws PDIException
	 */
	public void update(BitList qTasks, String[] varList) throws PDIException;
	
	/**
	 * Update expression value
	 * @param tasks
	 * @param monitor
	 * @throws PDIException
	 */
	public void updateMultiExpressions(BitList tasks, IProgressMonitor monitor) throws PDIException;
	
	/**
	 * Update expression value
	 * @param exprText
	 * @param tasks
	 * @param monitor
	 * @throws PDIException
	 */
	public void updateMultiExpressions(String exprText, BitList tasks, IProgressMonitor monitor) throws PDIException;
	
	/**
	 * Update status of multiple expressions
	 * @param exprText expression name
	 * @param enabled status of this expression
	 */
	public void updateStatusMultiExpressions(String exprText, boolean enabled);
}
