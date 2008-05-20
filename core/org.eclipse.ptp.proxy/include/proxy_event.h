/******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California.
 * This material was produced under U.S. Government contract W-7405-ENG-36
 * for Los Alamos National Laboratory, which is operated by the University
 * of California for the U.S. Department of Energy. The U.S. Government has
 * rights to use, reproduce, and distribute this software. NEITHER THE
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified
 * to produce derivative works, such modified software should be clearly  
 * marked, so as not to confuse it with the version available from LANL.
 *
 * Additionally, this program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * LA-CC 04-115
 ******************************************************************************/
 
#ifndef _PROXY_EVENT_H_
#define _PROXY_EVENT_H_

/*
 * Event code definitions.
 * 
 * These are the events used by the proxy runtime system.
 * 
 * Event codes that are marked 'LOCAL EVENT' are *not* sent across the wire. They
 * are used for internal communication only (mainly on the Java side, but can
 * be used by a C client if desired). All other events are sent across
 * the wire.
 */

#define PROXY_EV_OK				0
#define PROXY_EV_MESSAGE		1
#define PROXY_EV_CONNECTED		2	/* LOCAL EVENT */
#define PROXY_EV_DISCONNECTED	3	/* LOCAL EVENT */
#define PROXY_EV_TIMEOUT		4	/* LOCAL EVENT */
#define PROXY_EV_ERROR			5
#define PROXY_EV_SHUTDOWN		6

/*
 * Codes must EXACTLY match org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEvent
 */
#define PROXY_EV_RT_OFFSET				200
/*
 * Internal state events
 */
#define PROXY_EV_RT_STARTUP_STATE		PROXY_EV_RT_OFFSET + 1	/*LOCAL EVENT */
#define PROXY_EV_RT_CONNECTED_STATE		PROXY_EV_RT_OFFSET + 2	/*LOCAL EVENT */
#define PROXY_EV_RT_RUNNING_STATE		PROXY_EV_RT_OFFSET + 3	/*LOCAL EVENT */
#define PROXY_EV_RT_SHUTDOWN_STATE		PROXY_EV_RT_OFFSET + 4	/*LOCAL EVENT */
#define PROXY_EV_RT_ERROR_STATE			PROXY_EV_RT_OFFSET + 5	/*LOCAL EVENT */
/*
 * Internal error events
 */
#define PROXY_EV_RT_STARTUP_ERROR		PROXY_EV_RT_OFFSET + 10	/*LOCAL EVENT */

/*
 * Command error events
 */
#define PROXY_EV_RT_SUBMITJOB_ERROR		PROXY_EV_RT_OFFSET + 11
#define PROXY_EV_RT_TERMINATEJOB_ERROR	PROXY_EV_RT_OFFSET + 12
/*
 * New model element events
 */
#define PROXY_EV_RT_NEW_JOB				PROXY_EV_RT_OFFSET + 20
#define PROXY_EV_RT_NEW_MACHINE			PROXY_EV_RT_OFFSET + 21
#define PROXY_EV_RT_NEW_NODE			PROXY_EV_RT_OFFSET + 22
#define PROXY_EV_RT_NEW_PROCESS			PROXY_EV_RT_OFFSET + 23
#define PROXY_EV_RT_NEW_QUEUE			PROXY_EV_RT_OFFSET + 24
/*
 * Change model element events
 */
#define PROXY_EV_RT_JOB_CHANGE			PROXY_EV_RT_OFFSET + 30
#define PROXY_EV_RT_MACHINE_CHANGE		PROXY_EV_RT_OFFSET + 31
#define PROXY_EV_RT_NODE_CHANGE			PROXY_EV_RT_OFFSET + 32
#define PROXY_EV_RT_PROCESS_CHANGE		PROXY_EV_RT_OFFSET + 33
#define PROXY_EV_RT_QUEUE_CHANGE		PROXY_EV_RT_OFFSET + 34
/*
 * Remove model element events
 */
#define PROXY_EV_RT_REMOVE_ALL			PROXY_EV_RT_OFFSET + 40
#define PROXY_EV_RT_REMOVE_JOB			PROXY_EV_RT_OFFSET + 41
#define PROXY_EV_RT_REMOVE_MACHINE		PROXY_EV_RT_OFFSET + 42
#define PROXY_EV_RT_REMOVE_NODE			PROXY_EV_RT_OFFSET + 43
#define PROXY_EV_RT_REMOVE_PROCESS		PROXY_EV_RT_OFFSET + 44
#define PROXY_EV_RT_REMOVE_QUEUE		PROXY_EV_RT_OFFSET + 45
/*
 * Miscellaneous events
 */
#define PROXY_EV_RT_MESSAGE				PROXY_EV_RT_OFFSET + 50	/* LOCAL EVENT */
#define PROXY_EV_RT_ATTR_DEF			PROXY_EV_RT_OFFSET + 51

void		proxy_add_string_attribute(proxy_msg *m, char *attr, char *value);
void		proxy_add_int_attribute(proxy_msg *m, char *attr, int value);
proxy_msg *	proxy_ok_event(int trans_id);
proxy_msg *	proxy_shutdown_event(int trans_id);
proxy_msg *	proxy_message_event(int trans_id, char *level, int code, char *fmt, ...);
proxy_msg *	proxy_error_event(int trans_id, int code, char *fmt, ...);
proxy_msg *	proxy_submitjob_error_event(int trans_id, char *jobSubId, int code, char *msg);
proxy_msg *	proxy_terminatejob_error_event(int trans_id, char *job_id, int code, char *msg);
proxy_msg *	proxy_attr_def_int_event(int trans_id, char *id, char *name, char *desc, int disp, int def);
proxy_msg *	proxy_attr_def_string_event(int trans_id, char *id, char *name, char *desc, int disp, char *def);
proxy_msg *	proxy_new_machine_event(int trans_id, char *rm_id, char *machine_id_range, char *name, char *state);
proxy_msg *	proxy_new_job_event(int trans_id, char *queue_id, char *job_id_range, char *name, char *state, char *jobSubId);
proxy_msg *	proxy_new_node_event(int trans_id, char *mach_id, int num_nodes);
void		proxy_add_node(proxy_msg *m, char *node_id, char *name, char *state, int extra_attrs);
proxy_msg *	proxy_new_process_event(int trans_id, char *job_id, int num_procs);
void		proxy_add_process(proxy_msg *m, char *proc_id, char *name, char *state, int extra_attrs);
proxy_msg *	proxy_new_queue_event(int trans_id, char *rm_id, char *queue_id, char *name, char *state);
proxy_msg *	proxy_job_change_event(int trans_id, char *id_range, int num_attrs);
proxy_msg *	proxy_machine_change_event(int trans_id, char *id_range, int num_attrs);
proxy_msg *	proxy_node_change_event(int trans_id, char *id_range, int num_attrs);
proxy_msg *	proxy_process_change_event(int trans_id, char *id_range, int num_attrs);
proxy_msg *	proxy_queue_change_event(int trans_id, char *id_range, int num_attrs);
proxy_msg *	proxy_remove_all_event(int trans_id);
proxy_msg *	proxy_remove_job_event(int trans_id, char *id_range);
proxy_msg *	proxy_remove_machine_event(int trans_id, char *id_range);
proxy_msg *	proxy_remove_node_event(int trans_id, char *id_range);
proxy_msg *	proxy_remove_process_event(int trans_id, char *id_range);
proxy_msg *	proxy_remove_queue_event(int trans_id, char *id_range);
#endif /* !_PROXY_EVENT_H_ */
