package org.eclipse.ptp.rmsystem;

import org.eclipse.ptp.core.attributes.EnumeratedAttributeDefinition;
import org.eclipse.ptp.core.attributes.IEnumeratedAttributeDefinition;


/**
 * Resource manager status
 * </p>
 */
public class MachineState {
	public enum State {
		UNKNOWN,
		UP,
		DOWN,
		STOPPED
	};

	public static final String STATE_ATTR_ID = "machineState";

	private final static IEnumeratedAttributeDefinition stateAttrDef = 
		new EnumeratedAttributeDefinition(STATE_ATTR_ID, "state", "Machine State", State.UNKNOWN, State.values());
	
	public static IEnumeratedAttributeDefinition getStateAttributeDefinition() {
		return stateAttrDef;
	}
}
