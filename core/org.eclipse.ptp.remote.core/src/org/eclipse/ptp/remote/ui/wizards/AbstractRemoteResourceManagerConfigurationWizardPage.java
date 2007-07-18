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
package org.eclipse.ptp.remote.ui.wizards;

import java.io.File;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.remote.AbstractRemoteResourceManagerConfiguration;
import org.eclipse.ptp.remote.IRemoteConnection;
import org.eclipse.ptp.remote.IRemoteConnectionManager;
import org.eclipse.ptp.remote.IRemoteFileManager;
import org.eclipse.ptp.remote.IRemoteServices;
import org.eclipse.ptp.remote.PTPRemotePlugin;
import org.eclipse.ptp.remote.ui.preferences.PreferenceConstants;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class AbstractRemoteResourceManagerConfigurationWizardPage extends
		RMConfigurationWizardPage {
	
	protected class WidgetListener extends SelectionAdapter implements ModifyListener, IPropertyChangeListener 
	{
		public void modifyText(ModifyEvent evt) {
			Object source = evt.getSource();
			if(!loading && source == serverText)
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
			else if (source == manualButton) {
				manualLaunch = manualButton.getSelection();
				updatePage();
			}
		}
	}

	public static final String EMPTY_STRING = "";
	private AbstractRemoteResourceManagerConfiguration config;
	private String proxyPath = EMPTY_STRING;
	private String connectionName = EMPTY_STRING;
	private String remoteServicesId = EMPTY_STRING;
	private boolean manualLaunch = false;
	private boolean loading = true;
	private boolean isValid;

	protected Text serverText = null;
	protected Button browseButton = null;
	protected Button manualButton = null;
	protected WidgetListener listener = new WidgetListener();
	protected Button newRemoteConnectionButton;
	protected Label  connectionLabel;
	protected Combo  remoteCombo;
	protected Combo  connectionCombo;

	public AbstractRemoteResourceManagerConfigurationWizardPage(RMConfigurationWizard wizard,
			String title) {
		super(wizard, title);
		
		final RMConfigurationWizard confWizard = getConfigurationWizard();
		config = (AbstractRemoteResourceManagerConfiguration) confWizard.getConfiguration();
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

		createContents(composite, 3);
		
		loading = true;
		loadSaved();
		loading = false;
		
		defaultSetting();

		setControl(composite);
	}

	public boolean performOk() 
	{
		store();
		config.setRemoteServicesId(remoteServicesId);
		config.setConnectionName(connectionName);
		config.setProxyServerPath(proxyPath);
		config.setManualLaunch(manualLaunch);
		config.setDefaultNameAndDesc();
		return true;
	}

	private Button createButton(Composite parent, String label, int type) {
		Button button = new Button(parent, type);
		button.setText(label);
		GridData data = new GridData();
		button.setLayoutData(data);
		return button;
	}
	
	private Button createCheckButton(Composite parent, String label) {
		return createButton(parent, label, SWT.CHECK | SWT.LEFT);
	}
	
	private void createContents(Composite parent, int colSpan) {
		Composite projComp = new Composite(parent, SWT.NONE);
		GridLayout projLayout = new GridLayout();
		projLayout.numColumns = 3;
		projLayout.marginHeight = 0;
		projLayout.marginWidth = 0;
		projComp.setLayout(projLayout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = colSpan;
		projComp.setLayoutData(gd);

		Label label = new Label(projComp, SWT.NONE);
		label.setText("Remote service provider:");
		gd = new GridData();
		gd.horizontalSpan = 1;
		label.setLayoutData(gd);
		
		remoteCombo = new Combo(projComp, SWT.DROP_DOWN | SWT.READ_ONLY);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		remoteCombo.setLayoutData(gd);
		remoteCombo.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				updateConnectionPulldown();
			}
		});
		
		// TODO work out how to skip a cell!!!
		label = new Label(projComp, SWT.NONE);
		gd = new GridData();
		gd.horizontalSpan = 1;
		label.setLayoutData(gd);

		connectionLabel = new Label(projComp, SWT.NONE);
		connectionLabel.setText("Proxy server location:");
		gd = new GridData();
		gd.horizontalSpan = 1;
		connectionLabel.setLayoutData(gd);
		
		connectionCombo = new Combo(projComp, SWT.DROP_DOWN | SWT.READ_ONLY);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		connectionCombo.setLayoutData(gd);
		connectionCombo.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				updateConnection();
			}
		});

		newRemoteConnectionButton = SWTUtil.createPushButton(projComp, "New...", null);
		newRemoteConnectionButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent evt) {
				handleNewRemoteConnectionSelected();
				updateConnectionPulldown();
				updatePage();
			}
		});	

		updateRemotePulldown();
		
		Composite proxyComp = new Composite(parent, SWT.NONE);
		GridLayout proxyLayout = new GridLayout();
		proxyLayout.numColumns = 2;
		proxyLayout.marginHeight = 0;
		proxyLayout.marginWidth = 0;
		proxyComp.setLayout(proxyLayout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		proxyComp.setLayoutData(gd);
		
		Label proxyLabel = new Label(proxyComp, SWT.NONE);
		proxyLabel.setText("Path to proxy server executable:");
		gd = new GridData();
		gd.horizontalSpan = 2;
		proxyLabel.setLayoutData(gd);

		serverText = new Text(proxyComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		serverText.setLayoutData(gd);
		serverText.addModifyListener(listener);
		browseButton = SWTUtil.createPushButton(proxyComp, "Browse", null);
		browseButton.addSelectionListener(listener);
		
		manualButton = createCheckButton(parent, "Launch ORTE server manually");
		manualButton.addSelectionListener(listener);
	}
	
	private void loadSaved()
	{
		Preferences preferences = PTPCorePlugin.getDefault().getPluginPreferences();
		
		proxyPath = preferences.getString(PreferenceConstants.PROXY_PATH);
		/* if they don't have the ptp_orte_proxy path set, let's try and give them a default that might help */
		if(proxyPath.equals("")) {
			proxyPath = PTPCorePlugin.getDefault().locateFragmentFile("org.eclipse.ptp", "ptp_orte_proxy");
	    }
		
		if (proxyPath == null || proxyPath.equals("")) {
			proxyPath = "";
			setValid(false);
			return;
		}
		serverText.setText(proxyPath);
		manualButton.setSelection(preferences.getBoolean(PreferenceConstants.LAUNCH_MANUALLY));
	}

	private void setValid(boolean b) {
		isValid = b;
		setPageComplete(isValid);
	}

	private void store() 
	{
		if (serverText != null) {
			proxyPath = serverText.getText();
		}
	}
	
	protected void defaultSetting() 
	{
		serverText.setText(proxyPath);
	}

	protected String getFieldContent(String text) 
	{
		if (text.trim().length() == 0 || text.equals(EMPTY_STRING))
			return null;
	
		return text;
	}

	protected void handleNewRemoteConnectionSelected() 
	{
		IRemoteServices remoteServices = PTPRemotePlugin.getDefault().getRemoteServices(remoteServicesId);
		IRemoteConnectionManager connMgr = remoteServices.getConnectionManager();
		connMgr.newConnection(getControl().getShell());
	}

	/**
	 * Show a dialog that lets the user select a file
	 */
	protected void handlePathBrowseButtonSelected() 
	{
		IRemoteServices remoteServices = PTPRemotePlugin.getDefault().getRemoteServices(remoteServicesId);
		IRemoteConnectionManager connMgr = remoteServices.getConnectionManager();
		IRemoteConnection connection = connMgr.getConnection();
		IRemoteFileManager fileMgr = remoteServices.getFileManager();
		String correctPath = getFieldContent(serverText.getText());
		String selectedPath = fileMgr.browseRemoteFile(getControl().getShell(), connection, "Select Proxy Server Executable", correctPath);
		if (selectedPath != null) {
			serverText.setText(selectedPath);
		}
	}

	protected boolean isValidSetting() 
	{
		if (serverText != null) {
			String name = getFieldContent(serverText.getText());
			if (name == null) {
				setErrorMessage("Invalid proxy server executable");
				//setValid(false);
				return false;
			}
			else {
				File path = new File(name);
				if (!path.exists() || !path.isFile()) {
					setErrorMessage("Invalid proxy server executable");
					//setValid(false);
					return false;
				}
			}
		}
	
		return true;
	}

	protected void updateConnection() {
		int currentSelection = connectionCombo.getSelectionIndex();
		if (currentSelection >= 0) {
			connectionName = connectionCombo.getItem(currentSelection);
		}
	}
	
	protected void updateConnectionPulldown() {
		IRemoteServices[] allRemoteServices = PTPRemotePlugin.getDefault().getAllRemoteServices();
		if (allRemoteServices != null && allRemoteServices.length > 0) {
			IRemoteServices selectedServices = allRemoteServices[remoteCombo.getSelectionIndex()];
			remoteServicesId = selectedServices.getId();
			IRemoteConnectionManager connMgr = selectedServices.getConnectionManager();
			IRemoteConnection[] connections = connMgr.getConnections();
			connectionCombo.removeAll();
			for (int i = 0; i < connections.length; i++) {
				connectionCombo.add(connections[i].getName());
			}
			if (connections.length > 0) {
				connectionCombo.select(connections.length - 1);
			}
		}
	}
	
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

	protected void updateRemotePulldown() {
		IRemoteServices[] remoteServices = PTPRemotePlugin.getDefault().getAllRemoteServices();
		IRemoteServices defServices = PTPRemotePlugin.getDefault().getDefaultServices();
		int defIndex = remoteServices.length - 1; 
		remoteCombo.removeAll();
		for (int i = 0; i < remoteServices.length; i++) {
			remoteCombo.add(remoteServices[i].getName());
			if (remoteServices[i].equals(defServices)) {
				defIndex = i;
			}
		}
		if (remoteServices.length > 0) {
			remoteCombo.select(defIndex);
		}
	}

}
