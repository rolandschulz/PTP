#ifndef ROUTETABLE_H_
#define ROUTETABLE_H_

struct routing_tbl_struct {
	char nodeID[255];
	char hostname[255]; // See RFC1034 for maximum size of a domain name
	int port;
};

int wait_for_routing_file(char *filename, FILE **routing_file, unsigned sec);
int close_routing_file(FILE *routing_file);
int read_routing_table_size(FILE *routing_file, int *table_size);
int read_routing_table_entry(FILE *routing_file, struct routing_tbl_struct *table);

#endif /*ROUTETABLE_H_*/