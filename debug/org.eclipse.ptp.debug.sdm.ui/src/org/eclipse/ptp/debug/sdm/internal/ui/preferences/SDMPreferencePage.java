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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ptp.debug.sdm.core.SDMDebugCorePlugin;
import org.eclipse.ptp.debug.sdm.core.SDMPreferenceConstants;
import org.eclipse.ptp.debug.sdm.ui.messages.Messages;
import org.eclipse.ptp.debug.ui.PreferencesAdapter;
import org.eclipse.ptp.ui.preferences.AbstractPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @author Clement chu
 *
 */
public class SDMPreferencePage extends AbstractPreferencePage {
	protected class WidgetListener extends SelectionAdapter implements ModifyListener {
   		public void modifyText(ModifyEvent e) {
	    		setValid(isValid());        	
	    }
	    public void widgetSelected(SelectionEvent e) {
   			Object source = e.getSource();
   			if (source == sdmBackendCombo)
   				handleSDMComboSelected();
   		}
    }
	
	private Text sdmBackendPathText = null;
	private Text sdmArgsText = null;
	private Combo sdmBackendCombo = null;
	private Button debugEnabledButton;
	private Button debugStartupButton;
	private Button debugMessagesButton;
	private Button debugRoutingButton;
	private Button debugServerButton;
	private Button debugBackendButton;
	private Button debugProtocolButton;
	private Button debugMasterEnabledButton;
	private Button debugMasterTraceButton;
	private Button debugMasterTraceMoreButton;
	private Button debugMasterOutputButton;
	
	private boolean debugEnabled = false;
	private int debugLevel = SDMPreferenceConstants.DEBUG_LEVEL_NONE;
	private boolean debugMasterEnabled = false;
	private int debugMasterLevel = SDMPreferenceConstants.DEBUG_MASTER_NONE;
	
    protected WidgetListener listener = new WidgetListener();
    
	public SDMPreferencePage() {
		super();
		setPreferenceStore(new PreferencesAdapter(SDMDebugCorePlugin.getDefault().getPluginPreferences()));
		setDescription(Messages.SDMPreferencePage_0);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#isValid()
	 */
	public boolean isValid() {
		setErrorMessage(null);
		setMessage(null);
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	public void performDefaults() { 
		IPreferenceStore store = getPreferenceStore();
		sdmArgsText.setText(store.getDefaultString(SDMPreferenceConstants.SDM_DEBUGGER_ARGS));
		sdmBackendCombo.select(sdmBackendCombo.indexOf(store.getDefaultString(SDMPreferenceConstants.SDM_DEBUGGER_BACKEND_TYPE)));
		sdmBackendPathText.setText(store.getDefaultString(SDMPreferenceConstants.SDM_DEBUGGER_BACKEND_PATH));
		debugEnabled = false;
		debugLevel = SDMPreferenceConstants.DEBUG_LEVEL_NONE;
		debugMasterEnabled = false;
		debugMasterLevel = SDMPreferenceConstants.DEBUG_MASTER_NONE;
		updateDebugButtons();
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
		Composite group = createGroupComposite(parent, 1, false, Messages.SDMPreferencePage_1);
		Composite comp = createComposite(group, 2);

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		
		new Label(comp, SWT.NONE).setText(Messages.SDMPreferencePage_2);
		sdmBackendCombo = new Combo(comp, SWT.READ_ONLY);
		sdmBackendCombo.setLayoutData(gd);
		sdmBackendCombo.setItems(SDMPreferenceConstants.SDM_DEBUGGER_BACKENDS);
		sdmBackendCombo.addSelectionListener(listener);

		new Label(comp, SWT.NONE).setText(Messages.SDMPreferencePage_3);
		sdmBackendPathText = new Text(comp, SWT.SINGLE | SWT.BORDER);
		sdmBackendPathText.setLayoutData(gd);
		sdmBackendPathText.addModifyListener(listener);
		
		new Label(comp, SWT.NONE).setText(Messages.SDMPreferencePage_4);
		sdmArgsText = new Text(comp, SWT.SINGLE | SWT.BORDER);
		sdmArgsText.setLayoutData(gd);
		sdmArgsText.addModifyListener(listener);	
		
		Composite debugGroup = createGroupComposite(parent, 2, false, Messages.SDMPreferencePage_5);
		Composite sdmDebugComp = createComposite(debugGroup, 1);
		gd = new GridData();
		gd.verticalAlignment = SWT.TOP;
		sdmDebugComp.setLayoutData(gd);
		
		debugEnabledButton = new Button(sdmDebugComp, SWT.CHECK);
		debugEnabledButton.setText(Messages.SDMPreferencePage_6);
		debugEnabledButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				debugEnabled = debugEnabledButton.getSelection();
				updateDebugButtons();
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		debugStartupButton = new Button(sdmDebugComp, SWT.CHECK);
		debugStartupButton.setText(Messages.SDMPreferencePage_7);
		gd = new GridData();
		gd.horizontalIndent = 20;
		debugStartupButton.setLayoutData(gd);
		debugStartupButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (debugStartupButton.getSelection()) {
					debugLevel |= SDMPreferenceConstants.DEBUG_LEVEL_STARTUP;
				} else {
					debugLevel &= ~ SDMPreferenceConstants.DEBUG_LEVEL_STARTUP;
				}
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		debugMessagesButton = new Button(sdmDebugComp, SWT.CHECK);
		debugMessagesButton.setText(Messages.SDMPreferencePage_8);
		gd = new GridData();
		gd.horizontalIndent = 20;
		debugMessagesButton.setLayoutData(gd);
		debugMessagesButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (debugMessagesButton.getSelection()) {
					debugLevel |= SDMPreferenceConstants.DEBUG_LEVEL_MESSAGES;
				} else {
					debugLevel &= ~ SDMPreferenceConstants.DEBUG_LEVEL_MESSAGES;
				}
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		debugRoutingButton = new Button(sdmDebugComp, SWT.CHECK);
		debugRoutingButton.setText(Messages.SDMPreferencePage_9);
		gd = new GridData();
		gd.horizontalIndent = 20;
		debugRoutingButton.setLayoutData(gd);
		debugRoutingButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (debugRoutingButton.getSelection()) {
					debugLevel |= SDMPreferenceConstants.DEBUG_LEVEL_ROUTING;
				} else {
					debugLevel &= ~ SDMPreferenceConstants.DEBUG_LEVEL_ROUTING;
				}
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		debugServerButton = new Button(sdmDebugComp, SWT.CHECK);
		debugServerButton.setText(Messages.SDMPreferencePage_10);
		gd = new GridData();
		gd.horizontalIndent = 20;
		debugServerButton.setLayoutData(gd);
		debugServerButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (debugServerButton.getSelection()) {
					debugLevel |= SDMPreferenceConstants.DEBUG_LEVEL_SERVER;
				} else {
					debugLevel &= ~ SDMPreferenceConstants.DEBUG_LEVEL_SERVER;
				}
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		debugBackendButton = new Button(sdmDebugComp, SWT.CHECK);
		debugBackendButton.setText(Messages.SDMPreferencePage_11);
		gd = new GridData();
		gd.horizontalIndent = 20;
		debugBackendButton.setLayoutData(gd);
		debugBackendButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (debugBackendButton.getSelection()) {
					debugLevel |= SDMPreferenceConstants.DEBUG_LEVEL_BACKEND;
				} else {
					debugLevel &= ~ SDMPreferenceConstants.DEBUG_LEVEL_BACKEND;
				}
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		debugProtocolButton = new Button(sdmDebugComp, SWT.CHECK);
		debugProtocolButton.setText(Messages.SDMPreferencePage_12);
		gd = new GridData();
		gd.horizontalIndent = 20;
		debugProtocolButton.setLayoutData(gd);
		debugProtocolButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (debugProtocolButton.getSelection()) {
					debugLevel |= SDMPreferenceConstants.DEBUG_LEVEL_PROTOCOL;
				} else {
					debugLevel &= ~ SDMPreferenceConstants.DEBUG_LEVEL_PROTOCOL;
				}
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		Composite masterDebugComp = createComposite(debugGroup, 1);
		gd = new GridData();
		gd.verticalAlignment = SWT.TOP;
		masterDebugComp.setLayoutData(gd);
		
		debugMasterEnabledButton = new Button(masterDebugComp, SWT.CHECK);
		debugMasterEnabledButton.setText(Messages.SDMPreferencePage_13);
		debugMasterEnabledButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				debugMasterEnabled = debugMasterEnabledButton.getSelection();
				updateDebugButtons();
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		debugMasterTraceButton = new Button(masterDebugComp, SWT.CHECK);
		debugMasterTraceButton.setText(Messages.SDMPreferencePage_14);
		gd = new GridData();
		gd.horizontalIndent = 20;
		debugMasterTraceButton.setLayoutData(gd);
		debugMasterTraceButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (debugMasterTraceButton.getSelection()) {
					debugMasterLevel |= SDMPreferenceConstants.DEBUG_MASTER_TRACING;
				} else {
					debugMasterLevel &= ~ SDMPreferenceConstants.DEBUG_MASTER_TRACING;
				}
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		debugMasterTraceMoreButton = new Button(masterDebugComp, SWT.CHECK);
		debugMasterTraceMoreButton.setText(Messages.SDMPreferencePage_15);
		gd = new GridData();
		gd.horizontalIndent = 20;
		debugMasterTraceMoreButton.setLayoutData(gd);
		debugMasterTraceMoreButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (debugMasterTraceMoreButton.getSelection()) {
					debugMasterLevel |= SDMPreferenceConstants.DEBUG_MASTER_TRACING_MORE;
				} else {
					debugMasterLevel &= ~ SDMPreferenceConstants.DEBUG_MASTER_TRACING_MORE;
				}
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		debugMasterOutputButton = new Button(masterDebugComp, SWT.CHECK);
		debugMasterOutputButton.setText(Messages.SDMPreferencePage_16);
		gd = new GridData();
		gd.horizontalIndent = 20;
		debugMasterOutputButton.setLayoutData(gd);
		debugMasterOutputButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (debugMasterOutputButton.getSelection()) {
					debugMasterLevel |= SDMPreferenceConstants.DEBUG_MASTER_OUTPUT;
				} else {
					debugMasterLevel &= ~ SDMPreferenceConstants.DEBUG_MASTER_OUTPUT;
				}
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		updateDebugButtons();
		
		Dialog.applyDialogFont(parent);
	}
	
	private void updateDebugButtons() {
		debugEnabledButton.setSelection(debugEnabled);
		
		debugStartupButton.setEnabled(debugEnabled);
		debugMessagesButton.setEnabled(debugEnabled);
		debugRoutingButton.setEnabled(debugEnabled);
		debugServerButton.setEnabled(debugEnabled);
		debugBackendButton.setEnabled(debugEnabled);
		debugProtocolButton.setEnabled(debugEnabled);
		
		debugStartupButton.setSelection((debugLevel & SDMPreferenceConstants.DEBUG_LEVEL_STARTUP) == SDMPreferenceConstants.DEBUG_LEVEL_STARTUP);
		debugMessagesButton.setSelection((debugLevel & SDMPreferenceConstants.DEBUG_LEVEL_MESSAGES) == SDMPreferenceConstants.DEBUG_LEVEL_MESSAGES);
		debugRoutingButton.setSelection((debugLevel & SDMPreferenceConstants.DEBUG_LEVEL_ROUTING) == SDMPreferenceConstants.DEBUG_LEVEL_ROUTING);
		debugServerButton.setSelection((debugLevel & SDMPreferenceConstants.DEBUG_LEVEL_SERVER) == SDMPreferenceConstants.DEBUG_LEVEL_SERVER);
		debugBackendButton.setSelection((debugLevel & SDMPreferenceConstants.DEBUG_LEVEL_BACKEND) == SDMPreferenceConstants.DEBUG_LEVEL_BACKEND);
		debugProtocolButton.setSelection((debugLevel & SDMPreferenceConstants.DEBUG_LEVEL_PROTOCOL) == SDMPreferenceConstants.DEBUG_LEVEL_PROTOCOL);

		debugMasterEnabledButton.setSelection(debugMasterEnabled);
		
		debugMasterTraceButton.setEnabled(debugMasterEnabled);
		debugMasterTraceMoreButton.setEnabled(debugMasterEnabled);
		debugMasterOutputButton.setEnabled(debugMasterEnabled);
		
		debugMasterTraceButton.setSelection((debugMasterLevel & SDMPreferenceConstants.DEBUG_MASTER_TRACING) == SDMPreferenceConstants.DEBUG_MASTER_TRACING);
		debugMasterTraceMoreButton.setSelection((debugMasterLevel & SDMPreferenceConstants.DEBUG_MASTER_TRACING_MORE) == SDMPreferenceConstants.DEBUG_MASTER_TRACING_MORE);
		debugMasterOutputButton.setSelection((debugMasterLevel & SDMPreferenceConstants.DEBUG_MASTER_OUTPUT) == SDMPreferenceConstants.DEBUG_MASTER_OUTPUT);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.preferences.AbstractPreferencePage#setValues()
	 */
	protected void setValues() {
		IPreferenceStore store = getPreferenceStore();
		sdmArgsText.setText(store.getString(SDMPreferenceConstants.SDM_DEBUGGER_ARGS));
		sdmBackendCombo.select(sdmBackendCombo.indexOf(store.getString(SDMPreferenceConstants.SDM_DEBUGGER_BACKEND_TYPE)));
		sdmBackendPathText.setText(store.getString(SDMPreferenceConstants.SDM_DEBUGGER_BACKEND_PATH));
		debugEnabled = store.getBoolean(SDMPreferenceConstants.SDM_DEBUG_ENABLED);
		debugLevel = store.getInt(SDMPreferenceConstants.SDM_DEBUG_LEVEL);
		debugMasterEnabled = store.getBoolean(SDMPreferenceConstants.SDM_DEBUG_MASTER_ENABLED);
		debugMasterLevel = store.getInt(SDMPreferenceConstants.SDM_DEBUG_MASTER_LEVEL);
		updateDebugButtons();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.preferences.AbstractPreferencePage#storeValues()
	 */
	protected void storeValues() {
		IPreferenceStore store = getPreferenceStore();
		store.setValue(SDMPreferenceConstants.SDM_DEBUGGER_ARGS, sdmArgsText.getText());
		store.setValue(SDMPreferenceConstants.SDM_DEBUG_ENABLED, debugEnabled);
		store.setValue(SDMPreferenceConstants.SDM_DEBUG_LEVEL, debugLevel);
		store.setValue(SDMPreferenceConstants.SDM_DEBUG_MASTER_ENABLED, debugMasterEnabled);
		store.setValue(SDMPreferenceConstants.SDM_DEBUG_MASTER_LEVEL, debugMasterLevel);
		store.setValue(SDMPreferenceConstants.SDM_DEBUGGER_BACKEND_TYPE, sdmBackendCombo.getItem(sdmBackendCombo.getSelectionIndex()));
		store.setValue(SDMPreferenceConstants.SDM_DEBUGGER_BACKEND_PATH, sdmBackendPathText.getText());
	}
}
