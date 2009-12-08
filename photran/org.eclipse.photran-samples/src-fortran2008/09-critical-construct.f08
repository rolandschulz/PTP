! Demonstrates the Fortran 2008 CRITICAL construct
! Exercises R213, 810-812
! J. Overbey - 8 Dec 2009
implicit none

critical
end critical

name: critical
end critical

name: critical
end critical name

critical
endcritical

name: critical
endcritical

name: critical
endcritical name

!critical: critical
!end critical critical

end program
