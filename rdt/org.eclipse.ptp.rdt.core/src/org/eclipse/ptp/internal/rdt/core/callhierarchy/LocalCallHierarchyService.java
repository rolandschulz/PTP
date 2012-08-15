/*******************************************************************************
 * Copyright (c) 2008, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation
 *******************************************************************************/ 



package org.eclipse.ptp.internal.rdt.core.callhierarchy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.utils.EFSExtensionManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.internal.rdt.core.CModelUtil;
import org.eclipse.ptp.internal.rdt.core.index.IndexQueries;
import org.eclipse.ptp.internal.rdt.core.model.ICProjectFactory;
import org.eclipse.ptp.internal.rdt.core.model.IIndexLocationConverterFactory;
import org.eclipse.ptp.internal.rdt.core.model.LocalCProjectFactory;
import org.eclipse.ptp.internal.rdt.core.model.LocalIndexLocationConverterFactory;
import org.eclipse.ptp.internal.rdt.core.model.Scope;

public class LocalCallHierarchyService extends AbstractCallHierarchyService {

	private static IIndexLocationConverterFactory converterFactory = new LocalIndexLocationConverterFactory();
	
	/* -- ST-Origin --
	 * Source folder: org.eclipse.cdt.ui/src
	 * Class: org.eclipse.cdt.internal.ui.callhierarchy.CHQueries
	 * Version: 1.20
	 */
	
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
			
			findCalledBy(callee, index, result);
			return result;
		}
		finally {
			index.releaseReadLock();
		}
	}
	
	private static void findCalledBy(ICElement callee, IIndex index, CalledByResult result)
			throws CoreException {
		final ICProject project = callee.getCProject();
		String path = EFSExtensionManager.getDefault().getPathFromURI(callee.getLocationURI());
		IBinding calleeBinding= IndexQueries.elementToBinding(index, callee, path);
		if (calleeBinding != null) {
			findCalledBy1(index, calleeBinding, true, project, result);
			if (calleeBinding instanceof ICPPMethod) {
				IBinding[] overriddenBindings= ClassTypeHelper.findOverridden((ICPPMethod) calleeBinding, null);
				for (IBinding overriddenBinding : overriddenBindings) {
					findCalledBy1(index, overriddenBinding, false, project, result);
				}

			}
		}
	}
		
	private static void findCalledBy1(IIndex index, IBinding callee, boolean includeOrdinaryCalls,
			ICProject project, CalledByResult result) throws CoreException {
		findCalledBy2(index, callee, includeOrdinaryCalls, project, result);
		List<? extends IBinding> specializations = IndexQueries.findSpecializations(callee);
		for (IBinding spec : specializations) {
			findCalledBy2(index, spec, includeOrdinaryCalls, project, result);
		}
	}


	private static void findCalledBy2(IIndex index, IBinding callee, boolean includeOrdinaryCalls, ICProject project, CalledByResult result) 
			throws CoreException {
		IIndexName[] names= index.findNames(callee, IIndex.FIND_REFERENCES | IIndex.SEARCH_ACROSS_LANGUAGE_BOUNDARIES);
		for (IIndexName rname : names) {
			if (includeOrdinaryCalls || rname.couldBePolymorphicMethodCall()) {
				IIndexName caller= rname.getEnclosingDefinition();
				if (caller != null) {
					ICElement elem= IndexQueries.getCElementForName(project, index, caller, converterFactory, new LocalCProjectFactory());
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
			final ICProject project = caller.getCProject();
			ICProjectFactory projectFactory = new LocalCProjectFactory();
			for (IIndexName name : refs) {
				IBinding binding= index.findBinding(name);
				if (isRelevantForCallHierarchy(binding)) {
					for(;;) {
						ICElement[] defs=null;
						if (binding instanceof ICPPMethod) {
							defs = findOverriders(index, (ICPPMethod) binding, project, projectFactory);
						}
						if (defs == null) {
							defs= IndexQueries.findRepresentative(index, binding, converterFactory, project, projectFactory);
						}
						if (defs != null && defs.length > 0) {
							result.add(defs, name);
						} else if (binding instanceof ICPPSpecialization) {
							binding= ((ICPPSpecialization) binding).getSpecializedBinding();
							if (binding != null)
								continue;
						}
						break;


					}
					
				}
			}
		}
		return result;
	}
	
	/**
	 * Searches for overriders of method and converts them to ICElement, returns null, if there are none.
	 */
	static ICElement[] findOverriders(IIndex index, ICPPMethod binding, ICProject project, ICProjectFactory projectFactory)	throws CoreException {
		IBinding[] virtualOverriders= ClassTypeHelper.findOverriders(index, binding);
		if (virtualOverriders.length > 0) {
			ArrayList<ICElement> list= new ArrayList<ICElement>();
			list.addAll(Arrays.asList(IndexQueries.findRepresentative(index, binding, converterFactory, project, projectFactory)));
			for (IBinding overrider : virtualOverriders) {
				list.addAll(Arrays.asList(IndexQueries.findRepresentative(index, overrider, converterFactory, project, projectFactory)));
			}
			return list.toArray(new ICElement[list.size()]);
		}
		return null;
	}

	
	/*********************************************************************************************************************************/
	/* -- ST-Origin --
	 * Source folder: org.eclipse.cdt.ui/src
	 * Class: org.eclipse.cdt.internal.ui.callhierarchy.CallHierarchyUI
	 * Version: 1.25
	 */
	
	private static final ICElement[] NO_ELEMENTS = {};
	
	public ICElement[] findDefinitions(Scope scope, ICProject project, IWorkingCopy workingCopy, int selectionStart, int selectionLength, IProgressMonitor pm) throws CoreException {
		try {
			IIndex index= CCorePlugin.getIndexManager().getIndex(project, IIndexManager.ADD_DEPENDENCIES | IIndexManager.ADD_DEPENDENT);
	
			index.acquireReadLock();
			try {
				IASTName name= IndexQueries.getSelectedName(index, workingCopy, selectionStart, selectionLength);
				if (name != null) {
					IBinding binding= name.resolveBinding();
					if (isRelevantForCallHierarchy(binding)) {
						final LocalCProjectFactory projectFactory = new LocalCProjectFactory();
						if (name.isDefinition()) {
							ICElement elem= IndexQueries.getCElementForName(project, index, name, converterFactory, projectFactory);
							if (elem != null) {
								return new ICElement[]{elem};
							}
							return NO_ELEMENTS;
						}


						ICElement[] elems= IndexQueries.findAllDefinitions(index, binding, converterFactory, project, projectFactory);
						if (elems.length != 0) 
							return elems;
							
								
						if (name.isDeclaration()) {
							ICElement elem= IndexQueries.getCElementForName(project, index, name, converterFactory, projectFactory);
							if (elem != null) {
								return new ICElement[] {elem};
							}
							return NO_ELEMENTS;

						}
						ICElement elem= IndexQueries.findAnyDeclaration(index, project, binding, converterFactory, projectFactory);
						
						if (elem != null) {
							return new ICElement[]{elem};
						}
						if (binding instanceof ICPPSpecialization) {
							return findSpecializationDeclaration(binding, project, index, projectFactory);
						}

					
						return NO_ELEMENTS;

						
					}
				}
			}
			finally {
				
					index.releaseReadLock();
				
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
	
	private static ICElement[] findSpecializationDeclaration(IBinding binding, ICProject project,
			IIndex index, LocalCProjectFactory projectFactory) throws CoreException {
		while (binding instanceof ICPPSpecialization) {
			IBinding original= ((ICPPSpecialization) binding).getSpecializedBinding();
			ICElement[] elems= IndexQueries.findAllDefinitions(index, original, converterFactory, project, projectFactory);
			if (elems.length == 0) {
				ICElement elem= IndexQueries.findAnyDeclaration(index, project, original, converterFactory, projectFactory);
				if (elem != null) {
					elems= new ICElement[]{elem};
				}
			}
			if (elems.length > 0) {
				return elems;
			}
			binding= original;
		}
		return NO_ELEMENTS;
	}


	public ICElement[] findDefinitions(Scope scope, ICElement input, IProgressMonitor pm) {
		try {
			final ITranslationUnit tu= CModelUtil.getTranslationUnit(input);
			if (tu != null) {
				final ICProject project= tu.getCProject();
				final IIndex index= CCorePlugin.getIndexManager().getIndex(project, IIndexManager.ADD_DEPENDENCIES | IIndexManager.ADD_DEPENDENT);
	
				index.acquireReadLock();
				try {
					final LocalCProjectFactory projectFactory = new LocalCProjectFactory();
					if (needToFindDefinition(input)) {
						String path = EFSExtensionManager.getDefault().getPathFromURI(input.getLocationURI());
						IBinding binding= IndexQueries.elementToBinding(index, input, path);
						if (binding != null) {
							ICElement[] result= IndexQueries.findAllDefinitions(index, binding, converterFactory, project, projectFactory);
							if (result.length > 0) {
								return result;
							}
						}
					}
					IIndexName name= IndexQueries.elementToName(index, input);
					if (name != null) {
						ICElement handle= IndexQueries.getCElementForName(tu, index, name, projectFactory);
						return new ICElement[] {handle};
					}
				}
				finally {
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
		return new ICElement[] {input};
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

	public Map<String, ICElement[]> findOverriders(Scope scope,
			ICElement input, IProgressMonitor pm) throws CoreException, InterruptedException {
		final ITranslationUnit tu= CModelUtil.getTranslationUnit(input);
		final ICProject project= tu.getCProject();
		final IIndex index= CCorePlugin.getIndexManager().getIndex(project, IIndexManager.ADD_DEPENDENCIES | IIndexManager.ADD_DEPENDENT);
		Map<String, ICElement[]> result = new HashMap<String, ICElement[]>();
		index.acquireReadLock();
		try{
			IIndexName methodName= IndexQueries.elementToName(index, input);
			if (methodName != null) {
				ICProjectFactory projectFactory = new LocalCProjectFactory();
				IBinding methodBinding= index.findBinding(methodName);
				if (methodBinding instanceof ICPPMethod) {
					ICElement[] defs= findOverriders(index, (ICPPMethod) methodBinding, project, projectFactory);
					if (defs != null && defs.length > 0) {
						result.put(methodBinding.getLinkage().getLinkageID()+"", defs); //$NON-NLS-1$
					}
				}
			}
		}
		finally {
			index.releaseReadLock();
		}
		return result;


	}
}
