#include "config.h"

#include <sys/param.h>

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <dirent.h>
#include <string.h>
#include <time.h>

#include "sdm.h"

/*
 * Environment variables used to supply rank information
 */
static char * RankVars[] = {
	"OMPI_MCA_orte_ess_vpid", 		/* Open MPI 1.3+ */
	"OMPI_MCA_ns_nds_vpid", 		/* Open MPI 1.2 */
	"PMI_RANK", 					/* MPICH2 */
	"MP_CHILD",						/* IBM PE */
	"PCMP_PTP_RANK", 				/* Platform-MPI */
	"SLURM_PROCID",					/* SLURM */
	"X10_PLACE",					/* X10 */
	NULL
};

/*
 * Environment variables used to supply number of ranks.
 * This is not currently used as the routing table size
 * determines the number of ranks.
 */
static char * NumRankVars[] = {
	"X10_NPLACES",					/* X10 */
	NULL
};

#define DEFAULT_ROUTING_FILE	"routing_file"
#define BUFFER_SIZE				255
#define ROUTING_TABLE_TIMEOUT	1000 /* number of tries */
#define ROUTING_TABLE_WAIT		1000*1000 /* usec */
#define PORT_BASE				50000
#define PORT_RANGE				10000

static int wait_for_routing_file(char *filename, FILE **routing_file, int *route_size, unsigned sec);
static int read_routing_table_entry(FILE *routing_file, routing_table_entry *entry);
static int close_routing_file(FILE *routing_file);
static int generate_routing_file(char *filename, char *routes);
static int generate_port(void);

static FILE *	routing_file = NULL;
static char *	routing_file_path = DEFAULT_ROUTING_FILE;
static int		master;

/**
 * Initialize the routetable abstraction. The routetable will provide
 * the total number of processes and the ID to hostname mapping.
 *
 * This operation blocks until the routing file is available, or the
 * timeout expires (currently 1000 seconds).
 *
 * @return 0 on success, -1 on failure
 */
int
sdm_routing_table_init(int argc, char *argv[])
{
	FILE *				rt_file;
	int					rv;
	int					tbl_size;
	int					server_id;
	int					ch;
	char *				envval = NULL;
	char **				var;

	/*
	 * If sdm servers are started by the mpirun, then their ID (rank) will be
	 * available from the environment. Important! If the variable is not found, then
	 * this sdm is assumed to be the master.
	 *
	 * The master sdm usually has the option "--master" (apart from the case above).
	 *
	 * Server sdm's can also have their ID's set using the "--server=id" option. This
	 * allows the servers to be started by a non-MPI runtime.
	 */

	for (ch = 0; ch < argc; ch++) {
		char * arg = argv[ch];
		if (strncmp(arg, "--master", 8) == 0) {
			master = 1;
		} else if (strncmp(arg, "--server=", 9) == 0) {
			master = 0;
			server_id = (int)strtol(arg+9, NULL, 10);
		} else if (strncmp(arg, "--routing_file=", 15) == 0) {
			routing_file_path = &arg[15];
		}
	}

	if (!master) {
		/*
		 * If no server IDs were set, check the environment
		 */
		for (var = RankVars; *var != NULL; var++) {
			envval = getenv(*var);
			if (envval != NULL) {
				server_id = (int)strtol(envval, NULL, 10);
				break;
			}
		}
	} else {
		/*
		 * If master, see if we need to generate routing file
		 */
		for (ch = 0; ch < argc; ch++) {
			char * arg = argv[ch];
			if (strncmp(arg, "--generate_routes=", 18) == 0) {
				rv = generate_routing_file(routing_file_path, &arg[18]);
				if (rv == -1) {
					DEBUG_PRINTF(DEBUG_LEVEL_ROUTING, "[%s] Error creating routing file\n", master ? "master" : "server");
					return -1;
				}
			}
		}
	}

	/*
	 * Master and servers wait for the routing file to appear
	 */
	rv = wait_for_routing_file(routing_file_path, &rt_file, &tbl_size, ROUTING_TABLE_TIMEOUT); //TODO: Get filename from the environment
	if (rv == -1) { // No need to close, since wait_for_routing_file does it when error
		// Error!
		DEBUG_PRINTF(DEBUG_LEVEL_ROUTING, "[%s] Error opening the routing file\n", master ? "master" : "server");
		return -1;
	} else if (rv == -2){
		DEBUG_PRINTF(DEBUG_LEVEL_ROUTING, "[%s] Timeout while waiting for routing file\n", master ? "master" : "server");
		return -1;
	}
	close_routing_file(rt_file);

	DEBUG_PRINTF(DEBUG_LEVEL_ROUTING, "[%s] Found routing file, size=%d\n", master ? "master" : "server", tbl_size);
	if (tbl_size == 0) {
		DEBUG_PRINTF(DEBUG_LEVEL_ROUTING, "[%s] Invalid routing file size\n", master ? "master" : "server");
		return -1;
	}

	sdm_route_set_size(tbl_size+1);
	SDM_MASTER = tbl_size;

	if (master) {
		sdm_route_set_id(SDM_MASTER);
	} else {
		sdm_route_set_id(server_id);
	}

	DEBUG_PRINTF(DEBUG_LEVEL_ROUTING, "[%d] size %d\n", sdm_route_get_id(), sdm_route_get_size());

	/*
	 * Once we have size and ID we can initialize child/parent relationships. This may need
	 * to happen at a different time if we are receiving the routing table from a parent.
	 */
	if (sdm_route_init(argc, argv) < 0) {
		return -1;
	}

	return 0;
}

void
sdm_routing_table_set(void)
{
	int	rv;
	int	tbl_size;

	if (routing_file != NULL) {
		close_routing_file(routing_file);
		routing_file = NULL;
	}

	rv = wait_for_routing_file(routing_file_path, &routing_file, &tbl_size, ROUTING_TABLE_TIMEOUT); //TODO: Get filename from the environment
	if (rv == -1) { // No need to close, since wait_for_routing_file does it when error
		// Error!
		DEBUG_PRINTS(DEBUG_LEVEL_ROUTING, "Error opening the routing file\n");
	} else if (rv == -2) {
		DEBUG_PRINTS(DEBUG_LEVEL_ROUTING, "Timeout while waiting for routing file\n");
	}
}

routing_table_entry *
sdm_routing_table_next(void)
{
	int	rv;
	static routing_table_entry	entry;

	if (routing_file == NULL) {
		return NULL;
	}

	rv = read_routing_table_entry(routing_file, &entry);

	if (rv < 0) {
		if (rv == -1) {
			DEBUG_PRINTF(DEBUG_LEVEL_ROUTING, "[%d] Error reading routing table entry\n", sdm_route_get_id());
		}
		close_routing_file(routing_file);
		routing_file = NULL;
		return NULL;
	}

	return &entry;
}

/*
 * Get the number of lines in a file.
 * File pointer is set to the beginning of the file
 *
 * @return number of lines
 *
 */
static int
line_count(FILE *file)
{
	int num_lines = 0;

	int ret_val;
	char ret_char;

	fseek(file, 0, SEEK_SET);

	while(1) {
		ret_val = fgetc(file);

		if(ret_val == EOF) {
			if(ferror(file))
				return -1;
			break;
		}

		ret_char = (char)ret_val;

		if(ret_char == '\n')
			num_lines++;
	}
	fseek(file, 0, SEEK_SET);

	return num_lines;
}

/**
 * Read an entry of an opened routing_file on the table pointer.
 * Assumes file pointer must be at the beginning of a line after the header
 *
 * @return 0 if successful, -2 if EOF, -1 if error
 */
static int
read_routing_table_entry(FILE *routing_file, routing_table_entry *entry)
{
	int rv;
	rv = fscanf(routing_file, "%d %255s %d\n",
			&(entry->nodeID), entry->hostname,
			&(entry->port));

	DEBUG_PRINTF(DEBUG_LEVEL_ROUTING, "[%d] nodeID: %d, hostname: %s, port: %d\n", sdm_route_get_id(),
			entry->nodeID, entry->hostname, entry->port);

	if (rv == EOF)
		return -2;
	// Error reading file
	if(ferror(routing_file)) {
		return -1;
	}

	return 0;
}

/**
 * Get the number of lines from the header of the file
 * It will set the file pointer to just after the header of the file
 *
 * @return -2 if file at EOF, -1 if error, 0 if successful
 */
static int
read_routing_table_size(FILE *routing_file, int *size)
{
	int rv;

	fseek(routing_file, 0, SEEK_SET);
	rv = fscanf(routing_file, "%d\n", size);

	if(ferror(routing_file) != 0)
		return -1;
	if(rv != 1)
		return -2; // Just EOF

	return 0;
}

/**
 * Close a routing table pointed by th routing_file parameter
 *
 * @return 0 on success, -1 on failure
 */
static int
close_routing_file(FILE *routing_file)
{
	return fclose(routing_file);
}

/**
 * Wait for sec seconds for a routing file filename, returning its file pointer in
 * the routing_file parameter.
 *
 * @return 0 if successful, -1 if error, -2 if file not ready in sec seconds
 */
static int
wait_for_routing_file(char *filename, FILE **routing_file, int *route_size, unsigned sec)
{
	FILE *	fp;
	char	wd[MAXPATHLEN];

	getcwd(wd, MAXPATHLEN);

	// Wait for file to be created
	while (sec-- > 0) {
		/*
		 * List files to force an updated view of the working directory before
		 * opening the rounting_file. On NFS file systems, fopen never sees
		 * recently created files without the update.
		 */
		DIR * dir = opendir(wd);
		closedir(dir);

		fp = fopen(filename, "r");

		if (fp == NULL) {
			if (errno != ENOENT) {
				perror("fopen");
				return -1;
			}
			// File not created yet. Wait...
		} else {
			int eff_size, size;
			int rv;

			// Compare the filesize with the size on the header of the file
			eff_size = line_count(fp); // Returns FILE pointer to 0
			rv = read_routing_table_size(fp, &size); // Returns FILE pointer to
															// the after the header

			DEBUG_PRINTF(DEBUG_LEVEL_ROUTING, "[%s] effsize: %d, size: %d, rv: %d\n", master ? "master" : "server", eff_size, size, rv);

			switch (rv) {
			case -1:
				// error
				return -1;
				break;
			case -2:
				// Size not available yet. Close file and wait
				fclose(fp);
				break;
			default:
				// We have file size. Now wait until effective file size equals
				// file size
				if ((eff_size - 1) == size) {
					*routing_file = fp;
					*route_size = size;
					return 0;
				}

				fclose(fp);
				break;
			}
		}

		usleep(ROUTING_TABLE_WAIT);
	}

	return -2;
}

static int
generate_routing_file(char *filename, char *routes)
{
	int		cnt;
	char *	s;
	char *	t;
	char *	route;
	FILE *	fp;

	srandom(time(NULL));

	for (s = routes, cnt = 1; (t = strchr(s, ',')) != NULL; s = t+1, cnt++);

	fp = fopen(filename, "w");
	if (fp == NULL) {
		perror("fopen");
		return -1;
	}

	fprintf(fp, "%d\n", cnt);
	for (cnt = 0; (route = strsep(&routes, ",")) != NULL; cnt++) {
		fprintf(fp, "%d %s %d\n", cnt, route, generate_port());
	}
	fclose(fp);
	return 0;
}

static int
generate_port(void)
{
	return PORT_BASE + random() / (RAND_MAX / PORT_RANGE + 1);
}
