/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.rm.ibm.ll.ui.wizards;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.ui.IRemoteUIFileManager;
import org.eclipse.ptp.remote.ui.IRemoteUIServices;
import org.eclipse.ptp.remote.ui.PTPRemoteUIPlugin;
import org.eclipse.ptp.rm.ibm.ll.core.IBMLLPreferenceConstants;
import org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration;
import org.eclipse.ptp.rm.ibm.ll.ui.LLUIPlugin;
import org.eclipse.ptp.rm.ibm.ll.ui.messages.Messages;
import org.eclipse.ptp.ui.wizards.IRMConfigurationWizard;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizardPage;
import org.eclipse.ptp.utils.ui.swt.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

public class IBMLLResourceManagerOptionWizardPage extends RMConfigurationWizardPage {
	private class EventMonitor implements SelectionListener, ModifyListener {
		public EventMonitor() {
			// System.err.println("wizard: EventMonitor entered");
			updateConfigOptions();
		}

		public void modifyText(ModifyEvent e) {
			// System.err.println("preferences: modifyText entered");
			// System.err.println("widget entered is "+ e.widget.toString());
			if (e.widget.equals(proxyPollingJob)) {
				// System.err.println("widget entered is proxyPollingJob");
			}
			if (e.widget.equals(proxyPollingNodeMin)) {
				// System.err.println("widget entered is proxyPollingNodeMin");
				int min_value = proxyPollingNodeMin.getSelection();
				int max_value = proxyPollingNodeMax.getSelection();
				if (min_value > max_value) {
					proxyPollingNodeMax.setSelection(min_value);
				}

			}
			if (e.widget.equals(proxyPollingNodeMax)) {
				// System.err.println("widget entered is proxyPollingNodeMax");
				int min_value = proxyPollingNodeMin.getSelection();
				int max_value = proxyPollingNodeMax.getSelection();
				if (max_value < min_value) {
					proxyPollingNodeMin.setSelection(max_value);
				}
			}
			updateConfigOptions();
			// TODO - check for valid file here - or else implement a browse
			// button to select and check somewhere

		}

		public void widgetDefaultSelected(SelectionEvent e) {
			// System.err.println("wizard: widgetDefaultSelected entered");
			updateConfigOptions();
		}

		public void widgetModified(SelectionEvent e) {
			// System.err.println("wizard: widgetModified entered");
			updateConfigOptions();
		}

		public void widgetSelected(SelectionEvent e) {
			updateConfigOptions();
		}
	}

	private Composite preferencePane;
	
	private Button proxyTraceMessageButton = null;
	private Button proxyInfoMessageButton = null;
	private Button proxyWarningMessageButton = null;
	private Button proxyErrorMessageButton = null;
	private Button proxyFatalMessageButton = null;
	private Button proxyArgsMessageButton = null;

	private Button proxyDebugLoopButton = null;
	private Button proxyForceLocalRadioButton = null;
	private Button proxyForceMulticlusterRadioButton = null;

	private Button proxyLLDefaultRadioButton = null;
	private Button proxyTemplateNeverRadioButton = null;
	private Button proxyTemplateAlwaysRadioButton = null;;

	private Button guiTraceMessageButton = null;
	private Button guiInfoMessageButton = null;
	private Button guiWarningMessageButton = null;
	private Button guiErrorMessageButton = null;
	private Button guiFatalMessageButton = null;

	private Button guiArgsMessageButton = null;
	private Spinner proxyPollingNodeMin = null;
	private Spinner proxyPollingNodeMax = null;

	private Spinner proxyPollingJob = null;

	private EventMonitor eventMonitor = null;

	private IIBMLLResourceManagerConfiguration config;

	public IBMLLResourceManagerOptionWizardPage(IRMConfigurationWizard wizard) {
		super(wizard, Messages.getString("Wizard.InvocationOptionsTitle")); //$NON-NLS-1$
		setTitle(Messages.getString("Wizard.InvocationOptionsTitle")); //$NON-NLS-1$
		setDescription(Messages.getString("Wizard.InvocationOptions")); //$NON-NLS-1$
	}

	/**
	 * Create the widgets for this proxy configuration page
	 * 
	 * @param parent
	 *            - The parent widget for this class
	 */
	@Override
	public void createControl(Composite parent) {

		Group proxyLibraryGroup = null;
		Group proxyOptionsGroup = null;
		Group proxyDebugGroup = null;
		Group guiOptionsGroup = null;
		Group proxyMulticlusterGroup = null;
		Group proxyTemplateGroup = null;
		Group proxyTemplateOptionsGroup = null;
		Group proxyPollingGroup = null;
		
		config = (IIBMLLResourceManagerConfiguration) getConfiguration();
		String preferenceValue;

		eventMonitor = new EventMonitor();

		GridLayout layout;

		preferencePane = new Composite(parent, SWT.NONE);
		layout = new GridLayout(2, true);
		preferencePane.setLayout(layout);


		// *********************************************************************
		// Check box group for proxy messages
		// *********************************************************************
		proxyOptionsGroup = new Group(preferencePane, SWT.NONE);
		proxyOptionsGroup.setLayout(new RowLayout());
		proxyOptionsGroup.setText(Messages.getString("IBMLLPrefWizPage.proxyOptionsGroupLabel")); //$NON-NLS-1$

		proxyTraceMessageButton = new Button(proxyOptionsGroup, SWT.CHECK);
		proxyTraceMessageButton.setText(Messages.getString("IBMLLPrefWizPage.ProxyTraceMessageLabel")); //$NON-NLS-1$
		proxyTraceMessageButton.setToolTipText(Messages.getString("IBMLLPrefWizPage.ProxyTraceMessageToolTip")); //$NON-NLS-1$
		preferenceValue = config.getTraceOption();
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			proxyTraceMessageButton.setSelection(true);
		}
		proxyTraceMessageButton.addSelectionListener(eventMonitor);

		proxyInfoMessageButton = new Button(proxyOptionsGroup, SWT.CHECK);
		proxyInfoMessageButton.setText(Messages.getString("IBMLLPrefWizPage.ProxyInfoMessageLabel")); //$NON-NLS-1$
		proxyInfoMessageButton.setToolTipText(Messages.getString("IBMLLPrefWizPage.ProxyInfoMessageToolTip")); //$NON-NLS-1$
		preferenceValue = config.getInfoMessage();
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			proxyInfoMessageButton.setSelection(true);
		}
		proxyInfoMessageButton.addSelectionListener(eventMonitor);

		proxyWarningMessageButton = new Button(proxyOptionsGroup, SWT.CHECK);
		proxyWarningMessageButton.setText(Messages.getString("IBMLLPrefWizPage.ProxyWarningMessageLabel")); //$NON-NLS-1$
		proxyWarningMessageButton.setToolTipText(Messages.getString("IBMLLPrefWizPage.ProxyWarningMessageToolTip")); //$NON-NLS-1$
		preferenceValue = config.getWarningMessage();
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			proxyWarningMessageButton.setSelection(true);
		}
		proxyWarningMessageButton.addSelectionListener(eventMonitor);

		proxyErrorMessageButton = new Button(proxyOptionsGroup, SWT.CHECK);
		proxyErrorMessageButton.setText(Messages.getString("IBMLLPrefWizPage.ProxyErrorMessageLabel")); //$NON-NLS-1$
		proxyErrorMessageButton.setToolTipText(Messages.getString("IBMLLPrefWizPage.ProxyErrorMessageToolTip")); //$NON-NLS-1$
		preferenceValue = config.getErrorMessage();
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			proxyErrorMessageButton.setSelection(true);
		}
		proxyErrorMessageButton.addSelectionListener(eventMonitor);

		proxyFatalMessageButton = new Button(proxyOptionsGroup, SWT.CHECK);
		proxyFatalMessageButton.setText(Messages.getString("IBMLLPrefWizPage.ProxyFatalMessageLabel")); //$NON-NLS-1$
		proxyFatalMessageButton.setToolTipText(Messages.getString("IBMLLPrefWizPage.ProxyFatalMessageToolTip")); //$NON-NLS-1$
		preferenceValue = config.getFatalMessage();
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			proxyFatalMessageButton.setSelection(true);
		}
		proxyFatalMessageButton.addSelectionListener(eventMonitor);

		proxyArgsMessageButton = new Button(proxyOptionsGroup, SWT.CHECK);
		proxyArgsMessageButton.setText(Messages.getString("IBMLLPrefWizPage.ProxyArgsMessageLabel")); //$NON-NLS-1$
		proxyArgsMessageButton.setToolTipText(Messages.getString("IBMLLPrefWizPage.ProxyArgsMessageToolTip")); //$NON-NLS-1$
		preferenceValue = config.getArgsMessage();
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			proxyArgsMessageButton.setSelection(true);
		}
		proxyArgsMessageButton.addSelectionListener(eventMonitor);

		// *********************************************************************
		// Check box group for proxy debug
		// *********************************************************************
		proxyDebugGroup = new Group(preferencePane, SWT.NONE);
		proxyDebugGroup.setLayout(new RowLayout());
		proxyDebugGroup.setText(Messages.getString("IBMLLPrefWizPage.proxyDebugGroupLabel")); //$NON-NLS-1$

		proxyDebugLoopButton = new Button(proxyDebugGroup, SWT.CHECK);
		proxyDebugLoopButton.setText(Messages.getString("IBMLLPrefWizPage.ProxyDebugLoopLabel")); //$NON-NLS-1$
		proxyDebugLoopButton.setToolTipText(Messages.getString("IBMLLPrefWizPage.ProxyDebugLoopToolTip")); //$NON-NLS-1$
		preferenceValue = config.getDebugLoop();
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			proxyDebugLoopButton.setSelection(true);
		}
		proxyDebugLoopButton.addSelectionListener(eventMonitor);

		// *********************************************************************
		// Check box group for gui messages
		// *********************************************************************
		guiOptionsGroup = new Group(preferencePane, SWT.NONE);
		guiOptionsGroup.setLayout(new RowLayout());
		guiOptionsGroup.setText(Messages.getString("IBMLLPrefWizPage.guiOptionsGroupLabel")); //$NON-NLS-1$

		guiTraceMessageButton = new Button(guiOptionsGroup, SWT.CHECK);
		guiTraceMessageButton.setText(Messages.getString("IBMLLPrefWizPage.GuiTraceMessageLabel")); //$NON-NLS-1$
		guiTraceMessageButton.setToolTipText(Messages.getString("IBMLLPrefWizPage.GuiTraceMessageToolTip")); //$NON-NLS-1$
		preferenceValue = config.getGuiTraceMessage();
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			guiTraceMessageButton.setSelection(true);
		}
		guiTraceMessageButton.addSelectionListener(eventMonitor);

		guiInfoMessageButton = new Button(guiOptionsGroup, SWT.CHECK);
		guiInfoMessageButton.setText(Messages.getString("IBMLLPrefWizPage.GuiInfoMessageLabel")); //$NON-NLS-1$
		guiInfoMessageButton.setToolTipText(Messages.getString("IBMLLPrefWizPage.GuiInfoMessageToolTip")); //$NON-NLS-1$
		preferenceValue = config.getGuiInfoMessage();
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			guiInfoMessageButton.setSelection(true);
		}
		guiInfoMessageButton.addSelectionListener(eventMonitor);

		guiWarningMessageButton = new Button(guiOptionsGroup, SWT.CHECK);
		guiWarningMessageButton.setText(Messages.getString("IBMLLPrefWizPage.GuiWarningMessageLabel")); //$NON-NLS-1$
		guiWarningMessageButton.setToolTipText(Messages.getString("IBMLLPrefWizPage.GuiWarningMessageToolTip")); //$NON-NLS-1$
		preferenceValue = config.getGuiWarningMessage();
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			guiWarningMessageButton.setSelection(true);
		}
		guiWarningMessageButton.addSelectionListener(eventMonitor);

		guiErrorMessageButton = new Button(guiOptionsGroup, SWT.CHECK);
		guiErrorMessageButton.setText(Messages.getString("IBMLLPrefWizPage.GuiErrorMessageLabel")); //$NON-NLS-1$
		guiErrorMessageButton.setToolTipText(Messages.getString("IBMLLPrefWizPage.GuiErrorMessageToolTip")); //$NON-NLS-1$
		preferenceValue = config.getGuiErrorMessage();
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			guiErrorMessageButton.setSelection(true);
		}
		guiErrorMessageButton.addSelectionListener(eventMonitor);

		guiFatalMessageButton = new Button(guiOptionsGroup, SWT.CHECK);
		guiFatalMessageButton.setText(Messages.getString("IBMLLPrefWizPage.GuiFatalMessageLabel")); //$NON-NLS-1$
		guiFatalMessageButton.setToolTipText(Messages.getString("IBMLLPrefWizPage.GuiFatalMessageToolTip")); //$NON-NLS-1$
		preferenceValue = config.getGuiFatalMessage();
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			guiFatalMessageButton.setSelection(true);
		}
		guiFatalMessageButton.addSelectionListener(eventMonitor);

		guiArgsMessageButton = new Button(guiOptionsGroup, SWT.CHECK);
		guiArgsMessageButton.setText(Messages.getString("IBMLLPrefWizPage.GuiArgsMessageLabel")); //$NON-NLS-1$
		guiArgsMessageButton.setToolTipText(Messages.getString("IBMLLPrefWizPage.GuiArgsMessageToolTip")); //$NON-NLS-1$
		preferenceValue = config.getGuiArgsMessage();
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			guiArgsMessageButton.setSelection(true);
		}
		guiArgsMessageButton.addSelectionListener(eventMonitor);

		// *********************************************************************
		// Radio box group for multicluster
		// *********************************************************************
		proxyMulticlusterGroup = new Group(preferencePane, SWT.NONE);
		proxyMulticlusterGroup.setLayout(new RowLayout());
		proxyMulticlusterGroup.setText(Messages.getString("IBMLLPrefWizPage.proxyMulticlusterGroupLabel")); //$NON-NLS-1$

		proxyLLDefaultRadioButton = new Button(proxyMulticlusterGroup, SWT.RADIO);
		proxyLLDefaultRadioButton.setText(Messages.getString("IBMLLPrefWizPage.proxyMulticlusterDefaultLabel")); //$NON-NLS-1$
		preferenceValue = config.getDefaultMulticluster();
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			proxyLLDefaultRadioButton.setSelection(true);
		}
		proxyLLDefaultRadioButton.setData(new Integer(SWT.IMAGE_BMP));
		proxyLLDefaultRadioButton.addSelectionListener(eventMonitor);
		proxyLLDefaultRadioButton.setToolTipText(Messages.getString("IBMLLPrefWizPage.proxyMulticlusterDefaultToolTip")); //$NON-NLS-1$

		proxyForceLocalRadioButton = new Button(proxyMulticlusterGroup, SWT.RADIO);
		proxyForceLocalRadioButton.setText(Messages.getString("IBMLLPrefWizPage.proxyMulticlusterForceLocalLabel")); //$NON-NLS-1$
		preferenceValue = config.getForceProxyLocal();
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			proxyForceLocalRadioButton.setSelection(true);
		}
		proxyForceLocalRadioButton.setData(new Integer(SWT.IMAGE_BMP));
		proxyForceLocalRadioButton.addSelectionListener(eventMonitor);
		proxyForceLocalRadioButton.setToolTipText(Messages.getString("IBMLLPrefWizPage.proxyMulticlusterForceLocalToolTip")); //$NON-NLS-1$

		proxyForceMulticlusterRadioButton = new Button(proxyMulticlusterGroup, SWT.RADIO);
		proxyForceMulticlusterRadioButton.setText(Messages.getString("IBMLLPrefWizPage.proxyMulticlusterForceMulticlusterLabel")); //$NON-NLS-1$
		preferenceValue = config.getForceProxyMulticluster();
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			proxyForceMulticlusterRadioButton.setSelection(true);
		}
		proxyForceMulticlusterRadioButton.setData(new Integer(SWT.IMAGE_BMP));
		proxyForceMulticlusterRadioButton.addSelectionListener(eventMonitor);
		proxyForceMulticlusterRadioButton.setToolTipText(Messages
				.getString("IBMLLPrefWizPage.proxyMulticlusterForceMulticlusterToolTip")); //$NON-NLS-1$


		// *********************************************************************
		// Template options group
		// *********************************************************************
		proxyTemplateOptionsGroup = new Group(preferencePane, SWT.NONE);
		proxyTemplateOptionsGroup.setLayout(new RowLayout());
		proxyTemplateOptionsGroup.setText(Messages.getString("IBMLLPrefWizPage.proxyTemplateOptionsGroupLabel")); //$NON-NLS-1$

		proxyTemplateNeverRadioButton = new Button(proxyTemplateOptionsGroup, SWT.RADIO);
		proxyTemplateNeverRadioButton.setText(Messages.getString("IBMLLPrefWizPage.proxyTemplateNeverLabel")); //$NON-NLS-1$
		proxyTemplateNeverRadioButton.setData(new Integer(SWT.IMAGE_BMP));
		preferenceValue = config.getSuppressTemplateWrite();
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			proxyTemplateNeverRadioButton.setSelection(true);
		}
		proxyTemplateNeverRadioButton.addSelectionListener(eventMonitor);
		proxyTemplateNeverRadioButton.setToolTipText(Messages.getString("IBMLLPrefWizPage.proxyTemplateNeverToolTip")); //$NON-NLS-1$

		proxyTemplateAlwaysRadioButton = new Button(proxyTemplateOptionsGroup, SWT.RADIO);
		proxyTemplateAlwaysRadioButton.setText(Messages.getString("IBMLLPrefWizPage.proxyTemplateAlwaysLabel")); //$NON-NLS-1$
		preferenceValue = config.getTemplateWriteAlways();
		if (preferenceValue.equals(IBMLLPreferenceConstants.LL_YES)) {
			proxyTemplateAlwaysRadioButton.setSelection(true);
		}
		proxyTemplateAlwaysRadioButton.setData(new Integer(SWT.IMAGE_BMP));
		proxyTemplateAlwaysRadioButton.addSelectionListener(eventMonitor);
		proxyTemplateAlwaysRadioButton.setToolTipText(Messages.getString("IBMLLPrefWizPage.proxyTemplateAlwaysToolTip")); //$NON-NLS-1$

		// *********************************************************************
		// Polling options group
		// *********************************************************************
		proxyPollingGroup = new Group(preferencePane, SWT.NONE);
		proxyPollingGroup.setLayout(new RowLayout());
		proxyPollingGroup.setText(Messages.getString("IBMLLPrefWizPage.proxyPollingGroupLabel")); //$NON-NLS-1$

		new Label(proxyPollingGroup, SWT.NONE).setText(Messages.getString("IBMLLPrefWizPage.proxyPollingNodeMinLabel")); //$NON-NLS-1$

		proxyPollingNodeMin = new Spinner(proxyPollingGroup, SWT.READ_ONLY);
		proxyPollingNodeMin.setIncrement(10);
		proxyPollingNodeMin.setMinimum(30);
		proxyPollingNodeMin.setMaximum(300);
		proxyPollingNodeMin.setPageIncrement(50);
		proxyPollingNodeMin.setSelection(config.getMinNodePolling());
		proxyPollingNodeMin.setToolTipText(Messages.getString("IBMLLPrefWizPage.proxyPollingNodeMinToolTip")); //$NON-NLS-1$
		proxyPollingNodeMin.addModifyListener(eventMonitor);

		new Label(proxyPollingGroup, SWT.NONE).setText(Messages.getString("IBMLLPrefWizPage.proxyPollingNodeMaxLabel")); //$NON-NLS-1$

		proxyPollingNodeMax = new Spinner(proxyPollingGroup, SWT.READ_ONLY);
		proxyPollingNodeMax.setIncrement(10);
		proxyPollingNodeMax.setMinimum(30);
		proxyPollingNodeMax.setMaximum(500);
		proxyPollingNodeMax.setPageIncrement(50);
		proxyPollingNodeMax.setSelection(config.getMaxNodePolling());
		proxyPollingNodeMax.setToolTipText(Messages.getString("IBMLLPrefWizPage.proxyPollingNodeMaxToolTip")); //$NON-NLS-1$
		proxyPollingNodeMax.addModifyListener(eventMonitor);

		new Label(proxyPollingGroup, SWT.NONE).setText(Messages.getString("IBMLLPrefWizPage.proxyPollingJobLabel")); //$NON-NLS-1$

		proxyPollingJob = new Spinner(proxyPollingGroup, SWT.READ_ONLY);
		proxyPollingJob.setIncrement(10);
		proxyPollingJob.setMinimum(30);
		proxyPollingJob.setMaximum(300);
		proxyPollingJob.setPageIncrement(50);
		proxyPollingJob.setSelection(config.getJobPolling());
		proxyPollingJob.setToolTipText(Messages.getString("IBMLLPrefWizPage.proxyPollingJobToolTip")); //$NON-NLS-1$
		proxyPollingJob.addModifyListener(eventMonitor);

		setControl(preferencePane);
	}

	protected GridLayout createGridLayout(int columns, boolean isEqual, int mh, int mw) {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = columns;
		gridLayout.makeColumnsEqualWidth = isEqual;
		gridLayout.marginHeight = mh;
		gridLayout.marginWidth = mw;
		return gridLayout;
	}

	protected String getFieldContent(String text) {
		if (text.trim().length() == 0 || text.equals("")) { //$NON-NLS-1$
			return null;
		}

		return text;
	}

	/**
	 * Prompt the user to select a directory on the remote system
	 * 
	 * @param message
	 *            Title for the directory selector dialog
	 * @param currentPath
	 *            The current path to use as a starting point when opening the
	 *            directory selector
	 * @return Pathname to the selected directory or null
	 */
	protected String getRemoteDirectory(String message, String currentPath) {
		IRemoteServices services;
		IRemoteConnection connection;
		String serviceID;
		IRemoteConnectionManager connectionManager;

		serviceID = config.getRemoteServicesId();
		if (serviceID == null) {
			LLUIPlugin.getDefault().logError("getRemoteDirectory: " + Messages.getString("IBMLLResourceManagerOptionWizardPage.3")); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
		IWizardContainer container = null;
		if (getControl().isVisible()) {
			container = getWizard().getContainer();
		}
		services = PTPRemoteUIPlugin.getDefault().getRemoteServices(serviceID, container);
		if (services == null) {
			LLUIPlugin.getDefault().logError("getRemoteDirectory: " + Messages.getString("IBMLLResourceManagerOptionWizardPage.5")); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
		connectionManager = services.getConnectionManager();
		if (connectionManager == null) {
			LLUIPlugin.getDefault().logError("getRemoteDirectory: " + Messages.getString("IBMLLResourceManagerOptionWizardPage.7")); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
		connection = connectionManager.getConnection(config.getConnectionName());
		if (connection == null) {
			LLUIPlugin.getDefault().logError("getRemotePath: " + Messages.getString("IBMLLResourceManagerOptionWizardPage.9")); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
		IRemoteUIServices remoteUIServices = PTPRemoteUIPlugin.getDefault().getRemoteUIServices(services);
		if (remoteUIServices != null) {
			IRemoteUIFileManager fileMgr = remoteUIServices.getUIFileManager();
			if (fileMgr != null) {
				fileMgr.setConnection(connection);
				return fileMgr.browseDirectory(getShell(), message, currentPath, 0);
			}
		}
		LLUIPlugin.getDefault().logError("getRemotePath: " + Messages.getString("IBMLLResourceManagerOptionWizardPage.21")); //$NON-NLS-1$ //$NON-NLS-2$
		return null;
	}

	/**
	 * Prompt the user to select a file on the remote system
	 * 
	 * @param message
	 *            Title for the file selector dialog
	 * @param currentPath
	 *            The current path to use as a starting point when opening the
	 *            file selector
	 * @return Pathname to the selected file or null
	 */
	protected String getRemotePath(String message, String currentPath) {
		IRemoteServices services;
		IRemoteConnection connection;
		String serviceID;
		IRemoteConnectionManager connectionManager;

		serviceID = config.getRemoteServicesId();
		if (serviceID == null) {
			LLUIPlugin.getDefault().logError("getRemotePath: " + Messages.getString("IBMLLResourceManagerOptionWizardPage.3")); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
		IWizardContainer container = null;
		if (getControl().isVisible()) {
			container = getWizard().getContainer();
		}
		services = PTPRemoteUIPlugin.getDefault().getRemoteServices(serviceID, container);
		if (services == null) {
			LLUIPlugin.getDefault().logError("getRemotePath: " + Messages.getString("IBMLLResourceManagerOptionWizardPage.5")); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
		connectionManager = services.getConnectionManager();
		if (connectionManager == null) {
			LLUIPlugin.getDefault().logError("getRemotePath: " + Messages.getString("IBMLLResourceManagerOptionWizardPage.7")); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
		connection = connectionManager.getConnection(config.getConnectionName());
		if (connection == null) {
			LLUIPlugin.getDefault().logError("getRemotePath: " + Messages.getString("IBMLLResourceManagerOptionWizardPage.9")); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
		IRemoteUIServices remoteUIServices = PTPRemoteUIPlugin.getDefault().getRemoteUIServices(services);
		if (remoteUIServices != null) {
			IRemoteUIFileManager fileMgr = remoteUIServices.getUIFileManager();
			if (fileMgr != null) {
				fileMgr.setConnection(connection);
				return fileMgr.browseFile(getShell(), message, currentPath, 0);
			}
		}
		LLUIPlugin.getDefault().logError("getRemotePath: " + Messages.getString("IBMLLResourceManagerOptionWizardPage.11")); //$NON-NLS-1$ //$NON-NLS-2$
		return null;
	}

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
	 * Retrieve the settings specified on this panel and create the string
	 * containing proxy invocation options corresponding to those values
	 * 
	 * @return Status indicating successful completion
	 */
	protected boolean updateConfigOptions() {
		String options;
		boolean selection;

		options = ""; //$NON-NLS-1$

		if (proxyTraceMessageButton != null) {
			selection = proxyTraceMessageButton.getSelection();
			config.setTraceOption(selection ? IBMLLPreferenceConstants.LL_YES : IBMLLPreferenceConstants.LL_NO);
			if (selection) {
				options = options + "--trace_messages=y "; //$NON-NLS-1$
			}
		}

		if (proxyInfoMessageButton != null) {
			selection = proxyInfoMessageButton.getSelection();
			config.setInfoMessage(selection ? IBMLLPreferenceConstants.LL_YES : IBMLLPreferenceConstants.LL_NO);
			if (selection) {
				options = options + "--info_messages=y "; //$NON-NLS-1$
			}
		}

		if (proxyWarningMessageButton != null) {
			selection = proxyWarningMessageButton.getSelection();
			config.setWarningMessage(selection ? IBMLLPreferenceConstants.LL_YES : IBMLLPreferenceConstants.LL_NO);
			if (selection) {
				options = options + "--warning_messages=y "; //$NON-NLS-1$
			}
		}

		if (proxyErrorMessageButton != null) {
			selection = proxyErrorMessageButton.getSelection();
			config.setErrorMessage(selection ? IBMLLPreferenceConstants.LL_YES : IBMLLPreferenceConstants.LL_NO);
			if (selection) {
				options = options + "--error_messages=y "; //$NON-NLS-1$
			}
		}

		if (proxyFatalMessageButton != null) {
			selection = proxyFatalMessageButton.getSelection();
			config.setFatalMessage(selection ? IBMLLPreferenceConstants.LL_YES : IBMLLPreferenceConstants.LL_NO);
			if (selection) {
				options = options + "--fatal_messages=y "; //$NON-NLS-1$
			}
		}

		if (proxyArgsMessageButton != null) {
			selection = proxyArgsMessageButton.getSelection();
			config.setArgsMessage(selection ? IBMLLPreferenceConstants.LL_YES : IBMLLPreferenceConstants.LL_NO);
			if (selection) {
				options = options + "--args_messages=y "; //$NON-NLS-1$
			}
		}

		if (proxyDebugLoopButton != null) {
			selection = proxyDebugLoopButton.getSelection();
			config.setDebugLoop(selection ? IBMLLPreferenceConstants.LL_YES : IBMLLPreferenceConstants.LL_NO);
			if (selection) {
				options = options + "--debug_loop=y "; //$NON-NLS-1$
			}
		}

		if ((proxyLLDefaultRadioButton != null) && (proxyForceLocalRadioButton != null)
				&& (proxyForceMulticlusterRadioButton != null)) {
			if (proxyLLDefaultRadioButton.getSelection()) {
				options = options + "--multicluster=d "; //$NON-NLS-1$
			} else if (proxyForceLocalRadioButton.getSelection()) {
				options = options + "--multicluster=n "; //$NON-NLS-1$
			} else if (proxyForceMulticlusterRadioButton.getSelection()) {
				options = options + "--multicluster=y "; //$NON-NLS-1$
			}
		}

		if (proxyLLDefaultRadioButton != null) {
			config.setDefaultMulticluster(proxyLLDefaultRadioButton.getSelection() ? IBMLLPreferenceConstants.LL_YES
					: IBMLLPreferenceConstants.LL_NO);
		}

		if (proxyForceLocalRadioButton != null) {
			config.setForceProxyLocal(proxyForceLocalRadioButton.getSelection() ? IBMLLPreferenceConstants.LL_YES
					: IBMLLPreferenceConstants.LL_NO);
		}

		if (proxyForceMulticlusterRadioButton != null) {
			config.setForceProxyMulticluster(proxyForceMulticlusterRadioButton.getSelection() ? IBMLLPreferenceConstants.LL_YES
					: IBMLLPreferenceConstants.LL_NO);
		}

		if (proxyTemplateNeverRadioButton != null) {
			if (proxyTemplateNeverRadioButton.getSelection()) {
				options = options + "--template_write=n "; //$NON-NLS-1$
			} else if (proxyTemplateAlwaysRadioButton.getSelection()) {
				options = options + "--template_write=a "; //$NON-NLS-1$
			}
		}

		if (proxyTemplateNeverRadioButton != null) {
			config.setSuppressTemplateWrite(proxyTemplateNeverRadioButton.getSelection() ? IBMLLPreferenceConstants.LL_YES
					: IBMLLPreferenceConstants.LL_NO);
		}

		if (proxyTemplateAlwaysRadioButton != null) {
			config.setTemplateWriteAlways(proxyTemplateAlwaysRadioButton.getSelection() ? IBMLLPreferenceConstants.LL_YES
					: IBMLLPreferenceConstants.LL_NO);
		}

		if (proxyPollingNodeMin != null) {
			config.setMinNodePolling(proxyPollingNodeMin.getSelection());
			options = options + "--node_polling_min=" //$NON-NLS-1$
					+ proxyPollingNodeMin.getSelection() + " "; //$NON-NLS-1$
		}

		if (proxyPollingNodeMax != null) {
			config.setMaxNodePolling(proxyPollingNodeMax.getSelection());
			options = options + "--node_polling_max=" //$NON-NLS-1$
					+ proxyPollingNodeMax.getSelection() + " "; //$NON-NLS-1$
		}

		if (proxyPollingJob != null) {
			config.setJobPolling(proxyPollingJob.getSelection());
			options = options + "--job_polling=" //$NON-NLS-1$
					+ proxyPollingJob.getSelection() + " "; //$NON-NLS-1$
		}

		if (guiTraceMessageButton != null) {
			config.setGuiTraceMessage(guiTraceMessageButton.getSelection() ? IBMLLPreferenceConstants.LL_YES
					: IBMLLPreferenceConstants.LL_NO);
		}

		if (guiArgsMessageButton != null) {
			config.setGuiArgsMessage(guiArgsMessageButton.getSelection() ? IBMLLPreferenceConstants.LL_YES
					: IBMLLPreferenceConstants.LL_NO);
		}

		if (guiInfoMessageButton != null) {
			config.setGuiInfoMessage(guiInfoMessageButton.getSelection() ? IBMLLPreferenceConstants.LL_YES
					: IBMLLPreferenceConstants.LL_NO);
		}

		if (guiWarningMessageButton != null) {
			config.setGuiWarningMessage(guiWarningMessageButton.getSelection() ? IBMLLPreferenceConstants.LL_YES
					: IBMLLPreferenceConstants.LL_NO);
		}

		if (guiErrorMessageButton != null) {
			config.setGuiErrorMessage(guiErrorMessageButton.getSelection() ? IBMLLPreferenceConstants.LL_YES
					: IBMLLPreferenceConstants.LL_NO);
		}

		if (guiFatalMessageButton != null) {
			config.setGuiFatalMessage(guiFatalMessageButton.getSelection() ? IBMLLPreferenceConstants.LL_YES
					: IBMLLPreferenceConstants.LL_NO);
		}

		// System.err.println("Options from wizard page are now set to: " +
		// options );

		config.setInvocationOptions(options);

		return true;
	}

	/**
	 * Validate a pathname on the remote system
	 * 
	 * @param path
	 *            pathname to validate
	 * @param needDirectory
	 *            Flag indicating if path specifies a directory
	 * @return true if the pathname is valid, false otherwise
	 */
	protected boolean validatePath(String path, boolean needDirectory) {
		IRemoteServices services;
		IRemoteConnection connection;
		IRemoteConnectionManager connectionManager;
		IRemoteFileManager fileManager;
		IFileStore file;
		IFileInfo fileInfo;
		String serviceID;

		if (path == null) {
			return true;
		}
		serviceID = config.getRemoteServicesId();
		if (serviceID == null) {
			LLUIPlugin.getDefault().logError("validatePath: " + Messages.getString("IBMLLResourceManagerOptionWizardPage.3")); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
		IWizardContainer container = null;
		if (getControl().isVisible()) {
			container = getWizard().getContainer();
		}
		services = PTPRemoteUIPlugin.getDefault().getRemoteServices(serviceID, container);
		if (services == null) {
			LLUIPlugin.getDefault().logError("validatePath: " + Messages.getString("IBMLLResourceManagerOptionWizardPage.5")); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
		connectionManager = services.getConnectionManager();
		if (connectionManager == null) {
			LLUIPlugin.getDefault().logError("validatePath: " + Messages.getString("IBMLLResourceManagerOptionWizardPage.7")); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
		connection = connectionManager.getConnection(config.getConnectionName());
		if (connection == null) {
			LLUIPlugin.getDefault().logError("validatePath: " + Messages.getString("IBMLLResourceManagerOptionWizardPage.9")); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
		fileManager = services.getFileManager(connection);
		if (fileManager == null) {
			LLUIPlugin.getDefault().logError("validatePath: " + Messages.getString("IBMLLResourceManagerOptionWizardPage.31")); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
		file = fileManager.getResource(path);
		if (file == null) {
			return false;
		}
		try {
			fileInfo = file.fetchInfo(EFS.NONE, new NullProgressMonitor());
			return fileInfo.exists() && (fileInfo.isDirectory() == needDirectory);
		} catch (CoreException e) {
			return false;
		}
	}
}
