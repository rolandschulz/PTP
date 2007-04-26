package org.eclipse.ptp.core.elements.attributes;

import org.eclipse.ptp.core.attributes.EnumeratedAttributeDefinition;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IEnumeratedAttributeDefinition;
import org.eclipse.ptp.core.attributes.IIntegerAttributeDefinition;
import org.eclipse.ptp.core.attributes.IntegerAttributeDefinition;


/**
 * Job attributes
 */
public class JobAttributes {
	public enum State {
		STARTED,
		RUNNING,
		ABORTED,
		STOPPED
	};
	
	public static final String STATE_ATTR_ID = "jobState";
	public static final String SUBID_ATTR_ID = "jobSubId";

	private final static IEnumeratedAttributeDefinition stateAttrDef = 
		new EnumeratedAttributeDefinition(STATE_ATTR_ID, "state", "Job State", State.STARTED, State.values());

	private final static IIntegerAttributeDefinition subIdAttrDef = 
		new IntegerAttributeDefinition(SUBID_ATTR_ID, SUBID_ATTR_ID, "Job Submission ID", 0);

	public static IEnumeratedAttributeDefinition getStateAttributeDefinition() {
		return stateAttrDef;
	}
	
	public static IIntegerAttributeDefinition getSubIdAttributeDefinition() {
		return subIdAttrDef;
	}

	public static IAttributeDefinition[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[]{stateAttrDef, subIdAttrDef};
	}
}
