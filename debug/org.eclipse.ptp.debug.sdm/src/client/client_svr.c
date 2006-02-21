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
 * Client runs as task id num_procs, where num_procs are the number of processes
 * in the job being debugged, and is responsible for coordinating protocol
 * messages between the debug client interface (whatever that may be)
 * and the debug servers.
 * 
 * Note that there will be num_procs+1 [0..num_procs] processes in our 
 * communicator, where num_procs is the number of processes in the parallel 
 * job being debugged. To simplify the accounting, we use the task id of
 * num_procs as the client task id and [0..num_procs-1] for the server
 * task ids.
 */

#include <config.h>

#include <sys/time.h>

#include <mpi.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include "dbg.h"
#include "dbg_client.h"
#include "dbg_mpi.h"
#include "bitset.h"
#include "list.h"
#include "hash.h"

#define WAIT_TIMEOUT_MS			500
#define ELAPSED_TIME_MS(t1, t2)	(((t1.tv_sec * 1000 - t2.tv_sec * 1000)) + ((t1.tv_usec / 1000) - (t2.tv_usec / 1000)))
#define TIMER_RUNNING				0
#define TIMER_EXPIRED				1
#define TIMER_DISABLED			2

/*
 * A request represents an asynchronous send/receive transaction between the client
 * and all servers. completed() is called once all replys have been received.
 */
struct active_request {
	int				id;
	bitset *			outstanding_procs;
	int				wait_type;
	void *			data;
	Hash *			events;
	struct timeval	start_time;
	int				timer_state;
};
typedef struct active_request	active_request;

static int			num_servers;
static bitset *		sending_procs;
static bitset *		receiving_procs;
static bitset *		interrupt_procs;
static List *		active_requests;
static char **		send_bufs;
static MPI_Request *	send_requests;
static char **		recv_bufs;
static MPI_Request *	recv_requests;
static int *			pids;
static MPI_Status *	stats;
static void			(*cmd_completed_callback)(dbg_event *, void *);

static void 
send_completed(Hash *h, void *data)
{
	HashEntry *	he;
	dbg_event *	e;
	
	if (cmd_completed_callback == NULL)
		return;
		
	for (HashSet(h); (he = HashGet(h)) != NULL; ) {
		e = (dbg_event *)he->h_data;
		cmd_completed_callback(e, data);
		HashRemove(h, he->h_hval);
		FreeDbgEvent(e);
	}
}
	
static void
new_request(bitset *procs, int wait_type, void *data)
{
	active_request *	r;
	static int		id = 0;

	r = (active_request *)malloc(sizeof(active_request));
	r->id = id++;
	r->outstanding_procs = bitset_copy(procs);
	r->wait_type = wait_type;
	r->data = data;
	r->timer_state = TIMER_DISABLED;
	r->events = HashCreate(bitset_size(procs));
	AddToList(active_requests, (void *)r);

	DEBUG_PRINT("created new request %d for %s\n", r->id, bitset_to_set(procs));
}

static void
free_request(active_request *r)
{
	RemoveFromList(active_requests, (void *)r);
	bitset_free(r->outstanding_procs);
	HashDestroy(r->events, NULL);
	free(r);
}

static void
start_timer(active_request *r)
{
	(void)gettimeofday(&r->start_time, NULL);
	r->timer_state = TIMER_RUNNING;
}

static int
check_timer(active_request *r)
{
	struct timeval	time;
	
	if (r->timer_state == TIMER_RUNNING) {
		(void)gettimeofday(&time, NULL);
		
		if (ELAPSED_TIME_MS(time, r->start_time) >= WAIT_TIMEOUT_MS)
			r->timer_state = TIMER_EXPIRED;
	}
	
	return r->timer_state;
}

static void
disable_timer(active_request *r)
{
	r->timer_state = TIMER_DISABLED;
}
	
void
ClntRegisterCallback(void	 (*cmd)(dbg_event *, void *))
{
	cmd_completed_callback = cmd;
}

void
ClntInit(int svr_no)
{
	int	i;
	
	num_servers = svr_no;
	
	send_bufs = (char **)malloc(sizeof(char *) * num_servers);
	send_requests = (MPI_Request *) malloc(sizeof(MPI_Request) * num_servers);
	recv_bufs = (char **)malloc(sizeof(char *) * num_servers);
	recv_requests = (MPI_Request *) malloc(sizeof(MPI_Request) * num_servers);
	pids = (int *)malloc(sizeof(int) * num_servers);
	stats = (MPI_Status *)malloc(sizeof(MPI_Status) * num_servers);

	for (i = 0; i < num_servers; i++) {
		send_requests[i] = MPI_REQUEST_NULL;
		recv_requests[i] = MPI_REQUEST_NULL;
		recv_bufs[i] = (char *)malloc(sizeof(unsigned int) * 2);
	}

	sending_procs = bitset_new(num_servers);
	receiving_procs = bitset_new(num_servers);
	interrupt_procs = bitset_new(num_servers);
	
	active_requests = NewList();
}

/*
 * Send a command to the servers specified in bitset. 
 * 
 * Commands can only be send to processes that do not have an active request pending. The
 * exception is the interrupt command which can be sent at any time. The response to
 * an interrupt command is to complete the pending request.
 */
int
ClntSendCommand(bitset *procs, int wait_type, char *str, void *data)
{
	int				pid;
	int				cmd_len;
	bitset *			p;

	if (bitset_isempty(procs))
		return 0;
		
	/*
	 * Check if any processes already have active requests
	 */
	p = bitset_or(sending_procs, receiving_procs);
	bitset_andeq(p, procs);
	if (!bitset_isempty(p)) {
		if (cmd_completed_callback != NULL)
			cmd_completed_callback(DbgErrorEvent(DBGERR_INPROGRESS, NULL), NULL);
		return -1;
	}

	bitset_free(p);
	
	/*
	 * Update sending processes
	 */	
	bitset_oreq(sending_procs, procs);

	/*
	 * We know that there are no outstanding receiving procs in this set. This means no
	 * existing requests contain any of these procs, so we need to create a new request.
	 */
	new_request(procs, wait_type, data);

	/*
	 * Now post commands to the servers
	 */
	for (pid = 0; pid < num_servers; pid++) {
		if (bitset_test(procs, pid)) {
			/*
			 * MPI spec does not allow read access to a send buffer while send is in progress
			 * so we must make a copy for each send.
			 */
			send_bufs[pid] = strdup(str);
			cmd_len = strlen(str);

			MPI_Isend(send_bufs[pid], cmd_len, MPI_CHAR, pid, TAG_NORMAL, MPI_COMM_WORLD, &send_requests[pid]); // TODO: handle fatal errors
		}
	}

	return 0;
}

int
ClntSendInterrupt(bitset *procs)
{
	if (!bitset_isempty(procs)) {
		/*
		 * Update procs to interrupt
		 */	
		bitset_oreq(interrupt_procs, procs);
	}

	return 0;
}

/*
 * Check for any replies from servers. If any are received, and these complete a send request,
 * then processes the reply.
 */
int
ClntProgressCmds(void)
{
	int				i;
	int				recv_pid;
	int 				completed;
	char *			reply_buf;
	unsigned int		count;
	unsigned int	*	hdr;
	active_request *	r;
	MPI_Status		stat;
	dbg_event *		e;
	bitset *			p;

	/*
	 * Check for completed sends
	 */
	while (!bitset_isempty(sending_procs)) {
		if (MPI_Testsome(num_servers, send_requests, &completed, pids, stats) != MPI_SUCCESS) {
			printf("error in testsome\n");
			return -1;
		}
		
		for (i = 0; i < completed; i++) {
			bitset_unset(sending_procs, pids[i]);
			bitset_set(receiving_procs, pids[i]);
			free(send_bufs[pids[i]]);
			MPI_Irecv(recv_bufs[pids[i]], 2, MPI_UNSIGNED, pids[i], TAG_NORMAL, MPI_COMM_WORLD, &recv_requests[pids[i]]); // TODO: handle fatal errors
		}
	}

	/*
	 * Only interrupt procs that have received our command
	 */
	p = bitset_and(interrupt_procs, receiving_procs); 
	for (i = 0; i < num_servers; i++) {
		if (bitset_test(p, i)) {
			MPI_Send(NULL, 0, MPI_CHAR, i, TAG_INTERRUPT, MPI_COMM_WORLD); // TODO: handle fatal errors
			bitset_unset(interrupt_procs, i);
		}
	}
	bitset_free(p);
	
	/*
	 * Check for replys
	 */
	if (!bitset_isempty(receiving_procs)) {
		if (MPI_Testsome(num_servers, recv_requests, &completed, pids, stats) != MPI_SUCCESS) {
			printf("error in testsome\n");
			return -1;
		}
		
		for (i = 0; i < completed; i++) {
			/*
			 * A message is available, so receive it
			 * 
			 * A message is split into two parts: a header comprising two
			 * unsigned integers (a hash value and a length); a body which 
			 * is the dbg_event structure converted to a string.
			 * 
			 * The hash is computed by each server and is used to quickly
			 * coalesce events.
			 * 
			 * The length is the length of the event string.
			 * 
			 */
			
			recv_pid = pids[i];
			
			hdr = (unsigned int *)(recv_bufs[recv_pid]);
			count = hdr[1];
			reply_buf = (char *)malloc(count + 1);
			
			MPI_Recv(reply_buf, count, MPI_CHAR, recv_pid, 0, MPI_COMM_WORLD, &stat);
			reply_buf[count] = '\0';

			DEBUG_PRINT("recv reply from %d\n", recv_pid);
			
			/*
			 * Check if any requests are completed for this proc
			 */
			for (SetList(active_requests); (r = (active_request *)GetListElement(active_requests)) != NULL; ) {
				if (bitset_test(r->outstanding_procs, recv_pid)) {
					/*
					 * Save event if it is new, otherwise just add this process to the event
					 */
					if ((e = HashSearch(r->events, hdr[0])) == NULL) {
						if (DbgStrToEvent(reply_buf, &e) < 0) {
							fprintf(stderr, "Bad protocol: conversion to event failed! <%s>\n", reply_buf); fflush(stderr);
						} else {
							e->procs = bitset_new(num_servers);
							HashInsert(r->events, hdr[0], (void *)e);
						}
					}
					
					if (e != NULL)
						bitset_set(e->procs, recv_pid);
									
					bitset_unset(r->outstanding_procs, recv_pid);
		
					DEBUG_PRINT("got reply from %d for request %d\n", recv_pid, r->id);
					
					/*
					 * Start timer if necessary
					 */
					if (r->wait_type == DBG_EV_WAITSOME && check_timer(r) != TIMER_RUNNING)
						start_timer(r);
						
					break;
				}
			}
			
			free(reply_buf);
			
			/*
			 * remove from receiving bitsets
			 */
			bitset_unset(receiving_procs, recv_pid);
			DEBUG_PRINT("removing %d\n", recv_pid);
		}
	}
	
	/*
	 * Check for completed commands and timeouts on DBG_EV_WAITSOME commands
	 */
	for (SetList(active_requests); (r = (active_request *)GetListElement(active_requests)) != NULL; ) {
		if (bitset_isempty(r->outstanding_procs)) {
			send_completed(r->events, r->data);
			free_request(r);
			continue;
		}
		
		if (r->wait_type == DBG_EV_WAITSOME && check_timer(r) == TIMER_EXPIRED) {
			send_completed(r->events, r->data);
			disable_timer(r);
		}
	}
	
	return 0;
}
