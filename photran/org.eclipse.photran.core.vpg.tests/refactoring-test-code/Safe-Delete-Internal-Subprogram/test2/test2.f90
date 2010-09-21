program p
    a = 3
    d = 4
	call s 
    stop
    contains
	subroutine s !<<<<<7,5,7,15,fail-initial

	integer ::a
	call s
	end subroutine

end program