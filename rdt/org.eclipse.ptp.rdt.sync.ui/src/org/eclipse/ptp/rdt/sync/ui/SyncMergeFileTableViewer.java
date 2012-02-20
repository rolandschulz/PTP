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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.ptp.internal.rdt.sync.ui.SyncPluginImages;
import org.eclipse.ptp.rdt.sync.core.SyncManager;
import org.eclipse.ptp.rdt.sync.core.serviceproviders.ISyncServiceProvider;
import org.eclipse.ptp.rdt.sync.ui.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class SyncMergeFileTableViewer extends ViewPart {
	private Image CHECKED;
	private Image UNCHECKED;
	private IProject project;
	private TableViewer fileTableViewer;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.ViewPart#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String,
	 *      java.lang.Object)
	 */
	@Override
	public void setInitializationData(IConfigurationElement cfig, String propertyName, Object data) {
		super.setInitializationData(cfig, propertyName, data);
		CHECKED = SyncPluginImages.DESC_RESOLVED_MERGE.createImage();
		UNCHECKED = SyncPluginImages.DESC_UNRESOLVED_MERGE.createImage();
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
	}

	public void createPartControl(Composite parent) {
		synchronized(this) {
			fileTableViewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
			
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
				@Override
				public Image getImage(Object element) {
					assert(element instanceof IPath);
					if (SyncManager.getResolved(project, (IPath) element)) {
						return CHECKED;
					} else {
						return UNCHECKED;
					}
				}
				
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
				public void selectionChanged(SelectionChangedEvent event) {
					Object selection = ((IStructuredSelection)event.getSelection()).getFirstElement();
					if (selection instanceof IFile) {
						SyncMergeEditor.open((IFile) selection);
					}
				}
			});

			// Set input
			fileTableViewer.setContentProvider(ArrayContentProvider.getInstance());
			if (project != null) {
				ISyncServiceProvider provider = SyncManager.getSyncProvider(project);
				fileTableViewer.setInput(provider.getMergeConflictFiles());
			}
		}
	}

	public void setFocus() {
		fileTableViewer.getControl().setFocus();
	}
	
	/**
	 * Set the project displayed by this view. Ideally, this would be passed in on view creation, but Eclipse does not support
	 * passing of parameters to views.
	 *
	 * @param project
	 */
	public void setProject(IProject p) {
		project = p;
		synchronized(this) {
			if (fileTableViewer != null) {
				ISyncServiceProvider provider = SyncManager.getSyncProvider(project);
				fileTableViewer.setInput(provider.getMergeConflictFiles());
			}
		}
	}
}
