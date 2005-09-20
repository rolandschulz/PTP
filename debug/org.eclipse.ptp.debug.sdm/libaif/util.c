/*
** Utility routines
**
** Copyright (c) 1996-2002 by Guardsoft Pty Ltd.
**
** This program is free software; you can redistribute it and/or modify
** it under the terms of the GNU General Public License as published by
** the Free Software Foundation; either version 2 of the License, or
** (at your option) any later version.
**
** This program is distributed in the hope that it will be useful,
** but WITHOUT ANY WARRANTY; without even the implied warranty of
** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
** GNU General Public License for more details.
**
** You should have received a copy of the GNU General Public License
** along with this program; if not, write to the Free Software
** Foundation, Inc., 59 Temple Place - Suite 330,
** Boston, MA 02111-1307, USA.
**
*/

#ifdef HAVE_CONFIG_H
#include	<config.h>
#endif /* HAVE_CONFIG_H */

RCSID("$Id$");

#include	<stdio.h>
#include	<stdlib.h>

void *
_aif_alloc(int size)
{
	void *	m = malloc(size);

	if ( m == (void *)NULL )
		perror("malloc error");

	return m;
}

void *
_aif_resize(void *m, int s)
{
	if ( m == (void *)NULL )
		return _aif_alloc(s);

	m = realloc(m, s);

	if ( m == (void *)NULL )
		perror("realloc error");

	return m;
}

void
_aif_free(void *m)
{
	if ( m == (void *)NULL )
	{
		fprintf(stderr, "Warning: freeing NULL pointer.\n");
		return;
	}

	(void)free(m);
}
