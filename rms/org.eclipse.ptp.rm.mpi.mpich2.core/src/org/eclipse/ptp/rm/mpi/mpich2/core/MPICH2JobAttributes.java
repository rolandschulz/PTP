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
import org.eclipse.ptp.core.attributes.EnumeratedAttributeDefinition;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IntegerAttributeDefinition;
import org.eclipse.ptp.core.attributes.StringAttributeDefinition;
import org.eclipse.ptp.rm.mpi.mpich2.core.messages.Messages;


/**
 * Node attributes.
 * @author Daniel Felix Ferber
 *
 */
public class MPICH2JobAttributes {

	public enum MappingMode {
		UNKNOWN,
		BY_NODE,
		BY_SLOT
	}

	//	private static final String NUM_MAPPED_NODES_ATTR_ID = "MPICH2_NumMappedNodes";
	private static final String MPI_JOB_ID_ATTR_ID = "MPICH2_MPIJobId"; //$NON-NLS-1$
	private static final String VPID_START_ATTR_ID = "MPICH2_VpidStart"; //$NON-NLS-1$
	private static final String VPID_RANGE_ATTR_ID = "MPICH2_VpidRange"; //$NON-NLS-1$
	private static final String MAPPING_MODE_ATTR_ID = "MPICH2_MappingMode"; //$NON-NLS-1$
	private static final String HOSTNAME_ATTR_ID = "MPICH2_HostName"; //$NON-NLS-1$

	//	private final static IntegerAttributeDefinition numMappedNodesDef =
	//		new IntegerAttributeDefinition(NUM_MAPPED_NODES_ATTR_ID, "Mapped nodes",
	//				"Number of mapped nodes", true, 0);

	private final static IntegerAttributeDefinition mpiJobIdAttrDef =
		new IntegerAttributeDefinition(MPI_JOB_ID_ATTR_ID, Messages.MPICH2JobAttributes_mpiJobIdAttrDef_title,
				Messages.MPICH2JobAttributes_mpiJobIdAttrDef_description, true, new Integer(0));

	private final static IntegerAttributeDefinition vpidStartAttrDef =
		new IntegerAttributeDefinition(VPID_START_ATTR_ID, Messages.MPICH2JobAttributes_vpidStartAttrDef_title,
				Messages.MPICH2JobAttributes_vpidStartAttrDef_description, true, new Integer(0));

	private final static IntegerAttributeDefinition vpidRangeAttrDef =
		new IntegerAttributeDefinition(VPID_RANGE_ATTR_ID, Messages.MPICH2JobAttributes_vpidRangeAttrDef_title,
				Messages.MPICH2JobAttributes_vpidRangeAttrDef_description, true, new Integer(0));

	private final static EnumeratedAttributeDefinition<MappingMode> mappingModeAttrDef =
		new EnumeratedAttributeDefinition<MappingMode>(MAPPING_MODE_ATTR_ID, Messages.MPICH2JobAttributes_mappingModeAttrDef_title, Messages.MPICH2JobAttributes_mappingModeAttrDef_description,
				true, MappingMode.UNKNOWN);

	private final static StringAttributeDefinition hostnameAttrDef =
		new StringAttributeDefinition(HOSTNAME_ATTR_ID, Messages.MPICH2JobAttributes_hostnameAttrDef_title, Messages.MPICH2JobAttributes_hostnameAttrDef_description, true, ""); //$NON-NLS-1$

	/**
	 */
	public static IntegerAttributeDefinition getMpiJobIdAttributeDefinition() {
		return mpiJobIdAttrDef;
	}

	/**
	 */
	public static IntegerAttributeDefinition getVpidStartAttributeDefinition() {
		return vpidStartAttrDef;
	}

	/**
	 */
	public static IntegerAttributeDefinition getVpidRangeAttributeDefinition() {
		return vpidRangeAttrDef;
	}

	/**
	 */
	public static EnumeratedAttributeDefinition<MappingMode> getMappingModeAttributeDefinition() {
		return mappingModeAttrDef;
	}

	/**
	 */
	public static StringAttributeDefinition getHostnameAttributeDefinition() {
		return hostnameAttrDef;
	}

	public static IAttributeDefinition<?,?,?>[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[]{mpiJobIdAttrDef,vpidStartAttrDef,vpidRangeAttrDef,mappingModeAttrDef};
	}


}
