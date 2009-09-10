/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

#ifdef __gnu_linux__
#define _GNU_SOURCE
#endif /* __gnu_linux__ */

#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <ctype.h>

#include "compat.h"
#include "proxy_attr.h"

/*
 * Test if this attribute string has matching key. Also tests
 * to see if the attribute string is formatted correctly.
 */
int
proxy_test_attribute(char *key, char *attr_str)
{
	int len = strlen(key);
	
	/*
	 * Attribute string must be at least the length of key + 1 characters long.
	 */
	if (strlen(attr_str) <= len) 
		return 0;
	
	/*
	 * Attribute string must have '=' separating key and value.
	 */
	if (attr_str[len] != '=') 
		return 0;
	
	/*
	 * Key must match.
	 */
	return strncmp(attr_str, key, len) == 0;
}

/*
 * Find the attribute name (key).
 * Caller must free the return string.
 */
char *
proxy_copy_attribute_name(char *attr_str)
{
	char *	s = strchr(attr_str, '=');

	if (s != NULL) {
		int 	len = s - attr_str;
		char *	res = (char *)malloc(len+1);
		memcpy(res, attr_str, len);
		*(res+len) = '\0';
		return res;
	}

	return NULL;
}

/*
 * Find the attribute that corresponds to the filter attribute name.
 * Caller must free the return string.
 */
char *
proxy_copy_attribute_name_filter(char *attr_str)
{
	int 	len;
	char *	s = proxy_copy_attribute_name(attr_str);

	if (s != NULL) {
		len = strlen(s);
		if (len > 6 && strcmp(s + len - 6, "Filter") == 0) {
			s[len-6] = '\0';
			return s;
		}
	}

	return NULL;
}

/*
 * Return attribute value as a string. If we have tested for the attribute key
 * before calling this, then it is guaranteed to return a non-NULL string.
 */
char *
proxy_get_attribute_value_str(char *attr_str)
{
	char *	s = strchr(attr_str, '=');
	
	if (s != NULL) {
		return s + 1;
	}
	
	return NULL;
}

/*
 * Return attribute value as an int. May return a misleading value if attribute
 * value is not an integer.
 */
int
proxy_get_attribute_value_int(char *attr_str)
{
	char *	s = strchr(attr_str, '=');
	
	if (s != NULL) {
		return strtol(s + 1, NULL, 10);
	}
	
	return 0;
}

/*
 * Return attribute value as a boolean. Returns false if the
 * attribute value is not the (case insensitive) string "true".
 */
int
proxy_get_attribute_value_bool(char *attr_str)
{
	char *	s = strchr(attr_str, '=');
	
	if (s != NULL) {
		return strcasecmp(s + 1, "true") == 0;
	}
	
	return 0;
}
