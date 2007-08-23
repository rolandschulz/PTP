program substringTest
	stop
contains

! This is fine...
subroutine x
	character (len=80) :: a(3)
	character (len=80) :: b = "whatever"
	a(1) = b(1:5)
end subroutine x

! ...but this semantically equivalent subroutine fails to parse
subroutine y
	character (len=80) :: a(3)
	character (len=80) :: b
	b = "whatever"
	a(1) = b(1:5)
end subroutine y

end program substringTest
