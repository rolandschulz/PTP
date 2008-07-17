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
import org.eclipse.ptp.core.attributes.ArrayAttributeDefinition;
import org.eclipse.ptp.core.attributes.EnumeratedAttributeDefinition;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IntegerAttributeDefinition;
import org.eclipse.ptp.core.attributes.StringAttributeDefinition;


/**
 * Node attributes
 */
public class OpenMpiJobAttributes {

	public enum MappingMode {
		UNKNOWN,
		BY_NODE,
		BY_SLOT
	};

//	private static final String NUM_MAPPED_NODES_ATTR_ID = "Open_MPI_NumMappedNodes";
	private static final String MPI_JOB_ID_ATTR_ID = "Open_MPI_MpiJobId";
	private static final String VPID_START_ATTR_ID = "Open_MPI_VpidStart";
	private static final String VPID_RANGE_ATTR_ID = "Open_MPI_VpidRange";
	private static final String MAPPING_MODE_ATTR_ID = "Open_MPI_MappingMode";
	private static final String HOSTNAME_ATTR_ID = "Open_MPI_HostName";

//	private final static IntegerAttributeDefinition numMappedNodesDef =
//		new IntegerAttributeDefinition(NUM_MAPPED_NODES_ATTR_ID, "Mapped nodes",
//				"Number of mapped nodes", true, 0);

	private final static IntegerAttributeDefinition mpiJobId =
		new IntegerAttributeDefinition(MPI_JOB_ID_ATTR_ID, "openmpi job id",
				"openmpi job id", true, 0);

	private final static IntegerAttributeDefinition vpidStart =
		new IntegerAttributeDefinition(VPID_START_ATTR_ID, "vpid start",
				"vpid start", true, 0);

	private final static IntegerAttributeDefinition vpidRange =
		new IntegerAttributeDefinition(VPID_RANGE_ATTR_ID, "vpid range",
				"vpid range", true, 0);

    private final static EnumeratedAttributeDefinition<MappingMode> mappingModeDefinition =
        new EnumeratedAttributeDefinition<MappingMode>(MAPPING_MODE_ATTR_ID, "Mapping mode", "Mapping mode",
                true, MappingMode.UNKNOWN);
    
    private final static StringAttributeDefinition hostname = 
   	 new StringAttributeDefinition(HOSTNAME_ATTR_ID, "Open MPI job ID", "Open MPI job ID", true, "");
	
 	/**
	 * <p>
	 * openmpi 1.2 and 1.3
 	 */
//	public static IntegerAttributeDefinition getNumMappedNodesDefinition() {
//		return numMappedNodesDef;
//	}

	/**
	 * <p>
	 * openmpi 1.2 only.
	 */
	public static IntegerAttributeDefinition getMpiJobId() {
		return mpiJobId;
	}

	/**
	 * <p>
	 * openmpi 1.2 only.
	 */
	public static IntegerAttributeDefinition getVpidStart() {
		return vpidStart;
	}

	/**
	 * <p>
	 * openmpi 1.2 only.
	 */
	public static IntegerAttributeDefinition getVpidRange() {
		return vpidRange;
	}

	/**
	 * <p>
	 * openmpi 1.2 and 1.3
	 */
	public static EnumeratedAttributeDefinition<MappingMode> getMappingModeDefinition() {
		return mappingModeDefinition;
	}
	
	/**
	 * <p>
	 * openmpi 1.2 only.
	 */
	public static StringAttributeDefinition getHostname() {
		return hostname;
	}
	
	public static IAttributeDefinition<?,?,?>[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[]{mpiJobId,vpidStart,vpidRange,mappingModeDefinition};
	}
	
	
}
