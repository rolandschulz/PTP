/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California and others.
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
 * Contributors:
 *    Albert L. Rossi (NCSA)  -- modified to disable proxy path for
 *    							 automatically deployed RMs
 *******************************************************************************/
package org.eclipse.ptp.rm.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteProxyOptions;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.ptp.remote.ui.IRemoteUIFileManager;
import org.eclipse.ptp.remote.ui.IRemoteUIServices;
import org.eclipse.ptp.remote.ui.PTPRemoteUIPlugin;
import org.eclipse.ptp.rm.core.rmsystem.IRemoteResourceManagerConfiguration;
import org.eclipse.ptp.rm.ui.RMUIPlugin;
import org.eclipse.ptp.rm.ui.messages.Messages;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.progress.UIJob;

/**
 * Abstract base class for wizard pages used to configure proxy resource
 * managers
 */
public abstract class AbstractRemoteProxyResourceManagerConfigurationWizardPage extends RMConfigurationWizardPage {

	/**
	 * Job to validate proxyPath. We use a job here so that the input is only
	 * validated after the user finishes (or pauses typing). This is to prevent
	 * a remote lookup on every key stroke.
	 * 
	 */
	protected class ValidateJob extends UIJob {
		public ValidateJob() {
			super("ValidateJob"); //$NON-NLS-1$
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			validateProxyPath();
			updatePage();
			return Status.OK_STATUS;
		}
	}

	protected class WidgetListener extends SelectionAdapter implements ModifyListener, IPropertyChangeListener {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.
		 * events.ModifyEvent)
		 */
		public void modifyText(ModifyEvent evt) {
			Object source = evt.getSource();
			if (!loading && (source == proxyPathText)) {
				/*
				 * Reschedule the validate job to run in VALIDATE_TIMER ms every
				 * time the user hits a key.
				 */
				validateJob.cancel();
				validateJob.schedule(VALIDATE_TIMER);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org
		 * .eclipse.jface.util.PropertyChangeEvent)
		 */
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(FieldEditor.IS_VALID))
				updatePage();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse
		 * .swt.events.SelectionEvent)
		 */
		@Override
		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			if (source == browseButton)
				handlePathBrowseButtonSelected();
			else if (source == optionsButton) {
				proxyArgs = createOptionsDialog(getShell(), proxyArgs);
				updatePage();
			} else {
				updateSettings();
				updatePage();
			}
		}
	}

	public static final String EMPTY_STRING = ""; //$NON-NLS-1$
	public static final int VALIDATE_TIMER = 250;
	/**
	 * @since 1.1
	 */
	protected IRemoteResourceManagerConfiguration config;
	/**
	 * @since 1.1
	 */
	protected String proxyPath = EMPTY_STRING;
	/**
	 * @since 1.1
	 */
	protected String proxyArgs = EMPTY_STRING;
	/**
	 * @since 1.1
	 */
	protected String localAddr = EMPTY_STRING;
	/**
	 * @since 1.1
	 */
	protected IRemoteServices remoteServices = null;
	/**
	 * @since 1.1
	 */
	protected IRemoteConnectionManager connectionManager = null;
	/**
	 * @since 1.1
	 */
	protected IRemoteUIConnectionManager uiConnectionManager = null;
	/**
	 * @since 1.1
	 */
	protected IRemoteConnection connection = null;
	/**
	 * @since 1.1
	 */
	protected boolean loading = true;
	/**
	 * @since 1.1
	 */
	protected boolean isValid;
	/**
	 * @since 1.1
	 */
	protected boolean muxPortFwd = false;
	/**
	 * @since 1.1
	 */
	protected boolean portFwdSupported = true;
	/**
	 * @since 1.1
	 */
	protected boolean proxyPathIsValid = true;
	/**
	 * @since 1.1
	 */
	protected boolean manualLaunch = false;

	/**
	 * @since 1.1
	 */
	protected final Job validateJob = new ValidateJob();
	/**
	 * @since 1.1
	 */
	protected final WidgetListener listener = new WidgetListener();

	/**
	 * @since 1.1
	 */
	protected Text proxyPathText = null;
	/**
	 * @since 1.1
	 */
	protected Button optionsButton = null;
	/**
	 * @since 1.1
	 */
	protected Button browseButton = null;
	/**
	 * @since 1.1
	 */
	protected Button noneButton = null;
	/**
	 * @since 1.1
	 */
	protected Button portForwardingButton = null;
	/**
	 * @since 1.1
	 */
	protected Button manualButton = null;
	/**
	 * @since 1.1
	 */
	protected Button newConnectionButton;
	/**
	 * @since 1.1
	 */
	protected Combo remoteCombo;
	/**
	 * @since 1.1
	 */
	protected Combo connectionCombo;
	/**
	 * @since 1.1
	 */
	protected Combo localAddrCombo;

	/**
	 * @since 1.1
	 */
	protected boolean proxyPathEnabled = true;
	/**
	 * @since 1.1
	 */
	protected boolean proxyOptionsEnabled = true;

	public AbstractRemoteProxyResourceManagerConfigurationWizardPage(IRMConfigurationWizard wizard, String title) {
		super(wizard, title);
		setPageComplete(false);
		isValid = false;
	}

	/**
	 * Attempt to open a connection.
	 */
	private void checkConnection() {
		if (!connection.isOpen()) {
			IRunnableWithProgress op = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						connection.open(monitor);
						if (monitor.isCanceled())
							throw new InterruptedException(Messages.AbstractRemoteProxyResourceManagerConfigurationWizardPage_0);
					} catch (RemoteConnectionException e) {
						throw new InvocationTargetException(e);
					}
				}

			};
			try {
				new ProgressMonitorDialog(getShell()).run(true, true, op);
			} catch (InvocationTargetException e) {
				ErrorDialog.openError(getShell(), Messages.AbstractRemoteProxyResourceManagerConfigurationWizardPage_1,
						Messages.AbstractRemoteProxyResourceManagerConfigurationWizardPage_2, new Status(IStatus.ERROR,
								RMUIPlugin.PLUGIN_ID, e.getCause().getMessage()));
			} catch (InterruptedException e) {
				ErrorDialog.openError(getShell(), Messages.AbstractRemoteProxyResourceManagerConfigurationWizardPage_3,
						Messages.AbstractRemoteProxyResourceManagerConfigurationWizardPage_2, new Status(IStatus.ERROR,
								RMUIPlugin.PLUGIN_ID, e.getMessage()));
			}
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
		label.setText(Messages.AbstractRemoteProxyResourceManagerConfigurationWizardPage_4);
		gd = new GridData();
		gd.horizontalSpan = 1;
		label.setLayoutData(gd);

		remoteCombo = new Combo(remoteComp, SWT.DROP_DOWN | SWT.READ_ONLY);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		remoteCombo.setLayoutData(gd);

		/*
		 * Proxy location
		 */
		label = new Label(remoteComp, SWT.NONE);
		label.setText(Messages.AbstractRemoteProxyResourceManagerConfigurationWizardPage_5);
		gd = new GridData();
		gd.horizontalSpan = 1;
		label.setLayoutData(gd);

		connectionCombo = new Combo(remoteComp, SWT.DROP_DOWN | SWT.READ_ONLY);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		connectionCombo.setLayoutData(gd);

		newConnectionButton = SWTUtil.createPushButton(remoteComp,
				Messages.AbstractRemoteProxyResourceManagerConfigurationWizardPage_6, null);

		if (proxyPathEnabled) {
			/*
			 * Proxy path
			 */
			label = new Label(remoteComp, SWT.NONE);
			label.setText(Messages.AbstractRemoteProxyResourceManagerConfigurationWizardPage_7);
			gd = new GridData();
			gd.horizontalSpan = 1;
			label.setLayoutData(gd);

			proxyPathText = new Text(remoteComp, SWT.SINGLE | SWT.BORDER);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 1;
			gd.grabExcessHorizontalSpace = true;
			gd.widthHint = 100;
			proxyPathText.setLayoutData(gd);
			proxyPathText.addModifyListener(listener);

			browseButton = SWTUtil.createPushButton(remoteComp,
					Messages.AbstractRemoteProxyResourceManagerConfigurationWizardPage_8, null);
			browseButton.addSelectionListener(listener);

		}

		if (proxyOptionsEnabled) {
			/*
			 * Proxy options
			 */
			optionsButton = SWTUtil.createPushButton(remoteComp,
					Messages.AbstractRemoteProxyResourceManagerConfigurationWizardPage_9, null);
			optionsButton.addSelectionListener(listener);

		}

		/*
		 * Multiplexing options
		 */
		Group mxGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		mxGroup.setLayout(createGridLayout(1, true, 10, 10));
		mxGroup.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));
		mxGroup.setText(Messages.AbstractRemoteProxyResourceManagerConfigurationWizardPage_10);

		noneButton = createRadioButton(mxGroup, Messages.AbstractRemoteProxyResourceManagerConfigurationWizardPage_11,
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
		label.setText(Messages.AbstractRemoteProxyResourceManagerConfigurationWizardPage_12);
		gd = new GridData();
		gd.horizontalSpan = 1;
		label.setLayoutData(gd);

		localAddrCombo = new Combo(addrComp, SWT.DROP_DOWN);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		localAddrCombo.setLayoutData(gd);

		portForwardingButton = createRadioButton(mxGroup, Messages.AbstractRemoteProxyResourceManagerConfigurationWizardPage_13,
				"mxGroup", listener); //$NON-NLS-1$
		portForwardingButton.addSelectionListener(listener);

		/*
		 * Manual launch
		 */
		manualButton = createCheckButton(parent, "Launch server manually"); //$NON-NLS-1$
		manualButton.addSelectionListener(listener);

		registerListeners();
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
		composite.setLayout(topLayout);
		createContents(composite);
		setControl(composite);
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

	/**
	 * Creates the dialog when the proxy "Options..." button is selected.
	 * Override if you want to provide your own dialog.
	 * 
	 * @param parent
	 *            the parent composite to contain the dialog area
	 * @return the proxy options string
	 */
	protected String createOptionsDialog(Shell shell, String initialOptions) {
		InputDialog dialog = new InputDialog(shell, Messages.AbstractRemoteProxyResourceManagerConfigurationWizardPage_14,
				Messages.AbstractRemoteProxyResourceManagerConfigurationWizardPage_15, initialOptions, null);
		if (dialog.open() == Dialog.OK)
			return dialog.getValue();
		return initialOptions;
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
		if (null != listener)
			button.addSelectionListener(listener);
		return button;
	}

	/**
	 * Transfer current settings to text fields
	 */
	protected void defaultSetting() {
		if (proxyPathEnabled) {
			proxyPathText.setText(proxyPath);
		}
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
			if (hostname.endsWith(".in-addr.arpa")) //$NON-NLS-1$
				return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			// should not happen
		}
		return hostname;
	}

	/**
	 * Clean up the content of a text field.
	 * 
	 * @param text
	 * @return cleaned up text.
	 */
	protected String getFieldContent(String text) {
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
		 * Disable port forwarding button if it's not supported. If port
		 * forwarding was selected, switch to 'none' instead.
		 */
		if (connection != null)
			portFwdSupported = connection.supportsTCPPortForwarding();
		/*
		 * Linux doesn't call modify handler (which calls updateSettings &
		 * updatePage) so need to call them explicitly here
		 */
		updateSettings();
		updatePage();
	}

	/**
	 * Handle creation of a new connection by pressing the 'New...' button.
	 * Calls handleRemoteServicesSelected() to update the connection combo with
	 * the new connection.
	 * 
	 * TODO should probably select the new connection
	 */
	protected void handleNewRemoteConnectionSelected() {
		if (uiConnectionManager != null)
			handleRemoteServiceSelected(uiConnectionManager.newConnection(getShell()));
	}

	/**
	 * Show a dialog that lets the user select a file.
	 */
	protected void handlePathBrowseButtonSelected() {
		if (!proxyPathEnabled) {
			return;
		}
		if (connection != null) {
			checkConnection();
			if (connection.isOpen()) {
				IRemoteUIServices remoteUIServices = PTPRemoteUIPlugin.getDefault().getRemoteUIServices(remoteServices);
				if (remoteUIServices != null) {
					IRemoteUIFileManager fileMgr = remoteUIServices.getUIFileManager();
					if (fileMgr != null) {
						fileMgr.setConnection(connection);
						String correctPath = proxyPathText.getText();
						String selectedPath = fileMgr.browseFile(getShell(),
								Messages.AbstractRemoteProxyResourceManagerConfigurationWizardPage_16, correctPath, 0);
						if (selectedPath != null)
							proxyPathText.setText(selectedPath.toString());
					}
				}
			}
		}
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
		IRemoteServices[] allRemoteServices = PTPRemoteCorePlugin.getDefault().getAllRemoteServices();
		int selectionIndex = remoteCombo.getSelectionIndex();
		if (allRemoteServices != null && allRemoteServices.length > 0 && selectionIndex >= 0) {
			remoteServices = allRemoteServices[selectionIndex];
			connectionManager = remoteServices.getConnectionManager();
			IRemoteUIServices remUIServices = PTPRemoteUIPlugin.getDefault().getRemoteUIServices(remoteServices);
			if (remUIServices != null)
				uiConnectionManager = remUIServices.getUIConnectionManager();
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
				if (conn != null && connections[i].equals(conn))
					selected = i;
			}
			if (connections.length > 0) {
				connectionCombo.select(selected);
				/*
				 * Linux doesn't call selection handler so need to call it
				 * explicitly here
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
	 * Initialize the contents of the controls on the page. This is called after
	 * the controls have been created.
	 */
	private void initContents() {
		loading = true;
		config = (IRemoteResourceManagerConfiguration) getConfigurationWizard().getConfiguration();
		loadSaved();
		updateSettings();
		defaultSetting();
		initializeRemoteServicesCombo();
		initializeLocalHostCombo();
		loading = false;
		updatePage();
	}

	/**
	 * Initialize the contents of the local address selection combo. Host names
	 * are obtained by performing a reverse lookup on the IP addresses of each
	 * network interface. If DNS is configured correctly, this should add the
	 * fully qualified domain name, otherwise it will probably be the IP
	 * address. We also add the configuration address to the combo in case it
	 * was specified manually.
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
					if (ip instanceof Inet4Address)
						addrs.add(fixHostName(ip.getCanonicalHostName()));
				}
			}
		} catch (Exception e) {
			// at least we'll still get localhost
		}
		if (addrs.size() == 0)
			addrs.add("localhost"); //$NON-NLS-1$
		localAddrCombo.removeAll();
		int index = 0;
		int selection = -1;
		for (String addr : addrs) {
			localAddrCombo.add(addr);
			if ((localAddr.equals("") && addr.equals("localhost")) //$NON-NLS-1$ //$NON-NLS-2$
					|| addr.equals(localAddr))
				selection = index;
			index++;
		}
		/*
		 * localAddr is not in the list, so add it and make it the current
		 * selection
		 */
		if (selection < 0) {
			if (!localAddr.equals("")) //$NON-NLS-1$
				localAddrCombo.add(localAddr);
			selection = localAddrCombo.getItemCount() - 1;
		}
		localAddrCombo.select(selection);
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
		if (remoteServices != null)
			defServices = remoteServices;
		else
			defServices = PTPRemoteCorePlugin.getDefault().getDefaultServices();
		int defIndex = 0;
		Arrays.sort(allServices, new Comparator<IRemoteServices>() {
			public int compare(IRemoteServices c1, IRemoteServices c2) {
				return c1.getName().compareToIgnoreCase(c2.getName());
			}
		});
		remoteCombo.removeAll();
		for (int i = 0; i < allServices.length; i++) {
			remoteCombo.add(allServices[i].getName());
			if (allServices[i].equals(defServices))
				defIndex = i;
		}
		if (allServices.length > 0) {
			remoteCombo.select(defIndex);
			/*
			 * Linux doesn't call selection handler so need to call it
			 * explicitly here
			 */
			handleRemoteServiceSelected(connection);
			handleConnectionSelected();
		}
	}

	/**
	 * @return
	 */
	protected boolean isValidSetting() {
		if (proxyPathEnabled && proxyPathText != null) {
			String name = getFieldContent(proxyPathText.getText());
			if (name == null || !proxyPathIsValid) {
				setErrorMessage(Messages.AbstractRemoteProxyResourceManagerConfigurationWizardPage_17);
				return false;
			}
		}

		return true;
	}

	/**
	 * Load the initial wizard state from the configuration settings.
	 */
	private void loadSaved() {
		proxyPath = config.getProxyServerPath();
		proxyArgs = config.getInvocationOptionsStr();
		localAddr = config.getLocalAddress();

		String rmID = config.getRemoteServicesId();
		if (rmID != null) {
			remoteServices = PTPRemoteCorePlugin.getDefault().getRemoteServices(rmID);
			String conn = config.getConnectionName();
			if (remoteServices != null && conn != null)
				connection = remoteServices.getConnectionManager().getConnection(conn);
		}

		int options = config.getOptions();

		muxPortFwd = (options & IRemoteProxyOptions.PORT_FORWARDING) == IRemoteProxyOptions.PORT_FORWARDING;
		manualLaunch = (options & IRemoteProxyOptions.MANUAL_LAUNCH) == IRemoteProxyOptions.MANUAL_LAUNCH;
	}

	/**
	 * Save the current state in the RM configuration. This is called whenever
	 * anything is changed.
	 * 
	 * @return
	 */
	public boolean performOk() {
		store();
		int options = 0;
		if (muxPortFwd)
			options |= IRemoteProxyOptions.PORT_FORWARDING;
		if (manualLaunch)
			options |= IRemoteProxyOptions.MANUAL_LAUNCH;
		if (remoteServices != null)
			config.setRemoteServicesId(remoteServices.getId());
		if (connection != null)
			config.setConnectionName(connection.getName());
		config.setLocalAddress(localAddr);
		config.setProxyServerPath(proxyPath);
		config.setInvocationOptions(proxyArgs);
		config.setOptions(options);
		return true;
	}

	/**
	 * @since 1.1
	 */
	private void registerListeners() {
		remoteCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				/*
				 * If we're loading saved settings, then we want to select the
				 * saved connection after the remote services are selected.
				 * Otherwise just pick the default item.
				 */
				if (loading)
					handleRemoteServiceSelected(connection);
				else
					handleRemoteServiceSelected(null);
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
			@Override
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
	 * @param complete
	 *            true if complete
	 */
	private void setValid(boolean complete) {
		isValid = complete;
		setPageComplete(isValid);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean visible) {
		if (visible)
			initContents();
		super.setVisible(visible);
	}

	/**
	 * @param style
	 * @param space
	 * @return
	 */
	protected GridData spanGridData(int style, int space) {
		GridData gd = null;
		if (style == -1)
			gd = new GridData();
		else
			gd = new GridData(style);
		gd.horizontalSpan = space;
		return gd;
	}

	/**
	 * Store text fields
	 */
	private void store() {
		if (proxyPathEnabled && proxyPathText != null) {
			proxyPath = proxyPathText.getText();
		}
	}

	/**
	 * Call to update page status and store any changed settings
	 */
	protected void updatePage() {
		if (!loading) {
			setErrorMessage(null);
			setMessage(null);

			if (!isValidSetting())
				setValid(false);
			else {
				performOk();
				setValid(true);
			}
		}
	}

	/**
	 * Update wizard UI selections from settings. This should be called whenever
	 * any settings are changed.
	 * 
	 * @since 1.1
	 */
	protected void updateSettings() {
		/*
		 * Get current settings unless we're initializing things
		 */
		if (!loading) {
			muxPortFwd = portForwardingButton.getSelection();
			manualLaunch = manualButton.getSelection();
		}

		/*
		 * If no localAddr has been specified in the configuration, select a
		 * default one.
		 */
		if (!loading || localAddr.equals("")) //$NON-NLS-1$
			localAddr = localAddrCombo.getText();

		/*
		 * Fix settings
		 */
		if (muxPortFwd && !portFwdSupported)
			muxPortFwd = false;

		if (muxPortFwd && manualLaunch)
			manualLaunch = false;

		/*
		 * Update UI to display correct settings
		 */
		if (noneButton != null)
			noneButton.setSelection(!muxPortFwd);

		if (portForwardingButton != null) {
			portForwardingButton.setSelection(muxPortFwd);
			portForwardingButton.setEnabled(portFwdSupported);
		}

		if (localAddrCombo != null)
			localAddrCombo.setEnabled(!muxPortFwd);

		if (manualButton != null) {
			manualButton.setSelection(manualLaunch && !muxPortFwd);
			manualButton.setEnabled(!muxPortFwd);
		}
	}

	/**
	 * Check if the proxy path supplied in proxyPathText is a valid file on the
	 * remote system.
	 * 
	 * @return true if valid
	 * @since 1.1
	 */
	protected boolean validateProxyPath() {
		if (!proxyPathEnabled) {
			return true;
		}
		proxyPathIsValid = false;
		final String path = proxyPathText.getText();
		if (path != null)
			if (connection != null) {
				checkConnection();
				if (connection.isOpen())
					if (remoteServices != null) {
						final IRemoteFileManager fileMgr = remoteServices.getFileManager(connection);
						if (fileMgr != null) {
							IRunnableWithProgress op = new IRunnableWithProgress() {
								public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
									try {
										IFileStore file = fileMgr.getResource(path);
										if (!monitor.isCanceled())
											proxyPathIsValid = file.fetchInfo(EFS.NONE, monitor).exists();
									} catch (CoreException e) {
										throw new InvocationTargetException(e);
									}
								}

							};
							try {
								ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
								dialog.setOpenOnRun(false);
								dialog.run(true, true, op);
							} catch (InvocationTargetException e) {
								// return false
							} catch (InterruptedException e) {
								// return false
							}
						}
					}
			}
		return proxyPathIsValid;
	}
}
