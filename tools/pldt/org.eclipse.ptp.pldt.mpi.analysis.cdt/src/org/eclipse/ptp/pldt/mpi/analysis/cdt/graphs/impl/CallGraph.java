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

package org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.impl;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.ICallGraph;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.ICallGraphNode;

public class CallGraph implements ICallGraph {
	/** list of call graph nodes */
	protected List<ICallGraphNode> nodes_;
	/** list of global variable names */
	protected List<String> env_;

	/** the entry node according to topological order */
	protected ICallGraphNode topEntry_;
	/** the entry node according to the reverse topological order */
	protected ICallGraphNode botEntry_;
	/** list of cycles in the call graph */
	protected List<List<ICallGraphNode>> cycles_;

	public CallGraph() {
		nodes_ = new ArrayList<ICallGraphNode>();
		env_ = new ArrayList<String>();
		topEntry_ = null;
		botEntry_ = null;
		cycles_ = new ArrayList<List<ICallGraphNode>>();
	}

	public void addNode(ICallGraphNode node) {
		for (Iterator<ICallGraphNode> i = nodes_.iterator(); i.hasNext();) {
			ICallGraphNode n = i.next();
			if (n.getFuncName().equals(node.getFuncName()) &&
					n.getFileName().equals(node.getFileName()))
				return;
		}
		nodes_.add(node);
	}

	public List<ICallGraphNode> getAllNodes() {
		return nodes_;
	}

	public ICallGraphNode botEntry() {
		return botEntry_;
	}

	public List<List<ICallGraphNode>> getCycles() {
		return cycles_;
	}

	/** @return the enclosing function of an AST node */
	protected ICallGraphNode getEnclosingFunc(IASTNode n) {
		IASTNode parent = n.getParent();
		while (parent != null) {
			if (parent instanceof IASTFunctionDefinition)
				return getNode((IASTFunctionDefinition) parent);
			parent = parent.getParent();
		}
		return null;
	}

	public List<String> getEnv() {
		return env_;
	}

	/**
	 * @return the call graph node with the function name "funcName".
	 *         If there is more than one function with the same name (but
	 *         with different scopes), and there is one such function in the
	 *         file "fileName", then return this function node
	 */
	public ICallGraphNode getNode(String fileName, String funcName) {
		ICallGraphNode tmp = null;
		for (Iterator<ICallGraphNode> i = nodes_.iterator(); i.hasNext();) {
			ICallGraphNode node = i.next();
			if (node.getFuncName().equals(funcName)) {
				tmp = node;
				if (node.getFileName().equals(fileName))
					return node;
			}
		}
		return tmp;
	}

	public ICallGraphNode getNode(IASTFunctionDefinition fdef) {
		for (Iterator<ICallGraphNode> i = nodes_.iterator(); i.hasNext();) {
			ICallGraphNode node = i.next();
			if (node.getFuncDef() == fdef)
				return node;
		}
		return null;
	}

	public ICallGraphNode topEntry() {
		return topEntry_;
	}

	public void setBotEntry(ICallGraphNode node) {
		botEntry_ = node;
	}

	public void setTopEntry(ICallGraphNode node) {
		topEntry_ = node;
	}

	/**
	 * Constructing the call graph consists of three steps:
	 * (1) calculating caller and callee relations among functions.
	 * Note that call graph nodes have been collected before
	 * constructing the call graph
	 * (2) check the recursive function calls and collecting all cycles
	 * (3) other options for future extension
	 */
	public void buildCG() {
		CGBuilder builder = new CGBuilder();
		builder.run();
		checkRecursive();
		otherOP();
	}

	/**
	 * Call Graph Builder
	 *
	 */
	class CGBuilder extends ASTVisitor {
		ICallGraphNode currentNode_;

		public void run()
		{
			this.shouldVisitExpressions = true;
			this.shouldVisitStatements = true;
			this.shouldVisitDeclarations = true;

			for (Iterator<ICallGraphNode> i = nodes_.iterator(); i.hasNext();) {
				currentNode_ = i.next();
				IASTFunctionDefinition func = currentNode_.getFuncDef();
				func.accept(this);
				ICallGraphNode enclosingFuncNode = getEnclosingFunc(func);
				if (enclosingFuncNode != null) {
					// fd is nested declared function
					enclosingFuncNode.addCallee(currentNode_);
					currentNode_.addCaller(enclosingFuncNode);
				}
			}
		}

		public int visit(IASTExpression expression)
		{
			if (expression instanceof IASTFunctionCallExpression) {
				IASTFunctionCallExpression funcExpr = (IASTFunctionCallExpression) expression;
				IASTExpression funcname = funcExpr.getFunctionNameExpression();
				String signature = funcname.getRawSignature();
				// System.out.println(signature);
				ICallGraphNode fnode = getNode(currentNode_.getFileName(), signature);
				if (fnode != null) {
					/* This is a user-defined function call */
					currentNode_.addCallee(fnode);
					fnode.addCaller(currentNode_);
				}
			}
			return PROCESS_CONTINUE;
		}
	}

	protected Stack<ICallGraphNode> order;

	public void checkRecursive() {
		order = new Stack<ICallGraphNode>();
		DFS();
		RV_DFS();

		Hashtable<ICallGraphNode, List<ICallGraphNode>> recursions =
				new Hashtable<ICallGraphNode, List<ICallGraphNode>>();
		for (ICallGraphNode node = botEntry_; node != null; node = node.botNext()) {
			ICallGraphNode pi = (ICallGraphNode) node.getAttr("pi"); //$NON-NLS-1$
			if (pi != null) {
				if (recursions.containsKey(pi)) {
					List<ICallGraphNode> list = recursions.get(pi);
					list.add(node);
				} else {
					List<ICallGraphNode> list = new ArrayList<ICallGraphNode>();
					list.add(node);
					recursions.put(pi, list);
				}
			}
		}
		for (Enumeration<ICallGraphNode> e = recursions.keys(); e.hasMoreElements();) {
			ICallGraphNode root = e.nextElement();
			List<ICallGraphNode> list = recursions.get(root);
			if (!list.contains(root))
				list.add(root);
			cycles_.add(list);
			for (Iterator<ICallGraphNode> i = list.iterator(); i.hasNext();) {
				i.next().setRecursive(true);
			}
		}
		for (Iterator<ICallGraphNode> i = nodes_.iterator(); i.hasNext();) {
			ICallGraphNode node = i.next();
			if (node.getCallees().contains(node)) { // self-recursion
				List<ICallGraphNode> list = new ArrayList<ICallGraphNode>();
				list.add(node);
				cycles_.add(list);
				node.setRecursive(true);
			}
		}
	}

	/**
	 * Depth-First Search (?)
	 */
	protected void DFS() {
		for (Iterator<ICallGraphNode> i = nodes_.iterator(); i.hasNext();) {
			ICallGraphNode node = i.next();
			node.setAttr("color", new Integer(0)); //$NON-NLS-1$
			node.removeAttr("pi"); //$NON-NLS-1$
		}
		for (Iterator<ICallGraphNode> i = nodes_.iterator(); i.hasNext();) {
			ICallGraphNode node = i.next();
			int color = ((Integer) node.getAttr("color")).intValue(); //$NON-NLS-1$
			if (color == 0)
				DFSVisit(node);
		}
	}

	protected void DFSVisit(ICallGraphNode node) {
		node.setAttr("color", new Integer(1)); //$NON-NLS-1$
		for (Iterator<ICallGraphNode> i = node.getCallees().iterator(); i.hasNext();) {
			ICallGraphNode callee = i.next();
			int color = ((Integer) callee.getAttr("color")).intValue(); //$NON-NLS-1$
			if (color == 0) { // white
				callee.setAttr("pi", node); //$NON-NLS-1$
				DFSVisit(callee);
			}
		}
		order.push(node);
	}

	/** 
	 * Reverse Depth-First Search (?)
	 */
	protected void RV_DFS() {
		for (Iterator<ICallGraphNode> i = nodes_.iterator(); i.hasNext();) {
			ICallGraphNode node = i.next();
			node.setAttr("color", new Integer(0)); //$NON-NLS-1$
			node.removeAttr("pi"); //$NON-NLS-1$
		}

		ICallGraphNode n = null;
		ICallGraphNode m = null;
		try{
			topEntry_ = order.peek();
		}catch (EmptyStackException e) {
			System.out.println("EmptyStackException in CallGraph.RV_DFS but continuing.  Probably due to non-.c file, which is not supported for MPI barrier analysis.");
			//topEntry_=null; // is this valid??
			return;
		}
		while (!order.empty()) {
			n = order.pop();
			n.setBotNext(m);
			if (m != null)
				m.setTopNext(n);
			m = n;
			int color = ((Integer) n.getAttr("color")).intValue(); //$NON-NLS-1$
			if (color == 0) {
				RV_DFSVisit(n);
			}
		}
		botEntry_ = m;

		for (n = botEntry_; n != null; n = n.botNext()) {
			ICallGraphNode pred = (ICallGraphNode) n.getAttr("pi"); //$NON-NLS-1$
			ICallGraphNode temp = null;
			while (pred != null) {
				temp = pred;
				pred = (ICallGraphNode) pred.getAttr("pi"); //$NON-NLS-1$
			}
			if (temp != null)
				n.setAttr("pi", temp); //$NON-NLS-1$
		}
	}

	protected void RV_DFSVisit(ICallGraphNode node) {
		node.setAttr("color", new Integer(1)); //$NON-NLS-1$
		for (Iterator<ICallGraphNode> i = node.getCallers().iterator(); i.hasNext();) {
			ICallGraphNode caller = i.next();
			int color = ((Integer) caller.getAttr("color")).intValue(); //$NON-NLS-1$
			if (color == 0) { // white
				caller.setAttr("pi", node); //$NON-NLS-1$
				RV_DFSVisit(caller);
			}
		}
	}

	public void otherOP() {
	}

	public void print() {
		for (Iterator<ICallGraphNode> i = nodes_.iterator(); i.hasNext();) {
			ICallGraphNode node = i.next();
			System.out.print(node.getFuncName() + " calls: "); //$NON-NLS-1$
			for (Iterator<ICallGraphNode> ii = node.getCallees().iterator(); ii.hasNext();) {
				ICallGraphNode callee = ii.next();
				System.out.println(callee.getFuncName() + ", "); //$NON-NLS-1$
			}
			System.out.println(""); //$NON-NLS-1$
			System.out.println(""); //$NON-NLS-1$
		}
	}
}
