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

#include "compression.h"
#include "huffman_byte_encoder.h"
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <limits.h>

/* given method and symbol id, dump the char representation of the symbol into 
out and store the length of the representation in bytes into rep_len. 
*/
void huffman_get_symbol_rep(struct compression_method *method, int id, unsigned char *out, int *rep_len)
{
    *rep_len = 1;
    *out = (unsigned char)id;
}

/* initilizes huffman object with 256 symbols */
int init_byte_huffman(struct compression_method *method)
{
    return init_huffman(method, NSYMBOLS);
}

/* Gets the symbol object corresponsing to the representation "rep" with length "len".
method - The compression_method object.
rep - The stream representation of the symbol
len - The length of the representation
Returns the symbol struct corresponding to this representation.
*/
struct symbol *huffman_get_symbol(struct compression_method *method, unsigned char *rep, int len)
{
    return ((struct HuffmanCompressionTable *)(method->compression_table))->symbols + *rep;
}
