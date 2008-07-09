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
 * Class: org.eclipse.cdt.internal.ui.typehierarchy.THHierarchyModel
 * Version: 1.15
 */
package org.eclipse.ptp.internal.rdt.core.typehierarchy;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.internal.rdt.core.index.IndexQueries;
import org.eclipse.ptp.internal.rdt.core.model.Scope;

public class LocalTypeHierarchyService extends AbstractTypeHierarchyService {
	@Override
	public THGraph computeGraph(Scope scope, ICElement input, IProgressMonitor monitor) throws CoreException, InterruptedException {
		THGraph graph= new THGraph();
		ICProject[] projects= CoreModel.getDefault().getCModel().getCProjects();
		IIndex index= CCorePlugin.getIndexManager().getIndex(projects);
		index.acquireReadLock();
		try {
			if (monitor.isCanceled())
				return null;
			graph.defineInputNode(index, input);
			graph.addSuperClasses(index, monitor);
			if (monitor.isCanceled())
				return null;
			graph.addSubClasses(index, monitor);
			if (monitor.isCanceled())
				return null;
			return graph;
		}
		finally {
			index.releaseReadLock(); 
		}
	}

	private ICElement findDeclaration(ICProject project, IIndex index, IASTName name, IBinding binding) 
			throws CoreException {
		if (name != null && name.isDefinition()) {
			return IndexQueries.getCElementForName(project, index, name);
		}
	
		ICElement[] elems= IndexQueries.findAllDefinitions(index, binding);
		if (elems.length > 0) {
			return elems[0];
		}
		return IndexQueries.findAnyDeclaration(index, project, binding);
	}

	private ICElement findDefinition(ICProject project, IIndex index, IASTName name, IBinding binding) 
			throws CoreException {
		if (name != null && name.isDefinition()) {
			return IndexQueries.getCElementForName(project, index, name);
		}
	
		ICElement[] elems= IndexQueries.findAllDefinitions(index, binding);
		if (elems.length > 0) {
			return elems[0];
		}
		return IndexQueries.findAnyDeclaration(index, project, binding);
	}

	@Override
	public ICElement[] findInput(Scope scope, ICElement member)  {
		ICProject project= member.getCProject();
		try {
			IIndex index= CCorePlugin.getIndexManager().getIndex(project, IIndexManager.ADD_DEPENDENCIES | IIndexManager.ADD_DEPENDENT);
			index.acquireReadLock();
			try {
				IIndexName name= IndexQueries.elementToName(index, member);
				if (name != null) {
					member= IndexQueries.getCElementForName(project, index, name);
					IBinding binding= index.findBinding(name);
					binding= findTypeBinding(binding);
					if (isValidTypeInput(binding)) {
						ICElement input= findDefinition(project, index, null, binding);
						if (input != null) {
							return new ICElement[] {input, member};
						}
					}
				}
			}
			finally {
				if (index != null) {
					index.releaseReadLock();
				}
			}
		}
		catch (CoreException e) {
			CCorePlugin.log(e);
		} 
		catch (InterruptedException e) {
		}
		return null;
	}

	@Override
	public ICElement[] findInput(Scope scope, ICProject project, IWorkingCopy workingCopy, int selectionStart, int selectionLength) throws CoreException {
		try {
			IIndex index= CCorePlugin.getIndexManager().getIndex(project, IIndexManager.ADD_DEPENDENCIES | IIndexManager.ADD_DEPENDENT);
	
			index.acquireReadLock();
			try {
				IASTName name= IndexQueries.getSelectedName(index, workingCopy, selectionStart, selectionLength);
				if (name != null) {
					IBinding binding= name.resolveBinding();
					if (!isValidInput(binding)) {
						return null;
					}
					ICElement member= null;
					if (!isValidTypeInput(binding)) {
						member= findDeclaration(project, index, name, binding);
						name= null;
						binding= findTypeBinding(binding);
					}
					if (isValidTypeInput(binding)) {
						ICElement input= findDefinition(project, index, name, binding);
						if (input != null) {
							return new ICElement[] {input, member};
						}
					}
				}
			}
			finally {
				if (index != null) {
					index.releaseReadLock();
				}
			}
		}
		catch (CoreException e) {
			CCorePlugin.log(e);
		} 
		catch (InterruptedException e) {
		}
		return null;
	}

	public static boolean isValidInput(IBinding binding) {
		if (isValidTypeInput(binding)
				|| binding instanceof ICPPMember
				|| binding instanceof IEnumerator
				|| binding instanceof IField) {
			return true;
		}
		return false;
	}

	public static boolean isValidTypeInput(IBinding binding) {
		if (binding instanceof ICompositeType
				|| binding instanceof IEnumeration 
				|| binding instanceof ITypedef) {
			return true;
		}
		return false;
	}

	private static IBinding findTypeBinding(IBinding memberBinding) {
		try {
			if (memberBinding instanceof IEnumerator) {
				IType type= ((IEnumerator) memberBinding).getType();
				if (type instanceof IBinding) {
					return (IBinding) type;
				}
			}
			else if (memberBinding instanceof ICPPMember) {
				return ((ICPPMember) memberBinding).getClassOwner();
			}
			else if (memberBinding instanceof IField) {
				return ((IField) memberBinding).getCompositeTypeOwner();
			}
		} catch (DOMException e) {
			// don't log problem bindings
		}
		return null;
	}
}
