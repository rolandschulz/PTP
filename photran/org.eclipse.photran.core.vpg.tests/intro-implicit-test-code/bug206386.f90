program hello
    integer :: i = 2, j = 2, mySillyVariable = 5
    print *, i, j, MYsIlLyVaRiAbLe
    CALL wORTHLESSfUNCTION(i+1,j+1,MySillyVariable)
    print *, i, j, mYsILLYvARIABLE
    stop

    contains
    subroutine WorthlessFunction(i,j,mySillyVariable)
        intent(IN)  :: i, j
        intent(OUT) :: mySillyVariable

        mySillyVariable = i + j
    end subroutine WorthlessFunction
end program
