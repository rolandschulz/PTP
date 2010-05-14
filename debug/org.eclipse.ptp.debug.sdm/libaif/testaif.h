/*
 * TestFlags are used so that each particular test can be tailored
 * to be tested only with some AIF functions.
 *
 * These are the list of functions that cannot be "turned off" by TestFlags
 * since they are very essential to test other functions.
 *
 * AIFPrint
 * CopyAIF
 * AIFArrayIndexInit
 * AIFArrayElement
 * AIFArrayIndexInc
 * AIFArrayIndexFree
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 */


#define SELF	0x01 	/* see the note below */

typedef struct
{
	/* Flags
	 * If it is 1, the test for the function will be executed */

	int 	AIFIsZero;
	int 	AIFCompare;
	int 	AIFTypeCompare;

	int 	AIFArrayRank;
	int 	AIFArrayInfo;
	int 	AIFArrayBounds;
	int 	AIFArrayIndexType;
	int 	AIFArrayMinIndex;
	int 	AIFArrayRankSize;
	int 	AIFArraySize;
	int 	AIFArraySlice;
	int 	AIFArrayPerm;
	int 	AIFArrayElementToInt;
	int 	AIFArrayElementToLongest;
	int 	AIFArrayElementToDouble;
	int 	AIFArrayElementToDoublest;
	int 	AIFArrayRef;

	int 	AIFNeg;
	int 	AIFNot;

	/* For some functions, we can pass a second AIF to be tested with.
	 * If it is NULL, the test for the function will not be executed
	 * If it is SELF, the first AIF is the same with the second AIF */

	AIF * 	AIFDiff;
	AIF * 	AIFAnd;
	AIF * 	AIFOr;

	AIF * 	AIFAdd;
	AIF * 	AIFSub;
	AIF * 	AIFMul;
	AIF * 	AIFDiv;
	AIF * 	AIFRem;

	AIF * 	AIFSetArrayData;

	/* For some functions, we can also specify the 'depth'
	 * of the traversal */

	int	D_AIFPrint;

} TestFlags;


