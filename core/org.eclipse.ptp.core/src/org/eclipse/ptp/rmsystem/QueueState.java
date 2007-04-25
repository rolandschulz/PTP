package org.eclipse.ptp.rmsystem;

import org.eclipse.ptp.core.attributes.EnumeratedAttributeDefinition;
import org.eclipse.ptp.core.attributes.IEnumeratedAttributeDefinition;


/**
 * Resource manager status
 * </p>
 */
public class QueueState {
	public enum State {
		NORMAL,
		COLLECTING,
		DRAINING,
		STOPPED
	};

	public static final String STATE_ATTR_ID = "queueState";

	private final static IEnumeratedAttributeDefinition stateAttrDef = 
		new EnumeratedAttributeDefinition(STATE_ATTR_ID, "state", "Queue State", State.NORMAL, State.values());
	
	public static IEnumeratedAttributeDefinition getStateAttributeDefinition() {
		return stateAttrDef;
	}
}
