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
#ifndef _MIRESULTRECORD_H_
#define _MIRESULTRECORD_H_

#include "list.h"
#include "MIString.h"

#define MIResultRecordDONE		"done"
#define MIResultRecordRUNNING		"running"
#define MIResultRecordCONNECTED	"connected"
#define MIResultRecordERROR		"error"
#define MIResultRecordEXIT		"exit"

/**
 * GDB/MI ResultRecord.
 */
struct MIResultRecord {
	List *results;
	char *resultClass;
	int token;
};
typedef struct MIResultRecord MIResultRecord;

extern MIResultRecord *MIResultRecordNew(void);
extern MIString *MIResultRecordToString(MIResultRecord *rr);
extern void MIResultRecordFree(MIResultRecord *rr);
#endif _MIRESULTRECORD_H_
