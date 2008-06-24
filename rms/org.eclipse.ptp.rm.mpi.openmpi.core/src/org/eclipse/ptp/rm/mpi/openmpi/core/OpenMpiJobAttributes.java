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

	private static final String NUM_MAPPED_NODES = "numMappedNodes";
	private static final String NUM_APP_CONTEXTS = "numAppContexts";
	private static final String MPI_JOB_ID = "mpiJobId";
	private static final String VPID_START = "vpidStart";
	private static final String VPID_RANGE = "vpidRange";
	private static final String MAPPING_MODE = "mappingMode";

	private final static IntegerAttributeDefinition numMappedNodesDef =
		new IntegerAttributeDefinition(NUM_MAPPED_NODES, "Mapped nodes",
				"Number of mapped nodes", true, 0);

	private final static IntegerAttributeDefinition numAppContexts =
		new IntegerAttributeDefinition(NUM_APP_CONTEXTS, "Application contexts",
				"Number of application contexts", true, 0);

	private final static IntegerAttributeDefinition mpiJobId =
		new IntegerAttributeDefinition(MPI_JOB_ID, "openmpi job id",
				"openmpi job id", true, 0);

	private final static IntegerAttributeDefinition vpidStart =
		new IntegerAttributeDefinition(VPID_START, "vpid start",
				"vpid start", true, 0);

	private final static IntegerAttributeDefinition vpidRange =
		new IntegerAttributeDefinition(VPID_RANGE, "vpid range",
				"vpid range", true, 0);

    private final static EnumeratedAttributeDefinition<MappingMode> mappingModeDefinition =
        new EnumeratedAttributeDefinition<MappingMode>(MAPPING_MODE, "Mapping mode", "Mapping mode",
                true, MappingMode.UNKNOWN);

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

	public static IAttributeDefinition<?,?,?>[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[]{numMappedNodesDef,numAppContexts,mpiJobId,vpidStart,vpidRange,mappingModeDefinition};
	}
}
