/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rm.ui.wizards;

import org.eclipse.ptp.ui.wizards.IRMConfigurationWizard;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * 
 * @author Daniel Felix Ferber
 * 
 */
public abstract class AbstractConfigurationWizardPage extends RMConfigurationWizardPage {

	private final WizardPageWidgetListener listener = createListener();
	private final WizardPageDataSource dataSource = createDataSource();

	public AbstractConfigurationWizardPage(IRMConfigurationWizard wizard, String pageName) {
		super(wizard, pageName);
	}

	/**
	 * Create listener for the wizard page. The listener must extend
	 * {@link WizardPageWidgetListener} and add specific behavior for widgets of
	 * the the preference page.
	 * 
	 * @return the listener
	 */
	protected abstract WizardPageWidgetListener createListener();

	/**
	 * Create data source to handle page content. The listener must extend
	 * {@link WizardPageDataSource} and add specific behavior for widgets of the
	 * the preference page.
	 * 
	 * @return the listener
	 */
	protected abstract WizardPageDataSource createDataSource();

	protected WizardPageWidgetListener getWidgetListener() {
		return listener;
	}

	public WizardPageDataSource getDataSource() {
		return dataSource;
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			resetErrorMessages();
			listener.disable();
			dataSource.setConfiguration(getConfiguration());
			dataSource.loadAndUpdate();
			listener.enable();
			updateControls();
		}
		super.setVisible(visible);
	}

	abstract public void updateControls();

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
		listener.disable();
		Composite composite = doCreateContents(parent);
		setControl(composite);
		listener.enable();
	}

	protected abstract Composite doCreateContents(Composite composite);

}