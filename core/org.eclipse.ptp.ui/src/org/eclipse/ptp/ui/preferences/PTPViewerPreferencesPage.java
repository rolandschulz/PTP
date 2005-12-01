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
package org.eclipse.ptp.ui.preferences;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.ptp.ui.IPTPUIConstants;
import org.eclipse.ptp.ui.PTPUIPlugin;
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
public class PTPViewerPreferencesPage extends AbstractPerferencePage {
	private Button showRulerButton = null;
	
	public PTPViewerPreferencesPage() {
		super();
		setDescription(PreferenceMessages.getString("PTPViewerPreferencesPage.desc"));
		setPreferenceStore(PTPUIPlugin.getDefault().getPreferenceStore());
	}
	
	protected Control createContents(Composite parent) {
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
		Composite comp = createGroupComposite(parent, 1, false, PreferenceMessages.getString("PTPViewerPreferencesPage.genName"));
		showRulerButton = createCheckButton(comp, PreferenceMessages.getString("PTPViewerPreferencesPage.displayRuler"));
	}
	protected void defaultSetting() {
		Preferences preferences = PTPUIPlugin.getDefault().getPluginPreferences();
		preferences.setDefault(IPTPUIConstants.SHOW_RULER, true);
	}
	public void performDefaults() { 
		Preferences preferences = PTPUIPlugin.getDefault().getPluginPreferences();
		showRulerButton.setSelection(preferences.getBoolean(IPTPUIConstants.SHOW_RULER));
		super.performDefaults();
	}
	public boolean performOk() {
		storeValues();
		PTPUIPlugin.getDefault().savePluginPreferences();
		PTPUIPlugin.getDefault().firePreferencesListeners();
		return true;
	}
	
	protected void setValues() {
		Preferences preferences = PTPUIPlugin.getDefault().getPluginPreferences();
		showRulerButton.setSelection(preferences.getBoolean(IPTPUIConstants.SHOW_RULER));
	}
	protected void storeValues() {
		Preferences preferences = PTPUIPlugin.getDefault().getPluginPreferences();
		preferences.setValue(IPTPUIConstants.SHOW_RULER, showRulerButton.getSelection());
	}
}
