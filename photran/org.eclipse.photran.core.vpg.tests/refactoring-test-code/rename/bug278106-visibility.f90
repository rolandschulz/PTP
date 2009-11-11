module m1
  integer :: m1a, m1b ! 2,14 !2,19
end module m1

module m2
  use m1
  private :: m1b ! 7,14
end module m2

subroutine s1
  use m2
  !integer :: m1a !! ILLEGAL
  integer :: m1b ! 13,14
  print *, m1a, m1b ! 14,12 14,17
end subroutine s1

module m3
  use m1
  private
end module m3

subroutine s2
  use m3
  integer :: m1a ! 24,14
  integer :: m1b ! 25,14
  print *, m1a, m1b ! 26,12 26,17
end subroutine s2

module m4
  use m1
  private
  public :: m1a ! 32,13
end module m4

subroutine s3
  use m4
  !integer :: m1a !! ILLEGAL
  integer :: m1b ! 38,14
  print *, m1a, m1b ! 39,12 39,17
end subroutine s3
