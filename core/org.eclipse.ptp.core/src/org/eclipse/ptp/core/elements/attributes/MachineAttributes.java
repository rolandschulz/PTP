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
		STOPPED
	};

    private static final String STATE_ATTR_ID = "machineState";
    private static final String STATE_NUMNODES_ID = "numNodes";

    private final static EnumeratedAttributeDefinition<State> stateAttrDef = 
        new EnumeratedAttributeDefinition<State>(STATE_ATTR_ID, "state", "Machine State",
                State.UNKNOWN);
    
    private final static IntegerAttributeDefinition numNodesAttrDef = 
        new IntegerAttributeDefinition(STATE_NUMNODES_ID, "nodes", "Number of Nodes",
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
