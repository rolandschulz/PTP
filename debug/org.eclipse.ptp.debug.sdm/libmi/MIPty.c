/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

#ifdef __gnu_linux__
#define _GNU_SOURCE
#endif /* __gnu_linux__ */

#include <stdio.h>
#include <errno.h>
#include <fcntl.h>
#include <grp.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>

int
get_master_pty(char **name)
{
	int i,j;
	int master;
	char *slavename;

#ifdef __APPLE__
	master = posix_openpt(O_RDWR);
#else /* __APPLE__ */
	master = open("/dev/ptmx", O_RDWR);
#endif /* __APPLE__ */
	
	if (master >= 0 
			&& grantpt(master) >= 0 
			&& unlockpt(master) >= 0 )
	{
		slavename = ptsname(master);
		if (slavename != NULL) {
			*name = strdup(slavename);
			return master;
		}
		close(master);
	}
	
	return -1;
}

int
get_slave_pty(char *name)
{
	struct group *gptr;
	gid_t gid;
	int slave = -1;
	if (strcmp(name, "dev/pts/")) {
		if ((gptr = getgrnam("tty")) != 0 ) {
			gid = gptr -> gr_gid;			
		}
		else {
			gid = -1;
		}
	
		chown(name, getuid(), gid);
		chmod(name, S_IRUSR | S_IWUSR);
	}
	
	/* open the corresponding slave pty */
	slave = open(name, O_RDWR);	
	return (slave);
}
