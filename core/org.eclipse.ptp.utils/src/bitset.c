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
#include <strings.h>
#include <ctype.h>

#include "compat.h"
#include "bitset.h"
#include "varstr.h"

bitset *
bitset_new(int nbits)
{
	bitset *	b;

	b = malloc(sizeof(bitset));

	b->bs_nbits = nbits;
	b->bs_size = BIT_INDEX(nbits-1) + 1;
	b->bs_bits = (bits *)malloc(SIZE_TO_BYTES(b->bs_size));
	memset(b->bs_bits, 0, SIZE_TO_BYTES(b->bs_size));

	return b;
}

void
bitset_free(bitset *b)
{
	free(b->bs_bits);
	free(b);
}

void
bitset_copy(bitset *b1, bitset *b2)
{
	memcpy(b1->bs_bits, b2->bs_bits, MIN(SIZE_TO_BYTES(b1->bs_size), SIZE_TO_BYTES(b2->bs_size)));
}

bitset *
bitset_dup(bitset *b)
{
	bitset *nb = bitset_new(b->bs_nbits);

	bitset_copy(nb, b);

	return nb;
}

int
bitset_isempty(bitset *b)
{
	int	i;

	for (i = 0; i < b->bs_size; i++)
		if (b->bs_bits[i] != 0)
			return 0;

	return 1;
}


void
bitset_clear(bitset *b)
{
	memset(b->bs_bits, 0, SIZE_TO_BYTES(b->bs_size));
}

/*
 * Compute logical AND of bitsets
 *
 * If bitsets are different sizes, create a new bitset that
 * is the same size as the larger of the two. The high bits
 * will be ANDed with 0.
 */
bitset *
bitset_and(bitset *b1, bitset *b2)
{
	bitset *	nb;

	if (b1->bs_size > b2->bs_size) {
		nb = bitset_dup(b1);
		bitset_andeq(nb, b2);
	} else {
		nb = bitset_dup(b2);
		bitset_andeq(nb, b1);
	}

	return nb;
}

/*
 * Compute b1 &= b2
 *
 * If bitsets are different sizes, high bits are assumed
 * to be 0.
 */
void
bitset_andeq(bitset *b1, bitset *b2)
{
	int	i;

	for (i = 0; i < MIN(b1->bs_size, b2->bs_size); i++)
		b1->bs_bits[i] &= b2->bs_bits[i];

	/*
	 * Mask high bits (if any)
	 */
	for ( ; i < b1->bs_size; i++)
		b1->bs_bits[i] = 0;
}

/*
 * Compute b1 &= ~b2
 *
 * If bitsets are different sizes, high bits are assumed
 * to be 0.
 */
void
bitset_andeqnot(bitset *b1, bitset *b2)
{
	int	i;

	for (i = 0; i < MIN(b1->bs_size, b2->bs_size); i++)
		b1->bs_bits[i] &= ~b2->bs_bits[i];

	/*
	 * Mask high bits (if any)
	 */
	for ( ; i < b1->bs_size; i++)
		b1->bs_bits[i] = 0;
}

/*
 * Compute logical OR of bitsets
 */
bitset *
bitset_or(bitset *b1, bitset *b2)
{
	bitset *	nb;

	if (b1->bs_size > b2->bs_size) {
		nb = bitset_dup(b1);
		bitset_oreq(nb, b2);
	} else {
		nb = bitset_dup(b2);
		bitset_oreq(nb, b1);
	}

	return nb;
}

/*
 * Compute b1 |= b2
 */
void
bitset_oreq(bitset *b1, bitset *b2)
{
	int	i;

	for (i = 0; i < MIN(b1->bs_size, b2->bs_size); i++)
		b1->bs_bits[i] |= b2->bs_bits[i];

	/*
	 * Mask out unused high bits
	 */
	if (BIT_IN_OBJ(b1->bs_nbits) != 0)
		b1->bs_bits[b1->bs_size-1] &= (1 << (BIT_IN_OBJ(b1->bs_nbits-1) + 1)) - 1;
}

/*
 * Compute ~b
 */
void
bitset_invert(bitset *b)
{
	int		i;

	for (i = 0; i < b->bs_size; i++)
		b->bs_bits[i] = ~b->bs_bits[i];

	/*
	 * Mask out unused high bits
	 */
	if (BIT_IN_OBJ(b->bs_nbits) != 0)
		b->bs_bits[b->bs_size-1] &= (1 << (BIT_IN_OBJ(b->bs_nbits-1) + 1)) - 1;
}

/*
 * Test if two bitsets are equal
 *
 * Bitsets must be the same size
 */
int
bitset_eq(bitset *b1, bitset *b2)
{
	int	i;

	if (b1->bs_nbits != b2->bs_nbits)
		return 0;

	for (i = 0; i < b1->bs_size; i++)
		if (b1->bs_bits[i] != b2->bs_bits[i])
			return 0;

	return 1;
}

/*
 * Test if two bitsets share any bits
 *
 * If bitsets are different sizes, high bits are assumed
 * to be 0.
 */
int
bitset_compare(bitset *b1, bitset *b2)
{
	int	i;

	for (i = 0; i < MIN(b1->bs_size, b2->bs_size); i++)
		if ((b1->bs_bits[i] & b2->bs_bits[i]) != 0)
			return 1;

	return 0;
}

/**
 * Add a bit to the set. Bits are numbered from 0.
 */
void
bitset_set(bitset *b, int bit)
{
	if (bit < 0 || bit >= b->bs_nbits)
		return;

	b->bs_bits[BIT_INDEX(bit)] |= (1 << BIT_IN_OBJ(bit));
}

void
bitset_unset(bitset *b, int bit)
{
	if (bit < 0 || bit >= b->bs_nbits)
		return;

	b->bs_bits[BIT_INDEX(bit)] &= ~(1 << BIT_IN_OBJ(bit));
}

int
bitset_test(bitset *b, int bit)
{
	bits		mask = (1 << BIT_IN_OBJ(bit));

	if (bit < 0 || bit >= b->bs_nbits)
		return 0;

	return (b->bs_bits[BIT_INDEX(bit)] & mask) == mask;
}

/*
 * Find the first bit set in the bitset.
 *
 * NOTE: ffs() assumes an integer argument. If sizeof(bits) is anything
 * else this will need to be fixed.
 */
int
bitset_firstset(bitset *b)
{
	int	i;

	for (i = 0; i < b->bs_size; i++)
		if (b->bs_bits[i] != 0)
			break;

	if (i == b->bs_size)
		return -1;

	return (SIZE_TO_BYTES(i) << 3) + ffs(b->bs_bits[i]) - 1;
}

static char tohex[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

#define NUM_BYTES(bits) (((bits-1) >> 3) + 1)	/* number of bytes required to store bits */
#define NUM_LEN			8*2						/* number of characters required to store largest length */
#define SEP_LEN			1						/* number of characters for ':' separator */

/**
 * Return a string representation of a bitset. We use hex to compress
 * the string somewhat and drop leading zeros.
 *
 * Format is "NN:HHHHHH..." where "NN" is the actual number of bits in hex,
 * and "HHHHH...." is a hex representation of the bits in the set. The number
 * of characters in the bit string is always rounded to the nearest byte.
 *
 * e.g. "111" -> "3:07"
 * 		"11011010101011101" -> "11:01b55d"
 */
char *
bitset_to_str(bitset *b)
{
	int				n;
	int				bytes;
	int				pbit;
	int				bit;
	char *			str;
	char *			s;
	unsigned char	val;

	if (b == NULL) {
		return strdup("0:00");
	}

	/*
	 * Find out how many bytes needed (rounded up)
	 */
	bytes = NUM_BYTES(b->bs_nbits);

	str = (char *)malloc(bytes*2 + NUM_LEN + SEP_LEN + 1);

	/*
	 * Start with actual number of bits (silently truncate to 32 bits)
	 */
	n = sprintf(str, "%x", b->bs_nbits & 0xffffffff);

	s = str + n;

	*s++ = ':';

	for (pbit = (bytes << 3) - 1; pbit > 0; ) {
		for (val = 0, bit = 3; bit >= 0; bit--, pbit--) {
			if (pbit < b->bs_nbits && bitset_test(b, pbit)) {
				val |= (1 << bit);
			}
		}
		*s++ = tohex[val & 0x0f];
	}

	if (b->bs_nbits == 0) {
		*s++ = '0';
	}

	*s = '\0';

	return str;
}

/**
 * Convert string into a bitset. Inverse of bitset_to_str().
 *
 */
bitset *
str_to_bitset(char *str, char **end)
{
	int			nbits;
	int			bytes;
	int			n;
	int			pos;
	int			b;
	bitset *	bp;

	if (str == NULL) {
		return NULL;
	}

	for (nbits = 0; *str != ':' && *str != '\0' && isxdigit(*str); str++) {
		nbits <<= 4;
		nbits += digittoint(*str);
	}

	bytes = NUM_BYTES(nbits);

	if (*str++ != ':' || nbits == 0) {
		return NULL;
	}

	bp = bitset_new(nbits);

	for (pos = (bytes << 3) - 1; *str != '\0' && isxdigit(*str) && pos >= 0; str++) {
		b = digittoint(*str);
		for (n = 3; n >= 0; n--, pos--) {
			if (b & (1 << n)) {
				bitset_set(bp, pos);
			}
		}
	}

	if (end != NULL) {
		*end = str;
	}

	return bp;
}

static int
emit_range(varstr *v, char sep, int lower, int upper)
{
	if (lower < 0 || upper < lower) {
		return 0;
	}

	if (sep) {
		varstr_add(v, sep);
	}

	if (lower != upper) {
		varstr_sprintf(v, "%d-%d", lower, upper);
	} else {
		varstr_sprintf(v, "%d", lower);
	}

	return 1;
}

/*
 * Convert bitset to set notation of the form
 *
 * 	{0-2,4,5-100}
 */
char *
bitset_to_set(bitset *b)
{
	int			bit;
	int			lower;
	int			upper;
	char		sep = 0;
	char *		str;
	varstr *	v;

	if (b == NULL)
		return strdup("{}");

	v = varstr_fromstr("{");

	for (bit = 0, lower = -1, upper = -1; bit < b->bs_nbits; bit++) {
		if (bitset_test(b, bit)) {
			if (lower < 0) {
				lower = bit;
			}
			upper = bit;
		} else {
			if (emit_range(v, sep, lower, upper)) {
				sep = ',';
			}
			lower = bit + 1;
		}
	}

	emit_range(v, sep, lower, upper);

	varstr_add(v, '}');

	str = varstr_tostr(v);
	varstr_free(v);

	return str;
}

unsigned int
count_bits(bits b)
{
	unsigned int	n = 0;

	while (b != 0) {
		n++;
		b &= (b-1);
	}

	return n;
}

/**
 * Number of bits in the set (as opposed to the total size of the set)
 */
int
bitset_count(bitset *b)
{
	int	i;
	int	count = 0;

	for (i = 0; i < b->bs_size; i++)
		count += count_bits(b->bs_bits[i]);

	return count;
}

/**
 * Number of bits this set can represent
 */
int
bitset_size(bitset *b)
{
	return b->bs_nbits;
}
