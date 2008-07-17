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
import org.eclipse.ptp.remote.IRemoteConnection;
import org.eclipse.ptp.remote.IRemoteFileManager;
import org.eclipse.ptp.remote.IRemoteServices;
import org.eclipse.ptp.remote.PTPRemotePlugin;
import org.eclipse.ptp.remote.exception.RemoteConnectionException;
import org.eclipse.ptp.rm.core.rmsystem.AbstractToolRMConfiguration;
import org.eclipse.ptp.rm.remote.core.AbstractRemoteResourceManagerConfiguration;
import org.eclipse.ptp.rm.ui.Activator;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.ui.utils.SWTUtil;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizard;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
public class AbstractToolRMConfigurationWizardPage extends RMConfigurationWizardPage {

	protected boolean listenerEnabled = false;
	public static final String EMPTY_STRING = ""; //$NON-NLS-1$

	private IRemoteServices remoteServices = null;
	private IRemoteConnection connection = null;

	protected Text launchCmdText = null;
	protected Text discoverCmdText = null;
	protected Text continuousMonitorCmdText = null;
	protected Text periodicMonitorCmdText = null;
	protected Spinner periodicMonitorTimeSpinner = null;
	protected Text remoteInstallPathText = null;
	protected Button browseButton = null;
	protected Button defaultButton = null;

	protected WizardListener listener = createListener();
	protected DataSource dataSource = createDataSource();

	protected int capabilities = AbstractToolRMConfiguration.NO_CAP_SET;

	protected interface WizardListener extends ModifyListener, SelectionListener {
	}

	protected class WidgetListener implements WizardListener {
		final public void modifyText(ModifyEvent evt) {
			if (! listenerEnabled) return;
			doModifyText(evt);
		}

		protected void doModifyText(ModifyEvent evt) {
			Object source = evt.getSource();
			if(source == launchCmdText ||
					source == discoverCmdText ||
					source == periodicMonitorCmdText ||
					source == continuousMonitorCmdText ||
					source == periodicMonitorTimeSpinner ||
					source == remoteInstallPathText) {
				resetErrorStatus();
				dataSource.updateAndStore();
			} else {
				assert false;
			}
		}

		final public void widgetSelected(SelectionEvent e) {
			if (! listenerEnabled) return;

			doWidgetSelected(e);
		}

		protected void doWidgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			if (source == browseButton) {
				handlePathBrowseButtonSelected();
			} else if (source == defaultButton)  {
				resetErrorStatus();
				dataSource.updateAndStore();
				updateControls();
			} else {
				assert false;
			}
		}

		final public void widgetDefaultSelected(SelectionEvent e) {
			// Empty.
		}
	}

	protected WizardListener createListener() {
		return new WidgetListener();
	}

	protected DataSource createDataSource() {
		return new DataSource();
	}

	protected  class DataSource {
		protected class ValidationException extends Exception {
			private static final long serialVersionUID = 1L;

			public ValidationException(String message) {
				super(message);
			}
		}

		private AbstractToolRMConfiguration config = null;

		private boolean useDefaults = false;
		private String launchCmd = null;
		private String discoverCmd = null;
		private String periodicMonitorCmd = null;
		private int periodicMonitorTime = 0;
		private String continuousMonitorCmd = null;
		private String remoteInstallPath = null;

		public void setCommandFields(String launchCmd, String discoverCmd, String periodicMonitorCmd, int periodicMonitorTime, String continuousMonitorCmd, String remoteInstallPath) {
			this.launchCmd = launchCmd;
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

		protected String extractText(Text text) {
			String s = text.getText().trim();
			return (s.length() == 0 ? null : s);
		}

		protected void applyText(Text t, String s) {
			if (s == null) t.setText(EMPTY_STRING);
			else t.setText(s);
		}

		protected void copyFromFields() throws ValidationException {
			useDefaults = defaultButton.getSelection();

			if (launchCmdText != null)
				launchCmd = extractText(launchCmdText);
			if (discoverCmdText != null)
				discoverCmd = extractText(discoverCmdText);
			if (periodicMonitorCmdText != null)
				periodicMonitorCmd = extractText(periodicMonitorCmdText);
			if (periodicMonitorTimeSpinner != null)
				periodicMonitorTime = periodicMonitorTimeSpinner.getSelection();
			if (continuousMonitorCmdText != null)
				continuousMonitorCmd = extractText(continuousMonitorCmdText);
			if (remoteInstallPathText != null)
				remoteInstallPath = extractText(remoteInstallPathText);
		}

		protected void validateLocal() throws ValidationException {
			if (! useDefaults) {
				if (launchCmdText != null && launchCmd == null) {
					throw new ValidationException("Launch command is missing");
				}
				if (discoverCmdText != null) {
					if (discoverCmd == null) {
						throw new ValidationException("Discover command is missing");
					}
					if (periodicMonitorTimeSpinner != null && periodicMonitorTime < 1) {
						throw new ValidationException("Time period must be an integer greater than 0");
					}
				}
			}
		}

		protected void storeConfig() {
			if (launchCmdText != null)
				config.setLaunchCmd(launchCmd);
			if (discoverCmdText != null)
				config.setDiscoverCmd(discoverCmd);
			if (periodicMonitorCmdText != null)
				config.setPeriodicMonitorCmd(periodicMonitorCmd);
			if (periodicMonitorTimeSpinner != null)
				config.setPeriodicMonitorTime(periodicMonitorTime);
			if (continuousMonitorCmdText != null)
				config.setContinuousMonitorCmd(continuousMonitorCmd);
			if (remoteInstallPathText != null)
				config.setRemoteInstallPath(remoteInstallPath);
			config.setUseDefaults(useDefaults);
		}

		protected void loadConfig() {
			if (launchCmdText != null)
				launchCmd = config.getLaunchCmd();
			if (discoverCmdText != null)
				discoverCmd = config.getDiscoverCmd();
			if (periodicMonitorCmdText != null)
				periodicMonitorCmd = config.getPeriodicMonitorCmd();
			if (periodicMonitorTimeSpinner != null)
				periodicMonitorTime = config.getPeriodicMonitorTime();
			if (continuousMonitorCmdText != null)
				continuousMonitorCmd = config.getContinuousMonitorCmd();
			if (remoteInstallPathText != null)
				remoteInstallPath = config.getRemoteInstallPath();
			useDefaults = config.useDefaults();
		}

		protected void copyToFields() {
			if (launchCmdText != null)
				applyText(launchCmdText, launchCmd);
			if (discoverCmdText != null)
				applyText(discoverCmdText, discoverCmd);
			if (periodicMonitorCmdText != null)
				applyText(periodicMonitorCmdText, periodicMonitorCmd);
			if (periodicMonitorTimeSpinner != null)
				periodicMonitorTimeSpinner.setSelection(periodicMonitorTime);
			if (continuousMonitorCmdText != null)
				applyText(continuousMonitorCmdText, continuousMonitorCmd);
			if (remoteInstallPathText != null)
				applyText(remoteInstallPathText, remoteInstallPath);
			defaultButton.setSelection(useDefaults);
		}

		protected void validateGlobal() throws ValidationException {
			// Nothing yet. Would validate the entire GenericMPIResourceManagerConfiguration.
		}

		public void getFromFields() {
			try {
				copyFromFields();
				validateLocal();
			} catch (ValidationException e) {
				setErrorMessage(e.getLocalizedMessage());
				setPageComplete(false);
			}
		}

		public void putToFields() {
			copyToFields();
			try {
				validateLocal();
			} catch (ValidationException e) {
				setErrorMessage(e.getLocalizedMessage());
				setPageComplete(false);
			}
		}

		public void updateAndStore() {
			try {
				copyFromFields();
				validateLocal();
				validateGlobal();
				storeConfig();
			} catch (ValidationException e) {
				setErrorMessage(e.getLocalizedMessage());
				setPageComplete(false);
			}
		}

		public void loadAndUpdate() {
			loadConfig();
			copyToFields();
			try {
				validateLocal();
				validateGlobal();
			} catch (ValidationException e) {
				setErrorMessage(e.getLocalizedMessage());
				setPageComplete(false);
			}
		}

		public void setConfig(IResourceManagerConfiguration configuration) {
			config = (AbstractToolRMConfiguration) configuration;
		}

		public AbstractToolRMConfiguration getConfig() {
			return config;
		}
	}

	public AbstractToolRMConfigurationWizardPage(RMConfigurationWizard wizard, int capabilities, String pageName, String title, String description) {
		super(wizard, pageName);
		this.capabilities = capabilities;
		setTitle(title);
		setDescription(description);
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout topLayout = new GridLayout();
	    composite.setLayout(topLayout);
		createContents(composite);
		setControl(composite);
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			resetErrorStatus();

			listenerEnabled = false;
			dataSource.setConfig(getConfigurationWizard().getConfiguration());
			dataSource.loadAndUpdate();
			listenerEnabled = true;
			updateControls();
		}
		super.setVisible(visible);
	}

	protected void createContents(Composite parent) {
		createOpenMpiContests(parent);
	}

	protected void createOpenMpiContests(Composite parent) {
		Composite contents = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		contents.setLayout(layout);

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		contents.setLayoutData(gd);

		/*
		 * Default button
		 */
		defaultButton = createCheckButton(contents, "Use default settings");
		defaultButton.addSelectionListener(listener);
		defaultButton.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, 4, 1));

		/*
		 * launch cmd
		 */
		if ((capabilities & AbstractToolRMConfiguration.CAP_LAUNCH) != 0) {
			Label label = new Label(contents, SWT.NONE);
			label.setText("Launch command:");

			launchCmdText = new Text(contents, SWT.SINGLE | SWT.BORDER);
			launchCmdText.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, 3, 1));
			launchCmdText.addModifyListener(listener);
		}

		/*
		 * discover cmd
		 */
		if ((capabilities & AbstractToolRMConfiguration.CAP_DISCOVER) != 0) {
			Label label = new Label(contents, SWT.NONE);
			label.setText("Discover command:");

			discoverCmdText = new Text(contents, SWT.SINGLE | SWT.BORDER);
			discoverCmdText.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, 3, 1));
			discoverCmdText.addModifyListener(listener);
		}

		/*
		 * Periodic monitor cmd and time
		 */
		if ((capabilities & AbstractToolRMConfiguration.CAP_PERIODIC_MONITOR) != 0) {
			Label label = new Label(contents, SWT.NONE);
			label.setText("Monitor command:");

			periodicMonitorCmdText = new Text(contents, SWT.SINGLE | SWT.BORDER);
			periodicMonitorCmdText.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, 1, 1));
			periodicMonitorCmdText.addModifyListener(listener);

			label = new Label(contents, SWT.NONE);
			label.setText("Period:");
			periodicMonitorTimeSpinner = new Spinner(contents, SWT.SINGLE | SWT.BORDER);
			periodicMonitorCmdText.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, false, false, 1, 1));
			periodicMonitorTimeSpinner.addModifyListener(listener);
		}

		/*
		 * Continuous monitor cmd
		 */
		if ((capabilities & AbstractToolRMConfiguration.CAP_CONTINUOUS_MONITOR) != 0) {
			Label label = new Label(contents, SWT.NONE);
			label.setText("Continuous monitor command:");

			continuousMonitorCmdText = new Text(contents, SWT.SINGLE | SWT.BORDER);
			continuousMonitorCmdText.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, 3, 1));
			continuousMonitorCmdText.addModifyListener(listener);
		}

		/*
		 * Installation path
		 */
		if ((capabilities & AbstractToolRMConfiguration.CAP_REMOTE_INSTALL_PATH) != 0) {
			Label label = new Label(contents, SWT.NONE);
			label.setText("Path to Open MPI Installation:");

			remoteInstallPathText = new Text(contents, SWT.SINGLE | SWT.BORDER);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			gd.widthHint = 60;
			remoteInstallPathText.setLayoutData(gd);
			remoteInstallPathText.addModifyListener(listener);

			browseButton = SWTUtil.createPushButton(contents, "Browse", null); //$NON-NLS-1$
			browseButton.addSelectionListener(listener);
		}
	}

	/**
	 * Update wizard UI selections from settings. This should be called whenever any
	 * settings are changed.
	 */
	protected void updateControls() {
		boolean enabled = ! defaultButton.getSelection();

		if (launchCmdText != null)
			launchCmdText.setEnabled(enabled);
		if (discoverCmdText != null)
			discoverCmdText.setEnabled(enabled);
		if (periodicMonitorCmdText != null)
			periodicMonitorCmdText.setEnabled(enabled);
		if (periodicMonitorTimeSpinner != null)
			periodicMonitorTimeSpinner.setEnabled(enabled);
		if (continuousMonitorCmdText != null)
			continuousMonitorCmdText.setEnabled(enabled);
		if (remoteInstallPathText != null)
			remoteInstallPathText.setEnabled(enabled);
		if (browseButton != null)
			browseButton.setEnabled(enabled);
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
	 * Show a dialog that lets the user select a file.
	 */
	protected void handlePathBrowseButtonSelected() {
		/*
		 * Need to do this here because the connection may have been changed
		 * by the previous wizard page
		 */

		AbstractRemoteResourceManagerConfiguration config = (AbstractRemoteResourceManagerConfiguration)dataSource.getConfig();
		String rmID = config.getRemoteServicesId();
		if (rmID != null) {
			remoteServices = PTPRemotePlugin.getDefault().getRemoteServices(rmID);
			String conn = config.getConnectionName();
			if (remoteServices != null && conn != null) {
				connection = remoteServices.getConnectionManager().getConnection(conn);
			}
		}

		if (connection != null) {
			if (!connection.isOpen()) {
				IRunnableWithProgress op = new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor)
							throws InvocationTargetException,
							InterruptedException {
						try {
							connection.open(monitor);
						} catch (RemoteConnectionException e) {
							ErrorDialog.openError(getShell(), "Connection Error",
									"Could not open connection",
									new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
						}
					}

				};
				try {
					new ProgressMonitorDialog(getShell()).run(true, true, op);
				} catch (InvocationTargetException e) {
					ErrorDialog.openError(getShell(), "Connection Error",
							"Could not open connection",
							new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
				} catch (InterruptedException e) {
					ErrorDialog.openError(getShell(), "Connection Error",
							"Could not open connection",
							new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
				}
			}
			IRemoteFileManager fileMgr = remoteServices.getFileManager(connection);

			String initialPath = "//"; // Start at root since OMPI is probably installed in the system somewhere
			IPath selectedPath = fileMgr.browseFile(getControl().getShell(), "Select path to Open MPI installation", initialPath);
			if (selectedPath != null) {
				remoteInstallPathText.setText(selectedPath.toString());
			}
		}
	}

	protected void resetErrorStatus() {
		setPageComplete(true);
		setErrorMessage(null);
		setMessage(null);
	}
}
