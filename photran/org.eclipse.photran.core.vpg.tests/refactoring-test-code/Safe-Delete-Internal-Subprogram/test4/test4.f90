program p
    a = 3
    d = 4
   stop
end program

    subroutine s !<<<<<7,5,7,15,fail-initial

	integer ::a
	call s
	end subroutine

