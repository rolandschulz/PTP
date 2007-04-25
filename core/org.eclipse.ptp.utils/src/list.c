/*
** List routines
**
** Copyright (c) 1996-2002 by Guardsoft Pty Ltd.
**
** This program is free software; you can redistribute it and/or modify
** it under the terms of the GNU General Public License as published by
** the Free Software Foundation; either version 2 of the License, or
** (at your option) any later version.
**
** This program is distributed in the hope that it will be useful,
** but WITHOUT ANY WARRANTY; without even the implied warranty of
** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
** GNU General Public License for more details.
**
** You should have received a copy of the GNU General Public License
** along with this program; if not, write to the Free Software
** Foundation, Inc., 59 Temple Place - Suite 330,
** Boston, MA 02111-1307, USA.
**
*/

#include <stdio.h>
#include <stdlib.h>

#include	"compat.h"
#include	"list.h"

THREAD_DECL(list);

List *
NewList(void)
{
	List *	l;

	l = (List *)malloc(sizeof(List));

	l->l_head = (ListElement *)NULL;
	l->l_tail = &l->l_head;
	l->l_nel = 0;

	return l;
}

/*
** Add new element to end of list.
*/
void		
AddToList(List *l, void *v)
{
	ListElement *	e;

	if ( l == (List *)NULL)
		return;

	e = malloc(sizeof(ListElement));

	e->l_value = v;
	e->l_next = (ListElement *)NULL;
	
	THREAD_LOCK(list);
	
	*(l->l_tail) = e;
	l->l_tail = &e->l_next;
	l->l_nel++;
	
	THREAD_UNLOCK(list);
}

/*
** Add new element to beginning of list (stack emulation).
*/
void		
AddFirst(List *l, void *v)
{
	ListElement *	e;
	ListElement *	ep;

	if ( l == (List *)NULL)
		return;

	e = malloc(sizeof(ListElement));

	e->l_value = v;
	e->l_next = l->l_head;

	THREAD_LOCK(list);
	
	/*
	** Adjust tail if necessary.
	*/
	if
	(
		(ep = l->l_head) != (ListElement *)NULL 
		&&
		ep->l_next == (ListElement *)NULL 
	)
		l->l_tail = &ep->l_next;

	l->l_head = e;
	l->l_nel++;
	
	THREAD_UNLOCK(list);
}

void
AppendList(List *dst, List *src)
{
	void *	e;

	SetList(src);

	while ( (e = GetListElement(src)) != (void *)NULL)
		AddToList(dst, e);
}

void		
RemoveFromList(List *l, void *v)
{
	ListElement *	e;
	ListElement *	ep;

	THREAD_LOCK(list);
	
	if ( l == (List *)NULL || l->l_nel == 0 ) {
		THREAD_UNLOCK(list);
		return;
	}

	for ( ep = e = l->l_head ; e != (ListElement *)NULL ; )
	{
		if ( e->l_value != v )
		{
			if ( ep != e )
				ep = ep->l_next;
			e = e->l_next;
			continue;
		}

		if ( l->l_scan == e )
			l->l_scan = ep;

		if ( ep == e )
		{
			l->l_head = e->l_next;

			if ( e->l_next == (ListElement *)NULL )
				l->l_tail = &l->l_head;
		}
		else
		{
			ep->l_next = e->l_next;

			if ( e->l_next == (ListElement *)NULL )
				l->l_tail = &ep->l_next;
		}

		free(e);

		break;
	}

	l->l_nel--;
	
	THREAD_UNLOCK(list);
}

/*
** Remove first element of list (stack emulation)
*/
void *
RemoveFirst(List *l)
{
	void *		v;
	ListElement *	e;
	ListElement *	ep;

	THREAD_LOCK(list);

	if ( l == (List *)NULL || l->l_nel == 0 ) {
		THREAD_UNLOCK(list);
		return (void *)NULL;
	}

	ep = e = l->l_head;

	if ( l->l_scan == e )
		l->l_scan = e->l_next;

	l->l_head = e->l_next;

	if ( e->l_next == (ListElement *)NULL )
		l->l_tail = &l->l_head;

	v = e->l_value;

	free(e);

	l->l_nel--;

	THREAD_UNLOCK(list);

	return v;
}

void		
DestroyList(List *l, void (*destroy)())
{
	ListElement *	ep;
	ListElement *	en;

	if ( l == (List *)NULL )
		return;

	THREAD_LOCK(list);

	ep = l->l_head;

	while ( ep != (ListElement *)NULL )
	{
		en = ep->l_next;

		if ( destroy != (void (*)())NULL )
			destroy(ep->l_value);

		free(ep);

		ep = en;
	}

	free(l);

	THREAD_UNLOCK(list);
}

void
SetList(List *l)
{
	if ( l == (List *)NULL )
		return;

	THREAD_LOCK(list);
	l->l_scan = l->l_head;
	THREAD_UNLOCK(list);
}

void *
GetListElement(List *l)
{
	void *			val;
	ListElement *	le;

	THREAD_LOCK(list);
	
	if ( l == (List *)NULL || l->l_scan == (ListElement *)NULL ) {
		THREAD_UNLOCK(list);
		return (void *)NULL;
	}

	le = l->l_scan;
	l->l_scan = le->l_next;
	
	val = le->l_value;
	
	THREAD_UNLOCK(list);
	
	return val;
}

void *
GetFirstElement(List *l)
{
	void *	val;
	
	THREAD_LOCK(list);
	
	if ( l == (List *)NULL || l->l_head == (ListElement *) NULL) {
		THREAD_UNLOCK(list);
		return (void *)NULL;
	}

	val = l->l_head->l_value;
	
	THREAD_UNLOCK(list);
	
	return val;
}

int
EmptyList(List *l)
{
	int	res;
	
	THREAD_LOCK(list);
	res = (int)(l == (List *)NULL || l->l_nel == 0);
	THREAD_UNLOCK(list);
	
	return res;
}

int
InList(List *l, void *v)
{
	ListElement *	e;

	THREAD_LOCK(list);

	if ( l == (List *)NULL ) {
		THREAD_UNLOCK(list);
		return 0;
	}

	for ( e = l->l_head ; e != (ListElement *)NULL ; e = e->l_next ) {
		if ( e->l_value == v ) {
			THREAD_UNLOCK(list);
			return 1;
		}
	}

	THREAD_UNLOCK(list);

	return 0;
}

int
SizeOfList(List *l)
{
	int	res;
	
	if ( l == (List *)NULL )
		return 0;

	THREAD_LOCK(list);
	res = l->l_nel;
	THREAD_UNLOCK(list);
	
	return res;
}
