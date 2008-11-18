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
package org.eclipse.ptp.rm.mpi.mpich2.ui.preferences;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.ptp.rm.mpi.mpich2.core.MPICH2PreferenceManager;
import org.eclipse.ptp.rm.mpi.mpich2.core.rmsystem.MPICH2ResourceManagerConfiguration;
import org.eclipse.ptp.rm.mpi.mpich2.ui.messages.Messages;
import org.eclipse.ptp.rm.ui.preferences.AbstractToolsPreferencePage;

/**
 * 
 * @author Daniel Felix Ferber
 *
 */
public class MPICH2PreferencePage extends AbstractToolsPreferencePage {

	public MPICH2PreferencePage() {
		super(MPICH2PreferenceManager.PREFIX, MPICH2ResourceManagerConfiguration.MPICH2_CAPABILITIES, Messages.MPICH2PreferencePage_Title);
	}

	@Override
	public Preferences getPreferences() {
		return MPICH2PreferenceManager.getPreferences();
	}

	@Override
	public void savePreferences() {
		MPICH2PreferenceManager.savePreferences();
	}
}
