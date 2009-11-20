/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.rdt.ui.wizards;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

public class DStoreServerWidget extends Composite {
	public enum FieldModifier {
		VALUE_DSTORE_LOCATION, 
		VALUE_DSTORE_COMMAND,
		VALUE_DSTORE_ENV,
		VALUE_INDEX_LOCATION
	}
	
	private ListenerList modifyListeners = new ListenerList();
	
	private DStoreServerPage fServerPage;
	private DStoreEnvironmentPage fEnvironmentPage;
	private DStoreIndexPage fIndexPage;
	
	public DStoreServerWidget(Composite parent, int style) {
		super(parent, style);
		
		setFont(parent.getFont());
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		setLayout(layout);
		setLayoutData(new GridData(GridData.FILL_BOTH));

		TabFolder folder= new TabFolder(this, SWT.NONE);
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));
		folder.setFont(this.getFont());

		fServerPage = new DStoreServerPage(folder, SWT.NONE);
		fServerPage.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				notifyListeners(e);
			}
		});
		TabItem item = new TabItem(folder, SWT.NONE);
        item.setText("Server"); //$NON-NLS-1$
        item.setControl(fServerPage);
        
        fEnvironmentPage = new DStoreEnvironmentPage(folder, SWT.NONE);
        fEnvironmentPage.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				notifyListeners(e);
			}
		});
		item = new TabItem(folder, SWT.NONE);
        item.setText("Environment"); //$NON-NLS-1$
        item.setControl(fEnvironmentPage);

        fIndexPage = new DStoreIndexPage(folder, SWT.NONE);
        fIndexPage.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				notifyListeners(e);
			}
		});
		item = new TabItem(folder, SWT.NONE);
        item.setText("Index"); //$NON-NLS-1$
        item.setControl(fIndexPage);
	}
	
	public void addModifyListener(ModifyListener listener) {
		modifyListeners.add(listener);
	}
	
	public String getDStoreCommand() {
		return fServerPage.getServerCommand();
	}

	public String getDStoreEnvironment() {
		return fEnvironmentPage.getEnvironment();
	}
	
	public String getDStoreLocation() {
		return fServerPage.getLocationPath();
	}
	
	public String getIndexLocation() {
		return fIndexPage.getLocationPath();
	}
	
	public void removeModifyListener(ModifyListener listener) {
		modifyListeners.remove(listener);
	}
	
	public void setConnection(IRemoteServices services, IRemoteConnection conn) {
		fServerPage.setConnection(services, conn);
		fIndexPage.setConnection(services, conn);
	}
	
	public void setDStoreCommand(String command) {
		fServerPage.setServerCommand(command);
	}
	
	public void setDStoreEnvironment(String env) {
		fEnvironmentPage.setEnvironment(env);
	}
	
	public void setDStoreLocation(String path) {
		fServerPage.setLocationPath(path);
	}
	
	public void setIndexLocation(String path) {
		fIndexPage.setLocationPath(path);
	}
	
	private void notifyListeners(ModifyEvent e) {
		for (Object listener : modifyListeners.getListeners()) {
			((ModifyListener)listener).modifyText(e);
		}
	}
}
