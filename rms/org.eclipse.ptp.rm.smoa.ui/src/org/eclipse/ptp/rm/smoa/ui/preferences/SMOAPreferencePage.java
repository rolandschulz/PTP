/*******************************************************************************
 * Copyright (c) 2010 Poznan Supercomputing and Networking Center
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jan Konczak (PSNC) - initial implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.smoa.ui.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.ptp.rm.smoa.core.rmsystem.PoolingIntervalsAndStatic;
import org.eclipse.ptp.rm.smoa.ui.SMOAUIPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Represents preferences page for SMOA Computing plug-in.
 * 
 * Currently only pooling intervals are set up here.
 */
public class SMOAPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	private Composite parent;
	private Spinner taskInterval;
	private Spinner outInterval;

	public SMOAPreferencePage() {
	}

	@Override
	protected Control createContents(Composite arg0) {
		parent = new Composite(arg0, SWT.NONE);

		final GridLayout layout = new GridLayout(2, false);
		parent.setLayout(layout);

		final IPreferenceStore store = SMOAUIPlugin.getDefault().getPreferenceStore();

		// Task
		Label label = new Label(parent, SWT.NONE);
		label.setText(Messages.SMOAPreferencePage_TaskPooling);
		label.setLayoutData(new GridData());

		taskInterval = new Spinner(parent, SWT.BORDER);
		taskInterval.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		int taskCheck = store.getInt(SMOAUIPlugin.KEY_INTERVAL_TASK);
		if (taskCheck < 100) {
			taskCheck = doGetPreferenceStore().getDefaultInt(
					SMOAUIPlugin.KEY_INTERVAL_TASK);
		}

		taskInterval.setValues(taskCheck, 100, 3600000, 3, 1000, 10000);

		// out
		label = new Label(parent, SWT.NONE);
		label.setText(Messages.SMOAPreferencePage_OutputPooling);
		label.setLayoutData(new GridData());

		outInterval = new Spinner(parent, SWT.BORDER);
		outInterval.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		int outCheck = store.getInt(SMOAUIPlugin.KEY_INTERVAL_OUT);
		if (outCheck < 100) {
			outCheck = doGetPreferenceStore().getDefaultInt(
					SMOAUIPlugin.KEY_INTERVAL_OUT);
		}

		outInterval.setValues(outCheck, 100, 3600000, 3, 1000, 10000);

		return parent;
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return SMOAUIPlugin.getDefault().getPreferenceStore();
	}

	public void init(IWorkbench arg0) {

		doGetPreferenceStore().setDefault(SMOAUIPlugin.KEY_INTERVAL_TASK,
				PoolingIntervalsAndStatic.DEFAULT_POOLING_STATE);
		doGetPreferenceStore().setDefault(SMOAUIPlugin.KEY_INTERVAL_OUT,
				PoolingIntervalsAndStatic.DEFAULT_POOLING_OUT);
	}

	@Override
	public boolean performOk() {
		final IPreferenceStore preferenceStore = doGetPreferenceStore();
		preferenceStore.setValue(SMOAUIPlugin.KEY_INTERVAL_TASK,
				taskInterval.getSelection());
		preferenceStore.setValue(SMOAUIPlugin.KEY_INTERVAL_OUT,
				outInterval.getSelection());

		PoolingIntervalsAndStatic.setPoolingIntervalTask(taskInterval.getSelection());
		PoolingIntervalsAndStatic.setPoolingIntervalOut(outInterval.getSelection());

		return true;
	}

}