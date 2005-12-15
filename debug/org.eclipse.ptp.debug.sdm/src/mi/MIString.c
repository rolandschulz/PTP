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

#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include "MIString.h"

MIString *
MIStringNew(char *fmt, ...)
{
	va_list ap;
	MIString *s;

	s = (MIString *)malloc(sizeof(MIString));
	va_start(ap, fmt);
	vasprintf(&s->buf, fmt, ap);
	va_end(ap);
	s->slen = strlen(s->buf);
	
	return s;
}

void
MIStringFree(MIString *str)
{
	free(str->buf);
	free(str);
}

void
MIStringAppend(MIString *str, MIString *str2)
{
	int len = str->slen + str2->slen;
	char *buf = (char *)malloc(len + 1);
	
	memcpy(buf, str->buf, str->slen);
	memcpy(&buf[str->slen], str2->buf, str2->slen);
	buf[len] = '\0';
	
	free(str->buf);
	
	str->buf = buf;
	str->slen = len;
	
	MIStringFree(str2);
}

char *
MIStringToCString(MIString *str)
{
	return str->buf;
}

char *
MIIntToCString(int val)
{
	static char *	str = NULL;
	
	if (str != NULL)
		free(str);
	asprintf(&str, "%d", val);
	return str;
}
