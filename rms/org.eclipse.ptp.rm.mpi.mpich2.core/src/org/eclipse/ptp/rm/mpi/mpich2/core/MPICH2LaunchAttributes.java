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
package org.eclipse.ptp.rm.mpi.mpich2.core;

import org.eclipse.ptp.core.attributes.ArrayAttributeDefinition;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.StringAttributeDefinition;
import org.eclipse.ptp.rm.mpi.mpich2.core.messages.Messages;


/**
 * OMPI-specific attributes
 * 
 * @author Daniel Felix Ferber
 */
public class MPICH2LaunchAttributes {
	private static final String LAUNCH_ARGS_ATTR_ID = "MPICH2_args"; //$NON-NLS-1$
	private static final String ENV_KEYS_ATTR_ID = "MPICH2_envKeys"; //$NON-NLS-1$
	private static final String ENV_ARGS_ATTR_ID = "MPICH2_env"; //$NON-NLS-1$

	private final static StringAttributeDefinition launchArgsAttrDef =
		new StringAttributeDefinition(LAUNCH_ARGS_ATTR_ID, Messages.MPICH2LaunchAttributes_launchArgsAttrDef_title,
				Messages.MPICH2LaunchAttributes_launchArgsAttrDef_description, false, ""); //$NON-NLS-1$

	private final static ArrayAttributeDefinition<String> environmentKeyAttrDef = new ArrayAttributeDefinition<String>(
			ENV_KEYS_ATTR_ID, Messages.MPICH2LaunchAttributes_environmentKeyAttrDef_title,
			Messages.MPICH2LaunchAttributes_environmentKeyAttrDef_description, false,
			new String[0]);

	private final static StringAttributeDefinition environmentArgsAttrDef = new StringAttributeDefinition(
			ENV_ARGS_ATTR_ID, Messages.MPICH2LaunchAttributes_environmentArgsAttrDef_title, Messages.MPICH2LaunchAttributes_environmentArgsAttrDef_description, false,
	"${MPICH2_envKeys:: -x ::-x :}"); //$NON-NLS-1$

	/**
	 * List of names of environment variables for the application.
	 */
	public static ArrayAttributeDefinition<String> getEnvironmentKeysAttributeDefinition() {
		return environmentKeyAttrDef;
	}

	/**
	 * Environment variables for the application, as expected by openmpi command line.
	 */
	public static StringAttributeDefinition getEnvironmentArgsAttributeDefinition() {
		return environmentArgsAttrDef;
	}

	/**
	 * Arguments for the application, as expected by openmpi command line.
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
