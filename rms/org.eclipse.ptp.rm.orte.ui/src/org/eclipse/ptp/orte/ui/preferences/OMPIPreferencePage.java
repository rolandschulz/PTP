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
package org.eclipse.ptp.orte.ui.preferences;

import java.io.File;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.orte.ui.internal.ui.Messages;
import org.eclipse.ptp.ui.PTPUIPlugin;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class OMPIPreferencePage extends PreferencePage implements IWorkbenchPreferencePage, PreferenceConstants 
{
	public static final String EMPTY_STRING = "";

	protected Text orteServerText = null;

	protected Button browseButton = null;
	protected Button fManualButton = null;

	private String orteServerFile = EMPTY_STRING;

	private boolean loading = true;

	public OMPIPreferencePage() {
		setPreferenceStore(PTPUIPlugin.getDefault().getPreferenceStore());
	}

	protected class WidgetListener extends SelectionAdapter implements ModifyListener, IPropertyChangeListener 
	{
		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			if (source == browseButton)
				handlePathBrowseButtonSelected();
			else
				updatePreferencePage();
		}

		public void modifyText(ModifyEvent evt) {
			Object source = evt.getSource();
			if(!loading && source == orteServerText)
				updatePreferencePage();
		}

		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(FieldEditor.IS_VALID))
				updatePreferencePage();
		}
	}

	protected WidgetListener listener = new WidgetListener();

	protected Control createContents(Composite parent) 
	{
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(createGridLayout(1, true, 0, 0));
		composite.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));

		createORTEContents(composite);

		loading = true;
		loadSaved();
		loading = false;
		
		defaultSetting();
		return composite;
	}

	private void createORTEContents(Composite parent)
	{
		Group bGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		bGroup.setLayout(createGridLayout(1, true, 10, 10));
		bGroup.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));
		bGroup.setText(Messages.getString("OMPIPreferencesPage.group_proxy"));
		
		new Label(bGroup, SWT.WRAP).setText("Enter the path to the PTP ORTE proxy server.");
		
		Composite orteserver = new Composite(bGroup, SWT.NONE);
		orteserver.setLayout(createGridLayout(3, false, 0, 0));
		orteserver.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));
		
		new Label(orteserver, SWT.NONE).setText(Messages
				.getString("OMPIPreferencesPage.orteServer_text"));
		orteServerText = new Text(orteserver, SWT.SINGLE | SWT.BORDER);
		orteServerText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		orteServerText.addModifyListener(listener);
		browseButton = SWTUtil.createPushButton(orteserver, Messages
				.getString("OMPIPreferencesPage.browseButton"), null);
		browseButton.addSelectionListener(listener);
		
		fManualButton = createCheckButton(parent, Messages.getString("OMPIPreferencesPage.manual"));

	}
	protected Button createCheckButton(Composite parent, String label) {
		return createButton(parent, label, SWT.CHECK | SWT.LEFT);
	}
	protected Button createButton(Composite parent, String label, int type) {
		Button button = new Button(parent, type);
		button.setText(label);
		GridData data = new GridData();
		button.setLayoutData(data);
		return button;
	}
	protected void defaultSetting() 
	{
		orteServerText.setText(orteServerFile);
	}
	
	private void loadSaved()
	{
		Preferences preferences = PTPCorePlugin.getDefault().getPluginPreferences();
		
		orteServerFile = preferences.getString(PreferenceConstants.ORTE_PROXY_PATH);
		/* if they don't have the ptp_orte_proxy path set, let's try and give them a default that might help */
		if(orteServerFile.equals("")) {
			orteServerFile = PTPCorePlugin.getDefault().locateFragmentFile("org.eclipse.ptp", "ptp_orte_proxy");
        }
		
		if (orteServerFile == null) {
			orteServerFile = "";
		}
		
		orteServerText.setText(orteServerFile);
		fManualButton.setSelection(preferences.getBoolean(PreferenceConstants.ORTE_LAUNCH_MANUALLY));
	}

	public void init(IWorkbench workbench) 
	{
	}

	public void dispose() 
	{
		super.dispose();
	}

	public void performDefaults() 
	{
		defaultSetting();
		updateApplyButton();
	}

	private void store() 
	{
		orteServerFile = orteServerText.getText();
	}

	public boolean performOk() 
	{
		store();
		Preferences preferences = PTPCorePlugin.getDefault().getPluginPreferences();

		preferences.setValue(PreferenceConstants.ORTE_PROXY_PATH, orteServerFile);
		preferences.setValue(PreferenceConstants.ORTE_LAUNCH_MANUALLY, fManualButton.getSelection());

		PTPCorePlugin.getDefault().savePluginPreferences();

		return true;
	}

	/**
	 * Show a dialog that lets the user select a file
	 */
	protected void handlePathBrowseButtonSelected() 
	{
		FileDialog dialog = new FileDialog(getShell());
		dialog.setText(Messages
				.getString("OMPIPreferencesPage.Select_ORTE_PROXY_FILE"));
		String correctPath = getFieldContent(orteServerText.getText());
		if (correctPath != null) {
			File path = new File(correctPath);
			if (path.exists())
				dialog.setFilterPath(path.isFile() ? correctPath : path
						.getParent());
		}

		String selectedPath = dialog.open();
		if (selectedPath != null)
			orteServerText.setText(selectedPath);
	}

	protected boolean isValidORTEdSetting() 
	{
		String name = getFieldContent(orteServerText.getText());
		if (name == null) {
			setErrorMessage(Messages
					.getString("OMPIPreferencesPage.Incorrect_server_file"));
			//setValid(false);
			//return false;
		}
		else {
			File path = new File(name);
			if (!path.exists() || !path.isFile()) {
				setErrorMessage(Messages
					.getString("OMPIPreferencesPage.Incorrect_server_file"));
				//setValid(false);
				//return false;
			}
		}

		return true;
	}
	
	protected void updatePreferencePage() 
	{
		setErrorMessage(null);
		setMessage(null);

		if (!isValidORTEdSetting())
			return;

		performOk();
		setValid(true);
	}

	protected String getFieldContent(String text) 
	{
		if (text.trim().length() == 0 || text.equals(EMPTY_STRING))
			return null;

		return text;
	}

	protected GridLayout createGridLayout(int columns, boolean isEqual, int mh, int mw)  {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = columns;
		gridLayout.makeColumnsEqualWidth = isEqual;
		gridLayout.marginHeight = mh;
		gridLayout.marginWidth = mw;
		return gridLayout;
	}

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
}