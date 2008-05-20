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

/*
 * Utility routines for constructing and sending proxy runtime events.
 * 
 * These routines define the format of runtime events for communication with
 * runtime proxy clients.
 * 
 * See org.eclipse.ptp.rtsystem.AbstractProxyRuntimeSystem for a description
 * of the protocol format.
 */

#ifdef __gnu_linux__
#define _GNU_SOURCE
#endif /* __gnu_linux__ */

#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <ctype.h>

#include "proxy.h"
#include "proxy_msg.h"
#include "args.h"
#include "list.h"
#include "compat.h"

/*
 * Add a string attribute to an existing event.
 */
void
proxy_add_string_attribute(proxy_msg *m, char *attr, char *value)
{
	proxy_msg_add_keyval_string(m, attr, value);
}

/*
 * Add an integer attribute to an existing event.
 */
void
proxy_add_int_attribute(proxy_msg *m, char *attr, int value)
{
	proxy_msg_add_keyval_int(m, attr, value);
}

/*
 * OK EVENT. Sent in response to a sucessfully
 * completed command.
 */
proxy_msg *
proxy_ok_event(int trans_id)
{
 	return new_proxy_msg(PROXY_EV_OK, trans_id);
}

/*
 * SHUTDOWN EVENT. Sent in response to a quit command.
 */
proxy_msg *
proxy_shutdown_event(int trans_id)
{
 	return new_proxy_msg(PROXY_EV_SHUTDOWN, trans_id);
}

/*
 * MESSAGE EVENT. Used for sending messages that
 * should be logged by the client.
 */
proxy_msg *
proxy_message_event(int trans_id, char *level, int code, char *fmt, ...)
{
	va_list		ap;
	char *		msg;
	proxy_msg *	m = new_proxy_msg(PROXY_EV_MESSAGE, trans_id);
	
	va_start(ap, fmt);
	vasprintf(&msg, fmt, ap);
	va_end(ap);
	
	proxy_msg_add_int(m, 3); /* 3 attributes */
	proxy_msg_add_keyval_string(m, MSG_LEVEL_ATTR, level);
	proxy_msg_add_keyval_int(m, MSG_CODE_ATTR, code);
	proxy_msg_add_keyval_string(m, MSG_TEXT_ATTR, msg);
	
	return m;	
}

/*
 * ERROR EVENT. General error event that should be sent to
 * indicate an error condition. NOTE: command errors should
 * use the specific error events below.
 */
proxy_msg *
proxy_error_event(int trans_id, int code, char *fmt, ...)
{
	va_list		ap;
	char *		msg;
	proxy_msg *	m = new_proxy_msg(PROXY_EV_ERROR, trans_id);
	
	va_start(ap, fmt);
	vasprintf(&msg, fmt, ap);
	va_end(ap);

	proxy_msg_add_int(m, 2); /* 2 attributes */
	proxy_msg_add_keyval_int(m, ERROR_CODE_ATTR, code);
	proxy_msg_add_keyval_string(m, ERROR_MSG_ATTR, msg);
	
	return m;
}

/*
 * SUBMITJOB ERROR EVENT. Used to indicate an error when attempting
 * to submit a job. The jobSubId argument is the job submission ID
 * that was specified to the submitJob command. This event should
 * be used to indicate submission errors as soon as the job submission
 * id has been obtained.
 */
proxy_msg *
proxy_submitjob_error_event(int trans_id, char *jobSubId, int code, char *msg)
{
	proxy_msg *	m = new_proxy_msg(PROXY_EV_RT_SUBMITJOB_ERROR, trans_id);
	
	proxy_msg_add_int(m, 3); /* 3 attributes */
	proxy_msg_add_keyval_string(m, JOB_SUB_ID_ATTR, jobSubId);
	proxy_msg_add_keyval_int(m, ERROR_CODE_ATTR, code);
	proxy_msg_add_keyval_string(m, ERROR_MSG_ATTR, msg);

	return m;
}

/*
 * TERMINATEJOB ERROR EVENT. Used to indicate an error when attempting
 * to terminate a job. The jobId argument is the job ID
 * that was specified to the terminateJob command.
 */
proxy_msg *
proxy_terminatejob_error_event(int trans_id, char *jobId, int code, char *msg)
{
	proxy_msg *	m = new_proxy_msg(PROXY_EV_RT_TERMINATEJOB_ERROR, trans_id);
	
	proxy_msg_add_int(m, 3); /* 3 attributes */
	proxy_msg_add_keyval_string(m, JOB_ID_ATTR, jobId);
	proxy_msg_add_keyval_int(m, ERROR_CODE_ATTR, code);
	proxy_msg_add_keyval_string(m, ERROR_MSG_ATTR, msg);

	return m;
}

/*
 * ATTR DEF INT EVENT. Uused to define a new INTEGER attribute that can be used
 * in subsequent events.
 */
proxy_msg *
proxy_attr_def_int_event(int trans_id, char *id, char *name, char *desc, int disp, int def)
{
	proxy_msg *	m = new_proxy_msg(PROXY_EV_RT_ATTR_DEF, trans_id);
	
	proxy_msg_add_int(m, 1); /* 1 attribute def */
	proxy_msg_add_int(m, 5); /* 5 attributes */
	proxy_msg_add_string(m, id);
	proxy_msg_add_string(m, "INTEGER");
	proxy_msg_add_string(m, name);
	proxy_msg_add_string(m, desc);
	proxy_msg_add_string(m, disp ? "true" : "false");
	proxy_msg_add_int(m, def);
	
	return m;	
}

/*
 * ATTR DEF STRING EVENT. Used to define a new STRING attribute that can be used
 * in subsequent events.
 */
proxy_msg *
proxy_attr_def_string_event(int trans_id, char *id, char *name, char *desc, int disp, char *def)
{
	proxy_msg *	m = new_proxy_msg(PROXY_EV_RT_ATTR_DEF, trans_id);
	
	proxy_msg_add_int(m, 1); /* 1 attribute def */
	proxy_msg_add_int(m, 5); /* 5 attributes */
	proxy_msg_add_string(m, id);
	proxy_msg_add_string(m, "STRING");
	proxy_msg_add_string(m, name);
	proxy_msg_add_string(m, desc);
	proxy_msg_add_string(m, disp ? "true" : "false");
	proxy_msg_add_string(m, def);
	
	return m;	
}

/*
 * NEW MACHINE. Used to define new machine model elements.
 */
proxy_msg *
proxy_new_machine_event(int trans_id, char *rm_id, char *machine_id_range, char *name, char *state)
{
	proxy_msg *	m = new_proxy_msg(PROXY_EV_RT_NEW_MACHINE, trans_id);
	
	proxy_msg_add_string(m, rm_id);
	proxy_msg_add_int(m, 1); /* 1 new machine range */
	proxy_msg_add_string(m, machine_id_range);
	proxy_msg_add_int(m, 2); /* 2 attributes */
	proxy_msg_add_keyval_string(m, ELEMENT_NAME_ATTR, name);
	proxy_msg_add_keyval_string(m, MACHINE_STATE_ATTR, state);	
	
	return m;	
}

/*
 * NEW JOB EVENT. Used to create a job model element. Jobs can either be created in
 * response to a submitJob command, or to represent existing jobs in a queue (for example).
 * 
 * New jobs created in response to a submitJob event *MUST* provide the job submission ID.
 */
proxy_msg *
proxy_new_job_event(int trans_id, char *queue_id, char *job_id_range, char *name, char *state, char *jobSubId)
{
	proxy_msg *	m = new_proxy_msg(PROXY_EV_RT_NEW_JOB, trans_id);
	
	proxy_msg_add_string(m, queue_id);	
	proxy_msg_add_int(m, 1); /* 1 new job range */
	proxy_msg_add_string(m, job_id_range);
	
	if (jobSubId != NULL) {
		proxy_msg_add_int(m, 3); /* 3 attributes */
		proxy_msg_add_keyval_string(m, JOB_SUB_ID_ATTR, jobSubId);
	} else {
		proxy_msg_add_int(m, 2); /* 2 attributes */
	}
	
	proxy_msg_add_keyval_string(m, ELEMENT_NAME_ATTR, name);
	proxy_msg_add_keyval_string(m, JOB_STATE_ATTR, state);
	
	return m;	
}

/*
 * NEW NODE EVENT. Used to create new node model elements.
 */
proxy_msg *
proxy_new_node_event(int trans_id, char *mach_id, int num_nodes)
{
	proxy_msg *	m = new_proxy_msg(PROXY_EV_RT_NEW_NODE, trans_id);
	
	proxy_msg_add_string(m, mach_id);
	proxy_msg_add_int(m, num_nodes);
	
	return m;
}

/*
 * Add a node to a new node event.
 */
void
proxy_add_node(proxy_msg *m, char *node_id, char *name, char *state, int extra_attrs)
{
	proxy_msg_add_string(m, node_id);
	proxy_msg_add_int(m, 2 + extra_attrs);
	proxy_msg_add_keyval_string(m, ELEMENT_NAME_ATTR, name);
	proxy_msg_add_keyval_string(m, NODE_STATE_ATTR, state);
}

/*
 * NEW PROCESS EVENT. Used to create a new process model element.
 */
proxy_msg *
proxy_new_process_event(int trans_id, char *job_id, int num_procs)
{
	proxy_msg *	m = new_proxy_msg(PROXY_EV_RT_NEW_PROCESS, trans_id);
	
	proxy_msg_add_string(m, job_id);	
	proxy_msg_add_int(m, num_procs);	
	
	return m;
}

void
proxy_add_process(proxy_msg *m, char *proc_id, char *name, char *state, int extra_attrs)
{
	proxy_msg_add_string(m, proc_id);	
	proxy_msg_add_int(m, 2 + extra_attrs);	
	proxy_msg_add_keyval_string(m, ELEMENT_NAME_ATTR, name);
	proxy_msg_add_keyval_string(m, PROC_STATE_ATTR, state);	
}

/*
 * NEW QUEUE EVENT. Used to create a new queue model element.
 */
proxy_msg *
proxy_new_queue_event(int trans_id, char *rm_id, char *queue_id, char *name, char *state)
{
	proxy_msg *	m = new_proxy_msg(PROXY_EV_RT_NEW_QUEUE, trans_id);
	
	proxy_msg_add_string(m, rm_id);	
	proxy_msg_add_int(m, 1); /* 1 new queue */
	proxy_msg_add_string(m, queue_id);
	proxy_msg_add_int(m, 2); /* 2 attributes */
	proxy_msg_add_keyval_string(m, ELEMENT_NAME_ATTR, name);
	proxy_msg_add_keyval_string(m, QUEUE_STATE_ATTR, state);
	
	return m;	
}
	
/*
 * JOB CHANGE EVENT. Used to change attributes on a job.
 */
proxy_msg *
proxy_job_change_event(int trans_id, char *id_range, int num_attrs)
{
	proxy_msg *	m = new_proxy_msg(PROXY_EV_RT_JOB_CHANGE, trans_id);
	
	proxy_msg_add_int(m, 1); /* 1 id range */
	proxy_msg_add_string(m, id_range);
	proxy_msg_add_int(m, num_attrs);	
	
	return m;	
}

/*
 * MACHINE CHANGE EVENT. Used to change attributes on a machine.
 */
proxy_msg *
proxy_machine_change_event(int trans_id, char *id_range, int num_attrs)
{
	proxy_msg *	m = new_proxy_msg(PROXY_EV_RT_MACHINE_CHANGE, trans_id);
	
	proxy_msg_add_int(m, 1); /* 1 id range */
	proxy_msg_add_string(m, id_range);
	proxy_msg_add_int(m, num_attrs);	
	
	return m;	
}

/*
 * NODE CHANGE EVENT. Used to change attributes on a node.
 */
proxy_msg *
proxy_node_change_event(int trans_id, char *id_range, int num_attrs)
{
	proxy_msg *	m = new_proxy_msg(PROXY_EV_RT_NODE_CHANGE, trans_id);
	
	proxy_msg_add_int(m, 1); /* 1 id range */
	proxy_msg_add_string(m, id_range);
	proxy_msg_add_int(m, num_attrs);	
	
	return m;	
}

/*
 * PROCESS CHANGE EVENT. Used to change attributes on a process.
 */
proxy_msg *
proxy_process_change_event(int trans_id, char *id_range, int num_attrs)
{
	proxy_msg *m = new_proxy_msg(PROXY_EV_RT_PROCESS_CHANGE, trans_id);
	
	proxy_msg_add_int(m, 1); /* 1 id range */
	proxy_msg_add_string(m, id_range);
	proxy_msg_add_int(m, num_attrs);
	
	return m;
}

/*
 * QUEUE CHANGE EVENT. Used to change attributes on a queue.
 */
proxy_msg *
proxy_queue_change_event(int trans_id, char *id_range, int num_attrs)
{
	proxy_msg *m = new_proxy_msg(PROXY_EV_RT_QUEUE_CHANGE, trans_id);
	
	proxy_msg_add_int(m, 1); /* 1 id range */
	proxy_msg_add_string(m, id_range);
	proxy_msg_add_int(m, num_attrs);
	
	return m;
}

/*
 * REMOVE ALL EVENT. Used to remove all model elements.
 */
proxy_msg *
proxy_remove_all_event(int trans_id)
{
	return new_proxy_msg(PROXY_EV_RT_REMOVE_ALL, trans_id);
}

/*
 * REMOVE JOB EVENT. Used to remove job model elements.
 */
proxy_msg *
proxy_remove_job_event(int trans_id, char *id_range)
{
	proxy_msg *m = new_proxy_msg(PROXY_EV_RT_REMOVE_JOB, trans_id);
	
	proxy_msg_add_string(m, id_range);
	
	return m;
}

/*
 * REMOVE MACHINE EVENT. Used to remove machine model elements.
 */
proxy_msg *
proxy_remove_machine_event(int trans_id, char *id_range)
{
	proxy_msg *m = new_proxy_msg(PROXY_EV_RT_REMOVE_MACHINE, trans_id);
	
	proxy_msg_add_string(m, id_range);
	
	return m;
}

/*
 * REMOVE NODE EVENT. Used to remove node model elements.
 */
proxy_msg *
proxy_remove_node_event(int trans_id, char *id_range)
{
	proxy_msg *m = new_proxy_msg(PROXY_EV_RT_REMOVE_NODE, trans_id);
	
	proxy_msg_add_string(m, id_range);
	
	return m;
}

/*
 * REMOVE PROCESS EVENT. Used to remove process model elements.
 */
proxy_msg *
proxy_remove_process_event(int trans_id, char *id_range)
{
	proxy_msg *m = new_proxy_msg(PROXY_EV_RT_REMOVE_PROCESS, trans_id);
	
	proxy_msg_add_string(m, id_range);
	
	return m;
}

/*
 * REMOVE QUEUE EVENT. Used to remove queue model elements.
 */
proxy_msg *
proxy_remove_queue_event(int trans_id, char *id_range)
{
	proxy_msg *m = new_proxy_msg(PROXY_EV_RT_REMOVE_QUEUE, trans_id);
	
	proxy_msg_add_string(m, id_range);
	
	return m;
}
