/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.internal.debug.core.sourcelookup;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;
import org.eclipse.debug.core.sourcelookup.containers.DirectorySourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.FolderSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.ProjectSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.WorkspaceSourceContainer;
import org.eclipse.ptp.debug.core.model.IPBreakpoint;

public class PSourceLookupDirector extends AbstractSourceLookupDirector implements IPSourceLookupDirector {
	private static Set<String> fSupportedTypes;

	static {
		fSupportedTypes = new HashSet<String>();
		fSupportedTypes.add(WorkspaceSourceContainer.TYPE_ID);
		fSupportedTypes.add(ProjectSourceContainer.TYPE_ID);
		fSupportedTypes.add(FolderSourceContainer.TYPE_ID);
		fSupportedTypes.add(DirectorySourceContainer.TYPE_ID);
		fSupportedTypes.add(ResourceMappingSourceContainer.TYPE_ID);

	}

	/**
	 * Test if the breakpoint is in a source file in the project.
	 * 
	 * @param breakpoint
	 *            breakpoint to test
	 * @return true if the breakpoint is in a source file
	 */
	public boolean contains(IPBreakpoint breakpoint) {
		try {
			final String handle = breakpoint.getSourceHandle();
			for (ISourceContainer container : getSourceContainers()) {
				if (contains(container, handle)) {
					return true;
				}
			}
		} catch (final CoreException e) {
		}
		return false;
	}

	/**
	 * @param project
	 * @return
	 */
	public boolean contains(IProject project) {
		for (ISourceContainer container : getSourceContainers()) {
			if (contains(container, project)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.sourcelookup.IPSourceLookupDirector#
	 * getCompilationPath(java.lang.String)
	 */
	public IPath getCompilationPath(String sourceName) {
		IPath path = null;
		for (ISourceContainer container : getSourceContainers()) {
			final IPath cp = getCompilationPath(container, sourceName);
			if (cp != null) {
				path = cp;
				break;
			}
		}
		return path;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.sourcelookup.ISourceLookupDirector#
	 * initializeParticipants()
	 */
	public void initializeParticipants() {
		addParticipants(new ISourceLookupParticipant[] { new PSourceLookupParticipant() });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector#
	 * supportsSourceContainerType
	 * (org.eclipse.debug.core.sourcelookup.ISourceContainerType)
	 */
	@Override
	public boolean supportsSourceContainerType(ISourceContainerType type) {
		return fSupportedTypes.contains(type.getId());
	}

	private boolean contains(ISourceContainer container, IProject project) {
		if (container instanceof ProjectSourceContainer && ((ProjectSourceContainer) container).getProject().equals(project)) {
			return true;
		}
		try {
			for (ISourceContainer child : container.getSourceContainers()) {
				if (contains(child, project)) {
					return true;
				}
			}
		} catch (final CoreException e) {
		}
		return false;
	}

	private boolean contains(ISourceContainer container, String sourceName) {
		final IPath path = new Path(sourceName);
		if (!path.isValidPath(sourceName)) {
			return false;
		}
		if (container instanceof ProjectSourceContainer) {
			final IProject project = ((ProjectSourceContainer) container).getProject();
			final IPath srcPath = new Path(sourceName);
			return project.exists(srcPath);
		}
		if (container instanceof FolderSourceContainer) {
			final IContainer folder = ((FolderSourceContainer) container).getContainer();
			final IPath srcPath = new Path(sourceName);
			return folder.exists(srcPath);
		}
		if (container instanceof ResourceMappingSourceContainer) {
			return (((ResourceMappingSourceContainer) container).getCompilationPath(sourceName) != null);
		}
		try {
			for (ISourceContainer child : container.getSourceContainers()) {
				if (contains(child, sourceName)) {
					return true;
				}
			}
		} catch (final CoreException e) {
		}
		return false;
	}

	private IPath getCompilationPath(ISourceContainer container, String sourceName) {
		IPath path = null;
		if (container instanceof ResourceMappingSourceContainer) {
			path = ((ResourceMappingSourceContainer) container).getCompilationPath(sourceName);
		} else {
			try {
				for (ISourceContainer child : container.getSourceContainers()) {
					path = getCompilationPath(child, sourceName);
					if (path != null) {
						break;
					}
				}
			} catch (final CoreException e) {
			}
		}
		return path;
	}
}
