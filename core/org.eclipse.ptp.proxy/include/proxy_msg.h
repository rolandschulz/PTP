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

#ifndef _PROXY_MSG_H_
#define _PROXY_MSG_H_

#include "list.h"
#include "bitset.h"

#define PTP_MSG_ID_SIZE			4
#define PTP_MSG_ID_MASK			0xffff
#define PTP_MSG_TRANS_ID_SIZE	8
#define PTP_MSG_TRANS_ID_MASK	0xffffffff
#define PTP_MSG_NARGS_SIZE		8
#define PTP_MSG_NARGS_MASK		0xffffffff
#define PTP_MSG_ARG_LEN_SIZE	8
#define PTP_MSG_ARG_LEN_MASK	0xffffffff

/*
 * Proxy errors
 */
#define PTP_ERROR_MALFORMED_COMMAND	0

struct proxy_msg {
	int		msg_id;
	int		trans_id;
	int		num_args;
	int 	arg_size;
	char **	args;
};
typedef struct proxy_msg proxy_msg;

extern int 			proxy_list_to_str(List *, int (*)(void *, char **), char **);
extern void			proxy_get_data(char *, char **, int *);
extern void			proxy_get_int(char *, int *);
extern void			proxy_get_bitset(char *, bitset **);
extern int 			proxy_deserialize_msg(char *, int len, proxy_msg **);
extern int 			proxy_serialize_msg(proxy_msg *, char **, int *);
extern int			proxy_msg_decode_string(char *, int, char **, char **);
extern void			proxy_msg_add_int(proxy_msg *, int);
extern void			proxy_msg_add_string(proxy_msg *, char *);
extern void			proxy_msg_add_string_nocopy(proxy_msg *, char *);
extern void			proxy_msg_add_data(proxy_msg *, char *, int);
extern void			proxy_msg_add_args(proxy_msg *, int, char **);
extern void			proxy_msg_add_keyval_int(proxy_msg *, char *, int);
extern void			proxy_msg_add_keyval_string(proxy_msg *, char *, char *);
extern void			proxy_msg_add_bitset(proxy_msg *, bitset *);
extern void			proxy_msg_insert_bitset(proxy_msg *, bitset *, int);
extern void			proxy_msg_add_list(proxy_msg *, List *, void (*)(proxy_msg *, void *));
extern proxy_msg *	new_proxy_msg(int, int);
extern void			free_proxy_msg(proxy_msg *);
extern int			proxy_queue_msg(List *, proxy_msg *);
extern void 		proxy_process_msgs(List *, void (*)(proxy_msg*, void *), void *);
extern void             proxy_set_flow_control(int flag);
extern int              proxy_get_flow_control();
#endif /* !_PROXY_MSG_H_ */
