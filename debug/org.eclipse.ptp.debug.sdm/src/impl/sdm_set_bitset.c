/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

/*
 * ID set implementation. These routines implement a set abstraction using bitsets.
 */

#include <stdlib.h>
#include <string.h>

#include "compat.h"
#include "bitset.h"
#include "sdm.h"

struct sdm_idset {
	bitset *	set;
	int			iterator;
};

sdm_idset
sdm_set_new(void)
{
	sdm_idset set = (sdm_idset)malloc(sizeof(struct sdm_idset));
	set->set = bitset_new(sdm_route_get_size());
	set->iterator = 0;
	return set;
}

void
sdm_set_free(sdm_idset set)
{
	bitset_free(set->set);
	free(set);
}

sdm_idset
sdm_set_clear(sdm_idset set)
{
	bitset_clear(set->set);
	return set;
}

int
sdm_set_size(const sdm_idset set)
{
	return bitset_count(set->set);
}

int
sdm_set_is_subset(const sdm_idset set1, const sdm_idset set2)
{
	return bitset_eq(set1->set, set2->set);
}

int
sdm_set_compare(const sdm_idset set1, const sdm_idset set2)
{
	return bitset_compare(set1->set, set2->set);
}

int
sdm_set_contains(const sdm_idset set, const sdm_id id)
{
	return bitset_test(set->set, id) == 1;
}

sdm_id
sdm_set_first(const sdm_idset set)
{
	set->iterator = bitset_firstset(set->set);
	return set->iterator;
}

sdm_id
sdm_set_next(const sdm_idset set)
{
	while (set->iterator < bitset_size(set->set)) {
		set->iterator++;
		if (bitset_test(set->set, set->iterator)) {
			break;
		}
	}

	return set->iterator;
}

int
sdm_set_done(const sdm_idset set)
{
	return set->iterator < 0 || set->iterator >= bitset_size(set->set);
}

int
sdm_set_is_empty(const sdm_idset set)
{
	return bitset_isempty(set->set);
}

sdm_idset
sdm_set_add_element(const sdm_idset set, const sdm_id id)
{
	bitset_set(set->set, id);
	return set;
}

sdm_idset
sdm_set_remove_element(const sdm_idset set, const sdm_id id)
{
	bitset_unset(set->set, id);
	return set;
}

sdm_idset
sdm_set_add_all(const sdm_idset set, const sdm_id id)
{
	bitset *b = bitset_new(id+1);
	bitset_invert(b);
	bitset_oreq(set->set, b);
	bitset_free(b);
	return set;
}

sdm_idset
sdm_set_union(const sdm_idset set1, const sdm_idset set2)
{
	bitset_oreq(set1->set, set2->set);
	return set1;
}

sdm_idset
sdm_set_intersect(const sdm_idset set1, const sdm_idset set2)
{
	/*
	 * If either set is empty, the result is U (the universe)
	 */
	if (bitset_isempty(set1->set) || bitset_isempty(set2->set)) {
		bitset_clear(set1->set);
		bitset_invert(set1->set);
	} else {
		bitset_andeq(set1->set, set2->set);
	}
	return set1;
}

sdm_idset
sdm_set_diff(const sdm_idset set1, const sdm_idset set2)
{
	bitset_andeqnot(set1->set, set2->set);
	return set1;
}

sdm_id
sdm_set_max(const sdm_idset set)
{
	return bitset_size(set->set) - 1;
}

int
sdm_set_serialize(const sdm_idset set, char *buf, char **end)
{
	char *	str = bitset_to_str(set->set);
	int		len = strlen(str);

	memcpy(buf, str, len);
	free(str);
	if (end != NULL) {
		*end = buf + len;
	}
	return len;
}

int
sdm_set_serialized_length(const sdm_idset set)
{
	return sizeof(int) * 2 + 1 + bitset_size(set->set) / 4;
}

int
sdm_set_deserialize(sdm_idset set, char *buf, char **end)
{
	char *		e;
	bitset *	b = str_to_bitset(buf, &e);

	bitset_copy(set->set, b);
	bitset_free(b);
	if (end != NULL) {
		*end = e;
	}
	return e - buf;
}

char *
_set_to_str(sdm_idset set)
{
	char *			p;
	static int		str_count = 0;
	static char *	str[5];

	if (str_count >= 5) {
		str_count = 0;
	}
	if (str[str_count] != NULL) {
		free(str[str_count]);
	}
	p = str[str_count++] = bitset_to_set(set->set);
	return p;
}
