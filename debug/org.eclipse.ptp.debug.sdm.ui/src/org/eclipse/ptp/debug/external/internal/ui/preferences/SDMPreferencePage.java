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
package org.eclipse.ptp.debug.external.internal.ui.preferences;

import java.io.File;
import java.net.URL;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ptp.debug.core.IPDebugConstants;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.ui.preferences.AbstractPerferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @author Clement chu
 *
 */
public class SDMPreferencePage extends AbstractPerferencePage {
	private Text sdmPathText = null;
	private Text sdmArgsText = null;
	private Button sdmPathButton = null;
	
    protected class WidgetListener extends SelectionAdapter implements ModifyListener {
   		public void widgetSelected(SelectionEvent e) {
    		Object source = e.getSource();
        if (source == sdmPathButton)
       		handleSDMButtonSelected();
   		}
	    public void modifyText(ModifyEvent e) {
    		setValid(isValid());        	
	    }
    }
    protected WidgetListener listener = new WidgetListener();
    
	public SDMPreferencePage() {
		super();
		setDescription(PreferenceMessages.getString("SDMPreferencePage.desc"));
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
		createSDMSetting(composite);
		defaultSetting();
		setValues();
		return composite;
	}
	protected void createSDMSetting(Composite parent) {
		Composite group = createGroupComposite(parent, 1, false, PreferenceMessages.getString("SDMPreferencePage.sdm_group"));
		Composite comp = createComposite(group, 3);

		new Label(comp, SWT.NONE).setText(PreferenceMessages.getString("SDMPreferencePage.sdm_selection"));

		sdmPathText = new Text(comp, SWT.SINGLE | SWT.BORDER);
		sdmPathText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		sdmPathText.addModifyListener(listener);
		
		sdmPathButton = createButton(comp, PreferenceMessages.getString("SDMPreferencePage.browse_1"), SWT.PUSH);
		sdmPathButton.addSelectionListener(listener);

		new Label(comp, SWT.NONE).setText(PreferenceMessages.getString("SDMPreferencePage.sdm_arguments"));

		sdmArgsText = new Text(comp, SWT.SINGLE | SWT.BORDER);
		sdmArgsText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		sdmArgsText.addModifyListener(listener);
	}
	protected void defaultSetting() {
		Preferences preferences = PTPDebugCorePlugin.getDefault().getPluginPreferences();
		URL url = Platform.find(Platform.getBundle(PTPDebugCorePlugin.PLUGIN_ID), new Path("/"));
		if (url != null) {
			try {
				File path = new File(Platform.asLocalURL(url).getPath());
				String ipath = path.getAbsolutePath();
				int idx = ipath.indexOf(PTPDebugCorePlugin.PLUGIN_ID);
				String ipath2 = ipath.substring(0, idx) + "org.eclipse.ptp.debug.sdm/sdm";
				preferences.setDefault(IPDebugConstants.PREF_PTP_DEBUGGER_FILE, ipath2);
			} catch(Exception e) { 
			}
		}
		preferences.setDefault(IPDebugConstants.PREF_PTP_DEBUGGER_ARGS, IPDebugConstants.PREF_DEFAULT_DEUBGGER_ARGS);
	}
	public void performDefaults() { 
		//IPreferenceStore store = getPreferenceStore();
		Preferences preferences = PTPDebugCorePlugin.getDefault().getPluginPreferences();
		sdmPathText.setText(preferences.getDefaultString(IPDebugConstants.PREF_PTP_DEBUGGER_FILE));
		sdmArgsText.setText(preferences.getDefaultString(IPDebugConstants.PREF_PTP_DEBUGGER_ARGS));
		super.performDefaults();
	}
	protected void setValues() {
		//IPreferenceStore store = getPreferenceStore();
		Preferences preferences = PTPDebugCorePlugin.getDefault().getPluginPreferences();
		sdmPathText.setText(preferences.getString(IPDebugConstants.PREF_PTP_DEBUGGER_FILE));
		sdmArgsText.setText(preferences.getString(IPDebugConstants.PREF_PTP_DEBUGGER_ARGS));
	}	
	protected void storeValues() {
		//IPreferenceStore store = getPreferenceStore();
		Preferences preferences = PTPDebugCorePlugin.getDefault().getPluginPreferences();
		preferences.setValue(IPDebugConstants.PREF_PTP_DEBUGGER_FILE, sdmPathText.getText());
		preferences.setValue(IPDebugConstants.PREF_PTP_DEBUGGER_ARGS, sdmArgsText.getText());
	}
	
    public void propertyChange(PropertyChangeEvent event) {
    	setValid(isValid());
    }	
	public boolean performOk() {
		storeValues();
		PTPDebugCorePlugin.getDefault().savePluginPreferences();
		return true;
	}

	public boolean isValid() {
		setErrorMessage(null);
		setMessage(null);
		String name = sdmPathText.getText();
		if (name == null) {
			setErrorMessage(PreferenceMessages.getString("SDMPreferencePage.no_sdm"));
			return false;
		}
		File sdmFile = new File(name);
		if (!sdmFile.isFile()) {
			setErrorMessage(PreferenceMessages.getString("SDMPreferencePage.Selection_must_be_file"));
			return false;
		}
		return true;
	}
	
	private void handleSDMButtonSelected() {
		FileDialog dialog = new FileDialog(getShell(), SWT.NONE);
		String filePath = dialog.open();
		if (filePath != null) {
			sdmPathText.setText(filePath);
		}
	}
}
