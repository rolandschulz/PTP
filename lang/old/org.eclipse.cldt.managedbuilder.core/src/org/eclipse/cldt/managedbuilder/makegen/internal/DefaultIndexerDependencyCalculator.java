/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cldt.managedbuilder.makegen.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cldt.core.FortranCorePlugin;
import org.eclipse.cldt.core.search.ICSearchConstants;
import org.eclipse.cldt.core.search.ICSearchScope;
import org.eclipse.cldt.core.search.SearchEngine;
import org.eclipse.cldt.internal.core.search.PathCollector;
import org.eclipse.cldt.internal.core.search.PatternSearchJob;
import org.eclipse.cldt.internal.core.search.indexing.IndexManager;
import org.eclipse.cldt.internal.core.search.matching.CSearchPattern;
import org.eclipse.cldt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cldt.managedbuilder.makegen.IManagedDependencyGenerator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;

/**
 * @since 2.0
 */
public class DefaultIndexerDependencyCalculator implements IManagedDependencyGenerator {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderDependencyCalculator#findDependencies(org.eclipse.core.resources.IResource)
	 */
	public IResource[] findDependencies(IResource resource, IProject project) {
		PathCollector pathCollector = new PathCollector();
		ICSearchScope scope = SearchEngine.createWorkspaceScope();
		CSearchPattern pattern = CSearchPattern.createPattern(resource.getLocation().toOSString(), ICSearchConstants.INCLUDE, ICSearchConstants.REFERENCES, ICSearchConstants.EXACT_MATCH, true);
		IndexManager indexManager = FortranCorePlugin.getDefault().getCoreModel().getIndexManager();
		indexManager.performConcurrentJob(
			new PatternSearchJob(
				(CSearchPattern) pattern,
				scope,
				pathCollector, 
				indexManager),
			ICSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
			null, null);
		
		// We will get back an array of resource names relative to the workspace
		String[] deps = pathCollector.getPaths();
		
		// Convert them to something useful
		List depList = new ArrayList();
		IResource res = null;
		IWorkspaceRoot root = null;
		if (project != null) { 
			root = project.getWorkspace().getRoot();
		}
		for (int index = 0; index < deps.length; ++index) {
			res = root.findMember(deps[index]);
			if (res != null) {
				depList.add(res); 
			}
		}
		
		return (IResource[]) depList.toArray(new IResource[depList.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderDependencyCalculator#getCalculatorType()
	 */
	public int getCalculatorType() {
		// Tell the 
		return TYPE_INDEXER;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderDependencyCalculator#getDependencyCommand()
	 */
	public String getDependencyCommand(IResource resource, IManagedBuildInfo info) {
		// There is no command
		return null;
	}
}