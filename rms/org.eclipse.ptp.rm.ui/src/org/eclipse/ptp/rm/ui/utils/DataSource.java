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
package org.eclipse.ptp.rm.ui.utils;

import org.eclipse.swt.widgets.Text;

/**
 * Common features for a intermediary buffer, to load and store content of
 * widget on dialog pages and to validate their content.
 * <p>
 * This class eases reading/writing values from storage and setting/getting them
 * to/from input widgets of the dialog page.
 * <p>
 * For that purpose, the class assumes a intermediary storage (implemented by
 * extending class) that buffers the values when getting from widgets and
 * writing them to storage storage. The same intermediary storage is used to
 * buffer values when reading values from storage storage and setting them to
 * widgets. While in intermediary buffer, values are validated.
 * <p>
 * Each step is performed by a specific abstract method that is implemented by
 * the extending class. Methods are provided for a complete sequence of
 * operations (eg. getting from widgets, validating, writing to storage). The
 * class controls the validity state of the dialog page and its error/warning
 * message.
 *
 * @author Daniel Felix Ferber
 */
public abstract class DataSource {
	/**
	 * Exception raised by validation methods when input of a field is invalid.
	 * The message of the exception is displayed on the preference page.
	 */
	public class ValidationException extends Exception {
		private static final long serialVersionUID = 1L;

		public ValidationException(String message) {
			super(message);
		}
	}

	protected static final String EMPTY_STRING = "";

	/**
	 * Update the page with the error message and status.
	 *
	 * @param e
	 *            Exception raised while validating the intermediate storage.
	 */
	abstract protected void setError(ValidationException e);

	/**
	 * Facility to get string value of a {@link Text} widget, or null if the
	 * widget is empty.
	 *
	 * @param text
	 *            The widget
	 * @return The string value of widget or null widget is empty.
	 */
	protected String extractText(Text text) {
		String s = text.getText().trim();
		return (s.length() == 0 ? null : s);
	}

	/**
	 * Facility to set the string value of a {@link Text} widget.
	 *
	 * @param t
	 *            The {@link Text} widget.
	 * @param s
	 *            The new string value.
	 */
	protected void applyText(Text t, String s) {
		if (s == null)
			t.setText(EMPTY_STRING);
		else
			t.setText(s);
	}

	/**
	 * Called to individually validate each value in intermediary storage.
	 * Should be used to test format and range of values.
	 *
	 * @throws ValidationException
	 *             A value in intermediary storage is invalid. The message of
	 *             the exception is shown as error message in the preference
	 *             page.
	 */
	abstract protected void validateLocal() throws ValidationException;

	/**
	 * Called to validate the intermediary storage as a whole. Should be used to
	 * test values make sense with each other.
	 *
	 * @throws ValidationException
	 *             A value in intermediary storage is invalid. The message of
	 *             the exception is shown as error message in the preference
	 *             page.
	 */
	protected void validateGlobal() throws ValidationException {
		// By default, there is no global validation.
	}

	/**
	 * Called to write the intermediary storage into storage.
	 */
	abstract protected void copyToStorage();

	/**
	 * Called to read the intermediary storage from storage.
	 */
	abstract protected void loadFromStorage();

	/**
	 * Called to assign default values to intermediary storage.
	 */
	abstract protected void loadDefault();

	/**
	 * Copy values from the intermediary storage to the widgets.
	 */
	abstract protected void copyToFields();

	/**
	 * Copy values from the widgets to intermediary storage.
	 *
	 * @throws ValidationException
	 *             A widget cannot be read because it contains an invalid value
	 *             that cannot be parsed to intermediary storage.
	 */
	abstract protected void copyFromFields() throws ValidationException;

	/**
	 * Update visibility and 'enablebility' of widgets.
	 */
	abstract protected void update();

	/**
	 * Get values from widgets into intermediary storage and validate them.
	 */
	final public void justValidate() {
		try {
			copyFromFields();
			validateLocal();
		} catch (ValidationException e) {
			setError(e);
		}
	}

	/**
	 * Put values from intermediary storage into widgets and update visibility and 'enablebility' of widgets.
	 */
	final public void justUpdate() {
		copyToFields();
		try {
			validateLocal();
		} catch (ValidationException e) {
			setError(e);
		}
		update();
	}

	/**
	 * Get values from widgets, validate them and, if validations was
	 * successful, store them to preferences.
	 *
	 * @return true if could store to preferences, false otherwise.
	 */
	final public boolean storeAndValidate() {
		try {
			copyFromFields();
			validateLocal();
			validateGlobal();
			copyToStorage();
			return true;
		} catch (ValidationException e) {
			setError(e);
			return false;
		}
	}

	/**
	 * Get values from input fields, validates them and stores them to
	 * preferences.
	 */
	final public void loadAndUpdate() {
		loadFromStorage();
		copyToFields();
		try {
			validateLocal();
			validateGlobal();
		} catch (ValidationException e) {
			setError(e);
		}
		update();
	}

	/**
	 * Get default values from preferences storage and put them into widgets.
	 * Values are also validated.
	 */
	final public void loadDefaultsAndUpdate() {
		loadDefault();
		copyToFields();
		try {
			validateLocal();
			validateGlobal();
		} catch (ValidationException e) {
			setError(e);
		}
		update();
	}
}
