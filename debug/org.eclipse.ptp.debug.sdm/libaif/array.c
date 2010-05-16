/*
 * AIF routines that operate specifically on arrays.
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
#include	<memory.h>
#include	<string.h>

#include	"aif.h"
#include	"aiferr.h"
#include	"aifint.h"

int
AIFArrayRank(AIF *a)
{
	if ( a == (AIF *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return -1;
	}

	if ( AIFType(a) != AIF_ARRAY )
	{
		SetAIFError(AIFERR_TYPE, NULL);
		return -1;
	}

	return FDSArrayRank(AIF_FORMAT(a));
}

int
AIFArraySize(AIF *a)
{
	if ( a == (AIF *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return -1;
	}

	if ( AIFType(a) != AIF_ARRAY )
	{
		SetAIFError(AIFERR_TYPE, NULL);
		return -1;
	}

	return FDSArraySize(AIF_FORMAT(a));
}

/*
 * Parse array type descriptor and extract the minimum index and
 * number of elements for each dimension.
 */
int
AIFArrayBounds(AIF *a, int rank, int **min, int **size)
{
	if ( a == (AIF *)NULL || min == (int **)NULL || size == (int **)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return -1;
	}

	if ( AIFType(a) != AIF_ARRAY )
	{
		SetAIFError(AIFERR_TYPE, NULL);
		return -1;
	}

	FDSArrayBounds(AIF_FORMAT(a), rank, min, size);

	return 0;
}

/*
 * Extract info about an array. Returns the index type of the
 * first dimension (assumes all dimensions are the same type),
 * the element type of the array, and the number of dimensions
 * of the array.
 */
int
AIFArrayInfo(AIF *a, int *rank, char **type, int *idx)
{
	char *	itype;

	if ( a == (AIF *)NULL || rank == (int *)NULL || type == (char **)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return -1;
	}

	if ( AIFType(a) != AIF_ARRAY )
	{
		SetAIFError(AIFERR_TYPE, NULL);
		return -1;
	}

	FDSArrayInfo(AIF_FORMAT(a), rank, (char **)type, &itype);

	if ( idx != (int *)NULL )
		*idx = FDSType(itype);

	return 0;
}

/* A post-processing routine that is called by AIFArraySlice().
 * This routine checks the fds and the data of an AIF object, and it creates
 * a new data for the object 'rdata' which is consistent. The function checks
 * all of the AIF_PTR_REFERENCEs and it changes the first occurence of a
 * particular AIF_PTR_REFERENCE to an appropriate AIF_PTR_NAME based on the
 * 'res' array. It modifies the 'fds' and the 'data'. 'rdata' will also
 * be advanced to the end.
 *
 * The routine gets 4 parameters:
 * fds - the fds of an AIF object
 * data - the original data of an AIF object
 * rdata - the new data for the object
 * res - a 'char *' array that stores the references
 */
int
_aif_array_slice_post(char **fds, char **data, char **rdata, char **res)
{
	int 	datalen;
	int 	index;
	char *	fmt;
	char **	d;

	_fds_resolve(fds);

	switch ( FDSType(*fds) )
	{
	case AIF_REFERENCE:
		fmt = _fds_lookup(fds);
		_aif_array_slice_post(&fmt, data, rdata, res);
		return 0;

	case AIF_ARRAY:
		{
			AIFIndex * ix;
			int i;
			char * fbase;

			ix = FDSArrayIndexInit(*fds);
			_fds_advance(fds);

			for ( i = 0; i < ix->i_nel; i++ )
			{
				fbase = ix->i_btype;
				_aif_array_slice_post(&fbase, data, rdata, res);
			}

			return 0;
		}

	case AIF_AGGREGATE:
		(*fds)++;
		_fds_skipid(fds);

		while ( **fds != FDS_AGGREGATE_ACCESS_SEP )
		{
			*fds = strchr(*fds, FDS_AGGREGATE_FIELD_NAME_END) + 1;
			_aif_array_slice_post(fds, data, rdata, res);

			if ( **fds == FDS_AGGREGATE_FIELD_SEP )
				(*fds)++;
		}

		*fds = strchr(*fds, FDS_AGGREGATE_END) + 1;

		return 0;

	case AIF_POINTER:

		(*fds)++;

		switch ( (int)**data )
		{
		case AIF_PTR_NIL:
			**rdata = **data;
			(*data)++;
			(*rdata)++;
			_fds_advance(fds);
			return 0;

		case AIF_PTR_NORMAL:
			**rdata = **data;
			(*data)++;
			(*rdata)++;
			_aif_array_slice_post(fds, data, rdata, res);
			return 0;

		case AIF_PTR_NAME:
			memcpy(*rdata, *data, 5); /* 1 + 4 (ptr name) */
			(*data) += 5;
			(*rdata) += 5;
			_aif_array_slice_post(fds, data, rdata, res);
			return 0;

		case AIF_PTR_REFERENCE:
			d = data;
			_ptrname_to_int(d, &index);
			if ( res[index] != NULL )
			{
				*(*rdata)++ = AIF_PTR_NAME;
				_int_to_ptrname(index, *rdata);
				*rdata += 4;
				datalen = FDSDataSize(*fds, res[index]);
				memcpy(*rdata, res[index], datalen);
				(*rdata) += datalen;
				(*data) += 5;
				_fds_advance(fds);
				res[index]=NULL;
			}
			else
			{
				memcpy(*rdata, *data, 5);
				(*data) += 5;
				(*rdata) += 5;
			}
			return 0;
		}

	default:
			datalen = FDSDataSize(*fds, *data);
			_fds_advance(fds);
			memcpy(*rdata, *data, datalen);
			(*data) += datalen;
			(*rdata) += datalen;
			return 0;
	}
}

/*
 * A pre-processing routine that is called by AIFArraySlice().
 * This routine checks the fds and the data, and it saves all of the
 * references that are pointed by AIF_PTR_NAMEs into 'res'.
 *
 * The function gets 3 parameters, the fds and the data of an AIF object
 * that is going to be scanned, and a 'char *' array (res) to store
 * the results. All of the elements of the 'res' array must be initialized
 * to NULL. It modifies 'fds' and 'data'.
 */
int
_aif_array_slice_pre(char **fds, char **data, char **res)
{
	int 	datalen;
	char * 	fmt;

	_fds_resolve(fds);

	switch ( FDSType(*fds) )
	{
	case AIF_REFERENCE:
		fmt = _fds_lookup(fds);
		_aif_array_slice_pre(&fmt, data, res);
		return 0;

	case AIF_ARRAY:
		{
			AIFIndex * ix;
			int i;
			char * fbase;

			ix = FDSArrayIndexInit(*fds);
			_fds_advance(fds);

			for ( i = 0; i < ix->i_nel; i++ )
			{
				fbase = ix->i_btype;
				_aif_array_slice_pre(&fbase, data, res);
			}

			return 0;
		}

	case AIF_AGGREGATE:
		(*fds)++;
		_fds_skipid(fds);

		while ( **fds != FDS_AGGREGATE_ACCESS_SEP )
		{
			*fds = strchr(*fds, FDS_AGGREGATE_FIELD_NAME_END) + 1;
			_aif_array_slice_pre(fds, data, res);

			if ( **fds == FDS_AGGREGATE_FIELD_SEP )
				(*fds)++;
		}

		*fds = strchr(*fds, FDS_AGGREGATE_END) + 1;

		return 0;

	case AIF_POINTER:
		{
			int index;

			(*fds)++;

			switch ( (int)**data )
			{
			case AIF_PTR_NIL:
				(*data)++;
				_fds_advance(fds);
				return 0;

			case AIF_PTR_NORMAL:
				(*data)++;
				_aif_array_slice_pre(fds, data, res);
				return 0;

			case AIF_PTR_NAME:
				(*data)++;
				_ptrname_to_int(data, &index);
				res[index] = *data;
				_aif_array_slice_pre(fds, data, res);
				return 0;

			case AIF_PTR_REFERENCE:
				(*data) += 5;
				_fds_advance(fds);
				return 0;
			}
		}

	default:
			datalen = FDSDataSize(*fds, *data);
			_fds_advance(fds);
			(*data) += datalen;
			return 0;
	}
}

/*
 * Evaluate array slice.
 * XXX: TOFIX
 */
AIF *
AIFArraySlice(AIF *a, int rank, int *mn, int *sz)
{
	int			i;
	int			nrank = 0;
	int *		rankp;
	int			rl;
	char *		rf;
	char *		fds;
	char *		r;
	AIF *		ra;
	AIFIndex *	ix1;
	AIFIndex *	ix2 = (AIFIndex *)NULL;
	char *		res_array[MAX_VALUES_SEEN+1] = {NULL};

	if ( a == (AIF *)NULL || mn == (int *)NULL || sz == (int *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return (AIF *)NULL;
	}

	if ( AIFType(a) != AIF_ARRAY )
	{
		SetAIFError(AIFERR_TYPE, NULL);
		return (AIF *)NULL;
	}

	ix1 = FDSArrayIndexInit(AIF_FORMAT(a));

	if ( rank != ix1->i_rank )
	{
		SetAIFError(AIFERR_INDEX, NULL);
		AIFArrayIndexFree(ix1);
		return (AIF *)NULL;
	}

	/*
	** Create rankp array used by AIFIndexInRange().
	*/
	rankp = (int *)_aif_alloc(rank * sizeof(int *));

	/*
	** Construct a new fds.
	*/
	rl = strlen(AIF_FORMAT(a)) + 1;
	rf = fds = (char *)_aif_alloc(rl);

	/*
	** Calculate nrank, the number of dimensions of the (new) array.
	** This corresponds to the number of elements where sz > 0
	** (i.e. where an empty slice has not been specified.)
	** When sz == -1 we slice the whole dimension.
	*/
	for ( i = 0 ; i < rank ; i++ )
	{
		if ( sz[i] < 0 )
		{
			mn[i] = ix1->i_min[i];
			sz[i] = ix1->i_size[i];
		}
		else if
		(
			mn[i] < ix1->i_min[i] 
			||
			mn[i] >= ix1->i_min[i] + ix1->i_size[i]
			||
			sz[i] > ix1->i_size[i]
		)
		{
			SetAIFError(AIFERR_INDEX, NULL);
			AIFArrayIndexFree(ix1);
			_aif_free(fds);
			_aif_free(rankp);
			return (AIF *)NULL;
		}
		else if ( sz[i] == 0 ) {
			continue;
		}

		rankp[nrank++] = i;

		*rf++ = FDS_ARRAY_START;
		rl--;
		r = FDSRangeInit(mn[i], sz[i]);
		strncpy(rf, r, rl);
		rf += strlen(r);
		rl -= strlen(r);
		*rf++ = FDS_ARRAY_END;
		rl--;
		_aif_free(r);
	}

	strcpy(rf, ix1->i_btype);

	/*
	** Create a new structure to hold the sliced array.
	*/

	/* In the old implementation, we use : ra = MakeAIF(fds, NULL); 
	 * However, with complex data structures, we cannot use MakeAIF()
	 * since the actual size of the ra (i.e. AIF_LEN(ra)) cannot be known 
	 * only from the variable 'fds'.
	 */

	ra = NewAIF(0, BUFSIZ);
	AIF_FORMAT(ra) = fds;

	if ( nrank > 0 )
		ix2 = FDSArrayIndexInit(AIF_FORMAT(ra));

	/*
	** Now build the data.
	*/
	for ( i = 0 ; i < ix1->i_nel ; i++ )
	{
		char *	data;
		char *	ndata;
		int counter, limit;
		int size = 0;
		int subsize = 0;
		int len;

		if ( !AIFIndexInRange(rank, ix1->i_index, mn, sz) )
		{
			AIFArrayIndexInc(ix1);
			continue;
		}

		limit = AIFIndexOffset(ix1->i_rank, ix1->i_index, 
					ix1->i_min, ix1->i_size, NULL);
		for ( counter = 0; counter < limit; counter++ )
		{
			subsize = FDSDataSize(ix1->i_btype,AIF_DATA(a)+size);
			size += subsize;
		}

		data = AIF_DATA(a) + size;
		len = FDSDataSize(ix1->i_btype, data);

		/* old code: data = AIF_DATA(a) + AIFIndexOffset(ix1->i_rank, ix1->i_index, ix1->i_min, ix1->i_max, NULL) * ix1->i_bsize; */

		if ( nrank == 0 )
		{
			memcpy(AIF_DATA(ra), data, len);
			break;
		}

		size = 0; subsize = 0;
		limit = AIFIndexOffset(ix2->i_rank, ix2->i_index, 
					ix2->i_min, ix2->i_size, NULL);
		for ( counter = 0; counter < limit; counter++ )
		{
			subsize = FDSDataSize(ix2->i_btype,AIF_DATA(ra)+size);
			size += subsize;
		}

		ndata = AIF_DATA(ra) + size;

		/* old code: ndata = AIF_DATA(ra) + AIFIndexOffset(ix2->i_rank, ix2->i_index, ix2->i_min, ix2->i_max, NULL) * ix2->i_bsize; */

		/* old code: memcpy(ndata, data, ix1->i_bsize); */
		memcpy(ndata, data, len);

		AIFArrayIndexInc(ix1);
		AIFArrayIndexInc(ix2);
	}

	/* Do pre-processing and post-processing steps */
	{
		char * f = AIF_FORMAT(a);
		char * d = AIF_DATA(a);
		char * n;
		char * tmp;

		_aif_array_slice_pre(&f, &d, res_array);

		f = AIF_FORMAT(ra);
		d = _aif_alloc(BUFSIZ);
		tmp = d;
		memcpy(d, AIF_DATA(ra), BUFSIZ);
		n = AIF_DATA(ra);

		_aif_array_slice_post(&f, &d, &n, res_array);
		/* we need to use 'tmp' because after the call of
		 * _aif_array_slice_post(), d points to the end, thus
		 * the memory cannot be freed by using _aif_free(d)
		 */

		_aif_free(tmp);
	}

	AIFArrayIndexFree(ix1);

	if ( nrank > 0 )
		AIFArrayIndexFree(ix2);

	_aif_free(rankp);

	return ra;
}

/*
 * XXX: TOFIX
 */
/*
 * The old AIFArrayPerm() relied on the assumption that all elements of the
 * AIF array have the same size (i.e. it wrote the data for the 
 * resultant array in a "random access" way).
 * To solve this problem, we create dynamic arrays to hold the data and the
 * length, after that we iterate the arrays to copy the data to the resultant
 * AIF array.
 */
AIF *
AIFArrayPerm(AIF *a, int *index)
{
	int		i;
	int		rl;
	char *		rf;
	AIF *		ra;
	AIFIndex *	ix;
	int 		len;

	char **		data_array;
	int *		len_array;

	if ( a == (AIF *)NULL || index == (int *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return (AIF *)NULL;
	}

	if ( AIFType(a) != AIF_ARRAY )
	{
		SetAIFError(AIFERR_TYPE, NULL);
		return (AIF *)NULL;
	}

	/*
	** Initialise the index arrays.
	*/
	ix = FDSArrayIndexInit(AIF_FORMAT(a));

	/*
	** Create a new structure to hold the permuted array.
	*/
	ra = NewAIF(strlen(AIF_FORMAT(a))+1, BUFSIZ);

	/*
	** Construct a new fds.
	*/
	memcpy(AIF_FORMAT(ra), AIF_FORMAT(a), strlen(AIF_FORMAT(a))+1);
	rf = AIF_FORMAT(ra);
	rl = strlen(rf) + 1;

	for ( i = 0 ; i < ix->i_rank ; i++ )
	{
		char * r = FDSRangeInit(ix->i_min[index[i]], ix->i_size[index[i]]);
		*rf++ = FDS_ARRAY_START;
		rl--;
		strncpy(rf, r, rl);
		rf += strlen(r);
		rl -= strlen(r);
		*rf++ = FDS_ARRAY_END;
		rl--;
		_aif_free(r);
	}

	strcat(AIF_FORMAT(ra), ix->i_btype);

	len_array = _aif_alloc( ix->i_nel * sizeof(int) );
	data_array = _aif_alloc( ix->i_nel * sizeof(char *) );

	/*
	** Now build the data.
	*/
	for ( i = 0 ; i < ix->i_nel ; i++ )
	{
		char *	d;
		int 	counter, limit;
		int	size = 0; 
		int	subsize = 0;

		limit = AIFIndexOffset(ix->i_rank, ix->i_index,
				ix->i_min, ix->i_size, NULL);

		for ( counter = 0; counter < limit; counter++ )
		{
			subsize = FDSDataSize(ix->i_btype, AIF_DATA(a)+size);
			size += subsize;
		}

		d = AIF_DATA(a) + size;
		len = FDSDataSize(ix->i_btype, d);

		size = 0; subsize = 0;
		limit = AIFIndexOffset(ix->i_rank, ix->i_index,
				ix->i_min, ix->i_size, NULL);

		data_array[limit] = d;
		len_array[limit] = len;

		AIFArrayIndexInc(ix);
	}

	len = 0;

	/* Copy the data to the resultant AIF array */
	for ( i = 0 ; i < ix->i_nel ; i++ )
	{
		char * rd;

		rd = AIF_DATA(ra) + len;

		memcpy(rd, data_array[i], len_array[i]);

		len += len_array[i];
	}

	AIFArrayIndexFree(ix);

	_aif_free(data_array);
	_aif_free(len_array);

	return ra;
}

char *
AIFArrayIndexType(AIF *a)
{

	if ( a == (AIF *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return NULL;
	}

	if ( AIFType(a) != AIF_ARRAY )
	{
		SetAIFError(AIFERR_TYPE, NULL);
		return NULL;
	}

	return FDSArrayIndexType(AIF_FORMAT(a));
}

int
AIFArrayMinIndex(AIF *a, int n)
{
	if ( a == (AIF *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return -1;
	}

	if ( AIFType(a) != AIF_ARRAY )
	{
		SetAIFError(AIFERR_TYPE, NULL);
		return -1;
	}

	return FDSArrayMinIndex(AIF_FORMAT(a), n);
}

int
AIFArrayRankSize(AIF *a, int n)
{
	if ( a == (AIF *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return -1;
	}

	if ( AIFType(a) != AIF_ARRAY )
	{
		SetAIFError(AIFERR_TYPE, NULL);
		return -1;
	}

	return FDSArrayRankSize(AIF_FORMAT(a), n);
}

/*
 * The index array is used to compute the location of data in
 * a multi-dimensional array, and basically corresponds to a loop
 * counter in normal circumstances.
 */

#define PERMUTE(p, d) ((p) != NULL ? (p)[d] : d)

/*
 * Initialise an index array to the minimum index value for each 
 * dimension, given the number of dimensions (rank), and the minimum 
 * and maximum values of each dimension (min, max). Returns the number 
 * of elements in the array.
 */
AIFIndex *
AIFArrayIndexInit(AIF *a)
{
	if ( a == (AIF *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return (AIFIndex *)NULL;
	}

	if ( AIFType(a) != AIF_ARRAY )
	{
		SetAIFError(AIFERR_TYPE, NULL);
		return (AIFIndex *)NULL;
	}

	return FDSArrayIndexInit(AIF_FORMAT(a));
}

/*
 * Check that the index value of each index in rankp is within the range
 * min .. min+size-1. Returns 1 if they are, otherwise 0.
 *
 * Note: rankp, index, min and size are guaranteed to have at least
 * rank elements.
 */
int
AIFIndexInRange(int rank, int *index, int *min, int *size)
{
	int	d;

	for ( d = rank - 1 ; d >= 0 ; d-- )
	{
		if ( min[d] < 0 )
			continue;

		if ( index[d] < min[d] || index[d] >= min[d] + size[d] )
			return 0;
	}

	return 1;
}

/*
 * Step through indexes in the order specified by the perm array.
 * Indexes cycle from min to max. Returns 1 on carry out.
 */
int
AIFArrayIndexInc(AIFIndex *ix)
{
	int	d;

	for ( d = ix->i_rank - 1 ; d >= 0 ; d-- )
	{
		if ( ++(ix->i_index[d]) >= ix->i_min[d] + ix->i_size[d] )
		{
			ix->i_index[d] = ix->i_min[d];
			continue;
		}

		return 0;
	}

	ix->i_finished = 1;

	return 1;
}

void
AIFArrayIndexFree(AIFIndex *ix)
{
	_aif_free(ix->i_btype);
	_aif_free(ix->i_index);
	_aif_free(ix->i_min);
	_aif_free(ix->i_size);
	_aif_free(ix);
}

/*
 * Index array element and return as doublest
 * XXX: TOFIX
 */
int
AIFArrayElementToDoublest(AIF *a, AIFIndex *ix, AIFDOUBLEST *val)
{
	if ( a == (AIF *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return -1;
	}

	if ( AIFType(a) != AIF_ARRAY )
	{
		SetAIFError(AIFERR_TYPE, NULL);
		return -1;
	}

	if ( AIFBaseType(a) != AIF_FLOATING )
	{
		SetAIFError(AIFERR_TYPE, NULL);
		return -1;
	}

	return _aif_to_doublest(AIF_DATA(a) + AIFIndexOffset(ix->i_rank, ix->i_index, ix->i_min, ix->i_size, NULL) * ix->i_bsize, ix->i_bsize, val);
}

int
AIFArrayElementToDouble(AIF *a, AIFIndex *ix, double *val)
{
	int		res;
	AIFDOUBLEST	dd;

	res = AIFArrayElementToDoublest(a, ix, &dd);
	
	if ( res >= 0 )
		*val = (double)dd; /* maybe lose some precision */

	return res;
}

/*
 * Index array element and return as longest
 * XXX: TOFIX
 */
int
AIFArrayElementToLongest(AIF *a, AIFIndex *ix, AIFLONGEST *val)
{
	if ( a == (AIF *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return -1;
	}

	if ( AIFType(a) != AIF_ARRAY )
	{
		SetAIFError(AIFERR_TYPE, NULL);
		return -1;
	}

	if ( AIFBaseType(a) != AIF_INTEGER )
	{
		SetAIFError(AIFERR_TYPE, NULL);
		return -1;
	}

	return _aif_to_longest(AIF_DATA(a) + AIFIndexOffset(ix->i_rank, ix->i_index, ix->i_min, ix->i_size, NULL) * ix->i_bsize, ix->i_bsize, val);
}

int
AIFArrayElementToInt(AIF *a, AIFIndex *ix, int *val)
{
	int		res;
	AIFLONGEST	l;

	res = AIFArrayElementToLongest(a, ix, &l);

	if ( res >= 0 )
		*val = (int)l;
	
	return res;
}

AIF *
_aif_array_ref(AIF *a, int rank, int *index, int *min, int *size, char *btype, int bsize)
{
	AIF *	ae;
	int	counter;
	int	offset;
	char *	theFormat;
	char *	dataStart;
	char *	dataEnd;

	ae = MakeAIF(strdup(btype), NULL);

	offset = AIFIndexOffset(rank, index, min, size, (int *)NULL);
	dataStart = dataEnd = AIF_DATA(a);

	for ( counter = 0 ; counter <= offset ; counter++ )
	{
		theFormat = btype;
		dataStart = dataEnd;
		_fds_skip_data(&theFormat, &dataEnd);
	}

	AIF_DATA(ae) = _aif_alloc(dataEnd - dataStart + 1);
	memcpy(AIF_DATA(ae), dataStart, dataEnd - dataStart);

	return ae;
}

/*
 * Compute size of array.
 */
int
_aif_array_size(char *fds, char *data)
{
	int		i;
	int		n = 0;
	char *		dstart;
	char *		fmt;
	char *		btype;
	AIFIndex *	ix;

	ix = FDSArrayIndexInit(fds);

	btype = FDSBaseType(fds);

	for ( i = 0 ; i <= ix->i_nel ; i++ )
	{
		fmt = btype;
		dstart = data;
		_fds_skip_data(&fmt, &data);
		n += data - dstart;
	}

	AIFArrayIndexFree(ix);

	return n;
}

/*
 * Return array element at index.
 */
AIF *
AIFArrayElement(AIF *a, AIFIndex *ix)
{
	if ( a == (AIF *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return (AIF *)NULL;
	}

	if ( AIFType(a) != AIF_ARRAY )
	{
		SetAIFError(AIFERR_TYPE, NULL);
		return (AIF *)NULL;
	}

	if ( ix->i_finished )
		return (AIF *)NULL;

	return _aif_array_ref(a, ix->i_rank, ix->i_index, ix->i_min, ix->i_size, ix->i_btype, ix->i_bsize);
}

/*
 * Return array element at location.
 */
AIF *
AIFArrayRef(AIF *a, int rank, int *loc)
{
	int		i;
	AIF *		ae;
	AIFIndex *	ix;

	if ( a == (AIF *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return (AIF *)NULL;
	}

	if ( AIFType(a) != AIF_ARRAY )
	{
		SetAIFError(AIFERR_TYPE, NULL);
		return (AIF *)NULL;
	}

	ix = AIFArrayIndexInit(a);

	if ( rank != ix->i_rank )
	{
		AIFArrayIndexFree(ix);
		SetAIFError(AIFERR_INDEX, NULL);
		return (AIF *)NULL;
	}

	for ( i = 0 ; i < rank ; i++ )
	{
		if ( loc[i] < ix->i_min[i] || loc[i] >= ix->i_min[i] + ix->i_size[i] )
		{
			AIFArrayIndexFree(ix);
			SetAIFError(AIFERR_INDEX, NULL);
			return (AIF *)NULL;
		}
	}

	ae = _aif_array_ref(a, ix->i_rank, loc, ix->i_min, ix->i_size, ix->i_btype, ix->i_bsize);

	AIFArrayIndexFree(ix);

	return ae;
}

/*
 * Copy data into an array.
 * XXX: TOFIX
 */
int
AIFSetArrayData(AIF *dst, AIFIndex *ix, AIF *src)
{
	char *	dd;

	if
	(
		dst == (AIF *)NULL
		||
		src == (AIF *)NULL
		||
		ix == (AIFIndex *)NULL
	)
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return -1;
	}               

	if
	( 
		AIFType(dst) != AIF_ARRAY
		||
		AIFBaseType(dst) != AIFType(src)
	)
	{       
		SetAIFError(AIFERR_TYPE, NULL);
		return -1;
	}

	dd = AIF_DATA(dst) + AIFIndexOffset(ix->i_rank, ix->i_index, ix->i_min, ix->i_size, NULL) * ix->i_bsize;

	memcpy(dd, AIF_DATA(src), ix->i_bsize);

	return 0;
}

/*
 * Calculate the data offset in the array given the current value
 * of the indexes, and the minimum indexes and size of each dimension.
 */
int
AIFIndexOffset(int rank, int *index, int *min, int *size, int *perm)
{
	int	d;
	int	p;
	int	off = 0;
	int	sz = 1;

	for ( d = rank - 1 ; d >= 0 ; d-- )
	{
		p = PERMUTE(perm, d);
		off += (index[p] - min[p]) * sz;
		sz *= size[p];
	}

	return off;
}



