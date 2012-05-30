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
package org.eclipse.ptp.core.elements.attributes;

import org.eclipse.ptp.core.attributes.EnumeratedAttributeDefinition;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IntegerAttributeDefinition;
import org.eclipse.ptp.core.messages.Messages;

/**
 * Machine attributes
 */
@Deprecated
public class MachineAttributes {
	public enum State {
		UP,
		DOWN,
		ALERT,
		ERROR,
		UNKNOWN,
	};

	private static final String STATE_ATTR_ID = "machineState"; //$NON-NLS-1$
	private static final String NUMNODES_ATTR_ID = "numNodes"; //$NON-NLS-1$

	private final static EnumeratedAttributeDefinition<State> stateAttrDef = new EnumeratedAttributeDefinition<State>(
			STATE_ATTR_ID, "state", Messages.MachineAttributes_0, //$NON-NLS-1$
			true, State.UNKNOWN);

	private final static IntegerAttributeDefinition numNodesAttrDef = new IntegerAttributeDefinition(NUMNODES_ATTR_ID,
			"nodes", Messages.MachineAttributes_1, //$NON-NLS-1$
			true, 0);

	public static IAttributeDefinition<?, ?, ?>[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[] { stateAttrDef, numNodesAttrDef };
	}

	public static IntegerAttributeDefinition getNumNodesAttributeDefinition() {
		return numNodesAttrDef;
	}

	public static EnumeratedAttributeDefinition<State> getStateAttributeDefinition() {
		return stateAttrDef;
	}
}
