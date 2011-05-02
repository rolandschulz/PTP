/**********************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.openmp.analysis.ompcfg;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;

/**
 * Does a depth first search through an OMPCFG,
 * given the initial node
 * 
 * @author pazel
 * 
 */
public abstract class OMPDFS
{
	protected OMPCFGNode startNode_ = null;
	protected HashSet visited_ = new HashSet();

	protected Stack currentNodes_ = new Stack();

	public static final int CONTINUE = 0;
	public static final int SKIP = 1; // SKIP following current node further
	public static final int ABORT = 2; // abord DFS

	/**
	 * OMPDFS - constructor
	 * 
	 * @param startNode
	 *            - OMPCFGNode
	 */
	public OMPDFS(OMPCFGNode startNode)
	{
		startNode_ = startNode;
	}

	/**
	 * startWalking - method to start the dfs walking
	 * 
	 */
	public void startWalking()
	{
		walkDFS(startNode_);
	}

	/**
	 * walkDFS - recursively walk the tree visiting each node 1 time
	 * NOTE: only connected component to start node
	 * 
	 * @param node
	 *            - OMPCFGNode
	 * @return int (code to continue, skip, or abort)
	 */
	protected int walkDFS(OMPCFGNode node)
	{
		if (visited_.contains(node))
			return CONTINUE;

		currentNodes_.push(node); // sets the new context on the stack
		int code = visit(node);
		visited_.add(node);

		if (code == SKIP) {
			currentNodes_.pop();
			return CONTINUE;
		} // we only skip this level, continue others
		if (code == ABORT) {
			currentNodes_.pop();
			return ABORT;
		}

		OMPCFGNode[] outNodes = node.getOutNodes();
		for (int i = 0; i < outNodes.length; i++) {
			int iCode = walkDFS(outNodes[i]);
			if (iCode == ABORT) {
				currentNodes_.pop();
				return ABORT;
			} // code cannot be skip
		}
		currentNodes_.pop();

		return CONTINUE;
	}

	/**
	 * getNodeStack - get the current node stack context
	 * 
	 * @return OMPCFGNode []
	 */
	public OMPCFGNode[] getNodeStack()
	{
		OMPCFGNode[] l = new OMPCFGNode[currentNodes_.size()];
		int count = 0;
		for (Iterator i = currentNodes_.iterator(); i.hasNext();)
			l[count++] = (OMPCFGNode) i.next();
		return l;
	}

	/**
	 * getNodeStackSize - get the size of the stack
	 * 
	 * @return int
	 */
	public int getNodeStackSize()
	{
		return currentNodes_.size();
	}

	/**
	 * isVisited - determine if node has been visited on walk
	 * 
	 * @param node
	 *            - OMPCFGNode
	 * @return boolean
	 */
	public boolean isVisited(OMPCFGNode node)
	{
		return visited_.contains(node);
	}

	/**
	 * visit - visit method - what the user usually overrides
	 * 
	 * @param node
	 *            - OMPCFGNode
	 */
	public abstract int visit(OMPCFGNode node);

}
