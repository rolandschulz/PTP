/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.pbs.ui.preferences;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.ptp.rm.pbs.core.PBSPreferenceManager;
import org.eclipse.ptp.rm.remote.ui.preferences.AbstractRemotePreferencePage;

public class PBSPreferencePage extends AbstractRemotePreferencePage {
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.ui.preferences.AbstractRemotePreferencePage#getPreferences()
	 */
	public Preferences getPreferences() {
		return PBSPreferenceManager.getPreferences();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.ui.preferences.AbstractRemotePreferencePage#savePreferences()
	 */
	public void savePreferences() {
		PBSPreferenceManager.savePreferences();
	}
}