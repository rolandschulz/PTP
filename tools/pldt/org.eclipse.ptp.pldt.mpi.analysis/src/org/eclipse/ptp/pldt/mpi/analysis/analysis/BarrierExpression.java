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

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

/**
 * "A Barrier Expression is a special form of a path expression that, for a
 * given program point, describes the sequences of barriers that may execute
 * until a thread reaches that point. Barrier Expressions provide a compact
 * representation of the synchronization structure of the program."
 * 
 * -- From Zhang/Duesterwald paper
 * 
 * @author Yuan Zhang
 * 
 */
public class BarrierExpression {
	protected BarrierExpressionOP op_;
	protected BarrierExpression operand1_;
	protected BarrierExpression operand2_;

	protected int length;
	/** 'Top' is symbol for infinite length, or error */
	public static final int TOP = -3;

	/* For instrumentation */
	public static int count_branch = 0;
	public static int count_repeat = 0;
	public static int count_node = 0;

	boolean visited;
	boolean ceVisited; // visited in counter example construction

	/**
	 * There are four terminals: <br>
	 * barrier id(int > 0) <br>
	 * function name (String) <br>
	 * \top -- undecidable <br>
	 * \bot -- no barrier
	 */
	protected int type;
	/** type is bottom - no barrier */
	public static final int BE_bot = 1;
	/** type is barrier ID (int > 0) */
	public static final int BE_ID = 2;
	/** type is function name (String) */
	public static final int BE_func = 3;

	protected int barrierID;
	protected String funcName_;

	protected BarrierExpression parent_;

	/** Has the mismatching error already reported? */
	protected boolean error = false;

	public BarrierExpression() {
		init();
	}

	public BarrierExpression(int operator, IASTExpression cond,
			IASTStatement stmt) {
		init();
		op_ = new BarrierExpressionOP(operator, cond, stmt);
		if (op_.getOperator() == BarrierExpressionOP.op_branch)
			count_branch++;
		else if (op_.getOperator() == BarrierExpressionOP.op_repeat)
			count_repeat++;
	}

	public BarrierExpression(String func) {
		init();
		type = BE_func;
		funcName_ = func;
		count_node++;
	}

	public BarrierExpression(int id) {
		init();
		if (id == BE_bot)
			type = id;
		else {
			type = BE_ID;
			barrierID = id;
		}
		count_node++;
	}

	private void init() {
		op_ = null;
		operand1_ = null;
		operand2_ = null;
		length = -1;
		type = -1;
		barrierID = 0;
		funcName_ = null;
		parent_ = null;
		visited = false;
		ceVisited = false;
	}

	public void setOperand1(BarrierExpression op1) {
		op1.setParent(this);
		operand1_ = op1;
	}

	public void setOperand2(BarrierExpression op2) {
		op2.setParent(this);
		operand2_ = op2;
	}

	public BarrierExpression getOP1() {
		return operand1_;
	}

	public BarrierExpression getOP2() {
		return operand2_;
	}

	public BarrierExpression getParent() {
		return parent_;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int l) {
		length = l;
	}

	public void setParent(BarrierExpression parent) {
		this.parent_ = parent;
	}

	public boolean isBot() {
		return type == BE_bot;
	}

	public boolean isBarrier() {
		return type == BE_ID;
	}

	public boolean isFunc() {
		return type == BE_func;
	}

	public BarrierExpressionOP getOP() {
		return op_;
	}

	public int getBarrierID() {
		return barrierID;
	}

	public String getFuncName() {
		return funcName_;
	}

	public void setErrorFlag(boolean val) {
		error = val;
	}

	public boolean getErrorFlag() {
		return error;
	}

	/**
	 * @return: BE = BE1.BE2
	 */
	public static BarrierExpression concatBE(BarrierExpression BE1, BarrierExpression BE2) {
		BarrierExpression be = null;
		if (BE1.isBot()) {
			be = BE2;
		} else if (BE2.isBot()) {
			be = BE1;
		} else {
			be = new BarrierExpression(BarrierExpressionOP.op_concat, null, null);
			be.setOperand1(BE1);
			be.setOperand2(BE2);
		}
		return be;
	}

	public static BarrierExpression concatBE(BarrierExpression BE1,
			BarrierExpression BE2,
			BarrierExpression BE3) {
		return concatBE(concatBE(BE1, BE2), BE3);
	}

	/**
	 * @return: BE' = BE*
	 */
	public static BarrierExpression repeatBE(BarrierExpression BE,
			IASTExpression cond, IASTStatement stmt) {
		BarrierExpression be = null;
		if (BE.isBot()) {
			be = BE;
		} else {
			be = new BarrierExpression(
					BarrierExpressionOP.op_repeat, cond, stmt);
			be.setOperand1(BE);
		}
		return be;
	}

	/**
	 * @return: BE = BE1 | BE2
	 */
	public static BarrierExpression branchBE(BarrierExpression BE1,
			BarrierExpression BE2,
			IASTExpression cond,
			IASTStatement stmt) {
		BarrierExpression be = null;
		if (BE1.isBot() && BE2.isBot()) {
			be = BE2;
		} else {
			be = new BarrierExpression(
					BarrierExpressionOP.op_branch, cond, stmt);
			be.setOperand1(BE1);
			be.setOperand2(BE2);
		}
		return be;
	}

	public String prettyPrinter() {
		if (op_ != null && op_.operator == BarrierExpressionOP.op_concat) {
			return operand1_.prettyPrinter() + "." + operand2_.prettyPrinter(); //$NON-NLS-1$
		} else if (op_ != null && op_.operator == BarrierExpressionOP.op_repeat) {
			return "(" + operand1_.prettyPrinter() + ")*"; //$NON-NLS-1$ //$NON-NLS-2$
		} else if (op_ != null && op_.operator == BarrierExpressionOP.op_branch) {
			return "((" + operand1_.prettyPrinter() + ") | (" + operand2_.prettyPrinter() + "))"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} else if (type == BE_ID) {
			Integer i = new Integer(barrierID);
			return i.toString();
		} else if (type == BE_func) {
			return funcName_;
		} else if (type == BE_bot) {
			return "(\\bot)"; //$NON-NLS-1$
		}
		return null;
	}

	public String toString() {
		return prettyPrinter();
	}

	/**
	 * Barrier Expression Operator
	 */
	public class BarrierExpressionOP
	{
		public static final int op_concat = 1;
		public static final int op_repeat = 2;
		public static final int op_branch = 3;
		private int operator;
		private IASTExpression condition_;
		private IASTStatement condStmt_;

		public BarrierExpressionOP() {
			operator = 0;
			condition_ = null;
		}

		public BarrierExpressionOP(int op, IASTExpression cond, IASTStatement stmt) {
			operator = op;
			condition_ = cond;
			condStmt_ = stmt;
		}

		public boolean withCondition() {
			return operator == op_branch | operator == op_repeat;
		}

		public int getOperator() {
			return operator;
		}

		public IASTExpression getCondition() {
			return condition_;
		}

		public IASTStatement getStatement() {
			return condStmt_;
		}
	}

}
