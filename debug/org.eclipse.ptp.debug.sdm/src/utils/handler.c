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

/**
 * Create an event handler structure and add it to the list of handlers
 */
handler *
NewHandler(int type, void *data)
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
void
DestroyHandler(handler *h)
{
	RemoveFromList(dbg_handlers, (void *)h);
	free(h);
}

void
SetHandler(void)
{
	SetList(dbg_handlers);
}

handler *
GetHandler(void)
{
	return (handler *)GetListElement(dbg_handlers);
}

void
RegisterEventHandler(void (*event_callback)(proxy_event *, void *), void *data)
{
	handler *	h;

	h = NewHandler(HANDLER_EVENT, NULL);
	h->event_handler = event_callback;
	h->data = data;
}

/**
 * Unregister file descriptor handler
 */
void
UnregisterEventHandler(void (*event_callback)(proxy_event *, void *))
{
	handler *	h;

	for (SetHandler(); (h = GetHandler()) != NULL; ) {
		if (h->htype == HANDLER_EVENT && h->event_handler == event_callback)
			DestroyHandler(h);
	}
}

/**
 * Register a handler for file descriptor events.
 */
void
RegisterFileHandler(int fd, int type, int (*file_handler)(int, void *), void *data)
{
	handler *	h;
	
	h = NewHandler(HANDLER_FILE, data);
	h->file_type = type;
	h->fd = fd;
	h->file_handler = file_handler;
}

/**
 * Unregister file descriptor handler
 */
void
UnregisterFileHandler(int fd)
{
	handler *	h;

	for (SetHandler(); (h = GetHandler()) != NULL; ) {
		if (h->htype == HANDLER_FILE && h->fd == fd)
			DestroyHandler(h);
	}
}