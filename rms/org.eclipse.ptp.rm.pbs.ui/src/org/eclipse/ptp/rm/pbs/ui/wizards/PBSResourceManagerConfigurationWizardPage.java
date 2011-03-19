/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Albert L. Rossi - modifications to expose proxy type configuration (2010/11/04)
 ******************************************************************************/
package org.eclipse.ptp.rm.pbs.ui.wizards;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.rm.pbs.core.IPBSNonNLSConstants;
import org.eclipse.ptp.rm.pbs.core.rmsystem.PBSResourceManagerConfiguration;
import org.eclipse.ptp.rm.pbs.ui.PBSUIPlugin;
import org.eclipse.ptp.rm.pbs.ui.messages.Messages;
import org.eclipse.ptp.rm.pbs.ui.utils.WidgetUtils;
import org.eclipse.ptp.rm.ui.wizards.AbstractRemoteProxyResourceManagerConfigurationWizardPage;
import org.eclipse.ptp.ui.wizards.IRMConfigurationWizard;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.osgi.framework.Bundle;

/**
 * Class provides for choice of configuration file to deploy with proxy.
 * 
 * @author arossi
 * 
 */
public final class PBSResourceManagerConfigurationWizardPage extends AbstractRemoteProxyResourceManagerConfigurationWizardPage
		implements IPBSNonNLSConstants {

	private String[] types;
	private Combo proxyTypes;
	private final Properties proxyConfigs;
	private PBSResourceManagerConfiguration pbsConfig;

	public PBSResourceManagerConfigurationWizardPage(IRMConfigurationWizard wizard) {
		super(wizard, Messages.PBSResourceManagerConfigurationWizardPage_name);
		setTitle(Messages.PBSResourceManagerConfigurationWizardPage_title);
		setDescription(Messages.PBSResourceManagerConfigurationWizardPage_description);
		proxyPathEnabled = false;
		fManualLaunchEnabled = false;
		proxyConfigs = new Properties();
		setAvailableConfigurations();
	}

	@Override
	public boolean performOk() {
		pbsConfig.setProxyConfiguration(proxyConfigs.getProperty(proxyTypes.getText()));
		return super.performOk();
	}

	@Override
	protected void addCustomWidgets(Composite parent) {
		proxyTypes = WidgetUtils.createItemCombo(parent, Messages.PBSProxyConfigComboTitle, types, ZEROSTR, ZEROSTR, true, null, 2);
		proxyTypes.addSelectionListener(listener);
	}

	@Override
	protected void initContents() {
		super.initContents();
		pbsConfig = (PBSResourceManagerConfiguration) getConfiguration();
		String proxyPath = pbsConfig.getProxyConfiguration();
		if (proxyPath != null && proxyPath.length() != 0) {
			for (int i = 0; i < types.length; i++) {
				String path = proxyConfigs.getProperty(types[i]);
				if (proxyPath.equals(path)) {
					proxyTypes.select(i);
					break;
				}
			}
		}
	}

	@Override
	protected boolean isValidSetting() {
		String choice = proxyTypes.getText();
		if (choice == null || choice.length() == 0) {
			return false;
		}
		return super.isValidSetting();
	}

	private void getAvailableConfigurations() throws IOException {
		proxyConfigs.clear();
		URL url = null;
		if (PBSUIPlugin.getDefault() != null) {
			Bundle bundle = PBSUIPlugin.getDefault().getBundle();
			url = FileLocator.find(bundle, new Path(SRC + PATH_SEP + RM_CONFIG_PROPS), null);
		} else {
			url = new File(RM_CONFIG_PROPS).toURL();
		}

		if (url == null) {
			return;
		}
		InputStream s = null;
		try {
			s = url.openStream();
			proxyConfigs.load(s);
		} finally {
			try {
				if (s != null) {
					s.close();
				}
			} catch (IOException e) {
			}
		}
	}

	private void setAvailableConfigurations() {
		try {
			getAvailableConfigurations();
			types = proxyConfigs.keySet().toArray(new String[0]);
		} catch (IOException t) {
			t.printStackTrace();
			types = new String[0];
		}
	}
}
