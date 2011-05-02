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
package org.eclipse.ptp.pldt.openmp.analysis.ompcfg.factory;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.ptp.pldt.openmp.analysis.ompcfg.OMPCFGNode;
import org.eclipse.ptp.pldt.openmp.analysis.ompcfg.OMPPragmaNode;

/**
 * Identifies a pair of barriers, and a set of all nodes in-between
 * 
 * @author pazel
 * 
 */
public class PhaseConcurrencyAnalysis
{
	// The bounds of this phase
	protected OMPPragmaNode beginNode_ = null;
	protected OMPPragmaNode endNode_ = null;
	// The following are the concurrent OMPCFGnode's to this phase
	protected LinkedHashSet nodes_ = new LinkedHashSet();

	/**
	 * PhaseConcurrencyAnalysis - constructor
	 * 
	 * @param beginNode
	 *            - OMPPragmaNode
	 * @param endNode
	 *            - OMPPragmaNode
	 */
	public PhaseConcurrencyAnalysis(OMPPragmaNode beginNode, OMPPragmaNode endNode)
	{
		beginNode_ = beginNode;
		endNode_ = endNode;
	}

	/**
	 * getBeginNode - get the first delimiting barrier node
	 * 
	 * @return OMPPragmaNode
	 */
	public OMPPragmaNode getBeginNode()
	{
		return beginNode_;
	}

	/**
	 * getEndNode - get the end delimiting barrier node
	 * 
	 * @return
	 */
	public OMPPragmaNode getEndNode()
	{
		return endNode_;
	}

	/**
	 * add - add a node to the phase
	 * 
	 * @param node
	 *            - OMPCFGNode
	 */
	public void add(OMPCFGNode node)
	{
		if (!nodes_.contains(node))
			nodes_.add(node);
	}

	/**
	 * getNodes - get the set of nodes
	 * 
	 * @return Set
	 */
	public Set getNodes()
	{
		return nodes_;
	}

	public void printPhase(PrintStream ps)
	{
		ps.println("Begin pragma (" + beginNode_.getId() + ")=" + beginNode_.getType());
		ps.println("End   pragma (" + endNode_.getId() + ")=" + endNode_.getType());
		ps.print("   nodes=");
		for (Iterator i = nodes_.iterator(); i.hasNext();) {
			OMPCFGNode n = (OMPCFGNode) i.next();
			ps.print(n.getId() + " ");
		}
		ps.println();
	}
}
