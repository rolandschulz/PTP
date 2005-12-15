#ifndef _MISTRING_H_
#define _MISTRING_H_
#include <stdarg.h>

struct MIString {
	int     slen;
	char *  buf;
};
typedef struct MIString MIString;

extern MIString *MIStringNew(char *fmt, ...);
extern void MIStringFree(MIString *str);
extern void MIStringAppend(MIString *str, MIString *str2);
extern char *MIStringToCString(MIString *str);
extern char *MIIntToCString(int val);
#endif /* _MISTRING_H_ */
