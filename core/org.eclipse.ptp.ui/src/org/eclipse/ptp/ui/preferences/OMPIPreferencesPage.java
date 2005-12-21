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
package org.eclipse.ptp.ui.preferences;

import java.io.File;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.debug.internal.ui.SWTUtil;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.PreferenceConstants;
import org.eclipse.ptp.internal.core.CoreMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
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

public class OMPIPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage, PreferenceConstants 
{
	public static final String EMPTY_STRING = "";

	protected Text ortedPathText = null;
	protected Text ortedArgsText = null;
	protected Text ortedFullText = null;
	protected Text orteServerText = null;

	protected Button browseButton = null;
	protected Button browseButton2 = null;

	private String defaultOrtedArgs = "--scope public --seed --persistent";
	private String ortedArgs = EMPTY_STRING;
	private String ortedFile = EMPTY_STRING;
	private String orteServerFile = EMPTY_STRING;

	private boolean loading = true;

	public OMPIPreferencesPage() {
		setPreferenceStore(PTPCorePlugin.getDefault().getPreferenceStore());
	}

	protected class WidgetListener extends SelectionAdapter implements ModifyListener, IPropertyChangeListener 
	{
		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			if (source == browseButton)
				handlePathBrowseButtonSelected();
			else if (source == browseButton2)
				handlePathBrowseButtonSelected2();
			else
				updatePreferencePage();
		}

		public void modifyText(ModifyEvent evt) {
			Object source = evt.getSource();
			if(!loading && (source == ortedPathText || source == orteServerText))
				updatePreferencePage();
			else if(source == ortedArgsText) {
				ortedFullText.setText(ortedPathText.getText()+" "+ortedArgsText.getText());
			}
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

		createORTEdContents(composite);

		loading = true;
		loadSaved();
		loading = false;
		
		defaultSetting();
		return composite;
	}

	private void createORTEdContents(Composite parent)
	{
		Group aGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		aGroup.setLayout(createGridLayout(1, true, 10, 10));
		aGroup.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));
		aGroup.setText(CoreMessages.getResourceString("OMPIPreferencesPage.group_orted"));
		
		Label ortedComment = new Label(aGroup, SWT.WRAP);
		ortedComment.setText("Enter the path to the Open Runtime Environment Daemon (ORTEd).");
		ortedComment = new Label(aGroup, SWT.WRAP);
		ortedComment.setText("PTP will take care of starting and stopping this as necessary to interface to the ORTE.");
		
		Composite ortedFilecomposite = new Composite(aGroup, SWT.NONE);
		ortedFilecomposite.setLayout(createGridLayout(3, false, 0, 0));
		ortedFilecomposite.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));

		new Label(ortedFilecomposite, SWT.NONE).setText(CoreMessages
				.getResourceString("OMPIPreferencesPage.ortedFile_text"));
		ortedPathText = new Text(ortedFilecomposite, SWT.SINGLE | SWT.BORDER);
		ortedPathText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		ortedPathText.addModifyListener(listener);
		browseButton = SWTUtil.createPushButton(ortedFilecomposite, CoreMessages
				.getResourceString("PTPPreferencesPage.browseButton"), null);
		browseButton.addSelectionListener(listener);
		
		Composite ortedArgs = new Composite(aGroup, SWT.NONE);
		ortedArgs.setLayout(createGridLayout(2, false, 0, 0));
		ortedArgs.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));
		
		new Label(ortedArgs, SWT.NONE).setText(CoreMessages
				.getResourceString("OMPIPreferencesPage.ortedArgs_text"));
		ortedArgsText = new Text(ortedArgs, SWT.SINGLE | SWT.BORDER);
		ortedArgsText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		ortedArgsText.addModifyListener(listener);
		
		Composite ortedFull = new Composite(aGroup, SWT.NONE);
		ortedFull.setLayout(createGridLayout(2, false, 0, 0));
		ortedFull.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));
		
		new Label(ortedFull, SWT.NONE).setText(CoreMessages
				.getResourceString("OMPIPreferencesPage.ortedFull_text"));
		ortedFullText = new Text(ortedFull, SWT.READ_ONLY | SWT.SINGLE | SWT.BORDER);
		ortedFullText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		ortedFullText.addModifyListener(listener);
		ortedFullText.setText(ortedPathText.getText()+" "+ortedArgsText.getText());
		
		
		Group bGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		bGroup.setLayout(createGridLayout(1, true, 10, 10));
		bGroup.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));
		bGroup.setText(CoreMessages.getResourceString("OMPIPreferencesPage.group_proxy"));
		
		new Label(bGroup, SWT.WRAP).setText("Enter the path to the PTP ORTE proxy server.");
		
		Composite orteserver = new Composite(bGroup, SWT.NONE);
		orteserver.setLayout(createGridLayout(3, false, 0, 0));
		orteserver.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));
		
		new Label(orteserver, SWT.NONE).setText(CoreMessages
				.getResourceString("OMPIPreferencesPage.orteServer_text"));
		orteServerText = new Text(orteserver, SWT.SINGLE | SWT.BORDER);
		orteServerText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		orteServerText.addModifyListener(listener);
		browseButton2 = SWTUtil.createPushButton(orteserver, CoreMessages
				.getResourceString("PTPPreferencesPage.browseButton"), null);
		browseButton2.addSelectionListener(listener);
	}

	protected void defaultSetting() 
	{
		orteServerText.setText(orteServerFile);
		
		ortedPathText.setText(ortedFile);
		ortedArgsText.setText(ortedArgs);
		ortedFullText.setText(ortedPathText.getText()+" "+ortedArgsText.getText());
	}
	
	private void loadSaved()
	{
		Preferences preferences = PTPCorePlugin.getDefault().getPluginPreferences();
		
		orteServerFile = preferences.getString(PreferenceConstants.ORTE_SERVER_PATH);
		orteServerText.setText(orteServerFile);

		ortedFile = preferences.getString(PreferenceConstants.ORTE_ORTED_PATH);
		ortedPathText.setText(ortedFile);
		ortedArgs = preferences.getString(PreferenceConstants.ORTE_ORTED_ARGS);
		if(ortedArgs.equals("")) ortedArgs = defaultOrtedArgs;
		ortedArgsText.setText(ortedArgs);
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
		ortedFile = ortedPathText.getText();
		ortedArgs = ortedArgsText.getText();
		orteServerFile = orteServerText.getText();
	}

	public boolean performOk() 
	{
		store();
		Preferences preferences = PTPCorePlugin.getDefault()
				.getPluginPreferences();

		preferences.setValue(PreferenceConstants.ORTE_ORTED_PATH, ortedFile);
		preferences.setValue(PreferenceConstants.ORTE_ORTED_ARGS, ortedArgs);
		preferences.setValue(PreferenceConstants.ORTE_SERVER_PATH, orteServerFile);

		PTPCorePlugin.getDefault().savePluginPreferences();

		return true;
	}

	/**
	 * Show a dialog that lets the user select a file
	 */
	protected void handlePathBrowseButtonSelected() 
	{
		FileDialog dialog = new FileDialog(getShell());
		dialog.setText(CoreMessages
				.getResourceString("OMPIPreferencesPage.Select_ORTEd_FILE"));
		String correctPath = getFieldContent(ortedPathText.getText());
		if (correctPath != null) {
			File path = new File(correctPath);
			if (path.exists())
				dialog.setFilterPath(path.isFile() ? correctPath : path
						.getParent());
		}

		String selectedPath = dialog.open();
		if (selectedPath != null)
			ortedPathText.setText(selectedPath);
	}
	
	protected void handlePathBrowseButtonSelected2() 
	{
		FileDialog dialog = new FileDialog(getShell());
		dialog.setText(CoreMessages
				.getResourceString("OMPIPreferencesPage.Select_ORTE_PROXY_FILE"));
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
		String name = getFieldContent(ortedPathText.getText());
		if (name == null) {
			setErrorMessage(CoreMessages
					.getResourceString("OMPIPreferencesPage.Incorrect_ORTEd_file"));
			//setValid(false);
			//return false;
		}
		else {
			File path = new File(name);
			if (!path.exists() || !path.isFile()) {
				setErrorMessage(CoreMessages
					.getResourceString("OMPIPreferencesPage.Incorrect_ORTEd_file"));
				//setValid(false);
				//return false;
			}
		}
		
		name = getFieldContent(orteServerText.getText());
		if (name == null) {
			setErrorMessage(CoreMessages
					.getResourceString("OMPIPreferencesPage.Incorrect_server_file"));
			//setValid(false);
			//return false;
		}
		else {
			File path = new File(name);
			if (!path.exists() || !path.isFile()) {
				setErrorMessage(CoreMessages
					.getResourceString("OMPIPreferencesPage.Incorrect_server_file"));
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
