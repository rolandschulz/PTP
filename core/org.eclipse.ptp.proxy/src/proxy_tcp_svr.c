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
 * The proxy handles communication between the client debug library API and the
 * client debugger, since they may be running on different hosts, and will
 * certainly be running in different processes.
 */
#include <config.h>

#include <sys/socket.h>
#include <arpa/inet.h>
#include <netdb.h>

#include <stdio.h>
#include <errno.h>
#include <string.h>
#include <unistd.h>
#include <stdlib.h>

#include "compat.h"
#include "args.h"
#include "proxy.h"
#include "proxy_event.h"
#include "proxy_cmd.h"
#include "proxy_tcp.h"
#include "handler.h"

static int	proxy_tcp_svr_init(proxy_svr *, void **);
static int	proxy_tcp_svr_create(proxy_svr *, int);
static int	proxy_tcp_svr_connect(proxy_svr *, char *, int);
static int  proxy_tcp_svr_progress(proxy_svr *);
static void	proxy_tcp_svr_process_cmds(void);
static void	proxy_tcp_svr_finish(proxy_svr *);

static int	proxy_tcp_svr_recv_msgs(int, void *);
static int	proxy_tcp_svr_accept(int, void *);
static int	proxy_tcp_svr_dispatch(proxy_svr *, char *, int);

proxy_svr_funcs proxy_tcp_svr_funcs =
{
	proxy_tcp_svr_init,
	proxy_tcp_svr_create,
	proxy_tcp_svr_connect,
	proxy_tcp_svr_progress,
	proxy_tcp_svr_finish,
};

/*
 * Called for each event that is placed on the event queue.
 * Sends the event to the proxy peer.
 */
static void
proxy_tcp_svr_event_callback(void *ev_data, void *data)
{
	int					len;
	proxy_svr *			svr = (proxy_svr *)ev_data;
	proxy_tcp_conn *	conn = (proxy_tcp_conn *)svr->svr_data;
	proxy_msg *			msg = (proxy_msg *)data;
	char *				str;

	if (proxy_serialize_msg(msg, &str, &len) < 0) {
		/*
		 * TODO should send an error back to proxy peer
		 */
		fprintf(stderr, "proxy_tcp_svr_event_callback: event conversion failed\n");
		return;
	}

	(void)proxy_tcp_send_msg(conn, str, len);
	free(str);
}

/*
 * Called to process any commands in the read buffer.
 */
static void
proxy_tcp_svr_cmd_callback(void *cmd_data, void *data)
{
	int						len;
	char *					msg = NULL;
	proxy_svr *				svr = (proxy_svr *)cmd_data;
	proxy_tcp_conn *		conn = (proxy_tcp_conn *)svr->svr_data;

	if (proxy_tcp_get_msg(conn, &msg, &len) > 0) {
		proxy_tcp_svr_dispatch(svr, msg, len);
		if (msg != NULL) free(msg);
	}
}

static int
proxy_tcp_svr_init(proxy_svr *svr, void **data)
{
	proxy_tcp_conn		*conn;

	proxy_tcp_create_conn(&conn);
	conn->svr = svr;
	*data = (void *)conn;

	return PTP_PROXY_RES_OK;
}

/**
 * Create server socket and bind address to it.
 *
 * @return conn structure containing server socket and port
 */
static int
proxy_tcp_svr_create(proxy_svr *svr, int port)
{
	socklen_t				slen;
	SOCKET					sd;
	struct sockaddr_in		sname;
	proxy_tcp_conn *		conn = (proxy_tcp_conn *)svr->svr_data;

	if ( (sd = socket(PF_INET, SOCK_STREAM, 0)) == INVALID_SOCKET )
	{
		proxy_set_error(PTP_PROXY_ERR_SYSTEM, strerror(errno));
		return PTP_PROXY_RES_ERR;
	}

	memset (&sname, 0, sizeof(sname));
	sname.sin_family = PF_INET;
	sname.sin_port = htons(port);
	sname.sin_addr.s_addr = htonl(INADDR_ANY);

	if (bind(sd,(struct sockaddr *) &sname, sizeof(sname)) == SOCKET_ERROR )
	{
		proxy_set_error(PTP_PROXY_ERR_SYSTEM, strerror(errno));
		CLOSE_SOCKET(sd);
		return PTP_PROXY_RES_ERR;
	}

	slen = sizeof(sname);

	if ( getsockname(sd, (struct sockaddr *)&sname, &slen) == SOCKET_ERROR )
	{
		proxy_set_error(PTP_PROXY_ERR_SYSTEM, strerror(errno));
		CLOSE_SOCKET(sd);
		return PTP_PROXY_RES_ERR;
	}

	if ( listen(sd, 5) == SOCKET_ERROR )
	{
		proxy_set_error(PTP_PROXY_ERR_SYSTEM, strerror(errno));
		CLOSE_SOCKET(sd);
		return PTP_PROXY_RES_ERR;
	}

	conn->svr_sock = sd;
	conn->port = (int) ntohs(sname.sin_port);

	RegisterFileHandler(sd, READ_FILE_HANDLER, proxy_tcp_svr_accept, (void *)svr);
	RegisterEventHandler(PTP_PROXY_EVENT_HANDLER, proxy_tcp_svr_event_callback, (void *)svr);
	RegisterEventHandler(PTP_PROXY_CMD_HANDLER, proxy_tcp_svr_cmd_callback, (void *)svr);

	return PTP_PROXY_RES_OK;
}

/**
 * Connect to a remote proxy.
 */
static int
proxy_tcp_svr_connect(proxy_svr *svr, char *host, int port)
{
	SOCKET					sd;
	struct hostent *		hp;
	struct sockaddr_in		scket;
	proxy_tcp_conn *		conn = (proxy_tcp_conn *)svr->svr_data;

	if (host == NULL) {
		proxy_set_error(PTP_PROXY_ERR_SERVER, "no host specified");
		return PTP_PROXY_RES_ERR;
	}

	hp = gethostbyname(host);

	if (hp == (struct hostent *)NULL) {
		proxy_set_error(PTP_PROXY_ERR_SERVER, "could not find host");
		return PTP_PROXY_RES_ERR;
	}

	if ( (sd = socket(PF_INET, SOCK_STREAM, 0)) == INVALID_SOCKET )
	{
		proxy_set_error(PTP_PROXY_ERR_SYSTEM, strerror(errno));
		return PTP_PROXY_RES_ERR;
	}

	memset (&scket,0,sizeof(scket));
	scket.sin_family = PF_INET;
	scket.sin_port = htons((u_short) port);
	memcpy(&(scket.sin_addr), *(hp->h_addr_list), sizeof(struct in_addr));

	if ( connect(sd, (struct sockaddr *) &scket, sizeof(scket)) == SOCKET_ERROR )
	{
		proxy_set_error(PTP_PROXY_ERR_SYSTEM, strerror(errno));
		CLOSE_SOCKET(sd);
		return PTP_PROXY_RES_ERR;
	}

	conn->sess_sock = sd;
	conn->host = strdup(host);
	conn->port = port;

	RegisterEventHandler(PTP_PROXY_EVENT_HANDLER, proxy_tcp_svr_event_callback, (void *)svr);
	RegisterEventHandler(PTP_PROXY_CMD_HANDLER, proxy_tcp_svr_cmd_callback, (void *)svr);
	RegisterFileHandler(sd, READ_FILE_HANDLER, proxy_tcp_svr_recv_msgs, (void *)svr);

	return PTP_PROXY_RES_OK;
}

/**
 * Accept a new proxy connection. Register dispatch routine.
 */
static int
proxy_tcp_svr_accept(int fd, void *data)
{
	socklen_t				fromlen;
	SOCKET					ns;
	struct sockaddr			addr;
	proxy_svr *				svr = (proxy_svr *)data;
	proxy_tcp_conn *		conn = (proxy_tcp_conn *)svr->svr_data;

	fromlen = sizeof(addr);
	ns = accept(fd, &addr, &fromlen);
	if (ns < 0) {
		proxy_set_error(PTP_PROXY_ERR_SYSTEM, strerror(errno));
		return PTP_PROXY_RES_ERR;
	}

	/*
	 * Only allow one connection at a time.
	 */
	if (conn->connected) {
		CLOSE_SOCKET(ns); // reject
		return PTP_PROXY_RES_OK;
	}

	if (conn->svr->svr_helper_funcs->newconn != NULL && conn->svr->svr_helper_funcs->newconn() < 0) {
		CLOSE_SOCKET(ns); // reject
		return PTP_PROXY_RES_OK;
	}

	conn->sess_sock = ns;
	conn->connected++;

	RegisterFileHandler(ns, READ_FILE_HANDLER, proxy_tcp_svr_recv_msgs, (void *)svr);

	return PTP_PROXY_RES_OK;
}

/**
 * Cleanup prior to server exit.
 */
static void
proxy_tcp_svr_finish(proxy_svr *svr)
{
	proxy_tcp_conn *	conn = (proxy_tcp_conn *)svr->svr_data;

	if (conn->sess_sock != INVALID_SOCKET) {
		UnregisterFileHandler(conn->sess_sock);
		CLOSE_SOCKET(conn->sess_sock);
		conn->sess_sock = INVALID_SOCKET;
	}

	if (conn->svr_sock != INVALID_SOCKET) {
		UnregisterFileHandler(conn->svr_sock);
		CLOSE_SOCKET(conn->svr_sock);
		conn->svr_sock = INVALID_SOCKET;
	}

	proxy_tcp_destroy_conn(conn);
}

/**
 * Check for incoming messages.
 */
static void
proxy_tcp_svr_process_cmds()
{
	CallEventHandlers(PTP_PROXY_CMD_HANDLER, NULL);
}

static void
proxy_tcp_svr_process_events(proxy_msg *msg, void *data)
{
	CallEventHandlers(PTP_PROXY_EVENT_HANDLER, (void *)msg);
}

/**
 * Processes any queued events Also checks for ready file descriptors
 * and calls appropriate handlers.
 */
static int
proxy_tcp_svr_progress(proxy_svr *svr)
{
	fd_set					rfds;
	fd_set					wfds;
	fd_set					efds;
	int						res;
	int						nfds = 0;
	struct timeval			tv;
	struct timeval *		timeout = &tv;

	proxy_process_msgs(svr->svr_events, proxy_tcp_svr_process_events, NULL);

	/* Set up fd sets */
	GenerateFDSets(&nfds, &rfds, &wfds, &efds);

	if (svr->svr_timeout == NULL) {
		timeout = NULL;
	} else {
		memcpy((char *)timeout, (char *)svr->svr_timeout, sizeof(struct timeval));
	}

	for ( ;; ) {
		res = select(nfds+1, &rfds, &wfds, &efds, &tv);

		switch (res) {
		case INVALID_SOCKET:
			if ( errno == EINTR )
				continue;

			perror("socket");
			return PTP_PROXY_RES_ERR;

		case 0:
			/* Timeout. */
			break;

		default:
			if (CallFileHandlers(&rfds, &wfds, &efds) < 0)
				return PTP_PROXY_RES_ERR;
		}

		break;
	}

	proxy_tcp_svr_process_cmds();

	return 0;
}

/*
 * Dispatch a command to the server
 *
 * proxy_tcp_svr_dispatch() should never fail. If we get a read error from the client then we just
 * assume the client has gone away. Errors from server commands are just reported back to the
 * client.
 */
static int
proxy_tcp_svr_dispatch(proxy_svr *svr, char *msg, int len)
{
	int					idx;
	char *				err_str;
	proxy_commands * 	cmd_tab = svr->svr_commands;
	proxy_msg *			m;
	proxy_cmd			cmd;
	int			rc;

	DEBUG_PRINT("proxy_tcp_svr_dispatch: received <%s>\n", msg);

	if (proxy_deserialize_msg(msg, len, &m) < 0) {
		proxy_msg *err = new_proxy_msg(0, PTP_PROXY_EV_MESSAGE);
		proxy_msg_add_int(err, 3); /* 3 attributes */
		proxy_msg_add_keyval_string(err, PTP_MSG_LEVEL_ATTR, PTP_MSG_LEVEL_FATAL);
		proxy_msg_add_keyval_int(err, PTP_MSG_CODE_ATTR, PTP_PROXY_ERR_PROTO);
		proxy_msg_add_int(err, PTP_ERROR_MALFORMED_COMMAND);
		asprintf(&err_str, "malformed command, len is %d", len);
		proxy_msg_add_keyval_string(err, PTP_MSG_TEXT_ATTR, err_str);
		free(err_str);
		return proxy_queue_msg(svr->svr_events, err);
	}

    idx = m->msg_id - cmd_tab->cmd_base;

	DEBUG_PRINT("proxy_tcp_svr_dispatch: about to dispatch idx=%d\n", idx);

	if (idx >= 0 && idx < cmd_tab->cmd_size) {
		cmd = cmd_tab->cmd_funcs[idx];
		if (cmd != NULL) {
			(void)cmd(m->trans_id, m->num_args, m->args);
		}
	} else {
		proxy_msg *err = new_proxy_msg(0, PTP_PROXY_EV_MESSAGE);
		proxy_msg_add_int(err, 3); /* 3 attributes */
		proxy_msg_add_keyval_string(err, PTP_MSG_LEVEL_ATTR, PTP_MSG_LEVEL_FATAL);
		proxy_msg_add_keyval_int(err, PTP_MSG_CODE_ATTR, PTP_PROXY_ERR_PROTO);
		proxy_msg_add_int(err, PTP_ERROR_MALFORMED_COMMAND);
		asprintf(&err_str, "malformed command, len is %d", len);
		proxy_msg_add_keyval_string(err, PTP_MSG_TEXT_ATTR, err_str);
		free(err_str);
		rc = proxy_queue_msg(svr->svr_events, err);
	}

	free_proxy_msg(m);

	DEBUG_PRINT("%s\n", "Leaving proxy_tcp_svr_dispatch");

	return rc;
}

static int
proxy_tcp_svr_recv_msgs(int fd, void *data)
{
	proxy_svr *			svr = (proxy_svr *)data;
	proxy_tcp_conn *	conn = (proxy_tcp_conn *)svr->svr_data;

	return proxy_tcp_recv_msgs(conn);
}
