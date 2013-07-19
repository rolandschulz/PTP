/*******************************************************************************
 * Copyright (c) 2011,2013 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.sync.ui.preferences;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.internal.rdt.sync.ui.RDTSyncUIPlugin;
import org.eclipse.ptp.internal.rdt.sync.ui.messages.Messages;
import org.eclipse.ptp.rdt.sync.core.AbstractSyncFileFilter;
import org.eclipse.ptp.rdt.sync.core.SyncConfig;
import org.eclipse.ptp.rdt.sync.core.SyncConfigManager;
import org.eclipse.ptp.rdt.sync.core.SyncManager;
import org.eclipse.ptp.rdt.sync.core.exceptions.MissingConnectionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * Dialog to display a File tree and other options, where users can select the patterns/files to be sync'ed.<br>
 * This class is used for two scenarios:<br>
 * 1. Filter page for new sync project - to specify settings for new project<br>
 * 2. Filter page for existing project - to alter existing Sync Filter settings<br>
 * 
 * Uses ResourceMatchers (e.g. PathResourceMatcher, RegexResourceMatcher, and WildcardResourceMatcher) to match the
 * "patterns" entered to actual files in the project.
 */
public class SyncFileFilterDialog extends Dialog implements ISyncFilterWidgetPatternChangeListener {
	private static final Display display = Display.getCurrent();

	private final IProject project;
	private final AbstractSyncFileFilter filter;
	private CheckboxTreeViewer treeViewer;
	private Button showRemoteButton;
	private Label remoteErrorLabel;
	private SyncFilterWidget filterWidget;

	/** Boolean to help tell which boolean arguments do what */
	private static final boolean GRAB_EXCESS = true;
	/** Boolean to turn on funky colors to debug which composites contain what */
	private static final boolean DEBUG = false;

	/**
	 * Constructor for a new filter dialog. Behavior of the page varies based on whether arguments are null and whether
	 * targetFilter is set. Specifically, whether the file view is shown, how or if the filter is saved, and if preference page
	 * functionality is available. See comments for the default constructor and static open methods for details.
	 * 
	 * @param p
	 *            project
	 * @param targetFilter
	 */
	private SyncFileFilterDialog(Shell parent, IProject p, AbstractSyncFileFilter targetFilter) {
		super(parent);
		setShellStyle(SWT.RESIZE | getShellStyle());

		project = p;

		if (targetFilter == null) {
			if (project == null) {
				filter = SyncManager.getDefaultFileFilter();
			} else {
				filter = SyncManager.getFileFilter(project).clone();
			}
		} else {
			filter = targetFilter;
		}

		setReturnCode(CANCEL);
	}

	/**
	 * Open a dialog to change the filter of the passed project.
	 * 
	 * @param parent
	 *            the parent shell
	 * @param project
	 *            existing project
	 * @return open return code
	 */
	public static int open(Shell parent, IProject project) {
		return new SyncFileFilterDialog(parent, project, null).open();
	}

	/**
	 * Open a dialog to change the passed filter. This is useful for the new project wizard and other places where
	 * the filter does not yet have a context. This method blocks, as it assumes the client wants to wait for the filter changes.
	 * 
	 * The client most likely should pass a copy of the filter to be altered, and then check the return code for OK or Cancel
	 * to decide if the copy should be kept.
	 * 
	 * @param parent
	 *            the parent shell
	 * @param filter
	 *            a sync file filter that will be modified
	 * @return open return code
	 */
	public static int openBlocking(Shell parent, AbstractSyncFileFilter filter) {
		SyncFileFilterDialog page = new SyncFileFilterDialog(parent, null, filter);
		page.setBlockOnOpen(true);
		return page.open();
	}

	/**
	 * Configures the shell (sets window title)
	 * 
	 * @param shell
	 */
	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(Messages.NewSyncFileFilterPage_Title);
	}

	/**
	 * Creates the main window's contents
	 * 
	 * @param parent
	 *            the main window
	 * @return Control
	 */

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite main = (Composite) super.createDialogArea(parent);

		Composite composite = new Composite(main, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.widthHint = 500;
		composite.setLayoutData(gd);

		// To edit existing filters for a project, create the tree viewer
		if (project != null) {
			// Composite for tree viewer
			Composite treeViewerComposite = new Composite(composite, SWT.NONE);
			GridLayout treeLayout = new GridLayout(2, false);
			treeViewerComposite.setLayout(treeLayout);

			gd = new GridData(SWT.FILL, SWT.FILL, GRAB_EXCESS, GRAB_EXCESS);
			if (DEBUG) {
				colorComposite(treeViewerComposite, SWT.COLOR_RED);
			}

			treeViewerComposite.setLayoutData(gd);

			// Label for file tree viewer
			Label treeViewerLabel = new Label(treeViewerComposite, SWT.NONE);
			treeViewerLabel.setText(Messages.NewSyncFileFilterPage_Select_files_to_be_included);
			gd = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
			treeViewerLabel.setLayoutData(gd);

			// File tree viewer
			treeViewer = new CheckboxTreeViewer(treeViewerComposite);
			gd = new GridData(SWT.FILL, SWT.FILL, GRAB_EXCESS, GRAB_EXCESS, 2, 1);
			gd.heightHint = 200;
			treeViewer.getTree().setLayoutData(gd);
			treeViewer.setContentProvider(new SFTTreeContentProvider());
			treeViewer.setLabelProvider(new SFTTreeLabelProvider());
			treeViewer.setCheckStateProvider(new ICheckStateProvider() {
				@Override
				public boolean isChecked(Object element) {
					if (filter.shouldIgnore((IResource) element)) {
						return false;
					} else {
						return true;
					}
				}

				@Override
				public boolean isGrayed(Object element) {
					return false;
				}
			});
			treeViewer.addCheckStateListener(new ICheckStateListener() {
				@Override
				public void checkStateChanged(CheckStateChangedEvent event) {
					filter.addPattern((IResource) event.getElement(), !event.getChecked());
					update();
				}
			});
			treeViewer.setInput(project);

			showRemoteButton = new Button(treeViewerComposite, SWT.CHECK);
			showRemoteButton.setText(Messages.NewSyncFileFilterPage_Show_remote_files);
			showRemoteButton.setSelection(((SFTTreeContentProvider) treeViewer.getContentProvider()).getShowRemoteFiles());
			showRemoteButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					update();
				}
			});

			remoteErrorLabel = new Label(treeViewerComposite, SWT.CENTER);
			remoteErrorLabel.setForeground(display.getSystemColor(SWT.COLOR_DARK_RED));
			gd = new GridData(SWT.FILL, SWT.CENTER, GRAB_EXCESS, false);
			remoteErrorLabel.setLayoutData(gd);
			if (DEBUG) {
				remoteErrorLabel.setText("remote error label here"); //$NON-NLS-1$
			}
		}

		filterWidget = new SyncFilterWidget(composite, SWT.NONE);
		filterWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		filterWidget.setFilter(filter);
		filterWidget.addNewPatternChangeListener(this);

		update();
		return composite;
	}

	@Override
	protected void okPressed() {
		// Bug 407601 project is null during new project creation
		if (project != null) {
			try {
				SyncManager.saveFileFilter(project, filter);
			} catch (CoreException e) {
				RDTSyncUIPlugin.log(e);
			}
		}
		setReturnCode(OK);
		close();
	}

	private void update() {
		if (project != null) {
			boolean showRemote = showRemoteButton.getSelection();
			if (showRemote) {
				if (!((SFTTreeContentProvider) treeViewer.getContentProvider()).isConnected()) {
					showRemote = false;
					remoteErrorLabel.setText(Messages.NewSyncFileFilterPage_Remote_is_disconnected);
				} else {
					remoteErrorLabel.setText(""); //$NON-NLS-1$
				}
			}
			showRemoteButton.setSelection(showRemote);
			((SFTTreeContentProvider) treeViewer.getContentProvider()).setShowRemoteFiles(showRemote);
			treeViewer.refresh();
			filterWidget.update();
		}
	}

	private class SFTTreeContentProvider implements ITreeContentProvider {
		private final RemoteContentProvider remoteFiles;
		private boolean showRemoteFiles = false;

		public SFTTreeContentProvider() {
			SyncConfig config = SyncConfigManager.getActive(project);
			if (config == null) {
				// System error handled by BuildConfigurationManager
				remoteFiles = null;
			} else {
				RemoteContentProvider tmpRCP;
				try {
					tmpRCP = new RemoteContentProvider(config.getRemoteConnection(), new Path(config.getLocation(project)), project);
				} catch (MissingConnectionException e) {
					tmpRCP = null;
				}
				remoteFiles = tmpRCP;
			}
		}

		/**
		 * Get whether remote files are displayed
		 */
		public boolean getShowRemoteFiles() {
			return showRemoteFiles;
		}

		/**
		 * Set displaying of remote files
		 * 
		 * @param b
		 */
		public void setShowRemoteFiles(boolean b) {
			showRemoteFiles = b;
		}

		/**
		 * Gets the children of the specified object
		 * 
		 * @param element
		 *            the parent object
		 * @return Object[]
		 */
		@Override
		public Object[] getChildren(Object element) {
			ArrayList<IResource> children = new ArrayList<IResource>();

			if (element instanceof IFolder && !filter.shouldIgnore((IResource) element)) {
				if (((IFolder) element).isAccessible()) {
					try {
						for (IResource localChild : ((IFolder) element).members()) {
							children.add(localChild);
						}
					} catch (CoreException e) {
						assert (false); // This should never happen, since we check for existence before accessing the folder.
					}
				}

				if (showRemoteFiles && remoteFiles != null) {
					for (Object remoteChild : remoteFiles.getChildren(element)) {
						this.addUniqueResource(children, (IResource) remoteChild);
					}
				}
			}

			return children.toArray();
		}

		/**
		 * Gets the parent of the specified object
		 * 
		 * @param element
		 *            the object
		 * @return Object
		 */
		@Override
		public Object getParent(Object element) {
			return ((IResource) element).getParent();
		}

		/**
		 * Returns whether the passed object has children
		 * 
		 * @param element
		 *            the parent object private class SFTStyledCellLabelProvider extends
		 * @return boolean
		 */
		@Override
		public boolean hasChildren(Object element) {
			// Get the children
			Object[] obj = getChildren(element);

			// Return whether the parent has children
			return obj == null ? false : obj.length > 0;
		}

		/**
		 * Gets the root element(s) of the tree
		 * This code is very similar to "getChildren" but the root of the project tree requires special handling (no IFolder
		 * for the root).
		 * 
		 * @param element
		 *            the input data
		 * @return Object[]
		 */
		@Override
		public Object[] getElements(Object element) {
			ArrayList<IResource> children = new ArrayList<IResource>();

			if (element instanceof IProject && ((IProject) element).isAccessible()) {
				try {
					for (IResource localChild : ((IProject) element).members()) {
						children.add(localChild);
					}
				} catch (CoreException e) {
					assert (false); // This should never happen, since we check for existence before accessing the project.
				}

				if (showRemoteFiles && remoteFiles != null) {
					for (Object remoteChild : remoteFiles.getElements(element)) {
						this.addUniqueResource(children, (IResource) remoteChild);
					}
				}
			}

			return children.toArray();
		}

		/**
		 * Disposes any created resources
		 */
		@Override
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
		@Override
		public void inputChanged(Viewer element, Object arg1, Object arg2) {
			// Nothing to do
		}

		// Utility function to add resources to a list only if it is unique.
		// Returns whether the resource was added.
		private boolean addUniqueResource(Collection<IResource> resList, IResource newRes) {
			for (IResource res : resList) {
				if (res.getProjectRelativePath().equals(newRes.getProjectRelativePath())) {
					return false;
				}
			}

			resList.add(newRes);
			return true;
		}

		/**
		 * Check that connection is still open - useful for client to inform user when the connection goes down.
		 * 
		 * @return whether connection is still open
		 */
		public boolean isConnected() {
			if (remoteFiles == null) {
				return false;
			} else {
				return remoteFiles.isOpen();
			}
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
		@Override
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
		@Override
		public String getText(Object element) {
			return ((IResource) element).getName();
		}

		/**
		 * Called when this LabelProvider is being disposed
		 */
		@Override
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
		@Override
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

	private void colorComposite(Composite comp, int color) {
		if (DEBUG) {
			// color e.g. SWT.COLOR_RED
			org.eclipse.swt.graphics.Color gcolor = Display.getCurrent().getSystemColor(color);
			comp.setBackground(gcolor);
		}

	}

	/**
	 * Updates tree whenever patterns change in the sync filter widget
	 */
	@Override
	public void patternChanged() {
		if (treeViewer != null) {
			treeViewer.refresh();
		}
	}

	/**
	 * Intercept close events to remove the dialog as a pattern-change listener.
	 */
	@Override
	public boolean close() {
		filterWidget.removePatternChangeListener(this);
		return super.close();
	}
}