/******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California.
 * This material was produced under U.S. Government contract W-7405-ENG-36
 * for Los Alamos National Laboratory, which is operated by the University
 * of California for the U.S. Department of Energy. The U.S. Government has
 * rights to use, reproduce, and distribute this software. NEITHER THE
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified
 * to produce derivative works, such modified software should be clearly
 * marked, so as not to confuse it with the version available from LANL.
 *
 * Additionally, this program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * LA-CC 04-115
 ******************************************************************************/
#ifdef __gnu_linux__
#define _GNU_SOURCE
#endif /* __gnu_linux__ */

#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <ctype.h>
#include <assert.h>

#include "proxy.h"
#include "proxy_msg.h"
#include "args.h"
#include "list.h"
#include "serdes.h"
#include "compat.h"
#include "varint.h"

#ifdef __linux__
extern int digittoint(int c);
#endif /* __linux__ */

#define ARG_SIZE	100
#define PACKET_SIZE_INCREMENT 1024

static int				packet_allocation;
static int 				packet_size;
static unsigned char *	packet;
static int				proxy_flow_control = 0;

static char tohex[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

static void packet_append_varint(int val);
static void packet_append_bytes(int length, char *data);
static void packet_append_type(int attr_type);

/*
 * Convert a message to a packet ready to send over the wire.
 *
 * Note: packet length is added when the packet is transmitted.
 */
int
proxy_serialize_msg(proxy_msg *m, unsigned char **result, int *result_len)
{
	int		i;
	char *	p;

	if (m == NULL) {
		return -1;
	}

	/*
	 * Allocate the packet
	 */
	packet_allocation = PACKET_SIZE_INCREMENT;
	packet = (unsigned char *) malloc(packet_allocation);
	assert(packet != NULL);
	packet_size = 0;

	/*
	 * Compression flag byte is first byte in buffer.
	 * For now, there is no compression. If the buffer is compressed,
	 * the compression code will set the flag.
	 */
	packet_append_varint(0);
	
	/*
	 * Copy message id, transaction id and parameter count into buffer
	 * There is no need to check if the packet buffer is full at this point
	 * since the buffer is guaranteed to be large enough to hold three
	 * varints.
	 */
	packet_append_varint(m->msg_id);
	packet_append_varint(m->trans_id);
	packet_append_varint(m->num_args);

	/*
	 * Iterate thru the message paremeters, copying them to the packet
	 * buffer. Message parameters in the message's arg's array are either
	 * a single string or a key=value string pair.
	 * Paremeters are converted into a pair of length/value pairs when
	 * they are copied to the packet buffer, with the first length/value
	 * pair being the key of a key=value pair and the second length/value
	 * pair being the value of a key=value pair or a stand-alone string's
	 * value.
	 */
	for (i = 0; i < m->num_args; i++) {
		/*
		 * Split key=value pair at '='. If there is no '=' then
		 * key is omitted.
		 */
		packet_append_type(STRING_ATTR);
		p = strchr(m->args[i], '=');
		if (p == NULL) {
			/* Key is omitted, so set it's length to zero then
			 * append the length and data for the value
			 */
			packet_append_varint(0);
			packet_append_varint(strlen(m->args[i]));
			packet_append_bytes(strlen(m->args[i]), m->args[i]);
		} else {
			/*
			 * Parameter is key=value pair. Append fields for
			 * key and value to buffer. 
			 */
			char * key = strtok(m->args[i], "=");
			packet_append_varint(strlen(key));
			packet_append_bytes(strlen(key), key);
			packet_append_varint(strlen(p + 1));
			packet_append_bytes(strlen(p + 1), p + 1);
		}
	}
	*result = packet;
	*result_len = packet_size;

	return 0;
}

/*
 * Append a length field in varint format to the packet buffer
 */
void
packet_append_varint(int value)
{
	if ((packet_allocation - packet_size) < MAX_VARINT_LENGTH) {
		packet_allocation += PACKET_SIZE_INCREMENT;
		packet = (unsigned char *) realloc(packet, packet_allocation);
		assert(packet != NULL);
	}
	packet_size += varint_encode(value, &packet[packet_size], NULL);
}

/*
 * Append 'length' bytes to the end of the packet buffer, reallocating the
 * buffer if necessary
 */
void
packet_append_bytes(int length, char *data)
{
	if ((packet_allocation - packet_size) < length) {
		packet_allocation += length;
		packet = (unsigned char *) realloc(packet, packet_allocation);
		assert(packet != NULL);
	}
	memcpy(&packet[packet_size], data, length);
	packet_size += length;
}

/*
 * Append the attribute type for the following message argument to the packet
 * buffer.
 */
void
packet_append_type(int attr_type)
{
	if ((packet_allocation - packet_size) <= 0) {
		packet_allocation += PACKET_SIZE_INCREMENT;
		packet = (unsigned char *) realloc(packet, packet_allocation);
		assert(packet != NULL);
	}
	packet[packet_size++] = attr_type;
}

void
proxy_get_data(char *str, char **data, int *len)
{
	int		data_len;
	char	ch;
	char *	p;

	data_len = strlen(str) / 2;

	*len = data_len;
	*data = p = (char *)malloc(sizeof(char) * data_len);

	for (; data_len > 0; data_len--) {
		ch = digittoint(*str++);
		ch <<= 4;
		ch |= digittoint(*str++);

		*p++ = ch;
	}
}

void
proxy_get_int(char *str, int *val)
{
	*val = (int)strtol(str, NULL, 10);
}

void
proxy_get_bitset(unsigned char *str, bitset **b)
{
	*b = bitset_decode(str, NULL);
}

/*
 * Convert wire protocol to message.
 *
 * Packet length has already been removed.
 */
int
proxy_deserialize_msg(unsigned char *packet, int packet_len, proxy_msg **msg)
{
	int					i;
	int					flags;
	int					msg_id;
	int					trans_id;
	int					num_args;
	proxy_msg *			m = NULL;
	char *				arg;

	if (packet == NULL) {
		return -1;
	}

	/*
	 * flags
	 */
	varint_decode(&flags, packet, &packet);

	/*
	 * message ID
	 */
	varint_decode(&msg_id, packet, &packet);

	/*
	 * transaction ID
	 */
	varint_decode(&trans_id, packet, &packet);

	/*
	 * number of args
	 */
	varint_decode(&num_args, packet, &packet);

	m = new_proxy_msg(msg_id, trans_id);

	for (i = 0; i < num_args; i++) {
		if (proxy_msg_decode_string(packet, &arg, &packet) < 0) {
			free_proxy_msg(m);
			return -1;
		}
		proxy_msg_add_string_nocopy(m, arg);
	}

	*msg = m;

	return 0;
}

/*
 * Decode string argument. Returns pointer to the character after
 * the end of the string in 'end'
 */
int
proxy_msg_decode_string(unsigned char *packet, char **arg, unsigned char **end)
{
	char *			buf = NULL;
	char *			p;
	unsigned char *	key_str;
	unsigned char *	val_str;
	char			arg_type;
	int				key_length;
	int				val_length;

	arg_type = *packet++;
	switch (arg_type) {
	case '\0':
		varint_decode(&key_length, packet, &packet);
		key_str = packet;
		packet += key_length;

		varint_decode(&val_length, packet, &packet);
		val_str = packet;
		packet += val_length;

		if ((key_length < 0) || (val_length < 0)) {
			return -1;
		}

		buf = (char *) malloc(((key_length + val_length + 2) *
				    sizeof(char)));
		assert(buf != NULL);

		p = buf;
		*p = '\0';
		if (key_length > 0) {
			memcpy(p, key_str, key_length);
			p += key_length;
			*p++ = '=';
		}
		if (val_length > 0) {
			memcpy(p, val_str, val_length);
		}
		p[val_length] = '\0';
		*end = packet;
	}
	*arg = buf;
	return 0;
}

static void
add_arg(proxy_msg *m, char *arg)
{
	int 	size = m->arg_size;

	if (m->arg_size < m->num_args + 2) {
		m->arg_size += ARG_SIZE;
	}

	if (size == 0) {
		m->arg_size++; // extra space to null terminate arguments
		m->args = (char **)malloc(sizeof(char *) * m->arg_size);
	} else if (size < m->arg_size) {
		m->args = (char **)realloc(m->args, sizeof(char *) * m->arg_size);
	}

	m->args[m->num_args] = arg;
	m->num_args++;

	/*
	 * Make sure that args are always null terminated
	 */
	m->args[m->num_args] = NULL;
}

void
proxy_msg_add_data(proxy_msg *m, char *data, int len)
{
	int		i;
	char	ch;
	char *	arg;
	char *	p;

	if (data == NULL) {
		proxy_msg_add_string(m, "00");
		return;
	}

	p = arg = (char *)malloc((len * 2) + 8 + 2);

	/*
	 * Encode data
	 */
	for (i = 0; i < len; i++) {
		ch = *data++;
		*p++ = tohex[(ch >> 4) & 0xf];
		*p++ = tohex[ch & 0xf];
	}

	*p = '\0';

	proxy_msg_add_string_nocopy(m, arg);
}

void
proxy_msg_add_int(proxy_msg *m, int val)
{
	char *	str_val;

	asprintf(&str_val, "%d", val);
	add_arg(m, str_val);
}

void
proxy_msg_add_string(proxy_msg *m, char *val)
{
	if (val == NULL) {
		val = "";
	}
	add_arg(m, strdup(val));
}

void
proxy_msg_add_string_nocopy(proxy_msg *m, char *val)
{
	if (val == NULL) {
		val = strdup("");
	}
	add_arg(m, val);
}

void
proxy_msg_add_args(proxy_msg *m, int nargs, char **args)
{
	int i;

	if (nargs == 0)
		return;

	for (i = 0; i < nargs; i++) {
		add_arg(m, strdup(args[i]));
	}
}

void
proxy_msg_add_keyval_int(proxy_msg *m, char *key, int val)
{
	char *	kv;

	asprintf(&kv, "%s=%d", key, val);
	add_arg(m, kv);
}

void
proxy_msg_add_keyval_string(proxy_msg *m, char *key, char *val)
{
	char *	kv;

	asprintf(&kv, "%s=%s", key, val);
	add_arg(m, kv);
}

void
proxy_msg_add_bitset(proxy_msg *m, bitset *b)
{
	add_arg(m, bitset_to_str(b));
}

void
proxy_msg_insert_bitset(proxy_msg *m, bitset *b, int idx)
{
	int 	i;
	char *	tmp_arg;

	if (idx < 0)
		idx = 0;

	/*
	 * First add bitset to end
	 */
	proxy_msg_add_bitset(m, b);

	/*
	 * Just return if the insert location is at or past end
	 */
	if (idx >= m->num_args) {
		return;
	}

	/*
	 * Otherwise rotate last argument into required position
	 */

	tmp_arg = m->args[m->num_args-1];

	for (i = m->num_args-1; i > idx; i--) {
		m->args[i] = m->args[i-1];
	}

	m->args[idx] = tmp_arg;
}

proxy_msg *
new_proxy_msg(int msg_id, int trans_id)
{
	proxy_msg *	m = (proxy_msg *)malloc(sizeof(proxy_msg));

	m->msg_id = msg_id;
	m->trans_id = trans_id;
	m->arg_size = 0;
	m->num_args = 0;
	m->args = NULL;

	return m;
}

void
free_proxy_msg(proxy_msg *m)
{
	int i;

	for (i = 0; i < m->num_args; i++) {
		free(m->args[i]);
	}

	free(m->args);
	free(m);
}

/*
 * Add message to list of messages waiting to be sent.
 */
int
proxy_queue_msg(List *ev_list, proxy_msg *m)
{
	AddToList(ev_list, (void *)m);
	return 0;
}

/*
 * Process any queued messages.
 */
void
proxy_process_msgs(List *msg_list, void (*callback)(proxy_msg *, void *), void *data)
{
	proxy_msg *	m;

	if (msg_list == NULL)
		return;

	if (!proxy_get_flow_control()) {
		while ((m = (proxy_msg *)RemoveFirst(msg_list)) != NULL) {
			callback(m, data);
			free_proxy_msg(m);
		}
	}
}

/*
 * Set flag indicating if proxy flow control is active
 */
void
proxy_set_flow_control(int flag)
{
	proxy_flow_control = flag;
}

/*
 * Return flag indicating if proxy flow control is active
 */
int proxy_get_flow_control()
{
	return proxy_flow_control;
}
