/*******************************************************************************
 * Copyright (c) 2008,2009 
 * School of Computer, National University of Defense Technology, P.R.China
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 			Peichang Shi <pcmutates@163.com>/<pcshi@nudt.edu.cn>
 *******************************************************************************/
package org.eclipse.ptp.rm.slurm.ui.preferences;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.ptp.rm.slurm.core.SLURMPreferenceManager;
import org.eclipse.ptp.rm.remote.ui.preferences.AbstractRemotePreferencePage;

public class SLURMPreferencePage extends AbstractRemotePreferencePage {
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.ui.preferences.AbstractRemotePreferencePage#getPreferences()
	 */
	public Preferences getPreferences() {
		return SLURMPreferenceManager.getPreferences();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.ui.preferences.AbstractRemotePreferencePage#savePreferences()
	 */
	public void savePreferences() {
		SLURMPreferenceManager.savePreferences();
	}
}