/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.ui;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class SyncGitPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private Combo fConnectionCombo;
	private Text fGitLocationText;
	private Button fBrowseButton;

	public SyncGitPreferencePage() {
		super();
		super.noDefaultAndApplyButton();
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		fConnectionCombo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		fConnectionCombo.setLayoutData(gd);
		fConnectionCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// handleConnectionSelected();
			}
		});

		fGitLocationText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fGitLocationText.setLayoutData(gd);
		fGitLocationText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				// update();
			}
		});

		// TODO: Fix externalized strings
		fBrowseButton = new Button(composite, SWT.PUSH);
		fBrowseButton.setText("Browse..."); //$NON-NLS-1$
		fBrowseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// What to do?
			}
		});
		
		return composite;
	}

	@Override
	public void init(IWorkbench arg0) {
		// nothing to do
	}
}
