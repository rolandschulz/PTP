package org.eclipse.ptp.rm.ui.wizards;

import org.eclipse.ptp.rm.ui.utils.DataSource;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;

public abstract class WizardPageDataSource extends DataSource {
	private AbstractConfigurationWizardPage page;
	private IResourceManagerConfiguration config;

	protected WizardPageDataSource(AbstractConfigurationWizardPage page) {
		this.page = page;
	}

	@Override
	protected void setError(ValidationException e) {
		page.setErrorMessage(e.getLocalizedMessage());
		page.setPageComplete(false);
	}

	@Override
	protected void update() {
		page.updateControls();
	}

	public IResourceManagerConfiguration getConfig() {
		return config;
	}

	public void setConfig(IResourceManagerConfiguration config) {
		this.config = config;
	}
}
