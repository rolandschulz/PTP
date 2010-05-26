/*
 * Routines dealing with format descriptor strings (FDS).
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
#include	<stdarg.h>
#include	<string.h>

#include	<ctype.h>
#include	<stdlib.h>

#include	"aif.h"
#include	"aiferr.h"
#include	"aifint.h"

static char	_fds_type_str[NUM_AIF_TYPES][12] = 
{
	{ FDS_INVALID, '%', 'd', FDS_INVALID, '\0' },

	{ FDS_INTEGER, '%', 'c', '%', 'd', '\0' },

	{ FDS_FLOATING, '%', 'd', '\0' },

	{ FDS_POINTER, '%', 's', '%', 's', '\0' },

	{ FDS_ARRAY_START, '%', 's', FDS_ARRAY_END, '%', 's', '\0' },

	{ FDS_AGGREGATE_START, '%', 's', FDS_TYPENAME_END, ';', ';', ';', FDS_AGGREGATE_END, '\0' },

	{ FDS_UNION_START, '%', 's', FDS_TYPENAME_END, FDS_UNION_END, '\0' },

	{ FDS_FUNCTION, FDS_FUNCTION_ARG_END, '%', 's', '\0' },

	{ FDS_VOID, '%', 'd', '\0' },

	{ FDS_REGION, '%', 'd', '%', 's', '\0' },

	{ '%', FDS_NAME, '%', 'd', FDS_NAME_END, '%', 's', '\0' },

	{ FDS_REFERENCE, '%', 'd', FDS_REFERENCE_END, '\0' },

	{ FDS_STRING, '\0' },

	{ FDS_CHARACTER, '\0' },

	{ FDS_RANGE, '%', 'd', FDS_RANGE_SEP, '%', 'd', '%', 's', '\0' },

	{ FDS_ENUM_START, '%', 's', FDS_TYPENAME_END, FDS_ENUM_END, FDS_INTEGER, '%', 'c', '4', '\0' },

	{ FDS_BOOLEAN, '%', 'd', '\0' },
	
	{ FDS_ADDRESS, '%', 'd', '\0' },

	{ FDS_CHAR_POINTER, '%', 's', '\0' }
};

static char _fds_array_end[] = {FDS_ARRAY_END, '\0'};
static char _fds_function_arg_end[] = {FDS_FUNCTION_ARG_END, '\0'};
static char _fds_name_end[] = {FDS_NAME_END, '\0'};
static char _fds_enum_end[] = {FDS_ENUM_END, '\0'};
static char _fds_enum_const_name_end[] = {FDS_ENUM_SEP, '\0' };
static char _fds_enum_const_end[] = { FDS_ENUM_CONST_SEP, FDS_ENUM_END, '\0' };
static char _fds_aggregate_access_sep[] = {FDS_AGGREGATE_ACCESS_SEP, '\0'};
static char _fds_aggregate_field_name_end[] = {FDS_AGGREGATE_FIELD_NAME_END, '\0' };
static char _fds_aggregate_field_end[] = { FDS_AGGREGATE_ACCESS_SEP, FDS_AGGREGATE_FIELD_SEP, FDS_AGGREGATE_END, '\0' };
static char _fds_union_field_name_end[] = {FDS_UNION_FIELD_NAME_END, '\0' };
static char _fds_union_field_end[] = { FDS_UNION_FIELD_SEP, FDS_UNION_END, '\0' };

#define MAX_CALLS	2

static char *	_fds_buf[MAX_CALLS];
static int		_fds_pos = 0;

char *
_fds_skipnum(char *str)
{
	if ( str == NULL )
		return NULL;

	while ( *str != '\0' && isdigit((int)*str) )
		str++;

	return str;
}

char *
_fds_skipto(char *str, char *set)
{
	char *	s;

	if ( str == NULL )
		return NULL;

	for ( ; *str != '\0' ; str++ )
	{
		if
		(
			*str == FDS_AGGREGATE_START
			|| 
			*str == FDS_UNION_START 
			|| 
			*str == FDS_ARRAY_START 
			|| 
			*str == FDS_ENUM_START
		)
		{
			str = _fds_skiptomatch(str);
			str++;
		}

		for ( s = set ; *s != '\0' ; s++ )
			if ( *s == *str )
				return str;
	}

	return str;
}

/* 
 * Convert an ascii number to a number. NOTE: silently truncates
 * the number to fit in sizeof int.
 */

int
_fds_getnum(char *str)
{
	int	n;
	char *	p;
	char *	t;

	p = _fds_skipnum(str);

	if ( *p == '\0' )
		return strtoul(str, NULL, 10);

	n = p - str;
	t = (char *)_aif_alloc(n + 1);
	memcpy(t, str, n);
	t[n] = '\0';

	n = strtoul(t, NULL, 10);

	_aif_free(t);

	return n;
}

int
FDSType(char *str)
{
	int	type;

	if 
	(
		str == NULL 
		|| 
		*str == '\0'
	)
		return AIF_INVALID;

	switch ( *str )
	{
	case FDS_BOOLEAN: /* boolean */
		type = AIF_BOOLEAN; 	
		break; 		

	case FDS_CHARACTER: /* character */
		type = AIF_CHARACTER;
		break;

	case FDS_INTEGER: /* integer */
		type = AIF_INTEGER;
		break;

	case FDS_FLOATING: /* floating */
		type = AIF_FLOATING;
		break;

	case FDS_POINTER: /* pointer */
		type = AIF_POINTER;
		break;

	case FDS_ARRAY_START: /* array */
		type = AIF_ARRAY;
		break;

	case FDS_AGGREGATE_START:  /* struct or class */
		type = AIF_AGGREGATE;
		break;

	case FDS_UNION_START:  /* union */
		type = AIF_UNION;
		break;

	case FDS_ENUM_START: /* enumeration */
		type = AIF_ENUM;
		break;

	case FDS_RANGE: /* range */
		type = AIF_INTEGER;
		break;

	case FDS_FUNCTION: /* function */
		type = AIF_FUNCTION;
		break;

	case FDS_STRING: /* string */
		type = AIF_STRING;
		break;

	case FDS_CHAR_POINTER: /* char pointer */
		type = AIF_CHAR_POINTER;
		break;

	case FDS_ADDRESS: /* address */
		type = AIF_ADDRESS;
		break;

	case FDS_VOID: /* void */
		type = AIF_VOID;
		break;

	case FDS_REGION: /* ZPL region */
		type = AIF_REGION;
		break;

	case FDS_NAME: /* named component */
		type = AIF_NAME;
		break;

	case FDS_REFERENCE: /* reference to named component*/
		type = AIF_REFERENCE;
		break;

	default:
		type = AIF_INVALID;
	}

	return type;
}

/* extracts the base type of a pointer or array type, specified by the     */
/* type descriptor.                                                        */

char *
FDSBaseType(char *type)
{
	do
	{
		if ( (type = _fds_base_type(type)) == NULL )
			return NULL;
	}
	while ( FDSType(type) == AIF_ARRAY || FDSType(type) == AIF_POINTER );

	return type;
}

char *
_fds_base_type(char *type)
{
	char *	p;

	if ( type == NULL || *type == '\0' )
		return NULL;

	switch ( *type++ )
	{
	case FDS_FUNCTION: /* function */
		p = _fds_skipto(type, _fds_function_arg_end);
		p++;
		break;

	case FDS_POINTER: /* pointer */
		_fds_advance(&type); /* skip address */
		p = type;
		break;
		
	case FDS_ARRAY_START: /* array */
		p = _fds_skipto(type, _fds_array_end);
		p++;
		break;

	case FDS_ENUM_START: /* enum */
		p = _fds_skipto(type, _fds_enum_end);
		p++;
		break;

	case FDS_RANGE: /* range */
		p = _fds_skipnum(type); /* skip MinValue */
		p += 2;         /* skip '..' */
		p = _fds_skipnum(p); /* skip MaxValue */
		break;

	case FDS_REGION: /* region */
		p = type + 1;	/* skip FDS_REGION Rank */
		break;

	default:
		return NULL;
	}

	return p;
}

/* In AIF, we can find out about the size of the AIF object by looking at the
 * AIF_LEN(), but this method cannot be used to find out about the size
 * of each individual element in a aggregate. To overcome this problem, we can use
 * FDSTypeSize() with the FDS for each element.
 * 
 * However, FDSTypeSize() cannot be used to know the real size if the aggregate
 * contains references or strings (since we also need to access the data to
 * know the size of these data types).
 * 
 * We use FDSDataSize() to overcome the limitation of FDSTypeSize(). It can 
 * access FDS and the data of an AIF object to calculate the (real) size of 
 * the data.
 */

int
FDSDataSize(char *fds, char *data)
{
        char * tmp1 = fds;
        char * tmp2 = data;

		ResetAIFError();

        _fds_skip_data(&tmp1, &tmp2);

        if ( AIFError() == AIFERR_NOERR )
                return (tmp2-data);
        else
                return -1;
}

/*
 * Gets the size of a type in bytes, specified by the type descriptor.
 * If the size cannot fit in a int, then it is silently truncated.
 * returns size if successful, -1 otherwise.
 *
 * Note: we no longer know the size of aggregates or unions so these are
 * always -1.
 */
int
FDSTypeSize(char *type)
{
	int	s;
	int	rank;
	int	size;
	int	subsize;

	if ( type == NULL || *type == '\0' )
		return -1;

	switch ( *type )
	{
	case FDS_ADDRESS:
	case FDS_BOOLEAN: /* boolean */
		return _fds_getnum(type + 1);

	case FDS_CHARACTER: /* character */
		return 1;

	case FDS_INTEGER: /* integer */
		return _fds_getnum(type + 2);

	case FDS_REFERENCE: /* reference use */
		return 0;

	case FDS_NAME: /* named component */
		return FDSTypeSize(strchr(type, FDS_NAME_END) + 1);

	case FDS_FUNCTION: /* function */
		return FDSTypeSize(strchr(type, FDS_FUNCTION_ARG_END) + 1);

	case FDS_POINTER: /* pointer */
		return FDSTypeSize(type+1);

	case FDS_FLOATING: /* floating */
	case FDS_VOID: /* void */
		return _fds_getnum(type + 1);

	case FDS_AGGREGATE_START: /* aggregate */
		type++; /* past open brace */
		size = 0;
		subsize = 0;

		_fds_skipid(&type);
		
		while ( *type != FDS_AGGREGATE_END )
		{
			if ( *type == FDS_AGGREGATE_ACCESS_SEP )
			{
				type++;
				continue;
			}

			type = strchr(type, FDS_AGGREGATE_FIELD_NAME_END) + 1;

			/* to start of field */
			if ( (subsize = FDSTypeSize(type)) < 0 )
				return -1;

			size += subsize;

			_fds_advance(&type);

			if ( *type == FDS_AGGREGATE_FIELD_SEP )
				type++;
		}
		return size;

	case FDS_UNION_START:
		type++; /* past open brace */
		size = 0;
		subsize = 0;
		
		_fds_skipid(&type);
		
		while ( *type != FDS_UNION_END )
		{
			type = strchr(type, FDS_UNION_FIELD_NAME_END) + 1;

			/* to start of field */
			if ( (subsize = FDSTypeSize(type)) < 0 )
				return -1;

			if ( subsize > size )
				size = subsize;

			_fds_advance(&type);

			if ( *type == FDS_UNION_FIELD_SEP )
				type++;
		}
		return size;

	case FDS_ARRAY_START: /* array */
		if ( (s = FDSTypeSize(_fds_base_type(type))) < 0 )
			return -1;

		return s * _fds_array_size(type);

	case FDS_ENUM_START: /* enumeration */
		return 4; /* all enumerations are based on 4-byte integers */

	case FDS_RANGE: /* range */
		return FDSTypeSize(_fds_base_type(type));

	case FDS_REGION: /* region */
		rank = _fds_getnum(type + 1);
		s = FDSTypeSize(_fds_base_type(type));
		return rank * s * 2;

	case FDS_CHAR_POINTER:
	case FDS_STRING:
	default:
		return -1;
	}

	/* NOT REACHED */
}

int
FDSIsSigned(char *fds)
{
	if ( *fds++ == FDS_INTEGER )
		return (int)(*fds == FDS_INTEGER_SIGNED);

	return 0;
}

/*
 * Create an FDS type description. The result does not
 * need to be freed.
 *
 * Calls to TypeToFDS() may be nested as follows:
 * 
 *    TypeToFDS(AIF_ARRAY, 
 *      TypeToFDS(AIF_RANGE, 0, 10, 
 *        TypeToFDS(AIF_INTEGER, 4)
 *      ),
 *      TypeToFDS(AIF_INTEGER, 10)
 *   );
 *
 * TypeToFDS currently only allows two nested calls. If
 * an FDS type is created requiring more than two, this will
 * need to be increased.
 */
char *
TypeToFDS(int type, ...)
{
	va_list	args;
	int		v1;
	int		v2;
	char *	v3;
	char *	v4;
	char *	buf;

	if ( type < 0 || type >= NUM_AIF_TYPES )
		type = AIF_INVALID;

	va_start(args, type);

	switch ( type )
	{
	case AIF_INTEGER:
		v1 = va_arg(args, int);
		v2 = va_arg(args, int);
		asprintf(&buf, _fds_type_str[type], v1 ? FDS_INTEGER_SIGNED : FDS_INTEGER_UNSIGNED, v2);
		break;

	case AIF_FLOATING:
	case AIF_VOID:
	case AIF_REFERENCE:
		v1 = va_arg(args, int);
		asprintf(&buf, _fds_type_str[type], v1);
		break;

	case AIF_POINTER:
		v3 = va_arg(args, char *);
		v4 = va_arg(args, char *);
		asprintf(&buf, _fds_type_str[type], v3, v4);
		break;

	case AIF_FUNCTION:
		v3 = va_arg(args, char *);
		asprintf(&buf, _fds_type_str[type], v3);
		break;

	case AIF_ENUM:
		v3 = va_arg(args, char *);
		if (v3 == NULL) {
			v3 = "";
		}
		v1 = va_arg(args, int);
		asprintf(&buf, _fds_type_str[type], v3, v1 ? FDS_INTEGER_SIGNED : FDS_INTEGER_UNSIGNED);
		break;

	case AIF_CHAR_POINTER:
		v3 = va_arg(args, char *);
		asprintf(&buf, _fds_type_str[type], v3);
		break;

	case AIF_ADDRESS:
	case AIF_BOOLEAN:
		v1 = va_arg(args, int);
		asprintf(&buf, _fds_type_str[type], v1);
		break;

	case AIF_STRING:
	case AIF_CHARACTER:
		asprintf(&buf, _fds_type_str[type]);
		break;

	case AIF_AGGREGATE:
	case AIF_UNION:
		v3 = va_arg(args, char *);
		if (v3 == NULL) {
			v3 = "";
		}
		asprintf(&buf, _fds_type_str[type], v3);
		break;

	case AIF_RANGE:
		v1 = va_arg(args, int);
		v2 = va_arg(args, int);
		v3 = va_arg(args, char *);
		asprintf(&buf, _fds_type_str[type], v1, v2, v3);
		break;

	case AIF_ARRAY:
		v3 = va_arg(args, char *);
		v4 = va_arg(args, char *);
		asprintf(&buf, _fds_type_str[type], v3, v4);
		break;

	case AIF_NAME:
	case AIF_REGION:
		v1 = va_arg(args, int);
		v3 = va_arg(args, char *);
		if ( v3 == NULL ) {
			v3 = "";
		}
		asprintf(&buf, _fds_type_str[type], v1, v3);
		break;

	case AIF_INVALID:
	default:
		asprintf(&buf, _fds_type_str[AIF_INVALID], 0);
	}

	va_end(args);

	if (_fds_pos > MAX_CALLS) {
		_fds_pos = 0;
	}
	if (_fds_buf[_fds_pos] != NULL) {
		free(_fds_buf[_fds_pos]);
	}
	_fds_buf[_fds_pos++] = buf;

	return buf;
}

int
FDSTypeCompare(char *f1, char *f2)
{
	int		n;
	int		res;
	char *		n1;
	char *		n2;
	char *		t1;
	char *		t2;
	AIFIndex *	ix1;
	AIFIndex *	ix2;

	if ( *f1 == FDS_NAME )
	{
		f1 = _fds_skipto(f1, _fds_name_end);
		f1++;
	}

	if ( *f2 == FDS_NAME )
	{
		f2 = _fds_skipto(f2, _fds_name_end);
		f2++;
	}

	if ( FDSType(f1) != FDSType(f2) )
		return 0;

	switch ( FDSType(f1) )
	{
	case AIF_ENUM:
		res = strcmp(f1, f2) == 0;
		break;

	case AIF_INTEGER:
	case AIF_FLOATING:
	case AIF_VOID:
	case AIF_REGION:
	case AIF_ADDRESS:
	case AIF_BOOLEAN:
		res = FDSTypeSize(f1) == FDSTypeSize(f2);
		break;

	case AIF_POINTER:
	case AIF_FUNCTION:
		res = FDSTypeCompare(FDSBaseType(f1), FDSBaseType(f2));
		break;

	case AIF_REFERENCE:
		res = FDSType(f2) == FDSType(f1);
		break;

	case AIF_ARRAY:
		ix1 = FDSArrayIndexInit(f1);
		ix2 = FDSArrayIndexInit(f1);

		if ( ix1->i_rank != ix2->i_rank )
		{
			AIFArrayIndexFree(ix1);
			AIFArrayIndexFree(ix2);
			return 0;
		}

		for ( n = 0 ; n < ix1->i_rank ; n++ )
		{
			if ( ix1->i_size[n] != ix2->i_size[n] )
			{
				AIFArrayIndexFree(ix1);
				AIFArrayIndexFree(ix2);
				return 0;
			}
		}

		res = FDSTypeSize(ix1->i_btype) == FDSTypeSize(ix2->i_btype);

		AIFArrayIndexFree(ix1);
		AIFArrayIndexFree(ix2);
		break;

	case AIF_UNION:
	case AIF_AGGREGATE:
		if ( FDSNumFields(f1) != FDSNumFields(f2) )
		{
			res = 0;
			break;
		}

		for ( n = 0 ; n < FDSNumFields(f1) ; n++ )
		{
			if
			(
				FDSAggregateFieldByNumber(f1, n, &n1, &t1) < 0
				||
				FDSAggregateFieldByNumber(f2, n, &n2, &t2) < 0
			)
				return 0;

			_aif_free(n1);
			_aif_free(n2);

			if ( !FDSTypeCompare(t1, t2) )
			{
				_aif_free(t1);
				_aif_free(t2);
				return 0;
			}

			_aif_free(t1);
			_aif_free(t2);
		}

		res = 1;
		break;

	case AIF_CHAR_POINTER:
	case AIF_STRING:
	case AIF_CHARACTER:
		res = 1;
		break;

	default:
		res = 0;
	}

	return res;
}

/************************************************************
 ********************** ARRAY ROUTINES **********************
 ************************************************************/

/* extracts the index type of an array type, specified by the              */
/* type descriptor.                                                        */
/* index type descriptor is returned if a valid (not a NULL) pointer to    */
/* char is passed as parameter.                                            */
/* returns 0 if successful, -1 otherwise.                                  */

char *
FDSArrayIndexType(char *type)
{
	char *	p;
	char *	pi;
	char *	index;

	if
	(
		type == NULL 
		||
		*type == '\0'
		||
		*type != FDS_ARRAY_START
		||
		*++type != FDS_RANGE
	)
		return NULL;

	p = _fds_skipnum(++type);   /* skip MinValue */
	p += 2;                /* skip '..' */
	p = _fds_skipnum(p);        /* skip MaxValue */

	pi = p;
	p = _fds_skipto(p, _fds_array_end);
	*p = 0;

	index = strdup(pi);

	*p = FDS_ARRAY_END;

	return index;
}

int
_fds_array_min_index(char *type)
{
	if ( *++type != FDS_RANGE )
		return -1;

	return _fds_getnum(++type);
}

int
FDSArrayMinIndex(char *fds, int n)
{
	while ( FDSType(fds) == AIF_ARRAY && n > 0 )
	{
		fds = _fds_base_type(fds);
		n--;
	}

	return (n == 0) ? _fds_array_min_index(fds) : -1;
}

int
_fds_array_size(char *type)
{
	char *	p;

	if ( *++type != FDS_RANGE )
		return -1;

	p = _fds_skipnum(++type);	/* skip MinValue */
	p++;						/* skip ',' */

	return _fds_getnum(p);
}

/*
 * Number of elements in dimension n
 */
int
FDSArrayRankSize(char *fds, int n)
{
	while ( FDSType(fds) == AIF_ARRAY && n > 0 )
	{
		fds = _fds_base_type(fds);
		n--;
	}

	return (n == 0) ? _fds_array_size(fds) : -1;
}

/*
 * Calculate the number of dimensions of an array.
 */
int
FDSArrayRank(char *fds)
{
	int	d = 0;

	while ( FDSType(fds) == AIF_ARRAY )
	{
		fds = _fds_base_type(fds);
		d++;
	}

	return d;
}

/*
 * Calculate the total size of an array
 */
int
FDSArraySize(char *fds)
{
	int	num;
	int	size = 1;

	while ( FDSType(fds) == AIF_ARRAY )
	{
		num = _fds_array_size(fds);
		fds = _fds_base_type(fds);

		size *= num;
	}

	return size;
}

char *
FDSRangeInit(int min, int size)
{
	return strdup(TypeToFDS(AIF_RANGE, min, size, "is4"));
}

char *
FDSArrayInit(int min, int size, char *btype)
{
	char *	res;
	char *	range = FDSRangeInit(min, size);
	
	res = strdup(TypeToFDS(AIF_ARRAY, range, btype));
	_aif_free(range);
	return res;
}

/*
 * Parse array type descriptor and extract the minimum index value and number
 * of elements for each dimension.
 */
void
FDSArrayBounds(char *fds, int rank, int **min, int **size)
{
	int		i;
	int *		mn;
	int *		sz;

	*min = (int *)_aif_alloc(rank*sizeof(int));
	*size = (int *)_aif_alloc(rank*sizeof(int));

	mn = *min;
	sz = *size;

	for ( i = 0 ; i < rank ; i++) {
		*mn = _fds_array_min_index(fds);
		*sz = _fds_array_size(fds);

		fds = _fds_base_type(fds);

		mn++;
		sz++;
	}
}

/*
 * Extract info about an array. Returns the index type of the
 * first dimension (assumes all dimensions are the same type),
 * the element type of the array, and the number of dimensions
 * of the array. It is the responsibility of the caller to free
 * memory allocated to el.
 * 
 */
void
FDSArrayInfo(char *fds, int *rank, char **el, char **ix)
{
	int	d;

	if ( ix != NULL )
		*ix = FDSArrayIndexType(fds);

	d = 0;

	while ( FDSType(fds) == AIF_ARRAY )
	{
		fds = _fds_base_type(fds);
		d++;
	}

	*el = strdup(fds);
	*rank = d;
}

AIFIndex *
FDSArrayIndexInit(char *fmt)
{
	int		d;
	int		nel = 1;
	int		rank;
	char *		btype;
	AIFIndex *	ix;

	FDSArrayInfo(fmt, &rank, &btype, NULL);

	if ( rank == 0 )
		return (AIFIndex *)NULL;

	ix = (AIFIndex *)_aif_alloc(sizeof(AIFIndex));
	ix->i_finished = 0;
	ix->i_rank = rank;
	ix->i_btype = btype;
	ix->i_bsize = FDSTypeSize(ix->i_btype);
	ix->i_index = (int *)_aif_alloc(ix->i_rank * sizeof(int));

	FDSArrayBounds(fmt, ix->i_rank, &(ix->i_min), &(ix->i_size));

	for ( d = ix->i_rank - 1 ; d >= 0 ; d-- )
	{
		nel *= ix->i_size[d];
		ix->i_index[d] = ix->i_min[d];
	}

	ix->i_nel = nel;

	return ix;
}

/************************************************************
 ********************** AGGREGATE ROUTINES **********************
 ************************************************************/

/*
 * The fds of a aggregate has this format:
 *
 *  {name|entry,...;entry,...;entry,...;entry,...}
 *
 *  name is the name of the aggregate, or empty for an unnamed type
 *  each entry has the format "name=type"
 *  a aggregate comprises 4 sections separated by ';', corresponding
 *    to public, private, protected and hidden members 
 */

#define AGGREGATE_START(fds, res) \
	if (*(fds) == FDS_NAME) { \
		(fds) = _fds_skipto((fds), _fds_name_end); \
		(fds)++; \
	} \
	if ( *(fds++) != FDS_AGGREGATE_START ) \
		return (res); \
	while ( *(fds) != FDS_TYPENAME_END ) \
		(fds)++; \
	(fds)++; \
	if ( *(fds) == '\0' ) \
		return (res); \
	while ( *(fds) == FDS_AGGREGATE_ACCESS_SEP ) \
		(fds)++; \
	if ( *(fds) == FDS_AGGREGATE_END ) \
		return (res);

char *
_fds_skiptomatch(char *fds)
{
	char	ender = 0;

	/*
	** assert *fds == '{' or '['; find the matching '}' or ']'
	*/

	switch ( *fds )
	{
	case FDS_AGGREGATE_START:
		ender = FDS_AGGREGATE_END;
		break;

	case FDS_ARRAY_START:
		ender = FDS_ARRAY_END;
		break;

	case FDS_ENUM_START:
		ender = FDS_ENUM_END;
		break;

	case FDS_UNION_START:
		ender = FDS_UNION_END;
		break;
	}

	fds++;

	for ( ;; )
	{
		if ( *fds == ender )
			return fds;

		if
		(
			*fds == FDS_AGGREGATE_START
			|| 
			*fds == FDS_UNION_START 
			|| 
			*fds == FDS_ARRAY_START 
			|| 
			*fds == FDS_ENUM_START
		)
			fds = _fds_skiptomatch(fds);

		fds++;
	}
}

/*
 * The function ignores access specifiers
 */
char *
_fds_skiptofield(char *fds, int n)
{
	AGGREGATE_START(fds, NULL);

	while ( *fds != '\0' && *fds != FDS_AGGREGATE_END && n > 0 )
	{
		if ( *fds == FDS_AGGREGATE_FIELD_SEP )
			n--;
		else if ( *fds == FDS_AGGREGATE_ACCESS_SEP )
		{
			if 
			( 
			 	*(fds+1) != FDS_AGGREGATE_ACCESS_SEP
				&&
				*(fds+1) != FDS_AGGREGATE_END
			)
				n--;
		}
		else if
		(
			*fds == FDS_AGGREGATE_START
			|| 
			*fds == FDS_UNION_START 
			|| 
			*fds == FDS_ARRAY_START 
			|| 
			*fds == FDS_ENUM_START
		)
			fds = _fds_skiptomatch(fds);

		fds++;

	}

	if ( *fds == '\0' || *fds == FDS_AGGREGATE_END )
		return NULL;

	return fds;
}

/*
 * The function ignores access specifiers, ie: total number of fields
 */
int
FDSNumFields(char *fds)
{
	int	n = 0;

	if (*fds == FDS_NAME) 
	{
		fds = _fds_skipto(fds, _fds_name_end);
		fds++;
	}

	if ( *(fds++) != FDS_AGGREGATE_START )
		return -1;

	while ( *(fds) != FDS_TYPENAME_END )
		(fds)++;
	(fds)++;

	if ( *fds == '\0' )
		return -1;

	while ( *fds == FDS_AGGREGATE_ACCESS_SEP )
		(fds)++;

	if ( *fds == FDS_AGGREGATE_END )
		return 0;

	while ( *fds != '\0' && *fds != FDS_AGGREGATE_END )
	{
		if ( *fds == FDS_AGGREGATE_FIELD_SEP )
			n++;
		else if ( *fds == FDS_AGGREGATE_ACCESS_SEP )
		{
			if 
			( 
			 	*(fds+1) != FDS_AGGREGATE_ACCESS_SEP
				&&
				*(fds+1) != FDS_AGGREGATE_END
			)
				n++;
		}
		else if
		(
			*fds == FDS_AGGREGATE_START
			|| 
			*fds == FDS_UNION_START 
			|| 
			*fds == FDS_ARRAY_START 
			|| 
			*fds == FDS_ENUM_START
		)
			fds = _fds_skiptomatch(fds);

		fds++;
	}

	return n + 1;
}

/*
 * Returns a newly alloced string for name and type
 * The function ignores access specifiers
 */
int
FDSAggregateFieldByNumber(char *fds, int n, char **name, char **type)
{
	if ( (fds = _fds_skiptofield(fds, n)) == NULL )
		return -1;

        if ( (*name = _field_attribute(fds, NULL, _fds_aggregate_field_name_end)) == NULL )
		return -1;

        if ( (*type = _field_attribute(fds, _fds_aggregate_field_name_end, _fds_aggregate_field_end)) == NULL )
		return -1;

	return 0;
}

/*
 * Returns a newly alloced string for type
 * The function ignores access specifiers
 */
int
FDSAggregateFieldByName(char *fds, char *name, char **type)
{
	char *	nm;

	AGGREGATE_START(fds, -1);

	while ( *fds != '\0' && *fds != FDS_AGGREGATE_END ) {
		nm = fds;

		fds = _fds_skipto(fds, _fds_aggregate_field_name_end);

		if ( *fds == '\0' ) {
			return -1;
		}

		*fds = '\0'; /* temporarily */

		if ( strcmp(nm, name) == 0 ) {
			*fds = FDS_AGGREGATE_FIELD_NAME_END;

			if ( (*type = _field_attribute(nm,
							_fds_aggregate_field_name_end,
							_fds_aggregate_field_end)) == NULL ) {
				return -1;
			}

			return 0;
		}

		*fds = FDS_AGGREGATE_FIELD_NAME_END;

		fds = _fds_skipto(fds, _fds_aggregate_field_end);
		fds++;
	}

	return -1;
}

/*
 * Arrange the members of AIF aggregate defined by fds and data into a new_fds
 * and a new_data so that the FDS looks like fdsref
 *
 * If number_of_members of fdsref > number_of_members of fds 
 *	returns -1
 * Else
 * 	creates the new_fds and new_data accordingly and returns 0
 *
 * If fds got more members, the rest will be appended at the end, so
 * fdsref can be thought of as a prefix of fds.
 *
 * The function only works with the public section.
 * It does not change fdsref, fds and data.
 * It allocates the memory for new_fds and new_data
 */
int
_fds_aggregate_arrange(char *fdsref, char *fds, char *data, char **new_fds, char **new_data)
{
	char * ptr_fds = fds;
	char * ptr_data = data;
	int len_new_fds;
	int len_new_data;
	int number_fds = 0;	/* number_of_members of fds */
	int number_fdsref = 0;	/* number_of_members of fdsref */

	char * tmp;
	int n;

	char ** fds_members;
	char ** data_members;
	int * fds_members_len;
	int * data_members_len;
	int * fds_members_flag; /* to flag that the member has been copied */

	/* calculate the number_of_members of fds and fdsref
	** we do not use FDSNumFields() since we are only interested in the
	** public section */
	tmp = fds;
	AGGREGATE_START(tmp, -1);
	while ( *tmp != FDS_AGGREGATE_ACCESS_SEP )
	{
		tmp = strchr(tmp, FDS_AGGREGATE_FIELD_NAME_END) + 1;
		if
		(
                        *tmp == FDS_AGGREGATE_START || *tmp == FDS_UNION_START ||
                        *tmp == FDS_ARRAY_START || *tmp == FDS_ENUM_START
                )
                        tmp = _fds_skiptomatch(tmp) + 1;

		tmp = _fds_skipto(tmp, _fds_aggregate_field_end);
		number_fds++;
	}

	tmp = fdsref;
	AGGREGATE_START(tmp, -1);
	while ( *tmp != FDS_AGGREGATE_ACCESS_SEP )
	{
		tmp = strchr(tmp, FDS_AGGREGATE_FIELD_NAME_END) + 1;
		if
		(
                        *tmp == FDS_AGGREGATE_START || *tmp == FDS_UNION_START ||
                        *tmp == FDS_ARRAY_START || *tmp == FDS_ENUM_START
                )
                        tmp = _fds_skiptomatch(tmp) + 1;

		tmp = _fds_skipto(tmp, _fds_aggregate_field_end);
		number_fdsref++;
	}

	/* check for the number_of_member variables */
	if ( number_fdsref > number_fds )
		return -1;
	
	/* allocate new_fds and new_data */
	_fds_skip_data(&ptr_fds, &ptr_data);
	len_new_fds = ptr_fds - fds;
	len_new_data = ptr_data - data;
	if (*new_fds == NULL)
		*new_fds = _aif_alloc(len_new_fds);
	if (*new_data == NULL)
		*new_data = _aif_alloc(len_new_data);

	/* we divide fds and data into several fields/members
	** we store the pointers for easy retrieval */
	fds_members = (char **) _aif_alloc(sizeof(char *) * number_fds);
	fds_members_len = (int *) _aif_alloc(sizeof(int) * number_fds);
	data_members = (char **) _aif_alloc(sizeof(char *) * number_fds);
	data_members_len = (int *) _aif_alloc(sizeof(int) * number_fds);
	fds_members_flag = (int *) _aif_alloc(sizeof(int) * number_fds);

	ptr_fds = fds;
	ptr_data = data;
	AGGREGATE_START(ptr_fds, -1);
	for (n=0; n<number_fds; n++)
	{
		fds_members[n] = ptr_fds;
		data_members[n] = ptr_data;

		ptr_fds = strchr(ptr_fds, FDS_AGGREGATE_FIELD_NAME_END) + 1;
		_fds_skip_data(&ptr_fds, &ptr_data);
		fds_members_len[n] = ptr_fds - fds_members[n];
		data_members_len[n] = ptr_data - data_members[n];

		fds_members_flag[n] = 0;

		ptr_fds++;	
	}

	/* we start building up the new_fds and new_data */
	ptr_fds = fds;
	AGGREGATE_START(ptr_fds, -1);
	memcpy(*new_fds, fds, ptr_fds - fds); /* copy the id to the new_fds */

	ptr_fds = *new_fds + (ptr_fds - fds);
	ptr_data = *new_data;

	tmp = fdsref;
	AGGREGATE_START(tmp, -1);
	while ( *tmp != FDS_AGGREGATE_ACCESS_SEP )
	{
		char endchar, *end;

		if ( *tmp == FDS_AGGREGATE_FIELD_SEP ) tmp++;
		end = tmp;
		end = _fds_skipto(end, _fds_aggregate_field_end);
		endchar = *end; /* we store *end since *end can be ',' or ';' */
		*end = '\0'; /* temporarily */

		for (n=0; n<number_fds; n++)
		{
			/* we check the fds_members_flag so the function can
			** run faster for large data structures */
			if ( fds_members_flag[n] == 0 )
			{
				/* tmp and fds_members[*] contain
				** 'field_name=field_type', but we only want
				** to compare field_name */
				char * temp_a = strchr(tmp, FDS_AGGREGATE_FIELD_NAME_END);
				char * temp_b = strchr(fds_members[n], FDS_AGGREGATE_FIELD_NAME_END);
				int strcmp_res;
				*temp_a = *temp_b = '\0'; /* temporarily */
				strcmp_res = strcmp(tmp, fds_members[n]);
				*temp_a = *temp_b = FDS_AGGREGATE_FIELD_NAME_END;
				if (strcmp_res == 0)
					break;
			}
		}

		if (n == number_fds) /* not found, an error */
		{
			*end = endchar;
			_aif_free(fds_members);
			_aif_free(fds_members_len);
			_aif_free(fds_members_flag);
			_aif_free(data_members);
			_aif_free(data_members_len);
			_aif_free(*new_fds);
			_aif_free(*new_data);
			 return -1;
		}

		memcpy(ptr_data, data_members[n], data_members_len[n]);
		memcpy(ptr_fds, fds_members[n], fds_members_len[n]);
		fds_members_flag[n] = 1;
		ptr_data += data_members_len[n];
		ptr_fds += fds_members_len[n];
		*end = endchar;
		*(ptr_fds++) = FDS_AGGREGATE_FIELD_SEP;
		tmp = end;
	}

	/* copy members which have not been copied */
	if ( number_fds > number_fdsref )
	{
		for (n=0; n<number_fds; n++)
		{
			if ( fds_members_flag[n] == 0 )
			{
				memcpy(ptr_data, data_members[n], data_members_len[n]);
				memcpy(ptr_fds, fds_members[n], fds_members_len[n]);
				ptr_data += data_members_len[n];
				ptr_fds += fds_members_len[n];
				*(ptr_fds++) = FDS_AGGREGATE_FIELD_SEP;
			}
		}
	}

	/* copy the rest of the fds to the new_fds */
	ptr_fds--;
	memcpy(ptr_fds, fds_members[number_fds-1] + fds_members_len[number_fds-1], len_new_fds - (ptr_fds - *new_fds));

	_aif_free(fds_members);
	_aif_free(fds_members_len);
	_aif_free(fds_members_flag);
	_aif_free(data_members);
	_aif_free(data_members_len);
	return 0;
}

int
FDSAggregateFieldSize(char *fds, char *name)
{
	char *type;
	int   size;

	if ( FDSAggregateFieldByName(fds, name, &type) )
	{
                SetAIFError(AIFERR_BADARG, NULL);
                return -1;
	}

	size = FDSTypeSize(type);
	_aif_free(type);

	return size;
}

/*
 * The function ignores access specifiers
 * return value indicates the position of the field
 * the counter starts at 0, ie: first element is at position 0
 * if the field cannot be found, it returns -1
 */
int
FDSAggregateFieldIndex(char *fds, char *name)
{
	int	counter = 0;
	int	len;
	char *	nm;

	AGGREGATE_START(fds, -1);

	while ( *fds != '\0' && *fds != FDS_AGGREGATE_END )
	{
		nm = fds;

		fds = _fds_skipto(fds, _fds_aggregate_field_name_end);

		if ( *fds == '\0' )
			return -1;

		len = fds - nm;

		if ( strncmp(nm, name, len) == 0 )
			return counter;

		counter++;
		fds = _fds_skipto(fds, _fds_aggregate_field_end);
		fds++;
	}

	return -1;
}

char *
FDSAggregateInit(char *id)
{
	return strdup(TypeToFDS(AIF_AGGREGATE, id));
}

/*
 * returns the data length from field 0 until field n (inclusive)
 */
int
_data_len_index(char *fds, int n)
{
	int size = 0;
	int subsize = 0;

	AGGREGATE_START(fds, -1);
		
	while ( *fds != '\0' && *fds != FDS_AGGREGATE_END && n >= 0 )
	{
		if ( *fds == FDS_AGGREGATE_ACCESS_SEP )
		{
			fds++;
			continue;
		}

		fds = _fds_skipto(fds, _fds_aggregate_field_name_end);

		if ( *fds++ == '\0' )
			return 0;

		/* to start of field */
		if ( (subsize = FDSTypeSize(fds)) < 0 )
			return -1;

		size += subsize;

		n--;

		_fds_advance(&fds);

		if ( *fds == FDS_AGGREGATE_FIELD_SEP )
			fds++;
	}

	return size;
}

int
_data_len_public(char *fds)
{
	int size = 0;
	int subsize = 0;

	if (*(fds) == FDS_NAME) { 
        	(fds) = _fds_skipto((fds), _fds_name_end); 
        	(fds)++; 
	} 
	if ( *(fds++) != FDS_AGGREGATE_START )
        	return -1;

        while ( *(fds) != FDS_TYPENAME_END )
               	(fds)++; 
        (fds)++; 

	if ( *(fds) == '\0' ) 
        	return -1;
	if ( *(fds) == FDS_AGGREGATE_ACCESS_SEP )
        	return 0;
		
	while ( *fds != FDS_AGGREGATE_END )
	{
		if ( *fds == FDS_AGGREGATE_ACCESS_SEP )
			break;

		fds = _fds_skipto(fds, _fds_aggregate_field_name_end);

		if ( *fds++ == '\0' )
			return 0;

		/* to start of field */
		if ( (subsize = FDSTypeSize(fds)) < 0 )
			return -1;

		size += subsize;

		_fds_advance(&fds);

		if ( *fds == FDS_AGGREGATE_FIELD_SEP )
			fds++;
	}

	return size;
}

/*
 * returns a pointer to a newly allocated structure
 */
char *
_field_attribute(char *s, char *starter, char *ender)
{
	int	len;
	char *	np;
	char *	_fds_field_attr; /* we alloc as needed */

	if ( starter != NULL )
	{
		s = _fds_skipto(s, starter);

		if ( *s++ == '\0' )
			return NULL;
	}

	np = s;
	s = _fds_skipto(s, ender);

	if ( *s == '\0' )
		return NULL;

	len = s - np;

	_fds_field_attr = _aif_alloc(len+1);

	memcpy(_fds_field_attr, np, len);
	_fds_field_attr[len] = '\0';

	return _fds_field_attr;
}

#define ENUM_START(fds, res) \
	if (*(fds) == FDS_NAME) { \
		(fds) = _fds_skipto((fds), _fds_name_end); \
		(fds)++; \
	} \
	if ( *(fds++) != FDS_ENUM_START ) \
		return (res); \
	while ( *(fds) != FDS_TYPENAME_END ) \
		(fds)++; \
	(fds)++; \
	if ( *(fds) == '\0' || *(fds) == FDS_ENUM_END ) \
		return (res);

/* based on the fds and the enum value,
 * the function gives the enum name (string),
 * it allocs the string for the caller */
int
FDSEnumConstByValue(char *fds, char **name, int val)
{
	int	len;
	char *	nm;

	ENUM_START(fds, -1);

	while ( *fds != '\0' && *fds != FDS_ENUM_END )
	{
		nm = fds;

		fds = _fds_skipto(fds, _fds_enum_const_name_end);

		if ( *fds == '\0' )
			return -1;

		len = fds - nm;

		fds++;
		if ( val == _fds_getnum(fds) )
		{
			*name = _aif_alloc(len + 1);
			**name = '\0';
			strncat(*name, nm, len);
			
			return 0;
		}

		fds = _fds_skipto(fds, _fds_enum_const_end);
		fds++;
	}

	return -1;
}

/* based on the fds and the enum name (string),
 * the function gives the enum value */
int
FDSEnumConstByName(char *fds, char *name, int *val)
{
	int	len;
	char *	nm;

	ENUM_START(fds, -1);

	while ( *fds != '\0' && *fds != FDS_ENUM_END )
	{
		nm = fds;

		fds = _fds_skipto(fds, _fds_enum_const_name_end);

		if ( *fds == '\0' )
			return -1;

		len = fds - nm;

		if ( strncmp(nm, name, len) == 0 )
		{
			fds++;
                        *val = _fds_getnum(fds);

			return 0;
		}

		fds = _fds_skipto(fds, _fds_enum_const_end);
		fds++;
	}

	return -1;
}

char *
FDSEnumInit(char *id)
{
	return strdup(TypeToFDS(AIF_ENUM, id, 1));
}

char *
FDSAddConstToEnum(char *fds, char *name, int val)
{
	int	v;
	char *	nfmt;
	char *	end;
	char	field[BUFSIZ];

	if ( FDSEnumConstByName(fds, name, &v) == 0 )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return NULL;
	}

	/* XXX TODO
	if ( FDSEnumFieldByValue(fds, name, &nfmt) == 0 )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		_aif_free(nfmt);
		return NULL;
	}
	*/

	snprintf(field, BUFSIZ, "%s%c%d", name, FDS_ENUM_SEP, val);

	nfmt = (char *)_aif_alloc(strlen(fds) + strlen(field) + 2);
	*nfmt = '\0';

	strcpy(nfmt, fds);

	if
	(
		(end = strrchr(nfmt, FDS_ENUM_END)) == NULL
		||
		end - nfmt < 1
	)
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return NULL;
	}

	if ( *(end-1) != FDS_TYPENAME_END )
		*end++ = FDS_ENUM_CONST_SEP;

	sprintf(end, "%s%s", field, strrchr(fds, FDS_ENUM_END));

	ResetAIFError();

	return nfmt;
}

int
FDSEnumAdd(char **fds, char *name, int val)
{
	char * temp;

	temp = FDSAddConstToEnum(*fds, name, val);

	if ( temp == NULL )
		return -1;

	_aif_free(*fds);
	*fds = temp;

	return 0;
}

#define UNION_START(fds, res) \
	if (*(fds) == FDS_NAME) { \
		(fds) = _fds_skipto((fds), _fds_name_end); \
		(fds)++; \
	} \
	if ( *(fds++) != FDS_UNION_START ) \
		return (res); \
	while ( *(fds) != FDS_TYPENAME_END ) \
		(fds)++; \
	(fds)++; \
	if ( *(fds) == '\0' || *(fds) == FDS_UNION_END ) \
		return (res);

int
FDSUnionFieldByName(char *fds, char *name, char **type)
{
	int	len;
	char *	nm;

	UNION_START(fds, -1);

	while ( *fds != '\0' && *fds != FDS_UNION_END )
	{
		nm = fds;

		fds = _fds_skipto(fds, _fds_union_field_name_end);

		if ( *fds == '\0' )
			return -1;

		len = fds - nm;

		if ( strncmp(nm, name, len) == 0 )
		{
			fds++;
			nm = fds;
			fds = _fds_skipto(fds, _fds_union_field_end);

			if ( *fds == '\0' )
				return -1;

			len = fds - nm;
			*type = _aif_alloc(len+1);
			strncpy(*type, nm, len);
			(*type)[len] = '\0';

			return 0;
		}

		fds = _fds_skipto(fds, _fds_union_field_end);
		fds++;
	}

	return -1;
}

char *
FDSUnionInit(char *id)
{
	return strdup(TypeToFDS(AIF_UNION, id));
}

char *
FDSAddFieldToUnion(char *fds, char *name, char *type)
{
	char *	nfmt;
	char *	end;
	char	field[BUFSIZ];
	char *  dummy;

	if ( FDSUnionFieldByName(fds, name, &dummy) == 0 )
	{
		_aif_free(dummy);
		SetAIFError(AIFERR_BADARG, NULL);
		return NULL;
	}

	snprintf(field, BUFSIZ, "%s%c%s", name, FDS_UNION_FIELD_NAME_END, type);

	nfmt = (char *)_aif_alloc(strlen(fds) + strlen(field) + 2);
	*nfmt = '\0';

	strcpy(nfmt, fds);

	if
	(
		(end = strrchr(nfmt, FDS_UNION_END)) == NULL
		||
		end - nfmt < 1
	)
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return NULL;
	}

	if ( *(end-1) != FDS_TYPENAME_END )
		*end++ = FDS_UNION_FIELD_SEP;

	sprintf(end, "%s%c", field, FDS_UNION_END);

	ResetAIFError();

	return nfmt;
}

int
FDSUnionAdd(char **fds, char *name, char *type)
{
	char * temp;

	temp = FDSAddFieldToUnion(*fds, name, type);

	if ( temp == NULL ) 
		return -1;

	_aif_free(*fds);
	*fds = temp;

	return 0;
}

char *
FDSAddFieldToAggregate(char *fds, AIFAccess acc, char *name, char *type)
{
	char * 	temp;
	char *	nfmt;
	char *	end;
	char *  rest;

	if (FDSAggregateFieldByName(fds, name, &nfmt) == 0)
	{
		SetAIFError(AIFERR_BADARG, NULL);
		_aif_free(nfmt);
		return NULL;
	}

	nfmt = (char *)_aif_alloc(strlen(fds) + strlen(name) + strlen(type) +3);
	*nfmt = '\0';

	strcpy(nfmt, fds);

	temp = nfmt;

	if (*(fds) == FDS_NAME) { 
        	(fds) = _fds_skipto((fds), _fds_name_end); 
        	(fds)++; 
	} 

	if (*(temp) == FDS_NAME) {
        	(temp) = _fds_skipto((temp), _fds_name_end);
        	(temp)++;
	}

	if
	(
		(end = _fds_skipto(temp+1, _fds_aggregate_access_sep)) == NULL
		||
		end - temp < 1
	)
	{
		SetAIFError(AIFERR_BADARG, NULL);
		_aif_free(nfmt);
		return NULL;
	}

	rest = _fds_skipto(fds+1, _fds_aggregate_access_sep);

	switch ( acc )
	{
		case AIF_ACCESS_PACKAGE:
			end = _fds_skipto(++end, _fds_aggregate_access_sep);
			if ( end == NULL )
			{
				SetAIFError(AIFERR_BADARG, NULL);
				_aif_free(nfmt);
				return NULL;
			}
			rest = _fds_skipto(++rest, _fds_aggregate_access_sep);
			/* fall through */

		case AIF_ACCESS_PRIVATE:
			end = _fds_skipto(++end, _fds_aggregate_access_sep);
			if ( end == NULL )
			{
				SetAIFError(AIFERR_BADARG, NULL);
				_aif_free(nfmt);
				return NULL;
			}
			rest = _fds_skipto(++rest, _fds_aggregate_access_sep);
			/* fall through */

		case AIF_ACCESS_PROTECTED:
			end = _fds_skipto(++end, _fds_aggregate_access_sep);
			if ( end == NULL )
			{
				SetAIFError(AIFERR_BADARG, NULL);
				_aif_free(nfmt);
				return NULL;
			}
			rest = _fds_skipto(++rest, _fds_aggregate_access_sep);
			/* fall through */

		case AIF_ACCESS_PUBLIC:
		case AIF_ACCESS_UNKNOWN:
			break;
	}

	if ( *(end-1) != FDS_TYPENAME_END && *(end-1) != FDS_AGGREGATE_ACCESS_SEP )
		*end++ = FDS_AGGREGATE_FIELD_SEP;

	sprintf(end, "%s%c%s%s",
		name,
		FDS_AGGREGATE_FIELD_NAME_END,
		type,
		rest);

	ResetAIFError();

	return nfmt;
}

int
FDSAggregateAdd(char **fds, AIFAccess acc, char *name, char *type)
{
	char * temp;

	temp = FDSAddFieldToAggregate(*fds, acc, name, type);

	if ( temp == NULL )
		return -1;

	_aif_free(*fds);
	*fds = temp;

	return 0;
}

int
FDSSetIdentifier(char **fds, char *id)
{
	int	id_len;
	char  *	temp;
	char  *	new;

	temp = *fds;
	id_len = strlen(id);

	new = _aif_alloc( strlen(*fds) + strlen(id) + 1 );
	*new = '\0';

	if ( *temp == FDS_NAME ) 
	{
		temp = _fds_skipto(temp, _fds_name_end);
		temp++;
		strncat(new, *fds, temp - *fds);
	}

	if ( *temp == FDS_UNION_START || 
	     *temp == FDS_AGGREGATE_START ||
	     *temp == FDS_ENUM_START )
	{
		if ( *(temp+1) != FDS_TYPENAME_END )
		{
			_aif_free(new);
                	SetAIFError(AIFERR_BADARG, NULL);
			return -1;
		}

		strncat(new, temp, 1);
		strncat(new, id, id_len);
		strcat(new, temp+1);
		_aif_free(*fds);
		*fds = new;
		return 0;
	}
	else
	{
		_aif_free(new);
                SetAIFError(AIFERR_BADARG, NULL);
		return -1;
	}
}

char *
FDSGetIdentifier(char *fds)
{
	char *temp;
	char *id;
	int   id_len;

	if ( *fds == FDS_NAME ) 
	{
		fds = _fds_skipto(fds, _fds_name_end);
		fds++;
	}

	if ( *fds == FDS_UNION_START || 
	     *fds == FDS_AGGREGATE_START ||
	     *fds == FDS_ENUM_START )
	{
		fds++;

		if ( *fds == FDS_TYPENAME_END )
		{
			SetAIFError(AIFERR_BADARG, NULL);
			return NULL;
		}

		temp = fds;
		fds = strchr(fds, FDS_TYPENAME_END);

		id_len = fds - temp;
		id = _aif_alloc( id_len + 1 );
		*id = '\0';
		strncat(id, temp, id_len);
		return id;
	}
	else
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return NULL;
	}
}

void
_fds_advance(char **fds)
{
	switch ( FDSType((*fds)++) )
	{
	case AIF_ADDRESS:
	case AIF_BOOLEAN:
		*fds = _fds_skipnum(*fds);
		break;

	case AIF_ENUM:
		_fds_skipid(fds);

		*fds = _fds_skipto(*fds, _fds_enum_end);
		(*fds) += 2;
		/* fall through */

	case AIF_INTEGER:
		*fds += 1; /* u or s */
		*fds = _fds_skipnum(*fds);
		break;

	case AIF_FLOATING:
	case AIF_VOID:
		*fds = _fds_skipnum(*fds);
		break;

	case AIF_FUNCTION:
		while ( *(*fds)++ != FDS_FUNCTION_ARG_END )
			;

		_fds_advance(fds);
		break;

	case AIF_ARRAY:
		while ( *(*fds)++ != FDS_ARRAY_END )
			; /* past the index type */

		_fds_advance(fds); /* past the base type */
		break;

	case AIF_UNION:
	case AIF_AGGREGATE:
		_fds_skipid(fds);

		for ( ;; ) {

			if ( **fds == FDS_AGGREGATE_ACCESS_SEP )
			{
				(*fds)++;
				continue;
			}
			
			if ( **fds == FDS_AGGREGATE_END )
				break;

			while ( *(*fds)++ != FDS_AGGREGATE_FIELD_NAME_END )
				; /* past the field name */

			_fds_advance(fds); /* past the field type */

			if ( **fds == FDS_AGGREGATE_FIELD_SEP )
				(*fds)++;
		}
		(*fds)++; /* past closing brace */
		break;

	case AIF_NAME:
		*fds = _fds_skipnum(*fds);
		(*fds)++; /* past closing '/' */
		_fds_advance(fds); /* past the named type */
		break;

	case AIF_POINTER:
		_fds_advance(fds); /* past the address */
		_fds_advance(fds); /* past the pointed-to type */
		break;

	case AIF_CHAR_POINTER:
		_fds_advance(fds); /* past the address */
		break;

	case AIF_REFERENCE:
		*fds = _fds_skipnum(*fds);
		(*fds)++; /* past closing '/' */
		break;

	case AIF_CHARACTER:
	case AIF_STRING:
		break; /* already skipped all we should */

	default:
		fprintf(stderr, "no way to advance ... \"%s\"\n", *fds);
		break;
	}
}

/*
 * **fds is integer, float, or character.  Return how many bytes it
 * has and advance *fds to its end
 */
int
_fds_count_bytes(char **fds)
{
	int bytes;
	
	switch ( **fds )
	{
	case FDS_FLOATING:
		(*fds)++; /* past f */
		bytes = _fds_getnum(*fds);
		*fds = _fds_skipnum(*fds); /* past size indicator */
		break;

	case FDS_ENUM_START:
		*fds = _fds_skipto(++(*fds), _fds_enum_end);
		(*fds)++;
		/* fall through */	

	case FDS_INTEGER:
		*fds += 2; /* past iu or is */
		bytes = _fds_getnum(*fds);
		*fds = _fds_skipnum(*fds); /* past size indicator */
		break;

	case FDS_CHARACTER:
		(*fds)++; /* past c */
		bytes = 1;
		break;

	case FDS_BOOLEAN:
	case FDS_ADDRESS:
		(*fds)++; /* past b or a */
		bytes = _fds_getnum(*fds);
		*fds = _fds_skipnum(*fds); /* past size indicator */
		break;

	case FDS_VOID:
		(*fds)++; /* past v */
		bytes = _fds_getnum(*fds);
		*fds = _fds_skipnum(*fds); /* past size indicator */
		break;

	default:
		bytes = -1; /* internal error */
	}

	return bytes;
}

/*
 * Count bytes but don't advance pointer
 */
int
_fds_count_bytes_na(char **fds)
{
	char *	fmt = *fds;

	return _fds_count_bytes(&fmt);
}

void
_fds_resolve(char **fmt)
{
	int	index;

	while ( **fmt == FDS_NAME )
	{
		(*fmt)++;
		index = (int)strtol(*fmt, NULL, 10);
		*fmt = (char *)_fds_skipnum(*fmt)+1; /* past name, trailing slash */
		_aif_types_seen[index+MAX_TYPES_SEEN/2] = *fmt;
	}
}

char *
_fds_lookup(char **fmt)
{
	int	index;

	(*fmt)++;
	index = (int)strtol(*fmt, NULL, 10);
	*fmt = (char *)_fds_skipnum(*fmt)+1; /* past name, trailing slash */

	return _aif_types_seen[index+MAX_TYPES_SEEN/2];
}

/*
 * Find the target data for a pointer.
 *
 * @param fds format descriptor for pointer (need to calculate address size). At the
 * 		  end of the call, fds will be advanced to the beginning of the target type
 * @param data points to the data part of an AIF. At the end of the call, data
 *        will be advanced to the beggining of the target data
 * @param index a unique index for saving/retrieving values in order
 *        to simultaneously maintain targets for multiple AIFs.
 * @return a pointer to the data region where the target data actually
 *         starts, or 0 if null or invalid pointer
 *
 * Format of the data is:
 * 	<type>[<name>][<address>[<target>]]
 *
 * Where:
 *
 * <type> is a single byte:
 * 		0: null pointer, no data
 *  	1: ordinary pointer, data contains <address> and <target>
 *  	2: named pointer, data contains <name>, <address> and <target>
 *  	3: pointer reference, data contains <name>
 *  	4: invalid pointer, data contains <address>
 *  <name> is an unsigned 4 byte integer
 *  <address> is the address of the pointer
 *	<target> is the target data
 */

char *
_find_target(char **fds, char **data, int index)
{
	int		name;
	char	code = **data;
	char *	res;

	(*fds)++;	/* past the ^ */
	(*data)++;	/* past the code */

	switch ( code )
	{
	case AIF_PTR_NIL:
		_fds_advance(fds); /* skip address type */
		return (char *)0;

	case AIF_PTR_INVALID: /* invalid pointer value */
		_fds_skip_data(fds, data); /* skip address */
		return (char *)0;

	case AIF_PTR_NORMAL: /* normal pointer value */
		_fds_skip_data(fds, data); /* skip address */
		res = *data;
		return res;

	case AIF_PTR_NAME: /* named value */
		_ptrname_to_int(data, &name);
		_fds_skip_data(fds, data); /* skip address type */
		_aif_values_seen[name+index*MAX_VALUES_SEEN/2] = *data;
		return *data;

	case AIF_PTR_REFERENCE: /* reference to named value */
		_ptrname_to_int(data, &name);
		_fds_advance(fds); /* skip address type */
		return _aif_values_seen[name+index*MAX_VALUES_SEEN/2];
	}

	return 0; /* should never fall through */
}

/*
 * Get the pointer type.
 */
int
_get_pointer_type(char *data)
{
	return (int)(*data);
}

/*
 * Get the pointer name.
 */
int
_get_pointer_name(char *data)
{
	int name = 0;
	int type = _get_pointer_type(data);

	if (type == AIF_PTR_NAME || type == AIF_PTR_REFERENCE) {
		data++; /* skip code */
		_ptrname_to_int(&data, &name);
	}

	return name;
}

/* 
 * Skip over one full data unit, specified by the fds,
 * At return, fds should be at its end, and data is advanced.
 */

void
_fds_skip_data(char **fds, char **data)
{
	int		i;
	int		bytes;
	AIFIndex *	ix;
	char *		fmt;

	_fds_resolve(fds);

	switch ( FDSType(*fds) )
	{
	case AIF_REFERENCE:
		fmt = _fds_lookup(fds); // need a temporary copy
		_fds_skip_data(&fmt, data);
		return;

	case AIF_POINTER:
		if (_find_target(fds, data, 0) == 0)
			_fds_advance(fds);
		else
			_fds_skip_data(fds, data);
		return;

	case AIF_VOID:
	case AIF_CHARACTER:
	case AIF_INTEGER:
	case AIF_BOOLEAN:
	case AIF_FLOATING:
	case AIF_ADDRESS:
	case AIF_ENUM:
		(*data) += _fds_count_bytes(fds);
		return;

	case AIF_ARRAY:
		ix = FDSArrayIndexInit(*fds);

		_fds_advance(fds); /* skip over the entire array fds */

		for ( i = 0 ; i < ix->i_nel ; i++ )
		{
			fmt = ix->i_btype;
			_fds_skip_data(&fmt, data);
		}

		AIFArrayIndexFree(ix);
		return;

	case AIF_AGGREGATE:
		(*fds)++; /* past FDS_AGGREGATE_START */
		_fds_skipid(fds);

		while ( **fds != FDS_AGGREGATE_END )
		{
			if ( **fds == FDS_AGGREGATE_ACCESS_SEP )
			{
				(*fds)++;
				continue;
			}

			*fds = strchr(*fds, FDS_AGGREGATE_FIELD_NAME_END) + 1;
				/* to start of field */
			_fds_skip_data(fds, data);

			if ( **fds == FDS_AGGREGATE_FIELD_SEP )
				(*fds)++;
		}

		(*fds)++; /* past FDS_AGGREGATE_END */
		return;

	case AIF_UNION:
		(*fds)++; /* past FDS_UNION_START */
		_fds_skipid(fds);

		while ( **fds != FDS_UNION_END )
		{
			*fds = strchr(*fds, FDS_UNION_FIELD_NAME_END) + 1;
				/* to start of field */
			_fds_skip_data(fds, data);

			if ( **fds == FDS_UNION_FIELD_SEP )
				(*fds)++;
		}

		(*fds)++; /* past FDS_UNION_END */
		return;

	case AIF_CHAR_POINTER:
		(*fds)++; /* past p */
		_fds_skip_data(fds, data); /* past address */
		bytes = (*(*data)++ & 0xff) << 8;
		bytes += *(*data)++ & 0xff;
		*data += bytes;
		return;

	case AIF_STRING:
		(*fds)++; /* past s */
		bytes = (*(*data)++ & 0xff) << 8;
		bytes += *(*data)++ & 0xff;
		*data += bytes;
		return;

	case AIF_FUNCTION:
		while ( **data != '\0' )
			(*data)++;
		(*data)++; /* past null */
		return;
			
	default:
		SetAIFError(AIFERR_TYPE, NULL);
		return;
	}
}

void
_fds_skipid(char **fds)
{
	if (**fds != FDS_TYPENAME_END)
		*fds = strchr(*fds, FDS_TYPENAME_END);
	*fds += 1;
}

char *
_fds_add_function_arg(char *fds, char *arg)
{
	char * nfmt;
	char * temp;
	char * rest;

        if ( FDSType(fds) != AIF_FUNCTION )
        {
                SetAIFError(AIFERR_BADARG, NULL);
                return NULL;
        }

        nfmt = (char *)_aif_alloc(strlen(fds) + strlen(arg) + 2);
        *nfmt = '\0';

        strcpy(nfmt, fds);

        if ( (temp = _fds_skipto(nfmt, _fds_function_arg_end)) == NULL )
        {
                SetAIFError(AIFERR_BADARG, NULL);
                _aif_free(nfmt);
                return NULL;
        }

        rest = _fds_skipto(fds, _fds_function_arg_end);

        if ( temp != (nfmt + 1) )
                *temp++ = FDS_FUNCTION_ARG_SEP;

        sprintf(temp, "%s%s",
                arg,
                rest);

        ResetAIFError();

        return nfmt;
}



