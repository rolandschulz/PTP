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
 
#ifndef _PROXY_H_
#define _PROXY_H_

#include <stdarg.h>
#include <list.h>

#include "proxy_msg.h"
#include "proxy_cmd.h"
#include "proxy_event.h"
#include "proxy_attr.h"

#define PTP_PROXY_RES_OK			0
#define PTP_PROXY_RES_ERR			-1

#define PTP_PROXY_ERR_CLIENT		0
#define PTP_PROXY_ERR_SERVER		1
#define PTP_PROXY_ERR_PROTO			2
#define PTP_PROXY_ERR_SYSTEM		3

#define PTP_PROXY_EVENT_HANDLER		1
#define PTP_PROXY_CMD_HANDLER		2

struct proxy;
struct proxy_clnt;
struct proxy_svr;

struct proxy_clnt_helper_funcs {
	void (*eventhandler)(void *, void *);
	void *eventdata;
};
typedef struct proxy_clnt_helper_funcs	proxy_clnt_helper_funcs;

struct proxy_clnt_funcs {
	/*
	 * Service functions
	 */
	int (*init)(struct proxy_clnt *, void **, char *, va_list);
	int (*connect)(struct proxy_clnt *);
	int (*create)(struct proxy_clnt *);
	int (*progress)(struct proxy_clnt *);
};
typedef struct proxy_clnt_funcs	proxy_clnt_funcs;

struct proxy_svr_helper_funcs {
	/*
	 * Service functions
	 */
	int (*newconn)(void);
	int (*numservers)(void);
};
typedef struct proxy_svr_helper_funcs	proxy_svr_helper_funcs;

/*
 * Functions for managing proxy server.
 */
struct proxy_svr_funcs {
	int (*init)(struct proxy_svr *, void **);
	int (*create)(struct proxy_svr *, int);
	int (*connect)(struct proxy_svr *, char *, int);
	int (*progress)(struct proxy_svr *);
	void (*finish)(struct proxy_svr *);
};
typedef struct proxy_svr_funcs	proxy_svr_funcs;

/*
 * A proxy client send commands to and receives events from a server.
 */
struct proxy_clnt {
	struct proxy *						proxy;
	struct proxy_clnt_helper_funcs *	clnt_helper_funcs;
	void *								clnt_data;
	struct timeval *					clnt_timeout;
	List *								clnt_events;
};
typedef struct proxy_clnt	proxy_clnt;

/*
 * A proxy server receives commands from and sends events to a client.
 */
struct proxy_svr {
	struct proxy *					proxy;
	struct proxy_svr_helper_funcs *	svr_helper_funcs;
	struct proxy_commands *			svr_commands;
	void *							svr_data;
	struct timeval *				svr_timeout;
	List *							svr_events;
};
typedef struct proxy_svr	proxy_svr;

/*
 * The proxy structure encapsulates both the client and server sides
 * of the communication protocol.
 */
struct proxy {
	char *							name;
	struct proxy_handler_funcs *	handler_funcs;
	struct proxy_clnt_funcs *		clnt_funcs;
	struct proxy_svr_funcs *		svr_funcs;
};
typedef struct proxy	proxy;

extern proxy 	proxies[];

extern int		find_proxy(char *, proxy **);
extern void		proxy_set_error(int, char *);
extern int		proxy_get_error(void);
extern char *	proxy_get_error_str(void);
extern void		proxy_event_callback(proxy *, char *);

extern int		proxy_svr_init(char *, struct timeval *, proxy_svr_helper_funcs *, proxy_commands *cmds, proxy_svr **);
extern int		proxy_svr_create(proxy_svr *, int);
extern int		proxy_svr_connect(proxy_svr *, char *, int);
extern int		proxy_svr_progress(proxy_svr *);
extern void		proxy_svr_finish(proxy_svr *);
extern int		proxy_svr_queue_msg(proxy_svr *, proxy_msg *);

extern int		proxy_clnt_init(char *, struct timeval *, proxy_clnt_helper_funcs *, proxy_clnt **, char *, va_list);
extern int 		proxy_clnt_connect(proxy_clnt *);
extern int 		proxy_clnt_create(proxy_clnt *);
extern int 		proxy_clnt_progress(proxy_clnt *);
extern int		proxy_clnt_queue_msg(proxy_clnt *, proxy_msg *);

#endif /* _PROXY_H_*/
