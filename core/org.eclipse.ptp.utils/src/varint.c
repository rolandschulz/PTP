/*
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

#include <stdio.h>

#include "varint.h"


/*
 * Convert the specified integer value to varint format and copy to the
 * supplied buffer. If 'end' is not NULL, return a pointer to the next
 * character after the varint in the buffer. Returns the length of
 * the converted varint.
 *
 * The varint format is described at
 * http://code.google.com/apis/protocolbuffers/docs/encoding.html#varints
 * Note that this function is not thread safe.
 */
int
varint_encode(int value, unsigned char *buf, unsigned char **end)
{
	unsigned char *	p = buf;
	unsigned int 	num;

	num = (unsigned int) value;
	do {
		*p++ = (num & 0x7f) | 0x80;
		num >>= 7;
	} while (num != 0);

	*(p-1) &= 0x7f;

	if (end != NULL) {
		*end = p;
	}

	return p - buf;
}


/*
 * Convert the data specified by varint_p to integer from varint format and
 * store the value in 'result'. If 'end' is not NULL, store a pointer to the next
 * character after the varint in the buffer. Returns the length of the
 * varint in the buffer.
 */
int
varint_decode(int *result, unsigned char *varint_p, unsigned char **end)
{
	unsigned char *p;
	int shift;
	int length;
	int value;

	p = varint_p;
	length = 0;
	value = 0;
	shift = 0;
	for (;;) {
		value |= ((*p & 0x7f) << shift);
		length++;
		if ((*p & 0x80) == 0x0) {
			break;
		}
		p++;
		shift += 7;
	}

	if (end != NULL) {
		*end = ++p;
	}

	*result = value;

	return length;
}

/*
 * Return the number of bytes required to hold an integer in varint format
 */
int varint_length(int num)
{
	if (num < 0) {
		return 5;
	}
	else {
		if (num < (1 << 7)) {
			return 1;
		}
		else if (num < (1 << 14)) {
			return 2;
		}
		else if (num < (1 << 21)) {
			return 3;
		}
		else if (num < (1 << 28)) {
			return 4;
		}
		else {
			return 5;
		}
	}
}
