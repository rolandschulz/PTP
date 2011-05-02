/**********************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.openmp.analysis.ompcfg.factory;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

import org.eclipse.ptp.pldt.openmp.analysis.ompcfg.OMPBasicBlock;
import org.eclipse.ptp.pldt.openmp.analysis.ompcfg.OMPCFGNode;
import org.eclipse.ptp.pldt.openmp.analysis.ompcfg.OMPPragmaNode;

/**
 * Collects information about each pass of OMPCFGmaker
 * 
 * @author pazel
 * 
 */
public class OMPCFGResult
{
	/**
	 * List of sequential chains in 1-level pass: the control flow between
	 * chains is broken by continue, break, return, or goto
	 */
	protected LinkedList chains_ = new LinkedList(); // of Chain
	/**
	 * List of chains with no sequential flow to entry point - these get
	 * aggregated. Could be dead code, but could be branched into.
	 */
	protected LinkedList unconnectedChains_ = new LinkedList(); // of Chain
	/**
	 * List of blocks ending in continue, break, return, or goto whose further
	 * continuation needs to be resolved
	 */
	protected LinkedList unresolvedControlFlow_ = new LinkedList(); // of Chain
	/** list of labeled blocks (and related chain) */
	protected Hashtable labelMap_ = new Hashtable(); // IASTName->LabeledBlock
	/** list of pragma nodes encountered */
	protected LinkedList pragmaNodeList_ = new LinkedList();

	/** block termination reason - unknown */
	public static final int TermUnknown = -1;
	/** block termination reason - no termination */
	public static final int TermForward = 0; // no termination
	/** block termination reason - continue */
	public static final int TermContinue = 1; // continue
	/** block termination reason - break */
	public static final int TermBreak = 2; // break
	/** block termination reason - return */
	public static final int TermReturn = 3; // return
	/** block termination reason - goto */
	public static final int TermGoto = 4; // goto

	/**
	 * OMPCFGResult - constructor
	 * 
	 */
	public OMPCFGResult() {
	}

	/**
	 * addChain - add a chain to the chainList
	 * 
	 * @param headNode
	 * @param tailNode
	 * @param terminationReason
	 * @param termStmt
	 */
	public Chain addChain(OMPCFGNode headNode,
			OMPCFGNode tailNode,
			int terminationReason,
			IASTStatement termStmt)
	{
		Chain chain = new Chain(headNode, tailNode, terminationReason, termStmt);
		chains_.add(chain);

		// run through chain to find all labeled blocks and add to labelmap
		addLabels(chain);
		addPragmas(chain);
		return chain;
	}

	/**
	 * addChain - add a chain to this result
	 * 
	 * @param chain
	 *            - Chain
	 * @return Chain
	 */
	public Chain addChain(Chain chain)
	{
		chains_.add(chain);
		addLabels(chain);
		addPragmas(chain);
		return chain;
	}

	private void addLabels(Chain chain)
	{
		OMPCFGNode node = chain.getHeadNode();
		while (node != null) {
			if (node instanceof OMPBasicBlock) {
				IASTName label = ((OMPBasicBlock) node).getLabel();
				if (label != null) {
					if (!labelMap_.containsKey(label))
						labelMap_.put(label, new LabeledBlock(node, chain));
				}
			}
			node = node.getSuccessor(); // use this knowing each node has a follower
		}
	}

	private void addPragmas(Chain chain)
	{
		OMPCFGNode node = chain.getHeadNode();
		while (node != null) {
			if (node instanceof OMPPragmaNode) {
				if (!pragmaNodeList_.contains(node))
					pragmaNodeList_.add(node);
			}
			node = node.getSuccessor(); // use this knowing each node has a follower
		}
	}

	public void merge(OMPCFGResult result)
	{
		// we don't merge chains_;

		// we merge all the others
		unconnectedChains_.addAll(result.getUnconnectedChains());
		unresolvedControlFlow_.addAll(result.getUnresolvedControlFlow());
		labelMap_.putAll(result.getLabelMap());
		pragmaNodeList_.addAll(result.getPragmaNodeList());
	}

	public void resolve()
	{
		// Usually done to the final cfg results
		// we try to resolve all unresolved control flow (goto's) and
		// return/exit nodes (sink)
		// and maybe flag all unconnected chains as dead code

		for (Iterator i = unresolvedControlFlow_.iterator(); i.hasNext();) {
			Chain uChain = (Chain) i.next();
			if (uChain.getTerminationReason() == TermGoto) {
				IASTStatement tStmt = uChain.getTerminationStmt();
				if (tStmt != null && (tStmt instanceof IASTGotoStatement)) {
					IASTName targetLabel = ((IASTGotoStatement) tStmt).getName();
					OMPCFGNode lNode = (OMPCFGNode) labelMap_.get(targetLabel);
					if (lNode != null) {
						uChain.getTailNode().connectTo(lNode);
						removeUnresolvedControlFlow(uChain);
					}
				}
			}
		}
	}

	public void addUnconnectedChain(Chain chain)
	{
		unconnectedChains_.add(chain);
	}

	public void addUnresolvedControlFlow(Chain chain)
	{
		unresolvedControlFlow_.add(chain);
	}

	public void removeUnresolvedControlFlow(Chain chain)
	{
		unresolvedControlFlow_.remove(chain);
	}

	public int getNumberOfLabels()
	{
		return labelMap_.size();
	}

	public int getNumberOfPragmas()
	{
		return pragmaNodeList_.size();
	}

	// accessors
	public LinkedList getChains() {
		return chains_;
	}

	public LinkedList getUnconnectedChains() {
		return unconnectedChains_;
	}

	public LinkedList getUnresolvedControlFlow() {
		return unresolvedControlFlow_;
	}

	public Hashtable getLabelMap() {
		return labelMap_;
	}

	public LinkedList getPragmaNodeList() {
		return pragmaNodeList_;
	}

	/**
	 * Get the very first chain - a common want in the code (other are dead code)
	 * 
	 * @return Chain
	 */
	public Chain getFirstChain() {
		return (chains_.size() > 0 ? (Chain) (chains_.getFirst()) : null);
	}

	/**
	 * Get the very last chain - a common want in the code (other are dead code)
	 * 
	 * @return Chain
	 */
	public Chain getLastChain() {
		return (chains_.size() > 0 ? (Chain) (chains_.getLast()) : null);
	}

	/**
	 * Chain - qualifies a code chain
	 * 
	 */
	static public class Chain
	{
		protected OMPCFGNode headNode_ = null;
		protected OMPCFGNode tailNode_ = null;
		protected int terminationReason_ = TermUnknown;
		protected IASTStatement terminationStmt_ = null;

		public Chain(OMPCFGNode headNode, OMPCFGNode tailNode, int terminationReason, IASTStatement termStatement)
		{
			headNode_ = headNode;
			tailNode_ = tailNode;
			terminationReason_ = terminationReason;
			terminationStmt_ = termStatement;
		}

		/** Get head node in the Chain */
		public OMPCFGNode getHeadNode() {
			return headNode_;
		}

		/** Get tail node in the Chain */
		public OMPCFGNode getTailNode() {
			return tailNode_;
		}

		public int getTerminationReason() {
			return terminationReason_;
		}

		public IASTStatement getTerminationStmt() {
			return terminationStmt_;
		}
	}

	/** LabeledBlock - qualifies a block with a label */
	static public class LabeledBlock
	{
		protected OMPCFGNode labeledBlock_ = null;
		protected Chain relatedChain_ = null; // chain holding block

		public LabeledBlock(OMPCFGNode labeledBlock, Chain relatedChain)
		{
			labeledBlock_ = labeledBlock;
			relatedChain_ = relatedChain;
		}

		public OMPCFGNode getLabeledBlock() {
			return labeledBlock_;
		}

		public Chain getRelatedChain() {
			return relatedChain_;
		}
	}

}
