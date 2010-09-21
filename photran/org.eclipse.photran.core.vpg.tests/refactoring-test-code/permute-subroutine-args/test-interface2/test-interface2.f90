program testoptional
    interface
        subroutine testsub(A,B,*,*,C,D,E,F)
            integer, optional :: C
            integer, optional :: E
        end subroutine
    end interface

    call testsub(1,2,200,200,3,4,5,6)

    call testsub(1,2,200,200,F=3,D=4)

200 print *, "hello, world!"
end program testoptional

subroutine testsub(A,B,*,*,C,D,E,F) !<<<<< 16,1,16,5,2,1,3,0,5,4,6,7,pass
    integer, optional :: C
    integer, optional :: E
end subroutine

subroutine testsub2
    interface
        subroutine testsub(A,B,*,*,C,D,E,F)
            integer, optional :: C
            integer, optional :: E
        end subroutine

        subroutine testsub(A)
            integer :: A
        end subroutine
    end interface

    call testsub(1,2,300,300,D=20,E=30,F=2,C=5)

300 print *, "world, hello!!"

end subroutine
