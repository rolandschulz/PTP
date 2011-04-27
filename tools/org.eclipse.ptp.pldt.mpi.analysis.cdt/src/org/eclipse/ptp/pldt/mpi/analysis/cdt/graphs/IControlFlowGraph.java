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

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

/**
 * A control flow graph is directed graph. Each node is a block, and
 * each edge represents the jump in the control flow.
 * 
 * @author Yuan Zhang
 * 
 */

public interface IControlFlowGraph {

	/**
	 * @return the entry block
	 */
	public IBlock getEntry();

	/**
	 * @return the exit block
	 */
	public IBlock getExit();

	/**
	 * Search for the block which contains the statement stmt
	 */
	public IBlock getBlock(IASTStatement stmt);

	/**
	 * Search for the block which contains the condition expression
	 * expr, and expr is the predicate of statement parent
	 * 
	 * @return
	 */
	public IBlock getBlock(IASTExpression expr, IASTStatement parent);

	/**
	 * Search for the block which contains the label
	 * 
	 * @return
	 */
	public IBlock getBlock(IASTName label);

	public void addBlock(IBlock bb);

	/**
	 * Build the control flow relation
	 */
	public void buildCFG();

	public void print();
}
