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
 * Message transport implementation. This provides an implementation of
 * SDM messages using MPI.
 */

#include <mpi.h>
#include <stdlib.h>
#include <string.h>
#include <getopt.h>

#include "compat.h"
#include "list.h"
#include "sdm.h"

struct sdm_message {
	sdm_idset		dest; 			/* Destinations of the message */
	sdm_idset		src; 			/* Source of the message */
	sdm_aggregate	aggregate;		/* Message aggregation */
	char *			payload;		/* Payload */
	int				payload_len;	/* Payload length */
	char *			buf;			/* Receive buffer */
	int				buf_len;		/* Receive buffer length */
	void 			(*send_complete)(sdm_message msg);
};

static void (*sdm_recv_callback)(sdm_message msg) = NULL;
static void	(*payload_callback)(char *buf, int len) = NULL;

static void	setenviron(char *str, int val);

/**
 * Initialize the runtime abstraction.
 * @return 0 on success, -1 on failure
 */
int
sdm_message_init(int argc, char *argv[])
{
	int		ch;
	int		size;
	int		rank;
	int		jobid = -1;

	for (ch = 0; ch < argc; ch++) {
		char * arg = argv[ch];
		if (strncmp(arg, "--jobid", 7) == 0) {
			jobid = (int)strtol(arg+8, NULL, 10);
			break;
		}
	}

	MPI_Init(&argc, (char ***)&argv);
	MPI_Comm_size(MPI_COMM_WORLD, &size);
	MPI_Comm_rank(MPI_COMM_WORLD, &rank);

	sdm_route_set_size(size);
	sdm_route_set_id(rank);

	SDM_MASTER = size - 1;

#ifdef OMPI
	if (rank != SDM_MASTER) {
		setenviron("OMPI_MCA_ns_nds_jobid", jobid);
		setenviron("OMPI_MCA_ns_nds_vpid", rank);
		setenviron("OMPI_MCA_ns_nds_num_procs", size-1);
	}
#else /* OMPI */
#warning Debugging is not supported on this architecture
#endif /* OMPI */

	return 0;
}

/**
 * Finalize the message abstraction.
 */
void
sdm_message_finalize()
{
	MPI_Finalize();
}

/**
 * Send a message to the destinations.
 *
 * @return 0 on success, -1 on failure
 */
int
sdm_message_send(const sdm_message msg)
{
	int			len;
	int			agg_len;
	int			src_len;
	int			dest_len;
	char *		p;
	char *		buf;
	sdm_id		dest_id;
	sdm_idset	route;

	/*
	 * Remove our id from the destination of the message before forwarding.
	 */
	if (sdm_set_contains(msg->dest, sdm_route_get_id())) {
		sdm_set_remove_element(msg->dest, sdm_route_get_id());
		DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] sdm_message_send removing me from dest\n", sdm_route_get_id());
	}

	agg_len = sdm_aggregate_serialized_length(msg->aggregate);
	src_len = sdm_set_serialized_length(msg->src);
	dest_len = sdm_set_serialized_length(msg->dest);
	len = agg_len + src_len + dest_len + msg->payload_len + 2;
	p = buf = (char *)malloc(len);

	sdm_aggregate_serialize(msg->aggregate, p, &p);
	sdm_set_serialize(msg->src, p, &p);
	sdm_set_serialize(msg->dest, p, &p);
	memcpy(p, msg->payload, msg->payload_len);

	route = sdm_route_get_route(msg->dest);

	DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] sdm_message_send src %s dest %s route %s\n", sdm_route_get_id(),
		_set_to_str(msg->src),
		_set_to_str(msg->dest),
		_set_to_str(route));

	DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] sdm_message_send {%s}\n", sdm_route_get_id(), buf);


	for (dest_id = sdm_set_first(route); !sdm_set_done(route); dest_id = sdm_set_next(route)) {
		int err;

		DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] Sending len %d to %d\n", sdm_route_get_id(), len, dest_id);

		err = MPI_Send(buf, len, MPI_CHAR, dest_id, 0, MPI_COMM_WORLD);
		if (err != MPI_SUCCESS) {
			DEBUG_PRINTS(DEBUG_LEVEL_CLIENT, "MPI_Send failed!\n");
			return -1;
		}
	}

	if (msg->send_complete != NULL) {
		msg->send_complete(msg);
	}

	return 0;
}

/**
 * Message progress
 *
 * @return 0 on success, -1 on failure
 */
int
sdm_message_progress(void)
{
	int				err;
	int				avail;
	int				len;
	char *			p;
	char *			hdr;
	char *			buf;
	MPI_Status		stat;

	err = MPI_Iprobe(MPI_ANY_SOURCE, 0, MPI_COMM_WORLD, &avail, &stat);

	if (err != MPI_SUCCESS) {
		DEBUG_PRINTS(DEBUG_LEVEL_CLIENT, "MPI_Iprobe failed!\n");
		return -1;
	}

	if (avail) {
		MPI_Get_count(&stat, MPI_CHAR, &len);

		buf = (char *)malloc(len * sizeof(char));

		sdm_message msg = sdm_message_new(buf, len);

		err = MPI_Recv(buf, len, MPI_CHAR, stat.MPI_SOURCE, 0, MPI_COMM_WORLD, &stat);

		if (err != MPI_SUCCESS) {
			DEBUG_PRINTS(DEBUG_LEVEL_CLIENT, "MPI_Recv failed!\n");
			return -1;
		}

		DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] sdm_message_progress received len %d from %d\n", sdm_route_get_id(), len, stat.MPI_SOURCE);

		hdr = buf;
		sdm_aggregate_deserialize(msg->aggregate, hdr, &p);
		len -= (p - hdr);

		hdr = p;
		sdm_set_deserialize(msg->src, hdr, &p);
		len -= (p - hdr);

		hdr = p;
		sdm_set_deserialize(msg->dest, hdr, &p);
		len -= (p - hdr);

		msg->payload = p;
		msg->payload_len = len;

		DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] sdm_message_progress <%s>@(%s,%s) {%s}\n", sdm_route_get_id(),
				_aggregate_to_str(msg->aggregate),
				_set_to_str(msg->src),
				_set_to_str(msg->dest),
				p);

		if (sdm_recv_callback  != NULL) {
			sdm_recv_callback(msg);
		}
	}

	return 0;
}

void
sdm_message_set_send_callback(sdm_message msg, void (*callback)(sdm_message msg))
{
	msg->send_complete = callback;
}

void
sdm_message_set_recv_callback(void (*callback)(sdm_message msg))
{
	sdm_recv_callback = callback;
}

sdm_message
sdm_message_new(char *buf, int len)
{
	sdm_message	msg = (sdm_message)malloc(sizeof(struct sdm_message));

	msg->dest = sdm_set_new();
	msg->src = sdm_set_new();
	sdm_set_add_element(msg->src, sdm_route_get_id());
	msg->buf = buf;
	msg->buf_len = len;
	msg->payload = msg->buf;
	msg->payload_len = msg->buf_len;
	msg->aggregate = sdm_aggregate_new();
	msg->send_complete = NULL;

	return msg;
}

void
sdm_message_free(sdm_message msg)
{
	free(msg->buf);
	sdm_set_free(msg->src);
	sdm_set_free(msg->dest);
	sdm_aggregate_free(msg->aggregate);
	free(msg);
}

void
sdm_message_set_destination(const sdm_message msg, const sdm_idset dest_ids)
{
	sdm_set_union(msg->dest, dest_ids);
}

sdm_idset
sdm_message_get_destination(const sdm_message msg)
{
	return msg->dest;
}

void
sdm_message_set_source(const sdm_message msg, const sdm_idset source)
{
	sdm_set_union(msg->src, source);
}

sdm_idset
sdm_message_get_source(const sdm_message msg)
{
	return msg->src;
}

void
sdm_message_get_payload(const sdm_message msg, char **buf, int *len)
{
	*buf = msg->payload;
	*len = msg->payload_len;
}

sdm_aggregate
sdm_message_get_aggregate(const sdm_message msg)
{
	return msg->aggregate;
}

void
sdm_message_set_payload_callback(void (*callback)(char *buf, int len))
{
	payload_callback = callback;
}

void
sdm_message_deliver_payload(const sdm_message msg)
{
	if (payload_callback != NULL) {
		payload_callback(msg->payload, msg->payload_len);
	}
}

static void
setenviron(char *str, int val)
{
	char *	buf;

	asprintf(&buf, "%d", val);
	setenv(str, buf, 1);
	free(buf);
}
