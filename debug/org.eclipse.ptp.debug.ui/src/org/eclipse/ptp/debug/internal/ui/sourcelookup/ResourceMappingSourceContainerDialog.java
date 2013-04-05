/*******************************************************************************
 * Copyright (c) 2003, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.internal.ui.sourcelookup;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.debug.ui.messages.Messages;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.ptp.remote.ui.IRemoteUIFileManager;
import org.eclipse.ptp.remote.ui.IRemoteUIServices;
import org.eclipse.ptp.remote.ui.RemoteUIServices;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.views.navigator.ResourceComparator;

/**
 * The dialog for selecting the folder and path for which a mapping source
 * container will be created.
 * 
 * @since 4.0
 */
public class ResourceMappingSourceContainerDialog extends ElementTreeSelectionDialog {

	public static String EMPTY_STRING = ""; //$NON-NLS-1$

	private Text fPathText;

	private String fPath = EMPTY_STRING;
	private IContainer fContainer = null;
	private final IRemoteConnection fRemoteConnection;

	/**
	 * Sets the dialog values for its construction
	 * 
	 * @param parent
	 *            the parent of the dialog
	 * @param labelProvider
	 *            the label provider for the content of the tree in the dialog
	 * @param contentProvider
	 *            the provider of the tree content for the dialog
	 */
	public ResourceMappingSourceContainerDialog(Shell parent, ILabelProvider labelProvider, ITreeContentProvider contentProvider,
			IRemoteConnection conn) {
		super(parent, labelProvider, contentProvider);
		fRemoteConnection = conn;
		setTitle(Messages.ResourceMappingSourceContainerDialog_0);
		setInput(ResourcesPlugin.getWorkspace().getRoot());
		setComparator(new ResourceComparator(ResourceComparator.NAME));
		ISelectionStatusValidator validator = new ISelectionStatusValidator() {
			public IStatus validate(Object[] selection) {
				for (int i = 0; i < selection.length; i++) {
					if (!(selection[i] instanceof IFolder) && !(selection[i] instanceof IProject)) {
						return new Status(IStatus.ERROR, PTPDebugUIPlugin.PLUGIN_ID, -1,
								Messages.ResourceMappingSourceContainerDialog_1, null);
					}
				}
				if (selection.length == 0) {
					return new Status(IStatus.ERROR, PTPDebugUIPlugin.PLUGIN_ID, -1,
							Messages.ResourceMappingSourceContainerDialog_2, null);
				}
				fContainer = (IContainer) selection[0];
				if (fPathText.getText().equals(EMPTY_STRING)) {
					return new Status(IStatus.ERROR, PTPDebugUIPlugin.PLUGIN_ID, -1,
							Messages.ResourceMappingSourceContainerDialog_3, null);
				}
				return new Status(IStatus.OK, PTPDebugUIPlugin.PLUGIN_ID, 0, EMPTY_STRING, null);
			}
		};
		setValidator(validator);
		setDoubleClickSelects(true);
		setAllowMultiple(false);
		setMessage(Messages.ResourceMappingSourceContainerDialog_4);
		// PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
		// IDebugHelpContextIds.ADD_FOLDER_CONTAINER_DIALOG);
		addFilter(new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (!(element instanceof IFolder)) {
					if (element instanceof IProject) {
						return ((IProject) element).isAccessible();
					}
					return false;
				}
				return true;
			}
		});
	}

	public IContainer getContainer() {
		return fContainer;
	}

	public IPath getPath() {
		return new Path(fPath);
	}

	/**
	 * Browse for a directory.
	 * 
	 * @return path to directory selected in browser
	 */
	private String browseDirectory() {
		if (fContainer != null) {
			IRemoteUIServices remoteUISrv = getRemoteUIServices(fContainer.getProject());
			if (remoteUISrv != null) {
				IRemoteUIFileManager fileManager = remoteUISrv.getUIFileManager();
				if (fileManager != null) {
					IRemoteConnection conn = getRemoteConnection(fContainer.getProject());
					if (conn != null) {
						fileManager.setConnection(conn);
					} else {
						fileManager.showConnections(true);
					}
					return fileManager.browseDirectory(getShell(), Messages.ResourceMappingSourceContainerDialog_5,
							fPathText.getText(), 0);
				}
			} else {
				DirectoryDialog dialog = new DirectoryDialog(getShell());
				dialog.setText(Messages.ResourceMappingSourceContainerDialog_5);
				dialog.setFilterPath(fPathText.getText());
				return dialog.open();
			}
		}
		return null;
	}

	/**
	 * Get the remote connection used for this project. Will open the connection
	 * if it is closed.
	 * 
	 * @return IRemoteConnection
	 */
	private IRemoteConnection getRemoteConnection(IProject project) {
		if (!fRemoteConnection.isOpen()) {
			IRemoteUIServices uiServices = RemoteUIServices.getRemoteUIServices(fRemoteConnection.getRemoteServices());
			if (uiServices != null) {
				IRemoteUIConnectionManager connUIMgr = uiServices.getUIConnectionManager();
				if (connUIMgr != null) {
					connUIMgr.openConnectionWithProgress(getShell(), null, fRemoteConnection);
				}
			}
		}
		return fRemoteConnection;
	}

	/**
	 * Look up remote UI services for a project
	 * 
	 * @return IRemoteUIServices
	 */
	private IRemoteUIServices getRemoteUIServices(IProject project) {
		return RemoteUIServices.getRemoteUIServices(fRemoteConnection.getRemoteServices());
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite parentc = (Composite) super.createDialogArea(parent);

		Composite composite = new Composite(parentc, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);

		Font font = parentc.getFont();
		composite.setFont(font);

		Label label = new Label(composite, SWT.NONE);
		label.setText(Messages.ResourceMappingSourceContainerDialog_6);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		label.setLayoutData(data);

		fPathText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		fPathText.setText(EMPTY_STRING);
		fPathText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				fPath = fPathText.getText();
				updateOKStatus();
			}
		});
		data = new GridData(GridData.FILL_HORIZONTAL);
		fPathText.setLayoutData(data);

		Button button = new Button(composite, SWT.PUSH);
		button.setFont(font);
		button.setText("Browse..."); //$NON-NLS-1$
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String path = browseDirectory();
				if (path != null) {
					fPathText.setText(path);
				}
			}
		});

		return parentc;
	}
}
