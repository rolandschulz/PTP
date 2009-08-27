/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mike Kucera (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.ui.wizards;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.ptp.internal.rdt.ui.RSEUtils;
import org.eclipse.ptp.rdt.ui.messages.Messages;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.files.ui.dialogs.SystemRemoteFolderDialog;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class IndexFileLocationWidget extends Composite {

	
	
	///private final Label label;
	private final Text text;
	private final Button browseButton;
	//private final Button validateButton;
	private final Button defaultButton;
	
	private IHost host;
	private ListenerList pathListeners = new ListenerList();
	
	private Map<String,String> previousSelections = new HashMap<String,String>();

	
	
	
	public IndexFileLocationWidget(Composite parent, int style, IHost initialHost, String defaultPath) {
		super(parent, style);
		
		this.setLayout(new GridLayout(1, false));
		this.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Group group = new Group(this, SWT.NONE);
		group.setText(Messages.getString("IndexFileLocationWidget.0")); //$NON-NLS-1$
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(new GridData(GridData.FILL_BOTH));
		
//		label = new Label(this, SWT.NONE);
//		label.setText("Scope Config Location");
//		GridData data = new GridData();
//		data.horizontalSpan = 4;
//		label.setLayoutData(data);
		
		text = new Text(group, SWT.BORDER);
		GridData data = new GridData();
		data.widthHint = 300;
		text.setLayoutData(data);
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String path = text.getText();
				
				previousSelections.put(key(host), path);
				
				for(Object listener : pathListeners.getListeners()) {
					((IIndexFilePathChangeListener)listener).pathChanged(path);
				}
			}
		});
		
		browseButton = new Button(group, SWT.NONE);
		browseButton.setText(Messages.getString("IndexFileLocationWidget.1"));   //$NON-NLS-1$
		browseButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		browseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				browse();
			}
		});
		
		new Label(group, SWT.NONE).setText(""); //$NON-NLS-1$
		
//		validateButton = new Button(group, SWT.NONE);
//		validateButton.setText("Validate...");  //$NON-NLS-1$
//		validateButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		validateButton.addSelectionListener(new SelectionAdapter() {
//			public void widgetSelected(SelectionEvent e) {
//				validate();
//			}
//		});
//		
//		new Label(group, SWT.NONE).setText(""); //$NON-NLS-1$
		
		defaultButton = new Button(group, SWT.NONE);
		defaultButton.setText(Messages.getString("IndexFileLocationWidget.2"));  //$NON-NLS-1$
		defaultButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		defaultButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				restoreDefault();
			}
		});
		
//		Label warnLabel = new Label(group, SWT.NONE);
//		data = new GridData();
//		data.horizontalSpan = 2;
//		warnLabel.setLayoutData(data);
//		warnLabel.setText("Please reindex the project for changes to take effect."); //$NON-NLS-1$
		
		setHost(initialHost);
		if(defaultPath != null)
			text.setText(defaultPath);
	}
	
	
	public IHost getHost() {
		return host;
	}

	
	public void setHost(IHost host) {
		if(host == null)
			throw new NullPointerException();
		this.host = host;
		
		String path = previousSelections.get(key(host));
		if(path == null)
			path = RSEUtils.getDefaultConfigDirectory(host);
		if(path == null)
			path = ""; //$NON-NLS-1$
		
		text.setText(path); // modify event listener updates map
	}
	
	
	private static String key(IHost host) {
		return host.getSystemProfileName() + "." + host.getAliasName(); //$NON-NLS-1$
	}
	
	public String getConfigLocationPath() {
		return text.getText();
	}
	
	
	public void addPathListener(IIndexFilePathChangeListener listener) {
		pathListeners.add(listener);
	}
	
	public void removePathListener(IIndexFilePathChangeListener listener) {
		pathListeners.remove(listener);
	}
	
	private void browse() {
		SystemRemoteFolderDialog folderDialog = new SystemRemoteFolderDialog(getShell(), host.getAliasName());
		folderDialog.setDefaultSystemConnection(host, true);
		folderDialog.open();
		
		Object remoteObject = folderDialog.getSelectedObject();
		if(remoteObject instanceof IRemoteFile) {
			IRemoteFile folder = (IRemoteFile)remoteObject;
			text.setText(folder.getCanonicalPath());
		}
	}

	
//	private void validate() {
//		Shell shell = getShell();
//		String title = "Path Validation"; 
//		
//		String path = text.getText();
//		if(path == null || path.length() == 0) {
//			MessageDialog.openError(shell, title, "Please enter a path"); 
//			return;
//		}
//		
//		
//		VerifyResult result = RSEUtils.verifyRemoteConfigDirectory(host, path);
//		switch(result) {
//		case ERROR:
//			MessageDialog.openError(shell, title, "Could not contact remote host to verify path"); 
//			break;
//		case INVALID:
//			MessageDialog.openWarning(shell, title, "The path does not exist or is not writable"); 
//			break;
//		case VERIFIED:
//			MessageDialog.openInformation(shell, title, "The path exists and is writable");
//			break;
//		}
//	}
	
	private void restoreDefault() {
		text.setText(RSEUtils.getDefaultConfigDirectory(host));
	}
	

}
