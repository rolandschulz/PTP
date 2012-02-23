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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.ptp.internal.rdt.sync.ui.SyncPluginImages;
import org.eclipse.ptp.rdt.sync.core.SyncFlag;
import org.eclipse.ptp.rdt.sync.core.SyncManager;
import org.eclipse.ptp.rdt.sync.core.serviceproviders.ISyncServiceProvider;
import org.eclipse.ptp.rdt.sync.ui.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class SyncMergeFileTableViewer extends ViewPart {
	private Image CHECKED;
	private Image UNCHECKED;
	private IProject project;
	private TableViewer fileTableViewer;
	private Set<IPath> mergeConflictedFiles;
	private Action syncResolveAsLocalAction;
	private ISelectionListener selectionListener;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.ViewPart#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String,
	 *      java.lang.Object)
	 */
	@Override
	public void setInitializationData(IConfigurationElement cfig, String propertyName, Object data) {
		super.setInitializationData(cfig, propertyName, data);
		CHECKED = SyncPluginImages.DESC_RESOLVED_MERGE.createImage(false);
		UNCHECKED = SyncPluginImages.DESC_UNRESOLVED_MERGE.createImage(false);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		if (CHECKED != null) {
			CHECKED.dispose();
		}
		if (UNCHECKED != null) {
			UNCHECKED.dispose();
		}
		if (selectionListener != null) {
			ISelectionService selectionService = (ISelectionService) getSite().getService(ISelectionService.class);
			selectionService.removePostSelectionListener(selectionListener);
		}
	}

	public void createPartControl(Composite parent) {
		synchronized(this) {
			fileTableViewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
			
			// Create file column
			TableViewerColumn fileColumn = new TableViewerColumn(fileTableViewer, SWT.NONE);
			fileColumn.getColumn().setText(Messages.SyncMergeFileTreeViewer_0);
			fileColumn.getColumn().setWidth(200);
			fileColumn.getColumn().setResizable(true);
			fileColumn.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					assert(element instanceof IPath);
					return ((IPath) element).toString();
				}
			});
			
			// Create resolve column
			TableViewerColumn resolveColumn = new TableViewerColumn(fileTableViewer, SWT.NONE);
			resolveColumn.getColumn().setText(Messages.SyncMergeFileTreeViewer_1);
			resolveColumn.getColumn().setWidth(20);
			resolveColumn.getColumn().setResizable(true);
			resolveColumn.setLabelProvider(new ColumnLabelProvider() {
				/*
				 * (non-Javadoc)
				 * @see org.eclipse.jface.viewers.ColumnLabelProvider#getImage(java.lang.Object)
				 */
				@Override
				public Image getImage(Object element) {
					assert(element instanceof IPath);
					if (SyncManager.getResolved(project, (IPath) element)) {
						return CHECKED;
					} else {
						return UNCHECKED;
					}
				}
				
				/*
				 * (non-Javadoc)
				 * @see org.eclipse.jface.viewers.ColumnLabelProvider#getText(java.lang.Object)
				 */
				@Override
				public String getText(Object element) {
					assert(element instanceof IPath);
					// Return appropriate text only if images are unavailable
					if (SyncManager.getResolved(project, (IPath) element)) {
						if (CHECKED == null) {
							return "Yes"; //$NON-NLS-1$
						} else {
							return null;
						}
					} else {
						if (UNCHECKED == null) {
							return "No"; //$NON-NLS-1$
						} else {
							return null;
						}
					}
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
			
			// On selection, open the compare editor for the selected file
			fileTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					Object selection = ((IStructuredSelection)event.getSelection()).getFirstElement();
					assert(selection instanceof IPath);
					SyncMergeEditor.open(project.getFile((IPath) selection));
				}
			});
			
			// Add sync button to toolbar
			syncResolveAsLocalAction = new Action(Messages.SyncMergeFileTableViewer_0) {
				public void run() {
					try {
						SyncManager.syncResolveAsLocal(null, project, SyncFlag.FORCE, null);
					} catch (CoreException e) {
						// This should never happen because only a blocking sync can throw a core exception.
						RDTSyncUIPlugin.getDefault().logErrorMessage(Messages.SyncMergeFileTableViewer_1);
					}
				}
			};
			getViewSite().getActionBars().getToolBarManager().add(syncResolveAsLocalAction);

			// Allow user to toggle whether file is resolved
			fileTableViewer.addDoubleClickListener(new IDoubleClickListener() {
				@Override
				public void doubleClick(DoubleClickEvent event) {
					Object selection = ((IStructuredSelection) event.getSelection()).getFirstElement();
					assert(selection instanceof IPath);
					SyncManager.setResolved(project, (IPath) selection, !(SyncManager.getResolved(project, (IPath) selection)));
					if (SyncManager.getResolved(project, mergeConflictedFiles)) {
						syncResolveAsLocalAction.setEnabled(true);
					} else {
						syncResolveAsLocalAction.setEnabled(false);
					}
					fileTableViewer.refresh();
				}
			});

			// Set contents
			fileTableViewer.setContentProvider(ArrayContentProvider.getInstance());
			this.update(true);
			
			// Listen for selection changes
			ISelectionService selectionService = (ISelectionService) getSite().getService(ISelectionService.class);
			selectionListener = new ISelectionListener() {
				@Override
				public void selectionChanged(IWorkbenchPart part, ISelection selection) {
					update(false);
				}
			};
			selectionService.addPostSelectionListener(selectionListener);
		}
	}
	
	private void update(boolean updateCurrentProject) {
		IProject newProject = this.getProject();
		// Never go from displaying a project to displaying no project
		if (newProject == null && project != null) {
			return;
		}

		// Return if we are told not to update the current project, and there is no project change
		if (!updateCurrentProject && newProject == project) {
			return;
		}
		project = newProject;
		
		if (project != null) {
			ISyncServiceProvider provider = SyncManager.getSyncProvider(project);
			mergeConflictedFiles = provider.getMergeConflictFiles();
		} else {
			mergeConflictedFiles = new HashSet<IPath>();
		}
		fileTableViewer.setInput(mergeConflictedFiles);
		if (project != null && SyncManager.getResolved(project, mergeConflictedFiles)) {
			syncResolveAsLocalAction.setEnabled(true);
		} else {
			syncResolveAsLocalAction.setEnabled(false);
		}
		fileTableViewer.refresh();
	}

	public void setFocus() {
		fileTableViewer.getControl().setFocus();
	}

	/*
	 * Portions copied from org.eclipse.ptp.services.ui.wizards.setDefaultFromSelection
	 */
	private IProject getProject() {
		IStructuredSelection selection = this.getSelectedElements();
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
	
	private IStructuredSelection getSelectedElements() {
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
