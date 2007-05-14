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

#include "proxy.h"
#include "proxy_msg.h"
#include "args.h"
#include "list.h"
#include "compat.h"

#ifdef __linux__
extern int digittoint(int c);
#endif /* __linux__ */

// FIXME this is to get around bug in check_arg_space()!
#define ARG_SIZE	1000

static char tohex[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

/*
 * Convert a message to a packet ready to send over the wire.
 * 
 * Note: packet length is added when the packet is transmitted.
 */
int
proxy_serialize_msg(proxy_msg *m, char **result)
{
	int		i;
	int		hdr_len;
	int		arg_len;
	int		len = 0;
	char *	packet;

	if (m == NULL)
		return -1;
		
	/*
	 * Compute packet length. 
	 * 
	 * Header length includes leading space and separators.
	 * Body length include encoded strings and separators.
	 */
	hdr_len = MSG_ID_SIZE + MSG_TRANS_ID_SIZE + MSG_NARGS_SIZE + 3;
	
	for (i = 0; i < m->num_args; i++) {
		len += strlen(m->args[i]) + MSG_ARG_LEN_SIZE + 2;
	}
	
	/*
	 * Allocate packet
	 */
	packet = (char *)malloc(hdr_len + len + 1);

	sprintf(packet, " %0*x:%0*x:%0*x", 
		MSG_ID_SIZE, m->msg_id & MSG_ID_MASK, 
		MSG_TRANS_ID_SIZE, m->trans_id & MSG_TRANS_ID_MASK, 
		MSG_NARGS_SIZE, m->num_args & MSG_NARGS_MASK);
	
	len = hdr_len;
	
	for (i = 0; i < m->num_args; i++) {
		arg_len = strlen(m->args[i]);
		sprintf(packet + len, " %0*x:%s", 
			MSG_ARG_LEN_SIZE, arg_len & MSG_ARG_LEN_MASK, m->args[i]);
		len += arg_len + MSG_ARG_LEN_SIZE + 2;
	}
	
	*result = packet;
		
	return 0;
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
proxy_get_bitset(char *str, bitset **b)
{
	*b = str_to_bitset(str);
}

/*
 * Convert wire protocol to message. 
 * 
 * Packet length has already been removed.
 */
int
proxy_deserialize_msg(char *packet, int packet_len, proxy_msg **msg)
{
	int			i;
	int			msg_id;
	int			trans_id;
	int			num_args;
	proxy_msg *	m = NULL;
	char *		arg;
	char *		end;
		
	if (packet == NULL || *packet != ' ' || packet_len < MSG_ID_SIZE + MSG_TRANS_ID_SIZE + MSG_NARGS_SIZE + 3) {
		return -1;
	}
	
	/*
	 * message ID
	 */
	packet++; /* Skip space */
	end = packet + MSG_ID_SIZE;
	*end++ = '\0';
	msg_id = strtol(packet, NULL, 16);

	/*
	 * transaction ID
	 */
	packet = end;
	end = packet + MSG_TRANS_ID_SIZE;
	*end++ = '\0';
	trans_id = strtol(packet, NULL, 16);
	 
	/*
	 * number of args
	 */
	packet = end;
	end = packet + MSG_NARGS_SIZE;
	*end++ = '\0';
	num_args = strtol(packet, NULL, 16);

	m = new_proxy_msg(msg_id, trans_id);
	
	if (num_args > 0) {
		m->args = (char **)malloc(sizeof(char *) * num_args+1);
		packet = end;
		
		for (i = 0; i < num_args; i++) {
			if (proxy_msg_decode_string(packet, &arg, &packet) < 0) {
				free_proxy_msg(m);
				return -1;
			}
			proxy_msg_add_string(m, arg);
			packet++; /* skip space */
		}
		
		/*
		 * NULL terminate the args
		 */
		m->args[num_args] = NULL;
	}
		
	*msg = m;
	
	return 0;
}

/*
 * Decode string argument. Returns pointer to the character after
 * the end of the string in 'end'
 */
int
proxy_msg_decode_string(char *str, char **arg, char **end)
{
	int		arg_len;
	int 	str_len = strlen(str);
	char *	ep;
	char *	p;
	
	if (str_len < MSG_ARG_LEN_SIZE + 1) {
		return -1;
	}
	
	ep = str + MSG_ARG_LEN_SIZE;
	*ep++ = '\0';
	arg_len = strtol(str, NULL, 16);
	
	if (str_len < MSG_ARG_LEN_SIZE + arg_len + 1) {
		return -1;
	}
	
	p = (char *)malloc(arg_len + 1);
	memcpy(p, ep, arg_len);
	p[arg_len] = '\0';
	
	*arg = p;
	*end = ep + arg_len;
	
	return 0;
}

static void
check_arg_space(proxy_msg *m, int n)
{
	int size = m->arg_size;

	while (m->arg_size < m->num_args + n) {
		m->arg_size += ARG_SIZE;
	}
	
	if (size == 0) {
		m->args = (char **)malloc(sizeof(char *) * m->arg_size);
		m->free_args = (int *)malloc(sizeof(int *) * m->arg_size);
	} else if (size < m->arg_size) {
		m->args = (char **)realloc(m->args, m->arg_size);
		m->free_args = (int *)realloc(m->free_args, m->arg_size);
	}
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
	
	proxy_msg_add_string(m, arg);
}

void
proxy_msg_add_int(proxy_msg *m, int val)
{
	check_arg_space(m, 1);
	asprintf(&m->args[m->num_args], "%d", val);
	m->free_args[m->num_args] = 0;
	m->num_args++;
}

void
proxy_msg_add_string(proxy_msg *m, char *val)
{
	check_arg_space(m, 1);
	if (val == NULL) {
		m->args[m->num_args] = strdup("");
	} else {
		m->args[m->num_args] = strdup(val);		
	}
	m->free_args[m->num_args] = 1;
	m->num_args++;
}

void
proxy_msg_add_string_nocopy(proxy_msg *m, char *val)
{
	check_arg_space(m, 1);
	if (val == NULL) {
		m->args[m->num_args] = "";
	} else {
		m->args[m->num_args] = val;
	}
	m->free_args[m->num_args] = 0;
	m->num_args++;
}

void
proxy_msg_add_args(proxy_msg *m, int nargs, char **args)
{
	int i;
	
	check_arg_space(m, nargs);
	
	for (i = 0; i < nargs; i++) {
		m->args[m->num_args + i] = strdup(args[i]);
		m->free_args[m->num_args + i] = 1;
	}
	
	m->num_args += nargs;
}

void
proxy_msg_add_args_nocopy(proxy_msg *m, int nargs, char **args)
{
	int i;

	if (nargs > 0) {
		check_arg_space(m, nargs);
		
		for (i = 0; i < nargs; i++) {
			m->args[m->num_args + i] = args[i];
			m->free_args[m->num_args + i] = 0;
		}
		
		m->num_args += nargs;
	}
}

void
proxy_msg_add_keyval_int(proxy_msg *m, char *key, int val)
{
	check_arg_space(m, 1);
	asprintf(&m->args[m->num_args], "%s=%d", key, val);
	m->free_args[m->num_args] = 1;
	m->num_args++;
}

void
proxy_msg_add_keyval_string(proxy_msg *m, char *key, char *val)
{
	check_arg_space(m, 1);
	asprintf(&m->args[m->num_args], "%s=%s", key, val);
	m->free_args[m->num_args] = 1;
	m->num_args++;
}

void
proxy_msg_add_bitset(proxy_msg *m, bitset *b)
{
	check_arg_space(m, 1);
	m->args[m->num_args] = bitset_to_str(b);
	m->free_args[m->num_args] = 1;
	m->num_args++;
}

void
proxy_msg_insert_bitset(proxy_msg *m, bitset *b, int idx)
{
	int i;

	if (idx < 0)
		return;
		
	if (idx >= m->num_args) {
		proxy_msg_add_bitset(m, b);
		return;
	}
	
	check_arg_space(m, 1);
	
	for (i = m->num_args; i > idx; i--) {
		m->args[i] = m->args[i-1];
		m->free_args[i] = m->free_args[i-1];
	}
	
	m->args[idx] = bitset_to_str(b);
	m->free_args[m->num_args] = 1;
	m->num_args++;
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
		if (m->free_args) {
			free(m->args[i]);
		}
	}
		
	free(m->args);
	free(m);
}

/*
 * Add message to list of messages waiting to be sent.
 */
void
proxy_queue_msg(List *ev_list, proxy_msg *m)
{
	AddToList(ev_list, (void *)m);
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
		
	while ((m = (proxy_msg *)RemoveFirst(msg_list)) != NULL) {
		callback(m, data);
		free_proxy_msg(m);
	}
}

