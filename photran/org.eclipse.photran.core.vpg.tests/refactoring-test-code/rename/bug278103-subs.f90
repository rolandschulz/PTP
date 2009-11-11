subroutine MySub2

    USE Mod1

    REAL :: MyArray2(DIM) ! 5,22

    SAVE MyArray2

    MyArray2(DIM - 3) = 0.1 ! 9,14

    print *, MyArray2(DIM - 3) ! 11,23

    CALL PrintDim

end subroutine MySub2
