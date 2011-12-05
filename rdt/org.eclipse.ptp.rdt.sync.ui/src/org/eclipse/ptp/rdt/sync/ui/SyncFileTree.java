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

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.ptp.rdt.sync.core.PatternMatcher;
import org.eclipse.ptp.rdt.sync.core.RegexPatternMatcher;
import org.eclipse.ptp.rdt.sync.core.SyncFileFilter;
import org.eclipse.ptp.rdt.sync.core.SyncFileFilter.PatternType;
import org.eclipse.ptp.rdt.sync.core.SyncManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * File tree where users can select the files to be sync'ed
 */
public class SyncFileTree extends ApplicationWindow {
	private final IProject project;
	private final SyncFileFilter filter;
	private SyncCheckboxTreeViewer treeViewer;
	private Table patternTable;

	// A checkbox tree viewer that does not allow unchecked directories to be expanded.
	// Note: This illegally extends CheckboxTreeViewer but is the only simple way known to implement this behavior.
	// Also, the "isExpandable" method exists in the grandparent class and comments say that it may be overriden.
	private class SyncCheckboxTreeViewer extends CheckboxTreeViewer {
		public SyncCheckboxTreeViewer(Composite parent) {
			super(parent);
		}
		
		@Override
		public boolean isExpandable(Object element) {
			if (!super.isExpandable(element)) {
				return false;
			}
			
			IPath path = ((IResource) element).getProjectRelativePath();
			if (filter.shouldIgnore(path.toOSString())) {
				return false;
			} else {
				return true;
			}
		}
	}

	public SyncFileTree(IProject p) {
		super(null);
		project = p;
		filter = SyncManager.getFileFilter(project);
	}

	/**
	 * Configures the shell
	 *
	 * @param shell
	 */
	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Synchronized Files"); //$NON-NLS-1$
		shell.setSize(400, 400);
	}

	/**
	 * Creates the main window's contents
	 * 
	 * @param parent
	 *            the main window
	 * @return Control
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));

		// File tree viewer
		treeViewer = new SyncCheckboxTreeViewer(composite);
		treeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		treeViewer.setContentProvider(new SFTTreeContentProvider());
		treeViewer.setLabelProvider(new SFTTreeLabelProvider());
		treeViewer.setCheckStateProvider(new SFTCheckStateProvider());
		treeViewer.addCheckStateListener(new SFTCheckStateListener());
		treeViewer.setInput(project);
		
		// List of patterns
		patternTable = new Table(composite, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
		patternTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		TableColumn patternTableColumn = new TableColumn(patternTable, SWT.LEAD, 0);
		patternTableColumn.setWidth(100);
		Display display = Display.getCurrent();
		Color includeGreen = display.getSystemColor(SWT.COLOR_GREEN);
		Color excludeRed = display.getSystemColor(SWT.COLOR_RED);

		for (PatternMatcher pattern : filter.getPatterns()) {
			TableItem ti = new TableItem(patternTable, SWT.LEAD);
			ti.setText(pattern.toString());
			if (filter.getPatternType(pattern) == PatternType.INCLUDE) {
				ti.setForeground(includeGreen);
			} else {
				ti.setForeground(excludeRed);
			}
		}

	    Button cancelButton = new Button(composite, SWT.PUSH);
	    cancelButton.setText("    Cancel    "); //$NON-NLS-1$
	    cancelButton.setLayoutData(new GridData(SWT.RIGHT, SWT.NONE, true, false));
	    cancelButton.addSelectionListener(new SelectionAdapter() {
	      public void widgetSelected(SelectionEvent event) {
	    	  getShell().close();
	      }
	    });
	    
	    Button okButton = new Button(composite, SWT.PUSH);
	    okButton.setText("      OK      "); //$NON-NLS-1$
	    okButton.setLayoutData(new GridData(SWT.RIGHT, SWT.NONE, false, false));
	    okButton.addSelectionListener(new SelectionAdapter() {
	      public void widgetSelected(SelectionEvent event) {
	    	  SyncManager.saveFileFilter(project, filter);
	    	  getShell().close();
	      }
	    });

		return composite;
	}

	private class SFTTreeContentProvider implements ITreeContentProvider {
		/**
		 * Gets the children of the specified object
		 * 
		 * @param element
		 *            the parent object
		 * @return Object[]
		 */
		public Object[] getChildren(Object element) {
			if (element instanceof IFolder) {
				try {
					return ((IFolder) element).members();
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return new Object[0];
		}

		/**
		 * Gets the parent of the specified object
		 * 
		 * @param element
		 *            the object
		 * @return Object
		 */
		public Object getParent(Object element) {
			return ((IResource) element).getParent();
		}

		/**
		 * Returns whether the passed object has children
		 * 
		 * @param element
		 *            the parent object	private class SFTStyledCellLabelProvider extends 
		 * @return boolean
		 */
		public boolean hasChildren(Object element) {
			// Get the children
			Object[] obj = getChildren(element);

			// Return whether the parent has children
			return obj == null ? false : obj.length > 0;
		}

		/**
		 * Gets the root element(s) of the tree
		 * 
		 * @param element
		 *            the input data
		 * @return Object[]
		 */
		public Object[] getElements(Object element) {
			try {
				 return ((IProject) element).members();
			} catch (CoreException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return new Object[0];
		}

		/**
		 * Disposes any created resources
		 */
		public void dispose() {
			// Nothing to dispose
		}

		/**
		 * Called when the input changes
		 * 
		 * @param element
		 *            the viewer
		 * @param arg1
		 *            the old input
		 * @param arg2
		 *            the new input
		 */
		public void inputChanged(Viewer element, Object arg1, Object arg2) {
			// Nothing to change
		}
	}

	private class SFTTreeLabelProvider implements ILabelProvider {
		// Images for tree nodes
		private final Image folderImage = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
		private final Image fileImage = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);

		/**
		 * Gets the image to display for a node in the tree
		 * 
		 * @param element
		 *            the node
		 * @return Image
		 */
		public Image getImage(Object element) {
			if (element instanceof IFolder) {
				return folderImage;
			} else {
				return fileImage;
			}
		}

		/**
		 * Gets the text to display for a node in the tree
		 * 
		 * @param element
		 *            the node
		 * @return String
		 */
		public String getText(Object element) {
			return ((IResource) element).getName();
		}

		/**
		 * Called when this LabelProvider is being disposed
		 */
		public void dispose() {
			// Nothing to dispose
		}

		/**
		 * Returns whether changes to the specified property on the specified
		 * element would affect the label for the element
		 * 
		 * @param element
		 *            the element
		 * @param arg1
		 *            the property
		 * @return boolean
		 */
		public boolean isLabelProperty(Object element, String arg1) {
			return false;
		}

		@Override
		public void addListener(ILabelProviderListener listener) {
			// Listeners not supported
			
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
			// Listeners not supported
		}
	}
	
	// Simple check state provider - just refer to the project's file filter to see if the entry should be checked.
	private class SFTCheckStateProvider implements ICheckStateProvider {
		public boolean isChecked(Object element) {
			IPath path = ((IResource) element).getProjectRelativePath();
			if (filter.shouldIgnore(path.toOSString())) {
				return false;
			} else {
				return true;
			}
		}

		public boolean isGrayed(Object element) {
			return false;
		}
	}
	
	// Simple listener to update file filter as user checks and unchecks.
	private class SFTCheckStateListener implements ICheckStateListener {
		public void checkStateChanged(CheckStateChangedEvent event) {
			IPath path = ((IResource) (event.getElement())).getProjectRelativePath();
			if (event.getChecked()) {
				filter.removePattern(new RegexPatternMatcher(path.toOSString()));
			} else {
				filter.addPattern(new RegexPatternMatcher(path.toOSString()), PatternType.EXCLUDE);
			}
			
			treeViewer.refresh(event.getElement());
		}
	}
}