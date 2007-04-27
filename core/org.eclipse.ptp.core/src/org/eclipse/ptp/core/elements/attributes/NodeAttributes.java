package org.eclipse.ptp.core.elements.attributes;

import org.eclipse.ptp.core.attributes.EnumeratedAttributeDefinition;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IEnumeratedAttributeDefinition;


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

	private final static IEnumeratedAttributeDefinition stateAttrDef = 
		new EnumeratedAttributeDefinition(STATE_ATTR_ID, "state", "Node State", State.UNKNOWN, State.values());
	
	public static IEnumeratedAttributeDefinition getStateAttributeDefinition() {
		return stateAttrDef;
	}
	
	public static IAttributeDefinition[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[]{stateAttrDef};
	}
}
