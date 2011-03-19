/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.ui.wizards;

import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.ui.IRemoteUIConstants;
import org.eclipse.ptp.remote.ui.IRemoteUIFileManager;
import org.eclipse.ptp.remote.ui.IRemoteUIServices;
import org.eclipse.ptp.remote.ui.PTPRemoteUIPlugin;
import org.eclipse.ptp.rm.core.rmsystem.IRemoteResourceManagerConfiguration;
import org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration;
import org.eclipse.ptp.rm.ui.messages.Messages;
import org.eclipse.ptp.rmsystem.IResourceManagerComponentConfiguration;
import org.eclipse.ptp.ui.wizards.IRMConfigurationWizard;
import org.eclipse.ptp.utils.ui.swt.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

/*
 * TODO: make this class expansible for preference pages for tools.
 * One might want to extend this class and add more fields.
 * - Make listener extensible
 * - Make data source extensible
 * - Make createContents extensible
 */
/**
 *
 */
public abstract class AbstractToolRMConfigurationWizardPage extends AbstractConfigurationWizardPage {

	protected class DataSource extends WizardPageDataSource {
		private IToolRMConfiguration config = null;

		private boolean commandsEnabled = false;
		private boolean useToolDefaults = false;
		private boolean useInstallDefaults = false;

		private String launchCmd = null;
		private String debugCmd = null;
		private String discoverCmd = null;
		private String periodicMonitorCmd = null;
		private int periodicMonitorTime = 0;
		private String continuousMonitorCmd = null;
		private String remoteInstallPath = null;

		protected DataSource(AbstractConfigurationWizardPage page) {
			super(page);
		}

		public String getContinuousMonitorCmd() {
			return continuousMonitorCmd;
		}

		public String getDebugCmd() {
			return debugCmd;
		}

		public String getDiscoverCmd() {
			return discoverCmd;
		}

		public String getLaunchCmd() {
			return launchCmd;
		}

		public String getPeriodicMonitorCmd() {
			return periodicMonitorCmd;
		}

		public int getPeriodicMonitorTime() {
			return periodicMonitorTime;
		}

		public String getRemoteInstallPath() {
			return remoteInstallPath;
		}

		public boolean getCommandsEnabled() {
			return commandsEnabled;
		}

		public boolean getInstallDefaults() {
			return useInstallDefaults;
		}

		public boolean getUseDefaults() {
			return useToolDefaults;
		}

		/**
		 * @since 2.0
		 */
		public void setCommands(String launchCmd, String debugCmd, String discoverCmd, String periodicMonitorCmd,
				int periodicMonitorTime, String continuousMonitorCmd) {
			this.launchCmd = launchCmd;
			this.debugCmd = debugCmd;
			this.discoverCmd = discoverCmd;
			this.periodicMonitorCmd = periodicMonitorCmd;
			this.periodicMonitorTime = periodicMonitorTime;
			this.continuousMonitorCmd = continuousMonitorCmd;
		}

		/**
		 * @since 2.0
		 */
		public void setInstallPath(String remoteInstallPath) {
			this.remoteInstallPath = remoteInstallPath;
		}

		@Override
		public void setConfiguration(IResourceManagerComponentConfiguration configuration) {
			super.setConfiguration(configuration);
			// Store a local reference to the configuration
			this.config = (IToolRMConfiguration) configuration;
		}

		public void setCommandsEnabled(boolean enable) {
			this.commandsEnabled = enable;
		}

		public void setInstallDefaults(boolean useInstallDefaults) {
			this.useInstallDefaults = useInstallDefaults;
		}

		public void setUseDefaults(boolean useToolDefaults) {
			this.useToolDefaults = useToolDefaults;
		}

		@Override
		protected void copyFromFields() throws ValidationException {
			useToolDefaults = defaultCmdButton.getSelection();
			useInstallDefaults = defaultInstallButton.getSelection();

			if (launchCmdText != null) {
				launchCmd = extractText(launchCmdText);
			}
			if (debugCmdText != null) {
				debugCmd = extractText(debugCmdText);
			}
			if (discoverCmdText != null) {
				discoverCmd = extractText(discoverCmdText);
			}
			if (periodicMonitorCmdText != null) {
				periodicMonitorCmd = extractText(periodicMonitorCmdText);
			}
			if (periodicMonitorTimeSpinner != null) {
				periodicMonitorTime = periodicMonitorTimeSpinner.getSelection();
			}
			if (continuousMonitorCmdText != null) {
				continuousMonitorCmd = extractText(continuousMonitorCmdText);
			}
			if (remoteInstallPathText != null) {
				remoteInstallPath = extractText(remoteInstallPathText);
			}
		}

		@Override
		protected void copyToFields() {
			if (launchCmdText != null) {
				applyText(launchCmdText, launchCmd);
			}
			if (debugCmdText != null) {
				applyText(debugCmdText, debugCmd);
			}
			if (discoverCmdText != null) {
				applyText(discoverCmdText, discoverCmd);
			}
			if (periodicMonitorCmdText != null) {
				applyText(periodicMonitorCmdText, periodicMonitorCmd);
			}
			if (periodicMonitorTimeSpinner != null) {
				periodicMonitorTimeSpinner.setSelection(periodicMonitorTime);
			}
			if (continuousMonitorCmdText != null) {
				applyText(continuousMonitorCmdText, continuousMonitorCmd);
			}
			if (remoteInstallPathText != null) {
				applyText(remoteInstallPathText, remoteInstallPath);
			}
			defaultCmdButton.setSelection(useToolDefaults);
			defaultInstallButton.setSelection(useInstallDefaults);
		}

		@Override
		protected void copyToStorage() {
			if (launchCmdText != null) {
				config.setLaunchCmd(launchCmd);
			}
			if (debugCmdText != null) {
				config.setDebugCmd(debugCmd);
			}
			if (discoverCmdText != null) {
				config.setDiscoverCmd(discoverCmd);
			}
			if (periodicMonitorCmdText != null) {
				config.setPeriodicMonitorCmd(periodicMonitorCmd);
			}
			if (periodicMonitorTimeSpinner != null) {
				config.setPeriodicMonitorTime(periodicMonitorTime);
			}
			if (continuousMonitorCmdText != null) {
				config.setContinuousMonitorCmd(continuousMonitorCmd);
			}
			if (remoteInstallPathText != null) {
				config.setRemoteInstallPath(remoteInstallPath);
			}
			config.setUseToolDefaults(useToolDefaults);
			config.setUseInstallDefaults(useInstallDefaults);
			config.setCommandsEnabled(commandsEnabled);
		}

		@Override
		protected void loadDefault() {
			// not available
		}

		@Override
		protected void loadFromStorage() {
			if (launchCmdText != null) {
				launchCmd = config.getLaunchCmd();
			}
			if (debugCmdText != null) {
				debugCmd = config.getDebugCmd();
			}
			if (discoverCmdText != null) {
				discoverCmd = config.getDiscoverCmd();
			}
			if (periodicMonitorCmdText != null) {
				periodicMonitorCmd = config.getPeriodicMonitorCmd();
			}
			if (periodicMonitorTimeSpinner != null) {
				periodicMonitorTime = config.getPeriodicMonitorTime();
			}
			if (continuousMonitorCmdText != null) {
				continuousMonitorCmd = config.getContinuousMonitorCmd();
			}
			if (remoteInstallPathText != null) {
				remoteInstallPath = config.getRemoteInstallPath();
			}
			useToolDefaults = config.getUseToolDefaults();
			useInstallDefaults = config.getUseInstallDefaults();
			commandsEnabled = config.getCommandsEnabled();
		}

		@Override
		protected void validateGlobal() throws ValidationException {
			// Nothing yet. Would validate the entire
			// GenericMPIResourceManagerConfiguration.
		}

		@Override
		protected void validateLocal() throws ValidationException {
			if (!useToolDefaults) {
				if (launchCmdText != null && launchCmd == null) {
					throw new ValidationException(Messages.AbstractToolRMConfigurationWizardPage_Validation_MissingLaunchCommand);
				}
				if (debugCmdText != null && debugCmd == null) {
					throw new ValidationException(Messages.AbstractToolRMConfigurationWizardPage_Validation_MissingDebugCommand);
				}
				if (discoverCmdText != null) {
					if (discoverCmd == null) {
						throw new ValidationException(
								Messages.AbstractToolRMConfigurationWizardPage_Validation_MissingDiscoverCommand);
					}
					if (periodicMonitorTimeSpinner != null && periodicMonitorTime < 1) {
						throw new ValidationException(
								Messages.AbstractToolRMConfigurationWizardPage_Validation_InvalidPeriodicMonitorCommandTimeRange);
					}
				}
			}
		}
	}

	protected class WidgetListener extends WizardPageWidgetListener {
		@Override
		protected void doModifyText(ModifyEvent evt) {
			Object source = evt.getSource();
			if (source == launchCmdText || source == debugCmdText || source == discoverCmdText || source == periodicMonitorCmdText
					|| source == continuousMonitorCmdText || source == periodicMonitorTimeSpinner
					|| source == remoteInstallPathText) {
				resetErrorMessages();
				getDataSource().storeAndValidate();
			} else {
				assert false;
			}
		}

		@Override
		protected void doWidgetSelected(SelectionEvent e) {
			boolean enabled = isEnabled();
			disable();
			Object source = e.getSource();
			if (source == browseButton) {
				handlePathBrowseButtonSelected();
			} else if (source == defaultCmdButton) {
				resetErrorMessages();
				if (defaultCmdButton.getSelection()) {
					setToolCommandDefaults();
					getDataSource().justUpdate();
				}
				getDataSource().storeAndValidate();
				updateControls();
			} else if (source == defaultInstallButton) {
				resetErrorMessages();
				if (defaultInstallButton.getSelection()) {
					setInstallPathDefaults();
					getDataSource().justUpdate();
				}
				getDataSource().storeAndValidate();
				updateControls();
			} else {
				assert false;
			}
			setEnabled(enabled);
		}
	}

	// protected boolean listenerEnabled = false;
	public static final String EMPTY_STRING = ""; //$NON-NLS-1$

	private IRemoteServices remoteServices = null;
	private IRemoteConnection connection = null;

	protected Text launchCmdText = null;
	protected Text debugCmdText = null;
	protected Text discoverCmdText = null;
	protected Text continuousMonitorCmdText = null;
	protected Text periodicMonitorCmdText = null;
	protected Spinner periodicMonitorTimeSpinner = null;
	protected Text remoteInstallPathText = null;
	protected Button browseButton = null;
	protected Button defaultCmdButton = null;
	protected Button defaultInstallButton = null;

	protected boolean isEnabled;

	protected int capabilities = IToolRMConfiguration.NO_CAP_SET;

	public AbstractToolRMConfigurationWizardPage(IRMConfigurationWizard wizard, int capabilities, String pageName, String title,
			String description) {
		super(wizard, pageName);
		this.capabilities = capabilities;
		setTitle(title);
		setDescription(description);
	}

	/**
	 * Update wizard UI selections from settings. This should be called whenever
	 * any settings are changed.
	 */
	@Override
	public void updateControls() {
		boolean enabled = ((DataSource) getDataSource()).getCommandsEnabled();

		defaultCmdButton.setEnabled(enabled);

		if (enabled) {
			enabled = !defaultCmdButton.getSelection();
		}

		if (launchCmdText != null) {
			launchCmdText.setEnabled(enabled);
		}
		if (debugCmdText != null) {
			debugCmdText.setEnabled(enabled);
		}
		if (discoverCmdText != null) {
			discoverCmdText.setEnabled(enabled);
		}
		if (periodicMonitorCmdText != null) {
			periodicMonitorCmdText.setEnabled(enabled);
		}
		if (periodicMonitorTimeSpinner != null) {
			periodicMonitorTimeSpinner.setEnabled(enabled);
		}
		if (continuousMonitorCmdText != null) {
			continuousMonitorCmdText.setEnabled(enabled);
		}

		enabled = !defaultInstallButton.getSelection();

		if (remoteInstallPathText != null) {
			remoteInstallPathText.setEnabled(enabled);
		}
	}

	protected void createContents(Composite parent) {
		Composite contents = new Composite(parent, SWT.NONE);
		contents.setLayout(new GridLayout(4, false));
		contents.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Group cmdGroup = new Group(contents, SWT.SHADOW_ETCHED_IN);
		cmdGroup.setLayout(new GridLayout(4, false));
		cmdGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		cmdGroup.setText(Messages.AbstractToolRMConfigurationWizardPage_Label_CommandGroup);

		/*
		 * Default commands button
		 */
		defaultCmdButton = createCheckButton(cmdGroup, Messages.AbstractToolRMConfigurationWizardPage_Label_UseDefaultSettings);
		defaultCmdButton.addSelectionListener(getWidgetListener());
		defaultCmdButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));

		/*
		 * launch cmd
		 */
		if ((capabilities & IToolRMConfiguration.CAP_LAUNCH) != 0) {
			Label label = new Label(cmdGroup, SWT.NONE);
			label.setText(Messages.AbstractToolRMConfigurationWizardPage_Label_LaunchCommand);

			launchCmdText = new Text(cmdGroup, SWT.SINGLE | SWT.BORDER);
			launchCmdText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
			launchCmdText.addModifyListener(getWidgetListener());

			label = new Label(cmdGroup, SWT.NONE);
			label.setText(Messages.AbstractToolRMConfigurationWizardPage_Label_DebugCommand);

			debugCmdText = new Text(cmdGroup, SWT.SINGLE | SWT.BORDER);
			debugCmdText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
			debugCmdText.addModifyListener(getWidgetListener());
		}

		/*
		 * discover cmd
		 */
		if ((capabilities & IToolRMConfiguration.CAP_DISCOVER) != 0) {
			Label label = new Label(cmdGroup, SWT.NONE);
			label.setText(Messages.AbstractToolRMConfigurationWizardPage_Label_DiscoverCommand);

			discoverCmdText = new Text(cmdGroup, SWT.SINGLE | SWT.BORDER);
			discoverCmdText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
			discoverCmdText.addModifyListener(getWidgetListener());
		}

		/*
		 * Periodic monitor cmd and time
		 */
		if ((capabilities & IToolRMConfiguration.CAP_PERIODIC_MONITOR) != 0) {
			Label label = new Label(cmdGroup, SWT.NONE);
			label.setText(Messages.AbstractToolRMConfigurationWizardPage_Label_PeriodicMonitorCommand);

			periodicMonitorCmdText = new Text(cmdGroup, SWT.SINGLE | SWT.BORDER);
			periodicMonitorCmdText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			periodicMonitorCmdText.addModifyListener(getWidgetListener());

			label = new Label(cmdGroup, SWT.NONE);
			label.setText(Messages.AbstractToolRMConfigurationWizardPage_Label_PeriodicMonitorCommandPeriod);

			periodicMonitorTimeSpinner = new Spinner(cmdGroup, SWT.SINGLE | SWT.BORDER);
			periodicMonitorTimeSpinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			periodicMonitorTimeSpinner.addModifyListener(getWidgetListener());
		}

		/*
		 * Continuous monitor cmd
		 */
		if ((capabilities & IToolRMConfiguration.CAP_CONTINUOUS_MONITOR) != 0) {
			Label label = new Label(cmdGroup, SWT.NONE);
			label.setText(Messages.AbstractToolRMConfigurationWizardPage_Label_ContinuousMinitorCommand);

			continuousMonitorCmdText = new Text(cmdGroup, SWT.SINGLE | SWT.BORDER);
			continuousMonitorCmdText.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, 3, 1));
			continuousMonitorCmdText.addModifyListener(getWidgetListener());
		}

		/*
		 * Installation path
		 */
		if ((capabilities & IToolRMConfiguration.CAP_REMOTE_INSTALL_PATH) != 0) {
			Group pathGroup = new Group(contents, SWT.SHADOW_ETCHED_IN);
			pathGroup.setLayout(new GridLayout(4, false));
			pathGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
			pathGroup.setText(Messages.AbstractToolRMConfigurationWizardPage_Label_InstallationGroup);

			defaultInstallButton = createCheckButton(pathGroup,
					Messages.AbstractToolRMConfigurationWizardPage_Label_InstallationDefault);
			defaultInstallButton.addSelectionListener(getWidgetListener());
			defaultInstallButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));

			Label label = new Label(pathGroup, SWT.NONE);
			label.setText(Messages.AbstractToolRMConfigurationWizardPage_Label_InstallationLocation);

			remoteInstallPathText = new Text(pathGroup, SWT.SINGLE | SWT.BORDER);
			remoteInstallPathText.addModifyListener(getWidgetListener());
			remoteInstallPathText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			browseButton = SWTUtil.createPushButton(pathGroup,
					Messages.AbstractToolRMConfigurationWizardPage_Label_InstallationButton, null);
			browseButton.addSelectionListener(getWidgetListener());
			browseButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		}
	}

	@Override
	protected WizardPageDataSource createDataSource() {
		return new DataSource(this);
	}

	@Override
	protected WizardPageWidgetListener createListener() {
		return new WidgetListener();
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
	 * @since 2.0
	 */
	protected void setToolCommandDefaults() {
		// Override if needed
	}

	/**
	 * @since 2.0
	 */
	protected void setInstallPathDefaults() {
		// Override if needed
	}

	/**
	 * Show a dialog that lets the user select a file.
	 */
	protected void handlePathBrowseButtonSelected() {
		/*
		 * Need to do this here because the connection may have been changed by
		 * the previous wizard page
		 */

		IRemoteUIServices remUIServices = null;
		IRemoteResourceManagerConfiguration config = (IRemoteResourceManagerConfiguration) getDataSource().getConfiguration();
		String rmID = config.getRemoteServicesId();
		IWizardContainer container = null;
		if (getControl().isVisible()) {
			container = getWizard().getContainer();
		}
		if (rmID != null) {
			remoteServices = PTPRemoteUIPlugin.getDefault().getRemoteServices(rmID, container);
			String conn = config.getConnectionName();
			if (remoteServices != null && conn != null) {
				connection = remoteServices.getConnectionManager().getConnection(conn);
			}
			remUIServices = PTPRemoteUIPlugin.getDefault().getRemoteUIServices(remoteServices);
		}

		if (remUIServices != null && connection != null) {
			remUIServices.getUIConnectionManager().openConnectionWithProgress(getShell(), container, connection);
			if (connection.isOpen()) {
				IRemoteUIFileManager fileMgr = remUIServices.getUIFileManager();
				fileMgr.setConnection(connection);

				String initialPath = "//"; // Start at root since OMPI is probably installed in the system somewhere //$NON-NLS-1$
				String selectedPath = fileMgr.browseDirectory(getControl().getShell(),
						Messages.AbstractToolRMConfigurationWizardPage_Title_PathSelectionDialog, initialPath,
						IRemoteUIConstants.OPEN);
				if (selectedPath != null) {
					remoteInstallPathText.setText(selectedPath);
				}
			}
		}
	}
}
