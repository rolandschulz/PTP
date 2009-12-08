! Demonstrates the Fortran 2008 LOCK statements
! Exercises R214, 863-866
! J. Overbey - 8 Dec 2009

integer, parameter :: THREE = 3

lock (l)
lock (l, acquired_lock=q)
lock (l, acquired_lock=q, errmsg=e)
lock (l,                  errmsg=e, stat=s)
lock (l, acquired_lock=q,           stat=s)
lock (l,                  errmsg=e)
lock (l,                            stat=s)
lock (l, acquired_lock=q, errmsg=e, stat=s)
lock (l, stat=s, errmsg=e, acquired_lock=q)

unlock (l)
unlock (l, stat=s)
unlock (l, errmsg=e)
unlock (l, stat=s, errmsg=e)
unlock (l, errmsg=e, stat=s)

lock=3
acquired_lock=3
stat=3
errmsg="Hello"
unlock=3 * lock + acquired_lock

end program
