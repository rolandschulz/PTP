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

MIValue *
NewMIValue(void)
{
	MIValue *	val;
	
	val = (MIValue *)malloc(sizeof(MIValue));
	return val;	
}

MIValue *
NewMIConst(void)
{
	MIValue *	val = NewMIValue();
	val->type = MIValueTypeConst;
	val->cstring = NULL;
	return val;
}

MIValue *
NewMITuple(void)
{
	MIValue *	val = NewMIValue();
	val->type = MIValueTypeTuple;
	val->results = NULL;
	return val;
}

MIValue *
NewMIList(void)
{
	MIValue *	val = NewMIValue();
	val->type = MIValueTypeList;
	val->values = NULL;
	val->results = NULL;
	return val;
}

MIString *
MIConstToString(MIValue *v)
{
	return NewMIString(v->cstring);
}

MIString *
MIListToString(MIValue *v)
{
	int			first = 1;
	MIString *	str = NewMIString("[");
	MIResult *	r;
	MIValue *	val;

	for (SetList(v->results); (r = (MIResult *)GetListElement(v->results)) != NULL;) {
		if (!first)
			AppendMIString(str, NewMIString(","));
		first = 0;
		AppendMIString(str, MIResultToString(r));
	}
	first = 1;
	for (SetList(v->values); (val = (MIValue *)GetListElement(v->values)) != NULL;) {
		if (!first)
			AppendMIString(str, NewMIString(","));
		first = 0;
		AppendMIString(str, MIValueToString(val));
	}
	AppendMIString(str, NewMIString("]"));
	return str;
}

MIString *
MITupleToString(MIValue *v)
{
	int			first = 1;
	MIString *	str = NewMIString("{");
	MIResult *	r;
#ifdef __APPLE__
	MIValue *	val;
#endif /* __APPLE__ */

	for (SetList(v->results); (r = (MIResult *)GetListElement(v->results)) != NULL;) {
		if (!first)
			AppendMIString(str, NewMIString(","));
		first = 0;
		AppendMIString(str, MIResultToString(r));
	}
#ifdef __APPLE__
	first = 1;
	for (SetList(v->values); (val = (MIValue *)GetListElement(v->values)) != NULL;) {
		if (!first)
			AppendMIString(str, NewMIString(","));
		first = 0;
		AppendMIString(str, MIValueToString(val));
	}
#endif /* __APPLE__ */
	AppendMIString(str, NewMIString("}"));
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