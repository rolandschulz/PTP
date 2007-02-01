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
package org.eclipse.ptp.debug.core.sourcelookup;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
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
import org.eclipse.ptp.debug.internal.core.sourcelookup.PSourceLookupParticipant;

public class PSourceLookupDirector extends AbstractSourceLookupDirector implements IPSourceLookupDirector {
	private static Set fSupportedTypes;
	static {
		fSupportedTypes = new HashSet();
		fSupportedTypes.add(WorkspaceSourceContainer.TYPE_ID);
		fSupportedTypes.add(ProjectSourceContainer.TYPE_ID);
		fSupportedTypes.add(FolderSourceContainer.TYPE_ID);
		fSupportedTypes.add(DirectorySourceContainer.TYPE_ID);
		/**
		 *	No Mapping
		 * 	fSupportedTypes.add(MappingSourceContainer.TYPE_ID);
		 */
	}

	public void initializeParticipants() {
		addParticipants(new ISourceLookupParticipant[] { new PSourceLookupParticipant() });
	}
	public boolean supportsSourceContainerType(ISourceContainerType type) {
		return fSupportedTypes.contains(type.getId());
	}
	public boolean contains(IPBreakpoint breakpoint) {
		try {
			String handle = breakpoint.getSourceHandle();
			ISourceContainer[] containers = getSourceContainers();
			for (int i = 0; i < containers.length; ++i) {
				if (contains(containers[i], handle))
					return true;
			}
		} catch (CoreException e) {
		}
		return false;
	}
	public boolean contains(IProject project) {
		ISourceContainer[] containers = getSourceContainers();
		for (int i = 0; i < containers.length; ++i) {
			if (contains(containers[i], project))
				return true;
		}
		return false;
	}
	private boolean contains(ISourceContainer container, IProject project) {
		if (container instanceof ProjectSourceContainer && ((ProjectSourceContainer) container).getProject().equals(project)) {
			return true;
		}
		try {
			ISourceContainer[] containers;
			containers = container.getSourceContainers();
			for (int i = 0; i < containers.length; ++i) {
				if (contains(containers[i], project))
					return true;
			}
		} catch (CoreException e) {
		}
		return false;
	}
	private boolean contains(ISourceContainer container, String sourceName) {
		IPath path = new Path(sourceName);
		if (!path.isValidPath(sourceName))
			return false;
		if (container instanceof ProjectSourceContainer) {
			IProject project = ((ProjectSourceContainer) container).getProject();
			IPath projPath = project.getLocation();
			if (projPath.isPrefixOf(path)) {
				IFile file = ((ProjectSourceContainer) container).getProject().getFile(path.removeFirstSegments(projPath.segmentCount()));
				return (file != null && file.exists());
			}
		}
		if (container instanceof FolderSourceContainer) {
			IContainer folder = ((FolderSourceContainer) container).getContainer();
			IPath folderPath = folder.getLocation();
			if (folderPath.isPrefixOf(path)) {
				IFile file = ((FolderSourceContainer) container).getContainer().getFile(path.removeFirstSegments(folderPath.segmentCount()));
				return (file != null && file.exists());
			}
		}
		if (container instanceof PDirectorySourceContainer) {
			File dir = ((PDirectorySourceContainer) container).getDirectory();
			boolean searchSubfolders = ((PDirectorySourceContainer) container).searchSubfolders();
			IPath dirPath = new Path(dir.getAbsolutePath());
			if (searchSubfolders || dirPath.segmentCount() + 1 == path.segmentCount())
				return dirPath.isPrefixOf(path);
		}
		if (container instanceof MappingSourceContainer) {
			return (((MappingSourceContainer) container).getCompilationPath(sourceName) != null);
		}
		try {
			ISourceContainer[] containers;
			containers = container.getSourceContainers();
			for (int i = 0; i < containers.length; ++i) {
				if (contains(containers[i], sourceName))
					return true;
			}
		} catch (CoreException e) {
		}
		return false;
	}
	public IPath getCompilationPath(String sourceName) {
		IPath path = null;
		ISourceContainer[] containers = getSourceContainers();
		for (int i = 0; i < containers.length; ++i) {
			IPath cp = getCompilationPath(containers[i], sourceName);
			if (cp != null) {
				path = cp;
				break;
			}
		}
		return path;
	}
	private IPath getCompilationPath(ISourceContainer container, String sourceName) {
		IPath path = null;
		if (container instanceof MappingSourceContainer) {
			path = ((MappingSourceContainer) container).getCompilationPath(sourceName);
		} else {
			try {
				ISourceContainer[] containers;
				containers = container.getSourceContainers();
				for (int i = 0; i < containers.length; ++i) {
					path = getCompilationPath(containers[i], sourceName);
					if (path != null)
						break;
				}
			} catch (CoreException e) {
			}
		}
		return path;
	}
}
