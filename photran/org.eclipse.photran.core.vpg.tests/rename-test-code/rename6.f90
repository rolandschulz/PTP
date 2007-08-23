implicit none; type outer ! outer 21
  integer :: i
  type(outer), pointer :: inner ! outer 8
end type
integer :: j ! j 12
integer, parameter :: i = 2   ! i 23
character(len=i), save :: hi = 'hi', bye = 'bye' ! i 15 hi 27 bye 38
!type(outer) :: type
type(outer) :: ty ! outer 6 ty 16
integer :: array(5) ! array 12

!type%i = type%inner%i - i ! i 25

!2      9            22
 ty%i = ty%inner%i - i

!         11   16  20   25   30   35   40
namelist /nl1/ hi, bye /nl2/ hi, /nl3/ bye
read (5, nml=nl1) ! nl1 14

!    6  9   13               30
data hi,bye,ty%i /'Hi','Bye',i/
!    6       14
data array/5*i/
!    6     12
data array(i) /3/
!     7     13  17    23    29
data (array(j), j=1,5,i) /3*i/
end
