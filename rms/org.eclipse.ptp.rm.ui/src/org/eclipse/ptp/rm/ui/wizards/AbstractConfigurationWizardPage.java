package org.eclipse.ptp.rm.ui.wizards;

import org.eclipse.ptp.ui.wizards.RMConfigurationWizard;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public abstract class AbstractConfigurationWizardPage extends
		RMConfigurationWizardPage {

	protected WizardPageWidgetListener listener = createListener();
	protected WizardPageDataSource dataSource = createDataSource();

	public AbstractConfigurationWizardPage(RMConfigurationWizard wizard, String pageName) {
		super(wizard, pageName);
	}

	/**
	 * Create listener for the wizard page. The listener must extend {@link WizardPageWidgetListener} and add
	 * specific behavior for widgets of the the preference page.
	 * @return the listener
	 */
	protected abstract WizardPageWidgetListener createListener();

	/**
	 * Create data source to handle page content. The listener must extend {@link WizardPageDataSource} and add
	 * specific behavior for widgets of the the preference page.
	 * @return the listener
	 */
	protected abstract WizardPageDataSource createDataSource();

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			resetErrorMessages();
			listener.disable();
			dataSource.setConfig(getConfigurationWizard().getConfiguration());
			dataSource.loadAndUpdate();
			listener.enable();
			updateControls();
		}
		super.setVisible(visible);
	}

	abstract protected void updateControls();

	/**
	 * Convenience method for creating a button widget.
	 *
	 * @param parent
	 * @param label
	 * @param type
	 * @return the button widget
	 */
	protected Button createButton(Composite parent, String label, int type) {
		Button button = new Button(parent, type);
		button.setText(label);
		GridData data = new GridData();
		button.setLayoutData(data);
		return button;
	}

	/**
	 * Convenience method for creating a check button widget.
	 *
	 * @param parent
	 * @param label
	 * @return the check button widget
	 */
	protected Button createCheckButton(Composite parent, String label) {
		return createButton(parent, label, SWT.CHECK | SWT.LEFT);
	}

	protected void resetErrorMessages() {
		setPageComplete(true);
		setErrorMessage(null);
		setMessage(null);
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = doCreateContents(parent);
//		doCreateContents(composite);
		setControl(composite);
	}

	abstract protected Composite doCreateContents(Composite composite);

}