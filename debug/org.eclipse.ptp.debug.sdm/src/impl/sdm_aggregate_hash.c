/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

/*
 * Message aggregation based on hashing the message contents.
 */

#include <sys/time.h>

#include <stdlib.h>
#include <stdarg.h>
#include <stdio.h>
#include <string.h>
#include <getopt.h>

#include "compat.h"
#include "list.h"
#include "hash.h"
#include "sdm.h"

struct sdm_aggregate {
	unsigned int	value;
};

/*
 * A request represents an asynchronous send/receive transaction between the client
 * and all servers. The completion_callback() is called once all replies have been received
 * for a particular request.
 */
struct request {
	int				id;				/* this request id */
	sdm_idset		outstanding;	/* controllers remaining to send replies */
	int				timeout;		/* wait timeout for this request (microseconds) */
	Hash *			replys;			/* hash of replies we've received */
	struct timeval	start_time;		/* time that timer was started */
	int				timer_state;	/* state of timer */
};
typedef struct request	request;

static List *			all_requests;
static struct request *	current_request;

static void			new_request(sdm_idset dest, int timeout);
static void			free_request(request *);
static void			update_reply(request *req, sdm_message msg, int hash);
static unsigned int	str_to_hex(char *str, char **end);
static void			hex_to_str(char *str, char **end, unsigned int val);
static void			start_timer(request *r);
static int			check_timer(request *r);
static void			disable_timer(request *r);
static void			request_completed(request *req);
static int	 		(*completion_callback)(const sdm_message msg);

#define HEX_LEN	8

#define ELAPSED_TIME(t1, t2)	(((t1.tv_sec - t2.tv_sec) * 1000000) + (t1.tv_usec - t2.tv_usec))
#define TIMER_RUNNING			0
#define TIMER_EXPIRED			1
#define TIMER_DISABLED			2

int
sdm_aggregate_init(int argc, char *argv[])
{
	all_requests = NewList();
	current_request = NULL;
	completion_callback = NULL;
	return 0;
}

void
sdm_aggregate_serialize(const sdm_aggregate a, char *buf, char **end)
{
	hex_to_str(buf, end, a->value);
}

int
sdm_aggregate_serialized_length(const sdm_aggregate a)
{
	return HEX_LEN;
}

void
sdm_aggregate_deserialize(sdm_aggregate a, char *str, char **end)
{
	a->value = str_to_hex(str, end);
}

void
sdm_aggregate_set_completion_callback(int (*callback)(const sdm_message msg))
{
	completion_callback = callback;
}

/*
 * Aggregate a received message
 */
void
sdm_aggregate_message(sdm_message msg, unsigned int flags)
{
	sdm_aggregate	a;

	a = sdm_message_get_aggregate(msg);

	if (flags & SDM_AGGREGATE_UPSTREAM) {
		request *		req;

		/*
		 * Find the request this reply is for.
		 * Check if the request is completed.
		 */
		for (SetList(all_requests); (req = (request *)GetListElement(all_requests)) != NULL; ) {
			DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] sdm_aggregate_message compare %s %s\n", sdm_route_get_id(),
					_set_to_str(req->outstanding),
					_set_to_str(sdm_message_get_source(msg)));

			if (sdm_set_compare(req->outstanding, sdm_message_get_source(msg))) {
				update_reply(req, msg, a->value);
				break;
			}
		}
	} else if (flags & SDM_AGGREGATE_DOWNSTREAM) {
		new_request(sdm_message_get_destination(msg), a->value);
	}
}

/*
 * Check for completed or timed out commands
 */
void
sdm_aggregate_progress(void)
{
	request *	req;

	/*
	 * Process command requests
	 */
	for (SetList(all_requests); (req = (request *)GetListElement(all_requests)) != NULL; ) {
		if (sdm_set_is_empty(req->outstanding)) {
			request_completed(req);
			free_request(req);
		} else if (req->timeout > 0 && check_timer(req) == TIMER_EXPIRED) {
			request_completed(req);
			disable_timer(req);
		}
	}
}

void
sdm_aggregate_finalize(void)
{
	/*
	 * Process remaining requests.
	 */
	while (!EmptyList(all_requests)) {
		sdm_message_progress();
		sdm_aggregate_progress();
	}
}

sdm_aggregate
sdm_aggregate_new(void)
{
	sdm_aggregate	a = (sdm_aggregate)malloc(sizeof(struct sdm_aggregate));
	a->value = 0;
	return a;
}

void
sdm_aggregate_get_value(sdm_aggregate a, int type, ...)
{
	int	*		value;
    va_list		ap;

    va_start(ap, type);

    switch (type) {
    case SDM_AGGREGATE_TIMEOUT:
    case SDM_AGGREGATE_HASH:
    	value = va_arg(ap, int *);
    	*value = a->value;
    	break;
    }

    va_end(ap);
}

void
sdm_aggregate_set_value(sdm_aggregate a, int type, ...)
{
	int		len;
	char *	buf;
    va_list	ap;

    va_start(ap, type);

    switch (type) {
    case SDM_AGGREGATE_TIMEOUT:
    	a->value = va_arg(ap, int);
    	break;

    case SDM_AGGREGATE_HASH:
    	buf = va_arg(ap, char *);
    	len = va_arg(ap, int);
    	a->value = HashCompute(buf, len);
    	break;
    }

    va_end(ap);
}

void
sdm_aggregate_free(const sdm_aggregate a)
{
	free(a);
}

/********************************************/
/********************************************/

char *
_aggregate_to_str(sdm_aggregate a)
{
	static char *	res = NULL;

	if (res != NULL) {
		free(res);
	}

	res = (char *)malloc(sizeof(int)*2+1);
	hex_to_str(res, NULL, a->value);
	return res;
}

/*
 * Create a new aggregration request.
 *
 * @param dest is the destination of the message
 * @param timeout is the time to wait before forwarding any replies. 0 means infinite.
 *
 * The outstanding set is all controllers that will respond to this request and once
 * we have received replies from all controllers in this set, the request is complete.
 */
static void
new_request(sdm_idset dest, int timeout)
{
	request *	r;
	static int	id = 0;

	r = (request *)malloc(sizeof(request));
	r->id = id++;
	r->timeout = timeout;
	r->timer_state = TIMER_DISABLED;
	r->replys = HashCreate(sdm_route_get_size());
	r->outstanding = sdm_set_new();
	sdm_set_union(r->outstanding, sdm_route_reachable(dest));

	AddToList(all_requests, (void *)r);

	DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] Creating new request #%d expected replies %s)\n",
			sdm_route_get_id(), r->id, _set_to_str(r->outstanding));
}

/*
 * Free the request.
 */
static void
free_request(request *r)
{
	RemoveFromList(all_requests, (void *)r);
	sdm_set_free(r->outstanding);
	HashDestroy(r->replys, NULL);
	free(r);
}

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
	if (end != NULL) {
		*end = str + HEX_LEN;
	}
}


/*
 * Callback when a request is completed.
 */
static void
request_completed(request *req)
{
	HashEntry *	he;
	sdm_message msg;

	for (HashSet(req->replys); (he = HashGet(req->replys)) != NULL; ) {
		msg = (sdm_message)he->h_data;

		if (completion_callback != NULL) {
			DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] request %d completed for #%x\n", sdm_route_get_id(),
					req->id,
					he->h_hval);
			completion_callback(msg);
		}

		HashRemove(req->replys, he->h_hval);
	}
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
 * Given a new message, try to aggregate it with any outstanding messages.
 */
static void
update_reply(request *req, sdm_message msg, int hash)
{
	sdm_message		reply_msg;
	sdm_aggregate	na = sdm_message_get_aggregate(msg);

	/*
	 * Remove sources we have received replies from:
	 */
	sdm_set_diff(req->outstanding, sdm_message_get_source(msg));

	DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] reply updated: src=%s replies outstanding: %s\n",
			sdm_route_get_id(),
			_set_to_str(sdm_message_get_source(msg)),
			_set_to_str(req->outstanding));

	/*
	 * Save reply if it is new, otherwise just update the reply set
	 */
	if ((reply_msg = (sdm_message)HashSearch(req->replys, na->value)) == NULL) {
		DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] creating new reply #%x\n", sdm_route_get_id(), na->value);
		HashInsert(req->replys, na->value, (void *)msg);
	} else {
		DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] updating reply #%x with src %s\n",
				sdm_route_get_id(),
				na->value,
				_set_to_str(sdm_message_get_source(msg)));
		sdm_set_union(sdm_message_get_source(reply_msg), sdm_message_get_source(msg));
		sdm_message_free(msg);
	}

	/*
	 * Start timer if necessary
	 */
	if (req->timeout > 0 && check_timer(req) != TIMER_RUNNING && !sdm_set_is_empty(req->outstanding)) {
		start_timer(req);
	}
}
