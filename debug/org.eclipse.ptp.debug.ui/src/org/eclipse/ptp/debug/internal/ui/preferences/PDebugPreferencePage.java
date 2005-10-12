/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.debug.internal.ui.preferences;

import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.internal.ui.PDebugModelPresentation;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @author Clement chu
 *
 */
public class PDebugPreferencePage extends AbstractDebugPerferencePage {
	private Button fPathsButton = null;
	private Button fRegisteredProcessButton = null;
	
	public PDebugPreferencePage() {
		super();
		setDescription(PreferenceMessages.getString("PDebugPreferencePage.desc"));
		getPreferenceStore().addPropertyChangeListener(this);
	}
	
	protected Control createContents(Composite parent) {
		//TODO ignored help
		//getWorkbench().getHelpSystem().setHelp(getControl(), IPDebugHelpContextIds.P_DEBUG_PREFERENCE_PAGE);
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout(1, false);
		layout.numColumns = 1;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);
		createSpacer(composite, 1);
		createViewSettingPreferences(composite);
		defaultSetting();
		setValues();
		return composite;
	}
	protected void createViewSettingPreferences(Composite parent) {
		Composite comp = createGroupComposite(parent, 1, false, PreferenceMessages.getString("PDebugPreferencePage.default1"));
		fPathsButton = createCheckButton(comp, PreferenceMessages.getString("PDebugPreferencePage.default2"));
		fRegisteredProcessButton = createCheckButton(comp, PreferenceMessages.getString("PDebugPreferencePage.registerProcess"));
	}
	protected void defaultSetting() {
		IPreferenceStore store = getPreferenceStore();
		store.setDefault(IPDebugPreferenceConstants.PREF_SHOW_FULL_PATHS, false);
		store.setDefault(IPDebugPreferenceConstants.PREF_PTP_DEBUG_REGISTER_PROC_0, true);
	}
	public void performDefaults() { 
		IPreferenceStore store = getPreferenceStore();
		fPathsButton.setSelection(store.getDefaultBoolean(IPDebugPreferenceConstants.PREF_SHOW_FULL_PATHS));
		fRegisteredProcessButton.setSelection(store.getDefaultBoolean(IPDebugPreferenceConstants.PREF_PTP_DEBUG_REGISTER_PROC_0));		
		super.performDefaults();
	}
	
	public boolean performOk() {
		storeValues();
		if (changed) {
			refreshViews(new String[] {IDebugUIConstants.ID_BREAKPOINT_VIEW});
		}

		PTPDebugUIPlugin.getDefault().savePluginPreferences();
		PTPDebugCorePlugin.getDefault().savePluginPreferences();
		return true;
	}
	
	protected void setValues() {
		IPreferenceStore store = getPreferenceStore();
		fPathsButton.setSelection(store.getBoolean(IPDebugPreferenceConstants.PREF_SHOW_FULL_PATHS));
		fRegisteredProcessButton.setSelection(store.getBoolean(IPDebugPreferenceConstants.PREF_PTP_DEBUG_REGISTER_PROC_0));
	}
	protected void storeValues() {
		IPreferenceStore store = getPreferenceStore();
		store.setValue(IPDebugPreferenceConstants.PREF_SHOW_FULL_PATHS, fPathsButton.getSelection());
		store.setValue(IPDebugPreferenceConstants.PREF_PTP_DEBUG_REGISTER_PROC_0, fRegisteredProcessButton.getSelection());
		IDebugModelPresentation pres = PTPDebugUIPlugin.getDebugModelPresentation();
		if (pres != null) {
			pres.setAttribute(PDebugModelPresentation.DISPLAY_FULL_PATHS, fPathsButton.getSelection()?Boolean.TRUE:Boolean.FALSE);
		}
	}
	
    public void propertyChange(PropertyChangeEvent event) {
    	if (event.getProperty().equals(IPDebugPreferenceConstants.PREF_SHOW_FULL_PATHS))
    		changed = true;
    	else
    		changed = false;
    }	
}
