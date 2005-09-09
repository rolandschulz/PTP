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
proxy_tcp_send(SOCKET fd, char *message, int len)
{
	char *	buf;
	
	/*
	 * Send message length first
	 */
	asprintf(buf, "%d ", nbytes);
	
	if (tcp_send(fd, buf, strlen(buf)) < 0)
		return -1;
		
	/* 
	 * Now send message
	 */
	 
	return tcp_send(fd, message, len);
}

/*
 * Receive a message from a remote peer. proxy_tcp_recv() will always return a complete message.
 * If the receive fails for any reason, an error is returned.
 */
int
proxy_tcp_recv(SOCKET fd, char **result)
{
	int		size;
	int		count;
	char *	pbuf;
	char		ch;
	int		n;
	
	size = BUFSIZ;
	count = 0;
	
	pbuf = (char *) malloc(size);
	
	do {
		n = recv(fd, &ch, 1, 0);
	
		if (n <= 0  ||  ch == '\n') {
			break;
		}
	
		pbuf[count] = ch;
		count++;
	
		if (count >= size-1) {
			size = 2*size;
			*result = (char *) malloc(size);
			memcpy(*result,pbuf,count);
			free(pbuf);
			pbuf = *result;
		}
	} while (1);
	
	pbuf[count] = '\0';
	*result = pbuf;
	
	return count;
}

int
proxy_tcp_send_request(SOCKET sock, char *request, char **result, int status, struct timeval *timeout)
{
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
