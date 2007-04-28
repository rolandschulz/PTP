package org.eclipse.ptp.orte.core;

import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IntegerAttributeDefinition;


/**
 * ORTE specific attributes
 */
public class ORTEAttributes {
	private static final String NUM_PROCS_ATTR_ID = "jobNumProcs";

	private final static IntegerAttributeDefinition numProcsAttrDef = 
		new IntegerAttributeDefinition(NUM_PROCS_ATTR_ID, "Number of Processes", "Number of processes to launch", 0);

	public static IntegerAttributeDefinition getNumberOfProcessesAttributeDefinition() {
		return numProcsAttrDef;
	}

	public static IAttributeDefinition[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[]{numProcsAttrDef};
	}
}
