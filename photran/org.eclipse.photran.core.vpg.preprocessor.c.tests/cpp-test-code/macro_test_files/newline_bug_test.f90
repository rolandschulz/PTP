A newline token may not be returned if a macro expansion attempt fails.

#define SQUARE(x) x*x

SQUARE /*1*/
 /*2*/
 /*3*/ nothing

