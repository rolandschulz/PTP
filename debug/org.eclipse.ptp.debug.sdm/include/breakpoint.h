#ifndef BREAKPOINT_H_
#define BREAKPOINT_H_

#include "location.h"

struct breakpoint {
	int			bp_id;
	int			bp_ignore;
	int			bp_special;
	int			bp_deleted;
	char *		bp_type;
	location		bp_loc;
	int			bp_hits;
	char *		bp_stmt;
	char **		bp_commands;
};
typedef struct breakpoint breakpoint;

struct breakpointlist {
	breakpoint		bp;
	struct breakpointlist *	next;
};
typedef struct breakpointlist breakpointlist;

#endif /*BREAKPOINT_H_*/
