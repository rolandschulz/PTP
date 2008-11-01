#include "config.h"

#include <sys/param.h>

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <dirent.h>
#include <string.h>

#include "sdm.h"

static char * MPIRankVars[] = {
	"OMPI_MCA_orte_ess_vpid", 		/* Open MPI 1.3 */
	"OMPI_MCA_ns_nds_vpid", 		/* Open MPI 1.2 */
	"PMI_RANK", 					/* MPICH2 */
	"MP_CHILD",						/* IBM PE */
	NULL
};

#define BUFFER_SIZE				255
#define ROUTING_TABLE_TIMEOUT	1000 /* number of tries */
#define ROUTING_TABLE_WAIT		1000*1000 /* usec */

static int wait_for_routing_file(char *filename, FILE **routing_file, int *route_size, unsigned sec);
static int read_routing_table_entry(FILE *routing_file, routing_table_entry *entry);
static int close_routing_file(FILE *routing_file);

static FILE *	routing_file = NULL;

/**
 * Initialize the routetable abstraction. The routetable will provide
 * the total number of processes and the ID to hostname mapping.
 *
 * @return 0 on success, -1 on failure
 */
int
sdm_routing_table_init(int argc, char *argv[])
{
	FILE *				rt_file;
	int					rv;
	int					tbl_size;
	int					master = 0;
	int					ch;
	char *				envval = NULL;
	char **				var;

	for (ch = 0; ch < argc; ch++) {
		char * arg = argv[ch];
		if (strncmp(arg, "--master", 8) == 0) {
			master = 1;
			break;
		}
	}

	/*
	 * Master and servers wait for the routing file to appear
	 */
	rv = wait_for_routing_file("routing_file", &rt_file, &tbl_size, ROUTING_TABLE_TIMEOUT); //TODO: Get filename from the environment
	if(rv == -1) { // No need to close, since wait_for_routing_file does it when error
		// Error!
		DEBUG_PRINTS(DEBUG_LEVEL_CLIENT, "Error opening the routing file\n");
		return -1;
	} else if(rv == -2){
		DEBUG_PRINTS(DEBUG_LEVEL_CLIENT, "Timeout while waiting for routing file\n");
		return -1;
	}
	close_routing_file(rt_file);

	sdm_route_set_size(tbl_size+1);
	SDM_MASTER = tbl_size;

	/*
	 * Since the SDM servers will be started by the mpirun, get
	 * the ID from the environment.
	 * Important! If the variable is not declared, then
	 * this sdm is the master.
	 */

	for (var = MPIRankVars; *var != NULL; var++) {
		envval = getenv(*var);
		if (envval != NULL) {
			break;
		}
	}

	if (!master && envval == NULL) {
		DEBUG_PRINTS(DEBUG_LEVEL_CLIENT, "Could not find my ID!\n");
		return -1;
	}

	if (envval != NULL) {
		int id = strtol(envval, NULL, 10);
		sdm_route_set_id(id);
	} else {
		sdm_route_set_id(SDM_MASTER);
	}

	DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] size %d\n", sdm_route_get_id(), sdm_route_get_size());

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

	rv = wait_for_routing_file("routing_file", &routing_file, &tbl_size, ROUTING_TABLE_TIMEOUT); //TODO: Get filename from the environment
	if (rv == -1) { // No need to close, since wait_for_routing_file does it when error
		// Error!
		DEBUG_PRINTS(DEBUG_LEVEL_CLIENT, "Error opening the routing file\n");
	} else if (rv == -2) {
		DEBUG_PRINTS(DEBUG_LEVEL_CLIENT, "Timeout while waiting for routing file\n");
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
			DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] Error reading routing table entry\n", sdm_route_get_id());
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

	DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] nodeID: %d, hostname: %s, port: %d\n", sdm_route_get_id(),
			entry->nodeID, entry->hostname, entry->port);

	if (rv == EOF)
		return -2;
	// Error reading file
	if(ferror(routing_file)) {
		return -1;
	}

	return 0;
}

/*
 * Read the routing table to the passed structure.
 */
/*int read_routing_table(FILE *routing_file, int num_entries, struct routing_tbl_struct *table)
{
	int i, rv;

	// Get the file size from the beginning of the file


	for(i=0; i < num_entries; i++) {
		rv = fscanf(routing_file, "%255s;%255s;%d\n",
				table[i].nodeID, table[i].hostname,
				&(table[i].port));

		// Error reading file
		if(ferror(routing_file))
			return -1;
	}

}*/

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

			DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] effsize: %d, size: %d, rv: %d\n", sdm_route_get_id(),
					eff_size, size, rv);

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
			}
		}

		usleep(ROUTING_TABLE_WAIT);
	}

	return -2;
}


/*int main(int argc, char *argv[])
{
	int rv, i;

	FILE * routing_file = fopen("routing_file", "r");

	struct routing_tbl_struct * route_tbl;
	int num_lines;

	// Allocs structure size based on the file size
	if((num_lines = line_count(routing_file)) <= 0)
		return -1;

	route_tbl = (struct routing_tbl_struct *)malloc(sizeof(struct routing_tbl_struct) * num_lines);

	// Read entries to the structure
	rv = read_routing_table(routing_file, num_lines, route_tbl);

	if(rv <= 0)
		return -1;

	//

}*/
