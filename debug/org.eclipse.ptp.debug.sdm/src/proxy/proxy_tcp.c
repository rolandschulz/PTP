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

#include <sys/socket.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <string.h>
#include <stdio.h>
#include <unistd.h>
#include <ctype.h>

#include "compat.h"
#include "session.h"
#include "proxy.h"
#include "proxy_tcp.h"

struct timeval TCPTIMEOUT = { 25, 0 };

int
proxy_tcp_client_connect(char *host, int port, proxy_tcp_conn **cp)
{
	SOCKET                  sd;
	struct hostent *        hp;
	long int                haddr;
	struct sockaddr_in      scket;
	        
	*cp = (proxy_tcp_conn *) malloc(sizeof(proxy_tcp_conn));
	
	hp = gethostbyname(host);
	        
	if (hp == (struct hostent *)NULL) {
		fprintf(stderr, "could not find host \"%s\"\n", host);
		return -1;
	}
	
	haddr = ((hp->h_addr[0] & 0xff) << 24) |
			((hp->h_addr[1] & 0xff) << 16) |
			((hp->h_addr[2] & 0xff) <<  8) |
			((hp->h_addr[3] & 0xff) <<  0);
	
	if ( (sd = socket(PF_INET, SOCK_STREAM, 0)) == INVALID_SOCKET )
	{
		perror("socket");
		return -1;
	}
	
	memset (&scket,0,sizeof(scket));
	scket.sin_family = PF_INET;
	scket.sin_port = htons((u_short) port);
	scket.sin_addr.s_addr = htonl(haddr);
	
	if ( connect(sd, (struct sockaddr *) &scket, sizeof(scket)) == SOCKET_ERROR )
	{
		perror("connect");
		CLOSE_SOCKET(sd);
		return -1;
	}
	
	(*cp)->sock = sd;
	(*cp)->host = strdup(host);
	(*cp)->port = port;
			
	(*cp)->buf_size = BUFSIZ;
	(*cp)->buf = (char *)malloc((*cp)->buf_size);
	(*cp)->buf_pos = 0;
	(*cp)->total_read = 0;
	
	return 0;
}

int
proxy_tcp_result_to_event(char *result, dbg_event **ev)
{
	dbg_event *e;
	
	e = (dbg_event *)malloc(sizeof(dbg_event));
	*ev = e;
	return 0;
}


static int
tcp_send(SOCKET fd, char *buf, int len)
{
	int		n;

	while ( len > 0 ) {
		n = send(fd, buf, len, 0);
		if (n <= 0) {
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
 */
int
proxy_tcp_send(proxy_tcp_conn *conn, char *message, int len)
{
	char *	buf;
	
	/*
	 * Send message length first
	 */
	asprintf(&buf, "%d ", len);
	
	if (tcp_send(conn->sock, buf, strlen(buf)) < 0) {
		free(buf);
		return -1;
	}
	
	free(buf);
		
	/* 
	 * Now send message
	 */
	 
	return tcp_send(conn->sock, message, len);
}

/**
 * Receive a buffer from a remote peer and assemble the buffer into a message. proxy_tcp_recv() may
 * need to be called repeatedly to assemble the message. Once the message is available, it is
 * returned to the caller.
 * 
 * @return 
 * 	-1:	error
 * 	 0: read complete, no result available yet
 * 	>0: result available, length returned
 */
int
proxy_tcp_recv(proxy_tcp_conn *conn, char **result)
{
	char *	end;
	int		n;
	
	if (conn->total_read == conn->buf_size) {
		conn->buf_size += BUFSIZ;
		conn->buf = (char *)realloc(conn->buf, conn->buf_size);
	}
	
	n = recv(conn->sock, &conn->buf[conn->buf_pos], conn->buf_size - conn->total_read, 0);
	
	if (n < 0) {
		return -1;
	}
	
	/*
	 * Check for length
	 */
	if (conn->msg_len == 0) {
		conn->msg_len = strtol(conn->buf, &end, 10);
		
		/*
		 * check if we've received the length
		 */
		if (conn->msg_len > 0) {
			/*
			 * We've received something
			 */
			if (*end != ' ') {
				/*
				 * Not a length though
				 */
				conn->msg_len = 0;
				conn->buf_pos += n;
				conn->total_read += n;
				return 0;
			}
			
			conn->msg = end + 1;
		}
	}
	
	/*
	 * Ok, we have the length. Now make sure that we have either
	 * the entire buffer, or need to read more..
	 */
	 
	if (n - (conn->msg - conn->buf + 1) >= conn->msg_len) {
		*result = (char *)malloc(conn->msg_len + 1);
		memcpy(*result, conn->msg, conn->msg_len);
		(*result)[conn->msg_len] = '\0';
		conn->total_read = 0;
		conn->buf_pos = 0;
		return conn->msg_len;
	}
	
	/*
	 * Need more...
	 */
	conn->buf_pos += n;
	conn->total_read += n;
	
	return 0;
}

void
skipwhitespace(char **s)
{
	if (s == NULL || *s == NULL)
		return;
	
	while (isspace((int)**s))
		*s++;
}

char *
getword(char **s)
{
	char *	wp;
	
	skipwhitespace(s);
	
	wp = malloc(strlen(*s)+1);
	
	if (**s != '"'  ||  *(*s+1) != '"') {
		while (**s != '\0'  &&  !isspace((int)**s)) {
			*wp = **s;
			wp++;
			*s++;
		}
	} else {
		*s += 2;
		while (**s != '\0'  &&  (**s != '"'  ||  *(*s+1) != '"')) {
			*wp = **s;
			wp++;
			*s++;
		}
		if ( **s == '"'  &&  *(*s+1) == '"' ) {
			*s += 2;
		}
	}
	
	*wp = '\0';
	
	return wp;
}
