#ifndef STACKFRAME_H_
#define STACKFRAME_H_

#include "location.h"

struct stackframe {
	int		frame_level;
	location	frame_location;
};
typedef struct stackframe stackframe;

struct stackframelist {
	stackframe				frame;
	struct stackframelist *	next;
};
typedef struct stackframelist stackframelist;

#endif /*STACKFRAME_H_*/
