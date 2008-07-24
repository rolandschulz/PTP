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

#include "config.h"

#include <stdio.h>
#include <ctype.h>
#include "serdes.h"

/*
 * Convert a hexadecimal string into an integer.
 */
unsigned int
hex_str_to_int(char *str, char **end)
{
	int				n;
	unsigned int	val = 0;

	for (n = 0; n < HEX_LEN && *str != '\0' && isxdigit(*str); n++, str++) {
		val <<= 4;
		val += digittoint(*str);
	}

	if (end != NULL) {
		*end = str;
	}

	return val;
}

/*
 * Convert an integer to a hexadecimal string.
 */
void
int_to_hex_str(unsigned int val, char *str, char **end)
{
	sprintf(str, "%08x", val & 0xffffffff);
	if (end != NULL) {
		*end = str + HEX_LEN;
	}
}
