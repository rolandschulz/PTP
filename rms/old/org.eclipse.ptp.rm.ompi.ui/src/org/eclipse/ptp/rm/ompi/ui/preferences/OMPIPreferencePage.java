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
package org.eclipse.ptp.rm.ompi.ui.preferences;

import java.io.File;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ptp.rm.ompi.core.OMPIPreferenceManager;
import org.eclipse.ptp.rm.ompi.ui.internal.ui.Messages;
import org.eclipse.ptp.ui.utils.SWTUtil;
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
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class OMPIPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	protected class WidgetListener extends SelectionAdapter implements ModifyListener, IPropertyChangeListener {
		public void modifyText(ModifyEvent evt) {
			Object source = evt.getSource();
			if(!loading && (source == launchCmdText || source == discoverCmdText || source == monitorCmdText || source == pathText)) {
				updatePreferencePage();
			}
		}

		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(FieldEditor.IS_VALID)) {
				updatePreferencePage();
			}
		}

		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			if (source == browseButton) {
				handlePathBrowseButtonSelected();
			}
		}
	}

	public static final String EMPTY_STRING = "";  //$NON-NLS-1$
	private String launchCmd = EMPTY_STRING;
	private String discoverCmd = EMPTY_STRING;
	private String monitorCmd = EMPTY_STRING;
	private String path = EMPTY_STRING;
	private boolean loading = true;
	
	protected Text launchCmdText = null;
	protected Text discoverCmdText = null;
	protected Text monitorCmdText = null;
	protected Text pathText = null;
	protected Button browseButton = null;

	protected WidgetListener listener = new WidgetListener();

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#dispose()
	 */
	public void dispose() 
	{
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.ui.preferences.AbstractRemotePreferencePage#getPreferences()
	 */
	public Preferences getPreferences() {
		return OMPIPreferenceManager.getPreferences();
	}
	
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
		loadDefaults();
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

		preferences.setValue(OMPIPreferenceManager.PREFS_LAUNCH_CMD, launchCmd);
		preferences.setValue(OMPIPreferenceManager.PREFS_DISCOVER_CMD, discoverCmd);
		preferences.setValue(OMPIPreferenceManager.PREFS_MONITOR_CMD, monitorCmd);
		preferences.setValue(OMPIPreferenceManager.PREFS_PATH, path);
		
		savePreferences();

		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.ui.preferences.AbstractRemotePreferencePage#savePreferences()
	 */
	public void savePreferences() {
		OMPIPreferenceManager.savePreferences();
	}
	
	/**
	 * Load values from preference store
	 */
	private void loadSaved()
	{
		loading = true;
		
		Preferences preferences = getPreferences();
		
		launchCmd = preferences.getString(OMPIPreferenceManager.PREFS_LAUNCH_CMD);
		discoverCmd = preferences.getString(OMPIPreferenceManager.PREFS_DISCOVER_CMD);
		monitorCmd = preferences.getString(OMPIPreferenceManager.PREFS_MONITOR_CMD);
		path = preferences.getString(OMPIPreferenceManager.PREFS_PATH);
		launchCmdText.setText(launchCmd);
		discoverCmdText.setText(discoverCmd);
		monitorCmdText.setText(monitorCmd);
		pathText.setText(path);
		
		loading = false;
	}
	
	/**
	 * Load default values from preference store
	 */
	private void loadDefaults()
	{
		loading = true;
		
		Preferences preferences = getPreferences();
		
		launchCmd = preferences.getDefaultString(OMPIPreferenceManager.PREFS_LAUNCH_CMD);
		discoverCmd = preferences.getDefaultString(OMPIPreferenceManager.PREFS_DISCOVER_CMD);
		monitorCmd = preferences.getDefaultString(OMPIPreferenceManager.PREFS_MONITOR_CMD);
		path = preferences.getDefaultString(OMPIPreferenceManager.PREFS_PATH);
		launchCmdText.setText(launchCmd);
		discoverCmdText.setText(discoverCmd);
		monitorCmdText.setText(monitorCmd);
		pathText.setText(path);
		
		loading = false;
	}
	
	/**
	 * 
	 */
	private void store() 
	{
		launchCmd = launchCmdText.getText();
		discoverCmd = discoverCmdText.getText();
		monitorCmd = monitorCmdText.getText();
		path = pathText.getText();
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) 
	{
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		composite.setLayout(layout);

		Label label = new Label(composite, SWT.NONE);
		label.setText(Messages.getString("OMPIPreferencesPage.launchCmd"));  //$NON-NLS-1$

		launchCmdText = new Text(composite, SWT.BORDER);
		launchCmdText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		launchCmdText.addModifyListener(listener);
		
		label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText(Messages.getString("OMPIPreferencesPage.discoverCmd"));  //$NON-NLS-1$

		discoverCmdText = new Text(composite, SWT.BORDER);
		discoverCmdText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		discoverCmdText.addModifyListener(listener);

		label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText(Messages.getString("OMPIPreferencesPage.monitorCmd"));  //$NON-NLS-1$

		monitorCmdText = new Text(composite, SWT.BORDER);
		monitorCmdText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		monitorCmdText.addModifyListener(listener);
		
		label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText(Messages.getString("OMPIPreferencesPage.path"));  //$NON-NLS-1$

		pathText = new Text(composite, SWT.BORDER);
		pathText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		pathText.addModifyListener(listener);
		
		browseButton = SWTUtil.createPushButton(composite, Messages.getString("OMPIPreferencesPage.browseButton"), null);  //$NON-NLS-1$
		browseButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		browseButton.addSelectionListener(listener);
		
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
		pathText.setText(path);
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
		DirectoryDialog dialog = new DirectoryDialog(getShell());
		dialog.setText(Messages
				.getString("OMPIPreferencesPage.select")); //$NON-NLS-1$
		String correctPath = getFieldContent(pathText.getText());
		if (correctPath != null) {
			File path = new File(correctPath);
			if (path.exists()) {
				dialog.setFilterPath(path.isFile() ? correctPath : path
						.getParent());
			}
		}

		String selectedPath = dialog.open();
		if (selectedPath != null) {
			pathText.setText(selectedPath);
		}
	}

	/**
	 * @return
	 */
	protected boolean isValidSetting() 
	{
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
		if (style == -1) {
			gd = new GridData();
		} else {
			gd = new GridData(style);
		}
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

		if (!isValidSetting()) {
			return;
		}

		setValid(true);
	}
}