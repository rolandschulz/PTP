! Demonstrates the Fortran 2008 SYNC statements
! Exercises R214, 858-862
! J. Overbey - 8 Dec 2009

integer, parameter :: THREE = 3

sync=7
all=9
images=13
memory=12
sync=all
syncall=15
syncimages=32+syncall
syncmemory=syncimages
stat=3
errmsg="Hello"

sync all
sync all (stat=s)
sync all (errmsg=e)
sync all (stat=s, errmsg=e)
sync all (errmsg=e, stat=s)
syncall
syncall (stat=s)
syncall (errmsg=e)
syncall (stat=s, errmsg=e)
syncall (errmsg=e, stat=s)

sync images (3)
sync images (THREE)
sync images (1*(2*THREE))
sync images (*)
syncimages  (3)
syncimages  (THREE)
syncimages  (1*(2*THREE))
syncimages  (*)

sync images (1*(2*THREE), stat=s)
sync images (*,           stat=s)
syncimages  (1*(2*THREE), stat=s)
syncimages  (*,           stat=s)

sync images (1*(2*THREE), stat=s, errmsg=e)
sync images (*,           stat=s, errmsg=e)
syncimages  (1*(2*THREE), stat=s, errmsg=e)
syncimages  (*,           stat=s, errmsg=e)

sync memory
sync memory (stat=s)
sync memory (errmsg=e)
sync memory (stat=s, errmsg=e)
sync memory (errmsg=e, stat=s)
syncmemory
syncmemory (stat=s)
syncmemory (errmsg=e)
syncmemory (stat=s, errmsg=e)
syncmemory (errmsg=e, stat=s)

end program
