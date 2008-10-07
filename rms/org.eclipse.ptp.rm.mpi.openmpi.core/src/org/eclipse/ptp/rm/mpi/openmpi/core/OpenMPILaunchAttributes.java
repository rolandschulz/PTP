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
 * 
 * @author Daniel Felix Ferber
 */
public class OpenMPILaunchAttributes {
	private static final String LAUNCH_ARGS_ATTR_ID = "Open_MPI_args"; //$NON-NLS-1$
	private static final String ENV_KEYS_ATTR_ID = "Open_MPI_envKeys"; //$NON-NLS-1$
	private static final String ENV_ARGS_ATTR_ID = "Open_MPI_env"; //$NON-NLS-1$

	private final static StringAttributeDefinition launchArgsAttrDef =
		new StringAttributeDefinition(LAUNCH_ARGS_ATTR_ID, Messages.OpenMPILaunchAttributes_launchArgsAttrDef_title,
				Messages.OpenMPILaunchAttributes_launchArgsAttrDef_description, false, ""); //$NON-NLS-1$

	private final static ArrayAttributeDefinition<String> environmentKeyAttrDef = new ArrayAttributeDefinition<String>(
			ENV_KEYS_ATTR_ID, Messages.OpenMPILaunchAttributes_environmentKeyAttrDef_title,
			Messages.OpenMPILaunchAttributes_environmentKeyAttrDef_description, false,
			new String[0]);

	private final static StringAttributeDefinition environmentArgsAttrDef = new StringAttributeDefinition(
			ENV_ARGS_ATTR_ID, Messages.OpenMPILaunchAttributes_environmentArgsAttrDef_title, Messages.OpenMPILaunchAttributes_environmentArgsAttrDef_description, false,
	"${Open_MPI_envKeys:: -x ::-x :}"); //$NON-NLS-1$

	/**
	 * List of names of environment variables for the application.
	 * <p>
	 * Note: openmpi 1.2 and 1.3
	 */
	public static ArrayAttributeDefinition<String> getEnvironmentKeysAttributeDefinition() {
		return environmentKeyAttrDef;
	}

	/**
	 * Environment variables for the application, as expected by openmpi command line.
	 * <p>
	 * Note: openmpi 1.2 and 1.3
	 */
	public static StringAttributeDefinition getEnvironmentArgsAttributeDefinition() {
		return environmentArgsAttrDef;
	}

	/**
	 * Arguments for the application, as expected by openmpi command line.
	 * <p>
	 * Note: openmpi 1.2 and 1.3
	 */
	public static StringAttributeDefinition getLaunchArgumentsAttributeDefinition() {
		return launchArgsAttrDef;
	}

	public static IAttributeDefinition<?,?,?>[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[]{
				launchArgsAttrDef,environmentArgsAttrDef,environmentKeyAttrDef
		};
	}


}
