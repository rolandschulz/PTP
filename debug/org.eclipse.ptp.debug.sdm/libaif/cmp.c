/*
 * Routines for comparing AIF objects.
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
#include	<math.h>

#include	"aif.h"
#include	"aiferr.h"
#include	"aifint.h"

int
AIFIsZero(AIF *a, int *val)
{
	char *	f;
	char *	d;

	if ( a == (AIF *)NULL || val == (int *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return -1;
	}

	f = AIF_FORMAT(a);
	d = AIF_DATA(a);

	return _aif_is_zero(&f, &d, AIF_LEN(a), val);
}

int
_aif_is_zero(char **fds, char **data, int len, int *val)
{
	int		i;
	int		nv;
	int		bytes;
	int		reg;
	int		success;
	char		c;
	char *		target;
	AIFIndex *	ix;
	char *		fmt;
	AIFDOUBLEST	v;

	if ( **fds == FDS_NAME )
	{
		*fds = _fds_skipto(*fds, "/");
		(*fds)++;
	}

	switch ( FDSType(*fds) )
	{
	case AIF_UNION:
		bytes = FDSTypeSize(*fds);
		*val = 1; /* zero unless shown otherwise */

		(*fds)++; /* past open brace */

		_fds_skipid(fds);

		while ( **fds != FDS_UNION_END )
		{
			*fds = strchr(*fds, FDS_UNION_FIELD_NAME_END) + 1; /* to start of field */

                	if ( (i = FDSTypeSize(*fds)) < 0 )
				return -1;
                	fmt = _aif_alloc( sizeof(char) * i );
                	target = fmt;
                	memcpy(target, *data, i);

			if ( _aif_is_zero(fds, &target, len, val) < 0 )
			{
				_aif_free(fmt);
				return -1;
			}

			if ( *val == 0 )
				return 0; /* zero res means no need to look further */
			if ( **fds == FDS_UNION_FIELD_SEP )
				(*fds)++;
		}

		*fds = strchr(*fds, FDS_STRUCT_END) + 1; /*past close brace*/
		*data += bytes;

		return 0;

	case AIF_ENUM:
		*fds = _fds_base_type(*fds);
		bytes = _fds_count_bytes(fds);
		*val = _aif_int_is_zero(*data, bytes);
		(*data) += bytes;

		return 0;

	case AIF_POINTER:
		reg = (**data < (char) AIF_PTR_REFERENCE); /* data not a reference */

		(*fds)++; /* past ^ */

		target = _find_target(data, 0);

		if ( target == 0 )
		{
			/*
			** null pointer.  We call it equivalent to 0
			*/
			*val = 1;
			_fds_advance(fds); /* skip over the target fds */
			return 0; /* but we fail to find it. */
		}

		success = _aif_is_zero(fds, &target, len, val);

		if ( reg )
			*data = target; /* otherwise _find_target advanced for us */
		return success;

	case AIF_CHARACTER:
		bytes = _fds_count_bytes(fds);

		if ( _aif_to_char(*data, &c) < 0 )
			return -1;

		*val = (c == '\0');
		(*data) += bytes;

		return 0;

	case AIF_INTEGER:
	case AIF_BOOLEAN: /* if it is FALSE, val = 1, if it is TRUE, val = 0 */
		bytes = _fds_count_bytes(fds);
		*val = _aif_int_is_zero(*data, bytes);
		(*data) += bytes;

		return 0;

	case AIF_FLOATING:
		bytes = _fds_count_bytes(fds);

		if ( _aif_to_doublest(*data, bytes, &v) < 0 )
			return -1;

		*val = (v == (AIFDOUBLEST)0.0L);
		(*data) += bytes;

		return 0;

	case AIF_ARRAY:
		ix = FDSArrayIndexInit(*fds);

		_fds_advance(fds); /* skip over the entire array fds */

		for ( i = 0 ; i < ix->i_nel ; i++ )
		{
			fmt = ix->i_btype;

			if ( _aif_is_zero(&fmt, data, ix->i_bsize, &nv) < 0 )
				return -1;

			if ( nv == 0 ) /* no need to look further */
			{
				*val = 0;
				return 0;
			}
		}

		AIFArrayIndexFree(ix);

		*val = 1;

		return 0;

	/*
	** only uses the public section in AIF_STRUCT 
	*/
	case AIF_STRUCT:
		*val = 1; /* zero unless shown otherwise */

		(*fds)++; /* past open brace */
		_fds_skipid(fds);

		while ( **fds != FDS_STRUCT_ACCESS_SEP )
		{
			*fds = strchr(*fds, FDS_STRUCT_FIELD_NAME_END) + 1; /* to start of field */

			if ( _aif_is_zero(fds, data, len, val) < 0 )
				return -1;

			if ( *val == 0 )
				return 0; /* zero res means no need to look further */
			if ( **fds == FDS_STRUCT_FIELD_SEP )
				(*fds)++;
		}

		*fds = strchr(*fds, FDS_STRUCT_END) + 1; /*past close brace*/

		return 0;

	case AIF_STRING:
		(*fds)++; /* past "s" */
		bytes = (*(*data)++ & 0xff) << 8;
		bytes += *(*data)++ & 0xff;
		
		*data += bytes;

		*val = (bytes == 0);

		return 0;

	default:
		SetAIFError(AIFERR_TYPE, NULL);
		return -1;
	}
}

int
_aif_int_is_zero(char *data, int len)
{
	int	i;

	for ( i = 0 ; i < len ; i++ )
		if ( (int)*data++ != 0 )
			return 0;

	return 1;
}

/*
 * Compare types of two AIF values. Return true if they are equivalent,
 * false if not.
 */
int
AIFTypeCompare(AIF *a1, AIF *a2)
{
	return FDSTypeCompare(AIF_FORMAT(a1), AIF_FORMAT(a2));
}

int
AIFCompare(int depth, AIF *a1, AIF *a2, int *res)
{
	char *	f1;
	char *	f2;
	char *	d1;
	char *	d2;

	if ( a1 == (AIF *)NULL || a2 == (AIF *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return -1;
	}

	f1 = AIF_FORMAT(a1);
	f2 = AIF_FORMAT(a2);
	d1 = AIF_DATA(a1);
	d2 = AIF_DATA(a2);

	return _aif_compare(depth, res, &f1, &d1, &f2, &d2);
}

int
_aif_cmp_int(int *res, char *d1, int l1, char *d2, int l2)
{
	int	i;
	char *	r1;
	char *	r2;

	r1 = (char *)_aif_alloc(l2);

	_aif_neg_int(r1, d2, l2);

	r2 = (char *)_aif_alloc(MAX(l1, l2));

	_aif_add_int(r2, d1, l1, r1, l2);

	_aif_free(r1);

	if ( *r2 & 0x80 )
	{
		/*
		** negative result => a < b
		*/
		_aif_free(r2);
		*res = -1;
		return 0;
	}

	/*
	** Check for zero
	*/

	for ( i = 0 ; i < MAX(l1, l2) ; i++ )
	{
		if ( r2[i] != 0x0 )
		{
			/*
			** positive result => a > b
			*/
			_aif_free(r2);
			*res = 1;
			return 0;
		}
	}

	/*
	** zero result => a = b
	*/
	_aif_free(r2);
	*res = 0;
	return 0;
}

int
_aif_cmp_float(int *res, char *d1, int l1, char *d2, int l2)
{
	AIFDOUBLEST	v1;
	AIFDOUBLEST	v2;

	if
	(
		_aif_to_doublest(d1, l1, &v1) < 0
		||
		_aif_to_doublest(d2, l2, &v2) < 0
	)
		return -1;

	if ( v1 < v2 )
		*res = -1;
	else if ( v1 > v2 )
		*res = 1;
	else
		*res = 0;

	return 0;
}

int
_aif_cmp_char(int *res, char *d1, char *d2)
{
	char	v1;
	char	v2;

	if
	(
		_aif_to_char(d1, &v1) < 0
		||
		_aif_to_char(d2, &v2) < 0
	)
		return -1;

	if ( v1 < v2 )
		*res = -1;
	else if ( v1 > v2 )
		*res = 1;
	else
		*res = 0;

	return 0;
}

/*
 * data points to the data part of an AIF, starting at the format byte.
 *  0 means null
 *  1 means ordinary pointer
 *  2 means ordinary pointer, named
 *  3 means refer to existing name
 *
 * We save any names we see.  We offset the index for saving/retrieving 
 * values by "offset" in order to simultaneously maintain targets for multiple 
 * AIFs.  We return a pointer to the data region where the value actually 
 * starts, or 0 if null.  We also advance the data pointer past the initial 
 * code (and name if present).
 */

char *
_find_target(char **data, int offset)
{
	int	name;
	char	code = **data;

	(*data)++; /* past the code */

	switch ( code )
	{
	case AIF_PTR_NIL:
		return (char) 0;

	case AIF_PTR_NORMAL: /* unadorned value */
		return *data;

	case AIF_PTR_NAME: /* named value */
		_ptrname_to_int(*data, &name);
		(*data) += 4; /* past the name */
		_aif_values_seen[name+offset] = *data;

		return *data;

	case AIF_PTR_REFERENCE: /* reference to named value */
		_ptrname_to_int(*data, &name);
		(*data) += 4; /* past the name */

		return _aif_values_seen[name+offset];
	}

	return 0; /* if we fall through, no target to be found */
}

/*
 * Compare two AIF values. 
 *
 * For numeric values, sets res to:
 *
 *      -1 if d1 < d2
 *       0 if d1 == d2
 *       1 if d1 > d2
 *
 * For other values, sets res to:
 *
 *       0 if d1 == d2
 *       1 if d1 != d2
 *
 * For array type, res is 0 if all elements are equal, 1 otherwise.
 * The parameters f1, d1, f2, d2 are advanced through their structures; on
 * return, they are in the position following the relevant parts.
 * The return value is negative if there is a type failure.
 */
int
_aif_compare(int depth, int *res, char **f1, char **d1, char **f2, char **d2)
{
	int		i;
	int		l3;
	int		success; /* value to return indicating type match */
	int		bytes1;
	int		bytes2; /* number of bytes in arithmetic data */
	char *		fmt;
	char *		d3 = NULL;
	AIFIndex *	ix1;
	AIFIndex *	ix2;
	AIFLONGEST	val1;
	AIFLONGEST	val2;

	/* these cmp_by_name, f2_x* and d2_x* are used by 'case AIF_STRUCT' */
	int	cmp_by_name = 0;
	char *	f2_x1;
	char *	f2_x2;
	char *	d2_x1;
	char *	d2_x2;

	ResetAIFError();

	/*
	** resolve names and references first
	*/
	_fds_resolve(f1);
	_fds_resolve(f2);

	if ( FDSType(*f1) == AIF_REFERENCE )
	{
		fmt = _fds_lookup(f1);
		return _aif_compare(depth, res, &fmt, d1, f2, d2);
	}

	if ( FDSType(*f2) == AIF_REFERENCE )
	{
		fmt = _fds_lookup(f2);
		return _aif_compare(depth, res, f1, d1, &fmt, d2);
	}

	switch ( FDSType(*f1) )
	{
	case AIF_POINTER:
		(*f1)++;

		if ( FDSType(*f2) == AIF_POINTER )
		{
			int	reg1 = (**d1 < (char) 3);
			int	reg2 = (**d2 < (char) 3);
			char *	target1 = _find_target(d1, 0);
			char *	target2 = _find_target(d2, MAX_VALUES_SEEN/2);

			(*f2)++;

			if ( depth == -1 )
			{
				*res = 0;

				if ( target1 == 0 )
					_fds_advance(f1);
				else if ( !reg1 )
					_fds_advance(f1);
				else
					_fds_skip_data(f1, d1);

				if ( target2 == 0 )
					_fds_advance(f2);
				else if ( !reg2 )
					_fds_advance(f2);
				else
					_fds_skip_data(f2, d2);
				
				return 0;
			}
			else if ( depth == 1 )
				depth = -1;
			else if ( depth > 1 )
				depth--;

			if ( !reg1 && !reg2 )
			{
				/*
				** both parameters refer to elsewhere in the
				** data.  To avoid infinite loop, we just call 
				** them equal.
				*/

				_fds_advance(f1);
				_fds_advance(f2);

				*res = 0;

				return 0;
			}
			else if ( reg1 ^ reg2 )
			{
				/*
				** only one parameter refers elsewhere.
				** Under our strict interpretation, the data 
				** are different.
				*/
				_fds_advance(f1);
				_fds_advance(f2);

				*res = 0;

				SetAIFError(AIFERR_CONV, NULL);

				return -1;
			}
			else if ( !target1 && !target2 )
			{
				/*
				** both d1 and d2 are null
				*/
				_fds_advance(f1);
				_fds_advance(f2);

				*res = 0;

				return 0;
			}
			else if ( (target1 == 0) ^ (target2 == 0) )
			{
				/* 
				** only one null l
				*/
				_fds_advance(f1);
				_fds_advance(f2);

				/*
				** fudge an answer
				*/
				*res = (target1 == 0) ? -1 : 1;

				/*
				** but it is a type mismatch
				*/
				SetAIFError(AIFERR_CONV, NULL);

				return -1;
			}

			/* 
			** both point to something real. 
			*/

			success = _aif_compare(depth, res, f1, &target1, f2, &target2);

			if ( reg1 )
				*d1 = target1;

			if ( reg2 )
				*d2 = target2;

			return success;
		}
		else
		{
			/*
			** incompatible types
			*/
			SetAIFError(AIFERR_CONV, NULL);
			*res = 1;
			return -1;
		}

	case AIF_UNION:
		bytes1 = FDSTypeSize(*f1);
		bytes2 = FDSTypeSize(*f2);

		if ( FDSType(*f2) != AIF_UNION )
		{
			SetAIFError(AIFERR_CONV, NULL);
			*res = 1;
			return -1;
		}

		(*f1)++; /* past open brace */
		(*f2)++; /* past open brace */
		_fds_skipid(f1);
		_fds_skipid(f2);

		*res = 0; /* first assumption, they are equal */

		while ( **f1 != FDS_UNION_END )
		{
			int size1, size2;
			char *temp1, *temp2, *data1, *data2;

			if ( **f2 == FDS_UNION_END )
			{ 
				/* 
				** f1 has more fields
				*/
				SetAIFError(AIFERR_CONV, NULL);

				*res = 1; /* in a sense, d1 > d2 */

				return -1;
			}

			*f1 = strchr(*f1, FDS_UNION_FIELD_NAME_END) + 1;
			*f2 = strchr(*f2, FDS_UNION_FIELD_NAME_END) + 1;
			
			size1 = FDSTypeSize(*f1);
			size2 = FDSTypeSize(*f2);
			temp1 = _aif_alloc( sizeof(char) * size1 );
			temp2 = _aif_alloc( sizeof(char) * size2 );
			data1 = temp1;
			data2 = temp2;
			memcpy(data1, *d1, size1);
			memcpy(data2, *d2, size2);

			success = _aif_compare(depth, res, f1, &data1, f2, &data2);
			_aif_free(temp1);
			_aif_free(temp2);

			/*
			** non-zero res means no need to look further
			*/
			if ( *res )
				return 0;

			if ( **f1 == FDS_UNION_FIELD_SEP )
				(*f1)++;

			if ( **f2 == FDS_UNION_FIELD_SEP )
				(*f2)++;
		}

		if ( **f2 != FDS_UNION_END )
		{
			/*
			** f2 has more fields 
			*/
			SetAIFError(AIFERR_CONV, NULL);

			*res = -1; /* in a sense, d1 < d2 */
			
			return -1;
		}

		*f1 = strchr(*f1, FDS_UNION_END) + 1; /*past close brace*/
		*f2 = strchr(*f2, FDS_UNION_END) + 1; /*past close brace*/
		*d1 += bytes1;
		*d2 += bytes2;

		return 0;

	case AIF_ENUM:
		switch ( FDSType(*f2) )
		{
		case AIF_ENUM:
			*f1 = _fds_base_type(*f1);
			*f2 = _fds_base_type(*f2);

			bytes1 = _fds_count_bytes(f1);
			bytes2 = _fds_count_bytes(f2);

			success = _aif_cmp_int(res, *d1, bytes1, *d2, bytes2);

			*d1 += bytes1;
			*d2 += bytes2;

			return success;

		default:
			SetAIFError(AIFERR_CONV, NULL);
			*res = 1;
			return -1;
		}
		break;

	case AIF_INTEGER:
		switch ( FDSType(*f2) )
		{
		case AIF_INTEGER:
			bytes1 = _fds_count_bytes(f1);
			bytes2 = _fds_count_bytes(f2);

			success = _aif_cmp_int(res, *d1, bytes1, *d2, bytes2);

			*d1 += bytes1;
			*d2 += bytes2;

			return success;

		case AIF_FLOATING:
			bytes1 = _fds_count_bytes(f1);
			l3 = _aif_int_to_aif_float(&d3, *d1, bytes1);
			*d1 += bytes1;

			if ( l3 < 0 )
			{
				*res = 1;
				return -1;
			}

			bytes2 = _fds_count_bytes(f2);
			success = _aif_cmp_float(res, d3, l3, *d2, bytes2);
			*d2 += bytes2;

			return success;

		default:
			SetAIFError(AIFERR_CONV, NULL);
			*res = 1;
			return -1;
		}
		break;

	case AIF_FLOATING:
		switch ( FDSType(*f2) )
		{
		case AIF_INTEGER:
			bytes2 = _fds_count_bytes(f2);
			l3 = _aif_int_to_aif_float(&d3, *d2, bytes2);
			*d2 += bytes2;

			if ( l3 < 0 )
			{
				*res = 1;
				return -1;
			}

			bytes1 = _fds_count_bytes(f1);
			success = _aif_cmp_float(res, *d1, bytes1, d3, l3);
			*d1 += bytes1;

			return success;
			
		case AIF_FLOATING:
			bytes1 = _fds_count_bytes(f1);
			bytes2 = _fds_count_bytes(f2);

			success = _aif_cmp_float(res, *d1, bytes1, *d2, bytes2);

			*d1 += bytes1;
			*d2 += bytes2;

			return success;

		default:
			SetAIFError(AIFERR_CONV, NULL);
			*res = 1;
			return -1;
		}
		break;

	case AIF_CHARACTER:
		switch ( FDSType(*f2) )
		{
		case AIF_CHARACTER:
			bytes1 = _fds_count_bytes(f1);
			bytes2 = _fds_count_bytes(f2);

			success = _aif_cmp_char(res, *d1, *d2);

			*d1 += bytes1;
			*d2 += bytes2;

			return success;

		default:
			SetAIFError(AIFERR_CONV, NULL);
			*res = 1;
			return -1;
		}
		break;

	case AIF_ARRAY:
		switch ( FDSType(*f2) )
		{
		case AIF_ARRAY:
			ix1 = FDSArrayIndexInit(*f1);
			ix2 = FDSArrayIndexInit(*f2);

			/* 
			** skip over the entire array fds
			*/
			_fds_advance(f1);
			_fds_advance(f2);

			*res = 0; /* first assumption, they are equal */

			if ( ix1->i_nel != ix2->i_nel )
			{
				SetAIFError(AIFERR_SIZE, NULL);
				AIFArrayIndexFree(ix1);
				AIFArrayIndexFree(ix2);

				*res = 1;
				return -1;
			}

			for ( i = 0 ; i < ix1->i_nel ; i++ )
			{
				char *	fmt1;
				char *	fmt2;

				fmt1 = ix1->i_btype;
				fmt2 = ix2->i_btype;

				success = _aif_compare(depth, res, &fmt1, d1, &fmt2, d2);
				if ( success < 0 )
				{
					AIFArrayIndexFree(ix1);
					AIFArrayIndexFree(ix2);
					/* Do not need to set 'res' here,
					 * It's already set from the call
					 * to _aif_compare()
					 */
					return -1;
				}

				if ( *res != 0 )
				{
					/* 
					** no need to continue
					*/
					AIFArrayIndexFree(ix1);
					AIFArrayIndexFree(ix2);
					return 0;
				}
			}

			AIFArrayIndexFree(ix1);
			AIFArrayIndexFree(ix2);

			return 0;

		default:
			SetAIFError(AIFERR_CONV, NULL);
			*res = 1;
			return -1;
		}

	/*
	** only uses the public section in AIF_STRUCT
	*/
	case AIF_STRUCT:
		/* 
		** structure equivalence: we ignore field names
		*/

		if ( FDSType(*f2) != AIF_STRUCT )
		{
			SetAIFError(AIFERR_CONV, NULL);
			*res = 1;
			return -1;
		}

		if ( AIFGetOption(AIFOPT_CMP_METHOD) == AIF_CMP_BY_NAME )
		{	
			cmp_by_name = 1;
			f2_x1 = *f2;
			d2_x1 = *d2;
			f2_x2 = NULL;
			d2_x2 = NULL;
			_fds_skip_data(&f2_x1, &d2_x1);
			if ( _fds_struct_arrange(*f1, *f2, *d2, &f2_x2, &d2_x2) < 0 )
			{
				SetAIFError(AIFERR_CONV, NULL);
				*res = 1;
				return -1;
			}

			*f2 = f2_x2; 
			*d2 = d2_x2;
			/* at the end, we assign f2_x1 to f2, d2_x1 to d2,
			** and we free f2_x2 and d2_x2 */
		}

		(*f1)++; /* past open brace */
		(*f2)++; /* past open brace */
		_fds_skipid(f1);
		_fds_skipid(f2);

		*res = 0; /* first assumption, they are equal */

		while ( **f1 != FDS_STRUCT_ACCESS_SEP )
		{
			if ( **f2 == FDS_STRUCT_ACCESS_SEP )
			{ 
				/* 
				** f1 has more fields
				*/
				SetAIFError(AIFERR_CONV, NULL);

				*res = 1; /* in a sense, d1 > d2 */

				if (cmp_by_name == 1)
				{
					_aif_free(f2_x2);
					_aif_free(d2_x2);
					*f2 = f2_x1;
					*d2 = d2_x1;
				}
				return -1;
			}

			*f1 = strchr(*f1, FDS_STRUCT_FIELD_NAME_END) + 1; /* to start of field */
			*f2 = strchr(*f2, FDS_STRUCT_FIELD_NAME_END) + 1; /* to start of field */

			success = _aif_compare(depth, res, f1, d1, f2, d2);

			if ( success < 0 )
			{
				if (cmp_by_name == 1)
				{
					_aif_free(f2_x2);
					_aif_free(d2_x2);
					*f2 = f2_x1;
					*d2 = d2_x1;
				}
				/* Do not need to set 'res' here,
				 * It's already set from the call
				 * to _aif_compare()
				 */
				return -1;
			}

			/*
			** non-zero res means no need to look further
			*/
			if ( *res )
			{
				if (cmp_by_name == 1)
				{
					_aif_free(f2_x2);
					_aif_free(d2_x2);
					*f2 = f2_x1;
					*d2 = d2_x1;
				}
				return 0;
			}

			if ( **f1 == FDS_STRUCT_FIELD_SEP )
				(*f1)++;

			if ( **f2 == FDS_STRUCT_FIELD_SEP )
				(*f2)++;
		}

		if ( **f2 != FDS_STRUCT_ACCESS_SEP )
		{
			/*
			** f2 has more fields 
			*/
			SetAIFError(AIFERR_CONV, NULL);

			*res = -1; /* in a sense, d1 < d2 */
			
			if (cmp_by_name == 1)
			{
				_aif_free(f2_x2);
				_aif_free(d2_x2);
				*f2 = f2_x1;
				*d2 = d2_x1;
			}
			return -1;
		}

		*f1 = strchr(*f1, FDS_STRUCT_END) + 1; /*past close brace*/
		*f2 = strchr(*f2, FDS_STRUCT_END) + 1; /*past close brace*/

		if (cmp_by_name == 1)
		{
			_aif_free(f2_x2);
			_aif_free(d2_x2);
			*f2 = f2_x1;
			*d2 = d2_x1;
		}
		return 0;

	case AIF_STRING:
		if ( FDSType(*f2) != AIF_STRING )
		{
			SetAIFError(AIFERR_CONV, NULL);
			*res = 1;
			return -1;
		}

		(*f1)++; /* past "s" */
		(*f2)++; /* past "s" */

		bytes1 = (*(*d1)++ & 0xff) << 8;
		bytes1 += *(*d1)++ & 0xff;
		bytes2 = (*(*d2)++ & 0xff) << 8;
		bytes2 += *(*d2)++ & 0xff;

		*res = strncmp(*d1, *d2, MIN(bytes1, bytes2));

		*d1 += bytes1;
		*d2 += bytes2;

		if ( *res )
			return 0;

		if ( bytes1 == bytes2 )
		{
			/* 
			** they are the same in length and contents
			*/
			return 0;
		}
		else
		{
			/*
			** they start the same, but one is longer
			*/
			*res = (bytes1 < bytes2) ? -1 : 1;
			return 0;
		}

	case AIF_BOOLEAN:
		bytes1 = _fds_count_bytes_na(f1);
		bytes2 = _fds_count_bytes_na(f2);

		switch ( FDSType(*f2) )
		{
		case AIF_BOOLEAN:
			_aif_to_longest(*d1, bytes1, &val1);
			_aif_to_longest(*d2, bytes2, &val2);

			if( val1 == val2)
				*res = 0;
			else
				*res = 1;
			
			*d1 += bytes1;
			*d2 += bytes2;

			_fds_advance(f1);
			_fds_advance(f2);

			return 0;

		default:
			SetAIFError(AIFERR_CONV, NULL);
			*res = 1;
			return -1;
		}

	case AIF_VOID:
		if ( FDSType(*f2) == AIF_VOID )
			*res = 0;
		else
			*res = 1;

		return 0;

	default:
		SetAIFError(AIFERR_TYPE, NULL);
		*res = 1;
		return -1;
	}
}

int
_aif_eps(char *lo_fds, char *lo_data, int lo_len, char *hi_fds, char *hi_data, int hi_len, char *a_fds, char *a_data, int a_len, int *val)
{
	int		ft;
	char *		fds_ref;	

	int		lo_i, hi_i, a_i;
	double		lo_d, hi_d, a_d;

	AIFLONGEST	lo_ll, hi_ll, a_ll;
	AIFDOUBLEST	lo_dd, hi_dd, a_dd;

	ResetAIFError();

	_fds_resolve(&a_fds);

	if ( FDSType(a_fds) == AIF_REFERENCE )
	{
		fds_ref = _fds_lookup(&a_fds);
		return _aif_eps(lo_fds, lo_data, lo_len, hi_fds, hi_data, hi_len, fds_ref, a_data, a_len, val);
	}

	switch ( ft = FDSType(a_fds) )
	{
	case AIF_BOOLEAN:

		if ( FDSType(lo_fds) != AIF_BOOLEAN )
		{	
			SetAIFError(AIFERR_BADARG, NULL);
		 	return -1;
		}

		if ( _aif_to_longest(lo_data, lo_len, &lo_ll) < 0 ||
		     _aif_to_longest(a_data, a_len, &a_ll) < 0 )
			return -1;

		lo_i = (int) lo_ll;
		hi_i = (int) hi_ll;
		a_i = (int) a_ll;

		if ( lo_i == 0 && a_i == 0 )
			*val = -1;
		else if ( lo_i == -1 && a_i == 0 )
			*val = 0;
		else if ( a_i == -1 )
			*val = 1;

		return 0;
		
	case AIF_INTEGER:
	case AIF_CHARACTER:
	case AIF_ENUM:
		
		if ( FDSType(lo_fds) != ft ||
		     FDSType(hi_fds) != ft )
		{	
			SetAIFError(AIFERR_BADARG, NULL);
	 		return -1;
		}

		if ( _aif_to_longest(lo_data, lo_len, &lo_ll) < 0 ||
		     _aif_to_longest(a_data, a_len, &a_ll) < 0 ||
		     _aif_to_longest(hi_data, hi_len, &hi_ll) < 0 )
			return -1;

		lo_i = (int) lo_ll;
		hi_i = (int) hi_ll;
		a_i = (int) a_ll;

		if ( a_i < lo_i )
			*val = -1;
		else if ( lo_i <= a_i && a_i <= hi_i )
			*val = 0;
		else if (a_i > hi_i )
			*val = 1;

		return 0;

	case AIF_STRING:

		if ( FDSType(lo_fds) != AIF_STRING ||
		     FDSType(hi_fds) != AIF_STRING )
		{	
			SetAIFError(AIFERR_BADARG, NULL);
	 		return -1;
		}

		if ( _aif_to_longest(lo_data + 2, lo_len - 2, &lo_ll) < 0 ||
		     _aif_to_longest(a_data + 2, a_len - 2, &a_ll) < 0 ||
		     _aif_to_longest(hi_data + 2, hi_len - 2, &hi_ll) < 0 )
			return -1;

		lo_i = (int) lo_ll;
		hi_i = (int) hi_ll;
		a_i = (int) a_ll;

		if ( a_i < lo_i )
			*val = -1;
		else if ( lo_i <= a_i && a_i <= hi_i )
			*val = 0;
		else if (a_i > hi_i )
			*val = 1;

		return 0;

	case AIF_FLOATING:
		
		if ( FDSType(lo_fds) != AIF_FLOATING ||
		     FDSType(hi_fds) != AIF_FLOATING )
		{	
			SetAIFError(AIFERR_BADARG, NULL);
	 		return -1;
		}

		if ( _aif_to_doublest(lo_data, lo_len, &lo_dd) < 0 ||
		     _aif_to_doublest(a_data, a_len, &a_dd) < 0 ||
		     _aif_to_doublest(hi_data, hi_len, &hi_dd) < 0 )
			return -1;

		lo_d = (int) lo_dd;
		hi_d = (int) hi_dd;
		a_d = (int) a_dd;

		if ( a_d < lo_d )
			*val = -1;
		else if ( lo_d <= a_d && a_d <= hi_d )
			*val = 0;
		else if (a_d > hi_d )
			*val = 1;

		return 0;

	case AIF_ARRAY:
		{
			int 		tmp, i;
			AIFIndex *	ix;

			char *		t_fmt;
			char *		t_dt1;
			char *		t_dt2;
			int		t_len;

			ix = FDSArrayIndexInit(a_fds);

			t_dt1 = a_data;

			for ( i = 0 ; i < ix->i_nel ; i++ )
			{
	
				t_fmt = ix->i_btype;
				t_dt2 = t_dt1;

				_fds_skip_data(&t_fmt, &t_dt1);
				t_len = t_dt1 - t_dt2;

				if ( _aif_eps(lo_fds, lo_data, lo_len, hi_fds, hi_data, hi_len, ix->i_btype, t_dt2, t_len, &tmp) < 0 )
				{
					AIFArrayIndexFree(ix);
					return -1;
				}

				if ( tmp == 0 || tmp == 1 )
				{
					AIFArrayIndexFree(ix);
					*val = tmp;
					return 0;
				}
			}

			AIFArrayIndexFree(ix);
			*val = -1;
			return 0;
		}

	/* only works with the public section in AIF_STRUCT */
	case AIF_STRUCT:
		{
			int 		tmp;

			char *		t_ft1;
			char *		t_ft2;
			char *		t_dt1;
			char *		t_dt2;
			int		t_len;

			a_fds++;
			_fds_skipid(&a_fds);

			t_dt1 = a_data;
			t_ft1 = a_fds;

			while ( *t_ft1 != ';' )
			{
	
				t_ft1 = strchr(t_ft1, FDS_STRUCT_FIELD_NAME_END) + 1;

				t_dt2 = t_dt1;
				t_ft2 = t_ft1;

				_fds_skip_data(&t_ft1, &t_dt1);
				t_len = t_dt1 - t_dt2;

				if ( _aif_eps(lo_fds, lo_data, lo_len, hi_fds, hi_data, hi_len, t_ft2, t_dt2, t_len, &tmp) < 0 )
				{
					return -1;
				}

				if ( tmp == 0 || tmp == 1 )
				{
					*val = tmp;
					return 0;
				}
			}

			*val = -1;
			return 0;
		}

	case AIF_POINTER:
		{
			char *	target = _find_target(&a_data, 0);
			int	tmp;

			a_fds++;
			a_len--;

			if ( _aif_eps(lo_fds, lo_data, lo_len, hi_fds, hi_data, hi_len, a_fds, target, a_len, &tmp) < 0 )
			{
				return -1;
			}
			
			*val = tmp;
			return 0;
		}

	default:
		SetAIFError(AIFERR_TYPE, NULL);
		return -1;
	}
}

/* AIFEPS
 *
 * return_value is in val
 *
 * AIF_BOOLEAN (type of lo & hi is AIF_BOOLEAN)
 * val = -1		if	!lo & !a
 * val = 0		if	lo & !a
 * val = 1		if 	a
 *
 * AIF_INTEGER, AIF_CHAR, AIF_ENUM, AIF_STRING (type of lo & hi is AIF_INTEGER)
 * val = -1		if	a < lo
 * val = 0		if	lo <= a <= hi
 * val = 1		if	a > hi
 *
 * AIF_FLOATING (type of lo & hi is AIF_FLOATING)
 * val = -1		if	a < lo
 * val = 0		if	lo <= a <= hi
 * val = 1		if	a > hi
 *
 * AIF_ARRAY (type of lo & hi is the base of AIF_ARRAY)
 * val = -1		if	eps(a[i]) == -1 for ALL i
 * val = 0		if	eps(a[i]) == 0 for ANY i
 * val = 1		if	eps(a[i]) == 1 for ANY i
 *
 * AIF_STRUCT (type of lo & hi is AIF_STRUCT)
 * val = -1		if	eps(element[i]) == -1 for ALL i
 * val = 0		if	eps(element[i]) == 0 for ANY i
 * val = 1		if	eps(element[i]) == 1 for ANY i
 *
 * AIF_POINTER (type of lo & hi is the base of AIF_ARRAY)
 * val = eps(a)		if	a = *p
 *
 * Other types
 * ERROR
 */

int
AIFEPS(AIF *lo, AIF *hi, AIF *a, int *val)
{
	return _aif_eps(AIF_FORMAT(lo), AIF_DATA(lo), AIF_LEN(lo), AIF_FORMAT(hi), AIF_DATA(hi), AIF_LEN(hi), AIF_FORMAT(a), AIF_DATA(a), AIF_LEN(a), val);
}


AIF *
AIFDiff(int depth, AIF *a1, AIF *a2)
{
        char *  rd;
        char *  rf;
        char *  f1;
        char *  f2;
        char *  d1;
        char *  d2;

        if ( a1 == (AIF *)NULL || a2 == (AIF *)NULL )
        {
                SetAIFError(AIFERR_BADARG, NULL);
                return NULL;
        }

        f1 = AIF_FORMAT(a1);
        f2 = AIF_FORMAT(a2);
        d1 = AIF_DATA(a1);
        d2 = AIF_DATA(a2);

        rf = NULL; /* we don't know result so let _aif_binary_op work it out */
        rd = NULL;
        //rd = _aif_alloc(MAX(AIF_LEN(a1), AIF_LEN(a2)));

        if ( _aif_diff(depth, &rf, &rd, &f1, &d1, &f2, &d2) < 0 )
                return (AIF *)NULL;

	return MakeAIF(rf, rd);
}

/*
 * Compare two AIF values. Returns an AIF value that represents the
 * 'difference' between the values.
 *
 * In the case of scalar values the difference is: val1 - val2
 * In the case of booleans it is TRUE if they are different
 * In the case of strings it is a string containing '1' in the positions
 *   where characters differ and '0' in positions where they are the same.
 * 
 * Complex objects can also be compared. In this case the above rules are
 * applied to each field or element in the object.
 */
int
_aif_diff(int depth, char **rf, char **rd,
	  	     char **f1, char **d1,
	  	     char **f2, char **d2)
{
	int	i;
	int	bytes1;
	int	bytes2;
	char *	fmt;
	int	datalen; /* to keep track of the length of the resultant data */
	int	currentlen; /* current data length */

	/* these cmp_by_name, f2_x* and d2_x* are used by 'case AIF_STRUCT' */
	int	cmp_by_name = 0;
	char *	f2_x1;
	char *	f2_x2;
	char *	d2_x1;
	char *	d2_x2;

	ResetAIFError();

	_fds_resolve(f1);
	_fds_resolve(f2);

	if ( FDSType(*f1) == AIF_REFERENCE )
	{
		fmt = _fds_lookup(f1);
		return _aif_diff(depth, rf, rd, &fmt, d1, f2, d2);
	}

	if ( FDSType(*f2) == AIF_REFERENCE )
	{
		fmt = _fds_lookup(f2);
		return _aif_diff(depth, rf, rd, f1, d1, &fmt, d2);
	}

	switch ( FDSType(*f1) )
	{
	case AIF_INTEGER:
	case AIF_FLOATING:
	case AIF_CHARACTER:
		return _aif_binary_op(AIFOP_SUB, rf, rd, f1, d1, f2, d2);

	/* returns FALSE if they are the same */	
	case AIF_BOOLEAN:
		bytes1 = _fds_count_bytes_na(f1);
		bytes2 = _fds_count_bytes_na(f2);

		switch ( FDSType(*f2) )
		{
		case AIF_BOOLEAN:
			{
			
				AIFLONGEST	val1;
				AIFLONGEST	val2;
				AIFLONGEST	val3;
			
				if ( *rd == NULL )
					*rd = (char *)_aif_alloc(MAX(bytes1, bytes2));

				_aif_to_longest(*d1, bytes1, &val1);
				_aif_to_longest(*d2, bytes2, &val2);
				val3 = val1 ^ val2;
				_longest_to_aif(rd, MAX(bytes1, bytes2), val3);

				if ( *rf == NULL )
					*rf = strdup(AIF_BOOLEAN_TYPE());

				*d1 += bytes1;
				*d2 += bytes2;

				_fds_advance(f1);
				_fds_advance(f2);
			}

			return 0;

		default:
			SetAIFError(AIFERR_CONV, NULL);
			return -1;
		}
		break;

	case AIF_ARRAY:
		{
			AIFIndex *	ix1;
			AIFIndex *	ix2;
			char *		fmt1;
			char *		fmt2;
			char *		fres = NULL;
			char *		dres = NULL;

			if ( FDSType(*f2) != AIF_ARRAY )
			{
				SetAIFError(AIFERR_TYPE, NULL);
				return -1;
			}

			ix1 = FDSArrayIndexInit(*f1);
			ix2 = FDSArrayIndexInit(*f2);

			if ( ix1->i_nel != ix2->i_nel )
			{
				SetAIFError(AIFERR_SIZE, NULL);
				AIFArrayIndexFree(ix1);
				AIFArrayIndexFree(ix2);
				return -1;
			}

			if ( *rd == NULL )
			{
				/* We allocate rd as big as d1.
				* Note: Since _aif_diff can produce result which
				* is bigger than its arguments, (eg: _aif_diff
				* with STRINGS and POINTERS), we also need to
				* keep track of the length of rd, and resize it
				* when it's not enough.
				*/

				char * temp1 = *f1;
				char * temp2 = *d1;
				int    temp3 = 0;

				_fds_skip_data(&temp1, &temp2);
				temp3 = temp2 - (*d1);
				*rd = (char *)_aif_alloc(temp3);
				datalen = temp3;
			}

			if ( *rf == NULL )
				*rf = strdup(*f1);

			_fds_advance(f1); /* skip over the entire array fds */
			_fds_advance(f2);

			/*
			** If nel[12] > MAX_ADDRESS then we are not going to be
			** able to address all the data in the array. What should
			** we do?
			*/

			currentlen = 0; /* start of our data result region */

			for ( i = 0 ; i < ix1->i_nel ; i++ )
			{
				char * tmp1;
				char * tmp2;

				if ( dres != NULL )
				{
					_aif_free(dres);
					dres = NULL;
				}

				if ( fres != NULL )
				{
					_aif_free(fres);
					fres = NULL;
				}

				fmt1 = ix1->i_btype;
				fmt2 = ix2->i_btype;

				if ( _aif_diff(depth, &fres, &dres, &fmt1, d1, &fmt2, d2) < 0 )
				{
					AIFArrayIndexFree(ix1);
					AIFArrayIndexFree(ix2);
					return -1;
				}

				AIFArrayIndexInc(ix1);
				AIFArrayIndexInc(ix2);

				tmp1 = fres;
				tmp2 = dres;
				_fds_skip_data(&tmp1, &tmp2);

				/* Is the *rd large enough? */
				if (datalen < (currentlen + (tmp2 - dres)))
				{
					*rd = _aif_resize(*rd, datalen + BUFSIZ);
					datalen += BUFSIZ;
				}

				memcpy(*rd + currentlen, dres, tmp2-dres);
				currentlen += (tmp2-dres);
			}

			{
				/* we need to modify 'rf' because it is
				 * possible that _aif_diff returns something
				 * that has a different fds than the operands
				 */

				char *t1, *t2;
				int len;

				t1 = _fds_base_type(*rf);

				/* if it is multi-dimensional array, move t1
				 * to the base type
				 */

				while ( *t1 == FDS_ARRAY_START )
					t1 = _fds_base_type(t1);

				/* we want to keep the FDS_NAME */
				while ( *t1 == FDS_NAME )
				{
					t1++;
					t1 = (char *)_fds_skipnum(t1)+1;
				}

				t2 = t1;
				_fds_advance(&t2);

				len = strlen(fres);
				memmove(t1, fres, len);
				t1 += len;

				/* this part is necessary because it
				 * is possible that the array is part of a
				 * larger structure (ie: we don't want to cut
				 * the rest of the fds)
				 */
				if ( *t2 != '\0' )
				{
					len = strlen(t2);
					memmove(t1, t2, len+1);
				}
				else
					*t1 = '\0';
			}

			_aif_free(fres);
			_aif_free(dres);

			fmt = *rf;
			_fds_advance(&fmt);
			*fmt = '\0';

			AIFArrayIndexFree(ix1);
			AIFArrayIndexFree(ix2);
		}

		return 0;

	/* only works with the public section in AIF_STRUCT */
	case AIF_STRUCT:
		if ( FDSType(*f2) != AIF_STRUCT )
		{
			SetAIFError(AIFERR_TYPE, NULL);
			return -1;
		}

		if ( AIFGetOption(AIFOPT_CMP_METHOD) == AIF_CMP_BY_NAME )
		{	
			cmp_by_name = 1;
			f2_x1 = *f2;
			d2_x1 = *d2;
			f2_x2 = NULL;
			d2_x2 = NULL;
			_fds_skip_data(&f2_x1, &d2_x1);
			if ( _fds_struct_arrange(*f1, *f2, *d2, &f2_x2, &d2_x2) < 0 )
			{
				SetAIFError(AIFERR_CONV, NULL);
				return -1;
			}
			*f2 = f2_x2; 
			*d2 = d2_x2;
			/* at the end, we assign f2_x1 to f2, d2_x1 to d2,
			** and we free f2_x2 and d2_x2 */
		}

		if ( *rd == NULL )
		{
			/* We allocate rd as big as d1.
			* Note: Since _aif_diff can produce result which
			* is bigger than its arguments, (eg: _aif_diff
			* with STRINGS and POINTERS), we also need to
			* keep track of the length of rd, and resize it
			* when it's not enough.
			*/

			char * temp1 = *f1;
			char * temp2 = *d1;
			int    temp3 = 0;

			_fds_skip_data(&temp1, &temp2);
			temp3 = temp2 - (*d1);
			*rd = (char *)_aif_alloc(temp3);
			datalen = temp3;
		}

		if ( *rf == NULL )
			*rf = strdup(*f1);

		currentlen = 0; /* start of our data result region */

		(*f1)++; /* past open brace */
		(*f2)++; /* past open brace */
		_fds_skipid(f1);
		_fds_skipid(f2);

		while ( **f1 != ';' )
		{ /* do one field */

			char * dres = NULL;
			char * fres = NULL;
			char * tmp1;
			char * tmp2;

			if (**f2 == ';') { /* f1 has more fields */
				SetAIFError(AIFERR_TYPE, NULL);
				if (cmp_by_name == 1)
				{
					_aif_free(f2_x2);
					_aif_free(d2_x2);
					*f2 = f2_x1;
					*d2 = d2_x1;
				}
				return -1;
			}

			*f1 = strchr(*f1, FDS_STRUCT_FIELD_NAME_END) + 1; /* to start of field*/
			*f2 = strchr(*f2, FDS_STRUCT_FIELD_NAME_END) + 1; /* to start of field*/

			fmt = *f1;

			if ( _aif_diff(depth, &fres, &dres, f1, d1, f2, d2) < 0 )
			{
				if (cmp_by_name == 1)
				{
					_aif_free(f2_x2);
					_aif_free(d2_x2);
					*f2 = f2_x1;
					*d2 = d2_x1;
				}
				return -1; /* non-zero res means no need to look further */
			}

			tmp1 = fres;
			tmp2 = dres;
			_fds_skip_data(&tmp1, &tmp2);

			/* Is the *rd large enough? */
			if (datalen < (currentlen + (tmp2 - dres)))
			{
				*rd = _aif_resize(*rd, datalen + BUFSIZ);
				datalen += BUFSIZ;
			}

			memcpy(*rd + currentlen, dres, tmp2-dres);
			currentlen += (tmp2-dres);

			{
				char *t1, *t2;
				int len;

				t1 = strstr(*rf, fmt);
				t2 = t1;
				_fds_advance(&t2);

				len = strlen(fres);
				memmove(t1, fres, len);
				t1 += len;

				if ( t1 != t2 )
				{
					len = strlen(t2);
					memmove(t1, t2, len+1);
				}
			}

			_aif_free(fres);
			_aif_free(dres);

			if ( **f1 == ',' )
				(*f1)++;

			if ( **f2 == ',' )
				(*f2)++;
		} /* one field */

		if ( **f2 != ';' )
		{ /* f2 has more fields */
			if (cmp_by_name == 1)
			{
				_aif_free(f2_x2);
				_aif_free(d2_x2);
				*f2 = f2_x1;
				*d2 = d2_x1;
			}
			SetAIFError(AIFERR_TYPE, NULL);
			return -1;
		}

		fmt = *rf;
		_fds_advance(&fmt);
		*fmt = '\0';

		*f1 = strchr(*f1, '}') + 1; /* to start of field*/
		*f2 = strchr(*f2, '}') + 1; /* to start of field*/

		if (cmp_by_name == 1)
		{
			_aif_free(f2_x2);
			_aif_free(d2_x2);
			*f2 = f2_x1;
			*d2 = d2_x1;
		}
		return 0;

	case AIF_POINTER:
		if ( FDSType(*f2) != AIF_POINTER )
		{
			SetAIFError(AIFERR_TYPE, NULL);
			return -1;
		}
		else
		{
			int    ret;
			int    len;
			int    fds_type;
			char * tmp1;
			char * tmp2;
			char * ptr_type_1 = *d1;
			char * ptr_type_2 = *d2;

			char * target1 = _find_target(d1, 0);
			char * target2 = _find_target(d2, MAX_VALUES_SEEN/2);

			char * fres;
			char * dres;

			if ( depth == -1 )
			{
				if ( *rf == NULL )
				{
					tmp2 = (target1 == 0) ? *f1 : *f2;

					tmp1 = tmp2;
					_fds_advance(&tmp1);
					len = tmp1 - tmp2;
					*rf = _aif_alloc(sizeof(char)*len + 1);
					strncpy(*rf, tmp2, len);
					(*rf)[len] = '\0';
				}

				if ( *rd != NULL )
					_aif_free(*rd);
				*rd = _aif_alloc(1);
				**rd = (char) 0;

				(*f1)++;
				(*f2)++;

				if ( target1 == 0 ||
				     *ptr_type_1 == AIF_PTR_REFERENCE )
					_fds_advance(f1);
				else
					_fds_skip_data(f1, d1);

				if ( target2 == 0 ||
				     *ptr_type_2 == AIF_PTR_REFERENCE )
					_fds_advance(f2);
				else
					_fds_skip_data(f2, d2);

				return 0;

			}
			else if ( depth == 1 )
				depth = -1;
			else if ( depth > 1 )
				depth--;


			if ( target1 == 0 && target2 == 0 ) /* if it is NULL */
			{
				if ( *rf == NULL )
				{
					tmp2 = *f1;
					tmp1 = tmp2;
					_fds_advance(&tmp1);
					len = tmp1 - tmp2;
					*rf = _aif_alloc(sizeof(char)*len + 1);
					strncpy(*rf, tmp2, len);
					(*rf)[len] = '\0';
				}

				if ( *rd != NULL )
					_aif_free(*rd);
				*rd = _aif_alloc(1);
				**rd = (char) 0;

				_fds_advance(f1);
				_fds_advance(f2);

				return 0;
			}

			if ( *ptr_type_1 == AIF_PTR_REFERENCE ||
			     *ptr_type_2 == AIF_PTR_REFERENCE )
			{
				if ( *rf == NULL )
				{
					tmp2 = (*ptr_type_1==AIF_PTR_REFERENCE)?						 	*f1 : *f2;

					tmp1 = tmp2;
					_fds_advance(&tmp1);
					len = tmp1 - tmp2;
					*rf = _aif_alloc(sizeof(char)*len + 1);
					strncpy(*rf, tmp2, len);
					(*rf)[len] = '\0';
				}

				if ( *rd != NULL )
					_aif_free(*rd);
				*rd = _aif_alloc(5); /* 1 + 4 (ptr name) */

				if (*ptr_type_1 == AIF_PTR_REFERENCE)
				{
					int i;
					for ( i = 0; i < 5; i++) (*rd)[i] = ptr_type_1[i];

					_fds_advance(f1);

					/*
					** if it is null or a PTR_REF, just
					** advance the fds, since we've
					** skipped the data with _find_target
					*/
					if ( target2 == 0 || *ptr_type_2 == AIF_PTR_REFERENCE)
						_fds_advance(f2);
					else
					{
						(*f2)++;
						_fds_skip_data(f2, d2);
					}

				}
				else
				{
					int i;
					for ( i = 0; i < 5; i++) (*rd)[i] = ptr_type_2[i];

					_fds_advance(f2);

					/*
					** if it is null or a PTR_REF, just
					** advance the fds, since we've
					** skipped the data with _find_target
					*/
					if ( target1 == 0 || *ptr_type_1 == AIF_PTR_REFERENCE)
						_fds_advance(f1);
					else
					{
						(*f1)++;
						_fds_skip_data(f1, d1);
					}
				}

				return 0;
			}

			if ( target1 == 0 )	/* target1 is null, make 
						target2 as the result */
			{
				if ( *rf == NULL )
				{
					tmp2 = *f2;
					tmp1 = tmp2;
					_fds_advance(&tmp1);
					len = tmp1 - tmp2;
					*rf = _aif_alloc(sizeof(char)*len + 1);
					strncpy(*rf, tmp2, len);
					(*rf)[len] = '\0';
				}

				if ( *rd != NULL )
					_aif_free(*rd);

				{
					char * _d1;
					char * _d2;

					_d2 = ptr_type_2;
					_d1 = _d2;
					tmp2 = *f2;
					tmp1 = tmp2;
					_fds_skip_data(&tmp1, &_d1);
					len = _d1 - _d2;
					*rd = _aif_alloc(sizeof(char)*len);
					memcpy(*rd, _d2, len);
				}

				_fds_advance(f1);
				(*f2)++;
				_fds_skip_data(f2, d2);

				return 0;
			}

			if ( target2 == 0 )	/* target2 is null, make
						target1 as the result */
			{
				if ( *rf == NULL )
				{
					tmp2 = *f1;
					tmp1 = tmp2;
					_fds_advance(&tmp1);
					len = tmp1 - tmp2;
					*rf = _aif_alloc(sizeof(char)*len + 1);
					strncpy(*rf, tmp2, len);
					(*rf)[len] = '\0';
				}

				if ( *rd != NULL )
					_aif_free(*rd);

				{
					char * _d1;
					char * _d2;

					_d2 = ptr_type_1;
					_d1 = _d2;
					tmp2 = *f1;
					tmp1 = tmp2;
					_fds_skip_data(&tmp1, &_d1);
					len = _d1 - _d2;
					*rd = _aif_alloc(sizeof(char)*len);
					memcpy(*rd, _d2, len);
				}

				_fds_advance(f2);
				(*f1)++;
				_fds_skip_data(f1, d1);

				return 0;
			}

			(*f1)++;
			(*f2)++;

			if ( *rf == NULL )
			{
				*rf = _aif_alloc(BUFSIZ);
				**rf = '\0';
				strcat(*rf, "^");
			}

			if ( *rd != NULL )
				_aif_free(*rd);
			*rd = (char *)_aif_alloc(BUFSIZ);

			if ( *ptr_type_1 == AIF_PTR_NAME )
			{
				int i;
				for ( i = 0; i < 5; i++) (*rd)[i] = ptr_type_1[i];
			}
			else
				**rd = (char) AIF_PTR_NORMAL;

			dres = NULL;
			fres = NULL;

			fds_type = FDSType(*f1);
			tmp1 = *f1;

			ret = _aif_diff(depth, &fres, &dres, f1, d1, f2, d2);

			{
				char * ff = fres;
				char * dd = dres;

				_fds_skip_data(&ff, &dd);

				if ( *ptr_type_1 == AIF_PTR_NAME )
					memcpy((*rd)+5, dres, dd-dres);
				else
					memcpy((*rd)+1, dres, dd-dres);

				if ( fds_type == AIF_REFERENCE )
				{
					memcpy((*rf)+1, tmp1, *f1-tmp1);
					(*rf)[1 + *f1 - tmp1] = '\0';
				}
				else if ( fds_type == AIF_NAME )
				{
					tmp2 = tmp1;
					tmp2++;
					tmp2 = _fds_skipnum(tmp2);
					tmp2++;

					memcpy((*rf)+1, tmp1, tmp2-tmp1);
					(*rf)[1 + tmp2 - tmp1] = '\0';

					strncat((*rf), fres, ff-fres+1);
				}
				else
					strncat((*rf), fres, ff-fres+1);
			}

			return ret;

		}

	case AIF_STRING:
		if ( FDSType(*f2) != AIF_STRING )
		{
			SetAIFError(AIFERR_TYPE, NULL);
			return -1;
		}

		(*f1)++; /* past "s" */
		(*f2)++; /* past "s" */

		bytes1 = (*(*d1)++ & 0xff) << 8;
		bytes1 += *(*d1)++ & 0xff;
		bytes2 = (*(*d2)++ & 0xff) << 8;
		bytes2 += *(*d2)++ & 0xff;

		if ( *rd != NULL )
			_aif_free(*rd);

		*rd = _aif_alloc(bytes1 + bytes2 + 2);

		if ( bytes1 == bytes2 )
		{
			int i;
			int is_equal = 1;
			
			(*rd)[0] = (bytes1 >> 8) & 0xff;
                        (*rd)[1] = bytes1 & 0xff;

                        for (i=0; i<bytes1; i++)
			{
				if ( strncmp((*d1)+i, (*d2)+i, 1) )
				{
					(*rd)[2+i] = '1';
					is_equal = 0;
				}
				else
					(*rd)[2+i] = '0';
			}

			if ( is_equal == 1 )
			{
				(*rd)[0] = (0 >> 8) & 0xff;
                        	(*rd)[1] = 0 & 0xff;
			}
		}
		else if ( bytes1 > bytes2 )
		{
                        int i;

                        (*rd)[0] = (bytes1 >> 8) & 0xff;
                        (*rd)[1] = bytes1 & 0xff;

                        for ( i = 0 ; i < bytes2 ; i++ )
                                (*rd)[2+i] = strncmp((*d1)+i, (*d2)+i, 1) ? '1' : '0';
                        for ( i = 0 ; i < bytes1 - bytes2 ; i++ )
                                (*rd)[bytes2+2+i] = '1';
			
		}
                else if ( bytes1 < bytes2 )
                {
                        int i;

                        (*rd)[0] = (bytes2 >> 8) & 0xff;
                        (*rd)[1] = bytes2 & 0xff;

                        for ( i = 0 ; i < bytes1 ; i++ )
                                (*rd)[2+i] = strncmp((*d1)+i, (*d2)+i, 1) ? '1' : '0';
                        for ( i = 0 ; i < bytes2 - bytes1 ; i++ )
                                (*rd)[bytes1+2+i] = '1';

                }

		*d1 += bytes1;
		*d2 += bytes2;

		if ( *rf == NULL )
			*rf = strdup(AIF_STRING_TYPE());

		return 0;

	default:
		SetAIFError(AIFERR_TYPE, NULL);
		return -1;
	}
}







