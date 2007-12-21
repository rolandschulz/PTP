/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
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
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.ptp.remote.IRemoteFileManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Generic file/directory browser for remote resources. 
 * 
 * @author greg
 *
 */
public class RemoteResourceBrowser extends Dialog {
	public final static int FILE_BROWSER = 0x01;
	public final static int DIRECTORY_BROWSER = 0x02;

	private class FileSorter implements Comparator<IFileStore> {
		public int compare(IFileStore o1, IFileStore o2) {
			return o1.getName().compareTo(o2.getName());
		}
	}
	
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
	private String remotePath;
	
	public RemoteResourceBrowser(IRemoteFileManager fileMgr, IPath initialPath, Shell parent) {
		super(parent);
		setShellStyle(SWT.RESIZE | getShellStyle());
		this.fileMgr = fileMgr;
		this.initialPath = initialPath;
		try {
			cwd = fileMgr.getWorkingDirectory(new NullProgressMonitor());
		} catch (IOException e) {
			cwd = new Path("//");
		}
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
	private IPath findInitialPath(IPath initialPath) {
		IPath path = cwd;
		
		if (initialPath.matchingFirstSegments(cwd) != cwd.segmentCount()) {
			return path;
		}
		if (initialPath != null) {
			try {
				IFileInfo info = fileMgr.getResource(initialPath, new NullProgressMonitor()).fetchInfo();
				if (info.exists()) {
					path = initialPath;
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
		return contents;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		IPath initial = findInitialPath(initialPath);
		
		Composite main = (Composite) super.createDialogArea(parent);
		
		Label label = new Label(main, SWT.NONE);
		label.setText(dialogLabel);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		label.setLayoutData(gd);
		
		remotePathText = new Text(main, SWT.BORDER | SWT.SINGLE);
		remotePathText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				remotePath = remotePathText.getText();
			}
		});
		remotePathText.setText(initial.toString());
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = widthHint;
		remotePathText.setLayoutData(gd);
		
		IFileStore root;
		IFileStore[] roots;
		try {
			root = fileMgr.getResource(cwd, new NullProgressMonitor());
			roots = new IFileStore[] {root};
		} catch (IOException e) {
			return main; //FIXME
		}
		
		tree = new Tree(main, SWT.VIRTUAL | SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = heightHint;
		gd.widthHint = widthHint;
		tree.setLayoutData(gd);
		tree.setData(roots);
		tree.addListener(SWT.SetData, new Listener() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
			 */
			public void handleEvent(Event event) {
				TreeItem item = (TreeItem) event.item;
				TreeItem parentItem = item.getParentItem();
				IFileStore file = null;
				if (parentItem == null) {
					/* root-level item */
					IFileStore[] files = (IFileStore[]) tree.getData();
					file = files[event.index];
					item.setText(cwd.toString());
				} else {
					IFileStore[] files = (IFileStore[]) parentItem.getData();
					file = files[event.index];
					item.setText(file.getName());
				}
				if (file.fetchInfo().isDirectory()) {
					IFileStore[] files = null;
					try {
						files = file.childStores(EFS.NONE, new NullProgressMonitor());
					} catch (CoreException e) {
					}
					if (files != null) {
						Arrays.sort(files, new FileSorter());
						item.setData(files);
						item.setItemCount(files.length);
					}
				} else {
					if (browserType == DIRECTORY_BROWSER) {
						Color bg = getShell().getDisplay().getSystemColor(SWT.COLOR_GRAY);
						item.setForeground(bg);
					}
				}
			}
		});
		tree.addMouseListener(new MouseAdapter() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.MouseAdapter#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
			 */
			public void mouseDoubleClick(MouseEvent e) {
				Tree tree = (Tree) e.getSource();
				TreeItem[] items = tree.getSelection();
				if (items.length > 0) {
					TreeItem item = items[0];
					if (item.getExpanded()) {
						item.setExpanded(false);
					} else {
						item.setExpanded(true);
					}
				}
			}
		});
		tree.addSelectionListener(new SelectionAdapter() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				Tree tree = (Tree) e.getSource();
				TreeItem[] items = tree.getSelection();
				if (items.length > 0) {
					TreeItem item = items[0];
					if (item.getData() == null) {
						// A file
						if (browserType == DIRECTORY_BROWSER) {
							tree.deselectAll();
							okButton.setEnabled(false);
							return;
						}
					} else {
						// A directory
						if (browserType == FILE_BROWSER) {
							okButton.setEnabled(false);
							return;
						}
					}
					IPath path = new Path(item.getText());
					for (TreeItem parent = item.getParentItem(); parent != null; ) {
						path = new Path(parent.getText()).append(path);
						parent = parent.getParentItem();
					}
					remotePathText.setText(path.toString());
					okButton.setEnabled(true);
				}
			}
			
		});
		tree.setItemCount(roots.length);
		
		return main;
	}

}
