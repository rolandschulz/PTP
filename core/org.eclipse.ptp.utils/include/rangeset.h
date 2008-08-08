/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

#include "list.h"

struct str_buf {
	int		len;
	int		count;
	char *	contents;
};
typedef struct str_buf str_buf;

struct rangeset {
	List 	*	elements;
	str_buf *	buf;
	int			changed;
};
typedef struct rangeset	rangeset;

struct range {
	int low;
	int high;
};
typedef struct range	range;

extern rangeset *new_rangeset(void);
extern void insert_in_rangeset(rangeset *set, int val);
extern char *rangeset_to_string(rangeset *set);
extern void free_rangeset(rangeset *set);
