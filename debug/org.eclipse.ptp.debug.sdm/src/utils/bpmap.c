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

/*
 * Keeps a map of local breakpoint ID's (as supplied by the client) to the remote
 * breakpoint ID's (as determined by gdb).
 */

#ifdef __gnu_linux__
#define _GNU_SOURCE
#endif /* __gnu_linux__ */

#include "config.h"

#include <stdlib.h>
#include <unistd.h>
#include <string.h>

#include "gdb.h"
#include "dbg.h"
#include "dbg_error.h"

struct bpentry {
	int local;
	int remote;
	int temp;
};
typedef struct bpentry	bpentry;

struct bpmap {
	int					nels; /* number of elements currently in map */
	int					size; /* total size of map */
	struct bpentry *	maps;
};

static struct bpmap	_bp_map = { 0, 0, NULL };

void
AddBPMap(int local, int remote, int temp)
{
	int					i;
	struct bpentry *	map;

	if (_bp_map.size == 0) {
		_bp_map.maps = (struct bpentry *)malloc(sizeof(struct bpentry) * 100);
		_bp_map.size = 100;

		for (i = 0; i < _bp_map.size; i++) {
			map = &_bp_map.maps[i];
			map->remote = map->local = -1;
			map->temp = 0;
		}
	}

	if (_bp_map.nels == _bp_map.size) {
		i = _bp_map.size;
		_bp_map.size *= 2;
		_bp_map.maps = (struct bpentry *)realloc(_bp_map.maps, sizeof(struct bpentry) * _bp_map.size);

		for (; i < _bp_map.size; i++) {
			map = &_bp_map.maps[i];
			map->remote = map->local = -1;
			map->temp = 0;
		}
	}

	for (i = 0; i < _bp_map.size; i++) {
		map = &_bp_map.maps[i];
		if (map->remote == -1) {
			map->remote = remote;
			map->local = local;
			map->temp = temp;
			_bp_map.nels++;
			break;
		}
	}
}

/*
 * Remove a breakpoint map.
 *
 * @param id local ID of the breakpoint to remove
 */
void
RemoveBPMap(int id)
{
	int					i;
	struct bpentry *	map;

	for (i = 0; i < _bp_map.size; i++) {
		map = &_bp_map.maps[i];
		if (map->local == id) {
			map->remote = -1;
			map->local = -1;
			map->temp = 0;
			_bp_map.nels--;
			break;
		}
	}
}

/*
 * Get the corresponding ID from the map.
 *
 * @param id remote ID of the breakpoint to locate
 * @returns the corresponding local ID of the breakpoint if found
 * 			-1 if the BP is not found
 */
int
GetLocalBPID(int id)
{
	int					i;
	struct bpentry *	map;

	for (i = 0; i < _bp_map.size; i++) {
		map = &_bp_map.maps[i];
		if (map->remote == id) {
			return map->local;
		}
	}

	return -1;
}

/*
 * Get the corresponding ID from the map.
 *
 * @param id local ID of the breakpoint to locate
 * @returns the corresponding remote ID of the breakpoint if found
 * 			-1 if the BP is not found
 */
int
GetRemoteBPID(int id)
{
	int					i;
	struct bpentry *	map;

	for (i = 0; i < _bp_map.size; i++) {
		map = &_bp_map.maps[i];
		if (map->local == id) {
			return map->remote;
		}
	}

	return -1;
}

/*
 * Check if the breakpoint corresponding to the local ID is temporary.
 *
 * @param id local ID of the breakpoint
 * @returns 1 if the breakpoint is temporary
 */
int
IsTempBP(int id)
{
	int					i;
	struct bpentry *	map;

	for (i = 0; i < _bp_map.size; i++) {
		map = &_bp_map.maps[i];
		if (map->local == id) {
			return map->temp;
		}
	}

	return 0;
}

void
ClearBPMaps(void)
{
	int					i;
	int 				length = _bp_map.size;
	struct bpentry *	map;

	for (i = 0; i < length; i++) {
		map = &_bp_map.maps[i];
		if (map == NULL)
			return;

		map->remote = -1;
		map->local = -1;
		map->temp = 0;
		_bp_map.nels--;
	}
}

