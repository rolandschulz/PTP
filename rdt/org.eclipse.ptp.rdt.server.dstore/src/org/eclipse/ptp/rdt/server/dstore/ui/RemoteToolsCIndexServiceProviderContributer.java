/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.server.dstore.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.rdt.server.dstore.core.RemoteToolsCIndexServiceProvider;
import org.eclipse.ptp.rdt.server.dstore.messages.Messages;
import org.eclipse.ptp.rdt.server.dstore.ui.DStoreServerWidget.FieldModifier;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.ptp.remote.ui.PTPRemoteUIPlugin;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.IServiceProviderWorkingCopy;
import org.eclipse.ptp.services.ui.IServiceProviderContributor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

public class RemoteToolsCIndexServiceProviderContributer implements IServiceProviderContributor {

	private IServiceProviderWorkingCopy fProviderWorkingCopy;

	private final Map<Integer, IRemoteServices> fComboIndexToRemoteServicesProviderMap = new HashMap<Integer, IRemoteServices>();
	private IRemoteServices fSelectedServices;
	private final Map<Integer, IRemoteConnection> fComboIndexToRemoteConnectionMap = new HashMap<Integer, IRemoteConnection>();
	private IRemoteConnection fSelectedConnection;
	private DStoreServerWidget fServerWidget;
	private String fConfigPath;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.services.ui.IServiceProviderContributor#
	 * configureServiceProvider(org.eclipse.ptp.services.core.IServiceProvider,
	 * org.eclipse.swt.widgets.Composite)
	 */
	/**
	 * @since 2.0
	 */
	public void configureServiceProvider(IServiceProviderWorkingCopy sp, final Composite container) {

		fProviderWorkingCopy = null;

		if (sp instanceof IServiceProviderWorkingCopy) {
			fProviderWorkingCopy = sp;
		}
		if (!(sp.getOriginal() instanceof RemoteToolsCIndexServiceProvider))
			throw new IllegalArgumentException(); // should never happen

		container.setLayout(new GridLayout(1, false));

		Group connectionGroup = new Group(container, SWT.NONE);
		connectionGroup.setText(Messages.RemoteToolsCIndexServiceProviderContributer_0);
		connectionGroup.setLayout(new GridLayout(3, false));
		connectionGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Label for "Provider:"
		Label providerLabel = new Label(connectionGroup, SWT.LEFT);
		providerLabel.setText(Messages.RemoteToolsCIndexServiceProviderContributer_1);

		// combo for providers
		final Combo providerCombo = new Combo(connectionGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
		// set layout to grab horizontal space
		GridData data = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
		data.horizontalSpan = 2;
		providerCombo.setLayoutData(data);

		populateProviderCombo(providerCombo);

		// connection combo
		// Label for "Connection:"
		Label connectionLabel = new Label(connectionGroup, SWT.LEFT);
		connectionLabel.setText(Messages.RemoteToolsCIndexServiceProviderContributer_2);

		// combo for providers
		final Combo connectionCombo = new Combo(connectionGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
		// set layout to grab horizontal space
		connectionCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// populate the combo with a list of providers
		populateConnectionCombo(connectionCombo);

		// new connection button
		final Button newConnectionButton = new Button(connectionGroup, SWT.PUSH);
		newConnectionButton.setText(Messages.RemoteToolsCIndexServiceProviderContributer_3);
		updateNewConnectionButtonEnabled(newConnectionButton);

		newConnectionButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IRemoteUIConnectionManager connectionManager = getUIConnectionManager();
				if (connectionManager != null) {
					connectionManager.newConnection(container.getShell());
				}
				// refresh list of connections
				populateConnectionCombo(connectionCombo);
			}
		});

		providerCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int selectionIndex = providerCombo.getSelectionIndex();
				fSelectedServices = fComboIndexToRemoteServicesProviderMap.get(selectionIndex);
				populateConnectionCombo(connectionCombo);
				updateNewConnectionButtonEnabled(newConnectionButton);
				fServerWidget.setConnection(fSelectedConnection);
			}
		});

		connectionCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int selectionIndex = connectionCombo.getSelectionIndex();
				fSelectedConnection = fComboIndexToRemoteConnectionMap.get(selectionIndex);
				updateNewConnectionButtonEnabled(newConnectionButton);
				fProviderWorkingCopy.putString(RemoteToolsCIndexServiceProvider.SERVICE_ID_KEY, fSelectedConnection
						.getRemoteServices().getId());
				fProviderWorkingCopy.putString(RemoteToolsCIndexServiceProvider.CONNECTION_NAME_KEY, fSelectedConnection.getName());
				IPath workingDir = new Path(fSelectedConnection.getWorkingDirectory());
				fProviderWorkingCopy.putString(RemoteToolsCIndexServiceProvider.INDEX_LOCATION_KEY,
						workingDir.append(".eclipsesettings").toString()); //$NON-NLS-1$
				fServerWidget.setConnection(fSelectedConnection);
			}
		});

		fServerWidget = new DStoreServerWidget(container, SWT.NONE);
		data = new GridData(SWT.FILL, SWT.FILL, true, false);
		data.horizontalSpan = 3;
		fServerWidget.setLayoutData(data); // set layout to grab horizontal
											// space

		/*
		 * Set connection information before updating widget with saved data.
		 */
		fServerWidget.setConnection(fSelectedConnection);

		fServerWidget.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (e.data == FieldModifier.VALUE_INDEX_LOCATION) {
					fConfigPath = fServerWidget.getIndexLocation();
					fProviderWorkingCopy.putString(RemoteToolsCIndexServiceProvider.INDEX_LOCATION_KEY, fConfigPath);
				}
			}
		});

		/*
		 * Initialize widget with default values. This will trigger ModifyEvents
		 * on the widget to update the field variables.
		 */
		fServerWidget.setIndexLocation(fProviderWorkingCopy.getString(RemoteToolsCIndexServiceProvider.INDEX_LOCATION_KEY, "")); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.services.ui.IServiceProviderContributor#getWizard(org
	 * .eclipse.ptp.services.core.IServiceProvider,
	 * org.eclipse.jface.wizard.IWizardPage)
	 */
	public IWizard getWizard(IServiceProvider provider, IWizardPage page) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.services.ui.IServiceProviderContributor#getWizardPages
	 * (org.eclipse.jface.wizard.IWizard,
	 * org.eclipse.ptp.services.core.IServiceProvider)
	 */
	public WizardPage[] getWizardPages(IWizard wizard, IServiceProvider provider) {
		// TODO Auto-generated method stub
		return null;
	}

	private IRemoteUIConnectionManager getUIConnectionManager() {
		IRemoteUIConnectionManager connectionManager = PTPRemoteUIPlugin.getDefault().getRemoteUIServices(fSelectedServices)
				.getUIConnectionManager();
		return connectionManager;
	}

	private void populateConnectionCombo(final Combo connectionCombo) {
		connectionCombo.removeAll();

		// attempt to restore settings from saved state
		IRemoteConnection connectionSelected = null;
		String serviceID = fProviderWorkingCopy.getString(RemoteToolsCIndexServiceProvider.SERVICE_ID_KEY, null);

		if (serviceID != null) {
			IRemoteServices providerSelected = PTPRemoteUIPlugin.getDefault().getRemoteServices(serviceID,
					new ProgressMonitorDialog(connectionCombo.getShell()));
			String connectionName = fProviderWorkingCopy.getString(RemoteToolsCIndexServiceProvider.CONNECTION_NAME_KEY, null);
			if (providerSelected != null && connectionName != null) {
				connectionSelected = providerSelected.getConnectionManager().getConnection(connectionName);
			}
		}

		IRemoteConnection[] connections = fSelectedServices.getConnectionManager().getConnections();
		int toSelect = 0;

		for (int k = 0; k < connections.length; k++) {
			connectionCombo.add(connections[k].getName(), k);
			fComboIndexToRemoteConnectionMap.put(k, connections[k]);
			if (connectionSelected != null && connectionSelected.getName().equals(connections[k].getName())) {
				toSelect = k;
			}
		}

		// set selected connection to be the first one if we're not restoring
		// from settings
		connectionCombo.select(toSelect);
		fSelectedConnection = fComboIndexToRemoteConnectionMap.get(toSelect);
	}

	private void populateProviderCombo(final Combo providerCombo) {
		// attempt to restore settings from saved state
		IRemoteServices providerSelected = null;
		String serviceID = fProviderWorkingCopy.getString(RemoteToolsCIndexServiceProvider.SERVICE_ID_KEY, null);
		if (serviceID != null) {
			providerSelected = PTPRemoteUIPlugin.getDefault().getRemoteServices(serviceID,
					new ProgressMonitorDialog(providerCombo.getShell()));
		}

		// populate the combo with a list of providers
		IRemoteServices[] providers = PTPRemoteUIPlugin.getDefault().getRemoteServices(
				new ProgressMonitorDialog(providerCombo.getShell()));
		int toSelect = 0;

		for (int k = 0; k < providers.length; k++) {
			providerCombo.add(providers[k].getName(), k);
			fComboIndexToRemoteServicesProviderMap.put(k, providers[k]);

			if (providerSelected != null && providerSelected.getId().compareTo(providers[k].getId()) == 0) {
				toSelect = k;
			}
		}

		// set selected host to be the first one if we're not restoring from
		// settings
		providerCombo.select(toSelect);
		fSelectedServices = fComboIndexToRemoteServicesProviderMap.get(toSelect);
	}

	private void updateNewConnectionButtonEnabled(Button button) {
		IRemoteUIConnectionManager connectionManager = getUIConnectionManager();
		button.setEnabled(connectionManager != null);
	}
}
