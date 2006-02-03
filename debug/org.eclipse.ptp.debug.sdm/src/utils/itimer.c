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

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "itimer.h"

itimer *	
itimer_start(char *label) 
{
	static int	id = 0;
	itimer *		t = (itimer *)malloc(sizeof(itimer));
	
	if (label != NULL)
		t->label = strdup(label);
	else
		asprintf(&t->label, "timer_%d\n", id++);
		
	if (gettimeofday(&t->start, NULL) < 0)
		return NULL;
	
	t->mark.tv_sec = t->start.tv_sec;
	t->mark.tv_usec = t->start.tv_usec;

	printf("++++ %s: started at %ld+%ld\n", t->label, (long)t->start.tv_sec, (long)t->start.tv_usec); fflush(stdout);
	
	return t;
}

void	
itimer_mark(itimer *t, char *msg) 
{
	long				e;
	struct timeval	mark;
	
	(void)gettimeofday(&mark, NULL);
	
	e = ((long)mark.tv_sec * 1000 + (long)mark.tv_usec / 1000) - ((long)t->mark.tv_sec * 1000 + (long)t->mark.tv_usec / 1000);
	
	printf("++++ %s [%s]: elapsed time is %ld ms\n", t->label, msg, e); fflush(stdout);
	
	t->mark.tv_sec = mark.tv_sec;
	t->mark.tv_usec = mark.tv_usec;
}

void
itimer_finish(itimer *t) 
{
	long		e;
	
	(void)gettimeofday(&t->finish, NULL);
	
	e = ((long)t->finish.tv_sec * 1000 + (long)t->finish.tv_usec / 1000) 
		- ((long)t->start.tv_sec * 1000 + (long)t->start.tv_usec / 1000);
	
	printf("++++ %s: total elapsed time is %ld ms\n", t->label, e ); fflush(stdout);
}

void
itimer_free(itimer *t) 
{
	if (t->label != NULL)
		free(t->label);
	free(t);
}