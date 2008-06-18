/*
 * sdm.h
 *
 *  Created on: May 23, 2008
 *      Author: greg
 */

#ifndef SDM_H_
#define SDM_H_

#define SDM_MESSAGE_TYPE_NORMAL	0
#define SDM_MESSAGE_TYPE_URGENT	1

struct sdm_idset;
struct sdm_message;

typedef int						sdm_id;
typedef struct sdm_idset *		sdm_idset;
typedef struct sdm_message *	sdm_message;

extern sdm_id	SDM_MASTER;
/*
 * Startup/Initialization
 */
extern int sdm_init(int argc, char *argv[]);
extern int sdm_connect(const sdm_idset ids);
extern void sdm_finalize();

/*
 * Communication
 */
extern sdm_message sdm_message_new();
extern void sdm_message_free(sdm_message msg);
extern sdm_idset sdm_message_get_destination(const sdm_message msg);
extern sdm_idset sdm_message_get_source(const sdm_message msg);
extern int sdm_message_set_data(const sdm_message msg, char *buf, int len);
extern int sdm_message_get_data(const sdm_message msg, char **buf, int *len);
extern int sdm_message_get_type(const sdm_message msg);
extern void sdm_message_set_type(const sdm_message msg, int type);
extern int sdm_message_send(const sdm_message msg);
extern void sdm_message_set_send_callback(const sdm_message msg, void (*callback)(const sdm_message msg));
extern void sdm_message_set_recv_callback(void (*callback)(sdm_message msg));
extern int sdm_message_progress(void);

/*
 * Set operations
 */
extern sdm_idset sdm_set_new(void);
extern void sdm_set_free(sdm_idset set);
extern void sdm_set_clear(sdm_idset set);
extern int sdm_set_size(const sdm_idset set);
extern sdm_idset sdm_set_add_element(const sdm_idset set, const sdm_id id);
extern void sdm_set_remove_element(const sdm_idset set, const sdm_id id);
extern sdm_idset sdm_set_add_all(const sdm_idset set, const sdm_id id);
extern int sdm_set_is_subset(const sdm_idset set1, const sdm_idset set2);
extern int sdm_set_is_empty(const sdm_idset set);
extern int sdm_set_compare(const sdm_idset set1, const sdm_idset set2);
extern void sdm_set_union(const sdm_idset set1, const sdm_idset set2);
extern void sdm_set_intersect(const sdm_idset set1, const sdm_idset set2);
extern void sdm_set_diff(const sdm_idset set1, const sdm_idset set2);
extern int sdm_set_contains(const sdm_idset set, const sdm_id id);
extern sdm_id sdm_set_max(const sdm_idset set);
extern sdm_id sdm_set_first(const sdm_idset set);
extern sdm_id sdm_set_next(const sdm_idset set);
extern int sdm_set_done(const sdm_idset set);
extern char *sdm_set_serialize(const sdm_idset set);
extern void sdm_set_deserialize(sdm_idset set, char *str, char **end);
extern char *_set_to_str(const sdm_idset set);

/*
 * Routing
 */
extern int sdm_route_get_parent(void);
extern sdm_id sdm_route_get_id(void);
extern void sdm_route_set_id(sdm_id id);
extern int sdm_route_get_size(void);
extern void sdm_route_set_size(int s);
extern sdm_idset sdm_route_get_route(const sdm_idset dest);
extern sdm_idset sdm_route_reachable(const sdm_idset dest);

/*
 * Aggregation
 */
extern int sdm_aggregate_init(const sdm_message msg);
extern int sdm_aggregate(const sdm_message msg);

/*
 * Protocol operations
 */
extern int sdm_set_protocol_handler(int (*protocol_handler)(char *buf, int len));
extern int sdm_process_message(const sdm_message msg);

/*
 * I/O forwarding
 */
extern int sdm_set_stdin_handler(int fd_in, int fd_out, int (*stdin_handler)(int fd_in, int fd_out));
extern int sdm_set_stdout_handler(int fd_in, int fd_out, int (*stdout_handler)(int fd_in, int fd_out));
extern int sdm_set_stderr_handler(int fd_in, int fd_out, int (*stderr_handler)(int fd_in, int fd_out));

#endif /* SDM_H_ */
