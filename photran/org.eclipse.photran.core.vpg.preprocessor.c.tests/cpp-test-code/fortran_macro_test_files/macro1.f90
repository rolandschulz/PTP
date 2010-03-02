! Here's a comment at the top of the file
#define TYPE integer
#define VALUE 5
program hello_world
  implicit none
  TYPE :: hello = VALUE
  print *, hello
end program
