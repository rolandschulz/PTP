/*
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
 
#include "config.h"

#include "gdb.h"
#include "x10/x10_metadebug_info_map.h"

//#include "x10_variable.h"

#define NUM_FIELDS_LINE_INFO                5
#define NUM_FIELDS_VAR_INFO                 4
#define NUM_FIELDS_IN_TYPE_MEMBER 			5
#define NUM_FIELDS_BEFORE_IN_CLASS_MAP 		4
#define NUM_FIELDS_BEFORE_IN_CLOSURE_MAP 	7

#define CLASS_MEMBER_DEREFERENCE_STR 		"._val->"
#define POINTER_DEREFERENCE_STR 		 	"->"

static List *			X10_MetaDebugInfoList = NULL;
static int 				numFieldsVarInfo = NUM_FIELDS_VAR_INFO;

/*
 * Read x10String buffer of the MetaDebug record.
 */
static void 
getX10String(MISession *session, _x10_meta_debug_info_t* x10_MetaDebugInfo, char* x10StringAddress)
{
	memory 		*mem_X10Strings = NULL;
	memoryinfo  *memInfo_X10Strings = NULL;
	char * tmpData = NULL;

	if (x10_MetaDebugInfo->x10_string_size <= 0) {
		x10_MetaDebugInfo->x10_strings = NULL;
		return;
	}
	
	x10_MetaDebugInfo->x10_strings = (char*)malloc(x10_MetaDebugInfo->x10_string_size * sizeof(char) + 1);
	char *pX10Strings = x10_MetaDebugInfo->x10_strings;
	memInfo_X10Strings = GetMemoryInfo(session, 0, x10StringAddress, "x", 1, 1, x10_MetaDebugInfo->x10_string_size, NULL);
	for (SetList(memInfo_X10Strings->memories); (mem_X10Strings = (memory*)GetListElement(memInfo_X10Strings->memories)) != NULL;) {
		for (SetList(mem_X10Strings->data); (tmpData = (char *)GetListElement(mem_X10Strings->data)) != NULL;) {
			uint32_t charValue_32 = strtoul(tmpData, NULL, 16);
			uint8_t  charValue_8 = charValue_32;
			memcpy(pX10Strings, &charValue_8, 1); /*only copy the last byte*/
			pX10Strings++;
		}
	}
	FreeMemoryInfo(memInfo_X10Strings);
	memInfo_X10Strings = NULL;
	mem_X10Strings = NULL;
	return;
}

/*
 * Read x10SourceList of the MetaDebug record.
 */
static void 
getX10SourceList(MISession *session, _x10_meta_debug_info_t* x10_MetaDebugInfo, char* x10sourceListAddress)
{
	memory 		*mem_X10sourceList = NULL;
	memoryinfo  *memInfo_X10sourceList = NULL;
	char * tmpData = NULL;
	int sizeOfUint32_t = sizeof(uint32_t);

	if (x10_MetaDebugInfo->x10_source_list_size <= 0) {
		x10_MetaDebugInfo->x10_source_array_size = 0;
		x10_MetaDebugInfo->x10_source_list = NULL;
		return;
	}
	
	int numSourceFiles = x10_MetaDebugInfo->x10_source_list_size / sizeof(_x10source_file_t);
	int numRows = numSourceFiles;
	x10_MetaDebugInfo->x10_source_array_size = numSourceFiles;
	int numColumn = sizeof(_x10source_file_t) / sizeOfUint32_t;
	x10_MetaDebugInfo->x10_source_list = (_x10source_file_t*)malloc(numSourceFiles * sizeof(_x10source_file_t));
	_x10source_file_t *pX10sourceList = x10_MetaDebugInfo->x10_source_list;
	memInfo_X10sourceList= GetMemoryInfo(session, 0, x10sourceListAddress, "x", sizeOfUint32_t, numRows, numColumn, NULL);
	for (SetList(memInfo_X10sourceList->memories); (mem_X10sourceList = (memory*)GetListElement(memInfo_X10sourceList->memories)) != NULL;) {
		SetList(mem_X10sourceList->data);
		tmpData = (char *)GetListElement(mem_X10sourceList->data);
		uint32_t value = strtoul(tmpData, NULL, 16);
		pX10sourceList->string_index = value;
		
		tmpData = (char *)GetListElement(mem_X10sourceList->data);
		value = strtoul(tmpData, NULL, 16);
		pX10sourceList->string_index = value;
		
		pX10sourceList++;
	}
	FreeMemoryInfo(memInfo_X10sourceList);
	memInfo_X10sourceList = NULL;
	mem_X10sourceList = NULL;

	return;
}

/*
 * Read x10 to C++ mapping records of the MetaDebug record.
 */
static void 
getX10ToCPPList(MISession *session, _x10_meta_debug_info_t* x10_MetaDebugInfo, char *x10toCPPListAddress)
{
	memory 		*mem_X10ToCPPList = NULL;
	memoryinfo  *memInfo_X10ToCPPList = NULL;
	char * tmpData = NULL;
	int sizeOfUint16_t = sizeof(uint16_t);
	int sizeOfUint32_t = sizeof(uint32_t);
	uint16_t value_16_bits;
	int offset;

	if (x10_MetaDebugInfo->x10_to_cpp_list_size <= 0) {
		x10_MetaDebugInfo->x10_to_cpp_array_size = 0;
		x10_MetaDebugInfo->x10_to_cpp_list = NULL;
		return;
	}
	
	int numX10ToCPPRecords = x10_MetaDebugInfo->x10_to_cpp_list_size / sizeof(_x10_to_cpp_xref_t);
	int numRows = numX10ToCPPRecords;
	x10_MetaDebugInfo->x10_to_cpp_array_size = numX10ToCPPRecords;
	x10_MetaDebugInfo->x10_to_cpp_list = (_x10_to_cpp_xrefso_t*)malloc(numX10ToCPPRecords * sizeof(_x10_to_cpp_xrefso_t));
	_x10_to_cpp_xrefso_t *pX10ToCPPList = x10_MetaDebugInfo->x10_to_cpp_list;
	int innerIndex = 0;
	for (innerIndex = 0; innerIndex < numRows; innerIndex++) {
		offset = innerIndex * sizeof(_x10_to_cpp_xref_t);
		//first, get x10_index and x10_method, both of them are of size uint16_t
		memInfo_X10ToCPPList = GetMemoryInfo(session, offset, x10toCPPListAddress, "x", sizeOfUint16_t, 1, 2, NULL);
		SetList(memInfo_X10ToCPPList->memories);
		mem_X10ToCPPList = (memory*)GetListElement(memInfo_X10ToCPPList->memories);
		SetList(mem_X10ToCPPList->data);
		/* for uint16_t x10_index; */
		tmpData = (char *)GetListElement(mem_X10ToCPPList->data);
		value_16_bits = strtoul(tmpData, NULL, 16);
		pX10ToCPPList->x10_to_cpp_ref.x10_index = value_16_bits;
		/* for uint16_t x10_method */
		tmpData = (char *)GetListElement(mem_X10ToCPPList->data);
		value_16_bits = strtoul(tmpData, NULL, 16);
		pX10ToCPPList->x10_to_cpp_ref.x10_method = value_16_bits;

		FreeMemoryInfo(memInfo_X10ToCPPList);
		memInfo_X10ToCPPList = NULL;
		mem_X10ToCPPList = NULL;

		//Now, get the rest 3 fields, and all of them of uint32_t
		offset = offset + 2 * sizeOfUint16_t;
		memInfo_X10ToCPPList = GetMemoryInfo(session, offset, x10toCPPListAddress, "x", sizeOfUint32_t, 1, 4, NULL);
		SetList(memInfo_X10ToCPPList->memories);
		mem_X10ToCPPList= (memory*)GetListElement(memInfo_X10ToCPPList->memories);
		SetList(mem_X10ToCPPList->data);
		/* for uint32_t cpp_index */
		tmpData = (char *)GetListElement(mem_X10ToCPPList->data);
		uint32_t value = strtoul(tmpData, NULL, 16);
		pX10ToCPPList->x10_to_cpp_ref.cpp_index = value;
		/* for uint32_t x10_line */
		tmpData = (char *)GetListElement(mem_X10ToCPPList->data);
		value = strtoul(tmpData, NULL, 16);
		pX10ToCPPList->x10_to_cpp_ref.x10_line = value;
		/* for uint32_t cpp_from_line */
		tmpData = (char *)GetListElement(mem_X10ToCPPList->data);
		value = strtoul(tmpData, NULL, 16);
		pX10ToCPPList->x10_to_cpp_ref.cpp_from_line= value;
		/* for uint32_t _CPPToLine */
		tmpData = (char *)GetListElement(mem_X10ToCPPList->data);
		value = strtoul(tmpData, NULL, 16);
		pX10ToCPPList->x10_to_cpp_ref.cpp_to_line = value;
		pX10ToCPPList->first_step_over_line = 0;

		FreeMemoryInfo(memInfo_X10ToCPPList);
		memInfo_X10ToCPPList = NULL;
		mem_X10ToCPPList= NULL;
		pX10ToCPPList++;
	}

	return;
			
}

/*
 * Read C++ to X10 mapping records of the MetaDebug record.
 */
static void 
getCPPToX10List(MISession *session, _x10_meta_debug_info_t* x10_MetaDebugInfo, char *CPPtoX10xrefListAddress )
{
	memory 		*mem_CPPToX10List = NULL;
	memoryinfo  *memInfo_CPPToX10List = NULL;
	char * tmpData = NULL;
	int sizeOfUint16_t = sizeof(uint16_t);
	int sizeOfUint32_t = sizeof(uint32_t);
	uint16_t value_16_bits;
	int offset;

	if (x10_MetaDebugInfo->cpp_to_x10_xref_list_size <= 0) {
		x10_MetaDebugInfo->cpp_to_x10_xref_array_size = 0;
		x10_MetaDebugInfo->cpp_to_x10_xref_list = NULL;
		return;
	}
	
	int numCPPToX10Records = x10_MetaDebugInfo->cpp_to_x10_xref_list_size / sizeof(_cpp_to_x10_xref_t);
	int numRows = numCPPToX10Records;
	x10_MetaDebugInfo->cpp_to_x10_xref_array_size = numCPPToX10Records;
	x10_MetaDebugInfo->cpp_to_x10_xref_list = (_cpp_to_x10_xref_t*)malloc(numCPPToX10Records * sizeof(_cpp_to_x10_xref_t));
	_cpp_to_x10_xref_t *pCPPToX10List = x10_MetaDebugInfo->cpp_to_x10_xref_list;
	int innerIndex = 0;
	for (innerIndex = 0; innerIndex < numRows; innerIndex++) {
		offset = innerIndex * sizeof(_cpp_to_x10_xref_t);
		//first, get x10_index and x10_method, both of them are of size uint16_t
		memInfo_CPPToX10List = GetMemoryInfo(session, offset, CPPtoX10xrefListAddress, "x", sizeOfUint16_t, 1, 2, NULL);
		SetList(memInfo_CPPToX10List->memories);
		mem_CPPToX10List = (memory*)GetListElement(memInfo_CPPToX10List->memories);
		SetList(mem_CPPToX10List->data);
		/* for uint16_t x10_index; */
		tmpData = (char *)GetListElement(mem_CPPToX10List->data);
		value_16_bits = strtoul(tmpData, NULL, 16);
		pCPPToX10List->x10_index = value_16_bits;
		/* for uint16_t x10_method */
		tmpData = (char *)GetListElement(mem_CPPToX10List->data);
		value_16_bits = strtoul(tmpData, NULL, 16);
		pCPPToX10List->x10_method = value_16_bits;
		FreeMemoryInfo(memInfo_CPPToX10List);
		memInfo_CPPToX10List = NULL;
		mem_CPPToX10List = NULL;

		//Now, get the rest 4 fields, and all of them of uint32_t
		offset = offset + 2 * sizeOfUint16_t;
		memInfo_CPPToX10List = GetMemoryInfo(session, offset, CPPtoX10xrefListAddress, "x", sizeOfUint32_t, 1, 4, NULL);
		SetList(memInfo_CPPToX10List->memories);
		mem_CPPToX10List = (memory*)GetListElement(memInfo_CPPToX10List->memories);
		SetList(mem_CPPToX10List->data);
		/* for uint32_t cpp_index */
		tmpData = (char *)GetListElement(mem_CPPToX10List->data);
		uint32_t value = strtoul(tmpData, NULL, 16);
		pCPPToX10List->cpp_index = value;
		/* for uint32_t x10_line */
		tmpData = (char *)GetListElement(mem_CPPToX10List->data);
		value = strtoul(tmpData, NULL, 16);
		pCPPToX10List->x10_line = value;
		/* for uint32_t _CPPfromline */
		tmpData = (char *)GetListElement(mem_CPPToX10List->data);
		value = strtoul(tmpData, NULL, 16);
		pCPPToX10List->cpp_from_line = value;
		/* for uint32_t cpp_to_line */
		tmpData = (char *)GetListElement(mem_CPPToX10List->data);
		value = strtoul(tmpData, NULL, 16);
		pCPPToX10List->cpp_to_line = value;

		FreeMemoryInfo(memInfo_CPPToX10List);
		memInfo_CPPToX10List = NULL;
		mem_CPPToX10List = NULL;
		
		pCPPToX10List++;
	}

	return;
}

/*
 * Read x10 method records of the MetaDebug record.
 */
static void 
getX10MethodRecords(MISession *session, _x10_meta_debug_info_t* x10_MetaDebugInfo, char* x10methodNameListAddress)
{
	memory 		*mem_X10methodNameList = NULL;
	memoryinfo  *memInfo_X10methodNameList = NULL;
	char * tmpData = NULL;
	uint16_t value_16_bits;
	int offset;
	int sizeOfUint32_t = sizeof(uint32_t);
	int sizeOfUint64_t = sizeof(uint64_t);
	int sizeOfUint16_t = sizeof(uint16_t);
	int innerIndex;

	if (x10_MetaDebugInfo->x10_method_name_list_size <= 0) {
		x10_MetaDebugInfo->x10_method_mame_array_size = 0;
		x10_MetaDebugInfo->x10_method_list = NULL;
		return;
	}
	
	int numX10methodNameRecords = x10_MetaDebugInfo->x10_method_name_list_size / sizeof(_x10_method_name_t);
	int numRows = numX10methodNameRecords;
	x10_MetaDebugInfo->x10_method_mame_array_size = numX10methodNameRecords;
	x10_MetaDebugInfo->x10_method_list = (_x10_method_t*)malloc(numX10methodNameRecords * sizeof(_x10_method_t));
	_x10_method_t *pX10MethodList = (_x10_method_t*)(x10_MetaDebugInfo->x10_method_list);
			
	for (innerIndex = 0; innerIndex < numRows; innerIndex++) {
		offset = innerIndex * sizeof(_x10_method_name_t);
		//First, get x10_class, x10_method and x10_return_type.  All of them of uint32_t
		memInfo_X10methodNameList = GetMemoryInfo(session, offset, x10methodNameListAddress, "x", sizeOfUint32_t, 1, 4, NULL);
		SetList(memInfo_X10methodNameList->memories);
		mem_X10methodNameList = (memory*)GetListElement(memInfo_X10methodNameList->memories);
		SetList(mem_X10methodNameList->data);

		/* for uint32_t x10_class */
		tmpData = (char *)GetListElement(mem_X10methodNameList->data);
		uint32_t value = strtoul(tmpData, NULL, 16);
		pX10MethodList->method_name.x10_class = value;
				
		/* for uint32_t _x10method */
		tmpData = (char *)GetListElement(mem_X10methodNameList->data);
		value = strtoul(tmpData, NULL, 16);
		pX10MethodList->method_name.x10_method = value;

		/* for uint32_t x10_return_type */
		tmpData = (char *)GetListElement(mem_X10methodNameList->data);
		value = strtoul(tmpData, NULL, 16);
		pX10MethodList->method_name.x10_return_type = value;

		/* for uint32_t cpp_class */
		tmpData = (char *)GetListElement(mem_X10methodNameList->data);
		value = strtoul(tmpData, NULL, 16);
		pX10MethodList->method_name.cpp_class = value;

		FreeMemoryInfo(memInfo_X10methodNameList);
		memInfo_X10methodNameList = NULL;
		mem_X10methodNameList = NULL;
				
		/* for uint64_t x10_args */
		offset = offset + sizeOfUint32_t * 4;
		memInfo_X10methodNameList = GetMemoryInfo(session, offset, x10methodNameListAddress, "x", sizeOfUint64_t, 1, 1, NULL);
		SetList(memInfo_X10methodNameList->memories);
		mem_X10methodNameList = (memory*)GetListElement(memInfo_X10methodNameList->memories);
		SetList(mem_X10methodNameList->data);
		tmpData = (char *)GetListElement(mem_X10methodNameList->data);
		uint64_t value64 = strtoull(tmpData, NULL, 16);
		pX10MethodList->method_name.x10_args = value64;
		FreeMemoryInfo(memInfo_X10methodNameList);
		memInfo_X10methodNameList = NULL;
		mem_X10methodNameList = NULL;
			

		/* for uint16_t x10_arg_count and uint16_t line_index */
		offset = offset + sizeOfUint64_t;
		memInfo_X10methodNameList = GetMemoryInfo(session, offset, x10methodNameListAddress, "x", sizeOfUint16_t, 1, 2, NULL);
		SetList(memInfo_X10methodNameList->memories);
		mem_X10methodNameList = (memory*)GetListElement(memInfo_X10methodNameList->memories);
		SetList(mem_X10methodNameList->data);
		/* for x10_arg_count */
		tmpData = (char *)GetListElement(mem_X10methodNameList->data);
		value_16_bits = strtoul(tmpData, NULL, 16);
		pX10MethodList->method_name.x10_arg_count = value_16_bits;
		/* for line_index */
		tmpData = (char *)GetListElement(mem_X10methodNameList->data);
		value_16_bits = strtoul(tmpData, NULL, 16);
		pX10MethodList->method_name.line_index = value_16_bits;
		FreeMemoryInfo(memInfo_X10methodNameList);
		memInfo_X10methodNameList = NULL;
		mem_X10methodNameList = NULL;

		if (pX10MethodList->method_name.line_index < x10_MetaDebugInfo->cpp_to_x10_xref_array_size) {
			_cpp_to_x10_xref_t *cppToX10Ref = x10_MetaDebugInfo->cpp_to_x10_xref_list + pX10MethodList->method_name.line_index;
			pX10MethodList->cpp_from_line = cppToX10Ref->cpp_from_line;
  			pX10MethodList->cpp_to_line = cppToX10Ref->cpp_to_line;
  			pX10MethodList->cpp_index = cppToX10Ref->cpp_index;
		}
				
		pX10MethodList++;
	}

	return;
}

/*
 * Read x10 local variable mapping records of the MetaDebug record.
 */
static void 
getX10LocalVarList(MISession *session, _x10_meta_debug_info_t* x10_MetaDebugInfo, char* x10localVarListAddress )
{
	memory 		*mem_X10LocalVariableList = NULL;
	memoryinfo  *memInfo_X10LocalVariableList = NULL;
	char * tmpData = NULL;

	if (x10_MetaDebugInfo->x10_local_var_list_size <= 0) {
		x10_MetaDebugInfo->x10_local_var_array_size = 0;
		x10_MetaDebugInfo->x10_local_var_list = NULL;
		return;
	}
	
	int numX10localVarRecords = x10_MetaDebugInfo->x10_local_var_list_size / sizeof(_x10_local_var_map);
	int numRows = numX10localVarRecords;
	x10_MetaDebugInfo->x10_local_var_array_size = numX10localVarRecords;
	int numColumn = sizeof(_x10_local_var_map) / sizeof(uint32_t);
	x10_MetaDebugInfo->x10_local_var_list = (_x10_local_var_map*)malloc(numX10localVarRecords * sizeof(_x10_local_var_map));
	_x10_local_var_map *pX10LocalVariaList = (_x10_local_var_map*)(x10_MetaDebugInfo->x10_local_var_list);
	memInfo_X10LocalVariableList = GetMemoryInfo(session, 0, x10localVarListAddress, "x", sizeof(uint32_t), numRows, numColumn, NULL);
	for (SetList(memInfo_X10LocalVariableList->memories); (mem_X10LocalVariableList = (memory*)GetListElement(memInfo_X10LocalVariableList->memories)) != NULL;) {
		SetList(mem_X10LocalVariableList->data);
		
		/* for uint32_t x10_name */
		tmpData = (char *)GetListElement(mem_X10LocalVariableList->data);
		uint32_t value = strtoul(tmpData, NULL, 16);
		int32_t valueSigned = strtoul(tmpData, NULL, 16);
		pX10LocalVariaList->x10_name = value;
				
		/* for uint32_t x10_type */
		tmpData = (char *)GetListElement(mem_X10LocalVariableList->data);
		value = strtoul(tmpData, NULL, 16);
		pX10LocalVariaList->x10_type = value;

		/* for int32_t x10_type_index */
		tmpData = (char *)GetListElement(mem_X10LocalVariableList->data);
		valueSigned = strtol(tmpData, NULL, 16);
		pX10LocalVariaList->x10_type_index = valueSigned;

		/* for uint32_t cpp_name */
		tmpData = (char *)GetListElement(mem_X10LocalVariableList->data);
		value = strtoul(tmpData, NULL, 16);
		pX10LocalVariaList->cpp_name = value;

		/* for uint32_t x10_index */
		tmpData = (char *)GetListElement(mem_X10LocalVariableList->data);
		value = strtoul(tmpData, NULL, 16);
		pX10LocalVariaList->x10_index = value;

		/* for uint32_t x10_start_line */
		tmpData = (char *)GetListElement(mem_X10LocalVariableList->data);
		value = strtoul(tmpData, NULL, 16);
		pX10LocalVariaList->x10_start_line = value;

		/* for uint32_t x10_end_line */
		tmpData = (char *)GetListElement(mem_X10LocalVariableList->data);
		value = strtoul(tmpData, NULL, 16);
		pX10LocalVariaList->x10_end_line = value;
		
		pX10LocalVariaList++;
	}
	FreeMemoryInfo(memInfo_X10LocalVariableList);
	memInfo_X10LocalVariableList = NULL;
	mem_X10LocalVariableList = NULL;

	return;
}

/*
 * Read member list of an X10 class defined in MetaDebug record.
 */
static void 
getTypeMemberList(MISession *session, char* x10TypeMemberAddress, int memberCount, _x10_type_member *typeMemberPointer)
{
	memory 		*mem_X10TypeMapList = NULL;
	memoryinfo  *memInfo_X10TypeMapList = NULL;
	char * tmpData = NULL;
	
	memInfo_X10TypeMapList = GetMemoryInfo(session, 0, x10TypeMemberAddress, "x", sizeof(uint32_t), memberCount, NUM_FIELDS_IN_TYPE_MEMBER, NULL);	
	for (SetList(memInfo_X10TypeMapList->memories); (mem_X10TypeMapList = (memory*)GetListElement(memInfo_X10TypeMapList->memories)) != NULL;) {
		SetList(mem_X10TypeMapList->data);
		/* for uint32_t x10_type */
		tmpData = (char *)GetListElement(mem_X10TypeMapList->data);
		uint32_t value = strtoul(tmpData, NULL, 16);
		typeMemberPointer->x10_type = value;

		/* for int32_t x10_type_index */
		tmpData = (char *)GetListElement(mem_X10TypeMapList->data);
		int32_t valueSigned = strtol(tmpData, NULL, 16);
		typeMemberPointer->x10_type_index = valueSigned;
					
		/* for uint32_t x10_member_name */
		tmpData = (char *)GetListElement(mem_X10TypeMapList->data);
		value = strtoul(tmpData, NULL, 16);
		typeMemberPointer->x10_member_name = value;
					
		/* for uint32_t cpp_member_name */
		tmpData = (char *)GetListElement(mem_X10TypeMapList->data);
		value = strtoul(tmpData, NULL, 16);
		typeMemberPointer->cpp_member_name = value;

		/* for uint32_t cpp_class */
		tmpData = (char *)GetListElement(mem_X10TypeMapList->data);
		value = strtoul(tmpData, NULL, 16);
		typeMemberPointer->cpp_class = value;
					
		typeMemberPointer++;
	}
	FreeMemoryInfo(memInfo_X10TypeMapList);
	memInfo_X10TypeMapList = NULL;
	mem_X10TypeMapList = NULL;
	
}

/*
 * Read member definition of an X10 closure class from MetaDebug record.
 */
static void 
getX10ClosureMapList(MISession *session, _x10_meta_debug_info_t* x10_MetaDebugInfo, char* x10ClosureMapListAddress)
{
	memory 		*mem_X10TypeMapList = NULL;
	memoryinfo  *memInfo_X10TypeMapList = NULL;
	int sizeOfPointer = GetAddressLength(session);
	int sizeOfUint32_t = sizeof(uint32_t);
	char * tmpData = NULL;
	int  index = 0;
	int  offset = 0;

	if (x10_MetaDebugInfo->x10_closure_map_list_size <= 0) {
		x10_MetaDebugInfo->x10_closure_map_array_size = 0;
		x10_MetaDebugInfo->x10_closure_map_list = NULL;
		return;
	}
	
	//Elements in this list could be different size.  So we need to process depending on 
	//the type of each entry of this list.
	//We can guarantee the first field of each entry is the type.

	//TODO: this could be potential problem if debugger and debuggee running
	//in different memory bits.  Need to address.
	int numX10ClosureMapListRecord = x10_MetaDebugInfo->x10_closure_map_list_size / sizeof(_x10_closure_map);
	x10_MetaDebugInfo->x10_closure_map_array_size = numX10ClosureMapListRecord;
	x10_MetaDebugInfo->x10_closure_map_list = (_x10_closure_map*)malloc(numX10ClosureMapListRecord * sizeof(_x10_closure_map));
	_x10_closure_map *pClosureMapList = x10_MetaDebugInfo->x10_closure_map_list;
	for (index = 0; index < numX10ClosureMapListRecord; index++) {
		offset = index * (NUM_FIELDS_BEFORE_IN_CLOSURE_MAP * sizeOfUint32_t + sizeOfPointer);
		char *x10TypeMemberAddress = NULL;
		_x10_type_member *tmpPointer = NULL;
		
		memInfo_X10TypeMapList = GetMemoryInfo(session, offset, x10ClosureMapListAddress, "x", sizeOfUint32_t, 1, NUM_FIELDS_BEFORE_IN_CLOSURE_MAP, NULL);	
		SetList(memInfo_X10TypeMapList->memories);
		mem_X10TypeMapList = (memory*)GetListElement(memInfo_X10TypeMapList->memories);
		SetList(mem_X10TypeMapList->data);
		//get x10_type
		tmpData = (char *)GetListElement(mem_X10TypeMapList->data);
		uint32_t value = strtoul(tmpData, NULL, 16);
		pClosureMapList->x10_type = value;
		//Get x10_name
		tmpData = (char *)GetListElement(mem_X10TypeMapList->data);
		value = strtoul(tmpData, NULL, 16);
		pClosureMapList->x10_name = value;
		//Get x10_size
		tmpData = (char *)GetListElement(mem_X10TypeMapList->data);
		value = strtoul(tmpData, NULL, 16);
		pClosureMapList->x10_size = value;
		//Get x10_member_count
		tmpData = (char *)GetListElement(mem_X10TypeMapList->data);
		value = strtoul(tmpData, NULL, 16);
		pClosureMapList->x10_member_count = value;
		//Get x10_index
		tmpData = (char *)GetListElement(mem_X10TypeMapList->data);
		value = strtoul(tmpData, NULL, 16);
		pClosureMapList->x10_index = value;
		//Get x10_start_line
		tmpData = (char *)GetListElement(mem_X10TypeMapList->data);
		value = strtoul(tmpData, NULL, 16);
		pClosureMapList->x10_start_line = value;
		//Get x10_end_line
		tmpData = (char *)GetListElement(mem_X10TypeMapList->data);
		value = strtoul(tmpData, NULL, 16);
		pClosureMapList->x10_end_line = value;
		FreeMemoryInfo(memInfo_X10TypeMapList);
		memInfo_X10TypeMapList = NULL;
		mem_X10TypeMapList = NULL;
		//Get the x10_members address
		if (sizeOfPointer < 8) {
			offset = offset + sizeOfUint32_t*NUM_FIELDS_BEFORE_IN_CLOSURE_MAP;
		}
		else {
			offset = offset + 4 + sizeOfUint32_t*NUM_FIELDS_BEFORE_IN_CLOSURE_MAP;
		}
		memInfo_X10TypeMapList = GetMemoryInfo(session, offset, x10ClosureMapListAddress, "x", sizeOfPointer, 1, 1, NULL);	
		SetList(memInfo_X10TypeMapList->memories);
		mem_X10TypeMapList = (memory*)GetListElement(memInfo_X10TypeMapList->memories);
		SetList(mem_X10TypeMapList->data);
		x10TypeMemberAddress = strdup((char *)GetListElement(mem_X10TypeMapList->data));
		pClosureMapList->x10_members = (_x10_type_member*)malloc(pClosureMapList->x10_member_count * sizeof(_x10_type_member));
		tmpPointer = pClosureMapList->x10_members;
		FreeMemoryInfo(memInfo_X10TypeMapList);
		memInfo_X10TypeMapList = NULL;
		mem_X10TypeMapList = NULL;
		//Now, read the x10_members information
		getTypeMemberList(session, x10TypeMemberAddress, pClosureMapList->x10_member_count, tmpPointer);
		pClosureMapList++;		
	}	
}

/*
 * Read member definition of an X10 user defined class from MetaDebug record.
 */
static void 
getX10ClassMapList(MISession *session, _x10_meta_debug_info_t* x10_MetaDebugInfo, char* x10ClassMapListAddress)
{
	memory 		*mem_X10TypeMapList = NULL;
	memoryinfo  *memInfo_X10TypeMapList = NULL;
	int sizeOfPointer = GetAddressLength(session);
	int sizeOfUint32_t = sizeof(uint32_t);
	char * tmpData = NULL;
	int  index = 0;
	int  offset = 0;

	if (x10_MetaDebugInfo->x10_class_map_list_size <= 0) {
		x10_MetaDebugInfo->x10_class_map_array_size = 0;
		x10_MetaDebugInfo->x10_class_map_list = NULL;
		return;
	}
	
	//Elements in this list could be different size.  So we need to process depending on 
	//the type of each entry of this list.
	//We can guarantee the first field of each entry is the type.

	//TODO: this could be potential problem if debugger and debuggee running
	//in different memory bits.  Need to address.
	int numX10ClassMapListRecord = x10_MetaDebugInfo->x10_class_map_list_size / sizeof(_x10_class_map);
	x10_MetaDebugInfo->x10_class_map_array_size = numX10ClassMapListRecord;
	x10_MetaDebugInfo->x10_class_map_list = (_x10_class_map*)malloc(numX10ClassMapListRecord * sizeof(_x10_class_map));
	_x10_class_map *pClassMapList = x10_MetaDebugInfo->x10_class_map_list;
	for (index = 0; index < numX10ClassMapListRecord; index++) {
		offset = index * (NUM_FIELDS_BEFORE_IN_CLASS_MAP * sizeOfUint32_t + sizeOfPointer);
		char *x10TypeMemberAddress = NULL;
		_x10_type_member *tmpPointer = NULL;
		
		memInfo_X10TypeMapList = GetMemoryInfo(session, offset, x10ClassMapListAddress, "x", sizeOfUint32_t, 1, NUM_FIELDS_BEFORE_IN_CLASS_MAP, NULL);	
		SetList(memInfo_X10TypeMapList->memories);
		mem_X10TypeMapList = (memory*)GetListElement(memInfo_X10TypeMapList->memories);
		SetList(mem_X10TypeMapList->data);
		//get x10_type
		tmpData = (char *)GetListElement(mem_X10TypeMapList->data);
		uint32_t value = strtoul(tmpData, NULL, 16);
		pClassMapList->x10_type = value;
		//Get x10_name
		tmpData = (char *)GetListElement(mem_X10TypeMapList->data);
		value = strtoul(tmpData, NULL, 16);
		pClassMapList->x10_name = value;
		//Get x10_size
		tmpData = (char *)GetListElement(mem_X10TypeMapList->data);
		value = strtoul(tmpData, NULL, 16);
		pClassMapList->x10_size = value;
		//Get _x10ClassMemberCount
		tmpData = (char *)GetListElement(mem_X10TypeMapList->data);
		value = strtoul(tmpData, NULL, 16);
		pClassMapList->x10_member_count = value;
		FreeMemoryInfo(memInfo_X10TypeMapList);
		memInfo_X10TypeMapList = NULL;
		mem_X10TypeMapList = NULL;
		//Get the _x10TypeMember address
		memInfo_X10TypeMapList = GetMemoryInfo(session, offset+(sizeOfUint32_t*NUM_FIELDS_BEFORE_IN_CLASS_MAP), x10ClassMapListAddress, "x", sizeOfPointer, 1, 1, NULL);	
		SetList(memInfo_X10TypeMapList->memories);
		mem_X10TypeMapList = (memory*)GetListElement(memInfo_X10TypeMapList->memories);
		SetList(mem_X10TypeMapList->data);
		x10TypeMemberAddress = strdup((char *)GetListElement(mem_X10TypeMapList->data));
		pClassMapList->x10_members = (_x10_type_member*)malloc(pClassMapList->x10_member_count * sizeof(_x10_type_member));
		tmpPointer = pClassMapList->x10_members;
		FreeMemoryInfo(memInfo_X10TypeMapList);
		memInfo_X10TypeMapList = NULL;
		mem_X10TypeMapList = NULL;
		//Now, read the x10_members information
		getTypeMemberList(session, x10TypeMemberAddress, pClassMapList->x10_member_count, tmpPointer);
		pClassMapList++;		
	}	
}

/*
 * Read Array mapping records from MetaDebug record.
 */
static void 
getX10ArrayMapList(MISession *session, _x10_meta_debug_info_t* x10_MetaDebugInfo, char* x10ArrayMapListAddress )
{
	memory 		*mem_X10ArrayMapList = NULL;
	memoryinfo  *memInfo_X10ArrayMapList = NULL;
	char * tmpData = NULL;

	if (x10_MetaDebugInfo->x10_closure_map_list_size <= 0) {
		x10_MetaDebugInfo->x10_array_map_array_size = 0;
		x10_MetaDebugInfo->x10_array_map_list = NULL;
		return;
	}
	
	int numX10ArrayMapRecords = x10_MetaDebugInfo->x10_closure_map_list_size / sizeof(_x10_array_map);
	int numRows = numX10ArrayMapRecords;
	x10_MetaDebugInfo->x10_array_map_array_size = numX10ArrayMapRecords;
	int numColumn = sizeof(_x10_array_map) / sizeof(uint32_t);
	x10_MetaDebugInfo->x10_array_map_list = (_x10_array_map*)malloc(numX10ArrayMapRecords * sizeof(_x10_array_map));
	_x10_array_map *pX10ArrayMapList = (_x10_array_map*)(x10_MetaDebugInfo->x10_array_map_list);
	memInfo_X10ArrayMapList = GetMemoryInfo(session, 0, x10ArrayMapListAddress, "x", sizeof(uint32_t), numRows, numColumn, NULL);
	for (SetList(memInfo_X10ArrayMapList->memories); (mem_X10ArrayMapList = (memory*)GetListElement(memInfo_X10ArrayMapList->memories)) != NULL;) {
		SetList(mem_X10ArrayMapList->data);
		/* for uint32_t x10_type */
		tmpData = (char *)GetListElement(mem_X10ArrayMapList->data);
		uint32_t value = strtoul(tmpData, NULL, 16);
		pX10ArrayMapList->x10_type = value;
		
		/* for int32_t x10_type_index */
		tmpData = (char *)GetListElement(mem_X10ArrayMapList->data);
		int32_t valueSigned = strtol(tmpData, NULL, 16);
		pX10ArrayMapList->x10_type_index = valueSigned;
		
		pX10ArrayMapList++;
	}
	FreeMemoryInfo(memInfo_X10ArrayMapList);
	memInfo_X10ArrayMapList = NULL;
	mem_X10ArrayMapList = NULL;

	return;
}

/*
 * Read MetaDebug information record, and build up the X10<->C++ mapping table.
 */
void 
X10MetaDebugInfoMapCreate64(MISession *session, char *addressInfo)
{
	memory 		*mem = NULL;
	memoryinfo 	*memInfo = NULL;
	
	int sizeOfUint16_t = sizeof(uint16_t);
	int sizeOfUint32_t = sizeof(uint32_t);

	if (X10_MetaDebugInfoList == NULL)
		X10_MetaDebugInfoList = NewList();
	
	char *tmp = NULL;
	char *startAddressString = NULL;
	char *endAddressString = NULL;
	uint64_t startAddress;
	uint64_t endAddress;
	uint64_t length;

	char *x10StringAddress = NULL;
	char *x10sourceListAddress = NULL;
	char *x10toCPPListAddress = NULL;
	char *CPPtoX10xrefListAddress = NULL;
	char *x10methodNameListAddress = NULL;
	char *x10localVarListAddress = NULL;
	char *x10ClassMapListAddress = NULL;
	char *x10ClosureMapListAddress = NULL;
	char *x10ArrayMapListAddress = NULL;

	int includeVariableMap = 0;
	
	char * tmpData = NULL;
	
	/* get the start address */
	if (strstr(addressInfo, "0x") == NULL) {
		return;
	}
	
	/* find the string for the starting address */
	tmp = strchr(addressInfo, '0');
	startAddressString = strtok(tmp, " ");
	strtok(NULL, " "); /* get the "-" */
	endAddressString = strtok(NULL, " ");

	/* Now convert the startAddressString and endAddressString to real 
	 * address value so that we can calculate the length. 
	 */
	startAddress = strtoull(startAddressString, NULL, 16); 
	endAddress = strtoull(endAddressString, NULL, 16);
	length = endAddress - startAddress;

	int sizeOfPointer = GetAddressLength(session);
	
	//First, get the struct size of _x10_meta_debug_info_t
	memInfo = GetMemoryInfo(session, 0, startAddressString, "x", 2, 1, 1, NULL);
	SetList(memInfo->memories);
	mem = (memory*)GetListElement(memInfo->memories);
	uint16_t structureSize = 0;
	if (mem != NULL) {
		SetList(mem->data);
		tmpData = (char *)GetListElement(mem->data);
		structureSize = strtoul(tmpData, NULL, 16);
	}
	else {
		return;
	}
	FreeMemoryInfo(memInfo);
	memInfo = NULL;
	mem = NULL;

	if (structureSize > (4*(NUM_FIELDS_LINE_INFO + 1) + sizeOfPointer * NUM_FIELDS_LINE_INFO)) {
		//variable map information available
		includeVariableMap = 1;
	}

	if (structureSize > (NUM_FIELDS_LINE_INFO + NUM_FIELDS_VAR_INFO) * (4 + sizeOfPointer) + 4) {
		numFieldsVarInfo = NUM_FIELDS_VAR_INFO + 1;
	}
	
	//Now, we know the structure size, calculate how many _x10_meta_debug_info_t available.
	int numberStructures = length / structureSize;
	int outerIndex = 0;
	
	for (outerIndex = 0; outerIndex < numberStructures; outerIndex++) {
		//First, read the size information.  They are all of the size of unsigned.
		int offset = outerIndex * structureSize + sizeOfUint16_t + sizeof(char) + sizeof(char);

		if (includeVariableMap) {
			memInfo = GetMemoryInfo(session, offset, startAddressString, "x", sizeOfUint32_t, 1, NUM_FIELDS_LINE_INFO + numFieldsVarInfo, NULL);
		}
		else {
			memInfo = GetMemoryInfo(session, offset, startAddressString, "x", sizeOfUint32_t, 1, NUM_FIELDS_LINE_INFO, NULL);
		}

		SetList(memInfo->memories);
		mem = (memory*)GetListElement(memInfo->memories);
		if (mem == NULL || mem->data == NULL) {
			continue;
		}
		SetList(mem->data);
		_x10_meta_debug_info_t *x10_MetaDebugInfo = (_x10_meta_debug_info_t*) malloc(sizeof(_x10_meta_debug_info_t));
		uint32_t fieldSize;
		x10_MetaDebugInfo->header._structureSize = structureSize;
		//Get the x10_string_size
		tmpData = (char *)GetListElement(mem->data);
		fieldSize = strtoul(tmpData, NULL, 16);
		x10_MetaDebugInfo->x10_string_size = fieldSize;
		//Get the x10_source_list_size
		tmpData = (char *)GetListElement(mem->data);
		fieldSize = strtoul(tmpData, NULL, 16);
		x10_MetaDebugInfo->x10_source_list_size = fieldSize;
		//Get the x10_to_cpp_list_size
		tmpData = (char *)GetListElement(mem->data);
		fieldSize = strtoul(tmpData, NULL, 16);
		x10_MetaDebugInfo->x10_to_cpp_list_size = fieldSize;
		//Get the cpp_to_x10_xref_list_size
		tmpData = (char *)GetListElement(mem->data);
		fieldSize = strtoul(tmpData, NULL, 16);
		x10_MetaDebugInfo->cpp_to_x10_xref_list_size = fieldSize;
		//Get the x10_method_name_list_size
		tmpData = (char *)GetListElement(mem->data);
		fieldSize = strtoul(tmpData, NULL, 16);
		x10_MetaDebugInfo->x10_method_name_list_size = fieldSize;
		if (includeVariableMap) {
			//Get the x10_local_var_list_size
			tmpData = (char *)GetListElement(mem->data);
			fieldSize = strtoul(tmpData, NULL, 16);
			x10_MetaDebugInfo->x10_local_var_list_size = fieldSize;
			//Get the x10_class_map_list_size
			tmpData = (char *)GetListElement(mem->data);
			fieldSize = strtoul(tmpData, NULL, 16);
			x10_MetaDebugInfo->x10_class_map_list_size = fieldSize;
			//Get the x10_closure_map_list_size
			tmpData = (char *)GetListElement(mem->data);
			fieldSize = strtoul(tmpData, NULL, 16);
			x10_MetaDebugInfo->x10_closure_map_list_size = fieldSize;
			//Get the x10_array_map_list_size
			tmpData = (char *)GetListElement(mem->data);
			fieldSize = strtoul(tmpData, NULL, 16);
			x10_MetaDebugInfo->x10_array_map_list_size = fieldSize;
			//We skip the reference map for now.
		}

		FreeMemoryInfo(memInfo);
		memInfo = NULL;
		mem = NULL;	
		
		//Now, get the memeory addresses for the fields
		if (includeVariableMap) {
			offset = offset + (NUM_FIELDS_LINE_INFO + numFieldsVarInfo) * sizeOfUint32_t;
		}
		else {
			offset = offset + NUM_FIELDS_LINE_INFO * sizeOfUint32_t;
		}

		if (offset % sizeOfPointer != 0) {
			//skip 4 bytes
			offset += 4;
		}
		
		if (includeVariableMap) {
			memInfo = GetMemoryInfo(session, offset, startAddressString, "x", sizeOfPointer, 1, NUM_FIELDS_LINE_INFO + numFieldsVarInfo, NULL);
		}
		else {
			memInfo = GetMemoryInfo(session, offset, startAddressString, "x", sizeOfPointer, 1, NUM_FIELDS_LINE_INFO, NULL);
		}
		SetList(memInfo->memories);
		mem = (memory*)GetListElement(memInfo->memories);

		if (mem == NULL || mem->data == NULL) {
			continue;
		}
		SetList(mem->data);

		x10StringAddress = (char *)GetListElement(mem->data); 
		x10sourceListAddress = (char *)GetListElement(mem->data);
		x10toCPPListAddress = (char *)GetListElement(mem->data); 
		CPPtoX10xrefListAddress = (char *)GetListElement(mem->data); 
		x10methodNameListAddress = (char *)GetListElement(mem->data); 
		if (includeVariableMap) {
			x10localVarListAddress = (char *)GetListElement(mem->data);
			x10ClassMapListAddress = (char *)GetListElement(mem->data);
			x10ClosureMapListAddress = (char *)GetListElement(mem->data);
			x10ArrayMapListAddress = (char *)GetListElement(mem->data);
			//Skip the reference map for now.
		}

		//First, process the information for X10 strings
		getX10String(session, x10_MetaDebugInfo, x10StringAddress);	
		
		//Now, get the x10_source_list.
		getX10SourceList(session, x10_MetaDebugInfo, x10sourceListAddress);

		//Now, get the x10_to_cpp_list.
		getX10ToCPPList(session, x10_MetaDebugInfo, x10toCPPListAddress);
			
		//Now, get the CPPToX10 list.
		getCPPToX10List(session, x10_MetaDebugInfo, CPPtoX10xrefListAddress );
		
		//Now, get the x10methodName list.
		getX10MethodRecords(session, x10_MetaDebugInfo, x10methodNameListAddress);

		if (includeVariableMap) {
			//Now, get the x10_local_var_list list.
			getX10LocalVarList(session, x10_MetaDebugInfo, x10localVarListAddress );

			//Now, get the _x10_class_map list.
			getX10ClassMapList(session, x10_MetaDebugInfo, x10ClassMapListAddress);

			//Now, get the _x10_closure_map list.
			getX10ClosureMapList(session, x10_MetaDebugInfo, x10ClosureMapListAddress);

			//Now, get the x10_array_map_list list.
			getX10ArrayMapList(session, x10_MetaDebugInfo, x10ArrayMapListAddress);

			//Skip the reference map
		}
		AddToList(X10_MetaDebugInfoList, (void *)x10_MetaDebugInfo);
				
		FreeMemoryInfo(memInfo);
		memInfo = NULL;
		mem = NULL;
	
	}

	return;

}

/*
 * Parse the full path of a file, and return its base file name.
 */
static char *
getBaseFileName(char *inputName) {
	if (strchr(inputName, '/') == NULL && strchr(inputName, '\\') == NULL) {
		return inputName;
	}
	else {
		//Need to find out what is the file delimiter
		if (NULL != strchr(inputName, '/')) {
			//It is Linux file path
			return (strrchr(inputName, '/') + 1);
		}
		else {
			return (strrchr(inputName, '\\') + 1);
		}
	}
}

/*
 * Map an X10 line to its first C++ line.
 */
int 
X10LineToCPPLine(char* inputX10SourceFileName, int inputX10Line, char **pCPPSourceFileName, int *pCPPLine)
{
	//Going through all the list of X10_MetaDataInfoList
	_x10_meta_debug_info_t *pThisDebugInfo;
	int foundMatchingCPPLine = 0;
	for (SetList(X10_MetaDebugInfoList); (pThisDebugInfo = (_x10_meta_debug_info_t *)GetListElement(X10_MetaDebugInfoList)) != NULL;) {
		//First, make sure input x10SourceFileName is in the source file list.
		_x10source_file_t *pSourceFile = pThisDebugInfo->x10_source_list;
		int foundMetaDebugInfo = 0;
		int index = 0;
		for (index = 0; index < pThisDebugInfo->x10_source_array_size; index++) {
			uint32_t stringIndex = pSourceFile->string_index;
			char *sourceFileName = pThisDebugInfo->x10_strings + stringIndex;
			//It is the full name.  We need to get its base name
			char *baseSourceFileName = getBaseFileName(sourceFileName);
			if (0 == strcmp(inputX10SourceFileName, baseSourceFileName)) {
				foundMetaDebugInfo = 1;
				break;
			}
			pSourceFile++;
		}
		
		if (foundMetaDebugInfo == 0) {
		 	/* We did not found the x10 source in this debug info record, keep going.*/
		 	continue;
		}

		//We get here, it means the X10 to CPP line mapping should be in this meta debug info
		_x10_to_cpp_xrefso_t *x10ToCPPRef = pThisDebugInfo->x10_to_cpp_list;

		
		for (index = 0; index < pThisDebugInfo->x10_to_cpp_array_size; index++) {
			int x10SourceIndex = x10ToCPPRef->x10_to_cpp_ref.x10_index;
			_x10source_file_t *thisX10SourceFile = (_x10source_file_t *)(pThisDebugInfo->x10_source_list + x10SourceIndex * sizeof(_x10source_file_t));
			char *x10SourceFileName = (char*)(pThisDebugInfo->x10_strings + thisX10SourceFile->string_index);
			char *baseX10SourceFileName = getBaseFileName(x10SourceFileName);
			if (0 == strcmp(inputX10SourceFileName, baseX10SourceFileName)) {
				if (x10ToCPPRef->x10_to_cpp_ref.x10_line == inputX10Line) {
					char *cppSourceFileName = (pThisDebugInfo->x10_strings) + (x10ToCPPRef->x10_to_cpp_ref.cpp_index);
					char *cppSourceFileNameOut = strdup(cppSourceFileName);
					*pCPPSourceFileName = cppSourceFileNameOut;
					*pCPPLine = x10ToCPPRef->x10_to_cpp_ref.cpp_from_line;
					foundMatchingCPPLine = 1;
					break;
				}
			}
			x10ToCPPRef++;
		}
		
		if (foundMatchingCPPLine == 1) {
			//We found the matching line, return.
			return foundMatchingCPPLine;
		}
		if (foundMatchingCPPLine == 0) {
	  		//We did not find it.  Assign the original ones to the result
	  		*pCPPSourceFileName = inputX10SourceFileName;
	  		*pCPPLine = inputX10Line;
		}
		
	}

	return foundMatchingCPPLine;
}

/*
 * Check if the input C++ line is listed in the X10 to C++ cross reference table.
 */
int 
CppLineInX10ToCPPMap(char* inputCPPSourceFileName, int inputCPPLine)
{
	//Going through all the list of X10_MetaDataInfoList
	_x10_meta_debug_info_t *pThisDebugInfo;
	int foundMatchingCPPLine = 0;
	char *baseInputCppSourceFileName = getBaseFileName(inputCPPSourceFileName);
	for (SetList(X10_MetaDebugInfoList); (pThisDebugInfo = (_x10_meta_debug_info_t *)GetListElement(X10_MetaDebugInfoList)) != NULL;) {
		_x10_to_cpp_xrefso_t *x10ToCPPRefSO = pThisDebugInfo->x10_to_cpp_list;
		int index = 0;
		for (index = 0; index < pThisDebugInfo->x10_to_cpp_array_size; index++) {
			int cppSourceIndex = x10ToCPPRefSO->x10_to_cpp_ref.cpp_index;
			char *cppSourceFileName = (char*)(pThisDebugInfo->x10_strings + cppSourceIndex);
			char *baseCPPSourceFileName = getBaseFileName(cppSourceFileName);
			if (0 == strcmp(baseInputCppSourceFileName, baseCPPSourceFileName)) {
				if (inputCPPLine == x10ToCPPRefSO->x10_to_cpp_ref.cpp_from_line) {
					foundMatchingCPPLine = 1;
					x10ToCPPRefSO->first_step_over_line = inputCPPLine;
					break;
				}
			}
			x10ToCPPRefSO++;
		}
		
	}

	if (foundMatchingCPPLine) {
		return foundMatchingCPPLine;
	}

	//Now, search for the internal field firstStepOverLine in the X10->C++ mapping table.
	for (SetList(X10_MetaDebugInfoList); (pThisDebugInfo = (_x10_meta_debug_info_t *)GetListElement(X10_MetaDebugInfoList)) != NULL;) {
		_x10_to_cpp_xrefso_t *x10ToCPPRefSO = pThisDebugInfo->x10_to_cpp_list;
		int index = 0; 
		for (index = 0; index < pThisDebugInfo->x10_to_cpp_array_size; index++) {
			if (0 == x10ToCPPRefSO->first_step_over_line) {
				x10ToCPPRefSO++;
				continue;
			}
			int cppSourceIndex = x10ToCPPRefSO->x10_to_cpp_ref.cpp_index;
			char *cppSourceFileName = (char*)(pThisDebugInfo->x10_strings + cppSourceIndex);
			char *baseCPPSourceFileName = getBaseFileName(cppSourceFileName);
			if (0 == strcmp(baseInputCppSourceFileName, baseCPPSourceFileName)) {
				if (inputCPPLine == x10ToCPPRefSO->first_step_over_line) {
					foundMatchingCPPLine = 1;
					break;
				}
			}
			x10ToCPPRefSO++;
		}
	}

	if (foundMatchingCPPLine) {
		return foundMatchingCPPLine;
	}

	//If we get here, we need to search for in the C++ to X10 mapping table to see if this line is inside a range of
	//C++ lines mapped to one X10 line.  
	//If multiple ranges found, we want to use the entry of the smallest range, and find out what is the X10 line
	//corresponding to it.  And update the firstStepOverLine of that X10 entry in the X10->C++ mapping table.
	int rangeCount = -1;
	int foundIndexSoFar = -1;
	_x10_meta_debug_info_t *pFoundDebugInfo = NULL;
	for (SetList(X10_MetaDebugInfoList); (pThisDebugInfo = (_x10_meta_debug_info_t *)GetListElement(X10_MetaDebugInfoList)) != NULL;) {
		_cpp_to_x10_xref_t *cppToX10Ref = pThisDebugInfo->cpp_to_x10_xref_list;
		int index = 0;
		
		for (index = 0; index < pThisDebugInfo->cpp_to_x10_xref_array_size; index++) {
			int cppSourceIndex = cppToX10Ref->cpp_index;
			char *cppSourceFileName = (char*)(pThisDebugInfo->x10_strings + cppSourceIndex);
			char *baseCPPSourceFileName = getBaseFileName(cppSourceFileName);
			if (0 == strcmp(baseInputCppSourceFileName, baseCPPSourceFileName)) {
				if (inputCPPLine >= cppToX10Ref->cpp_from_line && inputCPPLine <= cppToX10Ref->cpp_to_line) {
					int thisRangeCount = cppToX10Ref->cpp_to_line - cppToX10Ref->cpp_from_line;

					if (rangeCount < 0) {
						//We haven't found any matches yet.
						rangeCount = thisRangeCount;
						foundIndexSoFar = index;
						pFoundDebugInfo = pThisDebugInfo;
					} else {
						if (thisRangeCount < rangeCount) {
							rangeCount = thisRangeCount;
							foundIndexSoFar = index;
							pFoundDebugInfo = pThisDebugInfo;
						}
					}
					if (rangeCount == 0) {
						//This is the smallest one can be.  We can exit here.
						break;
					}

				}
			}
			cppToX10Ref++;
		}

		if (0 == rangeCount) {
			break;
		}
		
	}

	//We found the match.
	if (foundIndexSoFar >= 0) {
		//We found the match.
		_cpp_to_x10_xref_t *cppToX10Ref = pFoundDebugInfo->cpp_to_x10_xref_list + foundIndexSoFar;
		//get its x10 line
		int x10Line = cppToX10Ref->x10_line;
		//Now find the entry in the X10->C++ mapping for this X10 line
		int index = 0;
		_x10_to_cpp_xrefso_t *x10ToCPPRefSO = pFoundDebugInfo->x10_to_cpp_list;
		for (index = 0; index < pFoundDebugInfo->x10_to_cpp_array_size; index++) {
			if (x10Line == x10ToCPPRefSO->x10_to_cpp_ref.x10_line) {
				//put the checking for "inputCPPLine == x10ToCPPRefSO->first_step_over_line" here just to make sure if
				//we miss in the checking for firstStepOverLine before.
				if (0 == x10ToCPPRefSO->first_step_over_line || inputCPPLine == x10ToCPPRefSO->first_step_over_line) {
					x10ToCPPRefSO->first_step_over_line = inputCPPLine;
					foundMatchingCPPLine = 1;
					break;
				}
				break;
			}
			x10ToCPPRefSO++;
		}
	}
	
	return foundMatchingCPPLine;
}


/* 
 * This method will take a C++ location (source file name + line #) 
 * and map it to an X10 location.
 */
int 
CPPLineToX10Line(char* inputCppSourceFileName, int inputCppLine, char **pX10SourceFileName, char **pX10MethodName, int *pX10Line)
{
	_x10_meta_debug_info_t *pThisDebugInfo;
	int foundMatchingX10Line = 0;

	if (NULL == inputCppSourceFileName) {
		return 0;
	}
	char *baseInputCppSourceFileName = getBaseFileName(inputCppSourceFileName);
	for (SetList(X10_MetaDebugInfoList); (pThisDebugInfo = (_x10_meta_debug_info_t *)GetListElement(X10_MetaDebugInfoList)) != NULL;) {
		_cpp_to_x10_xref_t *cppToX10Ref = pThisDebugInfo->cpp_to_x10_xref_list;
		int index = 0;
		int rangeCount = -1;
		int foundIndexSoFar = -1;
		for (index = 0; index < pThisDebugInfo->cpp_to_x10_xref_array_size; index++) {
			uint32_t nameIndex = cppToX10Ref->cpp_index;
			char *sourceFileName = pThisDebugInfo->x10_strings + nameIndex;
			char *baseSourceFileName = getBaseFileName(sourceFileName);
			if (NULL == baseSourceFileName) {
				continue;
			}
			if (0 == strcmp(baseInputCppSourceFileName, baseSourceFileName)) {
				if (inputCppLine >= cppToX10Ref->cpp_from_line && inputCppLine <= cppToX10Ref->cpp_to_line) {
					int thisRangeCount = cppToX10Ref->cpp_to_line - cppToX10Ref->cpp_from_line;
					if (rangeCount < 0) {
						//We haven't found any matches yet.
						rangeCount = thisRangeCount;
						foundIndexSoFar = index;
					} else {
						if (thisRangeCount < rangeCount) {
							rangeCount = thisRangeCount;
							foundIndexSoFar = index;
							
						}
					}
					if (rangeCount == 0) {
						//This is the smallest one can be.  We can exit here.
						break;
					}
				}
			}
			cppToX10Ref++;
		}
		//We found the match.
		if (foundIndexSoFar >= 0) {
			cppToX10Ref = 	pThisDebugInfo->cpp_to_x10_xref_list + foundIndexSoFar;
			int x10SourceIndex = cppToX10Ref->x10_index;
			_x10source_file_t *thisSourceFile = (_x10source_file_t *)(pThisDebugInfo->x10_source_list + x10SourceIndex * sizeof(_x10source_file_t));
			char *x10SourceFileName = (char*)(pThisDebugInfo->x10_strings + thisSourceFile->string_index);
			char *baseX10SourceFileName = getBaseFileName(x10SourceFileName);
			char *x10SourceFileNameOut = strdup(baseX10SourceFileName);
			*pX10SourceFileName = getBaseFileName(x10SourceFileNameOut);
			*pX10Line = cppToX10Ref->x10_line;
			if (pX10MethodName != NULL) {
				*pX10MethodName = NULL;
				//try to find the method name,  need to go through the method list
				int methodIndex = 0;
				for (methodIndex = 0; methodIndex < pThisDebugInfo->x10_method_mame_array_size; methodIndex++) {
					_x10_method_t *x10Method = (_x10_method_t *)(pThisDebugInfo->x10_method_list + methodIndex);
					if (0 != strcmp(baseInputCppSourceFileName, pThisDebugInfo->x10_strings + x10Method->cpp_index)) {
						continue;
					}
					if (inputCppLine >= x10Method->cpp_from_line && inputCppLine <= x10Method->cpp_to_line) {
						char *x10MethodName = pThisDebugInfo->x10_strings + x10Method->method_name.x10_method;
						char *x10MethodNameOut = strdup(x10MethodName);
						(*pX10MethodName) = x10MethodNameOut;
						break;
					}
				}
			}
			foundMatchingX10Line = 1;
			break;
		}

	}
	if (foundMatchingX10Line == 0) {
	  //We did not find it.  Assign the original ones to the result
	  *pX10SourceFileName = inputCppSourceFileName;
	  *pX10Line = inputCppLine;
	}
	return foundMatchingX10Line;
}

/* 
 * This method will return the CPP line for X10 main method
 */
int 
MapX10MainToCPPLine(char **pCppFileName, int *pLineNumber)
{
	_x10_meta_debug_info_t *pThisDebugInfo;
	int foundCppLine = 0;  //flag to indicate if the correpsonding line is found.
	int foundMatchingCPPLine = -1;
	char *foundCppFileName = NULL;
	for (SetList(X10_MetaDebugInfoList); (pThisDebugInfo = (_x10_meta_debug_info_t *)GetListElement(X10_MetaDebugInfoList)) != NULL;) {
		int methodIndex = 0;
		for (methodIndex = 0; methodIndex < pThisDebugInfo->x10_method_mame_array_size; methodIndex++) {
			_x10_method_t *x10Method = (_x10_method_t*)(pThisDebugInfo->x10_method_list + methodIndex);
			char *x10MethodNameString = pThisDebugInfo->x10_strings + x10Method->method_name.x10_method;
			if (0 == strcmp(x10MethodNameString, "main")) {
				foundCppLine = 1;
				foundMatchingCPPLine = x10Method->cpp_from_line;
				foundCppFileName = pThisDebugInfo->x10_strings + x10Method->cpp_index;
				break;
			}
		}
	}
	if (foundCppLine > 0) {
		*pLineNumber = foundMatchingCPPLine;
		*pCppFileName = strdup(foundCppFileName);
	}
	return foundCppLine;
}

/*
 * Return the list of local variables defined in the input context (C++ file and line number).
 */
List *
GetX10LocalVarNames(char *cppFileName, int cppLineNum )
{
	int mappedX10Line = 0;
	char *mappedX10File;
	
	int foundMatchingX10Line = CPPLineToX10Line(cppFileName, cppLineNum, &mappedX10File, NULL, &mappedX10Line);
	if (0 == foundMatchingX10Line) {
		return NULL;
	}
		
	List *result = NewList();
	
	_x10_meta_debug_info_t *pThisDebugInfo;
	for (SetList(X10_MetaDebugInfoList); (pThisDebugInfo = (_x10_meta_debug_info_t *)GetListElement(X10_MetaDebugInfoList)) != NULL;) {
		//Get the the x10_source_list for this x10 file.
		_x10_local_var_map *pX10LocalVarMap = pThisDebugInfo->x10_local_var_list;
		
		int index = 0;
		for (index = 0; index < pThisDebugInfo->x10_local_var_array_size; index++) {
			uint32_t x10FileIndex = pX10LocalVarMap->x10_index;
			_x10source_file_t *thisX10SourceFile = pThisDebugInfo->x10_source_list + x10FileIndex;
			char *x10FileNameForThisVar = pThisDebugInfo->x10_strings + thisX10SourceFile->string_index;
			char *baseX10FileNameForThisVar = getBaseFileName(x10FileNameForThisVar);
			if (0 == strcmp(baseX10FileNameForThisVar, mappedX10File)) {
				if (mappedX10Line >= pX10LocalVarMap->x10_start_line && mappedX10Line <= pX10LocalVarMap->x10_end_line) {
					//This variable is in scope.
					char *variableName = pThisDebugInfo->x10_strings + pX10LocalVarMap->x10_name;
					if (NULL == strstr(variableName, "id$")) {
						AddToList(result, strdup(variableName));
					}
					//AddToList(result, strdup(variableName));
				}
			}
			pX10LocalVarMap++;
		}
	}
	return result;
}

/*
 * Search for the class map record of the input class in input MetaDebug record.
 */
static _x10_class_map *
findClassMap(char *className, _x10_meta_debug_info_t *debugInfoMap)
{
	int index = 0;
	_x10_class_map *found = NULL;
	if (NULL == debugInfoMap) {
		return found;
	}
	_x10_class_map *classMap = debugInfoMap->x10_class_map_list;
	char *x10String = debugInfoMap->x10_strings;
	for (index = 0; index < debugInfoMap->x10_class_map_array_size; index++) {
		if (0 == strcmp(className, x10String + classMap->x10_name)) {
			//We found this class map
			found = classMap;
			break;
		}
		classMap++;
	}
	return found;
}

/*
 * Search for the class map record of the input class in all MetaDebug records.
 */
static _x10_class_map *
findClassMapInAll(char *className, _x10_meta_debug_info_t *itsDebugInfoMap)
{
	//First, search the class name in the passing debug info map
	_x10_class_map *pClassMap = findClassMap(className, itsDebugInfoMap);
	if (NULL != pClassMap) {
		return pClassMap;
	}
	
	_x10_meta_debug_info_t *pThisDebugInfo = NULL;
	for (SetList(X10_MetaDebugInfoList); (pThisDebugInfo = (_x10_meta_debug_info_t *)GetListElement(X10_MetaDebugInfoList)) != NULL; ) {
		if (pThisDebugInfo != itsDebugInfoMap) {
			pClassMap = findClassMap(className, pThisDebugInfo);
			if (pClassMap != NULL) {
				break;
			}
		}
	}
	return pClassMap;
}

/*
 * Built up the variable information of the input variable name, depending on the input context (C++ file name + line number).
 */
x10_var_t *
GetSymbolVariable (MISession *session, char *cppFileName, int cppLineNum, char *rootName, List *offsprings, long arraySize, long arrayStartIndex, int listChildren)
{
	int mappedX10Line = 0;
	char *mappedX10File;

	if (NULL == X10_MetaDebugInfoList) {
		return NULL;
	}
	
	int foundMatchingX10Line = CPPLineToX10Line(cppFileName, cppLineNum, &mappedX10File, NULL, &mappedX10Line);
	if (0 == foundMatchingX10Line) {
		return NULL;
	}

	//First, use the rootName to find out the local variable in the local variable map
	x10_var_t *result = NULL;
	x10_var_t *parent = NULL;
	_x10_meta_debug_info_t *pThisDebugInfo;
	int found = 0;
	int mapIndex = 0;
	int definedInCurrentLine = 0;
	for (SetList(X10_MetaDebugInfoList), mapIndex = 0; (pThisDebugInfo = (_x10_meta_debug_info_t *)GetListElement(X10_MetaDebugInfoList)) != NULL; mapIndex++) {
		_x10_local_var_map *pX10LocalVarMap = pThisDebugInfo->x10_local_var_list;
		if (NULL == pX10LocalVarMap) {
			continue;
		}
		int index = 0;
		for (index = 0; index < pThisDebugInfo->x10_local_var_array_size; index++) {
			if (strcmp(rootName, (char*)(pThisDebugInfo->x10_strings + pX10LocalVarMap->x10_name)) == 0) {
				uint32_t x10FileIndex = pX10LocalVarMap->x10_index;
				_x10source_file_t *thisX10SourceFile = pThisDebugInfo->x10_source_list + x10FileIndex;
				char *x10FileNameForThisVar = pThisDebugInfo->x10_strings + thisX10SourceFile->string_index;
				char *baseX10FileNameForThisVar = getBaseFileName(x10FileNameForThisVar);
				if (0 == strcmp(baseX10FileNameForThisVar, mappedX10File)) {
					if (mappedX10Line >= pX10LocalVarMap->x10_start_line && mappedX10Line <= pX10LocalVarMap->x10_end_line) {
						if (mappedX10Line == pX10LocalVarMap->x10_start_line) {
							definedInCurrentLine = 1;
						}
						//This variable is in scope.
						result = X10VarNew();
						result->var = X10VariableNew();
						result->var->cpp_name = strdup((char*)(pThisDebugInfo->x10_strings + pX10LocalVarMap->cpp_name));
						result->var->cpp_full_name = strdup(result->var->cpp_name);
						result->var->type = pX10LocalVarMap->x10_type;
						result->var->type_index = pX10LocalVarMap->x10_type_index;
						result->var->map_index = mapIndex;
						result->var->name = strdup(rootName);
						result->children = NULL;
						//Get the location of this variable
						if (!definedInCurrentLine) {
							result->var->location = VariableLocation(session, result->var);
						}
						if (X10DTArray == result->var->type) {
							result->var->is_array = 1;
							//ask the debug assistant for array size.
							if (!definedInCurrentLine) {
								//FIXME: to workaround the problem with uninitialize array problem, we need to get rawDataLocation here
								char *rawDataLocation = ElementRawDataLocation(session, result->var);
								if (NULL == rawDataLocation) {
									//deal with the case what size and location not matching
									result->var->num_children = 0;
								} else {
									free(rawDataLocation);
									result->var->num_children = ElementCount(session, NULL, result->var);
								}
							}
						}
						else if (X10DTClass == result->var->type) {
							result->var->type_name = strdup((char*)(pThisDebugInfo->x10_strings + result->var->type_index));
							_x10_class_map *pClassMap = findClassMapInAll(result->var->type_name, pThisDebugInfo);
							if (NULL == pClassMap) {
								//Could be one of the runtime class.
								result->var->num_children = 0;
							} else {
								result->var->num_children = pClassMap->x10_member_count;
							}
						} else if (X10DTRandom == result->var->type
						   			|| X10DTDist == result->var->type) {
							result->var->num_children = 0;
						} else if (X10DTRegion == result->var->type
									|| X10DTPlaceLocalHandle == result->var->type
									|| X10DTPoint == result->var->type
									|| X10DTDistArray == result->var->type) {
							x10_builtin_type_t *returnedType = NULL;
							QueryBuiltinType(session, result->var->type, &returnedType);
							if (NULL != returnedType) {
								result->var->num_children = returnedType->member_count;
								result->var->type_name = returnedType->type_name;
								free(returnedType);
							} else {
								result->var->num_children = 0;
								result->var->type_name = strdup("Unknown");
							}
						}
						
						if (NULL == result->var->type_name) {
							result->var->type_name = TypeName(result->var->type);
						} 
						found = 1;
						break;
					}
				}
			}
			pX10LocalVarMap++;
		}
		if (found) {
			//found
			break;
		}
	}

	if (!found) {
		return NULL;
	}
	
	found = 0;
	
	//Now we have the root variable.  If there are offsprings in the offsprings list, we need to find out the exact variable, which
	//is the offspring of the root variable.
	if (offsprings != NULL) {
		SetList(offsprings);
		char *currentOffspringName = NULL;
		while ((currentOffspringName = (char*)(GetListElement(offsprings))) != NULL) {
			//Find this child in the member map of this variable.
			 if (X10DTClass == result->var->type) {
			 	_x10_class_map *pClassMap = findClassMapInAll(result->var->type_name, pThisDebugInfo);
			 	int memberIndex = 0;
				_x10_type_member *pMember = pClassMap->x10_members;
				for (memberIndex = 0; memberIndex < pClassMap->x10_member_count; memberIndex++) {
					char *thisMemberName = (char*)(pThisDebugInfo->x10_strings + pMember->x10_member_name);
					if (0 == strcmp(thisMemberName, currentOffspringName)) {
						//We found this offspring.
						x10_var_t *memberVar = X10VarNew();
						memberVar->var = X10VariableNew();
						memberVar->var->type = pMember->x10_type;
						memberVar->var->type_index = pMember->x10_type_index;
						memberVar->var->map_index = mapIndex;
						memberVar->var->name = strdup((char*)(pThisDebugInfo->x10_strings + pMember->x10_member_name));
						memberVar->var->cpp_name = strdup((char*)(pThisDebugInfo->x10_strings + pMember->cpp_member_name));
						int cppFullNameLen = 0;
						int isVariableThis = 0;
						if (0 == strcmp(result->var->cpp_name, "this")) {
							isVariableThis = 1;
						}
						if (1 == isVariableThis) {
							cppFullNameLen = strlen(result->var->cpp_full_name) + 
												strlen(POINTER_DEREFERENCE_STR) + 
												strlen(memberVar->var->cpp_name) + 
												1;
						} else {
							cppFullNameLen = strlen(result->var->cpp_full_name) + 
												strlen(CLASS_MEMBER_DEREFERENCE_STR) + 
												strlen(memberVar->var->cpp_name) + 
												1;
						}
						memberVar->var->cpp_full_name = (char*)malloc(cppFullNameLen);
						strcpy(memberVar->var->cpp_full_name, result->var->cpp_full_name);
						if (1 == isVariableThis) {
							strcat(memberVar->var->cpp_full_name, POINTER_DEREFERENCE_STR);
						} else {
							strcat(memberVar->var->cpp_full_name, CLASS_MEMBER_DEREFERENCE_STR);
						}
						strcat(memberVar->var->cpp_full_name, memberVar->var->cpp_name);
						memberVar->children = NULL;
						memberVar->var->location = MemberLocation(session, result->var, memberVar->var);
						if (X10DTClass == memberVar->var->type) {
							memberVar->var->type_name = strdup((char*)(pThisDebugInfo->x10_strings + memberVar->var->type_index));
							_x10_class_map *pMemberClassMap = findClassMapInAll(memberVar->var->type_name, pThisDebugInfo);
							if (NULL == pMemberClassMap) {
								//Could be one of the runtime class.
								memberVar->var->num_children = 0;
							}
							else {
								memberVar->var->num_children = pMemberClassMap->x10_member_count;
							}
						}
						else if (X10DTArray == memberVar->var->type
									|| X10DTDistArrayLocalState == memberVar->var->type) {
							memberVar->var->is_array = 1;
							//FIXME: to workaround the problem with uninitialize array problem, we need to get rawDataLocation here
							char *rawDataLocation = ElementRawDataLocation(session, memberVar->var);
							if (NULL == rawDataLocation) {
								//deal with the case what size and location not matching
								memberVar->var->num_children = 0;
							}
							else {
								free(rawDataLocation);
								memberVar->var->num_children = ElementCount(session, NULL, memberVar->var);
							}
						}
						else if (X10DTRegion == memberVar->var->type
									|| X10DTPlaceLocalHandle == memberVar->var->type
									|| X10DTDistArray == memberVar->var->type
									|| X10DTPoint == memberVar->var->type) {
							x10_builtin_type_t *returnedType = NULL;
							QueryBuiltinType(session, memberVar->var->type, &returnedType);
							if (NULL != returnedType) {
								memberVar->var->num_children = returnedType->member_count;
								memberVar->var->type_name = returnedType->type_name;
								free(returnedType);
							}
							else {
								memberVar->var->num_children = 0;
								memberVar->var->type_name = strdup("Unknown");
							}
						}
						if (memberVar->var->type_name == NULL) {
							memberVar->var->type_name = TypeName(memberVar->var->type);
						}
						found = 1;
						if (NULL != parent) {
							X10VarFree(parent);
						}
						parent = result;
						result = memberVar;
						break;
					}
					pMember++;
				}
			 }
			 else if (X10DTRegion ==  result->var->type 
						|| X10DTPlaceLocalHandle ==  result->var->type
						|| X10DTDistArray ==  result->var->type
						|| X10DTPoint == result->var->type) {
			 	x10_builtin_type_t *returnedType = NULL;
				QueryBuiltinType(session, result->var->type, &returnedType);
				x10_var_t *memberVar = X10VarNew();
				int memberIndex = 0;
				x10member_t *pMember = returnedType->member;
				for (memberIndex = 0; memberIndex < returnedType->member_count; memberIndex++) {
					if (0 == strcmp(pMember->name, currentOffspringName)) {
						x10variable_t *thisMember = MemberVariable(session, result->var, pMember->name, pMember->type);
						if (NULL == thisMember) {
							return NULL;
						}
						memberVar->var = thisMember;
						found = 1;
						if (NULL != parent) {
							X10VarFree(parent);
						}
						parent = result;
						result = memberVar;
						break;
					}
					pMember++;
				}
				free(returnedType);
			 }
			 
		}
	}

	//Now we have the variable, we need to list the children if needed:
	if (listChildren && result->var->num_children> 0) {
		if (X10DTClass == result->var->type) {
			//Get the member map of this class, and construct the children.	
			result->children = NewList();
			_x10_class_map *pClassMap = findClassMapInAll(result->var->type_name, pThisDebugInfo);
			int memberIndex = 0;
			_x10_type_member *pMember = pClassMap->x10_members;
			for (memberIndex = 0; memberIndex < pClassMap->x10_member_count; memberIndex++) {
				x10_var_t *memberVar = X10VarNew();
				memberVar->var = X10VariableNew();
				memberVar->var->type = pMember->x10_type;
				memberVar->var->type_index = pMember->x10_type_index;
				memberVar->var->map_index = mapIndex;
				memberVar->var->name = strdup((char*)(pThisDebugInfo->x10_strings + pMember->x10_member_name));
				memberVar->var->cpp_name = strdup((char*)(pThisDebugInfo->x10_strings + pMember->cpp_member_name));
				int cppFullNameLen = 0;
				int isVariableThis = 0;
				if (0 == strcmp(result->var->cpp_name, "this")) {
					isVariableThis = 1;
				}
				if (1 == isVariableThis) {
					//This variable is "this", it is the _val pointer already.
					cppFullNameLen = strlen(result->var->cpp_full_name) + 
										strlen(POINTER_DEREFERENCE_STR) + 
										strlen(memberVar->var->cpp_name) + 
										1;
				}
				else {
					cppFullNameLen = strlen(result->var->cpp_full_name) + 
										strlen(CLASS_MEMBER_DEREFERENCE_STR) + 
										strlen(memberVar->var->cpp_name) + 
										1;
				}
				memberVar->var->cpp_full_name = (char*)malloc(cppFullNameLen);
				strcpy(memberVar->var->cpp_full_name, result->var->cpp_full_name);
				if (1 == isVariableThis) {
					strcat(memberVar->var->cpp_full_name, POINTER_DEREFERENCE_STR);
				}
				else {
					strcat(memberVar->var->cpp_full_name, CLASS_MEMBER_DEREFERENCE_STR);
				}
				strcat(memberVar->var->cpp_full_name, memberVar->var->cpp_name);
				memberVar->children = NULL;
				//Get the location of this member 
				memberVar->var->location = MemberLocation(session, result->var, memberVar->var);
				if (X10DTClass == memberVar->var->type) {
					memberVar->var->type_name = strdup((char*)(pThisDebugInfo->x10_strings + memberVar->var->type_index));
					_x10_class_map *pMemberClassMap = findClassMapInAll(memberVar->var->type_name, pThisDebugInfo);
					if (NULL == pMemberClassMap) {
						//Could be one of the runtime class.
						memberVar->var->num_children = 0;
					}
					else {
						memberVar->var->num_children = pMemberClassMap->x10_member_count;
					}
				}
				else if (X10DTArray == memberVar->var->type 
							|| X10DTDistArrayLocalState == memberVar->var->type) {
					memberVar->var->is_array = 1;

					//FIXME: to workaround the problem with uninitialize array problem, we need to get rawDataLocation here
					char *rawDataLocation = ElementRawDataLocation(session, memberVar->var);
					if (NULL == rawDataLocation) {
						//deal with the case what size and location not matching
						memberVar->var->num_children = 0;
					}
					else {
						free(rawDataLocation);
						memberVar->var->num_children = ElementCount(session, result->var, memberVar->var);
					}
							
				}
				else if (X10DTRegion == memberVar->var->type
							|| X10DTPlaceLocalHandle == memberVar->var->type
							|| X10DTDistArray == memberVar->var->type
							|| X10DTPoint == memberVar->var->type) {
					x10_builtin_type_t *returnedType = NULL;
					QueryBuiltinType(session, memberVar->var->type, &returnedType);
					if (NULL != returnedType) {
						memberVar->var->num_children = returnedType->member_count;
						memberVar->var->type_name = returnedType->type_name;
						free(returnedType);
					}
					else {
						memberVar->var->num_children = 0;
						memberVar->var->type_name = strdup("Unknown");
					}
				}
				if (memberVar->var->type_name == NULL) {
					memberVar->var->type_name = TypeName(memberVar->var->type);
				}
				AddToList(result->children, memberVar);
				pMember++;
			}
		}
		else if ((X10DTArray == result->var->type 
					|| X10DTArray == result->var->type)
					&& !definedInCurrentLine) {
			//The size will be updated to the input arraySize.
			if (result->var->num_children > arraySize) {
				result->var->num_children = arraySize;
			}
			//Get the address of where its data is stored
			char *rawDataLocation = NULL;
			//make sure for the fake array inside a point, its raw location is its location
			if (parent != NULL && X10DTPoint == parent->var->type) {
				rawDataLocation = result->var->location;
			}
			else {
				rawDataLocation = ElementRawDataLocation(session, result->var);
			}
			//construct the array children list
			int iteration = 0;
			int elementType = GetArrayElementType(result->var);
			result->children = NewList();
			for (iteration = 0; iteration < result->var->num_children; iteration++) {
				x10_var_t *elementVar = X10VarNew();
				elementVar->var = X10VariableNew();
				elementVar->var->type = elementType;
				elementVar->var->type_index = -1;
				elementVar->var->map_index = -1;
				elementVar->var->name = (char*)malloc(strlen(result->var->name) + 2 + 10);
				sprintf(elementVar->var->name, "%s[%lu]", result->var->name, arrayStartIndex+iteration);
				elementVar->children = NULL;
				//Get the location of this element
				char *elementDataLocation = ElementDataLocation(session, elementVar->var, rawDataLocation, arrayStartIndex + iteration);
				elementVar->var->location = elementDataLocation;
				AddToList(result->children, elementVar);
			}
		}
		else if ((X10DTRegion ==  result->var->type 
						|| X10DTPlaceLocalHandle ==  result->var->type
						|| X10DTDistArray ==  result->var->type
						|| X10DTPoint ==  result->var->type) 
						&& !definedInCurrentLine) {
			x10_builtin_type_t *returnedType = NULL;
			QueryBuiltinType(session, result->var->type, &returnedType);
			result->var->num_children = returnedType->member_count;
			result->var->type_name = returnedType->type_name;
			int memberIndex = 0;
			x10member_t *pMember = returnedType->member;
			result->children = NewList(); 
			for (memberIndex = 0; memberIndex < returnedType->member_count; memberIndex++) {
				x10_var_t *memberVar = X10VarNew();
				x10variable_t *thisMember = MemberVariable(session, result->var, pMember->name, pMember->type);
				if (NULL == thisMember) {
					return NULL;
				}
				memberVar->var = thisMember;
				memberVar->children = NULL;
				AddToList(result->children, memberVar);
				pMember++;
			}
			free(returnedType);
		}
			 
	}

	if (NULL != parent) {
		X10VarFree(parent);
	}					
	return result;
}

/*
 * Search for the Array element type in MetaDebug records, or the built in element type map.
 */
int 
GetArrayElementType(x10variable_t * var)
{
	int result = -1;
	int index = 0;
	_x10_meta_debug_info_t *pThisDebugInfo;
	int typeIndex = var->type_index;
	int mapIndex = var->map_index;

	if (typeIndex != -1 && mapIndex == -1) {
		return ElementType(var);
		
	}

	SetList(X10_MetaDebugInfoList);
	while(index < mapIndex) {
		//Skip first mapIndex of elements
		GetListElement(X10_MetaDebugInfoList);
	}
	pThisDebugInfo = (_x10_meta_debug_info_t *)GetListElement(X10_MetaDebugInfoList);
	if (typeIndex < pThisDebugInfo->x10_array_map_array_size) {
		_x10_array_map *pX10ArrayMap = pThisDebugInfo->x10_array_map_list + typeIndex;
		result = pX10ArrayMap->x10_type;
	}
	
	return result;
}

/*
 * Free up the memory for one MetaDebug record.
 */
static void 
freeMetaDebugInfoMap(_x10_meta_debug_info_t *map)
{
	//free x10_strings
	free(map->x10_strings);
	//free x10_source_list
	free(map->x10_source_list);
	//free x10_to_cpp_list
	free(map->x10_to_cpp_list);
	//free cpp_to_x10_xref_list
	free(map->cpp_to_x10_xref_list);
	//free x10_method_list
	free(map->x10_method_list);
	//free x10_local_var_list
	free(map->x10_local_var_list);
	//free x10_closure_map_list
	free(map->x10_closure_map_list);
	//free x10_array_map_list
	free(map->x10_array_map_list);
	//free x10_class_map_list
	//but first, need to free the type member list of this each class map.
	int index = 0;
	_x10_class_map *classMap = map->x10_class_map_list;
	for (index = 0; index < map->x10_class_map_array_size; index++) {
		free(classMap->x10_members);
		classMap++;
	}
	free(map->x10_class_map_list);

	return;
	
}

/*
 * Free up the memory for MetaDebug mapping table.
 */
void ClearMetaDebugInfoMaps()
{
	DestroyList(X10_MetaDebugInfoList, freeMetaDebugInfoMap);
}



