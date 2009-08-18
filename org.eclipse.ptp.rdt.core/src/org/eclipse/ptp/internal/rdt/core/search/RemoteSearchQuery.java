/*******************************************************************************
 * Copyright (c) 2006, 2009 QNX Software Systems and others.
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
 * Version: 1.16
 */

package org.eclipse.ptp.internal.rdt.core.search;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.browser.ITypeReference;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ptp.internal.rdt.core.index.DummyName;

public abstract class RemoteSearchQuery implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public static final int FIND_DECLARATIONS = 0x1;
	public static final int FIND_DEFINITIONS = 0x2;
	public static final int FIND_REFERENCES = 0x4;
	public static final int FIND_DECLARATIONS_DEFINITIONS = FIND_DECLARATIONS | FIND_DEFINITIONS;
	public static final int FIND_ALL_OCCURANCES = FIND_DECLARATIONS | FIND_DEFINITIONS | FIND_REFERENCES;
	
	protected int flags;
	
	protected ICElement[] scope;
	protected ICProject[] projects;
	protected List<RemoteSearchMatch> fMatches;

	protected IIndexLocationConverter fConverter;

	protected RemoteSearchQuery(ICElement[] scope, int flags) {
		this.flags = flags;
		this.scope = scope;
		
		if (scope == null) {
			// All CDT projects in workspace
			// do nothing for now - see RemoteIndexManager.getIndexForProjects()
		} else {
			Map<String, ICProject> projectMap = new HashMap<String, ICProject>();
			
			for (int i = 0; i < scope.length; ++i) {
				ICProject project = scope[i].getCProject();
				if (project != null)
					projectMap.put(project.getElementName(), project);
			}
			
			projects = projectMap.values().toArray(new ICProject[projectMap.size()]);
		}
		
		fMatches = new LinkedList<RemoteSearchMatch>();
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
		if (binding != null) {
			IIndexName[] names= index.findNames(binding, flags);
			collectNames(index, names);
		}
	}

	protected void createLocalMatches(IASTTranslationUnit ast, IBinding binding) {
		if (binding != null) {
			Set<IASTName> names= new HashSet<IASTName>();
			names.addAll(Arrays.asList(ast.getDeclarationsInAST(binding)));
			names.addAll(Arrays.asList(ast.getDefinitionsInAST(binding)));
			names.addAll(Arrays.asList(ast.getReferences(binding)));

			for (IASTName name : names) {
				if (   ((flags & FIND_DECLARATIONS) != 0 && name.isDeclaration())
					|| ((flags & FIND_DEFINITIONS) != 0 && name.isDefinition())
					|| ((flags & FIND_REFERENCES) != 0 && name.isReference())) {
					
					RemoteASTTypeInfo typeInfo= RemoteASTTypeInfo.create(name);
					if (typeInfo != null) {
						ITypeReference ref= typeInfo.getResolvedReference();
						if (ref != null) {
							IASTFileLocation loc = name.getFileLocation();
							IIndexFileLocation indexLocation = fConverter.fromInternalFormat(typeInfo.getIFL().getFullPath());
							IIndexName indexName = new DummyName(name, loc, indexLocation);
							fMatches.add(new RemoteSearchMatch(indexName, typeInfo, ref.getOffset(), ref.getLength()));
						}
					}
				}
			}
		}
	}

	public int getFlags() {
		return flags;
	}
	
	public abstract IStatus runWithIndex(IIndex index, IIndexLocationConverter converter, IProgressMonitor monitor);

	public List<RemoteSearchMatch> getMatches() {
		return fMatches;
	}
	
	public void setMatches(List<RemoteSearchMatch> matches) {
		fMatches = matches;
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
}
