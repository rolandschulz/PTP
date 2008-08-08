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
package org.eclipse.ptp.rm.mpi.openmpi.ui.preferences;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMPI13PreferenceManager;
import org.eclipse.ptp.rm.mpi.openmpi.core.rmsystem.OpenMPIResourceManagerConfiguration;
import org.eclipse.ptp.rm.ui.preferences.AbstractToolsPreferencePage;

public class OpenMPI13PreferencePage extends AbstractToolsPreferencePage {

	public OpenMPI13PreferencePage() {
		super(OpenMPI13PreferenceManager.PREFIX, OpenMPIResourceManagerConfiguration.OPENMPI_CAPABILITIES, "Open MPI 1.3 preferences");
	}

	@Override
	public Preferences getPreferences() {
		return OpenMPI13PreferenceManager.getPreferences();
	}

	@Override
	public void savePreferences() {
		OpenMPI13PreferenceManager.savePreferences();
	}
}
