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

	private final static EnumeratedAttributeDefinition stateAttrDef = 
		new EnumeratedAttributeDefinition(STATE_ATTR_ID, "Node State", "State of the node", State.UNKNOWN, State.values());
	
	private final static EnumeratedAttributeDefinition extraStateAttrDef = 
		new EnumeratedAttributeDefinition(EXTRA_STATE_ATTR_ID, "Extra Node State", "Extra state information for the node (e.g. job scheduler state)", ExtraState.NONE, ExtraState.values());
	
	private final static IntegerAttributeDefinition numAttrDef = 
		new IntegerAttributeDefinition(NUMBER_ATTR_ID, "Node Number", "Zero-based index of node", 0);
	
	public static EnumeratedAttributeDefinition getStateAttributeDefinition() {
		return stateAttrDef;
	}
	
	public static EnumeratedAttributeDefinition getExtraStateAttributeDefinition() {
		return extraStateAttrDef;
	}

	public static IntegerAttributeDefinition getNumberAttributeDefinition() {
		return numAttrDef;
	}
	
	public static IAttributeDefinition[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[]{stateAttrDef, extraStateAttrDef, numAttrDef};
	}
}
