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
#include <getopt.h>

#include "compat.h"
#include "list.h"
#include "serdes.h"
#include "sdm.h"
#include "routetable.h"
#include "helloproto.h"

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

#define MAX_PORT_INCREMENT 1000 /* Max port increment before failing */

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

	DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] in sdm_create_sockd_map\n",
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

	DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] adjacent nodes: %s\n",
			sdm_route_get_id(), _set_to_str(adjacent_nodes));

	children_sockd_map = NULL;

	if (!sdm_set_is_empty(adjacent_nodes)) {
		// Traverse adjacent nodes adding them to the children linked list
		sdm_id child;
		sdm_id_sockd_map_p p;

		for (child = sdm_set_first(adjacent_nodes); !sdm_set_done(adjacent_nodes);
		 child = sdm_set_next(adjacent_nodes)) {
			DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] adding %d to my map\n",
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

	//printf("[ACCEPT] sockfd: %d\n", sockfd);

	memset(&sockaddr_info, 0, sizeof(struct sockaddr_in));
	sockaddr_info.sin_family = AF_INET;
	sockaddr_info.sin_port = htons(parentport);
	sockaddr_info.sin_addr = addr;

	// Try to bind port until MAX_PORT_INCREMENT, except if bind error is not EADDRINUSE
	for(;parentport < parentbaseport + MAX_PORT_INCREMENT; parentport++) {
		if(bind(sockfd, (struct sockaddr *)&(sockaddr_info), sizeof(struct sockaddr_in)) < 0) {
			if(errno != EADDRINUSE) {
				// Error!
				DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] Could not bind to the port! - errno %d\n",
						sdm_route_get_id(), errno);
				return -1;
			}
		} else { // Bound successfully
			//printf("port bound: %d\n", parentport);
			break;
		}
		// Increment port number and try again
	}

	if(listen(sockfd, 5) < 0) {
		DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] Error listening to the port %d!\n", sdm_route_get_id(), parentport);
		return -1;
	}

	// Wait for a valid client to connect
	while(1) {
		memset(&peersockaddr_info, 0, sizeof(struct sockaddr_in));
		peersockfd = accept(sockfd, (struct sockaddr *)&(peersockaddr_info), &peeraddr_len);

		//printf("peersockfd: %d\n", peersockfd);

		if(peersockfd < 0) {
			perror("Status of accepting data from parent");
			DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] Could not accept port connection on port %d - errno %d!\n",
					sdm_route_get_id(), parentport, errno);
			return -1;
		}

		// Check if it is connected to another sdm process before returning sockfd
		if(is_client_peer_valid(peersockfd)) {
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
 * This function also checks if the protocol is the sdm one.
 */
int
sdm_connect_to_child(char *hostname, int childbaseport)
{
	struct addrinfo hints, *result;
	int sockfd;
	int childport;

	memset(&hints, 0, sizeof(struct addrinfo));
	hints.ai_family = PF_INET;
	hints.ai_socktype = SOCK_STREAM;

	for(childport = childbaseport;childport < childbaseport + MAX_PORT_INCREMENT;
	 childport++) {
		char port_str[10];

		// Get first result from the linked list
		sockfd = socket(PF_INET, SOCK_STREAM, 0);
		if(sockfd < 0) {
			perror("Socket");
			DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] socket syscall error\n", sdm_route_get_id());
			return -1;
		}

		// Connect to the provided address & port
		sprintf(port_str, "%d", childport);
		if(getaddrinfo(hostname, port_str, &hints, &result)) {
			perror("getaddrinfo");
			DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] getaddrinfo error. hostname: %s, port: %s\n",
					sdm_route_get_id(), hostname, port_str);
			return -1;
		}
		//printf("connecting to hostname %s port %d\n", hostname, childport);
		if(connect(sockfd, result->ai_addr, result->ai_addrlen) < 0) {
			if( (errno != ECONNREFUSED) && (errno != ENETUNREACH) &&
				(errno != ETIMEDOUT) ) {
				perror("connect");
				freeaddrinfo(result);
				DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] connect error. hostname: %s, port: %s\n",
						sdm_route_get_id(), hostname, port_str);
				return -1;
			}
		} else {
			freeaddrinfo(result);

			// Check if it is connected to another sdm process before returning sockfd
			if(is_server_peer_valid(sockfd)) {
				return sockfd;
			}
			close(sockfd);
		}
	}
	// No valid peer found!
	DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] No port found for the sdm child. hostname: %s\n",
			sdm_route_get_id(), hostname);
	return -1;
}

/**
 * Initializes the message layer using tcp/ip to transport data
 */
int
sdm_tcpip_init()
{
	FILE *rt_file;
	int rv, i;
	int tbl_size;
	struct routing_tbl_struct table_entry;
	sdm_id_sockd_map_p mapp;


	sdm_create_sockd_map();

	// Master and servers wait for the routing file to appear
	rv = wait_for_routing_file("routing_file", &rt_file, 10); //TODO: Get filename from the environment
	if(rv == -1) { // No need to close, since wait_for_routing_file does it when error
		// Error!
		DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] Error opening the routing file\n", sdm_route_get_id());
		return -1;
	} else if(rv == -2){
		DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] Timeout while waiting for routing file\n", sdm_route_get_id());
		return -1;
	}

	// If it isn't the master node bind to the port provided
	if(sdm_route_get_id() != SDM_MASTER) {
		int parentsockd = -1;

		//printf("This node is a server!\n");

		rv = read_routing_table_size(rt_file, &tbl_size);
		if(rv < 0) {
			DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] Error reading routing table size\n", sdm_route_get_id());
			goto error;
		}

		// For each entry, verify against the parent structure to see if the ID matches
		for(i = 0; i < tbl_size; i++) {
			int numericId;
			rv = read_routing_table_entry(rt_file, &table_entry);

			if(rv < 0) {
				DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] Error reading routing table entry\n", sdm_route_get_id());
				goto error;
				/*close_routing_file(rt_file);
				return -1;*/
			}
			//printf("NodeIdTable: %s\n", table_entry.nodeID);
			numericId = strtol(table_entry.nodeID, NULL, 10);
			//printf("numId: %d, parentId: %d, myid: %d\n", numericId, parent_sockd_map->id, sdm_route_get_id());
			if(numericId == sdm_route_get_id()) {
				parentsockd = sdm_parent_port_bind(table_entry.port);
				break;
			}
		}

		//parentsockd = sdm_parent_port_bind(parentport);
		//printf("parentsockd returns from bind: %d\n", parentsockd);
		if(parentsockd < 0) {
			goto error;
		}
		//printf("Parent %d successfully connected\n", parent_sockd_map->id);

		// Update the parent socket descriptor
		parent_sockd_map->sockd = parentsockd;
	}

	// If node is leaf, initialization is done
	if(children_sockd_map == NULL) {
		//printf("This node is a leaf\n");
		close_routing_file(rt_file);
		return 0;
	}

	// Otherwise, open connections to the children

	rv = read_routing_table_size(rt_file, &tbl_size);
	if(rv < 0) {
		DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] Error reading routing table size\n", sdm_route_get_id());
		goto error;
	}
	// For each entry, scan the children structure to see if the ID matches
	for(i = 0; i < tbl_size; i++) {
		int childsockd;
		rv = read_routing_table_entry(rt_file, &table_entry);

		if(rv < 0) {
			DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] Error reading routing table entry\n", sdm_route_get_id());
			goto error;
		}

		mapp = children_sockd_map;
		while(mapp != NULL) {
			int numericId = strtol(table_entry.nodeID, NULL, 10);
			//printf("read id: %d\n", mapp->id);
			if(mapp->id == numericId) {
				//printf("Childrenid found! Start connection!\n");
				// ID found! Connect to the children and generate a socket descriptor
				childsockd = sdm_connect_to_child(table_entry.hostname, table_entry.port);

				if(childsockd < 0) {
					goto error;
				}
				//printf("Connection to child %d successful\n", mapp->id);

				// Set the children socket descriptor
				mapp->sockd = childsockd;
				break;
			}
			mapp = mapp->next;
		}

		//printf("ID: %s, host: %s, port: %d\n", table_entry.nodeID, table_entry.hostname, table_entry.port);
	}

	close_routing_file(rt_file);

	return 0;

error:
	close_routing_file(rt_file);
	return -1;
}

/**
 * Close sockets and free all map structures
 */
void
sdm_clean_maps()
{
	if(parent_sockd_map != NULL) {
		close(parent_sockd_map->sockd);
		free(parent_sockd_map);
	}

	while(children_sockd_map != NULL) {
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
	//printf("Looking for the socket associated with nodeid: %d\n", nodeid);
	p = children_sockd_map;
	while(p != NULL) {
		if(p->id == nodeid) {
			//printf("child sockd %d matches\n", p->sockd);
			return p->sockd;
		}
		p = p->next;
	}

	// Then, must be the parent
	p = parent_sockd_map;
	if(p->id == nodeid) {
		//printf("parent sockd %d matches\n", p->sockd);
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
	while(p != NULL) {
		if(p->sockd == sockd) {
			return p->id;
		}
		p = p->next;
	}

	// Then, must be the parent
	p = parent_sockd_map;
	if(p->sockd == sockd) {
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

	struct timeval time = {0, 0};

	// Add all valid sockets to the FD_SET
	FD_ZERO(&rfdset);
	if(parent_sockd_map != NULL) {
//		printf("Inserting parentsockd %d on FD_SET\n", parent_sockd_map->sockd);
		FD_SET(parent_sockd_map->sockd, &rfdset);
		fdmax = parent_sockd_map->sockd;
	}
	p = children_sockd_map;
	while(p != NULL) {
//		printf("Inserting childsockd %d on FD_SET\n", p->sockd);
		FD_SET(p->sockd, &rfdset);

		if(p->sockd > fdmax) {
			fdmax = p->sockd;
		}

		p = p->next;
	}
//	printf("fdmax: %d\n", fdmax);

	if(fdmax < 0) {
		// No children socket opened
//		printf("No children socket opened!\n");
		return -2;
	}

	// Check if any of the sockets connected
	// but don't block
	selrv = select(fdmax + 1, &rfdset, NULL, NULL, &time);

	if(selrv < 0) {
		perror("select syscall status");
		DEBUG_PRINTS(DEBUG_LEVEL_CLIENT, "select syscall failed!\n");
		return -1;
	} else if(selrv == 0) { // No socket has data. Return appropriated code
//		printf("The sockets listened dont have data!\n");
		return -2;
	}

	// return the first active socket descriptor
	p = children_sockd_map;
	while(p != NULL) {
		if(FD_ISSET(p->sockd, &rfdset)) {
//			printf("childsockd %d active on FD_SET\n", p->sockd);
			return p->sockd;
		}

		p = p->next;
	}
	if(parent_sockd_map != NULL) {
		if(FD_ISSET(parent_sockd_map->sockd, &rfdset)) {
//			printf("parentsockd %d active on FD_SET\n", parent_sockd_map->sockd);
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
	int rv;

	rv = read(sockfd, (void *)length, sizeof(int));

//	printf("Read %d bytes from the message\n", *length);

	// Subtract sizeof(*length), since header was read
	*length -= sizeof(*length);


	// Read cannot be empty or generate an error
	if (rv <= 0) {
		if (rv < 0) {
			perror("Status on read syscall");
		}
		return -1;
	}

	return 0;
}

int
sdm_tcpip_msgbody_receive(int sockfd, char *buf, int length)
{
	int rcount;

	do {
		rcount = read(sockfd, buf, length);
//		printf("Read %d bytes from the message\n", rcount);

		if (rcount <= 0) {
			if (rcount < 0) {
				perror("Status on read syscall");
			}
			return -1;
		}
		buf += rcount;
		length -= rcount;
	} while(length > 0);

	return 0;
}

int
sdm_tcpip_send(int sockd, char *buf, int length)
{
	int wcount;

	// Write all message to the socket
	do {
		wcount = write(sockd, (void *)buf, length);
//		printf("wrote %d bytes\n", length);

		if (wcount <= 0) {
			if (wcount < 0) {
				perror("write syscall status");
			}
			DEBUG_PRINTS(DEBUG_LEVEL_CLIENT, "Could not send message data - write syscall failed!\n");
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
	int			len, maxlen; // effective and maximum length of the message
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
		 * This includes the length of the message at the beginning
		 */

		maxlen = sizeof(maxlen)
			+ HEX_LEN
			+ sdm_aggregate_serialized_length(msg->aggregate)
			+ sdm_set_serialized_length(msg->src)
			+ sdm_set_serialized_length(msg->dest)
			+ msg->payload_len;

		p = buf = (char *)malloc(maxlen);

//		printf("message maxlen: %d\n", maxlen);

		// Saves the address of the header
		header_addr = p;

		p += (int)sizeof(maxlen); // Points to the body of the message

		/*
		 * Note: maxlen was the maximum length of the serialized buffer, we
		 * now calculate the actual length for the send.
		 */
		len = sizeof(len);
		len += HEX_LEN;
		int_to_hex_str(msg->id, p, &p);
		len += sdm_aggregate_serialize(msg->aggregate, p, &p);
		len += sdm_set_serialize(msg->src, p, &p);
		len += sdm_set_serialize(msg->dest, p, &p);
		memcpy(p, msg->payload, msg->payload_len);
		len += msg->payload_len;

		// Copies the actual length to the header of the message
		memcpy(header_addr, (void *)&len, sizeof(len));

//		printf("Created the message to send\n");

		/*
		 * Send the message to each destination, converting the
		 * node ID to a proper address before the transmission
		 */

		for (dest_id = sdm_set_first(route); !sdm_set_done(route); dest_id = sdm_set_next(route)) {
			int sockd;

			DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] Sending len %d to %d\n", sdm_route_get_id(), len, dest_id);

			// Get socket descriptor corresponding to the node where the message will be sent
			sockd = sdm_fetch_sockd(dest_id);
			if(sockd < 0) {
				DEBUG_PRINTS(DEBUG_LEVEL_CLIENT, "Socket descriptor not found!\n");
				return -1;
			}

			p = buf;

			// Write all message to the socket
			if(sdm_tcpip_send(sockd, buf, len) < 0) {
				DEBUG_PRINTS(DEBUG_LEVEL_CLIENT, "Error sending message!\n");
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
 * Return one socket description related to a socket that is
 * ready to read.
 *
 * @return the socket descriptor, -2 if no socket available for reading, -1 on failure
 *
 * Get the size of the message in bytes
 * @return 0 if successful, -1 if socket closed or error
 *
 * Get a message from a socket. Since message length comes
 * before the message, the sdm_get_count function must
 * be executed first
 *
 * @return 0 if successful, -1 if error
 *
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

	if(sockfd == -1) {
		DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] Error retrieving socket descriptor\n", sdm_route_get_id());
		return -1;
	}

	if (sockfd >= 0) {
		err = sdm_tcpip_msgheader_receive(sockfd, &len);

//		printf("Message header: %d\n", len);

		if(err != 0) {
			DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] Error retrieving message size!\n", sdm_route_get_id());
			return -1;
		}

		buf = (char *)malloc(len);

		sdm_message msg = sdm_message_new(buf, len);

		if(sdm_tcpip_msgbody_receive(sockfd, buf, len) < 0) {
			DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] Error retrieving message!\n", sdm_route_get_id());
			return -1;
		}

//		printf("Init of message handling\n");
		DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] sdm_message_progress received len %d from %d\n",
				sdm_route_get_id(), len, sdm_fetch_nodeid(sockfd));

		msg->id = hex_str_to_int(buf, &buf);
		len -= HEX_LEN;

		if ((n = sdm_aggregate_deserialize(msg->aggregate, buf, &buf)) < 0) {
			DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] invalid header\n", sdm_route_get_id());
			return -1;
		}
		len -= n;

		if ((n = sdm_set_deserialize(msg->src, buf, &buf)) < 0) {
			DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] invalid header\n", sdm_route_get_id());
			return -1;
		}
		len -= n;

		if ((n = sdm_set_deserialize(msg->dest, buf, &buf)) < 0) {
			DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] invalid header\n", sdm_route_get_id());
			return -1;
		}
		len -= n;

		msg->payload = buf;
		msg->payload_len = len;

		DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] sdm_message_progress agg=%s src=%s dest=%s\n",
				sdm_route_get_id(),
				_aggregate_to_str(msg->aggregate),
				_set_to_str(msg->src),
				_set_to_str(msg->dest));

//		printf("End of message handling\n");

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
		DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] sdm_message_deliver \n", sdm_route_get_id());
		deliver_callback(msg);
	}
}
