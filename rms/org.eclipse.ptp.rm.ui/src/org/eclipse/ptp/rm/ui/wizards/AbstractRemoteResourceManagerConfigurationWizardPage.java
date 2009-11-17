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

package org.eclipse.ptp.rm.ui.wizards;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteProxyOptions;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.ptp.remote.ui.IRemoteUIServices;
import org.eclipse.ptp.remote.ui.PTPRemoteUIPlugin;
import org.eclipse.ptp.rm.core.rmsystem.IRemoteResourceManagerConfiguration;
import org.eclipse.ptp.rm.ui.messages.Messages;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.ui.wizards.IRMConfigurationWizard;
import org.eclipse.ptp.utils.ui.swt.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

/**
 * Abstract base class for wizard pages used to configure remote resource managers
 */
public abstract class AbstractRemoteResourceManagerConfigurationWizardPage extends
		AbstractConfigurationWizardPage {
	
	protected class DataSource extends WizardPageDataSource {
		private IRemoteResourceManagerConfiguration fConfig = null;
		
		private String fRemoteServicesId = null;
		private String fConnectionName = null;
		private boolean fPortForward = true;
		private String fLocalAddr = null;

		protected DataSource(AbstractConfigurationWizardPage page) {
			super(page);
		}

		public String getConnectionName() {
			return fConnectionName;
		}

		public String getLocalAddr() {
			return fLocalAddr;
		}

		public boolean getPortForward() {
			return fPortForward;
		}

		public String getRemoteServicesId() {
			return fRemoteServicesId;
		}

		public void setCommandFields(String remServId, String connName, String addr, boolean portFwd) {
			fRemoteServicesId = remServId;
			fConnectionName = connName;
			fLocalAddr = addr;
			fPortForward = portFwd;
		}

		@Override
		public void setConfig(IResourceManagerConfiguration configuration) {
			super.setConfig(configuration);
			// Store a local reference to the configuration
			fConfig = (IRemoteResourceManagerConfiguration) configuration;
		}

		@Override
		protected void copyFromFields() throws ValidationException {
			if (remoteCombo != null) {
				fRemoteServicesId = remoteServices.getId();
			}
			if (connectionCombo != null) {
				fConnectionName = connection.getName();
			}
			if (localAddrCombo != null) {
				fLocalAddr = extractText(localAddrCombo);
			}
			if (portForwardingButton != null) {
				fPortForward = portForwardingButton.getSelection();
			}
		}

		@Override
		protected void copyToFields() {
			if (localAddrCombo != null) {
				applyText(localAddrCombo, fLocalAddr);
			}
			if (fRemoteServicesId != null) {
				remoteServices = PTPRemoteCorePlugin.getDefault().getRemoteServices(fRemoteServicesId);
			}
			if (remoteServices != null && fConnectionName != null) {
				connection = remoteServices.getConnectionManager().getConnection(fConnectionName);
			}
			handleRemoteServiceSelected(connection);
		}

		@Override
		protected void copyToStorage() {
			if (remoteCombo != null) {
				fConfig.setRemoteServicesId(fRemoteServicesId);
			}
			if (connectionCombo != null) {
				fConfig.setConnectionName(fConnectionName);
			}
			if (localAddrCombo != null) {
				fConfig.setLocalAddress(fLocalAddr);
			}
			if (portForwardingButton != null) {
				int options = fConfig.getOptions();
				if (fPortForward) {
					options |= IRemoteProxyOptions.PORT_FORWARDING;
				} else {
					options &= ~IRemoteProxyOptions.PORT_FORWARDING;
				}
				fConfig.setOptions(options);
			}
		}

		@Override
		protected void loadDefault() {
			// not available
		}

		@Override
		protected void loadFromStorage() {
			if (remoteCombo != null) {
				fRemoteServicesId = fConfig.getRemoteServicesId();
				if (fRemoteServicesId != null) {
					remoteServices = PTPRemoteCorePlugin.getDefault().getRemoteServices(fRemoteServicesId);
				}
			}
			if (connectionCombo != null) {
				fConnectionName = fConfig.getConnectionName();
				if (remoteServices != null && fConnectionName != null) {
					connection = remoteServices.getConnectionManager().getConnection(fConnectionName);
				}
			}
			if (localAddrCombo != null) {
				fLocalAddr = fConfig.getLocalAddress();
				initializeLocalHostCombo();
			}
			if (portForwardingButton != null) {
				fPortForward = (fConfig.getOptions() & IRemoteProxyOptions.PORT_FORWARDING) == IRemoteProxyOptions.PORT_FORWARDING;
			}
			initializeRemoteServicesCombo();
		}

		@Override
		protected void validateGlobal() throws ValidationException {
			// Nothing yet. Would validate the entire GenericMPIResourceManagerConfiguration.
		}

		@Override
		protected void validateLocal() throws ValidationException {
			// Nothing to validate
		}
	}

	protected class WidgetListener extends WizardPageWidgetListener {
		@Override
		protected void doModifyText(ModifyEvent evt) {
			Object source = evt.getSource();
			if (source == remoteCombo) {
				handleRemoteServiceSelected(null);
				updateControls();
			} else if (source == connectionCombo) {
				handleConnectionSelected();
				getDataSource().storeAndValidate();
				updateControls();
			} else if (source == localAddrCombo) {
				resetErrorMessages();
				getDataSource().storeAndValidate();
			} else {
				assert false;
			}
		}

		@Override
		protected void doWidgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			if (source == newConnectionButton) {
				handleNewRemoteConnectionSelected();
				resetErrorMessages();
				getDataSource().storeAndValidate();
				updateControls();
			} else if (source == noneButton || source == portForwardingButton)  {
				resetErrorMessages();
				getDataSource().storeAndValidate();
				updateControls();
			} else {
				assert false;
			}
		}
	}
	
	public static final String EMPTY_STRING = ""; //$NON-NLS-1$
	protected IRemoteServices remoteServices = null;
	private IRemoteConnectionManager connectionManager = null;
	private IRemoteUIConnectionManager uiConnectionManager = null;
	protected IRemoteConnection connection = null;
	protected boolean portFwdSupported = true;

	protected Button noneButton = null;
	protected Button portForwardingButton = null;
	protected WidgetListener listener = new WidgetListener();
	protected Button newConnectionButton;
	protected Combo  remoteCombo;
	protected Combo  connectionCombo;
	protected Combo  localAddrCombo;

	public AbstractRemoteResourceManagerConfigurationWizardPage(IRMConfigurationWizard wizard,
			String title) {
		super(wizard, title);
		
		setPageComplete(false);
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
				NetworkInterface ni = netInterfaces.nextElement();
				Enumeration<InetAddress> alladdr = ni.getInetAddresses();
				while (alladdr.hasMoreElements()) {
					InetAddress ip = alladdr.nextElement();
					if (ip instanceof Inet4Address) {
						addrs.add(fixHostName(ip.getCanonicalHostName()));
					}
				}
			}                  
		} catch (Exception e) {
			// At least we'll still get localhost
		}
		if (addrs.size() == 0) {
			addrs.add("localhost"); //$NON-NLS-1$
		}
		localAddrCombo.removeAll();
		int index = 0;
		int selection = -1;
		DataSource data = (DataSource)getDataSource();
		for (String addr : addrs) {
			localAddrCombo.add(addr);
			if ((data.getLocalAddr().equals("") && addr.equals("localhost")) //$NON-NLS-1$ //$NON-NLS-2$
					|| addr.equals(data.getLocalAddr())) {
				selection = index;
			}
			index++;
		}
		/*
		 * localAddr is not in the list, so add it and make
		 * it the current selection
		 */
		if (selection < 0) {
			if (!data.getLocalAddr().equals("")){ //$NON-NLS-1$
				localAddrCombo.add(data.getLocalAddr());
			}
			selection = localAddrCombo.getItemCount()-1;
		}
		localAddrCombo.select(selection);
	}
	
	@Override
	public void updateControls() {
		boolean portFwd = ((DataSource)getDataSource()).getPortForward();
		
		if (localAddrCombo != null) {
			localAddrCombo.setEnabled(!portFwd);
		}
		if (portForwardingButton != null) {
			portForwardingButton.setEnabled(portFwdSupported);
			portForwardingButton.setSelection(portFwdSupported ? portFwd : false);
		}
		if (noneButton != null) {
			noneButton.setSelection(portFwdSupported ? !portFwd : true);
		}
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
		label.setText(Messages.AbstractRemoteResourceManagerConfigurationWizardPage_0);
		gd = new GridData();
		gd.horizontalSpan = 1;
		label.setLayoutData(gd);
		
		remoteCombo = new Combo(remoteComp, SWT.DROP_DOWN | SWT.READ_ONLY);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		remoteCombo.setLayoutData(gd);
		remoteCombo.addModifyListener(getWidgetListener());
		
		/*
		 * Remote location
		 */
		label = new Label(remoteComp, SWT.NONE);
		label.setText(Messages.AbstractRemoteResourceManagerConfigurationWizardPage_1);
		gd = new GridData();
		gd.horizontalSpan = 1;
		label.setLayoutData(gd);
		
		connectionCombo = new Combo(remoteComp, SWT.DROP_DOWN | SWT.READ_ONLY);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		connectionCombo.setLayoutData(gd);
		connectionCombo.addModifyListener(getWidgetListener());

		newConnectionButton = SWTUtil.createPushButton(remoteComp, Messages.AbstractRemoteResourceManagerConfigurationWizardPage_2, null);
		newConnectionButton.addSelectionListener(getWidgetListener());

		/*
		 * Multiplexing options
		 */
		Group mxGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		mxGroup.setLayout(createGridLayout(1, true, 10, 10));
		mxGroup.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));
		mxGroup.setText(Messages.AbstractRemoteResourceManagerConfigurationWizardPage_3);
		
		noneButton = createRadioButton(mxGroup, Messages.AbstractRemoteResourceManagerConfigurationWizardPage_4, "mxGroup", listener); //$NON-NLS-1$
		noneButton.addSelectionListener(getWidgetListener());
		
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
		label.setText(Messages.AbstractRemoteResourceManagerConfigurationWizardPage_5);
		gd = new GridData();
		gd.horizontalSpan = 1;
		label.setLayoutData(gd);
		
		localAddrCombo = new Combo(addrComp, SWT.DROP_DOWN);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		localAddrCombo.setLayoutData(gd);
		localAddrCombo.addModifyListener(getWidgetListener());

		portForwardingButton = createRadioButton(mxGroup, Messages.AbstractRemoteResourceManagerConfigurationWizardPage_6, "mxGroup", listener); //$NON-NLS-1$
		portForwardingButton.addSelectionListener(getWidgetListener());
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
			// Should not happen
		}
		return hostname;
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
	

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ui.wizards.AbstractConfigurationWizardPage#createDataSource()
	 */
	@Override
	protected WizardPageDataSource createDataSource() {
		return new DataSource(this);
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

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.ui.wizards.AbstractConfigurationWizardPage#createListener()
	 */
	@Override
	protected WizardPageWidgetListener createListener() {
		return new WidgetListener();
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

	@Override
	protected Composite doCreateContents(Composite parent) {
		Composite contents = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginBottom = 0;
		layout.marginTop = 0;
		layout.marginRight = 0;
		layout.marginLeft = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		contents.setLayout(layout);

		createContents(contents);

		return contents;
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
			handleRemoteServiceSelected(uiConnectionManager.newConnection(getShell()));
		}
	}

	/**
	 * Handle selection of a new remote services provider from the 
	 * remote services combo.
	 * 
	 * The assumption is that this will trigger a call to the selection
	 * handler for the connection combo.
	 * 
	 * @param conn connection to select as current. If conn is null, select the first item in the list.
	 */
	protected void handleRemoteServiceSelected(IRemoteConnection conn) {
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
			Arrays.sort(connections, new Comparator<IRemoteConnection>() {
				public int compare(IRemoteConnection c1, IRemoteConnection c2) {
					return c1.getName().compareToIgnoreCase(c2.getName());
				}
			});
			connectionCombo.removeAll();
			int selected = 0;
			for (int i = 0; i < connections.length; i++) {
				connectionCombo.add(connections[i].getName());
				if (conn != null && connections[i].equals(conn)) {
					selected = i;
				}
			}
			if (connections.length > 0) {
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
		int defIndex = 0; 
		Arrays.sort(allServices, new Comparator<IRemoteServices>() {
			public int compare(IRemoteServices c1, IRemoteServices c2) {
				return c1.getName().compareToIgnoreCase(c2.getName());
			}
		});		
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
			handleRemoteServiceSelected(connection); 
			handleConnectionSelected();
		}
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
}
