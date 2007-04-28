package org.eclipse.ptp.core.elements.attributes;

import org.eclipse.ptp.core.attributes.EnumeratedAttributeDefinition;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IEnumeratedAttributeDefinition;
import org.eclipse.ptp.core.attributes.IIntegerAttributeDefinition;
import org.eclipse.ptp.core.attributes.IStringAttributeDefinition;
import org.eclipse.ptp.core.attributes.IntegerAttributeDefinition;
import org.eclipse.ptp.core.attributes.StringAttributeDefinition;


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
	private static final String PID_ATTR_ID = "processPID";
	private static final String EXITCODE_ATTR_ID = "processExitCode";
	private static final String SIGNALNAME_ATTR_ID = "processSignalName";
	private static final String TASKID_ATTR_ID = "processTaskId";

	private final static IEnumeratedAttributeDefinition stateAttrDef = 
		new EnumeratedAttributeDefinition(STATE_ATTR_ID, "Process State", "Execution state of a process", State.STARTING, State.values());
	
	private final static IIntegerAttributeDefinition pidAttrDef = 
		new IntegerAttributeDefinition(PID_ATTR_ID, "Process ID", "Operating system process ID", 0);
	
	private final static IIntegerAttributeDefinition exitCodeAttrDef = 
		new IntegerAttributeDefinition(EXITCODE_ATTR_ID, "Process Exit Code", "Operating system exit code", 0);
	
	private final static IIntegerAttributeDefinition taskIdAttrDef = 
		new IntegerAttributeDefinition(TASKID_ATTR_ID, "Process Task ID", "MPI task ID of process", 0);
	
	private final static IStringAttributeDefinition signalNameAttrDef = 
		new StringAttributeDefinition(SIGNALNAME_ATTR_ID, "Process Signal", "Name of signal that caused process termination", "");
	
	public static IEnumeratedAttributeDefinition getStateAttributeDefinition() {
		return stateAttrDef;
	}
	
	public static IIntegerAttributeDefinition getPIDAttributeDefinition() {
		return pidAttrDef;
	}
	
	public static IIntegerAttributeDefinition getExitCodeAttributeDefinition() {
		return exitCodeAttrDef;
	}
	
	public static IIntegerAttributeDefinition getTaskIdAttributeDefinition() {
		return taskIdAttrDef;
	}
	
	public static IStringAttributeDefinition getSignalNameAttributeDefinition() {
		return signalNameAttrDef;
	}
	
	public static IAttributeDefinition[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[]{stateAttrDef, pidAttrDef, exitCodeAttrDef, taskIdAttrDef, signalNameAttrDef};
	}
}
