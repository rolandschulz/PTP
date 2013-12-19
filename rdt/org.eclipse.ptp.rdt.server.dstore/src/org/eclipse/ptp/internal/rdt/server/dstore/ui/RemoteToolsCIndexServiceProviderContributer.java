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
package org.eclipse.ptp.internal.rdt.server.dstore.ui;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.internal.rdt.server.dstore.core.RemoteToolsCIndexServiceProvider;
import org.eclipse.ptp.internal.rdt.server.dstore.messages.Messages;
import org.eclipse.ptp.internal.rdt.server.dstore.ui.DStoreServerWidget.FieldModifier;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.IServiceProviderWorkingCopy;
import org.eclipse.ptp.services.ui.IServiceProviderContributor;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.ui.widgets.RemoteConnectionWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class RemoteToolsCIndexServiceProviderContributer implements IServiceProviderContributor {

	private IServiceProviderWorkingCopy fProviderWorkingCopy;
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
	@Override
	public void configureServiceProvider(IServiceProviderWorkingCopy sp, final Composite container) {

		fProviderWorkingCopy = null;

		if (sp instanceof IServiceProviderWorkingCopy) {
			fProviderWorkingCopy = sp;
		}
		if (!(sp.getOriginal() instanceof RemoteToolsCIndexServiceProvider)) {
			throw new IllegalArgumentException(); // should never happen
		}

		container.setLayout(new GridLayout(1, false));

		final RemoteConnectionWidget remoteWidget = new RemoteConnectionWidget(container, SWT.NONE,
				Messages.RemoteToolsCIndexServiceProviderContributer_0, RemoteConnectionWidget.FLAG_FORCE_PROVIDER_SELECTION);
		remoteWidget.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		remoteWidget.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fSelectedConnection = remoteWidget.getConnection();
				if (fSelectedConnection != null) {
					fProviderWorkingCopy.putString(RemoteToolsCIndexServiceProvider.SERVICE_ID_KEY, fSelectedConnection
							.getRemoteServices().getId());
					fProviderWorkingCopy.putString(RemoteToolsCIndexServiceProvider.CONNECTION_NAME_KEY,
							fSelectedConnection.getName());
					IPath workingDir = new Path(fSelectedConnection.getWorkingDirectory());
					fProviderWorkingCopy.putString(RemoteToolsCIndexServiceProvider.INDEX_LOCATION_KEY,
							workingDir.append(".eclipsesettings").toString()); //$NON-NLS-1$
					fServerWidget.setConnection(fSelectedConnection);
				}
			}
		});
		fSelectedConnection = remoteWidget.getConnection();

		fServerWidget = new DStoreServerWidget(container, SWT.NONE);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
		data.horizontalSpan = 3;
		fServerWidget.setLayoutData(data); // set layout to grab horizontal
											// space

		/*
		 * Set connection information before updating widget with saved data.
		 */
		if (fSelectedConnection != null) {
			fServerWidget.setConnection(fSelectedConnection);
		}

		fServerWidget.addModifyListener(new ModifyListener() {
			@Override
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
	@Override
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
	@Override
	public WizardPage[] getWizardPages(IWizard wizard, IServiceProvider provider) {
		// TODO Auto-generated method stub
		return null;
	}
}
