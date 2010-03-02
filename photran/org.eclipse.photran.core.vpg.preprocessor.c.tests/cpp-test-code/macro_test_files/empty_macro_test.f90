There may be a problem reconstructing source code if a macro does not
produce any tokens, because the macro invocation cannot be attached to
the macro expansion.


#define BLANK
#define ECHO(x) x

/*1*/BLANK  //2

/*3*/ECHO()  //4

