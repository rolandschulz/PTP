! Here's a comment at the top of the file
#define TYPE_AND_VAR integer :: hello
#define VALUE 5
program hello_world
  implicit none
  TYPE_AND_VAR = VALUE
  print *, hello
  
end program
