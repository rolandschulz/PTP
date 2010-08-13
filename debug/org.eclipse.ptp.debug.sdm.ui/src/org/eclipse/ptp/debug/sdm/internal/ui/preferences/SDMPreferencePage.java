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
import org.eclipse.ptp.core.Preferences;
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

		@Override
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
	private Button debugMasterButton;
	private Button debugServerButton;
	private Button debugBackendButton;
	private Button debugProtocolButton;
	private Button debugClientEnabledButton;
	private Button debugClientTraceButton;
	private Button debugClientTraceMoreButton;
	private Button debugClientOutputButton;

	private boolean debugEnabled = false;
	private int debugLevel = SDMPreferenceConstants.DEBUG_LEVEL_NONE;
	private boolean debugClientEnabled = false;
	private int debugClientLevel = SDMPreferenceConstants.DEBUG_CLIENT_NONE;

	protected WidgetListener listener = new WidgetListener();

	public SDMPreferencePage() {
		super();
		setPreferenceStore(new PreferencesAdapter(SDMDebugCorePlugin.getUniqueIdentifier()));
		setDescription(Messages.SDMPreferencePage_0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#isValid()
	 */
	@Override
	public boolean isValid() {
		setErrorMessage(null);
		setMessage(null);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	@Override
	public void performDefaults() {
		IPreferenceStore store = getPreferenceStore();
		sdmArgsText.setText(store.getDefaultString(SDMPreferenceConstants.SDM_DEBUGGER_ARGS));
		sdmBackendCombo.select(sdmBackendCombo.indexOf(store.getDefaultString(SDMPreferenceConstants.SDM_DEBUGGER_BACKEND_TYPE)));
		sdmBackendPathText.setText(store.getDefaultString(SDMPreferenceConstants.SDM_DEBUGGER_BACKEND_PATH));
		debugEnabled = false;
		debugLevel = SDMPreferenceConstants.DEBUG_LEVEL_NONE;
		debugClientEnabled = false;
		debugClientLevel = SDMPreferenceConstants.DEBUG_CLIENT_NONE;
		updateDebugButtons();
		super.performDefaults();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		storeValues();
		Preferences.savePreferences(SDMDebugCorePlugin.getUniqueIdentifier());
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse
	 * .swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		// TODO ignored help
		// getWorkbench().getHelpSystem().setHelp(getControl(),
		// IPDebugHelpContextIds.P_DEBUG_PREFERENCE_PAGE);
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
				if (debugEnabled) {
					debugClientEnabled = true;
					debugClientLevel |= SDMPreferenceConstants.DEBUG_CLIENT_OUTPUT;
				}
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
					debugLevel &= ~SDMPreferenceConstants.DEBUG_LEVEL_STARTUP;
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
					debugLevel &= ~SDMPreferenceConstants.DEBUG_LEVEL_MESSAGES;
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
					debugLevel &= ~SDMPreferenceConstants.DEBUG_LEVEL_ROUTING;
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		debugMasterButton = new Button(sdmDebugComp, SWT.CHECK);
		debugMasterButton.setText(Messages.SDMPreferencePage_17);
		gd = new GridData();
		gd.horizontalIndent = 20;
		debugMasterButton.setLayoutData(gd);
		debugMasterButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (debugMasterButton.getSelection()) {
					debugLevel |= SDMPreferenceConstants.DEBUG_LEVEL_MASTER;
				} else {
					debugLevel &= ~SDMPreferenceConstants.DEBUG_LEVEL_MASTER;
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
					debugLevel &= ~SDMPreferenceConstants.DEBUG_LEVEL_SERVER;
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
					debugLevel &= ~SDMPreferenceConstants.DEBUG_LEVEL_BACKEND;
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
					debugLevel &= ~SDMPreferenceConstants.DEBUG_LEVEL_PROTOCOL;
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

		debugClientEnabledButton = new Button(masterDebugComp, SWT.CHECK);
		debugClientEnabledButton.setText(Messages.SDMPreferencePage_13);
		debugClientEnabledButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				debugClientEnabled = debugEnabled || debugClientEnabledButton.getSelection();
				updateDebugButtons();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		debugClientTraceButton = new Button(masterDebugComp, SWT.CHECK);
		debugClientTraceButton.setText(Messages.SDMPreferencePage_14);
		gd = new GridData();
		gd.horizontalIndent = 20;
		debugClientTraceButton.setLayoutData(gd);
		debugClientTraceButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (debugClientTraceButton.getSelection()) {
					debugClientLevel |= SDMPreferenceConstants.DEBUG_CLIENT_TRACING;
				} else {
					debugClientLevel &= ~SDMPreferenceConstants.DEBUG_CLIENT_TRACING;
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		debugClientTraceMoreButton = new Button(masterDebugComp, SWT.CHECK);
		debugClientTraceMoreButton.setText(Messages.SDMPreferencePage_15);
		gd = new GridData();
		gd.horizontalIndent = 20;
		debugClientTraceMoreButton.setLayoutData(gd);
		debugClientTraceMoreButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (debugClientTraceMoreButton.getSelection()) {
					debugClientLevel |= SDMPreferenceConstants.DEBUG_CLIENT_TRACING_MORE;
				} else {
					debugClientLevel &= ~SDMPreferenceConstants.DEBUG_CLIENT_TRACING_MORE;
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		debugClientOutputButton = new Button(masterDebugComp, SWT.CHECK);
		debugClientOutputButton.setText(Messages.SDMPreferencePage_16);
		gd = new GridData();
		gd.horizontalIndent = 20;
		debugClientOutputButton.setLayoutData(gd);
		debugClientOutputButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (debugClientOutputButton.getSelection()) {
					debugClientLevel |= SDMPreferenceConstants.DEBUG_CLIENT_OUTPUT;
				} else if (!debugEnabled) {
					debugClientLevel &= ~SDMPreferenceConstants.DEBUG_CLIENT_OUTPUT;
				}
				updateDebugButtons();
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
		debugMasterButton.setEnabled(debugEnabled);
		debugServerButton.setEnabled(debugEnabled);
		debugBackendButton.setEnabled(debugEnabled);
		debugProtocolButton.setEnabled(debugEnabled);

		debugStartupButton
				.setSelection((debugLevel & SDMPreferenceConstants.DEBUG_LEVEL_STARTUP) == SDMPreferenceConstants.DEBUG_LEVEL_STARTUP);
		debugMessagesButton
				.setSelection((debugLevel & SDMPreferenceConstants.DEBUG_LEVEL_MESSAGES) == SDMPreferenceConstants.DEBUG_LEVEL_MESSAGES);
		debugRoutingButton
				.setSelection((debugLevel & SDMPreferenceConstants.DEBUG_LEVEL_ROUTING) == SDMPreferenceConstants.DEBUG_LEVEL_ROUTING);
		debugMasterButton
				.setSelection((debugLevel & SDMPreferenceConstants.DEBUG_LEVEL_MASTER) == SDMPreferenceConstants.DEBUG_LEVEL_MASTER);
		debugServerButton
				.setSelection((debugLevel & SDMPreferenceConstants.DEBUG_LEVEL_SERVER) == SDMPreferenceConstants.DEBUG_LEVEL_SERVER);
		debugBackendButton
				.setSelection((debugLevel & SDMPreferenceConstants.DEBUG_LEVEL_BACKEND) == SDMPreferenceConstants.DEBUG_LEVEL_BACKEND);
		debugProtocolButton
				.setSelection((debugLevel & SDMPreferenceConstants.DEBUG_LEVEL_PROTOCOL) == SDMPreferenceConstants.DEBUG_LEVEL_PROTOCOL);

		debugClientEnabledButton.setSelection(debugClientEnabled);
		debugClientTraceButton.setEnabled(debugClientEnabled);
		debugClientTraceMoreButton.setEnabled(debugClientEnabled);
		debugClientOutputButton.setEnabled(debugClientEnabled);

		debugClientTraceButton
				.setSelection((debugClientLevel & SDMPreferenceConstants.DEBUG_CLIENT_TRACING) == SDMPreferenceConstants.DEBUG_CLIENT_TRACING);
		debugClientTraceMoreButton
				.setSelection((debugClientLevel & SDMPreferenceConstants.DEBUG_CLIENT_TRACING_MORE) == SDMPreferenceConstants.DEBUG_CLIENT_TRACING_MORE);
		debugClientOutputButton
				.setSelection((debugClientLevel & SDMPreferenceConstants.DEBUG_CLIENT_OUTPUT) == SDMPreferenceConstants.DEBUG_CLIENT_OUTPUT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.preferences.AbstractPreferencePage#setValues()
	 */
	@Override
	protected void setValues() {
		IPreferenceStore store = getPreferenceStore();
		sdmArgsText.setText(store.getString(SDMPreferenceConstants.SDM_DEBUGGER_ARGS));
		sdmBackendCombo.select(sdmBackendCombo.indexOf(store.getString(SDMPreferenceConstants.SDM_DEBUGGER_BACKEND_TYPE)));
		sdmBackendPathText.setText(store.getString(SDMPreferenceConstants.SDM_DEBUGGER_BACKEND_PATH));
		debugEnabled = store.getBoolean(SDMPreferenceConstants.SDM_DEBUG_ENABLED);
		debugLevel = store.getInt(SDMPreferenceConstants.SDM_DEBUG_LEVEL);
		debugClientEnabled = store.getBoolean(SDMPreferenceConstants.SDM_DEBUG_CLIENT_ENABLED);
		debugClientLevel = store.getInt(SDMPreferenceConstants.SDM_DEBUG_CLIENT_LEVEL);
		updateDebugButtons();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.preferences.AbstractPreferencePage#storeValues()
	 */
	@Override
	protected void storeValues() {
		IPreferenceStore store = getPreferenceStore();
		store.setValue(SDMPreferenceConstants.SDM_DEBUGGER_ARGS, sdmArgsText.getText());
		store.setValue(SDMPreferenceConstants.SDM_DEBUG_ENABLED, debugEnabled);
		store.setValue(SDMPreferenceConstants.SDM_DEBUG_LEVEL, debugLevel);
		store.setValue(SDMPreferenceConstants.SDM_DEBUG_CLIENT_ENABLED, debugClientEnabled);
		store.setValue(SDMPreferenceConstants.SDM_DEBUG_CLIENT_LEVEL, debugClientLevel);
		store.setValue(SDMPreferenceConstants.SDM_DEBUGGER_BACKEND_TYPE,
				sdmBackendCombo.getItem(sdmBackendCombo.getSelectionIndex()));
		store.setValue(SDMPreferenceConstants.SDM_DEBUGGER_BACKEND_PATH, sdmBackendPathText.getText());
	}
}
