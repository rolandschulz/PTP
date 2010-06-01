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
#include "proxy_stdio.h"
#include "handler.h"

static int	proxy_stdio_svr_init(proxy_svr *, void **);
static int	proxy_stdio_svr_create(proxy_svr *, int);
static int	proxy_stdio_svr_connect(proxy_svr *, char *, int);
static int  proxy_stdio_svr_progress(proxy_svr *);
static void	proxy_stdio_svr_process_cmds(void);
static void	proxy_stdio_svr_finish(proxy_svr *);

static int	proxy_stdio_svr_recv_msgs(int, void *);
static int	proxy_stdio_svr_dispatch(proxy_svr *, char *, int);

proxy_svr_funcs proxy_stdio_svr_funcs =
{
	proxy_stdio_svr_init,
	proxy_stdio_svr_create,
	proxy_stdio_svr_connect,
	proxy_stdio_svr_progress,
	proxy_stdio_svr_finish,
};

/*
 * Called for each event that is placed on the event queue.
 * Sends the event to the proxy peer.
 */
static void
proxy_stdio_svr_event_callback(void *ev_data, void *data)
{
	int					len;
	proxy_svr *			svr = (proxy_svr *)ev_data;
	proxy_stdio_conn *	conn = (proxy_stdio_conn *)svr->svr_data;
	proxy_msg *			msg = (proxy_msg *)data;
	char *				str;

	if (proxy_serialize_msg(msg, &str, &len) < 0) {
		/*
		 * TODO should send an error back to proxy peer
		 */
		fprintf(stderr, "proxy_stdio_svr_event_callback: event conversion failed\n");
		return;
	}

	(void)proxy_stdio_send_msg(conn, str, len);
	free(str);
}

/*
 * Called to process any commands in the read buffer.
 */
static void
proxy_stdio_svr_cmd_callback(void *cmd_data, void *data)
{
	int						len;
	char *					msg = NULL;
	proxy_svr *				svr = (proxy_svr *)cmd_data;
	proxy_stdio_conn *		conn = (proxy_stdio_conn *)svr->svr_data;

	if (proxy_stdio_get_msg(conn, &msg, &len) > 0) {
		proxy_stdio_svr_dispatch(svr, msg, len);
		if (msg != NULL) free(msg);
	}
}

static int
proxy_stdio_svr_init(proxy_svr *svr, void **data)
{
	proxy_stdio_conn		*conn;

	proxy_stdio_create_conn(&conn);
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
proxy_stdio_svr_create(proxy_svr *svr, int port)
{
	return PTP_PROXY_RES_OK;
}

/**
 * Connect to a remote proxy.
 */
static int
proxy_stdio_svr_connect(proxy_svr *svr, char *host, int port)
{
	proxy_stdio_conn *		conn = (proxy_stdio_conn *)svr->svr_data;

	conn->sess_in = 0;
	conn->sess_out = 1;

	RegisterFileHandler(conn->sess_in, READ_FILE_HANDLER, proxy_stdio_svr_recv_msgs, (void *)svr);
	RegisterEventHandler(PTP_PROXY_EVENT_HANDLER, proxy_stdio_svr_event_callback, (void *)svr);
	RegisterEventHandler(PTP_PROXY_CMD_HANDLER, proxy_stdio_svr_cmd_callback, (void *)svr);

	return PTP_PROXY_RES_OK;
}

/**
 * Cleanup prior to server exit.
 */
static void
proxy_stdio_svr_finish(proxy_svr *svr)
{
	proxy_stdio_conn *	conn = (proxy_stdio_conn *)svr->svr_data;

	if (conn->sess_in != INVALID_SOCKET) {
		UnregisterFileHandler(conn->sess_in);
	}

	if (conn->sess_out != INVALID_SOCKET) {
		UnregisterFileHandler(conn->sess_out);
	}

	proxy_stdio_destroy_conn(conn);
}

/**
 * Check for incoming messages.
 */
static void
proxy_stdio_svr_process_cmds()
{
	CallEventHandlers(PTP_PROXY_CMD_HANDLER, NULL);
}

static void
proxy_stdio_svr_process_events(proxy_msg *msg, void *data)
{
	CallEventHandlers(PTP_PROXY_EVENT_HANDLER, (void *)msg);
}

/**
 * Processes any queued events Also checks for ready file descriptors
 * and calls appropriate handlers.
 */
static int
proxy_stdio_svr_progress(proxy_svr *svr)
{
	fd_set					rfds;
	fd_set					wfds;
	fd_set					efds;
	int						res;
	int						nfds = 0;
	struct timeval			tv;
	struct timeval *		timeout = &tv;

	proxy_process_msgs(svr->svr_events, proxy_stdio_svr_process_events, NULL);

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

	proxy_stdio_svr_process_cmds();

	return 0;
}

/*
 * Dispatch a command to the server
 *
 * proxy_stdio_svr_dispatch() should never fail. If we get a read error from the client then we just
 * assume the client has gone away. Errors from server commands are just reported back to the
 * client.
 */
static int
proxy_stdio_svr_dispatch(proxy_svr *svr, char *msg, int len)
{
	int					idx;
	char *				err_str;
	proxy_commands * 	cmd_tab = svr->svr_commands;
	proxy_msg *			m;
	proxy_cmd			cmd;
	int			rc;

	DEBUG_PRINT("SVR received <%s>\n", msg);

	rc = 0;
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

	return rc;
}

static int
proxy_stdio_svr_recv_msgs(int fd, void *data)
{
	proxy_svr *			svr = (proxy_svr *)data;
	proxy_stdio_conn *	conn = (proxy_stdio_conn *)svr->svr_data;

	return proxy_stdio_recv_msgs(conn);
}
