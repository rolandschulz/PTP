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
#include <string.h>

#include "MIList.h"
#include "MIValue.h"
#include "MIResult.h"
#include "MIResultRecord.h"

MIResultRecord *
MIResultRecordNew(void)
{
	MIResultRecord *	rr;
	
	rr = (MIResultRecord *)malloc(sizeof(MIResultRecord));
	rr->results = NULL;
	rr->resultClass = MIResultRecordINVALID;
	rr->token = -1;
	return rr;
}

MIString *
MIResultRecordToString(MIResultRecord *rr)
{
	char *		class;
	MIString *	str;
	MIResult *	r;
	
	switch (rr->resultClass) {
	case MIResultRecordDONE:
		class = "done";
		break;
	case MIResultRecordRUNNING:
		class = "running";
		break;
	case MIResultRecordCONNECTED:
		class = "connected";
		break;
	case MIResultRecordERROR:
		class = "error";
		break;
	case MIResultRecordEXIT:
		class = "exit";
		break;
	default:
		class = "<invalid>";
		break;
	}
	
	if (rr->token != -1)
		str = MIStringNew("%d^%s", rr->token, class);
	else
		str = MIStringNew("^%s", class);
	
	if (rr->results != NULL) {
		for (MIListSet(rr->results); (r = (MIResult *)MIListGet(rr->results)) != NULL;) {
			MIStringAppend(str, MIStringNew(","));
			MIStringAppend(str, MIResultToString(r));
		}
	}
	
	return str;
}

void
MIResultRecordFree(MIResultRecord *rr)
{
	if (rr->results != NULL)
		MIListFree(rr->results, MIResultFree);
	free(rr);
}
