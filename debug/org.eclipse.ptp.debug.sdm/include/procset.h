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
