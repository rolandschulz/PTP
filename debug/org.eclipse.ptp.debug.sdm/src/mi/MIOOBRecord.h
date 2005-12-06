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

#include "list.h"

#define MIOOBRecordTypeAsync	1
#define MIOOBRecordTypeStream	2

#define MIOOBRecordExecAsync		1
#define MIOOBRecordStatusAsync	2
#define MIOOBRecordNotifyAsync	3

#define MIOOBRecordConsoleStream	1
#define MIOOBRecordLogStream		2
#define MIOOBRecordTargetStream	3

/**
 * @see MIOOBRecord
 */
struct MIOOBRecord {
	int type;
	int sub_type;
	List *results;
	char *class;
	int token;
	char *cstring;
};
typedef struct MIOOBRecord MIOOBRecord;

extern MIOOBRecord *NewMIExecAsyncOutput(void);
extern MIOOBRecord *NewMIStatusAsyncOutput(void);
extern MIOOBRecord *NewMINotifyAsyncOutput(void);
extern MIOOBRecord *NewMIConsoleStreamOutput(void);
extern MIOOBRecord *NewMITargetStreamOutput(void);
extern MIOOBRecord *NewMILogStreamOutput(void);
				