/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.internal.rm.jaxb.control.core;

import org.eclipse.ptp.internal.rm.jaxb.core.JAXBCoreConstants;

/**
 * Gathers all internal, unmodifiable string constants into a single place for convenience and in the interest of uncluttered code.
 */
public class JAXBControlConstants extends JAXBCoreConstants {

	public static final int COPY_BUFFER_SIZE = 64 * 1024;
	public static final int STREAM_BUFFER_SIZE = 8 * 1024;
	public static final int EOF = -1;
	public static final long MINUTE_IN_MS = 60 * 60 * 1000;

	public static final long STANDARD_WAIT = 1000;
	public static final int READY_FILE_BLOCK = 5;

	/* KEY WORDS */
	public static final String BASIC = "basic";//$NON-NLS-1$
	public static final String CHOICE = "choice";//$NON-NLS-1$
	public static final String DESC = "description";//$NON-NLS-1$
	public static final String MAX = "max";//$NON-NLS-1$
	public static final String MIN = "min";//$NON-NLS-1$
	public static final String READONLY = "readOnly";//$NON-NLS-1$
	public static final String STATUS = "status";//$NON-NLS-1$
	public static final String TOOLTIP = "tooltip";//$NON-NLS-1$
	public static final String TYPE = "type";//$NON-NLS-1$
	public static final String VALIDATOR = "validator";//$NON-NLS-1$
	public static final String SELECTED = "visible";//$NON-NLS-1$
	public static final String JOB_ID_TAG = "@jobId";//$NON-NLS-1$
	public static final String DEAD = "dead";//$NON-NLS-1$

	/* STANDARD PROPERTIES */

	public static final String CSH = "csh";//$NON-NLS-1$
	public static final String SH = ".sh";//$NON-NLS-1$
	public static final String SETENV = "setenv";//$NON-NLS-1$
	public static final String EXPORT = "export";//$NON-NLS-1$

	public static final String CUSTOM = "custom";//$NON-NLS-1$
	public static final String CONTROL_DOT = "control.";//$NON-NLS-1$
	public static final String CONTROL_USER_VAR = "control.user.name";//$NON-NLS-1$
	public static final String CONTROL_USER_NAME = "${ptp_rm:control.user.name#value}";//$NON-NLS-1$
	public static final String CONTROL_QUEUE_VAR = "control.queue.name";//$NON-NLS-1$
	public static final String CONTROL_QUEUE_NAME = "${ptp_rm:control.queue.name#value}";//$NON-NLS-1$
	public static final String CONTROL_ADDRESS_VAR = "control.address";//$NON-NLS-1$
	public static final String CONTROL_WORKING_DIR_VAR = "control.working.dir";//$NON-NLS-1$
	public static final String ARPA = ".in-addr.arpa";//$NON-NLS-1$
	public static final String ECLIPSESETTINGS = ".eclipsesettings";//$NON-NLS-1$
	public static final String DEBUG_PACKAGE = "org.eclipse.debug";//$NON-NLS-1$

	public static final String STARTUP = "OnStartUp";//$NON-NLS-1$
	public static final String SHUTDOWN = "OnShutDown";//$NON-NLS-1$
	public static final String DISCATTR = "DiscoverAttributes";//$NON-NLS-1$
	public static final String JOBSTATUS = "GetJobStatus";//$NON-NLS-1$

	/*
	 * Predefined attributes
	 */
	public static final String JOB_ATTRIBUTE = "jobAttribute";//$NON-NLS-1$
	public static final String ATTRIBUTE = "attribute";//$NON-NLS-1$
	public static final String PROPERTY = "property";//$NON-NLS-1$
	public static final String QUEUES = "available_queues";//$NON-NLS-1$
	public static final String JOB_ID = "job_id";//$NON-NLS-1$
	public static final String RM_ID = "rm_id";//$NON-NLS-1$
	public static final String EXEC_PATH = "executablePath";//$NON-NLS-1$
	public static final String EXEC_DIR = "executableDirectory";//$NON-NLS-1$
	public static final String PROG_ARGS = "progArgs";//$NON-NLS-1$
	public static final String DIRECTORY = "directory";//$NON-NLS-1$
	public static final String MPI_CMD = "mpiCommand";//$NON-NLS-1$
	public static final String MPI_ARGS = "mpiArgs";//$NON-NLS-1$
	public static final String MPI_PROCESSES = "mpiNumberOfProcesses";//$NON-NLS-1$
	public static final String DEBUGGER_EXEC_PATH = "debuggerExecutablePath";//$NON-NLS-1$
	public static final String DEBUGGER_ARGS = "debuggerArgs";//$NON-NLS-1$
	public static final String DEBUGGER_ID = "debuggerId";//$NON-NLS-1$
	public static final String DEBUGGER_LAUNCHER = "debuggerLauncher";//$NON-NLS-1$
	public static final String PTP_DIRECTORY = "ptpDirectory";//$NON-NLS-1$
	public static final String LAUNCH_MODE = "launchMode";//$NON-NLS-1$
	public static final String EMS_ATTR = "ems";//$NON-NLS-1$

	public static final String CASE_INSENSITIVE = "CASE_INSENSITIVE";//$NON-NLS-1$
	public static final String MULTILINE = "MULTILINE";//$NON-NLS-1$
	public static final String DOTALL = "DOTALL";//$NON-NLS-1$
	public static final String UNICODE_CASE = "UNICODE_CASE";//$NON-NLS-1$
	public static final String CANON_EQ = "CANON_EQ";//$NON-NLS-1$
	public static final String LITERAL = "LITERAL";//$NON-NLS-1$
	public static final String COMMENTS = "COMMENTS";//$NON-NLS-1$
	public static final String UNIX_LINES = "UNIX_LINES";//$NON-NLS-1$

	public static final String TOKENIZER_EXT_PT = "streamParserTokenizer";//$NON-NLS-1$
	public static final String TAIL = "tail";//$NON-NLS-1$
	public static final String MINUS_F = "-F";//$NON-NLS-1$

	/*
	 * IRemoteProcessBuilder
	 */
	public static final String TAG_NONE = "NONE";//$NON-NLS-1$
	public static final String TAG_ALLOCATE_PTY = "ALLOCATE_PTY";//$NON-NLS-1$
	public static final String TAG_FORWARD_X11 = "FORWARD_X11";//$NON-NLS-1$
}
