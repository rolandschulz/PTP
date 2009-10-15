/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Ed Swartz (Nokia)
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core.miners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.ptp.internal.rdt.core.model.BindingAdapter;
import org.eclipse.ptp.internal.rdt.core.model.CElement;
import org.eclipse.ptp.internal.rdt.core.model.Path;
import org.eclipse.ptp.internal.rdt.core.model.Scope;
import org.eclipse.ptp.internal.rdt.core.navigation.OpenDeclarationResult;
import org.eclipse.ptp.internal.rdt.core.navigation.SimpleName;
import org.eclipse.rse.dstore.universal.miners.UniversalServerUtilities;

public class OpenDeclarationHandler {
	
	private static final int KIND_OTHER = 0;
	private static final int KIND_USING_DECL = 1;
	private static final int KIND_DEFINITION = 2;
	public static final String CLASS_NAME = "CDTMiner-OpenDeclarationHandler"; //$NON-NLS-1$
	
	private static int PARSE_MODE_FAST = 
		ITranslationUnit.AST_SKIP_ALL_HEADERS | 
		ITranslationUnit.AST_CONFIGURE_USING_SOURCE_CONTEXT;

	
	public static OpenDeclarationResult handleOpenDeclaration(String scopeName, ITranslationUnit workingCopy, String selectedText, int selectionStart, int selectionLength, DataStore _dataStore) {

		UniversalServerUtilities.logDebugMessage(CLASS_NAME, "Getting declaration for selection in " + workingCopy.getElementName(), _dataStore); //$NON-NLS-1$
		UniversalServerUtilities.logDebugMessage(CLASS_NAME, "scope: " + scopeName, _dataStore); //$NON-NLS-1$
		UniversalServerUtilities.logDebugMessage(CLASS_NAME, "path: " + workingCopy.getLocationURI(), _dataStore); //$NON-NLS-1$
		UniversalServerUtilities.logDebugMessage(CLASS_NAME, "offset: " + selectionStart, _dataStore); //$NON-NLS-1$
		UniversalServerUtilities.logDebugMessage(CLASS_NAME, "length: " + selectionLength, _dataStore); //$NON-NLS-1$

		IIndex index = RemoteIndexManager.getInstance().getIndexForScope(Scope.WORKSPACE_ROOT_SCOPE_NAME, _dataStore);

		UniversalServerUtilities.logDebugMessage(CLASS_NAME, "Acquiring read lock", _dataStore); //$NON-NLS-1$
		
		
		try {
			index.acquireReadLock();
		} catch (InterruptedException e) {
			UniversalServerUtilities.logError(CLASS_NAME, e.toString(), e, _dataStore);
			return OpenDeclarationResult.failureUnexpectedError();
		}
		
		UniversalServerUtilities.logDebugMessage(CLASS_NAME, "Got Read lock", _dataStore); //$NON-NLS-1$
		
		try {
			return doHandleOpenDeclaration(scopeName, workingCopy, selectedText, selectionStart, selectionLength, index, _dataStore);
			
		} catch (CoreException e) {
			UniversalServerUtilities.logError(CLASS_NAME, e.toString(), e, _dataStore);
			return OpenDeclarationResult.failureUnexpectedError();
		} finally {
			index.releaseReadLock();
			UniversalServerUtilities.logDebugMessage(CLASS_NAME, "Lock released", _dataStore);   //$NON-NLS-1$
		}
	}

	
	/**
	 * PDOMNames and ASTNames are not serializable, need to convert them 
	 * to a serializable form before returning them.
	 */
	private static IName[] convertNames(IName[] names) {
		int n = names.length;
		IName[] converted = new IName[n];
		for(int i = 0; i < n; i++) {
			converted[i] = new SimpleName(names[i]);
		}
		
		return converted;
	}
	
	
	
	private static OpenDeclarationResult doHandleOpenDeclaration(String scopeName, ITranslationUnit workingCopy, String selectedText, 
			                                                     int selectionStart, int selectionLength, IIndex index, DataStore _dataStore) throws CoreException {
		IASTTranslationUnit ast = workingCopy.getAST(index, PARSE_MODE_FAST);
		
		final IASTNodeSelector nodeSelector = ast.getNodeSelector(null);
		IASTName searchName = nodeSelector.findEnclosingName(selectionStart, selectionLength);
		
		if (searchName != null) {
			IASTNode parent = searchName.getParent();
			if (parent instanceof IASTPreprocessorIncludeStatement) {
				String path = ((IASTPreprocessorIncludeStatement) parent).getPath();
				return OpenDeclarationResult.resultIncludePath(path);
			}
			
			IBinding binding = searchName.resolveBinding();
			
			if (binding != null && !(binding instanceof IProblemBinding)) {				
				int isKind= KIND_OTHER;
				if (searchName.isDefinition())
					isKind = (binding instanceof ICPPUsingDeclaration) ? KIND_USING_DECL : KIND_DEFINITION;
				
				IName[] declNames = findNames(index, ast, isKind, binding);
				if (declNames.length == 0) {
					if (binding instanceof ICPPSpecialization) {
						// bug 207320, handle template instances
						IBinding specialized = ((ICPPSpecialization) binding).getSpecializedBinding();
						if (specialized != null && !(specialized instanceof IProblemBinding)) {
							declNames = findNames(index, ast, KIND_DEFINITION, specialized);
						}
					} else if (binding instanceof ICPPMethod) {
						// bug 86829, handle implicit methods.
						ICPPMethod method= (ICPPMethod) binding;
						if (method.isImplicit()) {
							try {
								IBinding clsBinding = method.getClassOwner();
								if (clsBinding != null && !(clsBinding instanceof IProblemBinding)) {
									declNames = findNames(index, ast, KIND_OTHER, clsBinding);
								}
							} catch (DOMException e) { }
						}
					}
				}

				ICElement[] elements = convertToCElements(workingCopy, index, declNames, _dataStore);
				if(elements != null && elements.length > 0)
					return OpenDeclarationResult.resultCElements(elements);
					
				if(hasAtLeastOneLocation(declNames))
					return OpenDeclarationResult.resultNames(convertNames(declNames));
			}
		} 
		else {
			IASTNode node = nodeSelector.findEnclosingNode(selectionStart, selectionLength);
			if (node instanceof IASTPreprocessorIncludeStatement) {
				String path = ((IASTPreprocessorIncludeStatement) node).getPath();
				return OpenDeclarationResult.resultIncludePath(path);
			}
		}
		
		return navigationFallBack(ast, index, selectedText, _dataStore); 
	}
	


	private static ICElement[] convertToCElements(ITranslationUnit unit, IIndex index, IName[] names, DataStore _dataStore) {
		List<ICElement> elements = new ArrayList<ICElement>();
		for(IName name : names) {
			try {
				ICElement element = getCElementForName(unit, index, name);
				if(element instanceof ISourceReference)
					elements.add(element);
			} catch (CoreException e) {
				UniversalServerUtilities.logError(CLASS_NAME, e.toString(), e, _dataStore);
			} catch (DOMException e) {
				UniversalServerUtilities.logError(CLASS_NAME, e.toString(), e, _dataStore); 
			}
		}
		return elements.toArray(new ICElement[elements.size()]);
	}

	private static ICElement getCElementForName(ITranslationUnit unit, IIndex index, IName name) throws CoreException, DOMException {
		boolean isDefinition = name.isDefinition();
		IBinding binding;
		int offset, length;
		
		if(name instanceof IIndexName) {
			IIndexName indexName = (IIndexName) name;
			binding = index.findBinding(indexName);
			offset = indexName.getNodeOffset();
			length = indexName.getNodeLength();
			
		}
		else if(name instanceof IASTName) {
			IASTName astName = (IASTName) name;
			binding = astName.resolveBinding();
			if(binding == null)
				return null;
			IASTFileLocation loc = astName.getFileLocation();
			if(loc == null)
				return null;
			offset = loc.getNodeOffset();
			length = loc.getNodeLength();
		}
		else {
			return null;
		}
		
		ICElement element = BindingAdapter.adaptBinding(unit, binding, offset, length, isDefinition);
		if(element == null)
			return null;
		((CElement)element).setPath(new Path(name.getFileLocation().getFileName()));
		return element;
	}
	

	private static boolean hasAtLeastOneLocation(IName[] declNames) {
		for(IName name : declNames) {
			IASTFileLocation fileloc = name.getFileLocation();
			if(fileloc != null)
				return true;
		}
		return false;
	}
	

	private static IName[] findNames(IIndex index, IASTTranslationUnit ast, int isKind, IBinding binding) throws CoreException {
		IName[] declNames;
		if (isKind == KIND_DEFINITION) {
			declNames= findDeclarations(index, ast, binding);
		} else {
			declNames= findDefinitions(index, ast, isKind, binding);
		}

		if (declNames.length == 0) {
			if (isKind == KIND_DEFINITION) {
				declNames= findDefinitions(index, ast, isKind, binding);
			} else {
				declNames= findDeclarations(index, ast, binding);
			}
		}
		return declNames;
	}

	
	private static IName[] findDefinitions(IIndex index, IASTTranslationUnit ast, int isKind, IBinding binding) throws CoreException {
		List<IASTName> declNames= new ArrayList<IASTName>();
		declNames.addAll(Arrays.asList(ast.getDefinitionsInAST(binding)));
		for (Iterator<IASTName> i = declNames.iterator(); i.hasNext();) {
			IASTName name= i.next();
			if (name.resolveBinding() instanceof ICPPUsingDeclaration) {
				i.remove();
			}
		}
		if (!declNames.isEmpty()) {
			return declNames.toArray(new IASTName[declNames.size()]);
		}

		// 2. Try definition in index
		return index.findNames(binding, IIndex.FIND_DEFINITIONS | IIndex.SEARCH_ACROSS_LANGUAGE_BOUNDARIES);
	}

	
	private static IName[] findDeclarations(IIndex index, IASTTranslationUnit ast, IBinding binding) throws CoreException {
		IName[] declNames= ast.getDeclarationsInAST(binding);
		for (int i = 0; i < declNames.length; i++) {
			IName name = declNames[i];
			if (name.isDefinition()) 
				declNames[i]= null;
		}
		declNames= (IName[]) ArrayUtil.removeNulls(IName.class, declNames);
		if (declNames.length == 0) {
			declNames= index.findNames(binding, IIndex.FIND_DECLARATIONS | IIndex.SEARCH_ACROSS_LANGUAGE_BOUNDARIES);
		}
		return declNames;
	}
	

	/**
	 * If the names cannot be found using a binding then fall back to a text search.
	 */
	private static OpenDeclarationResult navigationFallBack(IASTTranslationUnit ast, IIndex index, String selectedText, DataStore _dataStore) {
		if(selectedText == null || selectedText.length() == 0)
			return null;
		
		try {
			char[] chars = selectedText.toCharArray();
			
			List<IName> names = new ArrayList<IName>();
			
			IndexFilter filter = IndexFilter.getDeclaredBindingFilter(ast.getLinkage().getLinkageID(), false);
			
			IIndexMacro[] macros = index.findMacros(chars, filter, null);
			for(final IIndexMacro macro : macros) {
				IName name = new SimpleName(macro.getFileLocation(), macro.getNameCharArray());
				names.add(name);
			}
			
			IIndexBinding[] bindings = index.findBindings(chars, false, filter, null);
			for (IBinding binding : bindings) {
				IName[] foundNames = findNames(index, ast, KIND_OTHER, binding);
				names.addAll(Arrays.asList(foundNames));
			}
			
			if(!names.isEmpty())
				return OpenDeclarationResult.resultNames(convertNames(names.toArray(new IName[names.size()])));
			
		} catch (CoreException e) {
			UniversalServerUtilities.logError(CLASS_NAME, e.toString(), e, _dataStore);
		}
		
		return OpenDeclarationResult.failureSymbolLookup(selectedText);
	}
	
}
