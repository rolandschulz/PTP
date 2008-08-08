#include "config.h"

#include <sys/param.h>

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <dirent.h>

#include "routetable.h"

#define BUFFER_SIZE 255

/*
 * Get the number of lines in a file.
 * File pointer is set to the beginning of the file
 *
 * @return number of lines
 *
 */
int line_count(FILE *file)
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
int
read_routing_table_entry(FILE *routing_file, struct routing_tbl_struct *table)
{
	int rv;
	rv = fscanf(routing_file, "%255s %255s %d\n",
			table->nodeID, table->hostname,
			&(table->port));

	printf("nodeID: %s, hostname: %s, port: %d\n", table->nodeID, table->hostname, table->port);

	// Error reading file
	if(ferror(routing_file))
		return -1;
	if(rv == EOF)
		return -2;


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
int
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
int
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
int
wait_for_routing_file(char *filename, FILE **routing_file, unsigned sec)
{
	char wd[MAXPATHLEN];

	getcwd(wd, MAXPATHLEN);

	sec += 1;

	// Wait for file to be created
	while (sec-- > 0) {
		/*
		 * List files to force an updated view of the working directory before
		 * opening the rounting_file. On NFS file systems, fopen never sees
		 * recently created files without the update.
		 */
		DIR * dir = opendir(wd);
		closedir(dir);

		*routing_file = fopen(filename, "r");

		if (*routing_file == NULL) {
			if (errno != ENOENT) {
				perror("fopen");
				return -1;
			}
			// File not created yet. Wait...
		} else {
			int eff_size, size;
			int rv;

			// Compare the filesize with the size on the header of the file
			eff_size = line_count(*routing_file); // Returns FILE pointer to 0
			rv = read_routing_table_size(*routing_file, &size); // Returns FILE pointer to
															// the after the header

			printf("effsize: %d, size: %d, rv: %d\n", eff_size, size, rv);
			switch (rv) {
			case -1:
				// error
				return -1;
				break;
			case -2:
				// Size not available yet. Close file and wait
				fclose(*routing_file);
				break;
			default:
				// We have file size. Now wait until effective file size equals
				// file size
				if ((eff_size - 1) == size)
					return 0;
				fclose(*routing_file);
			}
		}

		sleep(1);
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
