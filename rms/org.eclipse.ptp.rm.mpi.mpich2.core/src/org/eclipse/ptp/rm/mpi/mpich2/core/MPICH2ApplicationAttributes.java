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
package org.eclipse.ptp.rm.mpi.mpich2.core;

import org.eclipse.ptp.core.attributes.ArrayAttributeDefinition;
import org.eclipse.ptp.core.attributes.StringAttributeDefinition;
import org.eclipse.ptp.rm.mpi.mpich2.core.messages.Messages;

/**
 * 
 * @author Daniel Felix Ferber
 *
 */
public class MPICH2ApplicationAttributes {
	private static final String EFFECTIVE_MPICH2_ENV_ATTR_ID = "MPICH2_env"; //$NON-NLS-1$
	private static final String EFFECTIVE_MPICH2_PROG_ARGS_ATTR_ID = "MPICH2_progArgs"; //$NON-NLS-1$
	private static final String EFFECTIVE_MPICH2_WORKING_DIR_ATTR_ID = "MPICH2_workingDir"; //$NON-NLS-1$

	private final static ArrayAttributeDefinition<String> effectiveMPICH2EnvAttrDef =
		new ArrayAttributeDefinition<String>(EFFECTIVE_MPICH2_ENV_ATTR_ID, Messages.MPICH2ApplicationAttributes_effectiveMPICH2EnvAttrDef_title,
				Messages.MPICH2ApplicationAttributes_effectiveMPICH2EnvAttrDef_description, true, null);

	private final static ArrayAttributeDefinition<String> effectiveMPICH2ProgArgsAttrDef =
		new ArrayAttributeDefinition<String>(EFFECTIVE_MPICH2_PROG_ARGS_ATTR_ID, Messages.MPICH2ApplicationAttributes_effectiveMPICH2ProgArgsAttrDef_title,
				Messages.MPICH2ApplicationAttributes_effectiveMPICH2ProgArgsAttrDef_description, true, null);

	private final static StringAttributeDefinition effectiveMPICH2WorkingDirAttrDef =
		new StringAttributeDefinition(EFFECTIVE_MPICH2_WORKING_DIR_ATTR_ID, Messages.MPICH2ApplicationAttributes_effectiveMPICH2WorkingDirAttrDef_title,
				Messages.MPICH2ApplicationAttributes_effectiveMPICH2WorkingDirAttrDef_description, true, ""); //$NON-NLS-1$

	/**
	 * Environment variables that MPICH2 has passed to the processes.
	 */
	public static ArrayAttributeDefinition<String> getEffectiveMPICH2EnvAttributeDefinition() {
		return effectiveMPICH2EnvAttrDef;
	}

	/**
	 * Program arguments that MPICH2 has passed to the processes.
	 */
	public static ArrayAttributeDefinition<String> getEffectiveMPICH2ProgArgsAttributeDefinition() {
		return effectiveMPICH2ProgArgsAttrDef;
	}

	/**
	 * Working directory where MPICH2 has started the processes.
	 */
	public static StringAttributeDefinition getEffectiveMPICH2WorkingDirAttributeDefinition() {
		return effectiveMPICH2WorkingDirAttrDef;
	}
}
