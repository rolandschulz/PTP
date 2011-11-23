/*******************************************************************************
 * Copyright (c) 2011 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.core;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Class for filtering files during synchronization. Instead of a constructor, the user can create an empty filter or a filter that
 * has decent default behavior, filtering, for example, configuration files like .project and .cproject.
 * 
 * Facilities are then provided for adding and removing files and directories from filtering.
 */
public class SyncFileFilter {
	private final IProject project;
	private boolean filterBinaries = false;
	private final Set<IPath> filteredPaths = new HashSet<IPath>();

	// Private constructor - user should use create methods
	private SyncFileFilter(IProject p) {
		project = p;
	}
	
	// Copy constructor
	public SyncFileFilter(SyncFileFilter oldFilter) {
		project = oldFilter.project;
		filterBinaries = oldFilter.filterBinaries;
		filteredPaths.addAll(oldFilter.filteredPaths);
	}
	
	public static SyncFileFilter createEmptyFilter(IProject project) {
		return new SyncFileFilter(project);
	}
	
	public static SyncFileFilter createDefaultFilter(IProject project) {
		SyncFileFilter sff = new SyncFileFilter(project);
		sff.addDefaults();
		sff.setFilterBinaries(true);
		return sff;
	}
	
	/**
	 * Add the common, default list of paths to be filtered.
	 */
	public void addDefaults() {
		filteredPaths.add(new Path(".project")); //$NON-NLS-1$
		filteredPaths.add(new Path(".cproject")); //$NON-NLS-1$
		filteredPaths.add(new Path(".settings")); //$NON-NLS-1$
		// TODO: This Git-specific directory is defined in multiple places - need to refactor.
		filteredPaths.add(new Path(".ptp-sync")); //$NON-NLS-1$
	}
	
	/**
	 * Should binary files be filtered?
	 * @param b: true means filter, false means don't.
	 */
	public void setFilterBinaries(boolean b) {
		filterBinaries = b;
	}
	
	/**
	 * Return whether this is a binary file. Note that this only works for files recognized by CDT. This function does not yet
	 * recognize binary files in general.
	 * @param project
	 * @param path
	 * @return whether file is a binary file
	 */
	public boolean isBinaryFile(IProject project, IPath path) {
		try {
			ICElement fileElement = CoreModel.getDefault().create(project.getFile(path));
			if (fileElement == null) {
				return false;
			}
			int resType = fileElement.getElementType();
			if (resType == ICElement.C_BINARY) {
				return true;
			} else {
				return false;
			}
		} catch (NullPointerException e) {
			// CDT throws this exception for files not recognized. For now, be conservative and allow these files.
			return false;
		}
	}
	
	public boolean addPath(IPath path) {
		return filteredPaths.add(path);
	}
	
	public boolean removePath(IPath path) {
		return filteredPaths.remove(path);
	}
	/**
	 * Apply the filter to the given path
	 * @param path
	 * @return whether the path should be ignored
	 */
	public boolean shouldIgnore(IPath path) {
		for (IPath p : filteredPaths) {
			if (p.isPrefixOf(path)) {
				return true;
			}
		}
		return filterBinaries && this.isBinaryFile(project, path);
	}
}
