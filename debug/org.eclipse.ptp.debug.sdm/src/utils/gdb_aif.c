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

#ifdef __gnu_linux__
#define _GNU_SOURCE
#endif /* __gnu_linux__ */

#include "config.h"

#include <stdlib.h>
#include <unistd.h>
#include <string.h>

#include "dbg.h"
#include "dbg_error.h"
#include "gdb.h"

/********************************************************
 * TYPE CONVERSION
 ********************************************************/
#define T_UNKNOWN		0
/*
 * Simple types
 */
#define T_CHAR			1
#define T_SHORT			2
#define T_USHORT		3
#define T_INT			4
#define T_UINT			5
#define T_LONG			6
#define T_ULONG			7
#define T_LONGLONG		8
#define T_ULONGLONG		9
#define T_FLOAT			10
#define T_DOUBLE		11
#define T_STRING		12
#define T_BOOLEAN		13
/*
 * Complex (structure) types
 */
#define T_CHAR_PTR		14
#define T_FUNCTION		15
#define T_VOID_PTR		16
#define T_UNION			17
#define T_ENUM			18
#define T_ARRAY			19
#define T_STRUCT		20
#define T_POINTER		21
#define T_CLASS			22

static int
GetSimpleType(char *type)
{
	char *	t = type;
	int		len = strlen(type);

	if (strncmp(type, "void *", 6) == 0) { // void pointer
		return T_VOID_PTR;
	}
	if (strncmp(type, "enum", 4) == 0) { // enum
		return T_ENUM;
	}

	//check modifiers
	if (strncmp(type, "const volatile", 14) == 0) {
		t = &type[15]; //+ 1 remove whitespeace
	} else if (strncmp(type, "volatile", 8) == 0) {
		t = &type[9]; //+ 1 remove whitespeace
	} else if (strncmp(type, "const", 5) == 0) {
		t = &type[6]; //+ 1 remove whitespeace
	}

	if (strncmp(t, "char *", 6) == 0) {
		return T_CHAR_PTR;
	}
	if (strncmp(t, "char", 4) == 0 ||
		strncmp(t, "unsigned char", 13) == 0) ||
		strncmp(t, "integer*1", 9) == 0) ||
		strncmp(t, "integer(kind=3)", 15) == 0) ||
		strncmp(t, "integer(kind=1)", 15) == 0) ||
		strncmp(t, "character(kind=1)", 15) == 0) ||
		strncmp(t, "character(kind=3)", 15) == 0) {
		return T_CHAR;
	}
	if (strncmp(t, "short int", 9) == 0 ||
		strncmp(t, "int2", 4) == 0) ||
		strncmp(t, "integer*2", 9) == 0) ||
		strncmp(t, "integer(kind=5)", 15) == 0) {
		return T_SHORT;
	}
	if (strncmp(t, "short unsigned int", 18) == 0) {
		return T_USHORT;
	}
	if (strncmp(t, "unsigned int", 12) == 0) {
		return T_UINT;
	}
	if (strncmp(t, "long int", 8) == 0 ||
		strncmp(t, "long", 4) == 0 ||
		strncmp(t, "real*4", 6) == 0 ||
		strncmp(t, "int8", 4) == 0) ||
		strncmp(t, "integer(kind=7)", 15) == 0) ||
		strncmp(t, "integer(kind=2)", 15) == 0) {
		return T_LONG;
	}
	if (strncmp(t, "long unsigned int", 17) == 0) {
		return T_ULONG;
	}
#ifdef CC_HAS_LONG_LONG
	if (strncmp(t, "long long int", 13) == 0 ||
		return T_LONGLONG;
	}
	if (strncmp(t, "long long unsigned int", 22) == 0) {
		return T_ULONGLONG;
	}
#endif /* CC_HAS_LONG_LONG */
	if (strncmp(t, "float", 5) == 0 ||
		strncmp(t, "real*4", 6) == 0) ||
		strncmp(t, "real(kind=1)", 12) == 0) {
		return T_FLOAT;
	}
	if (strncmp(t, "double", 6) == 0) {
		strncmp(t, "real*8", 6) == 0) ||
		strncmp(t, "real(kind=2)", 12) == 0) {
		return T_DOUBLE;
	}
	if (strncmp(t, "string", 6) == 0) {
		return T_STRING;
	}
	if (strncmp(t, "bool", 4) == 0 ||
		strncmp(t, "logical4", 8) == 0) ||
		strncmp(t, "logical*", 8) == 0) ||
		strncmp(t, "logical(kind=", 13) == 0) {
 		return T_BOOLEAN;
 	}
	/*
	 * Catch all: must come after other integer tests
	 */
	if (strncmp(t, "int", 3) == 0 ||
		strncmp(t, "int4", 4) == 0 ||
		strncmp(t, "integer", 7) == 0) {
		return T_INT;
	}
	/*
	 * Must come after simple types to avoid matching GNU extensions
	 */
	if (type[len - 1] == ')') { // function
		return T_FUNCTION;
	}

	return  T_UNKNOWN;
}

/*
 * Attempt to determine the type of a variable from a type string. Assumes
 * that the type is structured. Returns T_UNKNOWN if the type can't be determined.
 *
 * Possible return values:
 * 	T_ARRAY
 * 	T_POINTER
 * 	T_CHAR_PTR
 * 	T_UNION
 * 	T_STRUCT
 * 	T_CLASS
 * 	T_UNKNOWN
 */
static int
GetComplexType(char *type)
{
	int len = strlen(type);

	DEBUG_PRINTF(DEBUG_LEVEL_BACKEND, "---------------------- GetComplexType (%s)\n", type);

	switch (type[len - 1]) {
	case ']':
		return T_ARRAY;
	case '*':
		if (type[len - 2] == '*') { //pointer pointer
			return T_POINTER;
		}
		if (strncmp(type, "char", 4) == 0) { //char pointer
			return T_CHAR_PTR;
		}
		return T_POINTER; //normal pointer
	default:
		if (strncmp(type, "union", 5) == 0) {
			return T_UNION;
		}
		if (strncmp(type, "struct", 6) == 0) {
			return T_STRUCT;
		}
		if (strncmp(type, "class", 5) == 0) {
			return T_CLASS;
		}
		return T_UNKNOWN;
	}
}

static int
GetType(char *type)
{
	int id = GetSimpleType(type);
	if (id == T_UNKNOWN) {
		id = GetComplexType(type);
	}
	return id;
}

/*
 * Find the base type of the pointer type.
 * Assumes type string is '<base type>*'
 */
static int
GetPointerBaseType(MISession *session, MIVar *var)
{
	int		id;
	char *	p;
	char *	str = strdup(var->type);

	/*
	 * Find first non space character before the '*'
	 */
	p = &str[strlen(str) - 1];
	*p-- = '\0';
	while (p != str && *p == ' ') {
		*p-- = '\0';
	}

	id = GetType(str);
	if (id == T_UNKNOWN) {
		p = GetPtypeValue(session, NULL, var);
		if (p != NULL) {
			id = GetType(p);
			free(p);
		}
	}

	free(str);

	return id;
}

/*
 * Get the base type of an array from it's type
 * definition string. This is only used if it's
 * not possible to get the type of one of the
 * array elements.
 */
static int
GetArrayBaseType(MISession *session, MIVar *var)
{
	int		id;
	char *	p;
	char *	str = strdup(var->type);

	p = strchr(str, '[');
	*p-- = '\0';
	while (p != str && *p == ' ') {
		*p-- = '\0';
	}

	id = GetType(str);
	if (id == T_UNKNOWN) {
		p = GetPtypeValue(session, NULL, var);
		if (p != NULL) {
			id = GetType(p);
			free(p);
		}
	}

	free(str);

	return id;
}

/*
 * Get the name of a type (struct, union or enum) from the
 * MI type string.
 *
 * Returns the type name or NULL if the type is unnamed.
 */
static char *
GetTypeName(char *type)
{
	char *	p;

	if ((p = strchr(type, ' ')) != NULL) {
		p++;
		if (strcmp(p, "{...}") == 0) {
			return NULL;
		}
		return p;
	}
	return type;
}

/*
 * Convert an access qualifier string to AIFAccess.
 *
 * If the string is empty, then the qualifier is assumed to be AIF_ACCESS_PACKAGE.
 *
 * Any other string will be treated as AIF_ACCESS_UNKNOWN
 */
static AIFAccess
GetAccessQualifier(char *access)
{
	if (strcmp(access, "private") == 0) {
		return AIF_ACCESS_PRIVATE;
	} else if (strcmp(access, "protected") == 0) {
		return AIF_ACCESS_PROTECTED;
	} else if (strcmp(access, "public") == 0) {
		return AIF_ACCESS_PUBLIC;
	} else if (*access == '\0') {
		return AIF_ACCESS_PACKAGE;
	}

	return AIF_ACCESS_UNKNOWN;
}

static AIF *
CreateSimpleAIF(int id, char *res)
{
	char *pch;

	if (res == NULL) {
		return NULL;
	}
	switch (id) {
		case T_STRING:
		case T_CHAR:
			if ((pch = strchr(res, ' ')) != NULL) {
				pch++;
				if (*pch == '\'') { //character
					pch--;
					*pch = '\0';
					return CharToAIF((char)strtol(res, NULL, 10));
				}
				else { //string
					return StringToAIF(pch);
				}
			}
			else {
				return CharToAIF((char)strtol(res, NULL, 10));
			}
			break;
		case T_SHORT:
			return ShortToAIF((short)strtol(res, NULL, 10));
		case T_USHORT:
			return UnsignedShortToAIF((unsigned short)strtoul(res, NULL, 10));
		case T_INT:
			return IntToAIF((int)strtol(res, NULL, 10));
		case T_UINT:
			return UnsignedIntToAIF((unsigned int)strtoul(res, NULL, 10));
		case T_LONG:
			return LongToAIF(strtol(res, NULL, 10));
		case T_ULONG:
			return UnsignedLongToAIF((unsigned long)strtoul(res, NULL, 10));
		#ifdef CC_HAS_LONG_LONG
			case T_LONGLONG:
				return LongLongToAIF(strtoll(res, NULL));
			case T_ULONGLONG:
				return UnsignedLongLongToAIF((unsigned long long)strtoull(res, NULL));
		#endif /* CC_HAS_LONG_LONG */
		case T_FLOAT:
			return FloatToAIF((float)strtod(res, NULL));
		case T_DOUBLE:
			return DoubleToAIF(strtod(res, NULL));
		case T_BOOLEAN:
			return BoolToAIF(strncmp(res, "true", 4) == 0 ? 1 : 0);
		default://other type
			return VoidToAIF(0, 0);
	}
}

static void
AddFieldToAggregate(MISession *session, AIF *a, MIVar *var, AIFAccess access, int named)
{
	AIF *	ac = GetAIF(session, var, named);
	if (ac != NULL) {
		DEBUG_PRINTF(DEBUG_LEVEL_BACKEND, "---------------------- AddFieldToAggregate adding field %s = (%s, %d)\n", var->exp, AIF_FORMAT(ac), AIF_LEN(ac));
		AIFAddFieldToAggregate(a, access, var->exp, ac);
		AIFFree(ac);
	}
}

/*
 * Get information from the "fake" child that is used
 * to contain the field information for a particular
 * access type.
 */
static void
GetAggregateFields(MISession *session, AIF *a, char *name, AIFAccess access, int named)
{
	int			i;
	MIVar *		var;

	DEBUG_PRINTF(DEBUG_LEVEL_BACKEND, "---------------------- GetAggregateFields (%s, %d)\n", name, access);

	var = GetMIVarClassFields(session, name);

	if (var != NULL) {
		if (var->children != NULL) {
			for (i = 0; i < var->numchild; i++) {
				AddFieldToAggregate(session, a, var->children[i], access, named);
			}
		}

		MIVarFree(var);
	}
}

static void
AddPartialFieldToAggregate(MISession *session, char *field, AIF *a, MIVar *var, AIFAccess access)
{
	AIF *	ac = GetPartialAIF(session, field, var);
	if (ac != NULL) {
		DEBUG_PRINTF(DEBUG_LEVEL_BACKEND, "---------------------- AddPartialFieldToAggregate adding field %s = (%s, %d)\n", var->exp, AIF_FORMAT(ac), AIF_LEN(ac));
		AIFAddFieldToAggregate(a, access, var->exp, ac);
		AIFFree(ac);
	} else {
		DEBUG_PRINTF(DEBUG_LEVEL_BACKEND, "---------------------- AddPartialFieldToAggregate field %s was null!\n", field);
	}
}

/*
 * Get information from the "fake" child that is used
 * to contain the field information for a particular
 * access type.
 */
static void
GetPartialAggregateFields(MISession *session, char *expr, AIF *a, char *name, AIFAccess access)
{
	int			i;
	MIVar *		var;
	char *		field;

	DEBUG_PRINTF(DEBUG_LEVEL_BACKEND, "---------------------- GetPartialAggregateFields (%s, %s, %d)\n", expr != NULL ? expr : "NULL", name, access);

	var = GetMIVarClassFields(session, name);

	if (var != NULL) {
		if (var->children != NULL) {
			for (i = 0; i < var->numchild; i++) {
				asprintf(&field, "(%s).%s", expr, var->children[i]->exp);
				AddPartialFieldToAggregate(session, field, a, var->children[i], access);
				free(field);
			}
		}

		MIVarFree(var);
	}
}

/*********************************
 * EXPORTED FUNCTIONS START HERE *
 *********************************/

int
GetAIFVar(MISession *session, char *var, AIF **val, char **type)
{
	AIF * res;
	MIVar *mivar;

	mivar = CreateMIVar(session, var);
	if (mivar == NULL) {
		DbgSetError(DBGERR_UNKNOWN_VARIABLE, var);
		return DBGRES_ERR;
	}

	if ((res = GetAIF(session, mivar, 0)) == NULL) {
		DbgSetError(DBGERR_UNKNOWN_TYPE, mivar->type);
		DeleteMIVar(session, mivar->name);
		MIVarFree(mivar);
		return DBGRES_ERR;
	}
	*type = strdup(mivar->type);
	*val = res;

	DeleteMIVar(session, mivar->name);
	MIVarFree(mivar);

	return DBGRES_OK;
}

/*
 * Create an AIF object that is a pointer to 'base'. The hexidecimal
 * address of the pointer is passed in 'addr'.
 */
AIF *
CreateAIFPointer(MISession *session, char *addr, AIF *base)
{
	AIF *ac;
	AIF *a;
	char *pch;

	if (addr == NULL) {
		ac = VoidToAIF(0, 0);
	} else {
		if ((pch = strchr(addr, ' ')) != NULL) {
			*pch = '\0';
		}
		addr += 2; //skip 0x
		ac = AddressToAIF(addr, GetAddressLength(session));
	}
	a = PointerToAIF(ac, base);
	AIFFree(ac);
	return a;
}

/*
 * Create an AIF char pointer from the results of
 * an MI -var-list-children command.
 *
 * Possible values for 'res' are:
 *
 * "" 				- invalid/uninitialized pointer
 * "0x0" 			- null pointer
 * "0xaddr \"str\"" - address and string
 */
AIF *
CreateAIFCharPointer(MISession *session, char *res)
{
	char *	pch;
	char *	val;
	char *	addr;
	AIF *	a;
	AIF *	ac;

	if (*res == '\0') {
		addr = strdup("0");
		val = "";
	} else {
		addr = strdup(res + 2);  //skip 0x
		if ((pch = strchr(addr, ' ')) != NULL) {
			*pch++ = '\0';
			val = pch;
		} else {
			val = "";
		}
	}

	ac = AddressToAIF(addr, GetAddressLength(session));
	a = CharPointerToAIF(ac, val);
	free(addr);
	AIFFree(ac);
	return a;
}

AIF *
GetSimpleAIF(MISession *session, MIVar *var)
{
	AIF *	a = NULL;
	AIF *	ac;
	char *	pt;
	char *	v;

	DEBUG_PRINTF(DEBUG_LEVEL_BACKEND, "---------------------- GetSimpleAIF (%s)\n", var->type);

	int id = GetSimpleType(var->type);
	if (id == T_UNKNOWN) {
		pt = GetPtypeValue(session, NULL, var);
		if (pt != NULL) {
			if (var->type != NULL) {
				free(var->type);
			}
			var->type = pt;
			id = GetSimpleType(var->type);
		}
	}
	switch (id) {
	case T_FUNCTION:
		ac = MakeAIF(AIF_FUNCTION_TYPE("is4"), ""); /* TODO: get real type */
		v = GetVarValue(session, var->name);
		a = CreateAIFPointer(session, v, ac);
		return a;
	case T_VOID_PTR:
		ac = VoidToAIF(0, 0);
		v = GetVarValue(session, var->name);
		a = CreateAIFPointer(session, v, ac);
		free(v);
		AIFFree(ac);
		return a;
	case T_ENUM:
		return EmptyEnumToAIF(GetTypeName(var->type));
	case T_UNKNOWN:
		/*
		 * Unrecognized type. Need a better way to handle this.
		 */
		return VoidToAIF(0, 0);
	default:
		v = GetVarValue(session, var->name);
		a = CreateSimpleAIF(id, v);
		free(v);
		return a;
	}
}

AIF *
GetStructAIF(MISession *session, MIVar *var, int named)
{
	int		i;
	MIVar *	v;
	AIF *	a;
	AIF *	ac;

	named++;

	a = EmptyAggregateToAIF(GetTypeName(var->type));

	for (i=0; i < var->numchild; i++) {
		v = var->children[i];
		//check whether child contains parent
		if (strcmp(var->type, v->type) == 0 && strcmp(var->name, v->name)) {
			NameAIF(a, named);
			ac = AIFNullPointer(a);
		} else if ((ac = GetAIF(session, v, named)) == NULL) {
			AIFFree(a);
			return NULL;
		}
		AIFAddFieldToAggregate(a, AIF_ACCESS_PUBLIC, v->exp, ac);
	}
	return a;
}

AIF *
GetClassAIF(MISession *session, MIVar *var, int named)
{
	int			i;
	MIVar *		child;
	AIFAccess	access;
	AIF *		a;

	named++;

	a = EmptyAggregateToAIF(GetTypeName(var->type));

	for (i=0; i < var->numchild; i++) {
		child = var->children[i];
		access = GetAccessQualifier(child->exp);
		if (access != AIF_ACCESS_UNKNOWN) {
			GetAggregateFields(session, a, child->name, access, named);
		} else {
			AddFieldToAggregate(session, a, child, AIF_ACCESS_PUBLIC, named);
		}
	}
	return a;
}

AIF *
GetUnionAIF(MISession *session, MIVar *var, int named)
{
	int		i;
	MIVar *	v;
	AIF *	a;
	AIF *	ac;

	named++;

	a = EmptyUnionToAIF(GetTypeName(var->type));

	for (i=0; i<var->numchild; i++) {
		v = var->children[i];
		//check whether child contains parent
		if (strcmp(var->type, v->type) == 0 && strcmp(var->name, v->name)) {
			NameAIF(a, named);
			ac = AIFNullPointer(a);
		} else if ((ac = GetAIF(session, v, named)) == NULL) {
			AIFFree(a);
			return NULL;
		}
		AIFAddFieldToUnion(a, v->exp, ac);
		AIFFree(ac);
	}
	return a;
}

AIF *
GetArrayAIF(MISession *session, MIVar *var, int named)
{
	int		i;
	int		id;
	MIVar *	v;
	AIF *	a = NULL;
	AIF *	ac;

	if (var->numchild <= 0) {
		id = GetArrayBaseType(session, var);
		ac = CreateSimpleAIF(id, "");
		a = EmptyArrayToAIF(0, 0, ac);
		AIFFree(ac);
	} else {
		for (i = 0; i < var->numchild; i++) {
			v = var->children[i];
			if ((ac = GetAIF(session, v, named)) == NULL) {
				return NULL;
			}
			if (a == NULL) {
				a = EmptyArrayToAIF(0, var->numchild, ac);
			}
			AIFAddArrayElement(a, i, ac);
			AIFFree(ac);
		}
	}
	return a;
}

AIF *
GetPointerAIF(MISession *session, MIVar *var, int named)
{
	AIF *	ac = NULL;
	AIF *	a;
	char *	v;
	int		id;

	id = GetPointerBaseType(session, var);

	switch (id) {
		case T_CHAR_PTR:
			v = GetVarValue(session, var->children[0]->name);
			a = CreateAIFCharPointer(session, v);
			free(v);
			return a;
		case T_UNION:
			ac = GetUnionAIF(session, var, named);
			break;
		case T_STRUCT:
			ac = GetStructAIF(session, var, named);
			break;
		default:
			if (var->numchild == 1) {
				ac = GetAIF(session, var->children[0], named);
			}
			break;
	}

	if (ac == NULL) {
		ac = VoidToAIF(0, 0);
	}
	v = GetVarValue(session, var->name);
	a = CreateAIFPointer(session, v, ac);
	free(v);
	AIFFree(ac);
	return a;
}

AIF *
GetComplexAIF(MISession *session, MIVar *var, int named)
{
	char *	v;
	char *	pt;
	AIF *	a = NULL;

	int id = GetComplexType(var->type);
	if (id == T_UNKNOWN) {
		pt = GetPtypeValue(session, NULL, var);
		if (pt != NULL) {
			if (var->type != NULL) {
				free(var->type);
			}
			var->type = pt;
			id = GetComplexType(var->type);
		}
	}

	switch (id) {
	case T_ARRAY:
		a = GetArrayAIF(session, var, named);
		break;
	case T_CHAR_PTR:
		v = GetVarValue(session, var->name);
		a = CreateAIFCharPointer(session, v);
		free(v);
		break;
	case T_POINTER:
		a = GetPointerAIF(session, var, named);
		break;
	case T_UNION:
		a = GetUnionAIF(session, var, named);
		break;
	case T_STRUCT:
		a = GetStructAIF(session, var, named);
		break;
	case T_CLASS:
		a = GetClassAIF(session, var, named);
		break;
	default:
		/*
		 * Maybe it was simple all along
		 */
		a = GetSimpleAIF(session, var);
	}
	return a;
}

AIF *
GetAIF(MISession *session, MIVar *var, int named)
{
	MICommand	*cmd;

	if (strcmp(var->type, "<text variable, no debug info>") == 0) {
		DbgSetError(DBGERR_NOSYMS, "");
		return NULL;
	}
	if (var->numchild == 0) { //simple type
		return GetSimpleAIF(session, var);
	}
	//complex type
	cmd = MIVarListChildren(var->name);
	SendCommandWait(session, cmd);
	if (!MICommandResultOK(cmd)) {
		if (MICommandResultClass(cmd) == MIResultRecordERROR) {
			char *err = MICommandResultErrorMessage(cmd);
			if (err != NULL) {
				DbgSetError(DBGERR_DEBUGGER, err);
				free(err);
			} else {
				DbgSetError(DBGERR_DEBUGGER, "got error from gdb, but no message");
			}
		} else {
			DbgSetError(DBGERR_DEBUGGER, "bad response from gdb");
		}
		MICommandFree(cmd);
		return NULL;
	}
	MIGetVarListChildrenInfo(cmd, var);
	MICommandFree(cmd);

	return GetComplexAIF(session, var, named);
}

/*************************** PARTIAL AIF ***************************/

/*
 * Create an array type corresponding to 'var'.
 */
AIF *
GetPartialArrayAIF(MISession *session, char *expr, MIVar *var)
{
	AIF *	a = NULL;
	AIF *	ac;
	int 	i;
	int 	id;

	DEBUG_PRINTF(DEBUG_LEVEL_BACKEND, "---------------------- GetPartialArrayAIF (%s, %s)\n", expr != NULL ? expr : "NULL", var->type);

	if (var->children == NULL || var->numchild <= 0) {
		id = GetArrayBaseType(session, var);
		ac = CreateSimpleAIF(id, "");
		a = EmptyArrayToAIF(0, var->numchild > 0 ? var->numchild : 0, ac);
		AIFFree(ac);
	} else {
		for (i = 0; i < var->numchild; i++) {
			ac = GetPartialAIF(session, expr, var->children[i]);
			if (ac != NULL) {
				if (a == NULL) {
					a = EmptyArrayToAIF(0, var->numchild, ac);
				}
				AIFAddArrayElement(a, i, ac);
				AIFFree(ac);
			}
		}
	}
	return a;
}

/*
 * Create an aggregate type corresponding to 'var'. The only difference
 * between a class and a struct aggregate type is that classes can have
 * members with access qualifiers other than public.
 *
 * An MI class variable has up to three children, one for
 * each access type "public", "protected", and "private".
 * These children are "fake" in that they do not correspond
 * to normal variables, but contain one child for each field
 * for the access type.
 */
AIF *
GetPartialAggregateAIF(MISession *session, char *expr, MIVar *var)
{
	AIF *		a;
	char *		field;
	int			i;
	MIVar *		child;
	AIFAccess	access;

	DEBUG_PRINTF(DEBUG_LEVEL_BACKEND, "---------------------- GetPartialAggregateAIF (%s, %s)\n", expr != NULL ? expr : "NULL", var->type);

	a = EmptyAggregateToAIF(GetTypeName(var->type));

	if (var->children != NULL) {
		for (i = 0; i < var->numchild; i++) {
			child = var->children[i];
			if (child->type == NULL) {
				access = GetAccessQualifier(child->exp);
				if (access != AIF_ACCESS_UNKNOWN) {
					GetPartialAggregateFields(session, expr, a, child->name, access);
					continue;
				}
			}
			if (strcmp(child->name, child->type) == 0) { // base type
				asprintf(&field, "(struct %s).%s", expr, child->exp);
				AddPartialFieldToAggregate(session, field, a, child, AIF_ACCESS_PUBLIC);
				free(field);
			} else {
				asprintf(&field, "(%s).%s", expr, child->exp);
				AddPartialFieldToAggregate(session, field, a, child, AIF_ACCESS_PUBLIC);
				free(field);
			}
		}
	}
	return a;
}

/*
 * Create a union type corresponding to 'var'.
 */
AIF *
GetPartialUnionAIF(MISession *session, char *expr, MIVar *var)
{
	AIF *	ac;
	AIF *	a;
	int		i;

	DEBUG_PRINTF(DEBUG_LEVEL_BACKEND, "---------------------- GetPartialUnionAIF (%s, %s)\n", expr != NULL ? expr : "NULL", var->type);

	a = EmptyUnionToAIF(GetTypeName(var->type));

	if (var->children != NULL) {
		for (i = 0; i < var->numchild; i++) {
			ac = GetPartialAIF(session, expr, var->children[i]);
			if (ac != NULL) {
				AIFAddFieldToUnion(a, var->children[i]->exp, ac);
				AIFFree(ac);
			}
		}
	}
	return a;
}

/*
 * Create a pointer to the base type of 'var'. Only obtain
 * the minimum amount of type information.
 */
AIF *
GetPartialPointerAIF(MISession *session, char *expr, MIVar *var)
{
	AIF *	ac;
	AIF *	a;
	char *	v;
	int		id;

	DEBUG_PRINTF(DEBUG_LEVEL_BACKEND, "---------------------- GetPartialPointerAIF (%s, %s)\n", expr != NULL ? expr : "NULL", var->type);

	id = GetPointerBaseType(session, var);

	if (var->children != NULL) {
		switch (id) {
		case T_CHAR_PTR:
			v = GetVarValue(session, var->children[0]->name);
			a = CreateAIFCharPointer(session, v);
			free(v);
			break;
		case T_POINTER:
			ac = VoidToAIF(0, 0);
			v = GetVarValue(session, var->children[0]->name);
			a = CreateAIFPointer(session, v, ac);
			free(v);
			AIFFree(ac);
			break;
		case T_UNION:
			a = GetPartialUnionAIF(session, expr, var);
			break;
		case T_STRUCT:
		case T_CLASS:
			a = GetPartialAggregateAIF(session, expr, var);
			break;
		default:
			if (var->numchild == 1) {
				a = GetPartialAIF(session, expr, var->children[0]);
			} else {
				a = VoidToAIF(0, 0);
			}
			break;
		}
	} else {
		switch (id) {
		case T_CHAR_PTR:
			v = GetVarValue(session, var->name);
			a = CreateAIFCharPointer(session, v);
			free(v);
			break;
		default:
			a = VoidToAIF(0, 0);
			break;
		}
	}
	v = GetVarValue(session, var->name);
	ac = CreateAIFPointer(session, v, a);
	free(v);
	AIFFree(a);
	return ac;
}

/*
 * Create a complex AIF object corresponding to 'var'.
 */
AIF *
GetPartialComplexAIF(MISession *session, char *expr, MIVar *var)
{
	char *	v;
	char *	type;
	AIF *	a = NULL;

	DEBUG_PRINTF(DEBUG_LEVEL_BACKEND, "---------------------- GetPartialComplexAIF (%s, %s)\n", expr != NULL ? expr : "NULL", var->type);

	int id = GetComplexType(var->type);
	if (id == T_UNKNOWN) {
		type = GetPtypeValue(session, expr, var);
		if (type != NULL) {
			if (var->type != NULL) {
				free(var->type);
			}
			var->type = type;
			id = GetComplexType(var->type);
		}
	}
	switch (id) {
	case T_ARRAY:
		a = GetPartialArrayAIF(session, expr, var);
		break;
	case T_CHAR_PTR:
		v = GetVarValue(session, var->name);
		a = CreateAIFCharPointer(session, v);
		free(v);
		break;
	case T_POINTER:
		a = GetPartialPointerAIF(session, expr, var);
		break;
	case T_UNION:
		a = GetPartialUnionAIF(session, expr, var);
		break;
	case T_STRUCT:
	case T_CLASS:
		a = GetPartialAggregateAIF(session, expr, var);
		break;
	default:
		/*
		 * Maybe it was simple all along
		 */
		a = GetSimpleAIF(session, var);
	}

	DEBUG_PRINTF(DEBUG_LEVEL_BACKEND, "---------------------- GetPartialComplexAIF id is %d, returns %s\n", id, a != NULL ? AIF_FORMAT(a) : "NULL");
	return a;
}

/*
 * Create a partial AIF object corresponding to 'var'.
 *
 * A partial AIF object contains only only enough type
 * information to determine if the variable is
 * structured or not. This speeds up displaying the variable
 * in the UI as the variable contents do not need to be
 * read from the program.
 *
 * Detailed type information and the variable contents will
 * be requested by the UI as the user drills into the variable.
 */
AIF *
GetPartialAIF(MISession *session, char *expr, MIVar *var)
{
	DEBUG_PRINTF(DEBUG_LEVEL_BACKEND, "---------------------- GetPartialAIF (%s, %s)\n", expr != NULL ? expr : "NULL", var->type);

	if (var->type == NULL || strcmp(var->type, "<text variable, no debug info>") == 0) {
		DbgSetError(DBGERR_NOSYMS, "");
		return NULL;
	}
	if (var->numchild == 0) {
		return GetSimpleAIF(session, var);
	}
	return GetPartialComplexAIF(session, expr, var);
}
