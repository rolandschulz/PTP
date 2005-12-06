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
#include <string.h>

#include "MIValue.h"
#include "MIResult.h"
#include "MIResultRecord.h"

MIResultRecord *
NewMIResultRecord(void)
{
	MIResultRecord *	rr;
	
	rr = (MIResultRecord *)malloc(sizeof(MIResultRecord));
	rr->results = NewList();
	rr->resultClass = NULL;
	rr->token = -1;
	return rr;
}

MIString *
MIResultRecordToString(MIResultRecord *rr)
{
	MIString *str;
	MIResult * r;
	
	if (rr->token != -1)
		str = NewMIString("%d^%s", rr->token, rr->resultClass);
	else
		str = NewMIString("^%s", rr->resultClass);
	
	for (SetList(rr->results); (r = (MIResult *)GetListElement(rr->results)) != NULL;) {
			AppendMIString(str, NewMIString(","));
			AppendMIString(str, MIResultToString(r));
	}
	
	return str;
}