#include <lsf/lsbatch.h>

#include <stdlib.h>
#include <string.h>

struct hostInfoEnt gHostInfo;
struct queueInfoEnt gQueueInfo;
char* gAppName = NULL;

struct hostInfoEnt*
lsb_hostinfo(char ** hosts, int * numhosts)
{
	gHostInfo.host = "localhost";
	gHostInfo.hStatus = 3;
	return &gHostInfo;	
}


struct queueInfoEnt*
lsb_queueinfo(char** queues, int *numQueues, char *host, char *userName, int options)
{
	gQueueInfo.queue = "interactive";
	gQueueInfo.qStatus = 99;
	return &gQueueInfo;
}


int
lsb_init(char *appName)
{
	gAppName = malloc(strlen(appName) + 1);
	strcpy(gAppName, appName);
}


void
lsb_perror(char* error)
{
	fprintf(stderr, "%s\n", error);  fflush(stdout);	
}


int
lsb_deletejob(LS_LONG_INT a, int b, int c)
{
	return 0;
}


LS_LONG_INT
lsb_modify(struct submit * a, struct submitReply * b, LS_LONG_INT c)
{
	return 0;
}
