/*******************************************************************************
 * Copyright (c) 2012 University of Illinois and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jeff Overbey (Illinois) - Initial API and implementation
 *    John Eblen (ORNL) - Altered to handle multiple configurations and save them
 *                        inside synchronized project's core (bug 393244)
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.ui.properties;

import java.net.URI;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.ui.properties.AbstractSingleBuildPage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.ems.core.EnvManagerConfigMap;
import org.eclipse.ptp.ems.ui.EnvManagerConfigWidget;
import org.eclipse.ptp.ems.ui.IErrorListener;
import org.eclipse.ptp.rdt.sync.core.BuildConfigurationManager;
import org.eclipse.ptp.rdt.sync.core.BuildScenario;
import org.eclipse.ptp.rdt.sync.core.MissingConnectionException;
import org.eclipse.ptp.rdt.sync.core.SyncManager;
import org.eclipse.ptp.rdt.sync.ui.RDTSyncUIPlugin;
import org.eclipse.ptp.rdt.sync.ui.messages.Messages;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * The Environment Management property page, which is available under the C/C++ Build category for synchronized remote projects.
 * 
 * @author Jeff Overbey
 */
public final class EnvManagerPropertiesPage extends AbstractSingleBuildPage {
	private EnvManagerConfigWidget ui = null;
	private final Map<String, EnvManagerConfigMap> configToPropertiesMap = new HashMap<String, EnvManagerConfigMap>();
	// This set consists of configs that are successfully loaded in "loadSettings". Ideally, it would only consist of changed
	// configs, but the widgets do not currently support listening for modifications.
	private final Set<IConfiguration> configsToSave = new HashSet<IConfiguration>();
	private IConfiguration configBeforeSwitched = null;
	private boolean widgetsReady = false;

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
			public void errorRaised(String message) {
				setErrorMessage(message);
			}

			@Override
			public void errorCleared() {
				setErrorMessage(null);
			}
		});

		IConfiguration initialConfig = getCfg();
		EnvManagerConfigMap initialMap = this.loadSettings(initialConfig);
		this.ui.setUseEMSCheckbox(initialMap.isEnvMgmtEnabled());
		this.ui.setManualConfigCheckbox(initialMap.isManualConfigEnabled());
		this.ui.setManualConfigText(initialMap.getManualConfigText());
		this.ui.configurationChanged(getSyncURI(), remoteConnection, initialMap.getConfigElements());
		configBeforeSwitched = initialConfig;
		widgetsReady = true;
	}

	private IRemoteConnection getConnection() {
		if (getControl().isDisposed()) {
			return null;
		}

		final BuildConfigurationManager bcm = BuildConfigurationManager.getInstance();
		final BuildScenario bs = bcm.getBuildScenarioForBuildConfiguration(getCfg());
		if (bs == null) {
			return null;
		}

		try {
			return bs.getRemoteConnection();
		} catch (MissingConnectionException e) {
			return null;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractSinglePage#cfgChanged(org.eclipse.cdt.core.settings.model.ICConfigurationDescription)
	 */
	@Override
	protected void cfgChanged(ICConfigurationDescription cfgd) {
		super.cfgChanged(cfgd);
		// This method is called before createWidgets, so ignore this initial call.
		if (widgetsReady == false) {
			return;
		}
		// Update settings for previous configuration first
		this.storeSettings(configBeforeSwitched);
		configBeforeSwitched = getCfg();		
		
		if (ui != null) {
			IRemoteConnection connection = getConnection();
			Set<String> settings = this.loadSettings(getCfg()).getConfigElements();
			ui.configurationChanged(getSyncURI(), connection, settings);
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
		this.performOk();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractSinglePage#performOk()
	 */
	@Override
	// Slight modification of same function from BuildRemotePropertiesPage
	public boolean performOk() {
		if (!super.performOk()) {
			return false;
		}
		if (widgetsReady == false) {
			return true;
		}

		// Disable sync auto while changing config files but make sure the previous setting is restored before exiting.
		boolean syncAutoSetting = SyncManager.getSyncAuto();
		SyncManager.setSyncAuto(false);
		try {
			// Don't forget to save changes made to the current configuration before proceeding
			this.storeSettings(configBeforeSwitched);
			for (IConfiguration config : configsToSave) {
				BuildConfigurationManager bcm = BuildConfigurationManager.getInstance();
				bcm.setEnvProperties(config, configToPropertiesMap.get(config.getId()).getAllProperties());
			}
		} catch (CoreException e) {
			IStatus status = new Status(IStatus.ERROR, RDTSyncUIPlugin.PLUGIN_ID, Messages.EnvManagerPropertiesPage_0, e);
			StatusManager.getManager().handle(status, StatusManager.SHOW);
			return false;
		} finally {
			SyncManager.setSyncAuto(syncAutoSetting);
		}
		return true;
	}

	// Load configuration properties to temporary storage
	// Never returns null - returns empty map on error reading data.
	private EnvManagerConfigMap loadSettings(IConfiguration config) {
		if (!configToPropertiesMap.containsKey(config.getId())) {
			BuildConfigurationManager bcm = BuildConfigurationManager.getInstance();
			try {
				configToPropertiesMap.put(config.getId(), new EnvManagerConfigMap(bcm.getEnvProperties(config)));
			} catch (CoreException e) {
				IStatus status = new Status(IStatus.ERROR, RDTSyncUIPlugin.PLUGIN_ID, Messages.EnvManagerPropertiesPage_0, e);
				StatusManager.getManager().handle(status, StatusManager.SHOW);
				return new EnvManagerConfigMap();
			}
		}
		return configToPropertiesMap.get(config.getId());
	}

	// Save configuration properties to temporary storage
	private void storeSettings(IConfiguration config) {
		EnvManagerConfigMap map = this.loadSettings(config);
		EnvManagerConfigMap mapCopy = new EnvManagerConfigMap(map);
		ui.saveConfiguration(map);
		if (!map.equals(mapCopy)) {
			configsToSave.add(config);
		}
	}
}
