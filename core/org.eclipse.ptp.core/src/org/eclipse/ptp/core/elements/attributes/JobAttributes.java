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
		PENDING,
		STARTED,
		RUNNING,
		TERMINATED,
		SUSPENDED,
		ERROR,
		UNKNOWN
	};
	
	public static final int IO_FORWARDING_NONE = 0x0;
	public static final int IO_FORWARDING_STDIN = 0x01;
	public static final int IO_FORWARDING_STDOUT = 0x02;
	public static final int IO_FORWARDING_STDERR = 0x04;
	
	private static final String STATE_ATTR_ID = "jobState";
	private static final String SUBID_ATTR_ID = "jobSubId";
	private static final String QUEUEID_ATTR_ID = "queueId";
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
	private static final String IO_FORWARDING_ATTR_ID = "ioForwarding";

	private final static EnumeratedAttributeDefinition<State> stateAttrDef = 
		new EnumeratedAttributeDefinition<State>(STATE_ATTR_ID, "Job State", "State of a job", 
				true, State.STARTED);

	private final static StringAttributeDefinition subIdAttrDef = 
		new StringAttributeDefinition(SUBID_ATTR_ID, "Job Submission ID",
				"Temporary ID used for job submission", false, "");

	private final static StringAttributeDefinition queueIdAttrDef = 
		new StringAttributeDefinition(QUEUEID_ATTR_ID, QUEUEID_ATTR_ID, 
				"Job submission queue ID", true, "");
	
	private final static IntegerAttributeDefinition numProcsAttrDef = 
		new IntegerAttributeDefinition(NUM_PROCS_ATTR_ID, "Number of Processes", 
				"Number of processes to launch", true, 0);

	private final static StringAttributeDefinition execNameAttrDef = 
		new StringAttributeDefinition(EXEC_NAME_ATTR_ID, "Executable Name",
				"Name of executable to be launched", true, "");

	private final static StringAttributeDefinition execPathAttrDef = 
		new StringAttributeDefinition(EXEC_PATH_ATTR_ID, "Executable Path",
				"Path of executable to be launched", true, "");

	private final static StringAttributeDefinition workingDirAttrDef = 
		new StringAttributeDefinition(WORKING_DIR_ATTR_ID, "Working Directory",
				"Working directory where executable will run", true, "");

	private final static ArrayAttributeDefinition<String> progArgsAttrDef = 
		new ArrayAttributeDefinition<String>(PROG_ARGS_ATTR_ID, "Program Arguments",
				"Command-line arguments supplied to executable", true, null);

	private final static ArrayAttributeDefinition<String> envAttrDef = 
		new ArrayAttributeDefinition<String>(ENV_ATTR_ID, "Environment",
				"Environment to be supplied to executable on launch", true, null);

	private final static StringAttributeDefinition debugExecNameAttrDef = 
		new StringAttributeDefinition(DEBUG_EXEC_NAME_ATTR_ID, "Debugger Executable Name",
				"Name of debugger executable", true, "");

	private final static StringAttributeDefinition debugExecPathAttrDef = 
		new StringAttributeDefinition(DEBUG_EXEC_PATH_ATTR_ID, "Debugger Executable Path",
				"Path to debugger executable", true,"");

	private final static ArrayAttributeDefinition<String> debugArgsAttrDef = 
		new ArrayAttributeDefinition<String>(DEBUG_ARGS_ATTR_ID, DEBUG_ARGS_ATTR_ID,
				"Debugger Arguments", true, null);

	private final static BooleanAttributeDefinition debugFlagAttrDef = 
		new BooleanAttributeDefinition(DEBUG_FLAG_ATTR_ID, DEBUG_FLAG_ATTR_ID, 
				"Debug Flag", true, false);

	private final static IntegerAttributeDefinition ioForwardingAttrDef = 
		new IntegerAttributeDefinition(IO_FORWARDING_ATTR_ID, "I/O Forwarding", 
				"Specify which I/O channels to foward", false, IO_FORWARDING_STDOUT);

	public static EnumeratedAttributeDefinition<State> getStateAttributeDefinition() {
		return stateAttrDef;
	}
	
	public static StringAttributeDefinition getSubIdAttributeDefinition() {
		return subIdAttrDef;
	}

	public static StringAttributeDefinition getQueueIdAttributeDefinition() {
		return queueIdAttrDef;
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

	public static IntegerAttributeDefinition getIOForwardingAttributeDefinition() {
		return ioForwardingAttrDef;
	}
	
	public static IAttributeDefinition<?,?,?>[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[]{
					stateAttrDef, 
					subIdAttrDef, 
					queueIdAttrDef,
					numProcsAttrDef,
					execNameAttrDef, 
					execPathAttrDef,
					workingDirAttrDef,
					progArgsAttrDef,
					envAttrDef,
					debugExecNameAttrDef,
					debugExecPathAttrDef,
					debugArgsAttrDef,
					debugFlagAttrDef,
					ioForwardingAttrDef,
				};
	}
}
