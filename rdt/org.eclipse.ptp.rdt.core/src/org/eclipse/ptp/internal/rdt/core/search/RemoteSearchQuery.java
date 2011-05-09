/*******************************************************************************
 * Copyright (c) 2006, 2011 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 * IBM Corporation
 *******************************************************************************/

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.ui/src
 * Class: org.eclipse.cdt.internal.ui.search.PDOMSearchQuery
 * Version: 1.34
 */

package org.eclipse.ptp.internal.rdt.core.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.browser.ITypeReference;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.browser.ASTTypeInfo;
import org.eclipse.cdt.internal.core.dom.parser.c.CVariableReadWriteFlags;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVariableReadWriteFlags;
import org.eclipse.cdt.internal.core.index.CIndex;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.IWritableIndex;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.internal.rdt.core.index.DummyFile;
import org.eclipse.ptp.internal.rdt.core.index.DummyName;
import org.eclipse.ptp.internal.rdt.core.index.IndexQueries;
import org.eclipse.ptp.internal.rdt.core.model.RemoteCProjectFactory;
import org.eclipse.ptp.internal.rdt.core.search.RemoteLineSearchElement.RemoteLineSearchElementMatch;



public abstract class RemoteSearchQuery implements Serializable {
	

	private static final long serialVersionUID = 1L;
	public static final int FIND_DECLARATIONS = IIndex.FIND_DECLARATIONS;
	public static final int FIND_DEFINITIONS = IIndex.FIND_DEFINITIONS;
	public static final int FIND_REFERENCES = IIndex.FIND_REFERENCES;

	public static final int FIND_DECLARATIONS_DEFINITIONS = FIND_DECLARATIONS | FIND_DEFINITIONS;
	public static final int FIND_ALL_OCCURRENCES = FIND_DECLARATIONS | FIND_DEFINITIONS | FIND_REFERENCES;
	
	/**
	 * Method names contain parameter types.
	 * e.g. <code>foo(int)</code>
	 */
	public final static long M_PARAMETER_TYPES= 1L << 0;

	protected int flags;
	
	protected ICElement[] scope;
	protected ICProject[] projects;
	private Set<String> fullPathFilter;

	protected List<RemoteSearchMatch> fMatches;
	
	protected Map<IIndexFile, RemoteLineSearchElement[]> remoteLineSearchElements;
	protected Map<IIndexFile, Set<RemoteLineSearchElementMatch>> remoteLineSearchElementMatches;
	protected Map<IIndexFileLocation, RemoteLineSearchElement[]> localLineSearchElements;
	protected Map<IIndexFileLocation, Set<RemoteLineSearchElementMatch>> localLineSearchElementMatches;

	protected IIndexLocationConverter fConverter;
	
	protected ICElement cElement;
	
	protected RemoteSearchQuery(ICElement[] scope, int flags) {
		this.flags = flags;
		this.scope = scope;
		
		if (scope == null) {
			// All CDT projects in workspace
			// do nothing for now - see RemoteIndexManager.getIndexForProjects()
		} else {
			Map<String, ICProject> projectMap = new HashMap<String, ICProject>();
			Set<String> pathFilter = new HashSet<String>();
			boolean needFilter= false;
			for (int i = 0; i < scope.length; ++i) {
				ICProject project = scope[i].getCProject();
				if (project != null && project.getProject().isOpen()) {
					IResource res= scope[i].getResource();
					if (res != null) {
						pathFilter.add(res.getFullPath().toString());
						needFilter= needFilter || !(res instanceof IProject);
					}
					projectMap.put(project.getElementName(), project);
				}
			
				projects = projectMap.values().toArray(new ICProject[projectMap.size()]);
				if (needFilter) {
					fullPathFilter= pathFilter;
				}

			}
		}
		
		fMatches = new LinkedList<RemoteSearchMatch>();
		remoteLineSearchElementMatches = new HashMap<IIndexFile, Set<RemoteLineSearchElementMatch>>();
		localLineSearchElementMatches = new HashMap<IIndexFileLocation, Set<RemoteLineSearchElementMatch>>();
		remoteLineSearchElements = new HashMap<IIndexFile, RemoteLineSearchElement[]>();
		localLineSearchElements = new HashMap<IIndexFileLocation, RemoteLineSearchElement[]>();
	}
	
	protected ICElement CElementForBinding(final IIndex index, IBinding binding)
	throws CoreException {
			IIndexName[] names= index.findNames(binding, IIndex.FIND_DECLARATIONS_DEFINITIONS);
			if (names.length > 0) {
				return IndexQueries.getCElementForName((ICProject) null, index, names[0], fConverter, new RemoteCProjectFactory());
				
			}
			return null;
	}
	
	
	
	
	
	public ICElement getcElement() {
		return cElement;
	}

	public boolean canRerun() {
		return true;
	}

	public boolean canRunInBackground() {
		return true;
	}

	/**
	 * Return true to filter name out of the match list.
	 * Override in a subclass to add scoping.
	 * @param name
	 * @return true to filter name out of the match list
	 */
	protected boolean filterName(IIndexName name) {
		return false; // i.e. keep it
	}
	private void createMatchesFromNames(IIndex index, Map<IIndexFile, Set<RemoteLineSearchElementMatch>> fileMatches,
			Collection<IIndexName> names, boolean isPolymorphicOnly) throws CoreException {
		if (names == null)
			return;

		ICProject preferred = getPreferredProject();
		for (IIndexName name : names) {
			if (!filterName(name)) {
				if (!isPolymorphicOnly || name.couldBePolymorphicMethodCall()) {
					IASTFileLocation loc = name.getFileLocation();
					IIndexFile file = name.getFile();
					//IIndexFileLocation indexLocation = fConverter.fromInternalFormat(loc.getFileName());
					//DummyFile indexDumFile = new DummyFile(indexLocation, file.getTimestamp());
					Set<RemoteLineSearchElementMatch> matches = fileMatches.get(file);
					if (matches == null) {
						matches = new HashSet<RemoteLineSearchElementMatch>();
						fileMatches.put(file, matches);
					}
					int nodeOffset = loc.getNodeOffset();
					int nodeLength = loc.getNodeLength();
					ICElement enclosingElement = null;
					IIndexName enclosingDefinition = name.getEnclosingDefinition();
					if (enclosingDefinition != null) {
						enclosingElement = IndexQueries.getCElementForName(preferred, index, enclosingDefinition, fConverter, new RemoteCProjectFactory());
					}
					boolean isWriteAccess = name.isWriteAccess();
					matches.add(new RemoteLineSearchElementMatch(nodeOffset, nodeLength, isPolymorphicOnly, enclosingElement,
							isWriteAccess));
				}
			}

		}
	}

	
	private void collectNames(IIndex index, Collection<IIndexName> names,
			Collection<IIndexName> polymorphicNames) throws CoreException {
		// group all matched names by files
		
		Map<IIndexFile, Set<RemoteLineSearchElementMatch>> thisRemoteLineSearchElementMatches =new HashMap<IIndexFile, Set<RemoteLineSearchElementMatch>>();
		createMatchesFromNames(index, thisRemoteLineSearchElementMatches, names, false);
		createMatchesFromNames(index, thisRemoteLineSearchElementMatches, polymorphicNames, true);
		for (Entry<IIndexFile, Set<RemoteLineSearchElementMatch>> entry : thisRemoteLineSearchElementMatches.entrySet()) {
			IIndexFile file = entry.getKey();
			Set<RemoteLineSearchElementMatch> matches = entry.getValue();
			RemoteLineSearchElement[] lineElements = {};
			// check if there is dirty text editor corresponding to file and convert matches
			IPath absolutePath = IndexLocationFactory.getAbsolutePath(file.getLocation());
			// scan file and group matches by line elements
			RemoteLineSearchElementMatch[] matchesArray = matches.toArray(new RemoteLineSearchElementMatch[matches.size()]);
			lineElements = RemoteLineSearchElement.createElements(file.getLocation(), matchesArray);
			IIndexFileLocation remoteIndexLocation = fConverter.fromInternalFormat(file.getLocation().getURI().getPath());
			DummyFile indexDumFile = new DummyFile(remoteIndexLocation, file.getTimestamp());
			remoteLineSearchElementMatches.put(indexDumFile, matches);
			for (RemoteLineSearchElement searchElement : lineElements) {
				searchElement.setLocation(remoteIndexLocation);
			}
			remoteLineSearchElements.put(indexDumFile, lineElements);
		}
		
		
		
		
		
	}

	
	private void collectNames(IIndex index, IIndexName[] names) throws CoreException {
		for (int i = 0; i < names.length; i++) {
			IIndexName name = names[i];
			if (!filterName(name)) {
				IASTFileLocation loc = name.getFileLocation();
				IIndexBinding binding= index.findBinding(name);
				IIndexFileLocation indexLocation = fConverter.fromInternalFormat(loc.getFileName());
				fMatches.add(new RemoteSearchMatch(index, binding, new DummyName(name, loc, indexLocation), loc.getNodeOffset(), loc.getNodeLength()));
			}
		}
	}
	
	protected void createMatches(IIndex index, IBinding binding) throws CoreException {
		createMatches(index, new IBinding[] { binding });
		/*
		if (binding != null) {
			IIndexName[] names= index.findNames(binding, flags);
			collectNames(index, names);
		}*/
	}
	
	protected void createMatches(IIndex index, IBinding[] bindings) throws CoreException {
		if (bindings == null)
			return;
		List<IIndexName> names= new ArrayList<IIndexName>();
		List<IIndexName> polymorphicNames= null;
		HashSet<IBinding> handled= new HashSet<IBinding>();
		
		for (IBinding binding : bindings) {
			if (binding != null && handled.add(binding)) {
				createMatches1(index, binding, names);
			}
		}
		
		if ((flags & FIND_REFERENCES) != 0) {
			for (IBinding binding : bindings) {
				if (binding != null) {
					List<? extends IBinding> specializations = IndexQueries.findSpecializations(binding);
					for (IBinding spec : specializations) {
						if (spec != null && handled.add(spec)) {
							createMatches1(index, spec, names);
						}
					}

					if (binding instanceof ICPPMethod) {
						ICPPMethod m= (ICPPMethod) binding;
						ICPPMethod[] msInBases = ClassTypeHelper.findOverridden(m);
						if (msInBases.length > 0) {
							if (polymorphicNames == null) {
								polymorphicNames= new ArrayList<IIndexName>();
							}
							for (ICPPMethod mInBase : msInBases) {
								if (mInBase != null && handled.add(mInBase)) {
									createMatches1(index, mInBase, polymorphicNames);
								}
							}
						}
					}
				}
			}
		}
		if (!names.isEmpty()) {
			collectNames(index, names, polymorphicNames);
		}

	}
	
	private void createMatches1(IIndex index, IBinding binding, List<IIndexName> names) throws CoreException {
		IIndexName[] bindingNames= index.findNames(binding, flags);
		if (fullPathFilter == null) {
			names.addAll(Arrays.asList(bindingNames));
		} else {
			for (IIndexName name : bindingNames) {
				String fullPath= name.getFile().getLocation().getFullPath();
				if (accept(fullPath)) 
					names.add(name);
			}
		}
	}
	
	private boolean accept(String fullPath) {
		for(;;) {
			if (fullPathFilter.contains(fullPath))
				return true;
			int idx= fullPath.lastIndexOf('/');
			if (idx < 0)
				return false;
			fullPath= fullPath.substring(0, idx);
		} 
	}
	/* -- ST-Origin --
	 * Source folder: org.eclipse.cdt.ui/src
	 * Class: org.eclipse.cdt.internal.ui.search.CSearchUtil
	 * Version: 1.14
	 */
	public boolean isWriteOccurrence(IASTName node, IBinding binding) {
		boolean isWrite;
		if (binding instanceof ICPPVariable) {
			isWrite = ((CPPVariableReadWriteFlags.getReadWriteFlags(node) & PDOMName.WRITE_ACCESS) != 0);
		}
		else { 
			isWrite = ((CVariableReadWriteFlags.getReadWriteFlags(node) & PDOMName.WRITE_ACCESS) != 0);
		}
		return isWrite;
	}

	protected void createLocalMatches(IASTTranslationUnit ast, IBinding binding)  throws CoreException{
		
		if (binding != null) {
			Set<IASTName> names= new HashSet<IASTName>();
			names.addAll(Arrays.asList(ast.getDeclarationsInAST(binding)));
			names.addAll(Arrays.asList(ast.getDefinitionsInAST(binding)));
			names.addAll(Arrays.asList(ast.getReferences(binding)));
			// Collect local matches from AST
			IIndexFileLocation indexFileLocation = null;
			Set<RemoteLineSearchElementMatch> localMatches = new HashSet<RemoteLineSearchElementMatch>();
			for (IASTName name : names) {
				if (((flags & FIND_DECLARATIONS) != 0 && name.isDeclaration()) ||
						((flags & FIND_DEFINITIONS) != 0 && name.isDefinition()) ||
						((flags & FIND_REFERENCES) != 0 && name.isReference())) {
					ASTTypeInfo typeInfo= ASTTypeInfo.create(name);
					if (typeInfo != null) {
						ITypeReference ref= typeInfo.getResolvedReference();
						indexFileLocation = typeInfo.getIFL();
						if (ref != null) {
							
							//IASTFileLocation loc = name.getFileLocation();
							//IIndexFileLocation indexLocation = fConverter.fromInternalFormat(loc.getFileName());
							//DummyFile indexDumFile = new DummyFile(indexLocation);
							//Set<RemoteLineSearchElementMatch> localMatches = thislocalLineSearchElementMatches.get(indexFileLocation);
							//if (localMatches == null) {
							
							//	thislocalLineSearchElementMatches.put(indexFileLocation, localMatches);
							//}
							ICElement element = null;
							IASTNode node = name;
							while (node != null && !(node instanceof IASTFunctionDefinition)) {
								node= node.getParent();
							}
							if (node != null) {
								IASTFunctionDefinition definition = (IASTFunctionDefinition) node;
								element = IndexQueries.getCElementForName(getPreferredProject(),
										ast.getIndex(), definition.getDeclarator().getName(), fConverter, new RemoteCProjectFactory());
							}
							boolean isWrite = isWriteOccurrence(name, binding);
							localMatches.add(new RemoteLineSearchElementMatch(ref.getOffset(), ref.getLength(), false,
									element, isWrite));
							
							
						}
					}
				}
			}
			RemoteLineSearchElementMatch[] matchesArray = localMatches.toArray(new RemoteLineSearchElementMatch[localMatches.size()]);
			RemoteLineSearchElement[] lineElements = RemoteLineSearchElement.createElements(indexFileLocation, matchesArray);
			IIndexFileLocation remoteIndexLocation = fConverter.fromInternalFormat(indexFileLocation.getURI().getPath());
			localLineSearchElementMatches.put(remoteIndexLocation, localMatches);
			localLineSearchElements.put(remoteIndexLocation, lineElements);
			
			
			
			
		}
	
	}
	
	private ICProject getPreferredProject() {
		ICProject preferred= null;
		if (projects != null && projects.length == 1) {
			preferred= projects[0];
		}
		return preferred;
	}


	public int getFlags() {
		return flags;
	}
	
	public abstract void runWithIndex(IIndex parseIndex,  IIndex searchScopeindex, IIndexLocationConverter converter, IProgressMonitor monitor) throws OperationCanceledException, CoreException, DOMException, InterruptedException;
	
	public IStatus runWithIndex(IWritableIndex[] indexList, IIndexLocationConverter converter, IProgressMonitor monitor){
		
		IStatus status = null;
		IIndex searchScopeIndex = null;
		if(indexList.length==1){
			searchScopeIndex = indexList[0];
		}else if(indexList.length>1){
			Set<IIndexFragment> fragments = new HashSet<IIndexFragment>();
			for(IWritableIndex projectIndex :  indexList){
				if(projectIndex !=null){
					IIndexFragment fragment = projectIndex.getWritableFragment();
					fragments.add(fragment);
				}
			}
			if(!fragments.isEmpty()){
				searchScopeIndex = new CIndex(fragments.toArray(new IIndexFragment[fragments.size()]), fragments.size()); 
			}
		}
		
		if(searchScopeIndex !=null){
				
			for(IWritableIndex projectIndex :  indexList){
				try{
					if(projectIndex!=null){
						runWithIndex(projectIndex,  searchScopeIndex, converter, monitor);
					}
				}catch (InterruptedException e1) {
					if(status !=null){
						status = CCorePlugin.createStatus(status.getMessage() + "::" + e1.getMessage()); //$NON-NLS-1$
					}else{
						status = CCorePlugin.createStatus(e1.getMessage());
					}
					
				}  catch (CoreException e2) {
					if(status !=null){
						status = CCorePlugin.createStatus(status.getMessage() + "::" + e2.getMessage()); //$NON-NLS-1$
					}else{
						status = CCorePlugin.createStatus(e2.getMessage());
					}	
				} catch (DOMException e3) {
					if(status !=null){
						status = CCorePlugin.createStatus(status.getMessage() + "::" + e3.getMessage()); //$NON-NLS-1$
					}else{
						status = CCorePlugin.createStatus(e3.getMessage());
					}
				}
				

			}
		}
		
		if(status !=null){
			return status;
		}else{
			return Status.OK_STATUS;
		}
	}

	public List<RemoteSearchMatch> getMatches() {
		return fMatches;
	}
	
	public void setMatches(List<RemoteSearchMatch> matches) {
		fMatches = matches;
	}
		
	public Map<IIndexFile, Set<RemoteLineSearchElementMatch>> getRemoteLineSearchElementMatchs() {
		return remoteLineSearchElementMatches;
	}
	
	
	
	public Map<IIndexFileLocation, Set<RemoteLineSearchElementMatch>> getLocalLineSearchElementMatches() {
		return localLineSearchElementMatches;
	}
	
	
	public Map<IIndexFileLocation, RemoteLineSearchElement[]> getLocalLineSearchElements() {
		return localLineSearchElements;
	}

	public Map<IIndexFile, RemoteLineSearchElement[]> getRemoteLineSearchElements() {
		return remoteLineSearchElements;
	}

	/**
	 * Get the projects involved in the search.
	 * @return array, never <code>null</code>
	 */
	public ICProject[] getProjects() {
		return projects;
	}
	
	public String getScopeDescription() {
		StringBuilder buf= new StringBuilder();
		if (scope == null) {
			return ""; //$NON-NLS-1$
		}
		switch (scope.length) {
		case 0:
			break;
		case 1:
			buf.append(scope[0].getElementName());
			break;
		case 2:
			buf.append(scope[0].getElementName());
			buf.append(", "); //$NON-NLS-1$
			buf.append(scope[1].getElementName());
			break;
		default:
			buf.append(scope[0].getElementName());
			buf.append(", "); //$NON-NLS-1$
			buf.append(scope[1].getElementName());
			buf.append(", ..."); //$NON-NLS-1$
			break;
		}
		return buf.toString();
	}

	public void setfConverter(IIndexLocationConverter fConverter) {
		this.fConverter = fConverter;
	}
	
	
	
	
}
