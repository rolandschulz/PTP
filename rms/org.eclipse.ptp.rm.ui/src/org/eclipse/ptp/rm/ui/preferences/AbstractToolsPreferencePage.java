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

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ptp.rm.core.AbstractToolsPreferenceManager;
import org.eclipse.ptp.rm.core.rmsystem.AbstractToolRMConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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
public abstract class AbstractToolsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private class WidgetListener implements ModifyListener {
		private boolean listenerEnabled = true;
		
		public void enable() { listenerEnabled = true; }
		public void disnable() { listenerEnabled = false; }

		public void modifyText(ModifyEvent evt) {
			if (! listenerEnabled) return;

			Object source = evt.getSource();
			if(source == launchCmdText || 
					source == discoverCmdText || 
					source == periodicMonitorCmdText || 
					source == continuousMonitorCmdText || 
					source == periodicMonitorTimeSpinner ||
					source == remoteInstallPathText) {
				resetErrorMessages();
				dataSource.updateAndValidate();
			} else {
				assert false;
			}
		}
		
		public void widgetDefaultSelected(SelectionEvent e) {
			// Empty.
		}
	}
	
	private class DataSource {
		class ValidationException extends Exception {
			public ValidationException(String message) {
				super(message);
			}
		}

//		Preferences config = null;

		String launchCmd = null;
		String discoverCmd = null;
		String periodicMonitorCmd = null;
		int periodicMonitorTime = 0;
		String continuousMonitorCmd = null;
		String remoteInstallPath = null;

		String extractText(Text text) {
			String s = text.getText().trim();
			return (s.length() == 0 ? null : s);
		}
		
		String toPreference(String s) {
			return (s == null ? "" : s);			
		}

		String fromPreference(String s) {
			return (s.equals(EMPTY_STRING) ? null : s);			
		}

		void applyText(Text t, String s) {
			if (s == null) t.setText(EMPTY_STRING);
			else t.setText(s);
		}

		void copyFromFields() throws ValidationException {
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

		void validateLocal() throws ValidationException {
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
		
		void storeConfig() {
			Preferences config = getPreferences();
			if (launchCmdText != null)
				config.setValue(AbstractToolsPreferenceManager.PREFS_LAUNCH_CMD, toPreference(launchCmd));
			if (discoverCmdText != null)
				config.setValue(AbstractToolsPreferenceManager.PREFS_DISCOVER_CMD, toPreference(discoverCmd));
			if (periodicMonitorCmdText != null)
				config.setValue(AbstractToolsPreferenceManager.PREFS_PERIODIC_MONITOR_CMD, toPreference(periodicMonitorCmd));
			if (periodicMonitorTimeSpinner != null)
				config.setValue(AbstractToolsPreferenceManager.PREFS_PERIODIC_MONITOR_TIME, periodicMonitorTime);
			if (continuousMonitorCmdText != null)
				config.setValue(AbstractToolsPreferenceManager.PREFS_CONTINUOUS_MONITOR_CMD, toPreference(continuousMonitorCmd));
			if (remoteInstallPathText != null)
				config.setValue(AbstractToolsPreferenceManager.PREFS_REMOTE_INSTALL_PATH, toPreference(remoteInstallPath));
			savePreferences();
		}

		void loadConfig() {
			Preferences config = getPreferences();
			if (launchCmdText != null)
				launchCmd = fromPreference(config.getString(AbstractToolsPreferenceManager.PREFS_LAUNCH_CMD));
			if (discoverCmdText != null)
				discoverCmd = fromPreference(config.getString(AbstractToolsPreferenceManager.PREFS_DISCOVER_CMD));
			if (periodicMonitorCmdText != null)
				periodicMonitorCmd = fromPreference(config.getString(AbstractToolsPreferenceManager.PREFS_PERIODIC_MONITOR_CMD));
			if (periodicMonitorTimeSpinner != null)
				periodicMonitorTime = config.getInt(AbstractToolsPreferenceManager.PREFS_PERIODIC_MONITOR_TIME);
			if (continuousMonitorCmdText != null)
				continuousMonitorCmd = fromPreference(config.getString(AbstractToolsPreferenceManager.PREFS_CONTINUOUS_MONITOR_CMD));
			if (remoteInstallPathText != null)
				remoteInstallPath = fromPreference(config.getString(AbstractToolsPreferenceManager.PREFS_REMOTE_INSTALL_PATH));
		}

		private void loadDefaultConfig() {
			Preferences config = getPreferences();
			if (launchCmdText != null)
				launchCmd = fromPreference(config.getDefaultString(AbstractToolsPreferenceManager.PREFS_LAUNCH_CMD));
			if (discoverCmdText != null)
				discoverCmd = fromPreference(config.getDefaultString(AbstractToolsPreferenceManager.PREFS_DISCOVER_CMD));
			if (periodicMonitorCmdText != null)
				periodicMonitorCmd = fromPreference(config.getDefaultString(AbstractToolsPreferenceManager.PREFS_PERIODIC_MONITOR_CMD));
			if (periodicMonitorTimeSpinner != null)
				periodicMonitorTime = config.getDefaultInt(AbstractToolsPreferenceManager.PREFS_PERIODIC_MONITOR_TIME);
			if (continuousMonitorCmdText != null)
				continuousMonitorCmd = fromPreference(config.getDefaultString(AbstractToolsPreferenceManager.PREFS_CONTINUOUS_MONITOR_CMD));
			if (remoteInstallPathText != null)
				remoteInstallPath = fromPreference(config.getDefaultString(AbstractToolsPreferenceManager.PREFS_REMOTE_INSTALL_PATH));
		}

		void copyToFields() {
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
		}

		void validateGlobal() throws ValidationException {
			// Nothing yet.
		}
		
		public void updateAndValidate() {
			try {
				copyFromFields();
				validateLocal();
				validateGlobal();
			} catch (ValidationException e) {
				setErrorMessage(e.getLocalizedMessage());
				setValid(false);
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
				setValid(false);
			}			
		}
		
		public boolean storeAndUpdate() {
			try {
				copyFromFields();
				validateLocal();
				validateGlobal();
				storeConfig();
				return true;
			} catch (ValidationException e) {
				setErrorMessage(e.getLocalizedMessage());
				//setValid(false);
				return false;
			}
		}

		public void loadDefaultsAndUpdate() {
			loadDefaultConfig();
			copyToFields();
			try {
				validateLocal();
				validateGlobal();
			} catch (ValidationException e) {
				setErrorMessage(e.getLocalizedMessage());
				setValid(false);
			}	
		}
	}

	public static final String EMPTY_STRING = "";  //$NON-NLS-1$
	private Text launchCmdText = null;
	private Text discoverCmdText = null;
	private Text continuousMonitorCmdText = null;
	private Text periodicMonitorCmdText = null;
	private Spinner periodicMonitorTimeSpinner = null;
	private Text remoteInstallPathText = null;
	
	private WidgetListener listener = new WidgetListener();
	private DataSource dataSource = new DataSource();
	
	private int capabilities = AbstractToolRMConfiguration.NO_CAP_SET;
	
    public AbstractToolsPreferencePage(int capabilities) {
		super();
    	this.capabilities = capabilities;
	}

	public AbstractToolsPreferencePage(int capabilities, String title, ImageDescriptor image) {
		super(title, image);
    	this.capabilities = capabilities;
	}

	public AbstractToolsPreferencePage(int capabilities, String title) {
		super(title);
    	this.capabilities = capabilities;
	}

	public void init(IWorkbench workbench) {
		// Nothing to do.
	}

	abstract public Preferences getPreferences();

	abstract public void savePreferences();
	
	@Override
	protected Control createContents(Composite parent) {
		Composite contents = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		contents.setLayout(layout);

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
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 3;
			gd.widthHint = 60;
			remoteInstallPathText.setLayoutData(gd);
			remoteInstallPathText.addModifyListener(listener);
		}
		resetErrorMessages();
		listener.disnable();
		dataSource.loadAndUpdate();
		listener.enable();
		return contents;
	}
	
	@Override
	public boolean performOk() {
		resetErrorMessages();
		dataSource.storeAndUpdate();
		return true;
//		return super.performOk();
	}
	
	@Override
	protected void performDefaults() {
		resetErrorMessages();
		listener.disnable();
		dataSource.loadDefaultsAndUpdate();
		listener.enable();
//		super.performDefaults();
	}
	
	@Override
	protected void performApply() {
		resetErrorMessages();
		dataSource.storeAndUpdate();
//		super.performApply();
	}
	
	protected void resetErrorMessages() {
		setErrorMessage(null);
		setMessage(null);
		setValid(true);
	}
}
