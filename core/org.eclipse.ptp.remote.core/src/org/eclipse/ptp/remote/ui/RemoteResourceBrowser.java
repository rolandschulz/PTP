/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.remote.ui;

import java.io.IOException;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ptp.remote.IRemoteFileManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Generic file/directory browser for remote resources. 
 * 
 * @author greg
 *
 */
public class RemoteResourceBrowser extends Dialog {
	public final static String EMPTY_STRING = ""; //$NON-NLS-1$
	public final static int FILE_BROWSER = 0x01;
	public final static int DIRECTORY_BROWSER = 0x02;

	private final static int widthHint = 300;
	private final static int heightHint = 300;
	
	private Tree tree;
	private Text remotePathText;
	private Button okButton;

	private int browserType;
	private IRemoteFileManager fileMgr;
	private IPath initialPath = null;
	private IPath cwd;
	private String dialogTitle;
	private String dialogLabel;
	private String remotePath = EMPTY_STRING;
	
	public RemoteResourceBrowser(IRemoteFileManager fileMgr, Shell parent) {
		super(parent);
		setShellStyle(SWT.RESIZE | getShellStyle());
		this.fileMgr = fileMgr;
		cwd = fileMgr.getWorkingDirectory();
		setTitle(Messages.RemoteResourceBrowser_resourceTitle);
		setType(FILE_BROWSER | DIRECTORY_BROWSER);
	}
	
	/**
	 * Get the path that was selected. 
	 * 
	 * @return selected path
	 */
	public IPath getPath() {
		if (remotePath.equals("")) {
			return null;
		}
		return new Path(remotePath);
	}

	/**
	 * Set the dialogTitle of the dialog.
	 * 
	 * @param dialogTitle
	 */
	public void setTitle(String title) {
		dialogTitle = title;
		if (dialogTitle == null) {
			dialogTitle = ""; //$NON-NLS-1$
		}
		Shell shell = getShell();
		if ((shell != null) && !shell.isDisposed()) {
			shell.setText(dialogTitle);
		}
	}
	
	/**
	 * Set the type of browser. Can be either a file browser (allows
	 * selection of files) or a directory browser (allows selection
	 * of directories), or both.
	 */
	public void setType(int type) {
		browserType = type;
		if (type == FILE_BROWSER) {
			dialogLabel = Messages.RemoteResourceBrowser_fileLabel;
			setTitle(Messages.RemoteResourceBrowser_fileTitle);
		} else if (type == DIRECTORY_BROWSER) {
			dialogLabel = Messages.RemoteResourceBrowser_directoryLabel;
			setTitle(Messages.RemoteResourceBrowser_directoryTitle);
		} else {
			dialogLabel = Messages.RemoteResourceBrowser_resourceLabel;
			setTitle(Messages.RemoteResourceBrowser_resourceTitle);
		}
	}

	/**
	 * Determine the initial path for the browser. This is the initialPath, if:
	 *
	 * 1. it was supplied
	 * 2. if it exists on the remote machine
	 * 3. if it is relative to the cwd
	 * 
	 * If none of these conditions are satisfied, then the initial path will be the cwd.
	 * 
	 * @param initialPath
	 * @return
	 */
	private IPath findInitialPath(IPath pathToCheck) {
		IPath path = cwd;
		
		if (pathToCheck.matchingFirstSegments(cwd) != cwd.segmentCount()) {
			return path;
		}
		if (pathToCheck != null) {
			try {
				IFileInfo info = fileMgr.getResource(pathToCheck, new NullProgressMonitor()).fetchInfo();
				if (info.exists()) {
					path = pathToCheck;
				}
			} catch (IOException e) {
			}
		}
		return path;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createButtonBar(Composite parent) {
		Control buttons = super.createButtonBar(parent);
		okButton = getButton(IDialogConstants.OK_ID);
		okButton.setEnabled(false);
		return buttons;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		setTitle(dialogTitle);
		remotePathText.setText(remotePath);
		return contents;
	}
	
	/**
	 * Set the initial path to start browsing. This will be set in the browser text field,
	 * and in a future version should expand the browser to this location if it exists.
	 * @param path
	 */
	public void setInitialPath(String path) {
		IPath initial = findInitialPath(new Path(path));
		remotePath = initial.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite main = (Composite) super.createDialogArea(parent);
		
		Label label = new Label(main, SWT.NONE);
		label.setText(dialogLabel);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		label.setLayoutData(gd);
		
		remotePathText = new Text(main, SWT.BORDER | SWT.SINGLE);
		remotePathText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				remotePath = remotePathText.getText();
				updateDialog();
			}
		});
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = widthHint;
		remotePathText.setLayoutData(gd);
		
		IFileStore root;
		try {
			root = fileMgr.getResource(cwd, new NullProgressMonitor());
		} catch (IOException e) {
			return main; //FIXME
		}
		
		tree = new Tree(main, SWT.SINGLE | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
				
		// see bug 158380
		data.heightHint = Math.max(main.getParent().getSize().y, heightHint);
		tree.setLayoutData(data);	

		TreeViewer treeViewer = new TreeViewer(tree) {
			/*
			 * Fix to allow filtering to be used without triggering fetching 
			 * of the contents of all children (see bug 62268)
			 */
			public boolean isExpandable(Object element) {
				ITreeContentProvider cp = (ITreeContentProvider) getContentProvider();
				if(cp == null)
					return false;
				
				return cp.hasChildren(element);
			}
		};
		treeViewer.setContentProvider(new RemoteContentProvider());
		treeViewer.setLabelProvider(new WorkbenchLabelProvider());
		treeViewer.getTree().addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
			}
		});
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
					IStructuredSelection ss = (IStructuredSelection)selection;
					Object element = ss.getFirstElement();
					if (element instanceof DeferredFileStore) {
						DeferredFileStore dfs = (DeferredFileStore)element;
						if (browserType == DIRECTORY_BROWSER) {
							if (dfs.isContainer()) {
								remotePathText.setText(dfs.getFileStore().toURI().getPath());
							}
						} else {
							if (dfs.isContainer()) {
								event.getSelectionProvider().setSelection(null);
							} else {
								remotePathText.setText(dfs.getFileStore().toURI().getPath());
							}
						}
					}
				}
			}
		});
		treeViewer.setComparator(new RemoteResourceComparator());
		treeViewer.setInput(new DeferredFileStore(root));

		return main;
	}
	
	private void updateDialog() {
		if (remotePathText.getText().equals(EMPTY_STRING)) {
			okButton.setEnabled(false);
		} else {
			okButton.setEnabled(true);
		}
	}

}
