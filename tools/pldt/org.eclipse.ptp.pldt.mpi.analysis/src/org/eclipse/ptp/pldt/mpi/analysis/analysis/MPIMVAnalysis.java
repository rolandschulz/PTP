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

package org.eclipse.ptp.pldt.mpi.analysis.analysis;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypeIdInitializerExpression;
import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousExpression;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.IBlock;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.ICallGraph;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.ICallGraphNode;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.IControlFlowGraph;

/**
 * 
 * @author zhangyua
 *
 */
public class MPIMVAnalysis{
	protected ICallGraph cg_;
	protected MPICallGraphNode currentNode_;
	
	protected final int lhs = 0;
	protected final int rhs = 1;
	
	protected boolean changed = false;
	
	public MPIMVAnalysis(ICallGraph cg){
		cg_ = cg;
	}
	
	public void run(){
		init();
		SeedsCollector sc = new SeedsCollector();
		sc.run();
		functionMVSummary();
		functionSlicing();
		exprMVAnalysis();
		
		for(ICallGraphNode n = cg_.topEntry(); n != null; n = n.topNext()){
			MPICallGraphNode node = (MPICallGraphNode)n;
			if(!node.marked) continue;
			IControlFlowGraph cfg = node.getCFG();
			for(IBlock b = cfg.getEntry(); b != null; b = b.topNext()){
				MPIBlock block = (MPIBlock)b;
				if(block.getMV() && (block.withBreak || block.withContinue))
					System.out.println("************  Multi-valued branch with Break/Continue in " + 
							node.getFuncName() + "(" + node.getFileName() + ")");
			}
		}
	}

	/** Initialize all parameters of a function as single-valued; 
	 * Initialize its MV and SV summary as empty
	 */
	private void init(){
		for(ICallGraphNode n = cg_.topEntry(); n != null; n = n.topNext()){
			MPICallGraphNode node = (MPICallGraphNode)n;
			if(!node.marked) continue;
			IASTFunctionDefinition fd = node.getFuncDef();
			IASTFunctionDeclarator fdecl = fd.getDeclarator();
			if(fdecl instanceof IASTStandardFunctionDeclarator){
				IASTStandardFunctionDeclarator sfunc = (IASTStandardFunctionDeclarator)fdecl;
				IASTParameterDeclaration[] params = sfunc.getParameters();
				for(int i = 0; i < params.length; i++){
					IASTName param = params[i].getDeclarator().getName();
					if(param.toString().equals("")) continue; //void parameter
					node.getParamMV().put(param.toString(), new Boolean(false));
					node.getMVSummary().put(param.toString(), new ArrayList<String>());
				}
			} else {
				ICASTKnRFunctionDeclarator krfunc = (ICASTKnRFunctionDeclarator)fdecl;
				IASTName[] params = krfunc.getParameterNames();
				for(int i=0; i<params.length; i++){
					node.getParamMV().put(params[i].toString(), new Boolean(false));
					node.getMVSummary().put(params[i].toString(), new ArrayList<String>());
				}
			}
			for(Iterator<String> i = node.getGlobalUse().iterator(); i.hasNext();){
				String var = i.next();
				node.getMVSummary().put(var, new ArrayList<String>());
			}
			/* Another entry if (1) the function has no parameter and no global use;
			 * (2) all parameters and global uses are SV. 
			 * (We call this function(context) as "empty MV input" function(context).)
			 * Use the function name as the key in these cases
			 */ 
			node.getMVSummary().put(node.getFuncName(), new ArrayList<String>());
		}
	}
	
	private void functionMVSummary(){
		changed = true;
		while(changed){
			changed = false;
			for(ICallGraphNode n = cg_.botEntry(); n != null; n = n.botNext()){
				MPICallGraphNode node = (MPICallGraphNode)n;
				if(!node.marked) continue;
				currentNode_ = node;
				for(Enumeration<String> e = node.getMVSummary().keys(); e.hasMoreElements();){
					String var = e.nextElement();
					singleVariableSlicing(var);
					/*
					System.out.print("Summary: ");
					for(Iterator i = ((ArrayList)node.getMVSummary().get(var)).iterator(); i.hasNext();){
						System.out.print((String)i.next() + ", ");
					}
					System.out.println(" ");
					*/
					clean();
				}
			}
		}
	}
	
	/**
	 *  For each function, assume all parameters are MV, find out which global
	 * variables (and passable parameters) are MV
	 */
	private void singleVariableSlicing(String var){
		IControlFlowGraph cfg = currentNode_.getCFG();
		
		WorkListCollector wlc = new WorkListCollector(currentNode_);
		LinkedList<IBlock> seeds = wlc.getWorkList();
		if(!var.equals(currentNode_.getFuncName())){
			seeds.add(cfg.getEntry());
			((MPIBlock)cfg.getEntry()).getMVvar().add(var);
		}
		
		while(!seeds.isEmpty()){
			MPIBlock block = (MPIBlock)seeds.remove();
			if(block.sliced) continue;
			block.sliced = true;
			
			if(block.getContent() == null) continue;
			
			Hashtable<String,List<IBlock>> DUSucc = block.getDUSucc();
			List<String> mv = block.getMVvar();
			List<String> newMV = new ArrayList<String>();
			
			handlePointers(block, mv);
			
			if(!block.getPhiVar().isEmpty()){
				List<IBlock> cond = block.getCond();
				int blockcase = 0;
				for(Iterator<IBlock> i = cond.iterator(); i.hasNext();){
					if(blockcase == 2) break;
					MPIBlock condblock = (MPIBlock)i.next();
					IASTStatement parent = condblock.getParent();
					if(parent instanceof IASTIfStatement || 
							parent instanceof IASTSwitchStatement)
						blockcase = 1;
					else if(parent instanceof IASTForStatement ||
							parent instanceof IASTDoStatement ||
							parent instanceof IASTWhileStatement){
						if(block.getType() == MPIBlock.exit_join_type)
							blockcase = 1;
						else if(block.getType() == MPIBlock.expr_type)
							blockcase = 2;
					}
				}
				if(blockcase == 1){
					newMV = Util.Union(newMV, block.getPhiVar());
				}
				else if(blockcase == 2){
					for(Iterator<IBlock> i = cond.iterator(); i.hasNext();){
						MPIBlock condblock = (MPIBlock)i.next();
						for(Iterator<String> ii = condblock.getUsedPhiVar().iterator();
								ii.hasNext();){
							String v = ii.next();
							if(mv.contains(v))
								newMV.add(v);
						}
					}
				}
			}	
			
			/** Intra-block slicing (Case #1) --- 
			 * Given MV/SV used variable, determine whether defined variables
			 * are MV or SV.  
			 */
			
			IASTNode content = block.getContent();
			ExprMVAnalyzer emva = new ExprMVAnalyzer(content, mv, block);
			emva.run();
			newMV = Util.Union(newMV, emva.getMVList());
			
			/** Intra-block slicing (Case #3) -- 
			 *  any used only multi-valued variable is still multi-valued
			 */
			for(Iterator<String> i = mv.iterator(); i.hasNext();){
				String v = i.next();
				if(block.getUse().contains(v) && !block.getDef().contains(v)){
					if(!newMV.contains(v))
						newMV.add(v);
				}
			}
			
			handlePointers(block, newMV);
			handleBroadCast(block, newMV);
			block.setMVvar(newMV);
			
			/** Inter-block slicing based on data dependences */
			for(Iterator<String> i = newMV.iterator(); i.hasNext();){
				String MVvar = i.next();
				List<IBlock> DUnext = DUSucc.get(MVvar);
				if(DUnext != null){
					for(Iterator<IBlock> ii = DUnext.iterator(); ii.hasNext();){
						MPIBlock b = (MPIBlock)ii.next();
						if(b.sliced == true) continue;
						if(!seeds.contains(b)){
							seeds.add(b);
							//System.out.println("block " + b.getID() + " is added to seeds");
						}
						if(!b.getMVvar().contains(MVvar))
							b.getMVvar().add(MVvar);
					}
				}
			}
			
			/** Inter-block slicing based on \Phi edges */
			for(Iterator<IBlock> i = block.getJoin().iterator(); i.hasNext();){
				MPIBlock join = (MPIBlock)i.next();
				if(!seeds.contains(join) && !join.sliced) 
					seeds.add(join);
			}
		}
		
		List<String> MVlist = currentNode_.getMVSummary().get(var);
		for(Iterator<String> i = currentNode_.getGlobalDef().iterator(); i.hasNext();){
			String v = i.next();
			if(((MPIBlock)cfg.getExit()).getMVvar().contains(v)){
				if(!MVlist.contains(v)) MVlist.add(v);
			}
		}
		for(Iterator<String> i = currentNode_.getParamDef().iterator(); i.hasNext();){
			String v = (String)i.next();
			if(((MPIBlock)cfg.getExit()).getMVvar().contains(v)){
				if(!MVlist.contains(v)) MVlist.add(v);
			}
		}
		

		if(returnMV())
			if(!MVlist.contains(currentNode_.getFuncName()))
				MVlist.add(currentNode_.getFuncName());
	}

	/** The return value of a function is multi-valued if any of its 
	 * return statement is multi-valued.
	 */
	private boolean returnMV(){
		boolean returnmv = false;
		for(Iterator<IBlock> i = ((MPIControlFlowGraph)currentNode_.getCFG()).getReturnBlocks().iterator(); 
			i.hasNext();){
			MPIBlock returnBlock = (MPIBlock)i.next();
			ExprMVAnalyzer ema = new ExprMVAnalyzer(returnBlock.getContent(), 
												returnBlock.getMVvar(), returnBlock);
			ema.run();
			returnmv = returnmv | ema.isMV();
			returnBlock.setMV(false); //reset it
		}
		return returnmv;
	}

	
	private void handlePointers(MPIBlock block, List<String> set){
		PointerAnalyzer pa = new PointerAnalyzer(block);
		pa.run();
		for(Iterator<String> i = pa.getAddr().iterator(); i.hasNext();){
			String var = i.next();
			if(block.getUse().contains(var)){
				if(!set.contains(var)) set.add(var);
			}
		}
		for(Iterator<String> i = pa.getDeref().iterator(); i.hasNext();){
			String var = i.next();
			if(block.getUse().contains(var)){
				if(!set.contains(var)) set.add(var);
			}
		}
	}
	
	private void handleBroadCast(MPIBlock block, List<String> set){
		BroadCastAnalyzer bca = new BroadCastAnalyzer(block);
		bca.run();
		String bcdata = bca.getBCdata();
		if(bcdata != null){
			if(set.contains(bcdata))
				set.remove(bcdata);
		}
		block.setMVvar(set);
	}
	
	
	private void clean(){
		for(IBlock b = currentNode_.getCFG().getEntry(); b != null; b = b.topNext()){
			MPIBlock block = (MPIBlock)b;
			block.sliced = false;
			block.setMVvar(new ArrayList<String>());
		}
	}
	
	
	class ExprMVAnalyzer{
		private IASTStatement stmt_;
		private IASTExpression expr_;
		private List<String> context_;
		private MPIBlock currentBlock_;

		private List<String> MVvar_;
		private Stack<boolean[]> exprListMVContext_;
		
		private boolean value;
	
		public ExprMVAnalyzer(IASTStatement stmt, List<String> context, MPIBlock block){
			stmt_ = stmt;
			expr_ = null;
			context_ = context;
			currentBlock_ = block;
			MVvar_ = new ArrayList<String>();
			exprListMVContext_ = new Stack<boolean[]>();
			value = false;
		}
	
		public ExprMVAnalyzer(IASTExpression expr, List<String> context, MPIBlock block){
			stmt_ = null;
			expr_ = expr;
			context_ = context;
			currentBlock_ = block;
			MVvar_ = new ArrayList<String>();
			exprListMVContext_ = new Stack<boolean[]>();
			value = false;
		}
		
		public ExprMVAnalyzer(IASTNode node, List<String> context, MPIBlock block){
			if(node instanceof IASTExpression){
				stmt_ = null;
				expr_ = (IASTExpression)node;
			} 
			else if(node instanceof IASTStatement){
				stmt_ = (IASTStatement)node;
				expr_ = null;
			}
			context_ = context;
			currentBlock_ = block;
			MVvar_ = new ArrayList<String>();
			exprListMVContext_ = new Stack<boolean[]>();
			value = false;
		}
		
		public void run(){
			if(stmt_ != null){
				value = UseDefMVMapping(stmt_);
			}
			else if(expr_ != null){
				List<String> defset = new ArrayList<String>();
				value = useDefMVMapping(expr_, rhs, null, defset);
			}
		}
		
		public boolean isMV(){return value;}
		public List<String> getMVList() {return MVvar_;}
		
		private boolean UseDefMVMapping(IASTStatement stmt){
			if(stmt instanceof IASTDeclarationStatement){
				boolean value = false;
				IASTDeclarationStatement declStmt = (IASTDeclarationStatement)stmt;
				IASTDeclaration decl = declStmt.getDeclaration();
				if(decl instanceof IASTSimpleDeclaration){
					IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration)decl;
					IASTDeclarator[] declarators = simpleDecl.getDeclarators();
					for(int i=0; i<declarators.length; i++){
						boolean v1 = false;
						IASTInitializer init = declarators[i].getInitializer();
						if(init != null){
							IASTName name = declarators[i].getName();
							v1 = handleInitializer(init);
							if(v1){
								if(!MVvar_.contains(name.toString()))
									MVvar_.add(name.toString());
							}
						}
						value = value | v1;
					}
				}
				return value;
			}
			else if(stmt instanceof IASTExpressionStatement){
				IASTExpressionStatement exprS = (IASTExpressionStatement)stmt;
				IASTExpression expr = exprS.getExpression();
				List<String> mvlist = new ArrayList<String>();
				value = useDefMVMapping(expr, rhs, null, mvlist);
			}
			return false;
		}
		
		private boolean handleInitializer(IASTInitializer init){
			if(init instanceof IASTInitializerExpression){
				IASTInitializerExpression initE = (IASTInitializerExpression)init;
				IASTExpression expr = initE.getExpression();
				List<String> mvlist = new ArrayList<String>();
				return useDefMVMapping(expr, rhs, null, mvlist);
			}
			else if(init instanceof IASTInitializerList){
				IASTInitializerList initList = (IASTInitializerList)init;
				IASTInitializer[] list = initList.getInitializers();
				boolean listvalue = false;
				for(int i=0; i<list.length; i++){
					listvalue = listvalue | handleInitializer(list[i]);
				}
				return listvalue;
			}
			return false;
		}
		
		
		/** Return true if "expr" is multi_valued.
		 * "set" contains all defined variables in "expr" through assignment
		 */
		private boolean useDefMVMapping(IASTExpression expr, int side, 
				IASTFunctionCallExpression func, List<String> set){
			boolean v1 = false, v2 = false, v3 = false;
			List<String> l1 = new ArrayList<String>(); 
			List<String> l2 = new ArrayList<String>();
			List<String> l3 = new ArrayList<String>();
			if(expr == null) return false; 
			if(expr instanceof IASTAmbiguousExpression){
			}
			else if(expr instanceof IASTArraySubscriptExpression){
				IASTArraySubscriptExpression asE = (IASTArraySubscriptExpression)expr;
				if(side == rhs){
					// = a[index_expr]
					v1 = useDefMVMapping(asE.getArrayExpression(), rhs, func, l1); 
					v2 = useDefMVMapping(asE.getSubscriptExpression(), rhs, func, l2);
				} else { //lhs
					// a[b[i]] = ... , a is defined, b and i are used
					v1 = useDefMVMapping(asE.getSubscriptExpression(), rhs, func, l1);
					v2 = useDefMVMapping(asE.getArrayExpression(), lhs, func, l2);
				}
				Util.addAll(set, l1);
				Util.addAll(set, l2);
				return v1 | v2;
			}
			else if(expr instanceof IASTBinaryExpression){
				IASTBinaryExpression biE = (IASTBinaryExpression)expr;
				int op = biE.getOperator();
				if(op == IASTBinaryExpression.op_assign){
					// x = y = z is right associative --> x = (y = z)
					// So the "side" will be always rhs
					v1 = useDefMVMapping(biE.getOperand1(), lhs, func, l1);
					v2 = useDefMVMapping(biE.getOperand2(), rhs, func, l2);
					if(v2){
						Util.addAll(MVvar_, l1);
					}
					Util.addAll(set, l1);
					Util.addAll(set, l2);
				}
				else if( op == IASTBinaryExpression.op_multiplyAssign ||
						 op == IASTBinaryExpression.op_divideAssign ||
						 op == IASTBinaryExpression.op_moduloAssign ||
						 op == IASTBinaryExpression.op_plusAssign ||
						 op == IASTBinaryExpression.op_minusAssign ||
						 op == IASTBinaryExpression.op_shiftLeftAssign ||
						 op == IASTBinaryExpression.op_shiftRightAssign ||
						 op == IASTBinaryExpression.op_binaryAndAssign ||
						 op == IASTBinaryExpression.op_binaryXorAssign ||
						 op == IASTBinaryExpression.op_binaryOrAssign){
					v1 = useDefMVMapping(biE.getOperand1(), rhs, func, l1);
					v2 = useDefMVMapping(biE.getOperand2(), rhs, func, l2);
					v3 = useDefMVMapping(biE.getOperand1(), lhs, func, l3);
					if(v1 | v2){
						Util.addAll(MVvar_, l3);
					}
					Util.addAll(set, l1);
					Util.addAll(set, l2);
					Util.addAll(set, l3);
				} 
				else {
					v1 = useDefMVMapping(biE.getOperand1(), rhs, func, l1);
					v2 = useDefMVMapping(biE.getOperand2(), rhs, func, l2);
					Util.addAll(set, l1);
					Util.addAll(set, l2);
				}
				return v1 | v2 | v3;
			}
			else if(expr instanceof IASTCastExpression){
				IASTCastExpression castE = (IASTCastExpression)expr;
				v1 = useDefMVMapping(castE.getOperand(), side, func, l1);
				Util.addAll(set, l1);
				return v1;
			}
			else if(expr instanceof IASTConditionalExpression){
				IASTConditionalExpression condE = (IASTConditionalExpression)expr;
				if(side == rhs){
					v1 = useDefMVMapping(condE.getLogicalConditionExpression(), rhs, func, l1); 
					v2 = useDefMVMapping(condE.getPositiveResultExpression(), rhs, func, l2); 
					v3 = useDefMVMapping(condE.getNegativeResultExpression(), rhs, func, l3);
				} else {
					// eg. (x > y ? x : y) = 1
					v1 = useDefMVMapping(condE.getLogicalConditionExpression(), rhs, func, l1); 
					v2 = useDefMVMapping(condE.getPositiveResultExpression(), lhs, func, l2); 
					v3 = useDefMVMapping(condE.getNegativeResultExpression(), lhs, func, l3);
				}
				Util.addAll(set, l1);
				Util.addAll(set, l2);
				Util.addAll(set, l3);
				return v1 | v2 | v3;
			}
			else if(expr instanceof IASTExpressionList){
				IASTExpressionList exprList = (IASTExpressionList)expr;
				IASTExpression[] exprs = exprList.getExpressions();
				boolean[] newContext_ = new boolean[exprs.length];
				for(int i = 0; i<exprs.length; i++){
					v1 = v1 | useDefMVMapping(exprs[i], side, func, l1);
					newContext_[i] = v1;
					Util.addAll(set, l1);
				}
				if(func != null) 
					exprListMVContext_.push(newContext_);
				return v1;
			}
			else if(expr instanceof IASTFieldReference){
				IASTFieldReference frE = (IASTFieldReference)expr;
				v1 = useDefMVMapping(frE.getFieldOwner(), side, func, l1);
				Util.addAll(set, l1);
				return v1;
			}
			else if(expr instanceof IASTFunctionCallExpression){
				IASTFunctionCallExpression funcE = (IASTFunctionCallExpression)expr;
				IASTExpression funcname = funcE.getFunctionNameExpression();
				String signature = funcname.getRawSignature();
				IASTExpression parameter = funcE.getParameterExpression();
				MPICallGraphNode n = (MPICallGraphNode)cg_.getNode(currentNode_.getFileName(), signature);
				if(n != null){
					/* 1. Determine whether each parameter is MV or SV
					 * 2. Refer to the MVsummary, find the set of MV variables 
					 * according to the parameter MV context, and find out 
					 * whether the return value is MV or SV
					 */
					boolean returnval = false;
					if(parameter != null){
						v1 = useDefMVMapping(parameter, side, funcE, l1);
						if(parameter instanceof IASTExpressionList){ // >1 parameter
							boolean[] paramContext = (boolean[])exprListMVContext_.pop();
							for(int i=0; i<paramContext.length; i++){
								if(paramContext[i]){
									String param = getFormalParamName(n, i);
									if(param != null){
										List<String> mvlist = n.getMVSummary().get(param);
										Util.addAll(MVvar_, mvlist);
										if(mvlist.contains(n.getFuncName())){ //return MV value
											MVvar_.remove(n.getFuncName());
											returnval = true;
										}
									}
								}
							}
						} else { // single parameter
							String param = getFormalParamName(n, 0);
							if(param != null){
								List<String> mvlist = n.getMVSummary().get(param);
								Util.addAll(MVvar_, mvlist);
								if(mvlist.contains(n.getFuncName())){ //return MV value
									MVvar_.remove(n.getFuncName());
									returnval = true;
								}
							}
						}
					}
					for(Iterator<String> i = n.getGlobalUse().iterator(); i.hasNext();){
						String guse = i.next();
						if(context_.contains(guse)){
							List<String> mvlist = n.getMVSummary().get(guse);
							Util.addAll(MVvar_, mvlist);
							if(mvlist.contains(n.getFuncName())){ //return MV value
								MVvar_.remove(n.getFuncName());
								returnval = true;
							}
						}
					}
					if(n.getMVSummary().size() == 1){//no parameter, no global use
						List<String> mvlist = n.getMVSummary().get(n.getFuncName());
						if(mvlist.size() == 1 && mvlist.contains(n.getFuncName()))
							returnval = true;
						else
							returnval = false;
					}
					return returnval;
				}
				else{ 
					if(parameter != null){
						v1 = useDefMVMapping(parameter, side, funcE, l1);
						if(parameter instanceof IASTExpressionList)
							exprListMVContext_.pop();
						Util.addAll(set, l1);
						return v1;
					}
					else 
						return false;
				}
			}
			else if(expr instanceof IASTIdExpression){
				IASTIdExpression id = (IASTIdExpression)expr;
				IASTName name = id.getName();
				String var = name.toString();
				if(var.startsWith("MPI_")) return false;
				if(side == rhs){
					if(func != null){ //function parameter
						IASTExpression funcname = func.getFunctionNameExpression();
						String signature = funcname.getRawSignature();
						ICallGraphNode n = cg_.getNode(currentNode_.getFileName(), signature);
						if(n == null) { //library function call
							if(currentBlock_.getDef().contains(var)){
								if(!set.contains(var)) set.add(var);
								if(context_.contains(var) && !MVvar_.contains(var))
									MVvar_.add(var);
							}
						}
					}
					if(context_.contains(var)) 
						return true;
				} else { //lhs
					if(!set.contains(var))
						set.add(var);
					return false;
				}
			}
			else if(expr instanceof IASTLiteralExpression){
			}
			else if(expr instanceof IASTProblemExpression){
			}
			else if(expr instanceof IASTTypeIdExpression){
			}
			else if(expr instanceof IASTUnaryExpression){
				IASTUnaryExpression uE = (IASTUnaryExpression)expr;
				int op = uE.getOperator();
				if(op == IASTUnaryExpression.op_prefixIncr ||
					  op == IASTUnaryExpression.op_prefixDecr ||
					  op == IASTUnaryExpression.op_postFixIncr ||
					  op == IASTUnaryExpression.op_postFixDecr) {
					v1 = useDefMVMapping(uE.getOperand(), rhs, func, l1);
					v2 = useDefMVMapping(uE.getOperand(), lhs, func, l2);
					if(v1){
						Util.addAll(MVvar_, l2);
					}
					Util.addAll(set, l1);
					Util.addAll(set, l2);
				} else {
					v1 = useDefMVMapping(uE.getOperand(), side, func, l1);
					Util.addAll(set, l1);
				}
				return v1 | v2;
			}
			else if(expr instanceof ICASTTypeIdInitializerExpression){
			}
			else{
			}
			return false;
		}
	}
	
	private String getFormalParamName(ICallGraphNode func, int index){
		IASTFunctionDefinition fd = func.getFuncDef();
		IASTFunctionDeclarator fdecl = fd.getDeclarator();
		if(fdecl instanceof IASTStandardFunctionDeclarator){
			IASTStandardFunctionDeclarator sfunc = (IASTStandardFunctionDeclarator)fdecl;
			IASTParameterDeclaration[] params = sfunc.getParameters();
			if(index >= params.length) // additional parameters
				return null;
			IASTName param = params[index].getDeclarator().getName();
			return param.toString();
		} else {
			ICASTKnRFunctionDeclarator krfunc = (ICASTKnRFunctionDeclarator)fdecl;
			IASTName[] params = krfunc.getParameterNames();
			if(index >= params.length)
				return null;
			return params[index].toString();
		}
	}
	
	/** Determines a set of multi-valued "VARIABLES" (local and global) 
	 * in each program point 
	 */
	private void functionSlicing(){
		for(ICallGraphNode n = cg_.topEntry(); n != null; n = n.topNext()){
			MPICallGraphNode node = (MPICallGraphNode)n;
			if(!node.marked) continue;
			currentNode_ = node;
			//System.out.println(node.getFuncName());
			IControlFlowGraph cfg = node.getCFG();
			
			/** The initial worklist contains seeds blocks in this function and 
			 * the entry block if any of its parameters is multi-valued. 
			 */
			WorkListCollector wlc = new WorkListCollector(currentNode_);
			LinkedList<IBlock> seeds = wlc.getWorkList();
			boolean hasMVparam = false;
			for(Enumeration<String> e = node.getParamMV().keys(); e.hasMoreElements();){
				String param = e.nextElement();
				boolean val = node.getParamMV().get(param).booleanValue();
				if(val){ // multi-valued parameter
					((MPIBlock)cfg.getEntry()).getMVvar().add(param);
					hasMVparam = true;
				}
			}
			if(hasMVparam) seeds.add(cfg.getEntry());
			
			/** Slicing */
			while(!seeds.isEmpty()){
				MPIBlock block = (MPIBlock)seeds.remove();
				if(block.sliced) continue;
				block.sliced = true;

				//System.out.println("current seed is " + block.getID());
				Hashtable<String,List<IBlock>> DUSucc = block.getDUSucc();
				List<String> mv = block.getMVvar();
				List<String> newMV = new ArrayList<String>();
				
				handlePointers(block, mv);

				/* Intra-block slicing (Case #1) ---
				 * If there is a \phi function in this block, consider two cases:
				 * (1) If this block is a join block for If statement or Switch
				 * statement, or if this block is break exit join block of a loop,
				 * then all \phi variables in this block are multi-valued;
				 * (2) If this block is a join block (i.e., cond block) for For 
				 * statement, While statement or Do statement, then its multi-valued 
				 * \phi variables have been already marked through data dependences, 
				 * and all used \phi variables are kept multi-valued.
				 * Note: a block falls into one and only one of these two cases.
				 */
				if(!block.getPhiVar().isEmpty()){
					List<IBlock> cond = block.getCond();
					int blockcase = 0;
					for(Iterator<IBlock> i = cond.iterator(); i.hasNext();){
						if(blockcase == 2) break;
						MPIBlock condblock = (MPIBlock)i.next();
						IASTStatement parent = condblock.getParent();
						if(parent instanceof IASTIfStatement || 
								parent instanceof IASTSwitchStatement)
							blockcase = 1;
						else if(parent instanceof IASTForStatement ||
								parent instanceof IASTDoStatement ||
								parent instanceof IASTWhileStatement){
							if(block.getType() == MPIBlock.exit_join_type)
								blockcase = 1;
							else if(block.getType() == MPIBlock.expr_type)
								blockcase = 2;
						}
					}
					if(blockcase == 1){
						newMV = Util.Union(newMV, block.getPhiVar());
					}
					else if(blockcase == 2){
						for(Iterator<IBlock> i = cond.iterator(); i.hasNext();){
							MPIBlock condblock = (MPIBlock)i.next();
							for(Iterator<String> ii = condblock.getUsedPhiVar().iterator();
									ii.hasNext();){
								String var = ii.next();
								if(mv.contains(var))
									newMV.add(var);
							}
						}
					}
				}	
				
				/** Intra-block slicing (Case #1) --- 
				 * Given MV/SV used variable, determine whether defined variables
				 * are MV or SV.  
				 */
				
				IASTNode content = block.getContent();
				ExprMVAnalyzer emva = new ExprMVAnalyzer(content, mv, block);
				emva.run();
				newMV = Util.Union(newMV, emva.getMVList());
				
				/** Intra-block slicing (Case #3) -- 
				 *  any used only multi-valued variable is still multi-valued
				 */
				for(Iterator<String> i = mv.iterator(); i.hasNext();){
					String var = i.next();
					if(block.getUse().contains(var) && !block.getDef().contains(var)){
						if(!newMV.contains(var))
							newMV.add(var);
					}
				}
				
				handlePointers(block, newMV);
				handleBroadCast(block, newMV);
				block.setMVvar(newMV);
				
				/** Inter-block slicing based on data dependences */
				for(Iterator<String> i = newMV.iterator(); i.hasNext();){
					String MVvar = i.next();
					List<IBlock> DUnext = DUSucc.get(MVvar);
					if(DUnext != null){
						for(Iterator<IBlock> ii = DUnext.iterator(); ii.hasNext();){
							MPIBlock b = (MPIBlock)ii.next();
							if(b.sliced == true) continue;
							if(!seeds.contains(b)){
								seeds.add(b);
								//System.out.println("block " + b.getID() + " is added to seeds");
							}
							if(!b.getMVvar().contains(MVvar))
								b.getMVvar().add(MVvar);
						}
					}
				}
				
				/** Inter-block slicing based on \Phi edges */
				for(Iterator<IBlock> i = block.getJoin().iterator(); i.hasNext();){
					MPIBlock join = (MPIBlock)i.next();
					if(!seeds.contains(join) && !join.sliced) 
						seeds.add(join);
				}
			}
			
			/* Function Multi-valued Summary --- (1) whether a function's REAL
			 * paramters are multi-valued (A real parameter of function foo is 
			 * multi-valued if in any of foo's call sites the parameter is 
			 * multi-valued)
			 */
			for(IBlock block = cfg.getEntry(); block != null; block = block.topNext()){
				FuncParamMVChecker fpc = new FuncParamMVChecker(node, (MPIBlock)block);
				fpc.run();
			}
		}
	}
	
	class SeedsCollector extends ASTVisitor{	
		private MPICallGraphNode currentFunc_;
		
		public void run(){
			this.shouldVisitExpressions = true;
			this.shouldVisitStatements = true;
			
			for(ICallGraphNode n = cg_.botEntry(); n != null; n = n.botNext()){
				MPICallGraphNode node = (MPICallGraphNode)n;
				if(!node.marked) continue;
				currentFunc_ = node;
				IControlFlowGraph cfg = node.getCFG();
				for(IBlock b = cfg.getEntry(); b != null; b = b.topNext()){
					IASTNode content = b.getContent();
					if(content != null) 
						content.accept(this);
				}
			}
			for(ICallGraphNode n = cg_.botEntry(); n != null; n = n.botNext()){
				MPICallGraphNode node = (MPICallGraphNode)n;
				if(!node.marked) continue;
				if(node.hasSeed()){
					for(Iterator<ICallGraphNode> i = node.getCallers().iterator(); i.hasNext();){
						MPICallGraphNode caller = (MPICallGraphNode)i.next();
						caller.setSeed(true);
					}
				}
			}
		}
	
		public int visit(IASTExpression expr){
			if(expr instanceof IASTFunctionCallExpression){
				IASTFunctionCallExpression funcExpr = (IASTFunctionCallExpression)expr;
				IASTExpression funcname = funcExpr.getFunctionNameExpression();
				String signature = funcname.getRawSignature();
				if(signature.equals("MPI_Comm_rank")){
					currentFunc_.setSeed(true);
				} 
			}
			return PROCESS_CONTINUE;
		}
	}
	
	
	/** Work list contains two kinds of blocks:
	 * (1) where MPI_Comm_rank() is directly or indirectly called; 
	 * (2) the entry block of a function that has some MV real parameters;
	 * 
	 * (1) is marked in SeedsCollector and collected in WorkListCollector 
	 * (2) is collected in the slicing function. 
	 */
	class WorkListCollector extends ASTVisitor{	
		private ICallGraphNode func_;
		private boolean inRankFunc;
		private IASTExpressionList params;
		private LinkedList<IBlock> wlist;
		private MPIBlock currentBlock_;
		
		public WorkListCollector(ICallGraphNode func){
			func_ = func;
			inRankFunc = false;
			wlist = new LinkedList<IBlock>();
		}
		
		public LinkedList<IBlock> getWorkList(){
			if(!((MPICallGraphNode)func_).hasSeed())  return wlist;
			
			this.shouldVisitExpressions = true;
			this.shouldVisitStatements = true;
			for(IBlock b = func_.getCFG().getEntry(); b != null; b = b.topNext()){
				currentBlock_ = (MPIBlock)b;
				IASTNode content = b.getContent();
				if(content != null) 
					content.accept(this);
			}
			return wlist;
		}
	
		public int visit(IASTExpression expr){
			if(expr instanceof IASTFunctionCallExpression){
				IASTFunctionCallExpression funcExpr = (IASTFunctionCallExpression)expr;
				IASTExpression funcname = funcExpr.getFunctionNameExpression();
				String signature = funcname.getRawSignature();
				if(signature.equals("MPI_Comm_rank")){
					inRankFunc = true;
				} else {
					MPICallGraphNode n = (MPICallGraphNode)cg_.getNode(currentNode_.getFileName(), signature);
					if(n != null && n.hasSeed()){
						if(!wlist.contains(currentBlock_))
							wlist.add(currentBlock_);
						List<String> genMV = n.getMVSummary().get(signature);
						Util.addAll(currentBlock_.getMVvar(), genMV);
						if(currentBlock_.getMVvar().contains(n.getFuncName()))
							currentBlock_.getMVvar().remove(n.getFuncName());
					}
				}
			} else if(expr instanceof IASTIdExpression){
				if(inRankFunc){
					IASTNode me = expr;
					IASTNode parent = me.getParent();
					while(true){
						if(parent == params) break;
						else if(parent instanceof IASTFunctionCallExpression) break;
						else{
							me = parent;
							parent = parent.getParent();
						}
					}
					if(!(parent instanceof IASTExpressionList))
						return PROCESS_CONTINUE;
					IASTExpression[] rankParams = params.getExpressions();
					int index;
					for(index=0; index<rankParams.length; index++){
						if(me == rankParams[index]) break;
					}
					if(index == 1){
						IASTIdExpression id = (IASTIdExpression)expr;
						String var = id.getName().toString();
						//if(var.equals("MPI_Comm_rank")) return PROCESS_CONTINUE;
						if(!currentBlock_.getMVvar().contains(var))
							currentBlock_.getMVvar().add(var);
						if(!wlist.contains(currentBlock_)) wlist.add(currentBlock_);
					}
				}
			} else if(expr instanceof IASTExpressionList){
				if(inRankFunc){
					params = (IASTExpressionList)expr;
				}
			}
			return PROCESS_CONTINUE;
		}
		
		public int leave(IASTExpression expr){
			if(expr instanceof IASTFunctionCallExpression){
				IASTFunctionCallExpression funcExpr = (IASTFunctionCallExpression)expr;
				IASTExpression funcname = funcExpr.getFunctionNameExpression();
				String signature = funcname.getRawSignature();
				if(signature.equals("MPI_Comm_rank")){
					inRankFunc = false;
				}
			}
			else if(expr instanceof IASTExpressionList){
				params = null;
			}
			return PROCESS_CONTINUE;
		}
	}
	
	class BroadCastAnalyzer extends ASTVisitor{
		private IBlock block_;
		private String var;
		
		public BroadCastAnalyzer(IBlock block){
			block_ = block;
			var = null;
		}
		public String getBCdata() {return var;}
		
		public void run(){
			this.shouldVisitStatements = true;
			this.shouldVisitExpressions = true;
			IASTNode content = block_.getContent();
			if(content != null) 
				content.accept(this);
		}
		
		public int visit(IASTExpression expr){
			if(expr instanceof IASTFunctionCallExpression){
				IASTFunctionCallExpression funcE = (IASTFunctionCallExpression)expr;
				IASTExpression funcname = funcE.getFunctionNameExpression();
				String signature = funcname.getRawSignature();
				if(signature.equals("MPI_Bcast")){
					IASTExpression paramE = funcE.getParameterExpression();
					IASTExpression[] params = ((IASTExpressionList)paramE).getExpressions();
					IASTExpression dataE = params[0];
					if(dataE instanceof IASTIdExpression){
						IASTIdExpression ID = (IASTIdExpression)dataE;
						var = ID.getName().toString();
					}
					else if(dataE instanceof IASTUnaryExpression){
						IASTUnaryExpression uE = (IASTUnaryExpression)dataE;
						if(uE.getOperator() == IASTUnaryExpression.op_amper &&
								uE.getOperand() instanceof IASTIdExpression){
							IASTIdExpression ID = (IASTIdExpression)uE.getOperand();
							var = ID.getName().toString();
						}
					}
				}
				return PROCESS_SKIP;
			}
			return PROCESS_CONTINUE;
		}

	}
	
	class PointerAnalyzer extends ASTVisitor{
		protected IBlock block_;
		protected List<String> deref_;
		protected List<String> addr_;
		
		public PointerAnalyzer(IBlock b){
			block_ = b;
			deref_ = new ArrayList<String>();
			addr_ = new ArrayList<String>();
		}
		
		public List<String> getDeref() {return deref_;}
		public List<String> getAddr() {return addr_;}
		
		public void run(){
			this.shouldVisitExpressions = true;
			this.shouldVisitStatements = true;
			IASTNode content = block_.getContent();
			if(content != null) 
				content.accept(this);
		}
		
		public int visit(IASTExpression expr){
			visitor(expr, false, false);
			return PROCESS_SKIP;
		}
		
		private void visitor(IASTExpression expr, boolean inDeref, boolean inAddr){
			if(expr instanceof IASTArraySubscriptExpression){
				IASTArraySubscriptExpression arrayE = (IASTArraySubscriptExpression)expr;
				visitor(arrayE.getArrayExpression(), inDeref, inAddr);
				visitor(arrayE.getSubscriptExpression(), false, false);
			}
			else if(expr instanceof IASTBinaryExpression){
				IASTBinaryExpression biE = (IASTBinaryExpression)expr;
				visitor(biE.getOperand1(), inDeref, inAddr);
				visitor(biE.getOperand2(), inDeref, inAddr);
			}
			else if(expr instanceof IASTConditionalExpression){
				IASTConditionalExpression condE = (IASTConditionalExpression)expr;
				visitor(condE.getLogicalConditionExpression(), inDeref, inAddr);
				visitor(condE.getPositiveResultExpression(), inDeref, inAddr);
				visitor(condE.getNegativeResultExpression(), inDeref, inAddr);
			}
			else if(expr instanceof IASTExpressionList){
				IASTExpressionList listE = (IASTExpressionList)expr;
				IASTExpression[] exprs = listE.getExpressions();
				for(int i=0; i<exprs.length; i++){
					visitor(exprs[i], inDeref, inAddr);
				}
			}
			else if(expr instanceof IASTFieldReference){
				IASTFieldReference fr = (IASTFieldReference)expr;
				visitor(fr.getFieldOwner(), inDeref, inAddr);
			}
			else if(expr instanceof IASTFunctionCallExpression){
				IASTFunctionCallExpression funcE = (IASTFunctionCallExpression)expr;
				IASTExpression funcname = funcE.getFunctionNameExpression();
				String signature = funcname.getRawSignature();
				ICallGraphNode n = cg_.getNode(currentNode_.getFileName(), signature);
				if(n != null) return;
				visitor(funcE.getFunctionNameExpression(), false, false);
				visitor(funcE.getParameterExpression(), inDeref, inAddr);
			}
			else if(expr instanceof IASTIdExpression){
				IASTIdExpression ID = (IASTIdExpression)expr;
				String var = ID.getName().toString();
				if(inDeref){
					if(!deref_.contains(var)) 
						deref_.add(var);
				} 
				if(inAddr){
					if(!addr_.contains(var))
						addr_.add(var);
				}
			}
			else if(expr instanceof IASTUnaryExpression){
				IASTUnaryExpression uE = (IASTUnaryExpression)expr;
				int op = uE.getOperator();
				boolean addrflag = inAddr;
				boolean derefflag = inDeref;
				if(op == IASTUnaryExpression.op_amper){ // &E
					addrflag = true;
				} else if(op == IASTUnaryExpression.op_star){ // *E
					derefflag = true;
				}
				visitor(uE.getOperand(), addrflag, derefflag);
			}
		}
	}
	
	class FuncParamMVChecker extends ASTVisitor{
		protected MPICallGraphNode currentNode_;
		protected MPIBlock block_;
		protected MPICallGraphNode func;
		
		public FuncParamMVChecker(MPICallGraphNode func, MPIBlock block){
			currentNode_ = func;
			block_ = block;
			func = null;
		}
		
		public void run(){
			this.shouldVisitExpressions = true;
			this.shouldVisitStatements = true;
			
			IASTNode content = block_.getContent();
			if(content != null) 
				content.accept(this);
		}
	
		public int visit(IASTExpression expr){
			if(expr instanceof IASTFunctionCallExpression){
				IASTFunctionCallExpression funcExpr = (IASTFunctionCallExpression)expr;
				IASTExpression funcname = funcExpr.getFunctionNameExpression();
				String signature = funcname.getRawSignature();
				func = (MPICallGraphNode)cg_.getNode(currentNode_.getFileName(), signature);
				if(func != null){
					IASTExpression parameter = funcExpr.getParameterExpression();
					if(parameter instanceof IASTExpressionList){
						IASTExpressionList paramListE = (IASTExpressionList)parameter;
						IASTExpression[] params = paramListE.getExpressions();
						for(int i=0; i<params.length; i++){
							ExprMVAnalyzer ema = new ExprMVAnalyzer(params[i], 
									block_.getMVvar(), block_);
							ema.run();
							if(ema.isMV()){
								String paramName = getFormalParamName(func, i);
								if(paramName != null)
									func.getParamMV().put(paramName, new Boolean(true));
							}
						}
					}
					else if(parameter != null){
						ExprMVAnalyzer ema = new ExprMVAnalyzer(parameter, 
								block_.getMVvar(), block_);
						ema.run();
						if(ema.isMV()){
							String paramName = getFormalParamName(func, 0);
							if(paramName != null)
								func.getParamMV().put(paramName, new Boolean(true));
						}
					}
				}
			}
			return PROCESS_CONTINUE;
		}
		
		public int leave(IASTExpression expr){
			if(expr instanceof IASTFunctionCallExpression){
				IASTFunctionCallExpression funcExpr = (IASTFunctionCallExpression)expr;
				IASTExpression funcname = funcExpr.getFunctionNameExpression();
				String signature = funcname.getRawSignature();
				if(cg_.getNode(currentNode_.getFileName(), signature) == func){
					func = null;
				}
			}
			return PROCESS_CONTINUE;
		}
	}
	
	/** Determine whether an "EXPRESSION" is multi-valued */
	private void exprMVAnalysis(){
		for(ICallGraphNode n = cg_.botEntry(); n != null; n = n.botNext()){
			MPICallGraphNode node = (MPICallGraphNode)n;
			if(!node.marked) continue;
			IControlFlowGraph cfg = node.getCFG();
			for(IBlock b = cfg.getEntry(); b != null; b = b.topNext()){
				MPIBlock block = (MPIBlock)b;
				//System.out.println("Block " + block.getID());
				ExprMVAnalyzer EA = new ExprMVAnalyzer(block.getContent(), block.getMVvar(), block);
				EA.run();
				block.setMV(EA.isMV());
			}
		}
	}
	
}
