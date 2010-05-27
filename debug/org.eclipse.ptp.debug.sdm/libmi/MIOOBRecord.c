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
#include "MIOOBRecord.h"

MIOOBRecord * 
NewMIMIOOBRecord(void)
{
	MIOOBRecord *	oob;
	
	oob = (MIOOBRecord *)malloc(sizeof(MIOOBRecord));
	oob->token = -1;
	oob->results = NULL;
	oob->class = NULL;
	oob->cstring = NULL;
	return oob;
}

MIOOBRecord * 
NewMIExecAsyncOutput(void)
{
	MIOOBRecord *	oob = NewMIMIOOBRecord();
	oob->type = MIOOBRecordTypeAsync;
	oob->sub_type = MIOOBRecordExecAsync;
	return oob;
}

MIOOBRecord *
NewMIStatusAsyncOutput(void)
{
	MIOOBRecord *	oob = NewMIMIOOBRecord();
	oob->type = MIOOBRecordTypeAsync;
	oob->sub_type = MIOOBRecordStatusAsync;
	return oob;
}

MIOOBRecord *
NewMINotifyAsyncOutput(void)
{
	MIOOBRecord *	oob = NewMIMIOOBRecord();
	oob->type = MIOOBRecordTypeAsync;
	oob->sub_type = MIOOBRecordNotifyAsync;
	return oob;
}

MIOOBRecord *
NewMIConsoleStreamOutput(void)
{
	MIOOBRecord *	oob = NewMIMIOOBRecord();
	oob->type = MIOOBRecordTypeStream;
	oob->sub_type = MIOOBRecordConsoleStream;
	return oob;
}

MIOOBRecord *
NewMITargetStreamOutput(void)
{
	MIOOBRecord *	oob = NewMIMIOOBRecord();
	oob->type = MIOOBRecordTypeStream;
	oob->sub_type = MIOOBRecordTargetStream;
	return oob;
}

MIOOBRecord *
NewMILogStreamOutput(void)
{
	MIOOBRecord *	oob = NewMIMIOOBRecord();
	oob->type = MIOOBRecordTypeStream;
	oob->sub_type = MIOOBRecordLogStream;
	return oob;
}

void 
MIOOBRecordFree(MIOOBRecord *oob)
{
	if (oob->results != NULL)
		MIListFree(oob->results, MIResultFree);
	if (oob->class != NULL)
		free(oob->class);
	if (oob->cstring != NULL)
		free(oob->cstring);
	free(oob);
}
