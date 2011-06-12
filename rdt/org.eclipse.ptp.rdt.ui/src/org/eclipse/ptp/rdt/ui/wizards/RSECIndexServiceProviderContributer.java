/*******************************************************************************
 * Copyright (c) 2009, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.ui.wizards;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.internal.rdt.core.serviceproviders.AbstractRemoteCIndexServiceProvider;
import org.eclipse.ptp.rdt.ui.messages.Messages;
import org.eclipse.ptp.rdt.ui.serviceproviders.RSECIndexServiceProvider;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.IServiceProviderWorkingCopy;
import org.eclipse.ptp.services.ui.IServiceProviderContributor;
import org.eclipse.rse.connectorservice.dstore.DStoreConnectorService;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.SystemStartHere;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.ui.wizards.newconnection.RSEDefaultNewConnectionWizard;
import org.eclipse.rse.ui.wizards.newconnection.RSEMainNewConnectionWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

/**
 * @since 2.0
 */
public class RSECIndexServiceProviderContributer implements IServiceProviderContributor {

	private IServiceProviderWorkingCopy fProviderWorkingCopy;

	private final Map<Integer, IHost> hostComboIndexToHostMap = new HashMap<Integer, IHost>();

	private IHost selectedHost;
	private String configPath;

	/**
	 * @since 3.1
	 */
	public void configureServiceProvider(IServiceProviderWorkingCopy sp, final Composite container) {
		//The UI components works with IServiceProviderWorkingCopy and not the original IServiceProvider
		
		fProviderWorkingCopy = null;
		if (sp instanceof IServiceProviderWorkingCopy) {
			fProviderWorkingCopy = (IServiceProviderWorkingCopy) sp;
		}

		if (!(sp.getOriginal() instanceof RSECIndexServiceProvider)) {
			throw new IllegalArgumentException(); // should never happen
		}

		container.setLayout(new GridLayout(1, false));

		Group connectionGroup = new Group(container, SWT.NONE);
		connectionGroup.setText(Messages.getString("RSECIndexServiceProviderContributer.ConnectionGroupName")); //$NON-NLS-1$
		connectionGroup.setLayout(new GridLayout(1, false));
		connectionGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Label for "Host:"
		Label hostLabel = new Label(connectionGroup, SWT.LEFT);
		hostLabel.setLayoutData(new GridData());
		hostLabel.setText(Messages.getString("HostSelectionDialog_0")); //$NON-NLS-1$

		// combo for hosts
		final Combo hostCombo = new Combo(connectionGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
		hostCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false)); // set
																				// layout
																				// to
																				// grab
																				// horizontal
																				// space

		// attempt to restore settings from saved state
		String hostName = fProviderWorkingCopy.getString(AbstractRemoteCIndexServiceProvider.HOST_NAME_KEY, null);

		// populate the combo with a list of hosts
		IHost[] hosts = SystemStartHere.getConnections();
		int toSelect = 0;

		for (int k = 0; k < hosts.length; k++) {
			hostCombo.add(hosts[k].getAliasName(), k);
			hostComboIndexToHostMap.put(k, hosts[k]);

			if (hostName != null && hostName.compareTo(hosts[k].getAliasName()) == 0) {
				toSelect = k;
			}
		}

		// set selected host to be the first one if we're not restoring from
		// settings
		hostCombo.select(toSelect);
		selectedHost = hostComboIndexToHostMap.get(toSelect);

		// button for creating new connections
		Button newConnectionButton = new Button(connectionGroup, SWT.PUSH);
		newConnectionButton.setLayoutData(new GridData());
		newConnectionButton.setText(Messages.getString("HostSelectionDialog.0")); //$NON-NLS-1$
		newConnectionButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// launch the RSE New Connection Wizard
				RSEMainNewConnectionWizard wizard = new RSEMainNewConnectionWizard();
				WizardDialog wizardDialog = new WizardDialog(container.getShell(), wizard);
				wizardDialog.open();

				IWizard actualWizard = wizard.getSelectedWizard();
				if (actualWizard instanceof RSEDefaultNewConnectionWizard) {
					// get the new host, if any
					IHost host = ((RSEDefaultNewConnectionWizard) actualWizard).getCreatedHost();

					// add the host
					int index = hostCombo.getItemCount() - 1;
					hostCombo.add(host.getAliasName(), index);
					hostComboIndexToHostMap.put(index, host);

					// select the new host
					hostCombo.select(index);
					selectedHost = host;
					updateProvider();
				}
			}

		});

		configPath = fProviderWorkingCopy.getString(AbstractRemoteCIndexServiceProvider.INDEX_LOCATION_KEY, ""); //$NON-NLS-1$

		final IndexFileLocationWidget scopeWidget = new IndexFileLocationWidget(container, SWT.NONE, selectedHost, configPath);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
		data.horizontalSpan = 3;
		scopeWidget.setLayoutData(data); // set layout to grab horizontal space
		scopeWidget.addPathListener(new IIndexFilePathChangeListener() {
			public void pathChanged(String newPath) {
				configPath = newPath;
				updateProvider();
			}
		});

		hostCombo.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				int selectionIndex = hostCombo.getSelectionIndex();
				selectedHost = hostComboIndexToHostMap.get(selectionIndex);
				scopeWidget.setHost(selectedHost);
				updateProvider();
			}
		});

		updateProvider();
	}

	private void updateProvider() {
		fProviderWorkingCopy.putString(AbstractRemoteCIndexServiceProvider.HOST_NAME_KEY, selectedHost.getAliasName());
		fProviderWorkingCopy.putString(AbstractRemoteCIndexServiceProvider.INDEX_LOCATION_KEY, configPath);
	}

	private IConnectorService getDStoreConnectorService(IHost host) {
		for (IConnectorService cs : host.getConnectorServices()) {
			if (cs instanceof DStoreConnectorService)
				return cs;
		}
		return null;
	}

	public IWizard getWizard(IServiceProvider provider, IWizardPage page) {
		// TODO Auto-generated method stub
		return null;
	}

	public WizardPage[] getWizardPages(IWizard wizard, IServiceProvider provider) {
		// TODO Auto-generated method stub
		return null;
	}

}
