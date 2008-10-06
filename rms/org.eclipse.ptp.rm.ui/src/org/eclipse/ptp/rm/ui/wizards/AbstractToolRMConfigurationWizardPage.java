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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.remote.ui.IRemoteUIFileManager;
import org.eclipse.ptp.remote.ui.IRemoteUIServices;
import org.eclipse.ptp.remote.ui.PTPRemoteUIPlugin;
import org.eclipse.ptp.rm.core.rmsystem.AbstractToolRMConfiguration;
import org.eclipse.ptp.rm.remote.core.AbstractRemoteResourceManagerConfiguration;
import org.eclipse.ptp.rm.ui.ToolsRMUIPlugin;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizard;
import org.eclipse.ptp.utils.ui.swt.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
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
public class AbstractToolRMConfigurationWizardPage extends AbstractConfigurationWizardPage {

	//	protected boolean listenerEnabled = false;
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
	protected Button defaultButton = null;

	protected int capabilities = AbstractToolRMConfiguration.NO_CAP_SET;

	protected class WidgetListener extends WizardPageWidgetListener {
		@Override
		protected void doModifyText(ModifyEvent evt) {
			Object source = evt.getSource();
			if(source == launchCmdText ||
					source == debugCmdText ||
					source == discoverCmdText ||
					source == periodicMonitorCmdText ||
					source == continuousMonitorCmdText ||
					source == periodicMonitorTimeSpinner ||
					source == remoteInstallPathText) {
				resetErrorMessages();
				getDataSource().storeAndValidate();
			} else {
				assert false;
			}
		}

		@Override
		protected void doWidgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			if (source == browseButton) {
				handlePathBrowseButtonSelected();
			} else if (source == defaultButton)  {
				resetErrorMessages();
				getDataSource().storeAndValidate();
				updateControls();
			} else {
				assert false;
			}
		}
	}

	protected  class DataSource extends WizardPageDataSource {
		protected DataSource(AbstractConfigurationWizardPage page) {
			super(page);
		}

		private AbstractToolRMConfiguration config = null;

		private boolean useDefaults = false;
		private String launchCmd = null;
		private String debugCmd = null;
		private String discoverCmd = null;
		private String periodicMonitorCmd = null;
		private int periodicMonitorTime = 0;
		private String continuousMonitorCmd = null;
		private String remoteInstallPath = null;

		public void setCommandFields(String launchCmd, String debugCmd, String discoverCmd, String periodicMonitorCmd, int periodicMonitorTime, String continuousMonitorCmd, String remoteInstallPath) {
			this.launchCmd = launchCmd;
			this.debugCmd = debugCmd;
			this.discoverCmd = discoverCmd;
			this.periodicMonitorCmd = periodicMonitorCmd;
			this.periodicMonitorTime = periodicMonitorTime;
			this.continuousMonitorCmd = continuousMonitorCmd;
			this.remoteInstallPath = remoteInstallPath;
		}

		public boolean isUseDefaults() {
			return useDefaults;
		}

		public String getLaunchCmd() {
			return launchCmd;
		}

		public String getDebugCmd() {
			return debugCmd;
		}

		public String getDiscoverCmd() {
			return discoverCmd;
		}

		public String getPeriodicMonitorCmd() {
			return periodicMonitorCmd;
		}

		public int getPeriodicMonitorTime() {
			return periodicMonitorTime;
		}

		public String getContinuousMonitorCmd() {
			return continuousMonitorCmd;
		}

		public String getRemoteInstallPath() {
			return remoteInstallPath;
		}

		@Override
		protected void copyFromFields() throws ValidationException {
			useDefaults = defaultButton.getSelection();

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
		protected void validateLocal() throws ValidationException {
			if (! useDefaults) {
				if (launchCmdText != null && launchCmd == null)
					throw new ValidationException(Messages.AbstractToolRMConfigurationWizardPage_Validation_MissingLaunchCommand);
				if (debugCmdText != null && debugCmd == null)
					throw new ValidationException(Messages.AbstractToolRMConfigurationWizardPage_Validation_MissingDebugCommand);
				if (discoverCmdText != null) {
					if (discoverCmd == null)
						throw new ValidationException(Messages.AbstractToolRMConfigurationWizardPage_Validation_MissingDiscoverCommand);
					if (periodicMonitorTimeSpinner != null && periodicMonitorTime < 1)
						throw new ValidationException(Messages.AbstractToolRMConfigurationWizardPage_Validation_InvalidPeriodicMonitorCommandTimeRange);
				}
			}
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
			config.setUseDefaults(useDefaults);
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
			// Hack, since "Use default" is not yet implement, always assign true
			// useDefaults = config.useDefaults();
			useDefaults = true;
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
			defaultButton.setSelection(useDefaults);
		}

		@Override
		protected void validateGlobal() throws ValidationException {
			// Nothing yet. Would validate the entire GenericMPIResourceManagerConfiguration.
		}

		@Override
		public void setConfig(IResourceManagerConfiguration configuration) {
			super.setConfig(config);
			// Store a local reference to the configuration
			this.config = (AbstractToolRMConfiguration) configuration;
		}

		@Override
		protected void loadDefault() {
			// not available
		}
	}

	public AbstractToolRMConfigurationWizardPage(RMConfigurationWizard wizard, int capabilities, String pageName, String title, String description) {
		super(wizard, pageName);
		this.capabilities = capabilities;
		setTitle(title);
		setDescription(description);
	}

	@Override
	protected WizardPageWidgetListener createListener() {
		return new WidgetListener();
	}

	@Override
	protected WizardPageDataSource createDataSource() {
		return new DataSource(this);
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

		createOpenMPIContents(contents);

		return contents;
	}

	protected void createOpenMPIContents(Composite parent) {
		Composite contents = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		contents.setLayout(layout);

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		contents.setLayoutData(gd);

		/*
		 * Default button
		 */
		defaultButton = createCheckButton(contents, Messages.AbstractToolRMConfigurationWizardPage_Label_UseDefaultSettings);
		defaultButton.addSelectionListener(getWidgetListener());
		defaultButton.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, 4, 1));

		/*
		 * launch cmd
		 */
		if ((capabilities & AbstractToolRMConfiguration.CAP_LAUNCH) != 0) {
			Label label = new Label(contents, SWT.NONE);
			label.setText(Messages.AbstractToolRMConfigurationWizardPage_Label_LaunchCommand);

			launchCmdText = new Text(contents, SWT.SINGLE | SWT.BORDER);
			launchCmdText.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, 3, 1));
			launchCmdText.addModifyListener(getWidgetListener());

			label = new Label(contents, SWT.NONE);
			label.setText(Messages.AbstractToolRMConfigurationWizardPage_Label_DebugCommand);

			debugCmdText = new Text(contents, SWT.SINGLE | SWT.BORDER);
			debugCmdText.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, 3, 1));
			debugCmdText.addModifyListener(getWidgetListener());
		}

		/*
		 * discover cmd
		 */
		if ((capabilities & AbstractToolRMConfiguration.CAP_DISCOVER) != 0) {
			Label label = new Label(contents, SWT.NONE);
			label.setText(Messages.AbstractToolRMConfigurationWizardPage_Label_DiscoverCommand);

			discoverCmdText = new Text(contents, SWT.SINGLE | SWT.BORDER);
			discoverCmdText.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, 3, 1));
			discoverCmdText.addModifyListener(getWidgetListener());
		}

		/*
		 * Periodic monitor cmd and time
		 */
		if ((capabilities & AbstractToolRMConfiguration.CAP_PERIODIC_MONITOR) != 0) {
			Label label = new Label(contents, SWT.NONE);
			label.setText(Messages.AbstractToolRMConfigurationWizardPage_Label_PeriodicMonitorCommand);

			periodicMonitorCmdText = new Text(contents, SWT.SINGLE | SWT.BORDER);
			periodicMonitorCmdText.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, 1, 1));
			periodicMonitorCmdText.addModifyListener(getWidgetListener());

			label = new Label(contents, SWT.NONE);
			label.setText(Messages.AbstractToolRMConfigurationWizardPage_Label_PeriodicMonitorCommandPeriod);
			periodicMonitorTimeSpinner = new Spinner(contents, SWT.SINGLE | SWT.BORDER);
			periodicMonitorCmdText.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, false, false, 1, 1));
			periodicMonitorTimeSpinner.addModifyListener(getWidgetListener());
		}

		/*
		 * Continuous monitor cmd
		 */
		if ((capabilities & AbstractToolRMConfiguration.CAP_CONTINUOUS_MONITOR) != 0) {
			Label label = new Label(contents, SWT.NONE);
			label.setText(Messages.AbstractToolRMConfigurationWizardPage_Label_ContinuousMinitorCommand);

			continuousMonitorCmdText = new Text(contents, SWT.SINGLE | SWT.BORDER);
			continuousMonitorCmdText.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, 3, 1));
			continuousMonitorCmdText.addModifyListener(getWidgetListener());
		}

		/*
		 * Installation path
		 */
		if ((capabilities & AbstractToolRMConfiguration.CAP_REMOTE_INSTALL_PATH) != 0) {
			Label label = new Label(contents, SWT.NONE);
			label.setText(Messages.AbstractToolRMConfigurationWizardPage_Label_PathInstallation);

			remoteInstallPathText = new Text(contents, SWT.SINGLE | SWT.BORDER);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			gd.widthHint = 60;
			remoteInstallPathText.setLayoutData(gd);
			remoteInstallPathText.addModifyListener(getWidgetListener());

			browseButton = SWTUtil.createPushButton(contents, "Browse", null); //$NON-NLS-1$
			browseButton.addSelectionListener(getWidgetListener());
		}
	}

	/**
	 * Show a dialog that lets the user select a file.
	 */
	protected void handlePathBrowseButtonSelected() {
		/*
		 * Need to do this here because the connection may have been changed
		 * by the previous wizard page
		 */

		IRemoteUIServices remUIServices = null;
		AbstractRemoteResourceManagerConfiguration config = (AbstractRemoteResourceManagerConfiguration)getDataSource().getConfig();
		String rmID = config.getRemoteServicesId();
		if (rmID != null) {
			remoteServices = PTPRemoteCorePlugin.getDefault().getRemoteServices(rmID);
			String conn = config.getConnectionName();
			if (remoteServices != null && conn != null) {
				connection = remoteServices.getConnectionManager().getConnection(conn);
			}
			remUIServices = PTPRemoteUIPlugin.getDefault().getRemoteUIServices(remoteServices);
		}

		if (remUIServices != null && connection != null) {
			if (!connection.isOpen()) {
				IRunnableWithProgress op = new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor)
					throws InvocationTargetException,
					InterruptedException {
						try {
							connection.open(monitor);
						} catch (RemoteConnectionException e) {
							ErrorDialog.openError(getShell(), Messages.AbstractToolRMConfigurationWizardPage_Exception_ConnectionError,
									Messages.AbstractToolRMConfigurationWizardPage_Exception_ConnectionErrorDescription,
									new Status(IStatus.ERROR, ToolsRMUIPlugin.PLUGIN_ID, e.getMessage()));
						}
					}

				};
				try {
					new ProgressMonitorDialog(getShell()).run(true, true, op);
				} catch (InvocationTargetException e) {
					ErrorDialog.openError(getShell(), Messages.AbstractToolRMConfigurationWizardPage_Exception_ConnectionError,
							Messages.AbstractToolRMConfigurationWizardPage_Exception_ConnectionErrorDescription,
							new Status(IStatus.ERROR, ToolsRMUIPlugin.PLUGIN_ID, e.getMessage()));
				} catch (InterruptedException e) {
					ErrorDialog.openError(getShell(), Messages.AbstractToolRMConfigurationWizardPage_Exception_ConnectionError,
							Messages.AbstractToolRMConfigurationWizardPage_Exception_ConnectionErrorDescription,
							new Status(IStatus.ERROR, ToolsRMUIPlugin.PLUGIN_ID, e.getMessage()));
				}
			}
			IRemoteUIFileManager fileMgr = remUIServices.getUIFileManager();
			fileMgr.setConnection(connection);

			String initialPath = "//"; // Start at root since OMPI is probably installed in the system somewhere //$NON-NLS-1$
			IPath selectedPath = fileMgr.browseFile(getControl().getShell(), Messages.AbstractToolRMConfigurationWizardPage_Title_PathSelectionDialog, initialPath);
			if (selectedPath != null) {
				remoteInstallPathText.setText(selectedPath.toString());
			}
		}
	}

	/**
	 * Update wizard UI selections from settings. This should be called whenever any
	 * settings are changed.
	 */
	@Override
	protected void updateControls() {
		// Hack, since "Use default" is not yet implement, always leave it disabled.
		defaultButton.setEnabled(false);

		boolean enabled = ! defaultButton.getSelection();

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
		if (remoteInstallPathText != null) {
			remoteInstallPathText.setEnabled(enabled);
		}
		if (browseButton != null) {
			browseButton.setEnabled(enabled);
		}
	}
}
