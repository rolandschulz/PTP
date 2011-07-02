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

#ifndef __COMPRESION__
#define __COMPRESION__ 


#include <string.h>
#include <stdlib.h>
#include <limits.h>
#include <stdio.h>

#define NBITSPERBYTE (8)
#ifndef NULL
#define NULL (0)
#endif

#define COMPRESSION_TABLE_FLAG		 0x10

enum Compression_Algorithms {BYTE_HUFFMAN};
enum Errors {NO_ERROR, OUT_OF_MEMORY, UNKNOWN_COMPRESSION_METHOD, ALL_ZERO_FREQUENCIES, SINGLE_SYMBOL_DEGENERATE, INVALID_ARGUMENT};

static char *error_strings [] = {
    "No error.", "Out of memory.", "Unknown compression method.", "All frequency inputs are zero.", "Degenerate case. Only one symbol in input stream.", "Invalid input argument(s)."
};

extern enum Errors compression_error_no;

void print_compression_error(void);

struct compression_buffer
{
    unsigned char *data;
    int compressed_len;
    int uncompressed_len;
	unsigned char flags;
};

/* symbol struct only has the encoding. Mapping to original representation will be responsibility of the implementation */
struct symbol
{
    unsigned char *encoding;
    int encoding_length;
};

struct compression_method;

/* a bunch of pointer to functions. Implementations will be provided by the actual compression algorithm being used */
typedef int (*init_compression_func)(struct compression_method *);

typedef int (*encoder_type)(struct compression_method *method, struct compression_buffer *input);

typedef unsigned char *(*decode_info_type)(struct compression_method *method, int *len);

/* this function decodes the data and returns the decoded data */
typedef struct compression_buffer *(*decoder_type)(struct compression_method *method, unsigned char *in, int compressed_len, int uncompressed_len, int decoder_info_present);

typedef void (*free_compression_func)(struct compression_method *method);

typedef struct symbol *(*get_symbol_func)(struct compression_method *method, unsigned char *rep, int len);


/*parses uncompressed input stream and returns size of next symbol */
typedef int (*stream_parser_func)(const unsigned char *input_stream);

typedef void (*get_symbol_rep_func)(struct compression_method *method, int id, unsigned char *out, int *rep_len);


struct compression_method
{
    enum Compression_Algorithms type;
    encoder_type encoder;
    decoder_type decoder;
    decode_info_type get_dictionary;
    init_compression_func initializer;
    free_compression_func destructor;
    stream_parser_func parser;
    get_symbol_func get_symbol;
    get_symbol_rep_func get_symbol_rep;

    void *decode_info; /* data structure used for decoding - huffman tree for ex */
    void *compression_table; /* some data structure used for storing information about the symbols  in the input stream */
    void **free_list; /*list of objects on heap to be freed */
};


/* compression related methods */

/* initialize compression object */
int init_compression(int method, struct compression_method *comp_object);
struct compression_buffer *compress(struct compression_method *method, struct compression_buffer *in_buf);
struct compression_buffer *uncompress(struct compression_method *method, struct compression_buffer *in_buf);
/* called at the end to free memory and other cleanups.  */
void end_compression(struct compression_method *method);

void pack_bits_into_stream(unsigned char *stream, unsigned char *pattern, int at, int len);


/* free the memory associated with this buffer as well as this buffer */
void free_compression_buffer(struct compression_buffer *buf);

/* allocate compression buffer. Returns a compression buffer with memory mem_size or NULL in case of failure */
struct compression_buffer * alloc_compression_buffer(int mem_size);

/* reallocs the compression buffer buf to new size new_mem_size. Returns the newly allocated buffer or NULL in case of failure */
void  *realloc_compression_buffer(struct compression_buffer *buf, int new_mem_size);

/* sets the pos bit in pattern to 1 if val is non-zero, otherwise to 0.*/
void set_bit(unsigned char *pattern, int pos, int val);

/* return non-zero if this machine is big endian architecture, otherwise zero */
int is_bigendian();

/* reverses the byte order of int representation */
void swap_int(unsigned int *in);

/* gets the pos bit in pattern. Returns zero if the bit is zero otherwise returns non-zero */
#define get_bit(pattern, pos) ((int)(*((pattern) + (pos) / NBITSPERBYTE) & (1 << (pos) % NBITSPERBYTE)))

#endif
