/******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly 
 * marked, so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 ******************************************************************************/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

#include "compat.h"
#include "bitset.h"

bitset *
bitset_new(int size)
{
	bitset *	p;
	
	p = malloc(sizeof(bitset));
	
	p->bs_nbits = size;
	p->bs_bits = BITVECTOR_CREATE(size);
	
	return p;
}

void		
bitset_free(bitset *p)
{
	BITVECTOR_FREE(p->bs_bits);
	free(p);
}

bitset *	
bitset_copy(bitset *p)
{
	bitset *np = bitset_new(p->bs_nbits);
	
	BITVECTOR_COPY(np->bs_bits, p->bs_bits);
	
	return np;
}

int	
bitset_isempty(bitset *p)
{
	return BITVECTOR_ISEMPTY(p->bs_bits);
}


void	
bitset_clear(bitset *p)
{
	return BITVECTOR_CLEAR(p->bs_bits);
}

bitset *	
bitset_and(bitset *p1, bitset *p2)
{
	bitset *np = bitset_new(MAX(p1->bs_nbits, p2->bs_nbits));
	
	BITVECTOR_AND(np->bs_bits, p1->bs_bits, p2->bs_bits);
	
	return np;
}

void
bitset_andeq(bitset *p1, bitset *p2)
{
	/*
	 * Silently fail if sets are different sizes
	 * */
	if (p1->bs_nbits != p2->bs_nbits)
		return;
	
	BITVECTOR_ANDEQ(p1->bs_bits, p2->bs_bits);
}

bitset *	
bitset_or(bitset *p1, bitset *p2)
{
	bitset *np = bitset_new(MAX(p1->bs_nbits, p2->bs_nbits));
	
	BITVECTOR_OR(np->bs_bits, p1->bs_bits, p2->bs_bits);
	
	return np;
}

void
bitset_oreq(bitset *p1, bitset *p2)
{
	/*
	 * Silently fail if sets are different sizes
	 * */
	if (p1->bs_nbits != p2->bs_nbits)
		return;
	
	BITVECTOR_OREQ(p1->bs_bits, p2->bs_bits);
}

/*
 * Bitvector always rounds up the number of bits to the nearest
 * chunk size. We need to reset the top most bits or they
 * will be incorrectly tested by isempty().
 */
static void
_invert_helper(int nb, BITVECTOR_TYPE bv)
{
	int	b;
	
	for (b = nb; b < BV_BITSIZE(bv); b++)
		BITVECTOR_UNSET(bv, b);
}

void		
bitset_invert(bitset *p)
{
	BITVECTOR_INVERT(p->bs_bits);
	_invert_helper(p->bs_nbits, p->bs_bits);
}
	
/**
 * Add a bit to the set. Bits are numbered from 0.
 */
void		
bitset_set(bitset *p, int proc)
{
	if (proc < 0 || proc >= p->bs_nbits)
		return;
		
	BITVECTOR_SET(p->bs_bits, proc);
}

void		
bitset_unset(bitset *p, int proc)
{
	if (proc < 0 || proc >= p->bs_nbits)
		return;
		
	BITVECTOR_UNSET(p->bs_bits, proc);
}

int		
bitset_test(bitset *p, int proc)
{
	if (proc < 0 || proc >= p->bs_nbits)
		return 0;
		
	return BITVECTOR_GET(p->bs_bits, proc);
}

static char tohex[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

/**
 * Return a string representation of a bitset. We use hex to compress
 * the string somewhat and drop leading zeros.
 * 
 * Format is "nnnnnnnn:bbbbbbb..." where "nnnnnnnn" is a hex representation of the
 * actual number of bits, and "bbbbb...." are bits in the
 * set (in hex).
 */
char *
bitset_to_str(bitset *p)
{
	int				n;
	int				nonzero = 0;
	int				bytes;
	int				pbit;
	int				bit;
	char *			str;
	char *			s;
	unsigned char	byte;
	
	if (p == NULL)
		return strdup("0:0");
		
	/*
	 * Find out how many bytes needed (rounded up)
	 */
	bytes = (p->bs_nbits >> 3) + 1;

	str = (char *)malloc((bytes * 2) + 8 + 2);
	
	/*
	 * Start with actual number of bits (silently truncate to 32 bits)
	 */
	n = sprintf(str, "%X", p->bs_nbits & 0xffffffff);
	
	s = str + n;
	
	*s++ = ':';
	
	for (pbit = (bytes << 3) - 1; pbit > 0; ) {
		for (byte = 0, bit = 3; bit >= 0; bit--, pbit--) {
			if (pbit < p->bs_nbits && BITVECTOR_GET(p->bs_bits, pbit)) {
				byte |= (1 << bit);
				nonzero = 1;
			}
		}
		
		if (nonzero) {
			*s++ = tohex[byte & 0x0f];
		}
	}
	
	if (!nonzero)
		*s++ = '0';
	
	*s = '\0';
	
	return str;
}

/**
 * Convert string into a bitset. Inverse of bitset_to_str().
 * 
 */
bitset *
str_to_bitset(char *str)
{
	int			nprocs;
	int			n;
	int			pos;
	int			b;
	char *		end;
	bitset *	p;
	
	if (str == NULL)
		return NULL;
		
	end = &str[strlen(str) - 1];
	
	for (nprocs = 0; *str != ':' && *str != '\0' && isxdigit(*str); str++) {
		nprocs <<= 4;
		nprocs += digittoint(*str);
	}
	
	if (*str != ':' || nprocs == 0)
		return NULL;
		
	p = bitset_new(nprocs);
	
	/*
	 * Easier if we start from end
	 */
	for (pos = 0; end != str && isxdigit(*end); end--) {
		b = digittoint(*end);
		for (n = 0; n < 4; n++, pos++) {
			if (b & (1 << n)) {
				bitset_set(p, pos);
			}
		}
	}
	
	if (end != str) {
		bitset_free(p);
		return NULL;
	}
	
	return p;
}

int
emit_range(char ** str, char sep, int lower, int upper)
{
	int			n;
	
	if (lower < 0 || upper < lower)
		return 0;
		
	if (sep)
		*(*str)++ = sep;
		
	if (lower != upper) {
		n = sprintf(*str, "%d-%d", lower, upper);
	} else {
		n = sprintf(*str, "%d", lower);
	}
	
	*str += n;
	
	return 1;
}

/*
 * Convert bitset to set notation of the form
 * 
 * 	{0-2,4,5-100}
 */
char *
bitset_to_set(bitset *p)
{
	int			proc;
	int			lower;
	int			upper;
	char			sep = 0;
	char *		str;
	char *		s;
	
	if (p == NULL)
		return strdup("{}");
		
	str = s = (char *)malloc(p->bs_nbits * 2 + 3);

	*s++ = '{';
	
	for (proc = 0, lower = -1, upper = -1; proc < p->bs_nbits; proc++) {	
		if (bitset_test(p, proc)) {
			if (lower < 0)
				lower = proc;
			
			upper = proc;
		} else {
			if (emit_range(&s, sep, lower, upper))
				sep = ',';
			lower = proc + 1;
		}
	}
	
	emit_range(&s, sep, lower, upper);
	
	*s++ = '}';
	*s = '\0';
	
	return str;
}

/**
 * Number of bits in the set (as opposed to the total size of the set)
 */
int
bitset_size(bitset *p)
{
	int	i;
	int	size = 0;
	
	for (i = 0; i < p->bs_nbits; i++)
		size += BITVECTOR_GET(p->bs_bits, i);
	return size;
}
