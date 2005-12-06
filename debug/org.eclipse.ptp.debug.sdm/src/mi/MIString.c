#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include "MIString.h"

MIString *
NewMIString(char *fmt, ...)
{
	va_list ap;
	MIString *s;

	s = (MIString *)malloc(sizeof(MIString));
	va_start(ap, fmt);
	vasprintf(&s->buf, fmt, ap);
	va_end(ap);
	s->slen = strlen(s->buf);
	
	return s;
}

void
FreeMIString(MIString *str)
{
	free(str->buf);
	free(str);
}

void
AppendMIString(MIString *str, MIString *str2)
{
	int len = str->slen + str2->slen;
	char *buf = (char *)malloc(len + 1);
	
	memcpy(buf, str->buf, str->slen);
	memcpy(&buf[str->slen], str2->buf, str2->slen);
	buf[len] = '\0';
	
	free(str->buf);
	
	str->buf = buf;
	str->slen = len;
	
	FreeMIString(str2);
}


char *
ToCString(MIString *str)
{
	return str->buf;
}