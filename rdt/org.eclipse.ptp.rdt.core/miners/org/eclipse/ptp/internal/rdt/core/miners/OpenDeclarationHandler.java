/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.ASTNameCollector;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTFunctionStyleMacroParameter;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorFunctionStyleMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
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
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.LookupData;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.cdt.internal.core.index.IIndexFragmentName;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.ptp.internal.rdt.core.model.BindingAdapter;
import org.eclipse.ptp.internal.rdt.core.model.CElement;
import org.eclipse.ptp.internal.rdt.core.model.Path;
import org.eclipse.ptp.internal.rdt.core.model.Scope;
import org.eclipse.ptp.internal.rdt.core.navigation.OpenDeclarationResult;
import org.eclipse.ptp.internal.rdt.core.navigation.SimpleASTFileLocation;
import org.eclipse.ptp.internal.rdt.core.navigation.SimpleName;
import org.eclipse.ptp.rdt.core.RDTLog;
import org.eclipse.rse.dstore.universal.miners.UniversalServerUtilities;

public class OpenDeclarationHandler {
	
	private enum NameKind { REFERENCE, DECLARATION, USING_DECL, DEFINITION }
	
	public static final String CLASS_NAME = "CDTMiner-OpenDeclarationHandler"; //$NON-NLS-1$
	
	private static int PARSE_MODE_FAST = 
		ITranslationUnit.AST_SKIP_ALL_HEADERS | 
		ITranslationUnit.AST_CONFIGURE_USING_SOURCE_CONTEXT;

	
	public interface INavigationErrorLogger { // because logging works different locally or remote
		public void logDebugMessage(String message);
		public void logError(String message, Throwable e);
	}
	
	public static OpenDeclarationResult handleOpenDeclarationRemotely(String scopeName, String scheme, ITranslationUnit workingCopy, String path, String selectedText, int selectionStart, int selectionLength, final DataStore _dataStore) {

		UniversalServerUtilities.logDebugMessage(CLASS_NAME, "Getting declaration for selection in " + workingCopy.getElementName(), _dataStore); //$NON-NLS-1$
		UniversalServerUtilities.logDebugMessage(CLASS_NAME, "scope: " + scopeName, _dataStore); //$NON-NLS-1$
		UniversalServerUtilities.logDebugMessage(CLASS_NAME, "path: " + workingCopy.getLocationURI(), _dataStore); //$NON-NLS-1$
		UniversalServerUtilities.logDebugMessage(CLASS_NAME, "offset: " + selectionStart, _dataStore); //$NON-NLS-1$
		UniversalServerUtilities.logDebugMessage(CLASS_NAME, "length: " + selectionLength, _dataStore); //$NON-NLS-1$

		IIndex project_index = RemoteIndexManager.getInstance().getIndexForScope(scopeName, _dataStore);
		IASTTranslationUnit ast = null;
		try {
			UniversalServerUtilities.logDebugMessage(CLASS_NAME, "Acquiring read lock for project_index", _dataStore); //$NON-NLS-1$
			project_index.acquireReadLock();
			ast = workingCopy.getAST(project_index, PARSE_MODE_FAST);
		}catch (InterruptedException e) {
			UniversalServerUtilities.logError(CLASS_NAME, e.toString(), e, _dataStore);
			return OpenDeclarationResult.failureUnexpectedError();
		}catch (CoreException e) {
			UniversalServerUtilities.logError(CLASS_NAME, e.toString(), e, _dataStore);
			return OpenDeclarationResult.failureUnexpectedError();
		} finally {
			UniversalServerUtilities.logDebugMessage(CLASS_NAME, "Releasing read lock for project_index", _dataStore); //$NON-NLS-1$
			project_index.releaseReadLock();
		}
		
		if(ast == null){
			return OpenDeclarationResult.failureUnexpectedError();
		}
			
		INavigationErrorLogger logger = new INavigationErrorLogger() {
			public void logDebugMessage(String message) {
				UniversalServerUtilities.logDebugMessage(CLASS_NAME, message, _dataStore);
			}
			public void logError(String message, Throwable e) {
				UniversalServerUtilities.logError(CLASS_NAME, e.toString(), e, _dataStore);	
			}
		};
		
		IIndex index = RemoteIndexManager.getInstance().getIndexForScope(Scope.WORKSPACE_ROOT_SCOPE_NAME, _dataStore);
		return doHandleOpenDeclaration(ast, workingCopy, selectedText, selectionStart, selectionLength, index, logger);
		
	}

	
	
	
	/* -- ST-Origin --
	 * Source folder: org.eclipse.cdt.ui/src
	 * Class: org.eclipse.cdt.internal.ui.search.actions.OpenDeclarationsJob
	 * Version: 1.22
	 */	
	
	public static OpenDeclarationResult doHandleOpenDeclaration(IASTTranslationUnit ast, ITranslationUnit workingCopy, String selectedText, 
			                                                     int selectionStart, int selectionLength, IIndex index, INavigationErrorLogger logger) {
		
		try{
			logger.logDebugMessage("Acquiring read lock for workspace_scope_index"); //$NON-NLS-1$
			index.acquireReadLock();
			final IASTNodeSelector nodeSelector = ast.getNodeSelector(null);
			
			IASTName sourceName= nodeSelector.findEnclosingName(selectionStart, selectionLength);
			IName[] implicitTargets = findImplicitTargets(index, ast, nodeSelector, selectionStart, selectionLength);
			if (sourceName == null) {
				if (implicitTargets.length > 0) {
					ICElement[] elements = convertToCElements(workingCopy, index, implicitTargets, logger);
					return OpenDeclarationResult.resultCElements(elements);				
				}
			} else {
				IASTNode parent = sourceName.getParent();
				if (parent instanceof IASTPreprocessorIncludeStatement) {
					String includedPath = ((IASTPreprocessorIncludeStatement) parent).getPath();
					if (includedPath == null || includedPath.equals("")) //$NON-NLS-1$
						return OpenDeclarationResult.failureIncludeLookup(selectedText);
					else
						return OpenDeclarationResult.resultIncludePath(includedPath);
				}
				
				NameKind kind = getNameKind(sourceName);
				IBinding b = sourceName.resolveBinding();
				IBinding[] bindings = new IBinding[] { b };
				if (b instanceof IProblemBinding) {
					IBinding[] candidateBindings = ((IProblemBinding) b).getCandidateBindings();
					if (candidateBindings.length != 0) {
						bindings = candidateBindings;
					}
				} else if (kind == NameKind.DEFINITION && b instanceof IType) {
					// Don't navigate away from a type definition.
					// Select the name at the current location instead.
					return OpenDeclarationResult.resultName(new SimpleName(sourceName));
				}
				IName[] targets = IName.EMPTY_ARRAY;
				String filename = ast.getFilePath();
				for (IBinding binding : bindings) {
					if (binding != null && !(binding instanceof IProblemBinding)) {
						IName[] names = findDeclNames(index, ast, kind, binding);
						for (final IName name : names) {
							if (name != null) {

								if (name instanceof IIndexName &&
										filename.equals(((IIndexName) name).getFileLocation().getFileName())) {
									// Exclude index names from the current file.
								} else if (areOverlappingNames(name, sourceName)) {
									// Exclude the current location.
								} else if (binding instanceof IParameter) {
									if (isInSameFunction(sourceName, name)) {
										targets = ArrayUtil.append(targets, name);
									}
								} else if (binding instanceof ICPPTemplateParameter) {
									if (isInSameTemplate(sourceName, name)) {
										targets = ArrayUtil.append(targets, name);			
									}
								} else {
									targets = ArrayUtil.append(targets, name);
								}
							}
						}
					}
					targets = ArrayUtil.trim(ArrayUtil.addAll(targets, implicitTargets));
					ICElement[] elements = convertToCElements(workingCopy, index, targets, logger);
					if(elements != null && elements.length > 0)
						return OpenDeclarationResult.resultCElements(elements);					
					else if(hasAtLeastOneLocation(targets))
						return OpenDeclarationResult.resultNames(convertNames(targets));
					
					return navigationFallBack(ast, index, selectedText, logger, workingCopy, sourceName, kind); 
				}
			
			}
			
			// No enclosing name, check if we're in an include statement
			IASTNode node = nodeSelector.findEnclosingNode(selectionStart, selectionLength);
			if (node instanceof IASTPreprocessorIncludeStatement) {
				String includedPath = ((IASTPreprocessorIncludeStatement) node).getPath();
				if (includedPath != "") //$NON-NLS-1$
					return OpenDeclarationResult.resultIncludePath(includedPath);
				else
					return OpenDeclarationResult.failureIncludeLookup(selectedText);
			} else if (node instanceof IASTPreprocessorFunctionStyleMacroDefinition) {
				IASTPreprocessorFunctionStyleMacroDefinition mdef= (IASTPreprocessorFunctionStyleMacroDefinition) node;
				for (IASTFunctionStyleMacroParameter par: mdef.getParameters()) {
					String parName= par.getParameter();
					if (parName.equals(selectedText)) {
						IASTFileLocation location = par.getFileLocation();
						if (location != null)
							return OpenDeclarationResult.resultLocation(new SimpleASTFileLocation(par.getFileLocation()));
					} 
				}
			}
					
			return navigationFallBack(ast, index, selectedText, logger, workingCopy, sourceName, NameKind.REFERENCE); 
		}catch (InterruptedException e) {
			logger.logError(e.toString(), e);
			return OpenDeclarationResult.failureUnexpectedError();
		}
		catch (CoreException e) {
			logger.logError(e.toString(), e);
			return OpenDeclarationResult.failureUnexpectedError();
		} finally {
			logger.logDebugMessage("Releasing read lock for workspace_scope_index"); //$NON-NLS-1$
			index.releaseReadLock();
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
	
	
	private static boolean areOverlappingNames(IName n1, IName n2) {
		if (n1 == n2)
			return true;

		IASTFileLocation loc1 = n1.getFileLocation();
		IASTFileLocation loc2 = n2.getFileLocation();
		if (loc1 == null || loc2 == null)
			return false;
		return loc1.getFileName().equals(loc2.getFileName()) &&
				max(loc1.getNodeOffset(), loc2.getNodeOffset()) <
				min(loc1.getNodeOffset() + loc1.getNodeLength(), loc2.getNodeOffset() + loc2.getNodeLength());
	}

	private static boolean isInSameFunction(IASTName refName, IName funcDeclName) {
		if (funcDeclName instanceof IASTName) {
			IASTDeclaration fdef = getEnclosingFunctionDefinition((IASTNode) funcDeclName);
			return fdef != null && fdef.contains(refName);
		} 
		return false;
	}
	
	private static IASTDeclaration getEnclosingFunctionDefinition(IASTNode node) {
		while (node != null && !(node instanceof IASTFunctionDefinition)) {
			node= node.getParent();
		}
		return (IASTDeclaration) node;
	}

	private static boolean isInSameTemplate(IASTName refName, IName templateDeclName) {
		
		if (templateDeclName instanceof IASTName) {
			IASTDeclaration template = getEnclosingTemplateDeclaration(refName);
			return template != null && template.contains(refName);
		} 
		return false;
	}
	
	private static IASTDeclaration getEnclosingTemplateDeclaration(IASTNode node) {
		while (node != null && !(node instanceof ICPPASTTemplateDeclaration)) {
			node= node.getParent();
		}
		return (IASTDeclaration) node;
	}



	private static ICElement[] convertToCElements(ITranslationUnit unit, IIndex index, IName[] names, INavigationErrorLogger logger) {
		List<ICElement> elements = new ArrayList<ICElement>();
		for(IName name : names) {
			try {
				ICElement element = getCElementForName(unit, index, name);
				if(element instanceof ISourceReference)
					elements.add(element);
			} catch (CoreException e) {
				logger.logError(e.toString(), e);
			} catch (DOMException e) {
				logger.logError(e.toString(), e); 
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
	
	private static IName[] findDeclNames(IIndex index, IASTTranslationUnit ast, NameKind kind, IBinding binding) throws CoreException {
		IName[] declNames = findNames(index, ast, kind, binding);
		// Bug 207320, handle template instances.
		while (declNames.length == 0 && binding instanceof ICPPSpecialization) {
			binding = ((ICPPSpecialization) binding).getSpecializedBinding();
			if (binding != null && !(binding instanceof IProblemBinding)) {
				declNames = findNames(index, ast, NameKind.DEFINITION, binding);
			}
		}
		if (declNames.length == 0 && binding instanceof ICPPMethod) {
			// Bug 86829, handle implicit methods.
			ICPPMethod method= (ICPPMethod) binding;
			if (method.isImplicit()) {
				IBinding clsBinding= method.getClassOwner();
				if (clsBinding != null && !(clsBinding instanceof IProblemBinding)) {
					declNames= findNames(index, ast, NameKind.REFERENCE, clsBinding);
				}
			}
		}
		return declNames;
	}
	

	private static IName[] findNames(IIndex index, IASTTranslationUnit ast, NameKind kind, IBinding binding) throws CoreException {
		IName[] declNames;
		if (kind == NameKind.DEFINITION) {
			declNames= findDeclarations(index, ast, binding);
		} else {
			declNames= findDefinitions(index, ast, kind, binding);
		}

		if (declNames.length == 0) {
			if (kind == NameKind.DEFINITION) {
				declNames= findDefinitions(index, ast, kind, binding);
			} else {
				declNames= findDeclarations(index, ast, binding);
			}
		}
		return declNames;
	}

	
	private static IName[] findDefinitions(IIndex index, IASTTranslationUnit ast, NameKind kind, IBinding binding) throws CoreException {
		List<IASTName> declNames= new ArrayList<IASTName>();
		declNames.addAll(Arrays.asList(ast.getDefinitionsInAST(binding)));
		for (Iterator<IASTName> i = declNames.iterator(); i.hasNext();) {
			IASTName name= i.next();
			final IBinding b2 = name.resolveBinding();
			if (b2 instanceof ICPPUsingDeclaration) {
				i.remove();
			}
			if (binding != b2 && binding instanceof ICPPSpecialization) {
				// Make sure binding specializes b2 so that for instance we do not navigate from
				// one partial specialization to another.
				IBinding spec= binding;
				while (spec instanceof ICPPSpecialization) {
					spec= ((ICPPSpecialization) spec).getSpecializedBinding();
					if (spec == b2)
						break;
				}
				if (!(spec instanceof ICPPSpecialization)) {
					i.remove();
				}
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
	 * Returns definitions of bindings referenced by implicit name at the given location.
	 */
	private static IName[] findImplicitTargets(IIndex index, IASTTranslationUnit ast, IASTNodeSelector nodeSelector,
			int offset, int length) throws CoreException {
		IName[] definitions = IName.EMPTY_ARRAY;
		IASTName firstName = nodeSelector.findEnclosingImplicitName(offset, length);
		if (firstName != null) {
			IASTImplicitNameOwner owner = (IASTImplicitNameOwner) firstName.getParent();
			for (IASTImplicitName name : owner.getImplicitNames()) {
				if (((ASTNode) name).getOffset() == ((ASTNode) firstName).getOffset()) {
					IBinding binding = name.resolveBinding(); // Guaranteed to resolve.
					IName[] declNames = findDeclNames(index, ast, NameKind.REFERENCE, binding);
					definitions = ArrayUtil.addAll(definitions, declNames);
				}
			}
		}
		return ArrayUtil.trim(definitions);
	}
	
	private static IBinding getBinding(IName name) {
		if (name instanceof IASTName) {
			return ((IASTName) name).resolveBinding();
		} else if (name instanceof IIndexFragmentName) {
			try {
				return ((IIndexFragmentName) name).getBinding();
			} catch (CoreException e) {
				// Fall through to return null.
			}
		}
		return null;
	}

	private static NameKind getNameKind(IName name) {
		if (name.isDefinition()) {
			if (getBinding(name) instanceof ICPPUsingDeclaration) {
				return NameKind.USING_DECL;
			} else {
				return NameKind.DEFINITION;
			}
		} else if (name.isDeclaration()) {
			return NameKind.DECLARATION;
		}
		return NameKind.REFERENCE;
	}
	

	/**
	 * If the names cannot be found using a binding then fall back to a text search.
	 */
	private static OpenDeclarationResult navigationFallBack(IASTTranslationUnit ast, IIndex index, String selectedText, INavigationErrorLogger logger, ITranslationUnit tu, IASTName sourceName, NameKind kind) {
		if(selectedText == null || selectedText.length() == 0)
			return null;
		
		try {
			final char[] name = selectedText.toCharArray();			
			List<IName> nameList = new ArrayList<IName>();
			List<ICElement> elems= new ArrayList<ICElement>();
			
			// Bug 252549, search for names in the AST first.
			Set<IBinding> primaryBindings= new HashSet<IBinding>();
			ASTNameCollector nc= new ASTNameCollector(selectedText);
			ast.accept(nc);
			IASTName[] candidates= nc.getNames();
			for (IASTName astName : candidates) {
				try {
					IBinding b= astName.resolveBinding();
					if (b != null && !(b instanceof IProblemBinding)){ 
						primaryBindings.add(b);
					}
				} catch (RuntimeException e) {
					RDTLog.logError(e);
				}
			}

			// Search the index, also.
			IndexFilter filter = IndexFilter.getDeclaredBindingFilter(ast.getLinkage().getLinkageID(), false);			
			IIndexBinding[] idxBindings = index.findBindings(name, false, filter, null);
			for (IIndexBinding idxBinding : idxBindings) {
				
				primaryBindings.add(idxBinding);
				
			}
			
			IIndexMacro[] macros = index.findMacros(name, filter, null);
			for(final IIndexMacro macro : macros) {
				IName macroName = new SimpleName(macro.getFileLocation(), macro.getNameCharArray());
				nameList.add(macroName);
			}
			ICElement[] elements = convertToCElements(tu, index, (nameList.toArray(new IName[nameList.size()])), logger);
			for (ICElement element : elements) {
				elems.add(element);
			}			
			
			Collection<IBinding> secondaryBindings;
			if (ast instanceof ICPPASTTranslationUnit) {
				secondaryBindings= cppRemoveSecondaryBindings(primaryBindings, sourceName);
			} else {
				secondaryBindings= defaultRemoveSecondaryBindings(primaryBindings, sourceName);
			}

			// Convert bindings to CElements.
			Collection<IBinding> bs= primaryBindings;
			for (int k = 0; k < 2; k++) {
				for (IBinding binding : bs) {
					IName[] names = (findNames(index, ast, kind, binding));
					// Exclude names of the same kind.
					for (int i = 0; i < names.length; i++) {
						if (getNameKind(names[i]) == kind) {
							names[i] = null;
						}
					}
					names = (IName[]) ArrayUtil.removeNulls(IName.class, names);					
					elements = convertToCElements(tu, index, names, logger);
					for (ICElement element : elements) {
						elems.add(element);
					}			
				}
				// In case we did not find anything, consider the secondary bindings.
				if (!elems.isEmpty())
					break;
				bs= secondaryBindings;
			}			
			if(!elems.isEmpty())
				return OpenDeclarationResult.resultCElements((ICElement[])elems.toArray());
			if (sourceName != null && sourceName.isDeclaration()) {
				// Select the name at the current location as the last resort.
				return OpenDeclarationResult.resultName(new SimpleName(sourceName));
			}
			
		} catch (CoreException e) {
			logger.logError(e.toString(), e);
		}
		
		return OpenDeclarationResult.failureSymbolLookup(selectedText);
	}
	
	private static Collection<IBinding> defaultRemoveSecondaryBindings(Set<IBinding> primaryBindings, IASTName sourceName) {
		if (sourceName != null) {
			IBinding b= sourceName.resolveBinding();
			if (b != null && ! (b instanceof IProblemBinding)) {
				try {
					for (Iterator<IBinding> iterator = primaryBindings.iterator(); iterator.hasNext();) {
						if (!checkOwnerNames(b, iterator.next()))
							iterator.remove();
					}
				} catch (DOMException e) {
					// Ignore
				}
			}
		}
		return Collections.emptyList();
	}
	
	private static boolean checkOwnerNames(IBinding b1, IBinding b2) throws DOMException {
		IBinding o1 = b1.getOwner();
		IBinding o2 = b2.getOwner();
		if (o1 == o2)
			return true;

		if (o1 == null || o2 == null)
			return false;

		if (!CharArrayUtils.equals(o1.getNameCharArray(), o2.getNameCharArray()))
			return false;

		return checkOwnerNames(o1, o2);
	}
	
	private static Collection<IBinding> cppRemoveSecondaryBindings(Set<IBinding> primaryBindings, IASTName sourceName) {
		List<IBinding> result= new ArrayList<IBinding>();
		String[] sourceQualifiedName= null;
		int funcArgCount= -1;
		if (sourceName != null) {
			final IBinding binding = sourceName.resolveBinding();
			if (binding != null) {
				sourceQualifiedName= CPPVisitor.getQualifiedName(binding);
				if (binding instanceof ICPPUnknownBinding) {
					LookupData data= CPPSemantics.createLookupData(sourceName);
					if (data.isFunctionCall()) {
						funcArgCount= data.getFunctionArgumentCount();
					}
				}
			}
		}

		for (Iterator<IBinding> iterator = primaryBindings.iterator(); iterator.hasNext();) {
			IBinding binding = iterator.next();
			if (sourceQualifiedName != null) {
				String[] qualifiedName = CPPVisitor.getQualifiedName(binding);
				if (!Arrays.equals(qualifiedName, sourceQualifiedName)) {
					iterator.remove();
					continue;
				}
			}
			if (funcArgCount != -1) {
				// For c++ we can check the number of parameters.
				if (binding instanceof ICPPFunction) {
					ICPPFunction f= (ICPPFunction) binding;
					if (f.getRequiredArgumentCount() > funcArgCount) {
						iterator.remove();
						result.add(binding);
						continue;
					}
					if (!f.takesVarArgs() && !f.hasParameterPack()) {
						final IType[] parameterTypes = f.getType().getParameterTypes();
						int maxArgs= parameterTypes.length;
						if (maxArgs == 1 && SemanticUtil.isVoidType(parameterTypes[0])) {
							maxArgs= 0;
						}
						if (maxArgs < funcArgCount) {
							iterator.remove();
							result.add(binding);
							continue;
						}
					}
				}
			}
		}

		return result;
	}
	
}
