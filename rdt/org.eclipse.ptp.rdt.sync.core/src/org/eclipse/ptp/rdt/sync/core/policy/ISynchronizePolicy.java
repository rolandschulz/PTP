/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.core.policy;

import org.eclipse.core.resources.IProject;
import org.eclipse.ptp.rdt.sync.core.BuildScenario;
import org.eclipse.ptp.rdt.sync.core.SyncManager.SyncMode;

/**
 * Provides synchronization policies.
 */
public interface ISynchronizePolicy {
	/**
	 * Get the build scenarios for a project. The mode argument is used to modify which set of scenarios is obtained.
	 * 
	 * @param project
	 *            synchronized project
	 * @param mode
	 *            a mode containing one of {@link SyncMode#ACTIVE} or {@link SyncMode#ALL}
	 * @return array of build scenarios or null if the project is not synchronized or there are no scenarios for the project
	 */
	public BuildScenario[] getSyncronizePolicy(IProject project, SyncMode mode);
}
