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
/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.StringAttributeDefinition;
import org.eclipse.ptp.rm.mpi.mpich2.core.messages.Messages;


/**
 * Node attributes.
 * @author Daniel Felix Ferber
 *
 */
public class MPICH2JobAttributes {

	private static final String JOB_ID_ATTR_ID = "MPICH2_jobid"; //$NON-NLS-1$
	private static final String HOSTNAME_ATTR_ID = "MPICH2_hostname"; //$NON-NLS-1$

	private final static StringAttributeDefinition jobIdAttrDef =
		new StringAttributeDefinition(JOB_ID_ATTR_ID, Messages.MPICH2JobAttributes_mpiJobIdAttrDef_title,
				Messages.MPICH2JobAttributes_mpiJobIdAttrDef_description, true, ""); //$NON-NLS-1$

	private final static StringAttributeDefinition hostnameAttrDef =
		new StringAttributeDefinition(HOSTNAME_ATTR_ID, Messages.MPICH2JobAttributes_hostnameAttrDef_title, 
				Messages.MPICH2JobAttributes_hostnameAttrDef_description, true, ""); //$NON-NLS-1$

	/**
	 */
	public static StringAttributeDefinition getJobIdAttributeDefinition() {
		return jobIdAttrDef;
	}

	/**
	 */
	public static StringAttributeDefinition getHostnameAttributeDefinition() {
		return hostnameAttrDef;
	}

	public static IAttributeDefinition<?,?,?>[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[]{jobIdAttrDef, hostnameAttrDef};
	}


}
