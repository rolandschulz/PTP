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

import org.eclipse.ptp.rm.ui.utils.DataSource;

public abstract class PreferenceDataSource extends DataSource {
	private AbstractPreferencePage page;

	protected PreferenceDataSource(AbstractPreferencePage page) {
		this.page = page;
	}

	/**
	 * Facility to convert string value to a value that can be stored in preference storage.
	 * Null values cannot be stored, therefore, they are converted to empty string.
	 * @param The string to be stored
	 * @return The converted string.
	 */
	protected String toPreference(String s) {
		return (s == null ? "" : s); //$NON-NLS-1$
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

	@Override
	protected void setErrorMessage(ValidationException e) {
		page.setErrorMessage(e.getLocalizedMessage());
		page.setValid(false);
	}

	@Override
	protected void update() {
		page.updateControls();
	}
}
