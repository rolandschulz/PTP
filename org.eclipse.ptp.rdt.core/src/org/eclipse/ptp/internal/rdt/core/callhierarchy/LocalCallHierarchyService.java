/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation
 *******************************************************************************/ 

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.ui/src
 * Class: org.eclipse.cdt.internal.ui.callhierarchy.CHContentProvider
 * Version: 1.17
 */

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.ui/src
 * Class: org.eclipse.cdt.internal.ui.callhierarchy.CallHierarchyUI
 * Version: 1.22
 */
package org.eclipse.ptp.internal.rdt.core.callhierarchy;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.internal.rdt.core.CModelUtil;
import org.eclipse.ptp.internal.rdt.core.index.IndexQueries;
import org.eclipse.ptp.internal.rdt.core.model.Scope;

public class LocalCallHierarchyService extends AbstractCallHierarchyService {
	private static final ICElement[] NO_ELEMENTS = {};

	public CalledByResult findCalledBy(Scope scope, ICElement callee, IProgressMonitor pm) 
			throws CoreException, InterruptedException {
		CalledByResult result= new CalledByResult();
		if (! (callee instanceof ISourceReference)) {
			return result;
		}

		ICProject[] projects= CoreModel.getDefault().getCModel().getCProjects();
		IIndex index= CCorePlugin.getIndexManager().getIndex(projects);
		index.acquireReadLock();
		try {
			IBinding calleeBinding= IndexQueries.elementToBinding(index, callee);
			findCalledBy(index, calleeBinding, callee.getCProject(), result);
			return result;
		}
		finally {
			index.releaseReadLock();
		}
	}

	private void findCalledBy(IIndex index, IBinding callee, ICProject project, CalledByResult result) 
			throws CoreException {
		if (callee != null) {
			IIndexName[] names= index.findReferences(callee);
			for (int i = 0; i < names.length; i++) {
				IIndexName rname = names[i];
				IIndexName caller= rname.getEnclosingDefinition();
				if (caller != null) {
					ICElement elem= IndexQueries.getCElementForName(project, index, caller, null);
					if (elem != null) {
						result.add(elem, rname);
					} 
				}
			}
		}
	}

	public CallsToResult findCalls(Scope scope, ICElement caller, IProgressMonitor pm) 
			throws CoreException, InterruptedException {
		ICProject[] projects= CoreModel.getDefault().getCModel().getCProjects();
		IIndex index= CCorePlugin.getIndexManager().getIndex(projects);
		index.acquireReadLock();
		try {
			return findCalls(caller, index, pm);
		}
		finally {
			index.releaseReadLock();
		}
	}

	private CallsToResult findCalls(ICElement caller, IIndex index, IProgressMonitor pm) 
		throws CoreException {
		CallsToResult result= new CallsToResult();
		IIndexName callerName= IndexQueries.elementToName(index, caller);
		if (callerName != null) {
			IIndexName[] refs= callerName.getEnclosedNames();
			for (int i = 0; i < refs.length; i++) {
				IIndexName name = refs[i];
				IBinding binding= index.findBinding(name);
				if (isRelevantForCallHierarchy(binding)) {
					ICElement[] defs = IndexQueries.findRepresentative(index, binding, null);
					if (defs != null && defs.length > 0) {
						result.add(defs, name);
					}
				}
			}
		}
		return result;
	}
	
	public ICElement[] findDefinitions(Scope scope, ICElement input, IProgressMonitor pm) {
		try {
			final ITranslationUnit tu= CModelUtil.getTranslationUnit(input);
			if (tu != null) {
				final ICProject project= tu.getCProject();
				final IIndex index= CCorePlugin.getIndexManager().getIndex(project, IIndexManager.ADD_DEPENDENCIES | IIndexManager.ADD_DEPENDENT);
	
				index.acquireReadLock();
				try {
					if (needToFindDefinition(input)) {
						IBinding binding= IndexQueries.elementToBinding(index, input);
						if (binding != null) {
							ICElement[] result= IndexQueries.findAllDefinitions(index, binding, null);
							if (result.length > 0) {
								return result;
							}
						}
					}
					IIndexName name= IndexQueries.elementToName(index, input);
					if (name != null) {
						ICElement handle= IndexQueries.getCElementForName(tu, index, name);
						return new ICElement[] {handle};
					}
				}
				finally {
					if (index != null) {
						index.releaseReadLock();
					}
				}
			}
		}
		catch (CoreException e) {
			CCorePlugin.log(e);
		} 
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		return new ICElement[] {input};
	}

	public ICElement[] findDefinitions(Scope scope, ICProject project, IWorkingCopy workingCopy, int selectionStart, int selectionLength, IProgressMonitor pm) throws CoreException {
		try {
			IIndex index= CCorePlugin.getIndexManager().getIndex(project, IIndexManager.ADD_DEPENDENCIES | IIndexManager.ADD_DEPENDENT);
	
			index.acquireReadLock();
			try {
				IASTName name= IndexQueries.getSelectedName(index, workingCopy, selectionStart, selectionLength);
				if (name != null) {
					IBinding binding= name.resolveBinding();
					if (isRelevantForCallHierarchy(binding)) {
						if (name.isDefinition()) {
							ICElement elem= IndexQueries.getCElementForName(project, index, name, null);
							if (elem != null) {
								return new ICElement[]{elem};
							}
						}
						else {
							ICElement[] elems= IndexQueries.findAllDefinitions(index, binding, null);
							if (elems.length == 0) {
								ICElement elem= null;
								if (name.isDeclaration()) {
									elem= IndexQueries.getCElementForName(project, index, name, null);
								}
								else {
									elem= IndexQueries.findAnyDeclaration(index, project, binding, null);
								}
								if (elem != null) {
									elems= new ICElement[]{elem};
								}
							}
							return elems;
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
			Thread.currentThread().interrupt();
		}
		return NO_ELEMENTS;
	}

	private static boolean needToFindDefinition(ICElement elem) {
		switch (elem.getElementType()) {
		case ICElement.C_FUNCTION_DECLARATION:
		case ICElement.C_METHOD_DECLARATION:
		case ICElement.C_TEMPLATE_FUNCTION_DECLARATION:
		case ICElement.C_TEMPLATE_METHOD_DECLARATION:
			return true;
		}
		return false;
	}
}
