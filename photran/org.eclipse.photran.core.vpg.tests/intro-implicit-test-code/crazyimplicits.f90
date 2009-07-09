program p
    type t
        integer n
    end type
    type(t) u

    a = 1
    d = 1
    u = t(5)
    call s
    print *, a, d, u
    stop
contains
  subroutine s
    implicit integer (a-c,g), real (d-e), type(t) (f), complex (h)

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
