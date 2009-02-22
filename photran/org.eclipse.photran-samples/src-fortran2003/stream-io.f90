!>
!! Sample program demonstrates stream I/O in Fortran 2003.
!!
!! Jeff Overbey (2/22/09)
!<
program stream_io
    implicit none

    integer, parameter :: FILE = 2

    call check_byte_kind
    call create_file
    call read_file
    stop

contains
    subroutine check_byte_kind
        integer(kind=1) :: byte = 127 + 1
        integer         :: int  = 127 + 1
        if (byte .ne. -128 .or. int .ne. 128) &
            stop "integer(kind=1) is not one byte"
    end subroutine

    subroutine create_file
        integer(kind=1) byte

	    open (FILE, status='replace', action='write', access='stream', form='unformatted', file='bytes')
	    write (FILE) transfer("A", byte)
	    write (FILE) transfer("B", byte)
	    write (FILE) new_line("x")
	    write (FILE) transfer("C", byte)
	    close (FILE)
    end subroutine

    subroutine read_file
        integer(kind=1) byte
        integer pos, status
        character*256 :: errmsg

	    open (FILE, status='old', action='read', access='stream', form='unformatted', file='bytes')
	    do
	        inquire (FILE, pos=pos)
	        print *, "Position is", pos

	        !read (FILE, pos=pos, iostat=status, iomsg=errmsg) byte
	        read (FILE, iostat=status, iomsg=errmsg) byte
	        if (status .eq. 0) then
	            print '("Read byte ", i5, ", status is ", i5)', byte, status
	        else if (is_iostat_end(status)) then
	            exit
	        else
	            print *, errmsg
	            stop "I/O error"
	        end if
	    end do
	    print '("Read ", i5, " bytes")', pos-1
	    close (FILE)
    end subroutine
end program
