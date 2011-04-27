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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignatedInitializer;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypeIdInitializerExpression;
import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousExpression;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.IBlock;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.ICallGraph;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.ICallGraphNode;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.IControlFlowGraph;

/**
 * Calculate the (inter-procedural) use set and def set of each block.
 * 
 * @author Yuan Zhang
 * 
 */
public class UseDefBuilder extends ASTVisitor {
	protected ICallGraph cg_;
	protected IControlFlowGraph cfg_ = null;

	protected List<String> use_ = null;
	protected List<String> def_ = null;
	protected List<String> guse_ = null;
	protected List<String> gdef_ = null;
	protected List<String> padef_ = null;

	protected final int lhs = 0;
	protected final int rhs = 1;

	protected MPICallGraphNode currentFunc_;
	protected Hashtable<String, List<IBlock>> defTable_ = null;

	public UseDefBuilder(ICallGraph cg) {
		cg_ = cg;
	}

	public void run() {
		this.shouldVisitDeclarations = true;
		this.shouldVisitExpressions = true;
		this.shouldVisitStatements = true;

		/* Fix point to deal with the recursion */
		boolean change = true;
		while (change) {
			change = false;
			for (ICallGraphNode n = cg_.botEntry(); n != null; n = n.botNext()) {
				MPICallGraphNode node = (MPICallGraphNode) n;
				// if(node.getFuncName().equals("yytnamerr"))
				// System.out.println(node.getFuncName());
				if (!node.marked)
					continue;
				List<String> oldguse_ = node.getGlobalUse();
				List<String> oldgdef_ = node.getGlobalDef();
				List<String> oldpadef_ = node.getParamDef();
				guse_ = new ArrayList<String>();
				gdef_ = new ArrayList<String>();
				padef_ = new ArrayList<String>();
				cfg_ = node.getCFG();
				currentFunc_ = node;
				for (IBlock b = cfg_.getEntry().topNext(); b != null; b = b.topNext()) {
					MPIBlock block = (MPIBlock) b;
					IASTNode content = block.getContent();
					if (content == null || content instanceof IASTName)
						continue;
					List<String> olduse_ = block.getUse();
					List<String> olddef_ = block.getDef();
					use_ = new ArrayList<String>();
					def_ = new ArrayList<String>();
					if (content instanceof IASTExpression) {
						IASTExpression expr = (IASTExpression) content;
						expr.accept(this);
					} else { // statement
						IASTStatement stmt = (IASTStatement) content;
						stmt.accept(this);
					}
					if (!Util.equals(use_, olduse_)) {
						block.setUse(use_);
						change = true;
					}
					if (!Util.equals(def_, olddef_)) {
						block.setDef(def_);
						change = true;
					}
				}
				if (!Util.equals(guse_, oldguse_)) {
					node.setGlobalUse(guse_);
					change = true;
				}
				if (!Util.equals(gdef_, oldgdef_)) {
					node.setGlobalDef(gdef_);
					change = true;
				}
				if (!Util.equals(padef_, oldpadef_)) {
					node.setParamDef(padef_);
					change = true;
				}
			}
		}

		for (ICallGraphNode n = cg_.botEntry(); n != null; n = n.botNext()) {
			MPICallGraphNode node = (MPICallGraphNode) n;
			if (!node.marked)
				continue;
			currentFunc_ = node;
			cfg_ = node.getCFG();
			setEntryBlock((MPIBlock) node.getCFG().getEntry());
			setExitBlock((MPIBlock) node.getCFG().getExit());
			defTable_ = new Hashtable<String, List<IBlock>>();
			for (IBlock b = cfg_.getEntry(); b != null; b = b.topNext()) {
				MPIBlock block = (MPIBlock) b;
				List<String> def = block.getDef();
				if (def.isEmpty())
					continue;
				for (Iterator<String> i = def.iterator(); i.hasNext();) {
					String var = i.next();
					List<IBlock> defblocks = defTable_.get(var);
					if (defblocks == null) {
						defblocks = new ArrayList<IBlock>();
						defblocks.add(block);
						defTable_.put(var, defblocks);
					} else {
						defblocks.add(block);
						defTable_.put(var, defblocks);
					}
				}
			}
			node.setDefTable(defTable_);
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
					IASTName name = declarators[i].getName();
					IASTInitializer init = declarators[i].getInitializer();
					if (init != null)
						def_.add(name.toString());
					processInitializer(init);
				}
			}
			return PROCESS_SKIP;
		}
		return PROCESS_CONTINUE;
	}

	private void processInitializer(IASTInitializer init) {
		if (init != null) {
			if (init instanceof IASTInitializerExpression) {
				IASTInitializerExpression initE = (IASTInitializerExpression) init;
				IASTExpression e = initE.getExpression();
				useDefSet(e, rhs, null, -1);
			}
			else if (init instanceof IASTInitializerList) {
				IASTInitializerList list = (IASTInitializerList) init;
				IASTInitializer[] inits = list.getInitializers();
				for (int j = 0; j < inits.length; j++) {
					processInitializer(inits[j]);
				}
			}
			else if (init instanceof ICASTDesignatedInitializer) {
				System.out.println("ICASTDesignatedInitializer found !"); //$NON-NLS-1$
			}
		}
	}

	/**
	 * The traverse is stopped (by PROCESS_SKIP) when the expression of
	 * the statement is visited. This method is used to find the expression.
	 */
	public int visit(IASTExpression expr) {
		useDefSet(expr, rhs, null, -1);
		return PROCESS_SKIP;
	}

	/**
	 * 
	 * @param expr
	 *            The expression being analyzed
	 * @param side
	 *            lhs if it is defined, rhs otherwise (rhs)
	 * @param funcall
	 *            the function call expression
	 * @param index
	 *            the index of the current parameter in funcall
	 */
	public void useDefSet(IASTExpression expr, int side,
			IASTFunctionCallExpression funcall, int index) {
		// Null expression for empty function parameter
		if (expr == null)
			return;
		if (expr instanceof IASTAmbiguousExpression) {
		}
		else if (expr instanceof IASTArraySubscriptExpression) {
			IASTArraySubscriptExpression asE = (IASTArraySubscriptExpression) expr;
			if (side == rhs) {
				// = a[index_expr]
				useDefSet(asE.getArrayExpression(), rhs, funcall, index);
				useDefSet(asE.getSubscriptExpression(), rhs, funcall, index);
			} else { // lhs
				// a[b[i]] = ... , a is defined, b and i are used
				useDefSet(asE.getSubscriptExpression(), rhs, funcall, index);
				useDefSet(asE.getArrayExpression(), lhs, funcall, index);
			}
		}
		else if (expr instanceof IASTBinaryExpression) {
			IASTBinaryExpression biE = (IASTBinaryExpression) expr;
			int op = biE.getOperator();
			if (op == IASTBinaryExpression.op_assign) {
				// x = y = z is right associative --> x = (y = z)
				// So the "side" will be always rhs
				useDefSet(biE.getOperand1(), lhs, funcall, index);
				useDefSet(biE.getOperand2(), rhs, funcall, index);
			}
			else if (op == IASTBinaryExpression.op_multiplyAssign ||
					op == IASTBinaryExpression.op_divideAssign ||
					op == IASTBinaryExpression.op_moduloAssign ||
					op == IASTBinaryExpression.op_plusAssign ||
					op == IASTBinaryExpression.op_minusAssign ||
					op == IASTBinaryExpression.op_shiftLeftAssign ||
					op == IASTBinaryExpression.op_shiftRightAssign ||
					op == IASTBinaryExpression.op_binaryAndAssign ||
					op == IASTBinaryExpression.op_binaryXorAssign ||
					op == IASTBinaryExpression.op_binaryOrAssign) {
				useDefSet(biE.getOperand1(), rhs, funcall, index);
				useDefSet(biE.getOperand2(), rhs, funcall, index);
				useDefSet(biE.getOperand1(), lhs, funcall, index);
			}
			else {
				useDefSet(biE.getOperand1(), rhs, funcall, index);
				useDefSet(biE.getOperand2(), rhs, funcall, index);
			}
		}
		else if (expr instanceof IASTCastExpression) {
			IASTCastExpression castE = (IASTCastExpression) expr;
			useDefSet(castE.getOperand(), side, funcall, index);
		}
		else if (expr instanceof IASTConditionalExpression) {
			IASTConditionalExpression condE = (IASTConditionalExpression) expr;
			if (side == rhs) {
				useDefSet(condE.getLogicalConditionExpression(), rhs, funcall, index);
				useDefSet(condE.getPositiveResultExpression(), rhs, funcall, index);
				useDefSet(condE.getNegativeResultExpression(), rhs, funcall, index);
			} else {
				// eg. x > y ? x : y = 1
				useDefSet(condE.getLogicalConditionExpression(), rhs, funcall, index);
				useDefSet(condE.getPositiveResultExpression(), lhs, funcall, index);
				useDefSet(condE.getNegativeResultExpression(), lhs, funcall, index);
			}
		}
		else if (expr instanceof IASTExpressionList) {
			IASTExpressionList exprList = (IASTExpressionList) expr;
			IASTExpression[] exprs = exprList.getExpressions();
			for (int i = 0; i < exprs.length; i++) {
				if (funcall != null)
					useDefSet(exprs[i], side, funcall, i);
				else
					useDefSet(exprs[i], side, funcall, index);
			}
		}
		else if (expr instanceof IASTFieldReference) {
			IASTFieldReference frE = (IASTFieldReference) expr;
			useDefSet(frE.getFieldOwner(), side, funcall, index);
		}
		else if (expr instanceof IASTFunctionCallExpression) {
			IASTFunctionCallExpression funcE = (IASTFunctionCallExpression) expr;
			IASTExpression funcname = funcE.getFunctionNameExpression();
			String signature = funcname.getRawSignature();
			MPICallGraphNode n = (MPICallGraphNode) cg_.getNode(currentFunc_.getFileName(), signature);
			if (n != null) {
				gdef_ = Util.Union(gdef_, n.getGlobalDef());
				guse_ = Util.Union(guse_, n.getGlobalUse());
				def_ = Util.Union(def_, n.getGlobalDef());
				use_ = Util.Union(use_, n.getGlobalUse());
			}
			IASTExpression paramE = funcE.getParameterExpression();
			if (paramE == null)
				return;
			if (paramE instanceof IASTExpressionList)
				useDefSet(funcE.getParameterExpression(), side, funcE, -1);
			else
				useDefSet(funcE.getParameterExpression(), side, funcE, 0);
		}
		else if (expr instanceof IASTIdExpression) {
			IASTIdExpression id = (IASTIdExpression) expr;
			IASTName name = id.getName();
			String var = name.toString();
			if (var.startsWith("MPI_"))return; //$NON-NLS-1$
			if (side == rhs) {
				if (!use_.contains(var))
					use_.add(var);
				if (cg_.getEnv().contains(var) && !guse_.contains(var))
					guse_.add(var);
				if (funcall != null) {
					if (isDefinedParam(funcall, index)) {
						if (!def_.contains(var))
							def_.add(var);
						if (cg_.getEnv().contains(var) && !gdef_.contains(var))
							gdef_.add(var);
						if (isPassableParam(var) && !padef_.contains(var))
							padef_.add(var);
					}
				}
			} else { // lhs
				if (!def_.contains(var))
					def_.add(var);
				if (cg_.getEnv().contains(var) && !gdef_.contains(var))
					gdef_.add(var);
				if (isPassableParam(var) && !padef_.contains(var))
					padef_.add(var);
			}
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
				useDefSet(uE.getOperand(), rhs, funcall, index);
				useDefSet(uE.getOperand(), lhs, funcall, index);
			} else {
				useDefSet(uE.getOperand(), side, funcall, index);
			}
		}
		else if (expr instanceof ICASTTypeIdInitializerExpression) {
		}
		else {
		}
	}

	private boolean isPassableParam(String name) {
		IASTFunctionDefinition fdef = currentFunc_.getFuncDef();
		IASTFunctionDeclarator fdecl = fdef.getDeclarator();
		if (fdecl instanceof IASTStandardFunctionDeclarator) {
			IASTStandardFunctionDeclarator sfunc = (IASTStandardFunctionDeclarator) fdecl;
			IASTParameterDeclaration[] params = sfunc.getParameters();
			for (int i = 0; i < params.length; i++) {
				IASTName param = params[i].getDeclarator().getName();
				IASTPointerOperator[] pops = params[i].getDeclarator().getPointerOperators();
				if (name.equals(param.toString()) && pops != IASTPointerOperator.EMPTY_ARRAY) {
					return true;
				}
			}
		} else {
			ICASTKnRFunctionDeclarator krfunc = (ICASTKnRFunctionDeclarator) fdecl;
			IASTName[] params = krfunc.getParameterNames();
			for (int i = 0; i < params.length; i++) {
				if (name.equals(params[i].toString())) {
					IASTDeclarator decl = krfunc.getDeclaratorForParameterName(params[i]);
					if (decl.getPointerOperators() != IASTPointerOperator.EMPTY_ARRAY)
						return true;
				}
			}
		}
		return false;
	}

	private boolean isDefinedParam(IASTFunctionCallExpression fE, int index) {
		if (index == -1)
			return false;
		IASTExpression funcname = fE.getFunctionNameExpression();
		String signature = funcname.getRawSignature();
		MPICallGraphNode node = (MPICallGraphNode) cg_.getNode(currentFunc_.getFileName(), signature);
		if (node != null) {
			List<String> padef = node.getParamDef();
			IASTFunctionDefinition fdef = node.getFuncDef();
			IASTFunctionDeclarator fdecl = fdef.getDeclarator();
			if (fdecl instanceof IASTStandardFunctionDeclarator) {
				IASTStandardFunctionDeclarator sfunc = (IASTStandardFunctionDeclarator) fdecl;
				IASTParameterDeclaration[] params = sfunc.getParameters();
				if (params.length <= index)
					return false;
				IASTName param = params[index].getDeclarator().getName();
				if (padef.contains(param.toString()))
					return true;
			} else {
				ICASTKnRFunctionDeclarator krfunc = (ICASTKnRFunctionDeclarator) fdecl;
				IASTName[] params = krfunc.getParameterNames();
				if (params.length <= index)
					return false;
				IASTName param = params[index];
				if (padef.contains(param.toString()))
					return true;
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
					return true;
			}
			else {
				IType type = parameterE.getExpressionType();
				if (type instanceof IArrayType ||
						type instanceof IPointerType)
					return true;
			}
		}
		return false;
	}

	/**
	 * All parameters of a function and used globals are assumed to be
	 * defined at the entry block.
	 */
	private void setEntryBlock(MPIBlock entry) {
		List<String> def = new ArrayList<String>();
		IASTFunctionDefinition fdef = currentFunc_.getFuncDef();
		IASTFunctionDeclarator fdecl = fdef.getDeclarator();
		if (fdecl instanceof IASTStandardFunctionDeclarator) {
			IASTStandardFunctionDeclarator sfunc = (IASTStandardFunctionDeclarator) fdecl;
			IASTParameterDeclaration[] params = sfunc.getParameters();
			for (int i = 0; i < params.length; i++) {
				IASTName param = params[i].getDeclarator().getName();
				def.add(param.toString());
			}
		} else {
			ICASTKnRFunctionDeclarator krfunc = (ICASTKnRFunctionDeclarator) fdecl;
			IASTName[] params = krfunc.getParameterNames();
			for (int i = 0; i < params.length; i++) {
				IASTName param = params[i];
				def.add(param.toString());
			}
		}
		for (Iterator<String> i = currentFunc_.getGlobalUse().iterator(); i.hasNext();) {
			def.add(i.next());
		}
		entry.setDef(def);
		entry.setUse(new ArrayList<String>());
	}

	/**
	 * All defined global variables and defined passable parameters are assumed
	 * to be used in the exit block
	 */
	private void setExitBlock(MPIBlock exit) {
		List<String> use = new ArrayList<String>();
		for (Iterator<String> i = currentFunc_.getGlobalDef().iterator(); i.hasNext();) {
			use.add(i.next());
		}
		for (Iterator<String> i = currentFunc_.getParamDef().iterator(); i.hasNext();) {
			use.add(i.next());
		}

		exit.setUse(use);
		exit.setDef(new ArrayList<String>());
	}
}
