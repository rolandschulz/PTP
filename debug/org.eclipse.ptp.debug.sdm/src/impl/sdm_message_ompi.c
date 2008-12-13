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

#include "config.h"

#include <mpi.h>
#include <stdlib.h>
#include <string.h>

#include "compat.h"
#include "list.h"
#include "serdes.h"
#include "sdm.h"

struct sdm_message {
	unsigned int	id;				/* ID of the message */
	sdm_idset		dest; 			/* Destinations of the message */
	sdm_idset		src; 			/* Sources of the message */
	sdm_aggregate	aggregate;		/* Message aggregation */
	char *			payload;		/* Payload */
	int				payload_len;	/* Payload length */
	char *			buf;			/* Receive buffer */
	int				buf_len;		/* Receive buffer length */
	void 			(*send_complete)(sdm_message msg);
};

static void (*sdm_recv_callback)(const sdm_message msg) = NULL;
static void	(*deliver_callback)(const sdm_message msg) = NULL;

static void	setenviron(char *str, int val);

/**
 * Initialize the runtime abstraction. The jobid is the job ID
 * that was allocated by the OpenMPI runtime system. This is used
 * to set an environment variable that will enable MPI_Init to
 * establish communication.
 *
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

	/*
	 * Compute the immediate destinations for the message
	 */
	route = sdm_route_get_route(msg->dest);

	DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] sdm_message_send src %s dest %s route %s\n", sdm_route_get_id(),
		_set_to_str(msg->src),
		_set_to_str(msg->dest),
		_set_to_str(route));

	if (!sdm_set_is_empty(route)) {
		/*
		 * Create a serialized version of the message
		 */

		len = HEX_LEN /* sizeof(id) */
			+ sdm_aggregate_serialized_length(msg->aggregate)
			+ sdm_set_serialized_length(msg->src)
			+ sdm_set_serialized_length(msg->dest)
			+ msg->payload_len;

		p = buf = (char *)malloc(len);

		/*
		 * Note: len was the maximum length of the serialized buffer, we
		 * now calculate the actual length for the send.
		 */

		len = HEX_LEN;
		int_to_hex_str(msg->id, p, &p);
		len += sdm_aggregate_serialize(msg->aggregate, p, &p);
		len += sdm_set_serialize(msg->src, p, &p);
		len += sdm_set_serialize(msg->dest, p, &p);
		memcpy(p, msg->payload, msg->payload_len);
		len += msg->payload_len;

		/*
		 * Send the message to each destination. This could be replaced with ISend to parallelize the
		 * sends, but since we're phasing out MPI we don't worry.
		 */

		for (dest_id = sdm_set_first(route); !sdm_set_done(route); dest_id = sdm_set_next(route)) {
			int err;

			DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] Sending len %d to %d\n", sdm_route_get_id(), len, dest_id);

			err = MPI_Send(buf, len, MPI_CHAR, dest_id, 0, MPI_COMM_WORLD);
			if (err != MPI_SUCCESS) {
				DEBUG_PRINTS(DEBUG_LEVEL_CLIENT, "MPI_Send failed!\n");
				return -1;
			}
		}

		/*
		 * Free resources.
		 */
		free(buf);
	}

	/*
	 * Notify that the send is complete.
	 */
	if (msg->send_complete != NULL) {
		msg->send_complete(msg);
	}

	return 0;
}

/**
 * Message progress. Caller is responsible for freeing allocated message resources.
 *
 * @return 0 on success, -1 on failure
 */
int
sdm_message_progress(void)
{
	int				err;
	int				avail;
	int				n;
	int				len;
	char *			buf;
	MPI_Status		stat;

	err = MPI_Iprobe(MPI_ANY_SOURCE, 0, MPI_COMM_WORLD, &avail, &stat);

	if (err != MPI_SUCCESS) {
		DEBUG_PRINTS(DEBUG_LEVEL_CLIENT, "MPI_Iprobe failed!\n");
		return -1;
	}

	if (avail) {
		MPI_Get_count(&stat, MPI_CHAR, &len);

		buf = (char *)malloc(len);

		sdm_message msg = sdm_message_new(buf, len);

		err = MPI_Recv(buf, len, MPI_CHAR, stat.MPI_SOURCE, 0, MPI_COMM_WORLD, &stat);

		if (err != MPI_SUCCESS) {
			DEBUG_PRINTS(DEBUG_LEVEL_CLIENT, "MPI_Recv failed!\n");
			return -1;
		}

		DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] sdm_message_progress received len %d from %d\n", sdm_route_get_id(), len, stat.MPI_SOURCE);

		msg->id = hex_str_to_int(buf, &buf);
		len -= HEX_LEN;

		if ((n = sdm_aggregate_deserialize(msg->aggregate, buf, &buf)) < 0) {
			DEBUG_PRINTS(DEBUG_LEVEL_CLIENT, "MPI_Recv invalid header\n");
			return -1;
		}
		len -= n;

		if ((n = sdm_set_deserialize(msg->src, buf, &buf)) < 0) {
			DEBUG_PRINTS(DEBUG_LEVEL_CLIENT, "MPI_Recv invalid header\n");
			return -1;
		}
		len -= n;

		if ((n = sdm_set_deserialize(msg->dest, buf, &buf)) < 0) {
			DEBUG_PRINTS(DEBUG_LEVEL_CLIENT, "MPI_Recv invalid header\n");
			return -1;
		}
		len -= n;

		msg->payload = buf;
		msg->payload_len = len;

		DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] sdm_message_progress agg=%s src=%s dest=%s\n", sdm_route_get_id(),
				_aggregate_to_str(msg->aggregate),
				_set_to_str(msg->src),
				_set_to_str(msg->dest));

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
	static unsigned int ids = 0;
	sdm_message	msg = (sdm_message)malloc(sizeof(struct sdm_message));

	msg->id = ids++;
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
	DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] Enter sdm_message_free\n", sdm_route_get_id());

	free(msg->buf);
	sdm_set_free(msg->src);
	sdm_set_free(msg->dest);
	sdm_aggregate_free(msg->aggregate);
	free(msg);

	DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] Leaving sdm_message_free\n", sdm_route_get_id());
}

unsigned int
sdm_message_get_id(const sdm_message msg)
{
	return msg->id;
}

void
sdm_message_set_id(const sdm_message msg, unsigned int id)
{
	msg->id = id;
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
sdm_message_set_aggregate(const sdm_message msg, const sdm_aggregate a)
{
	sdm_aggregate_copy(msg->aggregate, a);
}

void
sdm_message_set_deliver_callback(void (*callback)(const sdm_message msg))
{
	deliver_callback = callback;
}

void
sdm_message_deliver(const sdm_message msg)
{
	if (deliver_callback != NULL) {
		DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] sdm_message_deliver \n", sdm_route_get_id());
		deliver_callback(msg);
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
