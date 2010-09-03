/*
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

#ifndef VARINT_H_
#define VARINT_H_

#define MAX_VARINT_LENGTH	5

int varint_encode(int value, unsigned char *varint_p, unsigned char **end);
int	varint_decode(int *result, unsigned char *varint_p, unsigned char **end);
int varint_length(int value);

#endif /* VARINT_H_ */
