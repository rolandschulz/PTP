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

#ifdef __gnu_linux__
#define _GNU_SOURCE
#endif /* __gnu_linux__ */

#include <sys/types.h>
#include <sys/socket.h>
#include <sys/select.h>
#include <arpa/inet.h>
#include <netdb.h>

#include <string.h>
#include <errno.h>
#include <stdio.h>
#include <stdarg.h>
#include <unistd.h>
#include <stdlib.h>

#include "compat.h"
#include "proxy.h"
#include "proxy_event.h"
#include "proxy_tcp.h"
#include "handler.h"

struct timeval	SELECT_TIMEOUT = {0, 1000};

static int proxy_tcp_clnt_init(proxy_clnt *, void **, char *, va_list);
static int proxy_tcp_clnt_connect(proxy_clnt *);
static int proxy_tcp_clnt_create(proxy_clnt *);
static int proxy_tcp_clnt_accept(int, void *);
static int proxy_tcp_clnt_progress(proxy_clnt *);
static void	proxy_tcp_clnt_event_callback(void *, void *);
static void	proxy_tcp_clnt_cmd_callback(void *, void *);


proxy_clnt_funcs proxy_tcp_clnt_funcs =
{
	proxy_tcp_clnt_init,
	proxy_tcp_clnt_connect,
	proxy_tcp_clnt_create,
	proxy_tcp_clnt_progress,
};

static int
proxy_tcp_clnt_recv_msgs(int fd, void *data)
{
	proxy_clnt *				clnt = (proxy_clnt *)data;
	proxy_tcp_conn *			conn = (proxy_tcp_conn *)clnt->clnt_data;

	return proxy_tcp_recv_msgs(conn);
}

/*
 * CLIENT FUNCTIONS
 */
static int
proxy_tcp_clnt_init(proxy_clnt *pc, void **data, char *attr, va_list ap)
{
	int					port;
	char *				host = NULL;
	proxy_tcp_conn *		conn;

	while (attr != NULL) {
		if (strcmp(attr, "host") == 0)
			host = strdup(va_arg(ap, char *));
		else if (strcmp(attr, "port") == 0)
			port = va_arg(ap, int);

		attr = va_arg(ap, char *);
	}

	proxy_tcp_create_conn(&conn);

	conn->clnt = pc;
	if (host != NULL)
		conn->host = strdup(host);
	conn->port = port;

	*data = (void *)conn;

	return PTP_PROXY_RES_OK;
}

/**
 * Connect to a remote proxy.
 */
static int
proxy_tcp_clnt_connect(proxy_clnt *pc)
{
	SOCKET					sd;
	struct hostent *			hp;
	long int					haddr;
	struct sockaddr_in		scket;
	proxy_tcp_conn *			conn = (proxy_tcp_conn *)pc->clnt_data;

	if (conn->host == NULL) {
		proxy_set_error(PTP_PROXY_ERR_CLIENT, "no host specified");
		return PTP_PROXY_RES_ERR;
	}

	hp = gethostbyname(conn->host);

	if (hp == (struct hostent *)NULL) {
		proxy_set_error(PTP_PROXY_ERR_CLIENT, "could not find host");
		return PTP_PROXY_RES_ERR;
	}

	haddr = ((hp->h_addr[0] & 0xff) << 24) |
			((hp->h_addr[1] & 0xff) << 16) |
			((hp->h_addr[2] & 0xff) <<  8) |
			((hp->h_addr[3] & 0xff) <<  0);

	if ( (sd = socket(PF_INET, SOCK_STREAM, 0)) == INVALID_SOCKET )
	{
		proxy_set_error(PTP_PROXY_ERR_SYSTEM, strerror(errno));
		return PTP_PROXY_RES_ERR;
	}

	memset (&scket,0,sizeof(scket));
	scket.sin_family = PF_INET;
	scket.sin_port = htons((u_short) conn->port);
	scket.sin_addr.s_addr = htonl(haddr);

	if ( connect(sd, (struct sockaddr *) &scket, sizeof(scket)) == SOCKET_ERROR )
	{
		proxy_set_error(PTP_PROXY_ERR_SYSTEM, strerror(errno));
		CLOSE_SOCKET(sd);
		return PTP_PROXY_RES_ERR;
	}

	conn->sess_sock = sd;
	conn->connected++;

	RegisterEventHandler(PTP_PROXY_EVENT_HANDLER, proxy_tcp_clnt_event_callback, (void *)pc);
	RegisterEventHandler(PTP_PROXY_CMD_HANDLER, proxy_tcp_clnt_cmd_callback, (void *)pc);
	RegisterFileHandler(sd, READ_FILE_HANDLER, proxy_tcp_clnt_recv_msgs, (void *)pc);

	return PTP_PROXY_RES_OK;
}

static int
proxy_tcp_clnt_create(proxy_clnt *pc)
{
	socklen_t				slen;
	SOCKET					sd;
	struct sockaddr_in		sname;
	proxy_tcp_conn *		conn = (proxy_tcp_conn *)pc->clnt_data;

	if ( (sd = socket(PF_INET, SOCK_STREAM, 0)) == INVALID_SOCKET )
	{
		proxy_set_error(PTP_PROXY_ERR_SYSTEM, strerror(errno));
		return PTP_PROXY_RES_ERR;
	}

	memset (&sname, 0, sizeof(sname));
	sname.sin_family = PF_INET;
	sname.sin_port = htons(conn->port);
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

	RegisterFileHandler(sd, READ_FILE_HANDLER, proxy_tcp_clnt_accept, (void *)pc);
	RegisterEventHandler(PTP_PROXY_EVENT_HANDLER, proxy_tcp_clnt_event_callback, (void *)pc);
	RegisterEventHandler(PTP_PROXY_CMD_HANDLER, proxy_tcp_clnt_cmd_callback, (void *)pc);

	return PTP_PROXY_RES_OK;
}

static int
proxy_tcp_clnt_accept(int fd, void *data)
{
	SOCKET					ns;
	socklen_t				fromlen;
	struct sockaddr			addr;
	proxy_clnt *			pc = (proxy_clnt *)data;
	proxy_tcp_conn *		conn = (proxy_tcp_conn *)pc->clnt_data;

	fromlen = sizeof(addr);
	ns = accept(fd, &addr, &fromlen);
	if (ns < 0) {
		proxy_set_error(PTP_PROXY_ERR_SYSTEM, strerror(errno));
		return PTP_PROXY_RES_ERR;
	}

	/*
	 * Only allow one connection at a time.
	 */
	if (conn->sess_sock != INVALID_SOCKET) {
		CLOSE_SOCKET(ns); // reject
		return PTP_PROXY_RES_OK;
	}

	conn->sess_sock = ns;
	conn->connected++;

	RegisterFileHandler(ns, READ_FILE_HANDLER, proxy_tcp_clnt_recv_msgs, (void *)pc);

	if (pc->clnt_helper_funcs->eventhandler != NULL) {
		proxy_msg *m = new_proxy_msg(PTP_PROXY_EV_CONNECTED, 0); // TODO trans id should NOT be 0
		proxy_queue_msg(pc->clnt_events, m);
		pc->clnt_helper_funcs->eventhandler(m, pc->clnt_helper_funcs->eventdata);
		free_proxy_msg(m);
	}

	return PTP_PROXY_RES_OK;

}

static void
proxy_tcp_clnt_process_cmds()
{
	CallEventHandlers(PTP_PROXY_CMD_HANDLER, NULL);
}

static void
proxy_tcp_clnt_process_events(proxy_msg *msg, void *data)
{
	CallEventHandlers(PTP_PROXY_EVENT_HANDLER, (void *)msg);
}

static int
proxy_tcp_clnt_progress(proxy_clnt *clnt)
{
	fd_set					rfds;
	fd_set					wfds;
	fd_set					efds;
	int						res;
	int						nfds = 0;
	struct timeval			tv;
	struct timeval *		timeout = &tv;

	proxy_process_msgs(clnt->clnt_events, proxy_tcp_clnt_process_events, NULL);

	/* Set up fd sets */
	GenerateFDSets(&nfds, &rfds, &wfds, &efds);

	if (clnt->clnt_timeout == NULL) {
		timeout = NULL;
	} else {
		memcpy((char *)timeout, (char *)clnt->clnt_timeout, sizeof(struct timeval));
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

	proxy_tcp_clnt_process_cmds();

	return PTP_PROXY_RES_OK;
}

/*
 * Reads events from proxy peer and dispatches them.
 */
static void
proxy_tcp_clnt_event_callback(void *ev_data, void *data)
{
	int						len;
	char *					result;
	proxy_msg *				m;
	proxy_clnt *			clnt = (proxy_clnt *)ev_data;
	proxy_tcp_conn *		conn = (proxy_tcp_conn *)clnt->clnt_data;

	if (proxy_tcp_get_msg(conn, &result, &len) <= 0 ||
		clnt->clnt_helper_funcs->eventhandler == NULL)
		return;

	if (proxy_deserialize_msg(result, len, &m) < 0) {
		m = new_proxy_msg(PTP_PROXY_EV_MESSAGE, 0); // TODO trans id should NOT be 0
		proxy_msg_add_int(m, 3); /* 3 attributes */
		proxy_msg_add_keyval_string(m, PTP_MSG_LEVEL_ATTR, PTP_MSG_LEVEL_FATAL);
		proxy_msg_add_keyval_int(m, PTP_MSG_CODE_ATTR, PTP_PROXY_ERR_PROTO);
		proxy_msg_add_keyval_string(m, PTP_MSG_TEXT_ATTR, "Could not covert to event");
	}

	free(result);

	clnt->clnt_helper_funcs->eventhandler(m, clnt->clnt_helper_funcs->eventdata);
}

/*
 * Called to process any commands waiting to be sent. The command
 * is sent to the proxy peer.
 */
static void
proxy_tcp_clnt_cmd_callback(void *cmd_data, void *data)
{
	int						len;
	char *					str;
	proxy_clnt *			clnt = (proxy_clnt *)cmd_data;
	proxy_tcp_conn *		conn = (proxy_tcp_conn *)clnt->clnt_data;
	proxy_msg *				msg = (proxy_msg *)data;

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
