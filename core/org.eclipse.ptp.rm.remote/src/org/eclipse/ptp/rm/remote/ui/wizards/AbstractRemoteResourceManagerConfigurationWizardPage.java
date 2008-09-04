/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.rm.remote.ui.wizards;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteProxyOptions;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.ptp.remote.ui.IRemoteUIServices;
import org.eclipse.ptp.remote.ui.PTPRemoteUIPlugin;
import org.eclipse.ptp.rm.remote.core.AbstractRemoteResourceManagerConfiguration;
import org.eclipse.ptp.rm.remote.ui.Messages;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizard;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

public abstract class AbstractRemoteResourceManagerConfigurationWizardPage extends
		RMConfigurationWizardPage {
	
	protected class WidgetListener extends SelectionAdapter implements ModifyListener, IPropertyChangeListener 
	{
		/* (non-Javadoc)
		 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
		 */
		public void modifyText(ModifyEvent evt) {
		}
	
		/* (non-Javadoc)
		 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
		 */
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(FieldEditor.IS_VALID)) {
				updatePage();
			}
		}
	
		/* (non-Javadoc)
		 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			updateSettings();
			updatePage();
		}
	}
	
	public static final String EMPTY_STRING = "";
	private AbstractRemoteResourceManagerConfiguration config;
	private String localAddr = EMPTY_STRING;
	private IRemoteServices remoteServices = null;
	private IRemoteConnectionManager connectionManager = null;
	private IRemoteUIConnectionManager uiConnectionManager = null;
	private IRemoteConnection connection = null;
	private boolean loading = true;
	private boolean isValid;
	private boolean muxPortFwd = false;
	private boolean portFwdSupported = true;

	private Button noneButton = null;
	private Button portForwardingButton = null;
	private Button manualButton = null;
	private WidgetListener listener = new WidgetListener();
	private Button newConnectionButton;
	private Combo  remoteCombo;
	private Combo  connectionCombo;

	private Combo  localAddrCombo;

	public AbstractRemoteResourceManagerConfigurationWizardPage(RMConfigurationWizard wizard,
			String title) {
		super(wizard, title);
		
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
		createContents(composite);
		setControl(composite);
	}
	
	/**
	 * Initialize the contents of the local address selection combo. Host names are obtained by
	 * performing a reverse lookup on the IP addresses of each network interface. If DNS is configured
	 * correctly, this should add the fully qualified domain name, otherwise it will probably be
	 * the IP address. We also add the configuration address to the combo in case it was specified manually.
	 */
	public void initializeLocalHostCombo() {
		Set<String> addrs = new TreeSet<String>();
		try {
			Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
			while (netInterfaces.hasMoreElements()) {
				NetworkInterface ni = (NetworkInterface)netInterfaces.nextElement();
				Enumeration<InetAddress> alladdr = ni.getInetAddresses();
				while (alladdr.hasMoreElements()) {
					InetAddress ip = (InetAddress)alladdr.nextElement();
					if (ip instanceof Inet4Address) {
						addrs.add(fixHostName(ip.getCanonicalHostName()));
					}
				}
			}                  
		} catch (Exception e) {
		}
		if (addrs.size() == 0) {
			addrs.add("localhost"); //$NON-NLS-1$
		}
		localAddrCombo.removeAll();
		int index = 0;
		int selection = -1;
		for (String addr : addrs) {
			localAddrCombo.add(addr);
			if ((localAddr.equals("") && addr.equals("localhost")) //$NON-NLS-1$ //$NON-NLS-2$
					|| addr.equals(localAddr)) {
				selection = index;
			}
			index++;
		}
		/*
		 * localAddr is not in the list, so add it and make
		 * it the current selection
		 */
		if (selection < 0) {
			if (!localAddr.equals("")){ //$NON-NLS-1$
				localAddrCombo.add(localAddr);
			}
			selection = localAddrCombo.getItemCount()-1;
		}
		localAddrCombo.select(selection);
	}

	/**
	 * Save the current state in the RM configuration. This is called whenever
	 * anything is changed.
	 * 
	 * @return
	 */
	public boolean performOk() 
	{
		int options = 0;
		if (muxPortFwd) {
			options |= IRemoteProxyOptions.PORT_FORWARDING;
		}
		if (remoteServices != null) {
			config.setRemoteServicesId(remoteServices.getId());
		}
		if (connection != null) {
			config.setConnectionName(connection.getName());
		}
		config.setLocalAddress(localAddr);
		config.setOptions(options);
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
		 * Composite for remote information
		 */
		Composite remoteComp = new Composite(parent, SWT.NONE);
		GridLayout remoteLayout = new GridLayout();
		remoteLayout.numColumns = 4;
		remoteLayout.marginWidth = 0;
		remoteComp.setLayout(remoteLayout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 4;
		remoteComp.setLayoutData(gd);

		/*
		 * Remote provider
		 */
		Label label = new Label(remoteComp, SWT.NONE);
		label.setText(Messages.getString("RemoteConfigurationWizard.provider")); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalSpan = 1;
		label.setLayoutData(gd);
		
		remoteCombo = new Combo(remoteComp, SWT.DROP_DOWN | SWT.READ_ONLY);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		remoteCombo.setLayoutData(gd);
		
		/*
		 * Remote location
		 */
		label = new Label(remoteComp, SWT.NONE);
		label.setText(Messages.getString("RemoteConfigurationWizard.location")); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalSpan = 1;
		label.setLayoutData(gd);
		
		connectionCombo = new Combo(remoteComp, SWT.DROP_DOWN | SWT.READ_ONLY);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		connectionCombo.setLayoutData(gd);

		newConnectionButton = SWTUtil.createPushButton(remoteComp, Messages.getString("RemoteConfigurationWizard.newButton"), null); //$NON-NLS-1$

		/*
		 * Multiplexing options
		 */
		Group mxGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		mxGroup.setLayout(createGridLayout(1, true, 10, 10));
		mxGroup.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));
		mxGroup.setText(Messages.getString("RemoteConfigurationWizard.mxOptions"));
		
		noneButton = createRadioButton(mxGroup, Messages.getString("RemoteConfigurationWizard.noneButton"), "mxGroup", listener); //$NON-NLS-1$ //$NON-NLS-2$
		noneButton.addSelectionListener(listener);
		
		/*
		 * Local address
		 */
		Composite addrComp = new Composite(mxGroup, SWT.NONE);
		GridLayout addrLayout = new GridLayout();
		addrLayout.numColumns = 2;
		addrLayout.marginWidth = 0;
		addrLayout.marginLeft = 15;
		addrComp.setLayout(addrLayout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		addrComp.setLayoutData(gd);

		label = new Label(addrComp, SWT.NONE);
		label.setText(Messages.getString("RemoteConfigurationWizard.localAddress")); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalSpan = 1;
		label.setLayoutData(gd);
		
		localAddrCombo = new Combo(addrComp, SWT.DROP_DOWN);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		localAddrCombo.setLayoutData(gd);

		portForwardingButton = createRadioButton(mxGroup, Messages.getString("RemoteConfigurationWizard.portForwardingButton"), "mxGroup", listener); //$NON-NLS-1$ //$NON-NLS-2$
		portForwardingButton.addSelectionListener(listener);

		registerListeners();
	}
	
	/**
	 * In some nameserver configurations, getCanonicalHostName() will return the inverse mapping 
	 * of the IP address (e.g. 1.1.0.192.in-addr.arpa). In this case we just use the IP address.
	 * 
	 * @param hostname host name to be fixed
	 * @return fixed host name
	 */
	private String fixHostName(String hostname) {
		try {
			if (hostname.endsWith(".in-addr.arpa")) { //$NON-NLS-1$
				return InetAddress.getLocalHost().getHostAddress();
			}
		} catch (UnknownHostException e) {
		}
		return hostname;
	}
	
	/**
	 * Initialize the contents of the controls on the page. This is called after the
	 * controls have been created.
	 */
	private void initContents() {
		loading = true;
		config = (AbstractRemoteResourceManagerConfiguration) getConfigurationWizard().getConfiguration();
		loadSaved();
		updateSettings();
		initializeRemoteServicesCombo();
		initializeLocalHostCombo();
		updatePage();
		loading = false;
	}
	
	/**
	 * Load the initial wizard state from the configuration settings.
	 */
	private void loadSaved()
	{
		localAddr = config.getLocalAddress();
		
		String rmID = config.getRemoteServicesId();
		if (rmID != null) {
			remoteServices = PTPRemoteCorePlugin.getDefault().getRemoteServices(rmID);
			String conn = config.getConnectionName();
			if (remoteServices != null && conn != null) {
				connection = remoteServices.getConnectionManager().getConnection(conn);
			}
		}
		
		int options = config.getOptions();
		
		muxPortFwd = (options & IRemoteProxyOptions.PORT_FORWARDING) == IRemoteProxyOptions.PORT_FORWARDING;
	}

	private void registerListeners() {
		remoteCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				handleRemoteServiceSelected();
				updateSettings();
			}
		});
		connectionCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				handleConnectionSelected();
				updatePage();
			}
		});
		newConnectionButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleNewRemoteConnectionSelected();
				updatePage();
			}
		});	
		localAddrCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateSettings();
				updatePage();
			}
		});
	}
	
	/**
	 * Set the isValid flag and page completion status
	 * 
	 * @param complete	true if complete
	 */
	private void setValid(boolean complete) {
		isValid = complete;
		setPageComplete(isValid);
	}
	
	/**
	 * Update wizard UI selections from settings. This should be called whenever any
	 * settings are changed.
	 */
	private void updateSettings() {
		/*
		 * Get current settings unless we're initializing things
		 */
		if (!loading) {
			muxPortFwd = portForwardingButton.getSelection();
		}

		/*
		 * If no localAddr has been specified in the configuration, select
		 * a default one.
		 */
		if (!loading || localAddr.equals("")) {
			localAddr = localAddrCombo.getText();
		}

		/*
		 * Fix settings
		 */
		if (muxPortFwd && !portFwdSupported) {
			muxPortFwd = false;
		}
		
		/*
		 * Update UI to display correct settings
		 */
		if (noneButton != null) {
			noneButton.setSelection(!muxPortFwd);
		}
		
		if (portForwardingButton != null) {
			portForwardingButton.setSelection(muxPortFwd);
			portForwardingButton.setEnabled(portFwdSupported);
		}
		
		if (localAddrCombo != null) {
			localAddrCombo.setEnabled(!muxPortFwd);
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
		}
		/*
		 * Linux doesn't call modify handler (which calls updateSettings & updatePage) so need to call them explicitly here 
		 */
		updateSettings();
		updatePage();
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
		if (uiConnectionManager != null) {
			uiConnectionManager.newConnection(getShell());
			handleRemoteServiceSelected();
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
		IRemoteServices[] allRemoteServices = PTPRemoteCorePlugin.getDefault().getAllRemoteServices();
		int selectionIndex = remoteCombo.getSelectionIndex();
		if (allRemoteServices != null && allRemoteServices.length > 0 && selectionIndex >=0) {
			remoteServices = allRemoteServices[selectionIndex];
			connectionManager = remoteServices.getConnectionManager();
			IRemoteUIServices remUIServices = PTPRemoteUIPlugin.getDefault().getRemoteUIServices(remoteServices);
			if (remUIServices != null) {
				uiConnectionManager = remUIServices.getUIConnectionManager();
			}
			IRemoteConnection[] connections = connectionManager.getConnections();
			connectionCombo.removeAll();
			int selected = connections.length - 1;
			for (int i = 0; i < connections.length; i++) {
				connectionCombo.add(connections[i].getName());
				if (connection != null && connections[i].equals(connection)) {
					selected = i;
				}
			}
			if (connections.length > 0) {
				/*
				 * If we're not initializing then reset connection when new
				 * service provider is selected.
				 */
				if (!loading) {
					selected = connections.length - 1;
					connection = null;
				}
							
				connectionCombo.select(selected);
				/*
				 * Linux doesn't call selection handler so need to call it explicitly here 
				 */
				handleConnectionSelected(); 
			}
			
			/*
			 * Enable 'new' button if new connections are supported
			 */
			newConnectionButton.setEnabled(uiConnectionManager != null);
		}
	}

	/**
	 * Initialize the contents of the remote services combo.
	 * 
	 * The assumption is that this will trigger a call to the selection handling
	 * routine when the default index is selected.
	 */
	protected void initializeRemoteServicesCombo() {
		IRemoteServices[] allServices = PTPRemoteCorePlugin.getDefault().getAllRemoteServices();
		IRemoteServices defServices;
		if (remoteServices != null) {
			defServices = remoteServices;
		} else {
			defServices = PTPRemoteCorePlugin.getDefault().getDefaultServices();
		}
		int defIndex = allServices.length - 1; 
		remoteCombo.removeAll();
		for (int i = 0; i < allServices.length; i++) {
			remoteCombo.add(allServices[i].getName());
			if (allServices[i].equals(defServices)) {
				defIndex = i;
			}
		}
		if (allServices.length > 0) {
			remoteCombo.select(defIndex);
			/*
			 * Linux doesn't call selection handler so need to call it explicitly here
			 */ 
			handleRemoteServiceSelected(); 
			handleConnectionSelected();
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
		if (style == -1)
			gd = new GridData();
		else
			gd = new GridData(style);
		gd.horizontalSpan = space;
		return gd;
	}
	
	/**
	 * Call to update page status and store any changed settings
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
