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

import org.eclipse.ptp.debug.core.TaskSet;

/**
 * Provides the ability to evaluated variable
 * @author clement
 *
 */
public interface IPDIVariableManagement {
	/**
	 * Requests to delete named partial expression
	 * @param tasks target process
	 * @param exprId ID of partial expression to delete
	 * @throws PDIException on failure
	 */
	void deletePartialExpression(TaskSet tasks, String exprId) throws PDIException;

	/**
	 * Requests String value of given expression
	 * @param tasks target process
	 * @param expr expression to evaluate
	 * @throws PDIException on failure
	 */
	void evaluateExpression(TaskSet tasks, String expr) throws PDIException;
		
	/**
	 * Requests partial IAIF value of given expression
	 * @param tasks target process
	 * @param expr expression to evaluate
	 * @param exprId ID to refer to a pre-evaluated expression
	 * @param listChildren is list a children?
	 * @param express is expression?
	 * @throws PDIException on failure
	 */
	void evaluatePartialExpression(TaskSet tasks, String expr, String exprId, boolean listChildren, boolean express) throws PDIException;
	
	/**
	 * Request a list of argument for a range of level
	 * @param tasks target process
	 * @param low minimum level
	 * @param high maximum level
	 * @throws PDIException failure
	 */
	void listArguments(TaskSet tasks, int low, int high) throws PDIException;
	
	/**
	 * Requests a list of global variables
	 * @param tasks target process 
	 * @throws PDIException on failure
	 */
	void listGlobalVariables(TaskSet tasks) throws PDIException;
	
	/**
	 * Requests a list of local variables 
	 * @param tasks target process
	 * @throws PDIException on failure 
	 */
	void listLocalVariables(TaskSet tasks) throws PDIException;

	/**
	 * Requests IAIFType of given variable name
	 * @param tasks target process
	 * @param var variable name
	 * @throws PDIException on failure
	 */
	void retrieveVariableType(TaskSet tasks, String var) throws PDIException;
}
