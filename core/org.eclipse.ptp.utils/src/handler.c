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

#include "handler.h"
#include "list.h"

static List *		dbg_handlers = NULL;

static handler *	new_handler(int type, void *data);
static void			dispose_handler(handler *h);
static void			set_handler(void);
static handler *	get_handler(void);

/**
 * Register a handler for general events.
 */
void
RegisterEventHandler(int type, void (*event_callback)(void *, void *), void *data)
{
	handler *	h;

	h = new_handler(HANDLER_EVENT, NULL);
	h->event_type = type;
	h->event_handler = event_callback;
	h->data = data;
}

/**
 * Unregister file descriptor handler.
 * Can be called from callback function.
 */
void
UnregisterEventHandler(int type, void (*event_callback)(void *, void *))
{
	handler *	h;

	for (set_handler(); (h = get_handler()) != NULL; ) {
		if (IS_EVENT_HANDLER(h)
				&& h->event_type == type
				&& h->event_handler == event_callback) {
			h->htype |= HANDLER_DISPOSED;
		}
	}
}

/**
 * Register a handler for file descriptor events.
 */
void
RegisterFileHandler(int fd, int type, int (*file_handler)(int, void *), void *data)
{
	handler *	h;
	
	h = new_handler(HANDLER_FILE, data);
	h->file_type = type;
	h->fd = fd;
	h->error = 0;
	h->file_handler = file_handler;
}

/**
 * Unregister file descriptor handler
 * Can be called from callback function.
 */
void
UnregisterFileHandler(int fd)
{
	handler *	h;

	for (set_handler(); (h = get_handler()) != NULL; ) {
		if (IS_FILE_HANDLER(h) && h->fd == fd) {
			h->htype |= HANDLER_DISPOSED;
		}
	}
}

/**
 * Call all event handlers of a particular type
 */
void
CallEventHandlers(int type, void *data)
{
	handler *	h;

	for (set_handler(); (h = get_handler()) != NULL; ) {
		if (IS_EVENT_HANDLER(h)) {
			if (IS_DISPOSED(h)) {
				dispose_handler(h);
			} else if (h->event_type == type) {
				h->event_handler(h->data, data);
			}
		}
	}
}

/*
 * Call all file handlers
 */
int
CallFileHandlers(fd_set *rfds, fd_set *wfds, fd_set *efds)
{
	int			ret = 0;
	handler *	h;
	
	for (set_handler(); (h = get_handler()) != NULL; ) {
		if (IS_FILE_HANDLER(h)) {
			if (IS_DISPOSED(h)) {
				dispose_handler(h);
			} else if (h->error >= 0
					&& ((IS_FILE_READ_HANDLER(h) && FD_ISSET(h->fd, rfds))
							|| (IS_FILE_WRITE_HANDLER(h) && FD_ISSET(h->fd, wfds))
							|| (IS_FILE_EXCEPT_HANDLER(h) && FD_ISSET(h->fd, efds)))) {
				h->error = h->file_handler(h->fd, h->data);
				if (h->error < 0) {
					ret = -1;
				}
			}
		}
	}
	
	return ret;
}

/*
 * Generate file descriptor sets
 */
void
GenerateFDSets(int *nfds, fd_set *rfds, fd_set *wfds, fd_set *efds)
{
	handler *	h;
	
	*nfds = 0;
	FD_ZERO(rfds);
	FD_ZERO(wfds);
	FD_ZERO(efds);
	
	for (set_handler(); (h = get_handler()) != NULL; ) {
		if (IS_FILE_HANDLER(h) && !IS_DISPOSED(h)) {
			if (IS_FILE_READ_HANDLER(h)) {
				FD_SET(h->fd, rfds);
			}
			if (IS_FILE_WRITE_HANDLER(h)) {
				FD_SET(h->fd, wfds);
			}
			if (IS_FILE_EXCEPT_HANDLER(h)) {
				FD_SET(h->fd, efds);
			}
			if (h->fd > *nfds) {
				*nfds = h->fd;
			}
		}
	}
}

/**
 * Create an event handler structure and add it to the list of handlers
 */
static handler *
new_handler(int type, void *data)
{
	handler *	h;

	if (dbg_handlers == NULL)
		dbg_handlers = NewList();

	h = (handler *)malloc(sizeof(handler));
	h->htype = type;
	h->data = data;

	AddToList(dbg_handlers, (void *)h);

	return h;
}

/**
 * Remove handler from list and dispose of memory
 */
static void
dispose_handler(handler *h)
{
	RemoveFromList(dbg_handlers, (void *)h);
	free(h);
}

void
set_handler(void)
{
	SetList(dbg_handlers);
}

handler *
get_handler(void)
{
	return (handler *)GetListElement(dbg_handlers);
}
