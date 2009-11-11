PROGRAM MyProgram

    CALL MySub

    CALL MySub2

END PROGRAM MyProgram


subroutine MySub

    USE Mod1

    INTEGER, DIMENSION(DIM) :: MyArray = (/1, 10, 100, 1000, 10000/) ! 14,24

    print *, MyArray(DIM - 2) ! 16,22

end subroutine MySub
