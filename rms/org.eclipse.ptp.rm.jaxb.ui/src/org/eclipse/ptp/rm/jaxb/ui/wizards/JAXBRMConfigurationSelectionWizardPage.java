package org.eclipse.ptp.rm.jaxb.ui.wizards;

import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerConfiguration;
import org.eclipse.ptp.rm.jaxb.ui.dialogs.ConfigurationChoiceContainer;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.ui.wizards.IRMConfigurationWizard;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class JAXBRMConfigurationSelectionWizardPage extends RMConfigurationWizardPage implements IJAXBNonNLSConstants {

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
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout topLayout = new GridLayout();
		composite.setLayout(topLayout);
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
		jaxbConfig = (IJAXBResourceManagerConfiguration) getConfigurationWizard().getConfiguration();
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
