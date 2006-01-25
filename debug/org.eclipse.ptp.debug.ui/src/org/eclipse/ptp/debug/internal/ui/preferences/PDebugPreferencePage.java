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

import org.eclipse.core.runtime.Preferences;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.debug.core.IPDebugConstants;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.internal.ui.PDebugModelPresentation;
import org.eclipse.ptp.ui.preferences.AbstractPerferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;

/**
 * @author Clement chu
 *
 */
public class PDebugPreferencePage extends AbstractPerferencePage {
	private Button fPathsButton = null;
	private Button fRegisteredProcessButton = null;
	
	public PDebugPreferencePage() {
		super();
		setDescription(PreferenceMessages.getString("PDebugPreferencePage.desc"));
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
		//IPreferenceStore store = getPreferenceStore();
		Preferences preferences = PTPDebugCorePlugin.getDefault().getPluginPreferences();
		preferences.setDefault(IPDebugConstants.PREF_SHOW_FULL_PATHS, false);
		preferences.setValue(IPDebugConstants.PREF_SHOW_FULL_PATHS, false);
		preferences.setDefault(IPDebugConstants.PREF_PTP_DEBUG_REGISTER_PROC_0, true);
		preferences.setValue(IPDebugConstants.PREF_PTP_DEBUG_REGISTER_PROC_0, true);
	}
	public void performDefaults() { 
		//IPreferenceStore store = getPreferenceStore();
		defaultSetting();
		Preferences preferences = PTPDebugCorePlugin.getDefault().getPluginPreferences();
		fPathsButton.setSelection(preferences.getDefaultBoolean(IPDebugConstants.PREF_SHOW_FULL_PATHS));
		fRegisteredProcessButton.setSelection(preferences.getDefaultBoolean(IPDebugConstants.PREF_PTP_DEBUG_REGISTER_PROC_0));		
		super.performDefaults();
	}
	
	public boolean performOk() {
		storeValues();
		PTPDebugCorePlugin.getDefault().savePluginPreferences();
		refreshView();
		return true;
	}
	
	protected void setValues() {
		//IPreferenceStore store = getPreferenceStore();
		Preferences preferences = PTPDebugCorePlugin.getDefault().getPluginPreferences();
		fPathsButton.setSelection(preferences.getBoolean(IPDebugConstants.PREF_SHOW_FULL_PATHS));
		fRegisteredProcessButton.setSelection(preferences.getBoolean(IPDebugConstants.PREF_PTP_DEBUG_REGISTER_PROC_0));
	}
	protected void storeValues() {
		//IPreferenceStore store = getPreferenceStore();
		Preferences preferences = PTPDebugCorePlugin.getDefault().getPluginPreferences();
		preferences.setValue(IPDebugConstants.PREF_SHOW_FULL_PATHS, fPathsButton.getSelection());
		preferences.setValue(IPDebugConstants.PREF_PTP_DEBUG_REGISTER_PROC_0, fRegisteredProcessButton.getSelection());
	}
    protected void refreshView() {
    	IWorkbenchPage[] pages = getPages();
    	for (int i=0; i<pages.length; i++) {
			IViewPart part = pages[i].findView(IDebugUIConstants.ID_BREAKPOINT_VIEW);
			if (part != null) {
				IDebugView adapter = (IDebugView)part.getAdapter(IDebugView.class);
				if (adapter != null) {				
					Viewer viewer = adapter.getViewer();
					IDebugModelPresentation pres = adapter.getPresentation(PTPDebugCorePlugin.getUniqueIdentifier());
					if (pres != null) {
						pres.setAttribute(PDebugModelPresentation.DISPLAY_FULL_PATHS, fPathsButton.getSelection()?Boolean.TRUE:Boolean.FALSE);
					}
					if (viewer instanceof StructuredViewer) {
						final StructuredViewer structViewer = (StructuredViewer)viewer;
						BusyIndicator.showWhile( structViewer.getControl().getDisplay(), new Runnable() {
							public void run() {
								structViewer.refresh();
							}
						} );
					}
				}
			}
    	}
	}    
}
