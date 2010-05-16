/*
 * Test harness for the aif library.
 *
 * Originally written by Raphael Finkel 8/2000 raphael@cs.uky.edu
 *
 * Copyright (c) 1996-2002 by Guardsoft Pty Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */

#ifdef HAVE_CONFIG_H
#include "config.h"
#endif /* HAVE_CONFIG_H */

#include <stdio.h>
#include <stdlib.h>
#include <string.h> /* memcpy */
#include <unistd.h> /* getopt */
#ifdef __alpha
#include <string.h> /* memcpy */
#endif
#include "aif.h"
#include "aifint.h"
#include "testaif.h"

#define TEST_SERIES_1 0x01
#define TEST_SERIES_2 0x02
#define TEST_SERIES_3 0x04
#define TEST_SERIES_4 0x08
#define TEST_SERIES_5 0x10
#define TEST_SERIES_6 0x20



TestFlags all = {
	1,1,1,
	1,1,1,1,1,1,1,1,1,1,1,1,1,1,
	1,1,
	(AIF*)SELF, (AIF*)NULL, (AIF*)NULL,
	(AIF*)NULL, (AIF*)NULL, (AIF*)NULL, (AIF*)NULL, (AIF*)NULL,
	NULL,
	0};

TestFlags none = {
	0,0,0,
	0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	0,0,
	NULL,NULL,
	NULL,NULL,NULL,NULL,NULL,
	NULL,
	0};




/*****************************************
 ***        Function Prototypes        ***
 *****************************************/

void 	TestAllConstructors();
void 	TestAllAscii();
void 	TestAllArithmetic();
void 	TestAllCircularData();
void 	TestAllEPS();
void 	TestAllCompareByName();
void 	_testAscii(char *, char *, char *, char *, TestFlags);
void 	_testArithmetic(char *, AIF *, char *, TestFlags, AIF *);
void 	_testCircularData(char *, AIF *, AIF *, TestFlags);
void 	_testEPS(AIF *, AIF *, AIF *);
void 	_testCompareByName(int, AIF *, AIF *, char *);

int 	AIFTest(char *, AIF *, char *, TestFlags);
int 	AIFTestInt(char *, AIF *, char *, TestFlags);
int 	_aif_test(AIF *, TestFlags);
int 	_aif_test_general(AIF *, TestFlags);
int 	_aif_test_bool(AIF *, TestFlags);
int 	_aif_test_char(AIF *, TestFlags);
int 	_aif_test_int(AIF *, TestFlags);
int 	_aif_test_float(AIF *, TestFlags);
int 	_aif_test_array(AIF *, TestFlags);
int 	_aif_test_pointer(AIF *, TestFlags);
int 	_aif_test_region(AIF *, TestFlags);
int 	_aif_test_aggregate(AIF *, TestFlags);
int 	_aif_test_name(AIF *, TestFlags);
int 	_aif_test_reference(AIF *, TestFlags);
int 	_aif_test_string(AIF *, TestFlags);
int 	_aif_test_enum(AIF *, TestFlags);
int 	_aif_test_union(AIF *, TestFlags);



/*****************************************
 ***           Main Function           ***
 *****************************************/

int 
main(int argc, char *argv[])
{
	int	c;
	int	tests = 0;

	while ( (c = getopt(argc, argv, "123456")) != EOF )
	{
		switch ( c )
		{
		case '1':
			tests |= TEST_SERIES_1;
			break;

		case '2':
			tests |= TEST_SERIES_2;
			break;

		case '3':
			tests |= TEST_SERIES_3;
			break;
		
		case '4':
			tests |= TEST_SERIES_4;
			break;
		
		case '5':
			tests |= TEST_SERIES_5;
			break;
		
		case '6':
			tests |= TEST_SERIES_6;
			break;
		
		default:
			fprintf(stderr, "usage: testaif [-1] [-2] [-3] [-4] [-5] [-6]\n");
			exit(0);
		}
	}

	if ( tests == 0 || tests & TEST_SERIES_1 )
		TestAllConstructors();

	if ( tests & TEST_SERIES_2 )
		TestAllAscii();

	if ( tests == 0 || tests & TEST_SERIES_3 )
		TestAllArithmetic();

	if ( tests & TEST_SERIES_4 )
		TestAllCircularData();

	if ( tests == 0 || tests & TEST_SERIES_5 )
		TestAllEPS();

	if ( tests & TEST_SERIES_6 )
		TestAllCompareByName();

	fprintf(stdout, "\n****** END OF TEST ******\n");

	return 0;

}



/*****************************************
 ***       Function Definitions        ***
 *****************************************/

void 
TestAllConstructors()
{
	AIF *aString, *aFloat, *aDouble, *anInt, *aPointer, *aAggregate;
	AIF *anArray, *anEnum, *aUnion;
	TestFlags flag = all;
	int min, size;


	fprintf(stdout, "*********************************** \n");
	fprintf(stdout, "*** TEST SERIES 1: constructors *** \n");
	fprintf(stdout, "*********************************** \n\n");


	aString = StringToAIF("hello, world!");
	AIFTest("subtest1", aString, "string: hello, world!", flag);

	aFloat = FloatToAIF(2);
	AIFTest("subtest2", aFloat, "float: 2", flag);

	aDouble = DoubleToAIF(0);
	AIFTest("subtest3", aDouble, "double: 0", flag);

	anInt = IntToAIF(16);
	AIFTest("subtest4", anInt, "integer: 16", flag);

	aPointer = PointerToAIF(AddressToAIF("12345678", 4), anInt);
	AIFTest("subtest5", aPointer, "a pointer", flag);

	aPointer = PointerToAIF(AddressToAIF("ffffffff", 4), aPointer);
	AIFTest("subtest6", aPointer, "a double pointer", flag);

	aAggregate = EmptyAggregateToAIF("test_aggregate");
	AIFTest("subtest7", aAggregate, "empty aggregate", flag);

	AIFAddFieldToAggregate(aAggregate, AIF_ACCESS_PUBLIC, "field1", aFloat);
	AIFTest("subtest8", aAggregate, "with one field", flag);

	AIFAddFieldToAggregate(aAggregate, AIF_ACCESS_PUBLIC, "field2", IntToAIF(16));
	AIFTest("subtest9", aAggregate, "with two fields", flag);

	AIFAddFieldToAggregate(aAggregate, AIF_ACCESS_PUBLIC, "field3", aAggregate);
	AIFTest("subtest10", aAggregate, "with three fields", flag);

	min = 0; size = 10;
	anArray = ArrayToAIF(1, &min, &size, "abcdefghij", 10, "c");
	AIFTest("subtest11", anArray, "array of 10 chars", flag);

	AIFAddFieldToAggregate(aAggregate, AIF_ACCESS_PUBLIC, "field4", anArray);
	AIFTest("subtest12", aAggregate, "after the array is field 4", flag);

	/* test a linked list: 1 -> 2 -> nil */
	aAggregate = EmptyAggregateToAIF("test_aggregate");
	aAggregate = NameAIF(aAggregate, 0);
	AIFAddFieldToAggregate(aAggregate, AIF_ACCESS_PUBLIC, "value", IntToAIF(2));
	AIFAddFieldToAggregate(aAggregate, AIF_ACCESS_PUBLIC, "next", AIFNullPointer(aAggregate));
	aPointer = AIFNullPointer(aAggregate);
	aAggregate = EmptyAggregateToAIF("test_aggregate");
	AIFAddFieldToAggregate(aAggregate, AIF_ACCESS_PUBLIC, "value", IntToAIF(1));
	AIFAddFieldToAggregate(aAggregate, AIF_ACCESS_PUBLIC, "next", aPointer);
	aPointer = AIFNullPointer(aAggregate);
	AIFTest("subtest13", aPointer, "linked list 1 -> 2 ", flag);

	/* test an array of aggregates. */
	{
		char data[BUFSIZ], *dest;
		int min=1, size=9, subMin = 1, subSize = 2;
		int index;
		int smallArray[2] = {1,2};

		aAggregate = EmptyAggregateToAIF("test_aggregate");
		AIFAddFieldToAggregate(aAggregate, AIF_ACCESS_PUBLIC, "a", FloatToAIF(2.1));
		dest = data;
		for (index = 0; index < 2; index++) 
		{
			AIFNormalise(dest, sizeof(int), (char *)(smallArray+index), sizeof(int));
			dest += sizeof(int);
		}
		anArray = ArrayToAIF(1, &subMin, &subSize, data, 2*sizeof(int),
			AIF_FORMAT(IntToAIF(0)));
		AIFAddFieldToAggregate(aAggregate, AIF_ACCESS_PUBLIC, "b", anArray);
		AIFTest("subtest14", aAggregate, "underlying aggregate: {a=2.1 b=[1 2]}", flag);


		dest = data;
		for (index = min; index < min + size; index++)
		{
			memcpy(dest, AIF_DATA(aAggregate), AIF_LEN(aAggregate));
			dest += AIF_LEN(aAggregate);
		}
		anArray = ArrayToAIF(1, &min, &size, data, size*AIF_LEN(aAggregate), AIF_FORMAT(aAggregate));
		AIFTest("subtest15", anArray, "array of 9 underlying aggregates", flag);
	}


	anEnum = EmptyEnumToAIF("test_enum");
	AIFTest("subtest16", anEnum, "empty enum", flag);

	AIFAddConstToEnum(anEnum, "const1", IntToAIF(10));
	AIFAddConstToEnum(anEnum, "const2", IntToAIF(20));
	AIFAddConstToEnum(anEnum, "const3", IntToAIF(0));
	AIFSetEnum(anEnum, "const1");
	AIFTest("subtest17", anEnum, "enum: 10", flag);

	aAggregate = EmptyAggregateToAIF("test_enum");
	AIFAddFieldToAggregate(aAggregate, AIF_ACCESS_PUBLIC, "field1", anEnum);
	AIFTest("subtest18", aAggregate, "an enum in a aggregate", flag);

	AIFSetEnum(anEnum, "const3");
	aAggregate = EmptyAggregateToAIF("test_enum");
	AIFAddFieldToAggregate(aAggregate, AIF_ACCESS_PUBLIC, "field1", anEnum);
	AIFTest("subtest19", aAggregate, "same aggregate but the enum is set to another value", flag);

	AIFSetEnum(anEnum, "const2");
	aPointer = AIFNullPointer(anEnum);
	aPointer = AIFNullPointer(aPointer);
	AIFAddFieldToAggregate(aAggregate, AIF_ACCESS_PUBLIC, "field2", aPointer);
	AIFTest("subtest20", aAggregate, "field2 is a pointer to a pointer to an enum", flag);


	/* test an array of enums. */
	{
		char data[BUFSIZ], *dest;
		int min=1, size=15, index, val;
		dest = data;
		for (index = min; index < min + size; index++)
		{
			val = rand() % 3;
			switch (val)
			{
				case 0:
					AIFSetEnum(anEnum, "const1");
					break;
				case 1:
					AIFSetEnum(anEnum, "const2");
					break;
				case 2:
					AIFSetEnum(anEnum, "const3");
					break;
			}
			memcpy(dest, AIF_DATA(anEnum), AIF_LEN(anEnum));
			dest += AIF_LEN(anEnum);
		}

		anArray = ArrayToAIF(1, &min, &size, data, size*AIF_LEN(anEnum), AIF_FORMAT(anEnum));
		AIFTest("subtest21", anArray, "array of 15 underlying enums (random)", flag);
	}


	aUnion = EmptyUnionToAIF("test_union");
	AIFTest("subtest22", aUnion, "empty union", flag);

	AIFAddFieldToUnion(aUnion, "const1", IntToAIF(2));
	AIFAddFieldToUnion(aUnion, "const2", FloatToAIF(5.6));
	AIFTest("subtest23", aUnion, "union: 10", flag);

	aAggregate = EmptyAggregateToAIF("test_aggregate");
	AIFAddFieldToAggregate(aAggregate, AIF_ACCESS_PUBLIC, "field1", IntToAIF(1));
	AIFAddFieldToAggregate(aAggregate, AIF_ACCESS_PUBLIC, "field2", StringToAIF("Hello World"));
	AIFAddFieldToAggregate(aAggregate, AIF_ACCESS_PUBLIC, "field3", IntToAIF(2));
	AIFTest("subtest24", aAggregate, "a aggregate: 1, Hello World, 2", flag);

	/* Testing for a long string */
	aString = StringToAIF("AIF0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789AIF");
	AIFTest("subtest25", aString, "string: AIF100digitsAIF", flag);

	fprintf(stdout, "\n");
}

void
_testAscii(char *tst, char *fds, char *data, char *msg, TestFlags flag)
{
	AIF *toTest;
	toTest = AsciiToAIF(fds, data);
	AIFTest(tst, toTest, msg, flag);
	AIFFree(toTest);
}

/*
 * TODO: update to the new formats
 */
void 
TestAllAscii()
{
	TestFlags flag = all;


	fprintf(stdout, "********************************** \n");
	fprintf(stdout, "*** TEST SERIES 2: ascii input *** \n");
	fprintf(stdout, "********************************** \n\n");



	_testAscii("subtest1", "is4", "00000004", "4", flag);

	_testAscii("subtest2", "f4", "3f8ccccd", "1.1", flag);

	_testAscii("subtest3", "f8", "4005bf0a8b145769", "2.718281828459045235360287471352 (f8)", flag);

	_testAscii("subtest4", "f16", "40005bf0a8b1457695355fb8ac404e7a", "2.718281828459045235360287471352662497 (f16)", flag);

	_testAscii("subtest5", "{|a=is4,b=is4;;;}", "0000000200000003", "{a=2, b=3}", flag);

	_testAscii("subtest6", "[r0,2is4]is4", "0000000200000003", "[2, 3]", flag);

	_testAscii("subtest7", "%0/{|val=is4,next=^a4>0/,msg=pa4;;;}", "00000003000163", "{val=3, next=nil, msg=pa4}", flag);

	_testAscii("subtest8", "%0/{|val=is4,next=^a4>0/,msg=pa4;;;}", "0000000101000000020200000000000000030300000000016301620161", "a->b->c->self", flag);

	_testAscii("subtest9", "[r0,2is4]%0/{|val=is4,next=^a4>0/,msg=pa4;;;}", "00000001010000000202000000000000000303000000000163016201610000000203000000000162", "array of two lists of pointers", flag);

	_testAscii("subtest10", "%0/{|val=is4,next=^a4>0/,msg=pa4;;;}", "00000003020000000000000003030000000001630163", "{val=3, next=self, msg=^c}", flag);

	_testAscii("subtest11", "s", "000b613a20686f20686f20686f", "a: ho ho ho", flag);

	_testAscii("subtest12", "[r0,2is4]%0/{|val=is4,next=^a4>0/,msg=s;;;}", "0000000101000000020200000000000000030300000000000b633a20686f20686f20686f000b623a20686f20686f20686f000b613a20686f20686f20686f000000020300000000000b623a20686f20686f20686f", "d[,]", flag);

	_testAscii("subtest13", "[r0,100is4]^a4%0/{|name=s,data=is4,next=^a4>0/}", "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", "empty hash table", flag);

	_testAscii("subtest14", "[r0,100is4]^a4%0/{|name=s,data=is4,next=^a4>0/;;;}", "00000000000000000000000000000000000100057468657265000000010000000000010005746865726500000001000000000000000000010002686900000001000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", "hash table containing hi, there, there", flag);

	_testAscii("subtest15", "[r0,2is4]%0/{|val=is4,next=^a4>0/,msg=s;;;}", "0011111101000000020200000111000000030300000111000b633a20686f20686f20686f000b623a20686f20686f20616f000b613a20686f20686f20686f000000020300000111000b623a20686f20686f20686f", "d[,]", flag);

	_testAscii("subtest16", "%0/{|val=is4,next=^a4>0/,msg=pa4;;;}", "0000000101000000020200000133000000030300000133016301620161", "a->b->c->self", flag);

	_testAscii("subtest17", "[r0,2is4]%0/{|val=is4,next=^a4>0/,msg=pa4;;;}", "00000001010000000202000001330000000303000001330163016201610000000203000001330162", "array of two lists of pointers", flag);

	_testAscii("subtest18", "[r0,3is4][r0,2is4]is4", "000000060000000700000002000000030000000400000005", "array of two lists of pointers", flag);

	_testAscii("subtest19", "v4", "00000006", "void", flag);

	_testAscii("subtest20", "&is4,^a4pa4/is4", "6D61696E00", "function with args (int, char **) returning int", flag);

	fprintf(stdout, "\n");

}

void
_testArithmetic(char *msg1, AIF *a, char *msg2, TestFlags flag, AIF *b)
{
	flag.AIFAdd = b;
	flag.AIFSub = flag.AIFMul = flag.AIFDiv = flag.AIFRem = flag.AIFAdd;
	flag.AIFAnd = flag.AIFOr = flag.AIFAdd;
	AIFTestInt(msg1, a, msg2, flag);
}

void 
TestAllArithmetic()
{
	AIF *anEnum1, *anEnum2, *aAggregate1, *aAggregate2;
	TestFlags flag = none;

	flag.AIFNeg = 1;
	flag.AIFNot = 1;


	fprintf(stdout, "********************************* \n");
	fprintf(stdout, "*** TEST SERIES 3: arithmetic *** \n");
	fprintf(stdout, "********************************* \n\n");



	_testArithmetic("subtest1", IntToAIF(16), "16 and 17", flag, IntToAIF(17));

	_testArithmetic("subtest2", IntToAIF(18), "18 and 17", flag, IntToAIF(17));

	_testArithmetic("subtest3", IntToAIF(0), "0 and -1", flag, IntToAIF(-1));

	_testArithmetic("subtest4", IntToAIF(-1), "-1 and -1", flag, IntToAIF(-1));

	_testArithmetic("subtest5", IntToAIF(1073741824L), "1073741824L and 1073741825L", flag, IntToAIF(1073741825L));

	_testArithmetic("subtest6", LongestToAIF(1073741824L), "1073741824L and 1073741825L", flag, LongestToAIF(1073741825L));

	_testArithmetic("subtest7", LongestToAIF(7L), "7L and 3", flag, IntToAIF(3));

	_testArithmetic("subtest8", FloatToAIF(3.0), "3.0 and 1.5", flag, FloatToAIF(1.5));

	_testArithmetic("subtest9", FloatToAIF(7.0), "7.0 and 3", flag, IntToAIF(3));

	_testArithmetic("subtest10", IntToAIF(3), "3 and 7.0", flag, FloatToAIF(7.0));


	anEnum1 = EmptyEnumToAIF("test_enum1");
	anEnum2 = EmptyEnumToAIF("test_enum2");
	AIFAddConstToEnum(anEnum1, "const1", IntToAIF(16));
	AIFAddConstToEnum(anEnum1, "const2", IntToAIF(18));
	AIFAddConstToEnum(anEnum2, "const1", IntToAIF(17));
	AIFAddConstToEnum(anEnum2, "const2", IntToAIF(17));
	AIFSetEnum(anEnum1, "const1");
	AIFSetEnum(anEnum2, "const1");
	_testArithmetic("subtest11", anEnum1, "anEnum1 and anEnum2", flag, anEnum2);

	aAggregate1 = EmptyAggregateToAIF("test_aggregate1");
	aAggregate2 = EmptyAggregateToAIF("test_aggregate2");
	AIFAddFieldToAggregate(aAggregate1, AIF_ACCESS_PUBLIC, "field1", IntToAIF(16));
	AIFAddFieldToAggregate(aAggregate1, AIF_ACCESS_PUBLIC, "field2", IntToAIF(18));
	AIFAddFieldToAggregate(aAggregate2, AIF_ACCESS_PUBLIC, "field1", IntToAIF(17));
	AIFAddFieldToAggregate(aAggregate2, AIF_ACCESS_PUBLIC, "field2", IntToAIF(17));
	_testArithmetic("subtest12", aAggregate1, "aAggregate1 and aAggregate2", flag, aAggregate2);

	aAggregate1 = EmptyAggregateToAIF("test_aggregate1");
	aAggregate2 = EmptyAggregateToAIF("test_aggregate2");
	AIFAddFieldToAggregate(aAggregate1, AIF_ACCESS_PUBLIC, "field1", IntToAIF(18));
	AIFAddFieldToAggregate(aAggregate1, AIF_ACCESS_PUBLIC, "field2", anEnum1);
	AIFAddFieldToAggregate(aAggregate2, AIF_ACCESS_PUBLIC, "field1", IntToAIF(17));
	AIFAddFieldToAggregate(aAggregate2, AIF_ACCESS_PUBLIC, "field2", anEnum2);
	_testArithmetic("subtest13", aAggregate1, "aAggregate1 and aAggregate2", flag, aAggregate2);

	_testArithmetic("subtest14", anEnum1, "anEnum1 and 17", flag, IntToAIF(17));

	_testArithmetic("subtest15", anEnum1, "anEnum1 and 17", flag, FloatToAIF(17));

	_testArithmetic("subtest16", IntToAIF(17), "17 and anEnum1", flag, anEnum1);

	_testArithmetic("subtest17", FloatToAIF(17), "17 and anEnum1", flag, anEnum1);


	{
		char data[BUFSIZ], *dest;
		int min=1, size=2;
		AIF *anArray1, *anArray2;
		AIF *anInt1 = IntToAIF(10);
		AIF *anInt2 = IntToAIF(20);

		dest = data;
		memcpy(dest, AIF_DATA(anInt1), AIF_LEN(anInt1));
		dest += AIF_LEN(anInt1);
		memcpy(dest, AIF_DATA(anInt1), AIF_LEN(anInt1));
		dest += AIF_LEN(anInt1);
		anArray1 = ArrayToAIF(1, &min, &size, data, size*AIF_LEN(anInt1), AIF_FORMAT(anInt1));

		dest = data;
		memcpy(dest, AIF_DATA(anInt2), AIF_LEN(anInt2));
		dest += AIF_LEN(anInt2);
		memcpy(dest, AIF_DATA(anInt2), AIF_LEN(anInt2));
		dest += AIF_LEN(anInt2);
		anArray2 = ArrayToAIF(1, &min, &size, data, size*AIF_LEN(anInt2), AIF_FORMAT(anInt2));

		_testArithmetic("subtest18", anArray1, "anArray1 and anArray2", flag, anArray2);
	}

	{
		char data[BUFSIZ], *dest;
		int min=1, size=2;
		AIF *anArray1, *anArray2;

		dest = data;
		AIFSetEnum(anEnum1, "const1");
		memcpy(dest, AIF_DATA(anEnum1), AIF_LEN(anEnum1));
		dest += AIF_LEN(anEnum1);
		AIFSetEnum(anEnum1, "const2");
		memcpy(dest, AIF_DATA(anEnum1), AIF_LEN(anEnum1));
		dest += AIF_LEN(anEnum1);
		anArray1 = ArrayToAIF(1, &min, &size, data, size*AIF_LEN(anEnum1), AIF_FORMAT(anEnum1));

		dest = data;
		AIFSetEnum(anEnum2, "const1");
		memcpy(dest, AIF_DATA(anEnum2), AIF_LEN(anEnum2));
		dest += AIF_LEN(anEnum2);
		AIFSetEnum(anEnum2, "const2");
		memcpy(dest, AIF_DATA(anEnum2), AIF_LEN(anEnum2));
		dest += AIF_LEN(anEnum2);
		anArray2 = ArrayToAIF(1, &min, &size, data, size*AIF_LEN(anEnum2), AIF_FORMAT(anEnum2));

		_testArithmetic("subtest19", anArray1, "anArray1 and anArray2", flag, anArray2);
	}

	{
		char data[BUFSIZ], *dest;
		int min=1, size=2;
		AIF *anArray1, *anArray2;
		AIF *anInt = IntToAIF(3);
		AIF *aFloat = FloatToAIF(7);

			dest = data;
			memcpy(dest, AIF_DATA(anInt), AIF_LEN(anInt));
			dest += AIF_LEN(anInt);
			memcpy(dest, AIF_DATA(anInt), AIF_LEN(anInt));
			dest += AIF_LEN(anInt);
			anArray1 = ArrayToAIF(1, &min, &size, data, size*AIF_LEN(anInt), AIF_FORMAT(anInt));

			dest = data;
			memcpy(dest, AIF_DATA(aFloat), AIF_LEN(aFloat));
			dest += AIF_LEN(aFloat);
			memcpy(dest, AIF_DATA(aFloat), AIF_LEN(aFloat));
			dest += AIF_LEN(aFloat);
			anArray2 = ArrayToAIF(1, &min, &size, data, size*AIF_LEN(aFloat), AIF_FORMAT(aFloat));

		_testArithmetic("subtest20", anArray1, "anArray1 and anArray2", flag, anArray2);
	}

	aAggregate1 = EmptyAggregateToAIF("test_aggregate1");
	aAggregate2 = EmptyAggregateToAIF("test_aggregate2");
	AIFAddFieldToAggregate(aAggregate1, AIF_ACCESS_PUBLIC, "field1", IntToAIF(10));
	AIFAddFieldToAggregate(aAggregate1, AIF_ACCESS_PUBLIC, "field2", IntToAIF(10));
	AIFAddFieldToAggregate(aAggregate2, AIF_ACCESS_PUBLIC, "field1", FloatToAIF(7));
	AIFAddFieldToAggregate(aAggregate2, AIF_ACCESS_PUBLIC, "field2", FloatToAIF(7));
	_testArithmetic("subtest21", aAggregate1, "aAggregate1 and aAggregate2", flag, aAggregate2);


	_testArithmetic("subtest22", UnsignedIntToAIF(16), "unsigned 16 and unsigned 17", flag, UnsignedIntToAIF(17));

	_testArithmetic("subtest23", UnsignedIntToAIF(18), "unsigned 18 and unsigned 17", flag, UnsignedIntToAIF(17));

	_testArithmetic("subtest24", UnsignedIntToAIF(0), "unsigned 0 and unsigned -1", flag, UnsignedIntToAIF(-1));

	_testArithmetic("subtest25", UnsignedIntToAIF(-1), "unsigned -1 and unsigned -1", flag, UnsignedIntToAIF(-1));

	_testArithmetic("subtest26", UnsignedIntToAIF(1073741824L), "unsigned 1073741824L and unsigned 1073741825L", flag, UnsignedIntToAIF(1073741825L));

	fprintf(stdout, "\n");

}

void
_testCircularData(char * msg, AIF *a, AIF *b, TestFlags flag)
{
	TestFlags f = flag;
	char txt[BUFSIZ];

	AIFTest(msg, a, "(a) with depth = 0", f);
	AIFTest(msg, b, "(b) with depth = 0", f);

	for (f.D_AIFPrint = -2; f.D_AIFPrint <= flag.D_AIFPrint; f.D_AIFPrint++)
	{
		if (f.D_AIFPrint == 0) continue;

		sprintf(txt, "(a) with depth = %d", f.D_AIFPrint);
		AIFTest(">>>>>>>>", a, txt, f);

		sprintf(txt, "(b) with depth = %d", f.D_AIFPrint);
		AIFTest(">>>>>>>>", b, txt, f);
	}

	_testArithmetic("\n\narithmetic", a, "(a) and (b)", flag, b);

	f = none;
	f.AIFDiff = b;
	AIFTest("\n\nAIFDiff", a, "AIFDiff(a, b)", f);

	printf("\n----------------------------------\n\n");
}

/*
 * TODO: replace with constructors
 */
void 
TestAllCircularData()
{
	AIF *a, *b;

	TestFlags flag = none;

	fprintf(stdout, "************************************ \n");
	fprintf(stdout, "*** TEST SERIES 4: circular data *** \n");
	fprintf(stdout, "************************************ \n\n");

	a = AsciiToAIF((char *)strdup("^%0/{|a=is4,b=^>0/,c=is4;;;}"), (char *)strdup("02000000000000000a03000000000000000a"));
	b = AsciiToAIF((char *)strdup("^%0/{|a=is4,b=^>0/,c=is4;;;}"), (char *)strdup("02000000000000000b03000000000000000c"));
	flag.D_AIFPrint = 3;
	_testCircularData("subtest1", a, b, flag);


	a = AsciiToAIF((char *)strdup("^%0/{|a=is4,b=^>0/,c=is4;;;}"), (char *)strdup("02000000000000000a01000000140300000000000000150000000b"));
	b = AsciiToAIF((char *)strdup("^%0/{|a=is4,b=^>0/,c=is4;;;}"), (char *)strdup("02000000000000000b01000000160300000000000000180000000f"));
	flag.D_AIFPrint = 4;
	_testCircularData("subtest2", a, b, flag);


	a = AsciiToAIF((char *)strdup("^%0/{|a=is4,b=^>0/,c=^%1/{|a=is4,b=^>0/,c=^>1/,d=is4;;;},d=is4;;;}"), (char *)strdup("02000000000000000a010000001400010000001e0300000000000000001e00000014000000000a"));
	b = AsciiToAIF((char *)strdup("^%0/{|a=is4,b=^>0/,c=^%1/{|a=is4,b=^>0/,c=^>1/,d=is4;;;},d=is4;;;}"), (char *)strdup("02000000000000000b01000000170001000000230300000000000000002400000018000000000c"));
	flag.D_AIFPrint = 5;
	_testCircularData("subtest3", a, b, flag);


	a = AsciiToAIF((char *)strdup("^%0/{|a=is4,b=^>0/,c=^%1/{|a=is4,b=^>0/,c=^>1/,d=is4;;;},d=is4;;;}"), (char *)strdup("02000000000000000a0100000014010000001e00010000002800010000003200010000003c0300000000000000003c00000032000000280000001e0000000014000000000a"));
	b = AsciiToAIF((char *)strdup("^%0/{|a=is4,b=^>0/,c=^%1/{|a=is4,b=^>0/,c=^>1/,d=is4;;;},d=is4;;;}"), (char *)strdup("02000000000000000a0100000014010000001e00010000002800010000003200010000003c0300000000000000003c00000032000000280000001e0000000014000000000a"));
	flag.D_AIFPrint = 7;
	_testCircularData("subtest4", a, b, flag);


}

void
_testEPS(AIF *lo, AIF *hi, AIF *a)
{
	int val;
	int ret;

	ret = AIFEPS(lo, hi, a, &val);
	if (ret == 0)
	{
		printf("AIFEPS(lo = ");
		AIFPrint(stdout, 0, lo);
		printf(", hi = ");
		AIFPrint(stdout, 0, hi);
		printf(", a = ");
		AIFPrint(stdout, 0, a);
		printf("): val = %d\n", val);
	}
	else
		printf("*** ERROR *** : %s\n", AIFErrorStr());
}

void 
TestAllEPS()
{
	AIF * a;
	AIF * lo;
	AIF * hi;

	fprintf(stdout, "********************************** \n");
	fprintf(stdout, "*** TEST SERIES 5: AIFEPS      *** \n");
	fprintf(stdout, "********************************** \n\n");

	//AIF_BOOLEAN
	printf("\n>>>>>>> BOOLEAN\n");
	a = BoolToAIF(0); lo = BoolToAIF(0); hi = BoolToAIF(1);
	_testEPS(lo, hi, a);
	_testEPS(a, lo, hi);
	_testEPS(hi, a, lo);
	AIFFree(a); AIFFree(lo); AIFFree(hi);

	//AIF_INTEGER
	printf("\n>>>>>>> INTEGER\n");
	a = IntToAIF(5); lo = IntToAIF(3); hi = IntToAIF(8);
	_testEPS(lo, hi, a);
	_testEPS(a, lo, hi);
	_testEPS(hi, a, lo);
	AIFFree(a); AIFFree(lo); AIFFree(hi);

	//AIF_CHARACTER
	printf("\n>>>>>>> CHARACTER\n");
	a = CharToAIF('c'); lo = CharToAIF('b'); hi = CharToAIF('d');
	_testEPS(lo, hi, a);
	_testEPS(a, lo, hi);
	_testEPS(hi, a, lo);
	AIFFree(a); AIFFree(lo); AIFFree(hi);

	//AIF_ENUM
	printf("\n>>>>>>> ENUM\n");
	a = EmptyEnumToAIF("test_enum");
	AIFAddConstToEnum(a, "const1", IntToAIF(10));
	AIFAddConstToEnum(a, "const2", IntToAIF(20));
	AIFAddConstToEnum(a, "const3", IntToAIF(15));
	AIFSetEnum(a, "const3");
	lo = EmptyEnumToAIF(NULL);
	AIFAddConstToEnum(lo, "const1", IntToAIF(10));
	AIFAddConstToEnum(lo, "const2", IntToAIF(20));
	AIFAddConstToEnum(lo, "const3", IntToAIF(15));
	AIFSetEnum(lo, "const1");
	hi = EmptyEnumToAIF(NULL);
	AIFAddConstToEnum(hi, "const1", IntToAIF(10));
	AIFAddConstToEnum(hi, "const2", IntToAIF(20));
	AIFAddConstToEnum(hi, "const3", IntToAIF(15));
	AIFSetEnum(hi, "const2");
	_testEPS(lo, hi, a);
	_testEPS(a, lo, hi);
	_testEPS(hi, a, lo);
	AIFFree(a); AIFFree(lo); AIFFree(hi);

	a = EmptyEnumToAIF("test_enum");
	AIFAddConstToEnum(a, "const1a", IntToAIF(10));
	AIFAddConstToEnum(a, "const2a", IntToAIF(20));
	AIFAddConstToEnum(a, "const3a", IntToAIF(15));
	AIFSetEnum(a, "const3a");
	lo = EmptyEnumToAIF(NULL);
	AIFAddConstToEnum(lo, "const1lo", IntToAIF(10));
	AIFAddConstToEnum(lo, "const2lo", IntToAIF(20));
	AIFAddConstToEnum(lo, "const3lo", IntToAIF(15));
	AIFSetEnum(lo, "const1lo");
	hi = EmptyEnumToAIF(NULL);
	AIFAddConstToEnum(hi, "const1hi", IntToAIF(10));
	AIFAddConstToEnum(hi, "const2hi", IntToAIF(20));
	AIFAddConstToEnum(hi, "const3hi", IntToAIF(15));
	AIFSetEnum(hi, "const2hi");
	_testEPS(lo, hi, a);
	_testEPS(a, lo, hi);
	_testEPS(hi, a, lo);
	AIFFree(a); AIFFree(lo); AIFFree(hi);

	//AIF_STRING
	printf("\n>>>>>>> STRING\n");
	a = StringToAIF("b");
	lo = StringToAIF("a");
	hi = StringToAIF("c");
	_testEPS(lo, hi, a);
	_testEPS(a, lo, hi);
	_testEPS(hi, a, lo);
	AIFFree(a); AIFFree(lo); AIFFree(hi);

	//AIF_FLOATING
	printf("\n>>>>>>> FLOAT\n");
	a = FloatToAIF(5); lo = FloatToAIF(3); hi = FloatToAIF(8);
	_testEPS(lo, hi, a);
	_testEPS(a, lo, hi);
	_testEPS(hi, a, lo);
	AIFFree(a); AIFFree(lo); AIFFree(hi);
	a = DoubleToAIF(5); lo = DoubleToAIF(3); hi = DoubleToAIF(8);
	_testEPS(lo, hi, a);
	_testEPS(a, lo, hi);
	_testEPS(hi, a, lo);
	AIFFree(a); AIFFree(lo); AIFFree(hi);

	//AIF_ARRAY
	printf("\n>>>>>>> ARRAY\n");
	{
		char data[BUFSIZ], *dest;
		int index, submin, subsize;
		int smallArray[5] = {3,4,5,6,7};
		dest = data;
		submin = 1;
		subsize = 5;
		for (index = 0; index < subsize; index++)
		{
			AIFNormalise(dest, sizeof(int), (char *) (smallArray+index), sizeof(int));
			dest += sizeof(int);
		}
		a = ArrayToAIF(1, &submin, &subsize, data, subsize*sizeof(int), AIF_FORMAT(IntToAIF(0)));

	}
	lo = IntToAIF(10);
	hi = IntToAIF(20);
	_testEPS(lo, hi, a);
	AIFFree(lo); AIFFree(hi);

	lo = IntToAIF(1);
	hi = IntToAIF(20);
	_testEPS(lo, hi, a);
	AIFFree(lo); AIFFree(hi);

	lo = IntToAIF(1);
	hi = IntToAIF(2);
	_testEPS(lo, hi, a);
	AIFFree(lo); AIFFree(hi);
	AIFFree(a);

	//AIF_AGGREGATE
	printf("\n>>>>>>> AGGREGATE\n");
	{
		a = EmptyAggregateToAIF("test_aggregate");
		AIFAddFieldToAggregate(a, AIF_ACCESS_PUBLIC, "val1", IntToAIF(11));
		AIFAddFieldToAggregate(a, AIF_ACCESS_PUBLIC, "val2", IntToAIF(12));
		AIFAddFieldToAggregate(a, AIF_ACCESS_PUBLIC, "val3", IntToAIF(13));
	}
	lo = IntToAIF(18);
	hi = IntToAIF(20);
	_testEPS(lo, hi, a);
	AIFFree(lo); AIFFree(hi);

	lo = IntToAIF(1);
	hi = IntToAIF(20);
	_testEPS(lo, hi, a);
	AIFFree(lo); AIFFree(hi);

	lo = IntToAIF(1);
	hi = IntToAIF(2);
	_testEPS(lo, hi, a);
	AIFFree(lo); AIFFree(hi);
	AIFFree(a);

	//AIF_POINTER
	/* FIXME
	printf("\n>>>>>>> POINTER\n");
	a = IntToAIF(5); lo = IntToAIF(3); hi = IntToAIF(8);
	a = AIFNullPointer(a);
	_testEPS(lo, hi, a);
	AIFFree(lo); AIFFree(hi);

	lo = IntToAIF(1);
	hi = IntToAIF(2);
	_testEPS(lo, hi, a);
	AIFFree(lo); AIFFree(hi);

	lo = IntToAIF(6);
	hi = IntToAIF(7);
	_testEPS(lo, hi, a);
	AIFFree(lo); AIFFree(hi);
	AIFFree(a);
	*/

}

void
_testCompareByName(int dep, AIF *a, AIF *b, char *method)
{
	AIF * c;
	int res;

	if (strcmp(method, "AIFDiff") == 0)
	{
		c = AIFDiff(dep, a, b);
		if (c == NULL)
			printf("*** ERROR *** : %s\n", AIFErrorStr());
		else
		{
			printf("a = ");
			AIFPrint(stdout, 0, a);
			printf("\nb = ");
			AIFPrint(stdout, 0, b);
			printf("\nAIFDiff(depth = %d, a, b) = c = ", dep);
			AIFPrint(stdout, 0, c);
			printf("\n\n");

			AIFFree(c);
		}
	}
	else if (strcmp(method, "AIFCompare") == 0)
	{
		if (AIFCompare(dep, a, b, &res) < 0)
			printf("*** ERROR *** : %s\n", AIFErrorStr());
		else
		{
			printf("a = ");
			AIFPrint(stdout, 0, a);
			printf("\nb = ");
			AIFPrint(stdout, 0, b);
			printf("\nAIFCompare(depth = %d, a, b) = ", dep);
			printf("%d\n\n", res);
		}
	}
	else
		return;
}

/*
 * FIXME: Need to update formats.
 */

void 
TestAllCompareByName()
{
	AIF * a;
	AIF * b;

	AIFSetOption(AIFOPT_CMP_METHOD, AIF_CMP_BY_NAME);

	fprintf(stdout, "************************************** \n");
	fprintf(stdout, "*** TEST SERIES 6: compare by name *** \n");
	fprintf(stdout, "************************************** \n\n");

	a = AsciiToAIF("{|a=is4,b=s;;;}", "0000000400026363");
	b = AsciiToAIF("{|b=s,a=is4;;;}", "0002646400000002");
	_testCompareByName(0, a, b, "AIFDiff");
	AIFFree(a); AIFFree(b);

	a = AsciiToAIF("^%0/{|c=is4,b=^>0/,a=is4;;;}", "02000000000000000a01000000140300000000000000150000000b");
	b = AsciiToAIF("^%2/{|a=is4,b=^>2/,c=is4;;;}", "02000000000000000b01000000160300000000000000180000000f");
	_testCompareByName(0, a, b, "AIFDiff");
	_testCompareByName(1, a, b, "AIFDiff");
	_testCompareByName(2, a, b, "AIFDiff");
	_testCompareByName(3, a, b, "AIFDiff");
	AIFFree(a); AIFFree(b);

	a = AsciiToAIF("^%0/{|a=is4,b=^>0/,c=^%1/{|a=is4,b=^>0/,c=^>1/,d=is4;;;},d=is4;;;}", "02000000000000000a010000001400010000001e0300000000000000001e00000014000000000a");
	b = AsciiToAIF("^%2/{|d=is4,b=^>2/,c=^%3/{|d=is4,b=^>2/,c=^>3/,a=is4;;;},a=is4;;;}", "02000000000000000b01000000170001000000230300000000000000002400000018000000000c");
	_testCompareByName(0, a, b, "AIFDiff");
	_testCompareByName(1, a, b, "AIFDiff");
	_testCompareByName(2, a, b, "AIFDiff");
	_testCompareByName(3, a, b, "AIFDiff");
	AIFFree(a); AIFFree(b);

	/* This one works even though b will be rearranged to
        ** "{|a=is4,c=>0/,b=%0/is4;;;}" since
	** _fds_resolve() in _fds_skip_data() is called before the
	** rearrangement
	*/

	a = AsciiToAIF("{|a=is4,c=is4,b=is4;;;}", "0000000b0000001500000016");
	b = AsciiToAIF("{|a=is4,b=%0/is4,c=>0/;;;}", "0000000a0000001400000015");
	_testCompareByName(0, a, b, "AIFDiff");
	AIFFree(a); AIFFree(b);

	a = AsciiToAIF("{|a=is4,b=s;;;}", "0000000400026363");
	b = AsciiToAIF("{|b=s,a=is4;;;}", "0002636300000004");
	_testCompareByName(0, a, b, "AIFCompare");
	AIFFree(a); AIFFree(b);

	a = AsciiToAIF("{|a=is4,c=is4,b=is4;;;}", "0000000b0000001500000016");
	b = AsciiToAIF("{|a=is4,b=%0/is4,c=>0/;;;}", "0000000b0000001600000015");
	_testCompareByName(0, a, b, "AIFCompare");
	AIFFree(a); AIFFree(b);

	a = AsciiToAIF("^%0/{|a=is4,b=^>0/,c=^%1/{|a=is4,b=^>0/,c=^>1/,d=is4;;;},d=is4;;;}", "02000000000000000b010000001400010000001e0300000000000000001e00000014000000000a");
	b = AsciiToAIF("^%2/{|d=is4,b=^>2/,c=^%3/{|d=is4,b=^>2/,c=^>3/,a=is4;;;},a=is4;;;}", "02000000000000000a01000000170001000000230300000000000000002400000018000000000b");
	_testCompareByName(0, a, b, "AIFCompare");
	_testCompareByName(1, a, b, "AIFCompare");
	AIFFree(a); AIFFree(b);

	AIFSetOption(AIFOPT_CMP_METHOD, AIF_CMP_BY_POSITION);
}

/*
 * There are some test codes that share the same code in this file,
 * thus probably we can replace the similarities with C macros. However,
 * macros can make the code hard to read whereas we want a simple
 * test functions (to avoid the testing of the test functions)
 */


int
AIFTest(char *title, AIF *a, char *msg, TestFlags f)
{
	int ret;

	if ( title != NULL )
		fprintf(stdout, "%s > ", title);

	if ( msg != NULL )
        	fprintf(stdout, "%s:", msg);

	fprintf(stdout, "\n\tAIFPrint: ");
	if (AIFPrint(stdout, f.D_AIFPrint, a) != 0)
		fprintf(stdout, "\n\tAIFPrint ERROR: %s", AIFErrorStr());
	fprintf(stdout, " (%s)", AIF_FORMAT(a));

	ret = _aif_test(a, f);

	fprintf(stdout, "\n");

	return ret;
}

int
_aif_test(AIF *a, TestFlags f)
{
        //char * tmp;
        //_fds_resolve(fds);

	_aif_test_general(a, f);

	switch ( AIFType(a) )
	{
	case AIF_BOOLEAN:
			return _aif_test_bool(a, f);

	case AIF_CHARACTER:
			return _aif_test_char(a, f);

	case AIF_INTEGER:
			return _aif_test_int(a, f);

	case AIF_FLOATING:
			return _aif_test_float(a, f);

	case AIF_ARRAY:
			return _aif_test_array(a, f);

	case AIF_POINTER:
			return _aif_test_pointer(a, f);

	case AIF_REGION:
			return _aif_test_region(a, f);

	case AIF_AGGREGATE:
			return _aif_test_aggregate(a, f);

	case AIF_NAME:
			return _aif_test_name(a, f);

	case AIF_REFERENCE:
			//tmp = (char *) _fds_lookup(fds);
			//return _aif_test(fp, depth, &tmp, data);

	case AIF_STRING:
			return _aif_test_string(a, f);

	case AIF_ENUM:
			return _aif_test_enum(a, f);

	case AIF_UNION:
			return _aif_test_union(a, f);

	case AIF_FUNCTION:
	case AIF_VOID:
	default:
		fprintf(stdout, "Unsupported Type\n");
                break;
	}

	return -1;
}

int
_aif_test_general(AIF *a, TestFlags f)
{
	int zeroVal, compareVal;
	AIF * result;

	// Test AIFIsZero
	if (f.AIFIsZero == 1) {

		if (AIFIsZero(a, &zeroVal))
			fprintf(stdout, "\n\tAIFIsZero ERROR: trouble checking against zero: %s", AIFErrorStr());
		else
			fprintf(stdout, "\n\tAIFIsZero: %sequal to zero", zeroVal ? "" : "not ");

	}

	// Test AIFCompare
	if (f.AIFCompare == 1) {

		if (AIFCompare(0, a, a, &compareVal))
			fprintf(stdout, "\n\tAIFCompare (a,a) ERROR: trouble comparing against self: %s", AIFErrorStr());
		else if (compareVal)
			fprintf(stdout, "\n\tAIFCompare (a,a) ERROR: not equal to itself");
		else
			fprintf(stdout, "\n\tAIFCompare (a,a): OK");

	}

	// Test AIFTypeCompare
	if (f.AIFTypeCompare == 1) {

		if (!AIFTypeCompare(a, a))
			fprintf(stdout, "\n\tAIFTypeCompare (a,a) ERROR: not the same type as itself");
		else
			fprintf(stdout, "\n\tAIFTypeCompare (a,a): OK");

	}

	// Test AIFDiff
	if (f.AIFDiff != NULL) {

		if ( f.AIFDiff == (AIF*)SELF )
			result = AIFDiff(0, a, a);
		else
			result = AIFDiff(0, a, f.AIFDiff);
		if ( result != NULL )
		{
			fprintf(stdout, "\n\tAIFDiff (a,a or f.AIFDiff): ");
			if (AIFPrint(stdout, 0, result) != 0)
				fprintf(stdout, "\n\tAIFPrint ERROR: %s", AIFErrorStr());
			fprintf(stdout, " (%s)", AIF_FORMAT(result));
		}
		else
			fprintf(stdout, "\n\tAIFDiff (a,a or f.AIFDiff) ERROR: %s", AIFErrorStr());

	}

	return 0;
}

int
_aif_test_bool(AIF *a, TestFlags f)
{
	AIF * result;

	// Test AIFAnd
	if (f.AIFAnd != NULL) {

		if ( f.AIFAnd == (AIF*)SELF )
			result = AIFAnd(a,a);
		else
			result = AIFAnd(a, f.AIFAnd);
		if ( result != (AIF *)NULL )
		{
			fprintf(stdout, "\n\tAIFAnd (a, a or f.AIFAnd): ");
			if (AIFPrint(stdout, 0, result) != 0)
				fprintf(stdout, "\n\tAIFPrint ERROR: %s", AIFErrorStr());
			fprintf(stdout, " (%s)", AIF_FORMAT(result));
			AIFFree(result);
		}
		else
			fprintf(stdout, "\n\tAIFAnd (a, a or f.AIFAnd) ERROR: %s", AIFErrorStr());

	}

	// Test AIFOr
	if (f.AIFOr != NULL) {

		if ( f.AIFOr == (AIF*)SELF )
			result = AIFOr(a,a);
		else
			result = AIFOr(a, f.AIFOr);
		if ( result != (AIF *)NULL )
		{
			fprintf(stdout, "\n\tAIFOr (a, a or f.AIFOr): ");
			if (AIFPrint(stdout, 0, result) != 0)
				fprintf(stdout, "\n\tAIFPrint ERROR: %s", AIFErrorStr());
			fprintf(stdout, " (%s)", AIF_FORMAT(result));
			AIFFree(result);
		}
		else
			fprintf(stdout, "\n\tAIFOr (a, a or f.AIFOr) ERROR: %s", AIFErrorStr());

	}

	// Test AIFNeg
	if (f.AIFNeg == 1) {

		result = AIFNeg(a);
		if ( result != (AIF *)NULL )
		{
			fprintf(stdout, "\n\tAIFNeg: ");
			if (AIFPrint(stdout, 0, result) != 0)
				fprintf(stdout, "\n\tAIFPrint ERROR: %s", AIFErrorStr());
			fprintf(stdout, " (%s)", AIF_FORMAT(result));
			AIFFree(result);
		}
		else
			fprintf(stdout, "\n\tAIFNeg ERROR: %s", AIFErrorStr());

	}

	// Test AIFNot
	if (f.AIFNot == 1) {

		result = AIFNot(a);
        if ( result != (AIF *)NULL )
		{
			fprintf(stdout, "\n\tAIFNot: ");
			if (AIFPrint(stdout, 0, result) != 0)
				fprintf(stdout, "\n\tAIFPrint ERROR: %s", AIFErrorStr());
			fprintf(stdout, " (%s)", AIF_FORMAT(result));
			AIFFree(result);
		}
		else
			fprintf(stdout, "\n\tAIFNot ERROR: %s", AIFErrorStr());

	}

	return 0;
}

int
_aif_test_char(AIF *a, TestFlags f)
{
	_aif_test_bool(a, f);

	return 0;
}

int
_aif_test_int(AIF *a, TestFlags f)
{
	AIF * result;

	_aif_test_bool(a, f);

	// Test AIFAdd
	if (f.AIFAdd != NULL) {

		if ( f.AIFAdd == (AIF*)SELF )
			result = AIFAdd(a,a);
		else
			result = AIFAdd(a, f.AIFAdd);
		if ( result != (AIF *)NULL )
		{
			fprintf(stdout, "\n\tAIFAdd (a, a or f.AIFAdd): ");
			if (AIFPrint(stdout, 0, result) != 0)
				fprintf(stdout, "\n\tAIFPrint ERROR: %s", AIFErrorStr());
			fprintf(stdout, " (%s)", AIF_FORMAT(result));
			AIFFree(result);
		}
		else
			fprintf(stdout, "\n\tAIFAdd (a, a or f.AIFAdd) ERROR: %s", AIFErrorStr());

	}

	// Test AIFSub
	if (f.AIFSub != NULL) {

		if (f.AIFSub == (AIF*)SELF)
			result = AIFSub(a,a);
		else
			result = AIFSub(a, f.AIFSub);
		if ( result != (AIF *)NULL )
		{
			fprintf(stdout, "\n\tAIFSub (a, a or f.AIFSub): ");
			if (AIFPrint(stdout, 0, result) != 0)
				fprintf(stdout, "\n\tAIFPrint ERROR: %s", AIFErrorStr());
			fprintf(stdout, " (%s)", AIF_FORMAT(result));
			AIFFree(result);
		}
		else
			fprintf(stdout, "\n\tAIFSub (a, a or f.AIFSub) ERROR: %s", AIFErrorStr());

	}

	// Test AIFMul
	if (f.AIFMul != NULL) {

		if ( f.AIFMul == (AIF*)SELF)
			result = AIFMul(a,a);
		else
			result = AIFMul(a, f.AIFMul);
		if ( result != (AIF *)NULL )
		{
			fprintf(stdout, "\n\tAIFMul (a, a or f.AIFMul): ");
			if (AIFPrint(stdout, 0, result) != 0)
				fprintf(stdout, "\n\tAIFPrint ERROR: %s", AIFErrorStr());
			fprintf(stdout, " (%s)", AIF_FORMAT(result));
			AIFFree(result);
		}
		else
			fprintf(stdout, "\n\tAIFMul (a, a or f.AIFMul) ERROR: %s", AIFErrorStr());

	}

	// Test AIFDiv
	if (f.AIFDiv != NULL) {

		if (f.AIFDiv == (AIF*)SELF)
			result = AIFDiv(a,a);
		else
			result = AIFDiv(a, f.AIFDiv);
		if ( result != (AIF *)NULL )
		{
			fprintf(stdout, "\n\tAIFDiv (a, a or f.AIFDiv): ");
			if (AIFPrint(stdout, 0, result) != 0)
				fprintf(stdout, "\n\tAIFPrint ERROR: %s", AIFErrorStr());
			fprintf(stdout, " (%s)", AIF_FORMAT(result));
			AIFFree(result);
		}
		else
			fprintf(stdout, "\n\tAIFDiv (a, a or f.AIFDiv) ERROR: %s", AIFErrorStr());

	}

	// Test AIFRem
	if (f.AIFRem != NULL) {

		if (f.AIFRem == (AIF*)SELF)
			result = AIFRem(a,a);
		else
			result = AIFRem(a, f.AIFRem);
		if ( result != (AIF *)NULL )
		{
			fprintf(stdout, "\n\tAIFRem (a, a or f.AIFRem): ");
			if (AIFPrint(stdout, 0, result) != 0)
				fprintf(stdout, "\n\tAIFPrint ERROR: %s", AIFErrorStr());
			fprintf(stdout, " (%s)", AIF_FORMAT(result));
			AIFFree(result);
		}
		else
			fprintf(stdout, "\n\tAIFRem (a, a or f.AIFRem) ERROR: %s", AIFErrorStr());

	}

	return 0;
}

int
_aif_test_float(AIF *a, TestFlags f)
{
	_aif_test_int(a, f);

	return 0;
}

int
_aif_test_array(AIF *a, TestFlags f)
{
	int 		tmp1;
	char * 		tmp2;
	int		tmp3;
	AIFIndex * 	tmp4;

	AIFIndex * ix;
	AIF * ax;

	ix = AIFArrayIndexInit(a);
	if (ix == NULL)
	{
		fprintf(stdout, "\n\tAIFArrayIndexInit FATAL ERROR: %s", AIFErrorStr());
		exit(100);
	}


	// Test AIFArrayRank
	if (f.AIFArrayRank == 1) {

		if ( (tmp1 = AIFArrayRank(a)) < 0)
			fprintf(stdout, "\n\tAIFArrayRank ERROR: %s", AIFErrorStr());
		else
			fprintf(stdout, "\n\tAIFArrayRank: %d", tmp1);

	}

	// Test AIFArrayInfo
	if (f.AIFArrayInfo == 1) {

		if ( AIFArrayInfo(a, &tmp1, &tmp2, &tmp3) )
			fprintf(stdout, "\n\tAIFArrayInfo ERROR: %s", AIFErrorStr());
		else
		{
			fprintf(stdout, "\n\tAIFArrayInfo: rank (%d), type (%s), type_id (%d)", tmp1, tmp2, tmp3);
			_aif_free(tmp2);
		}

	}

	// Test AIFArrayBounds
	if (f.AIFArrayBounds == 1) {

		int *min, *size;

		if ( AIFArrayBounds(a, ix->i_rank, &min, &size) < 0 )
			fprintf(stdout, "\n\tAIFArrayBounds ERROR: %s", AIFErrorStr());
		else
		{
			fprintf(stdout, "\n\tAIFArrayBounds: ");

			for ( tmp3 = 0; tmp3 < ix->i_rank; tmp3++)
				fprintf(stdout, "[rank:%d min:%d size:%d] ",
						tmp3, min[tmp3], size[tmp3]);

			_aif_free(min);
			_aif_free(size);
		}

	}

	// Test AIFArrayIndexType
	if (f.AIFArrayIndexType == 1) {

		if ( (tmp2 = AIFArrayIndexType(a)) == NULL )
			fprintf(stdout, "\n\tAIFArrayIndexType ERROR: %s", AIFErrorStr());
		else
		{
			fprintf(stdout, "\n\tAIFArrayIndexType: %s", tmp2);
			_aif_free(tmp2);
		}

	}

	// Test AIFArrayMinIndex
	if (f.AIFArrayMinIndex == 1) {

		for ( tmp1 = 0 ; tmp1 < ix->i_rank ; tmp1++ )
		{
			if ( (tmp3 = AIFArrayMinIndex(a, tmp1)) < 0 )
				fprintf(stdout, "\n\tAIFArrayMinIndex ERROR: %s",
						AIFErrorStr());
			else
				fprintf(stdout, "\n\tAIFArrayMinIndex: [rank %d: %d]", tmp1, tmp3);
		}

	}

	// Test AIFArrayRankSize
	if (f.AIFArrayRankSize == 1) {

		for ( tmp1 = 0 ; tmp1 < ix->i_rank ; tmp1++ )
		{
			if ( (tmp3 = AIFArrayRankSize(a, tmp1)) < 0 )
				fprintf(stdout, "\n\tAIFArrayRankSize ERROR: %s",
						AIFErrorStr());
			else
				fprintf(stdout, "\n\tAIFArrayRankSize: [rank %d: %d]", tmp1, tmp3);
		}

	}


	// Test AIFArraySize
	if (f.AIFArraySize == 1) {

		if ( (tmp1 = AIFArraySize(a)) < 0)
			fprintf(stdout, "\n\tAIFArraySize ERROR: %s", AIFErrorStr());
		else
			fprintf(stdout, "\n\tAIFArraySize: %d", tmp1);

	}

	// Test AIFArraySlice
	if (f.AIFArraySlice == 1) {

		int * mn;
		int * sz;

		mn = (int *)_aif_alloc(sizeof(int) * ix->i_rank);
		sz = (int *)_aif_alloc(sizeof(int) * ix->i_rank);
	
		// AIFArraySlice (first element)
		for (tmp3 = 0; tmp3 < ix->i_rank; tmp3++)
		{
			mn[tmp3] = ix->i_min[tmp3];
			sz[tmp3] = 1;
		}
		ax = AIFArraySlice(a, ix->i_rank, mn, sz);
		if ( ax != (AIF *)NULL )
		{
			fprintf(stdout, "\n\tAIFArraySlice (first): ");
			if (AIFPrint(stdout, 0, ax) != 0)
				fprintf(stdout, "\n\tAIFPrint ERROR: %s", AIFErrorStr());
			fprintf(stdout, " (%s)", AIF_FORMAT(ax));
				AIFFree(ax);
		}
		else
			fprintf(stdout, "\n\tAIFArraySlice (first) ERROR: %s", AIFErrorStr());

		// AIFArraySlice (last element)
		for (tmp3 = 0; tmp3 < ix->i_rank; tmp3++)
		{
			mn[tmp3] = ix->i_min[tmp3] + ix->i_size[tmp3] - 1;
		}
		ax = AIFArraySlice(a, ix->i_rank, mn, sz);
		if ( ax != (AIF *)NULL )
		{
			fprintf(stdout, "\n\tAIFArraySlice (last): ");
			if (AIFPrint(stdout, 0, ax) != 0)
				fprintf(stdout, "\n\tAIFPrint ERROR: %s", AIFErrorStr());
			fprintf(stdout, " (%s)", AIF_FORMAT(ax));
			AIFFree(ax);
		}
		else
			fprintf(stdout, "\n\tAIFArraySlice (last) ERROR: %s", AIFErrorStr());

		// AIFArraySlice (2nd to 2nd last or all)
		for (tmp3 = 0; tmp3 < ix->i_rank; tmp3++)
		{
			if (ix->i_size[tmp3] > 3)
			{
				mn[tmp3] = ix->i_min[tmp3] + 1;
				sz[tmp3] = ix->i_size[tmp3] - 2;
			}
			else
			{
				mn[tmp3] = ix->i_min[tmp3];
				sz[tmp3] = ix->i_size[tmp3];
			}
		}
		ax = AIFArraySlice(a, ix->i_rank, mn, sz);
		if ( ax != (AIF *)NULL )
		{
			fprintf(stdout, "\n\tAIFArraySlice (2nd to 2nd last or all): ");
			if (AIFPrint(stdout, 0, ax) != 0)
				fprintf(stdout, "\n\tAIFPrint ERROR: %s", AIFErrorStr());
			fprintf(stdout, " (%s)", AIF_FORMAT(ax));
			AIFFree(ax);
		}
		else
			fprintf(stdout, "\n\tAIFArraySlice (2nd to 2nd last or all) ERROR: %s", AIFErrorStr());


		_aif_free(mn);
		_aif_free(sz);

	}


	// Test AIFArrayPerm (the rank must be > 1)
	if (f.AIFArrayPerm == 1 && ix->i_rank > 1) {

		int *perm;

		perm = (int *) _aif_alloc(sizeof(int) * ix->i_rank);

		for ( tmp3 = ix->i_rank-1; tmp3 >= 0; tmp3-- )
			perm[tmp3] = ix->i_rank - tmp3 - 1;

		if ( (ax = AIFArrayPerm(a, perm)) == NULL )
			fprintf(stdout, "\n\tAIFArrayPerm ERROR: %s", AIFErrorStr());
		else
		{
			fprintf(stdout, "\n\tAIFArrayPerm: ");
			if (AIFPrint(stdout, 0, ax) != 0)
				fprintf(stdout, "\n\tAIFPrint ERROR: %s", AIFErrorStr());
			fprintf(stdout, " (%s)", AIF_FORMAT(ax));
			AIFFree(ax);
		}

		_aif_free(perm);

	}

	// Test AIFArrayElementToInt
	tmp4 = AIFArrayIndexInit(a);
	if (tmp4 == NULL)
		fprintf(stdout, "\n\tAIFArrayIndexInit ERROR: %s", AIFErrorStr());

	if (f.AIFArrayElementToInt == 1 && AIFBaseType(a) == AIF_INTEGER ) {

		while (tmp4->i_finished != 1)
		{
			int x;
			if (AIFArrayElementToInt(a, tmp4, &x) < 0)
				fprintf(stdout, "\n\tAIFArrayElementToInt ERROR: %s", AIFErrorStr());
			else
				printf("\n\tAIFArrayElementToInt: %d (int)", x);

			AIFArrayIndexInc(tmp4);
		}

        AIFArrayIndexFree(tmp4);

	}

	// Test AIFArrayElementToLongest
	tmp4 = AIFArrayIndexInit(a);
	if (tmp4 == NULL)
		fprintf(stdout, "\n\tAIFArrayIndexInit ERROR: %s", AIFErrorStr());

	if (f.AIFArrayElementToLongest == 1 && AIFBaseType(a) == AIF_INTEGER ) {

		while (tmp4->i_finished != 1)
		{
			AIFLONGEST x;
			if (AIFArrayElementToLongest(a, tmp4, &x) < 0)
				fprintf(stdout, "\n\tAIFArrayElementToLongest ERROR: %s", AIFErrorStr());
			else
				printf("\n\tAIFArrayElementToLongest: %lld (AIFLONGEST)", x);

			AIFArrayIndexInc(tmp4);
		}

        AIFArrayIndexFree(tmp4);

	}

	// Test AIFArrayElementToDouble
	tmp4 = AIFArrayIndexInit(a);
	if (tmp4 == NULL)
		fprintf(stdout, "\n\tAIFArrayIndexInit ERROR: %s", AIFErrorStr());

	if (f.AIFArrayElementToDouble == 1 && AIFBaseType(a) == AIF_FLOATING ) {

		while (tmp4->i_finished != 1)
		{
			double x;
			if (AIFArrayElementToDouble(a, tmp4, &x) < 0)
				fprintf(stdout, "\n\tAIFArrayElementToDouble ERROR: %s", AIFErrorStr());
			else
				printf("\n\tAIFArrayElementToDouble: %f (double)", x);

			AIFArrayIndexInc(tmp4);
		}

        AIFArrayIndexFree(tmp4);

	}

	// Test AIFArrayElementToDoublest
	tmp4 = AIFArrayIndexInit(a);
	if (tmp4 == NULL)
		fprintf(stdout, "\n\tAIFArrayIndexInit ERROR: %s", AIFErrorStr());

	if (f.AIFArrayElementToDoublest == 1 && AIFBaseType(a) == AIF_FLOATING ) {

		while (tmp4->i_finished != 1)
		{
			AIFDOUBLEST x;
			if (AIFArrayElementToDoublest(a, tmp4, &x) < 0)
				fprintf(stdout, "\n\tAIFArrayElementToDoublest ERROR: %s", AIFErrorStr());
			else
			{
	#ifdef HAVE_LONG_DOUBLE
				printf("\n\tAIFArrayElementToDoublest: %Lf (AIFDOUBLEST)", x);
	#else
				printf("\n\tAIFArrayElementToDoublest: %f (AIFDOUBLEST)", x);
	#endif
			}

			AIFArrayIndexInc(tmp4);
		}

        AIFArrayIndexFree(tmp4);

	}


	// Test AIFArrayRef
	if (f.AIFArrayRef == 1) {

		int * loc;

		loc = (int *)_aif_alloc(sizeof(int) * ix->i_rank);

		for (tmp1 = 0; tmp1 < ix->i_rank; tmp1++)
			loc[tmp1] = ix->i_min[tmp1];

		ax = AIFArrayRef(a, ix->i_rank, loc);
		if ( ax != (AIF *)NULL )
		{
			fprintf(stdout, "\n\tAIFArrayRef (first): ");
			if (AIFPrint(stdout, 0, ax) != 0)
				fprintf(stdout, "\n\tAIFPrint ERROR: %s", AIFErrorStr());
			fprintf(stdout, " (%s)", AIF_FORMAT(ax));
				AIFFree(ax);
		}
		else
			fprintf(stdout, "\n\tAIFArrayRef (first) ERROR: %s", AIFErrorStr());

		for (tmp1 = 0; tmp1 < ix->i_rank; tmp1++)
			loc[tmp1] = ix->i_min[tmp1] + ix->i_size[tmp1] - 1;

		ax = AIFArrayRef(a, ix->i_rank, loc);
		if ( ax != (AIF *)NULL )
		{
			fprintf(stdout, "\n\tAIFArrayRef (last): ");
			if (AIFPrint(stdout, 0, ax) != 0)
				fprintf(stdout, "\n\tAIFPrint ERROR: %s", AIFErrorStr());
			fprintf(stdout, " (%s)", AIF_FORMAT(ax));
			AIFFree(ax);
		}
		else
			fprintf(stdout, "\n\tAIFArrayRef (last) ERROR: %s", AIFErrorStr());

		_aif_free(loc);
	}


	// Test AIFSetArrayData
	if (f.AIFSetArrayData != NULL) {

		AIF * result;

		tmp4 = AIFArrayIndexInit(a);
		if (tmp4 == NULL)
			fprintf(stdout, "\n\tAIFArrayIndexInit ERROR: %s", AIFErrorStr());

		if ( (result = CopyAIF(a)) == NULL)
			fprintf(stdout, "\n\tCopyAIF ERROR: %s", AIFErrorStr());

		if ( f.AIFSetArrayData == (AIF *) SELF )
			tmp1 = AIFSetArrayData(result, tmp4, result );
		else
			tmp1 = AIFSetArrayData(result, tmp4, f.AIFSetArrayData );

		if ( tmp1 < 0)
			fprintf(stdout, "\n\tAIFSetArrayData ERROR: %s", AIFErrorStr());
		else
		{
			fprintf(stdout, "\n\tAIFSetArrayData: Set [0] to ");
			if (AIFPrint(stdout, 0, f.AIFSetArrayData) != 0)
				fprintf(stdout, "\n\tAIFPrint ERROR: %s", AIFErrorStr());
			fprintf(stdout, " (%s): ", AIF_FORMAT((AIF *)f.AIFSetArrayData));

			if (AIFPrint(stdout, 0, result) != 0)
				fprintf(stdout, "\n\tAIFPrint ERROR: %s", AIFErrorStr());
			fprintf(stdout, " (%s)", AIF_FORMAT(result));
		}

       	AIFArrayIndexFree(tmp4);
	}


	// Test each of the array elements
	printf("\n");
	tmp1 = 0;
	tmp2 = (char *) _aif_alloc(sizeof(char) * 100);
	tmp4 = AIFArrayIndexInit(a);
	if (tmp4 == NULL)
		fprintf(stdout, "\n\tAIFArrayIndexInit ERROR: %s", AIFErrorStr());
	while ( (ax = AIFArrayElement(a, tmp4)) != NULL )
	{
		sprintf(tmp2, "\t --> Element %d", tmp1);
		AIFTest(NULL, ax, tmp2, f);
		AIFFree(ax);
		tmp1++;
		AIFArrayIndexInc(tmp4);
	}  

	if ( ax == NULL && AIFError() != AIFERR_NOERR )
		fprintf(stdout, "\n\tAIFArrayElement ERROR: %s", AIFErrorStr());

	_aif_free(tmp2);
	AIFArrayIndexFree(tmp4);



	AIFArrayIndexFree(ix);
	return 0;
}

int
_aif_test_pointer(AIF *a, TestFlags f)
{
	return 0;
}

int
_aif_test_region(AIF *a, TestFlags f)
{
	return 0;
}

int
_aif_test_aggregate(AIF *a, TestFlags f)
{
	return 0;
}

int
_aif_test_name(AIF *a, TestFlags f)
{
	return 0;
}

int
_aif_test_reference(AIF *a, TestFlags f)
{
	return 0;
}

int
_aif_test_string(AIF *a, TestFlags f)
{
	return 0;
}

int
_aif_test_enum(AIF *a, TestFlags f)
{
	return 0;
}

int
_aif_test_union(AIF *a, TestFlags f)
{
	return 0;
}


int
AIFTestInt(char *title, AIF *a, char *msg, TestFlags f)
{
	int ret;

	if ( title != NULL )
	fprintf(stdout, "%s > ", title);

	if ( msg != NULL )
		fprintf(stdout, "%s:", msg);

	fprintf(stdout, "\n\tAIFPrint: ");
	if (AIFPrint(stdout, 0, a) != 0)
		fprintf(stdout, "\n\tAIFPrint ERROR: %s", AIFErrorStr());
	fprintf(stdout, " (%s)", AIF_FORMAT(a));

	ret = _aif_test_int(a, f);

	fprintf(stdout, "\n");

	return ret;
}



