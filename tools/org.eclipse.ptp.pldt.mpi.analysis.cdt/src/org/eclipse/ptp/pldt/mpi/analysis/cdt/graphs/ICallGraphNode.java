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
import org.eclipse.core.resources.IResource;

/**
 * A call graph node corresponds to a user-defined function.
 * 
 * @author Yuan Zhang
 * 
 */
public interface ICallGraphNode {

	/**
	 * @return the function name
	 */
	public String getFuncName();

	/**
	 * @return the enclosing file name
	 */
	public String getFileName();

	public IResource getResource();

	/**
	 * @return the function declaration
	 */
	public IASTFunctionDefinition getFuncDef();

	/**
	 * @return the set of functions that call this function
	 */
	public List<ICallGraphNode> getCallers();

	public void addCaller(ICallGraphNode caller);

	/**
	 * @return the set of functions that are called by this function
	 */
	public List<ICallGraphNode> getCallees();

	public void addCallee(ICallGraphNode callee);

	/**
	 * @return the control flow graph of this function
	 */
	public IControlFlowGraph getCFG();

	public void setCFG(IControlFlowGraph cfg);

	/**
	 * @return the next function according to the topological order
	 */
	public ICallGraphNode topNext();

	public void setTopNext(ICallGraphNode node);

	/**
	 * @return the next function according to the reverse topological order
	 */
	public ICallGraphNode botNext();

	public void setBotNext(ICallGraphNode node);

	public void setAttr(String name, Object attr);

	public Object getAttr(String name);

	public void removeAttr(String name);

	public void setRecursive(boolean val);

	public boolean isRecursive();
}
