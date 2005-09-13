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

#include <stdlib.h>
#include <string.h>

#include "procset.h"

procset *
procset_new(int size)
{
	procset *	p;
	
	p = malloc(sizeof(procset));
	
	p->ps_nprocs = size;
	p->ps_procs = BITVECTOR_CREATE(size);
	
	return p;
}

void		
procset_free(procset *p)
{
	BITVECTOR_FREE(p->ps_procs);
	free(p);
}
	
void		
procset_add_proc(procset *p, int proc)
{
	BITVECTOR_SET(p->ps_procs, proc);
}

void		
procset_remove_proc(procset *p, int proc)
{
	BITVECTOR_UNSET(p->ps_procs, proc);
}

int		
procset_test(procset *p, int proc)
{
	return BITVECTOR_GET(p->ps_procs, proc);
}

static char tohex[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

/**
 * Return a string representation of a procset. We use hex to compress
 * the string somewhat.
 */
char *
procset_to_str(procset *p)
{
	int				bytes;
	int				pbit;
	int				bit;
	char *			str;
	char *			s;
	unsigned char	byte;
	
	if (p == NULL)
		return strdup("<null>");
		
	/*
	 * Round nprocs up to nearest byte
	 */
	bytes = (p->ps_nprocs >> 3) + 1;
	
	str = s = (char *)malloc((bytes * 2) + 1);
	
	for (pbit = bytes << 3; pbit >= 7; ) {
		for (byte = 0, bit = 7; bit >= 0; bit--, pbit--) {
			if (pbit <= p->ps_nprocs && BITVECTOR_GET(p->ps_procs, pbit))
				byte |= (1 << bit);			
		}
		
		*s++ = tohex[(byte >> 4) & 0x0f];
		*s++ = tohex[byte & 0x0f];
	}
	
	*s = '\0';
	
	return str;
}