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
