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

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "MIList.h"
#include "MIValue.h"
#include "MIResult.h"
#include "MIArg.h"

MIArg *
MIArgNew(void)
{
	MIArg *	arg = (MIArg *)malloc(sizeof(MIArg));
	arg->name = NULL;
	arg->value = NULL;
	return arg;
}

void
MIArgFree(MIArg *arg)
{
	if (arg->name != NULL)
		free(arg->name);
	if (arg->value != NULL)
		free(arg->value);
	free(arg);
}

/**
 * Parsing a MIList of the form:
 * [{name="xxx",value="yyy"},{name="xxx",value="yyy"},..]
 * [name="xxx",name="xxx",..]
 * [{name="xxx"},{name="xxx"}]
 * 
 * Parsing a MITuple of the form:
 * {{name="xxx",value="yyy"},{name="xxx",value="yyy"},..}
 * {name="xxx",name="xxx",..}
 * {{name="xxx"},{name="xxx"}}
 */
MIList *
MIArgsParse(MIValue *miValue) 
{
	MIList *	aList = MIListNew();
	MIList *	values = miValue->values;
	MIList *	results = miValue->results;
	MIValue *	value;
	MIResult *	result;
	
	if (values != NULL) {
		for (MIListSet(values); (value = (MIValue *)MIListGet(values)) != NULL; ) {
			if (value->type == MIValueTypeTuple) {
				MIArg *arg = MIArgParse(value);
				if (arg != NULL) {
					MIListAdd(aList, (void *)arg);
				}
			}
		}
	}
	
	if (results != NULL) {
		for (MIListSet(results); (result = (MIResult *)MIListGet(results)) != NULL; ) {
			value = result->value;
			if (value->type == MIValueTypeConst) {
				MIArg *arg = MIArgNew();
				arg->name = strdup(value->cstring);
				MIListAdd(aList, (void *)arg);
			}
		}
	}
	
	return aList;
}

/**
 * Parsing a MITuple of the form:
 * {name="xxx",value="yyy"}
 * {name="xxx"}
 */
MIArg *
MIArgParse(MIValue *tuple)
{
	MIValue *	value;
	MIResult *	result;
	char *		str;
	MIArg *		arg = MIArgNew();
	
	for (MIListSet(tuple->results); (result = (MIResult *)MIListGet(tuple->results)) != NULL; ) {
		value = result->value;
		if (value != NULL && value->type == MIValueTypeConst) {
			str = value->cstring;
		} else {
			str = "";
		}

		if (strcmp(result->variable, "name") == 0) {
			arg->name = strdup(str);
		} else if (strcmp(result->variable, "value") == 0) {
			arg->value = strdup(str);
		}
	}
	
	return arg;
}

MIString *
MIArgToString(MIArg *arg)
{
	return MIStringNew("%s=%s", arg->name, arg->value);
}
