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

#ifndef __HUFFMAN_CODER__
#define __HUFFMAN_CODER__

#define CACHE_TRIGGER (3)
/* CACHE_SIZE_LIMIT should not be set to more than 16 */
#define CACHE_SIZE_LIMIT (16) 
#define LARGE_MESSAGE (8192)

struct HuffmanCompressionTable
{
    struct symbol *symbols; /* id is implicit - the index in the array */
    int *freq;
    int length;
    void *encoding_mem_pool;
};

struct HuffmanNode 
{
    int symbol_index;
    int frequency;
    int tree_height;
    int min_encoding_len;
    struct HuffmanNode *left;
    struct HuffmanNode *right;    
};

int init_huffman(struct compression_method *method, int nsymbols);
void free_huffman(struct compression_method *method);
int huffman_encoder(struct compression_method *method, struct compression_buffer *input);
struct compression_buffer *huffman_decoder(struct compression_method *method, unsigned char *in_buf, int compressed_len, int uncompressed_len, int decode_info_present);
void huffman_update(struct compression_method *method, int *freq);
unsigned char *huffman_decode_info(struct compression_method *method, int *len);

/* checks if this node is a leaf. Returns zero if internal node otherwise returns non-zero.
Note that we need to check only one of the children (left or right) since an internal node in
a huffman tree will always have two children */
#define is_leaf(node) ((node)->left == NULL)

#ifndef MAX
#define MAX(a,b) ((a) > (b)?(a):(b))
#endif
#ifndef MIN
#define MIN(a,b) ((a) < (b)?(a):(b))
#endif

#endif
