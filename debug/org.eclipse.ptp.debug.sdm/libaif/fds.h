/*
 * Header file for FDS routines.
 *
 * Copyright (c) 1996-2002 by Guardsoft Pty Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */

#ifndef _FDS_H
#define _FDS_H

#define FDS_ARRAY_END				']'
#define FDS_ARRAY_START			'['
#define FDS_BOOLEAN				'b'
#define FDS_CHARACTER				'c'
#define FDS_ENUM_END				'>'
#define FDS_ENUM_CONST_SEP		','
#define FDS_ENUM_SEP				'='
#define FDS_ENUM_START			'<'
#define FDS_FLOATING				'f'
#define FDS_FUNCTION				'&'
#define FDS_FUNCTION_ARG_END		'/'
#define FDS_FUNCTION_ARG_SEP		','
#define FDS_ID					'|'
#define FDS_INTEGER				'i'
#define FDS_INTEGER_SIGNED		's'
#define FDS_INTEGER_UNSIGNED		'u'
#define FDS_INVALID				'?'
#define FDS_NAME					'%'
#define FDS_NAME_END				'/'
#define FDS_POINTER				'^'
#define FDS_RANGE				'r'
#define FDS_RANGE_SEP				'.'
#define FDS_REFERENCE				'>'
#define FDS_REFERENCE_END			'/'
#define FDS_REGION				'z'
#define FDS_STRING				's'
#define FDS_ADDRESS				'a'
#define FDS_STRUCT_ACCESS_SEP		';'
#define FDS_STRUCT_END			'}'
#define FDS_STRUCT_FIELD_NAME_END	'='
#define FDS_STRUCT_FIELD_SEP		','
#define FDS_STRUCT_START			'{'
#define FDS_UNION_END				')'
#define FDS_UNION_FIELD_NAME_END	'='
#define FDS_UNION_FIELD_SEP		','
#define FDS_UNION_START			'('
#define FDS_VOID				'v'
#define FDS_CHAR_POINTER		'p'


extern char *		FDSAddFieldToClass(char *, aifaccess, char *, char *);
extern char *		FDSAddConstToEnum(char *, char *, int);
extern char *		FDSAddFieldToStruct(char *, char *, char *);
extern char *		FDSAddFieldToUnion(char *, char *, char *);
extern char *		FDSRangeInit(int, int);
extern char *		FDSArrayInit(int, int, char *);
extern int			FDSArrayRank(char *);
extern int			FDSArraySize(char *);
extern char *		FDSArrayIndexType(char *);
extern int			FDSArrayMinIndex(char *, int);
extern int			FDSArrayMaxIndex(char *, int);
extern AIFIndex *	FDSArrayIndexInit(char *);
extern void			FDSArrayInfo(char *, int *, char **, char **);
extern void			FDSArrayBounds(char *, int , int **, int **, int **);
extern char *		FDSBaseType(char *);
extern int			FDSClassAdd(char **, aifaccess, char *, char *);
extern int			FDSClassFieldByName(char *, char *, char **);
extern int			FDSClassFieldByNumber(char *, int, char **, char **);
extern int			FDSClassFieldIndex(char *, char *);
extern int			FDSClassFieldSize(char *, char *);
extern char *		FDSClassInit(char *);
extern int			FDSDataSize(char *, char *);
extern int			FDSEnumAdd(char **, char *, int);
extern int			FDSEnumConstByName(char *, char *, int *);
extern int			FDSEnumConstByValue(char *, char **, int val);
extern char *		FDSEnumInit(char *);
extern char *		FDSGetIdentifier(char *);
extern int			FDSIsSigned(char *);
extern int			FDSNumFields(char *);
extern int			FDSSetIdentifier(char **, char *);
extern int			FDSStructAdd(char **, char *, char *);
extern int			FDSStructFieldByName(char *, char *, char **);
extern int			FDSStructFieldByNumber(char *, int, char **, char **);
extern int			FDSStructFieldIndex(char *, char *);
extern int			FDSStructFieldSize(char *, char *);
extern char *		FDSStructInit(char *);
extern int			FDSType(char *);
extern int			FDSTypeCompare(char *, char *);
extern int			FDSTypeSize(char *);
extern int			FDSUnionAdd(char **, char *, char *);
extern int			FDSUnionFieldByName(char *, char *, char **);
extern char *		FDSUnionInit(char *);
extern char *		TypeToFDS(int, ...);

#endif /* !_FDS_H */


