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
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IntegerAttributeDefinition;
import org.eclipse.ptp.core.attributes.StringAttributeDefinition;


/**
 * Node attributes
 */
public class OpenMpiNodeAttributes {

	private static final String NUM_NODES_ID = "numNodes";
	private static final String MAX_NUM_NODES_ID = "maxNumNodes";
	private static final String STATUS_MESSAGE = "statusMessage";

	private final static IntegerAttributeDefinition numNodesDef =
		new IntegerAttributeDefinition(NUM_NODES_ID, "Number of nodes",
				"Number of nodes on the host", true, 0);

	private final static IntegerAttributeDefinition maxNumNodesDef =
		new IntegerAttributeDefinition(MAX_NUM_NODES_ID, "Maximal number of nodes",
				"Maximal number of nodes on the host", true, 0);

	private final static StringAttributeDefinition statusMessageDef =
		new StringAttributeDefinition(STATUS_MESSAGE, "Status message",
				"Status message", true, "");

	/**
	 * Number of slots suggested on the node.
	 * <p>
	 * Note: openmpi 1.2 and 1.3
	 */
	public static IntegerAttributeDefinition getNumberOfNodesAttributeDefinition() {
		return numNodesDef;
	}

	/**
	 * Maximal number of slots on the node. Zero if not set.
	 * <p>
	 * Note: openmpi 1.2 and 1.3
	 */
	public static IntegerAttributeDefinition getMaximalNumberOfNodesAttributeDefinition() {
		return maxNumNodesDef;
	}

	/**
	 * Status message if it was not possible to discover the node.
	 * <p>
	 * Note: openmpi 1.2 and 1.3
	 */
	public static StringAttributeDefinition getStatusMessageDefinition() {
		return statusMessageDef;
	}

	public static IAttributeDefinition<?,?,?>[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[]{numNodesDef,maxNumNodesDef,statusMessageDef};
	}
}
