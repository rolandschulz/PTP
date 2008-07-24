package org.eclipse.ptp.rm.mpi.openmpi.ui.preferences;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.ptp.rm.mpi.openmpi.core.SDMPreferenceManager;
import org.eclipse.ptp.rm.ui.preferences.PreferenceDataSource;
import org.eclipse.ptp.rm.ui.preferences.PreferenceWidgetListener;
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

public class SDMPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private class WidgetListener extends PreferenceWidgetListener implements ModifyListener {
		public void modifyText(ModifyEvent evt) {
			if (! isEnabled()) return;

			Object source = evt.getSource();
//			if(source == launchCmdText ||
//					source == discoverCmdText ||
//					source == periodicMonitorCmdText ||
//					source == continuousMonitorCmdText ||
//					source == periodicMonitorTimeSpinner ||
//					source == remoteInstallPathText) {
//				resetErrorMessages();
//				dataSource.updateAndValidate();
//			} else {
//				assert false;
//			}
		}

		public void widgetDefaultSelected(SelectionEvent e) {
			// Empty.
		}
	}

	private class DataSource extends PreferenceDataSource {
		String sdmExec;
		String sdmHost;
		String sdmDebugger;
		int sdmPort;
		
		public DataSource(SDMPreferencePage preferencePage) {
			super(preferencePage);
		}

		protected void copyFromFields() throws ValidationException {
			
//			if (launchCmdText != null)
//				launchCmd = extractText(launchCmdText);
//			if (discoverCmdText != null)
//				discoverCmd = extractText(discoverCmdText);
//			if (periodicMonitorCmdText != null)
//				periodicMonitorCmd = extractText(periodicMonitorCmdText);
//			if (periodicMonitorTimeSpinner != null)
//				periodicMonitorTime = periodicMonitorTimeSpinner.getSelection();
//			if (continuousMonitorCmdText != null)
//				continuousMonitorCmd = extractText(continuousMonitorCmdText);
//			if (remoteInstallPathText != null)
//				remoteInstallPath = extractText(remoteInstallPathText);
		}

		protected void validateLocal() throws ValidationException {
//			if (launchCmdText != null && launchCmd == null) {
//				throw new ValidationException("Launch command is missing");
//			}
//			if (discoverCmdText != null) {
//				if (discoverCmd == null) {
//					throw new ValidationException("Discover command is missing");
//				}
//				if (periodicMonitorTimeSpinner != null && periodicMonitorTime < 1) {
//					throw new ValidationException("Time period must be an integer greater than 0");
//				}
//			}
		}

		protected void storeConfig() {
//			Preferences config = getPreferences();
//			if (launchCmdText != null)
//				config.setValue(prefix + AbstractToolsPreferenceManager.PREFS_LAUNCH_CMD, toPreference(launchCmd));
//			if (discoverCmdText != null)
//				config.setValue(prefix + AbstractToolsPreferenceManager.PREFS_DISCOVER_CMD, toPreference(discoverCmd));
//			if (periodicMonitorCmdText != null)
//				config.setValue(prefix + AbstractToolsPreferenceManager.PREFS_PERIODIC_MONITOR_CMD, toPreference(periodicMonitorCmd));
//			if (periodicMonitorTimeSpinner != null)
//				config.setValue(prefix + AbstractToolsPreferenceManager.PREFS_PERIODIC_MONITOR_TIME, periodicMonitorTime);
//			if (continuousMonitorCmdText != null)
//				config.setValue(prefix + AbstractToolsPreferenceManager.PREFS_CONTINUOUS_MONITOR_CMD, toPreference(continuousMonitorCmd));
//			if (remoteInstallPathText != null)
//				config.setValue(prefix + AbstractToolsPreferenceManager.PREFS_REMOTE_INSTALL_PATH, toPreference(remoteInstallPath));
			savePreferences();
		}

		protected void loadConfig() {
			Preferences config = getPreferences();
//			if (launchCmdText != null)
//				launchCmd = fromPreference(config.getString(prefix + AbstractToolsPreferenceManager.PREFS_LAUNCH_CMD));
//			if (discoverCmdText != null)
//				discoverCmd = fromPreference(config.getString(prefix + AbstractToolsPreferenceManager.PREFS_DISCOVER_CMD));
//			if (periodicMonitorCmdText != null)
//				periodicMonitorCmd = fromPreference(config.getString(prefix + AbstractToolsPreferenceManager.PREFS_PERIODIC_MONITOR_CMD));
//			if (periodicMonitorTimeSpinner != null)
//				periodicMonitorTime = config.getInt(prefix + AbstractToolsPreferenceManager.PREFS_PERIODIC_MONITOR_TIME);
//			if (continuousMonitorCmdText != null)
//				continuousMonitorCmd = fromPreference(config.getString(prefix + AbstractToolsPreferenceManager.PREFS_CONTINUOUS_MONITOR_CMD));
//			if (remoteInstallPathText != null)
//				remoteInstallPath = fromPreference(config.getString(prefix + AbstractToolsPreferenceManager.PREFS_REMOTE_INSTALL_PATH));
		}

		protected void loadDefaultConfig() {
			Preferences config = getPreferences();
//			if (launchCmdText != null)
//				launchCmd = fromPreference(config.getDefaultString(prefix + AbstractToolsPreferenceManager.PREFS_LAUNCH_CMD));
//			if (discoverCmdText != null)
//				discoverCmd = fromPreference(config.getDefaultString(prefix + AbstractToolsPreferenceManager.PREFS_DISCOVER_CMD));
//			if (periodicMonitorCmdText != null)
//				periodicMonitorCmd = fromPreference(config.getDefaultString(prefix + AbstractToolsPreferenceManager.PREFS_PERIODIC_MONITOR_CMD));
//			if (periodicMonitorTimeSpinner != null)
//				periodicMonitorTime = config.getDefaultInt(prefix + AbstractToolsPreferenceManager.PREFS_PERIODIC_MONITOR_TIME);
//			if (continuousMonitorCmdText != null)
//				continuousMonitorCmd = fromPreference(config.getDefaultString(prefix + AbstractToolsPreferenceManager.PREFS_CONTINUOUS_MONITOR_CMD));
//			if (remoteInstallPathText != null)
//				remoteInstallPath = fromPreference(config.getDefaultString(prefix + AbstractToolsPreferenceManager.PREFS_REMOTE_INSTALL_PATH));
		}

		protected void copyToFields() {
//			if (launchCmdText != null)
//				applyText(launchCmdText, launchCmd);
//			if (discoverCmdText != null)
//				applyText(discoverCmdText, discoverCmd);
//			if (periodicMonitorCmdText != null)
//				applyText(periodicMonitorCmdText, periodicMonitorCmd);
//			if (periodicMonitorTimeSpinner != null)
//				periodicMonitorTimeSpinner.setSelection(periodicMonitorTime);
//			if (continuousMonitorCmdText != null)
//				applyText(continuousMonitorCmdText, continuousMonitorCmd);
//			if (remoteInstallPathText != null)
//				applyText(remoteInstallPathText, remoteInstallPath);
		}
	}

	public static final String EMPTY_STRING = "";  //$NON-NLS-1$
	private Text sdmExecText = null;
	private Text sdmHostText = null;
	private Spinner sdmPortSpinner = null;
	private Text sdmDebuggerText = null;

	private WidgetListener listener = new WidgetListener();
	private DataSource dataSource = new DataSource(this);

    public SDMPreferencePage() {
		// TODO Auto-generated constructor stub
		super();
	}

	public void init(IWorkbench workbench) {
		// Nothing to do.
	}

	public Preferences getPreferences() {
		return SDMPreferenceManager.getPreferences();
	}

	public void savePreferences() {
		SDMPreferenceManager.savePreferences();
	}
	
	@Override
	protected Control createContents(Composite parent) {
		Composite contents = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		contents.setLayout(layout);

		{
			Label label = new Label(contents, SWT.NONE);
			label.setText("SDM executable:");

			sdmExecText = new Text(contents, SWT.SINGLE | SWT.BORDER);
			sdmExecText.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, 3, 1));
			sdmExecText.addModifyListener(listener);
		}

		{
			Label label = new Label(contents, SWT.NONE);
			label.setText("SDM host:");

			sdmHostText = new Text(contents, SWT.SINGLE | SWT.BORDER);
			sdmHostText.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, 3, 1));
			sdmHostText.addModifyListener(listener);
		}

		{
			Label label = new Label(contents, SWT.NONE);
			label.setText("SDM port number:");
			
			sdmPortSpinner = new Spinner(contents, SWT.SINGLE | SWT.BORDER);
			sdmPortSpinner.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, 3, 1));
			sdmPortSpinner.addModifyListener(listener);

		}
		
		{
			Label label = new Label(contents, SWT.NONE);
			label.setText("SDM debugger:");

			sdmDebuggerText = new Text(contents, SWT.SINGLE | SWT.BORDER);
			sdmDebuggerText.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, 3, 1));
			sdmDebuggerText.addModifyListener(listener);
		}

		resetErrorMessages();
		listener.disable();
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
		listener.disable();
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
