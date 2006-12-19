/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
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
package org.eclipse.ptp.lsf.ui.wizards;

import java.io.File;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.lsf.core.rmsystem.LSFResourceManagerConfiguration;
import org.eclipse.ptp.lsf.ui.preferences.PreferenceConstants;
import org.eclipse.ptp.ui.utils.SWTUtil;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizard;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public final class LSFResourceManagerConfigurationWizardPage extends
		RMConfigurationWizardPage {
	
	protected class WidgetListener extends SelectionAdapter implements ModifyListener, IPropertyChangeListener 
	{
		public void modifyText(ModifyEvent evt) {
			Object source = evt.getSource();
			if (!loading && (source == serverText || source == hostText))
				updatePage();
		}
	
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(FieldEditor.IS_VALID))
				updatePage();
		}
	
		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			if (source == browseButton)
				handlePathBrowseButtonSelected();
			else
				updatePage();
		}
	}

	public static final String EMPTY_STRING = "";
	private LSFResourceManagerConfiguration config;
	private String serverFile = EMPTY_STRING;
	private String host = EMPTY_STRING;
	private boolean loading = true;
	private boolean isValid;
	protected Text serverText = null;
	protected Text hostText = null;
	protected Button browseButton = null;
	protected Button fManualButton = null;
	protected WidgetListener listener = new WidgetListener();
	
	public LSFResourceManagerConfigurationWizardPage(RMConfigurationWizard wizard) {
		super(wizard, "LSF Configuration Wizard Page");
		setTitle("LSF Configuration Wizard Page");
		setDescription("LSF Configuration Wizard Page");
		
		//System.out.println("in LSFResourceManagerConfigurationWizardPage");
		
		final RMConfigurationWizard confWizard = getConfigurationWizard();
		config = (LSFResourceManagerConfiguration) confWizard.getConfiguration();
		setPageComplete(false);
		isValid = false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.wizards.RMConfigurationWizardPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		//System.out.println("In LSFResourceManagerConfigurationWizardPage.createControl");
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(createGridLayout(1, true, 0, 0));
		composite.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));

		createLSFContents(composite);
		
		loading = true;
		loadSaved();
		loading = false;
		
		defaultSetting();

		setControl(composite);
		
		//System.out.println("leaving LSFResourceManagerConfigurationWizardPage.createControl");
	}

	private void store() 
	{
		serverFile = serverText.getText();
		host = hostText.getText();
	}

	public boolean performOk() 
	{
		store();
		config.setServerFile(serverFile);
		config.setHost(host);
		config.setManualLaunch(fManualButton.getSelection());
		return true;
	}

	private void createLSFContents(Composite parent) {
		Group bGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		bGroup.setLayout(createGridLayout(1, true, 10, 10));
		bGroup.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));
		bGroup.setText("LSF PTP Proxy Server");
		
		new Label(bGroup, SWT.WRAP).setText("Enter the path to the PTP LSF proxy server.");
		
		Composite serverComp = new Composite(bGroup, SWT.NONE);
		serverComp.setLayout(createGridLayout(3, false, 0, 0));
		serverComp.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));
		
		new Label(serverComp, SWT.NONE).setText("LSF|PTP proxy server file:");
		serverText = new Text(serverComp, SWT.SINGLE | SWT.BORDER);
		serverText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		serverText.addModifyListener(listener);
		browseButton = SWTUtil.createPushButton(serverComp, "Browse", null);
		browseButton.addSelectionListener(listener);
		
		new Label(bGroup, SWT.WRAP).setText("Enter the name of the LSF server host.");
		
		Composite hostComp = new Composite(bGroup, SWT.NONE);
		hostComp.setLayout(createGridLayout(3, false, 0, 0));
		hostComp.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));
		
		new Label(hostComp, SWT.NONE).setText("LSF|PTP proxy server host:");
		hostText = new Text(hostComp, SWT.SINGLE | SWT.BORDER);
		hostText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		hostText.addModifyListener(listener);
		
		fManualButton = createCheckButton(parent, "Launch LSF server manually");
	}

	private void loadSaved()
	{
		Preferences preferences = PTPCorePlugin.getDefault().getPluginPreferences();
		
		serverFile = preferences.getString(PreferenceConstants.LSF_PROXY_PATH);
		/* if they don't have the proxy path set, let's try and give them a default that might help */
		if(serverFile.equals("")) {
			serverFile = PTPCorePlugin.getDefault().locateFragmentFile("org.eclipse.ptp.lsf.proxy", "ptp_lsf_proxy.py");
	    }
		
		//System.out.println("serverFile: " + serverFile);
		if (serverFile == null || serverFile.equals("")) {
			serverFile = "";
			setValid(false);
			return;
		}
		serverText.setText(serverFile);
		fManualButton.setSelection(preferences.getBoolean(PreferenceConstants.LSF_LAUNCH_MANUALLY));
	}

	private void setValid(boolean b) {
		isValid = b;
		setPageComplete(isValid);
	}

	protected Button createButton(Composite parent, String label, int type) {
		Button button = new Button(parent, type);
		button.setText(label);
		GridData data = new GridData();
		button.setLayoutData(data);
		return button;
	}

	protected Button createCheckButton(Composite parent, String label) {
		return createButton(parent, label, SWT.CHECK | SWT.LEFT);
	}

	protected GridLayout createGridLayout(int columns, boolean isEqual, int mh, int mw)  {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = columns;
		gridLayout.makeColumnsEqualWidth = isEqual;
		gridLayout.marginHeight = mh;
		gridLayout.marginWidth = mw;
		return gridLayout;
	}

	protected void defaultSetting() 
	{
		serverText.setText(serverFile);
	}

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
		dialog.setText("Select LSF|PTP Proxy server file");
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

	protected boolean isValidLSFdSetting() 
	{
		String server = getFieldContent(serverText.getText());
		if (server == null) {
			setErrorMessage("Invalid LSF|PTP proxy server file");
			//setValid(false);
			return false;
		}
		else {
			File path = new File(server);
			if (!path.exists() || !path.isFile()) {
				setErrorMessage("Invalid LSF|PTP proxy server file");
				//setValid(false);
				return false;
			}
		}
		
		return true;
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

	protected void updatePage() 
	{
		setValid(false);
		setErrorMessage(null);
		setMessage(null);
	
		if (!isValidLSFdSetting())
			return;
	
		performOk();
		setValid(true);
	}

}
