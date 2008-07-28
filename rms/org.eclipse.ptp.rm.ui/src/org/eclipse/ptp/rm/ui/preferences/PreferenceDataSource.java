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

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Text;

/**
 * Common fetures for a intermediary buffer to load and store content of widget on PreferencePages, and validate their
 * content.
 * <p>
 * This class eases reading/storing values from preference storage and setting/getting them to/from input widgets.
 * <p>
 * For that purpose, the class assumes a intermediary storage (implemented by extending class) that buffers the
 * values when getting from widgets and storing to preference storage. The same intermediary storage is used to 
 * buffer values when reading values from preference storage and setting them to widgets.
 * While in intermediary buffer, values are validated.
 * <p>
 * Each step is performed by a specific abstract method that is implemented by the extending class.
 * Methods are provided for a complete sequence of operations (eg. getting from widgets, validating, storing to preferences).
 * The class controls the validity state of the preference page and its error/warning messsage.
 * 
 * @author Daniel Felix Ferber
 */
public abstract class PreferenceDataSource {
	/**
	 * Exception raised by validation methods when input of a field is invalid. The message of the exception is displayed
	 * on the preference page.
	 */
	public class ValidationException extends Exception {
		private static final long serialVersionUID = 1L;

		public ValidationException(String message) {
			super(message);
		}
	}

	private static final String EMPTY_STRING = "";

	private PreferencePage page;

	/**
	 * Default constructor for the data source, attached to the PreferencePage.
	 * @param page The preference page controlled by the data source.
	 */
	protected PreferenceDataSource(PreferencePage page) {
		this.page = page;
	}

	/**
	 * Facility to get string value of a {@link Text} widget, or null if the widget is empty.
	 * @param text The widget
	 * @return The string value of widget or null widget is empty.
	 */
	protected String extractText(Text text) {
		String s = text.getText().trim();
		return (s.length() == 0 ? null : s);
	}

	/**
	 * Facility to convert string value to a value that can be stored in preference storage.
	 * Null values cannot be stored, therefore, they are converted to empty string. 
	 * @param The string to be stored
	 * @return The converted string.
	 */
	protected String toPreference(String s) {
		return (s == null ? "" : s);
	}

	/**
	 * Facility to convert a string read from storage of preference page to a string value.
	 * Empty string is converted to null, to indicate that the value is missing.
	 * @param The string from preference storage
	 * @return The converted string.
	 */
	protected String fromPreference(String s) {
		return (s.equals(EMPTY_STRING) ? null : s);
	}

	/**
	 * Facility to set the string value of a {@link Text} SWT widget.
	 * @param t The {@link Text} SWT widget.
	 * @param s The new string value. 
	 */
	protected void applyText(Text t, String s) {
		if (s == null)
			t.setText(EMPTY_STRING);
		else
			t.setText(s);
	}

	/**
	 * Called with values from input widgets shall be copied to intermediary storage.
	 * May throw {@link ValidationException} if a value cannot be gotten from a widget (eg. cannot be converted).
	 * @throws ValidationException A value could not be gotten from a widget. The message of the exception is shown as error message in the preference page.
	 */
	abstract	protected void copyFromFields() throws ValidationException;

	/**
	 * Called to individually validate each value in intermediary storage.
	 * Should be used to test format and range of values.
	 * @throws ValidationException A value in intermediary storage is invalid. The message of the exception is shown as error message in the preference page.
	 */
	abstract	protected void validateLocal() throws ValidationException;
	
	/**
	 * Called to store the intermediary storage. into preferences storage.
	 */
	abstract	protected void storeConfig();

	/**
	 * Called to read the intermediary storage. from the preferences storage.
	 */
	abstract	protected void loadConfig();

	/**
	 * Called to read from the preferences storage the default values for intermediary storage.
	 */
	abstract protected void loadDefaultConfig();

	/**
	 * Called to fill input widgets with values from the intermediary storage.
	 */
	abstract	protected void copyToFields();

	/**
	 * Called to validate the intermediary storage as a whole.
	 * Should be used to test values make sense with each other.
	 * @throws ValidationException A value in intermediary storage is invalid. The message of the exception is shown as error message in the preference page.
	 */
	protected void validateGlobal() throws ValidationException {
		// By default, there is no global validation.
	}

	/**
	 * Get values from input fields and only validates them, but does not store the preferences.
	 */
	public void validate() {
		try {
			copyFromFields();
			validateLocal();
			validateGlobal();
		} catch (ValidationException e) {
			page.setErrorMessage(e.getLocalizedMessage());
			page.setValid(false);
		}
	}

	/**
	 * Get values from input fields, validates them and stores them to preferences. 
	 */
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

	/**
	 * Get values from widgets, validate them and, if validations was successful, store them to preferences.  
	 * @return true if could store to preferences, false otherwise.
	 */
	public boolean storeAndValidate() {
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

	/**
	 * Get default values from preferences storage and put them into widgets.
	 * Values are also validated.
	 */
	public void loadDefaultsAndValidate() {
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
