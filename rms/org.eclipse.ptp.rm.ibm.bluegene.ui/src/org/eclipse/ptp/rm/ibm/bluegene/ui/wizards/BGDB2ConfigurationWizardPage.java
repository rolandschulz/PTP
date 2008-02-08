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
package org.eclipse.ptp.rm.ibm.bluegene.ui.wizards;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ptp.rm.ibm.bluegene.core.rmsystem.BGResourceManagerConfiguration;
import org.eclipse.ptp.rm.remote.ui.Messages;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizard;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class BGDB2ConfigurationWizardPage extends
		RMConfigurationWizardPage {
	
	protected class WidgetListener extends SelectionAdapter implements ModifyListener, IPropertyChangeListener 
	{
		public void modifyText(ModifyEvent evt) {
			if(!loading) {
				updatePage();
			}
		}
	
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(FieldEditor.IS_VALID)) {
				updatePage();
			}
		}
	}

	public static final String EMPTY_STRING = "";
	private BGResourceManagerConfiguration config;
	private String serviceNode = EMPTY_STRING;
	private String dbName = EMPTY_STRING;
	private String userName = EMPTY_STRING;
	private String password = EMPTY_STRING;
	private boolean loading = true;
	private boolean isValid;

	private Text serviceNodeText = null;
	private Text dbNameText = null;
	private Text userNameText = null;
	private Text passwordText = null;
	private WidgetListener listener = new WidgetListener();

	public BGDB2ConfigurationWizardPage(RMConfigurationWizard wizard) {
		super(wizard, "Service Node Configuration");
		
		final RMConfigurationWizard confWizard = getConfigurationWizard();
		config = (BGResourceManagerConfiguration) confWizard.getConfiguration();
		setPageComplete(false);
		isValid = false;
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.wizards.RMConfigurationWizardPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout topLayout = new GridLayout();
	    composite.setLayout(topLayout);

		loading = true;
		
		loadSaved();
		createContents(composite);
		defaultSetting();
		
		loading = false;

		setControl(composite);
		updatePage();
	}

	/**
	 * Save the current state in the RM configuration. This is called whenever
	 * anything is changed.
	 * 
	 * @return
	 */
	public boolean performOk() 
	{
		store();
		config.setServiceNode(serviceNode);
		config.setDatabaseName(dbName);
		config.setDatabaseUsername(userName);
		config.setDatabasePassword(password);
		return true;
	}
	
	/**
	 * Create the contents of the wizard page.
	 * 
	 * @param parent
	 * @param colSpan
	 */
	private void createContents(Composite parent) {
		/*
		 * Composite for remote provider and proxy location combo's
		 */
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		comp.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		comp.setLayoutData(gd);

		/*
		 * Service Node
		 */
		Label label = new Label(comp, SWT.NONE);
		label.setText(Messages.getString("Service node:"));
		gd = new GridData();
		gd.horizontalSpan = 1;
		label.setLayoutData(gd);
				
		serviceNodeText = new Text(comp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		gd.widthHint = 60;
		serviceNodeText.setLayoutData(gd);
		serviceNodeText.addModifyListener(listener);

		/*
		 * DB name
		 */
		label = new Label(comp, SWT.NONE);
		label.setText(Messages.getString("Database name:"));
		gd = new GridData();
		gd.horizontalSpan = 1;
		label.setLayoutData(gd);
				
		dbNameText = new Text(comp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		gd.widthHint = 60;
		dbNameText.setLayoutData(gd);
		dbNameText.addModifyListener(listener);

		/*
		 * username and password
		 */
		label = new Label(comp, SWT.NONE);
		label.setText(Messages.getString("Username:"));
		gd = new GridData();
		gd.horizontalSpan = 1;
		label.setLayoutData(gd);
				
		userNameText = new Text(comp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		gd.widthHint = 60;
		userNameText.setLayoutData(gd);
		userNameText.addModifyListener(listener);

		label = new Label(comp, SWT.NONE);
		label.setText(Messages.getString("Password:"));
		gd = new GridData();
		gd.horizontalSpan = 1;
		label.setLayoutData(gd);
				
		passwordText = new Text(comp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		gd.widthHint = 60;
		passwordText.setEchoChar('*');
		passwordText.setLayoutData(gd);
		passwordText.addModifyListener(listener);
	}
	
	/**
	 * Load the initial wizard state from the configuration settings.
	 */
	private void loadSaved()
	{
		serviceNode = config.getServiceNode();
		dbName = config.getDatabaseName();
		userName = config.getDatabaseUsername();
		password = config.getDatabasePassword();
	}
	
	/**
	 * @param b
	 */
	private void setValid(boolean b) {
		isValid = b;
		setPageComplete(isValid);
	}
	
	/**
	 * 
	 */
	private void store() 
	{
		if (serviceNodeText != null) {
			serviceNode = serviceNodeText.getText();
		}
		if (dbNameText != null) {
			dbName = dbNameText.getText();
		}
		if (userNameText != null) {
			userName = userNameText.getText();
		}
		if (passwordText != null) {
			password = passwordText.getText();
		}
	}

	/**
	 * Convenience method for creating a button widget.
	 * 
	 * @param parent
	 * @param label
	 * @param type
	 * @return the button widget
	 */
	protected Button createButton(Composite parent, String label, int type) {
		Button button = new Button(parent, type);
		button.setText(label);
		GridData data = new GridData();
		button.setLayoutData(data);
		return button;
	}
	
	/**
	 * Convenience method for creating a check button widget.
	 * 
	 * @param parent
	 * @param label
	 * @return the check button widget
	 */
	protected Button createCheckButton(Composite parent, String label) {
		return createButton(parent, label, SWT.CHECK | SWT.LEFT);
	}

	/**
	 * Convenience method for creating a grid layout.
	 * 
	 * @param columns
	 * @param isEqual
	 * @param mh
	 * @param mw
	 * @return the new grid layout
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
	 * Creates an new radio button instance and sets the default
	 * layout data.
	 *
	 * @param group  the composite in which to create the radio button
	 * @param label  the string to set into the radio button
	 * @param value  the string to identify radio button
	 * @return the new radio button
	 */ 
	protected Button createRadioButton(Composite parent, String label, String value, SelectionListener listener) {
		Button button = createButton(parent, label, SWT.RADIO | SWT.LEFT);
		button.setData((null == value) ? label : value);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.BEGINNING;
		button.setLayoutData(data);
		if(null != listener) {
			button.addSelectionListener(listener);
		}
		return button;
	}
	
	/**
	 * 
	 */
	protected void defaultSetting() 
	{
		serviceNodeText.setText(serviceNode);
		dbNameText.setText(dbName);
		userNameText.setText(userName);
		passwordText.setText(password);
	}

	/**
	 * Clean up the content of a text field.
	 * 
	 * @param text
	 * @return cleaned up text.
	 */
	protected String getFieldContent(String text) 
	{
		if (text.trim().length() == 0 || text.equals(EMPTY_STRING))
			return null;
	
		return text;
	}

	/**
	 * @return
	 */
	protected boolean isValidSetting() 
	{
		if (serviceNodeText != null) {
			String name = getFieldContent(serviceNodeText.getText());
			if (name == null) {
				setErrorMessage(Messages.getString("BGConfigurationWizard.invalid"));
				return false;
			}
		}
		if (dbNameText != null) {
			String name = getFieldContent(dbNameText.getText());
			if (name == null) {
				setErrorMessage(Messages.getString("BGConfigurationWizard.invalid"));
				return false;
			}
		}
		if (userNameText != null) {
			String name = getFieldContent(userNameText.getText());
			if (name == null) {
				setErrorMessage(Messages.getString("BGConfigurationWizard.invalid"));
				return false;
			}
		}
		if (passwordText != null) {
			String name = getFieldContent(passwordText.getText());
			if (name == null) {
				setErrorMessage(Messages.getString("BGConfigurationWizard.invalid"));
				return false;
			}
		}
	
		return true;
	}

	/**
	 * 
	 */
	protected void updatePage() 
	{
		setValid(false);
		setErrorMessage(null);
		setMessage(null);
	
		if (!isValidSetting()) {
			return;
		}
	
		performOk();
		setValid(true);
	}
}
