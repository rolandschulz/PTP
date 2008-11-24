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
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMPI12PreferenceManager;
import org.eclipse.ptp.rm.mpi.openmpi.core.rmsystem.OpenMPIResourceManagerConfiguration;
import org.eclipse.ptp.rm.mpi.openmpi.ui.messages.Messages;
import org.eclipse.ptp.rm.ui.preferences.AbstractToolsPreferencePage;

/**
 * 
 * @author Daniel Felix Ferber
 *
 */
public class OpenMPI12PreferencePage extends AbstractToolsPreferencePage {

	public OpenMPI12PreferencePage() {
		super(OpenMPI12PreferenceManager.PREFIX, OpenMPIResourceManagerConfiguration.OPENMPI_CAPABILITIES, Messages.OpenMPI12PreferencePage_Title);
	}

	@Override
	public Preferences getPreferences() {
		return OpenMPI12PreferenceManager.getPreferences();
	}

	@Override
	public void savePreferences() {
		OpenMPI12PreferenceManager.savePreferences();
	}
}
