! Demonstrates the Fortran 2008 BLOCK construct
! Exercises R213, 807, 808, 809
! J. Overbey - 1 Dec 2009
implicit none

integer :: n = 1
print *, n  ! Prints 1

block
end block

name: block
end block

name: block
end block name

block
endblock

name: block
endblock

name: block
endblock name

!block: block
!end block block

block
    integer :: n = 2  ! Shadows the above
    print *, n   ! Prints 2
end block

print *, n  ! Prints 1

end program
