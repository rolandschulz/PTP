package org.eclipse.ptp.rm.ui.dialogs;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.operation.IRunnableContext;
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
import org.eclipse.ptp.rm.ui.utils.VolatileRemoteResourceManagerConfiguration;
import org.eclipse.ptp.rm.ui.wizards.AbstractConfigurationWizardPage;
import org.eclipse.ptp.rm.ui.wizards.WizardPageDataSource;
import org.eclipse.ptp.rm.ui.wizards.WizardPageWidgetListener;
import org.eclipse.ptp.ui.preferences.ScrolledPageContent;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

/**
 * @since 2.0
 */
public abstract class ConnectionChoiceContainer {

	public class RMDataSource extends WizardPageDataSource {
		private String fRemoteServicesId = null;
		private String fConnectionName = null;
		private boolean fPortForward = true;
		private String fLocalAddr = null;
		private boolean fUseDefault = false;

		protected RMDataSource() {
			super(page);
			if (page == null) {
				setConfiguration(new VolatileRemoteResourceManagerConfiguration());
			}
		}

		/**
		 * @since 2.0
		 */
		@Override
		public IRemoteResourceManagerConfiguration getConfiguration() {
			return (IRemoteResourceManagerConfiguration) super.getConfiguration();
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

		/**
		 * @since 2.0
		 */
		public boolean getUseDefault() {
			return fUseDefault;
		}

		public void setConnectionName(String connName) {
			fConnectionName = connName;
		}

		public void setLocalAddr(String addr) {
			fLocalAddr = addr;
		}

		public void setPortForward(boolean portFwd) {
			fPortForward = portFwd;
		}

		public void setRemoteServicesId(String remServId) {
			fRemoteServicesId = remServId;
		}

		/**
		 * @since 2.0
		 */
		public void setUseDefault(boolean flag) {
			fUseDefault = flag;
		}

		@Override
		protected void copyFromFields() throws ValidationException {
			if (remoteCombo != null && fRemoteServices != null) {
				int index = remoteCombo.getSelectionIndex();
				if (index >= 0 && index < fRemoteServices.length) {
					fRemoteServicesId = fRemoteServices[index].getId();
				}
			}
			if (connectionCombo != null) {
				fConnectionName = extractText(connectionCombo);
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
			IRemoteServices services = null;
			IRemoteConnection connection = null;
			if (localAddrCombo != null) {
				applyText(localAddrCombo, fLocalAddr);
			}
			if (fRemoteServicesId != null) {
				services = getRemoteServices(fRemoteServicesId);
			}
			if (useDefaultButton != null) {
				useDefaultButton.setSelection(fUseDefault);
			}
			if (services != null && fConnectionName != null) {
				connection = getRemoteConnection(services, fConnectionName);
			}
			handleRemoteServiceSelected(connection);
			updateControls();
		}

		@Override
		protected void copyToStorage() {
			if (remoteCombo != null) {
				getConfiguration().setRemoteServicesId(fRemoteServicesId);
			}
			if (connectionCombo != null) {
				getConfiguration().setConnectionName(fConnectionName);
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
			if (remoteCombo != null) {
				fRemoteServicesId = initializeRemoteServicesCombo(id);
			}
			if (connectionCombo != null) {
				fConnectionName = getConfiguration().getConnectionName();
			}
			if (localAddrCombo != null) {
				fLocalAddr = getConfiguration().getLocalAddress();
				initializeLocalHostCombo();
			}
			if (portForwardingButton != null) {
				fPortForward = (getConfiguration().getOptions() & IRemoteProxyOptions.PORT_FORWARDING) == IRemoteProxyOptions.PORT_FORWARDING;
			}
			if (useDefaultButton != null) {
				fUseDefault = getConfiguration().getUseDefault();
			}
			/*
			 * if id is null then this is a new resource manager save the
			 * default values to storage in case a copyToStorage() is not
			 * triggered elsewhere.
			 */
			if (id == null) {
				copyToStorage();
			}
		}

		@Override
		protected void setErrorMessage(ValidationException e) {
			if (page != null) {
				super.setErrorMessage(e);
			}
		}

		@Override
		protected void update() {
			if (page != null) {
				super.update();
			}
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
			if (source == remoteCombo) {
				handleRemoteServiceSelected(null);
				dataSource.storeAndValidate();
				updateControls();
			} else if (source == connectionCombo) {
				handleConnectionSelected(dataSource.getRemoteServicesId());
				dataSource.storeAndValidate();
				updateControls();
			} else if (source == localAddrCombo) {
				resetErrorMessages();
				dataSource.storeAndValidate();
			} else {
				assert false;
			}
		}

		@Override
		protected void doWidgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			if (source == newConnectionButton) {
				handleNewRemoteConnectionSelected();
			} else if (source == noneButton || source == portForwardingButton) {
				resetErrorMessages();
				dataSource.storeAndValidate();
			} else if (source == localAddrCombo) {
				resetErrorMessages();
				dataSource.storeAndValidate();
			} else if (useDefaultButton != null && source == useDefaultButton) {
				dataSource.storeAndValidate();
				updateControls();
			} else {
				assert false;
			}
		}
	}

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	protected AbstractConfigurationWizardPage page;

	protected IRemoteUIConnectionManager fUIConnectionManager = null;
	protected IRemoteServices[] fRemoteServices;
	protected IRunnableContext context;

	protected boolean fEnableUseDefault = false;
	protected String fUseDefaultMessage;
	protected Composite control;

	protected Button noneButton = null;
	protected Button portForwardingButton = null;
	protected Button useDefaultButton = null;
	protected Button newConnectionButton;
	protected Combo remoteCombo;
	protected Combo connectionCombo;
	protected Combo localAddrCombo;
	protected ExpandableComposite advancedOptions;

	protected WidgetListener listener;
	protected RMDataSource dataSource;

	protected ConnectionChoiceContainer(AbstractConfigurationWizardPage page) {
		this.page = page;
		getDataSource();
		getListener();
	}

	/**
	 * Create the contents of the wizard page.
	 * 
	 * @param parent
	 * @param colSpan
	 */
	public Composite createContents(Composite parent) {
		control = parent;
		ScrolledPageContent pageContent = new ScrolledPageContent(parent);
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		Composite composite = pageContent.getBody();
		composite.setLayout(layout);

		if (fEnableUseDefault) {
			useDefaultButton = new Button(composite, SWT.CHECK);
			useDefaultButton.setText(fUseDefaultMessage);
			useDefaultButton.addSelectionListener(listener);
		}

		/*
		 * Composite for remote information
		 */
		Composite remoteComp = new Composite(composite, SWT.NONE);
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
		remoteCombo.addModifyListener(listener);
		remoteCombo.setFocus();

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
		connectionCombo.addModifyListener(listener);

		newConnectionButton = SWTUtil.createPushButton(remoteComp, Messages.AbstractRemoteResourceManagerConfigurationWizardPage_2,
				null);
		newConnectionButton.addSelectionListener(listener);

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
		label.setText(Messages.AbstractRemoteResourceManagerConfigurationWizardPage_5);
		gd = new GridData();
		gd.horizontalSpan = 1;
		label.setLayoutData(gd);

		localAddrCombo = new Combo(addrComp, SWT.DROP_DOWN);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		localAddrCombo.setLayoutData(gd);
		localAddrCombo.addModifyListener(listener);
		localAddrCombo.addSelectionListener(listener);

		portForwardingButton = createRadioButton(mxGroup, Messages.AbstractRemoteResourceManagerConfigurationWizardPage_6,
				"mxGroup", listener); //$NON-NLS-1$
		portForwardingButton.addSelectionListener(listener);

		remoteCombo.setFocus();
		return pageContent;
	}

	public RMDataSource getDataSource() {
		if (dataSource == null) {
			dataSource = new RMDataSource();
		}
		return dataSource;
	}

	public WizardPageWidgetListener getListener() {
		if (listener == null) {
			listener = new WidgetListener();
		}
		return listener;
	}

	public void setEnableUseDefault(String message) {
		fEnableUseDefault = true;
		fUseDefaultMessage = message;
	}

	public void updateControls() {
		final boolean enabled = listener.isEnabled();
		listener.disable();
		final boolean portFwd = dataSource.getPortForward();
		final IRemoteServices services = getRemoteServices(dataSource.getRemoteServicesId());
		final IRemoteConnection connection = getRemoteConnection(services, dataSource.getConnectionName());
		boolean supportsPortForwarding = false;
		if (connection != null) {
			supportsPortForwarding = connection.supportsTCPPortForwarding();
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
		/*
		 * Set enabled state of controls
		 */
		if (remoteCombo != null) {
			remoteCombo.setEnabled(!dataSource.getUseDefault());
		}
		if (connectionCombo != null) {
			connectionCombo.setEnabled(!dataSource.getUseDefault());
		}
		if (newConnectionButton != null) {
			newConnectionButton.setEnabled(!dataSource.getUseDefault());
		}
		if (advancedOptions != null) {
			advancedOptions.setEnabled(!dataSource.getUseDefault());
			if (dataSource.getUseDefault()) {
				advancedOptions.setExpanded(false);
			}
		}
		listener.setEnabled(enabled);
	}

	protected Button createButton(Composite parent, String label, int type) {
		Button button = new Button(parent, type);
		button.setText(label);
		GridData data = new GridData();
		button.setLayoutData(data);
		return button;
	}

	protected GridLayout createGridLayout(int columns, boolean isEqual, int mh, int mw) {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = columns;
		gridLayout.makeColumnsEqualWidth = isEqual;
		gridLayout.marginHeight = mh;
		gridLayout.marginWidth = mw;
		return gridLayout;
	}

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

	protected String getFieldContent(String text) {
		if (text.trim().length() == 0 || text.equals(EMPTY_STRING)) {
			return null;
		}

		return text;
	}

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

	protected IRemoteConnection getRemoteConnection(IRemoteServices services, String name) {
		IRemoteConnectionManager manager = getRemoteConnectionManager(services);
		if (manager != null) {
			return manager.getConnection(name);
		}
		return null;
	}

	protected IRemoteConnectionManager getRemoteConnectionManager(IRemoteServices services) {
		if (services != null) {
			return services.getConnectionManager();
		}
		return null;
	}

	protected IRemoteServices getRemoteServices(String id) {
		if (id != null && !id.equals(EMPTY_STRING)) {
			return PTPRemoteUIPlugin.getDefault().getRemoteServices(id, context);
		}
		return null;
	}

	protected IRemoteUIConnectionManager getRemoteUIConnectionManager() {
		return fUIConnectionManager;
	}

	/**
	 * Handle the section of a new connection. Update connection option buttons
	 * appropriately.
	 */
	protected void handleConnectionSelected(String servicesId) {
		final boolean enabled = listener.isEnabled();
		listener.disable();
		int currentSelection = connectionCombo.getSelectionIndex();
		IRemoteServices services = getRemoteServices(servicesId);
		if (currentSelection >= 0 && services != null) {
			String connectionName = connectionCombo.getItem(currentSelection);
			final IRemoteConnection connection = getRemoteConnection(services, connectionName);
			if (connection != null) {
				boolean portFwd = connection.supportsTCPPortForwarding();
				portForwardingButton.setEnabled(portFwd);
				portForwardingButton.setSelection(portFwd);
			}
		}
		listener.setEnabled(enabled);
	}

	/**
	 * Handle creation of a new connection by pressing the 'New...' button.
	 * Calls handleRemoteServicesSelected() to update the connection combo with
	 * the new connection.
	 * 
	 * TODO should probably select the new connection
	 */
	protected void handleNewRemoteConnectionSelected() {
		if (fUIConnectionManager != null) {
			handleRemoteServiceSelected(fUIConnectionManager.newConnection(control.getShell()));
		}

		resetErrorMessages();
		dataSource.storeAndValidate();
		updateControls();
	}

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
	protected void handleRemoteServiceSelected(IRemoteConnection conn) {
		final boolean enabled = listener.isEnabled();
		listener.disable();
		int selectionIndex = remoteCombo.getSelectionIndex();
		if (fRemoteServices != null && fRemoteServices.length > 0 && selectionIndex >= 0) {
			IRemoteServices remoteServices = fRemoteServices[selectionIndex];
			dataSource.setRemoteServicesId(remoteServices.getId());
			IRemoteConnectionManager connectionManager = remoteServices.getConnectionManager();
			IRemoteUIServices remUIServices = PTPRemoteUIPlugin.getDefault().getRemoteUIServices(remoteServices);
			if (remUIServices != null) {
				fUIConnectionManager = remUIServices.getUIConnectionManager();
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

			/*
			 * Enable 'new' button if new connections are supported
			 */
			newConnectionButton.setEnabled(fUIConnectionManager != null);

			if (connections.length > 0) {
				connectionCombo.select(selected);
				/*
				 * Events are disabled so call selection handler here
				 */
				handleConnectionSelected(remoteServices.getId());
			}
		}

		listener.setEnabled(enabled);
	}

	/**
	 * Initialize the contents of the local address selection combo. Host names
	 * are obtained by performing a reverse lookup on the IP addresses of each
	 * network interface. If DNS is configured correctly, this should add the
	 * fully qualified domain name, otherwise it will probably be the IP
	 * address. We also add the configuration address to the combo in case it
	 * was specified manually.
	 */
	protected void initializeLocalHostCombo() {
		final boolean enabled = listener.isEnabled();
		listener.disable();
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
			if (addr.equals(dataSource.getLocalAddr())) {
				selection = index;
			}
			index++;
		}
		/*
		 * localAddr is not in the list, so add it and make it the current
		 * selection
		 */
		if (selection < 0) {
			if (!dataSource.getLocalAddr().equals("")) { //$NON-NLS-1$
				localAddrCombo.add(dataSource.getLocalAddr());
			}
			selection = localAddrCombo.getItemCount() - 1;
		}
		localAddrCombo.select(selection);
		listener.setEnabled(enabled);
	}

	/**
	 * Initialize the contents of the remote services combo. Keeps an array of
	 * remote services that matches the combo elements. Returns the id of the
	 * selected element.
	 */
	protected String initializeRemoteServicesCombo(String id) {
		final boolean enabled = listener.isEnabled();
		listener.disable();
		fRemoteServices = PTPRemoteUIPlugin.getDefault().getRemoteServices(context);
		IRemoteServices defServices;
		if (id != null) {
			defServices = getRemoteServices(id);
		} else {
			defServices = PTPRemoteCorePlugin.getDefault().getDefaultServices();
		}
		int defIndex = 0;
		Arrays.sort(fRemoteServices, new Comparator<IRemoteServices>() {
			public int compare(IRemoteServices c1, IRemoteServices c2) {
				return c1.getName().compareToIgnoreCase(c2.getName());
			}
		});
		remoteCombo.removeAll();
		for (int i = 0; i < fRemoteServices.length; i++) {
			remoteCombo.add(fRemoteServices[i].getName());
			if (fRemoteServices[i].equals(defServices)) {
				defIndex = i;
			}
		}
		if (fRemoteServices.length > 0) {
			remoteCombo.select(defIndex);
		}
		listener.setEnabled(enabled);
		return defServices.getId();
	}

	protected abstract void resetErrorMessages();

	protected void setRunnableContext(IRunnableContext context) {
		this.context = context;
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
}
