! Exercises the Section 6 rules for co-array expressions
! J. Overbey - 7 Dec 2009

! This is obviously not semantically legal

! TODO: Finish this

a[3,4] = 7
b(1,2)[3,4] = a[3,4]
b(1,2)[3,4] = a[3,4:8] * b(3)%c[3,4]

allocate (a[3:4,5])

end program
