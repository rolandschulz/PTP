package org.eclipse.ptp.core.elements.attributes;

import org.eclipse.ptp.core.attributes.EnumeratedAttributeDefinition;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IntegerAttributeDefinition;


/**
 * Queue attributes
 */
public class QueueAttributes {
	public enum State {
		NORMAL,
		COLLECTING,
		DRAINING,
		STOPPED
	};

	private static final String STATE_ATTR_ID = "queueState";
	private static final String ID_ATTR_ID = "queueId";

	private final static EnumeratedAttributeDefinition<State> stateAttrDef = 
		new EnumeratedAttributeDefinition<State>(STATE_ATTR_ID, "state", "Queue State",
				State.NORMAL);
	
	private final static IntegerAttributeDefinition idAttrDef = 
		new IntegerAttributeDefinition(ID_ATTR_ID, ID_ATTR_ID, "Queue ID", 0);
	
	public static EnumeratedAttributeDefinition<State> getStateAttributeDefinition() {
		return stateAttrDef;
	}

	public static IntegerAttributeDefinition getIdAttributeDefinition() {
		return idAttrDef;
	}
	
	public static IAttributeDefinition[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[]{stateAttrDef, idAttrDef};
	}
}
