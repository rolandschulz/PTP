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
	sdm_idset	dest; /* All destinations */
	sdm_idset	src; /* Source of the message */
	char *		buf;
	int			len;
	int			tag;
	void 		(*send_complete)(sdm_message msg);
};

static List *	send_list = NULL;

static void (*sdm_recv_callback)(sdm_message msg);

static void
setenviron(char *str, int val)
{
	char *	buf;

	asprintf(&buf, "%d", val);
	setenv(str, buf, 1);
	free(buf);
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

	send_list = NewList();

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
	int			src_len;
	int			dest_len;
	char *		p;
	char *		src;
	char *		dest;
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

	src = sdm_set_serialize(msg->src);
	dest = sdm_set_serialize(msg->dest);

	src_len = strlen(src);
	dest_len = strlen(dest);
	len = src_len + dest_len + msg->len + 2;
	p = buf = (char *)malloc(len * sizeof(char));
	memcpy(p, src, src_len);
	p += src_len;
	memcpy(p, dest, dest_len);
	p += dest_len;
	memcpy(p, msg->buf, msg->len);

	free(src);
	free(dest);

	route = sdm_route_get_route(msg->dest);

	DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] sdm_message_send dest %s route %s\n", sdm_route_get_id(),
		_set_to_str(msg->dest),
		_set_to_str(route));

	for (dest_id = sdm_set_first(route); !sdm_set_done(route); dest_id = sdm_set_next(route)) {
		int err;

		DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] Sending (tag %d, len %d) to %d\n", sdm_route_get_id(), msg->tag, len, dest_id);

		err = MPI_Send(buf, len, MPI_CHAR, dest_id, msg->tag, MPI_COMM_WORLD);
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
	char *			src;
	char *			dest;
	char *			buf;
	MPI_Status		stat;

	err = MPI_Iprobe(MPI_ANY_SOURCE, MPI_ANY_TAG, MPI_COMM_WORLD, &avail, &stat);

	if (err != MPI_SUCCESS) {
		DEBUG_PRINTS(DEBUG_LEVEL_CLIENT, "MPI_Iprobe failed!\n");
		return -1;
	}

	if (avail) {
		sdm_message msg = sdm_message_new();

		MPI_Get_count(&stat, MPI_CHAR, &len);

		buf = (char *)malloc(len * sizeof(char));

		err = MPI_Recv(buf, len, MPI_CHAR, stat.MPI_SOURCE, stat.MPI_TAG, MPI_COMM_WORLD, &stat);

		if (err != MPI_SUCCESS) {
			DEBUG_PRINTS(DEBUG_LEVEL_CLIENT, "MPI_Recv failed!\n");
			return -1;
		}

		DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] Received (tag %d, len %d) from %d\n", sdm_route_get_id(), stat.MPI_TAG, len, stat.MPI_SOURCE);

		src = buf;
		sdm_set_deserialize(msg->src, src, &p);
		len -= (p - src);

		dest = p;
		sdm_set_deserialize(msg->dest, dest, &p);
		len -= (p - dest);

		msg->buf = p;
		msg->len = len;

		sdm_message_set_type(msg, stat.MPI_TAG);

		sdm_recv_callback(msg);

		sdm_message_free(msg);
		free(buf);
	}

	return 0;
}

int
sdm_message_get_type(const sdm_message msg)
{
	return msg->tag;
}

void
sdm_message_set_type(const sdm_message msg, int type)
{
	msg->tag = type;
}

sdm_message
sdm_message_new(void)
{
	sdm_message	msg = (sdm_message)malloc(sizeof(struct sdm_message));

	msg->dest = sdm_set_new();
	msg->src = sdm_set_new();
	msg->buf = NULL;
	msg->len = 0;
	msg->tag = SDM_MESSAGE_TYPE_NORMAL;
	msg->send_complete = NULL;

	return msg;
}

void
sdm_message_free(sdm_message msg)
{
	sdm_set_free(msg->dest);
	sdm_set_free(msg->src);
}

int
sdm_message_set_destination(const sdm_message msg, const sdm_idset dest_ids)
{
	sdm_set_union(msg->dest, dest_ids);
	return 0;
}

sdm_idset
sdm_message_get_destination(const sdm_message msg)
{
	return msg->dest;
}

int
sdm_message_set_source(const sdm_message msg, const sdm_id source_id)
{
	sdm_set_add_element(msg->src, source_id);
	return 0;
}

sdm_idset
sdm_message_get_source(const sdm_message msg)
{
	return msg->src;
}

int
sdm_message_set_data(const sdm_message msg, char *buf, int len)
{
	msg->buf = buf;
	msg->len = len;
	return 0;
}

int
sdm_message_get_data(const sdm_message msg, char **buf, int *len)
{
	*buf = msg->buf;
	*len = msg->len;
	return 0;
}
