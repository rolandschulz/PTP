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
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.BarrierTable.BarrierInfo;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.ICallGraphNode;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.impl.CallGraph;

public class MPICallGraph extends CallGraph {
	/** Is the global variable pointer type? */
	protected List<Boolean> gvPointer_;
	protected BarrierTable btable_;
	protected int count;
	private static final boolean traceOn = false;

	public MPICallGraph()
	{
		super();
		btable_ = new BarrierTable();
		count = 4; // 0-3 are types in BarrierExpression
		gvPointer_ = new ArrayList<Boolean>();
	}

	public List<Boolean> getGVPointer() {
		return gvPointer_;
	}

	public BarrierTable getBarrierTable() {
		return btable_;
	}

	public void otherOP() {
		// FunctionPointerChecker fpc = new FunctionPointerChecker();
		// fpc.run();
		FunctionMarker fm = new FunctionMarker();
		fm.run();
		checkBarrierRelatedCycles();
		if (traceOn)
			System.out.println((btable_.commCounter + 1) + " communicators "); //$NON-NLS-1$
	}

	class FunctionPointerChecker extends ASTVisitor {
		protected ICallGraphNode currentFunc_;
		protected boolean funcPointer = false;

		public boolean hasFuncPointer() {
			return funcPointer;
		}

		public void run() {
			this.shouldVisitDeclarations = true;
			this.shouldVisitStatements = true;
			this.shouldVisitExpressions = true;
			for (Iterator<ICallGraphNode> i = nodes_.iterator(); i.hasNext();) {
				currentFunc_ = i.next();
				currentFunc_.getFuncDef().accept(this);
			}
		}

		protected Stack<String> funcCall = new Stack<String>();

		public int visit(IASTExpression expr) {
			if (expr instanceof IASTFunctionCallExpression) {
				IASTFunctionCallExpression funcE = (IASTFunctionCallExpression) expr;
				IASTExpression funcname = funcE.getFunctionNameExpression();
				String signature = funcname.getRawSignature();
				funcCall.push(signature);
			}
			else if (expr instanceof IASTIdExpression) {
				IASTIdExpression idE = (IASTIdExpression) expr;
				String name = idE.getName().toString();
				if (!funcCall.empty() && name.equals(funcCall.peek())) {
					funcCall.pop();
				} else {
					ICallGraphNode node = getNode(currentFunc_.getFileName(), name);
					if (node != null) {
						funcPointer = true;
						System.out.println("Function Pointer " + name + " in " + //$NON-NLS-1$ //$NON-NLS-2$
								currentFunc_.getFuncName() + "(" + currentFunc_.getFileName() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			}
			return PROCESS_CONTINUE;
		}
	}

	class FunctionMarker extends ASTVisitor {
		protected MPICallGraphNode currentNode_ = null;

		public void run()
		{
			this.shouldVisitExpressions = true;
			this.shouldVisitStatements = true;
			this.shouldVisitDeclarations = true;

			for (Iterator<ICallGraphNode> i = nodes_.iterator(); i.hasNext();) {
				currentNode_ = (MPICallGraphNode) i.next();
				// System.out.println(currentNode_.getFuncName());
				currentNode_.getFuncDef().accept(this);
			}

			for (Iterator<ICallGraphNode> i = nodes_.iterator(); i.hasNext();) {
				currentNode_ = (MPICallGraphNode) i.next();
				// System.out.println(currentNode_.getFuncName());
				markBarrierRelated(currentNode_);
			}
			for (Iterator<ICallGraphNode> i = nodes_.iterator(); i.hasNext();) {
				currentNode_ = (MPICallGraphNode) i.next();
				markMPIInit(currentNode_);
			}

			markFunction();
			count = count - 4;
		}

		public int visit(IASTExpression expression)
		{
			if (expression instanceof IASTFunctionCallExpression) {
				IASTFunctionCallExpression funcExpr = (IASTFunctionCallExpression) expression;
				IASTExpression funcname = funcExpr.getFunctionNameExpression();
				String signature = funcname.getRawSignature();
				if (signature.equals("MPI_Barrier")) { //$NON-NLS-1$
					count++;
					BarrierInfo newbar = btable_.addBarrier(funcExpr, count,
							currentNode_.getResource(), currentNode_.getFuncName());
					currentNode_.setBarrierRelated(true);
					currentNode_.getAllBarriers().add(newbar);
				}
				else if (signature.equals("MPI_Init")) { //$NON-NLS-1$
					currentNode_.mpiInit = true;
				}
			}
			return PROCESS_CONTINUE;
		}

		/*
		 * Functions that directly call barriers (MPI_Barrier()) and their
		 * ancestors are barrier-related
		 */
		protected void markBarrierRelated(MPICallGraphNode node) {
			if (node.barrierRelated()) {
				// System.out.println(node.getName());
				for (Iterator<ICallGraphNode> ii = node.getCallers().iterator(); ii.hasNext();) {
					MPICallGraphNode caller = (MPICallGraphNode) ii.next();
					if (!caller.barrierRelated()) {
						caller.setBarrierRelated(true);
						markBarrierRelated(caller);
					}
				}
			}
		}

		protected void markMPIInit(MPICallGraphNode node) {
			if (node.mpiInit) {
				for (Iterator<ICallGraphNode> ii = node.getCallers().iterator(); ii.hasNext();) {
					MPICallGraphNode caller = (MPICallGraphNode) ii.next();
					if (!caller.mpiInit) {
						caller.mpiInit = true;
						markMPIInit(caller);
					}
				}
			}
		}

		/**
		 * A function is marked if:<br>
		 * (1) It is a barrier-related function<br>
		 * (2) It is called by (1)<br>
		 * (3) It is an MPI Initialization function (call MPI_Init)<br>
		 * (4) It is an ancestor of (3)<br>
		 * (5) It is called by (4)
		 */
		protected void markFunction() {
			int total = nodes_.size();
			int marked = 0;

			for (ICallGraphNode node = topEntry_; node != null; node = node.topNext()) {
				currentNode_ = (MPICallGraphNode) node;
				if (currentNode_.barrierRelated() || currentNode_.mpiInit) {
					currentNode_.marked = true;
					marked++;
				}
			}

			boolean changed = true;
			while (changed) {
				changed = false;
				for (ICallGraphNode node = topEntry_; node != null; node = node.topNext()) {
					currentNode_ = (MPICallGraphNode) node;
					if (currentNode_.marked) {
						for (Iterator<ICallGraphNode> i = currentNode_.getCallees().iterator(); i.hasNext();) {
							MPICallGraphNode callee = (MPICallGraphNode) i.next();
							if (!callee.marked) {
								callee.marked = true;
								marked++;
								changed = true;
							}
						}
					}
				}
			}
			if (traceOn)
				System.out.println(marked + " out of " + total + " functions are marked"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	protected void checkBarrierRelatedCycles() {

		for (Iterator<List<ICallGraphNode>> i = cycles_.iterator(); i.hasNext();) {
			List<ICallGraphNode> cycle = i.next();
			boolean barrierRelated = false;
			for (Iterator<ICallGraphNode> ii = cycle.iterator(); ii.hasNext();) {
				MPICallGraphNode node = (MPICallGraphNode) ii.next();
				if (node.barrierRelated) {
					barrierRelated = true;
					break;
				}
			}
			if (!barrierRelated)
				continue;
			String errorMsg = new String("Barrier related recursive function calls on: "); //$NON-NLS-1$
			for (Iterator<ICallGraphNode> ii = cycle.iterator(); ii.hasNext();) {
				ICallGraphNode func = ii.next();
				errorMsg = errorMsg + func.getFuncName() + "(" + func.getFileName() + "), "; //$NON-NLS-1$ //$NON-NLS-2$
			}
			System.out.println(errorMsg);
		}
	}
}
