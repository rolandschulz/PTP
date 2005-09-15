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

#include	<stdio.h>

#include	"list.h"

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
	*(l->l_tail) = e;

	l->l_tail = &e->l_next;
	l->l_nel++;
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

	if ( l == (List *)NULL || l->l_nel == 0 )
		return;

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

	if ( l == (List *)NULL || l->l_nel == 0 )
		return (void *)NULL;

	ep = e = l->l_head;

	if ( l->l_scan == e )
		l->l_scan = e->l_next;

	l->l_head = e->l_next;

	if ( e->l_next == (ListElement *)NULL )
		l->l_tail = &l->l_head;

	v = e->l_value;

	free(e);

	l->l_nel--;

	return v;
}

void		
DestroyList(List *l, void (*destroy)())
{
	ListElement *	ep;
	ListElement *	en;

	if ( l == (List *)NULL )
		return;

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
}

void
SetList(List *l)
{
	if ( l == (List *)NULL )
		return;

	l->l_scan = l->l_head;
}

void *
GetListElement(List *l)
{
	ListElement *	le;

	if ( l == (List *)NULL || l->l_scan == (ListElement *)NULL )
		return (void *)NULL;

	le = l->l_scan;

	l->l_scan = le->l_next;

	return le->l_value;
}

int
EmptyList(List *l)
{
	return (int)(l == (List *)NULL || l->l_nel == 0);
}

int
InList(List *l, void *v)
{
	ListElement *	e;

	if ( l == (List *)NULL )
		return 0;

	for ( e = l->l_head ; e != (ListElement *)NULL ; e = e->l_next )
		if ( e->l_value == v )
			return 1;

	return 0;
}

int
SizeOfList(List *l)
{
	if ( l == (List *)NULL )
		return 0;

	return l->l_nel;
}
