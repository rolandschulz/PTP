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

#ifndef PROXY_ATTR_H_
#define PROXY_ATTR_H_

/*
 * Predefined attribute ID's and values. These must EXACTLY match org.eclipse.ptp.core.elements.attributes
 */

/*
 * Element attributes
 */
#define ELEMENT_ID_ATTR				"id"
#define ELEMENT_NAME_ATTR			"name"

/*
 * Job attributes
 */
#define JOB_STATE_ATTR				"jobState"
#define 	JOB_STATE_INIT				"STARTED"
#define 	JOB_STATE_RUNNING			"RUNNING"
#define 	JOB_STATE_TERMINATED		"TERMINATED"
#define 	JOB_STATE_ERROR				"ERROR"
#define JOB_SUB_ID_ATTR				"jobSubId"
#define JOB_EXEC_NAME_ATTR			"execName"
#define JOB_EXEC_PATH_ATTR			"execPath"
#define JOB_WORKING_DIR_ATTR		"workingDir"
#define JOB_PROG_ARGS_ATTR			"progArgs"
#define JOB_ENV_ATTR				"env"
#define JOB_DEBUG_EXEC_NAME_ATTR	"debugExecName"
#define JOB_DEBUG_EXEC_PATH_ATTR	"debugExecPath"
#define JOB_DEBUG_ARGS_ATTR			"debugArgs"
#define JOB_DEBUG_FLAG_ATTR			"debug"

/*
 * Node attributes
 */
#define NODE_STATE_ATTR				"nodeState"
#define 	NODE_STATE_UP				"UP"
#define 	NODE_STATE_DOWN				"DOWN"
#define 	NODE_STATE_ERROR			"ERROR"
#define 	NODE_STATE_UNKNOWN			"UNKNOWN"
#define NODE_NUMBER_ATTR			"nodeNumber"
#ifdef HAVE_SYS_BPROC_H
#define NODE_GROUP_ATTR				"nodeGroup"
#define NODE_USER_ATTR				"nodeUser"
#define NODE_MODE_ATTR				"nodeMode"
#define 	DEFAULT_NODE_MODE		0111
#endif /* HAVE_SYS_BPROC_H */

/*
 * Process attributes
 */
#define PROC_STATE_ATTR				"processState"
#define		PROC_STATE_STARTING			"STARTING"
#define 	PROC_STATE_RUNNING			"RUNNING"
#define		PROC_STATE_EXITED			"EXITED"
#define		PROC_STATE_EXITED_SIGNALLED	"EXITED_SIGNALLED"
#define		PROC_STATE_STOPPED			"STOPPED"
#define		PROC_STATE_ERROR			"ERROR"
#define PROC_NODEID_ATTR			"processNodeId"
#define PROC_PID_ATTR				"processPID"
#define PROC_NUMBER_ATTR			"processNumber"
#define PROC_STDOUT_ATTR			"processStdout"
#define PROC_EXITCODE_ATTR			"processExitCode"
#define PROC_SIGNALNAME_ATTR		"processSignalName"
#endif /*PROXY_ATTR_H_*/
