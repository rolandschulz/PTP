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

#include <string.h>

#include "backend.h"

extern dbg_backend_funcs	GDBMIBackend;
extern dbg_backend_funcs	TestBackend;

dbg_backend	dbg_backends[] = {
	{"gdb-mi", &GDBMIBackend, "gdb"},
	{"test", &TestBackend, NULL}
};

int
find_dbg_backend(char *name, dbg_backend **dp)
{
	int				i;
	dbg_backend *	d;
	
	for (i = 0; i < sizeof(dbg_backends) / sizeof(dbg_backend); i++) {
		d = &dbg_backends[i];
		if (strcmp(name, d->db_name) == 0) {
			*dp = d;
			return 0;
		}
	}

	return -1;
}

void
backend_set_path(dbg_backend *d, char *path)
{
	d->db_exe_path = path;
}
