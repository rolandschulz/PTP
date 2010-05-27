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
  
#ifndef _MIVALUE_H_
#define _MIVALUE_H_

#include "MIList.h"
#include "MIString.h"

#define MIValueTypeInvalid	0
#define MIValueTypeConst		1
#define MIValueTypeList		2
#define MIValueTypeTuple		3

/**
 * GDB/MI value.
 */
struct MIValue {
	int type;
	char *cstring;
	MIList *values;
	MIList *results;
};
typedef struct MIValue MIValue;

extern MIValue *NewMIConst(void);
extern MIValue *NewMITuple(void);
extern MIValue *NewMIList(void);
extern MIString *MIConstToString(MIValue *);
extern MIString *MIListToString(MIValue *);
extern MIString *MITupleToString(MIValue *);
extern MIString *MIValueToString(MIValue *);
extern void MIValueFree(MIValue *v);
#endif /* _MIVALUE_H_ */
