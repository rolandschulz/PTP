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
	private static final String EXECNAME_ATTR_ID = "execName";
	private static final String EXECPATH_ATTR_ID = "execPath";
	private static final String WORKINGDIR_ATTR_ID = "workingDir";
	private static final String PROGARGS_ATTR_ID = "progArgs";
	private static final String ENV_ATTR_ID = "env";
	private static final String DEBUG_PATH_ATTR_ID = "debugPath";
	private static final String DEBUG_ARGS_ATTR_ID = "debugArgs";
	private static final String DEBUG_FLAG_ATTR_ID = "debug";

	private final static IEnumeratedAttributeDefinition stateAttrDef = 
		new EnumeratedAttributeDefinition(STATE_ATTR_ID, "state", "Job State", State.STARTED, State.values());

	private final static IIntegerAttributeDefinition subIdAttrDef = 
		new IntegerAttributeDefinition(SUBID_ATTR_ID, SUBID_ATTR_ID, "Job Submission ID", 0);

	private final static IStringAttributeDefinition execNameAttrDef = 
		new StringAttributeDefinition(EXECNAME_ATTR_ID, EXECNAME_ATTR_ID, "Executable Name", "");

	private final static IStringAttributeDefinition execPathAttrDef = 
		new StringAttributeDefinition(EXECPATH_ATTR_ID, EXECPATH_ATTR_ID, "Executable Path", "");

	private final static IStringAttributeDefinition workingDirAttrDef = 
		new StringAttributeDefinition(WORKINGDIR_ATTR_ID, WORKINGDIR_ATTR_ID, "Working Directory", "");

	private final static IArrayAttributeDefinition progArgsAttrDef = 
		new ArrayAttributeDefinition(PROGARGS_ATTR_ID, PROGARGS_ATTR_ID, "Program Arguments", null);

	private final static IArrayAttributeDefinition envAttrDef = 
		new ArrayAttributeDefinition(ENV_ATTR_ID, ENV_ATTR_ID, "Environment", null);

	private final static IStringAttributeDefinition debugPathAttrDef = 
		new StringAttributeDefinition(DEBUG_PATH_ATTR_ID, DEBUG_PATH_ATTR_ID, "Debugger Path", "");

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

	public static IStringAttributeDefinition getDebuggerPathAttributeDefinition() {
		return debugPathAttrDef;
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
					debugPathAttrDef,
					debugArgsAttrDef,
					debugFlagAttrDef
				};
	}
}
