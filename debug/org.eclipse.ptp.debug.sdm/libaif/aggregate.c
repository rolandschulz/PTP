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
#include	<string.h>
#ifndef WIN32
#include	<unistd.h>
#endif /* !WIN32 */

#include	"aif.h"
#include	"aifint.h"
#include	"aiferr.h"


/*
 * Create an empty AIF aggregate.
 */
AIF *
EmptyAggregateToAIF(char *id)
{
	AIF *	a;

	a = NewAIF(0, 0);

	AIF_FORMAT(a) = FDSAggregateInit(id);

	ResetAIFError();

	return a;
}

/*
 * Add a field to an aggregate. The field will be added to
 * the end of the section specified by the access qualifier.
 */
int
AIFAddFieldToAggregate(AIF *a, AIFAccess acc, char* field, AIF *content)
{
	int		data_len;
	int		old_len;
	int		new_len;
	char *	new_fmt;
	char *	new_data;
	char *	dummy;
	char *	data;
	char *	fds;


	if ( !FDSAggregateFieldByName(AIF_FORMAT(a), field, &dummy) )
	{
		_aif_free(dummy);
		SetAIFError(AIFERR_BADARG, NULL);
		return -1;
	}

	old_len = AIF_LEN(a);
	new_len = old_len + AIF_LEN(content);
	new_data = _aif_alloc(new_len);

	fds = AIF_FORMAT(a);
	data = AIF_DATA(a);

	_fds_skip_typename(&fds);
	_aggregate_skip_to_section_end(&fds, &data, acc);

	data_len = data - AIF_DATA(a);

	if (data_len > 0) {
		memcpy(new_data, AIF_DATA(a), data_len);
		memcpy(new_data + data_len, AIF_DATA(content), AIF_LEN(content));
		memcpy(new_data + data_len + AIF_LEN(content), AIF_DATA(a) + data_len, AIF_LEN(a) - data_len);
	} else {
		memcpy(new_data, AIF_DATA(content), AIF_LEN(content));
	}

	if (AIF_DATA(a) != NULL) {
		_aif_free(AIF_DATA(a));
	}

	AIF_DATA(a) = new_data;
	AIF_LEN(a) = new_len;

	new_fmt = FDSAddFieldToAggregate(AIF_FORMAT(a), acc, field, AIF_FORMAT(content));

	_aif_free(AIF_FORMAT(a));
	AIF_FORMAT(a) = new_fmt;

	ResetAIFError();

	return 0;
}

/*
 * Set the value of a field in an aggregate
 */
int
AIFSetAggregate(AIF *a, char* field, AIF *content)
{
	int	preLength;
	char *	dummyString;
	int	index;

	if ( FDSAggregateFieldByName(AIF_FORMAT(a), field, &dummyString) )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return -1;
	}

	index = FDSAggregateFieldIndex(AIF_FORMAT(a), field);
	preLength = _data_len_index(AIF_FORMAT(a), index);
	preLength -= FDSTypeSize(dummyString);
	_aif_free(dummyString);

	memcpy(AIF_DATA(a)+preLength, AIF_DATA(content), AIF_LEN(content));

	ResetAIFError();

	return(0);
}

/*
 * Get the value of a field in an aggregate
 */
AIF *
AIFGetAggregate(AIF *a, char *field)
{
	AIF  * new;
	char * type;
	int    len;
	int    pre;
	int    index;

	if ( FDSAggregateFieldByName(AIF_FORMAT(a), field, &type) < 0 )
	{
		SetAIFError(AIFERR_FIELD, NULL);
		return NULL;
	}

	if ( (len = FDSTypeSize(type)) < 0 )
	{
		_aif_free(type);
		return NULL;
	}

	new = NewAIF(0, len);

	index = FDSAggregateFieldIndex(AIF_FORMAT(a), field);
	pre = _data_len_index(AIF_FORMAT(a), index);
	pre -= len;

	AIF_FORMAT(new) = type;
	AIF_LEN(new) = len;
	memcpy(AIF_DATA(new), AIF_DATA(a)+pre, len);

	return new;
}

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

/*
 * Skip to the end of the access qualifier section specified by 'acc'
 * for the aggregate type 'fds'. Note that fds must be pointing
 * to the beginning of a section on entry.
 *
 * The data pointer will be advanced so that it points to the first
 * data byte of the next section, or one byte past the end of the
 * data if the PACKAGE section has been specified.
 */
void
_aggregate_skip_to_section_end(char **fds, char **data, AIFAccess acc)
{
	_fds_skip_name(fds);

	_aggregate_skip_section(fds, data);

	switch ( acc )
	{
		case AIF_ACCESS_PACKAGE:
			_aggregate_skip_section(fds, data);
			/* fall through */

		case AIF_ACCESS_PRIVATE:
			_aggregate_skip_section(fds, data);
			/* fall through */

		case AIF_ACCESS_PROTECTED:
			_aggregate_skip_section(fds, data);
			/* fall through */

		case AIF_ACCESS_PUBLIC:
		case AIF_ACCESS_UNKNOWN:
			break;
	}
}

/*
 * Skip fields until the end of a section or the end of
 * the aggregate is found. Note that fds must be pointing
 * to the beginning of a field on entry.
 *
 * On completion, the fds and data will point as follows:
 *
 * 1. If this was the last section, fds will point to the
 *    end of aggregate and data will point one byte past
 *    the end of the aggregate data.
 *
 * 2. Otherwise, fds will point to the section separator
 *    and data will point to the beginning of the next section.
 */
void
_aggregate_skip_section(char **fds, char **data)
{
	while (**fds != FDS_AGGREGATE_END && **fds != FDS_AGGREGATE_ACCESS_SEP) {
		_aggregate_skip_field(fds, data);

		if (**fds == FDS_AGGREGATE_FIELD_SEP) {
			(*fds)++;
		}
	}

	if (**fds == FDS_AGGREGATE_ACCESS_SEP) {
		(*fds)++;
	}
}

/*
 * Skip one field. The fds and data will point as
 * follows:
 *
 * 1. If this was the last field in a section, fds
 *    will point to the section separator and data
 *    will point to the first field in the
 *    following section.
 *
 * 2. If this was the last field in the last section,
 *    fds will point to the end of aggregate character
 *    and data will point one byte past the end of the
 *    aggregate data.
 *
 * 3. Otherwise, fds will point to the field separator
 *    and data will point to the next field.
 */
void
_aggregate_skip_field(char **fds, char **data)
{
	*fds = strchr(*fds, FDS_AGGREGATE_FIELD_NAME_END) + 1;
	_fds_skip_data(fds, data);
}
