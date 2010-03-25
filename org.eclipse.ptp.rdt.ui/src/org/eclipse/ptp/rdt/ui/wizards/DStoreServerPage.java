/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.rdt.ui.wizards;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.ptp.rdt.ui.wizards.DStoreServerWidget.FieldModifier;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.ui.widgets.RemoteDirectoryWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class DStoreServerPage extends Composite {
	private Text text;
	
	private RemoteDirectoryWidget fLocationWidget;
	private ListenerList modifyListeners = new ListenerList();
	
	public DStoreServerPage(Composite parent, int style) {
		super(parent, style);
		
		GridLayout layout = new GridLayout(1, false);
		setLayout(layout);
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		fLocationWidget = new RemoteDirectoryWidget(this, SWT.NONE, "DStore Location", null); //$NON-NLS-1$
		fLocationWidget.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				e.data = FieldModifier.VALUE_DSTORE_LOCATION;
				notifyListeners(e);
			}
		});
		
		Group group = new Group(this, SWT.NONE);
		group.setText("Server Command"); //$NON-NLS-1$
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite textComp = new Composite(group, SWT.NONE);
		textComp.setLayout(new GridLayout(2, false));
		textComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Label label = new Label(textComp, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		label.setText("Command:"); //$NON-NLS-1$
		
		text = new Text(textComp, SWT.BORDER);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = text.getLineHeight() * 3;
		data.widthHint = 300;
		text.setLayoutData(data);
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				e.data = FieldModifier.VALUE_DSTORE_COMMAND;
				notifyListeners(e);
			}
		});
	}
	
	public void addModifyListener(ModifyListener listener) {
		modifyListeners.add(listener);
	}
	
	public String getLocationPath() {
		return fLocationWidget.getLocationPath();
	}
	
	public String getServerCommand() {
		return text.getText();
	}
	
	public void removeModifyListener(ModifyListener listener) {
		modifyListeners.remove(listener);
	}

	public void setConnection(IRemoteServices services, IRemoteConnection conn) {
		fLocationWidget.setConnection(services, conn);
	}

	public void setLocationPath(String path) {
		fLocationWidget.setLocationPath(path);
	}
	
	public void setServerCommand(String command) {
		text.setText(command);
	}
	
	private void notifyListeners(ModifyEvent e) {
		for (Object listener : modifyListeners.getListeners()) {
			((ModifyListener)listener).modifyText(e);
		}
	}
}
