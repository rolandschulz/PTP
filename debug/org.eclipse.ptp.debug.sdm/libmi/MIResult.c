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
 
 /*
  * Based on the QNX Java implementation of the MI interface
  */

#include <stdlib.h>

#include "MIValue.h"
#include "MIResult.h"

MIResult *
MIResultNew(void)
{
	MIResult *	res;
	
	res = (MIResult *)malloc(sizeof(MIResult));
	return res;
}

MIString *
MIResultToString(MIResult *r)
{
	MIString *	str = MIStringNew(r->variable);
	MIString *	str2;
	char *		p;
	
	if (r->value != NULL) {
		MIStringAppend(str, MIStringNew("="));
		str2 = MIValueToString(r->value);
		p = MIStringToCString(str2);
		if (*p == '[' || *p =='{') {
			MIStringAppend(str, str2);
		} else {
			MIStringAppend(str, MIStringNew("\"%s\"", p));
			MIStringFree(str2);
		}
	}
	return str;
}

void
MIResultFree(MIResult *r)
{
	if (r->variable != NULL)
		free(r->variable);
	if (r->value != NULL)
		MIValueFree(r->value);
	free(r);
}
