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
 * NOTE: This list implementation is not synchronized across list iteration. If there are two threads 
 * simultaneously accessing the list, then one thread may affect the other's list access. For instance, 
 * if thread 1 calls GetNextElement(), then thread 2 calls GetListElelemt(), then the next time thread 1 calls
 * GetListElement(), it will get the 3rd element in the list, not the 2nd. Also, if two threads are accessing 
 * objects in the list, and freeing data associated with those objects while traversing the list, then this 
 * may cause problems where thread 1 frees data that thread 2 is about to access.
 * 
 * Applications wishing to use threads to access lists in this way should provide functions that obtain 
 * and release a global lock on the list which prevents any other thread from accessing the list while 
 * that lock is held.
 */

#include <stdio.h>
#include <stdlib.h>

#include	"compat.h"
#include	"list.h"

THREAD_DECL(list);

/*
 * Create a new empty list
 */
List *
NewList(void)
{
	List *	l;

	l = (List *)malloc(sizeof(List));

	l->l_head = (ListElement *)NULL;
	l->l_tail = &l->l_head;
	l->l_nel = 0;
	l->l_scan = NULL;

	return l;
}

/*
 * Add new element to end of list.
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
 * Add new element to beginning of list (stack emulation).
 */
void		
AddFirst(List *l, void *v)
{
	ListElement *	e;
	ListElement *	ep;

	if (l == (List *)NULL)
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

/*
 * Insert new element before given element
 */
void
InsertBefore(List *l, void *val, void *new_val) 
{
	ListElement **	e;
	ListElement *	ne;

	if (l == (List *)NULL) {
		return;
	}
	
	/*
	 * Find the element corresponding to val
	 */
	THREAD_LOCK(list);
	for (e = &l->l_head ; *e != (ListElement *)NULL ; e = &(*e)->l_next) {
		if ((*e)->l_value == val) {
			break;
		}
	}
	THREAD_UNLOCK(list);
	
	if (*e == NULL) {
		return;
	}

	ne = malloc(sizeof(ListElement));

	ne->l_value = new_val;
	THREAD_LOCK(list);
	ne->l_next = *e;
	*e = ne;
	l->l_nel++;
	THREAD_UNLOCK(list);	
}

/*
 * Append source list to destination list
 */
void
AppendList(List *dst, List *src)
{
	void *	e;

	SetList(src);

	while ( (e = GetListElement(src)) != (void *)NULL)
		AddToList(dst, e);
}

/*
 * Remove element from list
 */
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
 * Remove first element of list (stack emulation)
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

/*
 * Destroy list and its contents
 */
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

/*
 * Initialize list iterator
 */
void
SetList(List *l)
{
	if ( l == (List *)NULL )
		return;

	THREAD_LOCK(list);
	l->l_scan = l->l_head;
	THREAD_UNLOCK(list);
}

/*
 * Get next element from list. Returns NULL when there are no more elements
 */
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

/*
 * Get the first element in the list
 */
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

/*
 * Check if the list is empty. Returns true (1) if it is.
 */
int
EmptyList(List *l)
{
	int	res;
	
	THREAD_LOCK(list);
	res = (int)(l == (List *)NULL || l->l_nel == 0);
	THREAD_UNLOCK(list);
	
	return res;
}

/*
 * Check if element is in the list
 */
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

/*
 * Get the number of elements in the list
 */
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
