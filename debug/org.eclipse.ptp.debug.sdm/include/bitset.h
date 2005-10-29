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
#define BIT_IN_OBJ(bit)		((bit) - ((BIT_INDEX((bit)) * sizeof(bits)) << 3))
#define SIZE_TO_BYTES(size)	(sizeof(bits) * (size))

typedef unsigned int	bits;

struct bitset {
	unsigned int		bs_nbits;	/* total number of bits in set */
	bits *			bs_bits;		/* actual bits (unused bits are always 0)*/
	unsigned int		bs_size;		/* number of 'bits' objects */
};
typedef struct bitset bitset;

bitset *		bitset_new(int);
void			bitset_free(bitset *);
bitset *		bitset_copy(bitset *);
int			bitset_isempty(bitset *);
void			bitset_clear(bitset *);
void			bitset_set(bitset *, int);
void			bitset_unset(bitset *, int);
int			bitset_test(bitset *, int);
bitset *		bitset_and(bitset *, bitset *);
void			bitset_andeq(bitset *, bitset *);
bitset *		bitset_or(bitset *, bitset *);
void			bitset_oreq(bitset *, bitset *);
void			bitset_invert(bitset *);
char *		bitset_to_str(bitset *);
bitset *		str_to_bitset(char *);
int			bitset_size(bitset *);
char *		bitset_to_set(bitset *);
#endif /*_BITSET_H_*/
