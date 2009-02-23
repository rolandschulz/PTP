! Testing behavior of pass-by-reference

integer, parameter :: q = 1

call s(1)  ! Can't byref (literal)

n = 1
call s(n)  ! Can byref (variable)
print *, "Expected:", 2, "Actual:", n

call s(q)  ! Can't byref (parameter)
print *, "Expected:", 1, "Actual:", q

stop

contains
	subroutine s(p)
		integer :: p
		p = 2
		print *, "Expected:", 2, "Actual:", p
	end subroutine
end
