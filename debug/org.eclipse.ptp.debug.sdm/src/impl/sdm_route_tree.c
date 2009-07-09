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
 * Routing layer implementation. These routines implement a tree-based router. The tree can
 * be either binomial or balanced.
 */

#include "config.h"

#include <stdlib.h>

#include "compat.h"
#include "hash.h"
#include "sdm.h"

#define ROUTE_BINOMIAL	0
#define ROUTE_BALANCED	1

static int			size;				/* number of servers */
static sdm_id		my_id;				/* our ID */
static sdm_id		parent;				/* ID of our parent */
static Hash *		child_descendants;	/* hash containing all descendants of each of our children */
static sdm_idset	children;			/* contains all immediate children of the current location */
static sdm_idset	descendants;		/* contains all children of the current location */
static sdm_idset	route;				/* last computed route */
static sdm_idset	reachable;			/* last set of reachable servers */

int SDM_MASTER;

extern char *	_set_to_str(sdm_idset);

static int	high_bit(int value);
static void	find_descendents(sdm_idset set, int id_p, int root, int size, int p2, int descend);

#define NORMALIZE(this, size, root) (this + size - root) % size

/*
 * Initialize the routing layer.
 */
int
sdm_route_init(int argc, char *argv[]) {
	int		p2;
	int	 	this_p;
	int		high;
	int 	size;
	int		this;
	int		root;
	sdm_id	child;

	children = sdm_set_new();
	descendants = sdm_set_new();
	route = sdm_set_new();
	reachable = sdm_set_new();

	size = sdm_route_get_size();
	this = sdm_route_get_id();
	root = SDM_MASTER;
	p2 = high_bit(size) << 1;
	this_p = NORMALIZE(this, size, root);
	high = high_bit(this_p);

	if (this != root)
        parent = ((this_p & ~(1 << high)) + root) % size;
    else
    	parent = SDM_MASTER;

    find_descendents(children, this_p, root, size, p2, 0);
    find_descendents(descendants, this_p, root, size, p2, 1);

    child_descendants = HashCreate(sdm_set_size(children)+1);

	for (child = sdm_set_first(children); !sdm_set_done(children); child = sdm_set_next(children)) {
		sdm_idset desc = sdm_set_new();
		find_descendents(desc, NORMALIZE(child, size, root), root, size, p2, 1);
		HashInsert(child_descendants, child, (void *)desc);
		DEBUG_PRINTF(DEBUG_LEVEL_ROUTING, "[%d] route for %d is %s \n", sdm_route_get_id(), child, _set_to_str(desc));
	}

    return 0;
}

static void
set_free(void *val)
{
	sdm_set_free((sdm_idset)val);
}

void
sdm_route_finalize(void)
{
	sdm_set_free(children);
	sdm_set_free(descendants);
	sdm_set_free(route);
	sdm_set_free(reachable);
	HashDestroy(child_descendants, set_free);
}

/*
 * Given a destination set, compute the route for the message.
 */
sdm_idset
sdm_route_get_route(const sdm_idset dest)
{
	sdm_id child;

	sdm_set_clear(route);

	DEBUG_PRINTF(DEBUG_LEVEL_ROUTING, "[%d] sdm_route_get_route dest %s, parent %d\n",
			sdm_route_get_id(),
			_set_to_str(dest),
			parent);

	if (my_id != SDM_MASTER &&
			(sdm_set_contains(dest, parent) ||
					sdm_set_contains(dest, SDM_MASTER))) {
		sdm_set_add_element(route, parent);
		return route;
	}

	for (child = sdm_set_first(children); !sdm_set_done(children); child = sdm_set_next(children)) {
		sdm_idset desc = (sdm_idset)HashSearch(child_descendants, child);
		if (sdm_set_contains(dest, child) || sdm_set_compare(dest, desc)) {
			sdm_set_add_element(route, child);
		}
	}
	return route;
}

/*
 * Given a destination set, compute the set of all destinations
 * reachable from the current location. If the destination is empty,
 * the result is all possible destinations.
 */
sdm_idset
sdm_route_reachable(const sdm_idset dest)
{
	sdm_set_clear(reachable);
	sdm_set_union(reachable, descendants);
	if (my_id != SDM_MASTER) {
		sdm_set_add_element(reachable, my_id);
	}
	sdm_set_intersect(reachable, dest);
	return reachable;
}

sdm_id
sdm_route_get_id(void)
{
	return my_id;
}

void
sdm_route_set_id(sdm_id id)
{
	my_id = id;
}

sdm_id
sdm_route_get_parent(void)
{
	return parent;
}

int
sdm_route_get_size(void)
{
	return size;
}

void
sdm_route_set_size(int s)
{
	size = s;
}

/*
 * Create a set containing our descendants. If descend is 0, only find immediate
 * children. Assumes that the size of the set will fit into an integer.
 */
static void
find_descendents(sdm_idset set, int id_p, int root, int size, int p2, int descend)
{
	int				i;
	int 			high;
	int 			child;
	int 			child_p;
	unsigned int	mask;

	high = high_bit(id_p);

	for (i = high + 1, mask = 1 << i; i <= p2; ++i, mask <<= 1) {
        child_p = id_p | mask;
        if (child_p < size) {
            child = (child_p + root) % size;
            sdm_set_add_element(set, child);
            if (descend) {
            	find_descendents(set, child_p, root, size, p2, descend);
            }
        }
    }
}

/*
 * Find MSB of value.
 */
static int
high_bit(int value)
{
	int 			pos = (sizeof(int) << 3) - 1;
	unsigned int	mask;

	mask = 1 << pos;

	for (; pos >= 0; pos--) {
		if (value & mask) {
			break;
		}
		mask >>= 1;
	}

	return pos;
}
