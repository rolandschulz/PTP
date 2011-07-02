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
#include "huffman_coder.h"
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <limits.h>

/* compare node frequencies for two nodes in Huffman tree. If they have the same 
frequencies, then sort based on symbol_index so that the
sort is stable (the original array had nodes in order of symbol_index)
*/
 static int compare_node_frequencies(const void *node1, const void *node2)
{
    struct HuffmanNode *n1 = (struct HuffmanNode *)node1;
    struct HuffmanNode *n2 = (struct HuffmanNode *)node2;
    int ret = n1->frequency - n2->frequency;
    return ret?ret:n1->symbol_index - n2->symbol_index;
}

/* Initialize out as a parent node with left child node1 and 
right child node2 and frequency  = totalfreq */
static  void make_node(struct HuffmanNode *node1, struct HuffmanNode *node2, int totalfreq, struct HuffmanNode *out)
{
    out->frequency = totalfreq; /* frequency doubles up as frequency count for internal nodes */
    out->left = node1;
    out->right = node2;
    out->tree_height = MAX(node1->tree_height, node2->tree_height) + 1;
    out->min_encoding_len = MIN(node1->min_encoding_len, node2->min_encoding_len) + 1;
}

/* initializes the given compression_method object 
Returns non-zero on success, 0 on failure.
*/
int init_huffman(struct compression_method *method, int nsymbols)
{
    struct HuffmanCompressionTable *huff_table;
    int i;
    huff_table = (struct HuffmanCompressionTable *)malloc(sizeof(struct HuffmanCompressionTable));
    if (huff_table == NULL)
	{
		compression_error_no = OUT_OF_MEMORY;
		return 0;
	}
    method->compression_table = huff_table;
    memset(huff_table, 0, sizeof(struct HuffmanCompressionTable));
    huff_table->length = nsymbols;
    huff_table->symbols = (struct symbol *)malloc(nsymbols * sizeof(struct symbol));
    huff_table->freq  = (int *)malloc(nsymbols * sizeof(int));
    if (huff_table->symbols == NULL || huff_table->freq == NULL)
	{
		compression_error_no = OUT_OF_MEMORY;
		return 0;
	}
    for(i=0; i < nsymbols; i++)
        huff_table->freq[i] = 1;
    memset(huff_table->symbols, 0, nsymbols * sizeof(struct symbol));
    huff_table->encoding_mem_pool = NULL;
    return 1;
}

/* Given compression_method, update the frequencies and 
   build the necessary internal data structures based on the input freq.
   Returns non-zero on success, 0 on failure.
*/
void huffman_update(struct compression_method *method, int *freq)
{
    struct HuffmanCompressionTable *huff_table = (struct HuffmanCompressionTable *)method->compression_table;
    memcpy(huff_table->freq, freq, huff_table->length * sizeof(int));
    method->decode_info = NULL;
}

/* This function serializes the information required to decode. This data is sent as a header before the huffman compressed stream after the compressed and uncompressed lengths */

unsigned char *huffman_decode_info(struct compression_method *method, int *len)
{
    struct HuffmanCompressionTable *huff_table = (struct HuffmanCompressionTable *)method->compression_table;    
    *len = huff_table->length * sizeof(int);
    if (!is_bigendian())
    {
        int i;
        for(i = 0; i < huff_table->length; i++)
            swap_int((unsigned int *)huff_table->freq + i);
    }
    return (unsigned char *)(huff_table->freq);
}

/* Frees the memory related to this method. */
void free_huffman(struct compression_method *method)
{
/* free objects allocated in this algorithm */
    struct HuffmanCompressionTable *huff_table = (struct HuffmanCompressionTable *)method->compression_table;    
    if (huff_table)
    {
        free(huff_table->symbols);
        free(huff_table->freq);
        free(huff_table->encoding_mem_pool);
        free(huff_table);
    }
    if (method->free_list)
    {
        free(method->free_list[0]);
        free(method->free_list[1]);
        free(method->free_list);
    }
}

/* Assigns huffman codes and stores it in the symbol data structure.
node - The root node of the huffmn tree to begin with.
method - The compression method object.
pattern - Memory place holder for the pattern.
len - Length of the pattern.
mem_avail - The memory pool previously allocated from which to use memory for the encoding.
Returns the next available byte in the memory pool
*/

static void *assign_huffmancodes(struct HuffmanNode *node, struct compression_method *method, unsigned char *pattern, int len, void *mem_avail)
{
    unsigned char *new_pattern;
    int leaf = is_leaf(node);
    int new_nints;
/* crossing an integer boundary. Increase memory for pattern by sizeof(int).  */
    new_nints = 1 + (leaf?len - 1:len) / (sizeof(int) * NBITSPERBYTE);
    if (!leaf && len % (sizeof(int) * NBITSPERBYTE) == 0) 
    {
		new_pattern = (unsigned char *)malloc(sizeof(int) * new_nints);
        if (new_pattern == NULL)
		{
			compression_error_no = OUT_OF_MEMORY;
			return NULL;
		}
        memset(new_pattern, 0, sizeof(int) * new_nints);
        if (pattern != NULL)
            memcpy(new_pattern, pattern, sizeof(int) * (new_nints - 1));
    }
    else
        new_pattern = pattern;
    if (!leaf)
    {
        set_bit(new_pattern, len, 0);
        if ((mem_avail = assign_huffmancodes(node->left, method, new_pattern, len + 1, mem_avail)) == NULL)
            return 0;
        set_bit(new_pattern, len, 1);
        if ((mem_avail = assign_huffmancodes(node->right, method, new_pattern, len + 1, mem_avail)) == NULL)
            return 0;
    }
    else
    {
        struct HuffmanCompressionTable *huff_table = (struct HuffmanCompressionTable *)method->compression_table;
        int id = node->symbol_index;
        struct symbol *symbol = &(huff_table->symbols[id]);
        /* mem_avail points to the current available memory in the memory pool allocated earlier. Use this for 
        encoding and set mem_avail appropriately */
        symbol->encoding = mem_avail;
        symbol->encoding_length = len;
        memcpy(symbol->encoding, new_pattern, new_nints * sizeof(int));
        mem_avail += new_nints * sizeof(int);
#ifdef DEBUG
        {    
                int i=0;
                for(i=0; i < new_nints; i++)
                {
                        printf("Id %d --> Encoding: %x, Length: %d\n ", id, *(((int *)symbol->encoding) + i), len);
                        fflush(stdout);
                }
        }
#endif
    }
    if (new_pattern != pattern)
        free(new_pattern);
    return mem_avail;
}

/* Given two queues of HuffmanNode objects, finds the two nodes with the lowest frequency from the front of the queue and returns informtion about them 
queue1 - The first queue of HuffmanNode objects.
queue2 - The second queue of HuffmanNode objects.
len1 - The length of queue1.
len2 = The length of queue2.
node1 - The node with the least frequency (out).
node2 - The node with the second least frequency (out).
low_index1 - The pointer having index into front of queue1.
low_index2 = The pointer hacing index into front of queue2.
*/
static void find_lowest_freq_nodes(struct HuffmanNode *queue1, struct HuffmanNode *queue2, int len1, int len2, struct HuffmanNode **node1, struct HuffmanNode **node2, int *low_index1, int *low_index2)
{
    struct HuffmanNode *third_node = NULL;
    *node1 = *node2 = NULL;

    /* queue1 and queue2 are already sorted by frequency. we need to check 4 nodes, the first two nodes of queue1 and the first two nodex of queue2 */
    if (len1 > 0)
    {
        if (len2 > 0)
        {
            int ret = queue1[0].frequency - queue2[0].frequency;
            int ret1;
            if (ret < 0)
            {
                *node1 = &queue1[0];
                *node2 = &queue2[0];
                if (len1 > 1)
                    third_node = &queue1[1];
                *low_index1 += 1;
            }
            else
            {
                *node1 = &queue2[0];
                *node2 = &queue1[0];
                if (len2 > 1)
                    third_node = &queue2[1];
                *low_index2 += 1;
            }
            if (third_node == NULL)
            {
                if (ret < 0)
                    *low_index2 += 1;
                else
                    *low_index1 += 1;
                return;
            }
            ret1 = (*node2)->frequency - third_node->frequency;
            if (ret1 > 0 )
            {
                *node2 = third_node;
                if (ret < 0)
                    *low_index1 += 1;
                else
                    *low_index2 += 1;
            } 
            else
            {
                if (ret < 0)
                    *low_index2 += 1;
                else
                    *low_index1 += 1;
            }
        }
        else
        {
            *node1 = &queue1[0];
            *node2 = &queue1[1];
            *low_index1 += 2;
        }
    }
    else
    {    
        *node1 = &queue2[0];
        *node2 = &queue2[1];
        *low_index2 += 2;
    }
}

/* builds the huffman tree given the method
Returns the root node of the huffman tree
*/
static struct HuffmanNode *build_huffman_tree(struct compression_method *method)
{
    
    struct HuffmanCompressionTable *huff_table = (struct HuffmanCompressionTable *)method->compression_table;
    struct HuffmanNode *symbol_nodes;
    struct HuffmanNode *sorted_queue;
    int i;
    int low_index1;
    int high_index1;
    int low_index2;
    int high_index2;
    int nodes_left;
    struct HuffmanNode *node1;
    struct HuffmanNode *node2;
    int total_freq;

    if (method->free_list)
    {
        symbol_nodes = (struct HuffmanNode *)method->free_list[0];
        sorted_queue = (struct HuffmanNode *)method->free_list[1];
    }
    else
    {
        symbol_nodes = (struct HuffmanNode *)malloc(sizeof(struct HuffmanNode) * huff_table->length * 2); /*This is the first queue. total nodes required = 2n-1*/
        sorted_queue = (struct HuffmanNode *)malloc(sizeof(struct HuffmanNode) * huff_table->length * 2); /* second queue */
    }
    if (symbol_nodes == NULL || sorted_queue == NULL) 
	{
        compression_error_no = OUT_OF_MEMORY;
		return NULL;
	}
    for(i=0; i < huff_table->length; i++)
    {
        symbol_nodes[i].symbol_index = i;
        symbol_nodes[i].left = symbol_nodes[i].right = NULL;
        symbol_nodes[i].tree_height = 0;
        symbol_nodes[i].min_encoding_len = 0;
        symbol_nodes[i].frequency = huff_table->freq[i];
    }
    /* need stable sort to ensure consistency btween C & java */
    /* nothing special done here. comparator ensures stableness */
    qsort(symbol_nodes, huff_table->length, sizeof(struct HuffmanNode), compare_node_frequencies);

    low_index1 = 0; 
    low_index2 = 0;
    high_index1 = huff_table->length; 
    high_index2 = 0;

    nodes_left = high_index1 - low_index1 + high_index2 - low_index2;
    while(nodes_left > 1)
    {
        find_lowest_freq_nodes(symbol_nodes + low_index1, sorted_queue + low_index2, high_index1 - low_index1, high_index2 - low_index2, &node1, &node2, &low_index1, &low_index2);
        total_freq  = node1->frequency + node2->frequency;
            
        if (high_index1 - low_index1 == 0 || total_freq > symbol_nodes[high_index1-1].frequency)
                make_node(node1, node2, total_freq, symbol_nodes + high_index1++);
        else
                make_node(node1, node2, total_freq, sorted_queue + high_index2++);

        /* each time we take two nodes and form one, hence nodes_left-- */
        nodes_left--;
    }
    if (method->free_list == NULL)
    {
        /* to be deleted later */
		method->free_list = (void **)malloc(sizeof(void *) * 2);
        if (method->free_list == NULL)
        {
            free(sorted_queue); 
            free(symbol_nodes);
			compression_error_no = OUT_OF_MEMORY;
			return NULL;
        }
        method->free_list[0] = symbol_nodes; 
        method->free_list[1] = sorted_queue;
    }

    if (high_index1 - low_index1 > 0)
        return &symbol_nodes[low_index1];    
    else
        return &sorted_queue[low_index2];
}

 static int validate_frequencies(struct HuffmanCompressionTable *huff_table)
{
    int i;
    int count = 0;
    for(i=0; i < huff_table->length; i++)
        if (huff_table->freq[i] > 0)
        {
            count++;
            if (count > 1)
                break;
        }
    if (count == 0)
    {
        /* we don't want all zero frequencies */
        compression_error_no = ALL_ZERO_FREQUENCIES;
        return 0;
    }
    if (count == 1)
    {
        /* or only a single symbol occuring. Use run length encoding instead */
        compression_error_no = SINGLE_SYMBOL_DEGENERATE;
        return 0;
    }

    return 1;
}

/* Builds Huffman tree, assigns Huffman codes and returns the symbol frequencies for decoding purposes.
method - The compression method object.
input - The input compression buffer. For dynamic huffman, frequencies will be updated by parsing input.
len - length in bytes of the char buffer returned (out)
Returns a char buffer that can be used for decoding. In this case the frequencies
*/
int huffman_encoder(struct compression_method *method, struct compression_buffer *input)
{

    struct HuffmanCompressionTable *huff_table = (struct HuffmanCompressionTable *)method->compression_table;
    struct HuffmanNode *root, *prev_root;
    int nbits_int = sizeof(int) * NBITSPERBYTE;

    if (validate_frequencies(huff_table) == 0)
        return 0;
    prev_root = (struct HuffmanNode *)method->decode_info;

    /* frequencies are updated at this point. Build huffman tree. */
    method->decode_info = root = build_huffman_tree(method);
    if (root == NULL)
	{
		compression_error_no = OUT_OF_MEMORY;
		return 0;
	}

    /* memory pool for encoding. Avoid repeated small mallocs. Also check if previously allocated mem pool is enough. If so use it. */
    if (prev_root == NULL || prev_root->tree_height / nbits_int < root->tree_height / nbits_int)
    {
        free(huff_table->encoding_mem_pool);
		huff_table->encoding_mem_pool = malloc( (1 + (root->tree_height/ nbits_int)) * sizeof(int) * huff_table->length);
        if (huff_table->encoding_mem_pool == NULL)
		{
			compression_error_no = OUT_OF_MEMORY;
			return 0;
		}
    }

#if DEBUG
    printf("Max height: %d, Min encoding len %d\n", root->tree_height, root->min_encoding_len);
#endif

    if (assign_huffmancodes(root, method, 0, 0, huff_table->encoding_mem_pool) == NULL)
        return 0;
    return 1;
}

static struct compression_buffer *huffman_decoder_cached(struct compression_method *method, unsigned char *in_buf, int current_bit_pos, int compressed_len, int uncompressed_len);

/* Given the compression_method "method", deocdes the input compression_buffer and returns the uncompressed compression_buffer
method - The compression method.
in_buf - The input compression buffer to be decoded. uncompressed_len should be equal to the original length of the buffer and 
compressed_len should be equal to the length of this buffer in bytes.
Returns the decoded buffer with uncompressed_len set to the number of bytes in this buffer
*/
struct compression_buffer *huffman_decoder(struct compression_method *method, unsigned char *in_buf, int compressed_len, int uncompressed_len, int decode_info_present)
{
    struct HuffmanNode *root = (struct HuffmanNode *)method->decode_info;
    int i;
    struct HuffmanCompressionTable *huff_table = (struct HuffmanCompressionTable *)(method->compression_table);
    int current_bit_pos = 0;

    if (decode_info_present)
    {
            memcpy(huff_table->freq, in_buf, huff_table->length * sizeof(int));
            if (!is_bigendian())
                for(i=0; i < huff_table->length; i++)
                    swap_int((unsigned int *)huff_table->freq + i);
            current_bit_pos += NBITSPERBYTE * huff_table->length * sizeof(int);
    }
    else if (root == NULL)
    {
        int i;
        for(i = 0;i < huff_table->length; i++)
            huff_table->freq[i] = 1;
    }
    method->decode_info = root = build_huffman_tree(method);
    if (root == NULL)
	{
		compression_error_no = OUT_OF_MEMORY;
		return NULL;
	}

    /* use cached version if the tree is deep - ie min tree height is > CACHE_TRIGGER */
    if (root->min_encoding_len > CACHE_TRIGGER && root->min_encoding_len < CACHE_SIZE_LIMIT && compressed_len > LARGE_MESSAGE)
        return huffman_decoder_cached(method, in_buf, current_bit_pos, compressed_len, uncompressed_len);
    else
    {
        struct HuffmanNode *curr_node;
        struct compression_buffer *out_buf;
        int out_pos = 0;
        int rep_len;
        int symbol_id;
        out_buf = alloc_compression_buffer(sizeof(char) * uncompressed_len);
        out_buf->uncompressed_len = uncompressed_len;
        curr_node=root;
        while(1)
        {
                if (!is_leaf(curr_node))
                {

                        curr_node = get_bit(in_buf, current_bit_pos)? curr_node->right: curr_node->left;
                        current_bit_pos++;
                }
                else
                {
                        symbol_id = curr_node->symbol_index;
                        method->get_symbol_rep(method, symbol_id, out_buf->data + out_pos, &rep_len);
                        out_pos += rep_len;
                        curr_node = root;
                        if (out_pos >= uncompressed_len)
                                break;
                }
        }
        return out_buf;
    }
}

/* For big endian architecture, gets len bits from data starting from position "at". The bits are returned as the
lsb bits of the integer returned. 
Restriction: len should be <= 16 bits.
*/
static  unsigned int get_n_bits_big_endian(unsigned char *data, int at, int len)
{
    static unsigned int iret;
    static int offset;
    static unsigned char *ret; 
    iret = 0;
    offset = at % NBITSPERBYTE;
    ret = ((unsigned char *)&iret) + 3;

    data  += at / NBITSPERBYTE;
    *ret = *data;
    len -=  NBITSPERBYTE - offset;
    if (len > 0)
    {
        *--ret = *++data;
        len -= NBITSPERBYTE;
        if (len > 0)
            *--ret = *++data;
    }
    return iret >> offset;
}

/* For little endian architecture, gets len bits from data starting from position "at". The bits are returned as the
lsb bits of the integer returned. 
Restriction: len should be <= 16 bits.
*/
static  unsigned int get_n_bits_little_endian(unsigned char *data, int at, int len)
{
    static unsigned int iret;
    static int offset;
    static unsigned char *ret;
    iret = 0;
    offset = at % NBITSPERBYTE;
    ret = ((unsigned char *)&iret);

    data  += at / NBITSPERBYTE;
    *ret = *data;
    len -=  NBITSPERBYTE - offset;
    if (len > 0)
    {
        *++ret = *++data;
        len -= NBITSPERBYTE;
        if (len > 0)
            *++ret = *++data;
    }
    return iret >> offset;
}

/* Implements the huffman_decoder interface described above with caching upto the min tree height level.
For example, if min encoding length is 5 bits, it takes the first 5 bits of the input buffer 
and uses that as the index into a cache array.
*/
static struct compression_buffer *huffman_decoder_cached(struct compression_method *method, unsigned char *in_buf, int current_bit_pos, int compressed_len, int uncompressed_len)
{
    struct HuffmanNode *root = (struct HuffmanNode *)method->decode_info;
    struct HuffmanNode *curr_node;
    struct compression_buffer *out_buf;
    int out_pos = 0;
    int rep_len;
    int symbol_id;
    int min_length;
    struct HuffmanNode **cache = NULL;
    int array_size = 0;
    unsigned int part_bits = UINT_MAX;
    int bit_len = 0;
    int cache_filled=0;
    unsigned int (*get_n_bits)(unsigned char *, int, int);

    min_length = root->min_encoding_len;

    /* cache size is 2 power min_length */
    if (min_length < CACHE_SIZE_LIMIT)
    {
		cache = malloc(sizeof(void *) * (array_size = (1 << min_length)));
        if (cache  == NULL)
		{
			compression_error_no = OUT_OF_MEMORY;
			return NULL;
		}
        memset(cache, 0, array_size * sizeof(void *));
    }

    out_buf = alloc_compression_buffer(sizeof(char) * uncompressed_len);
    out_buf->uncompressed_len = uncompressed_len;

    curr_node=root;
    get_n_bits =  (is_bigendian())?&get_n_bits_big_endian: &get_n_bits_little_endian;
    while(1)
    {
        if (!is_leaf(curr_node))
        {

            curr_node = get_bit(in_buf, current_bit_pos)? curr_node->right: curr_node->left;
            current_bit_pos++;
            bit_len++;
            if ((bit_len == min_length) && part_bits != UINT_MAX && cache)
            {
                /* fill the cache */
                cache[part_bits] = curr_node;
                cache_filled++;
            }
        }
        else
        {
            symbol_id = curr_node->symbol_index;
            method->get_symbol_rep(method, symbol_id, out_buf->data + out_pos, &rep_len);
            out_pos += rep_len;
            if (out_pos >= uncompressed_len)
                break;
            curr_node = root;
            bit_len = 0;
            if (cache)
            {
                /* if we have filled all entries in the cache, we break and
                use a simplified loop */
                if (cache_filled == array_size)
                    break;
                part_bits = get_n_bits(in_buf, current_bit_pos, min_length) % array_size;
                if (cache[part_bits])
                {
                    /* use the cache */
                    curr_node = cache[part_bits];
                    current_bit_pos += min_length;
                    bit_len = min_length;
                }
            }

        }
    }
    if (out_pos < uncompressed_len)
    {
        /* All cache entries has already been filled. Only use */
        while(1)
        {
            if (!is_leaf(curr_node))
            {

                curr_node = get_bit(in_buf, current_bit_pos)? curr_node->right: curr_node->left;
                current_bit_pos++;
            }
            else
            {
                symbol_id = curr_node->symbol_index;
                method->get_symbol_rep(method, symbol_id, out_buf->data + out_pos, &rep_len);
                out_pos += rep_len;
                if (out_pos >= uncompressed_len)
                    break;
                part_bits = get_n_bits(in_buf, current_bit_pos, min_length) % array_size;
                curr_node = cache[part_bits];
                current_bit_pos += min_length;

            }
        }
    }
    free(cache);
    return out_buf;
}
