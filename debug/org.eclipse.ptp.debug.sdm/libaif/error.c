/*
 * Routines for handling AIF errors.
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
#include	<string.h>

#include	"aif.h"
#include	"aifint.h"
#include	"aiferr.h"

char *	aiferrstr[] =
{
	"No error",
	"Arguments are different sizes",
	"Can't convert arguments",
	"Function not supported for this type",
	"Bad argument",
	"Function not implemented",
	"Error opening file: %s",
	"Error writing file: %s",
	"Error reading file: %s",
	"Error seeking file: %s",
	"Bad file header",
	"Invalid mode for operation",
	"Bad descriptor",
	"No such field",
	"Invalid arithmetic operation",
	"Invalid base for conversion",
	"Index out of range",
	"String too long",
	"This error can't happen"
};

static aiferr	_aif_errno = AIFERR_NOERR;
static char *	_aif_errstr = NULL;

aiferr
AIFError(void)
{
	return _aif_errno;
}

char *
AIFErrorStr(void)
{
	static char	buf[BUFSIZ];

	if ( _aif_errno >= AIFERR_CANTHAPPEN )
		return "Unknown AIF error";

	if ( _aif_errstr != NULL && *_aif_errstr != '\0' )
	{
		snprintf(buf, BUFSIZ, aiferrstr[_aif_errno], _aif_errstr);
		return buf;
	}

	return aiferrstr[_aif_errno];
}

void
SetAIFError(aiferr errno, char *errstr)
{
	_aif_errno = errno;

	if ( _aif_errstr != NULL )
	{
		_aif_free(_aif_errstr);
		_aif_errstr = NULL;
	}

	if ( errstr != NULL )
		_aif_errstr = strdup(errstr);
}

void
ResetAIFError(void)
{
	_aif_errno = AIFERR_NOERR;

	if ( _aif_errstr != NULL )
	{
		_aif_free(_aif_errstr);
		_aif_errstr = NULL;
	}
}
