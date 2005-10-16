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
 
#ifndef _PROXY_EVENT_H_
#define _PROXY_EVENT_H_

#include "list.h"

#define PROXY_EV_OK			0
#define PROXY_EV_ERROR		1

#define PROXY_EVENT_HANDLER	0

struct proxy_event {
	int		event;
	char *	event_data;
	int		error_code;
	char *	error_msg;
};
typedef struct proxy_event proxy_event;

extern int 			proxy_data_to_str(char *, int, char **);
extern int 			proxy_cstring_to_str(char *, char **);
extern int 			proxy_list_to_str(List *, int (*)(void *, char **), char **);
extern int 			proxy_str_to_data(char *, char **, int *);
extern int 			proxy_str_to_cstring(char *, char **);
extern int 			proxy_str_to_int(char *, int *);
extern int 			proxy_str_to_event(char *, proxy_event **);
extern int 			proxy_event_to_str(proxy_event *, char **);
extern proxy_event *	new_proxy_event(int);
extern void			free_proxy_event(proxy_event *);
#endif /* proxy_event */
