/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/ 
package org.eclipse.ptp.internal.rdt.ui.scannerinfo;


import org.eclipse.rse.files.ui.dialogs.SystemRemoteFolderDialog;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;


/**
 * Dialog box that allows the user to browse the remote system for
 * include directories.
 * 
 * @author Mike Kucera
 */
public class RemoteIncludeDialog extends Dialog {
	
	private Shell shell; 
	
	// buttons
	private Button b_ok;
	private Button b_cancel;
	private Button b_browse;
	
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
	
	
	private final boolean isEdit;
	
	
	public RemoteIncludeDialog(Shell parent, String title, boolean isEdit) {
		
		super(parent);
		setText(title);
		this.isEdit = isEdit;
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
	 	while(!shell.isDisposed()) {
	 		if(!display.readAndDispatch()) 
	 			display.sleep();
	 	}
		return result;
	}
	

	protected Control createDialogArea(Composite c) {
		RowLayout rl = new RowLayout(SWT.VERTICAL); // don't need a grid layout
		rl.marginWidth = rl.marginHeight = 5;
		rl.fill = true;
		c.setLayout(rl);
		
		Label l1 = new Label(c, SWT.NONE);
		l1.setText(Messages.RemoteIncludeDialog_directory);

		// composite to hold text area and browse button
		Composite c1 = new Composite(c, SWT.NONE);
		c1.setLayout(new RowLayout(SWT.HORIZONTAL));

		text = new Text(c1, SWT.SINGLE | SWT.BORDER);
		text.setLayoutData(new RowData(300, SWT.DEFAULT));
		if(pathText != null)
			text.setText(pathText);

		b_browse = new Button(c1, SWT.PUSH);
		b_browse.setText(Messages.RemoteIncludeDialog_browse);
		b_browse.addSelectionListener(listener);

		// composite to hold checkboxes
		Composite c2 = new Composite(c, SWT.NONE);
		c2.setLayout(new RowLayout(SWT.VERTICAL));
		b_add2confs = new Button(c2, SWT.CHECK);
		b_add2confs.setText(Messages.RemoteIncludeDialog_configurations);
		b_add2langs = new Button(c2, SWT.CHECK);
		b_add2langs.setText(Messages.RemoteIncludeDialog_languages);
		
		if(isEdit) {
			b_add2confs.setVisible(false);
			b_add2langs.setVisible(false);
		}
		
		// composite to hold OK and Cancel buttons
		Composite c3 = new Composite(c, SWT.NONE);
		c3.setLayout(new GridLayout(3, false));
		
		GridData gd = new GridData();
		gd.widthHint = 200;
		Label ph = new Label(c3, 0); // place holder
		ph.setLayoutData(gd);
		ph.setText(""); //$NON-NLS-1$
		
		b_ok = new Button(c3, SWT.PUSH);
		b_ok.setText(Messages.RemoteIncludeDialog_ok); 
		b_ok.addSelectionListener(listener);
		gd = new GridData();
		gd.widthHint = 80;
		b_ok.setLayoutData(gd);
		
		b_cancel = new Button(c3, SWT.PUSH);
		b_cancel.setText(Messages.RemoteIncludeDialog_cancel); 
		b_cancel.addSelectionListener(listener);
		b_cancel.setLayoutData(new RowData(300, SWT.DEFAULT));
		gd = new GridData();
		gd.widthHint = 80;
		b_cancel.setLayoutData(gd);
		
		return c;
	}
	
	
	
	public void setPathText(String path) {
		this.pathText = path;
	}
	

	private SelectionListener listener = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			Widget pressed = e.widget;
			if(pressed.equals(b_ok)) { 
				directory = text.getText();
				isAllConfigurations = b_add2confs.getSelection();
				isAllLanguages = b_add2langs.getSelection();
				result = true;
				shell.dispose(); 
			} 
			else if(pressed.equals(b_cancel)) {
				result = false;
				shell.dispose();
			} 
			else if(pressed.equals(b_browse)) {
				SystemRemoteFolderDialog folderDialog = new SystemRemoteFolderDialog(shell, Messages.RemoteIncludeDialog_select); 
				folderDialog.open();
				Object remoteObject = folderDialog.getSelectedObject();
				if(remoteObject instanceof IRemoteFile) {
					IRemoteFile folder = (IRemoteFile)remoteObject;
					text.setText(folder.getCanonicalPath());
				}
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

}
