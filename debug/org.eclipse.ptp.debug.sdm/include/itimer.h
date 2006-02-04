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
 
#ifndef _ITIMER_H_
#define _ITIMER_H_

#include <sys/time.h>

#define MAX_MARKS	100

struct it_mark {
	long		elapsed;
	char *	label;
};
typedef struct it_mark	it_mark;

struct itimer {
	int				running;
	int				mark_cnt;
	char *			label;
	char *			mark_label;
	long				elapsed;
	long				total_elapsed;
	struct timeval	start;
	struct timeval	last_mark;
	struct timeval	stop;
	it_mark			marks[MAX_MARKS];
};
typedef struct itimer		itimer;

extern itimer *	itimer_new(char *label);
extern void		itimer_start(itimer *t);
extern void		itimer_mark(itimer *t, char *msg);
extern void		itimer_stop(itimer *t);
extern void		itimer_reset(itimer *t);
extern void		itimer_free(itimer *t);
extern void		itimer_print(itimer *t);

#endif /* _ITIMER_H_*/
