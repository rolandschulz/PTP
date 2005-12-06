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

extern MIString *NewMIString(char *fmt, ...);
extern void FreeMIString(MIString *str);
extern void AppendMIString(MIString *str, MIString *str2);
extern char *ToCString(MIString *str);
#endif _MISTRING_H_
