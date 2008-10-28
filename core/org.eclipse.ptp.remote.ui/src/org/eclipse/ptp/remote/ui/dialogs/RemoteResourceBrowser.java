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
package org.eclipse.ptp.remote.ui.dialogs;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.ptp.remote.ui.Messages;
import org.eclipse.ptp.remote.ui.PTPRemoteUIPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
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
	
	private Tree tree = null;
	private TreeViewer treeViewer = null;
	private Text remotePathText = null;
	private Button okButton = null;
	private Combo connectionCombo = null;
	private Button newButton = null;

	private int browserType;
	private String dialogTitle;
	private String dialogLabel;
	
	private boolean showConnections = false;
	private String remotePath = EMPTY_STRING;
	private String initialPath = null;
	private IRemoteServices services = null;
	private IRemoteFileManager fileMgr = null;
	private IRemoteConnection connection = null;
	private IRemoteConnectionManager connMgr = null;
	private IRemoteUIConnectionManager uiConnMgr = null;
	
	public RemoteResourceBrowser(IRemoteServices services, IRemoteConnection conn, Shell parent) {
		super(parent);
		setShellStyle(SWT.RESIZE | getShellStyle());
		this.services = services;
		this.connection = conn;
		if (conn == null) {
			showConnections = true;
		}
		this.connMgr = services.getConnectionManager();
		this.uiConnMgr = PTPRemoteUIPlugin.getDefault().getRemoteUIServices(services).getUIConnectionManager();
		setTitle(Messages.RemoteResourceBrowser_resourceTitle);
		setType(FILE_BROWSER | DIRECTORY_BROWSER);
	}
	
	/**
	 * Get the connection that was selected
	 * 
	 * @return selected connection
	 */
	public IRemoteConnection getConnection() {
		return connection;
	}
	
	/**
	 * Get the path that was selected. 
	 * 
	 * @return selected path
	 */
	public String getPath() {
		if (remotePath.equals("")) {
			return null;
		}
		return remotePath;
	}

	/**
	 * Set the initial path to start browsing. This will be set in the browser text field,
	 * and in a future version should expand the browser to this location if it exists.
	 * @param path
	 */
	public void setInitialPath(String path) {
		initialPath = path;
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
	 * Show available connections on browser if possible.
	 * 
	 * @param enable
	 */
	public void showConnections(boolean enable) {
		this.showConnections = enable;
	}

	/**
	 * Change the viewers input. Called when a new connection is selected.
	 * 
	 * @param conn new connection
	 * @return true if input successfully changed
	 */
	private boolean changeInput(final IRemoteConnection conn) {
		if (conn == null) {
			return false;
		}
		
		if (!conn.isOpen()) {
			IRunnableWithProgress op = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException,
						InterruptedException {
					try {
						conn.open(monitor);
					} catch (RemoteConnectionException e) {
						ErrorDialog.openError(getShell(), "Connection Error",
								"Could not open connection",
								new Status(IStatus.ERROR, PTPRemoteUIPlugin.PLUGIN_ID, e.getMessage()));
					}
				}

			};
			try {
				new ProgressMonitorDialog(getShell()).run(true, true, op);
			} catch (InvocationTargetException e) {
				ErrorDialog.openError(getShell(), "Connection Error",
						"Could not open connection",
						new Status(IStatus.ERROR, PTPRemoteUIPlugin.PLUGIN_ID, e.getMessage()));
			} catch (InterruptedException e) {
				ErrorDialog.openError(getShell(), "Connection Error",
						"Could not open connection",
						new Status(IStatus.ERROR, PTPRemoteUIPlugin.PLUGIN_ID, e.getMessage()));
			}						
		}
		
		fileMgr = services.getFileManager(conn);
		if (fileMgr != null) {
			/*
			 * Note: the call to findInitialPath must happen before the treeViewer input
			 * is set or the treeViewer fails. No idea why this is.
			 */
			IPath cwd = fileMgr.getWorkingDirectory();
			IPath initial = findInitialPath(cwd, new Path(initialPath));
			remotePathText.setText(initial.toString());
			
			IFileStore root;
			try {
				root = fileMgr.getResource(cwd, new NullProgressMonitor());
			} catch (IOException e) {
				return false;
			}
			
			treeViewer.setInput(new DeferredFileStore(root));
			
			connection = conn;
			return true;
		}
		
		return false;
	}
	
	/**
	 * When a new connection is selected, make sure it is open before using it.
	 */
	private void connectionSelected() {
		int i = connectionCombo.getSelectionIndex();
		if (i >= 0) {
			/*
			 * Make sure the connection is open before we try and read
			 * from the connection.
			 */
			final IRemoteConnection conn = connMgr.getConnection(connectionCombo.getItem(i));
			if(!changeInput(conn)) {
				/*
				 * Reset combo back to the previous selection
				 */
				if (connection == null) {
					connectionCombo.deselectAll();
				} else {
					for (i = 0; i < connectionCombo.getItemCount(); i++) {
						if (connection.equals(connectionCombo.getItem(i))) {
							connectionCombo.select(i);
							break;
						}
					}							
				}
			}
		}
	}
	
	/**
	 * Create composite to allow connection selection and creation.
	 * 
	 * @param comp
	 */
	private void createConnectionChooser(Composite comp) {
		final Composite connComp = new Composite(comp, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.marginWidth = 0;
		connComp.setLayout(layout);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		connComp.setLayoutData(gd);

		Label label = new Label(connComp, SWT.NONE);
		label.setText(Messages.RemoteResourceBrowser_connectonLabel);
		gd = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		label.setLayoutData(gd);

		connectionCombo = new Combo(connComp, SWT.READ_ONLY);
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		connectionCombo.setLayoutData(gd);
		connectionCombo.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent event) {
			}
			public void widgetSelected(SelectionEvent event) {
				if (event.getSource() == connectionCombo) {
					connectionSelected();
				}
			}
		});

		newButton = new Button(connComp, SWT.PUSH);
		newButton.setText(Messages.RemoteResourceBrowser_newConnection);
		gd = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		newButton.setLayoutData(gd);
		newButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				if (uiConnMgr != null) {
					IRemoteConnection conn = uiConnMgr.newConnection(getShell());
					if (conn != null) {
						updateConnectionCombo(conn);
					}
				}
			}
		});
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
	private IPath findInitialPath(IPath cwd, IPath pathToCheck) {
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
	
	/**
	 * Update connection combo dropdown with current connections. If supplied
	 * conn will be selected in the list.
	 * 
	 * @param conn connection to select in the list
	 */
	private void updateConnectionCombo(IRemoteConnection conn) {
		IRemoteConnectionManager mgr = services.getConnectionManager();
		IRemoteConnection[] conns = mgr.getConnections();
		
		if (conns.length > 0) {
			Arrays.sort(conns, new Comparator<IRemoteConnection>() {
				public int compare(IRemoteConnection c1, IRemoteConnection c2) {
					return c1.getName().compareToIgnoreCase(c2.getName());
				}
			});
		}
		
		connectionCombo.removeAll();
		
		int selected = -1;
		for (int i = 0; i < conns.length; i++) {
			connectionCombo.add(conns[i].getName());
			if (conn != null && conn.equals(conns[i])) {
				selected = i;
			}
		}
		
		if (selected < 0) {
			connectionCombo.deselectAll();
		} else {
			connectionCombo.select(selected);
		}
		
		connectionSelected();
	}

	private void updateDialog() {
		if (remotePathText.getText().equals(EMPTY_STRING)) {
			okButton.setEnabled(false);
		} else {
			okButton.setEnabled(true);
		}
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
		if (showConnections) {
			updateConnectionCombo(connection);
		} else {
			changeInput(connection);
		}
		return contents;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite main = (Composite) super.createDialogArea(parent);
		
		final Composite dialogComp = new Composite(main, SWT.NONE);
		dialogComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		dialogComp.setLayout(layout);

		Label label = new Label(dialogComp, SWT.NONE);
		label.setText(dialogLabel);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		label.setLayoutData(gd);
		
		if (showConnections) {
			createConnectionChooser(dialogComp);
		}
		
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
		
		tree = new Tree(main, SWT.SINGLE | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
				
		// see bug 158380
		data.heightHint = Math.max(main.getParent().getSize().y, heightHint);
		tree.setLayoutData(data);	

		treeViewer = new TreeViewer(tree) {
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

		return main;
	}
}
