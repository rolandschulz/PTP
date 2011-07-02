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

enum Errors compression_error_no = NO_ERROR;

/* prints diagnostic error message for last error. */
void print_compression_error(void)
{
    fprintf(stderr, "%s\n", error_strings[compression_error_no]);
}

/* free the memory associated with this buffer as well as this buffer */
void free_compression_buffer(struct compression_buffer *buf)
{
    free(buf->data);
    free(buf);
}

/* allocate compression buffer. Returns a compression buffer with memory mem_size or NULL in case of failure */
struct compression_buffer * alloc_compression_buffer(int mem_size)
{
    struct compression_buffer *ret = (struct compression_buffer *)malloc(sizeof(struct compression_buffer));
    if (ret == 0)
        return NULL;
	ret->data = (unsigned char *) malloc (mem_size);
    if (ret->data == NULL)
    {
        free(ret);
        return NULL;
    }
    return ret;
}

/* reallocs the compression buffer buf to new size new_mem_size. Returns the newly allocated buffer or NULL in case of failure */
void  *realloc_compression_buffer(struct compression_buffer *buf, int new_mem_size)
{
    return buf->data = realloc(buf->data, new_mem_size);
}

/* sets the pos bit in pattern to 1 if val is non-zero, otherwise to 0.*/
void set_bit(unsigned char *pattern, int pos, int val)
{
    pattern += pos / NBITSPERBYTE;
    val?((*pattern) |= 1 << pos % NBITSPERBYTE):((*pattern) &= ~(1 << pos % NBITSPERBYTE));
}

/* return non-zero if this machine is big endian architecture, otherwise zero */
int is_bigendian()
{
    unsigned int i = 1; 
    return !(*(unsigned char *)&i);
}

/* reverses the byte order of int representation */
void swap_int(unsigned int *in)
{
    unsigned int result = 0;
    unsigned char *c1 = (unsigned char *)in;
    unsigned char *c2 = (unsigned char *)&result;
    c2[0] = c1[3]; c2[1] = c1[2];
    c2[2] = c1[1]; c2[3] = c1[0];
    *in = result;    
}

/* initializes the compression.
 method - the algorithm (currently only HUFFMAN)
 comp_object - the compression object to initialize
 returns 1 on success and 0 on failure.
 */
int init_compression(int method, struct compression_method *comp_object)
{
    compression_error_no = NO_ERROR;
    if (comp_object == NULL)
    {
        compression_error_no = INVALID_ARGUMENT;
        return 0;
    }
    comp_object->decode_info = NULL;
    comp_object->compression_table = NULL;
    comp_object->free_list = NULL;
    switch(method)
    {
        case BYTE_HUFFMAN:
            {
                comp_object->type = BYTE_HUFFMAN;
                break;
            }
        default:
            compression_error_no = UNKNOWN_COMPRESSION_METHOD;
            return 0;
    }

    comp_object->encoder = &huffman_encoder;
    comp_object->decoder = &huffman_decoder;
    comp_object->get_dictionary = &huffman_decode_info;
    comp_object->initializer = &init_byte_huffman;
    comp_object->destructor = &free_huffman;
    comp_object->get_symbol = &huffman_get_symbol;
    comp_object->get_symbol_rep = &huffman_get_symbol_rep;
    comp_object->parser = NULL; /* constant 1 byte symbols */
    if (comp_object->initializer(comp_object) == 0)
        return 0;
    return 1;
}

/* cleanup and free any resources used by algorithm
    method - the compression method object
*/
void end_compression(struct compression_method *method)
{
    compression_error_no = NO_ERROR;
    method->destructor(method);
}

/* convention - lsb is 1st bit for below functions. Should use the same in Java */

/* generic function to pack bits of any len 
 pack len buts from pattern to stream starting at bit position at
*/
void pack_bits_into_stream(unsigned char *stream, unsigned char *pattern, int at, int len)
{
    int offset = at % NBITSPERBYTE;
    unsigned char *dest = stream + at / NBITSPERBYTE;
    int  mask;

    while(len > 0)
    {
        /* extract lower (NBITSPERBYTE - offset) bits from pattern */
        mask = (1 << (NBITSPERBYTE - offset)) - 1;

        *dest &=  ~(mask << offset);
        *dest |=  ((*pattern) & mask) << offset;
        *++dest = 0;
        *dest |= (*pattern) >> (NBITSPERBYTE - offset);
        pattern++;
        len -= NBITSPERBYTE;
    }
}

/* special case of pack_bits_into_stream - bits to be packed at current byte in stream at offset and len <= 8 bits */
static void pack_byte_bits(unsigned char *dest, unsigned char *pattern, int offset, int len)
{
    if (offset)
    {
        if (NBITSPERBYTE - offset >= len)
        {
            *dest &= ~(((1 << (NBITSPERBYTE - offset)) - 1) << offset);
            *dest |= (*pattern) << offset;
        }
        else
        {
            int mask = ((1 << (NBITSPERBYTE - offset)) - 1);
            *dest &= ~(mask << offset);
            *dest |= (*pattern) << offset;
            *(dest + 1) = (*pattern & ~mask) >> (NBITSPERBYTE - offset);
        }
              
    }
    else
        *dest = *pattern;


}

/* compresses the input buffer and returns the compressed buffer
 method - the compression method object
 in_buf - the input buffer to compress. Should have uncompressed_len set to number of bytes in buffer
 returns - the compressed buffer with compressed_len set to number of bytes in this buffer and uncompressed_len set to original number of bytes.  Returns NULL in case of failure.
 */
struct compression_buffer *compress(struct compression_method *method, struct compression_buffer *in_buf)
{
    struct compression_buffer *out;
    int current_buf_len;
    int bytes_left;
    unsigned char *curr_ptr;
    int bit_position = NBITSPERBYTE *  sizeof(int);
    int decode_len = 0;
    int symbol_len;
    int encoding_len;
    struct symbol *s;
    compression_error_no = NO_ERROR;
    unsigned char *decode_info = NULL;

    if (method == NULL || in_buf == NULL || in_buf->uncompressed_len == 0)
    {
        compression_error_no = INVALID_ARGUMENT;
        return NULL;
    }

    current_buf_len = (in_buf->uncompressed_len + sizeof(int) ) * NBITSPERBYTE;
    bytes_left = in_buf->uncompressed_len;
    curr_ptr = in_buf->data;

    get_symbol_func get_symbol = method->get_symbol;

    if (method->decode_info == NULL)
    {
        if (method->encoder(method, in_buf) == 0)
            return NULL;
        if ((decode_info = method->get_dictionary(method, &decode_len)) == NULL)
            return NULL;
        current_buf_len += decode_len * NBITSPERBYTE;
        bit_position += NBITSPERBYTE * decode_len;
        
    }
    if ((out = alloc_compression_buffer(current_buf_len / NBITSPERBYTE + 1 )) == NULL)
	{
		compression_error_no = OUT_OF_MEMORY;
		return 0;
	}
    stream_parser_func parser = method->parser;
    while(bytes_left)
    {
        symbol_len = (parser == NULL)?1:parser(curr_ptr);
        s = get_symbol(method, curr_ptr, symbol_len);
        encoding_len = s->encoding_length;
        /*  one might end up with a larger output */
        if ((encoding_len + bit_position) > current_buf_len )
        {
            current_buf_len += NBITSPERBYTE * encoding_len + current_buf_len;

            if (realloc_compression_buffer(out, current_buf_len/NBITSPERBYTE + 1 ) == NULL)
			{
				compression_error_no = OUT_OF_MEMORY;
				return 0;
			}
        }
        pack_bits_into_stream(out->data, s->encoding, bit_position, encoding_len);
        bit_position += encoding_len;
        curr_ptr += symbol_len;
        bytes_left-=symbol_len;
    }
    out->compressed_len = bit_position?(1 + bit_position / NBITSPERBYTE):0;
    *((int *)(out->data )) = out->uncompressed_len = in_buf->uncompressed_len;
    if (!is_bigendian())
        swap_int( (unsigned int *)(out->data) );
    if (decode_len)
        memcpy(out->data + sizeof(int), decode_info, decode_len);
    return out;
}

/* uncompresses the input buffer and returns the uncompressed buffer
 method - the compression method object
 in_buf - the input buffer to uncompress with compressed_len set to the size of this buffer and uncompressed_len set to  the original uncompressed len.
 returns - the uncompressed buffer with uncompressed_len set to the uncompressed len in bytes. Returns NULL in case of failure.
 */
struct compression_buffer *uncompress(struct compression_method *method, struct compression_buffer *in_buf)
{
    int uncompressed_len;
    compression_error_no = NO_ERROR;
    struct compression_buffer *ret;
    if (in_buf == NULL || method == NULL)
    {
        compression_error_no = INVALID_ARGUMENT;
        return NULL;
    }
    decoder_type decoder = method->decoder;

    /* unpack header data */
    uncompressed_len = *((int *)(in_buf->data));
    if (!is_bigendian())
        swap_int((unsigned int *)&uncompressed_len);
    ret = decoder(method, in_buf->data + sizeof(int), in_buf->compressed_len, uncompressed_len, in_buf->flags & COMPRESSION_TABLE_FLAG);
    return ret;
}
