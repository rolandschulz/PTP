package org.eclipse.ptp.ui.wizards;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;

public interface IRMConfigurationWizard extends IWizard {

	/**
	 * @since 5.0
	 */
	public IResourceManagerConfiguration getBaseConfiguration();

}