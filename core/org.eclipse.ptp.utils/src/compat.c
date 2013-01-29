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

#include "compat.h"

#if defined (__linux__) || defined (_AIX)
int
digittoint(int c)
{
	if (c >= '0' && c <= '9')
		return c - '0';
		
	if (c >= 'a' && c <= 'f')
		return c - 'a' + 10;
		
	if (c >= 'A' && c <= 'F')
		return c - 'A' + 10;
	
	return 0;
}
#endif /* __linux__ */


#ifndef HAVE_ASPRINTF
#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>

#ifndef va_copy
#define va_copy(__list1,__list2) ((void)(__list1 = __list2))
#endif /* va_copy */

/* 
 * assumes presence of vsnprintf (ISO C99) to calculate length of formatted string
 */

int
vasprintf(char **ret, const char *fmt, va_list ap)
{
	va_list ac;
	size_t len;
	char buf[2];

	va_copy(ac, ap);
	len = vsnprintf(buf, 2, fmt, ap);	/* get length of string first */
	*ret = malloc(len+1);
	len = vsnprintf(*ret, len+1, fmt, ac);
	va_end(ac);

	return len;
}


int
asprintf(char ** ret, const char * fmt, ...)
{
	va_list ap;
	size_t len;

	va_start(ap, fmt);
	len = vasprintf(ret, fmt, ap);
	va_end(ap);
	return len;
}
#endif /* !HAVE_ASPRINTF */
