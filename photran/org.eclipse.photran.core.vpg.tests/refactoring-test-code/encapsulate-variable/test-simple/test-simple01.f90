program main
    use module1
    implicit none
    integer :: blah
    
    blah = i + j + k !<<<<< 6, 12, 6, 13, pass
    
    i = blah + j
    
    call mult(i,j,blah)
    
contains
    subroutine mult(a,b,c)
        integer, intent(in) :: a, b, c
        integer :: temp
        temp = a*b*c
    end subroutine

end program
