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
 * Machine attributes
 */
public class MachineAttributes {
	public enum State {
		UNKNOWN,
		UP,
		DOWN,
		ALERT
	};

    private static final String STATE_ATTR_ID = "machineState";
    private static final String NUMNODES_ATTR_ID = "numNodes";

    private final static EnumeratedAttributeDefinition<State> stateAttrDef = 
        new EnumeratedAttributeDefinition<State>(STATE_ATTR_ID, "state", "Machine State",
                State.UNKNOWN);
    
    private final static IntegerAttributeDefinition numNodesAttrDef = 
        new IntegerAttributeDefinition(NUMNODES_ATTR_ID, "nodes", "Number of Nodes",
                0);
    
    public static EnumeratedAttributeDefinition<State> getStateAttributeDefinition() {
        return stateAttrDef;
    }
    
    public static IntegerAttributeDefinition getNumNodesAttributeDefinition() {
        return numNodesAttrDef;
    }
    
	public static IAttributeDefinition[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[]{stateAttrDef, numNodesAttrDef};
	}
}
