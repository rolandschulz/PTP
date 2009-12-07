/*******************************************************************************
 * Copyright (c) 2006, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Ed Swartz (Nokia)
 *    IBM Corporation
 *******************************************************************************/

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.ui/src
 * Class: org.eclipse.cdt.internal.ui.search.PDOMSearchTreeContentProvider
 * Version: 1.11
 */

package org.eclipse.ptp.internal.rdt.ui.search;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.cdt.internal.ui.search.IPDOMSearchContentProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

public class RemoteSearchTreeContentProvider implements ITreeContentProvider, IPDOMSearchContentProvider {

	private TreeViewer viewer;
	private RemoteSearchResult result;
	private Map<Object, Set<Object>> tree = new HashMap<Object, Set<Object>>();

	public Object[] getChildren(Object parentElement) {
		Set children = tree.get(parentElement);
		if (children == null)
			return new Object[0];
		
		return children.toArray();
	}

	public Object getParent(Object element) {
		Iterator p = tree.keySet().iterator();
		while (p.hasNext()) {
			Object parent = p.next();
			Set children = tree.get(parent);
			if (children.contains(element))
				return parent;
		}
		return null;
	}

 	public boolean hasChildren(Object element) {
 		return tree.get(element) != null;
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = (TreeViewer)viewer;
		this.result = (RemoteSearchResult) newInput;
		initialize(result);
		viewer.refresh();
	}

	/**
	 * Add a message to a project node indicating it has no results because indexer is disabled.
	 * @param project
	 */
	private void insertUnindexedProjectWarningElement(ICProject project) {
		insertCElement(project);
		insertChild(project, 
				new Status(IStatus.WARNING, CUIPlugin.PLUGIN_ID,
						CSearchMessages.PDOMSearchTreeContentProvider_IndexerNotEnabledWarning));
	}

	/**
	 * Add a message to a project node indicating it has no results because project is closed
	 * @param project
	 */
	private void insertClosedProjectWarningElement(ICProject project) {
		insertCElement(project);
		insertChild(project, 
				new Status(IStatus.WARNING, CUIPlugin.PLUGIN_ID,
						CSearchMessages.PDOMSearchTreeContentProvider_ProjectClosedWarning));
	}
	
	private boolean insertChild(Object parent, Object child) {
		Set<Object> children = tree.get(parent);
		if (children == null) {
			children = new HashSet<Object>();
			tree.put(parent, children);
		}
		return children.add(child);
	}
	
	private void insertSearchElement(RemoteSearchElement element) {
		IIndexFileLocation location= element.getLocation();
		IFile[] files;
		if(location.getFullPath()!=null) {
			files= new IFile[] {ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(location.getFullPath()))};
		} else {
			IPath path= IndexLocationFactory.getAbsolutePath(element.getLocation());
			
			if(path != null)
				files= ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(path);
			else
				files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(location.getURI());
		}
		boolean handled= false;
		if (files.length > 0) {
			for (int j = 0; j < files.length; ++j) {
				ICElement celement = CoreModel.getDefault().create(files[j]);
				if (celement != null) {
					insertChild(celement, element);
					insertCElement(celement);
					handled= true;
				}
			}
		} 
		if (!handled) {
			// insert a folder and then the file under that
			//IPath path = IndexLocationFactory.getAbsolutePath(location);
			IPath path = getAbsolutePath(location);
			
			if (path != null) {
				IPath directory = path.removeLastSegments(1);
				insertChild(location, element);
				insertChild(directory, location);
				insertChild(result, directory);
			} else {
				// URI not representable as a file
				insertChild(IPDOMSearchContentProvider.URI_CONTAINER, location.getURI());
				insertChild(result, IPDOMSearchContentProvider.URI_CONTAINER);
			}
		}
	}
	
	
	/**
	 * Copied from IndexLocationFactory.getAbsolutePath()
	 */
	static IPath getAbsolutePath(IIndexFileLocation location) {
		IPath path = URIUtil.toPath(location.getURI());
		// Workaround for platform bug http://bugs.eclipse.org/bugs/show_bug.cgi?id=261457
		if (path == null) {
			try {
				IFileStore store = EFS.getStore(location.getURI());
				if (store == null)
					return null;
				File file = store.toLocalFile(EFS.NONE, null);
				if (file == null) 
					return new Path(location.getURI().getPath()); // better than returning null
				path = new Path(file.getAbsolutePath());
			} catch (CoreException e) {
			}
		}
		return path;
	}
	
	
	private void insertCElement(ICElement element) {
		if (element instanceof ICProject)
			insertChild(result, element);
		else {
			ICElement parent = element.getParent();
			if (parent instanceof ISourceRoot && parent.getUnderlyingResource() instanceof IProject)
				// Skip source roots that are projects
				parent = parent.getParent();
			insertChild(parent, element);
			insertCElement(parent);
		}
	}
	
	public void elementsChanged(Object[] elements) {
		if (elements != null) {
			for (int i = 0; i < elements.length; ++i) {
				RemoteSearchElement element = (RemoteSearchElement)elements[i];
				if (result.getMatchCount(element) > 0) {
					insertSearchElement(element);
				} else {
					boolean remove = true;
					if (element instanceof ICProject) {
						ICProject cProject = (ICProject) element;
						remove = !addProjectWarningIfApplicable(cProject);
					}
					if (remove) {
						remove(element);
					}
				}
			}
		}
		if (!viewer.getTree().isDisposed()) {
			viewer.refresh();
		}
	}

	private boolean addProjectWarningIfApplicable(ICProject cProject) {
		if (cProject.getProject().isOpen()) {
			if (!CCorePlugin.getIndexManager().isProjectIndexed(cProject)) {
				insertUnindexedProjectWarningElement(cProject);
				return true;
			}
		} else {
			insertClosedProjectWarningElement(cProject);
			return true;
		}
		return false;
	}
	
	public void clear() {
		initialize(result);
	}
	
	private void initialize(final RemoteSearchResult result) {
		this.result = result;
		tree.clear();
		if (result != null) {
			// if indexer was busy, record that
			if (result.wasIndexerBusy()) {
				insertChild(result, IPDOMSearchContentProvider.INCOMPLETE_RESULTS_NODE); 
			}
			
			Object[] elements = result.getElements();
			for (int i = 0; i < elements.length; ++i) {
				insertSearchElement((RemoteSearchElement)elements[i]);
			}

			// add all the projects which have no results
			ICProject[] projects = ((RemoteSearchQueryAdapter)result.getQuery()).getProjects();
			for (int i = 0; i < projects.length; ++i) {
				ICProject project = projects[i];
				Object projectResults = tree.get(project);
				if (projectResults == null) {
					addProjectWarningIfApplicable(project);
				}
			}
		}
	}
	
	protected void remove(Object element) {
		Object parent = getParent(element);
		if (parent == null)
			// reached the search result
			return;
		
		Set siblings = tree.get(parent);
		siblings.remove(element);
		
		if (siblings.isEmpty()) {
			// remove the parent
			remove(parent);
			tree.remove(parent);
		}
	}
}
