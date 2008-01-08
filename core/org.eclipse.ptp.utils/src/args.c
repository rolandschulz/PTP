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

#include	<stdio.h>
#include	<stdarg.h>
#include	<stdlib.h>
#include	<string.h>
#include	<ctype.h>

#include	"args.h"

#define CHUNK_SIZE				8
#define LENGTH_TO_CHUNKS(len)	(len / CHUNK_SIZE + 1)
#define FREE_SPACE(len)			(CHUNK_SIZE - (len - 1) % CHUNK_SIZE - 1)

/*
 * Convert a string to an arg array
 */
char **
StrToArgs(char *s)
{
	int		squote = 0;
	int		dquote = 0;
	int		bsquote = 0;
	int		argc = 0;
	int		maxargc = 0;
	char *	arg;
	char *	copybuf;
	char **	argv = NULL;
	char **	nargv;

	if ( s == NULL )
		return argv;

	copybuf = (char *)malloc(strlen(s) + 1);

	do
	{
		while (isspace ((int)*s))
			s++;

		if ( (maxargc == 0) || (argc >= (maxargc - 1)) )
		{
			if ( argv == NULL )
			{
				maxargc = CHUNK_SIZE;
				nargv = (char **)malloc(maxargc * sizeof (char *));
			}
			else
			{
				maxargc += CHUNK_SIZE;
				nargv = (char **)realloc(argv, maxargc * sizeof (char *));
			}

			argv = nargv;
			argv[argc] = NULL;
		}

		arg = copybuf;

		while ( *s != '\0' )
		{
			if ( isspace((int)*s) && !squote && !dquote && !bsquote )
				break;

			switch (*s) {
			case '\\':
				if (bsquote) {
					*arg++ = *s;
					bsquote = 0;
				} else
					bsquote = 1;
				break;
			
			
			case '\'':
				if (squote)
					squote = 0;
				else
					squote = 1;
				break;
			
			case '"':
				if (dquote)
					dquote = 0;
				else
					dquote = 1;
				break;

			default:
				if (bsquote)
				{
					bsquote = 0;
					
					switch (*s) {
					case 'n':
						*arg++ = '\n';
						break;
					case 't':
						*arg++ = '\t';
						break;
					default:
						*arg++ = *s;
						break;
					}
					
					break;
				}
				
				*arg++ = *s;
				break;
			}

			s++;
		}

		*arg = '\0';

		if ( (argv[argc] = strdup(copybuf)) == NULL )
		{
			free(copybuf);
			FreeArgs(argv);
			argv = NULL;
			return argv;
		}

		argc++;
		argv[argc] = NULL;

		while ( isspace ((int)*s) )
			s++;

	}
	while ( *s != '\0' );

	free(copybuf);

	return argv;
}

/*
 * Free an arg array
 */
void
FreeArgs(char **args)
{
	char **	ap;

	if ( args == (char **)NULL )
		return;

	for ( ap = args ; *ap != NULL ; ap++ )
		free(*ap);

	free(args);
}

/*
 * Create a new arg array. If a0 is NULL, create an empty array.
 */
char **
NewArgs(char *a0, ...)
{
	int		n = 1;
	char **	ap;
	char **	app;
	va_list args;

	if ( a0 == NULL )
	{
		ap = (char **)malloc(CHUNK_SIZE * sizeof(char *));
		*ap = NULL;
		return ap;
	}

	/*
	 * Calclulate number of arguments (n starts at 1 to allow for a0)
	 */
	va_start(args, a0);

	while ( va_arg(args, char *) != NULL )
		n++;

	va_end(args);
	
	/*
	 * Round up to multiple of CHUNK_SIZE. This takes into account the
	 * NULL entry also.
	 */
	app = ap = (char **)malloc(LENGTH_TO_CHUNKS(n + 2) * CHUNK_SIZE * sizeof(char *));
	*app++ = strdup(a0);

	va_start(args, a0);

	while ( --n > 0 )
		*app++ = strdup(va_arg(args, char *));

	va_end(args);

	*app = NULL;

	return ap;
}

/*
 * Append arg array ep to arg array ap. Returns a newly allocated array.
 */
char **
AppendArgv(char **ap, char **ep)
{
	int		na = 0;
	int 	ne = 0;
	char **	app;
	char ** npp;

	for ( app = ap ; *app != NULL ; app++ )
		na++;

	for ( app = ep ; *app != NULL ; app++ )
		ne++;
	
	npp = (char **)malloc(LENGTH_TO_CHUNKS(na + ne + 1) * CHUNK_SIZE * sizeof(char *));

	for ( app = npp ; *ap != NULL ; ap++ )
		*app++ = strdup(*ap);

	for ( app = npp + na ; *ep != NULL ; ep++ )
		*app++ = strdup(*ep);

	*app = NULL;

	return npp;
}

/*
 * Append arguments to an arg array. Returns a newly allocted array.
 */
char **
AppendArgs(char **ap, ...)
{
	int		na = 0;
	int 	ne = 0;
	char *	a;
	char **	app;
	char ** npp;
	va_list args;

	for ( app = ap ; *app != NULL ; app++ )
		na++;

	va_start(args, ap);

	while ( va_arg(args, char *) != NULL )
		ne++;

	va_end(args);
	
	npp = (char **)malloc(LENGTH_TO_CHUNKS(na + ne + 1) * CHUNK_SIZE * sizeof(char *));

	for ( app = npp ; *ap != NULL ; ap++ )
		*app++ = strdup(*ap);

	va_start(args, ap);

	for ( app = npp + na; (a = va_arg(args, char *)) != NULL ; )
		*app++ = strdup(a);

	va_end(args);

	*app = NULL;

	return npp;
}

/*
 * Append a string to the array. Will expand the size of
 * the array if necessary. Returns the array with the string
 * appended.
 */
char **
AppendStr(char **ap, char *str)
{
	int		n;
	int		na = 0;
	char **	app;
	
	for ( app = ap ; *app != NULL ; app++ )
		na++;

	n = FREE_SPACE(na);
	
	if (n < 2) {
		ap = (char **)realloc(ap, LENGTH_TO_CHUNKS(na + 2) * CHUNK_SIZE * sizeof(char *));
	}
	
	*(ap + na) = strdup(str);
	*(ap + na + 1) = NULL;
	
	return ap;
}

/*
** Copy arguments into a string buffer.
*/
void
ArgsToBuf(char *argv[], int argc, char *buf, int len)
{
	int	i;
	int	l;

	*buf = '\0';

	for ( i = 0 ; i < argc ; i++ )
	{
		l = strlen(argv[i]);

		if ( l > len - 1 )
			return;

		strcpy(buf, argv[i]);
		buf += l;
		len -= l;

		if ( i < argc - 1 )
		{
			if ( len < 2 )
				return;

			strcpy(buf, " ");
			buf++;
			len--;
		}
	}
}

/*
 * Convert arguments to a string
 */
char *
ArgsToStr(char **ap) 
{
	int 	n = 0;
	char **	app;
	char *	res;
	char *	rp;
	
	for ( app = ap ; *app != NULL ; app++ ) {
		n += strlen(*app) + 1;
	}

	rp = res = (char *)malloc(sizeof(char *) * n + 1);
	
	for ( app = ap ; *app != NULL ; app++ ) {
		if (app != ap) {
			*rp++ = ' ';
		}
		n = strlen(*app);
		memcpy(rp, *app, n);
		rp += n;
	}
	*rp = '\0';
	
	return res;
}
