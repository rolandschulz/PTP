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

#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <ctype.h>

#include "proxy.h"
#include "proxy_event.h"
#include "args.h"
#include "list.h"

#ifdef __linux__
extern int digittoint(int c);
#endif /* __linux__ */

static char tohex[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

int
proxy_data_to_str(char *data, int len, char **result)
{
	int		i;
	int		n;
	char		ch;
	char *	res;
	
	if (data == NULL) {
		*result = strdup("1:00");
		return 0;
	}
	
	*result = res = (char *)malloc((len * 2) + 8 + 2);
	
	/*
	 * Convert length (silently truncate to 32 bits)
	 */
	n = sprintf(res, "%X:", len & 0xffffffff);
	
	res += n;
	
	/*
	 * Add rest of data 
	 */
	for (i = 0; i < len; i++) {
		ch = *data++;
		*res++ = tohex[(ch >> 4) & 0xf];
		*res++ = tohex[ch & 0xf];
	}
	
	*res = '\0';
	
	return 0;
}

int
proxy_cstring_to_str(char *str, char **result)
{
	int	len = 0;
	
	if (str != NULL)
		len = strlen(str) + 1;

	return proxy_data_to_str(str, len, result);
}

int
proxy_list_to_str(List *lst, int (*el_to_str)(void *, char **), char **result)
{
	int		i;
	int		len;
	int		count;
	void *	el;
	char *	res;
	char **	strs;
	/*
	 * Calculate number of strings.
	 */
	for (count = 0, SetList(lst); GetListElement(lst) != NULL; )
		count++;
	
	strs = (char **)malloc(sizeof(char *) * count);
	
	/*
	 * Convert strings.
	 */
	for (len = 0, i = 0, SetList(lst); (el = GetListElement(lst)) != NULL; i++) {
		el_to_str(el, &strs[i]);
		len += strlen(strs[i]) + 1;
	}

	*result = res = (char *)malloc(9 + len + 1);
	
	sprintf(res, "%d ", count & 0xffff);
	
	for (i = 0; i < count; i++) {
		if (i > 0)
			strcat(res, " ");
		strcat(res, strs[i]);
		free(strs[i]);
	}
	
	free(strs);

	return 0;
}

int
proxy_event_to_str(proxy_event *e, char **result)
{
	int		res = 0;
	char *	str;

	if (e == NULL)
		return -1;
	
	switch (e->event)
	{
	case PROXY_EV_OK:
 		asprintf(result, "%d %s", e->event, e->event_data);
		break;

	case PROXY_EV_ERROR:
		proxy_cstring_to_str(e->error_msg, &str);
 		asprintf(result, "%d %d %s", e->event, e->error_code, str);
		break;
		
	case PROXY_EV_CONNECTED:
 		asprintf(result, "%d", e->event);
		break;
		
	default:
		res = -1;
		break;
	}
	
	return res;
}

int
proxy_str_to_data(char *str, char **data, int *len)
{
	int		data_len;
	char		ch;
	char *	p;
	
	if (str == NULL)
		return -1;
		
	for (data_len = 0; *str != ':' && *str != '\0' && isxdigit(*str); str++) {
		data_len <<= 4;
		data_len += digittoint(*str);
	}
	
	if (*str++ != ':')
		return -1;

	*len = data_len;
	*data = p = (char *)malloc(sizeof(char) * data_len);
	
	for (; data_len > 0; data_len--) {
		ch = digittoint(*str++);
		ch <<= 4;
		ch |= digittoint(*str++);

		*p++ = ch;
	}
		
	return 0;
}

int
proxy_str_to_cstring(char *str, char **cstring)
{
	int	len;
	
	return proxy_str_to_data(str, cstring, &len);
}


int
proxy_str_to_int(char *str, int *val)
{
	if (str == NULL)
		return -1;
	
	*val = (int)strtol(str, NULL, 10);
	
	return 0;
}

int
proxy_str_to_event(char *str, proxy_event **ev)
{
	int				event;
	proxy_event *	e = NULL;
	char **			args;
	char *			rest;
		
	if (str == NULL || (rest = strchr(str, ' ')) == NULL)
		return -1;

	*rest++ = '\0';
	
	event = (int)strtol(str, NULL, 10);
	
	switch (event)
	{
	case PROXY_EV_OK:
		e = new_proxy_event(PROXY_EV_OK);
		e->event_data = strdup(rest);
		break;
	
	case PROXY_EV_ERROR:
		if ((args = Str2Args(rest)) == NULL)
			goto error_out;
			
		e = new_proxy_event(PROXY_EV_ERROR);
		if (proxy_str_to_int(args[0], &e->error_code) < 0 ||
				proxy_str_to_cstring(args[1], &e->error_msg) < 0) {
			FreeArgs(args);
			goto error_out;
		}
			
		FreeArgs(args);
		break;
		
	case PROXY_EV_CONNECTED:
		e = new_proxy_event(PROXY_EV_CONNECTED);
		break;
	
	default:
		goto error_out;
	}
	
	*ev = e;
	
	return 0;
	
error_out:
	if (e != NULL)
		free_proxy_event(e);
		
	return -1;
}

proxy_event *	
new_proxy_event(int event) {
	proxy_event *	e = (proxy_event *)malloc(sizeof(proxy_event));
	
	memset((void *)e, 0, sizeof(proxy_event));
	 
	e->event = event;
	
	return e;
}

void	
free_proxy_event(proxy_event *e) {
	switch (e->event) {
	case PROXY_EV_OK:
		if (e->event_data != NULL)
			free(e->event_data);
		break;
		
	case PROXY_EV_ERROR:
		if (e->error_msg != NULL)
			free(e->error_msg);
		break;
	}
		
	free(e);
}

void
proxy_event_callback(proxy *p, char *data)
{
	proxy_event *	e;
	
	if (p->handler_funcs->eventcallback != NULL) {
		e = new_proxy_event(PROXY_EV_OK);
		e->event_data = strdup(data);
		p->handler_funcs->eventcallback(PROXY_EVENT_HANDLER, (void *)e);
	}
}
