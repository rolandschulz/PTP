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
package org.eclipse.ptp.rm.ui.preferences;


import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.remote.core.IRemoteProxyOptions;
import org.eclipse.ptp.rm.core.RMPreferenceConstants;
import org.eclipse.ptp.rm.ui.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public abstract class AbstractRemoteRMPreferencePage extends PreferencePage implements IWorkbenchPreferencePage,
		RMPreferenceConstants {
	protected class WidgetListener extends SelectionAdapter implements IPropertyChangeListener {
		

		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(FieldEditor.IS_VALID))
				updatePreferencePage();
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			fManualButton.setEnabled(true);
			updatePreferencePage();
		}
	}

	public static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private String serverFile = EMPTY_STRING;
	private boolean loading = true;

	protected Button fNoneButton = null;
	protected Button fPortForwardingButton = null;
	protected Button fManualButton = null;

	protected WidgetListener listener = new WidgetListener();

	public AbstractRemoteRMPreferencePage() {
		// Empty
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.DialogPage#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
	}

	/**
	 * Gets the preference qualifier used to access settings for the RM. Each RM
	 * should supply different preference qualifier (usually the plugin ID).
	 * 
	 * @return preference qualifier
	 * @since 2.0
	 */
	public abstract String getPreferenceQualifier();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		// Nothing to do
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	@Override
	public void performDefaults() {
		loadDefaults();
		defaultSetting();
		updateApplyButton();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {

		int options = 0;
		if (fPortForwardingButton.getSelection()) {
			options |= IRemoteProxyOptions.PORT_FORWARDING;
		}
		if (fManualButton.getSelection()) {
			options |= IRemoteProxyOptions.MANUAL_LAUNCH;
		}
		Preferences.setInt(getPreferenceQualifier(), RMPreferenceConstants.OPTIONS, options);

		savePreferences();

		return true;
	}

	/**
	 * Called to save the current preferences to the store.
	 */
	public abstract void savePreferences();

	/**
	 * Update options fields in UI
	 */
	private void updateOptions(int options) {
		fPortForwardingButton.setSelection(false);
		fNoneButton.setSelection(true);
		if ((options & IRemoteProxyOptions.PORT_FORWARDING) == IRemoteProxyOptions.PORT_FORWARDING) {
			fPortForwardingButton.setSelection(true);
			fNoneButton.setSelection(false);
		}

		fManualButton.setSelection((options & IRemoteProxyOptions.MANUAL_LAUNCH) == IRemoteProxyOptions.MANUAL_LAUNCH);

	}

	/**
	 * Load values from preference store
	 */
	private void loadSaved() {
		loading = true;
		
		updateOptions(Preferences.getInt(getPreferenceQualifier(), RMPreferenceConstants.OPTIONS));
		loading = false;
	}

	/**
	 * Load default values from preference store
	 */
	private void loadDefaults() {
		loading = true;

		updateOptions(Preferences.getDefaultInt(getPreferenceQualifier(), RMPreferenceConstants.OPTIONS, 0));
		loading = false;
	}

	/**
	 * @param parent
	 * @param label
	 * @param type
	 * @return
	 */
	protected Button createButton(Composite parent, String label, int type) {
		Button button = new Button(parent, type);
		button.setText(label);
		GridData data = new GridData();
		button.setLayoutData(data);
		return button;
	}

	/**
	 * @param parent
	 * @param label
	 * @return
	 */
	protected Button createCheckButton(Composite parent, String label) {
		return createButton(parent, label, SWT.CHECK | SWT.LEFT);
	}

	/**
	 * Creates an new radiobutton instance and sets the default layout data.
	 * 
	 * @param group
	 *            the composite in which to create the radiobutton
	 * @param label
	 *            the string to set into the radiobutton
	 * @param value
	 *            the string to identify radiobutton
	 * @return the new checkbox
	 */
	private Button createRadioButton(Composite parent, String label, String value, SelectionListener listener) {
		Button button = createButton(parent, label, SWT.RADIO | SWT.LEFT);
		button.setData((null == value) ? label : value);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.BEGINNING;
		button.setLayoutData(data);
		if (null != listener)
			button.addSelectionListener(listener);
		return button;
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
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(createGridLayout(1, true, 0, 0));
		composite.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));

		Group bGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
		bGroup.setLayout(createGridLayout(1, true, 10, 10));
		bGroup.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));
		bGroup.setText(Messages.AbstractRemotePreferencePage_0);

		

		Group mxGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
		mxGroup.setLayout(createGridLayout(1, true, 10, 10));
		mxGroup.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));
		mxGroup.setText(Messages.AbstractRemotePreferencePage_4);

		fNoneButton = createRadioButton(mxGroup, Messages.AbstractRemotePreferencePage_5, "mxGroup", listener); //$NON-NLS-1$
		fPortForwardingButton = createRadioButton(mxGroup, Messages.AbstractRemotePreferencePage_6, "mxGroup", listener); //$NON-NLS-1$

		Group otherGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
		otherGroup.setLayout(createGridLayout(1, true, 10, 10));
		otherGroup.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));
		otherGroup.setText(Messages.AbstractRemotePreferencePage_8);

		fManualButton = createCheckButton(otherGroup, Messages.AbstractRemotePreferencePage_9);

		loadSaved();
		defaultSetting();
		return composite;
	}

	/**
	 * @param columns
	 * @param isEqual
	 * @param mh
	 * @param mw
	 * @return
	 */
	protected GridLayout createGridLayout(int columns, boolean isEqual, int mh, int mw) {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = columns;
		gridLayout.makeColumnsEqualWidth = isEqual;
		gridLayout.marginHeight = mh;
		gridLayout.marginWidth = mw;
		return gridLayout;
	}

	/**
	 * 
	 */
	protected void defaultSetting() {
		// Empty
	}

	/**
	 * @param text
	 * @return
	 */
	protected String getFieldContent(String text) {
		if (text.trim().length() == 0 || text.equals(EMPTY_STRING))
			return null;

		return text;
	}



	/**
	 * @return
	 */
	protected boolean isValidSetting() {
		return true;
	}

	/**
	 * @param style
	 * @param space
	 * @return
	 */
	protected GridData spanGridData(int style, int space) {
		GridData gd = null;
		if (style == -1)
			gd = new GridData();
		else
			gd = new GridData(style);
		gd.horizontalSpan = space;
		return gd;
	}

	/**
	 * 
	 */
	protected void updatePreferencePage() {
		setErrorMessage(null);
		setMessage(null);

		if (!isValidSetting())
			return;

		setValid(true);
	}
}