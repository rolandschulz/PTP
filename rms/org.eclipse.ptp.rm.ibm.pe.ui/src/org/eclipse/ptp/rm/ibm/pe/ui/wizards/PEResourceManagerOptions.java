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
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.ui.IRemoteUIFileManager;
import org.eclipse.ptp.remote.ui.IRemoteUIServices;
import org.eclipse.ptp.remote.ui.PTPRemoteUIPlugin;
import org.eclipse.ptp.rm.ibm.pe.core.PEPreferenceConstants;
import org.eclipse.ptp.rm.ibm.pe.core.rmsystem.IPEResourceManagerConfiguration;
import org.eclipse.ptp.rm.ibm.pe.ui.messages.Messages;
import org.eclipse.ptp.rm.ui.wizards.AbstractProxyOptions;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class PEResourceManagerOptions extends AbstractProxyOptions {
	private class EventMonitor implements SelectionListener, ModifyListener {
		public EventMonitor() {
		}

		public void modifyText(ModifyEvent e) {
			validateInput(e.getSource());
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

				if (remoteUIService == null) {
					remoteUIService = PTPRemoteUIPlugin.getDefault().getRemoteUIServices(getRemoteConnection().getRemoteServices());
				}
				if (remoteUIService != null) {
					IRemoteUIFileManager fmgr = remoteUIService.getUIFileManager();
					fmgr.setConnection(getRemoteConnection());
					selectedFile = fmgr
							.browseDirectory(getShell(), Messages.getString("PEDialogs.librarySelectorTitle"), "/", 0).toString(); //$NON-NLS-1$ //$NON-NLS-2$
				}
				if (selectedFile != null) {
					libOverridePath.setText(selectedFile);
				}
			} else {
				validateInput(e.getSource());
			}
		}
	}

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
	private Text nodePollMinInterval;
	private Text nodePollMaxInterval;
	private Text jobPollInterval;
	private Text libOverridePath;
	private Combo llMode;
	private Composite llOverrideBox;
	private Combo debugLevel;
	private Label debugLabel;
	private Label libOverrideLabel;
	private Label llModeLabel;
	private Label nodePollMinLabel;
	private Label nodePollMaxLabel;
	private Label jobPollLabel;
	private IRemoteUIServices remoteUIService;
	private EventMonitor eventMonitor;

	public PEResourceManagerOptions(WizardPage wizardPage, IPEResourceManagerConfiguration config) {
		super(wizardPage, config);
		setInitialOptions();
	}

	@Override
	protected IPEResourceManagerConfiguration getConfiguration() {
		return (IPEResourceManagerConfiguration) super.getConfiguration();
	}

	private void setInitialOptions() {
		StringTokenizer options;

		options = new StringTokenizer(getConfiguration().getInvocationOptionsStr(), " "); //$NON-NLS-1$
		getConfiguration().setSuspendProxy(PEPreferenceConstants.OPTION_NO);
		getConfiguration().setRunMiniproxy(PEPreferenceConstants.OPTION_NO);
		while (options.hasMoreTokens()) {
			String currentToken[];

			currentToken = options.nextToken().split("="); //$NON-NLS-1$
			if (currentToken.length == 1) {
				if (currentToken[0].equals(SUSPEND_AT_STARTUP_OPTION)) {
					getConfiguration().setSuspendProxy(PEPreferenceConstants.OPTION_YES);
				} else if (currentToken[0].equals(RUN_MINIPROXY_OPTION)) {
					getConfiguration().setRunMiniproxy(PEPreferenceConstants.OPTION_YES);
				} else if (currentToken[0].equals(USE_LOADLEVELER_OPTION)) {
					getConfiguration().setUseLoadLeveler(PEPreferenceConstants.OPTION_YES);
				}
			} else {
				if (currentToken[0].equals(MULTICLUSTER_OPTION)) {
					getConfiguration().setLoadLevelerMode(currentToken[1]);
				} else if (currentToken[0].equals(NODE_POLL_MIN_OPTION)) {
					getConfiguration().setNodeMinPollInterval(currentToken[1]);
				} else if (currentToken[0].equals(NODE_POLL_MAX_OPTION)) {
					getConfiguration().setNodeMaxPollInterval(currentToken[1]);
				} else if (currentToken[0].equals(JOB_POLL_OPTION)) {
					getConfiguration().setJobPollInterval(currentToken[1]);
				} else if (currentToken[0].equals(LIB_OVERRIDE_OPTION)) {
					getConfiguration().setLibraryOverride(currentToken[1]);
				} else if (currentToken[0].equals(TRACE_OPTION)) {
					getConfiguration().setDebugLevel(currentToken[1]);
				}
			}
		}
	}

	/**
	 * Set the widget enable state for these widgets based on whether the use
	 * LoadLeveler checkbox is checked
	 * 
	 * @param state
	 */
	private void setLLWidgetEnableState(boolean state) {
		llMode.setEnabled(state);
		nodePollMinInterval.setEnabled(state);
		nodePollMaxInterval.setEnabled(state);
		jobPollInterval.setEnabled(state);
		libOverridePath.setEnabled(state);
		libOverrideBrowse.setEnabled(state);
	}

	/**
	 * Create the widgets for this proxy configuration page and set their values
	 * to the last saved values for this resource manager
	 * 
	 */
	@Override
	protected Composite createContents(Composite parent) {
		setShell(parent.getShell());
		eventMonitor = new EventMonitor();
		optionsPane = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, true);
		optionsPane.setLayout(layout);
		optionsPane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		loadLevelerOption = new Button(optionsPane, SWT.CHECK);
		loadLevelerOption.setText(Messages.getString("PEDialogs.LoadLevelerOptionLabel")); //$NON-NLS-1$
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		loadLevelerOption.setLayoutData(gd);
		String preferenceValue = getConfiguration().getUseLoadLeveler();
		if ((preferenceValue != null) && (preferenceValue.equals(PEPreferenceConstants.OPTION_YES))) {
			loadLevelerOption.setSelection(true);
		}

		Composite llComp = new Composite(optionsPane, SWT.NONE);
		layout = new GridLayout(2, true);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		llComp.setLayout(layout);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.horizontalIndent = 20;
		gd.horizontalSpan = 2;
		llComp.setLayoutData(gd);

		llModeLabel = new Label(llComp, SWT.NONE);
		llModeLabel.setText(Messages.getString("PEDialogs.LLRunMode")); //$NON-NLS-1$
		llMode = new Combo(llComp, SWT.READ_ONLY);
		llMode.add(Messages.getString("PEDialogs.llModeLocal")); //$NON-NLS-1$
		llMode.add(Messages.getString("PEDialogs.llModeMulticluster")); //$NON-NLS-1$
		llMode.add(Messages.getString("PEDialogs.llModeDefault")); //$NON-NLS-1$
		preferenceValue = getConfiguration().getLoadLevelerMode();
		if (preferenceValue != null) {
			if (preferenceValue.equals("y")) { //$NON-NLS-1$
				llMode.select(1);
			} else if (preferenceValue.equals("n")) { //$NON-NLS-1$
				llMode.select(0);
			} else {
				llMode.select(2);
			}
		} else {
			llMode.select(2);
		}
		gd = new GridData();
		gd.horizontalAlignment = SWT.RIGHT;
		llMode.setLayoutData(gd);

		nodePollMinLabel = new Label(llComp, SWT.NONE);
		nodePollMinLabel.setText(Messages.getString("PEDialogs.minNodePollInterval")); //$NON-NLS-1$
		nodePollMinInterval = new Text(llComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		nodePollMinInterval.setLayoutData(gd);
		preferenceValue = getConfiguration().getNodeMinPollInterval();
		if (preferenceValue != null) {
			nodePollMinInterval.setText(preferenceValue);
		}

		nodePollMaxLabel = new Label(llComp, SWT.NONE);
		nodePollMaxLabel.setText(Messages.getString("PEDialogs.maxNodePollInterval")); //$NON-NLS-1$
		nodePollMaxInterval = new Text(llComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		nodePollMaxInterval.setLayoutData(gd);
		preferenceValue = getConfiguration().getNodeMaxPollInterval();
		if (preferenceValue != null) {
			nodePollMaxInterval.setText(preferenceValue);
		}

		jobPollLabel = new Label(llComp, SWT.NONE);
		jobPollLabel.setText(Messages.getString("PEDialogs.jobPollInterval")); //$NON-NLS-1$
		jobPollInterval = new Text(llComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		jobPollInterval.setLayoutData(gd);
		preferenceValue = getConfiguration().getJobPollInterval();
		if (preferenceValue != null) {
			jobPollInterval.setText(preferenceValue);
		}

		libOverrideLabel = new Label(llComp, SWT.NONE);
		libOverrideLabel.setText(Messages.getString("PEDialogs.libOverrideLabel")); //$NON-NLS-1$

		llOverrideBox = new Composite(llComp, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		llOverrideBox.setLayoutData(gd);
		layout = new GridLayout(2, false);
		layout.marginLeft = 0;
		layout.marginRight = 0;
		layout.marginWidth = 0;
		llOverrideBox.setLayout(layout);
		libOverridePath = new Text(llOverrideBox, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		libOverridePath.setLayoutData(gd);
		libOverrideBrowse = new Button(llOverrideBox, SWT.PUSH);
		libOverrideBrowse.setText(Messages.getString("PEDialogs.browse")); //$NON-NLS-1$
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = false;
		libOverrideBrowse.setLayoutData(gd);
		preferenceValue = getConfiguration().getLibraryOverride();
		if (preferenceValue != null) {
			libOverridePath.setText(preferenceValue);
		}

		runMiniproxy = new Button(optionsPane, SWT.CHECK);
		runMiniproxy.setText(Messages.getString("PEDialogs.MiniproxyLabel")); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalSpan = 2;
		runMiniproxy.setLayoutData(gd);
		preferenceValue = getConfiguration().getRunMiniproxy();
		if ((preferenceValue != null) && (preferenceValue.equals(PEPreferenceConstants.OPTION_YES))) {
			runMiniproxy.setSelection(true);
		}

		debugLabel = new Label(optionsPane, SWT.NONE);
		debugLabel.setText(Messages.getString("PEDialogs.TraceLevelLabel")); //$NON-NLS-1$
		debugLevel = new Combo(optionsPane, SWT.READ_ONLY);
		debugLevel.add(PEPreferenceConstants.TRACE_NOTHING);
		debugLevel.add(PEPreferenceConstants.TRACE_FUNCTION);
		debugLevel.add(PEPreferenceConstants.TRACE_DETAIL);
		preferenceValue = getConfiguration().getDebugLevel();
		if (preferenceValue != null) {
			debugLevel.setText(preferenceValue);
		}
		gd = new GridData();
		gd.horizontalAlignment = SWT.RIGHT;
		debugLevel.setLayoutData(gd);

		suspendOption = new Button(optionsPane, SWT.CHECK);
		suspendOption.setText(Messages.getString("PEDialogs.SuspendLabel")); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalSpan = 2;
		suspendOption.setLayoutData(gd);
		preferenceValue = getConfiguration().getSuspendProxy();
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
		llMode.addSelectionListener(eventMonitor);
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
	 * Validate proxy settings
	 * 
	 * @return Status indicating successful completion
	 */
	@SuppressWarnings("unused")
	protected boolean validateInput(Object eventSource) {
		getWizardPage().setErrorMessage(null);
		if (loadLevelerOption.getSelection()) {
			String widgetValue = nodePollMinInterval.getText().trim();
			if (widgetValue.length() > 0) {
				try {
					int interval = Integer.valueOf(widgetValue);
				} catch (NumberFormatException e) {
					getWizardPage().setErrorMessage(Messages.getString("PEDialogs.invalidMinPollInterval")); //$NON-NLS-1$
					return false;
				}
			}
			widgetValue = nodePollMaxInterval.getText().trim();
			if (widgetValue.length() > 0) {
				try {
					int interval = Integer.valueOf(widgetValue);
				} catch (NumberFormatException e) {
					getWizardPage().setErrorMessage(Messages.getString("PEDialogs.invalidMaxPollInterval")); //$NON-NLS-1$
					return false;
				}
			}
			widgetValue = jobPollInterval.getText().trim();
			if (widgetValue.length() > 0) {
				try {
					int interval = Integer.valueOf(widgetValue);
				} catch (NumberFormatException e) {
					getWizardPage().setErrorMessage(Messages.getString("PEDialogs.invalidJobPollInterval")); //$NON-NLS-1$
					return false;
				}
			}
			if (eventSource == libOverridePath) {
				widgetValue = libOverridePath.getText().trim();
				if (widgetValue.length() > 0) {
					IFileStore remoteResource;
					IFileInfo fileInfo;
					IPath testPath;
					testPath = new Path(widgetValue);
					if (!testPath.isValidPath(widgetValue)) {
						getWizardPage().setErrorMessage(Messages.getString("PEDialogs.invalidLibraryPath")); //$NON-NLS-1$
					}
					IRemoteFileManager fileMgr = getRemoteConnection().getRemoteServices().getFileManager(getRemoteConnection());
					remoteResource = fileMgr.getResource(testPath.toString());
					fileInfo = remoteResource.fetchInfo();
					if ((!fileInfo.exists()) || (!fileInfo.isDirectory())) {
						getWizardPage().setErrorMessage(Messages.getString("PEDialogs.invalidLibraryPath")); //$NON-NLS-1$
					}
				} else {
					getWizardPage().setErrorMessage(Messages.getString("PEDialogs.invalidLibraryPath")); //$NON-NLS-1$
				}
			}
		}

		return true;
	}

	@Override
	public void save() {
		String proxyOptions = ""; //$NON-NLS-1$

		if (loadLevelerOption.getSelection()) {
			proxyOptions += USE_LOADLEVELER_OPTION + " "; //$NON-NLS-1$
			getConfiguration().setUseLoadLeveler(PEPreferenceConstants.OPTION_YES);
			proxyOptions += MULTICLUSTER_OPTION + "="; //$NON-NLS-1$
			String multiclusterMode = "y"; //$NON-NLS-1$
			if (llMode.getText().equals(Messages.getString("PEDialogs.llModeDefault"))) { //$NON-NLS-1$
				multiclusterMode = "d"; //$NON-NLS-1$
			} else if (llMode.getText().equals(Messages.getString("PEDialogs.llModeLocal"))) { //$NON-NLS-1$
				multiclusterMode = "n"; //$NON-NLS-1$
			}
			proxyOptions += multiclusterMode + " "; //$NON-NLS-1$
			getConfiguration().setLoadLevelerMode(multiclusterMode);
			String widgetValue = nodePollMinInterval.getText().trim();
			proxyOptions += NODE_POLL_MIN_OPTION + "=" + widgetValue + " "; //$NON-NLS-1$ //$NON-NLS-2$
			getConfiguration().setNodeMinPollInterval(widgetValue);
			widgetValue = nodePollMaxInterval.getText().trim();
			proxyOptions += NODE_POLL_MAX_OPTION + "=" + widgetValue + " "; //$NON-NLS-1$ //$NON-NLS-2$
			getConfiguration().setNodeMaxPollInterval(widgetValue);
			widgetValue = jobPollInterval.getText().trim();
			proxyOptions += JOB_POLL_OPTION + "=" + widgetValue + " "; //$NON-NLS-1$ //$NON-NLS-2$
			getConfiguration().setJobPollInterval(widgetValue);
			widgetValue = libOverridePath.getText().trim();
			proxyOptions += LIB_OVERRIDE_OPTION + "=" + widgetValue + " "; //$NON-NLS-1$ //$NON-NLS-2$
			getConfiguration().setLibraryOverride(widgetValue);
		} else {
			getConfiguration().setUseLoadLeveler(PEPreferenceConstants.OPTION_NO);
		}

		String traceOpt = debugLevel.getText();
		proxyOptions += TRACE_OPTION + "=" + traceOpt + " "; //$NON-NLS-1$ //$NON-NLS-2$
		getConfiguration().setDebugLevel(traceOpt);

		if (suspendOption.getSelection()) {
			proxyOptions += SUSPEND_AT_STARTUP_OPTION;
			getConfiguration().setSuspendProxy(PEPreferenceConstants.OPTION_YES);
		} else {
			getConfiguration().setSuspendProxy(PEPreferenceConstants.OPTION_NO);
		}

		if (runMiniproxy.getSelection()) {
			proxyOptions += RUN_MINIPROXY_OPTION + " "; //$NON-NLS-1$
			getConfiguration().setRunMiniproxy(PEPreferenceConstants.OPTION_YES);
		} else {
			getConfiguration().setRunMiniproxy(PEPreferenceConstants.OPTION_NO);
		}

		getConfiguration().setInvocationOptions(proxyOptions);
	}
}
