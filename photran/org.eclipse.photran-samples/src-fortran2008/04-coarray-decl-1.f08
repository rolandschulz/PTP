! Exercises the following rules for co-array declarations:
! R437, 438, 502, 503, 509-513
! J. Overbey - 7 Dec 2009

implicit none

integer, parameter :: FIVE = 5

type t
  integer, allocatable :: n
  ! R437
  integer, codimension[*] :: ar1
  integer, contiguous     :: ar2
  integer                 :: ar3
  ! R438
  integer                 :: ar4(5,5)[*]*7
  integer                 :: ar5     [*]
end type

  ! R502
  integer, codimension[*] :: ar1
  integer, contiguous     :: ar2
  integer                 :: ar3
  ! R503
  integer                 :: ar4(5,5)[*]*7
  integer                 :: ar5     [*]

  ! R509 handled via tests for R510 and R511

  ! R510
  integer, codimension[:] :: deferred1
  integer, codimension[:,:] :: deferred2
  integer, codimension[:,:,:] :: deferred3
  integer, codimension[:,:,:,:] :: deferred4
  integer, codimension[:,:,:,:,:] :: deferred5

  ! R511
  integer, codimension[3:5, 7, 9:*] :: explicit1
  integer, codimension[3:5, 7,   *] :: explicit2
  integer, codimension[  5, 7,   *] :: explicit3
  integer, codimension[     7,   *] :: explicit4
  integer, codimension[   3+7,   *] :: explicit5
  integer, codimension[(FIVE+1)*2, (FIVE+1)*2:*] :: explicit6

end program
