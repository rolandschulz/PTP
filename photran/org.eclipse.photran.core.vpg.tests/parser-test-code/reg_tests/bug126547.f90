module blabla
implicit none

type,public :: atomic_type
    integer ::n
    integer, allocatable :: orbs(:,:)
end type atomic_type

end module blabla