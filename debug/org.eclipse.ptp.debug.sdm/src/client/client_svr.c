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

/*
 * The model assumes that each process being debugged will have an associated
 * controller. The controllers are partitioned in a tree such that each controller
 * has a parent (apart from the SDM_MASTER) and multiple children (apart from the leaves).
 * The SDM_MASTER does not control a process; it instead manages communication with the client.
 *
 * A controller receives a message from its parent and is responsible for forwarding
 * the message to a set of destinations. It is also responsible for receiving reply messages from
 * the children, coalescing these into one or more reply messages and forwarding them
 * back to the parent.
 */

#ifdef __gnu_linux__
#define _GNU_SOURCE
#endif /* __gnu_linux__ */

#include "config.h"

#include <sys/time.h>

#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include "compat.h"
#include "dbg.h"
#include "dbg_client.h"
#include "bitset.h"
#include "list.h"
#include "hash.h"
#include "sdm.h"

#define ELAPSED_TIME(t1, t2)	(((t1.tv_sec - t2.tv_sec) * 1000000) + (t1.tv_usec - t2.tv_usec))
#define TIMER_RUNNING			0
#define TIMER_EXPIRED			1
#define TIMER_DISABLED			2

#define	HEX_LEN					8

/*
 * A request represents an asynchronous send/receive transaction between the client
 * and all servers. completed() is called once all replies have been received.
 */
struct request {
	int				id;				/* this request id */
	int				active;			/* this request is active (i.e. has been forwarded) */
	int				pending;		/* there are pending interrupts to be sent */
	int				local;			/* this request must be executed locally */
	sdm_id			src;			/* the source of this request */
	sdm_idset		dest;			/* destination controllers */
	sdm_idset		interrupt;		/* controllers to interrupt */
	char *			msg;			/* buffer to send */
	sdm_idset		outstanding;	/* controllers remaining to send replies */
	int				timeout;		/* wait timeout for this request (microseconds) */
	void *			cb_data;		/* completed request callback data */
	Hash *			replys;			/* hash of replies we've received */
	struct timeval	start_time;		/* time that timer was started */
	int				timer_state;	/* state of timer */
};
typedef struct request	request;

struct reply {
	sdm_id		dest;				/* destination for this reply */
	sdm_idset 	source;				/* controllers that have generated this message */
	char *		msg;				/* message contents */
};
typedef struct reply	reply;

static int				shutting_down = 0;
static sdm_id			this;
static sdm_id			parent;
static List *			all_requests;
static struct request *	current_request;
static void				(*cmd_completed_callback)(sdm_id, sdm_idset, char *, void *);
static void *			local_cmd_data;
static void				(*local_cmd_callback)(char *, void *);
static void *			int_cmd_data;
static void				(*int_cmd_callback)(void *);
static reply *			new_reply(sdm_id, sdm_idset, char *);
static void				free_reply(reply *);
static void				recv_callback(sdm_message msg);

/*
 * Convert a hexadecimal string into an integer.
 */
static unsigned int
str_to_hex(char *str, char **end)
{
	int				n;
	unsigned int	val = 0;

	for (n = 0; n < HEX_LEN && *str != '\0' && isxdigit(*str); n++, str++) {
		val <<= 4;
		val += digittoint(*str);
	}

	if (end != NULL) {
		*end = str;
	}

	return val;
}

/*
 * Convert an integer to a hexadecimal string.
 */
static void
hex_to_str(char *str, char **end, unsigned int val)
{
	sprintf(str, "%08x", val & 0xffffffff);
	*end = str + HEX_LEN;
}

/*
 * Callback when a request is completed.
 */
static void
request_completed(Hash *h, void *data)
{
	HashEntry *	he;
	reply *		r;

	for (HashSet(h); (he = HashGet(h)) != NULL; ) {
		r = (reply *)he->h_data;

		if (cmd_completed_callback != NULL) {
			DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] CALLBACK %s for #%x\n", this, _set_to_str(r->source), he->h_hval);
			cmd_completed_callback(r->dest, r->source, r->msg, data);
		}

		free_reply(r);
		HashRemove(h, he->h_hval);
	}
}

/*
 * Create buffer to send. This consists of a string representation of
 * the 'timeout' value, and the command string.
 */
static void
pack_buffer(int timeout, char *cmd, char **buf, int *len)
{
	int		cmd_len;
	char *	rem;

	if (cmd != NULL)
		cmd_len = strlen(cmd) + 1;
	else
		cmd_len = 0;

	*len = cmd_len + HEX_LEN + 1;
	*buf = (char *)malloc(*len * sizeof(char));
	hex_to_str(*buf, &rem, timeout);
	if (cmd != NULL) {
		memcpy(rem, cmd, cmd_len);
	}
	*(*buf + *len) = '\0';
}

/*
 * Unpack a received buffer
 */
static void
unpack_buffer(char *buf, int len, int *timeout, char **cmd)
{
	*timeout = str_to_hex(buf, &buf);

	/*
	 * Remainder is cmd
	 */
	if (*buf != '\0') {
		*cmd = buf;
	} else {
		*cmd = NULL;
	}
}

/*
 * Create a new request. We assume that there are no outstanding commands active for
 * any controllers in the destination set. Interrupts do not have this restriction.
 *
 * @param src is the source of the request. This is where the reply will be sent to.
 * @param dest is the destination of the request. This is where the message will be forwarded to.
 * @param msg is the message
 * @param timeout is the time to wait before forwarding any replies. 0 means infinite.
 *
  * The outstanding set is all controllers that will respond to this request and once
 * we have received replies from all controllers in this set, the request is complete.
 */
static void
new_request(sdm_id src, sdm_idset dest, char *msg, int timeout, void *cbdata)
{
	request *	r;
	static int	id = 0;

	r = (request *)malloc(sizeof(request));
	r->id = id++;
	r->active = 0;
	r->pending = 0;
	r->local = sdm_set_contains(dest, this);
	r->src = src;
	r->dest = sdm_set_new();
	sdm_set_union(r->dest, dest);
	sdm_set_remove_element(r->dest, this);
	r->msg = strdup(msg);
	r->interrupt = sdm_set_new();
	r->timeout = timeout;
	r->cb_data = cbdata;
	r->timer_state = TIMER_DISABLED;
	r->replys = HashCreate(sdm_route_get_size());
	r->outstanding = sdm_set_new();
	sdm_set_union(r->outstanding, sdm_route_reachable(dest));

	AddToList(all_requests, (void *)r);

	DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] Creating new request #%d (dest %s req_dest %s, replies %s, %s)\n",
			this, r->id, _set_to_str(dest), _set_to_str(r->dest), _set_to_str(r->outstanding),
			r->local ? "local" : "not local");
}

/*
 * Request an interrupt to the pending request.
 */
static void
request_interrupt(request *r, sdm_idset dest)
{
	if (!r->pending) {
		sdm_set_clear(r->interrupt);
		r->pending = 1;
	}

	sdm_set_union(r->interrupt, dest);

	DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] request int %s, set=%s\n", this, _set_to_str(dest), _set_to_str(r->interrupt));
}

/*
 * Free the request.
 */
static void
free_request(request *r)
{
	RemoveFromList(all_requests, (void *)r);
	free(r->msg);
	sdm_set_free(r->dest);
	sdm_set_free(r->interrupt);
	sdm_set_free(r->outstanding);
	HashDestroy(r->replys, NULL);
	free(r);
}

/*
 * Create a new reply. The reply may be only to a subset of the controllers
 * specified in the request.
 *
 * @param dest is the destination to send the reply
 * @param source is the source of the reply
 * @param msg is the reply message
 */
static reply *
new_reply(sdm_id dest, sdm_idset source, char *msg)
{
	reply *	r;

	r = (reply *)malloc(sizeof(reply));
	r->dest = dest;
	r->source = sdm_set_new();
	sdm_set_union(r->source, source);
	r->msg = strdup(msg);

	return r;
}

/*
 * Free the reply
 */
static void
free_reply(reply *r)
{
	sdm_set_free(r->source);
	free(r->msg);
	free(r);
}

/*
 * Start a timer
 */
static void
start_timer(request *r)
{
	(void)gettimeofday(&r->start_time, NULL);
	r->timer_state = TIMER_RUNNING;
}

/*
 * Check if the timer has expired.
 */
static int
check_timer(request *r)
{
	struct timeval	time;

	if (r->timer_state == TIMER_RUNNING) {
		(void)gettimeofday(&time, NULL);

		if (ELAPSED_TIME(time, r->start_time) >= r->timeout) {
			r->timer_state = TIMER_EXPIRED;
		}
	}

	return r->timer_state;
}

/*
 * Disable the timer
 */
static void
disable_timer(request *r)
{
	r->timer_state = TIMER_DISABLED;
}

/*
 * Register completion callback. This callback is made
 * when a controller has received replies from all controllers
 * in the destination set, or the appropriate timeout has expired.
 */
void
ClntSvrRegisterCompletionCallback(void (*func)(sdm_id, sdm_idset, char *, void *))
{
	cmd_completed_callback = func;
}

/*
 * Register local command callback. This callback is made when
 * a command must be executed by this controller.
 */
void
ClntSvrRegisterLocalCmdCallback(void (*func)(char *, void *), void *data)
{
	local_cmd_callback = func;
	local_cmd_data = data;
}

/*
 * Register an interrupt command callback. This callback is made when
 * an interrupt command must be executed by this controller.
 */
void
ClntSvrRegisterInterruptCmdCallback(void (*func)(void *), void *data)
{
	int_cmd_callback = func;
	int_cmd_data = data;
}

/*
 * Initialize the request/response handler.
 */
void
ClntSvrInit(int size, int my_id)
{
	all_requests = NewList();
	current_request = NULL;
	local_cmd_callback = NULL;
	int_cmd_callback = NULL;

	this = sdm_route_get_id();
	parent = sdm_route_get_parent();

	sdm_message_set_recv_callback(recv_callback);
}

/*
 * Send a command to the controllers specified in the dest set.
 *
 * Commands can only be sent to controllers that do not have an active request pending. The
 * exception is the interrupt command which can be sent at any time. The response to
 * an interrupt command is to cause the pending request to complete.
 */
int
ClntSvrSendCommand(sdm_idset dest, int timeout, char *cmd, void *cbdata)
{
	DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] ClntSvrSendCommand %s\n", this, _set_to_str(dest));
	new_request(SDM_MASTER, dest, cmd, timeout, cbdata);

	return 0;
}

/*
 * Interrupt all active requests.
 */
static void
interrupt_all_requests(sdm_idset dest)
{
	sdm_idset	m;
	request *	r;

	for (SetList(all_requests); (r = (request *)GetListElement(all_requests)) != NULL; ) {
		if (r->active) {
			m = sdm_set_new();
			sdm_set_union(m, dest);
			sdm_set_intersect(m, r->outstanding);
			if (!sdm_set_is_empty(m)) {
				request_interrupt(r, m);
			}
			sdm_set_free(m);
		}
	}
}

/*
 * Interrupt dest controllers, which is assumed to only contain non-SDM_MASTER controllers. Only interrupt
 * active requests that have outstanding replies.
 */
int
ClntSvrSendInterrupt(sdm_idset dest)
{
	if (!sdm_set_is_empty(dest)) {
		interrupt_all_requests(dest);
	}

	return 0;
}

/*
 * Insert reply message into hash or update hash as appropriate.
 */
static void
update_reply(request *req, sdm_idset source, char *msg, int hash)
{
	reply *	r;

	/*
	 * Save reply if it is new, otherwise just update the reply bitset
	 */
	if ((r = HashSearch(req->replys, hash)) == NULL) {
		DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] creating new reply #%x\n", this, hash);
		r = new_reply(req->src, source, msg);
		HashInsert(req->replys, hash, (void *)r);
	} else {
		DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] updating reply #%x\n", this, hash);
		sdm_set_union(r->source, source);
	}

	/*
	 * Remove sources we have received replies from:
	 */
	sdm_set_diff(req->outstanding, r->source);
	DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] reply updated: src=%s, reply_src=%s replies outstanding: %s\n", this,
			_set_to_str(source),
			_set_to_str(r->source),
			_set_to_str(req->outstanding));

	/*
	 * Start timer if necessary
	 */
	if (req->timeout > 0 && check_timer(req) != TIMER_RUNNING && !sdm_set_is_empty(req->outstanding)) {
		start_timer(req);
	}
}

/*
 * Process local debugger message and update the appropriate reply. Cannot be called by SDM_MASTER.
 */
void
ClntSvrInsertMessage(char *msg)
{
	request *	r;
	sdm_idset	source;

	DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] insert '%s'\n", this, msg);

	for (SetList(all_requests); (r = (request *)GetListElement(all_requests)) != NULL; ) {
		if (sdm_set_contains(r->outstanding, this)) {
			source = sdm_set_new();
			sdm_set_add_element(source, this);
			update_reply(r, source, msg, HashCompute(msg, strlen(msg)));
			break;
		}
	}
}

/*
 * Send completed callback. Frees buffers allocated for send.
 */
static void
send_finished(sdm_message msg)
{
	int		len;
	char *	buf;

	sdm_message_get_data(msg, &buf, &len);
	free(buf);
	sdm_message_free(msg);
}

/*
 * Send a debugger event that originated from src to dest. Cannot be called by SDM_MASTER.
 */
void
ClntSvrSendReply(sdm_id dest, sdm_idset src, char *reply, void *data)
{
	int				buf_len;
	int				reply_len;
	unsigned int	hash;
	char *			reply_buf;
	char *			rem;
	sdm_message		msg;

	reply_len = strlen(reply);
	buf_len = reply_len + HEX_LEN + 1;

	hash = HashCompute(reply, reply_len);
	reply_buf = (char *)malloc(buf_len);
	hex_to_str(reply_buf, &rem, hash);
	memcpy(rem, reply, reply_len + 1);

	DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] send reply #%x from %s to %d\n", this, hash, _set_to_str(src), dest);

	msg = sdm_message_new();
	sdm_message_set_data(msg, reply_buf, buf_len);
	sdm_set_add_element(sdm_message_get_destination(msg), dest);
	sdm_set_union(sdm_message_get_source(msg), src);
	sdm_message_set_type(msg, SDM_MESSAGE_TYPE_NORMAL);
	sdm_message_set_send_callback(msg, send_finished);
	sdm_message_send(msg);
}

/*
 * Process a received message
 */
static void
recv_callback(sdm_message msg)
{
	int				len;
	char *			buf;
	request *		req;
	sdm_idset		src;

	src = sdm_message_get_source(msg);
	sdm_message_get_data(msg, &buf, &len);

	if (sdm_set_contains(src, parent)) {
		/*
		 * Message from parent. Create a new request that will be forwarded
		 * to children.
		 */

		int				timeout;
		char *			cmd;
		sdm_idset		dest;

		if (shutting_down)
			return;

		DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] got message from parent\n", this);

		dest = sdm_message_get_destination(msg);

		/*
		 * Process request and forward to children if there are any
		 */
		if (sdm_message_get_type(msg) == SDM_MESSAGE_TYPE_NORMAL) {
			unpack_buffer(buf, len-1, &timeout, &cmd);
			new_request(parent, dest, cmd, timeout, NULL);
		} else {
			interrupt_all_requests(dest);
		}
	} else {
		/*
		 * Must be from a child.
		 *
		 * A child message is split into two parts: a hash and the body
		 *
		 * The body is a null terminated string.
		 *
		 * The hash is computed by each controller and is used to quickly
		 * coalesce events.
		 */

		unsigned int hash;

		hash = str_to_hex(buf, &buf);

		DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] got child message #%x\n", this, hash);

		/*
		 * Find the request this reply is for.
		 * Check if the request is completed.
		 */
		for (SetList(all_requests); (req = (request *)GetListElement(all_requests)) != NULL; ) {
			if (req->active && sdm_set_compare(req->outstanding, src)) {
				update_reply(req, src, buf, hash);
				break;
			}
		}
	}
}

/*
 * Check for completed or timed out commands
 */
static void
flush_requests(void)
{
	request *			req;

	for (SetList(all_requests); (req = (request *)GetListElement(all_requests)) != NULL; ) {
		if (req->active) {
			if (sdm_set_is_empty(req->outstanding)) {
				request_completed(req->replys, req->cb_data);
				free_request(req);
			} else if (req->timeout > 0 && check_timer(req) == TIMER_EXPIRED) {
				request_completed(req->replys, req->cb_data);
				disable_timer(req);
			}
		}
	}
}

/*
 * Check and process commands/responses.
 */
int
ClntSvrProgressCmds(void)
{
	int			len;
	char *		buf;
	sdm_message	msg;
	request *	req;

	/*
	 * Process command requests
	 */
	for (SetList(all_requests); (req = (request *)GetListElement(all_requests)) != NULL; ) {
		if (req->pending) {
			if (!sdm_set_is_empty(sdm_route_get_route(req->interrupt))) {
				msg = sdm_message_new();
				sdm_set_union(sdm_message_get_destination(msg), req->interrupt);
				sdm_set_add_element(sdm_message_get_source(msg), this);
				pack_buffer(req->timeout, NULL, &buf, &len);
				sdm_message_set_data(msg, buf, len+1);
				sdm_message_set_type(msg, SDM_MESSAGE_TYPE_URGENT);
				sdm_message_set_send_callback(msg, send_finished);
				sdm_message_send(msg);
			}

			if (sdm_set_contains(req->interrupt, this) && int_cmd_callback != NULL) {
				DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] running interrupt locally\n", this);
				int_cmd_callback(local_cmd_data);
				DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] finished interrupt command\n", this);
			}

			req->pending = 0;
		} else if (!req->active) {
			if (!sdm_set_is_empty(sdm_route_get_route(req->dest))) {
				msg = sdm_message_new();
				sdm_set_union(sdm_message_get_destination(msg), req->dest);
				sdm_set_add_element(sdm_message_get_source(msg), this);
				pack_buffer(req->timeout, req->msg, &buf, &len);
				sdm_message_set_data(msg, buf, len+1);
				sdm_message_set_type(msg, SDM_MESSAGE_TYPE_NORMAL);
				sdm_message_set_send_callback(msg, send_finished);
				sdm_message_send(msg);
			}

			if (req->local && local_cmd_callback != NULL) {
				DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] running command locally\n", this);
				local_cmd_callback(req->msg, local_cmd_data);
				DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] finished local command\n", this);
			}

			req->active = 1;
		}
	}

	sdm_message_progress();

	flush_requests();

	return 0;
}

/*
 * Wait for any pending replies and flush requests before exiting
 */
void
ClntSvrFinish(void)
{
	request *	req;

	shutting_down = 1;

	/*
	 * Remove all non-active requests
	 */
	for (SetList(all_requests); (req = (request *)GetListElement(all_requests)) != NULL; ) {
		if (!req->active) {
			free_request(req);
		}
	}

	/*
	 * Process remaining requests.
	 */
	while (!EmptyList(all_requests)) {
		sdm_message_progress();
		flush_requests();
	}
}
