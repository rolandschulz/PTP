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
package org.eclipse.ptp.rm.slurm.core;

import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IntegerAttributeDefinition;
import org.eclipse.ptp.rm.slurm.core.messages.Messages;


/**
 * Job attributes
 */
public class SLURMJobAttributes {
	private static final String NUM_NODES_ATTR_ID = "numberOfNodes"; //$NON-NLS-1$
	private static final String TIME_LIMIT_ATTR_ID = "timeLimit"; //$NON-NLS-1$

	private final static IntegerAttributeDefinition numNodesAttrDef = 
		new IntegerAttributeDefinition(NUM_NODES_ATTR_ID, "Nodes",  //$NON-NLS-1$
				Messages.SLURMJobAttributes_0, true, 1);

	private final static IntegerAttributeDefinition timeLimitAttrDef = 
		new IntegerAttributeDefinition(TIME_LIMIT_ATTR_ID, "TimeLimit",  //$NON-NLS-1$
				Messages.SLURMJobAttributes_1, true, 1);

	public static IAttributeDefinition<?,?,?>[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[]{
				numNodesAttrDef,
				timeLimitAttrDef,
			};
	}

	public static IntegerAttributeDefinition getNumberOfNodesAttributeDefinition() {
		return numNodesAttrDef;
	}

	public static IntegerAttributeDefinition getTimeLimitAttributeDefinition() {
		return timeLimitAttrDef;
	}
}
