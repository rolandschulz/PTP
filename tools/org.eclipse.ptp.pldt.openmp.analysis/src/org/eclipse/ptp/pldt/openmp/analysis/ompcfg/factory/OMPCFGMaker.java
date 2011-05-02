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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTContinueStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;

import org.eclipse.ptp.pldt.openmp.analysis.PAST.PASTOMPPragma;
import org.eclipse.ptp.pldt.openmp.analysis.ompcfg.OMPBasicBlock;
import org.eclipse.ptp.pldt.openmp.analysis.ompcfg.OMPCFG;
import org.eclipse.ptp.pldt.openmp.analysis.ompcfg.OMPCFGNode;
import org.eclipse.ptp.pldt.openmp.analysis.ompcfg.OMPExpressionBlock;
import org.eclipse.ptp.pldt.openmp.analysis.ompcfg.OMPPragmaNode;
import org.eclipse.ptp.pldt.openmp.analysis.ompcfg.factory.OMPCFGResult.Chain;

/**
 * Builds a section of the control flow graph (like CFGMaker)
 * 
 * @author pazel
 * 
 */
public class OMPCFGMaker extends ASTVisitor
{
	protected IASTNode tNode_ = null;

	// This is the return structure
	protected OMPCFGResult result_ = new OMPCFGResult();

	// These get passed down to each level -
	// pragmaRegionMap_ never changes
	// pragmaContext increases/decreases on encountering pragma statements
	protected static Hashtable pragmaRegionMap_ = null; // maps IASTNode to pragma, if node is a pragma region
	protected static Hashtable pragmaLocationMap_ = null; // maps IASTnode to list of pragmas that may immed. succeed it
	protected static Stack pragmaContext_ = null; // stack of pragma - indicating depth of OMP control

	// Again the perspective is that we are building a list of a mix of
	// basic blocks and pragma nodes - nested processing done recursively
	// This two items manage the progress on the current chain
	protected OMPCFGNode firstNode_ = null;
	protected OMPCFGNode currentNode_ = null;

	protected static boolean traceOn_ = false;

	/**
	 * constructCFG - static method to build the cfg
	 * 
	 * @param pragma
	 *            - PASTOMPPragma (focus pragma for this graph
	 * @param statement
	 *            - IASTStatement (region for pragma)
	 * @param pragmaRegionMap
	 *            - Hashtable (mapping region to pragma)
	 * @param pragmaLocationMap
	 *            - Hashtable (mapping IASTNode to list of immed. succ. pragmas)
	 * @return OMPCFG
	 */
	public static OMPCFG constructCFG(PASTOMPPragma pragma, IASTStatement statement, Hashtable pragmaRegionMap,
			Hashtable pragmaLocationMap)
	{
		pragmaRegionMap_ = pragmaRegionMap;
		pragmaLocationMap_ = pragmaLocationMap;
		pragmaContext_ = new Stack();

		OMPCFGMaker maker = new OMPCFGMaker(statement);
		maker.buildCFG();
		OMPCFGResult result = maker.getResult();
		result.resolve();

		OMPCFG cfg = new OMPCFG(pragma,
				statement,
				result.getFirstChain(),
				result.getUnconnectedChains(),
				result.getUnresolvedControlFlow(),
				result.getLabelMap());

		pragmaRegionMap_ = null;
		pragmaLocationMap_ = null;
		pragmaContext_ = null;
		return cfg;
	}

	/**
	 * OMOPCFGMaker - constructor
	 * 
	 * @param tNode
	 *            - IASTNode
	 */
	protected OMPCFGMaker(IASTStatement tNode)
	{
		tNode_ = tNode;
		shouldVisitStatements = true;
		shouldVisitDeclarations = true;
	}

	/**
	 * OMPCFGMaker - constructor
	 * 
	 * @param tNode
	 *            - IASTNode
	 */
	protected OMPCFGMaker(IASTExpression tNode)
	{
		tNode_ = tNode;
		shouldVisitExpressions = true;
	}

	/**
	 * buildCFG - method to initiate building tree using tree walker
	 * 
	 */
	public void buildCFG()
	{
		if (tNode_ == null) // this can occur, e.g. blank else part of if
			firstNode_ = currentNode_ = new OMPBasicBlock(); // with nothing in it
		else
			tNode_.accept(this);

		// put the last chain on the list
		if (currentNode_ != null) {
			result_.addChain(firstNode_, currentNode_, OMPCFGResult.TermForward, null);
			currentNode_ = null;
			firstNode_ = null;
		}

	}

	public OMPCFGResult getResult() {
		return result_;
	}

	/**
	 * visit - for an expression block
	 * 
	 * @param expression
	 *            - IASTExpression
	 */
	public int visit(IASTExpression expression)
	{
		// simple for now - may need to drill down later
		OMPExpressionBlock b = new OMPExpressionBlock();
		b.setPragmaContext(getLastPragma());
		b.addExpression(expression);
		firstNode_ = currentNode_ = b;

		return PROCESS_CONTINUE;
	}

	public int visit(IASTDeclaration declaration)
	{
		return PROCESS_CONTINUE;
	}

	public int visit(IASTStatement statement)
	{
		// first, check if statement is a region for a pragma
		int checkValue = pragmaRegionCheck(statement);
		if (checkValue != -1)
			return checkValue; // -1 means carry foward in this code

		if (statement instanceof IASTIfStatement) {
			processIfStatement((IASTIfStatement) statement);
			pragmaRegionlessCheck(statement, PASTOMPPragma.NeighborProximity);
			return PROCESS_SKIP;
		}
		else if (statement instanceof IASTSwitchStatement) {
			processSwitchStatement((IASTSwitchStatement) statement);
			pragmaRegionlessCheck(statement, PASTOMPPragma.NeighborProximity);
			return PROCESS_SKIP;
		}
		else if (statement instanceof IASTCaseStatement) {
			processCaseStatement((IASTCaseStatement) statement);
			return PROCESS_CONTINUE;
		}
		else if (statement instanceof IASTForStatement) {
			processForStatement((IASTForStatement) statement);
			pragmaRegionlessCheck(statement, PASTOMPPragma.NeighborProximity);
			return PROCESS_SKIP;
		}
		else if (statement instanceof IASTWhileStatement) {
			processWhileStatement((IASTWhileStatement) statement);
			pragmaRegionlessCheck(statement, PASTOMPPragma.NeighborProximity);
			return PROCESS_SKIP;
		}
		else if (statement instanceof IASTDoStatement) {
			processDoStatement((IASTDoStatement) statement);
			pragmaRegionlessCheck(statement, PASTOMPPragma.NeighborProximity);
			return PROCESS_SKIP;
		}
		else if (statement instanceof IASTCompoundStatement) {
			pragmaRegionlessCheck(statement, PASTOMPPragma.ChildProximity);
			return PROCESS_CONTINUE;
		}
		else if (statement instanceof IASTBreakStatement) {
			addToBasicBlockAndChain(statement, OMPCFGResult.TermBreak);
			return PROCESS_CONTINUE;
		}
		else if (statement instanceof IASTContinueStatement) {
			addToBasicBlockAndChain(statement, OMPCFGResult.TermContinue);
			return PROCESS_CONTINUE;
		}
		else if (statement instanceof IASTReturnStatement) {
			addToBasicBlockAndChain(statement, OMPCFGResult.TermReturn);
			return PROCESS_CONTINUE;
		}

		// just add to the current basic block if there is one
		addToBasicBlock(statement);
		pragmaRegionlessCheck(statement, PASTOMPPragma.NeighborProximity);

		return PROCESS_CONTINUE;

	}

	/**
	 * pragmaRegionCheck - if stmt is region for a preceding pragma, add pragma
	 * 
	 * @param statement
	 *            - IASTStatement
	 * @return int (-1 means nothing done)
	 */
	protected int pragmaRegionCheck(IASTStatement statement)
	{
		PASTOMPPragma pragma = (PASTOMPPragma) pragmaRegionMap_.get(statement);

		// The following avoids a nasty recursion loop, resulting from
		// the recursion we invest on the following lines, i.e.
		// we keep building makers for the same statement
		if (pragmaContext_.size() != 0 && ((OMPPragmaNode) pragmaContext_.peek()).getPragma() == pragma)
			return -1;

		if (pragma != null) {
			OMPCFGNode pNode = new OMPPragmaNode(pragma, getLastPragma());
			pNode.setPragmaContext(getLastPragma());
			addToChain(pNode);

			// TODO: Analyse the context here to see if legal OMP pragma makes sense (ret -1)

			pragmaContext_.push(pNode); // bump the context

			// Do a 1 level recursion to process this statement - so we can pop this context
			OMPCFGMaker maker = new OMPCFGMaker(statement);
			maker.buildCFG();
			pragmaContext_.pop();

			result_.merge(maker.getResult());
			// in the case of null compound statements, there is nothing here
			if (maker.getResult().getFirstChain() != null) {
				pNode.connectTo(maker.getResult().getFirstChain().getHeadNode());
				currentNode_ = maker.getResult().getFirstChain().getTailNode();
			}

			// For certain pragmas, we will append an implicit barrier
			int type = pragma.getOMPType();
			if ((type == PASTOMPPragma.OmpParallel || type == PASTOMPPragma.OmpParallelFor ||
					type == PASTOMPPragma.OmpParallelSections || type == PASTOMPPragma.OmpSingle) &&
					pragma.getNoWait() == false) {
				addToChain(new OMPPragmaNode(), true); // constructor set implicit barrier
			}

			return PROCESS_SKIP;
		}

		return -1;
	}

	/**
	 * pragmaRegionlessCheck - add things like barrier, etc. as a separate node
	 * 
	 * @param statement
	 *            - IASTStatement (preceeding the pragma)
	 * @param proximity
	 *            - int (see PASTOMPPragma constants, child, neighbor)
	 */
	protected void pragmaRegionlessCheck(IASTStatement statement, int proximity)
	{
		LinkedList l = (LinkedList) pragmaLocationMap_.get(statement);
		if (l == null)
			return;

		for (Iterator i = l.iterator(); i.hasNext();) {
			PASTOMPPragma oPragma = (PASTOMPPragma) i.next();
			if (oPragma.getProximity() == proximity && oPragma.getRegion() == null) {
				OMPCFGNode pNode = new OMPPragmaNode(oPragma, getLastPragma());
				pNode.setPragmaContext(getLastPragma());
				addToChain(pNode);
				// no pop nor push needs to be done here
			}
		}
	}

	/**
	 * processIfStatement - process the the then and else into a unified result
	 * 
	 * @param ifs
	 *            - IASATIfStatement
	 */
	private void processIfStatement(IASTIfStatement ifs)
	{

		OMPCFGMaker ift = new OMPCFGMaker(ifs.getThenClause());
		OMPCFGMaker ife = new OMPCFGMaker(ifs.getElseClause());
		ift.buildCFG();
		// System.out.println("OMPCFGMaker-----------------------------Else");
		ife.buildCFG();
		// System.out.println("OMPCFGMaker-----------------------------Endif");
		Chain thenChain = ift.getResult().getFirstChain();
		Chain elseChain = ife.getResult().getFirstChain();

		// The OMP CFG elides a lot of conditionals to save expense in analysis
		// See if there is any reason to retain the structure, if not just add
		// the statement to the current basic block
		if (!getRelevance(ift.getResult()) && !getRelevance(ife.getResult())) {
			addToBasicBlock(ifs);
			return;
		}

		addBasicBlockBranch(ifs.getConditionExpression(), OMPBasicBlock.IfBranch);
		if (traceOn_)
			System.out.println("CFGFactory: Create IF BranchNode");

		currentNode_.connectTo(thenChain.getHeadNode());
		currentNode_.connectTo(elseChain.getHeadNode());
		// Build a join node to collect results - makes things easier
		OMPBasicBlock jn = new OMPBasicBlock();
		jn.setPragmaContext(getLastPragma());
		if (traceOn_)
			System.out.println("CFGFactory: Create JoinNode");
		// connect what they go through
		if (thenChain.getTerminationReason() == OMPCFGResult.TermForward)
			thenChain.getTailNode().connectTo(jn);
		if (elseChain.getTerminationReason() == OMPCFGResult.TermForward)
			elseChain.getTailNode().connectTo(jn);

		// merge the nested results into the higher results
		// note that IF does not settle any unresolved issues
		result_.merge(ift.getResult());
		result_.merge(ife.getResult());

		// add the join to the chain
		if (jn.hasPredecessors()) // only if used
			addToChain(jn, false);
	}

	/**
	 * processSwitchStatement - build the cfg for a switch statement
	 * 
	 * @param iss
	 *            - IASTSwitchStatement
	 */
	private void processSwitchStatement(IASTSwitchStatement iss)
	{
		IASTStatement body = iss.getBody();

		OMPCFGMaker caseBodyFactory = new OMPCFGMaker(body);
		caseBodyFactory.buildCFG();
		OMPCFGResult cBody = caseBodyFactory.getResult();

		// We try to complete the structure to get rid of all breakages
		// If there are none, and no barriers, we can just fold the whole switch into the case statement.
		// otherwise, we attach the whole mess to the cfg.
		//
		// We expect a set of chains.
		// In each chain, we will look for nodes that begin with a case statement.
		// If they end in a break or return, they are distinct paths in the case
		// otherwise they merge into each other.
		//
		// Build first, token basic blocks for the split and join
		OMPBasicBlock bn = new OMPBasicBlock();
		bn.setPragmaContext(getLastPragma());
		OMPBasicBlock jn = new OMPBasicBlock();
		jn.setPragmaContext(getLastPragma());
		bn.setBranchingExpression(iss.getControllerExpression(), OMPBasicBlock.SwitchBranch);
		LinkedList chains = cBody.getChains();
		for (Iterator i = chains.iterator(); i.hasNext();) {
			Chain chain = (Chain) i.next();
			OMPCFGNode p = chain.getHeadNode();
			while (p != null) {
				if (p instanceof OMPBasicBlock) {
					if (((OMPBasicBlock) p).isCase())
						bn.connectTo(p);
				}
				p = p.getSuccessor();
			}
		}

		// for all the breaks, look at unresolved control flow list and deal with it
		LinkedList ucf = cBody.getUnresolvedControlFlow();
		Object[] ucfList = ucf.toArray(); // need this form instead of iterator, due to removal conflict
		for (int i = 0; i < ucfList.length; i++) {
			Chain urcf = (Chain) ucfList[i];
			if (urcf.getTerminationReason() == OMPCFGResult.TermBreak) {
				urcf.getTailNode().connectTo(jn);
				cBody.removeUnresolvedControlFlow(urcf);
			}
		}

		// Now we see if we have anything of interest in this statement, if not, just fold it into the BB
		if (!getRelevance(cBody)) {
			addToBasicBlock(iss);
			return;
		}

		// We need this structure, fold into the exising block structure
		// merge results for all chains
		result_.merge(cBody);

		// add to current chain
		addToChain(bn, true);
		// the join does not follow the branch directly - there is stuff inbetween, make it the current node
		addToChain(jn, false);
	}

	/**
	 * processWhileStatement - process the while statement into cfg
	 * 
	 * @param whileStmt
	 *            - IASTWhileStatement
	 */
	private void processWhileStatement(IASTWhileStatement whileStmt)
	{
		IASTExpression condition = whileStmt.getCondition();
		IASTStatement body = whileStmt.getBody();

		OMPCFGMaker bodyBuilder = new OMPCFGMaker(body);
		bodyBuilder.buildCFG();
		OMPCFGResult cBody = bodyBuilder.getResult();

		// As in other stmts, build it all, do a check if we should fold in,
		// otherwise, use the nodes
		OMPBasicBlock bn = new OMPBasicBlock();
		bn.setPragmaContext(getLastPragma());
		bn.setBranchingExpression(condition, OMPBasicBlock.WhileBranch);
		OMPBasicBlock jn = new OMPBasicBlock();
		jn.setPragmaContext(getLastPragma());

		// Build the control flow
		bn.connectTo(jn);
		bn.connectTo(cBody.getFirstChain().getHeadNode());
		cBody.getLastChain().getTailNode().connectTo(bn);

		// for all the breaks and continues, look at unresolved control flow list and deal with it
		LinkedList ucf = cBody.getUnresolvedControlFlow();
		for (Iterator i = ucf.iterator(); i.hasNext();) {
			Chain urcf = (Chain) i.next();
			if (urcf.getTerminationReason() == OMPCFGResult.TermBreak) {
				urcf.getTailNode().connectTo(jn);
				cBody.removeUnresolvedControlFlow(urcf);
			}
			else if (urcf.getTerminationReason() == OMPCFGResult.TermContinue) {
				urcf.getTailNode().connectTo(bn);
				cBody.removeUnresolvedControlFlow(urcf);
			}
		}

		// Now we see if we have anything of interest in this statement, if not, just fold it into the BB
		if (!getRelevance(cBody)) {
			addToBasicBlock(whileStmt);
			return;
		}

		// merge results for all chains
		result_.merge(cBody);

		// add to current chain
		addToChain(bn, true);
		addToChain(jn, false);
	}

	/**
	 * processDoStatement - process Do statement into cfg
	 * 
	 * @param doStmt
	 *            - IASTDoStatement
	 */
	private void processDoStatement(IASTDoStatement doStmt)
	{
		IASTExpression condition = doStmt.getCondition();
		IASTStatement body = doStmt.getBody();

		OMPBasicBlock bn = new OMPBasicBlock();
		bn.setPragmaContext(getLastPragma());
		bn.setBranchingExpression(condition, OMPBasicBlock.DoBranch);
		OMPBasicBlock jn = new OMPBasicBlock();
		jn.setPragmaContext(getLastPragma());

		OMPCFGMaker bodyBuilder = new OMPCFGMaker(body);
		bodyBuilder.buildCFG();
		OMPCFGResult cBody = bodyBuilder.getResult();

		// Build the graph [ we still put the join right after branch, to have a "break" target
		cBody.getLastChain().getTailNode().connectTo(bn);
		bn.connectTo(cBody.getFirstChain().getHeadNode());
		bn.connectTo(jn);

		// Now we see if we have anything of interest in this statement, if not, just fold it into the BB
		if (!getRelevance(cBody)) {
			addToBasicBlock(doStmt);
			return;
		}

		// merge results for all chains
		result_.merge(cBody);

		// add to current chain
		addToChain(cBody.getFirstChain().getHeadNode(), true);
		addToChain(jn, true);
	}

	/**
	 * processCaseStatement - Start a new basic block beginning with this case
	 * 
	 * @param ics
	 *            - IASTCaseStatement
	 */
	private void processCaseStatement(IASTCaseStatement ics)
	{
		// Create a new basic block; but not a new chain
		OMPBasicBlock b = new OMPBasicBlock();
		b.setPragmaContext(getLastPragma());
		addToChain(b);
		if (traceOn_)
			System.out.println("CFGFactory: Create BasicBlock due to case statement");
		((OMPBasicBlock) currentNode_).addStatement(ics);
	}

	/**
	 * processForStatement - build the graph for the For statement
	 * 
	 * @param ifs
	 */
	private void processForStatement(IASTForStatement forStmt)
	{
		// TODO: there are cases where any of the following can be empty - to handle
		IASTStatement initializer = forStmt.getInitializerStatement();
		IASTExpression condition = forStmt.getConditionExpression();
		IASTExpression iteration = forStmt.getIterationExpression();
		IASTStatement body = forStmt.getBody();

		// We will build the final result, than analyse if we actually need this
		OMPBasicBlock bn = new OMPBasicBlock();
		bn.setBranchingExpression(condition, OMPBasicBlock.ForBranch);
		OMPBasicBlock jn = new OMPBasicBlock();

		OMPCFGMaker initBuilder = new OMPCFGMaker(initializer);
		initBuilder.buildCFG();
		OMPCFGResult cInit = initBuilder.getResult();

		OMPCFGMaker forBodyFactory = new OMPCFGMaker(body);
		forBodyFactory.buildCFG();
		OMPCFGResult cBody = forBodyFactory.getResult();

		OMPCFGMaker iterBodyFactory = new OMPCFGMaker(iteration);
		iterBodyFactory.buildCFG();
		OMPCFGResult cIter = iterBodyFactory.getResult();

		// Build the primary structure of the loop
		cInit.getLastChain().getTailNode().connectTo(bn);
		bn.connectTo(jn);
		bn.connectTo(cBody.getFirstChain().getHeadNode());
		cBody.getLastChain().getTailNode().connectTo(cIter.getFirstChain().getHeadNode());
		cIter.getLastChain().getTailNode().connectTo(bn);

		// for all the breaks and continues, look at unresolved control flow list and deal with it
		LinkedList ucf = cBody.getUnresolvedControlFlow();
		Object[] uList = ucf.toArray();
		for (int i = 0; i < uList.length; i++) {
			Chain urcf = (Chain) uList[i];
			if (urcf.getTerminationReason() == OMPCFGResult.TermBreak) {
				urcf.getTailNode().connectTo(jn);
				cBody.removeUnresolvedControlFlow(urcf);
			}
			else if (urcf.getTerminationReason() == OMPCFGResult.TermContinue) {
				urcf.getTailNode().connectTo(cIter.getFirstChain().getHeadNode());
				cBody.removeUnresolvedControlFlow(urcf);
			}
		}

		// Now we check if we are all resolved - only check the body
		if (!getRelevance(cBody)) {
			addToBasicBlock(forStmt);
			return;
		}

		// Otherwise we add all this into the control flow graph

		// merge results for all chains
		result_.merge(cInit);
		result_.merge(cIter);
		result_.merge(cBody);

		// add to current chain
		addToChain(cInit.getFirstChain().getHeadNode(), true);
		addToChain(bn, true);
		addToChain(jn, false); // bn already connected to jn
	}

	/**
	 * getRelevance - Look at result for existence of pragmas, or
	 * of labels, or of unresolved control flow
	 * 
	 * @param result
	 *            - OMPCFGResult
	 * @return - boolean (true means any of the above exist
	 */
	protected boolean getRelevance(OMPCFGResult result)
	{
		boolean tf = (result.getNumberOfPragmas() != 0 ||
				result.getNumberOfLabels() != 0 ||
				result.getUnresolvedControlFlow().size() != 0);
		return tf;
	}

	protected OMPPragmaNode getLastPragma()
	{
		return (pragmaContext_.isEmpty() ? null : (OMPPragmaNode) pragmaContext_.peek());
	}

	/**
	 * addToBasicBlock - add a statement to the basic block
	 * 
	 * @param statement
	 *            - IASTStatement
	 */
	private void addToBasicBlock(IASTStatement statement)
	{
		// default case, we are either adding a statement to a current basic block
		if (currentNode_ == null || !(currentNode_ instanceof OMPBasicBlock)) {
			OMPBasicBlock b = new OMPBasicBlock();
			b.setPragmaContext(getLastPragma());
			addToChain(b);
			// currentNode_ = new BasicBlock();
			if (traceOn_)
				System.out.println("CFGFactory: Create BasicBlock");
			// firstNode_ = currentNode_;
		}
		((OMPBasicBlock) currentNode_).addStatement(statement);
	}

	private void addBasicBlockBranch(IASTExpression expression, int type)
	{
		// Add a new BB if the current is null, or is not a basic block
		if (currentNode_ == null || !(currentNode_ instanceof OMPBasicBlock)) {
			addToChain(new OMPBasicBlock(), true);
			if (traceOn_)
				System.out.println("CFGFactory: Create BasicBlock");
		}
		assert (((OMPBasicBlock) currentNode_).getBranchingExpression() == null);
		((OMPBasicBlock) currentNode_).setBranchingExpression(expression, type);
	}

	/**
	 * addToCurrentChain - add the CFGNode to the current chain
	 * 
	 * @param node
	 *            - OMPCFGNode
	 * @param connect
	 *            - connect to prior node
	 */
	private void addToChain(OMPCFGNode node, boolean connect)
	{
		if (firstNode_ == null) {
			firstNode_ = node;
			currentNode_ = node;
		}
		else {
			if (connect)
				currentNode_.connectTo(node);
			currentNode_ = node;
		}
	}

	/**
	 * addToChain - short hand to add and connect to chain
	 * 
	 * @param node
	 */
	private void addToChain(OMPCFGNode node) {
		addToChain(node, true);
	}

	private void addToBasicBlockAndChain(IASTStatement statement, int termReason)
	{
		addToBasicBlock(statement);
		Chain chain = result_.addChain(firstNode_, currentNode_, termReason, statement);
		if (termReason == OMPCFGResult.TermBreak || termReason == OMPCFGResult.TermContinue ||
				termReason == OMPCFGResult.TermReturn)
			result_.addUnresolvedControlFlow(chain);
		currentNode_ = null;
		firstNode_ = null;
	}

}
