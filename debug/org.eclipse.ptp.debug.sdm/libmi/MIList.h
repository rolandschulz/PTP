/******************************************************************************
 * Copyright (c) 2005, 2010 The Regents of the University of California and others
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

#ifndef _MILIST_H_
#define _MILIST_H_

struct MIList
{
	int						l_nel;
	struct MIListElement *	l_head;
	struct MIListElement *	l_scan;
	struct MIListElement **	l_tail;
};
typedef struct MIList	MIList;

struct MIListElement
{
	void *					l_value;
	struct MIListElement *	l_next;
};
typedef struct MIListElement	MIListElement;

extern MIList *	MIListNew(void);
extern void		MIListAdd(MIList *, void *);
extern void		MIListAddFirst(MIList *, void *);
extern void		MIListAppend(MIList *, MIList *);
extern void		MIListInsertBefore(MIList *l, void *val, void *new_val);
extern void		MIListRemove(MIList *, void *);
extern void *	MIListRemoveFirst(MIList *);
extern void		MIListFree(MIList *, void (*)());
extern void		MIListSet(MIList *);
extern void *	MIListGet(MIList *);
extern void *	MIListGetFirstElement(MIList *);
extern int		MIListIsEmpty(MIList *);
extern int		MIListTest(MIList *, void *);
extern int		MIListSize(MIList *);
#endif /* !_MILIST_H_ */
