/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.lsf.ui.wizards;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.ptp.lsf.ui.LSFPreferenceManager;
import org.eclipse.ptp.remote.ui.wizards.AbstractRemoteResourceManagerConfigurationWizardPage;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizard;

public final class LSFResourceManagerConfigurationWizardPage extends
	AbstractRemoteResourceManagerConfigurationWizardPage {
	
	public LSFResourceManagerConfigurationWizardPage(RMConfigurationWizard wizard) {
		super(wizard, "LSF Proxy Configuration");
		setTitle("LSF Proxy Configuration");
		setDescription("Enter information to connect to an LSF proxy server");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.ui.wizards.AbstractRemoteResourceManagerConfigurationWizardPage#getPreferences()
	 */
	@Override
	public Preferences getPreferences() {
		return LSFPreferenceManager.getPreferences();
	}
}
