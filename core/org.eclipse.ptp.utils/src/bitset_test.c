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

void test_invert_mask(char *name, int nbits);
void test_oreq(char *name, int nb1, int nb2);
void test_str(char *name);
void test_set(char *name);
void test_boundary(char *name);
void test_nine(char *name);
void test_144(char *name);
void test_2048(char *name);

int
main(int argc, char *argv[])
{
	int bits_size = (sizeof(bits)<<3);

	test_invert_mask("test_invert_mask", bits_size*2-4);
	test_oreq("test_oreq", bits_size*2-4,bits_size*2);
	test_oreq("test_oreq", bits_size*2,bits_size*2-4);
	test_str("test_str");
	test_set("test_set");
	test_boundary("test_boundary");
	test_nine("test_nine");
	test_144("test_144");
	test_2048("test_2048");

	return 0;
}

void
test_invert_mask(char *name, int nbits)
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
		printf("TEST(%s) FAIL: high bits should be 0x%x, actually 0x%x\n", name, r, b->bs_bits[b->bs_size-1]);
	else
		printf("TEST(%s) SUCCEDED\n", name);
	bitset_free(b);
}


void
test_oreq(char *name, int nb1, int nb2)
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
			printf("TEST(%s) FAIL: [%d] 0x%x != 0x%x\n", name, i, r->bs_bits[i], b1->bs_bits[i]);
			return;
		}
	}

	printf("TEST(%s) SUCCEDED\n", name);
}

void
test_str(char *name)
{
	char *		str2;
	char * 		str1 = "17:6411eda";
	char *		end;
	bitset *	b = str_to_bitset(str1, &end);
	str2 = bitset_to_str(b);
	if (strncmp(str1, str2, 9) != 0) {
		printf("TEST(%s)_1 FAIL: %s != %s\n", name, str1, str2);
	} else if (*end != 'a') {
		printf("TEST(%s)_1 FAIL: end == %d\n", name, *end);
	} else {
		printf("TEST(%s)_1 SUCCEDED\n", name);
	}

	str1 = "3:07";
	b = str_to_bitset(str1, NULL);
	str2 = bitset_to_str(b);
	if (strncmp(str1, str2, 9) != 0) {
		printf("TEST(%s)_2 FAIL: %s != %s\n", name, str1, str2);
	} else if (*end != 'a') {
		printf("TEST(%s)_2 FAIL: end == %d\n", name, *end);
	} else {
		printf("TEST(%s)_2 SUCCEDED\n", name);
	}
}


void
test_set(char *name)
{
	char * 		str1 = "{0,3-5,7-9,14}";
	char *		str2;
	bitset *	b = bitset_new(15);
	bitset_set(b, 0);
	bitset_set(b, 3);
	bitset_set(b, 4);
	bitset_set(b, 5);
	bitset_set(b, 7);
	bitset_set(b, 8);
	bitset_set(b, 9);
	bitset_set(b, 14);
	str2 = bitset_to_set(b);
	if (strcmp(str1, str2) != 0) {
		printf("TEST(%s) FAIL: %s != %s\n", name, str1, str2);
	} else {
		printf("TEST(%s) SUCCEDED\n", name);
	}
}

void
test_boundary(char *name)
{
	char * 		str1 = "8:7f";
	char *		str2;
	char *		str3 = "8:ff";
	char *		end;
	bitset *	b = str_to_bitset(str1, &end);
	bitset_set(b, 7);
	str2 = bitset_to_str(b);
	if (strncmp(str3, str2, 4) != 0) {
		printf("TEST(%s) FAIL: %s != %s\n", name, str3, str2);
	} else {
		printf("TEST(%s) SUCCEDED\n", name);
	}
}

void
test_nine(char *name)
{
	char *		str1 = "9:0001";
	char *		str2;
	char *		str3;
	char *		end;
	bitset *	b1 = str_to_bitset(str1, &end);
	bitset *	b2 = bitset_new(9);
	bitset_set(b2, 0);
	str2 = bitset_to_str(b1);
	str3 = bitset_to_str(b2);
	if (strcmp(str2, str3) != 0) {
		printf("TEST(%s) FAIL: %s != %s\n", name, str2, str3);
	} else {
		printf("TEST(%s) SUCCEDED\n", name);
	}
}

void
test_144(char *name)
{
	char *		str1 = "90:800000000000000000000000000000000001";
	char *		str2;
	char *		str3;
	char *		end;
	bitset *	b1 = str_to_bitset(str1, &end);
	bitset *	b2 = bitset_new(144);
	bitset_set(b2, 0);
	bitset_set(b2, 143);
	str2 = bitset_to_str(b1);
	str3 = bitset_to_str(b2);
	if (strcmp(str2, str3) != 0) {
		printf("TEST(%s) FAIL: %s != %s\n", name, str2, str3);
	} else {
		printf("TEST(%s) SUCCEDED\n", name);
	}
}

void
test_2048(char *name)
{
	char *		str1 = "800:80000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001";
	char *		str2;
	char *		str3;
	char *		end;
	bitset *	b1 = str_to_bitset(str1, &end);
	bitset *	b2 = bitset_new(2048);
	bitset_set(b2, 0);
	bitset_set(b2, 2047);
	str2 = bitset_to_str(b1);
	str3 = bitset_to_str(b2);
	if (strcmp(str2, str3) != 0) {
		printf("TEST(%s) FAIL: %s != %s\n", name, str2, str3);
	} else {
		printf("TEST(%s) SUCCEDED\n", name);
	}
}
