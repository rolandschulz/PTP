package org.eclipse.ptp.rmsystem;

import org.eclipse.ptp.core.attributes.EnumeratedAttributeDefinition;
import org.eclipse.ptp.core.attributes.IEnumeratedAttributeDefinition;


/**
 * Resource manager status
 * </p>
 */
public class ResourceManagerState {
	public enum State {
		ERROR,
		STARTED,
		STOPPED,
	    SUSPENDED
	}
	
	public final static String STATE_ATTR_ID = "rmState";

	private final static IEnumeratedAttributeDefinition stateAttrDef = 
		new EnumeratedAttributeDefinition(STATE_ATTR_ID, "state", "RM State", State.STOPPED, State.values());
	
	public static IEnumeratedAttributeDefinition getStateAttributeDefinition() {
		return stateAttrDef;
	}
}