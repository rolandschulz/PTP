/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Ed Swartz (Nokia)
 *    IBM Corporation
 *******************************************************************************/ 

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.ui/src
 * Class: org.eclipse.cdt.internal.ui.viewsupport.IndexUI
 * Version: 1.39
 */
package org.eclipse.ptp.internal.rdt.core.index;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroExpansion;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInstanceCache;
import org.eclipse.cdt.internal.core.index.IndexFileLocation;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.internal.rdt.core.model.BindingAdapter;
import org.eclipse.ptp.internal.rdt.core.model.CElement;
import org.eclipse.ptp.internal.rdt.core.model.ICProjectFactory;
import org.eclipse.ptp.internal.rdt.core.model.IIndexLocationConverterFactory;
import org.eclipse.ptp.internal.rdt.core.model.LocalIndexLocationConverterFactory;
import org.eclipse.ptp.internal.rdt.core.model.TranslationUnit;
import org.eclipse.ptp.rdt.core.RDTLog;
import org.eclipse.ptp.rdt.core.activator.Activator;

public class IndexQueries {
	private static final ICElement[] EMPTY_ELEMENTS = new ICElement[0];

	public static IIndexBinding elementToBinding(IIndex index, ICElement element, String path) throws CoreException {
		return elementToBinding(index, element, path, -1);
	}

	public static IIndexBinding elementToBinding(IIndex index, ICElement element, String path, int linkageID) throws CoreException {
		if (element instanceof ISourceReference) {
			ISourceReference sf = ((ISourceReference)element);
			ISourceRange range= sf.getSourceRange();
			if (range.getIdLength() != 0) {
				IIndexName name= remoteElementToName(index, element, path, linkageID);
				if (name != null) {
					return index.findBinding(name);
				}
			}
			else {
				String name= element.getElementName();
				name= name.substring(name.lastIndexOf(':')+1);
				IIndexBinding[] bindings= index.findBindings(name.toCharArray(), IndexFilter.ALL, new NullProgressMonitor());
				for (IIndexBinding binding : bindings) {
					if (checkBinding(binding, element)) {
						return binding;
					}
				}
			}
		}
		return null;
	}

	private static boolean checkBinding(IIndexBinding binding, ICElement element) {
		switch(element.getElementType()) {
		case ICElement.C_ENUMERATION:
			return binding instanceof IEnumeration;
		case ICElement.C_NAMESPACE:
			return binding instanceof ICPPNamespace;
		case ICElement.C_STRUCT_DECLARATION:
		case ICElement.C_STRUCT:
			return binding instanceof ICompositeType && 
				((ICompositeType) binding).getKey() == ICompositeType.k_struct;
		case ICElement.C_CLASS:
		case ICElement.C_CLASS_DECLARATION:
			return binding instanceof ICPPClassType && 
				((ICompositeType) binding).getKey() == ICPPClassType.k_class;
		case ICElement.C_UNION:
		case ICElement.C_UNION_DECLARATION:
			return binding instanceof ICompositeType && 
				((ICompositeType) binding).getKey() == ICompositeType.k_union;
		case ICElement.C_TYPEDEF:
			return binding instanceof ITypedef;
		case ICElement.C_METHOD:	
		case ICElement.C_METHOD_DECLARATION:
			return binding instanceof ICPPMethod;
		case ICElement.C_FIELD:
			return binding instanceof IField;
		case ICElement.C_FUNCTION:	
		case ICElement.C_FUNCTION_DECLARATION:
			return binding instanceof ICPPFunction && !(binding instanceof ICPPMethod);
		case ICElement.C_VARIABLE:
		case ICElement.C_VARIABLE_DECLARATION:
			return binding instanceof IVariable;
		case ICElement.C_ENUMERATOR:
			return binding instanceof IEnumerator;
		case ICElement.C_TEMPLATE_CLASS:
		case ICElement.C_TEMPLATE_CLASS_DECLARATION:
		case ICElement.C_TEMPLATE_STRUCT:
		case ICElement.C_TEMPLATE_STRUCT_DECLARATION:
		case ICElement.C_TEMPLATE_UNION:
		case ICElement.C_TEMPLATE_UNION_DECLARATION:
			return binding instanceof ICPPClassTemplate;
		case ICElement.C_TEMPLATE_FUNCTION:
		case ICElement.C_TEMPLATE_FUNCTION_DECLARATION:
			return binding instanceof ICPPFunctionTemplate && !(binding instanceof ICPPMethod);
		case ICElement.C_TEMPLATE_METHOD_DECLARATION:
		case ICElement.C_TEMPLATE_METHOD:
			return binding instanceof ICPPFunctionTemplate && binding instanceof ICPPMethod;
		case ICElement.C_TEMPLATE_VARIABLE:
			return binding instanceof ICPPTemplateParameter;
		}
		return false;
	}
	
	public static IIndexName elementToName(IIndex index, ICElement element) throws CoreException {
		return elementToName(index, element, -1);
	}


	public static IIndexName elementToName(IIndex index, ICElement element, int linkageID) throws CoreException {
		if (element instanceof ISourceReference) {
			ISourceReference sf = ((ISourceReference)element);
			ITranslationUnit tu= sf.getTranslationUnit();
			if (tu != null) {
				IIndexFileLocation location= IndexLocationFactory.getIFL(tu);
				if (location != null) {
					IIndexFile[] files= index.getFiles(location);
					for (IIndexFile file : files) {
						if (linkageID == -1 || file.getLinkageID() == linkageID) {
							String elementName= element.getElementName();
							int idx= elementName.lastIndexOf(":")+1; //$NON-NLS-1$
							ISourceRange pos= sf.getSourceRange();
	//						IRegion region = getConvertedRegion(tu, file, pos.getIdStartPos()+idx, pos.getIdLength()-idx);
							int offset = pos.getIdStartPos()+idx;
							int length = pos.getIdLength()-idx;
							IIndexName[] names= file.findNames(offset, length);
							for (IIndexName name2 : names) {
								IIndexName name = name2;
								if (!name.isReference() && elementName.endsWith(new String(name.getSimpleID()))) {
									return name;
								}
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	public static IIndexName remoteElementToName(IIndex index, ICElement element, String path) throws CoreException {
		return remoteElementToName(index, element, path, -1);
	}
	
	public static IIndexName remoteElementToName(IIndex index, ICElement element, String path, int linkageID) throws CoreException {
		if (element instanceof ISourceReference) {
			ISourceReference sf = ((ISourceReference)element);
			ITranslationUnit tu= sf.getTranslationUnit();
			if (tu != null) {
				
				URI uri = null;
				try {
					uri = convertPathToLocalURI(path);
				} catch (URISyntaxException e) {
					RDTLog.logError(e);
				}
				
				IIndexFileLocation location = null;
				
				if(uri != null) {
					location = new IndexFileLocation(uri, path);
				}

				if (location != null) {
					IIndexFile[] files= index.getFiles(location);
					for (IIndexFile file : files) {
						if (linkageID == -1 || file.getLinkageID() == linkageID) {
							String elementName= element.getElementName();
							int idx= elementName.lastIndexOf(":")+1; //$NON-NLS-1$
							ISourceRange pos= sf.getSourceRange();
	//						IRegion region = getConvertedRegion(tu, file, pos.getIdStartPos()+idx, pos.getIdLength()-idx);
							int offset = pos.getIdStartPos()+idx;
							int length = pos.getIdLength()-idx;
							IIndexName[] names= file.findNames(offset, length);
							for (IIndexName name2 : names)  {
								IIndexName name = name2;
								if (!name.isReference() && elementName.endsWith(new String(name.getSimpleID()))) {
									return name;
								}
							}
						}
					}
				}
			}
		}
		return null;
	}

	private static URI convertPathToLocalURI(String path) throws URISyntaxException {
		URI uri = new URI("file", null, path, null); //$NON-NLS-1$
		return uri;
	}

	public static boolean isIndexed(IIndex index, ICElement element) throws CoreException {
		if (element instanceof ISourceReference) {
			ISourceReference sf = ((ISourceReference)element);
			ITranslationUnit tu= sf.getTranslationUnit();
			if (tu != null) {
				IIndexFileLocation location= IndexLocationFactory.getIFL(tu);
				if (location != null) {
					IIndexFile[] files= index.getFiles(location);
					return files.length > 0;
				}
			}
		}
		return false;
	}
	
	public static IIndexInclude elementToInclude(IIndex index, IInclude include) throws CoreException {
		if (include != null) {
			ITranslationUnit tu= include.getTranslationUnit();
			if (tu != null) {
				IIndexFileLocation location= IndexLocationFactory.getIFL(tu);
				if (location != null) {
					IIndexFile[] files= index.getFiles(location);
					for (IIndexFile file : files) {
						String elementName= include.getElementName();
						ISourceRange pos= include.getSourceRange();
						//IRegion region= getConvertedRegion(tu, file, pos.getIdStartPos(), pos.getIdLength());
						int offset = pos.getIdStartPos();

						IIndexInclude[] includes= index.findIncludes(file);
						int bestDiff= Integer.MAX_VALUE;
						IIndexInclude best= null;
						for (IIndexInclude candidate : includes) {
							int diff= Math.abs(candidate.getNameOffset()- offset);
							if (diff > bestDiff) {
								break;
							}
							if (candidate.getFullName().endsWith(elementName)) {
								bestDiff= diff;
								best= candidate;
							}
						}
						if (best != null)
							return best;
					}
				}
			}
		}
		return null;
	}


	public static ICElement[] findRepresentative(IIndex index, IBinding binding, IIndexLocationConverterFactory converter, ICProject preferProject, ICProjectFactory projectFactory) throws CoreException {
		ICElement[] defs = findAllDefinitions(index, binding, converter, preferProject, projectFactory);
		if (defs.length == 0) {
			ICElement elem = findAnyDeclaration(index, preferProject, binding, converter, projectFactory);
			if (elem != null) {
				defs = new ICElement[] { elem };
			}
		}
		return defs;
	}
	
	public static String[] findRepresentitivePaths(IIndex index, IBinding binding, IIndexLocationConverterFactory converter, ICProject preferProject, ICProjectFactory projectFactory) throws CoreException {
		IIndexName[] defs= index.findNames(binding, IIndex.FIND_DEFINITIONS | IIndex.SEARCH_ACROSS_LANGUAGE_BOUNDARIES);
		ArrayList<String> paths = new ArrayList<String>();
		
		for(IIndexName name : defs) {
			String path=null;
			URI locationURI = name.getFile().getLocation().getURI();
			if(converter instanceof LocalIndexLocationConverterFactory){
				//this is a local query
				IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(locationURI);
				
				
				if(files != null && files.length > 0) {
					path = files[0].getFullPath().toString();
				}
				
			}else{
				path = locationURI.getPath();
			}
						
			if(path!=null){
				paths.add(path);
			}
		}
		return paths.toArray(new String[0]);
	}
	
	public static ICElement[] findAllDefinitions(IIndex index, IBinding binding, IIndexLocationConverterFactory converter, ICProject preferProject, ICProjectFactory projectFactory) throws CoreException {
		if (binding != null) {
			IIndexName[] defs= index.findNames(binding, IIndex.FIND_DEFINITIONS | IIndex.SEARCH_ACROSS_LANGUAGE_BOUNDARIES);
			
			ArrayList<ICElement> result= new ArrayList<ICElement>();
			for (IIndexName in : defs) {
				ICElement definition= getCElementForName(preferProject, index, in, converter, projectFactory);
				if (definition != null) {
					result.add(definition);
				}
				
			}
			return result.toArray(new ICElement[result.size()]);
		}
		return EMPTY_ELEMENTS;
	}
	
	/**
	 * Creates CElementHandles for definitions or declarations when you expect to find those
	 * in the index.
	 * @param preferProject
	 * @param index
	 * @param declName
	 * @return the ICElementHandle or <code>null</code>.
	 */
	public static ICElement getCElementForName(ICProject preferProject, IIndex index, IASTName declName, IIndexLocationConverterFactory converter, ICProjectFactory projectFactory) 
			throws CoreException {
		assert !declName.isReference();
		IBinding binding= declName.resolveBinding();
		if (binding != null) {
			ICProject cProject = preferProject;
			
			if(preferProject == null) {
				cProject = projectFactory.getProjectForFile(declName.getFileLocation().getFileName());
			}
			
			ITranslationUnit tu= getTranslationUnit(cProject, declName, converter);
			if (tu != null) {
//				IFile file= (IFile) tu.getResource();
//				long timestamp= file != null ? file.getLocalTimeStamp() : 0;
				IASTFileLocation loc= declName.getFileLocation();
//				IRegion region= new Region(loc.getNodeOffset(), loc.getNodeLength());
//				IPositionConverter converter= CCorePlugin.getPositionTrackerManager().findPositionConverter(tu, timestamp);
//				if (converter != null) {
//					region= converter.actualToHistoric(region);
//				}
				try {
					return BindingAdapter.adaptBinding(tu, binding, loc.getNodeOffset(), loc.getNodeLength(), declName.isDefinition());
				} catch (DOMException e) {
					throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "", e)); //$NON-NLS-1$
				}
			}
		}
		return null;
	}
	
	public static ITranslationUnit getTranslationUnit(ICProject cproject, IName name, IIndexLocationConverterFactory converter) {
		return getTranslationUnit(cproject, name.getFileLocation(), converter);
	}

	private static ITranslationUnit getTranslationUnit(ICProject cproject, final IASTFileLocation fileLocation,
			IIndexLocationConverterFactory converter) {
		if (converter == null)
			throw new IllegalArgumentException();

		if (fileLocation != null) {
			IPath path = Path.fromOSString(fileLocation.getFileName());
			if (converter != null) {
				IIndexFileLocation location = converter.getConverter(cproject).fromInternalFormat(fileLocation.getFileName());
				if(location == null) {
					try {
						return CoreModelUtil.findTranslationUnitForLocation(path, cproject);
					} catch (CModelException e) {
						RDTLog.logError(e);
					}
				}
				TranslationUnit unit = new TranslationUnit(cproject, path.lastSegment(), cproject == null ? null : cproject
						.getElementName(), location.getURI());
				return unit;
			}

		}
		return null;
	}

	public static ICElement getCElementForName(ICProject preferProject, IIndex index, IIndexName declName, IIndexLocationConverterFactory converter, ICProjectFactory projectFactory) 
			throws CoreException {
		assert !declName.isReference();
		
		ICProject cProject = preferProject;
		
		if(preferProject == null) {
			cProject = projectFactory.getProjectForFile(declName.getFileLocation().getFileName());
		}
		
		ITranslationUnit tu= getTranslationUnit(cProject, declName, converter);
		if (tu != null) {
			return getCElementForName(tu, index, declName, projectFactory);
		}
		return null;
	}
	
	public static ICElement getCElementForName(ITranslationUnit tu, IIndex index, IIndexName declName, ICProjectFactory projectFactory) 
			throws CoreException {
//		IRegion region= new Region(declName.getNodeOffset(), declName.getNodeLength());
//		long timestamp= declName.getFile().getTimestamp();
		try {
			return BindingAdapter.adaptBinding(tu, index.findBinding(declName), declName.getNodeOffset(), declName.getNodeLength(), declName.isDefinition());
		} catch (DOMException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "", e)); //$NON-NLS-1$
		}
	}

	public static ICElement findAnyDeclaration(IIndex index, ICProject preferProject, IBinding binding, IIndexLocationConverterFactory converter, ICProjectFactory projectFactory) 
			throws CoreException {
		if (binding != null) {
			IIndexName[] names= index.findNames(binding, IIndex.FIND_DECLARATIONS);
			for (IIndexName name : names) {
				ICElement elem= getCElementForName(preferProject, index, name, converter, projectFactory);
				if (elem != null) {
					return elem;
				}
			}
		}
		return null;
	}

	public static ICElement attemptConvertionToHandle(IIndex index, ICElement input, IIndexLocationConverterFactory converter, ICProjectFactory projectFactory) throws CoreException {
		if (input instanceof CElement) {
			return input;
		}
		IIndexName name= elementToName(index, input);
		if (name != null) {
			ICElement handle= getCElementForName(input.getCProject(), index, name, converter, projectFactory);
			if (handle != null) {
				return handle;
			}
		} 
		return input;
	}

	public static IASTName getSelectedName(IIndex index, ITranslationUnit workingCopy, int selectionStart, int selectionLength) throws CoreException {
		if (workingCopy == null)
			return null;
		
		int options= ITranslationUnit.AST_SKIP_INDEXED_HEADERS;
		IASTTranslationUnit ast = workingCopy.getAST(index, options);
		IASTNodeSelector nodeSelector = ast.getNodeSelector(null);
		IASTName name = nodeSelector.findEnclosingName(selectionStart, selectionLength);
		if (name == null) {
			name= nodeSelector.findImplicitName(selectionStart, selectionLength);
		}
		if (name != null && name.getParent() instanceof IASTPreprocessorMacroExpansion) {
			IASTFileLocation floc= name.getParent().getFileLocation();
			IASTNode node= nodeSelector.findEnclosingNodeInExpansion(floc.getNodeOffset(), floc.getNodeLength());
			if (node instanceof IASTName) {
				name= (IASTName) node;
			} else if (node instanceof IASTFunctionCallExpression){
				IASTExpression expr= ((IASTFunctionCallExpression) node).getFunctionNameExpression();
				if (expr instanceof IASTIdExpression) {
					name= ((IASTIdExpression) expr).getName();
				}
			} else {
				if (node instanceof IASTSimpleDeclaration) {
					IASTNode[] dtors= ((IASTSimpleDeclaration) node).getDeclarators();
					if (dtors != null && dtors.length > 0) {
						node= dtors[0];
					}
				} else if (node instanceof IASTFunctionDefinition) {
					node= ((IASTFunctionDefinition) node).getDeclarator();
				}
				if (node instanceof IASTDeclarator) {
					IASTDeclarator dtor= ASTQueries.findTypeRelevantDeclarator((IASTDeclarator) node);
					name= dtor.getName();
				}
			}
		}
		return name;
		
	}
	
	/**
	 * Searches for all specializations that depend on the definition of the given binding.
	 */
	public static List<? extends IBinding> findSpecializations(IBinding binding) throws CoreException {
		List<IBinding> result= null;

		IBinding owner = binding.getOwner();
		if (owner != null) {
			List<? extends IBinding> specializedOwners= findSpecializations(owner);
			if (!specializedOwners.isEmpty()) {
				result= new ArrayList<IBinding>(specializedOwners.size());

				for (IBinding specOwner : specializedOwners) {
					if (specOwner instanceof ICPPClassSpecialization) {
						result.add(((ICPPClassSpecialization) specOwner).specializeMember(binding));
					}
				}
			}
		}
		
		if (binding instanceof ICPPInstanceCache) {
			final List<ICPPTemplateInstance> instances= Arrays.asList(((ICPPInstanceCache) binding).getAllInstances());
			if (!instances.isEmpty()) {
				if (result == null)
					result= new ArrayList<IBinding>(instances.size());


				for (ICPPTemplateInstance inst : instances) {
					if (!IndexFilter.ALL_DECLARED.acceptBinding(inst)) {
						result.add(inst);
					}
				}
			}
		}
		
		if (result != null) {
			return result;
		}
		return Collections.emptyList();
	}
}
