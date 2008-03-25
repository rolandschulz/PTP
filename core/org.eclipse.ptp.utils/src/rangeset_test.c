/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>
#include <ctype.h>

#include "compat.h"
#include "rangeset.h"

int
main(int argc, char *argv[])
{
	rangeset *	set = new_rangeset();

	insert_in_rangeset(set, 5);
	printf("rangeset = %s\n", rangeset_to_string(set));

	insert_in_rangeset(set, 1);
	insert_in_rangeset(set, 2);
	insert_in_rangeset(set, 6);
	insert_in_rangeset(set, 7);
	insert_in_rangeset(set, 4);
	insert_in_rangeset(set, 15);
	insert_in_rangeset(set, 9);
	insert_in_rangeset(set, 14);

	printf("rangeset = %s\n", rangeset_to_string(set));
	
	free_rangeset(set);
	
	return 0;
}
