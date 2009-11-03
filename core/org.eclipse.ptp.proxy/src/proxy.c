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
#endif /* __gnu_linux__ */

/*
 * Miscellaneous proxy functions.
 */
 
#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#include "proxy.h"

static int 			proxy_errno = PTP_PROXY_RES_OK;
static char *		proxy_errstr = NULL;

static char * proxy_error_tab[] = {
	"Proxy client error: %s",
	"Proxy server error: %s",
	"Error with proxy protocol: %s",
	"System error: %s",
	"%s"
};

int
find_proxy(char *name, proxy **pp)
{
	proxy *p;
	
	for (p = proxies; p->name != NULL; p++) {
		if (strcmp(p->name, name) == 0) {
			*pp = p;
			return 0;
		}
	}
	return -1;
}


/*
 * Error handling
 */
void
proxy_set_error(int errnum, char *msg)
{
	proxy_errno = errnum;
	
	if (proxy_errstr != NULL) {
		free(proxy_errstr);
		proxy_errstr = NULL;
	}
	
	if (proxy_errno >= sizeof(proxy_error_tab)/sizeof(char *)) {
		if (msg != NULL) {
			proxy_errstr = strdup(msg);
		} else {
			asprintf(&proxy_errstr, "Error %d occurred.", proxy_errno);
		}
	} else {
		if (msg == NULL)
			msg = "<null>";
			
		asprintf(&proxy_errstr, proxy_error_tab[proxy_errno], msg);
	}
}

int
proxy_get_error(void)
{
	return proxy_errno;
}

char *
proxy_get_error_str(void)
{
	if (proxy_errstr == NULL)
		return "";
		
	return proxy_errstr;
}
