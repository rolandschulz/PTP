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
package org.eclipse.ptp.rm.remote.ui.preferences;

import java.io.File;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ptp.remote.IRemoteProxyOptions;
import org.eclipse.ptp.rm.remote.ui.Messages;
import org.eclipse.ptp.ui.utils.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public abstract class AbstractRemotePreferencePage extends PreferencePage implements IWorkbenchPreferencePage, PreferenceConstants 
{
	protected class WidgetListener extends SelectionAdapter implements ModifyListener, IPropertyChangeListener 
	{
		public void modifyText(ModifyEvent evt) {
			Object source = evt.getSource();
			if(!loading && source == serverText)
				updatePreferencePage();
		}

		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(FieldEditor.IS_VALID))
				updatePreferencePage();
		}

		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			if (source == browseButton)
				handlePathBrowseButtonSelected();
			else {
				if (fStdioButton.getSelection()) {
					fManualButton.setEnabled(false);
					fManualButton.setSelection(false);
				} else {
					fManualButton.setEnabled(true);
				}
				updatePreferencePage();
			}
		}
	}

	public static final String EMPTY_STRING = "";
	private String serverFile = EMPTY_STRING;
	private boolean loading = true;
	protected Text serverText = null;

	protected Button browseButton = null;

	protected Button fNoneButton = null;
	protected Button fStdioButton = null;
	protected Button fPortForwardingButton = null;
	protected Button fManualButton = null;

	protected WidgetListener listener = new WidgetListener();

	public AbstractRemotePreferencePage() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#dispose()
	 */
	public void dispose() 
	{
		super.dispose();
	}

	/**
	 * Gets the preference settings to use for the RM. Each RM should supply
	 * different preference settings.
	 * 
	 * @return table of prefence settings
	 */
	public abstract Preferences getPreferences();
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) 
	{
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	public void performDefaults() 
	{
		defaultSetting();
		updateApplyButton();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	public boolean performOk() 
	{
		store();
		Preferences preferences = getPreferences();

		preferences.setValue(PreferenceConstants.PROXY_PATH, serverFile);
		
		int options = 0;
		if (fStdioButton.getSelection()) {
			options |= IRemoteProxyOptions.STDIO;
		}
		if (fPortForwardingButton.getSelection()) {
			options |= IRemoteProxyOptions.PORT_FORWARDING;
		}
		if (fManualButton.getSelection()) {
			options |= IRemoteProxyOptions.MANUAL_LAUNCH;
		}
		preferences.setValue(PreferenceConstants.OPTIONS, options);

		savePreferences();

		return true;
	}
	
	/**
	 * Called to save the current preferences to the store.
	 */
	public abstract void savePreferences();

	/**
	 * 
	 */
	private void loadSaved()
	{
		Preferences preferences = getPreferences();
		
		serverFile = preferences.getString(PreferenceConstants.PROXY_PATH);
		serverText.setText(serverFile);
		
		int options = preferences.getInt(PreferenceConstants.OPTIONS);
		if ((options & IRemoteProxyOptions.STDIO) == IRemoteProxyOptions.STDIO) {
			fStdioButton.setSelection(true);
		} else if ((options & IRemoteProxyOptions.PORT_FORWARDING) == IRemoteProxyOptions.PORT_FORWARDING) {
			fPortForwardingButton.setSelection(true);
		} else {
			fNoneButton.setSelection(true);
		}

		if (fStdioButton.getSelection()) {
			fManualButton.setEnabled(false);
			fManualButton.setSelection(false);
		} else {
			fManualButton.setSelection(
				(options & IRemoteProxyOptions.MANUAL_LAUNCH) == IRemoteProxyOptions.MANUAL_LAUNCH);
		}
	}
	
	/**
	 * 
	 */
	private void store() 
	{
		serverFile = serverText.getText();
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
	 * Creates an new radiobutton instance and sets the default
	 * layout data.
	 *
	 * @param group  the composite in which to create the radiobutton
	 * @param label  the string to set into the radiobutton
	 * @param value  the string to identify radiobutton
	 * @return the new checkbox
	 */ 
	private Button createRadioButton(Composite parent, String label, String value, SelectionListener listener) {
		Button button = createButton(parent, label, SWT.RADIO | SWT.LEFT);
		button.setData((null == value) ? label : value);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.BEGINNING;
		button.setLayoutData(data);
		if(null != listener)
			button.addSelectionListener(listener);
		return button;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) 
	{
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(createGridLayout(1, true, 0, 0));
		composite.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));

		Group bGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
		bGroup.setLayout(createGridLayout(1, true, 10, 10));
		bGroup.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));
		bGroup.setText(Messages.getString("RemotePreferencesPage.group_server"));
		
		new Label(bGroup, SWT.WRAP).setText(Messages
				.getString("RemotePreferencesPage.server_text"));
		
		Composite orteserver = new Composite(bGroup, SWT.NONE);
		orteserver.setLayout(createGridLayout(3, false, 0, 0));
		orteserver.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));
		
		new Label(orteserver, SWT.NONE).setText(Messages
				.getString("RemotePreferencesPage.server_label"));
		serverText = new Text(orteserver, SWT.SINGLE | SWT.BORDER);
		serverText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		serverText.addModifyListener(listener);
		browseButton = SWTUtil.createPushButton(orteserver, Messages
				.getString("RemotePreferencesPage.browseButton"), null);
		browseButton.addSelectionListener(listener);
		
		Group mxGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
		mxGroup.setLayout(createGridLayout(1, true, 10, 10));
		mxGroup.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));
		mxGroup.setText(Messages.getString("RemotePreferencesPage.mxOptions"));
		
		fNoneButton = createRadioButton(mxGroup, Messages.getString("RemotePreferencesPage.noneButton"), "mxGroup", listener);
		fPortForwardingButton = createRadioButton(mxGroup, Messages.getString("RemotePreferencesPage.portForwardingButton"), "mxGroup", listener);
		fStdioButton = createRadioButton(mxGroup, Messages.getString("RemotePreferencesPage.stdioButton"), "mxGroup", listener);

		Group otherGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
		otherGroup.setLayout(createGridLayout(1, true, 10, 10));
		otherGroup.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));
		otherGroup.setText(Messages.getString("RemotePreferencesPage.otherOptions"));

		fManualButton = createCheckButton(otherGroup, Messages.getString("RemotePreferencesPage.manualButton"));

		loading = true;
		loadSaved();
		loading = false;
		
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
	protected GridLayout createGridLayout(int columns, boolean isEqual, int mh, int mw)  {
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
	protected void defaultSetting() 
	{
		serverText.setText(serverFile);
	}

	/**
	 * @param text
	 * @return
	 */
	protected String getFieldContent(String text) 
	{
		if (text.trim().length() == 0 || text.equals(EMPTY_STRING))
			return null;

		return text;
	}
	
	/**
	 * Show a dialog that lets the user select a file
	 */
	protected void handlePathBrowseButtonSelected() 
	{
		FileDialog dialog = new FileDialog(getShell());
		dialog.setText(Messages
				.getString("RemotePreferencesPage.Select"));
		String correctPath = getFieldContent(serverText.getText());
		if (correctPath != null) {
			File path = new File(correctPath);
			if (path.exists())
				dialog.setFilterPath(path.isFile() ? correctPath : path
						.getParent());
		}

		String selectedPath = dialog.open();
		if (selectedPath != null)
			serverText.setText(selectedPath);
	}

	/**
	 * @return
	 */
	protected boolean isValidSetting() 
	{
		String name = getFieldContent(serverText.getText());
		if (name == null) {
			setErrorMessage(Messages
					.getString("RemotePreferencesPage.Invalid"));
			//setValid(false);
			//return false;
		}
		else {
			File path = new File(name);
			if (!path.exists() || !path.isFile()) {
				setErrorMessage(Messages
					.getString("RemotePreferencesPage.Invalid"));
				//setValid(false);
				//return false;
			}
		}

		return true;
	}

	/**
	 * @param style
	 * @param space
	 * @return
	 */
	protected GridData spanGridData(int style, int space) 
	{
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
	protected void updatePreferencePage() 
	{
		setErrorMessage(null);
		setMessage(null);

		if (!isValidSetting())
			return;

		performOk();
		setValid(true);
	}
}