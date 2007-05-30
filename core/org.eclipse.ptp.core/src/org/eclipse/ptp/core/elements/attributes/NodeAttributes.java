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


/**
 * Node attributes
 */
public class NodeAttributes {
	public enum State {
		UNKNOWN,
		UP,
		DOWN,
		ERROR
	};
	
	public enum ExtraState {
		NONE,
		USER_ALLOC_EXCL,
		USER_ALLOC_SHARED,
		OTHER_ALLOC_EXCL,
		OTHER_ALLOC_SHARED
	};

	private static final String STATE_ATTR_ID = "nodeState";
	private static final String EXTRA_STATE_ATTR_ID = "nodeExtraState";
	private static final String NUMBER_ATTR_ID = "nodeNumber";

	private final static EnumeratedAttributeDefinition<State> stateAttrDef = 
		new EnumeratedAttributeDefinition<State>(STATE_ATTR_ID, "Node State", "State of the node",
				State.UNKNOWN);
	
	private final static EnumeratedAttributeDefinition<ExtraState> extraStateAttrDef = 
		new EnumeratedAttributeDefinition<ExtraState>(EXTRA_STATE_ATTR_ID, "Extra Node State",
				"Extra state information for the node (e.g. job scheduler state)", ExtraState.NONE);
	
	private final static IntegerAttributeDefinition numAttrDef = 
		new IntegerAttributeDefinition(NUMBER_ATTR_ID, "Node Number", "Zero-based index of node", 0);
	
	public static EnumeratedAttributeDefinition<State> getStateAttributeDefinition() {
		return stateAttrDef;
	}
	
	public static EnumeratedAttributeDefinition<ExtraState> getExtraStateAttributeDefinition() {
		return extraStateAttrDef;
	}

	public static IntegerAttributeDefinition getNumberAttributeDefinition() {
		return numAttrDef;
	}
	
	public static IAttributeDefinition<?,?,?>[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[]{stateAttrDef, extraStateAttrDef, numAttrDef};
	}
}
