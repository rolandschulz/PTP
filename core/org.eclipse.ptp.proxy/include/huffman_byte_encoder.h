/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

#ifndef __HUFFMAN_BYTE_ENCODER__
#define __HUFFMAN_BYTE_ENCODER__ 

#include "huffman_coder.h"

#define NSYMBOLS (256)

void huffman_get_symbol_rep(struct compression_method *method, int id, unsigned char *out, int *rep_len);
int init_byte_huffman(struct compression_method *method);
struct symbol *huffman_get_symbol(struct compression_method *method, unsigned char *rep, int len);

#endif
