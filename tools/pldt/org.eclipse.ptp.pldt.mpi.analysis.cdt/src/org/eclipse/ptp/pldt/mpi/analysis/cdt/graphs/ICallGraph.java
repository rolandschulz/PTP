/**********************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;

/**
 * A call graph is a directed graph which represents the calling relation
 * among subroutines in a program. In CDT, it is partially determined
 * by static analysis.
 * 
 * @author Yuan Zhang
 * 
 */
public interface ICallGraph {

	/**
	 * @return all functions in the call graph
	 */
	public List<ICallGraphNode> getAllNodes();

	/**
	 * @return all global variables
	 */
	public List<String> getEnv();

	/**
	 * @return one root function. A root function is never called by
	 *         other functions. There may be multiple root functions in a call
	 *         graph.
	 */
	public ICallGraphNode topEntry();

	public void setTopEntry(ICallGraphNode node);

	/**
	 * @return one leaf function. A leaf function never calls any other
	 *         functions. There may be multiple leaf functions in a call graph.
	 */
	public ICallGraphNode botEntry();

	public void setBotEntry(ICallGraphNode node);

	/**
	 * @return list of recursive function calls
	 */
	public List<List<ICallGraphNode>> getCycles();

	/**
	 * Search for a function according to its filename and function name.
	 * 
	 * @param fileName
	 * @param funcName
	 * @return its call graph node if it is found; null otherwise.
	 */
	public ICallGraphNode getNode(String fileName, String funcName);

	/**
	 * Search for a function according to its declaration.
	 * 
	 * @param fdef
	 * @return its call graph node if it is found; null otherwise.
	 */
	public ICallGraphNode getNode(IASTFunctionDefinition fdef);

	/**
	 * Add a function node to the call graph. Its calling relation
	 * is not constructed yet.
	 * 
	 * @param node
	 */
	public void addNode(ICallGraphNode node);

	/**
	 * Build the calling relations. This method is not reponsible for
	 * collecting functions in a program.
	 */
	public void buildCG();

	public void print();

}
