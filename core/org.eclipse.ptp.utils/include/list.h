/*
** List definition
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

#ifndef _LIST_H_
#define _LIST_H_

struct List
{
	int						l_nel;
	struct ListElement *	l_head;
	struct ListElement *	l_scan;
	struct ListElement **	l_tail;
};
typedef struct List	List;

struct ListElement
{
	void *					l_value;
	struct ListElement *	l_next;
};
typedef struct ListElement	ListElement;

extern List *	NewList(void);
extern void		AddToList(List *, void *);
extern void		AddFirst(List *, void *);
extern void		AppendList(List *, List *);
extern void		RemoveFromList(List *, void *);
extern void *	RemoveFirst(List *);
extern void		DestroyList(List *, void (*)());
extern void		SetList(List *);
extern void *	GetListElement(List *);
extern void *	GetFirstElement(List *);
extern int		EmptyList(List *);
extern int		InList(List *, void *);
extern int		SizeOfList(List *);
#endif /* !_LIST_H_ */
