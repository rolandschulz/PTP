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
 
#ifndef _PROXY_STDIO_H_
#define _PROXY_STDIO_H_

#include "compat.h"

#define PTP_MSG_LEN_SIZE		8
#define PTP_MSG_LENGTH_MASK		0xffffffff

struct proxy_stdio_conn {
	proxy_clnt *	clnt;
	proxy_svr *		svr;
	int				connected;
	int				sess_in; // File descriptor to read from
	int				sess_out; // File descriptor to write to
	char *			buf;
	char			msg_len_buf[11];
	int				buf_size;
	int				buf_pos;
	int				total_read;
	char *			msg;
	int				msg_len;
	void			(*event_handler)(void *, void *);
	void *			event_data;
};
typedef struct proxy_stdio_conn	proxy_stdio_conn;

extern proxy_clnt_funcs proxy_stdio_clnt_funcs;
extern proxy_svr_funcs proxy_stdio_svr_funcs;

extern void		proxy_stdio_create_conn(proxy_stdio_conn **);
extern void		proxy_stdio_destroy_conn(proxy_stdio_conn *);
extern int		proxy_stdio_recv_msgs(proxy_stdio_conn *);
extern int		proxy_stdio_get_msg(proxy_stdio_conn *, char **, int *len);
extern int		proxy_stdio_send_msg(proxy_stdio_conn *, char *, int);
extern int		proxy_stdio_decode_string(char *, char **, char **);
extern void		skipspace(char *);
extern char *	getword(char **);

#endif /* _PROXY_STDIO_H_*/
