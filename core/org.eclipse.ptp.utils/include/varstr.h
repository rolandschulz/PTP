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

#ifndef _VARSTR_H_
#define _VARSTR_H_

struct varstr {
	char *	str_buf;
	char *	str_pos;
	int		buf_len;
};
typedef struct varstr	varstr;

varstr *	varstr_new(int initial_size);
varstr *	varstr_fromstr(char *str);
varstr *	varstr_cat(varstr *v, char *str);
varstr *	varstr_add(varstr *v, char ch);
varstr *	varstr_sprintf(varstr *v, char *fmt, ...);
char *		varstr_tostr(varstr *v);
void		varstr_free(varstr *v);

#endif /* !_VARSTR_H_ */
