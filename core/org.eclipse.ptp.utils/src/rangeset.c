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

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdarg.h>

#include "compat.h"
#include "rangeset.h"

#define INITIAL_SIZE	BUFSIZ

static str_buf *
new_buf(void)
{
	str_buf *	buf = (str_buf *)malloc(sizeof(str_buf));
	buf->contents = (char *)malloc(INITIAL_SIZE);
	buf->contents[0] = '\0';
	buf->len = INITIAL_SIZE;
	buf->count = 0;
	return buf;
}

static void
buf_printf(str_buf *buf, char *fmt, ...)
{
	int 	len;
	char *	str;
    va_list ap;
    
    va_start(ap, fmt);
    vasprintf(&str, fmt, ap);
    va_end(ap);

	len = strlen(str);
	
	while (buf->count + len >= buf->len) {
		buf->len *= 2;
		buf->contents = (char *)realloc(buf->contents, buf->len);
	}
	
	memcpy(buf->contents + buf->count, str, len);
	buf->count += len;
	buf->contents[buf->count] = '\0';
	
	free(str);
}

void
reset_buf(str_buf *buf)
{
	buf->count = 0;
}

char *
buf_contents(str_buf *buf)
{
	return buf->contents;
}

static void
free_buf(str_buf *buf)
{
	free(buf->contents);
	free(buf);
}

rangeset *
new_rangeset(void)
{
	rangeset * set = (rangeset *)malloc(sizeof(rangeset));
	set->elements = NewList();
	set->buf = new_buf();
	set->changed = 0;
	return set;
}

static range *
new_range(int low, int high)
{
	range * r = (range *)malloc(sizeof(range));
	r->low = low;
	r->high = high;
	return r;
}

void
insert_in_rangeset(rangeset *set, int val)
{
	range * r;
	range *	element;
	range * last = NULL;
	
	if (EmptyList(set->elements)) {
		r = new_range(val, val);
		AddToList(set->elements, r);
	} else {
		for (SetList(set->elements); (element = (range *)GetListElement(set->elements)) != NULL; ) {
			if (val < element->low - 1) {
				if (last == NULL) {
					r = new_range(val, val);
					AddFirst(set->elements, r);
				} else if (last != NULL && val > last->high + 1) {
					r = new_range(val, val);
					InsertBefore(set->elements, element, r);
				}
			} else if (val == element->low - 1) {
				element->low = val;
			} else if (val == element->high + 1) {
				element->high = val;
			}
			
			last = element;
		}
		
		if (val > last->high + 1) {
			r = new_range(val, val);
			AddToList(set->elements, r);
		}
	}
	
	set->changed = 1;
}

char *
rangeset_to_string(rangeset *set)
{
	int			first = 1;
	range *		element;
	
	if (set->changed) {
		reset_buf(set->buf);
		
		for (SetList(set->elements); (element = (range *)GetListElement(set->elements)) != NULL; ) {
			if (!first) {
				buf_printf(set->buf, ",");
			} else {
				first = 0;
			}
			if (element->low == element->high) {
				buf_printf(set->buf, "%d", element->low);
			} else {
				buf_printf(set->buf, "%d-%d", element->low, element->high);
			}
		}
		
		set->changed = 0;
	}
	
	return buf_contents(set->buf);
}

void
free_rangeset(rangeset *set)
{
	DestroyList(set->elements, free);
	free_buf(set->buf);
	free(set);
}
