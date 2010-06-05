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

import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.internal.ui.RemoteUIImages;
import org.eclipse.ptp.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.ptp.remote.ui.PTPRemoteUIPlugin;
import org.eclipse.ptp.remote.ui.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.ui.progress.PendingUpdateAdapter;

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
	public static final int SINGLE = 0x01;
	public static final int MULTI = 0x02;

	private final static int widthHint = 300;
	private final static int heightHint = 300;

	private Tree tree = null;
	private TreeViewer treeViewer = null;
	private Text remotePathText = null;
	private Button okButton = null;
	private Combo connectionCombo = null;
	private Button newButton = null;
	private Button upButton = null;

	private int browserType;
	private String dialogTitle;
	private String dialogLabel;

	private boolean showConnections = false;
	private String remotePath = EMPTY_STRING;
	private String remotePaths[];
	private String fInitialPath = null;
	private IPath fRootPath = null;
	private IRemoteServices fServices = null;
	private IRemoteFileManager fFileMgr = null;
	private IRemoteConnection fConnection = null;
	private IRemoteConnectionManager fConnMgr = null;
	private IRemoteUIConnectionManager fUIConnMgr = null;
	private int optionFlags = SINGLE;

	public RemoteResourceBrowser(IRemoteServices services,
			IRemoteConnection conn, Shell parent, int flags) {
		super(parent);
		setShellStyle(SWT.RESIZE | getShellStyle());
		fServices = services;
		fConnection = conn;
		this.optionFlags = flags;
		if (conn == null) {
			showConnections = true;
		}
		fConnMgr = services.getConnectionManager();
		fUIConnMgr = PTPRemoteUIPlugin.getDefault().getRemoteUIServices(
				services).getUIConnectionManager();
		setTitle(Messages.RemoteResourceBrowser_resourceTitle);
		setType(FILE_BROWSER | DIRECTORY_BROWSER);
	}

	/**
	 * Get the fConnection that was selected
	 * 
	 * @return selected fConnection
	 */
	public IRemoteConnection getConnection() {
		return fConnection;
	}

	/**
	 * Get the path that was selected.
	 * 
	 * @return selected path
	 */
	public String getPath() {
		if (remotePath.equals("")) { //$NON-NLS-1$
			return null;
		}
		return remotePath;
	}

	/**
	 * Get the paths that were selected.
	 * 
	 * @return selected paths
	 */
	public String[] getPaths() {
		return remotePaths;
	}

	/**
	 * Set the initial path to start browsing. This will be set in the browser
	 * text field, and in a future version should expand the browser to this
	 * location if it exists.
	 * 
	 * @param path
	 */
	public void setInitialPath(String path) {
		fInitialPath = path;
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
	 * Set the type of browser. Can be either a file browser (allows selection
	 * of files) or a directory browser (allows selection of directories), or
	 * both.
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
	 * Change the viewers input. Called when a new fConnection is selected.
	 * 
	 * @param conn
	 *            new fConnection
	 * @return true if input successfully changed
	 */
	private boolean changeInput(final IRemoteConnection conn) {
		if (conn == null) {
			return false;
		}

		if (!conn.isOpen()) {
			fUIConnMgr.openConnectionWithProgress(getShell(), conn);
			if (!conn.isOpen()) {
				return false;
			}
		}

		fFileMgr = fServices.getFileManager(conn);
		if (fFileMgr != null) {
			/*
			 * Note: the call to findInitialPath must happen before the
			 * treeViewer input is set or the treeViewer fails. No idea why this
			 * is.
			 */
			String cwd = conn.getWorkingDirectory();
			IPath initial = findInitialPath(cwd, fInitialPath);
			
			//TODO: not platform independent - needs IRemotePath 
			setRoot(initial.toString());

			fConnection = conn;
			return true;
		}

		return false;
	}
	
	/**
	 * When a new fConnection is selected, make sure it is open before using it.
	 */
	private void connectionSelected() {
		int i = connectionCombo.getSelectionIndex();
		if (i >= 0) {
			/*
			 * Make sure the fConnection is open before we try and read from the
			 * fConnection.
			 */
			final IRemoteConnection conn = fConnMgr
					.getConnection(connectionCombo.getItem(i));
			if (!changeInput(conn)) {
				/*
				 * Reset combo back to the previous selection
				 */
				if (fConnection == null) {
					connectionCombo.deselectAll();
				} else {
					for (i = 0; i < connectionCombo.getItemCount(); i++) {
						if (fConnection.equals(connectionCombo.getItem(i))) {
							connectionCombo.select(i);
							break;
						}
					}
				}
			}
		}
	}

	/**
	 * Create composite to allow fConnection selection and creation.
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
				if (fUIConnMgr != null) {
					IRemoteConnection conn = fUIConnMgr
							.newConnection(getShell());
					if (conn != null) {
						updateConnectionCombo(conn);
					}
				}
			}
		});
	}

	/**
	 * Determine the initial path for the browser. If the initial path is
	 * not supplied or does not exist on the remote machine, then the
	 * initial path will be the cwd.
	 * 
	 * @param cwd
	 * @param initialPath
	 * @return
	 */
	private IPath findInitialPath(String cwd, String initialPath) {
		if (initialPath != null) {
			IPath path = new Path(initialPath);
			if (path.isAbsolute() &&
				fFileMgr.getResource(initialPath).fetchInfo().exists()) {
				return new Path(initialPath);
			}
		}
		return new Path(cwd);
	}

	/**
	 * Set the root directory for the browser. This will also update
	 * the text field with the path.
	 * 
	 * @param path path of root directory
	 */
	private void setRoot(String path) {
		if (fFileMgr != null) {
			IFileStore root = fFileMgr.getResource(path);
			treeViewer.setInput(new DeferredFileStore(root));
			remotePathText.setText(path);
			remotePathText.setSelection(remotePathText.getText().length());
			fRootPath = new Path(path);
		}
	}

	/**
	 * Update fConnection combo dropdown with current connections. If supplied
	 * conn will be selected in the list.
	 * 
	 * @param conn
	 *            fConnection to select in the list
	 */
	private void updateConnectionCombo(IRemoteConnection conn) {
		IRemoteConnectionManager mgr = fServices.getConnectionManager();
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
		if (remotePathText != null && okButton != null) {
			if (remotePathText.getText().equals(EMPTY_STRING)) {
				okButton.setEnabled(false);
			} else {
				okButton.setEnabled(true);
			}
		}
		if (remotePathText != null && upButton != null) {
			boolean enabled = false;
			String pathText = remotePathText.getText();
			if (!pathText.equals("")) { //$NON-NLS-1$
				IPath path = new Path(pathText);
				enabled = !path.isRoot();
			}
			upButton.setEnabled(enabled);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButton(org.eclipse.swt.widgets.Composite, int, java.lang.String, boolean)
	 */
	@Override
	protected Button createButton(Composite parent, int id, String label,
			boolean defaultButton) {
		Button button = super.createButton(parent, id, label, defaultButton);
		if (id == IDialogConstants.OK_ID) {
			okButton = button;
		}
		return button;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.Dialog#createContents(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		setTitle(dialogTitle);
		remotePathText.setText(remotePath);
		if (showConnections) {
			updateConnectionCombo(fConnection);
		} else {
			changeInput(fConnection);
		}
		return contents;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite main = (Composite) super.createDialogArea(parent);

		final Composite dialogComp = new Composite(main, SWT.NONE);
		dialogComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false));
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		dialogComp.setLayout(layout);

		Label label = new Label(dialogComp, SWT.NONE);
		label.setText(dialogLabel);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);

		if (showConnections) {
			createConnectionChooser(dialogComp);
		}

		remotePathText = new Text(dialogComp, SWT.BORDER | SWT.SINGLE);
		remotePathText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				remotePath = remotePathText.getText();
				updateDialog();
			}
		});
		remotePathText.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				getShell().setDefaultButton(null); // allow text widget to receive SWT.DefaultSelection event
			}
			public void focusLost(FocusEvent e) {
				getShell().setDefaultButton(okButton);
			}
		});
		remotePathText.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				remotePathText.setSelection(remotePathText.getText().length());
				setRoot(remotePathText.getText());
			}
			
		});
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = widthHint;
		remotePathText.setLayoutData(gd);
		
		upButton = new Button(dialogComp, SWT.PUSH|SWT.FLAT);
		upButton.setImage(RemoteUIImages.get(RemoteUIImages.IMG_ELCL_UP_NAV));
		upButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (!fRootPath.isRoot()) {
					setRoot(fRootPath.removeLastSegments(1).toOSString());
				}
			}
		});

		if ((optionFlags & MULTI) == MULTI) {
			tree = new Tree(main, SWT.MULTI | SWT.BORDER);
		} else {
			tree = new Tree(main, SWT.SINGLE | SWT.BORDER);
		}

		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		// see bug 158380
		gd.heightHint = Math.max(main.getParent().getSize().y, heightHint);
		tree.setLayoutData(gd);

		treeViewer = new TreeViewer(tree);
		treeViewer.setContentProvider(new RemoteContentProvider());
		treeViewer.setLabelProvider(new WorkbenchLabelProvider());
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				if (!selection.isEmpty()
						&& selection instanceof IStructuredSelection) {
					IStructuredSelection ss = (IStructuredSelection) selection;
					Object element = ss.getFirstElement();
					if (element instanceof DeferredFileStore) {
						DeferredFileStore dfs = (DeferredFileStore) element;
						remotePathText.setText(dfs.getFileStore()
								.toURI().getPath());
					}
					Vector<String> selectedPaths = new Vector<String>(ss.size());
					for (Object currentSelection : ss.toArray()) {
						if (currentSelection instanceof DeferredFileStore) {
							selectedPaths.add(((DeferredFileStore) currentSelection)
											.getFileStore().toURI()
											.getPath());
						}
					}
					remotePaths = selectedPaths.toArray(new String[0]);
				}
			}
		});
		treeViewer.setComparator(new RemoteResourceComparator());
		treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection s = (IStructuredSelection) event.getSelection();
				Object o = s.getFirstElement();
				if (treeViewer.isExpandable(o)) {
					treeViewer.setExpandedState(o, !treeViewer.getExpandedState(o));
				}			
			}
			
		});
		if (browserType == DIRECTORY_BROWSER) {
			treeViewer.addFilter(new ViewerFilter() {
				public boolean select(Viewer viewer, Object parentElement,
						Object element) {
					if ((element instanceof DeferredFileStore)) {
						return ((DeferredFileStore)element).isContainer();
					}
					return element instanceof PendingUpdateAdapter;
				}
			});
		}
		
		updateDialog();

		return main;
	}
}
