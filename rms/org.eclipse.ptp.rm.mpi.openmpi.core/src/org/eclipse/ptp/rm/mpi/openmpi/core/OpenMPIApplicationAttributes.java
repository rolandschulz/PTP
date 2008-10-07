/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rm.mpi.openmpi.core;

import org.eclipse.ptp.core.attributes.ArrayAttributeDefinition;
import org.eclipse.ptp.core.attributes.StringAttributeDefinition;

/**
 * 
 * @author Daniel Felix Ferber
 *
 */
public class OpenMPIApplicationAttributes {
	private static final String EFFECTIVE_OPEN_MPI_ENV_ATTR_ID = "Open_MPI_env"; //$NON-NLS-1$
	private static final String EFFECTIVE_OPEN_MPI_PROG_ARGS_ATTR_ID = "Open_MPI_progArgs"; //$NON-NLS-1$
	private static final String EFFECTIVE_OPEN_MPI_WORKING_DIR_ATTR_ID = "Open_MPI_workingDir"; //$NON-NLS-1$

	private final static ArrayAttributeDefinition<String> effectiveOpenMPIEnvAttrDef =
		new ArrayAttributeDefinition<String>(EFFECTIVE_OPEN_MPI_ENV_ATTR_ID, Messages.OpenMPIApplicationAttributes_effectiveOpenMPIEnvAttrDef_title,
				Messages.OpenMPIApplicationAttributes_effectiveOpenMPIEnvAttrDef_description, true, null);

	private final static ArrayAttributeDefinition<String> effectiveOpenMPIProgArgsAttrDef =
		new ArrayAttributeDefinition<String>(EFFECTIVE_OPEN_MPI_PROG_ARGS_ATTR_ID, Messages.OpenMPIApplicationAttributes_effectiveOpenMPIProgArgsAttrDef_title,
				Messages.OpenMPIApplicationAttributes_effectiveOpenMPIProgArgsAttrDef_description, true, null);

	private final static StringAttributeDefinition effectiveOpenMPIWorkingDirAttrDef =
		new StringAttributeDefinition(EFFECTIVE_OPEN_MPI_WORKING_DIR_ATTR_ID, Messages.OpenMPIApplicationAttributes_effectiveOpenMPIWorkingDirAttrDef_title,
				Messages.OpenMPIApplicationAttributes_effectiveOpenMPIWorkingDirAttrDef_description, true, ""); //$NON-NLS-1$

	/**
	 * Environment variables that Open MPI has passed to the processes.
	 * <p>
	 * openmpi 1.2 only.
	 */
	public static ArrayAttributeDefinition<String> getEffectiveOpenMPIEnvAttributeDefinition() {
		return effectiveOpenMPIEnvAttrDef;
	}

	/**
	 * Program arguments that Open MPI has passed to the processes.
	 * <p>
	 * openmpi 1.2 only.
	 */
	public static ArrayAttributeDefinition<String> getEffectiveOpenMPIProgArgsAttributeDefinition() {
		return effectiveOpenMPIProgArgsAttrDef;
	}

	/**
	 * Working directory where Open MPI has started the processes.
	 * <p>
	 * openmpi 1.2 only.
	 */
	public static StringAttributeDefinition getEffectiveOpenMPIWorkingDirAttributeDefinition() {
		return effectiveOpenMPIWorkingDirAttrDef;
	}
}
