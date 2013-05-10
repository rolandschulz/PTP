/****************************************************************************
 *			Tuning and Analysis Utilities
 *			http://www.cs.uoregon.edu/research/paracomp/tau
 ****************************************************************************
 * Copyright (c) 1997-2006
 *    Department of Computer and Information Science, University of Oregon
 *    Advanced Computing Laboratory, Los Alamos National Laboratory
 *    Research Center Juelich, ZAM Germany	
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Wyatt Spear - initial API and implementation
 ****************************************************************************/
package org.eclipse.ptp.etfw.tau;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ptp.etfw.tau.messages.Messages;
import org.eclipse.ptp.etfw.tau.papitest.TestPAPI;
import org.eclipse.ptp.internal.etfw.Activator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.osgi.service.prefs.BackingStoreException;

public class TAUPrefPage extends PreferencePage implements IWorkbenchPreferencePage {
	protected class WidgetListener extends SelectionAdapter implements ModifyListener, IPropertyChangeListener
	{
		public void modifyText(ModifyEvent evt) {
			// Object source = evt.getSource();
			// if(source==tauBin){
			// }

			updatePreferencePage();
		}

		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(FieldEditor.IS_VALID)) {
				updatePreferencePage();
			}
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			final Object source = e.getSource();
			if (source == testPAPI) {
				tp.run();
			}
			updatePreferencePage();
		}
	}

	protected Button checkAutoOpts = null;
	protected Button checkAixOpts = null;

	protected Button testPAPI = null;

	TestPAPI tp = new TestPAPI();

	protected WidgetListener listener = new WidgetListener();

	protected Button createButton(Composite parent, String label, int type) {
		final Button button = new Button(parent, type);
		button.setText(label);
		final GridData data = new GridData();
		button.setLayoutData(data);
		return button;
	}

	protected Button createCheckButton(Composite parent, String label) {
		return createButton(parent, label, SWT.CHECK | SWT.LEFT);
	}

	@Override
	protected Control createContents(Composite parent) {

		// TODO: Implement tau-option checking
		final GridData gridData = new GridData(GridData.VERTICAL_ALIGN_END);
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;

		if (org.eclipse.cdt.utils.Platform.getOS().toLowerCase().trim().indexOf("aix") >= 0) //$NON-NLS-1$
		{
			checkAixOpts = createCheckButton(parent, Messages.TAUPrefPage_AutoEclipseInternal);
			checkAixOpts.setLayoutData(gridData);
			checkAixOpts.addSelectionListener(listener);
		}

		checkAutoOpts = createCheckButton(parent, Messages.TAUPrefPage_CheckTauOptions);
		checkAutoOpts.setLayoutData(gridData);
		checkAutoOpts.addSelectionListener(listener);

		testPAPI = new Button(parent, SWT.NONE);
		testPAPI.setText(Messages.TAUPrefPage_TestPapi);
		testPAPI.addSelectionListener(listener);

		return parent;
	}

	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub

	}

	/* This may be unused... */
	protected void loadSaved()
	{
		// Preferences preferences = ETFWUtils.getDefault().getPluginPreferences();
		final IPreferencesService service = Platform.getPreferencesService();

		// TODO: Add checks
		checkAutoOpts.setSelection(service.getBoolean(Activator.PLUGIN_ID, ITAULaunchConfigurationConstants.TAU_CHECK_AUTO_OPT,
				true, null));
		if (checkAixOpts != null)
		{
			checkAixOpts.setSelection(service.getBoolean(Activator.PLUGIN_ID, ITAULaunchConfigurationConstants.TAU_CHECK_AIX_OPT,
					false, null));
		}
	}

	@Override
	public boolean performOk()
	{

		final IEclipsePreferences preferences = new InstanceScope().getNode(Activator.PLUGIN_ID);
		// Preferences preferences = ETFWUtils.getDefault().getPluginPreferences();

		// TODO: Add checks
		preferences.putBoolean(ITAULaunchConfigurationConstants.TAU_CHECK_AUTO_OPT, checkAutoOpts.getSelection());
		if (checkAixOpts != null)
		{
			preferences.putBoolean(ITAULaunchConfigurationConstants.TAU_CHECK_AIX_OPT, checkAixOpts.getSelection());
		}

		try {
			preferences.flush();
		} catch (final BackingStoreException e) {
			e.printStackTrace();
		}
		return true;
	}

	protected void updatePreferencePage()
	{
		setErrorMessage(null);
		setMessage(null);

		setValid(true);
	}

}
