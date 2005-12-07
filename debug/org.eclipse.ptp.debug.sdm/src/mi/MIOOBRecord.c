/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

#include <stdlib.h>

#include "list.h"
#include "MIValue.h"
#include "MIResult.h"
#include "MIOOBRecord.h"

MIOOBRecord * 
NewMIMIOOBRecord(void)
{
	MIOOBRecord *	oob;
	
	oob = (MIOOBRecord *)malloc(sizeof(MIOOBRecord));
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
		DestroyList(oob->results, MIResultFree);
	if (oob->class != NULL)
		free(oob->class);
	if (oob->cstring != NULL)
		free(oob->cstring);
	free(oob);
}
