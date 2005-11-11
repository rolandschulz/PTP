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

import java.io.File;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
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
public class PDebuggerPage extends AbstractDebugPerferencePage {
	//public final String DEBUG_SIMULATOR_NAME = "Debug Simulator";
	//private Combo simulatorCombo = null;
	//private IntegerFieldEditor timeoutField = null;
	//private IntegerFieldEditor eventTimeField = null;
	private Text sdmPathText = null;
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
    
	public PDebuggerPage() {
		super();
		setPreferenceStore(PTPDebugUIPlugin.getDefault().getPreferenceStore());
		getPreferenceStore().addPropertyChangeListener(this);
		setDescription(PreferenceMessages.getString("PDebuggerPage.desc"));
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
		//createSpacer(composite, 1);
		//createCommunicationSetting(composite);
		//createSpacer(composite, 1);
		//createEventSetting(composite);
		defaultSetting();
		setValues();
		return composite;
	}
	protected void createSDMSetting(Composite parent) {
		Composite group = createGroupComposite(parent, 1, false, PreferenceMessages.getString("PDebuggerPage.sdm_group"));
		Composite comp = createComposite(group, 3);
		new Label(comp, SWT.NONE).setText(PreferenceMessages.getString("PDebuggerPage.sdm_selection"));

		sdmPathText = new Text(comp, SWT.SINGLE | SWT.BORDER);
		sdmPathText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		sdmPathText.addModifyListener(listener);
		
		sdmPathButton = createButton(comp, PreferenceMessages.getString("PDebuggerPage.browse_1"), SWT.PUSH);
		sdmPathButton.addSelectionListener(listener);
	}
	/*
	protected void createCommunicationSetting(Composite parent) {
		Composite group = createGroupComposite(parent, 1, false, PreferenceMessages.getString("PDebuggerPage.default3"));
		Composite comp = createComposite(group, 1);
		timeoutField = new IntegerFieldEditor(IPDebugPreferenceConstants.PREF_PTP_DEBUG_COMM_TIMEOUT, PreferenceMessages.getString("PDebuggerPage.default4"), comp);
		timeoutField.setPropertyChangeListener(this);
		timeoutField.setEmptyStringAllowed(false);
	}
	protected void createEventSetting(Composite parent) {
		Composite group = createGroupComposite(parent, 1, false, PreferenceMessages.getString("PDebuggerPage.default5"));
		Composite comp = createComposite(group, 1);
		eventTimeField = new IntegerFieldEditor(IPDebugPreferenceConstants.PREF_PTP_DEBUG_EVENT_TIME, PreferenceMessages.getString("PDebuggerPage.default6"), comp);
		eventTimeField.setPropertyChangeListener(this);
		eventTimeField.setEmptyStringAllowed(false);
	}
	private String[] getDubuggers() {
		return new String[] {DEBUG_SIMULATOR_NAME};
	}
	private int getSelectedDebugger(String debuggerName) {
		String[] names = getDubuggers();
		for (int i=0; i<names.length; i++) {
			if (names[i].equals(debuggerName))
				return i;
		}
		return -1;
	}
	*/
	
	protected void defaultSetting() {
		//IPreferenceStore store = getPreferenceStore();
		//store.setDefault(IPDebugPreferenceConstants.PREF_PTP_SDM_FILE, sdmPathText.getText());
		//store.setDefault(IPDebugPreferenceConstants.PREF_PTP_DEBUG_COMM_TIMEOUT, IPDebugPreferenceConstants.DEFAULT_DEBUG_TIMEOUT);
		//store.setDefault(IPDebugPreferenceConstants.PREF_PTP_DEBUG_EVENT_TIME, IPDebugPreferenceConstants.DEFAULT_DEBUG_EVENTTIME);
	}
	public void performDefaults() { 
		//IPreferenceStore store = getPreferenceStore();
		//sdmPathText.setText(store.getString(IPDebugPreferenceConstants.PREF_PTP_SDM_FILE));
		//timeoutField.setStringValue(""+store.getDefaultInt(IPDebugPreferenceConstants.PREF_PTP_DEBUG_COMM_TIMEOUT));
		//eventTimeField.setStringValue(""+store.getDefaultInt(IPDebugPreferenceConstants.PREF_PTP_DEBUG_EVENT_TIME));
		super.performDefaults();
	}
	
	protected void setValues() {
		IPreferenceStore store = getPreferenceStore();
		sdmPathText.setText(store.getString(IPDebugPreferenceConstants.PREF_PTP_SDM_FILE));
		//simulatorCombo.select(getSelectedDebugger(store.getString(IPDebugPreferenceConstants.PREF_PTP_DEBUGGER)));
		//timeoutField.setStringValue(""+store.getInt(IPDebugPreferenceConstants.PREF_PTP_DEBUG_COMM_TIMEOUT));
		//eventTimeField.setStringValue(""+store.getInt(IPDebugPreferenceConstants.PREF_PTP_DEBUG_EVENT_TIME));
	}	
		
	protected void storeValues() {
		IPreferenceStore store = getPreferenceStore();
		store.setValue(IPDebugPreferenceConstants.PREF_PTP_SDM_FILE, sdmPathText.getText());
		//store.setValue(IPDebugPreferenceConstants.PREF_PTP_DEBUGGER, simulatorCombo.getText());
		//store.setValue(IPDebugPreferenceConstants.PREF_PTP_DEBUG_COMM_TIMEOUT, timeoutField.getIntValue());
		//store.setValue(IPDebugPreferenceConstants.PREF_PTP_DEBUG_EVENT_TIME, eventTimeField.getIntValue());
	}
	
    public void propertyChange(PropertyChangeEvent event) {
    	setValid(isValid());
    }	

	public boolean isValid() {
		setErrorMessage(null);
		setMessage(null);
		String name = sdmPathText.getText();
		if (name == null) {
			setErrorMessage(PreferenceMessages.getString("PDebuggerPage.no_sdm"));
			return false;
		}
		File sdmFile = new File(name);
		if (!sdmFile.isFile()) {
			setErrorMessage(PreferenceMessages.getString("PDebuggerPage.Selection_must_be_file"));
			return false;
		}

		/*
		if (!timeoutField.isValid()) {
			setErrorMessage(timeoutField.getErrorMessage());
			return false;
		}
		if (!eventTimeField.isValid()) {
			setErrorMessage(eventTimeField.getErrorMessage());
			return false;
		}
		*/
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
