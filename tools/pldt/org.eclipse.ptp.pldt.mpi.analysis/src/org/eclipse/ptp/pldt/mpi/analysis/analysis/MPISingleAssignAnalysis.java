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
import java.util.List;

import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignatedInitializer;
import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.ICallGraph;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.ICallGraphNode;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.IControlFlowGraph;

public class MPISingleAssignAnalysis {
	protected ICallGraph cg_;
	protected IControlFlowGraph cfg_;
	protected MPICallGraphNode currentNode_;
	protected Hashtable<String, Integer> assignNum_;
	protected Hashtable<String, Boolean> type_;
	private static final boolean traceOn = false;

	public MPISingleAssignAnalysis(ICallGraph cg) {
		cg_ = cg;
	}

	public void run() {
		for (ICallGraphNode n = cg_.botEntry(); n != null; n = n.botNext()) {
			MPICallGraphNode currentNode_ = (MPICallGraphNode) n;
			if (traceOn)
				System.out.println(currentNode_.getFuncName());
			cfg_ = currentNode_.getCFG();
			assignNum_ = currentNode_.getSAVar();
			type_ = currentNode_.getSAVarPointer();
			dataCollector dc = new dataCollector();
			dc.run();
			SingleAssignAnalyzer saa = new SingleAssignAnalyzer();
			saa.run();

			for (Enumeration<String> e = currentNode_.getSAVar().keys(); e.hasMoreElements();) {
				String var = e.nextElement();
				if (traceOn)
					System.out.println(var + " is defined " + currentNode_.getSAVar().get(var).intValue() + " times"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}

	class dataCollector extends ASTVisitor {
		public void run() {
			this.shouldVisitStatements = true;
			this.shouldVisitDeclarations = true;
			/* global variables */
			for (int i = 0; i < cg_.getEnv().size(); i++) {
				String var = cg_.getEnv().get(i);
				assignNum_.put(var, new Integer(0));
				type_.put(var, ((MPICallGraph) cg_).getGVPointer().get(i));
			}
			/* function parameters */
			List<String> parameters = new ArrayList<String>();
			List<Boolean> pointers = new ArrayList<Boolean>();
			getParameters(currentNode_.getFuncDef(), parameters, pointers);
			for (int i = 0; i < parameters.size(); i++) {
				String var = parameters.get(i);
				assignNum_.put(var, new Integer(0));
				type_.put(var, pointers.get(i));
			}
			/* local variables */
			currentNode_.getFuncDef().accept(this);
		}

		protected void getParameters(IASTFunctionDefinition fd,
				List<String> paramList, List<Boolean> pointerList) {
			IASTFunctionDeclarator fdecl = fd.getDeclarator();
			if (fdecl instanceof IASTStandardFunctionDeclarator) {
				IASTStandardFunctionDeclarator sfunc = (IASTStandardFunctionDeclarator) fdecl;
				IASTParameterDeclaration[] params = sfunc.getParameters();
				for (int i = 0; i < params.length; i++) {
					paramList.add(params[i].getDeclarator().getName().toString());
					IASTPointerOperator[] pops = params[i].getDeclarator().getPointerOperators();
					if (pops != IASTPointerOperator.EMPTY_ARRAY)
						pointerList.add(new Boolean(true));
					else
						pointerList.add(new Boolean(false));
				}
			} else {
				ICASTKnRFunctionDeclarator krfunc = (ICASTKnRFunctionDeclarator) fdecl;
				IASTName[] params = krfunc.getParameterNames();
				for (int i = 0; i < params.length; i++) {
					paramList.add(params[i].toString());
					IASTDeclarator decl = krfunc.getDeclaratorForParameterName(params[i]);
					if (decl.getPointerOperators() != IASTPointerOperator.EMPTY_ARRAY)
						pointerList.add(new Boolean(true));
					else
						pointerList.add(new Boolean(false));
				}
			}
		}

		public int visit(IASTStatement stmt) {
			if (stmt instanceof IASTDeclarationStatement) {
				IASTDeclarationStatement declStmt = (IASTDeclarationStatement) stmt;
				IASTDeclaration decl = declStmt.getDeclaration();
				if (decl instanceof IASTSimpleDeclaration) {
					IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) decl;
					IASTDeclarator[] declarators = simpleDecl.getDeclarators();
					for (int i = 0; i < declarators.length; i++) {
						String var = declarators[i].getName().toString();
						assignNum_.put(var, new Integer(0));
						if (declarators[i].getPointerOperators() != IASTPointerOperator.EMPTY_ARRAY)
							type_.put(var, new Boolean(true));
						else
							type_.put(var, new Boolean(false));
					}
				}
				return PROCESS_SKIP;
			}
			return PROCESS_CONTINUE;
		}
	}

	class SingleAssignAnalyzer extends ASTVisitor {

		public void run() {
			this.shouldVisitDeclarations = true;
			this.shouldVisitExpressions = true;
			this.shouldVisitStatements = true;

		}

		public int visit(IASTStatement stmt) {
			if (stmt instanceof IASTDeclarationStatement) {
				IASTDeclarationStatement declStmt = (IASTDeclarationStatement) stmt;
				IASTDeclaration decl = declStmt.getDeclaration();
				if (decl instanceof IASTSimpleDeclaration) {
					IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) decl;
					IASTDeclarator[] declarators = simpleDecl.getDeclarators();
					for (int i = 0; i < declarators.length; i++) {
						IASTName name = declarators[i].getName();
						IASTInitializer init = declarators[i].getInitializer();
						if (init != null) {
							processInitializer(init, name.toString());
						}
					}
				}
				return PROCESS_SKIP;
			}
			return PROCESS_CONTINUE;
		}

		protected void processInitializer(IASTInitializer init, String var) {
			if (init != null) {
				if (init instanceof IASTInitializerExpression) {
					IASTInitializerExpression initE = (IASTInitializerExpression) init;
					IASTExpression e = initE.getExpression();
					List<String> def = new ArrayList<String>();
					List<String> use = new ArrayList<String>();
					List<String> ldf = new ArrayList<String>();
					List<String> rdf = new ArrayList<String>();
					boolean value = saExpr(e, rhs, null, -1, def, use, ldf, rdf);
					for (Iterator<String> i = use.iterator(); i.hasNext();) {
						String v = i.next();
						int num = assignNum_.get(v).intValue();
						boolean pointerType = type_.get(v).booleanValue();
						if (pointerType) // pointers are used for math operations
							assignNum_.put(v, new Integer(num + 2));
					}
					boolean pointerType = type_.get(var).booleanValue();
					if (pointerType) {
						if (value) // init by malloc
							assignNum_.put(var, new Integer(1));
						else
							assignNum_.put(var, new Integer(2));
					} else {
						assignNum_.put(var, new Integer(1));
					}
				}
				else if (init instanceof IASTInitializerList) {
					IASTInitializerList list = (IASTInitializerList) init;
					IASTInitializer[] inits = list.getInitializers();
					for (int j = 0; j < inits.length; j++) {
						processInitializer(inits[j], var);
					}
				}
				else if (init instanceof ICASTDesignatedInitializer) {
					System.out.println("ICASTDesignatedInitializer found !"); //$NON-NLS-1$
				}
			}
		}

		public int visit(IASTExpression expr) {
			saExpr(expr, rhs, null, -1, new ArrayList<String>(),
					new ArrayList<String>(), new ArrayList<String>(),
					new ArrayList<String>());
			return PROCESS_SKIP;
		}

		protected final int lhs = 0;
		protected final int rhs = 1;
		protected final int lderef = 2; // lhs and dereference
		protected final int rderef = 3; // rhs and dereference

		/**
		 * return: is a malloc(calloc, realloc) met? <br>
		 * Params: def -- the set of defined variables <br>
		 * use -- the set of used (other than de-referenced) variables <br>
		 * ref -- the set of de-referenced variables
		 */
		protected boolean saExpr(IASTExpression expr, int side,
				IASTFunctionCallExpression func, int index,
				List<String> def, List<String> use,
				List<String> ldf, List<String> rdf) {
			List<String> d1 = new ArrayList<String>();
			List<String> d2 = new ArrayList<String>();
			List<String> d3 = new ArrayList<String>();
			List<String> u1 = new ArrayList<String>();
			List<String> u2 = new ArrayList<String>();
			List<String> u3 = new ArrayList<String>();
			List<String> lr1 = new ArrayList<String>();
			List<String> lr2 = new ArrayList<String>();
			List<String> lr3 = new ArrayList<String>();
			List<String> rr1 = new ArrayList<String>();
			List<String> rr2 = new ArrayList<String>();
			List<String> rr3 = new ArrayList<String>();
			boolean v1 = false, v2 = false, v3 = false;
			if (expr == null)
				return false;
			if (expr instanceof IASTArraySubscriptExpression) {
				IASTArraySubscriptExpression asE = (IASTArraySubscriptExpression) expr;
				if (side == rhs || side == rderef) {
					v1 = saExpr(asE.getArrayExpression(), rderef, func, index, d1, u1, lr1, rr1);
					v2 = saExpr(asE.getSubscriptExpression(), rhs, func, index, d2, u2, lr2, rr2);
				} else {
					v1 = saExpr(asE.getArrayExpression(), lderef, func, index, d1, u1, lr1, rr1);
					v2 = saExpr(asE.getSubscriptExpression(), rhs, func, index, d2, u2, lr2, rr2);
				}
				Util.addAll(def, d1);
				Util.addAll(def, d2);
				Util.addAll(use, u1);
				Util.addAll(use, u2);
				Util.addAll(ldf, lr1);
				Util.addAll(ldf, lr2);
				Util.addAll(rdf, rr1);
				Util.addAll(rdf, rr2);
				return v1 | v2;
			}
			else if (expr instanceof IASTBinaryExpression) {
				IASTBinaryExpression biE = (IASTBinaryExpression) expr;
				int op = biE.getOperator();
				if (op == IASTBinaryExpression.op_assign) {
					v1 = saExpr(biE.getOperand1(), lhs, func, index, d1, u1, lr1, rr1);
					v2 = saExpr(biE.getOperand2(), rhs, func, index, d2, u2, lr2, rr2);
					for (Iterator<String> i = d1.iterator(); i.hasNext();) {
						String var = i.next();
						int num = assignNum_.get(var).intValue();
						boolean pointerType = type_.get(var).booleanValue();
						if (pointerType) {
							if (v2) // init by malloc
								assignNum_.put(var, new Integer(num + 1));
							else
								assignNum_.put(var, new Integer(num + 2));
						} else {
							assignNum_.put(var, new Integer(num + 1));
						}
					}
					for (Iterator<String> i = u1.iterator(); i.hasNext();) {
						String var = i.next();
						int num = assignNum_.get(var).intValue();
						boolean pointerType = type_.get(var).booleanValue();
						if (pointerType) // pointers are used for math operations
							assignNum_.put(var, new Integer(num + 2));
					}
					for (Iterator<String> i = u2.iterator(); i.hasNext();) {
						String var = i.next();
						int num = assignNum_.get(var).intValue();
						boolean pointerType = type_.get(var).booleanValue();
						if (pointerType) // pointers are used for math operations
							assignNum_.put(var, new Integer(num + 2));
					}
				} else if (op == IASTBinaryExpression.op_multiplyAssign ||
						op == IASTBinaryExpression.op_divideAssign ||
						op == IASTBinaryExpression.op_moduloAssign ||
						op == IASTBinaryExpression.op_plusAssign ||
						op == IASTBinaryExpression.op_minusAssign ||
						op == IASTBinaryExpression.op_shiftLeftAssign ||
						op == IASTBinaryExpression.op_shiftRightAssign ||
						op == IASTBinaryExpression.op_binaryAndAssign ||
						op == IASTBinaryExpression.op_binaryXorAssign ||
						op == IASTBinaryExpression.op_binaryOrAssign) {
					v1 = saExpr(biE.getOperand1(), lhs, func, index, d1, u1, lr1, rr1);
					v2 = saExpr(biE.getOperand1(), rhs, func, index, d2, u2, lr2, rr2);
					v3 = saExpr(biE.getOperand2(), rhs, func, index, d3, u3, lr2, rr3);
					for (Iterator<String> i = d1.iterator(); i.hasNext();) {
						String var = i.next();
						int num = assignNum_.get(var).intValue();
						boolean pointerType = type_.get(var).booleanValue();
						if (pointerType) {
							if (v2) // init by malloc
								assignNum_.put(var, new Integer(num + 1));
							else
								assignNum_.put(var, new Integer(num + 2));
						} else {
							assignNum_.put(var, new Integer(num + 1));
						}
					}
					for (Iterator<String> i = u2.iterator(); i.hasNext();) {
						String var = i.next();
						int num = assignNum_.get(var).intValue();
						boolean pointerType = type_.get(index).booleanValue();
						if (pointerType) // pointers are used for math operations
							assignNum_.put(var, new Integer(num + 2));
					}
					for (Iterator<String> i = u3.iterator(); i.hasNext();) {
						String var = i.next();
						int num = assignNum_.get(var).intValue();
						boolean pointerType = type_.get(index).booleanValue();
						if (pointerType) // pointers are used for math operations
							assignNum_.put(var, new Integer(num + 2));
					}
				} else {
					v1 = saExpr(biE.getOperand1(), rhs, func, index, d1, u1, lr1, rr1);
					v2 = saExpr(biE.getOperand2(), rhs, func, index, d2, u2, lr2, rr2);
				}
				Util.addAll(def, d1);
				Util.addAll(def, d2);
				Util.addAll(def, d3);
				Util.addAll(use, u1);
				Util.addAll(use, u2);
				Util.addAll(use, u3);
				Util.addAll(ldf, lr1);
				Util.addAll(ldf, lr2);
				Util.addAll(ldf, lr3);
				Util.addAll(rdf, rr1);
				Util.addAll(rdf, rr2);
				Util.addAll(rdf, rr3);
				return v1 | v2 | v3;
			}
			else if (expr instanceof IASTCastExpression) {
				IASTCastExpression castE = (IASTCastExpression) expr;
				v1 = saExpr(castE.getOperand(), side, func, index, d1, u1, lr1, rr1);
				Util.addAll(def, d1);
				Util.addAll(use, u1);
				Util.addAll(ldf, lr1);
				Util.addAll(rdf, rr1);
				return v1;
			}
			else if (expr instanceof IASTConditionalExpression) {
				IASTConditionalExpression condE = (IASTConditionalExpression) expr;
				if (side == rhs || side == rderef) {
					v1 = saExpr(condE.getLogicalConditionExpression(), side, func, index, d1, u1, lr1, rr1);
					v2 = saExpr(condE.getPositiveResultExpression(), side, func, index, d2, u2, lr2, rr2);
					v3 = saExpr(condE.getNegativeResultExpression(), side, func, index, d3, u3, lr3, rr3);
				} else {
					// eg. x > y ? x : y = 1
					if (side == lhs)
						v1 = saExpr(condE.getLogicalConditionExpression(), rhs, func, index, d1, u1, lr1, rr1);
					else
						// side = lderef
						v1 = saExpr(condE.getLogicalConditionExpression(), rderef, func, index, d1, u1, lr1, rr1);
					v2 = saExpr(condE.getPositiveResultExpression(), side, func, index, d2, u2, lr2, rr2);
					v3 = saExpr(condE.getNegativeResultExpression(), side, func, index, d3, u3, lr3, rr3);
				}
				Util.addAll(def, d1);
				Util.addAll(def, d2);
				Util.addAll(def, d3);
				Util.addAll(use, u1);
				Util.addAll(use, u2);
				Util.addAll(use, u3);
				Util.addAll(ldf, lr1);
				Util.addAll(ldf, lr2);
				Util.addAll(ldf, lr3);
				Util.addAll(rdf, rr1);
				Util.addAll(rdf, rr2);
				Util.addAll(rdf, rr3);
				return v1 | v2 | v3;
			}
			else if (expr instanceof IASTExpressionList) {
				IASTExpressionList exprList = (IASTExpressionList) expr;
				IASTExpression[] exprs = exprList.getExpressions();
				for (int i = 0; i < exprs.length; i++) {
					if (func != null)
						v1 = v1 | saExpr(exprs[i], side, func, i, d1, u1, lr1, rr1);
					else
						v1 = v1 | saExpr(exprs[i], side, func, index, d1, u1, lr1, rr1);
					Util.addAll(def, d1);
					Util.addAll(use, u1);
					Util.addAll(ldf, lr1);
					Util.addAll(rdf, rr1);
				}
				return v1;
			}
			else if (expr instanceof IASTFieldReference) {
				IASTFieldReference frE = (IASTFieldReference) expr;
				if (side == lhs || side == lderef)
					v1 = saExpr(frE.getFieldOwner(), lderef, func, index, d1, u1, lr1, rr1);
				else
					v1 = saExpr(frE.getFieldOwner(), rderef, func, index, d1, u1, lr1, rr1);
				Util.addAll(def, d1);
				Util.addAll(use, u1);
				Util.addAll(ldf, lr1);
				Util.addAll(rdf, rr1);
				return v1;
			}
			else if (expr instanceof IASTFunctionCallExpression) {
				IASTFunctionCallExpression funcE = (IASTFunctionCallExpression) expr;
				IASTExpression funcname = funcE.getFunctionNameExpression();
				String signature = funcname.getRawSignature();
				if (signature.equals("malloc") || signature.equals("calloc") || //$NON-NLS-1$ //$NON-NLS-2$
						signature.equals("realloc")) //$NON-NLS-1$
					return true;
				IASTExpression paramE = funcE.getParameterExpression();
				if (paramE == null)
					return false;
				if (paramE instanceof IASTExpressionList)
					v1 = saExpr(paramE, side, funcE, -1, d1, u1, lr1, rr1);
				else
					v1 = saExpr(paramE, side, funcE, 0, d1, u1, lr1, rr1);
				/* update assignment number of global var */
				MPICallGraphNode node = (MPICallGraphNode) cg_.getNode(currentNode_.getFileName(), signature);
				if (node != null) {
					for (Iterator<String> i = cg_.getEnv().iterator(); i.hasNext();) {
						String gvar = i.next();
						if (!u1.contains(gvar) && !rr1.contains(gvar)) {
							int fnum = node.getSAVar().get(gvar).intValue();
							int num = assignNum_.get(gvar).intValue();
							assignNum_.put(gvar, new Integer(fnum + num));
						}
					}
				}
				Util.addAll(def, d1);
				Util.addAll(use, u1);
				Util.addAll(ldf, lr1);
				Util.addAll(rdf, rr1);
				return v1;
			}
			else if (expr instanceof IASTIdExpression) {
				IASTIdExpression id = (IASTIdExpression) expr;
				IASTName name = id.getName();
				String var = name.toString();
				if (var.startsWith("MPI_"))return false; //$NON-NLS-1$
				int fnum = 0;
				if (func != null) {
					fnum = getParamAssignNum(func, index);
				}
				if (side == rhs) {
					if (!use.contains(var))
						use.add(var);
					int num = assignNum_.get(var).intValue();
					assignNum_.put(var, new Integer(num + fnum));
				} else if (side == lhs) {
					if (!def.contains(var))
						def.add(var);
				} else if (side == lderef) {
					if (!ldf.contains(var))
						ldf.add(var);
				} else { // rderef
					if (!rdf.contains(var))
						rdf.add(var);
					int num = assignNum_.get(var).intValue();
					assignNum_.put(var, new Integer(num + fnum));
				}
				return false;
			}
			else if (expr instanceof IASTLiteralExpression) {
			}
			else if (expr instanceof IASTProblemExpression) {
			}
			else if (expr instanceof IASTTypeIdExpression) {
			}
			else if (expr instanceof IASTUnaryExpression) {
				IASTUnaryExpression uE = (IASTUnaryExpression) expr;
				int op = uE.getOperator();
				if (op == IASTUnaryExpression.op_prefixIncr ||
						op == IASTUnaryExpression.op_prefixDecr ||
						op == IASTUnaryExpression.op_postFixIncr ||
						op == IASTUnaryExpression.op_postFixDecr) {
					v1 = saExpr(uE.getOperand(), rhs, func, index, d1, u1, lr1, rr1);
					v2 = saExpr(uE.getOperand(), lhs, func, index, d2, u2, lr2, rr2);
					for (Iterator<String> i = d1.iterator(); i.hasNext();) {
						String var = i.next();
						int num = assignNum_.get(var).intValue();
						boolean pointerType = type_.get(var).booleanValue();
						if (pointerType) {
							if (v2) // init by malloc
								assignNum_.put(var, new Integer(num + 1));
							else
								assignNum_.put(var, new Integer(num + 2));
						} else {
							assignNum_.put(var, new Integer(num + 1));
						}
					}
					for (Iterator<String> i = u1.iterator(); i.hasNext();) {
						String var = i.next();
						int num = assignNum_.get(var).intValue();
						boolean pointerType = type_.get(var).booleanValue();
						if (pointerType) // pointers are used for math operations
							assignNum_.put(var, new Integer(num + 2));
					}
					for (Iterator<String> i = u2.iterator(); i.hasNext();) {
						String var = i.next();
						int num = assignNum_.get(var).intValue();
						boolean pointerType = type_.get(var).booleanValue();
						if (pointerType) // pointers are used for math operations
							assignNum_.put(var, new Integer(num + 2));
					}
					Util.addAll(def, d1);
					Util.addAll(def, d2);
					Util.addAll(use, u1);
					Util.addAll(use, u2);
					Util.addAll(ldf, lr1);
					Util.addAll(ldf, lr2);
					Util.addAll(rdf, rr1);
					Util.addAll(rdf, rr2);
				} else {
					v1 = saExpr(uE.getOperand(), side, func, index, d1, u1, lr1, rr1);
					Util.addAll(def, d1);
					Util.addAll(use, u1);
					Util.addAll(ldf, lr1);
					Util.addAll(rdf, rr1);
				}
				return v1 | v2;
			}
			return false;
		}

		protected int getParamAssignNum(IASTFunctionCallExpression fE, int index) {
			if (index == -1)
				return 0;
			IASTExpression funcname = fE.getFunctionNameExpression();
			String signature = funcname.getRawSignature();
			MPICallGraphNode node = (MPICallGraphNode) cg_.getNode(currentNode_.getFileName(), signature);
			if (node != null) {
				Hashtable<String, Integer> funcAssignNum = node.getSAVar();
				IASTFunctionDefinition fdef = node.getFuncDef();
				IASTFunctionDeclarator fdecl = fdef.getDeclarator();
				if (fdecl instanceof IASTStandardFunctionDeclarator) {
					IASTStandardFunctionDeclarator sfunc = (IASTStandardFunctionDeclarator) fdecl;
					IASTParameterDeclaration[] params = sfunc.getParameters();
					if (params.length <= index)
						return 0;
					IASTName param = params[index].getDeclarator().getName();
					return funcAssignNum.get(param.toString()).intValue();
				} else {
					ICASTKnRFunctionDeclarator krfunc = (ICASTKnRFunctionDeclarator) fdecl;
					IASTName[] params = krfunc.getParameterNames();
					if (params.length <= index)
						return 0;
					IASTName param = params[index];
					return funcAssignNum.get(param.toString()).intValue();
				}
			} else {
				/*
				 * Library function calls. Any parameter whose address
				 * is taken (pointer or array) is both defined and used.
				 */
				IASTExpression parameterE = fE.getParameterExpression();
				if (parameterE instanceof IASTExpressionList) {
					IASTExpressionList paramEList = (IASTExpressionList) parameterE;
					IASTExpression param = (paramEList.getExpressions())[index];
					IType type = param.getExpressionType();
					if (type instanceof IArrayType ||
							type instanceof IPointerType)
						return 2;
				}
				else {
					IType type = parameterE.getExpressionType();
					if (type instanceof IArrayType ||
							type instanceof IPointerType)
						return 2;
				}
			}
			return 0;
		}
	}

}
