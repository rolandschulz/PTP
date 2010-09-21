program p
    a = 3
    d = 4
  interface
    subroutine s
    end subroutine s
  end interface
    stop
end program

subroutine s !<<<<<11,1,11,11,fail-initial

	integer ::a
	call s
	end subroutine

