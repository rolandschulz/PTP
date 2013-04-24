/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.sync.cdt.ui.wizards;

import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.internal.rdt.sync.cdt.ui.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class AddSyncConfigWizardPage extends WizardPage {
	private Combo fConfigCombo;
	private String fConfigName;
	private final IProject fProject;

	public AddSyncConfigWizardPage(IProject project) {
		super("CDT AddSyncConfigWizardPage"); //$NON-NLS-1$
		fProject = project;
		setTitle(Messages.AddSyncConfigWizardPage_title); 
		setDescription(Messages.AddSyncConfigWizardPage_description); 
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Label label = new Label(composite, SWT.NONE);
		label.setText(Messages.AddSyncConfigWizardPage_Configuration); 
		fConfigCombo = new Combo(composite, SWT.READ_ONLY);
		fConfigCombo.setItems(getConfigurationNames(fProject));
		fConfigCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		fConfigCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = fConfigCombo.getSelectionIndex();
				if (index >= 0) {
					fConfigName = fConfigCombo.getItem(index);
				} else {
					fConfigName = null;
				}
				setPageComplete(validatePage());
			}

		});
		setControl(composite);
		setPageComplete(false);
	}

	private String[] getConfigurationNames(IProject project) {
		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
		if (buildInfo != null) {
			return buildInfo.getConfigurationNames();
		}
		return new String[0];
	}

	public String getBuildConfiguration() {
		return fConfigName;
	}

	private boolean validatePage() {
		if (fConfigName == null) {
			setErrorMessage(Messages.AddSyncConfigWizardPage_A_build_configuration_must_be_selected); 
			return false;
		}
		setErrorMessage(null);
		return true;
	}
}