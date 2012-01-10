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
package org.eclipse.ptp.rdt.sync.ui;

import java.util.ArrayList;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.rdt.sync.core.BuildConfigurationManager;
import org.eclipse.ptp.rdt.sync.core.serviceproviders.ISyncServiceProvider;

public class SyncMergeFileTreeViewer {
	public static void open(IProject project) {
		TreeViewer tv = new TreeViewer(null);
		tv.setContentProvider(new ConflictedFilesContentProvider());
		tv.setInput(project);
	}
	
	// A boilerplate content provider for project files, except that it filters files that are not merge-conflicted.
	private static class ConflictedFilesContentProvider implements ITreeContentProvider {
		private Set<IPath> conflictedFiles;

		public void dispose() {
			// Nothing to dispose
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// Nothing to do
		}

		public Object[] getElements(Object element) {
			ArrayList<IResource> children = new ArrayList<IResource>();

			if (element instanceof IProject && ((IProject) element).isAccessible()) {
				ISyncServiceProvider provider = BuildConfigurationManager.getInstance().getProjectSyncProvider((IProject) element);
				conflictedFiles = provider.getMergeConflictFiles();
				
				try {
					for (IResource localChild : ((IProject) element).members()) {
						if (shouldBeIncluded(localChild)) {
							children.add(localChild);
						}
					}
				} catch (CoreException e) {
					assert(false); // This should never happen, since we check for existence before accessing the project.
				}
			}

			return children.toArray();
		}

		public Object[] getChildren(Object element) {
			ArrayList<IResource> children = new ArrayList<IResource>();

			if (element instanceof IFolder) {
				if (((IFolder) element).isAccessible()) {
					try {
						for (IResource localChild : ((IFolder) element).members()) {
							if (shouldBeIncluded(localChild)) {
								children.add(localChild);
							}
						}
					} catch (CoreException e) {
						assert(false); // This should never happen, since we check for existence before accessing the folder.
					}
				}
			}

            return children.toArray();
		}
		
		public Object getParent(Object element) {
			return ((IResource) element).getParent();
		}

		public boolean hasChildren(Object element) {
            Object[] obj = getChildren(element);
            return obj == null ? false : obj.length > 0;
		}

		// Filter files that are okay (not merge-conflicted)
		private boolean shouldBeIncluded(IResource res) {
			if (res instanceof IFile && !conflictedFiles.contains(res.getProjectRelativePath())) {
				return false;
			} else {
				return true;
			}
		}
	}
}
