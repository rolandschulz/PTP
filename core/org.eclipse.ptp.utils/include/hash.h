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
** Hash functions.
**
** Copyright (c) 1996-2001 by GuardSoft Pty. Ltd.
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

#ifndef _HASH_H_
#define _HASH_H_

struct HashEntry
{
	unsigned int		h_hval;	/* actual hash value */
	void *				h_data;	/* entry data */
	struct HashEntry *	h_next;	/* next in chain */
};
typedef struct HashEntry	HashEntry;

/* Data type for reentrant functions.  */
struct Hash
{
    HashEntry **	table;		/* hash chain entries */
    unsigned int	logsize;	/* log 2 number of chains in table */
    unsigned int	size;		/* number of chains in table */
    unsigned int	count;		/* number of entries in table */
    unsigned int	mask;		/* mask used to compute chain */
    unsigned int	scan;		/* used for scanning hash table */
    HashEntry *		scan_entry;
};
typedef struct Hash	Hash;

extern Hash *		HashCreate(int);
extern void			HashDestroy(Hash *, void (*)(void *));
extern void *		HashSearch(Hash *, unsigned int);
extern unsigned int	HashCompute(char *, int);
extern HashEntry *	HashInsert(Hash *, unsigned int, void *);
extern void			HashRemove(Hash *, unsigned int);
extern void			HashSet(Hash *);
extern HashEntry *	HashGet(Hash *);

#endif /* !_HASH_H_ */
