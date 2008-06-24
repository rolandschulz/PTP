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
package org.eclipse.ptp.rm.mpi.openmpi.core;

import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.StringAttributeDefinition;


/**
 * OMPI-specific attributes
 */
public class OpenMpiLaunchAttributes {
	private static final String LAUNCH_ARGS_ATTR_ID = "mpiArgs";

	private final static StringAttributeDefinition launchArgs =
		new StringAttributeDefinition(LAUNCH_ARGS_ATTR_ID, LAUNCH_ARGS_ATTR_ID,
				"Launch Arguments", true, "");


	public static IAttributeDefinition<?,?,?>[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[]{
				launchArgs,
			};
	}

	public static StringAttributeDefinition getLaunchArgumentsAttributeDefinition() {
		return launchArgs;
	}
}
