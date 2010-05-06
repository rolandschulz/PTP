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

#ifdef __gnu_linux__
#define _GNU_SOURCE
#endif /* __gnu_linux__ */

#include "config.h"

#ifdef DEBUG
#include <stdio.h>
#include <stdarg.h>

int		debug_level = DEBUG_LEVEL_NONE;

#define debug_out	stdout

void
debug_printf(int level, char *fmt, ...)
{
	va_list	ap;

	va_start(ap, fmt);

	if ((level & debug_level) == level) {
		fprintf(debug_out, "SDM: ");
		vfprintf(debug_out, fmt, ap);
		fflush(debug_out);
	}

	va_end(ap);
}

void
debug_printargs(int level, char *msg, int nargs, char **args)
{
	int	i;

	if ((level & debug_level) == level) {
		fprintf(debug_out, "SDM: %s(", msg);
		for (i = 0; i < nargs; i++) {
			if (i > 0) {
				fprintf(debug_out, ",");
			}
			fprintf(debug_out, "%s", args[i]);
		}
		fprintf(debug_out, ")\n");
		fflush(debug_out);
	}
}
#endif /* DEBUG */
