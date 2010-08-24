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
package org.eclipse.ptp.rm.ui.preferences;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.rm.core.AbstractToolsPreferenceManager;
import org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration;
import org.eclipse.ptp.rm.ui.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/*
 * TODO: make this class expansible for preference pages for tools.
 * One might want to extend this class and add more fields.
 * - Make listener extensible
 * - Make data source extensible
 * - Make createContents extensible
 */
public abstract class AbstractToolsPreferencePage extends AbstractPreferencePage implements IWorkbenchPreferencePage {

	class DataSource extends PreferenceDataSource {
		String launchCmd = null;
		String debugCmd = null;
		String discoverCmd = null;
		String periodicMonitorCmd = null;
		int periodicMonitorTime = 0;
		String continuousMonitorCmd = null;
		String remoteInstallPath = null;

		DataSource(AbstractPreferencePage page) {
			super(page);
		}

		@Override
		protected void copyFromFields() throws ValidationException {
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
		}

		@Override
		protected void copyToStorage() {
			if (launchCmdText != null) {
				Preferences.setString(getQualifier(), AbstractToolsPreferenceManager.PREFS_LAUNCH_CMD, toPreference(launchCmd));
			}
			if (debugCmdText != null) {
				Preferences.setString(getQualifier(), AbstractToolsPreferenceManager.PREFS_DEBUG_CMD, toPreference(debugCmd));
			}
			if (discoverCmdText != null) {
				Preferences.setString(getQualifier(), AbstractToolsPreferenceManager.PREFS_DISCOVER_CMD, toPreference(discoverCmd));
			}
			if (periodicMonitorCmdText != null) {
				Preferences.setString(getQualifier(), AbstractToolsPreferenceManager.PREFS_PERIODIC_MONITOR_CMD,
						toPreference(periodicMonitorCmd));
			}
			if (periodicMonitorTimeSpinner != null) {
				Preferences.setInt(getQualifier(), AbstractToolsPreferenceManager.PREFS_PERIODIC_MONITOR_TIME, periodicMonitorTime);
			}
			if (continuousMonitorCmdText != null) {
				Preferences.setString(getQualifier(), AbstractToolsPreferenceManager.PREFS_CONTINUOUS_MONITOR_CMD,
						toPreference(continuousMonitorCmd));
			}
			if (remoteInstallPathText != null) {
				Preferences.setString(getQualifier(), AbstractToolsPreferenceManager.PREFS_REMOTE_INSTALL_PATH,
						toPreference(remoteInstallPath));
			}
			savePreferences();
		}

		@Override
		protected void loadDefault() {
			if (launchCmdText != null) {
				launchCmd = fromPreference(Preferences.getDefaultString(getQualifier(),
						AbstractToolsPreferenceManager.PREFS_LAUNCH_CMD, "")); //$NON-NLS-1$
			}
			if (debugCmdText != null) {
				debugCmd = fromPreference(Preferences.getDefaultString(getQualifier(),
						AbstractToolsPreferenceManager.PREFS_DEBUG_CMD, "")); //$NON-NLS-1$
			}
			if (discoverCmdText != null) {
				discoverCmd = fromPreference(Preferences.getDefaultString(getQualifier(),
						AbstractToolsPreferenceManager.PREFS_DISCOVER_CMD, "")); //$NON-NLS-1$
			}
			if (periodicMonitorCmdText != null) {
				periodicMonitorCmd = fromPreference(Preferences.getDefaultString(getQualifier(),
						AbstractToolsPreferenceManager.PREFS_PERIODIC_MONITOR_CMD, "")); //$NON-NLS-1$
			}
			if (periodicMonitorTimeSpinner != null) {
				periodicMonitorTime = Preferences.getDefaultInt(getQualifier(),
						AbstractToolsPreferenceManager.PREFS_PERIODIC_MONITOR_TIME, 0);
			}
			if (continuousMonitorCmdText != null) {
				continuousMonitorCmd = fromPreference(Preferences.getDefaultString(getQualifier(),
						AbstractToolsPreferenceManager.PREFS_CONTINUOUS_MONITOR_CMD, "")); //$NON-NLS-1$
			}
			if (remoteInstallPathText != null) {
				remoteInstallPath = fromPreference(Preferences.getDefaultString(getQualifier(),
						AbstractToolsPreferenceManager.PREFS_REMOTE_INSTALL_PATH, "")); //$NON-NLS-1$
			}
		}

		@Override
		protected void loadFromStorage() {
			if (launchCmdText != null) {
				launchCmd = fromPreference(Preferences.getString(getQualifier(), AbstractToolsPreferenceManager.PREFS_LAUNCH_CMD));
			}
			if (debugCmdText != null) {
				debugCmd = fromPreference(Preferences.getString(getQualifier(), AbstractToolsPreferenceManager.PREFS_DEBUG_CMD));
			}
			if (discoverCmdText != null) {
				discoverCmd = fromPreference(Preferences.getString(getQualifier(),
						AbstractToolsPreferenceManager.PREFS_DISCOVER_CMD));
			}
			if (periodicMonitorCmdText != null) {
				periodicMonitorCmd = fromPreference(Preferences.getString(getQualifier(),
						AbstractToolsPreferenceManager.PREFS_PERIODIC_MONITOR_CMD));
			}
			if (periodicMonitorTimeSpinner != null) {
				periodicMonitorTime = Preferences
						.getInt(getQualifier(), AbstractToolsPreferenceManager.PREFS_PERIODIC_MONITOR_TIME);
			}
			if (continuousMonitorCmdText != null) {
				continuousMonitorCmd = fromPreference(Preferences.getString(getQualifier(),
						AbstractToolsPreferenceManager.PREFS_CONTINUOUS_MONITOR_CMD));
			}
			if (remoteInstallPathText != null) {
				remoteInstallPath = fromPreference(Preferences.getString(getQualifier(),
						AbstractToolsPreferenceManager.PREFS_REMOTE_INSTALL_PATH));
			}
		}

		@Override
		protected void validateLocal() throws ValidationException {
			if (launchCmdText != null && launchCmd == null) {
				throw new ValidationException(Messages.AbstractToolsPreferencePage_Validation_MissingLaunchCommand);
			}
			if (debugCmdText != null && debugCmd == null) {
				throw new ValidationException(Messages.AbstractToolsPreferencePage_Validation_MissingMissingDebugCommand);
			}
			if (discoverCmdText != null) {
				if (discoverCmd == null) {
					throw new ValidationException(Messages.AbstractToolsPreferencePage_Validation_MissingDiscoverCommand);
				}
			}
			if (periodicMonitorCmdText != null) {
				// if (periodicMonitorCmd == null) {
				// throw new
				// ValidationException("Periodic monitor command is missing");
				// }
				if (periodicMonitorTimeSpinner != null && periodicMonitorTime < 1) {
					throw new ValidationException(
							Messages.AbstractToolsPreferencePage_Validation_InvalidPeriodicMonitorCommandTimeRange);
				}
			}
		}
	}

	class WidgetListener extends PreferenceWidgetListener implements ModifyListener {
		@Override
		public void doModifyText(ModifyEvent evt) {
			Object source = evt.getSource();
			if (source == launchCmdText || source == debugCmdText || source == discoverCmdText || source == periodicMonitorCmdText
					|| source == continuousMonitorCmdText || source == periodicMonitorTimeSpinner
					|| source == remoteInstallPathText) {
				resetErrorMessages();
				getDataSource().justValidate();
			} else {
				assert false;
			}
		}
	}

	public static final String EMPTY_STRING = ""; //$NON-NLS-1$

	/**
	 * @since 2.0
	 */
	protected Text launchCmdText = null;
	/**
	 * @since 2.0
	 */
	protected Text debugCmdText = null;
	/**
	 * @since 2.0
	 */
	protected Text discoverCmdText = null;
	/**
	 * @since 2.0
	 */
	protected Text continuousMonitorCmdText = null;
	/**
	 * @since 2.0
	 */
	protected Text periodicMonitorCmdText = null;
	/**
	 * @since 2.0
	 */
	protected Spinner periodicMonitorTimeSpinner = null;
	/**
	 * @since 2.0
	 */
	protected Text remoteInstallPathText = null;

	private int fCapabilities = IToolRMConfiguration.NO_CAP_SET;
	private final String fQualifier; // to get preferences

	public AbstractToolsPreferencePage(String qualifier, int capabilities) {
		super();
		fCapabilities = capabilities;
		fQualifier = qualifier;
	}

	public AbstractToolsPreferencePage(String qualifier, int capabilities, String title) {
		super(title);
		fCapabilities = capabilities;
		fQualifier = qualifier;
	}

	public AbstractToolsPreferencePage(String qualifier, int capabilities, String title, ImageDescriptor image) {
		super(title, image);
		fCapabilities = capabilities;
		fQualifier = qualifier;
	}

	public void init(IWorkbench workbench) {
		// Nothing to do.
	}

	@Override
	protected PreferenceDataSource createDataSource() {
		return new DataSource(this);
	}

	@Override
	protected PreferenceWidgetListener createListener() {
		return new WidgetListener();
	}

	@Override
	protected Composite doCreateContents(Composite parent) {
		Composite contents = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		contents.setLayout(layout);

		/*
		 * launch cmd
		 */
		if ((fCapabilities & IToolRMConfiguration.CAP_LAUNCH) != 0) {
			Label label = new Label(contents, SWT.NONE);
			label.setText(Messages.AbstractToolsPreferencePage_Label_LaunchCommand);

			launchCmdText = new Text(contents, SWT.SINGLE | SWT.BORDER);
			launchCmdText.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, 3, 1));
			launchCmdText.addModifyListener(getListener());

			label = new Label(contents, SWT.NONE);
			label.setText(Messages.AbstractToolsPreferencePage_Label_DebugCommand);

			debugCmdText = new Text(contents, SWT.SINGLE | SWT.BORDER);
			debugCmdText.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, 3, 1));
			debugCmdText.addModifyListener(getListener());
		}

		/*
		 * discover cmd
		 */
		if ((fCapabilities & IToolRMConfiguration.CAP_DISCOVER) != 0) {
			Label label = new Label(contents, SWT.NONE);
			label.setText(Messages.AbstractToolsPreferencePage_Label_DiscoverCommand);

			discoverCmdText = new Text(contents, SWT.SINGLE | SWT.BORDER);
			discoverCmdText.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, 3, 1));
			discoverCmdText.addModifyListener(getListener());
		}

		/*
		 * Periodic monitor cmd and time
		 */
		if ((fCapabilities & IToolRMConfiguration.CAP_PERIODIC_MONITOR) != 0) {
			Label label = new Label(contents, SWT.NONE);
			label.setText(Messages.AbstractToolsPreferencePage_Label_PeriodicMonitorCommand);

			periodicMonitorCmdText = new Text(contents, SWT.SINGLE | SWT.BORDER);
			periodicMonitorCmdText.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, 1, 1));
			periodicMonitorCmdText.addModifyListener(getListener());

			label = new Label(contents, SWT.NONE);
			label.setText(Messages.AbstractToolsPreferencePage_Label_PeriodicMonitorCommandPeriod);
			periodicMonitorTimeSpinner = new Spinner(contents, SWT.SINGLE | SWT.BORDER);
			periodicMonitorCmdText.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, false, false, 1, 1));
			periodicMonitorTimeSpinner.addModifyListener(getListener());
		}

		/*
		 * Continuous monitor cmd
		 */
		if ((fCapabilities & IToolRMConfiguration.CAP_CONTINUOUS_MONITOR) != 0) {
			Label label = new Label(contents, SWT.NONE);
			label.setText(Messages.AbstractToolsPreferencePage_Label_ContinuosMonitorCommand);

			continuousMonitorCmdText = new Text(contents, SWT.SINGLE | SWT.BORDER);
			continuousMonitorCmdText.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, 3, 1));
			continuousMonitorCmdText.addModifyListener(getListener());
		}

		/*
		 * Installation path
		 */
		if ((fCapabilities & IToolRMConfiguration.CAP_REMOTE_INSTALL_PATH) != 0) {
			Label label = new Label(contents, SWT.NONE);
			label.setText(Messages.AbstractToolsPreferencePage_Label_InstallationPath);

			remoteInstallPathText = new Text(contents, SWT.SINGLE | SWT.BORDER);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 3;
			gd.widthHint = 60;
			remoteInstallPathText.setLayoutData(gd);
			remoteInstallPathText.addModifyListener(getListener());
		}
		return contents;
	}

	/**
	 * @since 2.0
	 */
	protected String getQualifier() {
		return fQualifier;
	}

	@Override
	protected void updateControls() {
		// Nothing to do.
	}
}
