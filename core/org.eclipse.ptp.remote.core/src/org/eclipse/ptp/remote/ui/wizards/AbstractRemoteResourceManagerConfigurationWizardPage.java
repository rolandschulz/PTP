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
import org.eclipse.ptp.remote.AbstractRemoteResourceManagerConfiguration;
import org.eclipse.ptp.remote.IRemoteConnection;
import org.eclipse.ptp.remote.IRemoteConnectionManager;
import org.eclipse.ptp.remote.IRemoteFileManager;
import org.eclipse.ptp.remote.IRemoteProxyOptions;
import org.eclipse.ptp.remote.IRemoteServices;
import org.eclipse.ptp.remote.PTPRemotePlugin;
import org.eclipse.ptp.remote.ui.Messages;
import org.eclipse.ptp.remote.ui.preferences.PreferenceConstants;
import org.eclipse.ptp.ui.utils.SWTUtil;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizard;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public abstract class AbstractRemoteResourceManagerConfigurationWizardPage extends
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
			if (source == browseButton) {
				handlePathBrowseButtonSelected();
			} else {
				updateOptionsFromUI();
				updatePage();
			}
		}
	}

	public static final String EMPTY_STRING = "";
	private AbstractRemoteResourceManagerConfiguration config;
	private String proxyFile = EMPTY_STRING;
	private IRemoteServices remoteServices = null;
	private IRemoteConnectionManager connectionManager = null;
	private IRemoteConnection connection = null;
	private boolean loading = true;
	private boolean isValid;
	private boolean muxPortFwd = false;
	private boolean muxStdio = false;
	private boolean portFwdSupported = true;
	private boolean manualLaunch = false;

	protected Text serverText = null;
	protected Button browseButton = null;
	protected Button noneButton = null;
	protected Button stdioButton = null;
	protected Button portForwardingButton = null;
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

	/**
	 * Update wizard UI selections from options
	 */
	private void updateUIFromOptions() {
		/*
		 * Fix options first
		 */
		if (muxPortFwd && !portFwdSupported) {
			muxPortFwd = false;
		}
		
		if (muxStdio && manualLaunch) {
			manualLaunch = false;
		}
		
		if (noneButton != null) {
			noneButton.setSelection(!muxPortFwd && !muxStdio);
		}
		
		if (portForwardingButton != null) {
			portForwardingButton.setSelection(muxPortFwd);
			portForwardingButton.setEnabled(portFwdSupported);
		}
		
		if (stdioButton != null) {
			stdioButton.setSelection(muxStdio);
		}
		
		if (manualButton != null) {
			manualButton.setSelection(manualLaunch);
			manualButton.setEnabled(!muxStdio);
		}
	}
	
	/**
	 * Update wizard options from UI selections
	 */
	private void updateOptionsFromUI() {
		muxPortFwd = portForwardingButton.getSelection();
		muxStdio = stdioButton.getSelection();
		
		/*
		 * Stdio multiplexing and manual launch are mutually exclusive
		 */
		if (muxStdio) {
			manualButton.setSelection(false);
			manualButton.setEnabled(false);
		} else {
			manualButton.setEnabled(true);
		}
		
		manualLaunch = manualButton.getSelection();
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

	/**
	 * Get the preferences for this RM
	 * 
	 * @return RM preferences
	 */
	public abstract Preferences getPreferences();

	/**
	 * Save the current state in the RM configuration. This is called whenever
	 * anything is changed.
	 * 
	 * @return
	 */
	public boolean performOk() 
	{
		store();
		int options = 0;
		if (muxStdio) {
			options |= IRemoteProxyOptions.STDIO;
		}
		if (muxPortFwd) {
			options |= IRemoteProxyOptions.PORT_FORWARDING;
		}
		if (manualLaunch) {
			options |= IRemoteProxyOptions.MANUAL_LAUNCH;
		}
		if (remoteServices != null) {
			config.setRemoteServicesId(remoteServices.getId());
		}
		if (connection != null) {
			config.setConnectionName(connection.getName());
		}
		config.setProxyServerPath(proxyFile);
		config.setOptions(options);
		config.setDefaultNameAndDesc();
		return true;
	}
	
	/**
	 * Create the contents of the wizard page.
	 * 
	 * @param parent
	 * @param colSpan
	 */
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
		label.setText(Messages.getString("RemoteConfigurationWizard.provider"));
		gd = new GridData();
		gd.horizontalSpan = 1;
		label.setLayoutData(gd);
		
		remoteCombo = new Combo(projComp, SWT.DROP_DOWN | SWT.READ_ONLY);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		remoteCombo.setLayoutData(gd);
		
		// TODO work out how to skip a cell!!!
		label = new Label(projComp, SWT.NONE);
		gd = new GridData();
		gd.horizontalSpan = 1;
		label.setLayoutData(gd);

		connectionLabel = new Label(projComp, SWT.NONE);
		connectionLabel.setText(Messages.getString("RemoteConfigurationWizard.location"));
		gd = new GridData();
		gd.horizontalSpan = 1;
		connectionLabel.setLayoutData(gd);
		
		connectionCombo = new Combo(projComp, SWT.DROP_DOWN | SWT.READ_ONLY);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		connectionCombo.setLayoutData(gd);

		newRemoteConnectionButton = SWTUtil.createPushButton(projComp, Messages.getString("RemoteConfigurationWizard.newButton"), null);

		initializeRemoteServicesCombo();
		registerListeners();
		
		Composite proxyComp = new Composite(parent, SWT.NONE);
		GridLayout proxyLayout = new GridLayout();
		proxyLayout.numColumns = 2;
		proxyLayout.marginHeight = 0;
		proxyLayout.marginWidth = 0;
		proxyComp.setLayout(proxyLayout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		proxyComp.setLayoutData(gd);
		
		Label proxyLabel = new Label(proxyComp, SWT.NONE);
		proxyLabel.setText(Messages.getString("RemoteConfigurationWizard.path"));
		gd = new GridData();
		gd.horizontalSpan = 2;
		proxyLabel.setLayoutData(gd);

		serverText = new Text(proxyComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		serverText.setLayoutData(gd);
		serverText.addModifyListener(listener);
		browseButton = SWTUtil.createPushButton(proxyComp, Messages.getString("RemoteConfigurationWizard.browseButton"), null);
		browseButton.addSelectionListener(listener);
		
		Group mxGroup = new Group(proxyComp, SWT.SHADOW_ETCHED_IN);
		mxGroup.setLayout(createGridLayout(1, true, 10, 10));
		mxGroup.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));
		mxGroup.setText(Messages.getString("RemoteConfigurationWizard.mxOptions"));
		
		noneButton = createRadioButton(mxGroup, Messages.getString("RemoteConfigurationWizard.noneButton"), "mxGroup", listener);
		noneButton.addSelectionListener(listener);
		portForwardingButton = createRadioButton(mxGroup, Messages.getString("RemoteConfigurationWizard.portForwardingButton"), "mxGroup", listener);
		portForwardingButton.addSelectionListener(listener);
		stdioButton = createRadioButton(mxGroup, Messages.getString("RemoteConfigurationWizard.stdioButton"), "mxGroup", listener);
		stdioButton.addSelectionListener(listener);
		
		Group otherGroup = new Group(proxyComp, SWT.SHADOW_ETCHED_IN);
		otherGroup.setLayout(createGridLayout(1, true, 10, 10));
		otherGroup.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));
		otherGroup.setText(Messages.getString("RemoteConfigurationWizard.otherOptions"));

		manualButton = createCheckButton(otherGroup, Messages.getString("RemoteConfigurationWizard.manualButton"));
		manualButton.addSelectionListener(listener);
	}
	
	private void registerListeners() {
		remoteCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				handleRemoteServiceSelected();
			}
		});
		connectionCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				handleConnectionSelected();
				updatePage();
			}
		});
		newRemoteConnectionButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleNewRemoteConnectionSelected();
				updatePage();
			}
		});	
	}
	
	/**
	 * Load the initial wizard state from the preference settings.
	 */
	private void loadSaved()
	{
		Preferences preferences = getPreferences();
		
		proxyFile = preferences.getString(PreferenceConstants.PROXY_PATH);
		serverText.setText(proxyFile);
		int options = preferences.getInt(PreferenceConstants.OPTIONS);
		
		muxStdio = (options & IRemoteProxyOptions.STDIO) == IRemoteProxyOptions.STDIO;
		muxPortFwd = (options & IRemoteProxyOptions.PORT_FORWARDING) == IRemoteProxyOptions.PORT_FORWARDING;
		manualLaunch = (options & IRemoteProxyOptions.MANUAL_LAUNCH) == IRemoteProxyOptions.MANUAL_LAUNCH;

		updateUIFromOptions();
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
		if (serverText != null) {
			proxyFile = serverText.getText();
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
		serverText.setText(proxyFile);
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
	 * Handle the section of a new connection. Update connection option buttons
	 * appropriately.
	 */
	protected void handleConnectionSelected() {
		int currentSelection = connectionCombo.getSelectionIndex();
		if (currentSelection >= 0 && connectionManager != null) {
			String connectionName = connectionCombo.getItem(currentSelection);
			connection = connectionManager.getConnection(connectionName);
		}
		
		/*
		 * Disable port forwarding button if it's not supported. If port forwarding was selected,
		 * switch to 'none' instead.
		 */
		if (connection != null) {
			portFwdSupported = connection.supportsTCPPortForwarding();
			updateUIFromOptions();
		}
	}

	/**
	 * Handle creation of a new connection by pressing the 'New...' button. Calls
	 * handleRemoteServicesSelected() to update the connection combo with the new
	 * connection.
	 * 
	 * TODO should probably select the new connection
	 */
	protected void handleNewRemoteConnectionSelected() 
	{
		if (connectionManager != null) {
			connectionManager.newConnection(getControl().getShell());
			handleRemoteServiceSelected();
		}
	}

	/**
	 * Show a dialog that lets the user select a file.
	 */
	protected void handlePathBrowseButtonSelected() 
	{
		if (connection != null) {
			IRemoteFileManager fileMgr = remoteServices.getFileManager();
			String correctPath = getFieldContent(serverText.getText());
			String selectedPath = fileMgr.browseRemoteFile(getControl().getShell(), connection, Messages.getString("RemoteConfigurationWizard.select"), correctPath);
			if (selectedPath != null) {
				serverText.setText(selectedPath);
			}
		}
	}

	/**
	 * Handle selection of a new remote services provider from the 
	 * remote services combo.
	 * 
	 * The assumption is that this will trigger a call to the selection
	 * handler for the connection combo.
	 */
	protected void handleRemoteServiceSelected() {
		IRemoteServices[] allRemoteServices = PTPRemotePlugin.getDefault().getAllRemoteServices();
		int selectionIndex = remoteCombo.getSelectionIndex();
		if (allRemoteServices != null && allRemoteServices.length > 0 && selectionIndex >=0) {
			remoteServices = allRemoteServices[selectionIndex];
			connectionManager = remoteServices.getConnectionManager();
			IRemoteConnection[] connections = connectionManager.getConnections();
			connectionCombo.removeAll();
			for (int i = 0; i < connections.length; i++) {
				connectionCombo.add(connections[i].getName());
			}
			if (connections.length > 0) {
				// Should trigger call to selection handler
				connectionCombo.select(connections.length - 1);
			}
			
			/*
			 * Enable 'new' button if new connections are supported
			 */
			newRemoteConnectionButton.setEnabled(connectionManager.supportsNewConnections());
		}
	}
	
	/**
	 * Intialize the contents of the remote services combo.
	 * 
	 * The assumption is that this will trigger a call to the selection handling
	 * routing when the default index is selected.
	 */
	protected void initializeRemoteServicesCombo() {
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
			// Should trigger call to selection handler
			remoteCombo.select(defIndex);
			handleRemoteServiceSelected();
			handleConnectionSelected();
		}
	}
	
	/**
	 * @return
	 */
	protected boolean isValidSetting() 
	{
		if (serverText != null) {
			String name = getFieldContent(serverText.getText());
			if (name == null) {
				setErrorMessage(Messages.getString("RemoteConfigurationWizard.invalid"));
				//setValid(false);
				return false;
			}
			else {
				File path = new File(name);
				if (!path.exists() || !path.isFile()) {
					setErrorMessage(Messages.getString("RemoteConfigurationWizard.invalid"));
					//setValid(false);
					return false;
				}
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
