/**********************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation.
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
import java.util.Hashtable;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.core.resources.IResource;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.ICallGraphNode;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.IControlFlowGraph;

/**
 * Implementation of a call graph node, which represents a user-defined
 * function
 * 
 */
public class CallGraphNode implements ICallGraphNode {
	protected IResource resource_;
	protected String fileName_;
	protected String funcName_;
	protected IASTFunctionDefinition fdef_;
	protected List<ICallGraphNode> callers_;
	protected List<ICallGraphNode> callees_;
	protected boolean nested; // nested function declaration?
	protected IControlFlowGraph CFG_;

	/** next node in reverse-topological (bottom-up) order */
	protected ICallGraphNode botNext_;
	/** next node in topological (top-down) order */
	protected ICallGraphNode topNext_;
	protected Hashtable<String, Object> attrs_;

	protected boolean recursive;

	public CallGraphNode(IResource resource, String filename,
			String funcname, IASTFunctionDefinition fdef) {
		resource_ = resource;
		fileName_ = filename;
		funcName_ = funcname;
		fdef_ = fdef;
		init();
	}

	public CallGraphNode(IResource resource, String filename,
			IASTFunctionDefinition fdef) {
		resource_ = resource;
		fileName_ = filename;
		fdef_ = fdef;
		funcName_ = getFuncName(fdef);
		init();
	}

	private void init() {
		callers_ = new ArrayList<ICallGraphNode>();
		callees_ = new ArrayList<ICallGraphNode>();
		nested = false;
		CFG_ = null;
		topNext_ = null;
		botNext_ = null;
		attrs_ = new Hashtable<String, Object>();
		recursive = false;
	}

	private String getFuncName(IASTFunctionDefinition fdef) {
		IASTDeclarator fdecl = (IASTDeclarator) fdef.getDeclarator();
		return new String(((IASTName) fdecl.getName()).toCharArray());
	}

	public String getFuncName() {
		return funcName_;
	}

	public String getFileName() {
		return fileName_;
	}

	public IResource getResource() {
		return resource_;
	}

	public IASTFunctionDefinition getFuncDef() {
		return fdef_;
	}

	public boolean isNested() {
		return nested;
	}

	public List<ICallGraphNode> getCallers() {
		return callers_;
	}

	public void addCaller(ICallGraphNode caller) {
		if (!callers_.contains(caller))
			callers_.add(caller);
	}

	public List<ICallGraphNode> getCallees() {
		return callees_;
	}

	public void addCallee(ICallGraphNode callee) {
		if (!callees_.contains(callee))
			callees_.add(callee);
	}

	/** Returns next node in reverse-topological (bottom-up) order */
	public ICallGraphNode botNext() {
		return botNext_;
	}

	public void setBotNext(ICallGraphNode node) {
		botNext_ = node;
	}

	public IControlFlowGraph getCFG() {
		return CFG_;
	}

	/** Returns next node in topological (top-down) order */
	public ICallGraphNode topNext() {
		return topNext_;
	}

	public void setTopNext(ICallGraphNode node) {
		topNext_ = node;
	}

	public void setCFG(IControlFlowGraph cfg) {
		CFG_ = cfg;
	}

	public void setAttr(String name, Object attr) {
		attrs_.put(name, attr);
	}

	public Object getAttr(String name) {
		return attrs_.get(name);
	}

	public void removeAttr(String name) {
		attrs_.remove(name);
	}

	public void setRecursive(boolean val) {
		recursive = val;
	}

	public boolean isRecursive() {
		return recursive;
	}

}
