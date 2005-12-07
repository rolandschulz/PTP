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

MIValue *
NewMIValue(void)
{
	MIValue *	val;
	
	val = (MIValue *)malloc(sizeof(MIValue));
	val->cstring = NULL;
	val->values = NULL;
	val->results = NULL;
	return val;	
}

MIValue *
NewMIConst(void)
{
	MIValue *	val = NewMIValue();
	val->type = MIValueTypeConst;
	return val;
}

MIValue *
NewMITuple(void)
{
	MIValue *	val = NewMIValue();
	val->type = MIValueTypeTuple;
	return val;
}

MIValue *
NewMIList(void)
{
	MIValue *	val = NewMIValue();
	val->type = MIValueTypeList;
	return val;
}

MIString *
MIConstToString(MIValue *v)
{
	return MIStringNew(v->cstring);
}

MIString *
MIListToString(MIValue *v)
{
	int			first = 1;
	MIString *	str = MIStringNew("[");
	MIResult *	r;
	MIValue *	val;

	for (SetList(v->results); (r = (MIResult *)GetListElement(v->results)) != NULL;) {
		if (!first)
			MIStringAppend(str, MIStringNew(","));
		first = 0;
		MIStringAppend(str, MIResultToString(r));
	}
	first = 1;
	for (SetList(v->values); (val = (MIValue *)GetListElement(v->values)) != NULL;) {
		if (!first)
			MIStringAppend(str, MIStringNew(","));
		first = 0;
		MIStringAppend(str, MIValueToString(val));
	}
	MIStringAppend(str, MIStringNew("]"));
	return str;
}

MIString *
MITupleToString(MIValue *v)
{
	int			first = 1;
	MIString *	str = MIStringNew("{");
	MIResult *	r;
#ifdef __APPLE__
	MIValue *	val;
#endif /* __APPLE__ */

	for (SetList(v->results); (r = (MIResult *)GetListElement(v->results)) != NULL;) {
		if (!first)
			MIStringAppend(str, MIStringNew(","));
		first = 0;
		MIStringAppend(str, MIResultToString(r));
	}
#ifdef __APPLE__
	first = 1;
	for (SetList(v->values); (val = (MIValue *)GetListElement(v->values)) != NULL;) {
		if (!first)
			MIStringAppend(str, MIStringNew(","));
		first = 0;
		MIStringAppend(str, MIValueToString(val));
	}
#endif /* __APPLE__ */
	MIStringAppend(str, MIStringNew("}"));
	return str;
}

MIString *
MIValueToString(MIValue *v)
{
	MIString *	str = NULL;
	
	switch (v->type) {
	case MIValueTypeConst:
		str = MIConstToString(v);
		break;
		
	case MIValueTypeList:
		str = MIListToString(v);
		break;
		
	case MIValueTypeTuple:
		str = MITupleToString(v);
		break;
	}
	
	return str;
}

void
MIValueFree(MIValue *v)
{
	if (v->cstring != NULL)
		free(v->cstring);
	if (v->results != NULL)
		DestroyList(v->results, MIResultFree);
	if (v->values != NULL)
		DestroyList(v->values, MIValueFree);
	free(v);
}
