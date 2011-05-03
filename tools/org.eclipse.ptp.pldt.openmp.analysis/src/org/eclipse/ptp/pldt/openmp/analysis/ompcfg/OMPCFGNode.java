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

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Base class of OMP CFG
 * 
 * @author pazel
 * 
 */
public class OMPCFGNode
{
	// Use these while being built
	protected LinkedList inNodes_ = new LinkedList();
	protected LinkedList outNodes_ = new LinkedList();

	protected int id_ = -1; // useful for correlating phase displays

	protected OMPPragmaNode pragmaContext_ = null; // pragma chain head for this node

	/**
	 * OMPCFGNode - constructor
	 * 
	 */
	public OMPCFGNode()
	{

	}

	/**
	 * connectTo - make "this" point forward to input node (forward control flow)
	 * 
	 * @param node
	 *            - OMPCFGNode (that succeeds this in control flow
	 */
	public void connectTo(OMPCFGNode node)
	{
		if (node == null)
			return;
		addOutNode(node);
		node.addInNode(this);
	}

	/**
	 * addInNode - add an input node
	 * 
	 * @param inNode
	 *            - OMPCFGNode
	 */
	public void addInNode(OMPCFGNode inNode)
	{
		if (!inNodes_.contains(inNode))
			inNodes_.add(inNode);
	}

	/**
	 * addOutNode - add an output node
	 * 
	 * @param outNode
	 *            - OMPCFGNode
	 */
	public void addOutNode(OMPCFGNode outNode)
	{
		if (!outNodes_.contains(outNode))
			outNodes_.add(outNode);
	}

	/**
	 * getSuccessor - useful function to be used when it is known that there is 1 successor
	 * no check made for more than 1 - used in following chains
	 * 
	 * @return OMPCFGNode
	 */
	public OMPCFGNode getSuccessor()
	{
		if (outNodes_.size() != 0)
			return (OMPCFGNode) (outNodes_.get(0));
		return null;
	}

	/**
	 * hasPredecessors - tell if node has predecessors
	 * 
	 * @return boolean
	 */
	public boolean hasPredecessors()
	{
		return (inNodes_.size() != 0);
	}

	/**
	 * getInNodes - accessor to input nodes
	 * 
	 * @return OMPCFGNode []
	 */
	public OMPCFGNode[] getInNodes()
	{
		OMPCFGNode[] inNodesArray = new OMPCFGNode[inNodes_.size()];
		int count = 0;
		for (Iterator i = inNodes_.iterator(); i.hasNext();)
		{
			inNodesArray[count++] = (OMPCFGNode) i.next();
		}
		return inNodesArray;
	}

	/**
	 * getOutNodes - accessor to output nodes
	 * 
	 * @return OMPCFGNode []
	 */
	public OMPCFGNode[] getOutNodes()
	{
		OMPCFGNode[] outNodesArray = new OMPCFGNode[outNodes_.size()];
		int count = 0;
		for (Iterator i = outNodes_.iterator(); i.hasNext();)
		{
			outNodesArray[count++] = (OMPCFGNode) i.next();
		}
		return outNodesArray;
	}

	public void setId(int id)
	{
		id_ = id;
	}

	public int getId() {
		return id_;
	}

	public void setPragmaContext(OMPPragmaNode pNode) {
		pragmaContext_ = pNode;
	}

	public OMPPragmaNode getPragmaContext() {
		return pragmaContext_;
	}

}
