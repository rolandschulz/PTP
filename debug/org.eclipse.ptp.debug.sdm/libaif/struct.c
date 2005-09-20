/*
** Routines specific to AIF structure objects.
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
#ifndef WIN32
#include	<unistd.h>
#endif /* !WIN32 */

#include	"aif.h"
#include	"aifint.h"
#include	"aiferr.h"

int
AIFNumFields(AIF *a)
{
	if ( a == (AIF *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return -1;
	}

	if ( AIFType(a) != AIF_STRUCT )
	{
		SetAIFError(AIFERR_TYPE, NULL);
		return -1;
	}

	return FDSNumFields(AIF_FORMAT(a));
}

int
AIFFieldType(AIF *a, char *name)
{
	int	ret;
	char *	type;

	if ( a == (AIF *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return AIF_INVALID;
	}

	if ( AIFType(a) != AIF_STRUCT )
	{
		SetAIFError(AIFERR_TYPE, NULL);
		return AIF_INVALID;
	}

	if ( FDSStructFieldByName(AIF_FORMAT(a), name, &type) < 0 )
	{
		SetAIFError(AIFERR_FDS, NULL);
		return AIF_INVALID;
	}

	ret = FDSType(type);
	_aif_free(type);

	return ret;
}

int
AIFFieldToInt(AIF *a, char *name, int *val)
{
	AIFLONGEST	l;

	if ( AIFFieldToLongest(a, name, &l) < 0 )
		return -1;

	*val = (int)l; /* potential loss of precision */

	return 0;
}

/*XXX
** AIFFieldToLongest() and AIFFieldToDoublest() are broken because of the
** new structure format. We need to add AIFField().
*/
int
AIFFieldToLongest(AIF *a, char *name, AIFLONGEST *val)
{
	char *	type;

	if ( a == (AIF *)NULL || name == NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return -1;
	}

	if ( AIFType(a) != AIF_STRUCT )
	{
		SetAIFError(AIFERR_TYPE, NULL);
		return -1;
	}

	if ( name != NULL )
	{
		if ( FDSStructFieldByName(AIF_FORMAT(a), name, &type) < 0 )
		{
			SetAIFError(AIFERR_FDS, NULL);
			return -1;
		}

		if ( FDSType(type) != AIF_INTEGER )
		{
			SetAIFError(AIFERR_TYPE, NULL);
			return -1;
		}
	}

/*	return _aif_to_longest(AIF_DATA(a) + off, size, val);*/
	return 0;
}

int
AIFFieldToDouble(AIF *a, char *name, double *val)
{
	AIFDOUBLEST	d;

	if ( AIFFieldToDoublest(a, name, &d) < 0 )
		return -1;

	*val = (double)d; /* possible loss of precision */

	return 0;
}

/*XXX*/
int
AIFFieldToDoublest(AIF *a, char *name, AIFDOUBLEST *val)
{
	char *	type;

	if ( a == (AIF *)NULL || name == NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return -1;
	}

	if ( AIFType(a) != AIF_STRUCT )
	{
		SetAIFError(AIFERR_TYPE, NULL);
		return -1;
	}

	if ( name != NULL )
	{
		if ( FDSStructFieldByName(AIF_FORMAT(a), name, &type) < 0  )
		{
			SetAIFError(AIFERR_FDS, NULL);
			return -1;
		}

		if ( FDSType(type) != AIF_FLOATING )
		{
			SetAIFError(AIFERR_TYPE, NULL);
			return -1;
		}
	}

/*	return _aif_to_doublest(AIF_DATA(a) + off, size, val);*/
	return 0;
}
