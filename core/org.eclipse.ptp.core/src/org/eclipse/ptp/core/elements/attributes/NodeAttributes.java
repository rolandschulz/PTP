package org.eclipse.ptp.core.elements.attributes;

import org.eclipse.ptp.core.attributes.EnumeratedAttributeDefinition;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IEnumeratedAttributeDefinition;
import org.eclipse.ptp.core.attributes.IIntegerAttributeDefinition;
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

	private static final String STATE_ATTR_ID = "nodeState";
	private static final String NUMBER_ATTR_ID = "nodeNumber";

	private final static IEnumeratedAttributeDefinition stateAttrDef = 
		new EnumeratedAttributeDefinition(STATE_ATTR_ID, "state", "Node State", State.UNKNOWN, State.values());
	
	private final static IIntegerAttributeDefinition numAttrDef = 
		new IntegerAttributeDefinition(NUMBER_ATTR_ID, NUMBER_ATTR_ID, "Node Number", 0);
	
	public static IEnumeratedAttributeDefinition getStateAttributeDefinition() {
		return stateAttrDef;
	}
	
	public static IIntegerAttributeDefinition getNumberAttributeDefinition() {
		return numAttrDef;
	}
	
	public static IAttributeDefinition[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[]{stateAttrDef, numAttrDef};
	}
}
