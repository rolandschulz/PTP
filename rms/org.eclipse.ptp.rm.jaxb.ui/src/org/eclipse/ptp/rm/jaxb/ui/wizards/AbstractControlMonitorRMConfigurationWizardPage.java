/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.ui.wizards;

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
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.ptp.remote.ui.IRemoteUIFileManager;
import org.eclipse.ptp.remote.ui.IRemoteUIServices;
import org.eclipse.ptp.remote.ui.PTPRemoteUIPlugin;
import org.eclipse.ptp.rm.core.rmsystem.IRemoteResourceManagerConfiguration;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetActionUtils;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetBuilderUtils;
import org.eclipse.ptp.rm.ui.RMUIPlugin;
import org.eclipse.ptp.ui.wizards.IRMConfigurationWizard;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizardPage;
import org.eclipse.ptp.utils.ui.swt.SWTUtil;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.progress.UIJob;

/**
 * Abstract base class for wizard pages used to configure target resource
 * managers
 */
public abstract class AbstractControlMonitorRMConfigurationWizardPage extends RMConfigurationWizardPage implements
		IJAXBUINonNLSConstants {

	/**
	 * Job to validate targetPath. We use a job here so that the input is only
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
			validateTargetPath();
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
			if (source == targetPathText) {
				targetPath = targetPathText.getText();
				if (!loading) {
					validateJob.cancel();
					validateJob.schedule();
				}
			} else {
				if (source == remoteCombo) {
					/*
					 * If we're loading saved settings, then we want to select
					 * the saved connection after the remote services are
					 * selected. Otherwise just pick the default item.
					 */
					if (loading) {
						handleRemoteServiceSelected(connection);
					} else {
						handleRemoteServiceSelected(null);
					}
					updateSettings();
				} else if (source == connectionCombo) {
					handleConnectionSelected();
				} else if (source == localAddrCombo) {
					updateSettings();
				}
				updatePage();
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
			if (event.getProperty().equals(FieldEditor.IS_VALID)) {
				updatePage();
			}
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
			if (source == browseButton) {
				handlePathBrowseButtonSelected();
			} else {
				if (source == optionsButton) {
					targetArgs = WidgetActionUtils.openInputDialog(getShell(),
							Messages.AbstractRemoteProxyResourceManagerConfigurationWizardPage_14,
							Messages.AbstractRemoteProxyResourceManagerConfigurationWizardPage_15, targetArgs);
					if (targetArgs == null) {
						targetArgs = ZEROSTR;
					}
				} else if (connectionSharingEnabled && source == shareConnectionButton) {
					updateSettings();
				} else if (source == newConnectionButton) {
					handleNewRemoteConnectionSelected();
				} else {
					updateSettings();
				}
				updatePage();
			}
		}
	}

	public static final int VALIDATE_TIMER = 250;

	protected IRemoteResourceManagerConfiguration config;
	protected String targetPath = ZEROSTR;
	protected String targetArgs = ZEROSTR;
	protected String localAddr = ZEROSTR;
	protected IRemoteServices remoteServices;
	protected IRemoteServices[] fAllRemoteServices;
	protected IRemoteConnectionManager connectionManager;
	protected IRemoteUIConnectionManager uiConnectionManager;
	protected IRemoteConnection connection;
	protected boolean loading = true;
	protected boolean isValid;
	protected boolean muxPortFwd = false;
	protected boolean portFwdSupported = true;
	protected boolean targetPathIsValid = true;
	protected boolean manualLaunch = false;

	protected final Job validateJob = new ValidateJob();
	protected final WidgetListener listener = new WidgetListener();
	protected Text targetPathText;
	protected Button optionsButton;
	protected Button browseButton;
	protected Button noneButton;
	protected Button portForwardingButton;
	protected Button manualButton = null;
	protected Button newConnectionButton;
	protected Button shareConnectionButton;
	protected Combo remoteCombo;
	protected Combo connectionCombo;
	protected Combo localAddrCombo;

	protected boolean targetOptionsEnabled = true;
	protected boolean multiplexingEnabled = true;
	protected boolean fManualLaunchEnabled = true;
	protected boolean connectionSharingEnabled = true;

	public AbstractControlMonitorRMConfigurationWizardPage(IRMConfigurationWizard wizard, String title) {
		super(wizard, title);
		setPageComplete(false);
		isValid = false;
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
		Composite composite = WidgetBuilderUtils.createComposite(parent, 1);
		createContents(composite);
		setControl(composite);
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
		if (localAddrCombo == null) {
			return;
		}
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
			// at least we'll still get localhost
		}
		if (addrs.size() == 0) {
			addrs.add(LOCALHOST);
		}
		localAddrCombo.removeAll();
		int index = 0;
		int selection = -1;
		for (String addr : addrs) {
			localAddrCombo.add(addr);
			if ((localAddr.equals(ZEROSTR) && addr.equals(LOCALHOST)) || addr.equals(localAddr)) {
				selection = index;
			}
			index++;
		}
		/*
		 * localAddr is not in the list, so add it and make it the current
		 * selection
		 */
		if (selection < 0) {
			if (!localAddr.equals(ZEROSTR)) {
				localAddrCombo.add(localAddr);
			}
			selection = localAddrCombo.getItemCount() - 1;
		}
		localAddrCombo.select(selection);
	}

	/**
	 * Save the current state in the RM configuration. This is called whenever
	 * anything is changed.
	 * 
	 * @return
	 */
	public boolean performOk() {
		if (remoteServices != null) {
			config.setRemoteServicesId(remoteServices.getId());
		}
		if (connection != null) {
			setConnectionName(connection.getName());
		} else {
			setConnectionName(null);
		}
		setConnectionOptions();
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			initContents();
			updatePage();
		}
		super.setVisible(visible);
	}

	protected abstract void configureInternal();

	/**
	 * Clean up the content of a text field.
	 * 
	 * @param text
	 * @return cleaned up text.
	 */
	protected String getFieldContent(String text) {
		if (text.trim().length() == 0 || text.equals(ZEROSTR)) {
			return null;
		}

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
		 * forwarding was selected, switch to 'forward' instead.
		 */
		if (connection != null) {
			portFwdSupported = connection.supportsTCPPortForwarding();
			portForwardingButton.setSelection(portFwdSupported);
		}

		/*
		 * Linux doesn't call modify handler (which calls updateSettings &
		 * updatePage) so need to call them explicitly here
		 */
		updateSettings();
		validateJob.cancel();
		validateJob.schedule();
	}

	/**
	 * Handle creation of a new connection by pressing the 'New...' button.
	 * Calls handleRemoteServicesSelected() to update the connection combo with
	 * the new connection.
	 */
	protected void handleNewRemoteConnectionSelected() {
		if (uiConnectionManager != null) {
			handleRemoteServiceSelected(uiConnectionManager.newConnection(getShell()));
		}
	}

	/**
	 * Show a dialog that lets the user select a file.
	 */
	protected void handlePathBrowseButtonSelected() {
		if (connection != null) {
			checkConnection();
			if (connection.isOpen()) {
				IRemoteUIServices remoteUIServices = PTPRemoteUIPlugin.getDefault().getRemoteUIServices(remoteServices);
				if (remoteUIServices != null) {
					IRemoteUIFileManager fileMgr = remoteUIServices.getUIFileManager();
					if (fileMgr != null) {
						fileMgr.setConnection(connection);
						String correctPath = targetPathText.getText();
						String selectedPath = fileMgr.browseFile(getShell(),
								Messages.AbstractRemoteProxyResourceManagerConfigurationWizardPage_16, correctPath, 0);
						if (selectedPath != null) {
							targetPathText.setText(selectedPath.toString());
						}
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
		int selectionIndex = remoteCombo.getSelectionIndex();
		if (fAllRemoteServices != null && fAllRemoteServices.length > 0 && selectionIndex >= 0) {
			remoteServices = fAllRemoteServices[selectionIndex];
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
	 * 
	 * @since 2.0
	 */
	protected void initContents() {
		loading = true;
		config = (IRemoteResourceManagerConfiguration) getConfiguration();
		configureInternal();
		loadSaved();
		updateSettings();
		initializeRemoteServicesCombo();
		initializeLocalHostCombo();
		validateJob.cancel();
		validateJob.schedule();
		loading = false;
	}

	/**
	 * Initialize the contents of the remote services combo.
	 * 
	 * The assumption is that this will trigger a call to the selection handling
	 * routine when the default index is selected.
	 */
	protected void initializeRemoteServicesCombo() {
		IWizardContainer container = null;
		if (getControl().isVisible()) {
			container = getWizard().getContainer();
		}
		fAllRemoteServices = PTPRemoteUIPlugin.getDefault().getRemoteServices(container);
		IRemoteServices defServices;
		if (remoteServices != null) {
			defServices = remoteServices;
		} else {
			defServices = PTPRemoteCorePlugin.getDefault().getDefaultServices();
		}
		int defIndex = 0;
		Arrays.sort(fAllRemoteServices, new Comparator<IRemoteServices>() {
			public int compare(IRemoteServices c1, IRemoteServices c2) {
				return c1.getName().compareToIgnoreCase(c2.getName());
			}
		});
		remoteCombo.removeAll();
		for (int i = 0; i < fAllRemoteServices.length; i++) {
			remoteCombo.add(fAllRemoteServices[i].getName());
			if (fAllRemoteServices[i].equals(defServices)) {
				defIndex = i;
			}
		}
		if (fAllRemoteServices.length > 0) {
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
		if (targetPathText != null) {
			String name = getFieldContent(targetPathText.getText());
			if (name == null || !targetPathIsValid) {
				setErrorMessage(Messages.AbstractRemoteProxyResourceManagerConfigurationWizardPage_17);
				return false;
			}
		}

		return true;
	}

	protected abstract void loadConnectionOptions();

	protected abstract void setConnectionName(String name);

	protected abstract void setConnectionOptions();

	/**
	 * Call to update page status and store any changed settings
	 */
	protected void updatePage() {
		if (!loading) {
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
			if (portForwardingButton != null) {
				muxPortFwd = portForwardingButton.getSelection();
			}
			if (manualButton != null) {
				manualLaunch = manualButton.getSelection();
			}
		}

		if (shareConnectionButton != null) {
			boolean selected = shareConnectionButton.getSelection();
			remoteCombo.setEnabled(!selected);
			connectionCombo.setEnabled(!selected);
			newConnectionButton.setEnabled(!selected);
		}

		/*
		 * If no localAddr has been specified in the configuration, select a
		 * default one.
		 */
		if (localAddrCombo != null) {
			if (!loading || localAddr.equals(ZEROSTR)) {
				localAddr = localAddrCombo.getText();
			}
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
		targetPathText.setText(targetPath);

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

		if (manualButton != null) {
			manualButton.setSelection(manualLaunch);
		}
	}

	/**
	 * Check if the target path supplied in targetPathText is a valid file on
	 * the remote system.
	 * 
	 * @return true if valid
	 * @since 1.1
	 */
	protected boolean validateTargetPath() {
		targetPathIsValid = false;
		final String path = targetPathText.getText();
		if (path != null) {
			if (connection != null) {
				checkConnection();
				if (connection.isOpen()) {
					if (remoteServices != null) {
						final IRemoteFileManager fileMgr = remoteServices.getFileManager(connection);
						if (fileMgr != null) {
							IRunnableWithProgress op = new IRunnableWithProgress() {
								public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
									try {
										IFileStore file = fileMgr.getResource(path);
										if (!monitor.isCanceled()) {
											targetPathIsValid = file.fetchInfo(EFS.NONE, monitor).exists();
										}
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
			}
		}
		return targetPathIsValid;
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
						if (monitor.isCanceled()) {
							throw new InterruptedException(Messages.AbstractRemoteProxyResourceManagerConfigurationWizardPage_0);
						}
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
	 * Create the contents of the wizard page.
	 * 
	 * @param parent
	 * @param colSpan
	 */
	private void createContents(Composite parent) {
		/*
		 * connection sharing
		 */
		if (connectionSharingEnabled) {
			shareConnectionButton = WidgetBuilderUtils.createCheckButton(parent,
					Messages.AbstractRemoteProxyResourceManagerConfigurationWizardPage_3b, listener);
		}

		/*
		 * group for connection information
		 */
		GridLayout layout = WidgetBuilderUtils.createGridLayout(4, false);
		GridData gd = WidgetBuilderUtils.createGridDataFillH(4);
		Group remoteComp = WidgetBuilderUtils.createGroup(parent, SWT.NONE, layout, gd);

		/*
		 * connection provider
		 */
		WidgetBuilderUtils.createLabel(remoteComp, Messages.AbstractRemoteProxyResourceManagerConfigurationWizardPage_4, SWT.NONE,
				1);
		gd = WidgetBuilderUtils.createGridDataFillH(3);
		remoteCombo = WidgetBuilderUtils.createCombo(remoteComp, SWT.DROP_DOWN | SWT.READ_ONLY, gd, listener);

		/*
		 * connection location
		 */
		WidgetBuilderUtils.createLabel(remoteComp, Messages.AbstractRemoteProxyResourceManagerConfigurationWizardPage_5, SWT.NONE,
				1);
		gd = WidgetBuilderUtils.createGridDataFillH(2);
		// int style, boolean grabH, boolean grabV, int wHint, int hHint, int
		// cols
		connectionCombo = WidgetBuilderUtils.createCombo(remoteComp, SWT.DROP_DOWN | SWT.READ_ONLY, gd, listener);
		newConnectionButton = SWTUtil.createPushButton(remoteComp,
				Messages.AbstractRemoteProxyResourceManagerConfigurationWizardPage_6, null);

		/*
		 * group for service executable information
		 */
		layout = WidgetBuilderUtils.createGridLayout(4, false);
		gd = WidgetBuilderUtils.createGridDataFillH(4);
		Group serviceComp = WidgetBuilderUtils.createGroup(parent, SWT.NONE, layout, gd);

		/*
		 * Service path
		 */
		WidgetBuilderUtils.createLabel(serviceComp, Messages.AbstractRemoteProxyResourceManagerConfigurationWizardPage_7, SWT.NONE,
				1);
		gd = WidgetBuilderUtils.createGridDataFillH(2);
		targetPathText = WidgetBuilderUtils.createText(serviceComp, SWT.SINGLE | SWT.BORDER, gd, false, null, listener, null);

		browseButton = WidgetBuilderUtils.createPushButton(serviceComp,
				Messages.AbstractRemoteProxyResourceManagerConfigurationWizardPage_8, listener);

		if (targetOptionsEnabled) {
			optionsButton = WidgetBuilderUtils.createPushButton(serviceComp,
					Messages.AbstractRemoteProxyResourceManagerConfigurationWizardPage_9, listener);
		}

		/*
		 * Multiplexing options
		 */
		if (multiplexingEnabled) {
			layout = WidgetBuilderUtils.createGridLayout(1, true);
			gd = WidgetBuilderUtils.createGridDataFillH(2);
			Group mxGroup = WidgetBuilderUtils.createGroup(parent, SWT.SHADOW_ETCHED_IN, layout, gd,
					Messages.AbstractRemoteProxyResourceManagerConfigurationWizardPage_10);
			noneButton = WidgetBuilderUtils.createRadioButton(mxGroup,
					Messages.AbstractRemoteProxyResourceManagerConfigurationWizardPage_11, Messages.MXGroupTitle, listener);

			/*
			 * Local address
			 */
			layout = WidgetBuilderUtils.createGridLayout(2, true);
			gd = WidgetBuilderUtils.createGridDataFillH(2);
			Composite addrComp = WidgetBuilderUtils.createComposite(mxGroup, SWT.NONE, layout, gd);
			WidgetBuilderUtils.createLabel(addrComp, Messages.AbstractRemoteProxyResourceManagerConfigurationWizardPage_12,
					SWT.NONE, 1);
			gd = WidgetBuilderUtils.createGridData(GridData.FILL_HORIZONTAL, true, false, DEFAULT, DEFAULT, 1);
			localAddrCombo = WidgetBuilderUtils.createCombo(addrComp, SWT.DROP_DOWN, gd, listener);
			portForwardingButton = WidgetBuilderUtils.createRadioButton(mxGroup,
					Messages.AbstractRemoteProxyResourceManagerConfigurationWizardPage_13, Messages.MXGroupTitle, listener);
		}

		/*
		 * Manual launch
		 */
		if (fManualLaunchEnabled) {
			manualButton = WidgetBuilderUtils.createCheckButton(parent, Messages.ManualLaunch, listener);
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
			if (hostname.endsWith(ARPA)) {
				return InetAddress.getLocalHost().getHostAddress();
			}
		} catch (UnknownHostException e) {
			// should not happen
		}
		return hostname;
	}

	/**
	 * Load the initial wizard state from the configuration settings.
	 */
	private void loadSaved() {
		String rmID = config.getRemoteServicesId();
		if (rmID != null) {
			IWizardContainer container = null;
			if (getControl().isVisible()) {
				container = getWizard().getContainer();
			}
			remoteServices = PTPRemoteUIPlugin.getDefault().getRemoteServices(rmID, container);
			String conn = config.getConnectionName();
			if (remoteServices != null && conn != null) {
				connection = remoteServices.getConnectionManager().getConnection(conn);
			}
		} else {
			remoteServices = null;
			connection = null;
		}
		loadConnectionOptions();
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
}
