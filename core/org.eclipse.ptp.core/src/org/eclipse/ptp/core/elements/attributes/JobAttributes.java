package org.eclipse.ptp.core.elements.attributes;

import org.eclipse.ptp.core.attributes.ArrayAttributeDefinition;
import org.eclipse.ptp.core.attributes.BooleanAttributeDefinition;
import org.eclipse.ptp.core.attributes.EnumeratedAttributeDefinition;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IntegerAttributeDefinition;
import org.eclipse.ptp.core.attributes.StringAttributeDefinition;


/**
 * Job attributes
 */
public class JobAttributes {
	public enum State {
		STARTED,
		RUNNING,
		TERMINATED,
		ERROR
	};
	
	private static final String STATE_ATTR_ID = "jobState";
	private static final String SUBID_ATTR_ID = "jobSubId";
	private static final String EXEC_NAME_ATTR_ID = "execName";
	private static final String EXEC_PATH_ATTR_ID = "execPath";
	private static final String WORKING_DIR_ATTR_ID = "workingDir";
	private static final String PROG_ARGS_ATTR_ID = "progArgs";
	private static final String ENV_ATTR_ID = "env";
	private static final String DEBUG_EXEC_NAME_ATTR_ID = "debugExecName";
	private static final String DEBUG_EXEC_PATH_ATTR_ID = "debugExecPath";
	private static final String DEBUG_BACKEND_PATH_ATTR_ID = "debugBackendPath";
	private static final String DEBUG_ARGS_ATTR_ID = "debugArgs";
	private static final String DEBUG_FLAG_ATTR_ID = "debug";

	private final static EnumeratedAttributeDefinition stateAttrDef = 
		new EnumeratedAttributeDefinition(STATE_ATTR_ID, "Job State", "State of a job", State.STARTED, State.values());

	private final static IntegerAttributeDefinition subIdAttrDef = 
		new IntegerAttributeDefinition(SUBID_ATTR_ID, "Job Submission ID", "Temporary ID used for job submission", 0);

	private final static StringAttributeDefinition execNameAttrDef = 
		new StringAttributeDefinition(EXEC_NAME_ATTR_ID, "Executable Name", "Name of executable to be launched", "");

	private final static StringAttributeDefinition execPathAttrDef = 
		new StringAttributeDefinition(EXEC_PATH_ATTR_ID, "Executable Path", "Path of executable to be launched", "");

	private final static StringAttributeDefinition workingDirAttrDef = 
		new StringAttributeDefinition(WORKING_DIR_ATTR_ID, "Working Directory", "Working directory where executable will run", "");

	private final static ArrayAttributeDefinition progArgsAttrDef = 
		new ArrayAttributeDefinition(PROG_ARGS_ATTR_ID, "Program Arguments", "Command-line arguments supplied to executable", null);

	private final static ArrayAttributeDefinition envAttrDef = 
		new ArrayAttributeDefinition(ENV_ATTR_ID, "Environment", "Environment to be supplied to executable on launch", null);

	private final static StringAttributeDefinition debugExecNameAttrDef = 
		new StringAttributeDefinition(DEBUG_EXEC_NAME_ATTR_ID, "Debugger Executable Name", "Name of debugger executable", "");

	private final static StringAttributeDefinition debugExecPathAttrDef = 
		new StringAttributeDefinition(DEBUG_EXEC_PATH_ATTR_ID, "Debugger Executable NaPathme", "Path to debugger executable", "");

	private final static StringAttributeDefinition debugBackendAttrDef = 
		new StringAttributeDefinition(DEBUG_BACKEND_PATH_ATTR_ID, "Debugger Backend Path", "Path to backend debugger", "");

	private final static ArrayAttributeDefinition debugArgsAttrDef = 
		new ArrayAttributeDefinition(DEBUG_ARGS_ATTR_ID, DEBUG_ARGS_ATTR_ID, "Debugger Arguments", null);

	private final static BooleanAttributeDefinition debugFlagAttrDef = 
		new BooleanAttributeDefinition(DEBUG_FLAG_ATTR_ID, DEBUG_FLAG_ATTR_ID, "Debug Flag", false);

	public static EnumeratedAttributeDefinition getStateAttributeDefinition() {
		return stateAttrDef;
	}
	
	public static IntegerAttributeDefinition getSubIdAttributeDefinition() {
		return subIdAttrDef;
	}

	public static StringAttributeDefinition getExecutableNameAttributeDefinition() {
		return execNameAttrDef;
	}

	public static StringAttributeDefinition getExecutablePathAttributeDefinition() {
		return execPathAttrDef;
	}

	public static StringAttributeDefinition getWorkingDirectoryAttributeDefinition() {
		return workingDirAttrDef;
	}

	public static ArrayAttributeDefinition getProgramArgumentsAttributeDefinition() {
		return progArgsAttrDef;
	}

	public static ArrayAttributeDefinition getEnvironmentAttributeDefinition() {
		return envAttrDef;
	}

	public static StringAttributeDefinition getDebuggerExecutableNameAttributeDefinition() {
		return debugExecNameAttrDef;
	}

	public static StringAttributeDefinition getDebuggerExecutablePathAttributeDefinition() {
		return debugExecPathAttrDef;
	}

	public static StringAttributeDefinition getDebuggerBackendPathAttributeDefinition() {
		return debugBackendAttrDef;
	}

	public static ArrayAttributeDefinition getDebuggerArgumentsAttributeDefinition() {
		return debugArgsAttrDef;
	}

	public static BooleanAttributeDefinition getDebugFlagAttributeDefinition() {
		return debugFlagAttrDef;
	}

	public static IAttributeDefinition[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[]{
					stateAttrDef, 
					subIdAttrDef, 
					execNameAttrDef, 
					execPathAttrDef,
					workingDirAttrDef,
					progArgsAttrDef,
					envAttrDef,
					debugExecNameAttrDef,
					debugExecPathAttrDef,
					debugBackendAttrDef,
					debugArgsAttrDef,
					debugFlagAttrDef
				};
	}
}
