/*******************************************************************************
 * Copyright (c) 2010 Poznan Supercomputing and Networking Center
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jan Konczak (PSNC) - initial implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.smoa.ui.wizards;

import java.util.Vector;

import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.ui.IRemoteUIServices;
import org.eclipse.ptp.remote.ui.PTPRemoteUIPlugin;
import org.eclipse.ptp.rm.smoa.core.SMOARemoteServicesFactory;
import org.eclipse.ptp.rm.smoa.core.rmsystem.SMOAResourceManagerConfiguration;
import org.eclipse.ptp.ui.wizards.IRMConfigurationWizard;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * Prompts user for selecting desired method of file transfer.
 */
public class SMOAResourceManagerFileServicesPage extends RMConfigurationWizardPage {

	private Button rbSmoa;
	private Button rbSsh;

	private Composite connectionComposite;
	private Combo rservies;
	private Combo connCombo;
	private Button newConnection;
	private Label warnSmoa;

	private final Vector<IRemoteServices> remoteServices = new Vector<IRemoteServices>();
	private IRemoteConnection[] connections;
	private int selectedRservices;
	private final SMOAResourceManagerConfiguration conf;

	public SMOAResourceManagerFileServicesPage(IRMConfigurationWizard wizard) {
		super(wizard, Messages.SMOAResourceManagerFileServicesPage_FileServicesChoice);
		setTitle(Messages.SMOAResourceManagerFileServicesPage_FileServicesChoice);
		setDescription(Messages.SMOAResourceManagerFileServicesPage_FileServicesChoice_description);
		conf = (SMOAResourceManagerConfiguration) wizard.getBaseConfiguration();
	}

	private void createChoice(Composite mainComposite) {
		final Composite typeSelection = new Composite(mainComposite, SWT.NONE);
		typeSelection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		typeSelection.setLayout(new GridLayout(2, true));

		final Label caption = new Label(typeSelection, SWT.NONE);
		caption.setText(Messages.SMOAResourceManagerFileServicesPage_ChooseFileTransferMethods);
		final GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		caption.setLayoutData(gridData);

		rbSmoa = new Button(typeSelection, SWT.RADIO);
		rbSmoa.setText(Messages.SMOAResourceManagerFileServicesPage_TextForChoosingSmoaOnly);

		rbSsh = new Button(typeSelection, SWT.RADIO);
		rbSsh.setText(Messages.SMOAResourceManagerFileServicesPage_TextForChoosingAdditionalSsh);
	}

	private void createConnectionSelector(Composite mainComposite) {
		connectionComposite = new Composite(mainComposite, SWT.NONE);
		connectionComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		connectionComposite.setLayout(new GridLayout(3, false));

		new Label(connectionComposite, SWT.NONE).setText(Messages.SMOAResourceManagerFileServicesPage_RemoteServices);

		rservies = new Combo(connectionComposite, SWT.READ_ONLY);
		final GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		rservies.setLayoutData(gridData);

		new Label(connectionComposite, SWT.NONE).setText(Messages.SMOAResourceManagerFileServicesPage_Connection);

		connCombo = new Combo(connectionComposite, SWT.READ_ONLY);
		connCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		newConnection = new Button(connectionComposite, SWT.PUSH);
		newConnection.setText(Messages.SMOAResourceManagerFileServicesPage_New);

	}

	@Override
	public void createControl(Composite parent) {
		final Composite mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		setControl(mainComposite);

		final GridLayout mainLayout = new GridLayout();
		mainComposite.setLayout(mainLayout);

		createChoice(mainComposite);

		createConnectionSelector(mainComposite);

		warnSmoa = new Label(mainComposite, SWT.NONE);
		warnSmoa.setText(Messages.SMOAResourceManagerFileServicesPage_WarningAboutUsingSmoaOnly);
		warnSmoa.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		prepareListeners();

		initValues();

		setPageComplete(isValid());
	}

	private void enableConnSelection(boolean enabled) {
		for (final Control c : connectionComposite.getChildren()) {
			c.setEnabled(enabled);
		}
	}

	private void initValues() {
		final IRemoteServices[] _ = PTPRemoteCorePlugin.getDefault().getAllRemoteServices();
		for (final IRemoteServices services : _) {
			final String name = services.getName();
			if (SMOARemoteServicesFactory.ID.equals(services.getId())) {
				continue;
			}
			remoteServices.add(services);
			rservies.add(name);
		}

		connCombo.setEnabled(false);
		newConnection.setEnabled(false);

		selectedRservices = rservies.getSelectionIndex();

		final IRemoteConnection rconn = conf.getFileRemoteConnection();
		if (rconn == null) {
			rbSmoa.setSelection(true);
			enableConnSelection(false);
		} else {
			rbSsh.setSelection(true);
			int index = remoteServices.indexOf(rconn.getRemoteServices());
			if (index != -1) {
				rservies.select(index);

				updateConnectionList();

				index = -1;
				for (int i = 0; i < connections.length; i++) {
					// Comparing connections is dangerous, RSE messes up
					if (connections[i].getName().equals(rconn.getName())) {
						index = i;
						break;
					}
				}

				connCombo.select(index);
			}
		}
	}

	boolean isValid() {
		return connCombo.getSelectionIndex() != -1 || rbSmoa.getSelection();
	}

	private void prepareListeners() {
		rbSmoa.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				enableConnSelection(false);
				warnSmoa.setVisible(true);
				conf.setFileRemoteConnection(null);
				setPageComplete(isValid());
			}
		});

		rbSsh.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				enableConnSelection(true);
				warnSmoa.setVisible(false);
				setPageComplete(isValid());
			}
		});

		rservies.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {

				if (selectedRservices == rservies.getSelectionIndex()) {
					return;
				}

				updateConnectionList();
				setPageComplete(isValid());
			}
		});

		connCombo.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				setPageComplete(isValid());

				final int index = connCombo.getSelectionIndex();
				conf.setFileRemoteConnection(connections[index]);
			}
		});

		newConnection.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				if (selectedRservices == -1) {
					return;
				}
				final IRemoteServices rseServices = remoteServices.get(selectedRservices);
				final IRemoteUIServices remUIServices = PTPRemoteUIPlugin.getDefault().getRemoteUIServices(rseServices);
				if (remUIServices == null) {
					return;
				}

				final IRemoteConnection rconn = remUIServices.getUIConnectionManager().newConnection(getShell());

				if (rconn == null) {
					return;
				}

				updateConnectionList();

				int index = -1;
				for (int i = 0; i < connections.length; i++) {
					if (connections[i].getName().equals(rconn.getName())) {
						index = i;
						break;
					}
				}
				assert index != -1;
				connCombo.select(index);

				conf.setFileRemoteConnection(rconn);

				setPageComplete(isValid());

			}
		});
	}

	private void updateConnectionList() {
		selectedRservices = rservies.getSelectionIndex();

		connCombo.removeAll();

		final int index = rservies.getSelectionIndex();
		connections = remoteServices.get(index).getConnectionManager().getConnections();

		for (final IRemoteConnection conn : connections) {
			connCombo.add(conn.getName());
		}

		connCombo.setEnabled(true);
		newConnection.setEnabled(true);
	}
}
