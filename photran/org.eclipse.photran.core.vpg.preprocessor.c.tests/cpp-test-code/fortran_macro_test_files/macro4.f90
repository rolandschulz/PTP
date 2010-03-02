! This program will test some complicated token overlap situation
#define ECHO(x) x

program hello_world
  implicit none
  integer :: hello = 5
  if(3 ECHO(.lt). 4) print *, hello
  if(3 .ECHO(lt. 4 .)ECHO(and)ECHO(. 4 .lt). 5) print *, hello
  if(3 ECHO(<)ECHO(4)) print *ECHO(),ECHO() ECHO()hello
  print *, ECHO(hello ! in macro)
  print *, ECHO(hello ! in macro) out of macro
  
end program
