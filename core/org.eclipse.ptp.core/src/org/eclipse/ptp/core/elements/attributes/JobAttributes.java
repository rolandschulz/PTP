/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
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
	private static final String NUM_PROCS_ATTR_ID = "jobNumProcs";
	private static final String EXEC_NAME_ATTR_ID = "execName";
	private static final String EXEC_PATH_ATTR_ID = "execPath";
	private static final String WORKING_DIR_ATTR_ID = "workingDir";
	private static final String PROG_ARGS_ATTR_ID = "progArgs";
	private static final String ENV_ATTR_ID = "env";
	private static final String DEBUG_EXEC_NAME_ATTR_ID = "debugExecName";
	private static final String DEBUG_EXEC_PATH_ATTR_ID = "debugExecPath";
	private static final String DEBUG_ARGS_ATTR_ID = "debugArgs";
	private static final String DEBUG_FLAG_ATTR_ID = "debug";

	private final static EnumeratedAttributeDefinition<State> stateAttrDef = 
		new EnumeratedAttributeDefinition<State>(STATE_ATTR_ID, "Job State", "State of a job",
				State.STARTED);

	private final static StringAttributeDefinition subIdAttrDef = 
		new StringAttributeDefinition(SUBID_ATTR_ID, "Job Submission ID",
				"Temporary ID used for job submission", "");

	private final static IntegerAttributeDefinition numProcsAttrDef = 
		new IntegerAttributeDefinition(NUM_PROCS_ATTR_ID, "Number of Processes", "Number of processes to launch", 0);

	private final static StringAttributeDefinition execNameAttrDef = 
		new StringAttributeDefinition(EXEC_NAME_ATTR_ID, "Executable Name",
				"Name of executable to be launched", "");

	private final static StringAttributeDefinition execPathAttrDef = 
		new StringAttributeDefinition(EXEC_PATH_ATTR_ID, "Executable Path",
				"Path of executable to be launched", "");

	private final static StringAttributeDefinition workingDirAttrDef = 
		new StringAttributeDefinition(WORKING_DIR_ATTR_ID, "Working Directory",
				"Working directory where executable will run", "");

	private final static ArrayAttributeDefinition<String> progArgsAttrDef = 
		new ArrayAttributeDefinition<String>(PROG_ARGS_ATTR_ID, "Program Arguments",
				"Command-line arguments supplied to executable", null);

	private final static ArrayAttributeDefinition<String> envAttrDef = 
		new ArrayAttributeDefinition<String>(ENV_ATTR_ID, "Environment",
				"Environment to be supplied to executable on launch", null);

	private final static StringAttributeDefinition debugExecNameAttrDef = 
		new StringAttributeDefinition(DEBUG_EXEC_NAME_ATTR_ID, "Debugger Executable Name",
				"Name of debugger executable", "");

	private final static StringAttributeDefinition debugExecPathAttrDef = 
		new StringAttributeDefinition(DEBUG_EXEC_PATH_ATTR_ID, "Debugger Executable NaPathme",
				"Path to debugger executable", "");

	private final static ArrayAttributeDefinition<String> debugArgsAttrDef = 
		new ArrayAttributeDefinition<String>(DEBUG_ARGS_ATTR_ID, DEBUG_ARGS_ATTR_ID,
				"Debugger Arguments", null);

	private final static BooleanAttributeDefinition debugFlagAttrDef = 
		new BooleanAttributeDefinition(DEBUG_FLAG_ATTR_ID, DEBUG_FLAG_ATTR_ID, "Debug Flag",
				false);

	public static EnumeratedAttributeDefinition<State> getStateAttributeDefinition() {
		return stateAttrDef;
	}
	
	public static StringAttributeDefinition getSubIdAttributeDefinition() {
		return subIdAttrDef;
	}

	public static IntegerAttributeDefinition getNumberOfProcessesAttributeDefinition() {
		return numProcsAttrDef;
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

	public static ArrayAttributeDefinition<String> getProgramArgumentsAttributeDefinition() {
		return progArgsAttrDef;
	}

	public static ArrayAttributeDefinition<String> getEnvironmentAttributeDefinition() {
		return envAttrDef;
	}

	public static StringAttributeDefinition getDebuggerExecutableNameAttributeDefinition() {
		return debugExecNameAttrDef;
	}

	public static StringAttributeDefinition getDebuggerExecutablePathAttributeDefinition() {
		return debugExecPathAttrDef;
	}

	public static ArrayAttributeDefinition<String> getDebuggerArgumentsAttributeDefinition() {
		return debugArgsAttrDef;
	}

	public static BooleanAttributeDefinition getDebugFlagAttributeDefinition() {
		return debugFlagAttrDef;
	}

	public static IAttributeDefinition<?,?,?>[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[]{
					stateAttrDef, 
					subIdAttrDef, 
					numProcsAttrDef,
					execNameAttrDef, 
					execPathAttrDef,
					workingDirAttrDef,
					progArgsAttrDef,
					envAttrDef,
					debugExecNameAttrDef,
					debugExecPathAttrDef,
					debugArgsAttrDef,
					debugFlagAttrDef
				};
	}
}
