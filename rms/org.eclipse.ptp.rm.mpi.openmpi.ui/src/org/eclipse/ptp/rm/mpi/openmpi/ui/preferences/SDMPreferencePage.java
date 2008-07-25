package org.eclipse.ptp.rm.mpi.openmpi.ui.preferences;

import org.eclipse.core.runtime.Path;
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
			if(source == sdmExecText ||
					source == sdmHostText ||
					source == sdmDebuggerText ||
					source == sdmPortSpinner) {
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

	private class DataSource extends PreferenceDataSource {
		String sdmExec;
		String sdmHost;
		String sdmDebugger;
		int sdmPort;
		
		public DataSource(SDMPreferencePage preferencePage) {
			super(preferencePage);
		}

		protected void copyFromFields() throws ValidationException {
			sdmExec = extractText(sdmExecText);
			sdmHost= extractText(sdmHostText);
			sdmDebugger = extractText(sdmDebuggerText);
			sdmPort = sdmPortSpinner.getSelection();
		}

		protected void validateLocal() throws ValidationException {
			if (sdmExec == null) {
				throw new ValidationException("Executable path is missing.");
			} else {
				Path path = new Path(sdmExec);
				if (path.isAbsolute()) {
					throw new ValidationException("Executable path must be absolute.");
				}
			}
			if (sdmHost == null) {
				throw new ValidationException("Hostname is missing.");
			}
			if (sdmDebugger == null) {
				throw new ValidationException("Debugger name is missing.");
			}
			if (sdmPort < 1) {
				throw new ValidationException("Port number is missing.");
			} else if (sdmPort > 65535) {
				throw new ValidationException("Port number out of range.");
			}
		}

		protected void storeConfig() {
			Preferences config = getPreferences();
			config.setValue(SDMPreferenceManager.PREFIX + SDMPreferenceManager.PREF_SDM_EXEC, toPreference(sdmExec));
			config.setValue(SDMPreferenceManager.PREFIX + SDMPreferenceManager.PREF_SDM_HOST, toPreference(sdmHost));
			config.setValue(SDMPreferenceManager.PREFIX + SDMPreferenceManager.PREF_SDM_DEBUGGER, toPreference(sdmDebugger));
			config.setValue(SDMPreferenceManager.PREFIX + SDMPreferenceManager.PREF_SDM_PORT, sdmPort);
			savePreferences();
		}

		protected void loadConfig() {
			Preferences config = getPreferences();
			sdmExec = fromPreference(config.getString(SDMPreferenceManager.PREFIX + SDMPreferenceManager.PREF_SDM_EXEC));
			sdmHost = fromPreference(config.getString(SDMPreferenceManager.PREFIX + SDMPreferenceManager.PREF_SDM_HOST));
			sdmDebugger = fromPreference(config.getString(SDMPreferenceManager.PREFIX + SDMPreferenceManager.PREF_SDM_DEBUGGER));
			sdmPort = config.getInt(SDMPreferenceManager.PREFIX + SDMPreferenceManager.PREF_SDM_PORT);
		}

		protected void loadDefaultConfig() {
			Preferences config = getPreferences();
			sdmExec = fromPreference(config.getDefaultString(SDMPreferenceManager.PREFIX + SDMPreferenceManager.PREF_SDM_EXEC));
			sdmHost = fromPreference(config.getDefaultString(SDMPreferenceManager.PREFIX + SDMPreferenceManager.PREF_SDM_HOST));
			sdmDebugger = fromPreference(config.getDefaultString(SDMPreferenceManager.PREFIX + SDMPreferenceManager.PREF_SDM_DEBUGGER));
			sdmPort = config.getDefaultInt(SDMPreferenceManager.PREFIX + SDMPreferenceManager.PREF_SDM_PORT);
		}

		protected void copyToFields() {
			applyText(sdmExecText, sdmExec);
			applyText(sdmHostText, sdmHost);
			applyText(sdmDebuggerText, sdmDebugger);
			sdmPortSpinner.setSelection(sdmPort);
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
