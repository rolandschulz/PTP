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
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.rdt.sync.core.BuildConfigurationManager;
import org.eclipse.ptp.rdt.sync.ui.messages.Messages;
import org.eclipse.ptp.rdt.sync.core.serviceproviders.ISyncServiceProvider;
import org.eclipse.ptp.rdt.sync.core.services.IRemoteSyncServiceConstants;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceModelManager;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class SyncMergeFileTreeViewer extends ViewPart {
	private IProject project;
	private Composite parentComposite;
	private TreeViewer fileTree;
	private final ReentrantLock treeLock = new ReentrantLock();

	public void createPartControl(Composite parent) {
		parentComposite = parent;
		synchronized(treeLock) {
			fileTree = new TreeViewer(parent);
			fileTree.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					Object selection = ((IStructuredSelection)event.getSelection()).getFirstElement();
					if (selection instanceof IFile) {
						SyncMergeEditor.open((IFile) selection);
					}
				}
			});
			fileTree.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
			fileTree.setContentProvider(new ConflictedFilesContentProvider());
			if (project != null) {
				fileTree.setInput(project);
			}
		}
	}

	public void setFocus() {
		parentComposite.setFocus();
	}
	
	/**
	 * Set the project displayed by this view. Ideally, this would be passed in on view creation, but Eclipse does not support
	 * passing of parameters to views.
	 *
	 * @param project
	 */
	public void setProject(IProject p) {
		project = p;
		synchronized(treeLock) {
			if (fileTree != null) {
				fileTree.setInput(project);
			}
		}
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
				ISyncServiceProvider provider = this.getSyncProvider((IProject) element);
				if (provider != null) {
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
		
		// Low-level access to core to get sync provider. Logs error message and returns null if unable to find a provider.
		private ISyncServiceProvider getSyncProvider(IProject project) {
			ISyncServiceProvider provider = null;
			IConfiguration config = ManagedBuildManager.getBuildInfo(project).getDefaultConfiguration();
			BuildConfigurationManager bcm = BuildConfigurationManager.getInstance();
			IServiceConfiguration serviceConfig = bcm.getConfigurationForBuildConfiguration(config);
			if (serviceConfig != null) {
				IServiceModelManager serviceModel = ServiceModelManager.getInstance();
				IService syncService = serviceModel.getService(IRemoteSyncServiceConstants.SERVICE_SYNC);
				provider = (ISyncServiceProvider) serviceConfig.getServiceProvider(syncService);
			}
			
			if (provider == null) {
				RDTSyncUIPlugin.getDefault().logErrorMessage(Messages.SyncMergeFileTreeViewer_0 + config.getName());
			}
			return provider;
		}
	}
}
