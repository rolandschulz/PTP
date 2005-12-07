#ifndef _MISTRING_H_
#define _MISTRING_H_
#include <stdarg.h>

struct MIString {
	int     blen;
	int     slen;
	int     end;
	char *  buf;
};
typedef struct MIString MIString;

extern MIString *MIStringNew(char *fmt, ...);
extern void MIStringFree(MIString *str);
extern void MIStringAppend(MIString *str, MIString *str2);
extern char *MIStringToCString(MIString *str);
#endif _MISTRING_H_
