package org.eclipse.ptp.core.elements.attributes;

import org.eclipse.ptp.core.attributes.EnumeratedAttributeDefinition;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
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
	private static final String EXIT_CODE_ATTR_ID = "processExitCode";
	private static final String SIGNAL_NAME_ATTR_ID = "processSignalName";
	private static final String TASKID_ATTR_ID = "processTaskId";
	private static final String STDOUT_ATTR_ID = "processStdout";
	private static final String NODEID_ATTR_ID = "processNodeId";

	private final static EnumeratedAttributeDefinition stateAttrDef = 
		new EnumeratedAttributeDefinition(STATE_ATTR_ID, "Process State", "Execution state of a process", State.STARTING, State.values());
	
	private final static IntegerAttributeDefinition pidAttrDef = 
		new IntegerAttributeDefinition(PID_ATTR_ID, "Process ID", "Operating system process ID", 0);
	
	private final static IntegerAttributeDefinition exitCodeAttrDef = 
		new IntegerAttributeDefinition(EXIT_CODE_ATTR_ID, "Process Exit Code", "Operating system exit code", 0);
	
	private final static IntegerAttributeDefinition taskIdAttrDef = 
		new IntegerAttributeDefinition(TASKID_ATTR_ID, "Process Task ID", "MPI task ID of process", 0);
	
	private final static StringAttributeDefinition signalNameAttrDef = 
		new StringAttributeDefinition(SIGNAL_NAME_ATTR_ID, "Process Signal", "Name of signal that caused process termination", "");
	
	private final static StringAttributeDefinition stdoutNameAttrDef = 
		new StringAttributeDefinition(STDOUT_ATTR_ID, "Process Stdout", "Standard output from process", "");
	
	private final static StringAttributeDefinition nodeIdAttrDef = 
		new StringAttributeDefinition(NODEID_ATTR_ID, "Process Node ID", "Node that this process is running on", "");
	
	public static EnumeratedAttributeDefinition getStateAttributeDefinition() {
		return stateAttrDef;
	}
	
	public static IntegerAttributeDefinition getPIDAttributeDefinition() {
		return pidAttrDef;
	}
	
	public static IntegerAttributeDefinition getExitCodeAttributeDefinition() {
		return exitCodeAttrDef;
	}
	
	public static IntegerAttributeDefinition getTaskIdAttributeDefinition() {
		return taskIdAttrDef;
	}
	
	public static StringAttributeDefinition getSignalNameAttributeDefinition() {
		return signalNameAttrDef;
	}
	
	public static StringAttributeDefinition getStdoutAttributeDefinition() {
		return stdoutNameAttrDef;
	}
	
	public static StringAttributeDefinition getNodeIdAttributeDefinition() {
		return nodeIdAttrDef;
	}
	
	public static IAttributeDefinition[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[]{
				stateAttrDef, 
				pidAttrDef, 
				exitCodeAttrDef, 
				taskIdAttrDef, 
				signalNameAttrDef, 
				stdoutNameAttrDef,
				nodeIdAttrDef
			};
	}
}
