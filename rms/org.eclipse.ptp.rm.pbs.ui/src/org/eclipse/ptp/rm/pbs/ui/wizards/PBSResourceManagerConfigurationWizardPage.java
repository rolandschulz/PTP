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

import org.eclipse.ptp.rm.pbs.core.rmsystem.IPBSResourceManagerConfiguration;
import org.eclipse.ptp.rm.pbs.ui.IPBSNonNLSConstants;
import org.eclipse.ptp.rm.pbs.ui.messages.Messages;
import org.eclipse.ptp.rm.pbs.ui.utils.WidgetUtils;
import org.eclipse.ptp.rm.ui.wizards.AbstractRemoteProxyResourceManagerConfigurationWizardPage;
import org.eclipse.ptp.ui.wizards.IRMConfigurationWizard;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

/**
 * Class should also provide for choice of configuration file to deploy with
 * proxy.
 * 
 * 2010-11-03: currently just for demo purposes.
 * 
 * @author arossi
 * 
 */
public final class PBSResourceManagerConfigurationWizardPage extends AbstractRemoteProxyResourceManagerConfigurationWizardPage
		implements IPBSNonNLSConstants {

	private String[] types;
	private Combo proxyTypes;
	private IPBSResourceManagerConfiguration pbsConfig;

	public PBSResourceManagerConfigurationWizardPage(IRMConfigurationWizard wizard) {
		super(wizard, Messages.PBSResourceManagerConfigurationWizardPage_name);
		setTitle(Messages.PBSResourceManagerConfigurationWizardPage_title);
		setDescription(Messages.PBSResourceManagerConfigurationWizardPage_description);
		proxyPathEnabled = false;
		fManualLaunchEnabled = false;
		setAvailableConfigurations();
	}

	@Override
	public boolean performOk() {
		pbsConfig.setProxyConfiguration(proxyTypes.getText());
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
		pbsConfig = (IPBSResourceManagerConfiguration) config;
		String proxyType = pbsConfig.getProxyConfiguration();
		if (proxyType != null && proxyType.length() != 0)
			for (int i = 0; i < types.length; i++)
				if (proxyType.equals(types[i])) {
					proxyTypes.select(i);
					break;
				}
	}

	@Override
	protected boolean isValidSetting() {
		String choice = proxyTypes.getText();
		if (choice == null || choice.length() == 0)
			return false;
		return super.isValidSetting();
	}

	/*
	 * For now, uses hardcoded types. Will need to search resource(s) to find
	 * current available proxy configurations.
	 */
	private void setAvailableConfigurations() {
		types = new String[] { "PBS-Torque", "PBS-Pro" };//$NON-NLS-1$ //$NON-NLS-2$
	}
}
