! NOTE:!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
! not sure how much of this is legal Fortran.  just here to test 
! the syntax fixup of Sale's algorithm.  semantic checks have to be 
! done in the parser
! !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

real(8) integer
end

 module hello

end module hello

module integer

end module integer

module hello
  use, intrinsic :: iso_c_binding
contains
  subroutine test_0()
    integer :: integer
    integer :: y
    integer :: i, j
    real x, w, z
    integer value
    real :: integer
    real :: real
    integer(kind=4) :: kind
    real(8) integer 
    real, pointer :: my_pnter, real
  end subroutine test_0

  integer function foo()
  end function foo

  integer        function       foo()
  end function foo

  integer        function       subroutine()
  end function subroutine

  subroutine blah() bind(c)

  end subroutine blah

  subroutine function() bind(c)

  end subroutine function
end module hello

integer :: integer
integer real
integer    :: i
integer :: i,j
real x, w,z
integer value
integer    value
real :: integer
real :: real
integer(kind=4) :: kind
real(8) integer
real, pointer :: my_pnter, real
end

! Sale's algorithm says to ignore any characters between matching parens
INTEGER(KIND=4) :: N;
INTEGER(4) :: N;
real :: open
real :: save
real :: public
integer :: real
real :: integer
integer :: character
double precision :: integer
double precision :: doubleprecision
doubleprecision :: character
double precision integer

i = 3
integer = i
real = integer

i = foo(i,integer,foo(i,integer, real))

end

subroutine subroutine(integer, real) bind(c)
  
end subroutine subroutine

subroutine function()
  
end subroutine function

subroutine function

end subroutine function

module function

end module function

integer function subroutine()

end function subroutine

integer function subroutine() bind(c)

end function subroutine

integer function subroutine() bind(c)

end function subroutine

integer function subroutine() result(integer) bind(c)

end function subroutine

integer function subroutine() bind(c) result(integer) 

end function subroutine

10 i = 3
100       i = 4
101 integer = i
end

use my_mod
use integer
use, intrinsic :: some_intrinsic, only: integer, real
use, intrinsic ::  some_module, only: i, j

type my_type
   integer my_int
end type my_type

! i don't know if this is legal fortran or not..
type integer
   integer my_int
end type integer

type integer
   integer real
   real character
end type integer

type,bind(c)::my_c_type
  integer(c_int) :: i, j
end type my_c_type

type, bind(c)::my_c_type2
  integer(c_int) :: i, j
end type my_c_type2

type my_type
   integer :: m, n
end type my_type

type integer
   integer i, j
end type integer

type,bind(c) ::real
   integer(c_int) :: i
end type real

type ,    bind(c)   ::character
   integer(c_int) :: real
end type character

end

if (i .eq. j) then
   print *, 'i and j are equal'
end if

if (integer .eq. real)then
   print *, 'integer and real are equal'
end if

if (integer .eq. real) then
   print *, 'integer and real are equal'
else
   print *, 'integer and real are NOT equal'
end if

if (integer              .eq. real) then
   print *, 'integer and real are equal'
else if(integer .lt. real) then
   print *, 'integer is LESS THAN real'
end if

end

integer :: i, j
i = 10
j = 20

if(integer.le.real) 10 ,20,30
if(i+j) 10 ,20,30

10 print *, '10'
20 print *, '20'
30 print *, '30'
end

type initialized_type
   integer :: i = 1
end type initialized_type

type initialize_type
   integer, pointer :: ptr => null()
end type initialize_type

end

! probably not valid fortran, but should be accepted by the parser
associate(myPtr=>iptr)
end associate 

myassocstmt: associate(myPtr=>iptr)
end associate myassocstmt 

integer: associate(myPtr=>iptr)
i = 3
end associate integer

associate: associate(integer=>real)
real = open
end associate associate

call sub()
call subroutine()
call function(subroutine, return, stop, open, parameter)

end

procedure(foo) :: myProcDecl
procedure() :: myProc
procedure(real) :: myProc2
procedure(integer), pointer :: myProc3, function, subroutine

enum, bind(c)
enumerator :: a
end enum

enum, bind(c)
enumerator :: a=1, b, c
end enum

enum, bind(c)
enumerator    a=1, b, c
end enum

enum, bind(c)
enumerator    integer=1, real, save, open, function
end enum

! not sure that the expression allowed for an enumerator 
! can have a variable on the rhs or not...
enum, bind(c)
enumerator    integer=subroutine, real, save=3*5, open, function
end enum

end

private :: a, b, c
private a, b, c
public :: i, j, k
public integer, real, subroutine
private :: save, open, format

allocatable :: a(:), b, c[:]
allocatable a(:), b, c[:]
allocatable :: integer(:,:), real[:], subroutine
allocatable integer(:,:), real[:], subroutine

asynchronous :: a, b, c
asynchronous    a, b, c
asynchronous ::   integer, b, real
asynchronous  asynchronous, format, write

end

bind(c) :: a, b, c
bind(c, name='hello') :: integer
bind(c, name='real')subroutine
bind(c, name='real')          function
bind(c, name='real')    ::      open
bind(c)    ::      open, save, real

dimension :: a(:)
dimension :: a(:), b[:], c
dimension    a(:), b[:], c
dimension    integer(:), real[:], save
dimension :: integer(:), real[:], save

intent(inout) :: a, b, c
intent(in) :: a, b, c
intent(out) :: a, b, c
intent(out) integer, real, save, open, function
intent(inout) intent

parameter(a=1)
parameter(a=1,b=3,   c=2)
parameter   (integer=1,function=3,   subroutine=2,   parameter=15)

end

pointer :: a, b, c
pointer :: integer, real, save, open
pointer    integer, real, save, open
pointer    integer(:), real(:,:,:), save, open(:)

protected :: a, b
protected a, b
protected    integer, real
protected  ::    integer, real

save save
save :: save, integer, b, c, real
save :: /save/, /integer/, /b/, /c/, /real/
save    /save/, /integer/, /b/, /c/, /real/

target a, b, c
target ::  a, b, c
target ::  integer, function, subroutine(:,:), function[:]
target     integer, function, subroutine(:,:), function[:]

value :: a, b, c
value a, b, c
value integer, real, function, value
value value
value :: integer, real, function, value

volatile :: a, b, c
volatile a, b, c
volatile integer, real, function, value
volatile value
volatile :: integer, real, function, value
volatile volatile 

end

if(i > j) i = j
if(i < integer) integer = i
if(integer .ge. real) integer = real

if(integer .ge. real) then
   integer = real
end if

end

forall(i=1:2:3) i = j * 2
forall(i=1:2:3,j=1:2:10,k=1:2:20) i = j * k
forall(i=1:2:3,j=1:2:10,k=1:2) i = j * k
forall(i=1:2:3,j=1:2:10,k=1:2, i) i = j * k
forall(integer=1:2:3) integer = real * 2
forall(integer=1:2:3,real=1:2:10,subroutine=integer:2:20) integer = real * subroutine
forall(integer=1:2:subroutine,j=1:real:10,k=1:2) integer = j * real
forall(integer=1:2:3,function=1:2:10,k=1:2, i) integer = function * k
end

close(UNIT=3)
close(UNIT=3, IOSTAT=2, STATUS=1)
close(UNIT=real, IOSTAT=integer, STATUS=function)
end

read(iun) n
read(iun) iqual, m, array(1:m)
read (iun, '(1x, g14.7)', iostat = ios) x

read(integer) real
read(subroutine) integer, real, function(1:integer)
read (real, '(1x, g14.7)', iostat = ios) function

end

flush 0
flush i
flush(unit=i, err=integer)
flush( 0 )
flush integer
flush(unit=real, err=integer)

end

open(7)
open(unit=8, position = 0)
open(real)
open(unit=integer, position = real)

end

print *, 'hello', i, j
print *, 'i: ', i, ' j: ', j
print *, 'integer: ', integer, ' real: ', real

end

rewind(0)
rewind(unit=0, iostat=1)
rewind 1
rewind(unit=integer, iostat=real)
rewind(integer)
end

write(*,*) i
write(*,*) integer
write(*,*) real, function, subroutine

end

program main

end program main

program program

end program program

program integer
end program integer

exit i
exit integer
exit real
exit function
exit exit
end

inquire(iolength = iol) a(1:n)
inquire(unit=joan, opened=log_01, named=log_02, form=char_var, iostat=ios)
end

nullify(i, j, k%ptr)
nullify(i, j(1:n, 2), k%ptr)
nullify(integer, real(1:integer, 2), function%pointer)
end

integer => real
integer(1:n)=>real

end

return 1
return integer
return real
return return
end

stop 
stop int
stop 0
stop integer
stop real
end

wait(unit=integer)
end

allocate(my_var)
allocate(my_type%ptr, source=my_type_2%ptr)
allocate(my_type%next)
allocate(real)
allocate(integer%real, source=real%integer)

deallocate(my_var)
deallocate(my_type%ptr, source=my_type_2%ptr)
deallocate(my_type%next)
deallocate(real)
deallocate(integer%real, source=real%integer)

end

implicit none
implicit integer (i)
end

type(my_type) my_f90_type
type(integer) my_integer_type, real, character
integer(real) my_real_integer
end

integer(c_int) function foo()

end function foo

integer function subroutine()
end function subroutine

type(my_type) function foo()
end function foo

! ! is this even valid??
type(integer) function subroutine()
end function subroutine

! ! is this even valid??
integer(real) function subroutine()
end function subroutine

end

integer%real%my_type%this_array(1:n) => real
integer%real%my_type%this_ptr => real
integer%real%my_type%this_array(1:n)%next%ptr => real

end

ptr%ptr%ptr%variable = 3
integer%ptr%ptr%variable = 3
integer%real%subroutine%function(1:value)%real = 3.0
integer%real%subroutine%function(1:value)%real(:) = real%integer(:)
integer%real%subroutine%function(1:value)%real_ptr => 3.0
integer%real%subroutine%function(1:value)%real_ptrs(:) => real%my_ptrs(:)

end

namelist /nlist/ a, b, c
namelist /namelist/ integer, real, subroutine
end

equivalence (a,c(1)), (b,c(2))
equivalence (integer,real(1)), (subroutine,real(2))
end

common /blocka/ a, b, d(10, 30)
common i, j, k
common /integer/ real, subroutine, function(10, 30)
common value, pointer, save
end

where (pressure <= 1.0)
   pressure = pressure + inc_pressure
   temp = temp - 5.0
end where

where (integer <= real)
   integer = real
end where

where (integer <= real)
   integer = real
end where

mywhere: where (integer <= real)
   integer = real
end where mywhere

where: where (integer <= real)
   integer = real
end where where

function: where (integer <= real)
   integer = real
end where function

end

integer%real%my_type%this_func(1) => real
integer%real%my_type%this_func(1, subroutine) => real

end

where (integer <= 1.0)
   integer = integer + real
elsewhere(pressure <= 2.0)
   raining = .true.
end where

where (integer <= 1.0)
   integer = integer + real
elsewhere(integer <= 2.0) mywhere
   raining = .true.
end where

where (integer <= 1.0)
   integer = integer + real
   temp = temp - 5.0
else where(integer <= 2.0)
   raining = .true.
end where

where (integer <= 1.0)
   integer = integer + real
   temp = temp - 5.0
else where(integer <= 2.0) mywhere
   raining = .true.
end where

where (integer <= 1.0)
   integer = integer + real
   temp = temp - 5.0
else where(integer <= 2.0) elsewhere
   raining = .true.
end where

end

forall(integer=1:2:3,function=1:2:10,k=1:2, i) 
integer = function * k
end forall

forall(integer=1:2:3,function=1:2:10,k=1:2, i) 
integer = function * k
end forall integer

forall(integer=1:2:3,function=1:2:10,k=1:2, i) 
integer = function * k
endforall integer
end

select case(silly > 0)
case default
   i = 0
case(1)
   i = 1
end select 

select case(integer + real)
case(1:3) 
   i = 0
case(1)
   i = 1
end select 

end

myselect: select case(integer + real)
case(1:3) myselect
   i = 0
case(1) myselect
   i = 1
end select myselect

select case(real*2)
case default
   integer = function
case (4)
   integer = character
end select 
end

select case(integer + real)
case(integer:real) 
   integer = real
case(1)
   integer = function
end select 

subroutine: select case(integer + real)
case(1:3) subroutine
   i = 0
case(1) subroutine
   i = 1
case default subroutine ! not sure this is valid..
   integer = 2 * real
end select subroutine

end

select type (a=>p_or_c)
class is(point)
  print *, a%x, a%y
type is(point_3d)
  print*, a%x, a%y, a%z
end select

select type (a=>p_or_c)
class is(integer)
  print *, a%x, a%y
type is(integer)
  print*, a%x, a%y, a%z
end select

myselecttype: select type (a=>p_or_c)
class is(integer) myselecttype
  print *, a%x, a%y
type is(integer)    myselecttype
  print*, a%x, a%y, a%z
end select myselecttype

select: select type (a=>p_or_c)
class   is(integer) select
  print *, a%x, a%y
type is(integer)    select
  print*, a%x, a%y, a%z
end select select
end

do while (ios == 0)
   print *, 'hello'
end do

do while = 1,10
   print *, 'hello'
end do

! are these loops even valid fortran??
! they do work (they're accepted by the parser)
do while(:) = 1,10
   print *, 'hello'
end do
do while(i) = 1,10
   print *, 'hello'
end do
do while(i,j,k=3) = 1,10,10
   print *, 'hello'
end do
function: do while(:) = 1,10
   print *, 'hello'
end do      function
do while(i) = 1,10
   print *, 'hello'
end do
do while(integer,real,k=3, value(:,:)) = 1,10,10
   print *, 'hello'
end do

end

cycle
cycle i
cycle real
cycle function
end

where(i > j) i = j
where(i > j) integer = real
where(integer < real) integer = real

where = subroutine

end

do 10 ,while = 1,10
   print *, 'hello'
10 continue

do 10 ,while = 1,10
   print *, 'hello'
  10  continue

do 10 i = 1, 10
   do 10 j = 1, 10
      k = i+j
10 continue

do 80, l = 1, n
   do 80 i = 1,m
      if(sum > sum_max) goto 81
80    continue
81    continue

do 80, l = 1, n
   do 80 i = 1,m
      call calculate(array(i), result)
      if(result < 0.) cycle
      if(sum > sum_max) goto 81
80    continue
81    continue

end

n = 0
do 100 i = 1,10
   j = i
   do 100 k = 1,5
      l = k
100   n = n + 1

do 30 i = 1,10
   do 30 j = 1,10
30 end do

do 30 i = 1,10
   do 30 j = 1,10
30 enddo
do 40 i = 1,10
   do 40 j = 1,10
40 enddo

do 50 i = 1,10
   do 50 j = 1,5
50 print *, 'hello'   

do
   i = 3
end do

outer: do l = 1, n
   inner: do 40 i = 1,m
40 end do inner
end do outer
end

format(i) = 3
end

integer(1)%real(3) = 3
chart(i)%element_wt = weights(i+n-1)
integer(i)%element_wt = weights(i+n-1)

end

scalar_parent%scalar_field = scalar_parent%scalar_field
array_parent(j)%scalar_field = array_parent(j)%scalar_field
array_parent(1:n)%scalar_field = array_parent(1:n)%scalar_field
end

block data 
end block data

block data block
end block data block

blockdata integer
end blockdata integer

block data real
endblock data real

block data real
endblockdata real

interface int
end interface int

abstract interface 
end interface

interface real
end interface real

interface real
endinterface real

end

interface my_proc_decls
   module procedure myProcDecl
   module procedure myProc
   module procedure myProc2
   module procedure myProc3, function, subroutine
end interface my_proc_decls

end

module procedure

end module procedure

entry integer
entry real(integer, function)
end

! most likely, these are not actually valid fortran
intrinsic int1, int2
intrinsic integer, real, intrinsic
intrinsic :: integer, real
end

external sub0
external :: sub1, sub2
external integer
external :: integer, function
end

import a
import :: b
import integer
import :: integer, real, use
end

!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
! everything above this line works.
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!


end
