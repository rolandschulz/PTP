/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.internal.rdt.core.typehierarchy;

import java.net.URI;

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
import org.eclipse.cdt.utils.EFSExtensionManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.internal.rdt.core.index.IndexQueries;
import org.eclipse.ptp.internal.rdt.core.model.LocalCProjectFactory;
import org.eclipse.ptp.internal.rdt.core.model.LocalIndexLocationConverterFactory;
import org.eclipse.ptp.internal.rdt.core.model.Scope;
import org.eclipse.ptp.rdt.core.RDTLog;

/**
 * @author crecoskie
 * 
 */
public class LocalTypeHierarchyService extends AbstractTypeHierarchyService {

	private static ICElement findDeclaration(ICProject project, IIndex index, IASTName name, IBinding binding)
			throws CoreException {
		if (name != null && name.isDefinition()) {
			return IndexQueries.getCElementForName(project, index, name, new LocalIndexLocationConverterFactory(), new LocalCProjectFactory());
		}

		ICElement[] elems = IndexQueries.findAllDefinitions(index, binding, new LocalIndexLocationConverterFactory(), project, new LocalCProjectFactory());
		if (elems.length > 0) {
			return elems[0];
		}
		return IndexQueries.findAnyDeclaration(index, project, binding, new LocalIndexLocationConverterFactory(), new LocalCProjectFactory());
	}

	private static ICElement findDefinition(ICProject project, IIndex index, IASTName name, IBinding binding)
			throws CoreException {
		if (name != null && name.isDefinition()) {
			return IndexQueries.getCElementForName(project, index, name, new LocalIndexLocationConverterFactory(), new LocalCProjectFactory());
		}

		ICElement[] elems = IndexQueries.findAllDefinitions(index, binding, new LocalIndexLocationConverterFactory(), project, new LocalCProjectFactory());
		if (elems.length > 0) {
			return elems[0];
		}
		return IndexQueries.findAnyDeclaration(index, project, binding, new LocalIndexLocationConverterFactory(), new LocalCProjectFactory());
	}

	private static IBinding findTypeBinding(IBinding memberBinding) {
		try {
			if (memberBinding instanceof IEnumerator) {
				IType type = ((IEnumerator) memberBinding).getType();
				if (type instanceof IBinding) {
					return (IBinding) type;
				}
			} else if (memberBinding instanceof ICPPMember) {
				return ((ICPPMember) memberBinding).getClassOwner();
			} else if (memberBinding instanceof IField) {
				return ((IField) memberBinding).getCompositeTypeOwner();
			}
		} catch (DOMException e) {
			// don't log problem bindings
		}
		return null;
	}

	public static boolean isValidInput(IBinding binding) {
		if (isValidTypeInput(binding) || binding instanceof ICPPMember || binding instanceof IEnumerator
				|| binding instanceof IField) {
			return true;
		}
		return false;
	}

	public static boolean isValidInput(ICElement elem) {
		if (elem == null) {
			return false;
		}
		if (isValidTypeInput(elem)) {
			return true;
		}
		switch (elem.getElementType()) {
		case ICElement.C_FIELD:
		case ICElement.C_METHOD:
		case ICElement.C_METHOD_DECLARATION:
		case ICElement.C_TEMPLATE_METHOD:
		case ICElement.C_TEMPLATE_METHOD_DECLARATION:
		case ICElement.C_ENUMERATOR:
			return true;
		}
		return false;
	}

	public static boolean isValidTypeInput(IBinding binding) {
		if (binding instanceof ICompositeType || binding instanceof IEnumeration || binding instanceof ITypedef) {
			return true;
		}
		return false;
	}

	public static boolean isValidTypeInput(ICElement elem) {
		if (elem == null) {
			return false;
		}
		switch (elem.getElementType()) {
		case ICElement.C_CLASS:
		case ICElement.C_STRUCT:
		case ICElement.C_UNION:
		case ICElement.C_CLASS_DECLARATION:
		case ICElement.C_STRUCT_DECLARATION:
		case ICElement.C_UNION_DECLARATION:
		case ICElement.C_ENUMERATION:
		case ICElement.C_TYPEDEF:
			// case ICElement.C_TEMPLATE_CLASS:
			// case ICElement.C_TEMPLATE_CLASS_DECLARATION:
			// case ICElement.C_TEMPLATE_STRUCT:
			// case ICElement.C_TEMPLATE_STRUCT_DECLARATION:
			// case ICElement.C_TEMPLATE_UNION:
			// case ICElement.C_TEMPLATE_UNION_DECLARATION:
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.rdt.core.typehierarchy.AbstractTypeHierarchyService#computeGraph(org.eclipse.ptp.internal
	 * .rdt.core.model.Scope, org.eclipse.cdt.core.model.ICElement, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public THGraph computeGraph(Scope scope, ICElement input, IProgressMonitor monitor) throws CoreException,
			InterruptedException {
		THGraph graph = new THGraph();

		ICProject[] projectScope = CoreModel.getDefault().getCModel().getCProjects();
		IIndex index = CCorePlugin.getIndexManager().getIndex(projectScope);
		index.acquireReadLock();
		try {
			if (monitor.isCanceled())
				return null;

			URI locationURI = input.getLocationURI();
			IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(locationURI);
			String workspacePath = null; // if the file is external to the workspace then it's workspace path is null
			if(files != null && files.length > 0) {
				workspacePath = files[0].getFullPath().toString();
			}
			
			graph.setLocationConverterFactory(new LocalIndexLocationConverterFactory());
			graph.defineInputNode(index, input, new LocalCProjectFactory(), workspacePath);
			graph.addSuperClasses(index, monitor, new LocalCProjectFactory());
			if (monitor.isCanceled())
				return null;
			graph.addSubClasses(index, monitor, new LocalCProjectFactory());
			if (monitor.isCanceled())
				return null;
		} finally {
			index.releaseReadLock();
		}
		
		return graph;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.rdt.core.typehierarchy.AbstractTypeHierarchyService#findInput(org.eclipse.ptp.internal
	 * .rdt.core.model.Scope, org.eclipse.cdt.core.model.ICElement)
	 */
	@Override
	public ICElement[] findInput(Scope scope, ICElement input) {
		try {
			ICProject project = CCorePlugin.getDefault().getCoreModel().getCModel().getCProject(scope.getName());
			IIndex index = CCorePlugin.getIndexManager().getIndex(project,
					IIndexManager.ADD_DEPENDENCIES | IIndexManager.ADD_DEPENDENT);
			index.acquireReadLock();
			try {
				IIndexName name = IndexQueries.elementToName(index, input);
				if (name != null) {
					input = IndexQueries.getCElementForName(project, index, name,
							new LocalIndexLocationConverterFactory(), new LocalCProjectFactory());
					IBinding binding = index.findBinding(name);
					binding = findTypeBinding(binding);
					if (isValidTypeInput(binding)) {
						ICElement returnValue = findDefinition(project, index, null, binding);
						if (returnValue != null) {
							return new ICElement[] { input, input };
						}
					}
				}
			} finally {
				index.releaseReadLock();
			}
		} catch (CoreException e) {
			RDTLog.logError(e);
		} catch (InterruptedException e) {
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.rdt.core.typehierarchy.ITypeHierarchyService#findInput(org.eclipse.ptp.internal.rdt.
	 * core.model.Scope, org.eclipse.cdt.core.model.ICElement, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public ICElement[] findInput(Scope scope, ICElement memberInput, IProgressMonitor monitor) {

		try {
			ICProject project = CCorePlugin.getDefault().getCoreModel().getCModel().getCProject(scope.getName());
			IIndex index = CCorePlugin.getIndexManager().getIndex(project,
					IIndexManager.ADD_DEPENDENCIES | IIndexManager.ADD_DEPENDENT);
			index.acquireReadLock();
			try {
				IIndexName name = IndexQueries.elementToName(index, memberInput);
				if (name != null) {
					memberInput = IndexQueries.getCElementForName(project, index, name,
							new LocalIndexLocationConverterFactory(), new LocalCProjectFactory());
					IBinding binding = index.findBinding(name);
					binding = findTypeBinding(binding);
					if (isValidTypeInput(binding)) {
						ICElement input = findDefinition(project, index, null, binding);
						if (input != null) {
							return new ICElement[] { input, memberInput };
						}
					}
				}
			} finally {
				index.releaseReadLock();
			}
		} catch (CoreException e) {
			RDTLog.logError(e);
		} catch (InterruptedException e) {
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.rdt.core.typehierarchy.AbstractTypeHierarchyService#findInput(org.eclipse.ptp.internal
	 * .rdt.core.model.Scope, org.eclipse.cdt.core.model.ICProject, org.eclipse.cdt.core.model.IWorkingCopy, int, int)
	 */
	@Override
	public ICElement[] findInput(Scope scope, ICProject project, IWorkingCopy workingCopy, int selectionStart,
			int selectionLength) throws CoreException {
		try {
			IIndex index = CCorePlugin.getIndexManager().getIndex(project,
					IIndexManager.ADD_DEPENDENCIES | IIndexManager.ADD_DEPENDENT);

			index.acquireReadLock();
			try {
				IASTName name = IndexQueries.getSelectedName(index, workingCopy, selectionStart, selectionLength);
				if (name != null) {
					IBinding binding = name.resolveBinding();
					if (!isValidInput(binding)) {
						return null;
					}
					ICElement member = null;
					if (!isValidTypeInput(binding)) {
						member = findDeclaration(project, index, name, binding);
						name = null;
						binding = findTypeBinding(binding);
					}
					if (isValidTypeInput(binding)) {
						ICElement input = findDefinition(project, index, name, binding);
						if (input != null) {
							return new ICElement[] { input, member };
						}
					}
				}
			} finally {
				index.releaseReadLock();
			}
		} catch (CoreException e) {
			RDTLog.logError(e);
		} catch (InterruptedException e) {
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.rdt.core.typehierarchy.ITypeHierarchyService#findInput(org.eclipse.ptp.internal.rdt.
	 * core.model.Scope, org.eclipse.cdt.core.model.ICProject, org.eclipse.cdt.core.model.IWorkingCopy, int, int,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public ICElement[] findInput(Scope scope, ICProject project, IWorkingCopy workingCopy, int selectionStart,
			int selectionLength, IProgressMonitor monitor) throws CoreException {
		try {
			IIndex index = CCorePlugin.getIndexManager().getIndex(project,
					IIndexManager.ADD_DEPENDENCIES | IIndexManager.ADD_DEPENDENT);

			index.acquireReadLock();
			try {
				IASTName name = IndexQueries.getSelectedName(index, workingCopy, selectionStart, selectionLength);
				if (name != null) {
					IBinding binding = name.resolveBinding();
					if (!isValidInput(binding)) {
						return null;
					}
					ICElement member = null;
					if (!isValidTypeInput(binding)) {
						member = findDeclaration(project, index, name, binding);
						name = null;
						binding = findTypeBinding(binding);
					}
					if (isValidTypeInput(binding)) {
						ICElement input = findDefinition(project, index, name, binding);
						if (input != null) {
							return new ICElement[] { input, member };
						}
					}
				}
			} finally {
				index.releaseReadLock();
			}
		} catch (CoreException e) {
			RDTLog.logError(e);
		} catch (InterruptedException e) {
		}
		return null;
	}

}
