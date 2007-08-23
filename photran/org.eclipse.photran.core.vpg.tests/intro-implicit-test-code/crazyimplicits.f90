program p
    a = 3
    d = 4
    stop
contains
subroutine s
    implicit integer (a-c,g), real (d-e), type(t) (f), complex (h)
    type t
        integer n
    end type
    
    a = 3
    b = 3
    c = 3
    d = 3.0
    e = 3.0
    f%n = 3
    g = 3
    h = (3, 4)
end subroutine
end program
