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

#ifdef __gnu_linux__
#define _GNU_SOURCE
#endif

#include <sys/types.h>
#include <sys/socket.h>

#include <string.h>
#include <stdio.h>
#include <unistd.h>
#include <ctype.h>
#include <signal.h>
#include <stdlib.h>
#include <errno.h>

#include "compat.h"
#include "proxy.h"
#include "proxy_tcp.h"
#include "proxy_cmd.h"

struct timeval TCPTIMEOUT = { 25, 0 };

/**
 * Create a conn structure.
 */
void
proxy_tcp_create_conn(proxy_tcp_conn **conn)
{
	proxy_tcp_conn *c;

	c = (proxy_tcp_conn *) malloc(sizeof(proxy_tcp_conn));

	c->sess_sock = INVALID_SOCKET;
	c->svr_sock = INVALID_SOCKET;
	c->host = NULL;
	c->port = 0;
	c->connected = 0;
	c->buf_size = BUFSIZ;
	c->buf = (char *)malloc(c->buf_size);
	c->buf_pos = 0;
	c->total_read = 0;
	c->msg_len = 0;

	signal(SIGPIPE, SIG_IGN);

	*conn = c;
}

void
proxy_tcp_destroy_conn(proxy_tcp_conn *conn)
{
	if (conn->host != NULL)
		free(conn->host);
	free(conn->buf);
	free(conn);
}

static int
tcp_recv(proxy_tcp_conn *conn)
{
	int	n;

	if (conn->total_read == conn->buf_size) {
		conn->buf_size += BUFSIZ;
		conn->buf = (char *)realloc(conn->buf, conn->buf_size);
	}

	n = recv(conn->sess_sock, &conn->buf[conn->buf_pos], conn->buf_size - conn->total_read, 0);
	if (n <= 0) {
		if (n < 0)
			proxy_set_error(PTP_PROXY_ERR_SYSTEM, strerror(errno));
		else
			proxy_set_error(PTP_PROXY_ERR_PROTO, "connection terminated");

		CLOSE_SOCKET(conn->sess_sock);
		conn->connected = 0;
		return -1;
	}

	conn->buf_pos += n;
	conn->total_read += n;

	return n;
}

static int
tcp_send(proxy_tcp_conn *conn, char *buf, int len)
{
	int		n;

	while ( len > 0 ) {
		n = send(conn->sess_sock, buf, len, 0);
		if (n <= 0) {
			if (n < 0)
				perror("send");
			CLOSE_SOCKET(conn->sess_sock);
			conn->connected = 0;
			return -1;
		}

		len -= n;
		buf += n;
	}

	return 0;
}

/*
 * Send a message to a remote peer. proxy_tcp_send() will always send a complete message.
 * If the send fails for any reason, an error is returned.
 *
 * Silently truncates length to a maximum of 32 bits.
 */
int
proxy_tcp_send_msg(proxy_tcp_conn *conn, char *message, int len)
{
	char 	buf[PTP_MSG_LEN_SIZE+1];

	/*
	 * Send message length first
	 */
	sprintf(buf, "%0*x", PTP_MSG_LEN_SIZE, len & PTP_MSG_LENGTH_MASK);

	if (tcp_send(conn, buf, strlen(buf)) < 0) {
		return -1;
	}

	/*
	 * Now send message
	 */

	return tcp_send(conn, message, len);
}

static int
proxy_tcp_get_msg_len(proxy_tcp_conn *conn)
{
	char *	end;

	/*
	 * If we haven't read enough then return for more...
	 */
	if (conn->total_read < PTP_MSG_LEN_SIZE)
		return 0;

	conn->msg_len = strtol(conn->buf, &end, 16);

	/*
	 * check if we've received the length
	 */
	if (conn->msg_len == 0 || (conn->msg_len > 0 && *end != ' ')) {
		proxy_set_error(PTP_PROXY_ERR_PROTO, "could not understand message");
		return -1;
	}

	return conn->msg_len;
}

static int
proxy_tcp_copy_msg(proxy_tcp_conn *conn, char **result)
{
	int	n = conn->msg_len;

	*result = (char *)malloc(conn->msg_len + 1);
	memcpy(*result, &conn->buf[PTP_MSG_LEN_SIZE], conn->msg_len);
	(*result)[conn->msg_len] = '\0';

	/*
	 * Move rest of buffer down if necessary
	 */
	if (conn->total_read > conn->msg_len + PTP_MSG_LEN_SIZE) {
		conn->total_read -= conn->msg_len + PTP_MSG_LEN_SIZE;
		memmove(conn->buf, &conn->buf[conn->msg_len + PTP_MSG_LEN_SIZE], conn->total_read);
	} else {
		conn->buf_pos = 0;
		conn->total_read = 0;
	}

	conn->msg_len = 0;

	return n;
}

static int
proxy_tcp_get_msg_body(proxy_tcp_conn *conn, char **result)
{
	/*
	 * If we haven't read enough then return for more...
	 */
	if (conn->total_read - PTP_MSG_LEN_SIZE < conn->msg_len)
		return 0;

	return proxy_tcp_copy_msg(conn, result);
}

/*
 * Receive a buffer from a remote peer. It is possible that this buffer may contain a partial
 * message or a number of completed messages.
 *
 */
int
proxy_tcp_recv_msgs(proxy_tcp_conn *conn)
{
	/*
	 * Get whatever is available
	 */
	if (tcp_recv(conn) < 0)
		return -1;

	return 0;
}

/*
 * Get the first available message from the buffer. Repeated calls will get subsequent
 * messages from the buffer.
 *
 * @return	0	no messages available
 * 			>0	message available, length returned
 * 			-1	error
 */
int
proxy_tcp_get_msg(proxy_tcp_conn *conn, char **result, int *len)
{
	int	n;

	if (conn->msg_len == 0 && (n = proxy_tcp_get_msg_len(conn)) <= 0)
		return n;

	*len = conn->msg_len;
	return proxy_tcp_get_msg_body(conn, result);
}

/*
 * Decode string argument
 */
int
proxy_tcp_decode_string(char *str, char **arg, char **end)
{
	int		arg_len;
	int 	str_len = strlen(str);
	char *	ep;
	char *	p;

	if (str_len < PTP_MSG_ARG_LEN_SIZE + 1) {
		return -1;
	}

	ep = str + PTP_MSG_ARG_LEN_SIZE;
	*ep++ = '\0';
	arg_len = strtol(str, NULL, 16);

	if (str_len < PTP_MSG_ARG_LEN_SIZE + arg_len + 1) {
		return -1;
	}

	p = (char *)malloc(arg_len + 1);
	memcpy(p, ep, arg_len);
	p[arg_len] = '\0';

	*arg = p;
	*end = str + PTP_MSG_ARG_LEN_SIZE + arg_len + 1;

	return 0;
}
