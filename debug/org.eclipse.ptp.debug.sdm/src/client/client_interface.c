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

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <errno.h>

#include "sdm.h"
#include "dbg.h"
#include "dbg_proxy.h"
#include "session.h"
#include "proxy.h"
#include "proxy_event.h"
#include "bitset.h"
#include "handler.h"

/*
 * The TIMEOUT determines how often the debugger polls progress routines.
 * Too often and it uses too much CPU, too few and things will slow down...
 */
#define CLIENT_TIMEOUT	50000
#define SERVER_TIMEOUT	50000

static sdm_idset		tmp_idset;
static bitset *			tmp_bitset;

sdm_idset
bitset_to_idset(bitset *b)
{
	int bit;

	sdm_set_clear(tmp_idset);

	for (bit = 0; bit < bitset_size(b); bit++) {
		if (bitset_test(b, bit)) {
			sdm_set_add_element(tmp_idset, bit);
		}
	}

	return tmp_idset;
}

bitset *
idset_to_bitset(sdm_idset ids)
{
	sdm_id		id;

	bitset_clear(tmp_bitset);

	for (id = sdm_set_first(ids); !sdm_set_done(ids); id = sdm_set_next(ids)) {
		bitset_set(tmp_bitset, id);
	}

	return tmp_bitset;
}

static int
session_event_handler(sdm_message msg, void *data)
{
	int			len;
	char *		buf;
	dbg_event *	ev;
	proxy_msg *	pmsg;
	session *	s = (session *)data;

	sdm_message_get_payload(msg, &buf, &len);

	if (proxy_deserialize_msg(buf, len, &pmsg) < 0) {
 		fprintf(stderr, "could not deserialize message");
 	 	sdm_message_free(msg);
 	 	return -1;
	}

	if (DbgDeserializeEvent(pmsg->msg_id, pmsg->arg_size, pmsg->args, &ev) < 0) {
 		fprintf(stderr, "bad conversion to debug event");
 	 	sdm_message_free(msg);
 	 	return -1;
	}

	if (s->sess_event_handler != NULL) {
 		s->sess_event_handler(ev, s->sess_event_data);
	}

 	sdm_message_free(msg);

 	return 0;
}

static int
send_command(bitset *procs, int timeout, char *cmd, int len, void *cbdata)
{
	sdm_message 	msg;
	sdm_idset		dest = bitset_to_idset(procs);

	msg = sdm_message_new(cmd, len);
	sdm_aggregate_set_value(sdm_message_get_aggregate(msg), SDM_AGGREGATE_TIMEOUT, timeout);
	sdm_set_union(sdm_message_get_destination(msg), dest);

	sdm_aggregate_message(msg, SDM_AGGREGATE_DOWNSTREAM);

	sdm_message_set_send_callback(msg, sdm_message_free);
	sdm_message_send(msg);

	return 0;
}

/*
 * Session initialization
 */
int
DbgInit(session **sess, int argc, char *argv[])
{
	session *		s;
	
	*sess = s = (session *)malloc(sizeof(session));
	
	tmp_idset = sdm_set_new();
	tmp_bitset = bitset_new(s->sess_procs);

	if (sdm_init(argc, argv) < 0) {
		return -1;
	}

	s->sess_procs = sdm_route_get_size() - 1;

	sdm_aggregate_set_completion_callback(session_event_handler, (void *)s);

	return 0;
}

int
DbgConnect(session *s)
{
	return proxy_clnt_connect(s->sess_proxy);
}

int
DbgCreate(session *s)
{
	return proxy_clnt_create(s->sess_proxy);
}

int
DbgStartSession(session *s, char *dir, char *prog, char *args)
{
	int			res;
	int			len;
	char *		buf;
	bitset *	procs = bitset_new(s->sess_procs);
	proxy_msg *	msg = new_proxy_msg(DBG_STARTSESSION_CMD, 0);

	proxy_msg_add_int(msg, SERVER_TIMEOUT);
	proxy_msg_add_string(msg, dir);
	proxy_msg_add_string(msg, prog);
	proxy_msg_add_string(msg, args);
	proxy_serialize_msg(msg, &buf, &len);

	/*
	 * Create a bitset containing all processes
	 */
	bitset_invert(procs);

	res = send_command(procs, DBG_EV_WAITALL, buf, len, NULL);

	free_proxy_msg(msg);
	bitset_free(procs);
	
	return res;
}

/*
 * Breakpoint operations
 */
int 
DbgSetLineBreakpoint(session *s, bitset *set, int bpid, char *file, int line)
{
	int			res;
	int			len;
	char *		buf;
	proxy_msg *	msg = new_proxy_msg(DBG_SETLINEBREAKPOINT_CMD, 0);

	proxy_msg_add_int(msg, bpid);
	proxy_msg_add_string(msg, file);
	proxy_msg_add_int(msg, line);

	proxy_serialize_msg(msg, &buf, &len);

	res = send_command(set, DBG_EV_WAITALL, buf, len, NULL);

	free_proxy_msg(msg);
	
	return res;
}

int 
DbgSetFuncBreakpoint(session *s, bitset *set, int bpid, char *file, char *func)
{
	proxy_msg *	msg = new_proxy_msg(DBG_SETFUNCBREAKPOINT_CMD, 0);

	proxy_msg_add_bitset(msg, set);
	proxy_msg_add_int(msg, bpid);
	proxy_msg_add_string(msg, file);
	proxy_msg_add_string(msg, func);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

int 
DbgDeleteBreakpoint(session *s, bitset *set, int bpid)
{
	proxy_msg *	msg = new_proxy_msg(DBG_DELETEBREAKPOINT_CMD, 0);
	
	proxy_msg_add_bitset(msg, set);
	proxy_msg_add_int(msg, bpid);
	
	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

int 
DbgEnableBreakpoint(session *s, bitset *set, int bpid)
{
	proxy_msg *	msg = new_proxy_msg(DBG_ENABLEBREAKPOINT_CMD, 0);
	
	proxy_msg_add_bitset(msg, set);
	proxy_msg_add_int(msg, bpid);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

int 
DbgDisableBreakpoint(session *s, bitset *set, int bpid)
{
	proxy_msg *	msg = new_proxy_msg(DBG_DISABLEBREAKPOINT_CMD, 0);
	
	proxy_msg_add_bitset(msg, set);
	proxy_msg_add_int(msg, bpid);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

int 
DbgConditionBreakpoint(session *s, bitset *set, int bpid, char *expr)
{
	proxy_msg *	msg = new_proxy_msg(DBG_CONDITIONBREAKPOINT_CMD, 0);
	
	proxy_msg_add_bitset(msg, set);
	proxy_msg_add_int(msg, bpid);
	proxy_msg_add_string(msg, expr);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

int 
DbgBreakpointAfter(session *s, bitset *set, int bpid, int icount)
{
	proxy_msg *	msg = new_proxy_msg(DBG_BREAKPOINTAFTER_CMD, 0);
	
	proxy_msg_add_bitset(msg, set);
	proxy_msg_add_int(msg, bpid);
	proxy_msg_add_int(msg, icount);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

int 
DbgSetWatchpoint(session *s, bitset *set, int bpid, char *expr, int access, int read, char *condition, int icount)
{
	proxy_msg *	msg = new_proxy_msg(DBG_SETWATCHPOINT_CMD, 0);

	proxy_msg_add_bitset(msg, set);
	proxy_msg_add_int(msg, bpid);
	proxy_msg_add_string(msg, expr);
	proxy_msg_add_int(msg, access);
	proxy_msg_add_int(msg, read);
	proxy_msg_add_string(msg, condition);
	proxy_msg_add_int(msg, icount);
	
	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}
/*
 * Process control operations
 */
int 
DbgGo(session *s, bitset *set)
{
	int			res;
	int			len;
	char *		buf;
	proxy_msg *	msg = new_proxy_msg(DBG_GO_CMD, 0);

	proxy_serialize_msg(msg, &buf, &len);

	res = send_command(set, DBG_EV_WAITSOME, buf, len, NULL);

	free_proxy_msg(msg);
	
	return res;
}

int 
DbgStep(session *s, bitset *set, int count, int type)
{
	int			res;
	int			len;
	char *		buf;
	proxy_msg *	msg = new_proxy_msg(DBG_STEP_CMD, 0);
	
	proxy_msg_add_int(msg, count);
	proxy_msg_add_int(msg, type);
	proxy_serialize_msg(msg, &buf, &len);

	res = send_command(set, DBG_EV_WAITSOME, buf, len, NULL);
	
	free_proxy_msg(msg);
	
	return res;
}

int 
DbgTerminate(session *s, bitset *set)
{
	proxy_msg *	msg = new_proxy_msg(DBG_TERMINATE_CMD, 0);

	proxy_msg_add_bitset(msg, set);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

int 
DbgSuspend(session *s, bitset *set)
{
	proxy_msg *	msg = new_proxy_msg(DBG_SUSPEND_CMD, 0);

	proxy_msg_add_bitset(msg, set);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}
/*
 * Stack frame operations
 */
int 
DbgListStackframes(session *s, bitset *set, int low, int depth)
{
	int			res;
	int			len;
	char *		buf;
	proxy_msg *	msg = new_proxy_msg(DBG_LISTSTACKFRAMES_CMD, 0);
	
	proxy_msg_add_int(msg, low);
	proxy_msg_add_int(msg, depth);
	proxy_serialize_msg(msg, &buf, &len);

	res = send_command(set, DBG_EV_WAITALL, buf, len, NULL);
	
	return 0;
}

int 
DbgSetCurrentStackframe(session *s, bitset *set, int level)
{
	proxy_msg *	msg = new_proxy_msg(DBG_SETCURRENTSTACKFRAME_CMD, 0);
	
	proxy_msg_add_bitset(msg, set);
	proxy_msg_add_int(msg, level);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

/*
 * Expression/variable operations
 */
int 
DbgEvaluateExpression(session *s, bitset *set, char *exp)
{
	int			res;
	int			len;
	char *		buf;
	proxy_msg *	msg = new_proxy_msg(DBG_EVALUATEEXPRESSION_CMD, 0);

	proxy_msg_add_string(msg, exp);
	proxy_serialize_msg(msg, &buf, &len);

	res = send_command(set, DBG_EV_WAITSOME, buf, len, NULL);
	
	free_proxy_msg(msg);
	
	return res;
}

int 
DbgGetType(session *s, bitset *set, char *exp)
{
	proxy_msg *	msg = new_proxy_msg(DBG_GETTYPE_CMD, 0);

	proxy_msg_add_bitset(msg, set);
	proxy_msg_add_string(msg, exp);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

int 
DbgListLocalVariables(session *s, bitset *set)
{
	proxy_msg *	msg = new_proxy_msg(DBG_LISTLOCALVARIABLES_CMD, 0);

	proxy_msg_add_bitset(msg, set);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

int 
DbgListArguments(session *s, bitset *set, int low, int high)
{
	proxy_msg *	msg = new_proxy_msg(DBG_LISTARGUMENTS_CMD, 0);

	proxy_msg_add_bitset(msg, set);
	proxy_msg_add_int(msg, low);
	proxy_msg_add_int(msg, high);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

int 
DbgListGlobalVariables(session *s, bitset *set)
{
	proxy_msg *	msg = new_proxy_msg(DBG_LISTGLOBALVARIABLES_CMD, 0);

	proxy_msg_add_bitset(msg, set);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

int
DbgListInfoThreads(session *s, bitset *set) 
{
	proxy_msg *	msg = new_proxy_msg(DBG_LISTINFOTHREADS_CMD, 0);

	proxy_msg_add_bitset(msg, set);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

int 
DbgSetThreadSelect(session *s, bitset *set, int threadNum) 
{
	proxy_msg *	msg = new_proxy_msg(DBG_SETTHREADSELECT_CMD, 0);

	proxy_msg_add_bitset(msg, set);
	proxy_msg_add_int(msg, threadNum);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

int 
DbgStackInfoDepth(session *s, bitset *set) 
{
	proxy_msg *	msg = new_proxy_msg(DBG_STACKINFODEPTH_CMD, 0);

	proxy_msg_add_bitset(msg, set);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

int 
DbgDataReadMemory(session *s, bitset *set, long offset, char *address, char *format, int wordSize, int rows, int cols, char *asChar) 
{
	proxy_msg *	msg = new_proxy_msg(DBG_DATAREADMEMORY_CMD, 0);

	proxy_msg_add_bitset(msg, set);
	proxy_msg_add_int(msg, offset);
	proxy_msg_add_string(msg, address);
	proxy_msg_add_string(msg, format);
	proxy_msg_add_int(msg, wordSize);
	proxy_msg_add_int(msg, rows);
	proxy_msg_add_int(msg, cols);
	proxy_msg_add_string(msg, asChar);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

int 
DbgDataWriteMemory(session *s, bitset *set, long offset, char *address, char *format, int wordSize, char *value) 
{
	proxy_msg *	msg = new_proxy_msg(DBG_DATAWRITEMEMORY_CMD, 0);

	proxy_msg_add_bitset(msg, set);
	proxy_msg_add_int(msg, offset);
	proxy_msg_add_string(msg, address);
	proxy_msg_add_string(msg, format);
	proxy_msg_add_int(msg, wordSize);
	proxy_msg_add_string(msg, value);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

int
DbgListSignals(session *s, bitset *set, char *name)
{
	proxy_msg *	msg = new_proxy_msg(DBG_LISTSIGNALS_CMD, 0);

	proxy_msg_add_bitset(msg, set);
	proxy_msg_add_string(msg, name);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

int
DbgCLIHandle(session *s, bitset *set, char *arg)
{
	proxy_msg *	msg = new_proxy_msg(DBG_CLIHANDLE_CMD, 0);

	proxy_msg_add_bitset(msg, set);
	proxy_msg_add_string(msg, arg);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

int
DbgQuit(session *s)
{
	int			res;
	int			len;
	char *		buf;
	bitset *	procs = bitset_new(s->sess_procs);
	proxy_msg *	msg = new_proxy_msg(DBG_QUIT_CMD, 0);

	proxy_serialize_msg(msg, &buf, &len);

	bitset_invert(procs);

	res = send_command(procs, DBG_EV_WAITALL, buf, len, NULL);

	free_proxy_msg(msg);
	bitset_free(procs);

	return res;
}

int
DbgEvaluatePartialExpression(session *s, bitset *set, char *name, char *key, int listChildren, int express)
{
	proxy_msg *	msg = new_proxy_msg(DBG_EVALUATEPARTIALEXPRESSION_CMD, 0);

	proxy_msg_add_bitset(msg, set);
	proxy_msg_add_string(msg, name);
	proxy_msg_add_string(msg, key);
	proxy_msg_add_int(msg, listChildren);
	proxy_msg_add_int(msg, express);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

int
DbgDeletePartialExpression(session *s, bitset *set, char *arg)
{
	proxy_msg *	msg = new_proxy_msg(DBG_DELETEPARTIALEXPRESSION_CMD, 0);

	proxy_msg_add_bitset(msg, set);
	proxy_msg_add_string(msg, arg);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

/*
 * Event handling
 */
int
DbgProgress(session *s)
{
	return sdm_progress();
}

void
DbgRegisterEventHandler(session *s, void (*event_callback)(dbg_event *, void *), void *data)
{
	s->sess_event_handler = event_callback;
	s->sess_event_data = data;
}

/**
 * Unregister file descriptor handler
 */
void
DbgUnregisterEventHandler(session *s, void (*event_callback)(dbg_event *, void *))
{
	s->sess_event_handler = NULL;
}

/**
 * Register a handler for file descriptor events.
 */
void
DbgRegisterReadFileHandler(session *s, int fd, int (*file_handler)(int, void *), void *data)
{
	RegisterFileHandler(fd, READ_FILE_HANDLER, file_handler, data);
}

void
DbgRegisterWriteFileHandler(session *s, int fd, int (*file_handler)(int, void *), void *data)
{
	RegisterFileHandler(fd, WRITE_FILE_HANDLER, file_handler, data);
}

void
DbgRegisterExceptFileHandler(session *s, int fd, int (*file_handler)(int, void *), void *data)
{
	RegisterFileHandler(fd, EXCEPT_FILE_HANDLER, file_handler, data);
}

/**
 * Unregister file descriptor handler
 */
void
DbgUnregisterFileHandler(session *s, int fd)
{
	UnregisterFileHandler(fd);
}
