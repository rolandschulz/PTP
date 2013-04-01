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
import org.eclipse.ptp.rdt.sync.ui.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

// Main preference page for synchronized projects - currently empty
public class SyncPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	Composite composite;
	Label message;
	
	public SyncPreferencePage() {
		super();
		super.noDefaultAndApplyButton();
	}

	@Override
	protected Control createContents(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		message = new Label(composite, SWT.NONE);
		message.setText(Messages.SyncPreferencePage_0);
		message.setLayoutData(new GridData(SWT.LEAD, SWT.TOP));
		return composite;
	}
	
	@Override
	protected Point doComputeSize() {
		Point size = message.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		composite.setSize(size);
		super.doComputeSize();
		return composite.getSize();
	}

	@Override
	public void init(IWorkbench arg0) {
		// nothing to do
	}
}