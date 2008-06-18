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

#ifndef _BITSET_H_
#define _BITSET_H_

#define BIT_INDEX(bit)		((bit) >> 3) / (sizeof(bits))
#define BIT_IN_OBJ(bit)		((bit) % (sizeof(bits) << 3))
#define SIZE_TO_BYTES(size)	(sizeof(bits) * (size))

/*
 * NOTE: if you change the size of bits, then the ffs() routine may no longer work.
 */
typedef unsigned int	bits;

/*
 * Bitset definition. Note that the LSB is bit number '0'.
 */
struct bitset {
	unsigned int	bs_nbits;	/* total number of bits in set */
	bits *			bs_bits;	/* actual bits (unused bits are always 0)*/
	unsigned int	bs_size;	/* number of 'bits' objects */
};
typedef struct bitset bitset;

bitset *	bitset_new(int num);						/* create a new bitset to contain 'num' bits */
void		bitset_free(bitset *b);						/* dispose of a bitset */
bitset *	bitset_dup(bitset *b);						/* create a copy of a bitset */
void		bitset_copy(bitset *b1, bitset *b2);		/* copy bits from b2 to b1 */
int			bitset_isempty(bitset *b);					/* test if all bits are 0 */
void		bitset_clear(bitset *b);					/* set all bits to 0 */
void		bitset_set(bitset *b, int n);				/* set bit 'n' (0 == LSB) to 1 */
void		bitset_unset(bitset *b, int n);				/* set bit 'n' to 0 */
int			bitset_test(bitset *b, int n);				/* return the value of bit 'n' */
int			bitset_firstset(bitset *b);					/* find the first bit set to 1 (starting from LSB) */
bitset *	bitset_and(bitset *b1, bitset *b2);			/* compute b3 = b1 & b2 */
void		bitset_andeq(bitset *b1, bitset *b2);		/* compute b1 &= b2 */
void		bitset_andeqnot(bitset *b1, bitset *b2);	/* compute b1 &= ~b2 */
bitset *	bitset_or(bitset *b1, bitset *b2);			/* compute b3 = b1 | b2 */
void		bitset_oreq(bitset *b1, bitset *b2);		/* compute b1 |= b2 */
void		bitset_invert(bitset *b);					/* compute ~b */
int			bitset_eq(bitset *b1, bitset *b2);			/* test if (b1 & b2) == b1 */
int			bitset_compare(bitset *b1, bitset *b2);		/* test if (b1 & b2) != 0 */
char *		bitset_to_str(bitset *b);					/* convert b to a portable string representation */
bitset *	str_to_bitset(char *str, char **end);		/* convert a portable string represetation to a bitset */
int			bitset_count(bitset *b);					/* return the number of bits in the set */
int			bitset_size(bitset *b);						/* number of bits this bitset can represent */
char *		bitset_to_set(bitset *b);					/* convert b to set notation */
#endif /*_BITSET_H_*/
