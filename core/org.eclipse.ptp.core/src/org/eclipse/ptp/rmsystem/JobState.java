package org.eclipse.ptp.rmsystem;

import org.eclipse.ptp.core.attributes.EnumeratedAttributeDefinition;
import org.eclipse.ptp.core.attributes.IEnumeratedAttributeDefinition;


/**
 * Resource manager status
 * </p>
 */
public class JobState {
	public enum State {
		STARTED,
		RUNNING,
		ABORTED,
		STOPPED
	};
	
	public static final String STATE_ATTR_ID = "jobState";

	private final static IEnumeratedAttributeDefinition stateAttrDef = 
		new EnumeratedAttributeDefinition(STATE_ATTR_ID, "state", "Job State", State.STARTED, State.values());
	
	public static IEnumeratedAttributeDefinition getStateAttributeDefinition() {
		return stateAttrDef;
	}
}
