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

#include "MIList.h"
#include "MIValue.h"
#include "MIResult.h"

MIValue *
MIValueNew(void)
{
	MIValue *	val;
	
	val = (MIValue *)malloc(sizeof(MIValue));
	val->type = MIValueTypeInvalid;
	val->cstring = NULL;
	val->values = NULL;
	val->results = NULL;
	return val;	
}

MIValue *
NewMIConst(void)
{
	MIValue *	val = MIValueNew();
	val->type = MIValueTypeConst;
	return val;
}

MIValue *
NewMITuple(void)
{
	MIValue *	val = MIValueNew();
	val->type = MIValueTypeTuple;
	return val;
}

MIValue *
NewMIList(void)
{
	MIValue *	val = MIValueNew();
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

	for (MIListSet(v->results); (r = (MIResult *)MIListGet(v->results)) != NULL;) {
		if (!first) {
			MIStringAppend(str, MIStringNew(","));
		}
		first = 0;
		MIStringAppend(str, MIResultToString(r));
	}
	first = 1;
	for (MIListSet(v->values); (val = (MIValue *)MIListGet(v->values)) != NULL;) {
		if (!first) {
			MIStringAppend(str, MIStringNew(","));
		}
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

	for (MIListSet(v->results); (r = (MIResult *)MIListGet(v->results)) != NULL;) {
		if (!first) {
			MIStringAppend(str, MIStringNew(","));
		}
		first = 0;
		MIStringAppend(str, MIResultToString(r));
	}
#ifdef __APPLE__
	first = 1;
	for (MIListSet(v->values); (val = (MIValue *)MIListGet(v->values)) != NULL;) {
		if (!first) {
			MIStringAppend(str, MIStringNew(","));
		}
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
		MIListFree(v->results, MIResultFree);
	if (v->values != NULL)
		MIListFree(v->values, MIValueFree);
	free(v);
}
