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

import java.util.Arrays;
import java.util.List;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.rdt.sync.core.messages.Messages;
import org.eclipse.ui.IMemento;

/**
 * Class for filtering files during synchronization. Instead of a constructor, the user can create an empty filter or a filter that
 * has decent default behavior, filtering, for example, configuration files like .project and .cproject.
 * 
 * Facilities are then provided for adding and removing files and directories from filtering.
 */
public class SyncFileFilter {
	private static final String FILE_FILTER_PATH_ELEMENT_NAME = "file-filter-path"; //$NON-NLS-1$
	private static final String ATTR_PROJECT_NAME = "project"; //$NON-NLS-1$
	private static final String ATTR_FILTER_BINARIES = "filter-binaries"; //$NON-NLS-1$
	private static final String ATTR_FILE_FILTER_PATH = "filter-path"; //$NON-NLS-1$
	
	private final IProject project;
	private boolean filterBinaries = false;
	private final BasicTree filteredPaths = new BasicTree();

	// Private constructor - user should use create methods
	private SyncFileFilter(IProject p) {
		project = p;
	}
	
	// Copy constructor
	public SyncFileFilter(SyncFileFilter oldFilter) {
		project = oldFilter.project;
		filterBinaries = oldFilter.filterBinaries;
		List<List<String>> allPaths = oldFilter.filteredPaths.getItems();
		for (List<String> path : allPaths) {
			filteredPaths.add(path);
		}
	}
	
	/**
	 * Constructor for an empty filter for a given project. Most clients will want to use "createDefaultFilter"
	 *
	 * @param project
	 * @return the new filter
	 */
	public static SyncFileFilter createEmptyFilter(IProject project) {
		return new SyncFileFilter(project);
	}
	
	/**
	 * Constructor for a filter with all of the usual default paths already included.
	 *
	 * @param project
	 * @return the new filter
	 */
	public static SyncFileFilter createDefaultFilter(IProject project) {
		SyncFileFilter sff = new SyncFileFilter(project);
		sff.addDefaults();
		sff.setFilterBinaries(true);
		return sff;
	}
	
	/**
	 * Get filter's project
	 * @return project
	 */
	public IProject getProject() {
		return project;
	}
	
	/**
	 * Add the common, default list of paths to be filtered.
	 */
	public void addDefaults() {
		filteredPaths.add(".project"); //$NON-NLS-1$
		filteredPaths.add(".cproject"); //$NON-NLS-1$
		filteredPaths.add(".settings"); //$NON-NLS-1$
		// TODO: This Git-specific directory is defined in multiple places - need to refactor.
		filteredPaths.add(".ptp-sync"); //$NON-NLS-1$
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
	
	/**
	 * Add a path to the filter
	 * @param path
	 */
	public void addPath(IPath path) {
		filteredPaths.add(Arrays.asList(path.segments()));
	}
	
	/**
	 * Remove a path from the filter
	 * @param path
	 */
	public void removePath(IPath path) {
		filteredPaths.remove(Arrays.asList(path.segments()));
	}

	/**
	 * Apply the filter to the given path
	 * @param path
	 * @return whether the path should be ignored
	 */
	public boolean shouldIgnore(IPath path) {
		if (filteredPaths.contains(Arrays.asList(path.segments()))) {
			return true;
		}
		return filterBinaries && this.isBinaryFile(project, path);
	}
	
	/**
	 * Store filter in a given memento
	 *
	 * @param memento
	 */
	public void saveFilter(IMemento memento) {
		memento.putString(ATTR_PROJECT_NAME, project.getName());
		memento.putBoolean(ATTR_FILTER_BINARIES, filterBinaries);

		for (List<String> path : filteredPaths.getItems()) {
			StringBuilder pathBuilder = new StringBuilder();
			for (String segment : path) {
				pathBuilder.append(segment);
				pathBuilder.append(IPath.SEPARATOR);
			}
			IMemento pathMemento = memento.createChild(FILE_FILTER_PATH_ELEMENT_NAME);
			pathMemento.putString(ATTR_FILE_FILTER_PATH, pathBuilder.toString());
		}
	}
	
	/**
	 * Load filter from a given memento
	 *
	 * @param memento
	 * @return the restored filter
	 */
	public static SyncFileFilter loadFilter(IMemento memento) {
		String projectName = memento.getString(ATTR_PROJECT_NAME);
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if (project == null) {
			throw new RuntimeException("Project not found for filter data: " + project); //$NON-NLS-1$
		}

		SyncFileFilter filter = createEmptyFilter(project);
		filter.setFilterBinaries(memento.getBoolean(ATTR_FILTER_BINARIES));
		
		for (IMemento pathMemento : memento.getChildren(FILE_FILTER_PATH_ELEMENT_NAME)) {
			String path = pathMemento.getString(ATTR_FILE_FILTER_PATH);
			String[] segments = path.split(Character.toString(IPath.SEPARATOR));
			filter.filteredPaths.add(Arrays.asList(segments));
		}
		
		return filter;
	}
}
