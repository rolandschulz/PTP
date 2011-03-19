/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.rm.ibm.pe.ui.wizards;

import java.util.StringTokenizer;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.ui.IRemoteUIFileManager;
import org.eclipse.ptp.remote.ui.IRemoteUIServices;
import org.eclipse.ptp.remote.ui.PTPRemoteUIPlugin;
import org.eclipse.ptp.rm.ibm.pe.core.PEPreferenceConstants;
import org.eclipse.ptp.rm.ibm.pe.core.rmsystem.PEResourceManagerConfiguration;
import org.eclipse.ptp.rm.ibm.pe.ui.messages.Messages;
import org.eclipse.ptp.ui.wizards.IRMConfigurationWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class PEResourceManagerOptionDialog extends TitleAreaDialog {
	private static final String SUSPEND_AT_STARTUP_OPTION = "--suspend_at_startup"; //$NON-NLS-1$
	private static final String USE_LOADLEVELER_OPTION = "--useloadleveler"; //$NON-NLS-1$
	private static final String MULTICLUSTER_OPTION = "--multicluster"; //$NON-NLS-1$
	private static final String NODE_POLL_MIN_OPTION = "--node_polling_min"; //$NON-NLS-1$
	private static final String NODE_POLL_MAX_OPTION = "--node_polling_max"; //$NON-NLS-1$
	private static final String JOB_POLL_OPTION = "--job_polling"; //$NON-NLS-1$
	private static final String LIB_OVERRIDE_OPTION = "--lib_override"; //$NON-NLS-1$
	private static final String TRACE_OPTION = "--trace"; //$NON-NLS-1$
	private static final String RUN_MINIPROXY_OPTION = "--runMiniproxy"; //$NON-NLS-1$
	private Composite optionsPane;
	private Button loadLevelerOption;
	private Button suspendOption;
	private Button runMiniproxy;
	private Button libOverrideBrowse;
	private Button llModeLocal;
	private Button llModeMulticluster;
	private Button llModeDefault;
	private Text nodePollMinInterval;
	private Text nodePollMaxInterval;
	private Text jobPollInterval;
	private Text libOverridePath;
	private Group llModeGroup;
	private Composite llOverrideBox;
	private Combo debugLevel;
	private Label llLabel;
	private Label debugLabel;
	private Label suspendLabel;
	private Label runMiniproxyLabel;
	private Label libOverrideLabel;
	private Label llModeLabel;
	private Label nodePollMinLabel;
	private Label nodePollMaxLabel;
	private Label jobPollLabel;
	private final Shell parentShell;
	private String proxyOptions;
	private final PEResourceManagerConfiguration fConfig;
	private final IRMConfigurationWizard fWizard;
	private IRemoteServices remoteService;
	private IRemoteUIServices remoteUIService;
	private IRemoteConnection remoteConnection;
	private IRemoteConnectionManager connMgr;

	private EventMonitor eventMonitor;

	private class EventMonitor implements SelectionListener, ModifyListener {
		public EventMonitor() {
		}

		public void widgetDefaultSelected(SelectionEvent e) {
		}

		public void widgetSelected(SelectionEvent e) {
			if (e.getSource() == loadLevelerOption) {
				if (loadLevelerOption.getSelection()) {
					setLLWidgetEnableState(true);
				} else {
					setLLWidgetEnableState(false);
				}
			}
			if (e.getSource() == libOverrideBrowse) {
				String selectedFile = null;

				if (remoteUIService != null) {
					IRemoteUIFileManager fmgr = remoteUIService.getUIFileManager();
					fmgr.setConnection(remoteConnection);
					selectedFile = fmgr
							.browseDirectory(parentShell, Messages.getString("PEDialogs.librarySelectorTitle"), "/", 0).toString(); //$NON-NLS-1$ //$NON-NLS-2$
				}
				if (selectedFile != null) {
					libOverridePath.setText(selectedFile);
				}
			} else {
				validateInput(e.getSource());
			}
		}

		public void modifyText(ModifyEvent e) {
			validateInput(e.getSource());
		}
	}

	public PEResourceManagerOptionDialog(Shell parent, IRMConfigurationWizard wizard, String initialOptions) {
		super(parent);
		parentShell = parent;
		fWizard = wizard;
		// fConfig = (PEResourceManagerConfiguration) wizard.getConfiguration();
		fConfig = null;
		setInitialOptions(initialOptions);
		create();
	}

	/**
	 * Create the widgets for this proxy configuration page and set their values
	 * to the last saved values for this resource manager
	 * 
	 * @param parent
	 *            - The parent widget for this class
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		GridLayout layout;
		GridLayout libPathLayout;
		GridLayout modeLayout;
		GridData gd;
		String preferenceValue;

		setTitle(Messages.getString("PEDialogs.InvocationOptionsTitle")); //$NON-NLS-1$
		eventMonitor = new EventMonitor();
		optionsPane = new Composite(parent, SWT.NONE);
		layout = new GridLayout(2, true);
		optionsPane.setLayout(layout);

		llLabel = new Label(optionsPane, SWT.NONE);
		llLabel.setText(Messages.getString("PEDialogs.LoadLevelerOptionLabel")); //$NON-NLS-1$
		loadLevelerOption = new Button(optionsPane, SWT.CHECK);
		preferenceValue = fConfig.getUseLoadLeveler();
		if ((preferenceValue != null) && (preferenceValue.equals(PEPreferenceConstants.OPTION_YES))) {
			loadLevelerOption.setSelection(true);
		}
		llModeLabel = new Label(optionsPane, SWT.NONE);
		llModeLabel.setText(Messages.getString("PEDialogs.LLRunMode")); //$NON-NLS-1$
		llModeGroup = new Group(optionsPane, SWT.SHADOW_ETCHED_IN);
		modeLayout = new GridLayout(2, true);
		llModeGroup.setLayout(modeLayout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		llModeGroup.setLayoutData(gd);
		llModeLocal = new Button(llModeGroup, SWT.RADIO);
		llModeLocal.setText(Messages.getString("PEDialogs.llModeLocal")); //$NON-NLS-1$
		llModeMulticluster = new Button(llModeGroup, SWT.RADIO);
		llModeMulticluster.setText(Messages.getString("PEDialogs.llModeMulticluster")); //$NON-NLS-1$
		llModeDefault = new Button(llModeGroup, SWT.RADIO);
		llModeDefault.setText(Messages.getString("PEDialogs.llModeDefault")); //$NON-NLS-1$
		llModeDefault.setSelection(false);
		llModeLocal.setSelection(false);
		llModeMulticluster.setSelection(false);
		preferenceValue = fConfig.getLoadLevelerMode();
		if (preferenceValue != null) {
			if (preferenceValue.equals("y")) { //$NON-NLS-1$
				llModeMulticluster.setSelection(true);
			} else if (preferenceValue.equals("n")) { //$NON-NLS-1$
				llModeLocal.setSelection(true);
			} else {
				llModeDefault.setSelection(true);
			}
		} else {
			llModeDefault.setSelection(true);
		}

		nodePollMinLabel = new Label(optionsPane, SWT.NONE);
		nodePollMinLabel.setText(Messages.getString("PEDialogs.minNodePollInterval")); //$NON-NLS-1$
		nodePollMinInterval = new Text(optionsPane, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		nodePollMinInterval.setLayoutData(gd);
		preferenceValue = fConfig.getNodeMinPollInterval();
		if (preferenceValue != null) {
			nodePollMinInterval.setText(preferenceValue);
		}

		nodePollMaxLabel = new Label(optionsPane, SWT.NONE);
		nodePollMaxLabel.setText(Messages.getString("PEDialogs.maxNodePollInterval")); //$NON-NLS-1$
		nodePollMaxInterval = new Text(optionsPane, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		nodePollMaxInterval.setLayoutData(gd);
		preferenceValue = fConfig.getNodeMaxPollInterval();
		if (preferenceValue != null) {
			nodePollMaxInterval.setText(preferenceValue);
		}

		jobPollLabel = new Label(optionsPane, SWT.NONE);
		jobPollLabel.setText(Messages.getString("PEDialogs.jobPollInterval")); //$NON-NLS-1$
		jobPollInterval = new Text(optionsPane, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		jobPollInterval.setLayoutData(gd);
		preferenceValue = fConfig.getJobPollInterval();
		if (preferenceValue != null) {
			jobPollInterval.setText(preferenceValue);
		}

		libOverrideLabel = new Label(optionsPane, SWT.NONE);
		libOverrideLabel.setText(Messages.getString("PEDialogs.libOverrideLabel")); //$NON-NLS-1$

		llOverrideBox = new Composite(optionsPane, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		llOverrideBox.setLayoutData(gd);
		libPathLayout = new GridLayout(2, false);
		libPathLayout.marginLeft = 0;
		libPathLayout.marginRight = 0;
		libPathLayout.marginWidth = 0;
		llOverrideBox.setLayout(libPathLayout);
		libOverridePath = new Text(llOverrideBox, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		libOverridePath.setLayoutData(gd);
		libOverrideBrowse = new Button(llOverrideBox, SWT.PUSH);
		libOverrideBrowse.setText(Messages.getString("PEDialogs.browse")); //$NON-NLS-1$
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = false;
		libOverrideBrowse.setLayoutData(gd);
		preferenceValue = fConfig.getLibraryOverride();
		if (preferenceValue != null) {
			libOverridePath.setText(preferenceValue);
		}

		runMiniproxyLabel = new Label(optionsPane, SWT.NONE);
		runMiniproxyLabel.setText(Messages.getString("PEDialogs.MiniproxyLabel")); //$NON-NLS-1$
		runMiniproxy = new Button(optionsPane, SWT.CHECK);
		preferenceValue = fConfig.getRunMiniproxy();
		if ((preferenceValue != null) && (preferenceValue.equals(PEPreferenceConstants.OPTION_YES))) {
			runMiniproxy.setSelection(true);
		}

		debugLabel = new Label(optionsPane, SWT.NONE);
		debugLabel.setText(Messages.getString("PEDialogs.TraceLevelLabel")); //$NON-NLS-1$
		debugLevel = new Combo(optionsPane, SWT.READ_ONLY);
		debugLevel.add(PEPreferenceConstants.TRACE_NOTHING);
		debugLevel.add(PEPreferenceConstants.TRACE_FUNCTION);
		debugLevel.add(PEPreferenceConstants.TRACE_DETAIL);
		preferenceValue = fConfig.getDebugLevel();
		if (preferenceValue != null) {
			debugLevel.setText(preferenceValue);
		}

		suspendLabel = new Label(optionsPane, SWT.NULL);
		suspendLabel.setText(Messages.getString("PEDialogs.SuspendLabel")); //$NON-NLS-1$
		suspendOption = new Button(optionsPane, SWT.CHECK);
		preferenceValue = fConfig.getSuspendProxy();
		// suspendOption is used to specify that the proxy should suspend after
		// it has recognized this
		// option so that a debugger can attach to it. Since this is a debugging
		// aid, there is no
		// preferences setting for this flag (proxies should never suspend at
		// startup by default)
		if ((preferenceValue != null) && (preferenceValue.equals(PEPreferenceConstants.OPTION_YES))) {
			suspendOption.setSelection(true);
		}

		if (!loadLevelerOption.getSelection()) {
			setLLWidgetEnableState(false);
		}
		loadLevelerOption.addSelectionListener(eventMonitor);
		suspendOption.addSelectionListener(eventMonitor);
		llModeLocal.addSelectionListener(eventMonitor);
		llModeMulticluster.addSelectionListener(eventMonitor);
		llModeDefault.addSelectionListener(eventMonitor);
		nodePollMinInterval.addModifyListener(eventMonitor);
		nodePollMaxInterval.addModifyListener(eventMonitor);
		jobPollInterval.addModifyListener(eventMonitor);
		libOverridePath.addModifyListener(eventMonitor);
		libOverrideBrowse.addSelectionListener(eventMonitor);
		runMiniproxy.addSelectionListener(eventMonitor);
		suspendOption.addSelectionListener(eventMonitor);
		debugLevel.addSelectionListener(eventMonitor);
		// Ensure that the starting values for this dialog are valid and that
		// options contained in this
		// dialog are stored in the option string even if no changes are made.
		validateInput(null);
		return optionsPane;
	}

	/**
	 * Set the widget enable state for these widgets based on whether the use
	 * LoadLeveler checkbox is checked
	 * 
	 * @param state
	 */
	private void setLLWidgetEnableState(boolean state) {
		llModeLocal.setEnabled(state);
		llModeMulticluster.setEnabled(state);
		llModeDefault.setEnabled(state);
		nodePollMinInterval.setEnabled(state);
		nodePollMaxInterval.setEnabled(state);
		jobPollInterval.setEnabled(state);
		libOverridePath.setEnabled(state);
		libOverrideBrowse.setEnabled(state);
	}

	/**
	 * Retrieve the settings specified on this panel, validate those values, and
	 * create the string containing proxy invocation options corresponding to
	 * those values
	 * 
	 * @return Status indicating successful completion
	 */
	@SuppressWarnings("unused")
	protected boolean validateInput(Object eventSource) {
		String traceOpt;

		proxyOptions = ""; //$NON-NLS-1$
		setErrorMessage(null);
		if (loadLevelerOption.getSelection()) {
			String widgetValue;
			String multiclusterMode;
			int interval;

			proxyOptions = proxyOptions + USE_LOADLEVELER_OPTION + " "; //$NON-NLS-1$
			fConfig.setUseLoadLeveler(PEPreferenceConstants.OPTION_YES);
			proxyOptions = proxyOptions + MULTICLUSTER_OPTION + "="; //$NON-NLS-1$
			if (llModeDefault.getSelection()) {
				multiclusterMode = "d"; //$NON-NLS-1$
			} else if (llModeLocal.getSelection()) {
				multiclusterMode = "n"; //$NON-NLS-1$
			} else {
				multiclusterMode = "y"; //$NON-NLS-1$
			}
			proxyOptions = proxyOptions + multiclusterMode + " "; //$NON-NLS-1$
			fConfig.setLoadLevelerMode(multiclusterMode);
			widgetValue = nodePollMinInterval.getText().trim();
			if (widgetValue.length() > 0) {
				try {
					interval = Integer.valueOf(widgetValue);
				} catch (NumberFormatException e) {
					setErrorMessage(Messages.getString("PEDialogs.invalidMinPollInterval")); //$NON-NLS-1$
					return false;
				}
				proxyOptions = proxyOptions + NODE_POLL_MIN_OPTION + "=" + widgetValue + " "; //$NON-NLS-1$ //$NON-NLS-2$
			}
			fConfig.setNodeMinPollInterval(widgetValue);
			widgetValue = nodePollMaxInterval.getText().trim();
			if (widgetValue.length() > 0) {
				try {
					interval = Integer.valueOf(widgetValue);
				} catch (NumberFormatException e) {
					setErrorMessage(Messages.getString("PEDialogs.invalidMaxPollInterval")); //$NON-NLS-1$
					return false;
				}
				proxyOptions = proxyOptions + NODE_POLL_MAX_OPTION + "=" + widgetValue + " "; //$NON-NLS-1$ //$NON-NLS-2$
			}
			fConfig.setNodeMaxPollInterval(widgetValue);
			widgetValue = jobPollInterval.getText().trim();
			if (widgetValue.length() > 0) {
				try {
					interval = Integer.valueOf(widgetValue);
				} catch (NumberFormatException e) {
					setErrorMessage(Messages.getString("PEDialogs.invalidJobPollInterval")); //$NON-NLS-1$
					return false;
				}
				proxyOptions = proxyOptions + JOB_POLL_OPTION + "=" + widgetValue + " "; //$NON-NLS-1$ //$NON-NLS-2$
			}
			fConfig.setJobPollInterval(widgetValue);
			widgetValue = libOverridePath.getText().trim();
			if ((widgetValue.length() > 0) && (eventSource == libOverridePath)) {
				IFileStore remoteResource;
				IFileInfo fileInfo;
				IPath testPath;
				remoteService = PTPRemoteUIPlugin.getDefault().getRemoteServices(fConfig.getRemoteServicesId(),
						fWizard.getContainer());
				connMgr = remoteService.getConnectionManager();
				remoteConnection = connMgr.getConnection(fConfig.getConnectionName());
				testPath = new Path(widgetValue);
				if (!testPath.isValidPath(widgetValue)) {
					setErrorMessage(Messages.getString("PEDialogs.invalidLibraryPath")); //$NON-NLS-1$
				}
				remoteResource = remoteService.getFileManager(remoteConnection).getResource(testPath.toString());
				fileInfo = remoteResource.fetchInfo();
				if ((!fileInfo.exists()) || (!fileInfo.isDirectory())) {
					setErrorMessage(Messages.getString("PEDialogs.invalidLibraryPath")); //$NON-NLS-1$
				}
				proxyOptions = proxyOptions + LIB_OVERRIDE_OPTION + "=" + widgetValue + " "; //$NON-NLS-1$ //$NON-NLS-2$
			}
			fConfig.setLibraryOverride(widgetValue);
		} else {
			fConfig.setUseLoadLeveler(PEPreferenceConstants.OPTION_NO);
		}
		traceOpt = debugLevel.getText();
		if (traceOpt.length() > 0) {
			proxyOptions = proxyOptions + TRACE_OPTION + "=" + traceOpt + " "; //$NON-NLS-1$ //$NON-NLS-2$
			fConfig.setDebugLevel(traceOpt);
		}
		if (suspendOption.getSelection()) {
			proxyOptions = proxyOptions + SUSPEND_AT_STARTUP_OPTION;
			fConfig.setSuspendProxy(PEPreferenceConstants.OPTION_YES);
		} else {
			fConfig.setSuspendProxy(PEPreferenceConstants.OPTION_NO);
		}
		if (runMiniproxy.getSelection()) {
			proxyOptions = proxyOptions + RUN_MINIPROXY_OPTION + " "; //$NON-NLS-1$
			fConfig.setRunMiniproxy(PEPreferenceConstants.OPTION_YES);
		} else {
			fConfig.setRunMiniproxy(PEPreferenceConstants.OPTION_NO);
		}
		fConfig.setInvocationOptions(proxyOptions);
		return true;
	}

	public String getValue() {
		return proxyOptions;
	}

	public void setInitialOptions(String initialOptions) {
		StringTokenizer options;

		options = new StringTokenizer(initialOptions, " "); //$NON-NLS-1$
		fConfig.setSuspendProxy(PEPreferenceConstants.OPTION_NO);
		fConfig.setRunMiniproxy(PEPreferenceConstants.OPTION_NO);
		while (options.hasMoreTokens()) {
			String currentToken[];

			currentToken = options.nextToken().split("="); //$NON-NLS-1$
			if (currentToken.length == 1) {
				if (currentToken[0].equals(SUSPEND_AT_STARTUP_OPTION)) {
					fConfig.setSuspendProxy(PEPreferenceConstants.OPTION_YES);
				} else if (currentToken[0].equals(RUN_MINIPROXY_OPTION)) {
					fConfig.setRunMiniproxy(PEPreferenceConstants.OPTION_YES);
				} else if (currentToken[0].equals(USE_LOADLEVELER_OPTION)) {
					fConfig.setUseLoadLeveler(PEPreferenceConstants.OPTION_YES);
				}
			} else {
				if (currentToken[0].equals(MULTICLUSTER_OPTION)) {
					fConfig.setLoadLevelerMode(currentToken[1]);
				} else if (currentToken[0].equals(NODE_POLL_MIN_OPTION)) {
					fConfig.setNodeMinPollInterval(currentToken[1]);
				} else if (currentToken[0].equals(NODE_POLL_MAX_OPTION)) {
					fConfig.setNodeMaxPollInterval(currentToken[1]);
				} else if (currentToken[0].equals(JOB_POLL_OPTION)) {
					fConfig.setJobPollInterval(currentToken[1]);
				} else if (currentToken[0].equals(LIB_OVERRIDE_OPTION)) {
					fConfig.setLibraryOverride(currentToken[1]);
				} else if (currentToken[0].equals(TRACE_OPTION)) {
					fConfig.setDebugLevel(currentToken[1]);
				}
			}
		}
	}
}
