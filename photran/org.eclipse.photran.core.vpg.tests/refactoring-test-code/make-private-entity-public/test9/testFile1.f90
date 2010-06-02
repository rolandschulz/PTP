program testFile
    real a, b
    double precision dd
    integer g
    public a, b
    private dd
end program testFile

subroutine help
    integer q, w, e
    public q, w !<<<<< 11, 15, 11, 16, fail-initial
    private e
end subroutine help
