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

import org.eclipse.ptp.core.attributes.ArrayAttributeDefinition;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.StringAttributeDefinition;


/**
 * OMPI-specific attributes
 */
public class OpenMpiLaunchAttributes {
	private static final String LAUNCH_ARGS_ATTR_ID = "mpiArgs";
	private static final String ENV_KEYS_ATTR_ID = "envKeys";
	private static final String ENV_ARGS_ATTR_ID = "mpiEnvArgs";

	private final static StringAttributeDefinition launchArgs =
		new StringAttributeDefinition(LAUNCH_ARGS_ATTR_ID, "openmpi arguments",
				"Command line arguments for openmpi", false, "");

   private final static ArrayAttributeDefinition<String> environmentKeyDefinition = new ArrayAttributeDefinition<String>(
			ENV_KEYS_ATTR_ID, "Environment variables", "Name of environment variables supplied to the each openmpi process", false,
			new String[0]);

	private final static StringAttributeDefinition environmentArgsDefinition = new StringAttributeDefinition(
			ENV_ARGS_ATTR_ID, "Environment arguments", "Command line arguments to set environment variables for each openmpi process", false,
			"${envKeys:: -x ::-x :}");

	/**
	 * List of names of environment variables for the application.
	 * <p>
	 * Note: openmpi 1.2 and 1.3
	 */
 	public static ArrayAttributeDefinition<String> getEnvironmentKeysDefinition() {
		return environmentKeyDefinition;
	}

	/**
	 * Environment variables for the application, as expected by openmpi command line.
	 * <p>
	 * Note: openmpi 1.2 and 1.3
	 */
	public static StringAttributeDefinition getEnvironmentArgsDefinition() {
		return environmentArgsDefinition;
	}

	/**
	 * Arguments for the application, as expected by openmpi command line.
	 * <p>
	 * Note: openmpi 1.2 and 1.3
	 */
	public static StringAttributeDefinition getLaunchArgumentsAttributeDefinition() {
		return launchArgs;
	}

	public static IAttributeDefinition<?,?,?>[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[]{
				launchArgs,environmentArgsDefinition,environmentKeyDefinition
			};
	}


}
