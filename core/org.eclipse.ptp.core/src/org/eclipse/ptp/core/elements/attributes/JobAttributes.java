package org.eclipse.ptp.core.elements.attributes;

import org.eclipse.ptp.core.attributes.ArrayAttributeDefinition;
import org.eclipse.ptp.core.attributes.BooleanAttributeDefinition;
import org.eclipse.ptp.core.attributes.EnumeratedAttributeDefinition;
import org.eclipse.ptp.core.attributes.IArrayAttributeDefinition;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IBooleanAttributeDefinition;
import org.eclipse.ptp.core.attributes.IEnumeratedAttributeDefinition;
import org.eclipse.ptp.core.attributes.IIntegerAttributeDefinition;
import org.eclipse.ptp.core.attributes.IStringAttributeDefinition;
import org.eclipse.ptp.core.attributes.IntegerAttributeDefinition;
import org.eclipse.ptp.core.attributes.StringAttributeDefinition;


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

	private final static IEnumeratedAttributeDefinition stateAttrDef = 
		new EnumeratedAttributeDefinition(STATE_ATTR_ID, "Job State", "State of a job", State.STARTED, State.values());

	private final static IIntegerAttributeDefinition subIdAttrDef = 
		new IntegerAttributeDefinition(SUBID_ATTR_ID, "Job Submission ID", "Temporary ID used for job submission", 0);

	private final static IStringAttributeDefinition execNameAttrDef = 
		new StringAttributeDefinition(EXEC_NAME_ATTR_ID, "Executable Name", "Name of executable to be launched", "");

	private final static IStringAttributeDefinition execPathAttrDef = 
		new StringAttributeDefinition(EXEC_PATH_ATTR_ID, "Executable Path", "Path of executable to be launched", "");

	private final static IStringAttributeDefinition workingDirAttrDef = 
		new StringAttributeDefinition(WORKING_DIR_ATTR_ID, "Working Directory", "Working directory where executable will run", "");

	private final static IArrayAttributeDefinition progArgsAttrDef = 
		new ArrayAttributeDefinition(PROG_ARGS_ATTR_ID, "Program Arguments", "Command-line arguments supplied to executable", null);

	private final static IArrayAttributeDefinition envAttrDef = 
		new ArrayAttributeDefinition(ENV_ATTR_ID, "Environment", "Environment to be supplied to executable on launch", null);

	private final static IStringAttributeDefinition debugExecNameAttrDef = 
		new StringAttributeDefinition(DEBUG_EXEC_NAME_ATTR_ID, "Debugger Executable Name", "Name of debugger executable", "");

	private final static IStringAttributeDefinition debugExecPathAttrDef = 
		new StringAttributeDefinition(DEBUG_EXEC_PATH_ATTR_ID, "Debugger Executable NaPathme", "Path to debugger executable", "");

	private final static IStringAttributeDefinition debugBackendAttrDef = 
		new StringAttributeDefinition(DEBUG_BACKEND_PATH_ATTR_ID, "Debugger Backend Path", "Path to backend debugger", "");

	private final static IArrayAttributeDefinition debugArgsAttrDef = 
		new ArrayAttributeDefinition(DEBUG_ARGS_ATTR_ID, DEBUG_ARGS_ATTR_ID, "Debugger Arguments", null);

	private final static IBooleanAttributeDefinition debugFlagAttrDef = 
		new BooleanAttributeDefinition(DEBUG_FLAG_ATTR_ID, DEBUG_FLAG_ATTR_ID, "Debug Flag", false);

	public static IEnumeratedAttributeDefinition getStateAttributeDefinition() {
		return stateAttrDef;
	}
	
	public static IIntegerAttributeDefinition getSubIdAttributeDefinition() {
		return subIdAttrDef;
	}

	public static IStringAttributeDefinition getExecutableNameAttributeDefinition() {
		return execNameAttrDef;
	}

	public static IStringAttributeDefinition getExecutablePathAttributeDefinition() {
		return execPathAttrDef;
	}

	public static IStringAttributeDefinition getWorkingDirectoryAttributeDefinition() {
		return workingDirAttrDef;
	}

	public static IArrayAttributeDefinition getProgramArgumentsAttributeDefinition() {
		return progArgsAttrDef;
	}

	public static IArrayAttributeDefinition getEnvironmentAttributeDefinition() {
		return envAttrDef;
	}

	public static IStringAttributeDefinition getDebuggerExecutableNameAttributeDefinition() {
		return debugExecNameAttrDef;
	}

	public static IStringAttributeDefinition getDebuggerExecutablePathAttributeDefinition() {
		return debugExecPathAttrDef;
	}

	public static IStringAttributeDefinition getDebuggerBackendPathAttributeDefinition() {
		return debugBackendAttrDef;
	}

	public static IArrayAttributeDefinition getDebuggerArgumentsAttributeDefinition() {
		return debugArgsAttrDef;
	}

	public static IBooleanAttributeDefinition getDebugFlagAttributeDefinition() {
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
