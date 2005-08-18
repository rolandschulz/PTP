#ifndef LOCATION_H_
#define LOCATION_H_

struct location
{
	char *	loc_file;
	char *	loc_func;
	char *	loc_addr;
	int		loc_line;
};
typedef struct location location;

#endif /*LOCATION_H_*/
