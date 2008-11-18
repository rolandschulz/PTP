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
import org.eclipse.ptp.core.attributes.BooleanAttributeDefinition;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IntegerAttributeDefinition;
import org.eclipse.ptp.core.attributes.StringAttributeDefinition;
import org.eclipse.ptp.rm.mpi.mpich2.core.messages.Messages;


/**
 * Node attributes
 * @author Daniel Felix Ferber
 */
public class MPICH2NodeAttributes {

	private static final String NUM_NODES_ID = "MPICH2_numNodes"; //$NON-NLS-1$
	private static final String MAX_NUM_NODES_ID = "MPICH2_maxNumNodes"; //$NON-NLS-1$
	private static final String STATUS_MESSAGE = "MPICH2_statusMessage"; //$NON-NLS-1$
	private static final String OVERSUBSCRIBED = "MPICH2_oversubscribed"; //$NON-NLS-1$

	private final static IntegerAttributeDefinition numNodesAttrDef =
		new IntegerAttributeDefinition(NUM_NODES_ID, Messages.MPICH2NodeAttributes_numNodesAttrDef_title,
				Messages.MPICH2NodeAttributes_numNodesAttrDef_description, true, new Integer(0));

	private final static IntegerAttributeDefinition maxNumNodesAttrDef =
		new IntegerAttributeDefinition(MAX_NUM_NODES_ID, Messages.MPICH2NodeAttributes_maxNumNodesAttrDef_title,
				Messages.MPICH2NodeAttributes_maxNumNodesAttrDef_description, true, new Integer(0));

	private final static StringAttributeDefinition statusMessageAttrDef =
		new StringAttributeDefinition(STATUS_MESSAGE, Messages.MPICH2NodeAttributes_statusMessageAttrDef_title,
				Messages.MPICH2NodeAttributes_statusMessageAttrDef_description, true, ""); //$NON-NLS-1$

	private final static BooleanAttributeDefinition oversubscribedAttrDef =
		new BooleanAttributeDefinition(OVERSUBSCRIBED, Messages.MPICH2NodeAttributes_oversubscribedAttrDef_title,
				Messages.MPICH2NodeAttributes_oversubscribedAttrDef_description, true, new Boolean(false));

	/**
	 * Number of slots suggested on the node.
	 */
	public static IntegerAttributeDefinition getNumberOfNodesAttributeDefinition() {
		return numNodesAttrDef;
	}

	/**
	 * Maximal number of slots on the node. Zero if not set.
	 */
	public static IntegerAttributeDefinition getMaximalNumberOfNodesAttributeDefinition() {
		return maxNumNodesAttrDef;
	}

	/**
	 * Status message if it was not possible to discover the node.
	 */
	public static StringAttributeDefinition getStatusMessageAttributeDefinition() {
		return statusMessageAttrDef;
	}

	/**
	 * If node is oversubscribed.
	 */
	public static BooleanAttributeDefinition getOversubscribedAttributeDefinition() {
		return oversubscribedAttrDef;
	}

	public static IAttributeDefinition<?,?,?>[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[]{numNodesAttrDef,maxNumNodesAttrDef,statusMessageAttrDef};
	}
}
