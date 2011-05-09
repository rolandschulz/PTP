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

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;

import org.eclipse.ptp.pldt.openmp.analysis.PAST.PASTOMPPragma;
import org.eclipse.ptp.pldt.openmp.analysis.ompcfg.OMPCFG;
import org.eclipse.ptp.pldt.openmp.analysis.ompcfg.OMPCFGNode;
import org.eclipse.ptp.pldt.openmp.analysis.ompcfg.OMPDFS;
import org.eclipse.ptp.pldt.openmp.analysis.ompcfg.OMPPragmaNode;

/**
 * Main Algorithm -
 * 
 * Given: the start node of a control flow graph (OMPCFG)
 * Output: a set of barrier phases
 * 
 * 1) Start the algorithm using the start node of the cfg
 * 2) For each barrier reached by a DFS, said barrier neither prior processed
 * nor on the stack, push onto stack, and start fresh DFS on new barrier
 * 3) For each node encountered
 * a) if node is in a phase having its initial barrier match with top barrier
 * on the stack, add all nodes after that top barrier to that phase.
 * b) if node is a barrier, build phase, add nodes on stack from penultimate
 * barrier
 * 4) When a DFS completes, pop the stack
 * 5) Terminate when the stack is empty
 * 
 */

public class PhaseAnalysisFactory
{
	protected OMPCFG cfg_ = null;
	protected OMPCFGNode termNode_ = null;

	protected Stack barrierStack_ = new Stack(); // of DFSWalk's
	protected HashSet finishedBarriers_ = new HashSet(); // of barriers that are finished being
															// traversed from
	protected LinkedList phases_ = new LinkedList(); // of PhaseConcurrencyAnalysis's

	// A common search we do is, for a given node, and a given barrier, find all
	// phases that
	// a) contain that node
	// b) have that barrier as the starting barrier of the phase
	// Solution: map each node to a list of phases to which it belongs.
	// The list should be sparse, for filtering out the phases that
	// begin with a particular barrier should be inexpensive.
	protected Hashtable nodeToPhases_ = new Hashtable();

	/**
	 * PhaseAnalysisFactory - Constructor
	 * 
	 * @param cfg
	 *            - OMPCFG
	 */
	public PhaseAnalysisFactory(OMPCFG cfg)
	{
		cfg_ = cfg;
		termNode_ = cfg_.getTermNode();
	}

	public void buildPhases()
	{
		// Set up the first statement
		OMPCFGNode node = cfg_.getRoot();
		if (!(node instanceof OMPPragmaNode))
			return; // required
		DFSWalk pc = new DFSWalk((OMPPragmaNode) node);
		barrierStack_.push(pc);
		pc.trigger();
	}

	/**
	 * findPhase - find a specified phase and add it
	 * 
	 * @param bNode
	 *            - OMPPragmaNode
	 * @param eNode
	 *            - OMPPragmaNode
	 * @param addNew
	 *            - boolean
	 * @return PhaseConcurrencyAnalysis
	 */
	protected PhaseConcurrencyAnalysis findPhase(OMPPragmaNode bNode, OMPPragmaNode eNode, boolean addNew)
	{
		// Find the phase that starts and ends with these TODO: make more efficient
		for (Iterator i = phases_.iterator(); i.hasNext();) {
			PhaseConcurrencyAnalysis phase = (PhaseConcurrencyAnalysis) i.next();
			if (phase.getBeginNode() == bNode && phase.getEndNode() == eNode)
				return phase;
		}
		if (!addNew)
			return null;

		PhaseConcurrencyAnalysis phase = new PhaseConcurrencyAnalysis(bNode, eNode);
		phases_.add(phase);
		return phase;
	}

	/**
	 * addNodesToPhase - add a list of nodes to a specific phase
	 * 
	 * @param phase
	 *            - PhaseConcurrencyAnalysis
	 * @param nodes
	 *            - OMPCFGNode []
	 */
	protected void addNodesToPhase(PhaseConcurrencyAnalysis phase, OMPCFGNode[] nodes)
	{
		for (int i = 0; i < nodes.length; i++) {
			phase.add(nodes[i]);
			mapNodeToPhase(nodes[i], phase);
		}
	}

	protected boolean isFinished(OMPPragmaNode pNode)
	{
		return finishedBarriers_.contains(pNode);
	}

	protected boolean isOnBarrierStack(OMPPragmaNode pNode)
	{
		for (Iterator i = barrierStack_.iterator(); i.hasNext();) {
			DFSWalk dw = (DFSWalk) i.next();
			if (dw.getPragmaNode() == pNode)
				return true;
		}
		return false;
	}

	protected LinkedList memberPhases(OMPCFGNode node)
	{
		return (LinkedList) nodeToPhases_.get(node);
	}

	/**
	 * mapNodeToPhase - map a given node to a given phase in the node-phase map
	 * 
	 * @param node
	 *            - OMPCFGNode
	 * @param phase
	 *            - PhaseConcurrencyAnalysis
	 */
	protected void mapNodeToPhase(OMPCFGNode node, PhaseConcurrencyAnalysis phase)
	{
		LinkedList l = (LinkedList) nodeToPhases_.get(node);
		if (l == null) {
			l = new LinkedList();
			nodeToPhases_.put(node, l);
		}
		if (!l.contains(phase))
			l.add(phase);
	}

	/**
	 * getPhases - produce a list of all phases produced from this analysis
	 * 
	 * @return PhaseConcurrencyAnalysis []
	 */
	public PhaseConcurrencyAnalysis[] getPhases()
	{
		PhaseConcurrencyAnalysis[] l = new PhaseConcurrencyAnalysis[phases_.size()];
		int count = 0;
		for (Iterator i = phases_.iterator(); i.hasNext();)
			l[count++] = (PhaseConcurrencyAnalysis) i.next();
		return l;
	}

	// *************************************************************************
	// DFSWalk
	// *************************************************************************
	protected class DFSWalk extends OMPDFS
	{
		protected OMPPragmaNode phaseBeginNode_ = null;

		public DFSWalk(OMPPragmaNode phaseBeginNode)
		{
			super(phaseBeginNode);
			phaseBeginNode_ = phaseBeginNode;
		}

		public OMPPragmaNode getPragmaNode()
		{
			return phaseBeginNode_;
		}

		/**
		 * trigger - begin the process of walking the graph
		 * 
		 */
		public void trigger()
		{
			this.startWalking();
		}

		/**
		 * visit - visit method - what the user usually overrides
		 * 
		 * @param node
		 *            - OMPCFGNode
		 * @return int (see OMPDFS codes)
		 */
		public int visit(OMPCFGNode node)
		{
			if (node instanceof OMPPragmaNode) {
				OMPPragmaNode pragmaNode = (OMPPragmaNode) node;
				if (pragmaNode.getPragma() != null && pragmaNode.getPragma().getOMPType() != PASTOMPPragma.OmpBarrier)
					return OMPDFS.CONTINUE;
				// If at beginning of stack again, keep moving on
				if (getNodeStackSize() == 1)
					return OMPDFS.CONTINUE;
				// If necessary, create a new phase and add to list of phases
				PhaseConcurrencyAnalysis phase = findPhase(phaseBeginNode_, pragmaNode, true);
				addNodesToPhase(phase, getNodeStack());
				// If barrier is completed or is on stack, we skip searching forward
				if (isFinished(pragmaNode) || isOnBarrierStack(pragmaNode))
					return OMPDFS.SKIP;
				// Create a new walk to the next segments - increase the stack
				// NOTE: we may have to skip the followin for implict barrier
				DFSWalk pc = new DFSWalk(pragmaNode);
				barrierStack_.push(pc);
				pc.trigger();
				// above finished walking, pop the stack, and skip on this level
				barrierStack_.pop();
				return OMPDFS.SKIP;
			}
			else if (node == termNode_) { // this is an implicit barrier, but not further dfs
				if (getNodeStackSize() == 1)
					return OMPDFS.CONTINUE;
				return OMPDFS.CONTINUE;
			}

			// For each visited successor node, add node stack to all phases the successor
			// belongs to, said successor phase beginning with phaseBeginNode_
			// Note: we should not bother with phases that begin with other than phaseBeginNode_ -
			// as then we would have to account for partial paths from other such phases,
			// which we do not have (we glom all nodes together, no paths). That was why
			// we do fresh searches starting at new barriers.
			OMPCFGNode[] nodes = node.getOutNodes();
			for (int i = 0; i < nodes.length; i++) {
				if (isVisited(nodes[i])) {
					LinkedList l = memberPhases(nodes[i]);
					OMPCFGNode[] nodeStack = getNodeStack();
					Object[] listArray = l.toArray(); // safer to use this, l may get modified
					for (int j = 0; j < listArray.length; j++) {
						PhaseConcurrencyAnalysis phase = (PhaseConcurrencyAnalysis) listArray[j];
						if (phase.getBeginNode() == phaseBeginNode_)
							addNodesToPhase(phase, nodeStack);
					}
				}
			}

			return OMPDFS.CONTINUE;
		}

		/**
		 * atStartNode - check if we are at the 1st node of the search
		 * 
		 * @param node
		 * @return
		 */
		protected boolean atStartNode(OMPCFGNode node)
		{
			return (node == phaseBeginNode_ && getNodeStackSize() == 1);
		}

	}
}
