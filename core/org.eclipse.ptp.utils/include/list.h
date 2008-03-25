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
extern void		InsertBefore(List *l, void *val, void *new_val);
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
