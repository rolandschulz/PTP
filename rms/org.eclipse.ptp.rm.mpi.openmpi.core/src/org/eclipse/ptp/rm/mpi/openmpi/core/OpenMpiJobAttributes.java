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


/**
 * Node attributes
 */
public class OpenMpiJobAttributes {

	public enum MappingMode {
		UNKNOWN,
		BY_NODE,
		BY_SLOT
	};

	private static final String NUM_MAPPED_NODES_ATTR_ID = "numMappedNodes";
	private static final String NUM_APP_CONTEXTS_ATTR_ID = "numAppContexts";
	private static final String MPI_JOB_ID_ATTR_ID = "mpiJobId";
	private static final String VPID_START_ATTR_ID = "vpidStart";
	private static final String VPID_RANGE_ATTR_ID = "vpidRange";
	private static final String MAPPING_MODE_ATTR_ID = "mappingMode";
	private static final String ENV_KEYS_ATTR_ID = "envKeys";

	private final static IntegerAttributeDefinition numMappedNodesDef =
		new IntegerAttributeDefinition(NUM_MAPPED_NODES_ATTR_ID, "Mapped nodes",
				"Number of mapped nodes", true, 0);

	private final static IntegerAttributeDefinition numAppContexts =
		new IntegerAttributeDefinition(NUM_APP_CONTEXTS_ATTR_ID, "Application contexts",
				"Number of application contexts", true, 0);

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

    private final static ArrayAttributeDefinition<String> environmentKetysDefinition =
    	new ArrayAttributeDefinition<String>(ENV_KEYS_ATTR_ID, "Environment variables",
    			"Name of environment variables supplied to the executable", false, new String[0]);

	public static IntegerAttributeDefinition getNumMappedNodesDefinition() {
		return numMappedNodesDef;
	}

	public static IntegerAttributeDefinition getNumAppContextsDefinition() {
		return numAppContexts;
	}

	public static IntegerAttributeDefinition getMpiJobId() {
		return mpiJobId;
	}

	public static IntegerAttributeDefinition getVpidStart() {
		return vpidStart;
	}

	public static IntegerAttributeDefinition getVpidRange() {
		return vpidRange;
	}

	public static EnumeratedAttributeDefinition<MappingMode> getMappingModeDefinition() {
		return mappingModeDefinition;
	}

	public static ArrayAttributeDefinition<String> getEnvironmentKeysDefinition() {
		return environmentKetysDefinition;
	}

	public static IAttributeDefinition<?,?,?>[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[]{numMappedNodesDef,numAppContexts,mpiJobId,vpidStart,vpidRange,mappingModeDefinition};
	}
}
