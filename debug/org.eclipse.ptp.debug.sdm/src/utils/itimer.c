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
#ifdef __gnu_linux__
#define _GNU_SOURCE
#endif /* __gnu_linux__ */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "itimer.h"

itimer *	
itimer_new(char *label) 
{
	static int	id = 0;
	itimer *		t = (itimer *)malloc(sizeof(itimer));
	
	if (label != NULL)
		t->label = strdup(label);
	else
		asprintf(&t->label, "timer_%d\n", id++);

	t->running = 0;
	t->total_elapsed = 0;
	t->mark_cnt = 0;
	return t;
}

void
itimer_start(itimer *t)
{
	if (t->running) {
		printf("warning: attempt to start a running timer\n"); fflush(stdout);
		return;
	}
		
	(void)gettimeofday(&t->start, NULL);

	t->last_mark.tv_sec = t->start.tv_sec;
	t->last_mark.tv_usec = t->start.tv_usec;
	t->running = 1;
	t->elapsed = 0;
}

void
itimer_reset(itimer *t)
{
	t->running = 0;
	t->elapsed = 0;
	t->total_elapsed = 0;
}

void
itimer_print(itimer *t)
{
	int i;
	
	printf("++++ %s: started at %ld+%ld\n", t->label, (long)t->start.tv_sec, (long)t->start.tv_usec); fflush(stdout);
	for (i = 0; i < t->mark_cnt; i++)
		printf("++++ %s: \"%s\" mark time is %ld ms\n", t->label, t->marks[i].label, t->marks[i].elapsed); fflush(stdout);	
	if (t->elapsed > 0)
		printf("++++ %s: last elapsed time is %ld ms\n", t->label, t->elapsed); fflush(stdout);
	if (t->total_elapsed > 0)
		printf("++++ %s: total elapsed time is %ld ms\n", t->label, t->total_elapsed); fflush(stdout);
}

void	
itimer_mark(itimer *t, char *msg) 
{
	struct timeval	mark;
	
	if (t->mark_cnt == MAX_MARKS)
		return;
		
	(void)gettimeofday(&mark, NULL);
	
	t->marks[t->mark_cnt].elapsed = ((long)mark.tv_sec * 1000 + (long)mark.tv_usec / 1000) 
		- ((long)t->last_mark.tv_sec * 1000 + (long)t->last_mark.tv_usec / 1000);
	
	t->marks[t->mark_cnt].label = strdup(msg);
	
	t->last_mark.tv_sec = mark.tv_sec;
	t->last_mark.tv_usec = mark.tv_usec;
	t->mark_cnt++;
}

void
itimer_stop(itimer *t)
{
	if (!t->running) {
		printf("warning: attempt to stop a stopped timer\n"); fflush(stdout);
		return;
	}
		
	(void)gettimeofday(&t->stop, NULL);
	
	t->elapsed = ((long)t->stop.tv_sec * 1000 + (long)t->stop.tv_usec / 1000) 
		- ((long)t->start.tv_sec * 1000 + (long)t->start.tv_usec / 1000);
	t->total_elapsed += t->elapsed;
	t->running = 0;
}

void
itimer_free(itimer *t) 
{
	int i;
	if (t->label != NULL)
		free(t->label);
	for (i = 0; i < t->mark_cnt; i++)
		free(t->marks[i].label);	
	free(t);
}
