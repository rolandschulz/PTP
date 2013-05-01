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
package org.eclipse.ptp.internal.debug.ui.preferences;

import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.debug.core.IPDebugConstants;
import org.eclipse.ptp.internal.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.internal.debug.ui.PDebugModelPresentation;
import org.eclipse.ptp.internal.debug.ui.PreferencesAdapter;
import org.eclipse.ptp.internal.debug.ui.messages.Messages;
import org.eclipse.ptp.internal.ui.preferences.AbstractPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;

/**
 * @author Clement chu
 */
public class PDebugPreferencePage extends AbstractPreferencePage {
	private Button fPathsButton = null;
	private Button fRegisteredProcessButton = null;
	private IntegerFieldEditor commandTimeoutField = null;
	private Button updateVariableOnSuspendButton = null;
	private Button updateVariableOnChangeButton = null;

	protected class WidgetListener implements IPropertyChangeListener {
		public void propertyChange(PropertyChangeEvent event) {
			setValid(isValid());
		}
	}

	protected WidgetListener listener = new WidgetListener();

	/**
	 * Constructor
	 */
	public PDebugPreferencePage() {
		super();
		setDescription(Messages.PDebugPreferencePage_0);
		setPreferenceStore(new PreferencesAdapter(PTPDebugCorePlugin.getUniqueIdentifier()));
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
		createViewSettingPreferences(composite);
		createSpacer(composite, 1);
		createCommunicationPreferences(composite);
		createSpacer(composite, 1);
		createVariablesPreferences(composite);
		setValues();
		return composite;
	}

	/**
	 * Create other debug settings
	 * 
	 * @param parent
	 */
	protected void createOtherDebugSetting(Composite parent) {
	}

	/**
	 * Create view preference settings
	 * 
	 * @param parent
	 */
	protected void createViewSettingPreferences(Composite parent) {
		Composite comp = createGroupComposite(parent, 1, false, Messages.PDebugPreferencePage_1);
		fPathsButton = createCheckButton(comp, Messages.PDebugPreferencePage_2);
		fRegisteredProcessButton = createCheckButton(comp, Messages.PDebugPreferencePage_3);
		fRegisteredProcessButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				boolean isChecked = fRegisteredProcessButton.getSelection();
				if (!isChecked) {
					MessageDialog.openWarning(getShell(), Messages.PDebugPreferencePage_4, Messages.PDebugPreferencePage_5);
				}
			}
		});
	}

	/**
	 * Create communication preference settings
	 * 
	 * @param parent
	 */
	protected void createCommunicationPreferences(Composite parent) {
		Composite comp = createGroupComposite(parent, 1, false, Messages.PDebugPreferencePage_6);
		Composite spacingComposite = new Composite(comp, SWT.NONE);
		spacingComposite.setLayout(new GridLayout());
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		spacingComposite.setLayoutData(data);
		commandTimeoutField = new IntegerFieldEditor(IPDebugConstants.PREF_DEBUG_COMM_TIMEOUT, Messages.PDebugPreferencePage_7,
				spacingComposite);
		commandTimeoutField.setPreferenceStore(getPreferenceStore());
		commandTimeoutField.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
		commandTimeoutField.setValidRange(IPDebugConstants.MIN_REQUEST_TIMEOUT, IPDebugConstants.MAX_REQUEST_TIMEOUT);
		String minValue = Integer.toString(IPDebugConstants.MIN_REQUEST_TIMEOUT);
		String maxValue = Integer.toString(IPDebugConstants.MAX_REQUEST_TIMEOUT);
		commandTimeoutField.setErrorMessage(NLS.bind(Messages.PDebugPreferencePage_8, new Object[] { minValue, maxValue }));
		commandTimeoutField.setEmptyStringAllowed(false);
		commandTimeoutField.setPropertyChangeListener(listener);
		commandTimeoutField.load();
	}

	/**
	 * Create variable preference settings
	 * 
	 * @param parent
	 */
	protected void createVariablesPreferences(Composite parent) {
		Composite comp = createGroupComposite(parent, 1, false, Messages.PDebugPreferencePage_9);
		updateVariableOnSuspendButton = createCheckButton(comp, Messages.PDebugPreferencePage_10);
		updateVariableOnChangeButton = createCheckButton(comp, Messages.PDebugPreferencePage_11);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	@Override
	public void performDefaults() {
		IPreferenceStore store = getPreferenceStore();
		fPathsButton.setSelection(store.getDefaultBoolean(IPDebugConstants.PREF_SHOW_FULL_PATHS));
		fRegisteredProcessButton.setSelection(store.getDefaultBoolean(IPDebugConstants.PREF_DEBUG_REGISTER_PROC_0));
		commandTimeoutField.setStringValue(String.valueOf(store.getDefaultInt(IPDebugConstants.PREF_DEBUG_COMM_TIMEOUT)));
		updateVariableOnSuspendButton.setSelection(store.getDefaultBoolean(IPDebugConstants.PREF_UPDATE_VARIABLES_ON_SUSPEND));
		updateVariableOnChangeButton.setSelection(store.getDefaultBoolean(IPDebugConstants.PREF_UPDATE_VARIABLES_ON_CHANGE));
		commandTimeoutField.loadDefault();
		super.performDefaults();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		storeValues();
		Preferences.savePreferences(PTPDebugCorePlugin.getUniqueIdentifier());
		refreshView();
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.preferences.AbstractPreferencePage#setValues()
	 */
	@Override
	protected void setValues() {
		IPreferenceStore store = getPreferenceStore();
		fPathsButton.setSelection(store.getBoolean(IPDebugConstants.PREF_SHOW_FULL_PATHS));
		fRegisteredProcessButton.setSelection(store.getBoolean(IPDebugConstants.PREF_DEBUG_REGISTER_PROC_0));
		commandTimeoutField.setStringValue(String.valueOf(store.getInt(IPDebugConstants.PREF_DEBUG_COMM_TIMEOUT)));
		updateVariableOnSuspendButton.setSelection(store.getBoolean(IPDebugConstants.PREF_UPDATE_VARIABLES_ON_SUSPEND));
		updateVariableOnChangeButton.setSelection(store.getBoolean(IPDebugConstants.PREF_UPDATE_VARIABLES_ON_CHANGE));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.DialogPage#dispose()
	 */
	@Override
	public void dispose() {
		commandTimeoutField.dispose();
		fPathsButton.dispose();
		fRegisteredProcessButton.dispose();
		updateVariableOnSuspendButton.dispose();
		updateVariableOnChangeButton.dispose();
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.preferences.AbstractPreferencePage#storeValues()
	 */
	@Override
	protected void storeValues() {
		IPreferenceStore store = getPreferenceStore();
		store.setValue(IPDebugConstants.PREF_SHOW_FULL_PATHS, fPathsButton.getSelection());
		store.setValue(IPDebugConstants.PREF_DEBUG_REGISTER_PROC_0, fRegisteredProcessButton.getSelection());
		store.setValue(IPDebugConstants.PREF_DEBUG_COMM_TIMEOUT, commandTimeoutField.getIntValue());
		store.setValue(IPDebugConstants.PREF_UPDATE_VARIABLES_ON_SUSPEND, updateVariableOnSuspendButton.getSelection());
		store.setValue(IPDebugConstants.PREF_UPDATE_VARIABLES_ON_CHANGE, updateVariableOnChangeButton.getSelection());
		commandTimeoutField.store();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferencePage#isValid()
	 */
	@Override
	public boolean isValid() {
		setErrorMessage(null);
		setMessage(null);
		if (!commandTimeoutField.isValid()) {
			setErrorMessage(commandTimeoutField.getErrorMessage());
			return false;
		}
		return true;
	}

	/**
	 * Refresh debug breakpoint view
	 */
	protected void refreshView() {
		IWorkbenchPage[] pages = getPages();
		for (int i = 0; i < pages.length; i++) {
			IViewPart part = pages[i].findView(IDebugUIConstants.ID_BREAKPOINT_VIEW);
			if (part != null) {
				IDebugView adapter = (IDebugView) part.getAdapter(IDebugView.class);
				if (adapter != null) {
					Viewer viewer = adapter.getViewer();
					IDebugModelPresentation pres = adapter.getPresentation(PTPDebugCorePlugin.getUniqueIdentifier());
					if (pres != null) {
						pres.setAttribute(PDebugModelPresentation.DISPLAY_FULL_PATHS, fPathsButton.getSelection() ? Boolean.TRUE
								: Boolean.FALSE);
					}
					if (viewer instanceof StructuredViewer) {
						final StructuredViewer structViewer = (StructuredViewer) viewer;
						BusyIndicator.showWhile(structViewer.getControl().getDisplay(), new Runnable() {
							public void run() {
								structViewer.refresh();
							}
						});
					}
				}
			}
		}
	}
}
