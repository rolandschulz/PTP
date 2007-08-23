/* Several different functions are needed as there is no standard Fortran symbol name mangling */

void C_INT(int* i)
{
  *i = 3;
}

void c_int(int* i)
{
  *i = 3;
}

void c_int_(int* i)
{
  *i = 3;
}

void c_int__(int* i)
{
  *i = 3;
}
