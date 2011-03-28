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
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteProxyOptions;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.ptp.remote.ui.PTPRemoteUIPlugin;
import org.eclipse.ptp.remote.ui.widgets.RemoteConnectionWidget;
import org.eclipse.ptp.rm.core.rmsystem.IRemoteResourceManagerConfiguration;
import org.eclipse.ptp.rm.ui.messages.Messages;
import org.eclipse.ptp.ui.preferences.ScrolledPageContent;
import org.eclipse.ptp.ui.wizards.IRMConfigurationWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

/**
 * Abstract base class for wizard pages used to configure remote resource
 * managers
 */
public abstract class AbstractRemoteResourceManagerConfigurationWizardPage extends AbstractConfigurationWizardPage {

	protected class RMDataSource extends WizardPageDataSource {
		private IRemoteConnection fConnection = null;
		private boolean fPortForward = true;
		private String fLocalAddr = null;
		private boolean fUseDefault = false;

		protected RMDataSource(AbstractConfigurationWizardPage page) {
			super(page);
		}

		/**
		 * @since 2.0
		 */
		@Override
		public IRemoteResourceManagerConfiguration getConfiguration() {
			return (IRemoteResourceManagerConfiguration) super.getConfiguration();
		}

		/**
		 * @since 2.0
		 */
		public IRemoteConnection getConnection() {
			return fConnection;
		}

		public String getLocalAddr() {
			return fLocalAddr;
		}

		public boolean getPortForward() {
			return fPortForward;
		}

		/**
		 * @since 2.0
		 */
		public boolean getUseDefault() {
			return fUseDefault;
		}

		/**
		 * @since 2.0
		 */
		public void setConnection(IRemoteConnection conn) {
			fConnection = conn;
		}

		public void setLocalAddr(String addr) {
			fLocalAddr = addr;
		}

		public void setPortForward(boolean portFwd) {
			fPortForward = portFwd;
		}

		/**
		 * @since 2.0
		 */
		public void setUseDefault(boolean flag) {
			fUseDefault = flag;
		}

		@Override
		protected void copyFromFields() throws ValidationException {
			if (connectionWidget != null) {
				fConnection = connectionWidget.getConnection();
			}
			if (localAddrCombo != null) {
				fLocalAddr = extractText(localAddrCombo);
			}
			if (portForwardingButton != null) {
				fPortForward = portForwardingButton.getSelection();
			}
			if (useDefaultButton != null) {
				fUseDefault = useDefaultButton.getSelection();
			}
		}

		@Override
		protected void copyToFields() {
			if (localAddrCombo != null) {
				applyText(localAddrCombo, fLocalAddr);
			}
			if (useDefaultButton != null) {
				useDefaultButton.setSelection(fUseDefault);
			}
			if (connectionWidget != null) {
				connectionWidget.setConnection(fConnection);
			}
			updateControls();
		}

		@Override
		protected void copyToStorage() {
			if (fConnection != null) {
				getConfiguration().setRemoteServicesId(fConnection.getRemoteServices().getId());
				getConfiguration().setConnectionName(fConnection.getName());
			}
			if (localAddrCombo != null) {
				getConfiguration().setLocalAddress(fLocalAddr);
			}
			if (portForwardingButton != null) {
				int options = getConfiguration().getOptions();
				if (fPortForward) {
					options |= IRemoteProxyOptions.PORT_FORWARDING;
				} else {
					options &= ~IRemoteProxyOptions.PORT_FORWARDING;
				}
				getConfiguration().setOptions(options);
			}
			if (useDefaultButton != null) {
				getConfiguration().setUseDefault(fUseDefault);
			}
		}

		@Override
		protected void loadDefault() {
			// not available
		}

		@Override
		protected void loadFromStorage() {
			String id = getConfiguration().getRemoteServicesId();
			if (id != null) {
				IRemoteServices services = getRemoteServices(id);
				if (services != null) {
					String name = getConfiguration().getConnectionName();
					fConnection = getRemoteConnection(services, name);
				}
			} else {
				IRemoteServices services = PTPRemoteCorePlugin.getDefault().getDefaultServices();
				fConnection = services.getConnectionManager().getConnections()[0];
				copyToStorage();
			}
			fLocalAddr = getConfiguration().getLocalAddress();
			if (localAddrCombo != null) {
				initializeLocalHostCombo();
			}
			fPortForward = (getConfiguration().getOptions() & IRemoteProxyOptions.PORT_FORWARDING) == IRemoteProxyOptions.PORT_FORWARDING;
			fUseDefault = getConfiguration().getUseDefault();
		}

		@Override
		protected void validateGlobal() throws ValidationException {
			// Nothing yet. Would validate the entire
			// GenericMPIResourceManagerConfiguration.
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
			if (source == localAddrCombo) {
				resetErrorMessages();
				getDataSource().storeAndValidate();
			} else {
				assert false;
			}
		}

		@Override
		protected void doWidgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			if (source == connectionWidget) {
				handleConnectionSelected();
				getDataSource().storeAndValidate();
				updateControls();
			} else if (source == noneButton || source == portForwardingButton) {
				resetErrorMessages();
				getDataSource().storeAndValidate();
			} else if (source == localAddrCombo) {
				resetErrorMessages();
				getDataSource().storeAndValidate();
			} else if (useDefaultButton != null && source == useDefaultButton) {
				getDataSource().storeAndValidate();
				updateControls();
			} else {
				assert false;
			}
		}
	}

	public static final String EMPTY_STRING = ""; //$NON-NLS-1$

	private final IRemoteUIConnectionManager fUIConnectionManager = null;
	private boolean fEnableUseDefault = false;
	private String fUseDefaultMessage;

	protected Button noneButton = null;
	protected Button portForwardingButton = null;
	/**
	 * @since 2.0
	 */
	protected Button useDefaultButton = null;
	protected WidgetListener listener = new WidgetListener();
	protected Combo localAddrCombo;
	/**
	 * @since 2.0
	 */
	protected RemoteConnectionWidget connectionWidget;
	/**
	 * @since 2.0
	 */
	protected ExpandableComposite advancedOptions;

	protected IRemoteServices[] fRemoteServices;

	public AbstractRemoteResourceManagerConfigurationWizardPage(IRMConfigurationWizard wizard, String title) {
		super(wizard, title);
		setPageComplete(false);
	}

	/**
	 * Set the message and enable the used default checkbox
	 * 
	 * @since 2.0
	 */
	protected void setEnableUseDefault(String message) {
		fEnableUseDefault = true;
		fUseDefaultMessage = message;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.ui.wizards.RMConfigurationWizardPage#createControl(org
	 * .eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout topLayout = new GridLayout();
		topLayout.marginHeight = 0;
		topLayout.marginWidth = 0;
		composite.setLayout(topLayout);
		Composite pageContent = createContents(composite);
		pageContent.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
		setControl(composite);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.ui.wizards.AbstractConfigurationWizardPage#getDataSource
	 * ()
	 */
	@Override
	public RMDataSource getDataSource() {
		return (RMDataSource) super.getDataSource();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.ui.wizards.AbstractConfigurationWizardPage#updateControls
	 * ()
	 */
	@Override
	public void updateControls() {
		final boolean enabled = getWidgetListener().isEnabled();
		getWidgetListener().disable();
		final boolean portFwd = getDataSource().getPortForward();
		boolean supportsPortForwarding = false;
		if (getDataSource().getConnection() != null) {
			supportsPortForwarding = getDataSource().getConnection().supportsTCPPortForwarding();
		}
		if (localAddrCombo != null) {
			localAddrCombo.setEnabled(!portFwd);
		}
		if (portForwardingButton != null) {
			portForwardingButton.setEnabled(supportsPortForwarding);
			portForwardingButton.setSelection(supportsPortForwarding ? portFwd : false);
		}
		if (noneButton != null) {
			noneButton.setSelection(supportsPortForwarding ? !portFwd : true);
		}
		getWidgetListener().setEnabled(enabled);
	}

	/**
	 * Create the contents of the wizard page.
	 * 
	 * @param parent
	 * @param colSpan
	 */
	private Composite createContents(Composite parent) {
		ScrolledPageContent pageContent = new ScrolledPageContent(parent);
		GridLayout layout = new GridLayout();
		// layout.numColumns = 4;
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		Composite composite = pageContent.getBody();
		composite.setLayout(layout);

		if (fEnableUseDefault) {
			useDefaultButton = new Button(composite, SWT.CHECK);
			useDefaultButton.setText(fUseDefaultMessage);
			useDefaultButton.addSelectionListener(getWidgetListener());
		}

		connectionWidget = new RemoteConnectionWidget(composite, SWT.NONE, null);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		connectionWidget.setLayoutData(gd);
		connectionWidget.addSelectionListener(getWidgetListener());

		/*
		 * Advanced options
		 */
		advancedOptions = new ExpandableComposite(composite, SWT.NONE, ExpandableComposite.TWISTIE
				| ExpandableComposite.CLIENT_INDENT);
		advancedOptions.setText(Messages.AbstractRemoteResourceManagerConfigurationWizardPage_AdvancedOptions);
		advancedOptions.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				ScrolledPageContent parent = getParentScrolledComposite((ExpandableComposite) e.getSource());
				if (parent != null) {
					parent.reflow(true);
				}
			}
		});
		advancedOptions.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));

		Group mxGroup = new Group(advancedOptions, SWT.SHADOW_ETCHED_IN);
		advancedOptions.setClient(mxGroup);
		mxGroup.setLayout(createGridLayout(1, true, 10, 10));
		mxGroup.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));
		mxGroup.setText(Messages.AbstractRemoteResourceManagerConfigurationWizardPage_3);

		noneButton = createRadioButton(mxGroup, Messages.AbstractRemoteResourceManagerConfigurationWizardPage_4,
				"mxGroup", listener); //$NON-NLS-1$
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

		Label label = new Label(addrComp, SWT.NONE);
		label.setText(Messages.AbstractRemoteResourceManagerConfigurationWizardPage_5);
		gd = new GridData();
		gd.horizontalSpan = 1;
		label.setLayoutData(gd);

		localAddrCombo = new Combo(addrComp, SWT.DROP_DOWN);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		localAddrCombo.setLayoutData(gd);
		localAddrCombo.addModifyListener(getWidgetListener());
		localAddrCombo.addSelectionListener(getWidgetListener());

		portForwardingButton = createRadioButton(mxGroup, Messages.AbstractRemoteResourceManagerConfigurationWizardPage_6,
				"mxGroup", listener); //$NON-NLS-1$
		portForwardingButton.addSelectionListener(getWidgetListener());

		return pageContent;
	}

	/**
	 * In some nameserver configurations, getCanonicalHostName() will return the
	 * inverse mapping of the IP address (e.g. 1.1.0.192.in-addr.arpa). In this
	 * case we just use the IP address.
	 * 
	 * @param hostname
	 *            host name to be fixed
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ui.wizards.AbstractConfigurationWizardPage#
	 * createDataSource()
	 */
	@Override
	protected WizardPageDataSource createDataSource() {
		return new RMDataSource(this);
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
	protected GridLayout createGridLayout(int columns, boolean isEqual, int mh, int mw) {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = columns;
		gridLayout.makeColumnsEqualWidth = isEqual;
		gridLayout.marginHeight = mh;
		gridLayout.marginWidth = mw;
		return gridLayout;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.ui.wizards.AbstractConfigurationWizardPage#createListener
	 * ()
	 */
	@Override
	protected WizardPageWidgetListener createListener() {
		return new WidgetListener();
	}

	/**
	 * Creates an new radio button instance and sets the default layout data.
	 * 
	 * @param group
	 *            the composite in which to create the radio button
	 * @param label
	 *            the string to set into the radio button
	 * @param value
	 *            the string to identify radio button
	 * @return the new radio button
	 */
	protected Button createRadioButton(Composite parent, String label, String value, SelectionListener listener) {
		Button button = createButton(parent, label, SWT.RADIO | SWT.LEFT);
		button.setData((null == value) ? label : value);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.BEGINNING;
		button.setLayoutData(data);
		if (null != listener) {
			button.addSelectionListener(listener);
		}
		return button;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ui.wizards.AbstractConfigurationWizardPage#
	 * doCreateContents(org.eclipse.swt.widgets.Composite)
	 */
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
	protected String getFieldContent(String text) {
		if (text.trim().length() == 0 || text.equals(EMPTY_STRING)) {
			return null;
		}

		return text;
	}

	/**
	 * @since 2.0
	 */
	protected ScrolledPageContent getParentScrolledComposite(Control control) {
		Control parent = control.getParent();
		while (!(parent instanceof ScrolledPageContent) && parent != null) {
			parent = parent.getParent();
		}
		if (parent instanceof ScrolledPageContent) {
			return (ScrolledPageContent) parent;
		}
		return null;
	}

	/**
	 * Get a remote connection given a remote services provider and a connection
	 * name
	 * 
	 * @param services
	 *            remote services
	 * @param name
	 *            connection name
	 * @return remote connection or null if the name is not valid
	 */
	protected IRemoteConnection getRemoteConnection(IRemoteServices services, String name) {
		IRemoteConnectionManager manager = getRemoteConnectionManager(services);
		if (manager != null) {
			return manager.getConnection(name);
		}
		return null;
	}

	/**
	 * Get the connection manager for the given services
	 * 
	 * @return connection manager or null if one can't be found
	 */
	protected IRemoteConnectionManager getRemoteConnectionManager(IRemoteServices services) {
		if (services != null) {
			return services.getConnectionManager();
		}
		return null;
	}

	/**
	 * Get remote services give a remote services id.
	 * 
	 * @param id
	 *            id of remote services
	 * @return remote services or null if the id is not valid
	 */
	protected IRemoteServices getRemoteServices(String id) {
		if (id != null && !id.equals(EMPTY_STRING)) {
			IWizardContainer container = null;
			if (getControl().isVisible()) {
				container = getWizard().getContainer();
			}
			return PTPRemoteUIPlugin.getDefault().getRemoteServices(id, container);
		}
		return null;
	}

	/**
	 * @since 2.0
	 */
	protected IRemoteUIConnectionManager getRemoteUIConnectionManager() {
		return fUIConnectionManager;
	}

	/**
	 * Handle the section of a new connection. Update connection option buttons
	 * appropriately.
	 * 
	 * @since 2.0
	 */
	protected void handleConnectionSelected() {
		IRemoteConnection connection = connectionWidget.getConnection();
		if (connection != null) {
			boolean portFwd = connection.supportsTCPPortForwarding();
			portForwardingButton.setEnabled(portFwd);
			portForwardingButton.setSelection(portFwd);
			setPageComplete(true);
		} else {
			setPageComplete(false);
		}
	}

	/**
	 * Handle creation of a new connection by pressing the 'New...' button.
	 * Calls handleRemoteServicesSelected() to update the connection combo with
	 * the new connection.
	 * 
	 * TODO should probably select the new connection
	 */
	// protected void handleNewRemoteConnectionSelected() {
	// if (fUIConnectionManager != null) {
	// handleRemoteServiceSelected(fUIConnectionManager.newConnection(getShell()));
	// }
	//
	// resetErrorMessages();
	// getDataSource().storeAndValidate();
	// updateControls();
	// }

	/**
	 * Handle selection of a new remote services provider from the remote
	 * services combo.
	 * 
	 * The assumption is that this will trigger a call to the selection handler
	 * for the connection combo.
	 * 
	 * @param conn
	 *            connection to select as current. If conn is null, select the
	 *            first item in the list.
	 */
	// protected void handleRemoteServiceSelected(IRemoteConnection conn) {
	// final boolean enabled = getWidgetListener().isEnabled();
	// getWidgetListener().disable();
	// int selectionIndex = remoteCombo.getSelectionIndex();
	// if (fRemoteServices != null && fRemoteServices.length > 0 &&
	// selectionIndex >= 0) {
	// IRemoteServices remoteServices = fRemoteServices[selectionIndex];
	// getDataSource().setRemoteServicesId(remoteServices.getId());
	// IRemoteConnectionManager connectionManager =
	// remoteServices.getConnectionManager();
	// IRemoteUIServices remUIServices =
	// PTPRemoteUIPlugin.getDefault().getRemoteUIServices(remoteServices);
	// if (remUIServices != null) {
	// fUIConnectionManager = remUIServices.getUIConnectionManager();
	// }
	// IRemoteConnection[] connections = connectionManager.getConnections();
	// Arrays.sort(connections, new Comparator<IRemoteConnection>() {
	// public int compare(IRemoteConnection c1, IRemoteConnection c2) {
	// return c1.getName().compareToIgnoreCase(c2.getName());
	// }
	// });
	// connectionCombo.removeAll();
	// int selected = 0;
	// for (int i = 0; i < connections.length; i++) {
	// connectionCombo.add(connections[i].getName());
	// if (conn != null && connections[i].equals(conn)) {
	// selected = i;
	// }
	// }
	//
	// /*
	// * Enable 'new' button if new connections are supported
	// */
	// newConnectionButton.setEnabled(fUIConnectionManager != null);
	//
	// if (connections.length > 0) {
	// connectionCombo.select(selected);
	// /*
	// * Events are disabled so call selection handler here
	// */
	// handleConnectionSelected(remoteServices.getId());
	// }
	// }
	//
	// getWidgetListener().setEnabled(enabled);
	// }

	/**
	 * Initialize the contents of the local address selection combo. Host names
	 * are obtained by performing a reverse lookup on the IP addresses of each
	 * network interface. If DNS is configured correctly, this should add the
	 * fully qualified domain name, otherwise it will probably be the IP
	 * address. We also add the configuration address to the combo in case it
	 * was specified manually.
	 */
	protected void initializeLocalHostCombo() {
		final boolean enabled = getWidgetListener().isEnabled();
		getWidgetListener().disable();
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
		for (String addr : addrs) {
			localAddrCombo.add(addr);
			if (addr.equals(getDataSource().getLocalAddr())) {
				selection = index;
			}
			index++;
		}
		/*
		 * localAddr is not in the list, so add it and make it the current
		 * selection
		 */
		if (selection < 0) {
			if (!getDataSource().getLocalAddr().equals("")) { //$NON-NLS-1$
				localAddrCombo.add(getDataSource().getLocalAddr());
			}
			selection = localAddrCombo.getItemCount() - 1;
		}
		localAddrCombo.select(selection);
		getWidgetListener().setEnabled(enabled);
	}

	/**
	 * @param style
	 * @param space
	 * @return
	 */
	protected GridData spanGridData(int style, int space) {
		GridData gd = null;
		if (style == -1) {
			gd = new GridData();
		} else {
			gd = new GridData(style);
		}
		gd.horizontalSpan = space;
		return gd;
	}
}
