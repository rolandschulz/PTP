/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.ptp.internal.rdt.ui.RSEUtils;
import org.eclipse.ptp.rdt.ui.messages.Messages;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.files.ui.dialogs.SystemRemoteFolderDialog;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
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
		
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		this.setLayout(layout);
		this.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Group group = new Group(this, SWT.NONE);
		group.setText(Messages.getString("IndexFileLocationWidget.0")); //$NON-NLS-1$
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		text = new Text(group, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		//data.widthHint = 300;
		data.horizontalSpan = 2;
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
		
		text.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent fe){
				validateIndexLoc();
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
		
		defaultButton = new Button(group, SWT.NONE);
		defaultButton.setText(Messages.getString("IndexFileLocationWidget.2"));  //$NON-NLS-1$
		defaultButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		defaultButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				restoreDefault();
			}
		});
		
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
		validateIndexLoc();
	}

	
	private void restoreDefault() {
		text.setText(RSEUtils.getDefaultConfigDirectory(host));
		validateIndexLoc();
	}
	
	private void validateIndexLoc() {
		String path = text.getText();
		IRemoteFileSubSystem remoteFileSubSystem = RemoteFileUtility.getFileSubSystem(host);
		
		// display the message in the property dialog if possible
		Composite parent = text.getParent();
		PreferenceDialog dialog = null;
		while (parent!=null && !(parent instanceof Shell)) 
			parent=parent.getParent();
		if (parent instanceof Shell) {
			if (parent.getData() instanceof PreferenceDialog) {
				dialog = (PreferenceDialog)parent.getData();
				dialog.setMessage(null, IMessageProvider.NONE); 
			}					
		}			
		
		try {
			IRemoteFile currentRemoteFolder = 
				remoteFileSubSystem.getRemoteFileObject(path, new NullProgressMonitor());
						
			if (currentRemoteFolder == null || !currentRemoteFolder.canWrite()){	
				if (dialog!=null) 
					dialog.setMessage(Messages.getString("InvalidIndexLocationLabel"), IMessageProvider.ERROR); //$NON-NLS-1$
				else
					// just display a dialog
					MessageDialog.openWarning(getShell(), 
							Messages.getString("InvalidIndexLocationTitle"), Messages.getString("InvalidIndexLocationLabel")); //$NON-NLS-1$ //$NON-NLS-2$
			}
			
		} catch (SystemMessageException e1) {
			e1.printStackTrace();
		}
	}
	

}
