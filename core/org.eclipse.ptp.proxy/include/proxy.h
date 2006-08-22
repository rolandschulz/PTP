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

#define PROXY_RES_OK		0
#define PROXY_RES_ERR		-1

#define PROXY_ERR_CLIENT	0
#define PROXY_ERR_SERVER	1
#define PROXY_ERR_PROTO		2
#define PROXY_ERR_SYSTEM	3

struct proxy;
struct proxy_clnt;
struct proxy_svr;

struct proxy_handler_funcs {
	void (*regfile)(int, int, int (*)(int, void *), void *); 
	void (*unregfile)(int); 
	void (*regeventhandler)(int, void (*)(void *, void *), void *);
	void (*eventcallback)(int, void *);
};
typedef struct proxy_handler_funcs	proxy_handler_funcs;

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
	int (*sendcmd)(struct proxy_clnt *, char *, char *, va_list);
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

struct proxy_svr_commands {
	char *	cmd_name;
	int		(*cmd_func)(char **args);
};
typedef struct proxy_svr_commands	proxy_svr_commands;

struct proxy_svr_funcs {
	int (*init)(struct proxy_svr *, void **);
	int (*create)(struct proxy_svr *, int);
	int (*connect)(struct proxy_svr *, char *, int);
	int (*progress)(struct proxy_svr *);
	void (*finish)(struct proxy_svr *);
};
typedef struct proxy_svr_funcs	proxy_svr_funcs;

struct proxy_clnt {
	struct proxy *						proxy;
	struct proxy_clnt_helper_funcs *	clnt_helper_funcs;
	void *								clnt_data;
};
typedef struct proxy_clnt	proxy_clnt;

struct proxy_svr {
	struct proxy *					proxy;
	struct proxy_svr_helper_funcs *	svr_helper_funcs;
	struct proxy_svr_commands *		svr_commands;
	void *							svr_data;
};
typedef struct proxy_svr	proxy_svr;
	
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

extern int		proxy_svr_init(char *, proxy_handler_funcs *, proxy_svr_helper_funcs *, proxy_svr_commands *cmds, proxy_svr **);
extern int		proxy_svr_create(proxy_svr *, int);
extern int		proxy_svr_connect(proxy_svr *, char *, int);
extern int		proxy_svr_progress(proxy_svr *);
extern void		proxy_svr_finish(proxy_svr *);
extern void		proxy_svr_event_callback(proxy_svr *, char *);

extern int		proxy_clnt_init(char *, proxy_handler_funcs *, proxy_clnt_helper_funcs *, proxy_clnt **, char *, va_list);
extern int 		proxy_clnt_connect(proxy_clnt *);
extern int 		proxy_clnt_create(proxy_clnt *);
extern int 		proxy_clnt_progress(proxy_clnt *);
extern int 		proxy_clnt_sendcmd(proxy_clnt *, char *, char *, ...);
extern void		proxy_clnt_event_callback(proxy_clnt *, char *);

#endif /* _PROXY_H_*/
