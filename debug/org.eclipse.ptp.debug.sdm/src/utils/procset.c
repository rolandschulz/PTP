#include <stdlib.h>
#include "procset.h"

procset *
procset_new(int size)
{
	procset *	p;
	
	p = malloc(sizeof(procset));
	
	p->ps_procs = BITVECTOR_CREATE(size);
	
	return p;
}

void		
procset_free(procset *p)
{
	BITVECTOR_FREE(p->ps_procs);
	free(p);
}
	
void		
procset_add_proc(procset *p, int proc)
{
	BITVECTOR_SET(p->ps_procs, proc);
}

void		
procset_remove_proc(procset *p, int proc)
{
	BITVECTOR_UNSET(p->ps_procs, proc);
}

int		
procset_test(procset *p, int proc)
{
	return BITVECTOR_GET(p->ps_procs, proc);
}

char *
procset_to_str(procset *p)
{
	return ""; // TODO
}