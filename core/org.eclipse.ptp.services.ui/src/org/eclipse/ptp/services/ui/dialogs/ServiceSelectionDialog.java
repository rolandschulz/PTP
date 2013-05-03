/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.services.ui.dialogs;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.ptp.internal.services.ui.messages.Messages;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * Dialog that displays a list of available services and allows the user
 * to select one or more. The selected services are available by calling
 * {@link #getSelectedServices()}.
 */
public class ServiceSelectionDialog extends TitleAreaDialog {
	protected static final String SERVICE_KEY = "service-id"; //$NON-NLS-1$

	private IService[] fSelectedServices = new IService[0];
	private Table fTable;
	private IService[] fServices;
	
	public ServiceSelectionDialog(Shell parentShell, IService[] services) {
		super(parentShell);
		this.fServices = services;
		setShellStyle(getShellStyle() | SWT.SHEET);
	}
	
	public IService[] getSelectedServices() {
		return fSelectedServices;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
		GridLayout layout = new GridLayout(1, false);
		container.setLayout(layout);

		setTitle(Messages.ServiceSelectionDialog_0);
		setMessage(Messages.ServiceSelectionDialog_1);

		fTable = new Table(container, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		fTable.setLinesVisible(false);
		fTable.setHeaderVisible(false);
		fTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		fTable.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				TableItem[] items = fTable.getSelection();
				if (items.length == 0) {
					return;
				}
				fSelectedServices = new IService[items.length];
				for (int index = 0; index < items.length; index++) {
					fSelectedServices[index] = (IService) items[index].getData(SERVICE_KEY);
				}
			}
		});

		createTableContent();
		
		fTable.setVisible(true);
		return container;
	}
	
	private void createTableContent() {
		fTable.removeAll();
		
		for (IService service : fServices) {
			TableItem item = new TableItem (fTable, SWT.NONE);
			item.setText (0, service.getName());
			item.setData(SERVICE_KEY, service);
		}
	}
}
