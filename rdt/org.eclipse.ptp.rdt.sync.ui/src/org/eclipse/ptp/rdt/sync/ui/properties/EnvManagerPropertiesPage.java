/*******************************************************************************
 * Copyright (c) 2012 University of Illinois and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jeff Overbey (Illinois) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.ui.properties;

import java.net.URI;
import java.util.Set;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.managedbuilder.ui.properties.AbstractSingleBuildPage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.ems.core.EnvManagerProjectProperties;
import org.eclipse.ptp.ems.ui.EnvManagerConfigWidget;
import org.eclipse.ptp.ems.ui.IErrorListener;
import org.eclipse.ptp.rdt.core.serviceproviders.IRemoteExecutionServiceProvider;
import org.eclipse.ptp.rdt.core.services.IRDTServiceConstants;
import org.eclipse.ptp.rdt.sync.core.BuildConfigurationManager;
import org.eclipse.ptp.rdt.sync.core.RDTSyncCorePlugin;
import org.eclipse.ptp.rdt.sync.core.services.IRemoteSyncServiceConstants;
import org.eclipse.ptp.rdt.sync.ui.RDTSyncUIPlugin;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.swt.widgets.Composite;

/**
 * The Environment Management property page, which is available under the C/C++ Build category for synchronized remote projects.
 * 
 * @author Jeff Overbey
 */
public final class EnvManagerPropertiesPage extends AbstractSingleBuildPage {

	private EnvManagerConfigWidget ui = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractSinglePage#createWidgets(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createWidgets(Composite parent) {
		final IRemoteExecutionServiceProvider executionProvider = getRemoteServicesExecutionProvider();
		final IRemoteConnection remoteConnection = executionProvider == null ? null : executionProvider.getConnection();

		this.ui = new EnvManagerConfigWidget(parent, remoteConnection);
		this.ui.setErrorListener(new IErrorListener() {
			public void errorRaised(String message) {
				setErrorMessage(message);
			}
			public void errorCleared() {
				setErrorMessage(null);
			}
		});

		this.ui.setUseEMSCheckbox(isEnvConfigSupportEnabled());
		this.ui.configurationChanged(getSyncURI(), remoteConnection, computeSelectedItems());
	}

	private IRemoteExecutionServiceProvider getRemoteServicesExecutionProvider() {
		if (getControl().isDisposed()) {
			return null;
		}

		final BuildConfigurationManager bcm = BuildConfigurationManager.getInstance();
		final IServiceConfiguration serviceConfig = bcm.getConfigurationForBuildConfiguration(getCfg());
		if (serviceConfig == null) {
			return null;
		}

		final ServiceModelManager smm = ServiceModelManager.getInstance();
		final IService syncService = smm.getService(IRemoteSyncServiceConstants.SERVICE_SYNC);
		if (syncService == null || serviceConfig.isDisabled(syncService)) {
			return null;
		}

		final IService buildService = smm.getService(IRDTServiceConstants.SERVICE_BUILD);
		if (buildService == null) {
			return null;
		}

		final IServiceProvider provider = serviceConfig.getServiceProvider(buildService);
		if (!(provider instanceof IRemoteExecutionServiceProvider)) {
			return null;
		}

		return (IRemoteExecutionServiceProvider) provider;
	}

	private boolean isEnvConfigSupportEnabled() {
		try {
			return new EnvManagerProjectProperties(getProject()).isEnvMgmtEnabled();
		} catch (final Error e) {
			setErrorMessage(e.getClass().getSimpleName() + ": " + e.getLocalizedMessage()); //$NON-NLS-1$
			RDTSyncUIPlugin.log(e);
			return false;
		}
	}

	private URI getSyncURI() {
		final BuildConfigurationManager bcm = BuildConfigurationManager.getInstance();
		try {
			return bcm.getSyncLocationURI(getCfg(), getCfg().getOwner().getProject());
		} catch (final CoreException e) {
			setErrorMessage(e.getClass().getSimpleName() + ": " + e.getLocalizedMessage()); //$NON-NLS-1$
			RDTSyncUIPlugin.log(e);
			return null;
		}
	}

	private Set<String> computeSelectedItems() {
		try {
			final EnvManagerProjectProperties projectProperties = new EnvManagerProjectProperties(getProject());
			if (projectProperties.getConnectionName().equals(ui.getConnectionName())) {
				return projectProperties.getConfigElements();
			} else {
				// If the stored connection name is different,
				// then the stored list of modules is probably for a different machine,
				// so don't try to select those modules, since they're probably incomplete or invalid for this connection
				return null; // Revert to default selection
			}
		} catch (final Error e) {
			return null; // Revert to default selection
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractSinglePage#cfgChanged(org.eclipse.cdt.core.settings.model.ICConfigurationDescription)
	 */
	@Override
	protected void cfgChanged(ICConfigurationDescription cfgd) {
		super.cfgChanged(cfgd);
		if (ui != null) {
			final IRemoteExecutionServiceProvider executionProvider = getRemoteServicesExecutionProvider();
			ui.configurationChanged(getSyncURI(), executionProvider.getConnection(), computeSelectedItems());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractSinglePage#performDefaults()
	 */
	@Override
	public void performDefaults() {
		if (ui != null) {
			ui.setUseEMSCheckbox(false);
		}
		super.performDefaults();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractSinglePage#performApply(org.eclipse.cdt.core.settings.model.ICResourceDescription,
	 * org.eclipse.cdt.core.settings.model.ICResourceDescription)
	 */
	@Override
	protected void performApply(ICResourceDescription src, ICResourceDescription dst) {
		storeProjectProperties();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractSinglePage#performOk()
	 */
	@Override
	public boolean performOk() {
		storeProjectProperties();
		return super.performOk();
	}

	private void storeProjectProperties() {
		try {
			final EnvManagerProjectProperties projectProperties = new EnvManagerProjectProperties(getProject());
			ui.saveConfiguration(projectProperties);
		} catch (final Error e) {
			RDTSyncCorePlugin.log(e);
			setErrorMessage(e.getClass().getSimpleName() + ": " + e.getLocalizedMessage()); //$NON-NLS-1$
		}
	}
}
