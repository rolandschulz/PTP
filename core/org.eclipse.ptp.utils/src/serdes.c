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

#include <stdlib.h>
#include <stdio.h>
#include <ctype.h>
#include <string.h>

#include "serdes.h"

/*
 * Convert a hexadecimal string containing len digits into an integer.
 */
unsigned int
hex_str_to_int(char *str, int len, char **end)
{
	int				n;
	unsigned int	val = 0;

	for (n = 0; n < len && *str != '\0' && isxdigit(*str); n++, str++) {
		val <<= 4;
		val += digittoint(*str);
	}

	if (end != NULL) {
		*end = str;
	}

	return val;
}

/*
 * Convert an integer to a hexadecimal string of len digits.
 */
void
int_to_hex_str(unsigned int val, char *str, int len, char **end)
{
	char *	buf = (char *)malloc(len+1);

	/*
	 * Need a separate buffer because sprintf adds a null to
	 * the end of the string.
	 */
	snprintf(buf, len+1, "%0*x", len, val & 0xffffffff);

	memcpy(str, buf, len);

	if (end != NULL) {
		*end = str + len;
	}

	free(buf);
}
