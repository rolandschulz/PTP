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

#define INITIAL_MAXARGC	8

char **
Str2Args(char *s)
{
	int	squote = 0;
	int	dquote = 0;
	int	bsquote = 0;
	int	argc = 0;
	int	maxargc = 0;
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
				maxargc = INITIAL_MAXARGC;
				nargv = (char **)malloc(maxargc * sizeof (char *));
			}
			else
			{
				maxargc *= 2;
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

char **
NewArgs(char *a0, ...)
{
	int	n = 1;
	char **	ap;
	char **	app;
	va_list args;

	if ( a0 == NULL )
	{
		ap = (char **)malloc(sizeof(char *));
		*ap = NULL;
		return ap;
	}

	va_start(args, a0);

	while ( va_arg(args, char *) != NULL )
		n++;

	va_end(args);

	app = ap = (char **)malloc((n + 1) * sizeof(char *));
	*app++ = strdup(a0);

	va_start(args, a0);

	while ( --n > 0 )
		*app++ = strdup(va_arg(args, char *));

	va_end(args);

	*app = NULL;

	return ap;
}

char **
AppendArgv(char **ap, char **ep)
{
	int	n = 0;
	char **	na;
	char **	nap;
	char **	app;

	if ( ep == NULL || *ep == NULL )
		return ap;

	for ( app = ap ; *app != NULL ; app++ )
		n++;

	for ( app = ep ; *app != NULL ; app++ )
		n++;

	nap = na = (char **)malloc((n + 1) * sizeof(char *));

	for ( app = ap ; *app != NULL ; app++, nap++ )
		*nap = strdup(*app);

	for ( app = ep ; *app != NULL ; app++, nap++ )
		*nap = strdup(*app);

	*nap = NULL;

	FreeArgs(ap);

	return na;
}

char **
AppendArgs(char **ap, ...)
{
	int	n = 1;
	char *	a;
	char **	na;
	char **	nap;
	char **	app;
	va_list args;

	for ( app = ap ; *app != NULL ; app++ )
		n++;

	va_start(args, ap);

	while ( va_arg(args, char *) != NULL )
		n++;

	va_end(args);

	nap = na = (char **)malloc((n + 1) * sizeof(char *));

	for ( app = ap ; *app != NULL ; app++, nap++ )
		*nap = strdup(*app);

	va_start(args, ap);

	while ( (a = va_arg(args, char *)) != NULL )
		*nap++ = strdup(a);

	va_end(args);

	*nap = NULL;

	FreeArgs(ap);

	return na;
}

/*
** Convert arguments into a string.
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
