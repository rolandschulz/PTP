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

#define MIResultRecordINVALID	0
#define MIResultRecordDONE		1
#define MIResultRecordRUNNING	2
#define MIResultRecordCONNECTED	3
#define MIResultRecordERROR		4
#define MIResultRecordEXIT		5

/**
 * GDB/MI ResultRecord.
 */
struct MIResultRecord {
	List *	results;
	int		resultClass;
	int		token;
};
typedef struct MIResultRecord MIResultRecord;

extern MIResultRecord *MIResultRecordNew(void);
extern MIString *MIResultRecordToString(MIResultRecord *rr);
extern void MIResultRecordFree(MIResultRecord *rr);
#endif /* _MIRESULTRECORD_H_ */
