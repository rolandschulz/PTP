package org.eclipse.ptp.core.elements.attributes;

import org.eclipse.ptp.core.attributes.EnumeratedAttributeDefinition;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;


/**
 * Resource manager attributes
 */
public class ResourceManagerAttributes {
	public enum State {
		ERROR,
		STARTING,
		STARTED,
		STOPPING,
		STOPPED,
	    SUSPENDED
	}
	
	private final static String STATE_ATTR_ID = "rmState";

	private final static EnumeratedAttributeDefinition<State> stateAttrDef = 
		new EnumeratedAttributeDefinition<State>(STATE_ATTR_ID, "state", "RM State", State.STOPPED);
	
	public static EnumeratedAttributeDefinition<State> getStateAttributeDefinition() {
		return stateAttrDef;
	}
	
	public static IAttributeDefinition[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[]{stateAttrDef};
	}
}