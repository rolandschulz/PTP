/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.ui.scannerinfo;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.ptp.remote.ui.IRemoteUIFileManager;
import org.eclipse.ptp.remote.ui.PTPRemoteUIPlugin;
import org.eclipse.rse.core.filters.SystemFilterReference;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.files.ui.dialogs.SystemRemoteFolderDialog;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

/**
 * Dialog box that allows the user to browse the remote system for include
 * directories.
 */
public class RemoteIncludeDialog extends Dialog {

	private Shell shell;

	// buttons
	private Button b_ok;
	private Button b_cancel;
	private Button b_browse;
	private Button b_vars;

	// check boxes
	private Button b_add2confs;
	private Button b_add2langs;

	// the text input area
	private Text text;

	// used to pre-fill the text area with some text
	private String pathText = null;

	// results
	private boolean result = false;
	private String directory = null;
	private boolean isAllLanguages = false;
	private boolean isAllConfigurations = false;
	private final ICConfigurationDescription config;

	private final boolean isEdit;

	// TODO: should remove IHost and only use IRemoteServices
	// and IRemoteConnection

	// fHost used for RSE connections
	private IHost fHost = null;
	// fRemoteServices and fRemoteConnection used for others
	private IRemoteConnection fRemoteConnection = null;

	public RemoteIncludeDialog(Shell parent, String title, boolean isEdit, ICConfigurationDescription config) {
		super(parent);
		setText(title);
		this.isEdit = isEdit;
		this.config = config;
	}

	public boolean open() {
		Shell parent = getParent();
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
		shell.setText(getText());

		createDialogArea(shell);
		shell.pack();

		// center window
		Rectangle r1 = parent.getBounds();
		Rectangle r2 = shell.getBounds();
		int x = r1.x + (r1.width - r2.width) / 2;
		int y = r1.y + (r1.height - r2.height) / 2;
		shell.setBounds(x, y, r2.width, r2.height);

		shell.open();
		Display display = parent.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		return result;
	}

	protected void createDialogArea(Composite parent) {
		parent.setLayout(new GridLayout(2, false));
		GridData gd;

		Label l1 = new Label(parent, SWT.NONE);
		l1.setText(Messages.RemoteIncludeDialog_directory);
		l1.setLayoutData(gd = new GridData());
		gd.horizontalSpan = 2;

		text = new Text(parent, SWT.SINGLE | SWT.BORDER);
		text.setLayoutData(gd = new GridData(GridData.FILL_HORIZONTAL));
		gd.widthHint = 300;
		text.setText(pathText == null ? "" : pathText); //$NON-NLS-1$

		b_browse = new Button(parent, SWT.PUSH);
		b_browse.setText(Messages.RemoteIncludeDialog_browse);
		b_browse.addSelectionListener(listener);
		b_browse.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		new Label(parent, SWT.NONE);

		b_vars = new Button(parent, SWT.PUSH);
		b_vars.setText(Messages.RemoteIncludeDialog_vars);
		b_vars.addSelectionListener(listener);
		b_vars.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		b_add2confs = new Button(parent, SWT.CHECK);
		b_add2confs.setText(Messages.RemoteIncludeDialog_configurations);
		b_add2confs.setVisible(!isEdit);
		b_add2confs.setLayoutData(gd = new GridData());
		gd.horizontalSpan = 2;

		b_add2langs = new Button(parent, SWT.CHECK);
		b_add2langs.setText(Messages.RemoteIncludeDialog_languages);
		b_add2langs.setVisible(!isEdit);
		b_add2langs.setLayoutData(gd = new GridData());
		gd.horizontalSpan = 2;

		b_ok = new Button(parent, SWT.PUSH);
		b_ok.setText(Messages.RemoteIncludeDialog_ok);
		b_ok.addSelectionListener(listener);
		b_ok.setLayoutData(gd = new GridData());
		gd.widthHint = 80;
		gd.horizontalAlignment = SWT.END;

		b_cancel = new Button(parent, SWT.PUSH);
		b_cancel.setText(Messages.RemoteIncludeDialog_cancel);
		b_cancel.addSelectionListener(listener);
		b_cancel.setLayoutData(gd = new GridData());
		gd.widthHint = 80;
	}

	public void setPathText(String path) {
		this.pathText = path;
	}

	private final SelectionListener listener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			Widget pressed = e.widget;
			if (pressed.equals(b_ok)) {
				directory = text.getText();
				isAllConfigurations = b_add2confs.getSelection();
				isAllLanguages = b_add2langs.getSelection();
				result = true;
				shell.dispose();
			} else if (pressed.equals(b_cancel)) {
				result = false;
				shell.dispose();
			} else if (pressed.equals(b_browse)) {
				if (fHost != null) {
					SystemRemoteFolderDialog folderDialog = new SystemRemoteFolderDialog(shell,
							Messages.RemoteIncludeDialog_select, fHost);
					folderDialog.setShowNewConnectionPrompt(false);
					folderDialog.open();
					Object remoteObject = folderDialog.getSelectedObject();
					
					if (folderDialog.wasCancelled()) {
						return;
					}
					if (remoteObject instanceof IRemoteFile) {
						IRemoteFile folder = (IRemoteFile) remoteObject;
						text.setText(folder.getCanonicalPath());
					} else {
						// the default directory is the home directory which is a type of SystemFilterReference.
						String homeDir = ((SystemFilterReference) remoteObject).getSubSystem().getConnectorService().getHomeDirectory();
						text.setText(homeDir);
					}
				} else {
					IRemoteUIConnectionManager connMgr = getUIConnectionManager();
					if (connMgr != null) {
						connMgr.openConnectionWithProgress(shell, null, fRemoteConnection);
						if (fRemoteConnection.isOpen()) {
							IRemoteUIFileManager fileMgr = getUIFileManager();
							if (fileMgr != null) {
								fileMgr.setConnection(fRemoteConnection);
								String path = fileMgr.browseDirectory(shell, Messages.RemoteIncludeDialog_select, "", 0); //$NON-NLS-1$
								if (path != null) {
									text.setText(path);
								}
							}
						}
					}
				}
			} else if (pressed.equals(b_vars)) {
				String s = AbstractCPropertyTab.getVariableDialog(shell, config);
				if (s != null)
					text.insert(s);
			}
		}
	};

	public String getDirectory() {
		return directory;
	}

	public boolean isAllLanguages() {
		return isAllLanguages;
	}

	public boolean isAllConfigurations() {
		return isAllConfigurations;
	}

	public void setHost(IHost host) {
		fHost = host;
	}

	/**
	 * @since 2.0
	 */
	public void setConnection(IRemoteConnection connection) {
		fRemoteConnection = connection;
	}

	private IRemoteUIFileManager getUIFileManager() {
		if (fRemoteConnection != null) {
			return PTPRemoteUIPlugin.getDefault().getRemoteUIServices(fRemoteConnection.getRemoteServices()).getUIFileManager();
		}
		return null;
	}

	private IRemoteUIConnectionManager getUIConnectionManager() {
		if (fRemoteConnection != null) {
			return PTPRemoteUIPlugin.getDefault().getRemoteUIServices(fRemoteConnection.getRemoteServices())
					.getUIConnectionManager();
		}
		return null;
	}
}
