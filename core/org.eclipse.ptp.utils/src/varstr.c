/******************************************************************************
 * Copyright (c) 1996-2002, 2010 by Guardsoft Pty Ltd and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	Guardsoft Pty Ltd - initial API and implementation (from libaif)
 * 	Greg Watson - stand alone implementation
 *
 ******************************************************************************/

#include <stdlib.h>
#include <string.h>
#include <stdarg.h>
#include <stdio.h>

#include "varstr.h"

#define INITIAL_SIZE	32
#define STR_LEN(v) ((v)->str_pos - (v)->str_buf)

static void _resize(varstr *v, int len);

varstr *
varstr_new(int initial_size)
{
	varstr *	v = (varstr *)malloc(sizeof(varstr));

	if (initial_size <= 0) {
		initial_size = INITIAL_SIZE;
	}

	v->str_buf = (char *)malloc(initial_size);
	v->buf_len = initial_size;
	v->str_pos = v->str_buf;
	return v;
}

varstr *
varstr_fromstr(char *str)
{
	varstr *	v = varstr_new(strlen(str));

	return varstr_cat(v, str);
}

varstr *
varstr_cat(varstr *v, char *str)
{
	int	len = strlen(str);

	_resize(v, len);
	memcpy(v->str_pos, str, len);
	v->str_pos += len;
	return v;
}

varstr *
varstr_sprintf(varstr *v, char *fmt, ...)
{
	va_list	ap;
	char *	str;

	va_start(ap, fmt);
	vasprintf(&str, fmt, ap);
	va_end(ap);
	varstr_cat(v, str);
	free(str);
	return v;
}

varstr *
varstr_add(varstr *v, char ch)
{
	_resize(v, 1);

	*(v->str_pos++) = ch;
	return v;
}

char *
varstr_tostr(varstr *v)
{
	int 	len = STR_LEN(v);
	char *	str = (char *)malloc(len + 1);

	memcpy(str, v->str_buf, len);
	*(str + len) = '\0';

	return str;
}

void
varstr_free(varstr *v)
{
	free(v->str_buf);
	free(v);
}

/*
 * Resize the varstr buffer so that it is large enough
 * to accommodate len more characters.
 */
static void
_resize(varstr *v, int len)
{
	int strlen = STR_LEN(v);
	if (v->buf_len - strlen >= len) {
		return;
	}
	if (len < INITIAL_SIZE) {
		len = INITIAL_SIZE;
	}
	v->buf_len += len;
	v->str_buf = (char *)realloc(v->str_buf, v->buf_len);
	v->str_pos = v->str_buf + strlen;
}

