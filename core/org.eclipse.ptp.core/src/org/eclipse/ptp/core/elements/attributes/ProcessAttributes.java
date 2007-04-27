package org.eclipse.ptp.core.elements.attributes;

import org.eclipse.ptp.core.attributes.EnumeratedAttributeDefinition;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IEnumeratedAttributeDefinition;


/**
 * Process attributes
 */
public class ProcessAttributes {
	public enum State {
		STARTING,
		RUNNING,
		EXITED,
		EXITED_SIGNALLED,
		STOPPED,
		ERROR
	};

	private static final String STATE_ATTR_ID = "processState";

	private final static IEnumeratedAttributeDefinition stateAttrDef = 
		new EnumeratedAttributeDefinition(STATE_ATTR_ID, "state", "Process State", State.STARTING, State.values());
	
	public static IEnumeratedAttributeDefinition getStateAttributeDefinition() {
		return stateAttrDef;
	}
	
	public static IAttributeDefinition[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[]{stateAttrDef};
	}
}
