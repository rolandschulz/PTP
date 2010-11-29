/*
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

#include <stdlib.h>

#include <stdint.h>
#include <string.h>

#include "x10/x10_da.h"
#include "x10/x10_metadebug_info_map.h"

#define FALSE 0
#define TRUE  1

#define X10DT_Type_Null  			"Null"
#define X10DT_Type_Boolean 			"Boolean"
#define X10DT_Type_Byte    			"Byte"
#define X10DT_Type_Char    			"Char"
#define X10DT_Type_Double    		"Double"	
#define X10DT_Type_Float    		"Float"	
#define X10DT_Type_Int    			"Int"		
#define X10DT_Type_Long    			"Long"		
#define X10DT_Type_Short    		"Short"		

#define X10DT_Type_UByte    		"UByte"		
#define X10DT_Type_UInt    			"UInt"	
#define X10DT_Type_ULong    		"ULong"	
#define X10DT_Type_UShort    		"UShort"	
	
#define X10DT_Type_AsyncClosure    	"AsyncClosure"		
#define X10DT_Type_Class    		"Class"	

#define X10DT_Type_Array    		"Array"	
#define X10DT_Type_Dist    			"Dist"	
#define X10DT_Type_DistArray    	"DistArray"	
#define X10DT_Type_PlaceLocalHandle "PlaceLocalHandle"	
#define X10DT_Type_Rail    			"Rail"	
#define X10DT_Type_Random    		"Random"	
#define X10DT_Type_String    		"String"	
#define X10DT_Type_ValRail    		"ValRail"		
#define X10DT_Type_Region_0    		"Region"	
#define X10DT_Type_Region_1    		"Region"
#define X10DT_Type_Region_2    		"Region"	

#define OFFSET_PLACE_LOCAL_HANDLE_ID 			8
#define OFFSETPLACE_LOCAL_HANDLE_LOCALSTORAGE 	0
#define OFFSETPLACE_LOCAL_HANDLE_CACHED		 	12
#define MAX_ELEMENT_COUNT                       5000

#define CLASS_MEMBER_DEREFERENCE_STR 		"._val->"
#define VAL_VALUE_NAME 						"_val"
#define ADDRESS_OF                   		"&"
#define ARRAY_REGION_FIELD_NAME        		"x10__region"
#define ARRAY_SIZE_FIELD_NAME        		"x10__rawLength"
#define ARRAY_RAW_FIELD_NAME        		"x10__raw"
#define STRING_LENGHT_FIELD_NAME			"x10__content_length"
#define STRING_CONTENT_FIELD_NAME			"x10__content"
#define PLACE_LOCAL_HANDLE_NATIVE_FIELD_NAME "x10____NATIVE_FIELD__"
#define PLACE_LOCAL_HANDLE_LOCALSTORAGE_CPP_FIELD_NAME "x10__localStorage"

#define DISTARRAY_LOCALSTATE_SIZE           "x10__layout.x10__size"

#define FIELD_REFERENCE                 "."
#define ARRAY_RAW_DATA_FIELD_NAME      	"data"
#define THIS_NAME                       "this"

#define STRING_POINTER_TYPE_CAST_STRING "(struct \'x10::lang::String\' *)"

#if 0
#define ARRAY_TYPE_CAST_PART_ONE "(struct \'x10::array::Array<"
#define ARRAY_TYPE_CAST_PART_TWO ">\' *)"
#define DISTARRAY_LOCALSTATE_TYPE_CAST_PART_ONE "(struct \'x10::array::DistArray__LocalState<"
#define DISTARRAY_LOCALSTATE_TYPE_CAST_PART_TWO ">\' *)"

#define TYPE_STRING_STRING   "x10aux::ref<x10::lang::String> "
#define CHAR_POINTER_CASE_STRING		"(char *)"
#endif


#define CHAR_ARRAY_TYPE_CAST_STRING     "(struct \'x10::array::Array<x10_char>\' *)"
#define INT_ARRAY_TYPE_CAST_STRING      "(struct \'x10::array::Array<int>\' *)"
#define BYTE_ARRAY_TYPE_CAST_STRING     "(struct \'x10::array::Array<signed char>\' *)"
#define FLOAT_ARRAY_TYPE_CAST_STRING    "(struct \'x10::array::Array<float>\' *)"
#define DOUBLE_ARRAY_TYPE_CAST_STRING   "(struct \'x10::array::Array<double>\' *)"
#define LONG_ARRAY_TYPE_CAST_STRING    	"(struct \'x10::array::Array<long int>\' *)"
#define SHORT_ARRAY_TYPE_CAST_STRING    "(struct \'x10::array::Array<short int>\' *)"
#define UINT_ARRAY_TYPE_CAST_STRING     "(struct \'x10::array::Array<unsigned int>\' *)"
#define UBYTE_ARRAY_TYPE_CAST_STRING    "(struct \'x10::array::Array<unsigned char>\' *)"
#define ULONG_ARRAY_TYPE_CAST_STRING    "(struct \'x10::array::Array<long unsigned int>\' *)"
#define USHORT_ARRAY_TYPE_CAST_STRING   "(struct \'x10::array::Array<short unsigned int>\' *)"
#define STRING_ARRAY_TYPE_CAST_STRING   "(struct \'x10::array::Array<x10aux::ref<x10::lang::String> >\' *)"								




#define CHAR_DISTARRAY_LOCALSTATE_TYPE_CAST_STRING     "(struct \'x10::array::DistArray__LocalState<x10_char>\' *)"
#define INT_DISTARRAY_LOCALSTATE_TYPE_CAST_STRING      "(struct \'x10::array::DistArray__LocalState<int>\' *)"
#define BYTE_DISTARRAY_LOCALSTATE_TYPE_CAST_STRING     "(struct \'x10::array::DistArray__LocalState<signed char>\' *)"
#define FLOAT_DISTARRAY_LOCALSTATE_TYPE_CAST_STRING    "(struct \'x10::array::DistArray__LocalState<float>\' *)"
#define DOUBLE_DISTARRAY_LOCALSTATE_TYPE_CAST_STRING   "(struct \'x10::array::DistArray__LocalState<double>\' *)"
#define LONG_DISTARRAY_LOCALSTATE_TYPE_CAST_STRING     "(struct \'x10::array::DistArray__LocalState<long int>\' *)"
#define SHORT_DISTARRAY_LOCALSTATE_TYPE_CAST_STRING    "(struct \'x10::array::DistArray__LocalState<short int>\' *)"
#define UINT_DISTARRAY_LOCALSTATE_TYPE_CAST_STRING     "(struct \'x10::array::DistArray__LocalState<unsigned int>\' *)"
#define UBYTE_DISTARRAY_LOCALSTATE_TYPE_CAST_STRING    "(struct \'x10::array::DistArray__LocalState<unsigned char>\' *)"
#define ULONG_DISTARRAY_LOCALSTATE_TYPE_CAST_STRING    "(struct \'x10::array::DistArray__LocalState<long unsigned int>\' *)"
#define USHORT_DISTARRAY_LOCALSTATE_TYPE_CAST_STRING   "(struct \'x10::array::DistArray__LocalState<short unsigned int>\' *)"
#define STRING_DISTARRAY_LOCALSTATE_TYPE_CAST_STRING   "(struct \'x10::array::DistArray__LocalState<x10aux::ref<x10::lang::String> >\' *)"								



#define RIGHT_BRACKET_AND_DEREFERENCE 	")->"

#define X10DT_Int_CPP_TYPE				"x10_int"
#define X10DT_Long_CPP_TYPE				"x10_long"
#define X10DT_Short_CPP_TYPE			"x10_short"
#define X10DT_Byte_CPP_TYPE				"x10_byte"
#define X10DT_Char_CPP_TYPE				"x10_char"
#define X10DT_Float_CPP_TYPE			"x10_float"
#define X10DT_Double_CPP_TYPE			"x10_double"
#define X10DT_Boolean_CPP_TYPE			"x10_boolean"
#define X10DT_UByte_CPP_TYPE			"x10_ubyte"
#define X10DT_UInt_CPP_TYPE				"x10_uint"
#define X10DT_ULong_CPP_TYPE			"x10_ulong"
#define X10DT_UShort_CPP_TYPE			"x10_ushort"

static int SIZE_OF_X10DT_Int 			= 0;
static int SIZE_OF_X10DT_Long			= 0;
static int SIZE_OF_X10DT_Short 			= 0;
static int SIZE_OF_X10DT_Byte			= 0;
static int SIZE_OF_X10DT_Char			= 0;
static int SIZE_OF_X10DT_Float			= 0;
static int SIZE_OF_X10DT_Double			= 0;
static int SIZE_OF_X10DT_Boolean		= 0;
static int SIZE_OF_X10DT_UByte			= 0;
static int SIZE_OF_X10DT_UInt			= 0;
static int SIZE_OF_X10DT_ULong			= 0;
static int SIZE_OF_X10DT_UShort			= 0;
static int SIZE_OF_POINTER              = 0;

//Need to maintain an Array Element type map
static int AllocatedArrayElementTypeMapSize = 0;
static int arrayElementTypeMapSize = 0;
static _x10_array_map *arrayElementTypeMap = NULL;

/*
 * Look through the arrayElementTypeMap to see if this type + subtype have been listed there.
 * If yes, return this entry.
 * If not, append this type + subtype into the map.
 */
int static 
findOrAppendType(x10dbg_type_t type, x10dbg_type_t subType)
{
	if (NULL == arrayElementTypeMap) {
		AllocatedArrayElementTypeMapSize = 256;
		arrayElementTypeMap = (_x10_array_map*)malloc(AllocatedArrayElementTypeMapSize * sizeof(_x10_array_map));
	}
	
	int i = 0;
	_x10_array_map *p = arrayElementTypeMap;
	int result = -1;
	for (i = 0; i < arrayElementTypeMapSize; i++) {
		if (type == p->x10_type) {
			if (type != X10DTArray || type != X10DTDistArrayLocalState) {
				result = i;
				break;
			}
			else {
				//It is an array, make sure its subtype matches.
				int typeIndex = p->x10_type_index;
				if (-1 != typeIndex) {
					_x10_array_map *subTypeP = arrayElementTypeMap + typeIndex;
					if (subType == subTypeP->x10_type) {
						result = i;
						break;
					}
				}
			}
		}
		p++;
	}
	if (-1 != result) {
		return result;
	}
	//We will add this type into the map
	//since 256 should be big enough for all the simple type we deal with here.
	//So don't need to worry about that allocated space is not enough
	int subTypeIndex = -1;
	if (X10DTNull != subType) {
		subTypeIndex = findOrAppendType(subType, X10DTNull);
	}
	p = arrayElementTypeMap + arrayElementTypeMapSize;
	p->x10_type = type;
	p->x10_type_index = subTypeIndex;
	result = arrayElementTypeMapSize;
	arrayElementTypeMapSize++;
	return result;

}

/*
 * Get the address of an member of a class.
 */
static char *
getMemberAddress(MISession *session, x10variable_t *parent, x10variable_t *member)
{
	char *addressString = NULL;
	int  inputLength = 0;
	char *input = NULL;
	MIVar *miVar = NULL;
	char *varName = NULL;
	
	inputLength = strlen(ADDRESS_OF) + strlen(member->cpp_full_name) + 1;
	input = (char*)malloc(inputLength);
	strcpy(input, ADDRESS_OF);
	strcat(input, member->cpp_full_name);
	miVar = CreateMIVar(session, input);
	free(input);
	if (NULL == miVar) {
		return NULL;
	}
	varName = miVar->name;
	addressString = GetVarValue(session, varName);
	free(miVar);
	return addressString;
}

/*
 * Get the x10dbg_type_t value of the input type name.
 */
static x10dbg_type_t 
getTypeFromTypeName(char *typeName)
{
	if (0 == strcmp(typeName, "int")) {
		return X10DTInt;
	}
	else if (0 == strcmp(typeName, "signed char")) {
		return X10DTByte;
	}
	else if (0 == strcmp(typeName, "float")) {
		return X10DTFloat;
	}
	else if (0 == strcmp(typeName, "double")) {
		return X10DTDouble;
	}
	else if (0 == strcmp(typeName, "long")) {
		return X10DTLong;
	}
	else if (0 == strcmp(typeName, "short")) {
		return X10DTShort;
	}
	else if (0 == strcmp(typeName, "unsigned int")) {
		return X10DTUInt;
	}
	else if (0 == strcmp(typeName, "unsigned char")) {
		return X10DTUByte;
	}
	else if (0 == strcmp(typeName, "unsigned long")) {
		return X10DTULong;
	}
	else if (0 == strcmp(typeName, "unsigned short")) {
		return X10DTUShort;
	}
	else if (0 == strcmp(typeName, "x10_char")) {
		return X10DTChar;
	}
	else if (0 == strcmp(typeName, "x10::lang::String")) {
		return X10DTString;
	}
	else {
		return X10DTInt;
	}

	return X10DTInt;
	
}

/*
 * Check if the input variable name is "this".
 */
static int 
checkVariableThis(char *name)
{
	return (NULL != name && 0 == strcmp(name, THIS_NAME));
}

/*
 * Read the certain bytes of memory contents from a memory location.
 */
static char* 
readValueFromMemory(MISession *session, char *address, int length)
{
	char *result = NULL;
	char *p = NULL;
	memoryinfo  *memInfo_value = NULL;
	memory  	*mem_value = NULL;
	memInfo_value = GetMemoryInfo(session, 0, address, "x", length, 1, 1, NULL);
	if (NULL == memInfo_value) {
		return NULL;
	}
	SetList(memInfo_value->memories);
	mem_value = (memory*)GetListElement(memInfo_value->memories);
	SetList(mem_value->data);
	p = (char*)GetListElement(mem_value->data);
	if (p != NULL) {
		result = strdup(p);
	}
	FreeMemoryInfo(memInfo_value);
	return result;

}

/*
 * Input a gdb print command, and get the result of this command in hex form.
 */
static char* 
getHexLocation(MISession *session, char *input)
{
	char *resultString = NULL;
	char *location = NULL;
	char *p = NULL;
	char *tmp = NULL;
	MICommand *cmd = CLIPrintHex(input);
	SendCommandWait(session, cmd);
	if (!MICommandResultOK(cmd)) {
	//DEBUG_PRINTS(DEBUG_LEVEL_BACKEND, "------------------- ElementCount error\n");
		SetDebugError(cmd);
		MICommandFree(cmd);
		return NULL;
	}
	resultString = CLIGetHexValueLineFromPrintInfo(cmd);
	if (NULL == resultString) {
		return NULL;
	}
	p = strchr(resultString, '=');
	if (NULL != p) {
		p = p + 1; //skip '='
		tmp = strstr(p, "0x");
	}
	else {
		//looking for "0x" directly
		tmp = strstr(resultString, "0x");
	}
	if (NULL != tmp) {
		//hex representation
		location = strdup(p);
	}
	else if (NULL != p) {
		//decimal represenation
		location = strdup(p);
	}
	
	if (resultString != NULL) {
		free(resultString);
	}
	return location;

}

/*
 * Get the corresponding C++ implementation type of an X10 type.
 */
static char *
getTypeCppName(int type)
{
	char *typeCppname = NULL;

	switch (type) {
		case X10DTChar:
			typeCppname = X10DT_Char_CPP_TYPE;
			break;
		case X10DTInt:
			typeCppname = X10DT_Int_CPP_TYPE;
			break;
		case X10DTByte:
			typeCppname = X10DT_Byte_CPP_TYPE;
			break;
		case X10DTFloat:
			typeCppname = X10DT_Float_CPP_TYPE;
			break;
		case X10DTDouble:
			typeCppname = X10DT_Double_CPP_TYPE;
			break;
		case X10DTLong:
			typeCppname = X10DT_Long_CPP_TYPE;
			break;
		case X10DTShort:
			typeCppname = X10DT_Short_CPP_TYPE;
			break;
		case X10DTUInt:
			typeCppname = X10DT_UInt_CPP_TYPE;
			break;
		case X10DTUByte:
			typeCppname = X10DT_UByte_CPP_TYPE;
			break;
		case X10DTULong:
			typeCppname = X10DT_ULong_CPP_TYPE;
			break;
		case X10DTUShort:
			typeCppname = X10DT_UShort_CPP_TYPE;
			break;
		case X10DTBoolean:
			typeCppname = X10DT_Boolean_CPP_TYPE;
			break;
		default:
			typeCppname = X10DT_Int_CPP_TYPE;
			break;
	}

	return typeCppname;
	
}

/*
 * Return the gdb type casting command for the input X10 type.
 */
static char *
getArrayTypeCastString(int type)
{
	char *typeCastString = NULL;

	switch (type) {
		case X10DTChar:
			typeCastString = CHAR_ARRAY_TYPE_CAST_STRING;
			break;
		case X10DTInt:
			typeCastString = INT_ARRAY_TYPE_CAST_STRING;
			break;
		case X10DTByte:
			typeCastString = BYTE_ARRAY_TYPE_CAST_STRING;
			break;
		case X10DTFloat:
			typeCastString = FLOAT_ARRAY_TYPE_CAST_STRING;
			break;
		case X10DTDouble:
			typeCastString = DOUBLE_ARRAY_TYPE_CAST_STRING;
			break;
		case X10DTLong:
			typeCastString = LONG_ARRAY_TYPE_CAST_STRING;
			break;
		case X10DTShort:
			typeCastString = SHORT_ARRAY_TYPE_CAST_STRING;
			break;
		case X10DTUInt:
			typeCastString = UINT_ARRAY_TYPE_CAST_STRING;
			break;
		case X10DTUByte:
			typeCastString = UBYTE_ARRAY_TYPE_CAST_STRING;
			break;
		case X10DTULong:
			typeCastString = ULONG_ARRAY_TYPE_CAST_STRING;
			break;
		case X10DTUShort:
			typeCastString = USHORT_ARRAY_TYPE_CAST_STRING;
			break;
		case X10DTString:
			typeCastString = STRING_ARRAY_TYPE_CAST_STRING;
			break;
		default:
			typeCastString = INT_ARRAY_TYPE_CAST_STRING;
			break;
	}

	return typeCastString;
	
}

/*
 * Return the gdb type casting command for the input X10 type which is an element in DistArray.
 */
static char *
getDistArrayLocalStateTypeCastString(int type)
{
	char *typeCastString = NULL;

	switch (type) {
		case X10DTChar:
			typeCastString = CHAR_DISTARRAY_LOCALSTATE_TYPE_CAST_STRING;
			break;
		case X10DTInt:
			typeCastString = INT_DISTARRAY_LOCALSTATE_TYPE_CAST_STRING;
			break;
		case X10DTByte:
			typeCastString = BYTE_DISTARRAY_LOCALSTATE_TYPE_CAST_STRING;
			break;
		case X10DTFloat:
			typeCastString = FLOAT_DISTARRAY_LOCALSTATE_TYPE_CAST_STRING;
			break;
		case X10DTDouble:
			typeCastString = DOUBLE_DISTARRAY_LOCALSTATE_TYPE_CAST_STRING;
			break;
		case X10DTLong:
			typeCastString = LONG_DISTARRAY_LOCALSTATE_TYPE_CAST_STRING;
			break;
		case X10DTShort:
			typeCastString = SHORT_DISTARRAY_LOCALSTATE_TYPE_CAST_STRING;
			break;
		case X10DTUInt:
			typeCastString = UINT_DISTARRAY_LOCALSTATE_TYPE_CAST_STRING;
			break;
		case X10DTUByte:
			typeCastString = UBYTE_DISTARRAY_LOCALSTATE_TYPE_CAST_STRING;
			break;
		case X10DTULong:
			typeCastString = ULONG_DISTARRAY_LOCALSTATE_TYPE_CAST_STRING;
			break;
		case X10DTUShort:
			typeCastString = USHORT_DISTARRAY_LOCALSTATE_TYPE_CAST_STRING;
			break;
		case X10DTString:
			typeCastString = STRING_DISTARRAY_LOCALSTATE_TYPE_CAST_STRING;
			break;
		default:
			typeCastString = INT_DISTARRAY_LOCALSTATE_TYPE_CAST_STRING;
			break;
	}

	return typeCastString;
	
}

/*
 * Retrieve the contents of an X10 String variable.
 */
static char *
getStringContents(MISession *session, x10variable_t *var)
{
	char *input = NULL;
	int  inputLength = 0;
	int  contentLength = 0;
	MICommand *cmd = NULL;
	char *resultString = NULL;
	char *content = NULL;
	char *p = NULL;
	if (NULL == var->location) {
		return NULL;
	}
	//Find out the x10_content_length and x10_content
	//Get length;
	inputLength = 1 + strlen(STRING_POINTER_TYPE_CAST_STRING) + strlen(var->location) + strlen(RIGHT_BRACKET_AND_DEREFERENCE) + strlen(STRING_LENGHT_FIELD_NAME) + 1;
	input = (char*)malloc(inputLength);
	strcpy(input, "(");
	strcat(input, STRING_POINTER_TYPE_CAST_STRING);
	strcat(input, var->location);
	strcat(input, RIGHT_BRACKET_AND_DEREFERENCE);
	strcat(input, STRING_LENGHT_FIELD_NAME);
	cmd = CLIPrint(input);
	free(input);
	SendCommandWait(session, cmd);
	if (!MICommandResultOK(cmd)) {
		//DEBUG_PRINTS(DEBUG_LEVEL_BACKEND, "------------------- ElementCount error\n");
		SetDebugError(cmd);
		MICommandFree(cmd);
		return NULL;
	}
	resultString = CLIGetPrintInfo(cmd);
	p = strchr(resultString, '=');		
	MICommandFree(cmd);
	if (p != NULL) {
		contentLength = strtol(p+1, NULL, 10);
	}
	if (resultString != NULL) {
		free (resultString);
		resultString = NULL;
	}
	if (contentLength <= 0) {
		return NULL;
	}
	//Get content 
	inputLength = 1 + strlen(STRING_POINTER_TYPE_CAST_STRING) + strlen(var->location) + strlen(RIGHT_BRACKET_AND_DEREFERENCE) + strlen(STRING_CONTENT_FIELD_NAME) + 1;
	input = (char*)malloc(inputLength);
	strcpy(input, "(");
	strcat(input, STRING_POINTER_TYPE_CAST_STRING);
	strcat(input, var->location);
	strcat(input, RIGHT_BRACKET_AND_DEREFERENCE);
	strcat(input, STRING_CONTENT_FIELD_NAME);
	cmd = CLIPrint(input);
	free(input);
	SendCommandWait(session, cmd);
	if (!MICommandResultOK(cmd)) {
		//DEBUG_PRINTS(DEBUG_LEVEL_BACKEND, "------------------- getStringContents error\n");
		SetDebugError(cmd);
		MICommandFree(cmd);
		return NULL;
	}
	resultString = CLIGetPrintInfo(cmd);
	MICommandFree(cmd);
	//the result will be something like "$2 = 0x100bcc10 "dummy""
	p = strchr(resultString, '\"');
	if (NULL != p) {
		char *p1 = strrchr(resultString, '\"');
		if (p1 >= p)
		{
			*p1 = '\0';  //replace the ending '"' with '\0'
			content = strdup(p + 1);
		}
		else {
			content = strdup("");
		}
	}
	if (resultString != NULL) {
		free (resultString);
		resultString = NULL;
	}
	return content;
			
}

/*
 * Return if the input variable is an Array.
 */
int 
IsArray(x10variable_t var)
{
	return var.is_array;
}

/*
 * Return if the input variable is a user defined Class.
 */
int 
IsClass(x10variable_t var)
{
	return (X10DTClass == var.type);
}

/*
 * Return a member variable of a built-in X10 type.
 */
x10variable_t *
MemberVariable(MISession *session, x10variable_t *parent, const char* name, int type)
{
	x10variable_t *returnedVar = X10VariableNew();
	switch (parent->type) {
	case X10DTRegion:
		if (0 == strcmp(name, "rank") && X10DTInt == type) {
			returnedVar->name = strdup(name);
			returnedVar->cpp_name = strdup("x10__rank");
			int cppFullNameLen = strlen(parent->cpp_full_name) +
									strlen(CLASS_MEMBER_DEREFERENCE_STR) +
									strlen(returnedVar->cpp_name) +
									1;
			returnedVar->cpp_full_name = (char*)malloc(cppFullNameLen);
			strcpy(returnedVar->cpp_full_name, parent->cpp_full_name);
			strcat(returnedVar->cpp_full_name, CLASS_MEMBER_DEREFERENCE_STR);
			strcat(returnedVar->cpp_full_name, returnedVar->cpp_name);
			returnedVar->type = type;
			returnedVar->type_name = TypeName(type);
			if (0 == SIZE_OF_X10DT_Int)	{
				SIZE_OF_X10DT_Int = GetSizeOf(session, X10DT_Int_CPP_TYPE);
			}
			returnedVar->size = SIZE_OF_X10DT_Int;
			returnedVar->num_children = 0;
			returnedVar->location = MemberLocation(session, parent, returnedVar);
		} else if (0 == strcmp(name, "rect") && X10DTBoolean == type) {
			returnedVar->name = strdup("rect");
			returnedVar->cpp_name = strdup("x10__rect");
			int cppFullNameLen = strlen(parent->cpp_full_name) +
									strlen(CLASS_MEMBER_DEREFERENCE_STR) +
									strlen(returnedVar->cpp_name) +
									1;
			returnedVar->cpp_full_name = (char*)malloc(cppFullNameLen);
			strcpy(returnedVar->cpp_full_name, parent->cpp_full_name);
			strcat(returnedVar->cpp_full_name, CLASS_MEMBER_DEREFERENCE_STR);
			strcat(returnedVar->cpp_full_name, returnedVar->cpp_name);
			returnedVar->type = type;
			returnedVar->type_name = TypeName(type);
			if (0 == SIZE_OF_X10DT_Boolean)	{
				SIZE_OF_X10DT_Boolean = GetSizeOf(session, X10DT_Boolean_CPP_TYPE);
			}
			returnedVar->size = SIZE_OF_X10DT_Boolean;
			returnedVar->num_children = 0;
			returnedVar->location = MemberLocation(session, parent, returnedVar);
		} else if (0 == strcmp(name, "zeroBased") && X10DTBoolean == type) {
			returnedVar->name = strdup("zeroBased");
			returnedVar->cpp_name = strdup("x10__zeroBased");
			int cppFullNameLen = strlen(parent->cpp_full_name) +
									strlen(CLASS_MEMBER_DEREFERENCE_STR) +
									strlen(returnedVar->cpp_name) +
									1;
			returnedVar->cpp_full_name = (char*)malloc(cppFullNameLen);
			strcpy(returnedVar->cpp_full_name, parent->cpp_full_name);
			strcat(returnedVar->cpp_full_name, CLASS_MEMBER_DEREFERENCE_STR);
			strcat(returnedVar->cpp_full_name, returnedVar->cpp_name);
			returnedVar->type = type;
			returnedVar->type_name = TypeName(type);
			if (0 == SIZE_OF_X10DT_Boolean)	{
				SIZE_OF_X10DT_Boolean = GetSizeOf(session, X10DT_Boolean_CPP_TYPE);
			}
			returnedVar->size = SIZE_OF_X10DT_Boolean;
			returnedVar->num_children = 0;
			returnedVar->location = MemberLocation(session, parent, returnedVar);
		}
		break;
	case X10DTPlaceLocalHandle:
		if (0 == strcmp(name, "id") && X10DTInt == type) {
			returnedVar->name = strdup(name);
			returnedVar->cpp_name = strdup("x10__id");
			int cppFullNameLen = strlen(parent->cpp_full_name) +
									strlen(FIELD_REFERENCE) +
									strlen(PLACE_LOCAL_HANDLE_NATIVE_FIELD_NAME) +
									strlen(FIELD_REFERENCE) +
									strlen(returnedVar->cpp_name) +
									1;
			returnedVar->cpp_full_name = (char*)malloc(cppFullNameLen);
			strcpy(returnedVar->cpp_full_name, parent->cpp_full_name);
			strcat(returnedVar->cpp_full_name, FIELD_REFERENCE);
			strcat(returnedVar->cpp_full_name, PLACE_LOCAL_HANDLE_NATIVE_FIELD_NAME);
			strcat(returnedVar->cpp_full_name, FIELD_REFERENCE);
			strcat(returnedVar->cpp_full_name, returnedVar->cpp_name);
			returnedVar->type = type;
			returnedVar->type_name = TypeName(type);
			if (0 == SIZE_OF_X10DT_Int)	{
				SIZE_OF_X10DT_Int = GetSizeOf(session, X10DT_Int_CPP_TYPE);
			}
			returnedVar->size = SIZE_OF_X10DT_Int;
			returnedVar->num_children = 0;
			returnedVar->location = MemberLocation(session, parent, returnedVar);
		} else if (0 == strcmp(name, "localStorage") && X10DTClass == type) {
			returnedVar->name = strdup(name);
			returnedVar->cpp_name = strdup(PLACE_LOCAL_HANDLE_LOCALSTORAGE_CPP_FIELD_NAME);
			int cppFullNameLen = strlen(parent->cpp_full_name) +
									strlen(FIELD_REFERENCE) +
									strlen(PLACE_LOCAL_HANDLE_NATIVE_FIELD_NAME) +
									strlen(FIELD_REFERENCE) +
									strlen(returnedVar->cpp_name) +
									1;
			returnedVar->cpp_full_name = (char*)malloc(cppFullNameLen);
			strcpy(returnedVar->cpp_full_name, parent->cpp_full_name);
			strcat(returnedVar->cpp_full_name, FIELD_REFERENCE);
			strcat(returnedVar->cpp_full_name, PLACE_LOCAL_HANDLE_NATIVE_FIELD_NAME);
			strcat(returnedVar->cpp_full_name, FIELD_REFERENCE);
			strcat(returnedVar->cpp_full_name, returnedVar->cpp_name);
			//Need to find out the location of this member.
			returnedVar->location = MemberLocation(session, parent, returnedVar);

			//Use gdb command:
			int inputLength = strlen(parent->cpp_full_name) +
									strlen(FIELD_REFERENCE) +
									strlen(PLACE_LOCAL_HANDLE_NATIVE_FIELD_NAME)  +
									strlen(FIELD_REFERENCE) +
									strlen("x10__cached") +
									1;
				char *input = (char*)malloc(inputLength);
				strcpy(input, parent->cpp_full_name);
				strcat(input, FIELD_REFERENCE);
				strcat(input, PLACE_LOCAL_HANDLE_NATIVE_FIELD_NAME);
				strcat(input, FIELD_REFERENCE);
				strcat(input, "x10__cached");
				printf("Input is: %s\n", input);
				MIVar *miVar = CreateMIVar(session, input);
				if (NULL == miVar) {
					//This PlaceLocalHandle does not have any local value yet.
					printf("Cannot evaluate %s\n", input);
					returnedVar->num_children = 0;
					returnedVar->type = X10DTClass;
					returnedVar->type_name = strdup("Not_Defined");
					break;
				}
				char *miVarname = miVar->name;
				char *cachedResult = GetVarValue(session, miVarname);
				printf("cachedResult is: %s\n", cachedResult);
				fflush(stdout);
				if (NULL != strstr(cachedResult, "false")) {
					//This PlaceLocalHandle does not have any local value yet.
					returnedVar->num_children = 0;
					returnedVar->type = X10DTClass;
					returnedVar->type_name = strdup("Not_Defined");
					break;
				}

			//Read the content of it to see if it is NULL.
			if (0 == SIZE_OF_POINTER) {
				SIZE_OF_POINTER = GetAddressLength(session);
			}
			uint64_t localStorageValue = 0;
			char *localStorageStringValue = readValueFromMemory(session, returnedVar->location, SIZE_OF_POINTER);
			if (NULL != localStorageStringValue) {
				localStorageValue = strtoll(localStorageStringValue, NULL, 16);
			}
			if (localStorageValue == 0) {
				//This PlaceLocalHandle does not have any local value yet.
				returnedVar->location = localStorageStringValue;
				returnedVar->num_children = 0;
				returnedVar->type = X10DTClass;
				returnedVar->type_name = strdup("Not_Defined");
				break;
			}

			//Need to find out its type and its _val value
			//If we already know the type of this PlaceLocalHandle, use it.
			if (-1 != parent->type_index && -1 == parent->map_index) {
				_x10_array_map *thisTypeMap = arrayElementTypeMap + parent->type_index;
				returnedVar->type = thisTypeMap->x10_type;
				returnedVar->type_name = TypeName(returnedVar->type);
				if (X10DTArray == returnedVar->type || X10DTDistArrayLocalState == returnedVar->type) {
					returnedVar->type_index = thisTypeMap->x10_type_index;
					//ask the debug assistant for array size.
					returnedVar->num_children = ElementCount(session, parent, returnedVar);
					returnedVar->is_array = 1;
				}
			} else {
				//Otherwise, we need to figure it out.
				int inputLength = strlen(returnedVar->cpp_full_name) +
									strlen(FIELD_REFERENCE) +
									strlen(VAL_VALUE_NAME)  +
									1;
				char *input = (char*)malloc(inputLength);
				strcpy(input, returnedVar->cpp_full_name);
				strcat(input, FIELD_REFERENCE);
				strcat(input, VAL_VALUE_NAME);
				MIVar *miVar = CreateMIVar(session, input);
				free(input);
				if (NULL == miVar) {
					returnedVar->location = localStorageStringValue;
					returnedVar->num_children = 0;
					returnedVar->type = X10DTClass;
					returnedVar->type_name = strdup("Not_Defined");
					break;
				}
				char *varType = miVar->type;
				if (strstr(varType, "x10::array::Array") != NULL) {
					//it is an array
					returnedVar->type = X10DTArray;
					returnedVar->type_name = TypeName(returnedVar->type);
					returnedVar->is_array = 1;
					//Need to find out the element type of this variable
					//Find first ">", and find last "<".  The string in between, is the type.
					char *lastLeftArrow = strrchr(varType, '<');
					char *firstRightArrow = strchr(varType, '>');
					*firstRightArrow = '\0';
					x10dbg_type_t elementType = getTypeFromTypeName(lastLeftArrow+1);
					int typeIndex = findOrAppendType(elementType, X10DTNull);
					returnedVar->type_index = typeIndex;
					returnedVar->map_index = -1;
					//ask the debug assistant for array size.
					returnedVar->num_children = ElementCount(session, parent, returnedVar);
				} else {
					//it is a class
					returnedVar->type = X10DTClass;
					//Find out class information
					char *lastLeftArrow = strrchr(varType, '<');
					char *firstRightArrow = strchr(varType, '>');
					char *className = NULL;
					if (NULL == lastLeftArrow || NULL == firstRightArrow) {
						char *star = strrchr(varType, '*');
						if (NULL != star) {
							if (' ' == *(star-1)) {
								*(star-1) = '\0';
								className = varType;
							} else {
								*star = '\0';
								className = varType;
							}
						}
					} else {
						*firstRightArrow = '\0';
						className = lastLeftArrow + 1;
					}
					if (strstr(className, "Random")) {
						//Built in class Random.
						returnedVar->type_name = strdup(varType);
						returnedVar->location = MemberLocation(session, parent, returnedVar);
						returnedVar->num_children = 0;
					} else {
						returnedVar->type_name = strdup(varType);
						returnedVar->location = MemberLocation(session, parent, returnedVar);
						returnedVar->num_children = 0;
					}
				}
				free(miVar);
				
			}

				

		}
		break;
	case X10DTPoint:
		if (0 == strcmp(name, "rank")) {
			returnedVar->name = strdup(name);
			returnedVar->cpp_name = strdup("x10__rank");
			int cppFullNameLen = strlen(parent->cpp_full_name) +
									strlen(CLASS_MEMBER_DEREFERENCE_STR) +
									strlen(returnedVar->cpp_name) +
									1;
			returnedVar->cpp_full_name = (char*)malloc(cppFullNameLen);
			strcpy(returnedVar->cpp_full_name, parent->cpp_full_name);
			strcat(returnedVar->cpp_full_name, CLASS_MEMBER_DEREFERENCE_STR);
			strcat(returnedVar->cpp_full_name, returnedVar->cpp_name);
			returnedVar->type = X10DTInt;
			returnedVar->type_name = TypeName(type);
			if (0 == SIZE_OF_X10DT_Int) {
				SIZE_OF_X10DT_Int = GetSizeOf(session, X10DT_Int_CPP_TYPE);
			}
			returnedVar->size = SIZE_OF_X10DT_Int;
			returnedVar->num_children = 0;
			returnedVar->location = MemberLocation(session, parent, returnedVar);
		} else if (0 == strcmp(name, "c")) {
			returnedVar->name = strdup(name);
			returnedVar->cpp_name = strdup("x10__c0");
			int cppFullNameLen = strlen(parent->cpp_full_name) +
									strlen(CLASS_MEMBER_DEREFERENCE_STR) +
									strlen(returnedVar->cpp_name) +
									1;
			returnedVar->cpp_full_name = (char*)malloc(cppFullNameLen);
			strcpy(returnedVar->cpp_full_name, parent->cpp_full_name);
			strcat(returnedVar->cpp_full_name, CLASS_MEMBER_DEREFERENCE_STR);
			strcat(returnedVar->cpp_full_name, returnedVar->cpp_name);

			returnedVar->type = X10DTArray;
			returnedVar->type_name = TypeName(type);
			returnedVar->type_index = findOrAppendType(X10DTInt, X10DTNull);
			returnedVar->map_index = -1;
			returnedVar->location = MemberLocation(session, parent, returnedVar);
			//Need to find out the rank of it
			x10variable_t *rankMember = MemberVariable(session, parent, "rank", X10DTInt);
			if (NULL != rankMember && NULL != rankMember->location) {
				char *tmpData = readValueFromMemory(session, rankMember->location, SIZE_OF_X10DT_Int);
				if (NULL != tmpData) {
					returnedVar->num_children = strtoul(tmpData, NULL, 16);
					if (returnedVar->num_children > 4)
					{
						//For now, we only display up to rank 4.
						returnedVar->num_children = 4;
					}
					free(tmpData);
				}
			}
			//need to free rankMember.
			X10VariableFree(rankMember);
		}
		break;
	case X10DTDistArray:
		if (0 == strcmp(name, "dist")) {
			returnedVar->name = strdup(name);
			returnedVar->cpp_name = strdup("x10__dist");
			int cppFullNameLen = strlen(parent->cpp_full_name) +
									strlen(CLASS_MEMBER_DEREFERENCE_STR) +
									strlen(returnedVar->cpp_name) +
									1;
			returnedVar->cpp_full_name = (char*)malloc(cppFullNameLen);
			strcpy(returnedVar->cpp_full_name, parent->cpp_full_name);
			strcat(returnedVar->cpp_full_name, CLASS_MEMBER_DEREFERENCE_STR);
			strcat(returnedVar->cpp_full_name, returnedVar->cpp_name);
			returnedVar->type = X10DTDist;
			returnedVar->type_name = TypeName(type);
			returnedVar->num_children = 0;
			returnedVar->location = MemberLocation(session, parent, returnedVar);
		} else if (0 == strcmp(name, "localHandle")) {
			returnedVar->name = strdup(name);
			returnedVar->cpp_name = strdup("x10__localHandle");
			int cppFullNameLen = strlen(parent->cpp_full_name) +
									strlen(CLASS_MEMBER_DEREFERENCE_STR) +
									strlen(returnedVar->cpp_name) +
									1;
			returnedVar->cpp_full_name = (char*)malloc(cppFullNameLen);
			strcpy(returnedVar->cpp_full_name, parent->cpp_full_name);
			strcat(returnedVar->cpp_full_name, CLASS_MEMBER_DEREFERENCE_STR);
			strcat(returnedVar->cpp_full_name, returnedVar->cpp_name);

			returnedVar->type = X10DTPlaceLocalHandle;
			returnedVar->type_name = TypeName(type);
			//Need to find out its type
			MIVar *miVar = CreateMIVar(session, returnedVar->cpp_full_name);
			if (NULL != miVar) {
				char *varType = miVar->type;

				returnedVar->num_children = 2;
				returnedVar->location = MemberLocation(session, parent, returnedVar);

				//It should be a place local handle for DistArray__LocalState
				if (strstr(varType, "x10::array::DistArray__LocalState") != NULL) {
					char *lastLeftArrow = strrchr(varType, '<');
					char *firstRightArrow = strchr(varType, '>');
					*firstRightArrow = '\0';
					x10dbg_type_t elementType = getTypeFromTypeName(lastLeftArrow+1);
					int typeIndex = findOrAppendType(X10DTDistArrayLocalState, elementType);
					returnedVar->type_index = typeIndex;
				}
			} else {
				returnedVar->num_children = 0;
				returnedVar->location = NULL;
			}

		}
		break;
	default:
		returnedVar = NULL;
		break;
	}
	return returnedVar;
}

/*
 * Return the place of where this application is running on.
 */
int 
place()
{
	return 0;
}

/*
 * Retrieve the X10 variable of the input name from this debug session.
 */
x10variable_t *
SymbolVariable(void *session, const char* name)
{
	return NULL;	
}

/*
 * Return the type name used to be displayed in UI of the input X10 type.
 */
char* 
TypeName(x10dbg_type_t type)
{
	char *result = NULL;
	// Primitive Types
	switch(type) {
		case X10DTNull:
			result = strdup("Null");
			break;
		case X10DTBoolean:
			result = strdup("Boolean");
			break;
		case X10DTByte:
			result = strdup("Byte");
			break;
		case X10DTChar:
			result = strdup("Char");
			break;
		case X10DTDouble:
			result = strdup("Double");
			break;
		case X10DTFloat:
			result = strdup("Float");
			break;
		case X10DTInt:
			result = strdup("Int");
			break;
		case X10DTLong:
			result = strdup("Long");
			break;
		case X10DTShort:
			result = strdup("Short");
			break;
		case X10DTUByte:
			result = strdup("UByte");
			break;
		case X10DTUInt:
			result = strdup("UInt");
			break;
		case X10DTULong:
			result = strdup("ULong");
			break;
		case X10DTUShort:
			result = strdup("UShort");
			break;
		case X10DTAsyncClosure:
			result = strdup("AsyncClosure");
			break;
		case X10DTClass:
			result = strdup("Class");
			break;
		case X10DTArray:
			result = strdup("Array");
			break;
		case X10DTDistArrayLocalState:
			result = strdup("Array");
			break;
		case X10DTDist:
			result = strdup("Dist");
			break;
		case X10DTDistArray:
			result = strdup("DistArray");
			break;
		case X10DTPlaceLocalHandle:
			result = strdup("PlaceLocalHandle");
			break;
		case X10DTRail:
			result = strdup("Rail");
			break;
		case X10DTRandom:
			result = strdup("Random");
			break;
		case X10DTString:
			result = strdup("String");
			break;
		case X10DTValRail:
			result = strdup("ValRail");
			break;
		case X10DTRegion:
			result = strdup("Region");
			break;
		case X10DTPoint:
			result = strdup("Point");
			break;
		default:
			result = strdup("Null");
	}
	
	return result;
}


/*
 * Return the type of the input X10 variable.
 */
int 
VariableType(x10variable_t var)
{
	return var.type;
}

/*
 * Return the size of the input X10 variable.
 */
int 
VariableSize(x10variable_t var)
{
	return 0;
}

/*
 * Return the name of the input X10 variable.
 */
const char* 
VariableName(x10variable_t var)
{
	return var.name;
}

/*
 * Return the offset of the input X10 variable.
 */
int 
VariableOffsetOf(x10variable_t var)
{
	return 0;
}

/*
 * Return the memory location of the input X10 variable.
 */
char * 
VariableLocation(MISession *session, x10variable_t *var)
{
	char *addressString = NULL;
	char *input = NULL;
	int  inputLength = 0;
	switch (var->type) {
		case X10DTClass:
			//If this variable is "this", then it is already the _val value of this class object
			if (1 == checkVariableThis(var->cpp_name)) {
				input = strdup(var->cpp_name);
			}
			else {
				inputLength = strlen(var->cpp_full_name) + strlen(FIELD_REFERENCE) + strlen(VAL_VALUE_NAME) + 1;
				input = (char*)malloc(inputLength);
				strcpy(input, var->cpp_full_name);	
				strcat(input, FIELD_REFERENCE);
				strcat(input, VAL_VALUE_NAME);
			}
			break;
		case X10DTString:
		case X10DTRegion:
		case X10DTPoint:
			//Get where its _val points to
			inputLength = strlen(var->cpp_full_name) + strlen(FIELD_REFERENCE) + strlen(VAL_VALUE_NAME) + 1;
			input = (char*)malloc(inputLength);
			strcpy(input, var->cpp_full_name);	
			strcat(input, FIELD_REFERENCE);
			strcat(input, VAL_VALUE_NAME);
			break;
		case X10DTPlaceLocalHandle:
			//Get the location of its x10____NATIVE_FIELD__
			inputLength = strlen(ADDRESS_OF) + strlen(var->cpp_full_name) + strlen(FIELD_REFERENCE) + strlen(PLACE_LOCAL_HANDLE_NATIVE_FIELD_NAME) + 1;
			input = (char*)malloc(inputLength);
			strcpy(input, ADDRESS_OF);
			strcat(input,var->cpp_full_name);
			strcat(input, FIELD_REFERENCE);
			strcat(input, PLACE_LOCAL_HANDLE_NATIVE_FIELD_NAME);
			break;
		default:
			inputLength = strlen(ADDRESS_OF) + strlen(var->cpp_full_name) + 1;
			input = (char*)malloc(inputLength);
			strcpy(input, ADDRESS_OF);
			strcat(input, var->cpp_full_name);	
			break;
	}
	
	MIVar *miVar = CreateMIVar(session, input);
	free(input);
	if (NULL == miVar) {
		return NULL;
	}
	char *varName = miVar->name;
	addressString = GetVarValue(session, varName);
	if (NULL == addressString) {
		return NULL;
	}
	//Make sure the addree should be a valid hex number string
	char * p = strstr(addressString, "0x");
	if (NULL == p) {
		free(addressString);
		return NULL;
	}
	//Sometime, there is trailing value too, we need to get rid of.
	//So find the first space.
	char *p1 = strchr(p, ' ');
	if (NULL != p1) {
		*p1 = '\0';
	}
	char *returnedString = strdup(p);
	free(addressString);
	return returnedString;
}

/*
 * Return the location of a member variable.
 */
char* 
MemberLocation(MISession *session, x10variable_t *parent, x10variable_t *member)
{
	char *addressString = NULL;
	int  inputLength = 0;
	char *input = NULL;
	uint64_t parentAddress = 0;
	uint64_t memberAddress = 0;

	if (NULL == parent->location) {
		return NULL;
	}

	switch (parent->type) {
		case X10DTPlaceLocalHandle:
			//It is hard to figure out the type of it.  So we just use the hard code offset for now
			//for id, the offset is 8 bytes
			parentAddress = strtoll(parent->location, NULL, 16);
			if (0 == strcmp(member->name, "id")) {
				memberAddress = parentAddress + OFFSET_PLACE_LOCAL_HANDLE_ID;
			} else if (0 == strcmp(member->name, "localStorage")) {
				memberAddress = parentAddress + OFFSETPLACE_LOCAL_HANDLE_LOCALSTORAGE;
			}
			addressString = (char*)malloc(20);
			sprintf(addressString, "0x%llX", memberAddress);
			break; 
		case X10DTDistArray:
			if (0 == strcmp(member->name, "dist")) {
				addressString = getMemberAddress(session, parent, member);
			} else if (0 == strcmp(member->name, "localHandle")) {
				//Get the location of its x10____NATIVE_FIELD__
				//Its fullname should be parentName + .val-> + its cpp name + . + PLACE_LOCAL_HANDLE_NATIVE_FIELD_NAME
				inputLength = strlen(ADDRESS_OF) + 
								strlen(member->cpp_full_name) + 
								strlen(FIELD_REFERENCE) + 
								strlen(PLACE_LOCAL_HANDLE_NATIVE_FIELD_NAME) + 1;
				input = (char*)malloc(inputLength);
				strcpy(input, ADDRESS_OF);
				strcat(input, member->cpp_full_name);
				strcat(input, FIELD_REFERENCE);
				strcat(input, PLACE_LOCAL_HANDLE_NATIVE_FIELD_NAME);
				MIVar *miVar = CreateMIVar(session, input);
				free(input);
				if (NULL == miVar) {
					return NULL;
				}
				char *varName = miVar->name;
				addressString = GetVarValue(session, varName);
				free(miVar);
			}
			break;
		default:
			addressString = getMemberAddress(session, parent, member);
			break;
	}
	return addressString;
}

/*
 * Evalute the value of an input X10 variable.
 */
int 
VariableValue(MISession *session, x10variable_t *var, char** data, int* length)
{
	if (NULL == var->location) {
		*data = strdup("error_value_unknown");
		*length = strlen("error_value_unknown");
		return DBGRES_ERR;
	}
	
	char *resultString = NULL;
	if (X10DTString == var->type) {
		resultString = getStringContents(session, var);
		if (resultString != NULL) {
			*data = strdup(resultString);
			*length = strlen(resultString);
			free(resultString);
			return DBGRES_OK;
		} else {
			*data = strdup("error_value_unknown");
			*length = strlen("error_value_unknown");
			return DBGRES_ERR;
		}
	} else if (X10DTChar == var->type) {
		int readMemoryLength = 0;
		char *tmpData = NULL;
		
		if (0 == SIZE_OF_X10DT_Char) {
			SIZE_OF_X10DT_Char = GetSizeOf(session, X10DT_Char_CPP_TYPE);
		}
		readMemoryLength = SIZE_OF_X10DT_Char;
		tmpData = readValueFromMemory(session, var->location, readMemoryLength);
		if (NULL != tmpData) {
			uint8_t value_u8 = strtol(tmpData, NULL, 16);
			resultString = (char*)malloc(20);
			sprintf(resultString, "%c", value_u8);
			free(tmpData);
			*data = strdup(resultString);
			*length = strlen(resultString+1);
			free(resultString);
			return DBGRES_OK;
		} else {
			*data = strdup("error_value_unknown");
			*length = strlen("error_value_unknown");
			return DBGRES_ERR;
		}
		
	} else {
		char *typeCppName = getTypeCppName(var->type);
		//Construct gdb command to evaluate this value.
		int inputLength = 0;
		char *input = NULL;
		MICommand *cmd = NULL;
		//Get content 
		inputLength = 1 + strlen("*((") + 
						strlen(typeCppName) + 
						strlen(" *)") + 
						strlen(var->location) + 
						strlen(")") + 1;
		input = (char*)malloc(inputLength);
		strcpy(input, "*((");
		strcat(input, typeCppName);
		strcat(input, " *)");
		strcat(input, var->location);
		strcat(input, ")");
		cmd = CLIPrint(input);
		SendCommandWait(session, cmd);
		free(input);
		if (!MICommandResultOK(cmd)) {
			//DEBUG_PRINTS(DEBUG_LEVEL_BACKEND, "------------------- getStringContents error\n");
			SetDebugError(cmd);
			MICommandFree(cmd);
			return DBGRES_ERR;
		}
		resultString = CLIGetPrintInfo(cmd);
		MICommandFree(cmd);
 
		if (NULL == resultString) {
			*data = strdup("error_value_unknown");
			*length = strlen("error_value_unknown");
			return DBGRES_ERR;
		}
		char *p = strchr(resultString, '=');		
		
		if (NULL == p) {
			free(resultString);
			*data = strdup("error_value_unknown");
			*length = strlen("error_value_unknown");
			return DBGRES_ERR;
		}
		*data = strdup(p+1);
		*length = strlen(p+1);
		free(resultString);
		return DBGRES_OK;
	
	}
	
	
}

/*
 * Retrieve the members of an X10 built-in variable.
 */
int 
QueryBuiltinType(MISession *session, int x10Type, x10_builtin_type_t** builtInType)
{
	x10_builtin_type_t *returnedType = (x10_builtin_type_t*)malloc(sizeof(x10_builtin_type_t));
	x10member_t *p = NULL;
	
	switch (x10Type) {
	case X10DTRegion:
		returnedType->type_name = TypeName(x10Type);
		returnedType->member_count = 3;
		returnedType->member = (x10member_t*)malloc(returnedType->member_count * sizeof(x10member_t));
		p = returnedType->member;
		p->type = X10DTInt;
		p->name = strdup("rank");
		p++;
		p->type = X10DTBoolean;
		p->name = strdup("rect");
		p++;
		p->type = X10DTBoolean;
		p->name = strdup("zeroBased");
		break;
	case X10DTPlaceLocalHandle:
		returnedType->type_name = TypeName(x10Type);
		returnedType->member_count = 2;
		returnedType->member = (x10member_t*)malloc(returnedType->member_count * sizeof(x10member_t));
		p = returnedType->member;
		p->type = X10DTInt;
		p->name = strdup("id");
		p++;
		p->type = X10DTClass;
		p->name = strdup("localStorage");
		break;
	case X10DTDistArray:
		returnedType->type_name = TypeName(x10Type);
		returnedType->member_count = 2;
		returnedType->member = (x10member_t*)malloc(returnedType->member_count * sizeof(x10member_t));
		p = returnedType->member;
		p->type = X10DTDist;
		p->name = strdup("dist");
		p++;
		p->type = X10DTPlaceLocalHandle;
		p->name = strdup("localHandle");
		break;
	case X10DTPoint:
		returnedType->type_name = TypeName(x10Type);
		returnedType->member_count = 2;
		returnedType->member = (x10member_t*)malloc(returnedType->member_count * sizeof(x10member_t));
		p = returnedType->member;
		p->type = X10DTInt;
		p->name = strdup("rank");
		p++;
		p->type = X10DTArray;
		p->name = strdup("c");
		break;
	}
	*builtInType = returnedType;
	return DBGRES_OK;
}

/*
 * Return the element of an X10 Array variable.
 */
long 
ElementCount(MISession *session, x10variable_t *parent, x10variable_t *var)
{
	char *resultString = NULL;
	long elementCount = -1;
	if (NULL == parent) {
		//Just find out the _val->x10_rawLength of this variable.
		int inputLength = strlen(var->cpp_full_name) + strlen(CLASS_MEMBER_DEREFERENCE_STR) + strlen(ARRAY_SIZE_FIELD_NAME) + 1;
		char *input = (char*)malloc(inputLength);
		strcpy(input, var->cpp_full_name);
		strcat(input, CLASS_MEMBER_DEREFERENCE_STR);
		strcat(input, ARRAY_SIZE_FIELD_NAME);
		MIVar *miVar = CreateMIVar(session, input);
		free(input);
		if (NULL == miVar) {
			return 0;
		}
		char *varName = miVar->name;
		resultString = GetVarValue(session, varName);
		if (resultString != NULL) {
			elementCount = strtol(resultString, NULL, 10);
		}
		else {
			return 0;
		}
	} else {
		if (NULL == var->location) {
			return 0;
		}
		
		//It has parent, which could be tricky to get the result by evaluate.  
		//Use the location of the member to find out where _val points to.
		if (0 == SIZE_OF_POINTER) {
			SIZE_OF_POINTER = GetAddressLength(session);
		}
		//Read the value of _val
		char *_valValue = readValueFromMemory(session, var->location, SIZE_OF_POINTER);
		//Construct the eval string depending on the type of the array element.
		int elementType = GetArrayElementType(var);
		int inputLength = 0;
		char *input = NULL;
		char *typeCastString = NULL;
		char *p = NULL;
		if (X10DTDistArrayLocalState == var->type) {
			typeCastString = getDistArrayLocalStateTypeCastString(elementType);
		} else if (X10DTArray == var->type) {
			typeCastString = getArrayTypeCastString(elementType);
		}

		if (NULL == typeCastString) {
			return 0;
		}

		if (X10DTDistArrayLocalState == var->type) {
			inputLength = 1 + strlen(typeCastString) + strlen(_valValue) + strlen(RIGHT_BRACKET_AND_DEREFERENCE) + strlen(DISTARRAY_LOCALSTATE_SIZE) + 1;
			input = (char*)malloc(inputLength);
			strcpy(input, "(");
			strcat(input, typeCastString);
			strcat(input, _valValue);
			strcat(input, RIGHT_BRACKET_AND_DEREFERENCE);
			strcat(input, DISTARRAY_LOCALSTATE_SIZE);
		} else if (X10DTArray == var->type) {
			//First, we need to check if x10__region._val has is null.
			//If not, then, the x10__rawLength is usable.
			int checkInputLength = 0;
			char *checkInput = NULL;
			checkInputLength = 1 + 
								strlen(typeCastString) + 
								strlen(_valValue) + 
								strlen(RIGHT_BRACKET_AND_DEREFERENCE) + 
								strlen(ARRAY_REGION_FIELD_NAME) + 
								strlen(FIELD_REFERENCE) + 
								strlen(VAL_VALUE_NAME)+ 
								1;
			checkInput = (char*)malloc(checkInputLength);
			strcpy(checkInput, "(");
			strcat(checkInput, typeCastString);
			strcat(checkInput, _valValue);
			strcat(checkInput, RIGHT_BRACKET_AND_DEREFERENCE);
			strcat(checkInput, ARRAY_REGION_FIELD_NAME);
			strcat(checkInput, FIELD_REFERENCE);
			strcat(checkInput, VAL_VALUE_NAME);
			resultString = getHexLocation(session, checkInput);
	
			uint64_t regionAddress = 0;
			if (NULL != resultString) {
				regionAddress = strtoll(resultString, NULL, 16);
			}
			free (resultString);
			free (checkInput);
			
			if (regionAddress == 0) {
				return 0;
			}
			inputLength = 1 + strlen(typeCastString) + strlen(_valValue) + strlen(RIGHT_BRACKET_AND_DEREFERENCE) + strlen(ARRAY_SIZE_FIELD_NAME) + 1;
			input = (char*)malloc(inputLength);
			strcpy(input, "(");
			strcat(input, typeCastString);
			strcat(input, _valValue);
			strcat(input, RIGHT_BRACKET_AND_DEREFERENCE);
			strcat(input, ARRAY_SIZE_FIELD_NAME);
		}
		MICommand *cmd = CLIPrint(input);
		free(input);
		SendCommandWait(session, cmd);
		if (!MICommandResultOK(cmd)) {
			//DEBUG_PRINTS(DEBUG_LEVEL_BACKEND, "------------------- ElementCount error\n");
			SetDebugError(cmd);
			MICommandFree(cmd);
			return 0;
		}
		resultString = CLIGetPrintInfo(cmd);
		MICommandFree(cmd);
		
		if (NULL == resultString) {
			SetDebugError(cmd);
			return 0;
		}
		p = strchr(resultString, '=');
				
		if (p != NULL) {
			elementCount = strtol(p+1, NULL, 10);
		}

		free (resultString);
			
	}
		
	//Make sure element count is not too big for now
	if (elementCount > MAX_ELEMENT_COUNT) {
		elementCount = MAX_ELEMENT_COUNT;
	} else if (elementCount < 0) {
		//For some reason, we cannot get the elementCount.  Set it to 0
		elementCount = 0;
	}
	return elementCount;
}

/*
 * Return starting location of where the elements of an X10 variable stored.
 */
char* 
ElementRawDataLocation(MISession *session, x10variable_t *var)
{
	char *resultString = NULL;
	if (NULL == var->location) {
			return NULL;
	}
		
	//It has parent, which could be tricky to get the result by evaluate.  
	//Use the location of the member to find out where _val points to.
	if (0 == SIZE_OF_POINTER) {
		SIZE_OF_POINTER = GetAddressLength(session);
	}
	//Read the value of _val
	char *_valValue = readValueFromMemory(session, var->location, SIZE_OF_POINTER);
	//Construct the eval string depending on the type of the array element.
	int elementType = GetArrayElementType(var);
	int inputLength = 0;
	char *input = NULL;

	char *typeCastString = NULL;
	if (X10DTDistArrayLocalState == var->type) {
		typeCastString = getDistArrayLocalStateTypeCastString(elementType);
	} else if (X10DTArray == var->type) {
		typeCastString = getArrayTypeCastString(elementType);
	}
	if (NULL == typeCastString) {
		return 0;
	}
		
	if (NULL != typeCastString) {
		inputLength = 1 + strlen(typeCastString) + strlen(_valValue) + strlen(RIGHT_BRACKET_AND_DEREFERENCE) + strlen(ARRAY_RAW_FIELD_NAME) + strlen(FIELD_REFERENCE) + strlen(ARRAY_RAW_DATA_FIELD_NAME) + 1;
		input = (char*)malloc(inputLength);
		strcpy(input, "(");
		strcat(input, typeCastString);
		strcat(input, _valValue);
		strcat(input, RIGHT_BRACKET_AND_DEREFERENCE);
		strcat(input, ARRAY_RAW_FIELD_NAME);
		strcat(input, FIELD_REFERENCE);
		strcat(input, ARRAY_RAW_DATA_FIELD_NAME);
		resultString = getHexLocation(session, input);
		free(input);
		if (NULL != resultString) {
			//Make sure it is not a NULL value
			uint64_t address = strtoll(resultString, NULL, 16);
			if (address <= 0) {
				//Not a valid address
				free(resultString);
				resultString = NULL;
			}
		}
	}
		
	return resultString;
}

/*
 * Return memory location of a certain X10 Array element.
 */
char* 
ElementDataLocation(MISession *session, x10variable_t *var, char *rawDataLocation, long elementIndex)
{
	//convert rawDataLocatoin string to the number.
	uint64_t rawDataAddress = strtoll(rawDataLocation, NULL, 16);
	uint64_t elementDataAddress = 0;
	char *result = NULL;
	int elementSize = 0;
	if (0 == SIZE_OF_POINTER) {
		SIZE_OF_POINTER = GetAddressLength(session);
	}
	//depending on the type of element, we will find out the address of this element.
	switch (var->type) {
	case X10DTInt:
		if (0 == SIZE_OF_X10DT_Int) {
			SIZE_OF_X10DT_Int = GetSizeOf(session, X10DT_Int_CPP_TYPE);
		}
		elementSize = SIZE_OF_X10DT_Int;
		break;
	case X10DTLong:
		if (0 == SIZE_OF_X10DT_Long) {
			SIZE_OF_X10DT_Long = GetSizeOf(session, X10DT_Long_CPP_TYPE);
		}
		elementSize = SIZE_OF_X10DT_Long;
		break;
	case X10DTShort:
		if (0 == SIZE_OF_X10DT_Short) {
			SIZE_OF_X10DT_Short = GetSizeOf(session, X10DT_Short_CPP_TYPE);
		}
		elementSize = SIZE_OF_X10DT_Short ;
		break;
	case X10DTByte:
		if (0 == SIZE_OF_X10DT_Byte) {
			SIZE_OF_X10DT_Byte = GetSizeOf(session, X10DT_Byte_CPP_TYPE);
		}
		elementSize = SIZE_OF_X10DT_Byte;
		break;
	case X10DTChar:
		if (0 == SIZE_OF_X10DT_Char) {
			SIZE_OF_X10DT_Char = GetSizeOf(session, X10DT_Char_CPP_TYPE);
		}
		elementSize = SIZE_OF_X10DT_Char;
		break;
	case X10DTFloat:
		if (0 == SIZE_OF_X10DT_Float) {
			SIZE_OF_X10DT_Float = GetSizeOf(session, X10DT_Float_CPP_TYPE);
		}
		elementSize = SIZE_OF_X10DT_Float;
		break;
	case X10DTDouble:
		if (0 == SIZE_OF_X10DT_Double) {
			SIZE_OF_X10DT_Double = GetSizeOf(session, X10DT_Double_CPP_TYPE);
		}
		elementSize = SIZE_OF_X10DT_Double;
		break;
	case X10DTBoolean:
		if (0 == SIZE_OF_X10DT_Boolean) {
			SIZE_OF_X10DT_Boolean = GetSizeOf(session, X10DT_Boolean_CPP_TYPE);
		}
		elementSize = SIZE_OF_X10DT_Boolean;
		break;
	case X10DTUByte:
		if (0 == SIZE_OF_X10DT_UByte) {
			SIZE_OF_X10DT_UByte = GetSizeOf(session, X10DT_Boolean_CPP_TYPE);
		}
		elementSize = SIZE_OF_X10DT_UByte;
		break;
	case X10DTUInt:
		if (0 == SIZE_OF_X10DT_UInt) {
			SIZE_OF_X10DT_UInt = GetSizeOf(session, X10DT_UInt_CPP_TYPE);
		}
		elementSize = SIZE_OF_X10DT_UInt;
		break;
	case X10DTULong:
		if (0 == SIZE_OF_X10DT_ULong) {
			SIZE_OF_X10DT_ULong = GetSizeOf(session, X10DT_ULong_CPP_TYPE);
		}
		elementSize = SIZE_OF_X10DT_ULong;
		break;
	case X10DTUShort:
		if (0 == SIZE_OF_X10DT_UShort) {
			SIZE_OF_X10DT_UShort = GetSizeOf(session, X10DT_UShort_CPP_TYPE);
		}
		elementSize = SIZE_OF_X10DT_UShort;
		break;
	case X10DTString:
		if (0 == SIZE_OF_POINTER) {
			SIZE_OF_POINTER = GetAddressLength(session);
		}
		elementDataAddress = rawDataAddress + elementIndex * SIZE_OF_POINTER;
		char *addressString = (char*)malloc(20);
		sprintf(addressString, "0x%llX", elementDataAddress);
		result = readValueFromMemory(session, addressString, SIZE_OF_POINTER);
		return result;
	default:
		if (0 == SIZE_OF_X10DT_Int)
		{
			SIZE_OF_X10DT_Int = GetSizeOf(session, X10DT_Int_CPP_TYPE);
		}
		elementSize = SIZE_OF_X10DT_Int;
		break;
	}
	elementDataAddress = rawDataAddress + elementIndex * elementSize;
	//Now, need to convert it into a string which represent this number in decimal
	result = (char*)malloc(20);
	sprintf(result, "0x%llX", elementDataAddress);

	return result;
}

/*
 * Return the type of the element of an X10 Array variable.
 */
int 
ElementType(x10variable_t *var)
{
    int typeIndex = var->type_index;
    int mapIndex = var->map_index;
    if (NULL == arrayElementTypeMap) {
    	return X10DTNull;
    }
    if (typeIndex != -1 && mapIndex == -1 && arrayElementTypeMapSize > typeIndex) {
    	_x10_array_map *p = arrayElementTypeMap + typeIndex;
    	return p->x10_type;
    }
    return X10DTNull;
}

/*
 * Return the element of an X10 Array variable.
 */
int 
element(x10variable_t *var, void** data, int* length)
{
	return 0;
}
