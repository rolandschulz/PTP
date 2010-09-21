program p
    a = 3
    d = 4

    stop
    contains
	recursive subroutine s !<<<<<7,5,7,15,pass

	integer ::a
	call s
	end subroutine

end program
