/**********************************************************************
 * Copyright (c) 2009,2011 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.etfw.feedback.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.internal.etfw.feedback.Activator;
import org.eclipse.ptp.internal.etfw.feedback.messages.Messages;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.P_MAINTAIN_EXPAND_COLLAPSE_STATE, true);
		store.setDefault(PreferenceConstants.P_SHOW_NO_ITEMS_FOUND_DIALOG, true);
		store.setDefault(PreferenceConstants.P_CHOICE, "choice2"); //$NON-NLS-1$
		store.setDefault(PreferenceConstants.P_STRING,
				Messages.PreferenceInitializer_defaultValue);
	}

}
