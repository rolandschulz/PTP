start

#define DEFINED_VAR /*1*/

#ifdef UNDEFINED_VAR/*2*/
undefined-var is defined

#elif defined(ANOTHER_UNDEFINED_VAR) /*3*/
another-undefined-var is defined

#elif defined(DEFINED_VAR) /*4*/
defined-var is defined

#else/*5*/
none of the tested vars were defined

#endif/*6*/

#if defined(UNDEFINED_VAR)/*7*/
undefined-var is defined
#include "basic_included.f90"

#endif/*8*/

#if defined(DEFINED_VAR)/*17*/
defined-var is defined
#include "basic_included.f90"

#endif/*9*/

end
