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

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

/**
 * A "Block" contains either a single statement or a predicate expression, 
 * and it can be entered only at the beginning and exited only at the end.
 * 
 * @author Yuan Zhang
 *
 */
public interface IBlock {
	/**
	 * @return the unique ID
	 */
	public int getID();

	/**
	 * @return the list of successors in the control flow graph
	 */
	public List<IBlock> getSuccs();
	
	/**
	 * @return the list of predecessors in the control flow graph
	 */
	public List<IBlock> getPreds();
	
	/**
	 * @return the next basic block in CFG according to the topological 
	 * order (top-down order)
	 */
	public IBlock topNext();
	public void setTopNext(IBlock b);
	
	/**
	 * @return the next block in CFG according to the reverse
	 * topological order (bottom-up order)
	 */
	public IBlock botNext();
	public void setBotNext(IBlock b);

	/**
	 * @return the content (a predicate expression or a statement)
	 */
	public IASTNode getContent();
	
	/**
	 * @return true if this block contains expression <expr>
	 * which is the predicate of statement <parent>, false otherwise.
	 */
	public boolean search(IASTExpression expr, IASTStatement parent);
	/** 
	 * @return true if this block contains statement <stmt>,
	 * false otherwise.
	 */
	public boolean search(IASTStatement stmt);
	/**
	 * @return true if this block contains <label>, false otherwise
	 */
	public boolean search(IASTName label);
	
	/*
	 * @return dominators
	 */
	public List<IBlock> getDOM();
	public void setDOM(List<IBlock> set);
	
	/**
	 * @return post-dominators
	 */
	public List<IBlock> getPDOM();
	public void setPDOM(List<IBlock> set);

	/** An attribute (identified by its name) of a block could be any 
	 * property of it.
	 * @param name
	 * @param attr
	 */
	public void setAttr(String name, Object attr);
	public Object getAttr(String name);
	
	public void print();
}
