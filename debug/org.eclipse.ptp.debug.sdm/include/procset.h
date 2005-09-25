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
 
#ifndef PROCSET_H_
#define PROCSET_H_

#include "bitvector.h"

#define BITVECTOR_TYPE			bitvector *
#define BITVECTOR_NUMBITS(v)		((v)->size)
#define BITVECTOR_CREATE(size)	bitvector_create(size)
#define BITVECTOR_FREE(v)			bitvector_free(v)
#define BITVECTOR_SET(v, bit)		bitvector_set((v), (bit))
#define BITVECTOR_UNSET(v, bit)	bitvector_unset((v), (bit))
#define BITVECTOR_GET(v, bit)		(bitvector_get((v), (bit))!=0)
#define BITVECTOR_COPY(v1, v2)	bitvector_copy((v2), (v1))
#define BITVECTOR_ISEMPTY(v)		bitvector_isempty(v)
#define BITVECTOR_ANDEQ(v1, v2)	bitvector_andeq((v1), (v2))
#define BITVECTOR_OREQ(v1, v2)	bitvector_oreq((v1), (v2))
#define BITVECTOR_AND(v1, v2, v3)	bitvector_and((v1), (v2), (v3))
#define BITVECTOR_OR(v1, v2, v3)	bitvector_or((v1), (v2), (v3))
#define BITVECTOR_INVERT(v1)		bitvector_invert((v1))
#define BITVECTOR_CLEAR(v1)		bitvector_clear((v1))

struct procset {
	char *			ps_name;
	int				ps_nprocs;
	BITVECTOR_TYPE	ps_procs;
};
typedef struct procset procset;

procset *	procset_new(int);
void			procset_free(procset *);
procset *	procset_copy(procset *);
int			procset_isempty(procset *);
void			procset_add_proc(procset *, int);
void			procset_remove_proc(procset *, int);
int			procset_test(procset *, int);
procset *	procset_and(procset *, procset *);
void			procset_andeq(procset *, procset *);
procset *	procset_or(procset *, procset *);
void			procset_oreq(procset *, procset *);
void			procset_invert(procset *);
char *		procset_to_str(procset *);
procset *	str_to_procset(char *);
int			procset_size(procset *);
char *		procset_to_set(procset *);
#endif /*PROCSET_H_*/
