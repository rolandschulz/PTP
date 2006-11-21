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
 
 /*
  * Based on the QNX Java implementation of the MI interface
  */

/**
<pre>
`OUTPUT :'
     `( OUT-OF-BAND-RECORD )* [ RESULT-RECORD ] "(gdb)" NL'

`RESULT-RECORD :'
     ` [ TOKEN ] "^" RESULT-CLASS ( "," RESULT )* NL'

`OUT-OF-BAND-RECORD :'
     `ASYNC-RECORD | STREAM-RECORD'

`ASYNC-RECORD :'
     `EXEC-ASYNC-OUTPUT | STATUS-ASYNC-OUTPUT | NOTIFY-ASYNC-OUTPUT'

`EXEC-ASYNC-OUTPUT :'
     `[ TOKEN ] "*" ASYNC-OUTPUT'

`STATUS-ASYNC-OUTPUT :'
     `[ TOKEN ] "+" ASYNC-OUTPUT'

`NOTIFY-ASYNC-OUTPUT :'
     `[ TOKEN ] "=" ASYNC-OUTPUT'

`ASYNC-OUTPUT :'
     `ASYNC-CLASS ( "," RESULT )* NL'

`RESULT-CLASS :'
     `"done" | "running" | "connected" | "error" | "exit"'

`ASYNC-CLASS :'
     `"stopped" | OTHERS' (where OTHERS will be added depending on the
     needs--this is still in development).

`RESULT :'
     ` VARIABLE "=" VALUE'

`VARIABLE :'
     ` STRING '

`VALUE :'
     ` CONST | TUPLE | LIST '

`CONST :'
     `C-STRING'

`TUPLE :'
     ` "{}" | "{" RESULT ( "," RESULT )* "}" '

`LIST :'
     ` "[]" | "[" VALUE ( "," VALUE )* "]" | "[" RESULT ( "," RESULT )*
     "]" '

`STREAM-RECORD :'
     `CONSOLE-STREAM-OUTPUT | TARGET-STREAM-OUTPUT | LOG-STREAM-OUTPUT'

`CONSOLE-STREAM-OUTPUT :'
     `"~" C-STRING'

`TARGET-STREAM-OUTPUT :'
     `"@" C-STRING'

`LOG-STREAM-OUTPUT :'
     `"&" C-STRING'

`NL :'
     `CR | CR-LF'

`TOKEN :'
     _any sequence of digits_.

`C-STRING :'
     `""" SEVEN-BIT-ISO-C-STRING-CONTENT """'
</pre>
 */

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <ctype.h>

#include "list.h"
#include "MIOOBRecord.h"
#include "MIValue.h"
#include "MIResult.h"
#include "MIResultRecord.h"
#include "MIOutput.h"

static MIResultRecord *processMIResultRecord(char *buffer, int id);
static MIOOBRecord *processMIOOBRecord(char *buffer, int id);
static List *processMIResults(char **buffer);
static MIResult *processMIResult(char **buffer);
static MIValue *processMIValue(char **buffer);
static MIValue *processMITuple(char **buffer);
static MIValue *processMIList(char **buffer);
static char *translateCString(char **buffer);

char *primaryPrompt = "(gdb)"; //$NON-NLS-1$
char *secondaryPrompt = ">"; //$NON-NLS-1$

/**
 * Point of entry to create an AST for MI.
 *
 * @param buffer Output from MI Channel.
 * @param mi MIOutput
 * @see MIOutput
 */
 void
 MIParse(char *buffer, MIOutput *mi) 
 {
	int id = -1;
	char *s;
	char *token;

	while (buffer != NULL && *buffer != '\0') {
		s = strchr(buffer, '\n');
		if (s != NULL)
			*s++ = '\0';

		token = buffer;
		buffer = s;
		
		// Fetch the Token/Id
		if (*token != '\0' && isdigit(*token)) {
			id = strtol(token, &token, 10);
		}

		// ResultRecord ||| Out-Of-Band Records
		if (*token != '\0') {
			if (*token == '^') {
				token++;
				mi->rr = processMIResultRecord(token, id);
			} else if (strncmp(token, primaryPrompt, strlen(primaryPrompt)) == 0) {
				//break; // Do nothing.
			} else {
				MIOOBRecord *band = processMIOOBRecord(token, id);
				if (band != NULL) {
					if (mi->oobs == NULL)
						mi->oobs = NewList();
					AddToList(mi->oobs, (void *)band);
				}
			}
		}
	}
}

/**
 * Assuming '^' was deleted from the Result Record.
 */
static MIResultRecord *
processMIResultRecord(char *buffer, int id)
{
	MIResultRecord *rr = MIResultRecordNew();
	rr->token = id;
	if (strncmp(buffer, "done", 4) == 0) {
		rr->resultClass = MIResultRecordDONE;
		buffer += 4;
	} else if (strncmp(buffer, "error", 5) == 0) {
		rr->resultClass = MIResultRecordERROR;
		buffer += 5;
	} else if (strncmp(buffer, "exit", 4) == 0) {
		rr->resultClass = MIResultRecordEXIT;
		buffer += 4;
	} else if (strncmp(buffer, "running", 7) == 0) {
		rr->resultClass = MIResultRecordRUNNING;
		buffer += 7;
	} else if (strncmp(buffer, "connected", 9) == 0) {
		rr->resultClass = MIResultRecordCONNECTED;
		buffer += 9;
	} else {
		// FIXME:
		// Error throw an exception?
	}

	// Results are separated by commas.
	if (*buffer != '\0' && *buffer == ',') {
		buffer++;
		List *res = processMIResults(&buffer);
		rr->results = res;
	}
	return rr;
}

/**
 * Find OutOfBand Records depending on the starting token.
 */
static MIOOBRecord *
processMIOOBRecord(char *buffer, int id)
{
	MIOOBRecord *oob = NULL;
	char c = *buffer;
	if (c == '*' || c == '+' || c == '=') {
		// Consume the first char
		buffer++;
		switch (c) {
			case '*' :
				oob = NewMIExecAsyncOutput();
				break;

			case '+' :
				oob = NewMIStatusAsyncOutput();
				break;

			case '=' :
				oob = NewMINotifyAsyncOutput();
				break;
		}
		oob->token = id;
		// Extract the Async-Class
		char *s = strchr(buffer, ',');
		if (s != NULL) {
			*s++ = '\0';
			oob->class = strdup(buffer);
			// Consume the async-class and the comma
			buffer = s;
		} else {
			oob->class = strdup(buffer);
			buffer += strlen(buffer);
		}
		List *res = processMIResults(&buffer);
		oob->results = res;
	} else if (c == '~' || c == '@' || c == '&') {
		// Consume the first char
		buffer++;
		switch (c) {
			case '~' :
				oob = NewMIConsoleStreamOutput();
				break;

			case '@' :
				oob = NewMITargetStreamOutput();
				break;

			case '&' :
				oob = NewMILogStreamOutput();
				break;
		}
		// translateCString() assumes that the leading " is deleted
		if (*buffer != '\0' && *buffer == '"') {
			buffer++;
		}
		oob->cstring = translateCString(&buffer);
	} else {
		// Badly format MI line, just pass it to the user as target stream
		oob = NewMITargetStreamOutput();
		oob->cstring = strdup(buffer); //$NON-NLS-1$
	}
	return oob;
}

/**
 * Assuming that the usual leading comma was consumed.
 * Extract the MI Result comma seperated responses.
 */
static List * 
processMIResults(char **buffer)
{
	List *aList = NewList();
	MIResult *result = processMIResult(buffer);
	if (result != NULL) {
		AddToList(aList, (void *)result);
	}
	while (*(*buffer) != '\0' && *(*buffer) == ',') {
		(*buffer)++;
		result = processMIResult(buffer);
		if (result != NULL) {
			AddToList(aList, (void *)result);
		}
	}
	return aList;
}

/**
 * Construct the MIResult.  Characters will be consume/delete
 * moving forward constructing the AST.
 */
static MIResult *
processMIResult(char **buffer)
{
	MIResult *result = MIResultNew();
	MIValue *value;
	char *equal;
	if (*(*buffer) != '\0' && isalpha(*(*buffer)) && (equal = strchr(*buffer, '=')) != NULL) {
		char *variable = *buffer;
		*equal++ = '\0';
		
		result->variable = strdup(variable); // TODO strdup?
		*buffer = equal;
		value = processMIValue(buffer);
		result->value = value;
	} else if(*(*buffer) != '\0' && *(*buffer) == '"') {
		// This an error but we just swallow it and move on.
		value = processMIValue(buffer);
		result->value = value;
	} else {
		result->variable = strdup(*buffer);
		result->value = NewMIConst(); // Empty string:???
		*(*buffer) = '\0';
	}
	return result;
}

/**
 * Find a MIValue implementation or return null.
 */
static MIValue *
processMIValue(char **buffer)
{
	MIValue *value = NULL;
	if (*(*buffer) != '\0') {
		if (*(*buffer) == '{') {
			(*buffer)++;
			value = processMITuple(buffer);
		} else if (*(*buffer) == '[') {
			(*buffer)++;
			value = processMIList(buffer);
		} else if (*(*buffer) == '"') {
			(*buffer)++;
			value = NewMIConst();
			value->cstring = translateCString(buffer);
		}
	}
	return value;
}

/**
 * Assuming the starting '{' was deleted form the StringBuffer,
 * go to the closing '}' consuming/deleting all the characters.
 * This is usually call by processMIvalue();
 */
static MIValue *
processMITuple(char **buffer)
{
	MIValue *tuple = NewMITuple();
#ifdef __APPLE__
	List *values = NewList();
	MIValue *value;
	MIResult *result;
#endif /* __APPLE__ */
	List *results = NULL;
	// Catch closing '}'
	while (*(*buffer) != '\0' && *(*buffer) != '}') {
#ifdef __APPLE__
		// Try for the MIValue first
		value = processMIValue(buffer);
		if (value != NULL) {
			AddToList(values, (void *)value);
		} else {
			result = processMIResult(buffer);
			if (result != NULL) {
				if (results == NULL) {
					results = NewList();
				}				
				AddToList(results, (void *)result);
			}
		}
		if (*(*buffer) != '\0' && *(*buffer) == ',') {
			(*buffer)++;
		}
#else /* __APPLE__ */
		results = processMIResults(buffer);
#endif /* __APPLE__ */
	}
	if (*(*buffer) != '\0' && *(*buffer) == '}') {
		(*buffer)++;
	}
	tuple->results = results;
#ifdef __APPLE__
	tuple->values = values;
#endif /* __APPLE__ */
	return tuple;
}

/**
 * Assuming the leading '[' was deleted, find the closing
 * ']' consuming/delete chars from the StringBuffer.
 */
static MIValue *
processMIList(char **buffer)
{
	MIValue *list = NewMIList();
	List *valueList = NewList();
	List *resultList = NewList();
	MIValue *value;
	MIResult *result;
	// catch closing ']'
	while (*(*buffer) != '\0' && *(*buffer) != ']') {
		// Try for the MIValue first
		value = processMIValue(buffer);
		if (value != NULL) {
			AddToList(valueList, (void *)value);
		} else {
			result = processMIResult(buffer);
			if (result != NULL) {
				AddToList(resultList, (void *)result);
			}
		}
		if (*(*buffer) != '\0' && *(*buffer) == ',') {
			(*buffer)++;
		}
	}
	if (*(*buffer) != '\0' && *(*buffer) == ']') {
		(*buffer)++;
	}
	list->values = valueList;
	list->results = resultList;
	return list;
}

/*
 * MI C-String rather MICOnst values are enclose in double quotes
 * and any double quotes or backslash in the string are escaped.
 * Assuming the starting double quote was removed.
 * This method will stop at the closing double quote remove the extra
 * backslach escaping and return the string __without__ the enclosing double quotes
 * The orignal StringBuffer will move forward.
 */
static char *
translateCString(char **buffer)
{
	int escape = 0;
	int closingQuotes = 0;
	char *s = *buffer;
	char *sb = strdup(s);
	char *p = sb;

	for (; *s != '\0' && !closingQuotes; s++) {
		if (*s == '\\') {
			if (escape) {
				*p++ = *s;
				escape = 0;
			} else {
				escape = 1;
			}
		} else if (*s == '"') {
			if (escape) {
				*p++ = *s;
				escape = 0;
			} else {
				// Bail out.
				closingQuotes = 1;
			}
		} else {
			if (escape) {
				*p++ = '\\';
			}
			*p++ = *s;
			escape = 0;
		}
	}
	*buffer = s;
	*p++ = '\0';
	return sb;
}
