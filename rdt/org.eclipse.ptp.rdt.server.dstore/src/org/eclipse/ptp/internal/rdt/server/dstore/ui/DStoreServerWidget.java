/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.server.dstore.ui;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class DStoreServerWidget extends Composite {
	public enum FieldModifier {
		VALUE_DSTORE_LOCATION, VALUE_DSTORE_COMMAND, VALUE_DSTORE_ENV, VALUE_INDEX_LOCATION
	}

	private final ListenerList modifyListeners = new ListenerList();

	private final DStoreIndexWidget fIndexPage;

	public DStoreServerWidget(Composite parent, int style) {
		super(parent, style);

		setFont(parent.getFont());
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		setLayout(layout);
		setLayoutData(new GridData(GridData.FILL_BOTH));

		fIndexPage = new DStoreIndexWidget(this, SWT.NONE);
		fIndexPage.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				notifyListeners(e);
			}
		});
	}

	public void addModifyListener(ModifyListener listener) {
		modifyListeners.add(listener);
	}

	public String getIndexLocation() {
		return fIndexPage.getLocationPath();
	}

	public void removeModifyListener(ModifyListener listener) {
		modifyListeners.remove(listener);
	}

	public void setConnection(IRemoteConnection conn) {
		fIndexPage.setConnection(conn);
	}

	public void setIndexLocation(String path) {
		fIndexPage.setLocationPath(path);
	}

	private void notifyListeners(ModifyEvent e) {
		for (Object listener : modifyListeners.getListeners()) {
			((ModifyListener) listener).modifyText(e);
		}
	}
}
