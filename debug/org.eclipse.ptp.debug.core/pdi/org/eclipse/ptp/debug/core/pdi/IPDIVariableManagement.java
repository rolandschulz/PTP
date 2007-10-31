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

import org.eclipse.ptp.core.util.BitList;

/**
 * Provides the ability to evaluated variable
 * @author clement
 *
 */
public interface IPDIVariableManagement {
	/**
	 * Requests IAIF value of given variable name
	 * @param tasks target process
	 * @param expr variable name
	 * @throws PDIException on failure
	 */
	void retrieveAIF(BitList tasks, String expr) throws PDIException;

	/**
	 * Requests partial IAIF value of given variable name
	 * @param tasks target process
	 * @param expr variable name
	 * @param key variable key
	 * @param listChildren is list a children?
	 * @param express is expression?
	 * @throws PDIException on failure
	 */
	void retrievePartialAIF(BitList tasks, String expr, String key, boolean listChildren, boolean express) throws PDIException;

	/**
	 * Requests String value of given variable name
	 * @param tasks target process
	 * @param expr variable name
	 * @throws PDIException on failure
	 */
	void evaluateExpression(BitList tasks, String expr) throws PDIException;
	
	/**
	 * Requests String value of given variable name 
	 * @param tasks target process
	 * @param expr variable name
	 * @throws PDIException on failure
	 */
	void dataEvaluateExpression(BitList tasks, String expr) throws PDIException;
	
	/**
	 * Requests IAIFType of given variable name
	 * @param tasks target process
	 * @param var variable name
	 * @throws PDIException on failure
	 */
	void retrieveVariableType(BitList tasks, String var) throws PDIException;
	
	/**
	 * Request a list of argument for a range of level
	 * @param tasks target process
	 * @param low minimum level
	 * @param high maximum level
	 * @throws PDIException failure
	 */
	void listArguments(BitList tasks, int low, int high) throws PDIException;
	
	/**
	 * Requests a list of local variables 
	 * @param tasks target process
	 * @throws PDIException on failure 
	 */
	void listLocalVariables(BitList tasks) throws PDIException;
	
	/**
	 * Requests a list of global variables
	 * @param tasks target process 
	 * @throws PDIException on failure
	 */
	void listGlobalVariables(BitList tasks) throws PDIException;

	/**
	 * Requests to delete watching variable
	 * @param tasks target process
	 * @param var watching variable name
	 * @throws PDIException on failure
	 */
	void deleteVariable(BitList tasks, String var) throws PDIException;
}
