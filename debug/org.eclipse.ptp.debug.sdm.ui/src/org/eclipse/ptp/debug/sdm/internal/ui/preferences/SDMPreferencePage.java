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
package org.eclipse.ptp.debug.sdm.internal.ui.preferences;

import java.io.File;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ptp.debug.sdm.core.SDMDebugCorePlugin;
import org.eclipse.ptp.debug.sdm.core.SDMPreferenceConstants;
import org.eclipse.ptp.debug.ui.PreferencesAdapter;
import org.eclipse.ptp.ui.preferences.AbstractPerferencePage;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @author Clement chu
 *
 */
public class SDMPreferencePage extends AbstractPerferencePage {
	protected class WidgetListener extends SelectionAdapter implements ModifyListener {
   		public void modifyText(ModifyEvent e) {
	    		setValid(isValid());        	
	    }
	    public void widgetSelected(SelectionEvent e) {
   			Object source = e.getSource();
   			if (source == sdmPathButton)
   				handleSDMButtonSelected();
   			else if (source == sdmBackendCombo)
   				handleSDMComboSelected();
   		}
    }
	private Text sdmPathText = null;
	private Text sdmHostText = null;
	private Text sdmBackendPathText = null;
	private Text sdmArgsText = null;
	private Combo sdmBackendCombo = null;
	
    private Button sdmPathButton = null;
    protected WidgetListener listener = new WidgetListener();
    
	public SDMPreferencePage() {
		super();
		setPreferenceStore(new PreferencesAdapter(SDMDebugCorePlugin.getDefault().getPluginPreferences()));
		setDescription(PreferenceMessages.getString("SDMPreferencePage.sdm_desc"));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#isValid()
	 */
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	public void performDefaults() { 
		IPreferenceStore store = getPreferenceStore();
		sdmPathText.setText(store.getDefaultString(SDMPreferenceConstants.SDM_DEBUGGER_FILE));
		sdmArgsText.setText(store.getDefaultString(SDMPreferenceConstants.SDM_DEBUGGER_ARGS));
		sdmHostText.setText(store.getDefaultString(SDMPreferenceConstants.SDM_DEBUGGER_HOST));
		sdmBackendCombo.select(sdmBackendCombo.indexOf(store.getDefaultString(SDMPreferenceConstants.SDM_DEBUGGER_BACKEND_TYPE)));
		sdmBackendPathText.setText(store.getDefaultString(SDMPreferenceConstants.SDM_DEBUGGER_BACKEND_PATH));
		super.performDefaults();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	public boolean performOk() {
		storeValues();
		SDMDebugCorePlugin.getDefault().savePluginPreferences();
		return true;
	}
	
	/**
	 * Handle property change event
	 * 
     * @param event
     */
    public void propertyChange(PropertyChangeEvent event) {
    		setValid(isValid());
    }	
	
	/**
	 * Handle sdm path button selected
	 */
	private void handleSDMButtonSelected() {
		FileDialog dialog = new FileDialog(getShell(), SWT.NONE);
		String filePath = dialog.open();
		if (filePath != null) {
			sdmPathText.setText(filePath);
		}
	}
	
    /**
	 * Handle combo selection
	 */
	private void handleSDMComboSelected() {
	}	
    
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
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
		setValues();
		return composite;
	}
	
	/**
	 * @param parent
	 */
	protected void createSDMSetting(Composite parent) {
		Composite group = createGroupComposite(parent, 1, false, PreferenceMessages.getString("SDMPreferencePage.sdm_group"));
		Composite comp = createComposite(group, 3);

		new Label(comp, SWT.NONE).setText(PreferenceMessages.getString("SDMPreferencePage.sdm_host"));
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		sdmHostText = new Text(comp, SWT.SINGLE | SWT.BORDER);
		sdmHostText.setLayoutData(gd);
		sdmHostText.addModifyListener(listener);
		
		new Label(comp, SWT.NONE).setText(PreferenceMessages.getString("SDMPreferencePage.sdm_backend"));
		sdmBackendCombo = new Combo(comp, SWT.READ_ONLY);
		sdmBackendCombo.setLayoutData(gd);
		sdmBackendCombo.setItems(SDMPreferenceConstants.SDM_DEBUGGER_BACKENDS);
		sdmBackendCombo.addSelectionListener(listener);

		new Label(comp, SWT.NONE).setText(PreferenceMessages.getString("SDMPreferencePage.sdm_backend_path"));
		sdmBackendPathText = new Text(comp, SWT.SINGLE | SWT.BORDER);
		sdmBackendPathText.setLayoutData(gd);
		sdmBackendPathText.addModifyListener(listener);
		
		new Label(comp, SWT.NONE).setText(PreferenceMessages.getString("SDMPreferencePage.sdm_selection"));
		sdmPathText = new Text(comp, SWT.SINGLE | SWT.BORDER);
		sdmPathText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		sdmPathText.addModifyListener(listener);		
		sdmPathButton = createButton(comp, PreferenceMessages.getString("SDMPreferencePage.browse_1"), SWT.PUSH);
		sdmPathButton.addSelectionListener(listener);
		
		new Label(comp, SWT.NONE).setText(PreferenceMessages.getString("SDMPreferencePage.sdm_args"));
		sdmArgsText = new Text(comp, SWT.SINGLE | SWT.BORDER);
		sdmArgsText.setLayoutData(gd);
		sdmArgsText.addModifyListener(listener);		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.preferences.AbstractPerferencePage#setValues()
	 */
	protected void setValues() {
		IPreferenceStore store = getPreferenceStore();
		sdmPathText.setText(store.getString(SDMPreferenceConstants.SDM_DEBUGGER_FILE));
		sdmArgsText.setText(store.getString(SDMPreferenceConstants.SDM_DEBUGGER_ARGS));
		sdmHostText.setText(store.getString(SDMPreferenceConstants.SDM_DEBUGGER_HOST));
		sdmBackendCombo.select(sdmBackendCombo.indexOf(store.getString(SDMPreferenceConstants.SDM_DEBUGGER_BACKEND_TYPE)));
		sdmBackendPathText.setText(store.getString(SDMPreferenceConstants.SDM_DEBUGGER_BACKEND_PATH));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.preferences.AbstractPerferencePage#storeValues()
	 */
	protected void storeValues() {
		IPreferenceStore store = getPreferenceStore();
		store.setValue(SDMPreferenceConstants.SDM_DEBUGGER_FILE, sdmPathText.getText());
		store.setValue(SDMPreferenceConstants.SDM_DEBUGGER_ARGS, sdmArgsText.getText());
		store.setValue(SDMPreferenceConstants.SDM_DEBUGGER_HOST, sdmHostText.getText());
		store.setValue(SDMPreferenceConstants.SDM_DEBUGGER_BACKEND_TYPE, sdmBackendCombo.getItem(sdmBackendCombo.getSelectionIndex()));
		store.setValue(SDMPreferenceConstants.SDM_DEBUGGER_BACKEND_PATH, sdmBackendPathText.getText());
	}
}
