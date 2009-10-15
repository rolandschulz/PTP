/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.pbs.ui.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.remote.ui.IRemoteUIConstants;
import org.eclipse.ptp.remote.ui.IRemoteUIFileManager;
import org.eclipse.ptp.remote.ui.IRemoteUIServices;
import org.eclipse.ptp.remote.ui.PTPRemoteUIPlugin;
import org.eclipse.ptp.rm.pbs.core.rmsystem.IPBSResourceManagerConfiguration;
import org.eclipse.ptp.rm.pbs.ui.Activator;
import org.eclipse.ptp.rm.pbs.ui.messages.Messages;
import org.eclipse.ptp.ui.wizards.IRMConfigurationWizard;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizardPage;
import org.eclipse.ptp.utils.ui.swt.SWTUtil;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class PBSConfigurationWizardPage extends RMConfigurationWizardPage {
	
	protected class WidgetListener extends SelectionAdapter implements ModifyListener, IPropertyChangeListener 
	{
		public void modifyText(ModifyEvent evt) {
			Object source = evt.getSource();
			if(!loading && (source == pathText || source == argsText)) {
				updatePage();
			}
		}
	
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(FieldEditor.IS_VALID)) {
				updatePage();
			}
		}
	
		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			if (source == browseButton) {
				handlePathBrowseButtonSelected();
			} else {
				updateSettings();
				updatePage();
			}
		}
	}

	public static final String EMPTY_STRING = ""; //$NON-NLS-1$

	private IPBSResourceManagerConfiguration config;
	private String pbsdPath = EMPTY_STRING;
	private String pbsdArgs = EMPTY_STRING;
	private IRemoteServices remoteServices = null;
	private IRemoteConnection connection = null;
	private boolean loading = true;
	private boolean isValid;
	private boolean useDefaults;
	private Text pathText = null;

	private Text argsText = null;
	private Button browseButton = null;
	private Button defaultButton = null;
	private WidgetListener listener = new WidgetListener();
	
	public PBSConfigurationWizardPage(IRMConfigurationWizard wizard) {
		super(wizard, Messages.PBSConfigurationWizardPage_name);
		setTitle(Messages.PBSConfigurationWizardPage_title);
		setDescription(Messages.PBSConfigurationWizardPage_description);
		setPageComplete(true);
		isValid = true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.wizards.RMConfigurationWizardPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout topLayout = new GridLayout();
	    composite.setLayout(topLayout);
		createContents(composite);
		setControl(composite);
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
		config.setUseDefaults(useDefaults);
		if (!useDefaults) {
			config.setPBSdPath(pbsdPath);
			config.setPBSdArgs(pbsdArgs);
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			initContents();
		}
		super.setVisible(visible);
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
		Composite contents = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.marginWidth = 0;
		contents.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		contents.setLayoutData(gd);

		/*
		 * Default
		 */
		defaultButton = createCheckButton(contents, Messages.PBSConfigurationWizardPage_defaultButton);
		defaultButton.addSelectionListener(listener);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		defaultButton.setLayoutData(gd);

		/*
		 * PBSD path
		 */
		Label label = new Label(contents, SWT.NONE);
		label.setText(Messages.PBSConfigurationWizardPage_path);

		pathText = new Text(contents, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		gd.widthHint = 60;
		pathText.setLayoutData(gd);
		pathText.addModifyListener(listener);
		
		browseButton = SWTUtil.createPushButton(contents, Messages.PBSConfigurationWizardPage_browseButton, null);
		browseButton.addSelectionListener(listener);
		
		/*
		 * PBSD args
		 */
		label = new Label(contents, SWT.NONE);
		label.setText(Messages.PBSConfigurationWizardPage_arguments);
		
		argsText = new Text(contents, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		argsText.setLayoutData(gd);
		argsText.addModifyListener(listener);
	}

	/**
	 * Initialize the contents of the controls.
	 */
	private void initContents() {
		loading = true;
		config = (IPBSResourceManagerConfiguration)getConfigurationWizard().getConfiguration();
		loadSaved();
		updateSettings();
		defaultSetting();
		loading = false;
		updatePage();	
	}
	
	/**
	 * Load the initial wizard state from the configuration settings.
	 */
	private void loadSaved()
	{
		useDefaults = config.getUseDefaults();
		pbsdPath = config.getPBSdPath();
		pbsdArgs = config.getPBSdArgs();
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
		if (!loading) {
			if (defaultButton != null) {
				useDefaults = defaultButton.getSelection();
			}
			if (pathText != null) {
				pbsdPath = pathText.getText();
			}
			if (argsText != null) {
				pbsdArgs = argsText.getText();
			}
		}
	}
	
	/**
	 * Update wizard UI selections from settings. This should be called whenever any
	 * settings are changed.
	 */
	private void updateSettings() {
		store();
		pathText.setEnabled(!useDefaults);
		argsText.setEnabled(!useDefaults);
		browseButton.setEnabled(!useDefaults);
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
		defaultButton.setSelection(useDefaults);
		pathText.setText(pbsdPath);
		argsText.setText(pbsdArgs);
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
	 * Show a dialog that lets the user select a file.
	 */
	protected void handlePathBrowseButtonSelected() 
	{
		/*
		 * Need to do this here because the connection may have been changed 
		 * by the previous wizard page
		 */
		String rmID = config.getRemoteServicesId();
		if (rmID != null) {
			remoteServices = PTPRemoteCorePlugin.getDefault().getRemoteServices(rmID);
			String conn = config.getConnectionName();
			if (remoteServices != null && conn != null) {
				connection = remoteServices.getConnectionManager().getConnection(conn);
			}
		}
		
		if (connection != null) {
			if (!connection.isOpen()) {
				IRunnableWithProgress op = new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor)
							throws InvocationTargetException,
							InterruptedException {
						try {
							connection.open(monitor);
						} catch (RemoteConnectionException e) {
							ErrorDialog.openError(getShell(), Messages.PBSConfigurationWizardPage_connection_error,
									Messages.PBSConfigurationWizardPage_connection_error_msg,
									new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
						}
					}
					
				};
				try {
					new ProgressMonitorDialog(getShell()).run(true, true, op);
				} catch (InvocationTargetException e) {
					ErrorDialog.openError(getShell(), Messages.PBSConfigurationWizardPage_connection_error,
							Messages.PBSConfigurationWizardPage_connection_error_msg,
							new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
				} catch (InterruptedException e) {
					ErrorDialog.openError(getShell(), Messages.PBSConfigurationWizardPage_connection_error,
							Messages.PBSConfigurationWizardPage_connection_error_msg,
							new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
				}
			}
			IRemoteUIServices remoteUIServices = PTPRemoteUIPlugin.getDefault().getRemoteUIServices(remoteServices);
			IRemoteUIFileManager fileMgr = remoteUIServices.getUIFileManager();
			
			String initialPath = "//"; // Start at root since PBSD is probably installed in the system somewhere //$NON-NLS-1$
			String selectedPath = fileMgr.browseFile(getControl().getShell(), Messages.PBSConfigurationWizardPage_select, initialPath, IRemoteUIConstants.OPEN);
			if (selectedPath != null) {
				pathText.setText(selectedPath);
			}
		}
	}

	/**
	 * @return
	 */
	protected boolean isValidSetting() 
	{
		if (defaultButton != null && defaultButton.getSelection()) {
			return true;
		}
		
		if (pathText != null) {
			String name = getFieldContent(pathText.getText());
			if (name == null) {
				setErrorMessage(Messages.PBSConfigurationWizardPage_invalid); 
				return false;
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
	protected void updatePage() 
	{
		setErrorMessage(null);
		setMessage(null);
	
		if (!isValidSetting()) {
			setValid(false);
		} else {	
			performOk();
			setValid(true);
		}
	}
}
