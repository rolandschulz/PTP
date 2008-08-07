/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    IBM Corporation
 *******************************************************************************/ 

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.ui/src
 * Class: org.eclipse.cdt.internal.ui.typehierarchy.THGraph
 * Version: 1.14
 */
package org.eclipse.ptp.internal.rdt.core.typehierarchy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.internal.rdt.core.index.IndexQueries;
import org.eclipse.ptp.rdt.core.RDTLog;

public class THGraph implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private static final ICElement[] NO_MEMBERS = new ICElement[0];
	private THGraphNode fInputNode= null;
	private HashSet<THGraphNode> fRootNodes= new HashSet<THGraphNode>();
	private HashSet<THGraphNode> fLeaveNodes= new HashSet<THGraphNode>();
	private HashMap<ICElement, THGraphNode> fNodes= new HashMap<ICElement, THGraphNode>();
	private boolean fFileIsIndexed;
	
	private transient IIndexLocationConverter fConverter;
	
	public THGraph() {
	}

	public THGraphNode getInputNode() {
		return fInputNode;
	}
	
	public THGraphNode getNode(ICElement elem) {
		return fNodes.get(elem);
	}

	public void setLocationConverter(IIndexLocationConverter converter) {
		fConverter = converter;
	}
	
	private THGraphNode addNode(ICElement input) {
		THGraphNode node= fNodes.get(input); 

		if (node == null) {
			node= new THGraphNode(input);
			fNodes.put(input, node);
			fRootNodes.add(node);
			fLeaveNodes.add(node);
		}
		return node;
	}
	
	private THGraphEdge addEdge(THGraphNode from, THGraphNode to) {
		if (createsLoopOrIsDuplicate(from, to)) {
			return null;
		}
		THGraphEdge edge= new THGraphEdge(from, to);
		from.startEdge(edge);
		to.endEdge(edge);
		fRootNodes.remove(to);
		fLeaveNodes.remove(from);
		return edge;
	}

	private boolean createsLoopOrIsDuplicate(THGraphNode from, THGraphNode to) {
		if (from == to) {
			return true;
		}
		if (to.getOutgoing().isEmpty() || from.getIncoming().isEmpty()) {
			return false;
		}
		
		HashSet<THGraphNode> checked= new HashSet<THGraphNode>();
		ArrayList<THGraphNode> stack= new ArrayList<THGraphNode>();
		stack.add(to);
		
		while (!stack.isEmpty()) {
			THGraphNode node= stack.remove(stack.size()-1);
			List out= node.getOutgoing();
			for (Iterator iterator = out.iterator(); iterator.hasNext();) {
				THGraphEdge	edge= (THGraphEdge) iterator.next();
				node= edge.getEndNode();
				if (node == from) {
					return true;
				}
				if (checked.add(node)) {
					stack.add(node);
				}
			}
		}
		// check if edge is already there.
		List out= from.getOutgoing();
		for (Iterator iterator = out.iterator(); iterator.hasNext();) {
			THGraphEdge edge = (THGraphEdge) iterator.next();
			if (edge.getEndNode() == to) {
				return true;
			}
		}
		return false;
	}

	public Collection<THGraphNode> getRootNodes() {
		return fRootNodes;
	}

	public Collection<THGraphNode> getLeaveNodes() {
		return fLeaveNodes;
	}

	public void defineInputNode(IIndex index, ICElement input) {
		if (input != null) {
			try {
				fFileIsIndexed= true;
				input= IndexQueries.attemptConvertionToHandle(index, input, fConverter);
				fInputNode= addNode(input);
			} catch (CoreException e) {
				RDTLog.logError(e);
			}
		}
	}

	public void addSuperClasses(IIndex index, IProgressMonitor monitor) {
		if (fInputNode == null) {
			return;
		}
		HashSet<ICElement> handled= new HashSet<ICElement>();
		ArrayList<ICElement> stack= new ArrayList<ICElement>();
		stack.add(fInputNode.getElement());
		handled.add(fInputNode.getElement());
		while (!stack.isEmpty()) {
			if (monitor.isCanceled()) {
				return;
			}
			ICElement elem= stack.remove(stack.size()-1);
			THGraphNode graphNode= addNode(elem);
			try {
				IIndexBinding binding = IndexQueries.elementToBinding(index, elem);
				if (binding != null) {
					addMembers(index, graphNode, binding);
				}
				if (binding instanceof ICPPClassType) {
					ICPPClassType ct= (ICPPClassType) binding;
					ICPPBase[] bases= ct.getBases();
					for (int i = 0; i < bases.length; i++) {
						if (monitor.isCanceled()) {
							return;
						}
						ICPPBase base= bases[i];
						IName name= base.getBaseClassSpecifierName();
						IBinding basecl= name != null ? index.findBinding(name) : base.getBaseClass();
						ICElement[] baseElems= IndexQueries.findRepresentative(index, basecl, fConverter);
						for (int j = 0; j < baseElems.length; j++) {
							ICElement baseElem = baseElems[j];
							THGraphNode baseGraphNode= addNode(baseElem);
							addMembers(index, baseGraphNode, basecl);							
							addEdge(graphNode, baseGraphNode);
							if (handled.add(baseElem)) {
								stack.add(baseElem);
							}
						}
					}
				}
				else if (binding instanceof ITypedef) {
					ITypedef ct= (ITypedef) binding;
					IType type= ct.getType();
					if (type instanceof IBinding) {
						IBinding basecl= (IBinding) type;
						ICElement[] baseElems= IndexQueries.findRepresentative(index, basecl, fConverter);
						if (baseElems.length > 0) {
							ICElement baseElem= baseElems[0];
							THGraphNode baseGraphNode= addNode(baseElem);
							addMembers(index, baseGraphNode, basecl);							
							addEdge(graphNode, baseGraphNode);
							if (handled.add(baseElem)) {
								stack.add(baseElem);
							}
						}
					}
				}
			} catch (DOMException e) {
				// index bindings should not throw this kind of exception, might as well log it.
				RDTLog.logError(e);
			} catch (CoreException e) {
				RDTLog.logError(e);
			}
		}
	}

	public void addSubClasses(IIndex index, IProgressMonitor monitor) {
		if (fInputNode == null) {
			return;
		}
		HashSet<ICElement> handled= new HashSet<ICElement>();
		ArrayList<ICElement> stack= new ArrayList<ICElement>();
		ICElement element = fInputNode.getElement();
		stack.add(element);
		handled.add(element);
		while (!stack.isEmpty()) {
			if (monitor.isCanceled()) {
				return;
			}
			ICElement elem= stack.remove(stack.size()-1);
			THGraphNode graphNode= addNode(elem);
			try {
				IBinding binding = IndexQueries.elementToBinding(index, elem);
				if (binding != null) {
					IIndexName[] names= index.findNames(binding, IIndex.FIND_REFERENCES | IIndex.FIND_DEFINITIONS);
					for (int i = 0; i < names.length; i++) {
						if (monitor.isCanceled()) {
							return;
						}
						IIndexName indexName = names[i];
						if (indexName.isBaseSpecifier()) {
							IIndexName subClassDef= indexName.getEnclosingDefinition();
							if (subClassDef != null) {
								IBinding subClass= index.findBinding(subClassDef);
								ICElement[] subClassElems= IndexQueries.findRepresentative(index, subClass, fConverter);
								if (subClassElems.length > 0) {
									ICElement subClassElem= subClassElems[0];
									THGraphNode subGraphNode= addNode(subClassElem);
									addMembers(index, subGraphNode, subClass);							
									addEdge(subGraphNode, graphNode);
									if (handled.add(subClassElem)) {
										stack.add(subClassElem);
									}
								}
							}
						}
					}
				}
			} catch (CoreException e) {
				RDTLog.logError(e);
			}
		}
	}
	
	private void addMembers(IIndex index, THGraphNode graphNode, IBinding binding) throws CoreException {
		if (graphNode.getMembers(false) == null) {
			ArrayList<ICElement> memberList= new ArrayList<ICElement>();
			try {
				if (binding instanceof ICPPClassType) {
					ICPPClassType ct= (ICPPClassType) binding;
					IBinding[] members= ct.getDeclaredFields();
					addMemberElements(index, members, memberList);
					members= ct.getDeclaredMethods();
					addMemberElements(index, members, memberList);
				}
				else if (binding instanceof ICompositeType) {
					ICompositeType ct= (ICompositeType) binding;
					IBinding[] members= ct.getFields();
					addMemberElements(index, members, memberList);
				}
				else if (binding instanceof IEnumeration) {
					IEnumeration ct= (IEnumeration) binding;
					IBinding[] members= ct.getEnumerators();
					addMemberElements(index, members, memberList);
				}
			} catch (DOMException e) {
				// problem bindings should not be reported to the log.
			}
			if (memberList.isEmpty()) {
				graphNode.setMembers(NO_MEMBERS);
			}
			else {
				graphNode.setMembers(memberList.toArray(new ICElement[memberList.size()]));
			}
		}
	}
	
	private void addMemberElements(IIndex index, IBinding[] members, List<ICElement> memberList) 
			throws CoreException {
		for (int i = 0; i < members.length; i++) {
			IBinding binding = members[i];
			ICElement[] elems= IndexQueries.findRepresentative(index, binding, fConverter);
			if (elems.length > 0) {
				memberList.add(elems[0]);
			}
		}
	}

	public boolean isTrivial() {
		return fNodes.size() < 2;
	}

	public boolean isFileIndexed() {
		return fFileIsIndexed;
	}
}
