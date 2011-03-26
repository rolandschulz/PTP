/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.jaxb.ui.wizards;

import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerConfiguration;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.dialogs.ConfigurationChoiceContainer;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetBuilderUtils;
import org.eclipse.ptp.ui.wizards.IRMConfigurationWizard;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizardPage;
import org.eclipse.swt.widgets.Composite;

public class JAXBRMConfigurationSelectionWizardPage extends RMConfigurationWizardPage implements IJAXBUINonNLSConstants {

	private IJAXBResourceManagerConfiguration jaxbConfig;
	private ConfigurationChoiceContainer container;

	public JAXBRMConfigurationSelectionWizardPage(IRMConfigurationWizard wizard) {
		super(wizard, Messages.JAXBRMConfigurationSelectionWizardPage_Title);
		setTitle(Messages.JAXBRMConfigurationSelectionWizardPage_Title);
		setDescription(Messages.JAXBConfigurationWizardPage_Description);
		setPageComplete(false);
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = WidgetBuilderUtils.createComposite(parent, 1);
		container = new ConfigurationChoiceContainer(composite) {
			@Override
			protected void onUpdate() {
				setPageComplete(isValidSetting());
			}
		};
		setControl(composite);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			initContents();
		}
		super.setVisible(visible);
	}

	private void initContents() {
		jaxbConfig = (IJAXBResourceManagerConfiguration) getConfiguration();
		container.setConfig(jaxbConfig);
		container.setAvailableConfigurations();
		setPageComplete(isValidSetting());
	}

	private boolean isValidSetting() {
		String selected = container.getSelected();
		if (selected == null || selected.length() == 0) {
			return false;
		}
		return true;
	}
}
