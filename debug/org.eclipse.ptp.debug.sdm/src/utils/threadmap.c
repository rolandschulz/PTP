/*
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

/*
 * Keeps a map of thread ID's, and its information.
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
#include "threadmap.h"

struct thread_map {
	int				nels; // number of elements currently in map
	int				size; // total size of map
	thread_entry *	maps;
};

static struct thread_map _thread_map = {0, 0, NULL};

/*
 * Add a thread entry to thread map.
 *
 */
thread_entry *
AddThreadMap(int threadID, int stopStatus, int statementHookBPNum, int stepNop)
{
	int				i;
	thread_entry *	map = NULL;

	if (_thread_map.size == 0) {
		thread_entry *thisMaps = (thread_entry *)malloc(sizeof(thread_entry) * 10);
		_thread_map.maps = thisMaps;
		_thread_map.size = 10;

		for (i = 0; i < _thread_map.size; i++) {
			map = &_thread_map.maps[i];
			map->thread_id = -1;
			map->statement_hook_bp_num = -1;
			map->stop_status = THREAD_STATUS_STOP_UNKNOWN;
			map->skip_statement_hook = 0;
			map->step_nop = 0;
			map->stackframes = NULL;
		}
	}

	if (_thread_map.nels == _thread_map.size) {
		i = _thread_map.size;
		_thread_map.size *= 2;
		_thread_map.maps = (thread_entry *)realloc(_thread_map.maps, sizeof(thread_entry) * _thread_map.size);

		for (; i < _thread_map.size; i++) {
			map = &_thread_map.maps[i];
			map->thread_id = -1;
			map->statement_hook_bp_num = -1;
			map->stop_status = THREAD_STATUS_STOP_UNKNOWN;
			map->skip_statement_hook = 0;
			map->step_nop = 0;
			map->stackframes = NULL;
		}
	}

	for (i = 0; i < _thread_map.size; i++) {
		map = &_thread_map.maps[i];
		if (map->thread_id == -1) {
			map->thread_id = threadID;
			map->statement_hook_bp_num = statementHookBPNum;
			map->stop_status = stopStatus;
			map->skip_statement_hook = 0;
			map->step_nop = stepNop;
			map->stackframes = NULL;
			_thread_map.nels++;
			break;
		}
	}
	return map;
}

/*
 * Remove a thread entry from thread map.
 *
 */
void
RemoveThreadMap(int id)
{
	int				i;
	thread_entry *	map;
	for (i = 0; i < _thread_map.size; i++) {
		map = &_thread_map.maps[i];
		if (map->thread_id == id) {
			map->thread_id = -1;
			map->statement_hook_bp_num = -1;
			map->stop_status = THREAD_STATUS_STOP_UNKNOWN;
			map->skip_statement_hook = 0;
			map->step_nop = 0;
			if (NULL == map->stackframes)
			{
				DestroyList(map->stackframes, FreeStackframe);
				map->stackframes = NULL;
			}
			_thread_map.nels--;
			break;
		}
	}
}

/*
 * Find a thread entry in thread map.
 *
 */
thread_entry *
FindThreadEntry(int threadID)
{
	int				i;
	thread_entry *	map;
	for (i = 0; i < _thread_map.size; i++) {
		map = &_thread_map.maps[i];
		if (map->thread_id == threadID) {
			return map;
		}
	}
	return NULL;
}

/*
 * Remove all the entries in the thread map.
 *
 */
void
ClearThreadMaps()
{
	int				i;
	thread_entry *	map;
	int length = _thread_map.size;
	for (i = 0; i < length; i++) {
		map = &_thread_map.maps[i];
		if (map == NULL)
			return;

		map->thread_id = -1;
		map->statement_hook_bp_num = -1;
		map->stop_status = THREAD_STATUS_STOP_UNKNOWN;
		map->skip_statement_hook = 0;
		map->step_nop = 0;
		if (NULL == map->stackframes)
		{
			DestroyList(map->stackframes, FreeStackframe);
			map->stackframes = NULL;
		}
		_thread_map.nels--;
	}
}
