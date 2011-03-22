/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 * Albert L. Rossi - Modified to make this functionality available inside a 
 *                   modal dialog
 *******************************************************************************/

package org.eclipse.ptp.rm.ui.wizards;

import org.eclipse.ptp.rm.ui.dialogs.ConnectionChoiceContainer;
import org.eclipse.ptp.rm.ui.dialogs.ConnectionChoiceContainer.RMDataSource;
import org.eclipse.ptp.ui.wizards.IRMConfigurationWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * Abstract base class for wizard pages used to configure remote resource
 * managers
 */
public abstract class AbstractRemoteResourceManagerConfigurationWizardPage extends AbstractConfigurationWizardPage {

	/**
	 * @since 2.0
	 */
	protected class RMConnectionChoiceContainer extends ConnectionChoiceContainer {

		protected RMConnectionChoiceContainer(AbstractRemoteResourceManagerConfigurationWizardPage page) {
			super(page);
		}

		@Override
		protected void resetErrorMessages() {
			page.resetErrorMessages();
		}
	}

	/**
	 * @since 2.0
	 */
	protected ConnectionChoiceContainer choiceContainer;

	public static final String EMPTY_STRING = ""; //$NON-NLS-1$

	public AbstractRemoteResourceManagerConfigurationWizardPage(IRMConfigurationWizard wizard, String title) {
		super(wizard, title);
		setPageComplete(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.ui.wizards.RMConfigurationWizardPage#createControl(org
	 * .eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout topLayout = new GridLayout();
		topLayout.marginHeight = 0;
		topLayout.marginWidth = 0;
		composite.setLayout(topLayout);
		Composite pageContent = getChoiceContainer().createContents(composite);
		pageContent.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
		setControl(composite);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.ui.wizards.AbstractConfigurationWizardPage#getDataSource
	 * ()
	 */
	/**
	 * @since 2.0
	 */
	@Override
	public RMDataSource getDataSource() {
		return (RMDataSource) super.getDataSource();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.ui.wizards.AbstractConfigurationWizardPage#updateControls
	 * ()
	 */
	@Override
	public void updateControls() {
		getChoiceContainer().updateControls();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ui.wizards.AbstractConfigurationWizardPage#
	 * createDataSource()
	 */
	@Override
	protected WizardPageDataSource createDataSource() {
		return getChoiceContainer().getDataSource();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.ui.wizards.AbstractConfigurationWizardPage#createListener
	 * ()
	 */
	@Override
	protected WizardPageWidgetListener createListener() {
		return getChoiceContainer().getListener();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ui.wizards.AbstractConfigurationWizardPage#
	 * doCreateContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Composite doCreateContents(Composite parent) {
		Composite contents = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginBottom = 0;
		layout.marginTop = 0;
		layout.marginRight = 0;
		layout.marginLeft = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		contents.setLayout(layout);
		getChoiceContainer().createContents(contents);
		return contents;
	}

	/**
	 * Subclasses should override this method if a new container type is used.
	 * 
	 * @since 2.0
	 */
	protected ConnectionChoiceContainer getChoiceContainer() {
		if (choiceContainer == null) {
			choiceContainer = new RMConnectionChoiceContainer(this);
		}
		return choiceContainer;
	}
}
