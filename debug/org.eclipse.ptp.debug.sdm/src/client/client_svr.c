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
 * Implement an O(log(N)) communication protocol. 
 * 
 * The model assumes that each process being debugged will have an associated
 * controller. The controllers are partitioned in a tree such that each controller
 * has a parent (apart from the root) and multiple children (apart from the leaves).
 * The root does not control a process; it instead manages commincation with the client. 
 * A controller receives a message from its parent and is responsible for forwarding
 * it on to it's children. It is also responsible for receiving reply messages from
 * the children, coalescing these into one or more reply messages and forwarding them 
 * back to the parent.
 * 
 * We assume there will be num_procs+1 (0..num_procs) processes in our communicator, 
 * where 'num_procs' is the number of processes in the parallel 
 * job being debugged. To simplify the accounting, we use the task id of
 * 'num_procs' as the task id of the root and (0..num_procs-1) for the remaining
 * task ids.
 * 
 * Since the partitioning algorithm assumes the root has task id 0, we convert each 
 * task id prior to a send or receive to an partition id:
 * 
 *    part_id = (real_id + 1) % (num_procs+1)
 * 
 */

#ifdef __gnu_linux__
#define _GNU_SOURCE
#endif /* __gnu_linux__ */

#include "config.h"

#include <sys/time.h>

#include <mpi.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include "compat.h"
#include "dbg.h"
#include "dbg_client.h"
#include "bitset.h"
#include "list.h"
#include "hash.h"

#define ELAPSED_TIME(t1, t2)	(((t1.tv_sec - t2.tv_sec) * 1000000) + (t1.tv_usec - t2.tv_usec))
#define TIMER_RUNNING			0
#define TIMER_EXPIRED			1
#define TIMER_DISABLED			2

#define	TAG_NORMAL		0
#define	TAG_INTERRUPT	1

/*
 * A request represents an asynchronous send/receive transaction between the client
 * and all servers. completed() is called once all replys have been received.
 */
struct request {
	int				id;				/* this request id */
	int				active;			/* this request is active (i.e. has been forwarded) */
	int				interrupt;		/* there are pending interrupts to be sent */
	bitset *		active_mask;	/* destination controllers */
	bitset *		interrupt_mask;	/* controllers to interrupt */
	char *			msg;			/* buffer to send */
	bitset *		outstanding;	/* controllers remaining to send replys */
	int				timeout;		/* wait timeout for this request (microseconds) */
	void *			cb_data;		/* completed request callback data */
	Hash *			replys;			/* hash of replys we've received */
	struct timeval	start_time;		/* time that timer was started */
	int				timer_state;	/* state of timer */
};
typedef struct request	request;

struct reply {
	bitset *	mask;				/* controllers that have generated this message */
	char *		msg;				/* message contents */
};
typedef struct reply	reply;

static int				num_ctlrs;
static int				this;
static int				root;
static int				parent;
static bitset *			descendents;
static bitset *			children;
static List *			all_requests;
static struct request *	current_request;
static void				(*cmd_completed_callback)(bitset *, char *, void *);
static void *			local_cmd_data;
static void				(*local_cmd_callback)(char *, void *);
static void *			int_cmd_data;
static void				(*int_cmd_callback)(void *);
static reply *			new_reply(bitset *, char *);
static void				free_reply(reply *);

static void 
send_completed(Hash *h, void *data)
{
	HashEntry *	he;
	reply *		r;
	
	for (HashSet(h); (he = HashGet(h)) != NULL; ) {
		r = (reply *)he->h_data;
		
		if (cmd_completed_callback != NULL)
			cmd_completed_callback(r->mask, r->msg, data);
			
		free_reply(r);
		HashRemove(h, he->h_hval);
	}
}

/*
 * Create buffer to send. This consists of a string representation of 'mask', 
 * the 'wait' value, and the command string.
 */
static void
pack_buffer(bitset *mask, int timeout, char *cmd, char **buf, int *len)
{
	int		cmd_len;
	int		mask_len;
	int		wait_len;
	char *	mask_str;
	char *	wait_str;
	
	if (cmd != NULL)
		cmd_len = strlen(cmd) + 1;
	else
		cmd_len = 0;
		
	mask_str = bitset_to_str(mask);
	mask_len = strlen(mask_str) + 1;
	asprintf(&wait_str, "%X", timeout & 0xFFFFFFFF);
	wait_len = strlen(wait_str) + 1;
	*len = cmd_len + mask_len + wait_len;
	*buf = (char *)malloc(*len);
	memcpy(*buf, mask_str, mask_len);
	memcpy(*buf + mask_len, wait_str, wait_len);
	if (cmd != NULL)
		memcpy(*buf + mask_len + wait_len, cmd, cmd_len);
		
	free(mask_str);
	free(wait_str);
}

/*
 * Unpack a received buffer
 */
static void
unpack_buffer(char *buf, int len, bitset **mask, int *timeout, char **cmd)
{
	int		n;
	int		val = 0;
	
	n = strlen(buf);
	*mask = str_to_bitset(buf);
	buf += n + 1;
	n += strlen(buf) + 1;
	for (val = 0; *buf != '\0'; buf++) {
		val <<= 4;
		val += digittoint(*buf);
	}
	buf++;
	*timeout = val;
	if (n < len) {
		*cmd = strdup(buf);
	} else {
		*cmd = NULL;
	}
}

/*
 * Create a new request. We assume that there are no outstanding commands active for 
 * any controllers in 'mask'. Interrupts do not have this restriction.
 * 
 * The set ((this | descendents) & mask) is all controllers that will respond to this request.
 * Once we have received replys from all controllers in this set, the request is complete.
 */
static void
new_request(bitset *mask, char *msg, int timeout, void *cbdata)
{
	request *	r;
	bitset *	send_to;
	static int	id = 0;

	send_to = bitset_dup(descendents);
	if (this != root)
		bitset_set(send_to, this);
	bitset_andeq(send_to, mask);
	
	if (bitset_isempty(send_to)) {
		DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] No one to send to\n", this);
		return;
	}

	r = (request *)malloc(sizeof(request));
	r->id = id++;
	r->active = 0;
	r->interrupt = 0;
	if (msg != NULL)
		r->msg = strdup(msg);
	else
		r->msg = NULL;
	r->active_mask = bitset_dup(mask);
	r->interrupt_mask = NULL;
	r->timeout = timeout;
	r->cb_data = cbdata;
	r->timer_state = TIMER_DISABLED;
	r->replys = HashCreate(num_ctlrs);
	r->outstanding = send_to;
		
	AddToList(all_requests, (void *)r);
	
	DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] Creating new request (%d, %s, '%s')\n", this, r->id, bitset_to_set(send_to), msg);
}

static void
request_interrupt(request *r, bitset *mask)
{
	if (r->interrupt)
		bitset_oreq(r->interrupt_mask, mask);
	else {
		if (r->interrupt_mask != NULL)
			bitset_free(r->interrupt_mask);
		r->interrupt_mask = bitset_dup(mask);
		r->interrupt = 1;
	}
}

static void
free_request(request *r)
{
	RemoveFromList(all_requests, (void *)r);
	if (r->msg != NULL)
		free(r->msg);
	bitset_free(r->active_mask);
	if (r->interrupt_mask != NULL)
		bitset_free(r->interrupt_mask);
	bitset_free(r->outstanding);
	HashDestroy(r->replys, NULL);
	free(r);
}

static reply *
new_reply(bitset *mask, char *msg)
{
	reply *	r;

	r = (reply *)malloc(sizeof(reply));
	r->mask = bitset_dup(mask);
	r->msg = strdup(msg);

	return r;
}

static void
free_reply(reply *r)
{
	bitset_free(r->mask);
	free(r->msg);
	free(r);
}

static void
start_timer(request *r)
{
	(void)gettimeofday(&r->start_time, NULL);
	r->timer_state = TIMER_RUNNING;
}

static int
check_timer(request *r)
{
	struct timeval	time;
	
	if (r->timer_state == TIMER_RUNNING) {
		(void)gettimeofday(&time, NULL);
		
		if (ELAPSED_TIME(time, r->start_time) >= r->timeout)
			r->timer_state = TIMER_EXPIRED;
	}
	
	return r->timer_state;
}

static void
disable_timer(request *r)
{
	r->timer_state = TIMER_DISABLED;
}

/*
 * Find MSB of value.
 */
int
high_bit(int value)
{
	int 			pos = (sizeof(int) << 3) - 1;
	unsigned int	mask;

	mask = 1 << pos;

	for (; pos >= 0; pos--) {
		if (value & mask) {
			break;
		}
		mask >>= 1;
	}

	return pos;
}

/*
 * Register completion callback. This callback is made
 * when a controller has received replys from all it's
 * children, or the appropriate timout has expired.
 */
void
ClntSvrRegisterCompletionCallback(void (*func)(bitset *, char *, void *))
{
	cmd_completed_callback = func;
}

/*
 * Register local command callback. This callback is made when
 * a command is received from the parent, and must be executed
 * by this controller.
 */
void
ClntSvrRegisterLocalCmdCallback(void (*func)(char *, void *), void *data)
{
	local_cmd_callback = func;
	local_cmd_data = data;
}

/*
 * Register an interrupt command callback. This callback is made when
 * a command is received from the parent, and must be executed
 * by this controller.
 */
void
ClntSvrRegisterInterruptCmdCallback(void (*func)(void *), void *data)
{
	int_cmd_callback = func;
	int_cmd_data = data;
}

/*
 * Create a bitset containing our descendents. If descend is 0, only find immdiate
 * children.
 */
void
find_descendents(bitset *set, int id_p, int root, int size, int p2, int descend)
{
	int	i;
	int high;
	int	mask;
	int child;
	int child_p;
	
	high = high_bit(id_p);
		
	for (i = high + 1, mask = 1 << i; i <= p2; ++i, mask <<= 1) {
        child_p = id_p | mask;
        if (child_p < size) {
            child = (child_p + root) % size;
            bitset_set(set, child);
            if (descend)
            	find_descendents(set, child_p, root, size, p2, descend);
        }
    }
}

/*
 * Initialize partition. The bitset 'children' will contain all the immediate children
 * of this controller. The bitset 'descendents' will contain all children and their
 * descendents. 
 * 
 * Since the root controller is assumed to be 'num_ctlrs' - 1, the bitsets only need
 * to contain bits for the possible children (i.e. 'numctlrs - 1' bits).
 */
void
ClntSvrInit(int size, int my_id)
{
	int	p2;
	int this_p;
	int	high;
	
	num_ctlrs = size;
	this = my_id;
	root = size - 1;
	p2 = high_bit(size) << 1;
	this_p = (this + size - root) % size;
	high = high_bit(this_p);
	
	descendents = bitset_new(size - 1);
	children = bitset_new(size - 1);
	
	if (this != root)
        parent = ((this_p & ~(1 << high)) + root) % size;
    else
    	parent = -1; // No parent
        
    find_descendents(children, this_p, root, size, p2, 0);
    find_descendents(descendents, this_p, root, size, p2, 1);

	DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] children = %s\n", this, bitset_to_set(children));
	DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] descendents = %s\n", this, bitset_to_set(descendents));
	
	all_requests = NewList();
	current_request = NULL;
	local_cmd_callback = NULL;
	int_cmd_callback = NULL;
}

/*
 * Send a command to the controllers specified in bitset 'mask'. All controllers
 * actually get the command, only those in the bitset actually perform the command.
 * 'mask' is assumed to only contain non-root controllers.
 * 
 * Commands can only be sent to controllers that do not have an active request pending. The
 * exception is the interrupt command which can be sent at any time. The response to
 * an interrupt command is to cause the pending request to complete.
 */
int
ClntSvrSendCommand(bitset *mask, int timeout, char *cmd, void *cbdata)
{
	request *	r;

	if (!bitset_isempty(mask)) {
		for (SetList(all_requests); (r = (request *)GetListElement(all_requests)) != NULL; ) {
			if (bitset_compare(mask, r->outstanding)) {
				return -1;
			}
		}
							
		new_request(mask, cmd, timeout, cbdata);
	}
	
	return 0;
}

static void
interrupt_all_requests(bitset *mask)
{
	bitset *	m;
	request *	r;
	
	for (SetList(all_requests); (r = (request *)GetListElement(all_requests)) != NULL; ) {
		if (r->active) {
			m = bitset_and(mask, r->outstanding);
			if (!bitset_isempty(m))
				request_interrupt(r, m);
			bitset_free(m);
		}
	}
}

/*
 * Interrupt controllers in 'mask', which is assumed to only contain non-root controllers. Only interrupt
 * active requests that have outstanding replys.
 */
int
ClntSvrSendInterrupt(bitset *mask)
{
	if (!bitset_isempty(mask)) {
		interrupt_all_requests(mask);
	}

	return 0;
}

/*
 * Insert reply message into hash or update hash as appropriate.
 */
static void
update_reply(request *req, bitset *mask, char *msg, int hash)
{
	reply *	r;
	
	/*
	 * Save reply if it is new, otherwise just update the reply bitset
	 */
	if ((r = HashSearch(req->replys, hash)) == NULL) {
		DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] creating new reply (%s, '%s') for #%x\n", this, bitset_to_set(mask), msg, hash);
		r = new_reply(mask, msg);
		HashInsert(req->replys, hash, (void *)r);
	} else {
		DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] updating reply #%x\n", this, hash);
		bitset_oreq(r->mask, mask);
	}
	
	/*
	 * Unset bits we have received replys from:
	 */
	bitset_andeqnot(req->outstanding, r->mask);
	
	DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] request outstanding is now %s\n", this, bitset_to_set(req->outstanding));
	
	/* 
	 * Start timer if necessary
	 */
	if (req->timeout > 0 && check_timer(req) != TIMER_RUNNING && !bitset_isempty(req->outstanding))
		start_timer(req);
}

/*
 * Process local debugger message
 */
void
ClntSvrInsertMessage(char *msg)
{
	request *	r;
	bitset *	mask;
	
	DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] insert '%s'\n", this, msg);
	
	for (SetList(all_requests); (r = (request *)GetListElement(all_requests)) != NULL; ) {
		if (bitset_test(r->outstanding, this)) {
			mask = bitset_new(num_ctlrs - 1); // Exclude root controller
			bitset_set(mask, this);
			update_reply(r, mask, msg, HashCompute(msg, strlen(msg)));
			break;
		}
	}
}

/*
 * Send a local debugger event back to our parent
 */
void
ClntSvrSendReply(bitset *mask, char *msg, void *data)
{
	int				len;
	char *			reply_buf;
	unsigned int	hdr[2];
	

	pack_buffer(mask, 0, msg, &reply_buf, &len);
	
	hdr[0] = HashCompute(msg, strlen(msg));
	hdr[1] = len;

	MPI_Send(hdr, 2, MPI_UNSIGNED, parent, TAG_NORMAL, MPI_COMM_WORLD);
	MPI_Send(reply_buf, hdr[1], MPI_CHAR, parent, TAG_NORMAL, MPI_COMM_WORLD);

	DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] sent reply <(%x,%d),'%s...'>\n", this, hdr[0], hdr[1], reply_buf);
	
	free(reply_buf);
}

/*
 * Send command to children
 */
static void
send_to_children(bitset *mask, int tag, int timeout, char *msg)
{
	int			child_id;
	int			len;
	char *		buf;
	bitset *	sibs;
		
	/*
	 * Send commands to the children
	 */
	pack_buffer(mask, timeout, msg, &buf, &len);
	
	for (sibs = bitset_dup(children); (child_id = bitset_firstset(sibs)) != -1; bitset_unset(sibs, child_id)) {
		DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] sibs is %s\n", this, bitset_to_set(sibs));
		DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] Sending (%s, %d, '%s...') to %d\n", this, tag==TAG_INTERRUPT ? "interrupt" : "normal", len, buf, child_id);
		MPI_Send(buf, len, MPI_CHAR, child_id, tag, MPI_COMM_WORLD); // TODO: handle fatal errors
	}
		
	free(buf);
	bitset_free(sibs);
}

/*
 * Check for any messages sent to us. 
 * 
 * If ignore_parent is true, only messages from children will be processed. This
 * is used when the server is shutting down to make sure all pending child
 * requests are processed.
 */
static void
check_for_messages(int ignore_parent)
{
	int				flag;
	int				recv_id;
	int				timeout;
	int				len;
	char *			buf;
	char *			msg;
	bitset *		mask;
	unsigned int	hdr[2];
	MPI_Status		stat;
	request *		req;

	MPI_Iprobe(MPI_ANY_SOURCE, MPI_ANY_TAG, MPI_COMM_WORLD, &flag, &stat);

	if (flag != 0) {
		if (stat.MPI_SOURCE == parent) {
			if (ignore_parent)
				return;
				
			/*
			 * Receive command message from the parent
			 */
			MPI_Get_count(&stat, MPI_CHAR, &len);
			buf = (char *)malloc(len);
			MPI_Recv(buf, len, MPI_CHAR, parent, stat.MPI_TAG, MPI_COMM_WORLD, &stat);
			
			DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] got message from parent (%d, '%s...')\n", this, len, buf);
			
			unpack_buffer(buf, len, &mask, &timeout, &msg);

			/*
			 * Process request and forward to children if there are any
			 */
			if (stat.MPI_TAG == TAG_NORMAL)
				new_request(mask, msg, timeout, NULL);
			else
				interrupt_all_requests(mask);
			
			free(buf);
			free(msg);
			bitset_free(mask);
		} else {
			/*
			 * Must be from a child.
			 * 
			 * A child message is split into two parts: a header and a body
			 * 
			 * The header comprises two unsigned integers:
			 * 
			 * hash    - a hash value computed over the message body
			 * length  - length of the message body
			 * 
			 * The body is a null terminated bitset string followed by the message contents
			 * which is also a null terminated string (i.e. the length includes the nulls).
			 * 
			 * The hash is computed by each controller and is used to quickly
			 * coalesce events.
			 * 
			 * The length is the length of the event string.
			 * 
			 */
			 
			recv_id = stat.MPI_SOURCE;

			/*
			 * First get header
			 */
			MPI_Recv(hdr, 2, MPI_UNSIGNED, recv_id, TAG_NORMAL, MPI_COMM_WORLD, &stat);
			
			len = hdr[1];
			buf = (char *)malloc(len);
			
			/*
			 * Next get the remainder of the message
			 */
			MPI_Recv(buf, len, MPI_CHAR, recv_id, TAG_NORMAL, MPI_COMM_WORLD, &stat);
			
			unpack_buffer(buf, len, &mask, &timeout, &msg);
			
			DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] got reply from child %d (%x, %s, '%s')\n", this, recv_id, hdr[0], bitset_to_set(mask), msg);
			
			/*
			 * Find the request this reply is for.
			 * Check if the request is completed.
			 */
			for (SetList(all_requests); (req = (request *)GetListElement(all_requests)) != NULL; ) {
				if (req->active && bitset_compare(req->outstanding, mask)) {
					update_reply(req, mask, msg, hdr[0]);			
					break;
				}
			}
			
			free(buf);
			free(msg);
			bitset_free(mask);
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
			if (bitset_isempty(req->outstanding)) {
				send_completed(req->replys, req->cb_data);
				free_request(req);
			} else if (req->timeout > 0 && check_timer(req) == TIMER_EXPIRED) {
				send_completed(req->replys, req->cb_data);
				disable_timer(req);
			}
		}
	}
}

/*
 * Check and process commands from the parent and any replies from children. 
 */
int
ClntSvrProgressCmds(void)
{
	request *			req;

	/*
	 * Process command requests
	 */
	for (SetList(all_requests); (req = (request *)GetListElement(all_requests)) != NULL; ) {
		if (req->interrupt) {
			send_to_children(req->interrupt_mask, TAG_INTERRUPT, req->timeout, NULL);

			if (this != root && bitset_test(req->interrupt_mask, this) && int_cmd_callback != NULL) {
				DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] running interrupt locally\n", this);
				int_cmd_callback(local_cmd_data);
				DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] finished interrupt command\n", this);
			}
			
			req->interrupt = 0;
		} else if (!req->active) {
			send_to_children(req->active_mask, TAG_NORMAL, req->timeout, req->msg);
			
			if (this != root && bitset_test(req->active_mask, this) && local_cmd_callback != NULL) {
				DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] running command locally\n", this);
				local_cmd_callback(req->msg, local_cmd_data);
				DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] finished local command\n", this);
			}
			
			req->active = 1;
		}
	}

	check_for_messages(0);
	
	flush_requests();
	
	return 0;
}

/*
 * Wait for any pending replys and flush requests before exiting
 */
void
ClntSvrFinish(void)
{
	request *	req;
	
	/*
	 * Remove all non-active requests
	 */
	for (SetList(all_requests); (req = (request *)GetListElement(all_requests)) != NULL; ) {
		if (!req->active) {
			free_request(req);
		}
	}
	
	/*
	 * Process remaining requests, ignore messages from parent.
	 */
	while (!EmptyList(all_requests)) {
		check_for_messages(1);
		flush_requests();
	}
}
