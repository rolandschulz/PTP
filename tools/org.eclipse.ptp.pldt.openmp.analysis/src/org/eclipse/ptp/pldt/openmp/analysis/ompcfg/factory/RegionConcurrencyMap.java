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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.ptp.pldt.openmp.analysis.PAST.PASTOMPPragma;
import org.eclipse.ptp.pldt.openmp.analysis.ompcfg.OMPBasicBlock;
import org.eclipse.ptp.pldt.openmp.analysis.ompcfg.OMPCFG;
import org.eclipse.ptp.pldt.openmp.analysis.ompcfg.OMPCFGNode;
import org.eclipse.ptp.pldt.openmp.analysis.ompcfg.OMPDFS;
import org.eclipse.ptp.pldt.openmp.analysis.ompcfg.OMPExpressionBlock;
import org.eclipse.ptp.pldt.openmp.analysis.ompcfg.OMPPragmaNode;

/**
 * Builds the concurrency map and provides access to it
 * 
 * @author pazel
 * 
 */
public class RegionConcurrencyMap
{

	protected RegionConcurrencyAnalysis region_ = null;
	protected OMPCFG cfg_ = null;

	protected PhaseConcurrencyAnalysis[] phases_ = null;

	// The actual map
	protected ArrayList indexMap_ = new ArrayList(); // index->stmt
	protected Hashtable stmtMap_ = new Hashtable(); // stmt->index
	// stmt to BitSet, representing all statments concurrent to stmt
	protected Hashtable concurrencyMap_ = new Hashtable();

	// Used in each phase process to list all nodes found
	protected LinkedList phaseStmts_ = new LinkedList();

	// Some sub-regions need to exclude within themselves. To help with this,
	// we build a iastnode-->OMPPragmaNode hashmap, used by the second part of processConcurrency
	protected Hashtable exclusionMap_ = new Hashtable();

	/**
	 * RegionConcurrencyMap - constructor
	 * 
	 * @param component
	 *            - RegionConcurrencyAnalysis
	 */
	public RegionConcurrencyMap(RegionConcurrencyAnalysis region)
	{
		region_ = region;
		cfg_ = region_.getCFG();
		phases_ = region_.getPhases();
	}

	/**
	 * buildMap - build the concurrencyMap_ and other artifacts for the map
	 * 
	 */
	public void buildMap()
	{
		for (int i = 0; i < phases_.length; i++)
			processPhase(phases_[i]);
	}

	/**
	 * processPhase - process given phase into the concurrency map
	 * 
	 * @param phase
	 *            - PhaseConcurrencyAnalysis
	 */
	protected void processPhase(PhaseConcurrencyAnalysis phase)
	{
		Set nodes = phase.getNodes(); // of OMPCFGNode's
		phaseStmts_.clear();
		exclusionMap_.clear();

		OMPPragmaNode ePragma = null; // exclusion pragma

		for (Iterator i = nodes.iterator(); i.hasNext();) {
			OMPCFGNode node = (OMPCFGNode) i.next();

			// Establish the exclusion context if any
			ePragma = node.getPragmaContext();
			if (ePragma != null && ePragma.getPragma() != null) {
				int pType = ePragma.getPragma().getOMPType();
				// if none of these situations, all the stmts in the node can execute concurrently to each other
				// (note: if single and has barrier free path to self, stmts can execute concurrently to each other
				if (!((pType == PASTOMPPragma.OmpSingle && !hasBarrierFreePathToSelf((OMPBasicBlock) node)) ||
						pType == PASTOMPPragma.OmpMaster ||
						pType == PASTOMPPragma.OmpOrdered ||
						pType == PASTOMPPragma.OmpCritical || pType == PASTOMPPragma.OmpSection))
					ePragma = null;
			}
			else
				ePragma = null;

			if (node instanceof OMPBasicBlock) {
				LinkedList stmts = ((OMPBasicBlock) node).getFundamentals();
				for (Iterator j = stmts.iterator(); j.hasNext();) {
					IASTNode n = (IASTNode) j.next();
					indexNode(n, ePragma);
				}
				indexNode(((OMPBasicBlock) node).getBranchingExpression(), ePragma);
			}
			else if (node instanceof OMPExpressionBlock) {
				IASTExpression[] eList = ((OMPExpressionBlock) node).getExpressions();
				for (int j = 0; j < eList.length; j++)
					indexNode(eList[j], ePragma);
			}
		}

		// Go though the list of phase nodes, and
		// ASSUMING THEY ARE ALL CONCURRENT WITH EACH OTHER (will change later)
		// make them all concurrent to each other
		for (Iterator i = phaseStmts_.iterator(); i.hasNext();) {
			IASTNode n = (IASTNode) i.next();
			OMPPragmaNode ePragmaS = (OMPPragmaNode) exclusionMap_.get(n);
			int nIndex = ((Integer) (stmtMap_.get(n))).intValue();
			for (Iterator j = phaseStmts_.iterator(); j.hasNext();) {
				IASTNode m = (IASTNode) j.next();
				OMPPragmaNode ePragmaT = (OMPPragmaNode) exclusionMap_.get(m);

				// stmts in the same exclusion cannot be concurrent
				if (!(ePragmaT == ePragmaS && ePragmaT != null)) {
					BitSet b = (BitSet) (concurrencyMap_.get(m));
					b.set(nIndex);
				}

			}
		}
	}

	/**
	 * indexNode - index a node if node not seen to this process
	 * 
	 * @param n
	 *            - IASTNode
	 */
	protected void indexNode(IASTNode n, OMPPragmaNode ePragma)
	{
		if (n == null)
			return;
		if (!stmtMap_.containsKey(n)) {
			indexMap_.add(n);
			stmtMap_.put(n, new Integer(indexMap_.indexOf(n)));
			concurrencyMap_.put(n, new BitSet());
		}
		if (!phaseStmts_.contains(n))
			phaseStmts_.add(n);
		// Set up the exclusion map
		if (ePragma != null && !exclusionMap_.contains(n))
			exclusionMap_.put(n, ePragma);
	}

	/**
	 * getNodesConcurrentTo - get all nodes concurrent to given node
	 * 
	 * @param node
	 *            - IASTNode
	 * @return Set
	 */
	public Set getNodesConcurrentTo(IASTNode node)
	{
		BitSet b = (BitSet) concurrencyMap_.get(node);
		if (b == null)
			return null; // stmt never registered - ind. called for wrong component

		HashSet ans = new HashSet();
		for (int i = b.nextSetBit(0); i >= 0; i = b.nextSetBit(i + 1))
			ans.add(indexMap_.get(i));
		return ans;
	}

	private boolean hasBarrierFreePathToSelf(OMPBasicBlock singleBlock)
	{
		BarrierPathDFS dfs = new BarrierPathDFS(singleBlock);
		dfs.startWalking();

		return dfs.hasBarrierFreePath();
	}

	// -------------------------------------------------------------------------
	// BarrierPathDFS - find barrier free path to pragma
	// -------------------------------------------------------------------------
	private class BarrierPathDFS extends OMPDFS
	{
		// find a barrier free path to this node
		protected OMPPragmaNode target_ = null;
		protected boolean barrierFreeExists_ = false;

		public BarrierPathDFS(OMPBasicBlock block)
		{
			super(block);
			target_ = block.getPragmaContext();
		}

		public boolean hasBarrierFreePath()
		{
			return barrierFreeExists_;
		}

		/**
		 * visit - visit method - what the user usually overrides
		 * 
		 * @param node
		 *            - OMPCFGNode
		 */
		public int visit(OMPCFGNode node)
		{
			if (node instanceof OMPPragmaNode) {
				OMPPragmaNode thisNode = (OMPPragmaNode) node;
				if (thisNode.isImplicitBarrier() || thisNode.getPragma().getOMPType() == PASTOMPPragma.OmpBarrier)
					return SKIP;
				else if (thisNode == target_) {
					barrierFreeExists_ = true;
					return ABORT;
				}
			}
			return CONTINUE;
		}
	}

}
