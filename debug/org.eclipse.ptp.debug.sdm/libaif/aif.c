/*
 * Generic AIF routines.
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

#ifdef STDC_HEADERS
#include	<stdlib.h> /* for atoi() */
#include	<string.h> /* memset() */
#endif /* STDC_HEADERS */

#ifdef WIN32
#include	<io.h>
#endif /* WIN32 */

#include	"aif.h"
#include	"aiferr.h"
#include	"aifint.h"

char *	_aif_values_seen[MAX_VALUES_SEEN+1];
char *	_aif_types_seen[MAX_TYPES_SEEN+1];

static int	_aif_opt_cmp_depth = 0;
static int	_aif_opt_cmp_method = 0;

int
AIFSetOption(aifopt opt, int arg)
{
	switch( opt )
	{
	case AIFOPT_CMP_DEPTH:
		_aif_opt_cmp_depth = arg;
		break;

	case AIFOPT_CMP_METHOD:
		if ( arg != AIF_CMP_BY_POSITION && arg != AIF_CMP_BY_NAME )
			return -1;
		_aif_opt_cmp_method = arg;
		break;

	default:
		return -1;
	}
	return 0;
}

int
AIFGetOption(aifopt opt)
{
	switch( opt )
	{
	case AIFOPT_CMP_DEPTH:
		return _aif_opt_cmp_depth;

	case AIFOPT_CMP_METHOD:
		return _aif_opt_cmp_method;

	default:
		return -1;
	}
}

void
AIFFree(AIF *a)
{
	if ( AIF_FORMAT(a) != NULL )
		_aif_free(AIF_FORMAT(a));

	if ( AIF_DATA(a) != NULL )
		_aif_free(AIF_DATA(a));

	_aif_free(a);
}

int
AIFType(AIF *a)
{
	if ( a == (AIF *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return AIF_INVALID;
	}

	return FDSType(AIF_FORMAT(a));
}

int
AIFBaseType(AIF *a)
{
	if ( a == (AIF *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return AIF_INVALID;
	}

	return FDSType(FDSBaseType(AIF_FORMAT(a)));
}

long
AIFTypeSize(AIF *a)
{
	if ( a == (AIF *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return -1;
	}

	return AIF_LEN(a);
}

/*
 * Create a new AIF object. 0 for either len means
 * provided later.
 */
AIF *
NewAIF(int fmt_len, int data_len)
{
	AIF *	a;

	a = (AIF *)_aif_alloc(sizeof(AIF));

	AIF_FORMAT(a) = (char *)NULL;
	AIF_DATA(a) = (char *)NULL;

	if ( fmt_len > 0 )
	{
		AIF_FORMAT(a) = (char *)_aif_alloc(fmt_len);
		memset(AIF_FORMAT(a), 0, fmt_len);
	}

	if ( data_len > 0 )
	{
		AIF_DATA(a) = (char *)_aif_alloc(data_len);
		memset(AIF_DATA(a), 0, data_len);
	}

	AIF_LEN(a) = data_len;

	return a;
}

AIF *
MakeAIF(char *fmt, char *data)
{
	AIF *	a;

	if ( fmt == NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return (AIF *)NULL;
	}

	if ( data == NULL )
	{
		a = NewAIF(0, FDSTypeSize(fmt));
		AIF_FORMAT(a) = fmt;
	}
	else
	{
		char * tmp1;
		char * tmp2;
		tmp1 = fmt;
		tmp2 = data;
		_fds_skip_data(&tmp1, &tmp2);
		a = NewAIF(0, 0);
		AIF_FORMAT(a) = fmt;
		AIF_LEN(a) = tmp2-data;
		AIF_DATA(a) = data;
	}

	return a;
}

AIF *
CopyAIF(AIF *a)
{
	int	l1;
	AIF *	a1;

	if ( a == (AIF *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return (AIF *)NULL;
	}

	l1 = strlen(AIF_FORMAT(a)) + 1;

	a1 = NewAIF(l1, AIF_LEN(a));

	memcpy(AIF_FORMAT(a1), AIF_FORMAT(a), l1);

	if ( AIF_LEN(a) > 0 )
		memcpy(AIF_DATA(a1), AIF_DATA(a), AIF_LEN(a));

	return a1;
}

/*
 * Copy data into AIF structure.
 */
void
AIFSetData(AIF *a, char *data, int size)
{
	int	len;

	len = size > (int)AIF_LEN(a) ? size : (int)AIF_LEN(a);

	memcpy(AIF_DATA(a), data, len);
}

