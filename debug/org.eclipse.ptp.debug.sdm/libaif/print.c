/*
 * Routines to print AIF objects.
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
#include	<config.h>
#endif /* HAVE_CONFIG_H */

#include	<stdio.h>
#include	<stdlib.h>
#include	<string.h>

#include	"aif.h"
#include	"aifint.h"
#include	"aiferr.h"

int
AIFPrint(FILE *fp, int depth, AIF *a)
{
        char *  fmt;
        char *  data;

        if ( a == (AIF *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
                return -1;
	}

        fmt = AIF_FORMAT(a);
        data = AIF_DATA(a);

        return _aif_print(fp, depth, &fmt, &data);
}

/* 
 * Prints the object in human-readable form to fp, advances fds and data to
 * the first location after the part printed.  Arithmetic values are given at
 * least len bytes of significance, more if the fds indicates.
 */
int
_aif_print(FILE *fp, int depth, char **fds, char **data)
{
        char * tmp;

        _fds_resolve(fds);

        switch ( FDSType(*fds) )
        {
        case AIF_BOOLEAN:
                return _aif_print_bool(fp, depth, fds, data);

        case AIF_CHARACTER:
                return _aif_print_char(fp, depth, fds, data);

        case AIF_INTEGER:
                return _aif_print_int(fp, depth, fds, data);

        case AIF_FLOATING:
                return _aif_print_float(fp, depth, fds, data);

        case AIF_ARRAY:
                return _aif_print_array(fp, depth, fds, data);

        case AIF_POINTER:
                return _aif_print_pointer(fp, depth, fds, data);

        case AIF_REGION:
                return _aif_print_region(fp, depth, fds, data);

        case AIF_AGGREGATE:
                return _aif_print_aggregate(fp, depth, fds, data);

        case AIF_NAME:
                return _aif_print_name(fp, depth, fds, data);

        case AIF_REFERENCE:
                tmp = _fds_lookup(fds);
                return _aif_print(fp, depth, &tmp, data);

        case AIF_STRING:
                return _aif_print_string(fp, depth, fds, data);

        case AIF_ENUM:
                return _aif_print_enum(fp, depth, fds, data);

        case AIF_UNION:
                return _aif_print_union(fp, depth, fds, data);

        case AIF_VOID:
                return _aif_print_void(fp, depth, fds, data);

        case AIF_FUNCTION:
                return _aif_print_function(fp, depth, fds, data);

        default:
		SetAIFError(AIFERR_TYPE, NULL);
                break;
        }

	return -1;
}

int
_aif_print_bool(FILE *fp, int depth, char **fds, char **data)
{
	char *	str;
	
	_str_init();

	if ( _aif_bool_to_str(depth, fds, data) < 0 )
		return -1;

	str = _str_get();

	fprintf(fp, "%s", str);

	_aif_free(str);

	return 0;
}

int
_aif_print_char(FILE *fp, int depth, char **fds, char **data)
{
	char *	str;
	
	_str_init();

	if ( _aif_char_to_str(depth, fds, data) < 0 )
		return -1;

	str = _str_get();

	fprintf(fp, "%s", str);

	_aif_free(str);

	return 0;
}

int
_aif_print_int(FILE *fp, int depth, char **fds, char **data)
{
	char *	str;
	
	_str_init();

	if ( _aif_int_to_str(depth, fds, data) < 0 )
		return -1;

	str = _str_get();

	fprintf(fp, "%s", str);

	_aif_free(str);

	return 0;
}

int
_aif_print_float(FILE *fp, int depth, char **fds, char **data)
{
	char *	str;
	
	_str_init();

	if ( _aif_float_to_str(depth, fds, data) < 0 )
		return -1;

	str = _str_get();

	fprintf(fp, "%s", str);

	_aif_free(str);

	return 0;
}

int
_aif_print_array(FILE *fp, int depth, char **fds, char **data)
{
	char *	str;
	
	_str_init();

	if ( _aif_array_to_str(depth, fds, data) < 0 )
		return -1;

	str = _str_get();

	fprintf(fp, "%s", str);

	_aif_free(str);

	return 0;
}

int
_aif_print_pointer(FILE *fp, int depth, char **fds, char **data)
{
	char *	str;
	
	_str_init();

	if ( _aif_pointer_to_str(depth, fds, data) < 0 )
		return -1;

	str = _str_get();

	fprintf(fp, "%s", str);

	_aif_free(str);

	return 0;
}

int
_aif_print_region(FILE *fp, int depth, char **fds, char **data)
{
	char *	str;
	
	_str_init();

	if ( _aif_region_to_str(depth, fds, data) < 0 )
		return -1;

	str = _str_get();

	fprintf(fp, "%s", str);

	_aif_free(str);

	return 0;
}

int
_aif_print_aggregate(FILE *fp, int depth, char **fds, char **data)
{
	char *	str;
	
	_str_init();

	if ( _aif_aggregate_to_str(depth, fds, data) < 0 )
		return -1;

	str = _str_get();

	fprintf(fp, "%s", str);

	_aif_free(str);

	return 0;
}

int
_aif_print_name(FILE *fp, int depth, char **fds, char **data)
{
	char *	str;
	
	_str_init();

	if ( _aif_name_to_str(depth, fds, data) < 0 )
		return -1;

	str = _str_get();

	fprintf(fp, "%s", str);

	_aif_free(str);

	return 0;
}

int
_aif_print_reference(FILE *fp, int depth, char **fds, char **data)
{
	char *	str;
	
	_str_init();

	if ( _aif_reference_to_str(depth, fds, data) < 0 )
		return -1;

	str = _str_get();

	fprintf(fp, "%s", str);

	_aif_free(str);

	return 0;
}

int
_aif_print_string(FILE *fp, int depth, char **fds, char **data)
{
	char *	str;
	
	_str_init();

	if ( _aif_string_to_str(depth, fds, data) < 0 )
		return -1;

	str = _str_get();

	fprintf(fp, "%s", str);

	_aif_free(str);

	return 0;
}

int
_aif_print_enum(FILE *fp, int depth, char **fds, char **data)
{
	char *	str;
	
	_str_init();

	if ( _aif_enum_to_str(depth, fds, data) < 0 )
		return -1;

	str = _str_get();

	fprintf(fp, "%s", str);

	_aif_free(str);

	return 0;
}

int
_aif_print_union(FILE *fp, int depth, char **fds, char **data)
{
	char *	str;
	
	_str_init();

	if ( _aif_union_to_str(depth, fds, data) < 0 )
		return -1;

	str = _str_get();

	fprintf(fp, "%s", str);

	_aif_free(str);

	return 0;
}

int
_aif_print_void(FILE *fp, int depth, char **fds, char **data)
{
	char *	str;
	
	_str_init();

	if ( _aif_void_to_str(depth, fds, data) < 0 )
		return -1;

	str = _str_get();

	fprintf(fp, "%s", str);

	_aif_free(str);

	return 0;
}

int
_aif_print_function(FILE *fp, int depth, char **fds, char **data)
{
	char *	str;
	
	_str_init();

	if ( _aif_function_to_str(depth, fds, data) < 0 )
		return -1;

	str = _str_get();

	fprintf(fp, "%s", str);

	_aif_free(str);

	return 0;
}

int
AIFPrintBool(FILE *fp, int depth, AIF *a)
{
	char *	fmt;
	char *	data;

	if ( a == (AIF *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
                return -1;
	}

	fmt = AIF_FORMAT(a);
	data = AIF_DATA(a);

	return _aif_print_bool(fp, depth, &fmt, &data);
}

int
AIFPrintChar(FILE *fp, int depth, AIF *a)
{
	char *	fmt;
	char *	data;

	if ( a == (AIF *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
                return -1;
	}

	fmt = AIF_FORMAT(a);
	data = AIF_DATA(a);

	return _aif_print_char(fp, depth, &fmt, &data);
}

int
AIFPrintInt(FILE *fp, int depth, AIF *a)
{
	char *	fmt;
	char *	data;

	if ( a == (AIF *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
                return -1;
	}

	fmt = AIF_FORMAT(a);
	data = AIF_DATA(a);

	return _aif_print_int(fp, depth, &fmt, &data);
}

int
AIFPrintFloat(FILE *fp, int depth, AIF *a)
{
	char *	fmt;
	char *	data;

	if ( a == (AIF *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
                return -1;
	}

	fmt = AIF_FORMAT(a);
	data = AIF_DATA(a);

	return _aif_print_float(fp, depth, &fmt, &data);
}

int
AIFPrintArray(FILE *fp, int depth, AIF *a)
{
	char *	fmt;
	char *	data;

	if ( a == (AIF *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
                return -1;
	}

	fmt = AIF_FORMAT(a);
	data = AIF_DATA(a);

	return _aif_print_array(fp, depth, &fmt, &data);
}

int
AIFPrintPointer(FILE *fp, int depth, AIF *a)
{
	char *	fmt;
	char *	data;

	if ( a == (AIF *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
                return -1;
	}

	fmt = AIF_FORMAT(a);
	data = AIF_DATA(a);

	return _aif_print_pointer(fp, depth, &fmt, &data);
}

int
AIFPrintRegion(FILE *fp, int depth, AIF *a)
{
	char *	fmt;
	char *	data;

	if ( a == (AIF *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
                return -1;
	}

	fmt = AIF_FORMAT(a);
	data = AIF_DATA(a);

	return _aif_print_region(fp, depth, &fmt, &data);
}

int
AIFPrintAggregate(FILE *fp, int depth, AIF *a)
{
	char *	fmt;
	char *	data;

	if ( a == (AIF *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
                return -1;
	}

	fmt = AIF_FORMAT(a);
	data = AIF_DATA(a);

	return _aif_print_aggregate(fp, depth, &fmt, &data);
}

int
AIFPrintName(FILE *fp, int depth, AIF *a)
{
	char *	fmt;
	char *	data;

	if ( a == (AIF *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
                return -1;
	}

	fmt = AIF_FORMAT(a);
	data = AIF_DATA(a);

	return _aif_print_name(fp, depth, &fmt, &data);
}

int
AIFPrintReference(FILE *fp, int depth, AIF *a)
{
	char *	fmt;
	char *	data;

	if ( a == (AIF *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
                return -1;
	}

	fmt = AIF_FORMAT(a);
	data = AIF_DATA(a);

	return _aif_print_reference(fp, depth, &fmt, &data);
}

int
AIFPrintString(FILE *fp, int depth, AIF *a)
{
	char *	fmt;
	char *	data;

	if ( a == (AIF *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
                return -1;
	}

	fmt = AIF_FORMAT(a);
	data = AIF_DATA(a);

	return _aif_print_string(fp, depth, &fmt, &data);
}

int
AIFPrintEnum(FILE *fp, int depth, AIF *a)
{
	char *	fmt;
	char *	data;

	if ( a == (AIF *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
                return -1;
	}

	fmt = AIF_FORMAT(a);
	data = AIF_DATA(a);

	return _aif_print_enum(fp, depth, &fmt, &data);
}

int
AIFPrintUnion(FILE *fp, int depth, AIF *a)
{
	char *	fmt;
	char *	data;

	if ( a == (AIF *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
                return -1;
	}

	fmt = AIF_FORMAT(a);
	data = AIF_DATA(a);

	return _aif_print_union(fp, depth, &fmt, &data);
}

int
AIFPrintVoid(FILE *fp, int depth, AIF *a)
{
	char *	fmt;
	char *	data;

	if ( a == (AIF *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
                return -1;
	}

	fmt = AIF_FORMAT(a);
	data = AIF_DATA(a);

	return _aif_print_void(fp, depth, &fmt, &data);
}

int
AIFPrintFunction(FILE *fp, int depth, AIF *a)
{
	char *	fmt;
	char *	data;

	if ( a == (AIF *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
                return -1;
	}

	fmt = AIF_FORMAT(a);
	data = AIF_DATA(a);

	return _aif_print_function(fp, depth, &fmt, &data);
}

int
AIFPrintArrayIndex(FILE *fp, AIFIndex *ix)
{
	char *	str;

	if ( ix == (AIFIndex *)NULL )
	{
		SetAIFError(AIFERR_BADARG, NULL);
                return -1;
	}

	if ( AIFArrayIndexToStr(&str, ix) < 0 )
		return -1;

	fprintf(fp, "%s", str);

	_aif_free(str);

	return 0;
}

