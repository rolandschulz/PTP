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

import org.eclipse.swt.widgets.Combo;
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
		private boolean canSave = false;
		private boolean canAccept = false;

		public ValidationException(String message) {
			super(message);
			canSave = false;
			canAccept = false;
		}

		public ValidationException(String message, boolean canAccept, boolean canSave) {
			super(message);
			this.canAccept = canAccept;
			this.canSave = canSave;
		}

		public boolean canAccept() {
			return canAccept;
		}

		public boolean canSave() {
			return canSave;
		}
	}

	private ValidationException fFirstException = null;
	private boolean fCanSave = false;
	private boolean fCanAccept = false;

	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$

	/**
	 * Update the page with the error message and status.
	 * 
	 * @param e
	 *            Exception raised while validating the intermediate storage.
	 */
	protected abstract void setErrorMessage(ValidationException e);

	/**
	 * Facility to get string value of a {@link Text} widget, or null if the
	 * widget is empty.
	 * 
	 * @param text
	 *            The widget
	 * @return The string value of widget or null widget is empty.
	 */
	protected String extractText(Text text) {
		assert text != null;
		String s = text.getText().trim();
		return (s.length() == 0 ? null : s);
	}

	/**
	 * Facility to get string value of a {@link Combo} widget, or null if the
	 * widget is empty.
	 * 
	 * @param text
	 *            The widget
	 * @return The string value of widget or null widget is empty.
	 */
	protected String extractText(Combo text) {
		assert text != null;
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
		assert t != null;
		if (s == null) {
			t.setText(EMPTY_STRING);
		} else {
			t.setText(s);
		}
	}

	/**
	 * Facility to set the string value of a {@link Text} widget.
	 * 
	 * @param t
	 *            The {@link Text} widget.
	 * @param s
	 *            The new string value.
	 */
	protected void applyText(Combo t, String s) {
		assert t != null;
		if (s == null) {
			t.setText(EMPTY_STRING);
		} else {
			t.setText(s);
		}
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
	protected abstract void validateLocal() throws ValidationException;

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
	protected abstract void copyToStorage();

	/**
	 * Called to read the intermediary storage from storage.
	 */
	protected abstract void loadFromStorage();

	/**
	 * Called to assign default values to intermediary storage.
	 */
	protected abstract void loadDefault();

	/**
	 * Copy values from the intermediary storage to the widgets.
	 */
	protected abstract void copyToFields();

	/**
	 * Copy values from the widgets to intermediary storage.
	 * 
	 * @throws ValidationException
	 *             A widget cannot be read because it contains an invalid value
	 *             that cannot be parsed to intermediary storage.
	 */
	protected abstract void copyFromFields() throws ValidationException;

	/**
	 * Update visibility and 'enablebility' of widgets.
	 */
	protected abstract void update();

	protected void addException(ValidationException e) {
		DebugUtil.trace(DebugUtil.DATASOURCE_TRACING, "DataSource: Add Exception: {0}", e.getMessage()); //$NON-NLS-1$
		if (fFirstException == null) {
			fFirstException = e;
			fCanAccept = e.canAccept();
			fCanSave = e.canSave();
		} else {
			if (fCanSave && !e.canSave()) {
				fFirstException = e;
			}
			fCanAccept = fCanAccept && e.canAccept();
			fCanSave = fCanSave && e.canSave();
		}
	}

	protected void resetExceptions() {
		DebugUtil.trace(DebugUtil.DATASOURCE_TRACING, "DataSource: Reset exceptions"); //$NON-NLS-1$
		fFirstException = null;
		fCanAccept = true;
		fCanSave = true;
	}

	public boolean canAccept() {
		DebugUtil.trace(DebugUtil.DATASOURCE_TRACING, "DataSource: fCanAccept={0}", Boolean.toString(fCanAccept)); //$NON-NLS-1$
		return fCanAccept;
	}

	public boolean canSave() {
		DebugUtil.trace(DebugUtil.DATASOURCE_TRACING, "DataSource: fCanSave={0}", Boolean.toString(fCanSave)); //$NON-NLS-1$
		return fCanSave;
	}

	public boolean summarizeExceptions() {
		if (!fCanAccept || !fCanSave) {
			setErrorMessage(fFirstException);
			return false;
		}
		return true;
	}

	public String getErrorMessage() {
		if (fFirstException == null) {
			return null;
		}
		return fFirstException.getMessage();
	}

	/**
	 * Get values from widgets into intermediary storage and validate them.
	 */
	final public boolean justValidate() {
		DebugUtil.trace(DebugUtil.DATASOURCE_TRACING, "DataSource: justValidate()"); //$NON-NLS-1$
		try {
			resetExceptions();
			copyFromFields();
			validateLocal();
		} catch (ValidationException e) {
			addException(e);
		}
		update();
		return summarizeExceptions();
	}

	/**
	 * Put values from intermediary storage into widgets and update visibility
	 * and 'enablebility' of widgets.
	 */
	final public void justUpdate() {
		DebugUtil.trace(DebugUtil.DATASOURCE_TRACING, "DataSource: justUpdate()"); //$NON-NLS-1$
		copyToFields();
		try {
			resetExceptions();
			validateLocal();
			validateGlobal();
			summarizeExceptions();
		} catch (ValidationException e) {
			addException(e);
			summarizeExceptions();
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
		DebugUtil.trace(DebugUtil.DATASOURCE_TRACING, "DataSource: storeAndValidate()"); //$NON-NLS-1$
		try {
			resetExceptions();
			copyFromFields();
			validateLocal();
			validateGlobal();
			copyToStorage();
		} catch (ValidationException e) {
			addException(e);
		}
		update();
		return summarizeExceptions();
	}

	/**
	 * Get values from input fields, validates them and stores them to
	 * preferences.
	 */
	final public void loadAndUpdate() {
		DebugUtil.trace(DebugUtil.DATASOURCE_TRACING, "DataSource: loadAndUpdate()"); //$NON-NLS-1$
		loadFromStorage();
		copyToFields();
		try {
			resetExceptions();
			validateLocal();
			validateGlobal();
			summarizeExceptions();
		} catch (ValidationException e) {
			addException(e);
			summarizeExceptions();
		}
		update();
	}

	/**
	 * Get default values from preferences storage and put them into widgets.
	 * Values are also validated.
	 */
	final public void loadDefaultsAndUpdate() {
		DebugUtil.trace(DebugUtil.DATASOURCE_TRACING, "DataSource: loadDefaultsAndUpdate()"); //$NON-NLS-1$
		loadDefault();
		copyToFields();
		try {
			resetExceptions();
			validateLocal();
			validateGlobal();
			summarizeExceptions();
		} catch (ValidationException e) {
			addException(e);
			summarizeExceptions();
		}
		update();
	}
}
