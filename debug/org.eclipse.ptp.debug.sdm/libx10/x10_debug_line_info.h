/*
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

#ifndef _X10_DEBUG_LINE_INFO_H_
#define _X10_DEBUG_LINE_INFO_H_

#include <stdint.h>

#include "metadata_header.h"

/*
 * Structure describing X10 source files
 */
typedef struct {
  	uint32_t num_lines;    /* The number of lines in the X10 source file */
    uint32_t string_index; /* Index in _x10_strings of the name of the X10 source file. */
} _x10source_file_t;

/*
 * Structure that contains a cross reference of X10 statements
 * to the first C++ statement:
 * - If the first executable C++ statement corresponding to an X10 statement
 * is ambiguous due to platform or compiler differences, all possible first
 * statements are be present in the table. The debugger will use the first
 * one that maps to an instruction location.
 */
typedef struct {
  uint16_t x10_index;		/* Index of X10 file name in _x10_source_list */
  uint16_t x10_method;		/* Index into _x10_method_list of the
                               X10 method (see Method Mapping)	*/
  uint32_t cpp_index;		/* Index of C++ file name in the strings  */
  uint32_t x10_line;		/* Line number of X10 source file line	   */
  uint32_t cpp_from_line;	/* First line number of C++ line range */
  uint32_t cpp_to_line;		/* Last line number of C++ line range  */
} _x10_to_cpp_xref_t;

typedef struct {
  _x10_to_cpp_xref_t	x10_to_cpp_ref;
  uint32_t				first_step_over_line;
} _x10_to_cpp_xrefso_t;


/*
 * Structure that contains a cross reference of C++ statements to
 * X10 statements
 */
typedef struct {
  uint16_t x10_index;     /* Index of X10 file name in _x10_source_list	*/
  uint16_t x10_method;    /* Index into _X10methodNameList of the		*/
                          /* X10 method (see Method Mapping)			*/
  uint32_t cpp_index;     /* Index of C++ file name in the strings		*/
  uint32_t x10_line;      /* Line number of X10 line					*/
  uint32_t cpp_from_line; /* First line number of C++ line range		*/
  uint32_t cpp_to_line;   /* Last line number of C++ line range			*/
} _cpp_to_x10_xref_t;

/*
 * Structure containing X10 methods names
 */
typedef struct {
  uint32_t x10_class;          /* Index of the X10 containing class name in _x10_strings */
  uint32_t x10_method;         /* Index of the X10 method name in _x10_strings */
  uint32_t x10_return_type;    /* Index of the X10 return type in _x10_strings */
  uint64_t x10_args;           /* A pointer to a string that contains binary encodings of the */
                               /* argument indices.  Each group of 4 bytes represents the index */
                               /* of the corresponding argument in _x10_strings */
  uint32_t cpp_class;          /* Index of the C++ class name in _x10_strings */
  uint16_t x10_arg_count;      /* The number of X10 arguments */
  uint16_t line_index;         /* Index into _x10_to_cpp_list of the first line of the method */
}  _x10_method_name_t;

/*
 * Structure containing X10 methods name and line range
 */
typedef struct {
  _x10_method_name_t method_name; 
  uint32_t cpp_from_line;  		/* First line number of C++ line range		*/
  uint32_t cpp_to_line;    		/* Last line number of C++ line range		*/
  uint32_t cpp_index;           /* Index of C++ file name in _x10_strings	*/
}  _x10_method_t;

/*
 * Structure containing X10 local variable mapping
 * */
typedef struct {
	uint32_t x10_name;			/* Index of the X10 variable name in _x10_strings */
	uint32_t x10_type;          /* Classification of this type */
	int32_t x10_type_index; 	/* Index of the X10 type into appropriate _x10_class_map, _x10_closure_map  (if applicable) */
	uint32_t cpp_name;			/* Index of the C++ variable name in _x10_strings */
    uint32_t x10_index;         /* Index of X10 file name in _x10_source_list */
	uint32_t x10_start_line;    /* First line number of X10 line range */
	uint32_t x10_end_line;      /* Last line number of X10 line range */
}  _x10_local_var_map;

/*
 * The _x10_type_map and extenders of it are used in the member variable mappings
 */
typedef struct{
  uint32_t x10_type; /* Classification of this type */
  /* The details of the type follow.. */
} _x10_type_map;

typedef struct {
  	uint32_t x10_type;       	/* Classification of this type */
	int32_t x10_type_index;  	/* Index of the X10 type into appropriate _x10_type_map */
	uint32_t x10_member_name; 	/* Index of the X10 member name in _x10_strings */
  	uint32_t cpp_member_name; 	/* Index of the C++ member name in _x10_strings */
  	uint32_t cpp_class; 		/* Index of the C++ containing struct/class name in _x10_strings */
} _x10_type_member;

typedef struct {
  uint32_t x10_type; 				/* Classification of this type */
  uint32_t x10_name; 				/* Index of the X10 class type name in _X10 strings */
  uint32_t x10_size; 				/* number of bytes in the class */
  uint32_t x10_member_count; 		/* number of members in the class */
  _x10_type_member* x10_members;	/* pointer to an array of individual member types */
} _x10_class_map;

typedef struct {
  uint32_t x10_type; 				/* Classification of this type */
  uint32_t x10_name; 				/* Index of the X10 class type name in _X10 strings */
  uint32_t x10_size; 				/* number of bytes in the closure class */
  uint32_t x10_member_count;		/* number of members in the class */
  uint32_t x10_index;				/* Index of X10 file name in _x10_source_list */
  uint32_t x10_start_line;			/* the start line of the closure definition in the X10 source */
  uint32_t x10_end_line; 			/* the end line of the closure definition in the X10 source */
  _x10_type_member* x10_members;	/* pointer to an array of individual member types */
} _x10_closure_map;

typedef struct _x10_array_map {
	uint32_t x10_type; 		/* Classification of this type */
	int32_t x10_type_index;	/* Index of the X10 type into appropriate _x10_type_map (if applicable) */
} _x10_array_map;


typedef struct {
  uint32_t x10_type;			/* Classification of this type */
  uint32_t x10_referred_type;	/* type number of the referred type */
} _x10_ref_map;

typedef struct {
  uint32_t x10_type;			/* Classification of this type */
  uint32_t x10_referred_type;	/* type number of the referred type */
  uint32_t x10_name; 			/* Offset to the name of the typedef in _x10_strings */
} _x10_typedef_map;


typedef struct {
  MetaDataHeader header;				/* standard metadata header */

  /* The remainder of this structure is language/version specific */
  uint32_t x10_string_size;				/* the size in bytes of the string table (including the trailing NUL) */
  uint32_t x10_source_list_size;		/* the size in bytes of the X10 source list */
  uint32_t x10_to_cpp_list_size;		/* the size in bytes of the X10->C++ cross reference */
  uint32_t cpp_to_x10_xref_list_size;	/* the size in bytes of the C++->X10 cross reference */
  uint32_t x10_method_name_list_size;	/* the size in bytes of the X10 method name mapping list */
  uint32_t x10_local_var_list_size;		/* the size in bytes of the X10 local variable name mapping list */
  uint32_t x10_class_map_list_size;		/* the size in bytes of the X10 class mapping list */
  uint32_t x10_closure_map_list_size;	/* the size in bytes of the X10 async closure mapping list */
  uint32_t x10_array_map_list_size;	/* the size in bytes of the X10 array mapping list */

  uint32_t x10_source_array_size;   
  uint32_t x10_to_cpp_array_size;
  uint32_t cpp_to_x10_xref_array_size;
  uint32_t x10_method_mame_array_size;
  uint32_t x10_local_var_array_size;
  uint32_t x10_class_map_array_size;
  uint32_t x10_closure_map_array_size;
  uint32_t x10_array_map_array_size;

  char*           			x10_strings;
  _x10source_file_t * 		x10_source_list;
  _x10_to_cpp_xrefso_t *  	x10_to_cpp_list;
  _cpp_to_x10_xref_t *  	cpp_to_x10_xref_list;
  _x10_method_t * 			x10_method_list;
  _x10_local_var_map *   	x10_local_var_list;
  _x10_class_map *			x10_class_map_list;
  _x10_closure_map *		x10_closure_map_list;
  _x10_array_map *    		x10_array_map_list;
} _x10_meta_debug_info_t;


#endif /* _X10_DEBUG_LINE_INFO_H_ */

