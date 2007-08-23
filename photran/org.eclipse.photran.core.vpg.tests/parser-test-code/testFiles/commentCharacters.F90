! qwertyuiopasdfghjklzxcvbnm
! `1234567890-=
! !@#$%^&*()_+

! []{};':",./<>?
! tab 	
! ~ \ |
!`1234567890-=!@#$%^&*()_+[]{};':",./<>? 	~\|

PROGRAM test_comments !comment after stmt:`1234567890-=!@#$%^&*()_+[]{};':",./<>? 	~\|
	CHARACTER(5) :: str

	WRITE(*,  &  !comment in continuatiton:`1234567890-=!@#$%^&*()_+[]{};':",./<>? 	~\|
      *) &
	'bla bla string'


END PROGRAM test_comments
