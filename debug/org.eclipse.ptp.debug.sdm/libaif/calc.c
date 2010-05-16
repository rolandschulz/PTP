/*
 * Routines that support arithmetic/logical operations on
 * AIF objects.
 *
 * Copyright (c) 1996-2002 by Guardsoft Pty Ltd.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The _aif_div_int() routine was derived from source under
 * the following copyright:
 *
 *   Use of this program, for any purpose, is granted the author,
 *   Ian Kaplan, as long as this copyright notice is included in
 *   the source code or any source code derived from this program.
 *   The user assumes all responsibility for using this code.
 *
 */

#ifdef HAVE_CONFIG_H
#include	<config.h>
#endif /* HAVE_CONFIG_H */

#include	<stdio.h>
#include	<string.h>

#include	"aif.h"
#include	"aiferr.h"
#include	"aifint.h"

#if defined(WINDOWSNT) || defined(WIN32)
#define BITSPERBYTE     8
#elif defined(__APPLE__)
#include        <sys/types.h>
#define BITSPERBYTE     NBBY
#else
#include        <values.h>
#endif /* WINDOWSNT || WIN32 */

/*
 * Boolean bitwise and.
 */
void
_aif_and_bool(char *rd, char *d1, int l1, char *d2, int l2)
{
	AIFLONGEST	b1;
	AIFLONGEST	b2;
	AIFLONGEST	res;

	_aif_to_longest(d1, l1, &b1);
	_aif_to_longest(d2, l2, &b2);
	res = b1 && b2; /* not bitwise! */
	_longest_to_aif(&rd, MAX(l1, l2), res);
}

/*
 * Boolean bitwise or.
 */
void
_aif_or_bool(char *rd, char *d1, int l1, char *d2, int l2)
{
	AIFLONGEST	b1;
	AIFLONGEST	b2;
	AIFLONGEST	res;

	_aif_to_longest(d1, l1, &b1);
	_aif_to_longest(d2, l2, &b2);
	res = b1 || b2; /* not bitwise! */
	_longest_to_aif(&rd, MAX(l1, l2), res);
}

/*
 * Boolean 2's compliment.
 */
void
_aif_not_bool(char *rd, char *data, int len)
{
	AIFLONGEST	b;
	AIFLONGEST	res;

	_aif_to_longest(data, len, &b);
	res = !b; /* not bitwise! */
	_longest_to_aif(&rd, len, res);
}

/*
 * Integer 2's compliment.
 */
void
_aif_not_int(char *rd, char *data, int len)
{
	AIFLONGEST	val;

	_aif_to_longest(data, len, &val);
	val = ~val;
	_longest_to_aif(&rd, len, val);
}

/*
 * Integer negation.
 */
void
_aif_neg_int(char *rd, char *data, int len)
{
	AIFLONGEST	val;

	_aif_to_longest(data, len, &val);
	val = -val;
	_longest_to_aif(&rd, len, val);
}

/*
 * Integer addition.
 */
void
_aif_add_int(char *rd, char *d1, int l1, char *d2, int l2)
{
	AIFLONGEST	val1;
	AIFLONGEST	val2;
	AIFLONGEST	val;

	_aif_to_longest(d1, l1, &val1);
	_aif_to_longest(d2, l2, &val2);
	val = val1 + val2;
	_longest_to_aif(&rd, MAX(l1, l2), val);
}

/*
 * Integer bitwise and.
 */
void
_aif_and_int(char *rd, char *d1, int l1, char *d2, int l2)
{
	AIFLONGEST	val1;
	AIFLONGEST	val2;
	AIFLONGEST	val;

	_aif_to_longest(d1, l1, &val1);
	_aif_to_longest(d2, l2, &val2);
	val = val1 & val2;
	_longest_to_aif(&rd, MAX(l1, l2), val);
}

/*
 * Integer bitwise or.
 */
void
_aif_or_int(char *rd, char *d1, int l1, char *d2, int l2)
{
	AIFLONGEST	val1;
	AIFLONGEST	val2;
	AIFLONGEST	val;

	_aif_to_longest(d1, l1, &val1);
	_aif_to_longest(d2, l2, &val2);
	val = val1 | val2;
	_longest_to_aif(&rd, MAX(l1, l2), val);
}

/*
 * Integer subtraction.
 */
void
_aif_sub_int(char *rd, char *d1, int l1, char *d2, int l2)
{
	AIFLONGEST	val1;
	AIFLONGEST	val2;
	AIFLONGEST	val;

	_aif_to_longest(d1, l1, &val1);
	_aif_to_longest(d2, l2, &val2);
	val = val1 - val2;
	_longest_to_aif(&rd, MAX(l1, l2), val);
}

/*
 * Integer multiplication. Result is same size as the largest
 * argument. 
 */
void
_aif_mul_int(char *rd, char *d1, int l1, char *d2, int l2)
{
	AIFLONGEST	val1;
	AIFLONGEST	val2;
	AIFLONGEST	val;

	_aif_to_longest(d1, l1, &val1);
	_aif_to_longest(d2, l2, &val2);
	val = val1 * val2;
	_longest_to_aif(&rd, MAX(l1, l2), val);
} 

/*
 * Compare two integers. Returns:
 *	-1 if d1 < d2
 *	 0 if d1 == d2
 *	 1 if d1 > d2
 */
int
compare_int(char *d1, int l1, char *d2, int l2)
{
	int	s1 = 0;
	int	s2 = 0;

	if ( l2 > l1 )
	{
		for ( s2 = 0 ; s2 < l2-l1; s2++ )
			if ( d2[s2] != 0x0 )
				return -1;
	}
	else
	{
		for ( s1 = 0 ; s1 < l1-l2; s1++ )
			if ( d1[s1] != 0x0 )
				return 1;
	}

	for ( ; s1 < l1 ; s1++, s2++)
	{
		if ( d2[s2] > d1[s1] )
			return -1;

		if ( d2[s2] < d1[s1] )
			return 1;
	}

	return 0;
}

/*
 * Shift d, s bits to the left. s < BITSPERBYTE. Result in r.
 * d and r can overlap, but must be the same size.
 */
void
lshift_int(unsigned char *r, unsigned char *d, int l, int s)
{
	int	i;
	int	s2 = BITSPERBYTE - s;


	for ( i = 0 ; i < l-1 ; i++ )
		r[i] = (d[i] << s) | (d[i+1] >> s2);

	r[i] = d[i] << s;
}

/*
 * Shift d, s bits to the right. s < BITSPERBYTE. Result in r.
 * d and r can overlap, but must be the same size.
 */
void
rshift_int(unsigned char *r, unsigned char *d, int l, int s)
{
	int	i;
	int	s2 = BITSPERBYTE - s;


	for ( i = l-1 ; i > 0 ; i-- )
		r[i] = (d[i] >> s) | (d[i-1] << s2);

	r[0] = d[0] >> s;
}

/*
 * Integer division d1/d2 = rq + rr/d2. Quotient is same size as the largest
 * argument. Note: d2 != 0.
 *
 * XXX need to check remainder for correct sign.
 */
void
_aif_div_int(char *rq, char *rr, char *d1, int l1, char *d2, int l2)
{
	AIFLONGEST	val1;
	AIFLONGEST	val2;
	AIFLONGEST	val;

	_aif_to_longest(d1, l1, &val1);
	_aif_to_longest(d2, l2, &val2);
	val = val1 / val2;
	_longest_to_aif(&rq, MAX(l1, l2), val);
	val = val1 % val2;
	_longest_to_aif(&rr, MAX(l1, l2), val);
} 

/*
 * Perform a binary integer operation.
 */
int
_aif_binary_op_int(aifop op, char **rd, char *d1, int l1, char *d2, int l2)
{
	char *	rt=NULL;


	if ( l1 > sizeof(AIFLONGEST) )
		fprintf(stderr, "binary int operation: arg1 exceeds precision\n");

	if ( l2 > sizeof(AIFLONGEST) )
		fprintf(stderr, "binary int operation: arg2 exceeds precision\n");

	switch ( op )
	{
	case AIFOP_ADD:
	case AIFOP_SUB:
	case AIFOP_MUL:
	case AIFOP_AND:
	case AIFOP_OR:
		if ( *rd == NULL )
			*rd = (char *)_aif_alloc(MAX(l1, l2));
		break;

	case AIFOP_DIV:
	case AIFOP_REM:
		if ( *rd == NULL )
			*rd = (char *)_aif_alloc(l1);

		/*
		** Temp for quotient.
		*/
		rt = (char *)_aif_alloc(l1);
		break;

	default:
		SetAIFError(AIFERR_TYPE, NULL);
		return -1;
	}

	switch ( op )
	{
	case AIFOP_ADD:
		_aif_add_int(*rd, d1, l1, d2, l2);
		break;

	case AIFOP_SUB:
		_aif_sub_int(*rd, d1, l1, d2, l2);
		break;

	case AIFOP_MUL:
		_aif_mul_int(*rd, d1, l1, d2, l2);
		break;

	case AIFOP_AND:
		_aif_and_int(*rd, d1, l1, d2, l2);
		break;

	case AIFOP_OR:
		_aif_or_int(*rd, d1, l1, d2, l2);
		break;

	case AIFOP_DIV:
		if ( _aif_int_is_zero(d2, l2) )
		{
			SetAIFError(AIFERR_ARITH, NULL);
			return -1;
		}

		_aif_div_int(*rd, rt, d1, l1, d2, l2);

		_aif_free(rt);
		break;

	case AIFOP_REM:
		if ( _aif_int_is_zero(d2, l2) )
		{
			SetAIFError(AIFERR_ARITH, NULL);
			return -1;
		}

		_aif_div_int(rt, *rd, d1, l1, d2, l2);

		_aif_free(rt);
		break;

	default:
		SetAIFError(AIFERR_TYPE, NULL);
		return -1;
	}

	return 0;
}

/*
 * Perform a binary boolean operation.
 */
int
_aif_binary_op_bool(aifop op, char **rd, char *d1, int l1, char *d2, int l2)
{
	if ( *rd == NULL )
		*rd = (char *)_aif_alloc(MAX(l1, l2));

	switch ( op )
	{
	case AIFOP_AND:
		_aif_and_bool(*rd, d1, l1, d2, l2);
		break;

	case AIFOP_OR:
		_aif_or_bool(*rd, d1, l1, d2, l2);
		break;

	default:
		SetAIFError(AIFERR_TYPE, NULL);
		return -1;
	}

	return 0;
}

/*
 * Perform a unary boolean operation.
 */
int
_aif_unary_op_bool(aifop op, char **rd, char *d, int l)
{
	if ( *rd == NULL )
		*rd = (char *)_aif_alloc(l);

	switch ( op )
	{
	case AIFOP_NOT:
		_aif_not_bool(*rd, d, l);
		break;

	default:
		SetAIFError(AIFERR_TYPE, NULL);
		return -1;
	}

	return 0;
}

/*
 * Perform a unary integer operation.
 */
int
_aif_unary_op_int(aifop op, char **rd, char *d, int l)
{
	if ( *rd == NULL )
		*rd = (char *)_aif_alloc(l);

	switch ( op )
	{
	case AIFOP_NEG:
		_aif_neg_int(*rd, d, l);
		break;

	case AIFOP_NOT:
		_aif_not_int(*rd, d, l);
		break;

	default:
		SetAIFError(AIFERR_TYPE, NULL);
		return -1;
	}

	return 0;
}

/*
 * Perform a binary floating point operation.
 */
int
_aif_binary_op_float(aifop op, char **rd, char *d1, int l1, char *d2, int l2)
{
	AIFDOUBLEST	v;
	AIFDOUBLEST	v1;
	AIFDOUBLEST	v2;

	ResetAIFError();

	if
	(
		_aif_to_doublest(d1, l1, &v1) < 0
		||
		_aif_to_doublest(d2, l2, &v2) < 0
	)
		return -1;

	switch ( op )
	{
	case AIFOP_ADD:
		v = v1 + v2;
		break;

	case AIFOP_SUB:
		v = v1 - v2;
		break;

	case AIFOP_MUL:
		v = v1 * v2;
		break;

	case AIFOP_DIV:
		/*
		** Let floating point / deal with v2 == 0.
		*/
		v = v1 / v2;
		break;

	default:
		SetAIFError(AIFERR_TYPE, NULL);
		return -1;
	}

	if ( MAX(l1, l2) > sizeof(float) )
		return _doublest_to_aif(rd, MAX(l1, l2), v);
	else
		return _doublest_to_aif(rd, l1, v);
}

/*
 * Perform a unary floating point operation.
 */
int
_aif_unary_op_float(aifop op, char **rd, char *d1, int l1)
{
	AIFDOUBLEST	v;
	AIFDOUBLEST	v1;

	if ( _aif_to_doublest(d1, l1, &v1) < 0 )
		return -1;

	switch ( op )
	{
	case AIFOP_NEG:
		v = -v1;
		break;

	default:
		SetAIFError(AIFERR_TYPE, NULL);
		return -1;
	}

	return _doublest_to_aif(rd, l1, v);
}

/*
 * Perform a binary operation. Arguments can be integer or
 * floating point or structs or arrays resulting in such.
 * Advance the data and format pointers across the data.
 * Return 0 for success, -1 for failure.
 * Result in rd and rf.
 * If rd is null, we generate our own format and data strings.  It is
 * impossible to decide the proper length for the data for structured types; we
 * make wild guesses.  It's a bug.
 */
int
_aif_binary_op(aifop op, 
	char **rf, char **rd, 
	char **f1, char **d1,
	char **f2, char **d2)
{
	int	i;
	int	bytes1;
	int	bytes2;
	int	bytes3;
	char *	d;
	char *	fmt;
	char *	d3 = NULL;
	int res;

	ResetAIFError();

	_fds_resolve(f1);
	_fds_resolve(f2);

	if ( FDSType(*f1) == AIF_REFERENCE )
	{
		fmt = _fds_lookup(f1);
		return _aif_binary_op(op, rf, rd, &fmt, d1, f2, d2);
	}

	if ( FDSType(*f2) == AIF_REFERENCE )
	{
		fmt = _fds_lookup(f2);
		return _aif_binary_op(op, rf, rd, f1, d1, &fmt, d2);
	}

	switch ( FDSType(*f1) )
	{
	case AIF_ENUM:
		bytes1 = _fds_count_bytes_na(f1);

		switch ( FDSType(*f2) )
		{
		case AIF_ENUM:
		case AIF_INTEGER:
			bytes2 = _fds_count_bytes_na(f2);

			if ( _aif_binary_op_int(op, rd, *d1, bytes1, *d2, bytes2) < 0 )
				return -1;

			(*d1) += bytes1;
			(*d2) += bytes2;

			if ( *rf == NULL )
				*rf = strdup(AIF_INTEGER_TYPE(1, MAX(bytes1, bytes2)));
			else
			{
				char *t1, *t2;
				int len;

				t1 = strstr(*rf, *f1);	

				if ( t1 == NULL )
				{
					SetAIFError(AIFERR_CONV, NULL);
					return -1;
				}

				t2 = _fds_base_type(t1);
				len = strlen(t2);
				memmove(t1, t2, len+1);
			}

			_fds_advance(f1);
			_fds_advance(f2);

			return 0;

		case AIF_FLOATING:
			bytes2 = _fds_count_bytes_na(f2);

			if ( (bytes3 = _aif_int_to_aif_float(&d3, *d1, bytes1)) < 0 )
				return -1;

			if ( _aif_binary_op_float(op, rd, d3, bytes3, *d2, bytes2) < 0 )
			{
				_aif_free(d3);
				return -1;
			}

			(*d1) += bytes1;
			(*d2) += bytes2;

			if ( *rf == NULL ) 
				*rf = strdup(AIF_FLOATING_TYPE(MAX(bytes2, bytes3)));
			else
			{
				char *t1, *t2;
				int len;

				t1 = strstr(*rf, *f1);	

				if ( t1 == NULL )
				{
					SetAIFError(AIFERR_CONV, NULL);
					return -1;
				}

				t2 = t1;
				_fds_advance(&t2);
				len = strlen(t2);
				*t1 = FDS_FLOATING;
				*(t1+1) = (char) MAX(bytes2, bytes3);
				memmove(t1+2, t2, len+1);
			}

			_aif_free(d3);

			_fds_advance(f1);
			_fds_advance(f2);

			return 0;

		default:
			SetAIFError(AIFERR_CONV, NULL);
			return -1;
		}
		break;
		/* case AIF_ENUM */

	case AIF_INTEGER:
		bytes1 = _fds_count_bytes_na(f1);

		switch ( FDSType(*f2) )
		{
		case AIF_CHARACTER:
		case AIF_INTEGER:
		case AIF_ENUM:
			bytes2 = _fds_count_bytes_na(f2);

			if ( _aif_binary_op_int(op, rd, *d1, bytes1, *d2, bytes2) < 0 )
				return -1;

			(*d1) += bytes1;
			(*d2) += bytes2;

			if ( *rf == NULL )
				*rf = strdup(AIF_INTEGER_TYPE(
					FDSIsSigned(*f1) || FDSIsSigned(*f2),
					MAX(bytes1, bytes2)));

			_fds_advance(f1);
			_fds_advance(f2);

			return 0;

		case AIF_FLOATING:
			bytes2 = _fds_count_bytes_na(f2);

			if ( (bytes3 = _aif_int_to_aif_float(&d3, *d1, bytes1)) < 0 )
				return -1;

			if ( _aif_binary_op_float(op, rd, d3, bytes3, *d2, bytes2) < 0 )
			{
				_aif_free(d3);
				return -1;
			}

			(*d1) += bytes1;
			(*d2) += bytes2;

			if ( *rf == NULL ) 
				*rf = strdup(AIF_FLOATING_TYPE(MAX(bytes2, bytes3)));

			_aif_free(d3);

			_fds_advance(f1);
			_fds_advance(f2);

			return 0;

		case AIF_VOID:
			d = NULL;
			res = 1;
			if ( _longest_to_aif(&d, sizeof(int), (AIFLONGEST)res) < 0 )
				return -1;

			*rd = d;

			if ( *rf == NULL )
				*rf = strdup(AIF_INTEGER_TYPE(1, sizeof(int)));

			return 0;


		default:
			SetAIFError(AIFERR_CONV, NULL);
			return -1;
		}
		break;
		/* case AIF_INTEGER */

	case AIF_FLOATING:
		bytes1 = _fds_count_bytes_na(f1);

		switch ( FDSType(*f2) )
		{
		case AIF_ENUM:
		case AIF_INTEGER:
			bytes2 = _fds_count_bytes_na(f2);

			if ( (bytes3 = _aif_int_to_aif_float(&d3, *d2, bytes2)) < 0 )
				return -1;

			if ( _aif_binary_op_float(op, rd, *d1, bytes1, d3, bytes3) < 0 )
			{
				_aif_free(d3);
				return -1;
			}

			(*d1) += bytes1;
			(*d2) += bytes2;

			if ( *rf == NULL )
				*rf = strdup(AIF_FLOATING_TYPE(MAX(bytes2, bytes3)));

			_aif_free(d3);

			_fds_advance(f1);
			_fds_advance(f2);

			return 0;
			
		case AIF_FLOATING:
			bytes2 = _fds_count_bytes_na(f2);

			if ( _aif_binary_op_float(op, rd, *d1, bytes1, *d2, bytes2) < 0 )
				return -1;

			(*d1) += bytes1;
			(*d2) += bytes2;

			if ( *rf == NULL )
				*rf = strdup(AIF_FLOATING_TYPE(MAX(bytes1, bytes2)));

			_fds_advance(f1);
			_fds_advance(f2);

			return 0;

		case AIF_VOID:
			d = NULL;
			res = 1;
			if ( _longest_to_aif(&d, sizeof(int), (AIFLONGEST)res) < 0 )
				return -1;

			*rd = d;

			if ( *rf == NULL )
				*rf = strdup(AIF_INTEGER_TYPE(1, sizeof(int)));

			return 0;

		default:
			SetAIFError(AIFERR_CONV, NULL);
			return -1;
		}
		break;

	case AIF_BOOLEAN:
		bytes1 = _fds_count_bytes_na(f1);
		bytes2 = _fds_count_bytes_na(f2);

		switch ( FDSType(*f2) )
		{
		case AIF_BOOLEAN:
			if ( _aif_binary_op_bool(op, rd, *d1, bytes1, *d2, bytes2) < 0 )
				return -1;

			if ( *rf == NULL )
				*rf = strdup(AIF_BOOLEAN_TYPE(MAX(bytes1, bytes2)));

			*d1 += bytes1;
			*d2 += bytes2;

			_fds_advance(f1);
			_fds_advance(f2);

			return 0;

		default:
			SetAIFError(AIFERR_CONV, NULL);
			return -1;
		}
		break;

	case AIF_CHARACTER:
		bytes1 = _fds_count_bytes_na(f1);
		bytes2 = _fds_count_bytes_na(f2);

		switch ( FDSType(*f2) )
		{
		case AIF_INTEGER:
			if ( _aif_binary_op_int(op, rd, *d1, bytes1, *d2, bytes2) < 0 )
				return -1;

			*d1 += bytes1;
			*d2 += bytes2;

			if ( *rf == NULL )
				*rf = strdup(AIF_INTEGER_TYPE(
						FDSIsSigned(*f2),
						MAX(bytes1, bytes2)));

			_fds_advance(f1);
			_fds_advance(f2);

			return 0;

		case AIF_CHARACTER:
			if ( _aif_binary_op_int(op, rd, *d1, bytes1, *d2, bytes2) < 0 )
				return -1;

			if ( *rf == NULL )
				*rf = strdup(AIF_CHARACTER_TYPE());

			*d1 += bytes1;
			*d2 += bytes2;

			_fds_advance(f1);
			_fds_advance(f2);

			return 0;

		case AIF_VOID:
			d = NULL;
			res = 1;
			if ( _longest_to_aif(&d, sizeof(int), (AIFLONGEST)res) < 0 )
				return -1;

			*rd = d;

			if ( *rf == NULL )
				*rf = strdup(AIF_INTEGER_TYPE(1, sizeof(int)));

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
				if ( FDSType(*f2) == AIF_VOID )
				{
					d = NULL;
					res = 1;
					if ( _longest_to_aif(&d, sizeof(int), (AIFLONGEST)res) < 0 )
						return -1;

					*rd = d;

					if ( *rf == NULL )
						*rf = strdup(AIF_INTEGER_TYPE(1, sizeof(int)));

					return 0;
				}

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
				*rd = (char *)_aif_alloc(BUFSIZ); /* no way to know how big! */

			if ( *rf == NULL )
				*rf = strdup(*f1);

			_fds_advance(f1); /* skip over the entire array fds */
			_fds_advance(f2);

			/*
			** If nel[12] > MAX_ADDRESS then we are not going to be
			** able to address all the data in the array. What should
			** we do?
			*/

			d = *rd; /* start of our data result region */

			for ( i = 0 ; i < ix1->i_nel ; i++ )
			{
				char * tmp1;
				char * tmp2;

				if ( dres != NULL) 
				{
					_aif_free(dres);
					dres = NULL;
				}

				if ( fres != NULL) 
				{
					_aif_free(fres);
					fres = NULL;
				}

				fmt1 = ix1->i_btype;
				fmt2 = ix2->i_btype;

				if ( _aif_binary_op(op, &fres, &dres, &fmt1, d1, &fmt2, d2 ) < 0 )
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
				memcpy(d, dres, tmp2-dres);
				d += (tmp2-dres);
			}

			{
				char *t1, *t2;
				int len;

				t1 = _fds_base_type(*rf);
				t2 = t1;
				_fds_advance(&t2);

				len = strlen(fres);
				memmove(t1, fres, len);
				t1 += len;

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

	/* only works with the public section in AIF_AGGREGATE */
	case AIF_AGGREGATE:
		if ( FDSType(*f2) != AIF_AGGREGATE )
		{
			if ( FDSType(*f2) == AIF_VOID )
			{
				d = NULL;
				res = 1;
				if ( _longest_to_aif(&d, sizeof(int), (AIFLONGEST)res) < 0 )
					return -1;

				*rd = d;

				if ( *rf == NULL )
					*rf = strdup(AIF_INTEGER_TYPE(1, sizeof(int)));

				return 0;
			}

			SetAIFError(AIFERR_TYPE, NULL);
			return -1;
		}

		if ( *rd == NULL )
			*rd = (char *)_aif_alloc(BUFSIZ); /* no way to know how big! */

		if ( *rf == NULL )
			*rf = strdup(*f1); /* includes trailing junk.  bug. */

		d = *rd; /* start of our data result region */

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
				return -1;
			}

			*f1 = strchr(*f1, FDS_AGGREGATE_FIELD_NAME_END) + 1; /* to start of field*/
			*f2 = strchr(*f2, FDS_AGGREGATE_FIELD_NAME_END) + 1; /* to start of field*/
			
			fmt = *f1;

			if ( _aif_binary_op(op, &fres, &dres, f1, d1, f2, d2 ) < 0 )
				return -1; /* non-zero res means no need to look further */

			tmp1 = fres;
			tmp2 = dres;
			_fds_skip_data(&tmp1, &tmp2);
			memcpy(d, dres, tmp2-dres);
			d += (tmp2-dres);

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
			SetAIFError(AIFERR_TYPE, NULL);
			return -1;
		}

		fmt = *rf;
		_fds_advance(&fmt);
		*fmt = '\0';

		*f1 = strchr(*f1, '}') + 1; /* to start of field*/
		*f2 = strchr(*f2, '}') + 1; /* to start of field*/

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

			char * target1 = 0;//FIXME: = _find_target(d1, 0);
			char * target2 = 0;//FIXME: = _find_target(d2, MAX_VALUES_SEEN/2);

			char * fres;
			char * dres;

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
				}
				else
				{
					int i;
					for ( i = 0; i < 5; i++) (*rd)[i] = ptr_type_2[i];
				}

				_fds_advance(f1);
				_fds_advance(f2);

				return 0;
			}

			if ( target1 == 0 )     /* target1 is null, make
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

			if ( target2 == 0 )     /* target2 is null, make
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

			ret = _aif_binary_op(op, &fres, &dres, f1, d1, f2, d2);

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
		if ( FDSType(*f2) != AIF_STRING || op != AIFOP_ADD )
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

		d = _aif_alloc(bytes1 + bytes2 + 2);

		d[0] = ((bytes1 + bytes2) >> 8) & 0xff;
		d[1] = (bytes1 + bytes2) & 0xff;

		memcpy(d+2, *d1, bytes1);
		memcpy(d+2+bytes1, *d2, bytes2);

		*d1 += bytes1;
		*d2 += bytes2;

		*rd = d;

		if ( *rf == NULL )
			*rf = strdup(AIF_STRING_TYPE());

		return 0;

	case AIF_ADDRESS:
		bytes1 = _fds_count_bytes_na(f1);

		switch ( FDSType(*f2) )
		{
		case AIF_ADDRESS:
			bytes2 = _fds_count_bytes_na(f2);

			if ( _aif_binary_op_int(op, rd, *d1, bytes1, *d2, bytes2) < 0 )
				return -1;

			(*d1) += bytes1;
			(*d2) += bytes2;

			if ( *rf == NULL )
				*rf = strdup(AIF_ADDRESS_TYPE(MAX(bytes1, bytes2)));

			_fds_advance(f1);
			_fds_advance(f2);

			return 0;

		default:
			SetAIFError(AIFERR_CONV, NULL);
			return -1;
		}
		break;

	case AIF_VOID:
		if ( FDSType(*f2) == AIF_VOID )
		{
			d = NULL;
			res = 0;
			if ( _longest_to_aif(&d, sizeof(int), (AIFLONGEST)res) < 0 )
				return -1;

			*rd = d;

			if ( *rf == NULL )
				*rf = strdup(AIF_INTEGER_TYPE(1, sizeof(int)));

			return 0;
		}
		else
		{
			d = NULL;
			res = 1;
			if ( _longest_to_aif(&d, sizeof(int), (AIFLONGEST)res) < 0 )
				return -1;

			*rd = d;

			if ( *rf == NULL )
				*rf = strdup(AIF_INTEGER_TYPE(1, sizeof(int)));

			return 0;
		}
	default:
		SetAIFError(AIFERR_TYPE, NULL);
		return -1;
	}
}

/*
 * Perform a unary operation. Argument can be integer or
 * floating point or structs or arrays resulting in such.
 * Advance the data and format pointers across the data.
 * Return 0 for success, -1 for failure.
 * Result in rd and rf.
 * If rd is null, we generate our own format and data strings.  It is
 * impossible to decide the proper length for the data for structured types; we
 * make wild guesses.  It's a bug.
 */
int
_aif_unary_op(aifop op, char **rf, char **rd, char **fds, char **data)
{
	int	i;
	int	bytes;
	char *	dp;

	switch ( FDSType(*fds) )
	{
	case AIF_BOOLEAN:
		bytes = _fds_count_bytes(fds);

		if ( *rf == NULL )
			*rf = strdup(AIF_BOOLEAN_TYPE(bytes));

		if ( _aif_unary_op_bool(op, rd, *data, bytes) < 0 )
			return -1;

		(*data) += bytes;
		return 0;

	case AIF_ENUM:
		dp = *fds;
		bytes = _fds_count_bytes(fds);

		if ( *rf == NULL )
			*rf = strdup(AIF_INTEGER_TYPE(1, bytes));
		else
		{
			char *t1, *t2;
			int len;

			t1 = strstr(*rf, dp);	

			if ( t1 == NULL )
			{
				SetAIFError(AIFERR_CONV, NULL);
				return -1;
			}

			t2 = _fds_base_type(t1);
			len = strlen(t2);
			memmove(t1, t2, len+1);
		}

		if ( _aif_unary_op_int(op, rd, *data, bytes) < 0 )
			return -1;

		(*data) += bytes;

		return 0;

	case AIF_INTEGER:
		bytes = _fds_count_bytes_na(fds);

		if ( *rf == NULL )
			*rf = strdup(AIF_INTEGER_TYPE(FDSIsSigned(*fds), bytes));
		_fds_advance(fds);

		if ( _aif_unary_op_int(op, rd, *data, bytes) < 0 )
			return -1;

		(*data) += bytes;

		return 0;

	case AIF_FLOATING:
		bytes = _fds_count_bytes(fds);

		if ( *rf == NULL )
			*rf = strdup(AIF_FLOATING_TYPE(bytes));

		if ( _aif_unary_op_float(op, rd, *data, bytes) < 0 )
			return -1;

		(*data) += bytes;

		return 0;

	case AIF_ARRAY:
		{
			AIFIndex *	ix;
			char *		fmt;
			char *		fres = NULL;

			ix = FDSArrayIndexInit(*fds);

			if ( *rd == NULL )
			{
				*rd = (char *)_aif_alloc(ix->i_nel * ix->i_bsize); /* works unless we have subordinate lists */
				*rf = strdup(*fds);
			}

			if ( *rf == NULL )
				*rf = strdup(*fds);

			_fds_advance(fds); /* skip over the entire array fds */

			dp = *rd; /* start of our data result region */

			for ( i = 0 ; i < ix->i_nel ; i++ )
			{
				if ( fres != NULL )
				{
					_aif_free(fres);
					fres = NULL;
				}

				fmt = ix->i_btype;

				if ( _aif_unary_op(op, &fres, &dp, &fmt, data) < 0 )
				{
					AIFArrayIndexFree(ix);
					return -1;
				}

				dp += ix->i_bsize;

				AIFArrayIndexInc(ix);
			}

			{
				char *t1, *t2;
				int len;

				t1 = _fds_base_type(*rf);
				t2 = t1;
				_fds_advance(&t2);

				len = strlen(fres);
				memmove(t1, fres, len);
				t1 += len;

				if ( *t2 != '\0' )
				{
					len = strlen(t2);
					memmove(t1, t2, len+1);
				}
				else
					*t1 = '\0';
			}


			_aif_free(fres);

			AIFArrayIndexFree(ix);

			return 0;
		}

	/* only works with the public section in AIF_AGGREGATE */
	case AIF_AGGREGATE:
		if ( *rd == NULL )
		{
			*rd = (char *)_aif_alloc(BUFSIZ); /* no way to know how big! */
			*rf = strdup(*fds); /* will include trailing junk.  Bug */
		}

		if ( *rf == NULL )
			*rf = strdup(*fds);

		(*fds)++; /* past open brace */
		_fds_skipid(fds);

		dp = *rd;

		while ( **fds != ';' )
		{
			char * fres = NULL;
			char * fmt;

			*fds = strchr(*fds, FDS_AGGREGATE_FIELD_NAME_END) + 1; /*to start of field*/
			fmt = *fds;

			i = _fds_count_bytes_na(fds);

			if ( _aif_unary_op(op, &fres, &dp, fds, data) < 0 )
				return -1; /* non-zero res means no need to look further */

			dp += i;

			{
				char *t1, *t2;
				int len;

				t1 = strstr(*rf, fmt);
				t2 = strstr(*rf, *fds);

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

			if (**fds == ',') (*fds)++;
		}

		return 0;

	default:
		SetAIFError(AIFERR_TYPE, NULL);
		return -1;
	}

	/* NOT REACHED */
}

AIF *
_aif_unary(AIF *a, aifop op)
{
	char *	rd;
	char *	rf;
	char *	f;
	char *	d;

	if ( a == (AIF *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return (AIF *)NULL;
	}
	
	f = AIF_FORMAT(a);
	d = AIF_DATA(a);

	rf = NULL;
	rd = _aif_alloc(AIF_LEN(a));

	if ( _aif_unary_op(op, &rf, &rd, &f, &d) < 0 )
		return (AIF *)NULL;

	return MakeAIF(rf, rd);
}

AIF *
AIFNot(AIF *a)
{
	return _aif_unary(a, AIFOP_NOT);
}

AIF *
AIFNeg(AIF *a)
{
	return _aif_unary(a, AIFOP_NEG);
}

AIF *
_aif_binary(AIF *a1, AIF *a2, aifop op)
{
	char *	rd;
	char *	rf;
	char *	f1;
	char *	f2;
	char *	d1;
	char *	d2;

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

	if ( _aif_binary_op(op, &rf, &rd, &f1, &d1, &f2, &d2) < 0 )
		return (AIF *)NULL;

	return MakeAIF(rf, rd);
}
AIF *
AIFAdd(AIF *a1, AIF *a2)
{
	return _aif_binary(a1, a2, AIFOP_ADD);
}

AIF *
AIFSub(AIF *a1, AIF *a2)
{
	return _aif_binary(a1, a2, AIFOP_SUB);
}

AIF *
AIFMul(AIF *a1, AIF *a2)
{
	return _aif_binary(a1, a2, AIFOP_MUL);
} 

AIF *
AIFDiv(AIF *a1, AIF *a2)
{
	return _aif_binary(a1, a2, AIFOP_DIV);
} 

AIF *
AIFRem(AIF *a1, AIF *a2)
{
	return _aif_binary(a1, a2, AIFOP_REM);
} 

AIF *
AIFAnd(AIF *a1, AIF *a2)
{
	return _aif_binary(a1, a2, AIFOP_AND);
} 

AIF *
AIFOr(AIF *a1, AIF *a2)
{
	return _aif_binary(a1, a2, AIFOP_OR);
} 



