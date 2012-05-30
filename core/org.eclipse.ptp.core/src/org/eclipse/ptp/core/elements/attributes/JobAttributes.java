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
import org.eclipse.ptp.core.messages.Messages;

/**
 * Job attributes
 */
@Deprecated
public class JobAttributes {
	public enum State {
		STARTING,
		RUNNING,
		SUSPENDED,
		COMPLETED
	}

	public static final int IO_FORWARDING_NONE = 0x0;
	public static final int IO_FORWARDING_STDIN = 0x01;
	public static final int IO_FORWARDING_STDOUT = 0x02;
	public static final int IO_FORWARDING_STDERR = 0x04;

	private static final String DEBUG_ARGS_ATTR_ID = "debugArgs"; //$NON-NLS-1$
	private static final String DEBUG_EXEC_NAME_ATTR_ID = "debugExecName"; //$NON-NLS-1$
	private static final String DEBUG_EXEC_PATH_ATTR_ID = "debugExecPath"; //$NON-NLS-1$
	private static final String DEBUG_FLAG_ATTR_ID = "debug"; //$NON-NLS-1$
	private static final String DEBUG_STOP_IN_MAIN_ATTR_ID = "debugStopInMain"; //$NON-NLS-1$
	private static final String DEBUGGER_ID_ATTR_ID = "debugerId"; //$NON-NLS-1$
	private static final String ENV_ATTR_ID = "env"; //$NON-NLS-1$
	private static final String EXEC_NAME_ATTR_ID = "execName"; //$NON-NLS-1$
	private static final String EXEC_PATH_ATTR_ID = "execPath"; //$NON-NLS-1$
	private static final String IO_FORWARDING_ATTR_ID = "ioForwarding"; //$NON-NLS-1$
	private static final String LAUNCHED_BY_PTP_FLAG_ATTR_ID = "launchedByPTP"; //$NON-NLS-1$
	private static final String JOB_ID_ATTR_ID = "jobId"; //$NON-NLS-1$
	private static final String NUM_PROCS_ATTR_ID = "jobNumProcs"; //$NON-NLS-1$
	private static final String PROG_ARGS_ATTR_ID = "progArgs"; //$NON-NLS-1$
	private static final String QUEUEID_ATTR_ID = "queueId"; //$NON-NLS-1$
	private static final String STATE_ATTR_ID = "jobState"; //$NON-NLS-1$
	private static final String STATUS_ATTR_ID = "jobStatus"; //$NON-NLS-1$
	private static final String STATUS_MESSAGE_ATTR_ID = "jobStatusMessage"; //$NON-NLS-1$
	private static final String SUBID_ATTR_ID = "jobSubId"; //$NON-NLS-1$
	private static final String USERID_ATTR_ID = "userId"; //$NON-NLS-1$
	private static final String WORKING_DIR_ATTR_ID = "workingDir"; //$NON-NLS-1$

	private final static ArrayAttributeDefinition<String> debugArgsAttrDef = new ArrayAttributeDefinition<String>(
			DEBUG_ARGS_ATTR_ID, DEBUG_ARGS_ATTR_ID, Messages.JobAttributes_0, false, null);

	private final static StringAttributeDefinition debugExecNameAttrDef = new StringAttributeDefinition(DEBUG_EXEC_NAME_ATTR_ID,
			"Debugger Executable Name", //$NON-NLS-1$
			Messages.JobAttributes_1, false, ""); //$NON-NLS-1$

	private final static StringAttributeDefinition debugExecPathAttrDef = new StringAttributeDefinition(DEBUG_EXEC_PATH_ATTR_ID,
			"Debugger Executable Path", //$NON-NLS-1$
			Messages.JobAttributes_2, false, ""); //$NON-NLS-1$

	private final static BooleanAttributeDefinition debugFlagAttrDef = new BooleanAttributeDefinition(DEBUG_FLAG_ATTR_ID,
			DEBUG_FLAG_ATTR_ID, Messages.JobAttributes_3, false, false);

	private final static BooleanAttributeDefinition debugStopInMainAttrDef = new BooleanAttributeDefinition(
			DEBUG_STOP_IN_MAIN_ATTR_ID, "Stop In Main", //$NON-NLS-1$
			Messages.JobAttributes_4, false, true);

	private final static StringAttributeDefinition debuggerIdAttrDef = new StringAttributeDefinition(DEBUGGER_ID_ATTR_ID,
			"Debugger ID", //$NON-NLS-1$
			"ID of debugger", false, ""); //$NON-NLS-1$ //$NON-NLS-2$

	private final static ArrayAttributeDefinition<String> envAttrDef = new ArrayAttributeDefinition<String>(ENV_ATTR_ID,
			"Environment", //$NON-NLS-1$
			Messages.JobAttributes_5, true, null);

	private final static StringAttributeDefinition execNameAttrDef = new StringAttributeDefinition(EXEC_NAME_ATTR_ID,
			"Executable Name", //$NON-NLS-1$
			Messages.JobAttributes_6, true, ""); //$NON-NLS-1$

	private final static StringAttributeDefinition execPathAttrDef = new StringAttributeDefinition(EXEC_PATH_ATTR_ID,
			"Executable Path", //$NON-NLS-1$
			Messages.JobAttributes_7, true, ""); //$NON-NLS-1$

	private final static IntegerAttributeDefinition ioForwardingAttrDef = new IntegerAttributeDefinition(IO_FORWARDING_ATTR_ID,
			"I/O Forwarding", //$NON-NLS-1$
			Messages.JobAttributes_8, false, IO_FORWARDING_STDOUT);

	private final static BooleanAttributeDefinition launchedByPTPFlagAttrDef = new BooleanAttributeDefinition(
			LAUNCHED_BY_PTP_FLAG_ATTR_ID, "Launched By PTP", //$NON-NLS-1$
			Messages.JobAttributes_9, false, false);

	private final static StringAttributeDefinition jobIdAttrDef = new StringAttributeDefinition(JOB_ID_ATTR_ID, "Job ID", //$NON-NLS-1$
			Messages.JobAttributes_10, false, ""); //$NON-NLS-1$

	private final static IntegerAttributeDefinition numProcsAttrDef = new IntegerAttributeDefinition(NUM_PROCS_ATTR_ID, "Procs", //$NON-NLS-1$
			Messages.JobAttributes_11, true, 1);

	private final static ArrayAttributeDefinition<String> progArgsAttrDef = new ArrayAttributeDefinition<String>(PROG_ARGS_ATTR_ID,
			"Arguments", //$NON-NLS-1$
			Messages.JobAttributes_12, true, null);

	private final static StringAttributeDefinition queueIdAttrDef = new StringAttributeDefinition(QUEUEID_ATTR_ID, QUEUEID_ATTR_ID,
			Messages.JobAttributes_13, false, ""); //$NON-NLS-1$

	private final static EnumeratedAttributeDefinition<State> stateAttrDef = new EnumeratedAttributeDefinition<State>(
			STATE_ATTR_ID, "State", Messages.JobAttributes_14, //$NON-NLS-1$
			false, State.STARTING);

	private final static StringAttributeDefinition statusAttrDef = new StringAttributeDefinition(STATUS_ATTR_ID,
			"Status", Messages.JobAttributes_19, //$NON-NLS-1$
			true, ""); //$NON-NLS-1$

	private final static StringAttributeDefinition statusMessageAttrDef = new StringAttributeDefinition(STATUS_MESSAGE_ATTR_ID,
			"Status Message", //$NON-NLS-1$
			Messages.JobAttributes_18, true, ""); //$NON-NLS-1$

	private final static StringAttributeDefinition subIdAttrDef = new StringAttributeDefinition(SUBID_ATTR_ID, "Job Submission ID", //$NON-NLS-1$
			Messages.JobAttributes_15, false, ""); //$NON-NLS-1$

	private final static StringAttributeDefinition userIdAttrDef = new StringAttributeDefinition(USERID_ATTR_ID, "User", //$NON-NLS-1$
			Messages.JobAttributes_16, true, ""); //$NON-NLS-1$

	private final static StringAttributeDefinition workingDirAttrDef = new StringAttributeDefinition(WORKING_DIR_ATTR_ID,
			"Working Directory", //$NON-NLS-1$
			Messages.JobAttributes_17, true, ""); //$NON-NLS-1$

	public static BooleanAttributeDefinition getDebugFlagAttributeDefinition() {
		return debugFlagAttrDef;
	}

	public static ArrayAttributeDefinition<String> getDebuggerArgumentsAttributeDefinition() {
		return debugArgsAttrDef;
	}

	public static StringAttributeDefinition getDebuggerExecutableNameAttributeDefinition() {
		return debugExecNameAttrDef;
	}

	public static StringAttributeDefinition getDebuggerExecutablePathAttributeDefinition() {
		return debugExecPathAttrDef;
	}

	public static StringAttributeDefinition getDebuggerIdAttributeDefinition() {
		return debuggerIdAttrDef;
	}

	public static BooleanAttributeDefinition getDebuggerStopInMainFlagAttributeDefinition() {
		return debugStopInMainAttrDef;
	}

	public static IAttributeDefinition<?, ?, ?>[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[] { debugArgsAttrDef, debugExecNameAttrDef, debugExecPathAttrDef, debugFlagAttrDef,
				debugStopInMainAttrDef, debuggerIdAttrDef, envAttrDef, execNameAttrDef, execPathAttrDef, ioForwardingAttrDef,
				jobIdAttrDef, launchedByPTPFlagAttrDef, numProcsAttrDef, progArgsAttrDef, queueIdAttrDef, stateAttrDef,
				statusAttrDef, statusMessageAttrDef, subIdAttrDef, userIdAttrDef, workingDirAttrDef, };
	}

	public static ArrayAttributeDefinition<String> getEnvironmentAttributeDefinition() {
		return envAttrDef;
	}

	public static StringAttributeDefinition getExecutableNameAttributeDefinition() {
		return execNameAttrDef;
	}

	public static StringAttributeDefinition getExecutablePathAttributeDefinition() {
		return execPathAttrDef;
	}

	public static IntegerAttributeDefinition getIOForwardingAttributeDefinition() {
		return ioForwardingAttrDef;
	}

	public static StringAttributeDefinition getJobIdAttributeDefinition() {
		return jobIdAttrDef;
	}

	public static BooleanAttributeDefinition getLaunchedByPTPFlagAttributeDefinition() {
		return launchedByPTPFlagAttrDef;
	}

	public static IntegerAttributeDefinition getNumberOfProcessesAttributeDefinition() {
		return numProcsAttrDef;
	}

	public static ArrayAttributeDefinition<String> getProgramArgumentsAttributeDefinition() {
		return progArgsAttrDef;
	}

	public static StringAttributeDefinition getQueueIdAttributeDefinition() {
		return queueIdAttrDef;
	}

	public static EnumeratedAttributeDefinition<State> getStateAttributeDefinition() {
		return stateAttrDef;
	}

	public static StringAttributeDefinition getStatusAttributeDefinition() {
		return statusAttrDef;
	}

	public static StringAttributeDefinition getStatusMessageAttributeDefinition() {
		return statusMessageAttrDef;
	}

	public static StringAttributeDefinition getSubIdAttributeDefinition() {
		return subIdAttrDef;
	}

	public static StringAttributeDefinition getUserIdAttributeDefinition() {
		return userIdAttrDef;
	}

	public static StringAttributeDefinition getWorkingDirectoryAttributeDefinition() {
		return workingDirAttrDef;
	}
}
