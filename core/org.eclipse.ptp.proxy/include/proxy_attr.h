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
#define PTP_ELEMENT_ID_ATTR				"id"
#define PTP_ELEMENT_NAME_ATTR			"name"

/*
 * Filter attributes
 */
#define PTP_FILTER_CHILDREN_ATTR		"filterChildren"
/*
 * Machine attributes
 */
#define PTP_MACHINE_STATE_ATTR			"machineState"
#define 	PTP_MACHINE_STATE_UNKNOWN		"UNKNOWN"
#define 	PTP_MACHINE_STATE_UP			"UP"
#define 	PTP_MACHINE_STATE_DOWN			"DOWN"
#define 	PTP_MACHINE_STATE_ALERT			"ALERT"

/*
 * Job attributes
 */
#define PTP_JOB_STATE_ATTR				"jobState"
#define 	PTP_JOB_STATE_STARTING			"STARTING"
#define 	PTP_JOB_STATE_RUNNING			"RUNNING"
#define 	PTP_JOB_STATE_SUSPENDED			"SUSPENDED"
#define 	PTP_JOB_STATE_COMPLETED			"COMPLETED"
#define PTP_JOB_STATUS_ATTR				"jobStatus"
#define PTP_JOB_SUB_ID_ATTR				"jobSubId"
#define PTP_JOB_ID_ATTR					"jobId"
#define PTP_JOB_NUM_PROCS_ATTR			"jobNumProcs"
#define PTP_JOB_EXEC_NAME_ATTR			"execName"
#define PTP_JOB_EXEC_PATH_ATTR			"execPath"
#define PTP_JOB_WORKING_DIR_ATTR		"workingDir"
#define PTP_JOB_PROG_ARGS_ATTR			"progArgs"
#define PTP_JOB_ENV_ATTR				"env"
#define PTP_JOB_DEBUG_EXEC_NAME_ATTR	"debugExecName"
#define PTP_JOB_DEBUG_EXEC_PATH_ATTR	"debugExecPath"
#define PTP_JOB_DEBUG_ARGS_ATTR			"debugArgs"
#define PTP_JOB_DEBUG_FLAG_ATTR			"debug"

/*
 * Node attributes
 */
#define PTP_NODE_STATE_ATTR				"nodeState"
#define 	PTP_NODE_STATE_UP				"UP"
#define 	PTP_NODE_STATE_DOWN				"DOWN"
#define 	PTP_NODE_STATE_ERROR			"ERROR"
#define 	PTP_NODE_STATE_UNKNOWN			"UNKNOWN"
#define PTP_NODE_NUMBER_ATTR			"nodeNumber"

/*
 * Queue attributes
 */
#define PTP_QUEUE_STATE_ATTR			"queueState"
#define 	PTP_QUEUE_STATE_NORMAL			"NORMAL"
#define		PTP_QUEUE_STATE_STOPPED			"STOPPED"
#define PTP_QUEUE_STATUS_ATTR			"queueStatus"
#define PTP_QUEUE_ID_ATTR				"queueId"

/*
 * Process attributes
 */
#define PTP_PROC_STATE_ATTR				"processState"
#define		PTP_PROC_STATE_STARTING			"STARTING"
#define 	PTP_PROC_STATE_RUNNING			"RUNNING"
#define		PTP_PROC_STATE_SUSPENDED		"SUSPENDED"
#define		PTP_PROC_STATE_COMPLETED		"COMPLETED"
#define PTP_PROC_STATUS_ATTR			"processStatus"
#define PTP_PROC_NODEID_ATTR			"processNodeId"
#define PTP_PROC_PID_ATTR				"processPID"
#define PTP_PROC_INDEX_ATTR				"processIndex"
#define PTP_PROC_STDOUT_ATTR			"processStdout"
#define PTP_PROC_STDERR_ATTR			"processStderr"
#define PTP_PROC_EXITCODE_ATTR			"processExitCode"
#define PTP_PROC_SIGNALNAME_ATTR		"processSignalName"

/*
 * Message attributes
 */
#define PTP_MSG_LEVEL_ATTR				"messageLevel"
#define		PTP_MSG_LEVEL_FATAL				"FATAL"
#define		PTP_MSG_LEVEL_ERROR				"ERROR"
#define		PTP_MSG_LEVEL_WARNING			"WARNING"
#define		PTP_MSG_LEVEL_INFO				"INFO"
#define PTP_MSG_CODE_ATTR				"messageCode"
#define PTP_MSG_TEXT_ATTR				"messageText"

/*
 * ERROR attributes
 */
#define PTP_ERROR_CODE_ATTR				"errorCode"
#define PTP_ERROR_MSG_ATTR				"errorMsg"

/*
 * Miscellaneous attributes
 */
#define PTP_PROTOCOL_VERSION_ATTR		"version"
#define PTP_BASE_ID_ATTR				"baseId"

extern int		proxy_test_attribute(char *key, char *attr_str);
extern char *	proxy_copy_attribute_name(char *attr_str);
extern char *	proxy_copy_attribute_name_filter(char *attr_str);
extern char *	proxy_get_attribute_value_str(char *attr_str);
extern int		proxy_get_attribute_value_int(char *attr_str);
extern int		proxy_get_attribute_value_bool(char *attr_str);
#endif /*PROXY_ATTR_H_*/
