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
package org.eclipse.ptp.perf.tau;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ptp.perf.Activator;
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

public class TAUPrefPage extends PreferencePage implements IWorkbenchPreferencePage{
	protected Button checkAutoOpts=null;
	protected Button checkAixOpts=null;
	
	
	protected class WidgetListener extends SelectionAdapter implements ModifyListener, IPropertyChangeListener
	{
		public void widgetSelected(SelectionEvent e) {
//			Object source = e.getSource();
//			if(source == browseBinButton) {
//				handleBinBrowseButtonSelected();
//			}
			updatePreferencePage();
		}

		public void modifyText(ModifyEvent evt) {
//			Object source = evt.getSource();
//			if(source==tauBin){
//			}

			updatePreferencePage();
		}

		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(FieldEditor.IS_VALID))
				updatePreferencePage();
		}
	}

	protected WidgetListener listener = new WidgetListener();
	
	@Override
	protected Control createContents(Composite parent) {
		
		//TODO: Implement tau-option checking
		GridData gridData = new GridData(GridData.VERTICAL_ALIGN_END);
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		
		if(org.eclipse.cdt.utils.Platform.getOS().toLowerCase().trim().indexOf("aix")>=0)
		{
			checkAixOpts=createCheckButton(parent,"Automatically use Eclipse internal builder (May be needed for AIX compatibility)");
			checkAixOpts.setLayoutData(gridData);
			checkAixOpts.addSelectionListener(listener);
		}

		checkAutoOpts=createCheckButton(parent, "Check for TAU System options");
		checkAutoOpts.setLayoutData(gridData);
		checkAutoOpts.addSelectionListener(listener);
		return parent;
	}

	private void loadSaved()
	{
		Preferences preferences = Activator.getDefault().getPluginPreferences();
		
		//TODO: Add checks
		checkAutoOpts.setSelection(preferences.getBoolean("TAUCheckForAutoOptions"));
		if(checkAixOpts!=null)
			checkAixOpts.setSelection(preferences.getBoolean("TAUCheckForAIXOptions"));
	}
	
	public boolean performOk() 
	{
		Preferences preferences = Activator.getDefault().getPluginPreferences();

		//TODO: Add checks
		preferences.setValue("TAUCheckForAutoOptions", checkAutoOpts.getSelection());
		if(checkAixOpts!=null)
			preferences.setValue("TAUCheckForAIXOptions", checkAixOpts.getSelection());

		Activator.getDefault().savePluginPreferences();
		return true;
	}
	
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
		
	}
	
	protected void updatePreferencePage() 
	{
		setErrorMessage(null);
		setMessage(null);

		setValid(true);
	}
	
	protected Button createCheckButton(Composite parent, String label) {
		return createButton(parent, label, SWT.CHECK | SWT.LEFT);
	}
	protected Button createButton(Composite parent, String label, int type) {
		Button button = new Button(parent, type);
		button.setText(label);
		GridData data = new GridData();
		button.setLayoutData(data);
		return button;
	}

}
