/*
 * Routines for converting between native data format and
 * AIF or for constructing AIF objects.
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

#include	<assert.h>
#include	<ctype.h>
#include	<stdio.h>
#include	<string.h>
#include	<stdlib.h>
#include	<stdarg.h>

#include	"aif.h"
#include	"aiferr.h"
#include	"aifint.h"

#if defined(WINDOWSNT) || defined(WIN32)
#define BITSPERBYTE	8
#elif defined(__APPLE__)
#include	<sys/types.h>
#define BITSPERBYTE	NBBY
#else /* WINDOWSNT || WIN32 */
#include	<values.h>
#endif /* WINDOWSNT || WIN32 */

union ieee
{
	char		c;
	int		i;
	AIFLONGEST	li;
	float		f;
	double		d;
	AIFDOUBLEST	dd;
	/*
	** long enough for any conceivable precision
	*/
	char		bytes[256/BITSPERBYTE];
};

/*
 * For _aif_*_to_str() functions
 */
static char *_str_buf = NULL;
static char *_str_pos = NULL;
static int _str_buf_len = 0;

/*
 * Convert base type to standard byte ordering.
 */
void
_aif_normalise(char *dst, int dstlen, char *src, int srclen)
{
#ifndef WORDS_BIGENDIAN
	int	i;
#endif
	int	len;
	int	pad = 0;

	ResetAIFError();

	if ( srclen < dstlen )
	{
		len = srclen;
		pad = dstlen - srclen;
	} 
	else if ( srclen >= dstlen )
		len = dstlen;

#ifdef WORDS_BIGENDIAN
	memcpy(dst, src+srclen-len, len);

	if ( pad > 0 )
		memset(dst+len, '\0', pad);
#else /* WORDS_BIGENDIAN */
	for ( i = len - 1 ; i >= 0 ; i-- )
		*dst++ = src[i];

	if ( pad > 0 )
		memset(dst, '\0', pad);
#endif /* WORDS_BIGENDIAN */
}

/*
 * Normalise a value to the standard format
 */
void
AIFNormalise(char *dst, int dstlen, char *src, int srclen)
{
	_aif_normalise(dst, dstlen, src, srclen);
}

/*
 * Convert a pointer value to AIF data format. Result is rd.
 */
int
_pointer_to_aif(char **rd, const AIF *addr, const AIF *val)
{
	if ( rd == NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return -1;
	}

	ResetAIFError();

	if( *rd == NULL ) {
		*rd = (char *)_aif_alloc(AIF_LEN(addr) + AIF_LEN(val) + 1);
	}

	**rd = (char) AIF_PTR_NORMAL; /* indicates normal pointer value */
	
	memcpy((*rd)+1, AIF_DATA(addr), AIF_LEN(addr));
	memcpy((*rd)+1+AIF_LEN(addr), AIF_DATA(val), AIF_LEN(val));

	return 0;
}

/*
 * Convert an integer value of any length to AIF data format.
 * Result is rd.
 */
int
_longest_to_aif(char **rd, int len, AIFLONGEST val)
{
	int		srclen;
	union ieee	f;

	if ( rd == NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return -1;
	}

	ResetAIFError();

	if( *rd == NULL )
		*rd = (char *)_aif_alloc(len);

	if ( len <= sizeof(int) )
	{
		f.i = (int)val;
		srclen = sizeof(int);
	}
	else
	{
		f.li = val;
		srclen = sizeof(AIFLONGEST);
	}

	_aif_normalise(*rd, len, f.bytes, srclen);

	return 0;
}

/*
 * Convert a character value to AIF data format. Result is rd.
 */
int
_char_to_aif(char **rd, char val)
{
	union ieee	f;

	if ( rd == NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return -1;
	}

	ResetAIFError();

	if( *rd == NULL )
		*rd = (char *)_aif_alloc(sizeof(char));

	f.c = val;

	AIFNormalise(*rd, sizeof(char), f.bytes, sizeof(char));

	return 0;
}

/*
 * Create an empty named AIF object.
 */
AIF *
NameAIF(AIF *oldAIF, int name)
{
	AIF *	a;

	ResetAIFError();

	a = NewAIF(0, 0);

	AIF_FORMAT(a) = strdup(AIF_NAME_TYPE(name, AIF_FORMAT(oldAIF)));
	AIF_DATA(a) = _aif_alloc(AIF_LEN(oldAIF));
	memcpy(AIF_DATA(a), AIF_DATA(oldAIF), AIF_LEN(oldAIF));
	AIF_LEN(a) = AIF_LEN(oldAIF);

	return a;
}

int
AIFSetIdentifier(AIF *a, char *id)
{
	return FDSSetIdentifier(&(AIF_FORMAT(a)), id);
}

char *
AIFGetIdentifier(AIF *a)
{
	return FDSGetIdentifier(AIF_FORMAT(a));
}

/*
 * Create an empty reference to an AIF object
 */
AIF *
ReferenceAIF(int name)
{
	AIF *	a;

	ResetAIFError();

	a = NewAIF(0, 0);

	AIF_FORMAT(a) = strdup(AIF_REFERENCE_TYPE(name));
	AIF_LEN(a) = 0;
	
	return a;
}

/*
 * toWhat represents the sort of thing to which a null pointer needs to be
 * created.  If it is named, that name is used in the resulting AIF; otherwise,
 * the format of toWhat is used to generate the format of the new AIF.
 */
AIF *
AIFNull(AIF *toWhat)
{
	int	name;
	AIF *	a;
	char *	fmt;
	char *	oldFormat = AIF_FORMAT(toWhat);

	ResetAIFError();

	a = NewAIF(0, 1);

	if ( *oldFormat == '%' )
	{
		/*
		** a reference
		*/
		sscanf(oldFormat, "%%%d/", &name);
		fmt = strdup(AIF_REFERENCE_TYPE(name));
		AIF_FORMAT(a) = strdup(AIF_POINTER_TYPE(NULL, fmt));
		_aif_free(fmt);
	}
	else
	{
		/* 
		** repeat the format
		*/
		AIF_FORMAT(a) = strdup(AIF_POINTER_TYPE(NULL, oldFormat));
	}

	AIF_DATA(a)[0] = AIF_PTR_NIL;
	AIF_LEN(a) = 1;
	
	return a;
}

AIF *
PointerNameToAIF(AIF *i)
{
	AIF *	a;
	int	name;
	char *	fmt = AIF_FORMAT(i);

	a = NewAIF(0, AIF_LEN(i)+2);

	sscanf(fmt, "%%%d/", &name);

	AIF_FORMAT(a) = strdup(AIF_POINTER_TYPE(NULL, AIF_FORMAT(i)));

	*(AIF_DATA(a)) = (char) AIF_PTR_NAME;
	_int_to_ptrname( (int) name, AIF_DATA(a)+1);
	
	memcpy(AIF_DATA(a)+5, AIF_DATA(i), AIF_LEN(i));

	ResetAIFError();

	return a;
}

AIF *
PointerReferenceToAIF(AIF *toWhat)
{
	int	name;
	AIF *	a;
	char *	fmt;
	char *	oldFormat = AIF_FORMAT(toWhat);

	ResetAIFError();

	a = NewAIF(0, 2);

	if ( *oldFormat == '%' )
	{
		/*
		** a reference
		*/
		sscanf(oldFormat, "%%%d/", &name);
		fmt = strdup(AIF_REFERENCE_TYPE(name));
		AIF_FORMAT(a) = strdup(AIF_POINTER_TYPE(NULL, fmt));
		_aif_free(fmt);
	}

	AIF_DATA(a)[0] = AIF_PTR_REFERENCE;
	_int_to_ptrname( (int) name, AIF_DATA(a)+1);
	AIF_LEN(a) = 5;
	
	return a;
}

/*
 * Create a pointer to an AIF object.
 */
AIF *
PointerToAIF(AIF *addr, AIF *i)
{
	AIF *	a;

	a = NewAIF(0, AIF_LEN(addr)+AIF_LEN(i)+1);
	AIF_FORMAT(a) = strdup(AIF_POINTER_TYPE(AIF_FORMAT(addr), AIF_FORMAT(i)));

	if ( _pointer_to_aif(&AIF_DATA(a), addr, i) < 0 )
	{
		AIFFree(a);
		return (AIF *)NULL;
	}

	ResetAIFError();

	return a;
}

AIF *
CharPointerToAIF(AIF *addr, char *val)
{
	AIF *	a;
	int	length = strlen(val);

	if ( length > 1 << (2 * BITSPERBYTE) )
	{
		SetAIFError(AIFERR_STRING, NULL);
		return (AIF *)NULL;
	}

	ResetAIFError();

	a = NewAIF(0, AIF_LEN(addr)+length+2);
	
	AIF_FORMAT(a) = strdup(AIF_CHAR_POINTER_TYPE(AIF_FORMAT(addr)));

	memcpy(AIF_DATA(a), AIF_DATA(addr), AIF_LEN(addr));

	(AIF_DATA(a))[AIF_LEN(addr)] = (length >> 8) & 0xff;
	(AIF_DATA(a))[AIF_LEN(addr)+1] = length & 0xff;

	strncpy(AIF_DATA(a)+AIF_LEN(addr)+2, val, length);

	return a;
}

/*
 * Convert a string (defined as a pointer to a null terminated array
 * of characters) to AIF format.
 */
AIF *
StringToAIF(char *i)
{
	AIF *	a;
	int	length = strlen(i);

	if ( length > 1 << (2 * BITSPERBYTE) )
	{
		SetAIFError(AIFERR_STRING, NULL);
		return (AIF *)NULL;
	}

	ResetAIFError();

	a = NewAIF(0, length+2);

	AIF_FORMAT(a) = strdup(AIF_STRING_TYPE());

	(AIF_DATA(a))[0] = (length >> 8) & 0xff;
	(AIF_DATA(a))[1] = length & 0xff;

	strncpy(AIF_DATA(a)+2, i, length);

	return a;
}

/* Converts input buffer to hexadecimal display in output string.
 * String length is used since zero is legitimate.
 */
static void 
ByteToHex(char *out_string, char *in_string, int in_size)
{
	char hexchars[] = "0123456789ABCDEF";
	char ch;
	int i;

	for (i=0; i<in_size; i++) {
		ch = *in_string++;
		*out_string++ = hexchars[(ch >> 4) & 0xf]; /* [c/16]; */
		*out_string++ = hexchars[ ch & 0xf]; /* [c%16]; */
	}
}
/* Get default address format with size
 * 
 */
static char * 
GetDefaultAddress(int size)
{
	int i;
	const char *pattern = "00";
	char *addr = (char *)malloc(size*2 + 1);
	//char addr[size*2];
	//addr[0] = '\0';

	for (i=0; i<size; i++) {
		memcpy(&addr[i*2], pattern, 2);
		//strcat(addr, pattern);
	}
	addr[size*2] = '\0';
	return strdup(addr);
}
/* Converts hexadecimal display to binary. String length is used
 * and only hexadecimal pairs are handled.
 */
static void 
HexToByte(char *out_string, char *in_string, int in_size)
{
	int i;
	int h_value, c_value;
	int byte_val = 0;
	int in_len, def_len = 0;
	char *input;
	char *def_input;
	
	def_input = GetDefaultAddress(in_size);
	if (in_size%2 != 0) {
		out_string = strdup(def_input);
		return;
	}

	in_len = strlen(in_string);
	def_len = strlen(def_input);

	if (in_len == def_len) {
		strcpy(def_input, in_string);
	}
	else {
		for (i=def_len-in_len; i<def_len; i++) {
			def_input[i] = *in_string++;
		}
	}

	input = strdup(def_input);
	for (i=0; i<(in_size*2); i++) {
		if ((h_value = *input++) >= '0' && h_value <= '9')
			c_value = h_value - '0';
		else
			(c_value = 10 + (h_value & 0xf) - ('a' & 0xf));

		if (!(i%2))
			byte_val = c_value * 16;
		else
			*out_string++ = byte_val + c_value;
	}
}
/*
 * Convert an address (defined as a pointer to a null terminated array
 * of characters) to AIF format.
 */
AIF *
AddressToAIF(char *addr, int len)
{
	AIF * a;
	char * buf;
	
	if (addr == NULL) {
		addr = GetDefaultAddress(len);
	}

	a = NewAIF(0, len);
	AIF_FORMAT(a) = strdup(AIF_ADDRESS_TYPE(len));

	//memcpy(AIF_DATA(a), &addr, in_size);
	buf = _aif_alloc(len);
	HexToByte(buf, addr, len);
/*
	char *hex;
	hex = _aif_alloc(len);
	ByteToHex(hex, buf, len);
	printf("---- Address hex: %s, %i\n", hex, len);
	free(hex);
*/
	memcpy(AIF_DATA(a), buf, len);
	free(buf);
	return a;
}
/*
 * Convert a boolean value to AIF.
 */
AIF *
BoolToAIF(int b)
{
	AIF  * a;
	int    size;

	size = sizeof(int);

	a = NewAIF(0, size);

	AIF_FORMAT(a) = strdup(AIF_BOOLEAN_TYPE());

	if ( b )
		memset(AIF_DATA(a), 0xff, size);
	else
		memset(AIF_DATA(a), 0, size);

	return a;
}

/*
 * Convert a character to AIF.
 */
AIF *
CharToAIF(char c)
{
	AIF *	a;

	a = NewAIF(0, sizeof(char));

	AIF_FORMAT(a) = strdup(AIF_CHARACTER_TYPE());

	if ( _char_to_aif(&AIF_DATA(a), c) < 0 )
	{
		AIFFree(a);
		return (AIF *)NULL;
	}

	return a;
}

/*
 * Convert a short integer to AIF.
 */
AIF *
ShortToAIF(short i)
{
	AIF *	a;

	a = NewAIF(0, sizeof(short));

	AIF_FORMAT(a) = strdup(AIF_INTEGER_TYPE(1, sizeof(short)));

	if ( _longest_to_aif(&AIF_DATA(a), sizeof(short), (AIFLONGEST)i) < 0 )
	{
		AIFFree(a);
		return (AIF *)NULL;
	}

	return a;
}

/*
 * Convert an unsigned short to AIF.
 */
AIF *
UnsignedShortToAIF(unsigned short i)
{
	AIF *	a;

	a = NewAIF(0, sizeof(unsigned short));

	AIF_FORMAT(a) = strdup(AIF_INTEGER_TYPE(0, sizeof(unsigned short)));

	if ( _longest_to_aif(&AIF_DATA(a), sizeof(unsigned short), (AIFLONGEST)i) < 0 )
	{
		AIFFree(a);
		return (AIF *)NULL;
	}

	return a;
}

/*
 * Convert an integer to AIF.
 */
AIF *
IntToAIF(int i)
{
	AIF *	a;

	a = NewAIF(0, sizeof(int));

	AIF_FORMAT(a) = strdup(AIF_INTEGER_TYPE(1, sizeof(int)));

	if ( _longest_to_aif(&AIF_DATA(a), sizeof(int), (AIFLONGEST)i) < 0 )
	{
		AIFFree(a);
		return (AIF *)NULL;
	}

	return a;
}

/*
 * Convert an unsigned integer to AIF.
 */
AIF *
UnsignedIntToAIF(unsigned int i)
{
	AIF *	a;

	a = NewAIF(0, sizeof(unsigned int));

	AIF_FORMAT(a) = strdup(AIF_INTEGER_TYPE(0, sizeof(unsigned int)));

	if ( _longest_to_aif(&AIF_DATA(a), sizeof(unsigned int), (AIFLONGEST)i) < 0 )
	{
		AIFFree(a);
		return (AIF *)NULL;
	}

	return a;
}

/*
 * Convert a long integer to AIF.
 */
AIF *
LongToAIF(long i)
{
	AIF *	a;

	a = NewAIF(0, sizeof(long));

	AIF_FORMAT(a) = strdup(AIF_INTEGER_TYPE(1, sizeof(long)));

	if ( _longest_to_aif(&AIF_DATA(a), sizeof(long), (AIFLONGEST)i) < 0 )
	{
		AIFFree(a);
		return (AIF *)NULL;
	}

	return a;
}

/*
 * Convert an unsigned long to AIF.
 */
AIF *
UnsignedLongToAIF(unsigned long i)
{
	AIF *	a;

	a = NewAIF(0, sizeof(unsigned long));

	AIF_FORMAT(a) = strdup(AIF_INTEGER_TYPE(0, sizeof(unsigned long)));

	if ( _longest_to_aif(&AIF_DATA(a), sizeof(unsigned long), (AIFLONGEST)i) < 0 )
	{
		AIFFree(a);
		return (AIF *)NULL;
	}

	return a;
}

#ifdef CC_HAS_LONG_LONG
/*
 * Convert a long long integer to AIF.
 */
AIF *
LongLongToAIF(long long i)
{
	AIF *	a;

	a = NewAIF(0, sizeof(long long));

	AIF_FORMAT(a) = strdup(AIF_INTEGER_TYPE(1, sizeof(long long)));

	if ( _longest_to_aif(&AIF_DATA(a), sizeof(long long), (AIFLONGEST)i) < 0 )
	{
		AIFFree(a);
		return (AIF *)NULL;
	}

	return a;
}

/*
 * Convert an unsigned long long to AIF.
 */
AIF *
UnsignedLongLongToAIF(unsigned long long i)
{
	AIF *	a;

	a = NewAIF(0, sizeof(unsigned long long));

	AIF_FORMAT(a) = strdup(AIF_INTEGER_TYPE(0, sizeof(unsigned long long)));

	if ( _longest_to_aif(&AIF_DATA(a), sizeof(unsigned long long), (AIFLONGEST)i) < 0 )
	{
		AIFFree(a);
		return (AIF *)NULL;
	}

	return a;
}
#endif /* CC_HAS_LONG_LONG */

/*
 * Convert an integer of any length to AIF.
 */
AIF *
LongestToAIF(AIFLONGEST i)
{
	AIF *	a;

	a = NewAIF(0, sizeof(AIFLONGEST));

	AIF_FORMAT(a) = strdup(AIF_INTEGER_TYPE(1, sizeof(AIFLONGEST)));

	if ( _longest_to_aif(&AIF_DATA(a), sizeof(AIFLONGEST), i) < 0 )
	{
		AIFFree(a);
		return (AIF *)NULL;
	}

	return a;
}

/*
 * Convert a floating point value of any length to AIF data format.
 * Result in rd.
 */
int
_doublest_to_aif(char **rd, int len, AIFDOUBLEST val)
{
	union ieee	v;

	if ( rd == NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return -1;
	}

	ResetAIFError();

	if( *rd == NULL )
		*rd = (char *)_aif_alloc(len);

	if ( len <= sizeof(float) )
		v.f = (float)val;
	else if ( len == sizeof(double) )
		v.d = (double)val;
	else
		v.dd = val;

	AIFNormalise(*rd, len, v.bytes, len);

	return 0;
}

/*
 * Convert a single precision floating point value to AIF.
 */
AIF *
FloatToAIF(float f)
{
	AIF *	a;

	a = NewAIF(0, sizeof(float));

	AIF_FORMAT(a) = strdup(AIF_FLOATING_TYPE(sizeof(float)));

	if ( _doublest_to_aif(&AIF_DATA(a), sizeof(float), (AIFDOUBLEST)f) < 0 )
		return (AIF *)NULL;

	return a;
}

/*
 * Convert a double precision floating point value to AIF.
 */
AIF *
DoubleToAIF(double f)
{
	AIF *	a;

	a = NewAIF(0, sizeof(double));

	AIF_FORMAT(a) = strdup(AIF_FLOATING_TYPE(sizeof(double)));

	if ( _doublest_to_aif(&AIF_DATA(a), sizeof(double), (AIFDOUBLEST)f) < 0 )
		return (AIF *)NULL;

	return a;
}

/*
 * Convert an array to AIF.
 *
 * AIF *ArrayToAIF(int rank, int *min, int *max, char *data, int len, char *btype)
 *
 * - rank is the rank of the array
 * - min is the index lower bound, or an array of lower bounds if rank > 1
 * - max is the index upper bound, or an array of upper bounds if rank > 1
 * - data is a pointer to the array data, or NULL, and must be normalised
 * - len is length in bytes of the entire array
 * - btype is the type of the array elements
 */
AIF *
ArrayToAIF(int rank, int *min, int *max, char *data, int len, char *btype)
{
	int	i;
	int	bsize;
	AIF *	a;
	char *	itype; /* index type */
	char *	rtype; /* range type */
	char *	fds=NULL;

	bsize = FDSTypeSize(btype);

	a = NewAIF(0, len);

	btype = strdup(btype);
	itype = strdup(AIF_INTEGER_TYPE(0, sizeof(int)));

	for ( i = rank - 1  ; i >= 0 ; i-- )
	{
		rtype = strdup(AIF_RANGE_TYPE(min[i], max[i], itype));
		fds = strdup(AIF_ARRAY_TYPE(rtype, btype));

		_aif_free(rtype);
		_aif_free(btype);

		btype = fds;
	}

	assert(fds!=NULL);
	AIF_FORMAT(a) = fds;

	if ( len > 0 && data != NULL )
		memcpy(AIF_DATA(a), data, len);

	_aif_free(itype);

	ResetAIFError();

	return a;
}

/*
 * Create an empty array containing elemenst min..max and
 * with a base type given by btype
 */
AIF *
EmptyArrayToAIF(int min, int max, AIF *btype)
{
	AIF *	a;
	int len = 0;

	if (AIF_FORMAT(btype)[0] != FDS_CHAR_POINTER) {
		len = (max - min + 1) * AIFTypeSize(btype);
	}

	a = NewAIF(0, len);
	
	AIF_FORMAT(a) = FDSArrayInit(min, max, AIF_FORMAT(btype));
	
	ResetAIFError();
	
	return a;
}
/*
 * Add the element el to the array at the index given by idx.
 * This is used for array construction.
 */
void
AIFAddArrayElement(AIF *a, int idx, AIF *el)
{
	int	min;
	int	max;
	int len = AIFTypeSize(el);

	//if (len == 0 || AIFTypeSize(a) == 0)
		//return;

	if (AIF_FORMAT(el)[0] == FDS_CHAR_POINTER) {
		AIFAddComplexArrayElement(a, el);
	}
	else {
		min = FDSArrayMinIndex(AIF_FORMAT(a), 0);
		max = FDSArrayMaxIndex(AIF_FORMAT(a), 0);
		
		if (idx < min || idx > max)
			return;
					
		memcpy(AIF_DATA(a) + len * (idx - min), AIF_DATA(el), len);
	}
}

AIF *
EmptyComplexArrayToAIF(int min, int max, AIF *btype)
{
	AIF *	a;
	
	a = NewAIF(0, 0);
	
	AIF_FORMAT(a) = FDSArrayInit(min, max, AIF_FORMAT(btype));
	
	ResetAIFError();
	
	return a;
}
void
AIFAddComplexArrayElement(AIF *a, AIF *el)
{
	char *newData;
	int len = AIFTypeSize(el);
	int cap = AIFTypeSize(a);

	AIF_LEN(a) += len;
	newData = _aif_alloc(AIF_LEN(a));

	memcpy(newData, AIF_DATA(a), cap);
	memcpy(newData+cap, AIF_DATA(el), len);

	if (AIF_DATA(a))
		_aif_free(AIF_DATA(a));

	AIF_DATA(a) = _aif_alloc(AIF_LEN(a));

	memcpy(AIF_DATA(a), newData, AIF_LEN(a));

	_aif_free(newData);
}

/*
 * Create an empty AIF enumerated type.
 */
AIF *
EmptyEnumToAIF(char *id) 
{
        AIF *   a;

        a = NewAIF(0, sizeof(int));

        AIF_FORMAT(a) = FDSEnumInit(id);

        ResetAIFError();

        return(a);
}

/*
 * Add a constant to enumerated type.
 */
int
AIFAddConstToEnum(AIF *a, char *id, AIF *content)
{
	char *nfmt;
	int val;

	if ( AIFToInt(content, &val) < 0 )
		return -1;

	nfmt = FDSAddConstToEnum(AIF_FORMAT(a), id, val);

	if ( nfmt == NULL )
	{
		return -1;
	}
	else
	{
		_aif_free(AIF_FORMAT(a));
		AIF_FORMAT(a) = nfmt;
		return 0;
	}
}

/*
 * Set AIF enum type to a particular value.
 */
int
AIFSetEnum(AIF *a, char *id)
{
	int v;

	if ( FDSEnumConstByName(AIF_FORMAT(a), id, &v) < 0 )
	{
		SetAIFError(AIFERR_FIELD, NULL);
		return -1;
	}

	if ( _longest_to_aif(&AIF_DATA(a), sizeof(int), (AIFLONGEST)v) < 0 )
        {
                AIFFree(a);
                return -1;
        }

        return 0;
}

/*
 * Get current value of enum.
 */
AIF *
AIFGetEnum(AIF *a)
{
	AIF  * new;
	char * btype;
	int    len;


	if ( (btype = FDSBaseType(AIF_FORMAT(a))) == NULL )
		return NULL;

	if ( (len = FDSTypeSize(AIF_FORMAT(a))) < 0 )
		return NULL;

	new = NewAIF(0, len);

	AIF_FORMAT(new) = strdup(btype);
	AIF_LEN(new) = len;
	memcpy(AIF_DATA(new), AIF_DATA(a), len);

	return new;

}

/*
 * Create an empty AIF union type.
 */
AIF *
EmptyUnionToAIF(char *id) 
{
        AIF *   a;

        a = NewAIF(0, 0);

        AIF_FORMAT(a) = FDSUnionInit(id);

        ResetAIFError();

        return(a);
}

/*
 * Add a filed to an AIF union.
 */
int
AIFAddFieldToUnion(AIF *a, char *field, char *fmt)
{
	char *nfmt;

	nfmt = FDSAddFieldToUnion(AIF_FORMAT(a), field, fmt);

	if ( nfmt == NULL )
	{
		return -1;
	}
	else
	{
		_aif_free(AIF_FORMAT(a));
		AIF_FORMAT(a) = nfmt;
		return 0;
	}
}

/*
 * Set the value of a union.
 */
int
AIFSetUnion(AIF *a, char *field, AIF *data)
{
	char *type;
	int   len;

	if ( FDSUnionFieldByName(AIF_FORMAT(a), field, &type) < 0 )
	{
		SetAIFError(AIFERR_FIELD, NULL);
		return -1;
	}

	if ( strcmp(type, AIF_FORMAT(data)) != 0 )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return -1;
	}

	len = FDSTypeSize(AIF_FORMAT(a));
	AIF_LEN(a) = len;

	if ( AIF_DATA(a) != NULL )
		_aif_free(AIF_DATA(a));

	AIF_DATA(a) = _aif_alloc(len);
	memcpy(AIF_DATA(a), AIF_DATA(data), len);

        return 0;

}

/*
 * Get the value of a union.
 */
AIF *
AIFGetUnion(AIF *a, char *field)
{
	AIF  * new;
	char * type;
	int    len;

	if ( FDSUnionFieldByName(AIF_FORMAT(a), field, &type) < 0 )
	{
		SetAIFError(AIFERR_FIELD, NULL);
		return NULL;
	}

	if ( (len = FDSTypeSize(type)) < 0 )
		return NULL;

	new = NewAIF(0, len);

	AIF_FORMAT(new) = type;
	AIF_LEN(new) = len;
	memcpy(AIF_DATA(new), AIF_DATA(a), len);

	return new;

}

/*
 * Create an empty AIF structure.
 */
AIF *
EmptyStructToAIF(char *id)
{
	AIF *	a;

	a = NewAIF(0, 0);

	AIF_FORMAT(a) = FDSStructInit(id);

	ResetAIFError();

	return a;
}

/* 
 * Add a field to the public section of a structure
 */
int
AIFAddFieldToStruct(AIF *a, char* field, AIF *content)
{
	int	pubLength;
	int	oldLength;
	int	newLength;
	char *	newFormat;
	char *	newData;
	char *	dummyString;

	if ( !FDSStructFieldByName(AIF_FORMAT(a), field, &dummyString) )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		_aif_free(dummyString);
		return -1;
	}

	/* We use FDSDataSize() instead of:
		pubLength = _data_len_public(AIF_FORMAT(a));
	to calculate the data length, _data_len_public cannot handle the case
	where a string is one of the members of the struct (since the length
	of the string is encoded in the data section).
	Currently FDSDataSize() can only handle public members of structs, so
	it is safe to replace _data_len_public() with FDSDataSize().
	*/
	pubLength = FDSDataSize(AIF_FORMAT(a), AIF_DATA(a));
	oldLength = AIF_LEN(a);
	newLength = oldLength + AIF_LEN(content);

	newData = _aif_alloc(newLength);

	memcpy(newData, AIF_DATA(a), pubLength);
	memcpy(newData+pubLength, AIF_DATA(content), AIF_LEN(content));
	memcpy(newData+pubLength+AIF_LEN(content), AIF_DATA(a)+pubLength, oldLength-pubLength);

	if ( AIF_DATA(a) )
		_aif_free(AIF_DATA(a));

	AIF_DATA(a) = _aif_alloc(newLength);
	AIF_LEN(a) = newLength;

	memcpy(AIF_DATA(a), newData, newLength);

	newFormat = FDSAddFieldToStruct(AIF_FORMAT(a), field, AIF_FORMAT(content));

	_aif_free(AIF_FORMAT(a));
	AIF_FORMAT(a) = newFormat;

	_aif_free(newData);

	ResetAIFError();

	return 0;
}

/* 
 * Set the value of a field in a structure
 */
int
AIFSetStruct(AIF *a, char* field, AIF *content)
{
	int	preLength;
	char *	dummyString;
	int	index;

	if ( FDSStructFieldByName(AIF_FORMAT(a), field, &dummyString) )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return -1;
	}

	index = FDSStructFieldIndex(AIF_FORMAT(a), field);
	preLength = _data_len_index(AIF_FORMAT(a), index);
	preLength -= FDSTypeSize(dummyString);
	_aif_free(dummyString);

	memcpy(AIF_DATA(a)+preLength, AIF_DATA(content), AIF_LEN(content));

	ResetAIFError();

	return(0);
}

/*
 * Get the value of a field in a structure
 */
AIF *
AIFGetStruct(AIF *a, char *field)
{
	AIF  * new;
	char * type;
	int    len;
	int    pre;
	int    index;

	if ( FDSStructFieldByName(AIF_FORMAT(a), field, &type) < 0 )
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

	index = FDSStructFieldIndex(AIF_FORMAT(a), field);
	pre = _data_len_index(AIF_FORMAT(a), index);
	pre -= len;

	AIF_FORMAT(new) = type;
	AIF_LEN(new) = len;
	memcpy(AIF_DATA(new), AIF_DATA(a)+pre, len);

	return new;
}

/*
 * Convert an AIF value to a native character.
 */
int
_aif_to_char(char *data, char *val)
{
	int		s;

	if ( data == NULL || val == (char *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return -1;
	}

	ResetAIFError();

	for ( s = 0 ; s < sizeof(char) ; s++ )
		val[s] =  data[s];

	return 0;
}

/*
 * Convert an AIF integer to the longest integer available.
 */
int
_aif_to_longest(char *data, int len, AIFLONGEST *val)
{
	int			s;
	AIFLONGEST		i = 0;

	if ( data == NULL || val == (AIFLONGEST *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return -1;
	}

	if ( len > sizeof(AIFLONGEST) )
	{
		SetAIFError(AIFERR_CONV, NULL);
		return -1;
	}

	ResetAIFError();

	for ( s = 0 ; s < len ; s++ )
		i = i * 0x100 + (data[s] & 0xff);

	*val = i;

	return 0;
}

/*
 * Convert AIF integer to native integer. If native integer
 * size is too small, return low order bytes.
 */
int
AIFToInt(AIF *a, int *val)
{
	AIFLONGEST	l;

	if ( AIFToLongest(a, &l) < 0 )
		return -1;

	*val = l;

	return 0;
}

/*
 * Convert AIF value to longest integer available. Implicitly converts
 * an floating value to an integer.
 */
int
AIFToLongest(AIF *a, AIFLONGEST *val)
{
	int		res = 0;
	AIFDOUBLEST	d;

	if ( a == (AIF *)NULL || val == (AIFLONGEST *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return -1;
	}

	switch ( FDSType(AIF_FORMAT(a)) )
	{
	case AIF_INTEGER:
		res = _aif_to_longest(AIF_DATA(a), AIF_LEN(a), val);
		break;

	case AIF_FLOATING:
		if ( _aif_to_doublest(AIF_DATA(a), AIF_LEN(a), &d) < 0 )
			return -1;

		*val = (AIFLONGEST)d;
		res = 0;
		break;

	default:
		SetAIFError(AIFERR_TYPE, NULL);
		return -1;
	}

	return res;
}

/*
 * Convert an AIF floating data type to the largest native
 * floating poing representation available.
 */
int
_aif_to_doublest(char *data, int len, AIFDOUBLEST *val)
{
	union ieee	f;

	if ( data == NULL || val == (AIFDOUBLEST *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return -1;
	}

	ResetAIFError();

        AIFNormalise(f.bytes, len, data, len);

	if ( len == sizeof(float) )
		*val = (AIFDOUBLEST)f.f;
	else if ( len == sizeof(double) )
		*val = (AIFDOUBLEST)f.d;
	else if ( len == sizeof(AIFDOUBLEST) )
		*val = f.dd;
	else {
		fprintf(stderr, "conversion to float: %d does not match any native precision\n", len);

		*val = 0;
		SetAIFError(AIFERR_CONV, NULL);
		return -1;
	}

	return 0;
}

/*
 * Convert an AIF float to a native single precision floating point value.
 */
int
AIFToFloat(AIF *a, float *val)
{
	int		res;
	AIFDOUBLEST	dd;

	if ( a == (AIF *)NULL || val == (float *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return -1;
	}

	switch ( FDSType(AIF_FORMAT(a)) )
	{
	case AIF_INTEGER:
		res = _aif_int_to_doublest(AIF_DATA(a), AIF_LEN(a), &dd);
		break;

	case AIF_FLOATING:
		res = _aif_to_doublest(AIF_DATA(a), AIF_LEN(a), &dd);
		break;

	default:
		SetAIFError(AIFERR_TYPE, NULL);
		return -1;
	}

	if ( res < 0 )
		return -1;

	*val = (float)dd; /* possible loss of precision */
	return 0;
}

/*
 * Convert an AIF float to a native double precision floating point value.
 */
int
AIFToDouble(AIF *a, double *val)
{
	AIFDOUBLEST	d;

	if ( a == (AIF *)NULL || val == (double *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return -1;
	}

	if  ( AIFToDoublest(a, &d) < 0 )
		return -1;

	*val = (double)d; /* possible loss of precision */

	return 0;
}

/*
 * Convert an AIF value to a native floating point value using the
 * largest precision available. Implicitly converts an integer to
 * a float.
 */
int
AIFToDoublest(AIF *a, AIFDOUBLEST *val)
{
	int	res;

	if ( a == (AIF *)NULL || val == (AIFDOUBLEST *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return -1;
	}

	switch ( FDSType(AIF_FORMAT(a)) )
	{
	case AIF_INTEGER:
		res = _aif_int_to_doublest(AIF_DATA(a), AIF_LEN(a), val);
		break;

	case AIF_FLOATING:
		res = _aif_to_doublest(AIF_DATA(a), AIF_LEN(a), val);
		break;

	default:
		SetAIFError(AIFERR_TYPE, NULL);
		return -1;
	}

	return res;
}

/*
 * Convert an AIF integer to and AIF float using largest precision
 * available.
 */
int
_aif_int_to_doublest(char *data, int len, AIFDOUBLEST *val)
{
	AIFLONGEST	l;

	if ( _aif_to_longest(data, len, &l) < 0 )
		return -1;

	*val = (AIFDOUBLEST)l;

	return 0;
}

/*
 * Convert AIF integer to AIF float. Result is always f8.
 */
int
_aif_int_to_aif_float(char **data, char *d, int l)
{
	AIFDOUBLEST	vd;

	_aif_int_to_doublest(d, l, &vd);

	if ( _doublest_to_aif(data, sizeof(double), vd) < 0 )
		return -1;

	return sizeof(double);
}

/*
 * Convert AIF float to AIF integer. Result is always is4 (for convenience).
 */
int
_aif_float_to_aif_int(char **f, char *d, int l)
{
	int		vi;
	AIFDOUBLEST	vd;

	_aif_to_doublest(d, l, &vd);

	vi = (int)vd;

	if ( _longest_to_aif(f, sizeof(int), vi) < 0 )
		return -1;

	return sizeof(int);
}

/*
 * Convert number to ascii.
 */
char
ToAscii(int n, int base)
{
	if ( base < 10 || n < 10 )
		return (n & 0xf) + '0';

	return (n & 0xf) - 10 + 'a';
}

/*
 * Create a void AIF object.
 */
AIF *
VoidToAIF(char *data, int len)
{
	AIF *	a;

	a = NewAIF(0, len);

	AIF_FORMAT(a) = strdup(AIF_VOID_TYPE(len));

	if ( data != NULL && len > 0 )
		memcpy(AIF_DATA(a), data, len);

	return a;
}

/*
 * Convert an AIF void object to its native representation.
 */
int
AIFToVoid(AIF *a, char *data, int len)
{
	int	size;

	if ( data == NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return -1;
	}

	ResetAIFError();

	if ( len > 0 )
	{
		size = FDSTypeSize(AIF_FORMAT(a));
		memcpy(data, AIF_DATA(a), size > len ? size : len);
	}

	return 0;
}

/*
 * Coerce an AIF object into type t if possible.
 */
AIF *
AIFCoerce(AIF *a, char *t)
{
	int		tlen;
	int		alen;
	int		atype;
	AIFDOUBLEST	dval;
	char *		rd = NULL;
	AIF *		na;

	ResetAIFError();

	tlen = FDSTypeSize(t);
	alen = AIF_LEN(a);

	switch ( FDSType(t) )
	{
	case AIF_INTEGER:
		switch ( FDSType(AIF_FORMAT(a)) )
		{
		case AIF_INTEGER:
			if ( tlen > alen )
			{
				rd = (char *)_aif_alloc(tlen);
				memcpy(rd, AIF_DATA(a), tlen);
				return MakeAIF(strdup(t), rd);
			}
			else if ( tlen < alen )
			{
				SetAIFError(AIFERR_TYPE, NULL);
				return (AIF *)NULL;
			}

			return CopyAIF(a);

		case AIF_FLOATING:
			if ( _aif_float_to_aif_int(&rd, AIF_DATA(a), alen) < 0 )
				return (AIF *)NULL;

			return MakeAIF(strdup("is4"), rd);

		default:
			SetAIFError(AIFERR_TYPE, NULL);
			return (AIF *)NULL;
		}
		break;

	case AIF_FLOATING:
		switch ( atype = AIFType(a) )
		{
		case AIF_INTEGER:
		case AIF_FLOATING:
			if ( atype == AIF_INTEGER )
			{
				/*
				** Convert integer to double.
				*/
				if ( _aif_int_to_doublest(AIF_DATA(a), AIF_LEN(a), &dval) < 0 )
					return (AIF *)NULL;

				alen = sizeof(AIFDOUBLEST);
			}
			else
			{
				/*
				** Convert AIF float to double
				*/
				if ( _aif_to_doublest(AIF_DATA(a), AIF_LEN(a), &dval) < 0 )
					return (AIF *)NULL;
			}

			na = NewAIF(0, tlen);

			AIF_FORMAT(na) = strdup(AIF_FLOATING_TYPE(tlen));

			/*
			** Now take care of floating precision
			*/

			if ( _doublest_to_aif(&AIF_DATA(na), tlen, dval) < 0 )
				return (AIF *)NULL;

			return na;

		default:
			SetAIFError(AIFERR_TYPE, NULL);
			return (AIF *)NULL;
		}
		break;

	default:
		SetAIFError(AIFERR_TYPE, NULL);
		return (AIF *)NULL;
	}

	return na;
}

/* 
 * build an AIF from two ascii strings. 
 */
AIF *
AsciiToAIF(char *format, char *data)
{
	int	flen = strlen(format);
	int	dlen = strlen(data);
	long	acc;
	AIF *	a;
	char *	bdata;
	char *	bdp;
	char *	dp;
	char	buf[2];

	ResetAIFError();

	if ( dlen % 2 != 0 )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return (AIF *)NULL;
	}

	bdata = (char *) _aif_alloc(dlen/2);
	bdp = bdata;

	buf[1] = 0;

	dp = data;

	/*
	** convert two chars of data to one byte of bdata
	*/
	while ( *dp )
	{
		buf[0] = *dp++;
		acc = strtol(buf, 0, 16) << 4;
		buf[0] = *dp++;
		acc += strtol(buf, 0, 16);
		*bdp++ = (char) (acc & 0xff);
	}

	a = NewAIF(flen + 1, dlen/2);

	memcpy(AIF_DATA(a), bdata, AIF_LEN(a));
	memcpy(AIF_FORMAT(a), format, flen);

	_aif_free(bdata);

	return a;
}

/************************************
 *                                  *
 *   AIFToStr()-related functions   *
 *                                  *
 ************************************/

void
_str_init()
{
	if ( _str_buf_len == 0 )
	{
		_str_buf = _aif_alloc(BUFSIZ);
		_str_buf_len = BUFSIZ;
	}

	_str_pos = _str_buf;
}

void
_str_cat(char *str)
{
	int	len = strlen(str);

	while ( _str_pos + len + 1 > _str_buf + _str_buf_len )
	{
		int offset = _str_pos - _str_buf;
		_str_buf_len += BUFSIZ;
		_str_buf = _aif_resize(_str_buf, _str_buf_len);
		_str_pos = _str_buf + offset;
	}

	memcpy(_str_pos, str, len);
	_str_pos += len;
	*_str_pos = '\0';
}

void
_str_add(char ch)
{
	while ( _str_pos + 2 > _str_buf + _str_buf_len )
	{
		int offset = _str_pos - _str_buf;
		_str_buf_len += BUFSIZ;
		_str_buf = _aif_resize(_str_buf, _str_buf_len);
		_str_pos = _str_buf + offset;
	}

	*_str_pos++ = ch;
	*_str_pos = '\0';
}

char *
_str_get(void)
{
	return strdup(_str_buf);
}

int
_aif_bool_to_str(int depth, char **fds, char **data)
{
	int		bytes = sizeof(int);
	int		i;
	char		buf[BUFSIZ];
	AIFLONGEST	lli;

	(*fds)++;

	if ( _aif_to_longest(*data, bytes, &lli) < 0 )
		return -1;

	i = (int)lli;

	if ( i == -1 )
		snprintf(buf, BUFSIZ-1, "true");
	else if ( i == 0 )
		snprintf(buf, BUFSIZ-1, "false");
	else
		snprintf(buf, BUFSIZ-1, "%d", i);

	_str_cat(buf);
	*data += bytes;

	return 0;
}

int
AIFBoolToStr(char **str, int depth, AIF *a)
{
	char *	fmt;
	char *	data;

	if ( a == (AIF *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return -1;
	}

	fmt = AIF_FORMAT(a);
	data = AIF_DATA(a);

	_str_init();

	if ( _aif_bool_to_str(depth, &fmt, &data) < 0 )
		return -1;

	*str = _str_get();

	return 0;
}

int
_aif_char_to_str(int depth, char **fds, char **data)
{
	char	c;
	char	buf[BUFSIZ];

	(*fds)++;

	if ( _aif_to_char(*data, &c) < 0 )
		return -1;

	if ( c >= ' ' && c <= '~' )
		snprintf(buf, BUFSIZ-1, "%d '%c'", c, c);
	else
		snprintf(buf, BUFSIZ-1, "%d '\\%03o'", c & 0xff, c & 0xff);

	_str_cat(buf);
	*data += sizeof(char);

	return 0;
}

int
_aif_int_to_str(int depth, char **fds, char **data)
{
	int		bytes = FDSTypeSize(*fds);
	int		isSigned = FDSIsSigned(*fds);
	int		i;
	AIFLONGEST	lli;
	char	buf[BUFSIZ];

	*fds += 2;
	*fds = _fds_skipnum(*fds);

	if ( _aif_to_longest(*data, bytes, &lli) < 0 )
		return -1;

	if ( bytes <= sizeof(int) )
	{
		i = (int)lli;
		snprintf(buf, BUFSIZ-1, isSigned ? "%d" : "%u", i);
	}
	else if ( bytes == sizeof(long) )
	{
#ifdef CC_HAS_LONG_LONG
		long		li;

		li = (long)lli;
		snprintf(buf, BUFSIZ-1, isSigned ? "%ld" : "%lu" , li);
#else /* CC_HAS_LONG_LONG */
		snprintf(buf, BUFSIZ-1, isSigned ? "%ld" : "%lu" , lli);
#endif /* CC_HAS_LONG_LONG */
	}
	else
	{
#ifdef CC_HAS_LONG_LONG
	#ifdef WIN32
		snprintf(buf, BUFSIZ-1, isSigned ? "%I64d" : "%I64u" , lli);
	#else /* WIN32 */
		snprintf(buf, BUFSIZ-1, isSigned ? "%lld" : "%llu" , lli);
	#endif /* WIN32 */
#else /* CC_HAS_LONG_LONG */
		snprintf(buf, BUFSIZ-1, isSigned ? "%ld" : "%lu" , lli);
#endif /* CC_HAS_LONG_LONG */
	}

	_str_cat(buf);

	if ( bytes > sizeof(AIFLONGEST) )
	{
		snprintf(buf, BUFSIZ-1, "<truncated from %d bytes>", bytes);
		_str_cat(buf);
	}

	*data += bytes;

	return 0;
}

int
AIFIntToStr(char **str, int depth, AIF *a)
{
	char *	fmt;
	char *	data;

	if ( a == (AIF *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return -1;
	}

	fmt = AIF_FORMAT(a);
	data = AIF_DATA(a);

	_str_init();

	if ( _aif_int_to_str(depth, &fmt, &data) < 0 )
		return -1;

	*str = _str_get();

	return 0;
}

int
_aif_float_to_str(int depth, char **fds, char **data)
{
	AIFDOUBLEST	d;
	int		bytes = FDSTypeSize(*fds);
	char		buf[BUFSIZ];

	*fds += 1;
	*fds = _fds_skipnum(*fds);

	if ( _aif_to_doublest(*data, bytes, &d) < 0 )
		return -1;

	if ( bytes <= sizeof(float) )
#ifdef HAVE_LONG_DOUBLE
		snprintf(buf, BUFSIZ-1, "%.8Lg", d);
#else /* HAVE_LONG_DOUBLE */
		snprintf(buf, BUFSIZ-1, "%.8g", (float)d);
#endif /* HAVE_LONG_DOUBLE */
	else if ( bytes == sizeof(double) )
#ifdef HAVE_LONG_DOUBLE
		snprintf(buf, BUFSIZ-1, "%.16Lg", d);
	else
		snprintf(buf, BUFSIZ-1, "%.34Lg", d);
#else /* HAVE_LONG_DOUBLE */
		snprintf(buf, BUFSIZ-1, "%.16lg", (double)d);
#endif /* HAVE_LONG_DOUBLE */

	_str_cat(buf);
	*data += bytes;

	return 0;
}

int
AIFFloatToStr(char **str, int depth, AIF *a)
{
	char *	fmt;
	char *	data;

	if ( a == (AIF *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return -1;
	}

	fmt = AIF_FORMAT(a);
	data = AIF_DATA(a);

	_str_init();

	if ( _aif_float_to_str(depth, &fmt, &data) < 0 )
		return -1;

	*str = _str_get();

	return 0;
}

int
_aif_pointer_to_str(int depth, char **fds, char **data)
{
	int 	index;
	char 	buf[BUFSIZ];

	(*fds)++; /* past "^" */
	_str_cat("^");

	switch ( (int)**data )
	{
	case AIF_PTR_NIL: /* nil value */
		if ( depth == 1 )
			depth = -1;
		else if ( depth > 1 )
			depth--;

		(*data)++;
		_str_cat("null");
		_fds_advance(fds);
		break;

	case AIF_PTR_NORMAL: /* normal value */
		(*data)++;

		if ( depth == -1 )
		{
			_str_cat("<normal>");
			_fds_skip_data(fds, data);
			break;
		}
		else if ( depth == 1 )
			depth = -1;
		else if ( depth > 1 )
			depth--;
		
		_aif_to_str(depth, fds, data);
		break;

	case AIF_PTR_NAME: /* normal value, but remember it */
		(*data)++;
		_ptrname_to_int(*data, &index);
		*data += 4;
		_aif_values_seen[index] = *data;
		snprintf(buf, BUFSIZ-1, "<%d:>", index);
		_str_cat(buf);

		if ( depth == -1 )
		{
			_fds_skip_data(fds, data);
			break;
		}
		else if ( depth == 1 )
			depth = -1;
		else if ( depth > 1 )
			depth--;

		_aif_to_str(depth, fds, data);
		break;

	case AIF_PTR_REFERENCE: /* reference to remembered value */
		if ( depth == 1 )
		{
			char * target;

			depth = -1;
			target = _find_target(data, 0);
			_aif_to_str(depth, fds, &target);
		}
		else if ( depth > 1 )
		{
			char * target;

			depth--;
			target = _find_target(data, 0);
			_aif_to_str(depth, fds, &target);
		}
		else
		{
			(*data)++;
			_ptrname_to_int(*data, &index);
			*data += 4;
			snprintf(buf, BUFSIZ-1, "<=%d>", index);
			_str_cat(buf);
			_fds_advance(fds);
		}

		break;

	case AIF_PTR_INVALID: /* pointer is invalid */
		(*data)++;
		_str_cat("<invalid>");
		_fds_advance(fds);
		break;
	}

	return 0;
}

int
AIFPointerToStr(char **str, int depth, AIF *a)
{
	char *	fmt;
	char *	data;

	if ( a == (AIF *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return -1;
	}

	fmt = AIF_FORMAT(a);
	data = AIF_DATA(a);

	_str_init();

	if ( _aif_pointer_to_str(depth, &fmt, &data) )
		return -1;
	
	*str = _str_get();

	return 0;
}

int
_aif_region_to_str(int depth, char **fds, char **data)
{
	int		i;
	int		rank;
	int		size;
	AIFLONGEST	min;
	AIFLONGEST	max;
	char		buf[BUFSIZ];

	rank = _fds_getnum(*fds + 1);
	size = FDSTypeSize(FDSBaseType(*fds));

	_str_cat("[");

	for ( i = 0 ; i < rank ; i++ )
	{
		if ( i != 0 && rank > 1 )
			_str_cat(", ");

		if
		(
			_aif_to_longest(*data + 2 * i * size, size, &min) < 0
			||
			_aif_to_longest(*data + (2 * i + 1) * size, size, &max) < 0
		)
			return -1;

#ifdef CC_HAS_LONG_LONG
		snprintf(buf, BUFSIZ-1, "%lld..%lld", min, max);
#else /* CC_HAS_LONG_LONG */
		snprintf(buf, BUFSIZ-1, "%ld..%ld", min, max);
#endif /* CC_HAS_LONG_LONG */

		_str_cat(buf);
	}

	_str_cat("]");

	_fds_advance(fds);

	return 0;
}

int
_aif_array_to_str(int depth, char **fds, char **data)
{
	int		i;
	AIFIndex *	ix;
	char *		fmt;

	ix = FDSArrayIndexInit(*fds);

	_str_cat("[");

	for ( i = 0 ; i < ix->i_nel ; i++ )
	{
		fmt = ix->i_btype;

		/*
                if ( ix->i_rank == 1 )
			snprintf(_str_buf, BUFSIZ-1, "(%d)", i + ix->i_min[0]);
		*/

		_aif_to_str(depth, &fmt, data);

		if ( i < ix->i_nel - 1 )
			_str_cat(", ");

	}

	_str_cat("]");

	AIFArrayIndexFree(ix);

	_fds_advance(fds); /* skip over entire array descriptor */

	return 0;
}

int
AIFArrayToStr(char **str, int depth, AIF *a)
{
	char *	fmt;
	char *	data;

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

	fmt = AIF_FORMAT(a);
	data = AIF_DATA(a);

	_str_init();

	if ( _aif_array_to_str(depth, &fmt, &data) < 0 )
		return -1;

	*str = _str_get();

	return 0;
}

int
AIFArrayIndexToStr(char **str, AIFIndex *ix)
{
	int	i;
	char	buf[BUFSIZ];

	_str_init();

	for ( i = 0 ; i < ix->i_rank ; i++ )
	{
		snprintf(buf, BUFSIZ-1, "[%d]", ix->i_index[i]);
		_str_cat(buf);
	}

	*str = _str_get();

	return 0;
}

int
_aif_struct_to_str(int depth, char **fds, char **data)
{
	char *	inFds;	
	char	buf[BUFSIZ];
	int	isStruct = 1;	/* assume it is a struct */
				/* to avoid printing the semicolons if
				   it is a struct */

	_str_cat("{");

	(*fds)++; /* past open brace */

	/*
	** past nnn=
	*/

	_fds_skipid(fds);

	while ( **fds != FDS_STRUCT_END )
	{
		if ( isStruct 	&& (*fds)[0] == FDS_STRUCT_ACCESS_SEP
				&& (*fds)[1] == FDS_STRUCT_ACCESS_SEP
				&& (*fds)[2] == FDS_STRUCT_ACCESS_SEP )
		{
			(*fds) += 3;
			break;
		}
		else if ( **fds == FDS_STRUCT_ACCESS_SEP )
		{
			isStruct = 0;	/* it is a class */
			
			(*fds)++;
			_str_cat("; ");
			continue;
		}

		if ( (inFds = strchr(*fds, FDS_STRUCT_FIELD_NAME_END)) == NULL )
			break;

		*inFds = 0; /* temporarily */

		snprintf(buf, BUFSIZ-1, "%s = ", *fds);
		_str_cat(buf);

		*inFds++ = FDS_STRUCT_FIELD_NAME_END;

		*fds = inFds; /* to start of field */

		_aif_to_str(depth, fds, data);

		if ( **fds == FDS_STRUCT_FIELD_SEP )
		{
			(*fds)++;
			_str_cat(", ");
		}
	}

	_str_cat("}");

	(*fds)++; /* past close brace */

	return 0;
}

int
AIFStructToStr(char **str, int depth, AIF *a)
{
	char *	fmt;
	char *	data;

	if ( a == (AIF *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return -1;
	}

	fmt = AIF_FORMAT(a);
	data = AIF_DATA(a);

	_str_init();

	if ( _aif_struct_to_str(depth, &fmt, &data) < 0 )
		return -1;

	*str = _str_get();

	return 0;
}

int
_aif_reference_to_str(int depth, char **fds, char **data)
{
	int	index;
	char *	effectiveFds;

	(*fds)++; /* past ">" */
	index = atoi(*fds);
	*fds = _fds_skipnum(*fds); /* past name */
	(*fds)++; /* past "/" */
	effectiveFds = _aif_types_seen[index];

	_aif_to_str(depth, &effectiveFds, data);

	return 0;
}

int
AIFReferenceToStr(char **str, int depth, AIF *a)
{
	char *	fmt;
	char *	data;

	if ( a == (AIF *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return -1;
	}

	fmt = AIF_FORMAT(a);
	data = AIF_DATA(a);

	_str_init();

	if ( _aif_reference_to_str(depth, &fmt, &data) < 0 )
		return -1;

	*str = _str_get();

	return 0;
}

int
_aif_string_to_str(int depth, char **fds, char **data)
{
	int	bytes;
	char *	buf;

	(*fds)++; /* past "s" */
	bytes = (*(*data)++ & 0xff) << 8;
	bytes += *(*data)++ & 0xff;

	buf = _aif_alloc(bytes+3);

	*buf = '"';
	strncpy(&buf[1], *data, bytes);
	*(buf + bytes + 1) = '"';
	*(buf + bytes + 2) = 0; /* terminator */

	_str_cat(buf);

	*data += bytes;

	_aif_free(buf);

	return 0;
}

int
_aif_void_to_str(int depth, char **fds, char **data)
{
	int bytes = FDSTypeSize(*fds);

	*fds += 1; /* past "v" */
	*fds = _fds_skipnum(*fds);

	*data += bytes;

	_str_cat("VOID");
	return 0;
}

int
AIFVoidToStr(char **str, int depth, AIF *a)
{
	char *	fmt;
	char *	data;

	if ( a == (AIF *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return -1;
	}

	fmt = AIF_FORMAT(a);
	data = AIF_DATA(a);

	_str_init();

	if ( _aif_void_to_str(depth, &fmt, &data) < 0 )
		return -1;

	*str = _str_get();

	return 0;
}

int
_aif_union_to_str(int depth, char **fds, char **data)
{	
	char *	inFds;	
	char *	temp;
	char *	dt;
	int 	size;
	int	union_size;
	char	buf[BUFSIZ];

	_str_cat("{");

	union_size = FDSTypeSize(*fds);

	(*fds)++; /* past open brace */

	/*
	** past nnn=
	*/

	_fds_skipid(fds);

	while ( **fds != FDS_UNION_END )
	{
		if ( (inFds = strchr(*fds, FDS_UNION_FIELD_NAME_END)) == NULL )
			break;

		*inFds = 0; /* temporarily */

		snprintf(buf, BUFSIZ-1, "%s = ", *fds);
		_str_cat(buf);

		*inFds++ = FDS_UNION_FIELD_NAME_END;

		*fds = inFds; /* to start of field */

		size = FDSTypeSize(*fds);
		temp = _aif_alloc( sizeof(char) * size );
		dt = temp;
		memcpy(dt, *data, size);

		_aif_to_str(depth, fds, &dt);
		_aif_free(temp);

		if ( **fds == FDS_UNION_FIELD_SEP )
		{
			(*fds)++;
			_str_cat(", ");
		}
	}

	_str_cat("}");
	*data += union_size;

	(*fds)++; /* past close brace */

	return 0;
}

int
AIFUnionToStr(char **str, int depth, AIF *a)
{
	char *	fmt;
	char *	data;

	if ( a == (AIF *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return -1;
	}

	fmt = AIF_FORMAT(a);
	data = AIF_DATA(a);

	_str_init();

	if ( _aif_union_to_str(depth, &fmt, &data) < 0 )
		return -1;

	*str = _str_get();

	return 0;
}

int
_aif_function_to_str(int depth, char **fds, char **data)
{	
	char *	p;
	char	end[2] = {FDS_FUNCTION_ARG_END, 0};

	(*fds)++; /* past "&" */

	p = _fds_skipto(*fds, end);

	_str_cat(++p);
	_str_cat(" ");
	_str_cat(*data);
	_str_cat("(");

	while ( **fds != FDS_FUNCTION_ARG_END )
	{
		if ( **fds == FDS_UNION_FIELD_SEP )
			_str_cat(", ");
		else
			_str_add(**fds);

		(*fds)++;
	}

	_str_cat(")");

	(*fds)++; /* past arg end */

	_fds_advance(fds);
	_fds_skip_data(fds, data);

	return 0;
}

int
AIFFunctionToStr(char **str, int depth, AIF *a)
{
	char *	fmt;
	char *	data;

	if ( a == (AIF *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return -1;
	}

	fmt = AIF_FORMAT(a);
	data = AIF_DATA(a);

	_str_init();

	if ( _aif_function_to_str(depth, &fmt, &data) < 0 )
		return -1;

	*str = _str_get();

	return 0;
}

int
_aif_enum_to_str(int depth, char **fds, char **data)
{	
	int		bytes = FDSTypeSize(*fds);
	AIFLONGEST	lli;
	int		i;
	char *		string = NULL;

	/* check whether it is an empty enum or not */
	char * 		f = *fds;

	while ( *f != FDS_ID )
		f++;

	f++;
	if ( *f == FDS_ENUM_END )
	{	/* it is an empty enum */
		_str_cat("0");	
		*data += bytes;
		_fds_advance(fds);
		return 0;
	}

	if ( _aif_to_longest(*data, bytes, &lli) < 0 )
		return -1;

	i = (int) lli;

	if ( FDSEnumConstByValue(*fds, &string, i) < 0)
		return -1;
	
	_str_cat(string);
	_aif_free(string);

	*data += bytes;
	_fds_advance(fds);

	return 0;
}

int
AIFEnumToStr(char **str, int depth, AIF *a)
{
	char *	fmt;
	char *	data;

	if ( a == (AIF *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return -1;
	}

	fmt = AIF_FORMAT(a);
	data = AIF_DATA(a);

	_str_init();

	if ( _aif_enum_to_str(depth, &fmt, &data) < 0 )
		return -1;

	*str = _str_get();

	return 0;
}

int
_aif_name_to_str(int depth, char **fds, char **data)
{	
	int	index;

	(*fds)++; /* past "%" */
	index = atoi(*fds);
	*fds = _fds_skipnum(*fds); /* past name */
	(*fds)++; /* past "/" */
	_aif_types_seen[index] = *fds;

	return _aif_to_str(depth, fds, data);
}

/* 
 * Prints the object in human-readable form to fp, advances fds and data to
 * the first location after the part printed.  Arithmetic values are given at
 * least len bytes of significance, more if the fds indicates.
 */
int
_aif_to_str(int depth, char **fds, char **data)
{
	char * tmp;

	_fds_resolve(fds);

	switch ( FDSType(*fds) )
	{
	case AIF_BOOLEAN:
		return _aif_bool_to_str(depth, fds, data); 

	case AIF_CHARACTER:
		return _aif_char_to_str(depth, fds, data);

	case AIF_INTEGER:
		return _aif_int_to_str(depth, fds, data);

	case AIF_FLOATING:
		return _aif_float_to_str(depth, fds, data);

	case AIF_ARRAY:
		return _aif_array_to_str(depth, fds, data);

	case AIF_POINTER:
		return _aif_pointer_to_str(depth, fds, data);

	case AIF_REGION:
		return _aif_region_to_str(depth, fds, data);

	case AIF_STRUCT:
		return _aif_struct_to_str(depth, fds, data);

	case AIF_NAME:
		return _aif_name_to_str(depth, fds, data);

	case AIF_REFERENCE:
		tmp = _fds_lookup(fds);
		return _aif_to_str(depth, &tmp, data);

	case AIF_STRING:
		return _aif_string_to_str(depth, fds, data);

	case AIF_ENUM:
		return _aif_enum_to_str(depth, fds, data);

	case AIF_UNION:
		return _aif_union_to_str(depth, fds, data);

	case AIF_VOID:
		return _aif_void_to_str(depth, fds, data);

	case AIF_FUNCTION:
		return _aif_function_to_str(depth, fds, data);

	default:
		SetAIFError(AIFERR_TYPE, NULL);
		break;
	}

	return -1;
}

int
AIFToStr(char **str, int depth, AIF *a)
{
	char *	fmt;
	char *	data;

	if ( a == (AIF *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
		return -1;
	}

	fmt = AIF_FORMAT(a);
	data = AIF_DATA(a);

	_str_init();

	if ( _aif_to_str(depth, &fmt, &data) < 0 )
		return -1;

	*str = _str_get();

	return 0;
}

/* Convert a pointer name into an int (4 bytes) */
void
_ptrname_to_int(char *name, int *number)
{
	int	s;
	int 	i = 0;

	for ( s = 0 ; s < 4 ; s++ )
		i = i * 0x100 + (name[s] & 0xff);

	*number = i;
}

/* Convert an int (4 bytes) into a string for the pointer name */
void
_int_to_ptrname(int number, char *name)
{
	union ieee	f;
	f.i = number;

	_aif_normalise(name, 4, f.bytes, 4);
}

