/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems, Inc. and others.
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
 * Version: 1.16
 */
package org.eclipse.ptp.internal.rdt.core.typehierarchy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
import org.eclipse.ptp.internal.rdt.core.model.ICProjectFactory;
import org.eclipse.ptp.internal.rdt.core.model.IIndexLocationConverterFactory;
import org.eclipse.ptp.rdt.core.RDTLog;

public class THGraph implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private static final ICElement[] NO_MEMBERS = new ICElement[0];
	private THGraphNode fInputNode= null;
	private HashSet<THGraphNode> fRootNodes= new HashSet<THGraphNode>();
	private HashSet<THGraphNode> fLeaveNodes= new HashSet<THGraphNode>();
	private HashMap<ICElement, THGraphNode> fNodes= new HashMap<ICElement, THGraphNode>();
	private boolean fFileIsIndexed;
	
	private transient IIndexLocationConverterFactory fConverterFactory;
	
	public THGraph() {
	}

	public THGraphNode getInputNode() {
		return fInputNode;
	}
	
	public THGraphNode getNode(ICElement elem) {
		return fNodes.get(elem);
	}

	public void setLocationConverterFactory(IIndexLocationConverterFactory converter) {
		fConverterFactory = converter;
	}
	
	private THGraphNode addNode(ICElement input, String path) {
		THGraphNode node= fNodes.get(input); 

		if (node == null) {
			node= new THGraphNode(input, path);
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
			List<THGraphEdge> out= node.getOutgoing();
			for (THGraphEdge edge : out) {
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
		List<THGraphEdge> out= from.getOutgoing();
		for (THGraphEdge edge : out) {
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

	public void defineInputNode(IIndex index, ICElement input, ICProjectFactory projectFactory, String path) {
		if (input != null) {
			try {
				fFileIsIndexed= true;
				input= IndexQueries.attemptConvertionToHandle(index, input, fConverterFactory, projectFactory);
				fInputNode= addNode(input, path);
			} catch (CoreException e) {
				RDTLog.logError(e);
			}
		}
	}

	private class StackElement {
		@Override
		public int hashCode() {
			return fElement.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof StackElement) {
				StackElement se = (StackElement)obj;
				return fElement.equals(se.fElement);
			}
			return false;
		}

		public ICElement fElement;
		public String fPath;
		
		public StackElement(ICElement element, String path) {
			fElement = element;
			fPath = path;
		}

		private THGraph getOuterType() {
			return THGraph.this;
		}
	}
	
	public void addSuperClasses(IIndex index, IProgressMonitor monitor, ICProjectFactory projectFactory) {
		if (fInputNode == null) {
			return;
		}
		HashSet<StackElement> handled= new HashSet<StackElement>();
		ArrayList<StackElement> stack= new ArrayList<StackElement>();
		
		StackElement stackElement = new StackElement(fInputNode.getElement(), fInputNode.getPath());
		
		stack.add(stackElement);
		handled.add(stackElement);
		
		while (!stack.isEmpty()) {
			if (monitor.isCanceled()) {
				return;
			}
			
			StackElement se = stack.remove(stack.size()-1);
			THGraphNode graphNode= addNode(se.fElement, se.fPath);
			try {
				IIndexBinding binding = IndexQueries.elementToBinding(index, se.fElement, se.fPath);
				if (binding != null) {
					addMembers(index, graphNode, binding, projectFactory);
				}
				if (binding instanceof ICPPClassType) {
					ICPPClassType ct= (ICPPClassType) binding;
					ICPPBase[] bases= ct.getBases();
					for (ICPPBase base : bases) {
						if (monitor.isCanceled()) {
							return;
						}
						IName name= base.getBaseClassSpecifierName();
						IBinding basecl= name != null ? index.findBinding(name) : base.getBaseClass();
						ICElement[] baseElems= IndexQueries.findRepresentative(index, basecl, fConverterFactory, null, projectFactory);
						String[] paths = IndexQueries.findRepresentitivePaths(index, basecl, fConverterFactory, null, projectFactory);

						for (int j = 0; j < baseElems.length; j++) {
							ICElement baseElem = baseElems[j];
							String path = paths[j];
							THGraphNode baseGraphNode= addNode(baseElem, path);
							addMembers(index, baseGraphNode, basecl, projectFactory);							
							addEdge(graphNode, baseGraphNode);
							StackElement se1 = new StackElement(baseElem, path);
							
							if (handled.add(se1)) {
								stack.add(se1);
							}
						}
					}
				}
				else if (binding instanceof ITypedef) {
					ITypedef ct= (ITypedef) binding;
					IType type= ct.getType();
					if (type instanceof IBinding) {
						IBinding basecl= (IBinding) type;
						ICElement[] baseElems= IndexQueries.findRepresentative(index, basecl, fConverterFactory, null, projectFactory);
						String[] paths = IndexQueries.findRepresentitivePaths(index, basecl, fConverterFactory, null, projectFactory);
						if (baseElems.length > 0) {
							ICElement baseElem= baseElems[0];
							String path = paths[0];
							THGraphNode baseGraphNode= addNode(baseElem, path);
							addMembers(index, baseGraphNode, basecl, projectFactory);							
							addEdge(graphNode, baseGraphNode);
							StackElement se1 = new StackElement(baseElem, path);
							
							if (handled.add(se1)) {
								stack.add(se1);
							}
						}
					}
				}
			} catch (CoreException e) {
				RDTLog.logError(e);
			}
		}
	}

	public void addSubClasses(IIndex index, IProgressMonitor monitor, ICProjectFactory projectFactory) {
		if (fInputNode == null) {
			return;
		}
		HashSet<StackElement> handled= new HashSet<StackElement>();
		ArrayList<StackElement> stack= new ArrayList<StackElement>();
		ICElement element = fInputNode.getElement();
		String path = fInputNode.getPath();
		StackElement stackElement = new StackElement(element, path);
		stack.add(stackElement);
		handled.add(stackElement);
		while (!stack.isEmpty()) {
			if (monitor.isCanceled()) {
				return;
			}
			StackElement se = stack.remove(stack.size()-1);
			ICElement elem= se.fElement;
			THGraphNode graphNode= addNode(elem, se.fPath);
			try {
				IBinding binding = IndexQueries.elementToBinding(index, elem, se.fPath);
				if (binding != null) {
					IIndexName[] names= index.findNames(binding, IIndex.FIND_REFERENCES | IIndex.FIND_DEFINITIONS);
					for (IIndexName indexName : names) {
						if (monitor.isCanceled()) {
							return;
						}
						if (indexName.isBaseSpecifier()) {
							IIndexName subClassDef= indexName.getEnclosingDefinition();
							if (subClassDef != null) {
								IBinding subClass= index.findBinding(subClassDef);
								ICElement[] subClassElems= IndexQueries.findRepresentative(index, subClass, fConverterFactory, null, projectFactory);
								String[] paths = IndexQueries.findRepresentitivePaths(index, subClass, fConverterFactory, null, projectFactory);
								if (subClassElems.length > 0) {
									ICElement subClassElem= subClassElems[0];
									String path1 = paths[0];
									THGraphNode subGraphNode= addNode(subClassElem, path1);
									addMembers(index, subGraphNode, subClass, projectFactory);							
									addEdge(subGraphNode, graphNode);
									
									StackElement se1 = new StackElement(subClassElem, path1);
									
									if (handled.add(se1)) {
										stack.add(se1);
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
		
	private void addMembers(IIndex index, THGraphNode graphNode, IBinding binding, ICProjectFactory projectFactory) throws CoreException {
		if (graphNode.getMembers(false) == null) {
			ArrayList<ICElement> memberList= new ArrayList<ICElement>();
			if (binding instanceof ICPPClassType) {
				ICPPClassType ct= (ICPPClassType) binding;
				IBinding[] members= ct.getDeclaredFields();
				addMemberElements(index, members, memberList, projectFactory);
				members= ct.getDeclaredMethods();
				addMemberElements(index, members, memberList, projectFactory);
			}
			else if (binding instanceof ICompositeType) {
				ICompositeType ct= (ICompositeType) binding;
				IBinding[] members= ct.getFields();
				addMemberElements(index, members, memberList, projectFactory);
			}
			else if (binding instanceof IEnumeration) {
				IEnumeration ct= (IEnumeration) binding;
				IBinding[] members= ct.getEnumerators();
				addMemberElements(index, members, memberList, projectFactory);
			}
			if (memberList.isEmpty()) {
				graphNode.setMembers(NO_MEMBERS);
			}
			else {
				graphNode.setMembers(memberList.toArray(new ICElement[memberList.size()]));
			}
		}
	}
	
	private void addMemberElements(IIndex index, IBinding[] members, List<ICElement> memberList, ICProjectFactory projectFactory) 
			throws CoreException {
		for (IBinding binding : members) {
			ICElement[] elems= IndexQueries.findRepresentative(index, binding, fConverterFactory, null, projectFactory);
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
