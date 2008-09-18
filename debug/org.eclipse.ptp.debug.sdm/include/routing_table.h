/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

#ifndef ROUTING_TABLE_H_
#define ROUTING_TABLE_H_

struct routing_table_entry {
	sdm_id 	nodeID;
	char 	hostname[255]; // See RFC1034 for maximum size of a domain name
	int		port;
};
typedef struct routing_table_entry	routing_table_entry;

extern int						sdm_routing_table_init(int argc, char *argv[]);
extern void						sdm_routing_table_set(void);
extern routing_table_entry *	sdm_routing_table_next(void);

#endif /* ROUTING_TABLE_H_ */
