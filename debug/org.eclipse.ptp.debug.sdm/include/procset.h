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

#define CMD_BUF_SIZE 100
#define REPLY_BUF_SIZE 100

#define MAX_PROC_SET	100

#define BITVECTOR_TYPE	bitvector *
#define BITVECTOR_CREATE	bitvector_create
#define BITVECTOR_FREE	bitvector_free
#define BITVECTOR_SET		bitvector_set
#define BITVECTOR_UNSET	bitvector_unset
#define BITVECTOR_GET		bitvector_get

struct procset {
	char *			ps_name;
	BITVECTOR_TYPE	ps_procs;
};
typedef struct procset procset;

procset *procset_new(int);
void		procset_free(procset *);
void		procset_add_proc(procset *, int);
void		procset_remove_proc(procset *, int);
int		procset_test(procset *, int);
char *	procset_to_str(procset *);
#endif /*PROCSET_H_*/
