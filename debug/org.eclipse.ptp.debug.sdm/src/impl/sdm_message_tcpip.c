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
 * SDM messages using TCP/IP.
 */

#include <sys/types.h>
#include <sys/socket.h>
#include <sys/select.h>
#include <netdb.h>
#include <errno.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "compat.h"
#include "list.h"
#include "serdes.h"
#include "sdm.h"
#include "helloproto.h"

#define MESSAGE_LENGTH_SIZE	8
#define MESSAGE_ID_SIZE		8

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

/**
 * Base type for a linked list of ids and respective
 * socket descriptors
 */
typedef struct id_sockd_map {
	sdm_id id;
	int sockd;
	struct id_sockd_map *next;
} sdm_id_sockd_map, *sdm_id_sockd_map_p;

static void (*sdm_recv_callback)(sdm_message msg) = NULL;
static void	(*deliver_callback)(const sdm_message msg) = NULL;

#define MAX_PORT_INCREMENT	1000	/* Max port increment before failing */
#define CHILD_CONNECT_TRIES	5		/* Number of times to try each port */
#define CHILD_CONNECT_SLEEP	1		/* Seconds to sleep between each connection attempt */

sdm_id_sockd_map_p children_sockd_map, parent_sockd_map; /* Leaf case, children = NULL
									 						Root case, parent = NULL */
/** Begin of functions created by Richard **/

/**
 * Create the maps and fill with the ids of the children and parent
 */
void
sdm_create_sockd_map()
{
	sdm_idset all_nodes;
	sdm_idset adjacent_nodes;

	DEBUG_PRINTF(DEBUG_LEVEL_MESSAGES, "[%d] in sdm_create_sockd_map\n",
			sdm_route_get_id());

	// Set parent map
	if (sdm_route_get_id() == SDM_MASTER) {
		parent_sockd_map = NULL; // Root
	} else {
		parent_sockd_map = (sdm_id_sockd_map_p)malloc(sizeof(sdm_id_sockd_map));
		parent_sockd_map->id = sdm_route_get_parent();
	}

	// Get all adjacent nodes
	all_nodes = sdm_set_new();
	all_nodes = sdm_set_add_all(all_nodes, sdm_route_get_size()-1);

	// Filter the parent node and master
	if (parent_sockd_map != NULL) {
		sdm_set_remove_element(all_nodes, parent_sockd_map->id);
		sdm_set_remove_element(all_nodes, SDM_MASTER);
	}

	adjacent_nodes = sdm_route_get_route(all_nodes);

	DEBUG_PRINTF(DEBUG_LEVEL_MESSAGES, "[%d] adjacent nodes: %s\n",
			sdm_route_get_id(), _set_to_str(adjacent_nodes));

	children_sockd_map = NULL;

	if (!sdm_set_is_empty(adjacent_nodes)) {
		// Traverse adjacent nodes adding them to the children linked list
		sdm_id child;
		sdm_id_sockd_map_p p;

		for (child = sdm_set_first(adjacent_nodes); !sdm_set_done(adjacent_nodes);
		 child = sdm_set_next(adjacent_nodes)) {
			DEBUG_PRINTF(DEBUG_LEVEL_MESSAGES, "[%d] adding %d to my map\n",
					sdm_route_get_id(), child);
			p = (sdm_id_sockd_map_p)malloc(sizeof(sdm_id_sockd_map));
			p->next = children_sockd_map;
			p->id = child;
			children_sockd_map = p;
		}
	}
	sdm_set_free(all_nodes);
}

/**
 * Binds and listen to a port. After accepting connection, updates the
 *
 * @return socket descriptor if successful, -1 if error
 */
int
sdm_parent_port_bind(int parentbaseport)
{
	int sockfd, peersockfd;
	int parentport = parentbaseport;

	struct sockaddr_in sockaddr_info;
	struct sockaddr_in peersockaddr_info;
	struct in_addr addr = {INADDR_ANY};
	unsigned peeraddr_len = sizeof(struct in_addr);

	//.Create a socket and bind to the port
	sockfd = socket(PF_INET, SOCK_STREAM, 0);

	DEBUG_PRINTF(DEBUG_LEVEL_MESSAGES, "[%d] [ACCEPT] sockfd: %d\n", sdm_route_get_id(), sockfd);

	memset(&sockaddr_info, 0, sizeof(struct sockaddr_in));
	sockaddr_info.sin_family = AF_INET;
	sockaddr_info.sin_port = htons(parentport);
	sockaddr_info.sin_addr = addr;

	// Try to bind port until MAX_PORT_INCREMENT, except if bind error is not EADDRINUSE
	while (parentport < parentbaseport + MAX_PORT_INCREMENT) {
		if (bind(sockfd, (struct sockaddr *)&(sockaddr_info), sizeof(struct sockaddr_in)) >= 0) {
			DEBUG_PRINTF(DEBUG_LEVEL_MESSAGES, "[%d] port bound: %d\n", sdm_route_get_id(), parentport);
			break;
		}

		if (errno != EADDRINUSE) {
			// Error!
			DEBUG_PRINTF(DEBUG_LEVEL_MESSAGES, "[%d] Error binding to the port! - errno %d\n",
					sdm_route_get_id(), errno);
			return -1;
		}

		// Increment port number and try again
		sockaddr_info.sin_port = htons(++parentport);
	}

	if (parentport < parentbaseport + MAX_PORT_INCREMENT) {
		DEBUG_PRINTF(DEBUG_LEVEL_MESSAGES, "[%d] bound to port %d\n", sdm_route_get_id(), parentport);
	} else {
		DEBUG_PRINTF(DEBUG_LEVEL_MESSAGES, "[%d] could not find port to bind to\n", sdm_route_get_id());
		close(sockfd);
		return -1;
	}

	if (listen(sockfd, 5) < 0) {
		DEBUG_PRINTF(DEBUG_LEVEL_MESSAGES, "[%d] Error listening to the port %d!\n", sdm_route_get_id(), parentport);
		return -1;
	}

	// Wait for a valid client to connect
	while (1) {
		memset(&peersockaddr_info, 0, sizeof(struct sockaddr_in));
		peersockfd = accept(sockfd, (struct sockaddr *)&(peersockaddr_info), &peeraddr_len);

		DEBUG_PRINTF(DEBUG_LEVEL_MESSAGES, "[%d] peersockfd: %d\n", sdm_route_get_id(), peersockfd);

		if (peersockfd < 0) {
			perror("Status of accepting data from parent");
			DEBUG_PRINTF(DEBUG_LEVEL_MESSAGES, "[%d] Could not accept port connection on port %d - errno %d!\n",
					sdm_route_get_id(), parentport, errno);
			return -1;
		}

		// Check if it is connected to another sdm process before returning sockfd
		if (is_client_peer_valid(peersockfd)) {
			break;
		}
		close(peersockfd);
	}

	// We don't need to wait for connections anymore
	close(sockfd);

	return peersockfd;
}

/**
 * Connect to the child node using hostname. Port is the first that connects between
 * childbaseport and childbaseport + MAX_PORT_INCREMENT
 *
 * Try CHILD_CONNECT_TRIES to connect to each port, delaying CHILD_CONNECT_SLEEP between each attempt.
 *
 * This function also checks if the protocol is the sdm one.
 */
int
sdm_connect_to_child(char *hostname, int childbaseport)
{
	struct addrinfo hints, *result;
	int sockfd;
	int childport;
	int num_tries;

	memset(&hints, 0, sizeof(struct addrinfo));
	hints.ai_family = PF_INET;
	hints.ai_socktype = SOCK_STREAM;

	for (childport = childbaseport;childport < childbaseport + MAX_PORT_INCREMENT; childport++) {
		char port_str[10];

		// Get first result from the linked list
		sockfd = socket(PF_INET, SOCK_STREAM, 0);
		if (sockfd < 0) {
			perror("Socket");
			DEBUG_PRINTF(DEBUG_LEVEL_MESSAGES, "[%d] socket syscall error\n", sdm_route_get_id());
			return -1;
		}

		// Connect to the provided address & port
		sprintf(port_str, "%d", childport);
		if (getaddrinfo(hostname, port_str, &hints, &result)) {
			perror("getaddrinfo");
			DEBUG_PRINTF(DEBUG_LEVEL_MESSAGES, "[%d] getaddrinfo error. hostname: %s, port: %s\n",
					sdm_route_get_id(), hostname, port_str);
			return -1;
		}

		for (num_tries = 0; num_tries < CHILD_CONNECT_TRIES; num_tries++) {
			if (connect(sockfd, result->ai_addr, result->ai_addrlen) >= 0) {
				break;
			}

			if( (errno != ECONNREFUSED) && (errno != ENETUNREACH) &&
				(errno != ETIMEDOUT) ) {
				perror("connect");
				freeaddrinfo(result);
				DEBUG_PRINTF(DEBUG_LEVEL_MESSAGES, "[%d] connect error. hostname: %s, port: %s\n",
						sdm_route_get_id(), hostname, port_str);
				return -1;
			}

			sleep(CHILD_CONNECT_SLEEP);
		}

		if (num_tries < CHILD_CONNECT_TRIES){
			freeaddrinfo(result);

			// Check if it is connected to another sdm process before returning sockfd
			if (is_server_peer_valid(sockfd)) {
				return sockfd;
			}

			close(sockfd);
		}
	}
	// No valid peer found!
	DEBUG_PRINTF(DEBUG_LEVEL_MESSAGES, "[%d] No port found for the sdm child. hostname: %s\n",
			sdm_route_get_id(), hostname);
	return -1;
}

/**
 * Initializes the message layer using tcp/ip to transport data
 */
int
sdm_tcpip_init()
{
	routing_table_entry *	entry;
	sdm_id_sockd_map_p		mapp;

	sdm_create_sockd_map();

	// If it isn't the master node bind to the port provided
	if (sdm_route_get_id() != SDM_MASTER) {
		int parentsockd = -1;

		DEBUG_PRINTF(DEBUG_LEVEL_MESSAGES, "[%d] This node is a server!\n", sdm_route_get_id());

		for (sdm_routing_table_set(); (entry = sdm_routing_table_next()) != NULL; ) {
			DEBUG_PRINTF(DEBUG_LEVEL_MESSAGES, "[%d] found entry for node %d\n", sdm_route_get_id(), entry->nodeID);
			if(entry->nodeID == sdm_route_get_id()) {
				DEBUG_PRINTF(DEBUG_LEVEL_MESSAGES, "[%d] found my entry\n", sdm_route_get_id());
				parentsockd = sdm_parent_port_bind(entry->port);
				break;
			}
		}

		if (parentsockd < 0) {
			DEBUG_PRINTF(DEBUG_LEVEL_MESSAGES, "[%d] failed to bind to port\n", sdm_route_get_id());
			return -1;
		}

		DEBUG_PRINTF(DEBUG_LEVEL_MESSAGES, "[%d] Parent %d successfully connected\n", sdm_route_get_id(), parent_sockd_map->id);

		// Update the parent socket descriptor
		parent_sockd_map->sockd = parentsockd;
	}

	// If node is leaf, initialization is done
	if (children_sockd_map == NULL) {
		DEBUG_PRINTF(DEBUG_LEVEL_MESSAGES, "[%d] This node is a leaf\n", sdm_route_get_id());
		return 0;
	}

	// Otherwise, open connections to the children

	for (sdm_routing_table_set(); (entry = sdm_routing_table_next()) != NULL; ) {
		int childsockd;

		mapp = children_sockd_map;
		while (mapp != NULL) {
			if (mapp->id == entry->nodeID) {
				// ID found! Connect to the children and generate a socket descriptor
				childsockd = sdm_connect_to_child(entry->hostname, entry->port);

				if (childsockd < 0) {
					DEBUG_PRINTF(DEBUG_LEVEL_MESSAGES, "[%d] failed to connect to child %s:%d\n", sdm_route_get_id(), entry->hostname, entry->port);
					return -1;
				}
				DEBUG_PRINTF(DEBUG_LEVEL_MESSAGES, "[%d] Connection to child %d successful\n", sdm_route_get_id(), mapp->id);

				// Set the children socket descriptor
				mapp->sockd = childsockd;
				break;
			}
			mapp = mapp->next;
		}
	}

	return 0;
}

/**
 * Close sockets and free all map structures
 */
void
sdm_clean_maps()
{
	if (parent_sockd_map != NULL) {
		close(parent_sockd_map->sockd);
		free(parent_sockd_map);
	}

	while (children_sockd_map != NULL) {
		sdm_id_sockd_map_p p = children_sockd_map->next;
		close(children_sockd_map->sockd);
		free(children_sockd_map);
		children_sockd_map = p;
	}
}

/**
 * Given a nodeid, return an associated socket descriptor
 *
 * @return socket descriptor on success, -1 on error
 */
int
sdm_fetch_sockd(int nodeid)
{
	sdm_id_sockd_map_p p;

	// Look for nodeid on the children list first
	p = children_sockd_map;
	while (p != NULL) {
		if (p->id == nodeid) {
			return p->sockd;
		}
		p = p->next;
	}

	// Then, must be the parent
	p = parent_sockd_map;
	if (p->id == nodeid) {
		return p->sockd;
	}

	// Not mapped! Return an error code!
	return -1;
}

/**
 * Given a sockid, return an associated id
 *
 * @return id on success, -1 on error
 */
int
sdm_fetch_nodeid(int sockd)
{
	sdm_id_sockd_map_p p;

	// Look for nodeid on the children list first
	p = children_sockd_map;
	while (p != NULL) {
		if (p->sockd == sockd) {
			return p->id;
		}
		p = p->next;
	}

	// Then, must be the parent
	p = parent_sockd_map;
	if (p->sockd == sockd) {
		return p->id;
	}

	// Not mapped! Return an error code!
	return -1;
}

/**
 * Inspect all node's sockets for data, returning the socket descriptor
 * of a socket which contains data.
 *
 * @return socket descriptor on success, -1 on error, -2 if no socket has data
 */
int
sdm_get_active_sock_desc()
{
	fd_set rfdset;
	int fdmax = -1;
	int selrv;
	sdm_id_sockd_map_p p;

	struct timeval time = {0, 10000};

	// Add all valid sockets to the FD_SET
	FD_ZERO(&rfdset);
	if (parent_sockd_map != NULL) {
		FD_SET(parent_sockd_map->sockd, &rfdset);
		fdmax = parent_sockd_map->sockd;
	}

	for (p = children_sockd_map; p != NULL; ) {
		FD_SET(p->sockd, &rfdset);

		if (p->sockd > fdmax) {
			fdmax = p->sockd;
		}

		p = p->next;
	}

	if (fdmax < 0) {
		// No children socket opened
		return -2;
	}

	// Check if any of the sockets connected
	// but don't block
	while ((selrv = select(fdmax + 1, &rfdset, NULL, NULL, &time)) < 0) {
		if (errno != EINTR) {
			DEBUG_PRINTS(DEBUG_LEVEL_MESSAGES, "select syscall failed!\n");
			return -1;
		}
	}

	if (selrv == 0) { // No socket has data. Return appropriated code
//		printf("The sockets listened dont have data!\n");
		return -2;
	}

	// return the first active socket descriptor
	for (p = children_sockd_map; p != NULL; ) {
		if (FD_ISSET(p->sockd, &rfdset)) {
			return p->sockd;
		}

		p = p->next;
	}

	if (parent_sockd_map != NULL) {
		if (FD_ISSET(parent_sockd_map->sockd, &rfdset)) {
			return parent_sockd_map->sockd;
		}
	}

	// Should not reach this point
	return -1;
}

/**
 * Provides the number of bytes to be read of the message
 * (the size of the header is already subtracted)
 */
int
sdm_tcpip_msgheader_receive(int sockfd, int *length)
{
	int		n;
	int		len;
	char *	p;
	char	length_str[MESSAGE_LENGTH_SIZE];

	p = length_str;
	len = MESSAGE_LENGTH_SIZE;

	while ((n = read(sockfd, p, len)) < len) {
		if (n <= 0) {
			if (n < 0) {
				if (errno == EINTR) {
					continue;
				}
				perror("Status on read syscall");
			}
			return -1;
		}

		p += n;
		len -= n;
	}

	DEBUG_PRINTF(DEBUG_LEVEL_PROTOCOL, "HEADER:<%.*s>\n", MESSAGE_LENGTH_SIZE, length_str);

	*length = hex_str_to_int(length_str, MESSAGE_LENGTH_SIZE, NULL) - MESSAGE_LENGTH_SIZE;

	return 0;
}

int
sdm_tcpip_msgbody_receive(int sockfd, char *buf, int length)
{
	int n;

	while ((n = read(sockfd, buf, length)) < length) {
		if (n <= 0) {
			if (n < 0) {
				if (errno == EINTR) {
					continue;
				}
				perror("Status on read syscall");
			}
			return -1;
		}
		buf += n;
		length -= n;
	}

	DEBUG_PRINTF(DEBUG_LEVEL_PROTOCOL, "BODY:<%*s>\n", length, buf);

	return 0;
}

int
sdm_tcpip_send(int sockd, char *buf, int length)
{
	int wcount;

	// Write all message to the socket
	do {
		wcount = write(sockd, (void *)buf, length);

		if (wcount <= 0) {
			if (wcount < 0) {
				perror("write syscall status");
			}
			DEBUG_PRINTS(DEBUG_LEVEL_MESSAGES, "Could not send message data - write syscall failed!\n");
			return -1;
		}

		buf += wcount;
		length -= wcount;
	} while(length > 0);

	return 0;
}

/** End of functions created by Richard **/

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
	/*
	 * Initializes the communication layer
	 */
	if (sdm_tcpip_init() < 0) {
		return -1;
	}

	return 0;
}

/**
 * Finalize the message abstraction.
 */
void
sdm_message_finalize()
{
	// Clean the maps
	sdm_clean_maps();
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
	int			maxlen; // effective and maximum length of the message
							 // these two vars must have the same size!
	char *		p;
	char *		header_addr; // Pointer to the header of the message
	char *		buf;
	sdm_id		dest_id;
	sdm_idset	route;

	/*
	 * Remove our id from the destination of the message before forwarding.
	 */
	if (sdm_set_contains(msg->dest, sdm_route_get_id())) {
		sdm_set_remove_element(msg->dest, sdm_route_get_id());
		DEBUG_PRINTF(DEBUG_LEVEL_MESSAGES, "[%d] sdm_message_send removing me from dest\n", sdm_route_get_id());
	}

	/*
	 * Compute the immediate destinations for the message
	 */
	route = sdm_route_get_route(msg->dest);

	DEBUG_PRINTF(DEBUG_LEVEL_MESSAGES, "[%d] sdm_message_send src %s dest %s route %s\n", sdm_route_get_id(),
		_set_to_str(msg->src),
		_set_to_str(msg->dest),
		_set_to_str(route));

	if (!sdm_set_is_empty(route)) {
		/*
		 * Create a serialized version of the message
		 * This includes the length of the message at the beginning
		 */

		maxlen = MESSAGE_LENGTH_SIZE
			+ MESSAGE_ID_SIZE
			+ sdm_aggregate_serialized_length(msg->aggregate)
			+ sdm_set_serialized_length(msg->src)
			+ sdm_set_serialized_length(msg->dest)
			+ msg->payload_len;

		p = buf = (char *)malloc(maxlen);

		// Saves the address of the header
		header_addr = p;

		p += MESSAGE_LENGTH_SIZE; // Points to the body of the message

		/*
		 * Note: maxlen was the maximum length of the serialized buffer, we
		 * now calculate the actual length for the send.
		 */
		len = MESSAGE_LENGTH_SIZE;
		len += MESSAGE_ID_SIZE;
		int_to_hex_str(msg->id, p, MESSAGE_ID_SIZE, &p);
		len += sdm_aggregate_serialize(msg->aggregate, p, &p);
		len += sdm_set_serialize(msg->src, p, &p);
		len += sdm_set_serialize(msg->dest, p, &p);
		memcpy(p, msg->payload, msg->payload_len);
		len += msg->payload_len;

		// Copies the actual length to the header of the message
		int_to_hex_str(len, header_addr, MESSAGE_LENGTH_SIZE, NULL);

		/*
		 * Send the message to each destination, converting the
		 * node ID to a proper address before the transmission
		 */

		for (dest_id = sdm_set_first(route); !sdm_set_done(route); dest_id = sdm_set_next(route)) {
			int sockd;

			DEBUG_PRINTF(DEBUG_LEVEL_MESSAGES, "[%d] Sending len %d to %d\n", sdm_route_get_id(), len, dest_id);

			// Get socket descriptor corresponding to the node where the message will be sent
			sockd = sdm_fetch_sockd(dest_id);
			if (sockd < 0) {
				DEBUG_PRINTS(DEBUG_LEVEL_MESSAGES, "Socket descriptor not found!\n");
				return -1;
			}

			// Write all message to the socket
			if (sdm_tcpip_send(sockd, buf, len) < 0) {
				DEBUG_PRINTS(DEBUG_LEVEL_MESSAGES, "Error sending message!\n");
				return -1;
			}
		}

		/*
		 * Free resources.
		 */
		free(buf);
	}

	DEBUG_PRINTF(DEBUG_LEVEL_MESSAGES, "[%d] About to call send_complete\n", sdm_route_get_id());

	/*
	 * Notify that the send is complete.
	 */
	if (msg->send_complete != NULL) {
		msg->send_complete(msg);
	}

	DEBUG_PRINTF(DEBUG_LEVEL_MESSAGES, "[%d] Leaving sdm_message_send\n", sdm_route_get_id());

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
	int		n;
	int		err;
	int		len;
	char *	buf;

	// Retrieve the active socket descriptor
	int sockfd = sdm_get_active_sock_desc();

	if (sockfd == -1) {
		DEBUG_PRINTF(DEBUG_LEVEL_MESSAGES, "[%d] Error retrieving socket descriptor\n", sdm_route_get_id());
		return -1;
	}

	if (sockfd >= 0) {
		DEBUG_PRINTF(DEBUG_LEVEL_MESSAGES, "[%d] receiving header\n", sdm_route_get_id());

		err = sdm_tcpip_msgheader_receive(sockfd, &len);

		if(err != 0) {
			DEBUG_PRINTF(DEBUG_LEVEL_MESSAGES, "[%d] Error retrieving message size!\n", sdm_route_get_id());
			return -1;
		}

		buf = (char *)malloc(len);

		sdm_message msg = sdm_message_new(buf, len);

		if(sdm_tcpip_msgbody_receive(sockfd, buf, len) < 0) {
			DEBUG_PRINTF(DEBUG_LEVEL_MESSAGES, "[%d] Error retrieving message!\n", sdm_route_get_id());
			return -1;
		}

		DEBUG_PRINTF(DEBUG_LEVEL_MESSAGES, "[%d] sdm_message_progress received len %d from %d\n",
				sdm_route_get_id(), len, sdm_fetch_nodeid(sockfd));

		msg->id = hex_str_to_int(buf, MESSAGE_ID_SIZE, &buf);
		len -= MESSAGE_ID_SIZE;

		if ((n = sdm_aggregate_deserialize(msg->aggregate, buf, &buf)) < 0) {
			DEBUG_PRINTF(DEBUG_LEVEL_MESSAGES, "[%d] invalid header\n", sdm_route_get_id());
			return -1;
		}
		len -= n;

		if ((n = sdm_set_deserialize(msg->src, buf, &buf)) < 0) {
			DEBUG_PRINTF(DEBUG_LEVEL_MESSAGES, "[%d] invalid header\n", sdm_route_get_id());
			return -1;
		}
		len -= n;

		if ((n = sdm_set_deserialize(msg->dest, buf, &buf)) < 0) {
			DEBUG_PRINTF(DEBUG_LEVEL_MESSAGES, "[%d] invalid header\n", sdm_route_get_id());
			return -1;
		}
		len -= n;

		msg->payload = buf;
		msg->payload_len = len;

		DEBUG_PRINTF(DEBUG_LEVEL_MESSAGES, "[%d] sdm_message_progress agg=%s src=%s dest=%s\n",
				sdm_route_get_id(),
				_aggregate_to_str(msg->aggregate),
				_set_to_str(msg->src),
				_set_to_str(msg->dest));

		DEBUG_PRINTF(DEBUG_LEVEL_MESSAGES, "[%d] About to call recv_callback\n", sdm_route_get_id());

		if (sdm_recv_callback  != NULL) {
			sdm_recv_callback(msg);
		}

		DEBUG_PRINTF(DEBUG_LEVEL_MESSAGES, "[%d] Finished recv_callback\n", sdm_route_get_id());
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
	free(msg->buf);
	sdm_set_free(msg->src);
	sdm_set_free(msg->dest);
	sdm_aggregate_free(msg->aggregate);
	free(msg);
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
		DEBUG_PRINTF(DEBUG_LEVEL_MESSAGES, "[%d] sdm_message_deliver \n", sdm_route_get_id());
		deliver_callback(msg);
	}
}
