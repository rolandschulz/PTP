/**********************************************************************
 * Copyright (c) 2007,2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.mpi.analysis.analysis;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousExpression;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousStatement;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.BarrierExpression.BarrierExpressionOP;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.ICallGraph;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.ICallGraphNode;

/**
 * MPIBarrierExpr is an ASTVisitor
 * @author beth
 *
 */
public class MPIBarrierExpr extends ASTVisitor {
	protected BarrierTable bTable_;
	protected ICallGraph cg_;
	/** One stack for each communicator */
	protected Hashtable<String,Stack<BarrierExpression>> stacks_; 
	
	protected MPICallGraphNode currentNode_;
	private static final boolean traceOn=false;
	
	/**
	 * switch statement may be nested 
	 */
	protected int depth = 0;
	
	/**
	 * This field is used to recognize the associated case body. The idea is 
	 * the case body should have the same parent with the "case" statement. 
	 * One element in this stack is shared by all cases (and default) in one 
	 * switch statement. This is possible because of the tree structure. 
	 */
	protected Stack<IASTNode> caseParent = null;
	
	/**
	 * This field is used to record whether the current case has "break"
	 * statement. Same as above, one element is shared by all cases and default 
	 * in a switch statement.
	 */
	protected Stack<Boolean> withBreak = null;
	
	/**
	 *  Each bucket in this hashtable is a List, hashed by communicator.
	 * The List records the current barrier 
	 * expression for each case statement in the current (nested)switch statement.
	 * So: Hashtable(for each communicator) -->
	 *     Stack(for each nested switch statement) -->
	 *     ArrayList(for each case body) --> 
	 *     Tuple(caseStatement, CaseBarrierExpr) 
	 */
	protected Hashtable<String,Stack<List>> caseBE = null;
	
	public MPIBarrierExpr(BarrierTable btable, ICallGraph cg){
		bTable_ = btable;
		cg_ = cg;
	}
	
	private void init(){
		stacks_ = new Hashtable<String,Stack<BarrierExpression>>();
		for(Enumeration<String> e = bTable_.getTable().keys(); e.hasMoreElements();){
			String comm = e.nextElement();
			stacks_.put(comm, new Stack<BarrierExpression>());
		}
		
		depth = 0;
		caseParent = new Stack<IASTNode>();
		withBreak = new Stack<Boolean>();
		caseBE = new Hashtable<String,Stack<List>>();
		for(Enumeration<String> e = bTable_.getTable().keys(); e.hasMoreElements();){
			String comm = e.nextElement();
			caseBE.put(comm, new Stack<List>());
		}
	}
	
	
	public void run(){
		if(bTable_.isEmpty()) return;
		this.shouldVisitExpressions = true;
		this.shouldVisitStatements = true;
		this.shouldVisitDeclarations = true;
		for(ICallGraphNode n = cg_.botEntry(); n != null; n = n.botNext()){
			currentNode_ = (MPICallGraphNode)n;
			if(!currentNode_.marked) continue;
			if(!currentNode_.barrierRelated()) continue;
			//System.out.println("Barrier related function " + currentNode_.getFuncName());
			init();
			IASTFunctionDefinition func = currentNode_.getFuncDef();
			func.accept(this);
			for(Enumeration<String> e = stacks_.keys(); e.hasMoreElements();){
				String comm = e.nextElement();
				Stack<BarrierExpression> sk = stacks_.get(comm);
				BarrierExpression be = sk.pop();
				if(traceOn)System.out.println(currentNode_.getFuncName() + "(" + comm + "): " + be.prettyPrinter()); //$NON-NLS-1$ //$NON-NLS-2$
				if(traceOn)System.out.println(" "); //$NON-NLS-1$
				currentNode_.setBarrierExpr(comm, be);
			}
		}
		/*
		if(traceOn)System.out.println("Total number of repeat subtrees: " + BarrierExpression.count_repeat);
		if(traceOn)System.out.println("Total number of branch subtrees: " + BarrierExpression.count_branch);
		*/
		if(traceOn)System.out.println("Total number of nodes: " + BarrierExpression.count_node); //$NON-NLS-1$
		/*
		BarrierExpression.count_branch = 0;
		BarrierExpression.count_repeat = 0;
		*/
	}
	
	public int visit(IASTStatement stmt){
		if(stmt instanceof IASTSwitchStatement){
			for(Enumeration<Stack<List>> e = caseBE.elements(); e.hasMoreElements();){
				Stack<List> caseBEsk = e.nextElement();
				caseBEsk.push(new ArrayList<Object>());
			}
			withBreak.push(new Boolean(false));
			depth ++;
		}
		return PROCESS_CONTINUE;
	}
	
	public int leave(IASTStatement stmt){
		BarrierExpression be = null;
		BarrierExpression operand1 = null;
		BarrierExpression operand2 = null;
		BarrierExpression condBE = null;
		
		if(stmt instanceof IASTAmbiguousStatement){
			
		}
		else if(stmt instanceof IASTBreakStatement){
			IASTBreakStatement bkStmt = (IASTBreakStatement)stmt;
			if(inCaseStmt(bkStmt)){
				for(Enumeration<String> e = caseBE.keys(); e.hasMoreElements();){
					String comm = e.nextElement();
					Stack<List> caseBEsk = caseBE.get(comm);
					Stack<BarrierExpression> sk = stacks_.get(comm);
					be = new BarrierExpression(BarrierExpression.BE_bot);
					sk.push(be);
					List list = caseBEsk.peek();
					for(int i=1; i<list.size(); i+=2){
						CaseBarrierExpr cbe = (CaseBarrierExpr)list.get(i);
						cbe.close();
					}
				}
			}
			else{
				for(Enumeration<String> e = caseBE.keys(); e.hasMoreElements();){
					String comm = e.nextElement();
					Stack<BarrierExpression> sk = stacks_.get(comm);
					be = new BarrierExpression(BarrierExpression.BE_bot);
					sk.push(be);
				}
			}
		}
		else if(stmt instanceof IASTCaseStatement){
			IASTCaseStatement caseStmt = (IASTCaseStatement)stmt;
			/* Override the old parent if it exists */
			if(!caseParent.empty()) caseParent.pop();
			caseParent.push(caseStmt.getParent());
			
			for(Enumeration e = caseBE.keys(); e.hasMoreElements();){
				String comm = (String)e.nextElement();
				Stack<List> caseBEsk = caseBE.get(comm);
				Stack<BarrierExpression> sk = stacks_.get(comm);
				be = new BarrierExpression(BarrierExpression.BE_bot);
				sk.push(be);
				List list = caseBEsk.peek();
				list.add(caseStmt);
				list.add(new CaseBarrierExpr(be, caseStmt.getExpression()));
			}
			
			/* Toggle the withBreak sign of the previous case */
			if(!withBreak.empty()) withBreak.pop();
			withBreak.push(new Boolean(false));
		}
		else if(stmt instanceof IASTCompoundStatement){
			IASTCompoundStatement cmpStmt = (IASTCompoundStatement)stmt;
			IASTStatement [] s = cmpStmt.getStatements();
			if(s.length == 0) return PROCESS_CONTINUE;

			for(Enumeration<String> e = stacks_.keys(); e.hasMoreElements();){
				String comm = e.nextElement();
				Stack<BarrierExpression> sk = stacks_.get(comm);
				/* The concatenate operator is left-associative */
				int count = 0;
				int i;
				BarrierExpression[] BElist = new BarrierExpression[s.length];
				for(i=0; i<s.length; i++){
					if(s[i] == null) continue;
					BElist[count] = sk.pop();
					count ++;
				}
				operand1 = BElist[count-1];
				for (i = count-2; i >= 0; i-- ) {
					operand2 = BElist[i];
					be = BarrierExpression.concatBE(operand1, operand2);
					operand1 = be;
				}
				sk.push(operand1);
				fixSwitch(comm, cmpStmt, operand1);
			}
		}
		else if(stmt instanceof IASTContinueStatement){
			for(Enumeration<String> e = caseBE.keys(); e.hasMoreElements();){
				String comm = e.nextElement();
				Stack<BarrierExpression> sk = stacks_.get(comm);
				be = new BarrierExpression(BarrierExpression.BE_bot);
				sk.push(be);
			}
		}
		else if(stmt instanceof IASTDeclarationStatement){
			boolean initialized = false;
			IASTDeclarationStatement declStmt = (IASTDeclarationStatement)stmt;
			IASTDeclaration decl = declStmt.getDeclaration();
			if(decl instanceof IASTSimpleDeclaration){
				IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration)decl;
				IASTDeclarator[] declarators = simpleDecl.getDeclarators();
				for(int i=0; i<declarators.length; i++){
					IASTInitializer init = declarators[i].getInitializer();
					if(init != null){
						initialized = true;
						for(Enumeration<String> e = stacks_.keys(); e.hasMoreElements();){
							String comm = e.nextElement();
							Stack<BarrierExpression> sk = stacks_.get(comm);
							be = getInitializerBE(sk, init);
							sk.push(be);
						}
					}
				}
			}
			if(!initialized){
				for(Enumeration<String> e = stacks_.keys(); e.hasMoreElements();){
					String comm = e.nextElement();
					Stack<BarrierExpression> sk = stacks_.get(comm);
					be = new BarrierExpression(BarrierExpression.BE_bot);
					sk.push(be);
				}
			}
		}
		else if(stmt instanceof IASTDefaultStatement){
			/* DefaultStatement = CaseStatement + BreakStatement */
			IASTDefaultStatement dfStmt = (IASTDefaultStatement)stmt;
			/* Override the old parent if it exists */
			if(!caseParent.empty()) caseParent.pop();
			caseParent.push(dfStmt.getParent());
			
			for(Enumeration<String> e = caseBE.keys(); e.hasMoreElements();){
				String comm = e.nextElement();
				Stack<List> caseBEsk = caseBE.get(comm);
				Stack<BarrierExpression> sk = stacks_.get(comm);
				be = new BarrierExpression(BarrierExpression.BE_bot);
				List list = caseBEsk.peek();
				list.add(dfStmt);
				list.add(new CaseBarrierExpr(be, null));
			}
		}
		else if(stmt instanceof IASTDoStatement){
			IASTDoStatement doStmt = (IASTDoStatement)stmt;
			for(Enumeration<String> e = stacks_.keys(); e.hasMoreElements();){
				String comm = e.nextElement();
				Stack<BarrierExpression> sk = stacks_.get(comm);
				/* condition */
				if(doStmt.getCondition() != null)
					condBE = (BarrierExpression)sk.pop();
				else
					condBE = new BarrierExpression(BarrierExpression.BE_bot);
				/* loop body */
				if(doStmt.getBody() != null)
					operand1 = (BarrierExpression)sk.pop();
				else
					operand1 = new BarrierExpression(BarrierExpression.BE_bot);
				/* BE = body . cond. (body . cond)* */
				be = BarrierExpression.concatBE(operand1, condBE, 
						BarrierExpression.repeatBE(
							BarrierExpression.concatBE(operand1, condBE), 
								doStmt.getCondition(), stmt));
				sk.push(be);
				fixSwitch(comm, stmt, be);
			}
		}
		else if(stmt instanceof IASTExpressionStatement){
			for(Enumeration<String> e = stacks_.keys(); e.hasMoreElements();){
				String comm = e.nextElement();
				Stack<BarrierExpression> sk = stacks_.get(comm);
				be = sk.pop();
				sk.push(be);
				fixSwitch(comm, stmt, be);
			}
		}
		else if(stmt instanceof IASTForStatement){
			IASTForStatement forStmt = (IASTForStatement)stmt;
			BarrierExpression initBE = null;
			BarrierExpression iterBE = null;
			for(Enumeration<String> e = stacks_.keys(); e.hasMoreElements();){
				String comm = e.nextElement();
				Stack<BarrierExpression> sk = stacks_.get(comm);
				/* loop body */
				if(forStmt.getBody() != null)
					operand1 = sk.pop();
				else
					operand1 = new BarrierExpression(BarrierExpression.BE_bot);
				/* iterator */
				if(forStmt.getIterationExpression() != null)
					iterBE = sk.pop();
				else
					iterBE = new BarrierExpression(BarrierExpression.BE_bot);
				/* condition */
				if(forStmt.getConditionExpression() != null)
					condBE = sk.pop();
				else
					condBE = new BarrierExpression(BarrierExpression.BE_bot);
				/* initializer */
				if(forStmt.getInitializerStatement() != null)
					initBE = sk.pop();
				else
					initBE = new BarrierExpression(BarrierExpression.BE_bot);
				/* BE = init . cond. (body . iter . cond)* */
				be = BarrierExpression.concatBE(initBE, condBE, 
						BarrierExpression.repeatBE(
							BarrierExpression.concatBE(operand1, iterBE, condBE), 
							forStmt.getConditionExpression(), stmt));
				sk.push(be);
				fixSwitch(comm, stmt, be);
			}
		} 
		else if(stmt instanceof IASTGotoStatement){
			/* TODO */
		}
		else if(stmt instanceof IASTIfStatement){
			IASTIfStatement ifStmt = (IASTIfStatement)stmt;
			for(Enumeration<String> e = stacks_.keys(); e.hasMoreElements();){
				String comm = e.nextElement();
				Stack<BarrierExpression> sk = stacks_.get(comm);
				/* else clause */
				if(ifStmt.getElseClause() != null)
					operand2 = sk.pop();
				else 
					operand2 = new BarrierExpression(BarrierExpression.BE_bot);
				/* then clause */
				if(ifStmt.getThenClause() != null)
					operand1 = sk.pop();
				else
					operand1 = new BarrierExpression(BarrierExpression.BE_bot);
				/* condition*/
				if(ifStmt.getConditionExpression() != null)
					condBE = sk.pop();
				else
					condBE = new BarrierExpression(BarrierExpression.BE_bot);
				/* BE = cond. (then | else) */
				be = BarrierExpression.concatBE(condBE, 
						BarrierExpression.branchBE(operand1, operand2, 
							ifStmt.getConditionExpression(), stmt));
				sk.push(be); 		
				fixSwitch(comm, stmt, be);
			}
		}
		else if(stmt instanceof IASTLabelStatement){
			/* TODO */
		}
		else if(stmt instanceof IASTNullStatement){
			for(Enumeration<Stack<BarrierExpression>> e = stacks_.elements(); e.hasMoreElements();){
				Stack<BarrierExpression> sk = e.nextElement();
				be = new BarrierExpression(BarrierExpression.BE_bot);
				sk.push(be);
			}
		}
		else if(stmt instanceof IASTProblemStatement){
			for(Enumeration<Stack<BarrierExpression>> e = stacks_.elements(); e.hasMoreElements();){
				Stack<BarrierExpression> sk = e.nextElement();
				be = new BarrierExpression(BarrierExpression.BE_bot);
				sk.push(be);
			}
		}
		else if(stmt instanceof IASTReturnStatement){
			IASTReturnStatement rStmt = (IASTReturnStatement)stmt;
			if(rStmt.getReturnValue() == null){
				for(Enumeration<Stack<BarrierExpression>> e = stacks_.elements(); e.hasMoreElements();){
					Stack<BarrierExpression> sk = e.nextElement();
					be = new BarrierExpression(BarrierExpression.BE_bot);
					sk.push(be);
				}
			} else {
				for(Enumeration<String> e = caseBE.keys(); e.hasMoreElements();){
					String comm = e.nextElement();
					Stack<BarrierExpression> sk = stacks_.get(comm);
					be = sk.pop();
					sk.push(be);
					if(!withBreak.empty()){
						withBreak.pop();
						withBreak.push(new Boolean(true));
					}
					fixSwitch(comm, rStmt, be);
				}
			}
		}
		else if(stmt instanceof IASTSwitchStatement){
			IASTSwitchStatement swStmt = (IASTSwitchStatement)stmt;
			for(Enumeration<String> e = caseBE.keys(); e.hasMoreElements();){
				String comm = e.nextElement();
				Stack<List> caseBEsk = caseBE.get(comm);
				List list = caseBEsk.pop();
				CaseBarrierExpr cbe = (CaseBarrierExpr)list.get(1);
				operand1 = (BarrierExpression)cbe.getBE();
				for(int i=3; i<list.size(); i+=2){
					cbe = (CaseBarrierExpr)list.get(i);
					operand2 = (BarrierExpression)cbe.getBE();
					be = BarrierExpression.branchBE(operand1, operand2, 
							swStmt.getControllerExpression(), stmt);
					operand1 = be;
				}
				Stack<BarrierExpression> sk = stacks_.get(comm);
				sk.pop();
				sk.push(operand1);
			}
			withBreak.pop();
			caseParent.pop();
			depth --;
		}
		else if(stmt instanceof IASTWhileStatement){
			IASTWhileStatement whStmt = (IASTWhileStatement)stmt;
			for(Enumeration<String> e = stacks_.keys(); e.hasMoreElements();){
				String comm = e.nextElement();
				Stack<BarrierExpression> sk = stacks_.get(comm);
				/* loop body */
				if(whStmt.getBody() != null)
					operand1 = sk.pop();
				else
					operand1 = new BarrierExpression(BarrierExpression.BE_bot);
				/* condition */
				if(whStmt.getCondition() != null) 
					condBE = sk.pop();
				else
					condBE = new BarrierExpression(BarrierExpression.BE_bot);
				/* BE = cond . (body . cond)*  */
				be = BarrierExpression.concatBE(condBE, 
						BarrierExpression.repeatBE(
							BarrierExpression.concatBE(operand1, condBE), 
								whStmt.getCondition(), stmt));
				sk.push(be);
				fixSwitch(comm, stmt, be);
			}
		}

		return PROCESS_CONTINUE;
	}
	
	/** 
	 * If the current statement is a case body
	 */
	private void fixSwitch(String key, IASTStatement stmt, BarrierExpression BE){
		if(depth <= 0) return;
		if(stmt.getParent() == caseParent.peek()){
			Stack<List> caseBEsk = caseBE.get(key);
			List list = caseBEsk.peek();
			boolean flag = ((Boolean)withBreak.peek()).booleanValue();
			for(int i = 1; i<list.size(); i+=2){
				CaseBarrierExpr cbe = (CaseBarrierExpr)list.get(i);
				if(flag)
					cbe.addFinalBEElement(BE);
				else 
					cbe.addBEElement(BE);
			}
		}
	}
	
	private boolean inCaseStmt(IASTBreakStatement stmt){
		IASTNode parent = stmt.getParent();
		while(parent != null){
			if(parent instanceof IASTForStatement ||
			   parent instanceof IASTDoStatement ||
			   parent instanceof IASTWhileStatement)
				return false;
			else if(parent instanceof IASTSwitchStatement)
				return true;
			else if(parent instanceof IASTFunctionDefinition)
				return false;
			else
				parent = parent.getParent();
		}
		return false;
	}
	
	private BarrierExpression getInitializerBE(Stack sk, IASTInitializer init){		
		BarrierExpression BE = null;
		if(init instanceof IASTEqualsInitializer){
			BE = (BarrierExpression)sk.pop();
		}
		else if(init instanceof IASTInitializerList){// BRT  !will this be encountered? 
			IASTInitializerList list = (IASTInitializerList)init;
			IASTInitializer[] inits = list.getInitializers();
			for(int j = 0; j<inits.length; j++){
				if(BE == null)
					BE = getInitializerBE(sk, inits[j]);
				else
					BE = BarrierExpression.concatBE(BE, 
							getInitializerBE(sk, inits[j]));
			}
		}
		return BE;
	}
	
	/**
	 * An expression which doesn't have any "expression" 
	 * field is a terminal. 
	 */
	public int leave(IASTExpression expr){
		BarrierExpression be = null;
		BarrierExpression operand1 = null;
		BarrierExpression operand2 = null;
		BarrierExpression operand3 = null;
		
		if(expr instanceof IASTAmbiguousExpression){
			/* nothing */
		}
		else if(expr instanceof IASTArraySubscriptExpression){
			for(Enumeration<Stack<BarrierExpression>> e = stacks_.elements(); e.hasMoreElements();){
				Stack<BarrierExpression> sk = e.nextElement();
				operand2 = sk.pop();  //subscript
				operand1 = sk.pop();  //array
				be = BarrierExpression.concatBE(operand1, operand2);
				sk.push(be);
			}
		}
		else if(expr instanceof IASTBinaryExpression){
			for(Enumeration<Stack<BarrierExpression>> e = stacks_.elements(); e.hasMoreElements();){
				Stack<BarrierExpression> sk = e.nextElement();
				operand2 = sk.pop();
				operand1 = sk.pop();
				be = BarrierExpression.concatBE(operand1, operand2);
				sk.push(be);
			}
		}
		else if(expr instanceof IASTCastExpression){
			/* Has only one operator, leave it there */
		}
		else if(expr instanceof IASTConditionalExpression){
			IASTConditionalExpression cExpr = (IASTConditionalExpression)expr;
			IASTNode parent = expr.getParent();
			while(! (parent instanceof IASTStatement) )
				parent = parent.getParent();
			for(Enumeration<Stack<BarrierExpression>> e = stacks_.elements(); e.hasMoreElements();){
				Stack<BarrierExpression> sk = e.nextElement();
				operand2 = sk.pop(); /* negative */
				operand1 = sk.pop(); /* positive */
				operand3 = sk.pop(); /* condition */
				/* E = C ( P | N ) */
				be = BarrierExpression.concatBE(operand3, 
						BarrierExpression.branchBE(operand1, operand2, 
								cExpr.getLogicalConditionExpression(), 
								(IASTStatement)parent));
				sk.push(be);
			}
		}
		else if(expr instanceof IASTExpressionList){// BRT !this will not get executed!!  should it be hit ONLY for fn arg exprList? or not? can we do this elsewhere e.g. functionCallExpr?
			IASTExpressionList exprList = (IASTExpressionList)expr;
			IASTExpression[] exps = exprList.getExpressions();
			if(exps.length == 0) return PROCESS_CONTINUE;
			for(Enumeration<Stack<BarrierExpression>> e = stacks_.elements(); e.hasMoreElements();){
				Stack<BarrierExpression> sk = e.nextElement();
				int count = 0;
				int i;
				BarrierExpression[] BElist = new BarrierExpression[exps.length];
				for(i=0; i<exps.length; i++){
					if(exps[i] == null) continue;
					BElist[count] = sk.pop();
					count ++;
				}
				operand1 = BElist[count-1];
				for (i = count-2; i >= 0; i-- ) {
					operand2 = BElist[i];
					be = BarrierExpression.concatBE(operand1, operand2);
					operand1 = be;
				}
				sk.push(operand1);
			}
		}
		else if(expr instanceof IASTFieldReference){
			/*
			for(Enumeration e = stacks_.elements(); e.hasMoreElements();){
				Stack sk = (Stack)e.nextElement();
				operand2 = (BarrierExpression)sk.pop(); //name
				operand1 = (BarrierExpression)sk.pop(); //owner
				be = BarrierExpression.concatBE(operand1, operand2);
				sk.push(be);
			}
			*/
		}
		else if(expr instanceof IASTFunctionCallExpression){
			IASTFunctionCallExpression fExpr = (IASTFunctionCallExpression)expr;
			IASTExpression funcname = fExpr.getFunctionNameExpression();
			// IASTExpression parameter = fExpr.getParameterExpression(); // old 6.0 AST
			IASTInitializerClause[] arguments = fExpr.getArguments();  
			String signature = funcname.getRawSignature();
			int id = bTable_.isBarrier(fExpr);
			if(id != -1){ /* barrier */
				be = new BarrierExpression(id);
				String comm = bTable_.getComm(id);
				for(Enumeration<String> e = stacks_.keys(); e.hasMoreElements();){
					String commkey = e.nextElement();
					// get the BarrierExpressions for this communicator
					Stack<BarrierExpression> sk = stacks_.get(commkey);
					// if(parameter != null) sk.pop(); //parameter  // old 6.0 AST
					for(int i = 0; i<arguments.length; i ++) 
					{
						sk.pop();
					} 
					sk.pop(); //functionName 
					if(commkey.equals(comm)) sk.push(be);
					else sk.push(new BarrierExpression(BarrierExpression.BE_bot));
				}
			}
			else{
				MPICallGraphNode node = (MPICallGraphNode)cg_.getNode(currentNode_.getFileName(), signature);
				if(node != null && node.barrierRelated()){ 
					/* a function (directly or indirectly) with barriers */
					for(Enumeration<String> e = stacks_.keys(); e.hasMoreElements();){
						String comm = e.nextElement();
						Stack<BarrierExpression> sk = stacks_.get(comm);
						BarrierExpression funcBE = node.getBarrierExpr().get(comm);
						// if(parameter != null) sk.pop(); //parameter // old 6.0 AST
						for(int i = 0; i<arguments.length; i ++) 
						{
							sk.pop(); 
						}
						sk.pop(); //functionName 
						if(node == currentNode_){ //recursive functions
							be = new BarrierExpression(node.getFuncName());
						}
						else if(funcBE.isBot()){
							be = new BarrierExpression(BarrierExpression.BE_bot);
						} 
						else{
							be = new BarrierExpression(signature);
						}
						sk.push(be);
					}
				} else { //not a barrier related function
					be = new BarrierExpression(BarrierExpression.BE_bot);
					for(Enumeration<Stack<BarrierExpression>> e = stacks_.elements(); e.hasMoreElements();){
						Stack<BarrierExpression> sk = e.nextElement();
						// if(parameter != null) sk.pop(); //parameter // old 6.0 AST
						for(int i = 0; i<arguments.length; i ++) 
						{
							sk.pop();
						} 
						sk.pop(); //functionName  
						sk.push(be);
					}
				}
			}
		} // end IASTFunctionCallExpression
		else if(expr instanceof IASTIdExpression){ //terminal
			//System.out.println(((IASTIdExpression)expr).getName().toString());
			for(Enumeration<Stack<BarrierExpression>> e = stacks_.elements(); e.hasMoreElements();){
				Stack<BarrierExpression> sk = e.nextElement();
				be = new BarrierExpression(BarrierExpression.BE_bot);
				sk.push(be);
			}
		}
		else if(expr instanceof IASTLiteralExpression){ //terminal
			//System.out.println(((IASTLiteralExpression)expr).toString());
			for(Enumeration<Stack<BarrierExpression>> e = stacks_.elements(); e.hasMoreElements();){
				Stack<BarrierExpression> sk = e.nextElement();
				be = new BarrierExpression(BarrierExpression.BE_bot);
				sk.push(be);
			}
		}
		/*
		else if(expr instanceof IASTProblemExpression){
			System.out.println("IASTProblemExpression");
		}
		*/
		else if(expr instanceof IASTTypeIdExpression){
			for(Enumeration<Stack<BarrierExpression>> e = stacks_.elements(); e.hasMoreElements();){
				Stack<BarrierExpression> sk = e.nextElement();
				be = new BarrierExpression(BarrierExpression.BE_bot);
				sk.push(be);
			}
		}
		else if(expr instanceof IASTUnaryExpression){
			/* has only one operand, leave it there */
		}
		/*
		else if(expr instanceof ICASTTypeIdInitializerExpression){
			System.out.println("ICASTTypeIdInitializerExpression");
		}
		*/
		return PROCESS_CONTINUE;
	} // end leave()

	class CaseBarrierExpr{
		protected BarrierExpression BE;
		protected boolean closed;
		protected IASTExpression cond;
		
		CaseBarrierExpr(BarrierExpression be, IASTExpression cond){
			BE = be;
			closed = false;
			this.cond = cond;
		}
		
		public void close() { closed = true; }
		public BarrierExpression getBE() {return BE;}
		public IASTExpression getCond() {return cond;}

		public void addBEElement(BarrierExpression be){
			if(closed) return;
			if(BE == null){
				BE = be;
			} else {
				BE = BarrierExpression.concatBE(BE, be);
			}
		}
		
		public void addFinalBEElement(BarrierExpression be){
			addBEElement(be);
			close();
		}
	}
	
	protected void checkBarrierRecursion(){
		for(Iterator<List<ICallGraphNode>> i=cg_.getCycles().iterator(); i.hasNext();){
			List<ICallGraphNode> cycle = i.next();
			boolean barrierRelated = false;
			for(Iterator<ICallGraphNode> ii = cycle.iterator(); ii.hasNext();){
				MPICallGraphNode node = (MPICallGraphNode)ii.next();
				if(node.barrierRelated){
					barrierRelated = true;
					break;
				}
			}
			if(!barrierRelated) continue;
			if(cycle.size() > 1){
				System.out.println("Multi-Function barrier related cycles"); //$NON-NLS-1$
				return;
			}
			currentNode_ = (MPICallGraphNode)cycle.get(0);
			for(Enumeration<String> e = currentNode_.getBarrierExpr().keys(); e.hasMoreElements();){
				String comm = e.nextElement();
				BarrierExpression BE = currentNode_.getBarrierExpr().get(comm);
				if(recursion(BE) == recursionError){
					System.out.println("Recursion Error in " + comm); //$NON-NLS-1$
				}
			}
			
		}
	}
	
	protected final int recursionCorrect = 0;
	protected final int recursionError = 1;
	protected final int noRecursion = 2;
	
	protected int recursion(BarrierExpression BE){
		if(traceOn)System.out.println(BE.prettyPrinter());
		BarrierExpressionOP OP = BE.getOP();
		if(OP == null){
			if(BE.isFunc()){ //function call
				String funcName = BE.getFuncName();
				MPICallGraphNode fnode = (MPICallGraphNode)cg_.getNode(currentNode_.getFileName(), funcName);
				if(fnode == currentNode_)
					return recursionCorrect;
				else 
					return noRecursion;
			}
			else
				return noRecursion;
		}
		else if(OP.getOperator() == BarrierExpressionOP.op_concat){
			int v1 = recursion(BE.getOP1());
			int v2 = recursion(BE.getOP2());
			if(v1 == recursionError || v2 == recursionError)
				return recursionError;
			else if(v1 == recursionCorrect || v2 == recursionCorrect)
				return recursionCorrect;
			else 
				return noRecursion;

		}
		else if(OP.getOperator() == BarrierExpressionOP.op_branch){
			int v1 = recursion(BE.getOP1());
			int v2 = recursion(BE.getOP2());
			if(v1 == noRecursion && v2 == noRecursion)
				return noRecursion;
			else
				return recursionError;
		}
		else { //BarrierExpressionOP.op_repeat
			return recursion(BE.getOP1());

		}
	}


}
