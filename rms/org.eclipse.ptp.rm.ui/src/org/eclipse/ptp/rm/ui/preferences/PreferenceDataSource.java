package org.eclipse.ptp.rm.ui.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Text;

public abstract class PreferenceDataSource {
	public class ValidationException extends Exception {
		private static final long serialVersionUID = 1L;

		public ValidationException(String message) {
			super(message);
		}
	}

	private static final String EMPTY_STRING = "";

	private PreferencePage page;

	protected PreferenceDataSource(PreferencePage page) {
		this.page = page;
	}

	protected String extractText(Text text) {
		String s = text.getText().trim();
		return (s.length() == 0 ? null : s);
	}

	protected String toPreference(String s) {
		return (s == null ? "" : s);
	}

	protected String fromPreference(String s) {
		return (s.equals(EMPTY_STRING) ? null : s);
	}

	protected void applyText(Text t, String s) {
		if (s == null)
			t.setText(EMPTY_STRING);
		else
			t.setText(s);
	}

	abstract	protected void copyFromFields() throws ValidationException;

	abstract	protected void validateLocal() throws ValidationException;
	
	abstract	protected void storeConfig();

	abstract	protected void loadConfig();

	abstract protected void loadDefaultConfig();

	abstract	protected void copyToFields();

	protected void validateGlobal() throws ValidationException {
		// Nothing yet.
	}

	public void updateAndValidate() {
		try {
			copyFromFields();
			validateLocal();
			validateGlobal();
		} catch (ValidationException e) {
			page.setErrorMessage(e.getLocalizedMessage());
			page.setValid(false);
		}
	}

	public void loadAndUpdate() {
		loadConfig();
		copyToFields();
		try {
			validateLocal();
			validateGlobal();
		} catch (ValidationException e) {
			page.setErrorMessage(e.getLocalizedMessage());
			page.setValid(false);
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
			page.setErrorMessage(e.getLocalizedMessage());
			// setValid(false);
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
			page.setErrorMessage(e.getLocalizedMessage());
			page.setValid(false);
		}
	}
}
