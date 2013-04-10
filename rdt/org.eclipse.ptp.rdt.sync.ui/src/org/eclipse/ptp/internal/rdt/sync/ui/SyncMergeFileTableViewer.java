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
package org.eclipse.ptp.internal.rdt.sync.ui;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.ptp.internal.rdt.sync.ui.messages.Messages;
import org.eclipse.ptp.rdt.sync.core.BuildConfigurationManager;
import org.eclipse.ptp.rdt.sync.core.BuildScenario;
import org.eclipse.ptp.rdt.sync.core.ISyncListener;
import org.eclipse.ptp.rdt.sync.core.SyncEvent;
import org.eclipse.ptp.rdt.sync.core.SyncManager;
import org.eclipse.ptp.rdt.sync.core.resources.RemoteSyncNature;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class SyncMergeFileTableViewer extends ViewPart {
	private IProject project;
	private TableViewer fileTableViewer;
	private ISelectionListener selectionListener;
	private ISyncListener syncListener;
	private static SyncMergeFileTableViewer activeViewerInstance = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		if (selectionListener != null) {
			ISelectionService selectionService = (ISelectionService) getSite().getService(ISelectionService.class);
			selectionService.removePostSelectionListener(selectionListener);
		}
		if (project != null) {
			SyncManager.removePostSyncListener(project, syncListener);
		}
	}

	/**
	 * Return the currently active instance of this view. The purpose of this function is to allow communication with the viewer
	 * from external classes, such as for updating the viewer. Changes may be needed to the implementation if we ever decide to
	 * actually allow more than one instance.
	 * Note that this function does not create the viewer instance, unlike "getInstance()" in the common Java singleton pattern.
	 * 
	 * @return instance - may be null if viewer not instantiated.
	 */
	public static SyncMergeFileTableViewer getActiveInstance() {
		return activeViewerInstance;
	}

	@Override
	public void createPartControl(final Composite parent) {
		synchronized (this) {
			activeViewerInstance = this;
			fileTableViewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.MULTI);

			// Create file column
			TableViewerColumn fileColumn = new TableViewerColumn(fileTableViewer, SWT.NONE);
			fileColumn.getColumn().setText(Messages.SyncMergeFileTreeViewer_0);
			fileColumn.getColumn().setWidth(200);
			fileColumn.getColumn().setResizable(true);
			fileColumn.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					assert (element instanceof IFile);
					return ((IFile) element).getProjectRelativePath().toOSString();
				}
			});

			// Layout the viewer
			GridData gridData = new GridData();
			gridData.verticalAlignment = GridData.FILL;
			gridData.horizontalSpan = 2;
			gridData.grabExcessHorizontalSpace = true;
			gridData.grabExcessVerticalSpace = true;
			gridData.horizontalAlignment = GridData.FILL;
			fileTableViewer.getControl().setLayoutData(gridData);

			fileTableViewer.getTable().setHeaderVisible(true);
			fileTableViewer.getTable().setLinesVisible(true);

			// Open merge editor for file on double-click
			fileTableViewer.addDoubleClickListener(new IDoubleClickListener() {
				@Override
				public void doubleClick(DoubleClickEvent event) {
					Object selection = ((IStructuredSelection) event.getSelection()).getFirstElement();
					if (selection != null) {
						assert (selection instanceof IFile);
						SyncMergeEditor.open((IFile) selection);
					}
				}
			});

			// Listen for selection changes
			ISelectionService selectionService = (ISelectionService) getSite().getService(ISelectionService.class);
			selectionListener = new ISelectionListener() {
				@Override
				public void selectionChanged(IWorkbenchPart part, ISelection selection) {
					IProject selectedProject = getProject();
					if (selectedProject != null && selectedProject != project) {
						SyncMergeFileTableViewer.this.update(selectedProject);
					}
				}
			};
			selectionService.addPostSelectionListener(selectionListener);

			// Create a sync listener, which will be registered with the correct project
			syncListener = new ISyncListener() {
				@Override
				public void handleSyncEvent(SyncEvent event) {
					RDTSyncUIPlugin.getStandardDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							SyncMergeFileTableViewer.this.update(null);
						}
					});
				}
			};

			// Create a menu manager for the context menu
			MenuManager menuManager = new MenuManager();
			Menu menu = menuManager.createContextMenu(fileTableViewer.getTable());
			fileTableViewer.getTable().setMenu(menu);
			getSite().registerContextMenu(menuManager, fileTableViewer);
			getSite().setSelectionProvider(fileTableViewer);

			// Set contents
			fileTableViewer.setContentProvider(ArrayContentProvider.getInstance());
			this.update(getProject());
		}
	}

	// Update viewer and also switch to the passed project if it is not null
	public void update(IProject newProject) {
		// Switch projects if needed
		if ((newProject != null) && RemoteSyncNature.hasNature(newProject)) {
			if (project != null) {
				SyncManager.removePostSyncListener(project, syncListener);
			}
			SyncManager.addPostSyncListener(newProject, syncListener);
			project = newProject;
		}

		// Get merge-conflicted files
		Set<IPath> mergeConflictFiles = new HashSet<IPath>();
		if (project != null) {
			BuildConfigurationManager bcm = BuildConfigurationManager.getInstance();
			try {
				BuildScenario buildScenario = bcm.getActiveBuildScenario(project);
				mergeConflictFiles = bcm.getMergeConflictFiles(project, buildScenario);
			} catch (CoreException e) {
				RDTSyncUIPlugin.log(e);
			}
		}

		// Add to viewer as set of IFiles
		Set<IFile> fileSet = new HashSet<IFile>();
		for (IPath path : mergeConflictFiles) {
			fileSet.add(project.getFile(path));
		}
		fileTableViewer.setInput(fileSet);
		fileTableViewer.refresh();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		fileTableViewer.getControl().setFocus();
	}

	/*
	 * Portions copied from org.eclipse.ptp.services.ui.wizards.setDefaultFromSelection
	 */
	private static IProject getProject() {
		IStructuredSelection selection = getSelectedElements();
		if (selection == null) {
			return null;
		}

		Object firstElement = selection.getFirstElement();
		if (!(firstElement instanceof IAdaptable)) {
			return null;
		}
		Object o = ((IAdaptable) firstElement).getAdapter(IResource.class);
		if (o == null) {
			return null;
		}
		IResource resource = (IResource) o;

		return resource.getProject();
	}

	private static IStructuredSelection getSelectedElements() {
		IWorkbenchWindow wnd = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage pg = wnd.getActivePage();
		ISelection sel = pg.getSelection();

		if (!(sel instanceof IStructuredSelection)) {
			return null;
		} else {
			return (IStructuredSelection) sel;
		}
	}
}
