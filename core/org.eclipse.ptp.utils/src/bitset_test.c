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

void test_invert_mask(int tnum, int nbits);
void test_oreq(int tnum, int nb1, int nb2);
void test_str(int);
void test_boundary(int);

int
main(int argc, char *argv[])
{
	int bits_size = (sizeof(bits)<<3);

	test_invert_mask(1, bits_size*2-4);
	test_oreq(2, bits_size*2-4,bits_size*2);
	test_oreq(3, bits_size*2,bits_size*2-4);
	test_str(4);
	test_boundary(5);

	return 0;
}

void
test_invert_mask(int tnum, int nbits)
{
	int			i;
	bits 		r;
	bitset *	b;

	memset((char *)&r, 0, sizeof(bits));
	for (i = 0; i < nbits % (sizeof(bits) << 3); i++)
		r |= (1 << i);

	b = bitset_new(nbits);
	bitset_invert(b);
	if (b->bs_bits[b->bs_size-1] != r)
		printf("TEST_%d FAIL: high bits should be 0x%x, actually 0x%x\n", tnum, r, b->bs_bits[b->bs_size-1]);
	else
		printf("TEST_%d SUCCEDED\n", tnum);
	bitset_free(b);
}


void
test_oreq(int tnum, int nb1, int nb2)
{
	int			i;
	bitset *	b1;
	bitset *	b2;
	bitset *	r;

	b1 = bitset_new(nb1);
	b2 = bitset_new(nb2);
	bitset_set(b1, 1);
	bitset_set(b2, nb2-1);

	bitset_oreq(b1, b2);

	r = bitset_new(nb1);
	bitset_set(r, 1);
	if (nb1 >= nb2)
		bitset_set(r, nb2 - 1);

	for (i = 0; i < r->bs_size; i++) {
		if (r->bs_bits[i] != b1->bs_bits[i]) {
			printf("TEST_%d FAIL: [%d] 0x%x != 0x%x\n", i, r->bs_bits[i], b1->bs_bits[i], tnum);
			return;
		}
	}

	printf("TEST_%d SUCCEDED\n", tnum);
}

void
test_str(int tnum)
{
	char *		str2;
	char * 		str1 = "17:6411eda";
	char *		end;
	bitset *	b = str_to_bitset(str1, &end);
	str2 = bitset_to_str(b);
	if (strncmp(str1, str2, 9) != 0) {
		printf("TEST_%d_1 FAIL: %s != %s\n", tnum, str1, str2);
	} else if (*end != 'a') {
		printf("TEST_%d_1 FAIL: end == %d\n", tnum, *end);
	} else {
		printf("TEST_%d_1 SUCCEDED\n", tnum);
	}

	str1 = "3:07";
	b = str_to_bitset(str1, NULL);
	str2 = bitset_to_str(b);
	if (strncmp(str1, str2, 9) != 0) {
		printf("TEST_%d_2 FAIL: %s != %s\n", tnum, str1, str2);
	} else if (*end != 'a') {
		printf("TEST_%d_2 FAIL: end == %d\n", tnum, *end);
	} else {
		printf("TEST_%d_2 SUCCEDED\n", tnum);
	}
}

void
test_boundary(int tnum)
{
	char * 		str1 = "8:7f";
	char *		str2;
	char *		str3 = "8:ff";
	char *		end;
	bitset *	b = str_to_bitset(str1, &end);
	bitset_set(b, 7);
	str2 = bitset_to_str(b);
	if (strncmp(str3, str2, 4) != 0) {
		printf("TEST_%d_1 FAIL: %s != %s\n", tnum, str3, str2);
	} else {
		printf("TEST_%d_1 SUCCEDED\n", tnum);
	}
}
