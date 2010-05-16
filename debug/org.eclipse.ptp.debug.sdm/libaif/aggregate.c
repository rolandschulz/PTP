/*
 * Routines specific to AIF aggregate (struct/class) objects.
 *
 * Copyright (c) 1996-2002 by Guardsoft Pty Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */

#ifdef HAVE_CONFIG_H
#include	<config.h>
#endif /* HAVE_CONFIG_H */

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

	if ( AIFType(a) != AIF_AGGREGATE )
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

	if ( AIFType(a) != AIF_AGGREGATE )
	{
		SetAIFError(AIFERR_TYPE, NULL);
		return AIF_INVALID;
	}

	if ( FDSAggregateFieldByName(AIF_FORMAT(a), name, &type) < 0 )
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
 * AIFFieldToLongest() and AIFFieldToDoublest() are broken because of the
 * new structure format. We need to add AIFField().
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

	if ( AIFType(a) != AIF_AGGREGATE )
	{
		SetAIFError(AIFERR_TYPE, NULL);
		return -1;
	}

	if ( name != NULL )
	{
		if ( FDSAggregateFieldByName(AIF_FORMAT(a), name, &type) < 0 )
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

	if ( AIFType(a) != AIF_AGGREGATE )
	{
		SetAIFError(AIFERR_TYPE, NULL);
		return -1;
	}

	if ( name != NULL )
	{
		if ( FDSAggregateFieldByName(AIF_FORMAT(a), name, &type) < 0  )
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
