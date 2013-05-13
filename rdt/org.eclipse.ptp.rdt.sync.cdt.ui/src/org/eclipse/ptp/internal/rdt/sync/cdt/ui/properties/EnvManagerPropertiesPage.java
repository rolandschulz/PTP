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
package org.eclipse.ptp.internal.rdt.sync.cdt.ui.properties;

import java.net.URI;
import java.util.List;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.managedbuilder.ui.properties.AbstractSingleBuildPage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.ems.core.EnvManagerProjectProperties;
import org.eclipse.ptp.ems.ui.EnvManagerConfigWidget;
import org.eclipse.ptp.ems.ui.IErrorListener;
import org.eclipse.ptp.internal.rdt.sync.cdt.ui.Activator;
import org.eclipse.ptp.rdt.sync.core.SyncConfig;
import org.eclipse.ptp.rdt.sync.core.SyncConfigManager;
import org.eclipse.ptp.rdt.sync.core.exceptions.MissingConnectionException;
import org.eclipse.ptp.remote.core.IRemoteConnection;
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
	 * @see org.eclipse.cdt.ui.newui.AbstractSinglePage#cfgChanged(org.eclipse.cdt.core.settings.model.ICConfigurationDescription)
	 */
	@Override
	protected void cfgChanged(ICConfigurationDescription cfgd) {
		super.cfgChanged(cfgd);
		if (ui != null) {
			IRemoteConnection connection = getConnection();
			ui.configurationChanged(getSyncURI(), connection, computeSelectedItems());
		}
	}

	private List<String> computeSelectedItems() {
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
	 * @see org.eclipse.cdt.ui.newui.AbstractSinglePage#createWidgets(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createWidgets(Composite parent) {
		final IRemoteConnection remoteConnection = getConnection();

		this.ui = new EnvManagerConfigWidget(parent, remoteConnection);
		this.ui.setErrorListener(new IErrorListener() {
			@Override
			public void errorCleared() {
				setErrorMessage(null);
			}

			@Override
			public void errorRaised(String message) {
				setErrorMessage(message);
			}
		});

		this.ui.setUseEMSCheckbox(isEnvConfigSupportEnabled());
		this.ui.setManualConfigCheckbox(isManualConfigEnabled());
		this.ui.setManualConfigText(getManualConfigText());
		this.ui.configurationChanged(getSyncURI(), remoteConnection, computeSelectedItems());
	}

	private IRemoteConnection getConnection() {
		if (getControl().isDisposed()) {
			return null;
		}

		SyncConfig config = SyncConfigManager.getActive(getProject());
		if (config != null) {
			try {
				return config.getRemoteConnection();
			} catch (MissingConnectionException e) {
				// Return null anyway
			}
		}
		return null;
	}

	private String getManualConfigText() {
		try {
			return getProjectProperties().getManualConfigText();
		} catch (final Error e) {
			return ""; //$NON-NLS-1$
		}
	}

	private EnvManagerProjectProperties getProjectProperties() {
		try {
			return new EnvManagerProjectProperties(getProject());
		} catch (final Error e) {
			setErrorMessage(e.getClass().getSimpleName() + ": " + e.getLocalizedMessage()); //$NON-NLS-1$
			Activator.log(e);
			throw e;
		}
	}

	private URI getSyncURI() {
		try {
			return SyncConfigManager.getActiveSyncLocationURI(getProject());
		} catch (CoreException e) {
			setErrorMessage(e.getClass().getSimpleName() + ": " + e.getLocalizedMessage()); //$NON-NLS-1$
			Activator.log(e);
		}
		return null;
	}

	private boolean isEnvConfigSupportEnabled() {
		try {
			return getProjectProperties().isEnvMgmtEnabled();
		} catch (final Error e) {
			return false;
		}
	}

	private boolean isManualConfigEnabled() {
		try {
			return getProjectProperties().isManualConfigEnabled();
		} catch (final Error e) {
			return false;
		}
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
			Activator.log(e);
			setErrorMessage(e.getClass().getSimpleName() + ": " + e.getLocalizedMessage()); //$NON-NLS-1$
		}
	}
}
