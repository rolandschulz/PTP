program priv2
    implicit none
    double precision f
    real q
    private f
    public q !<<<<< 6, 12, 6, 13, fail-initial
end program priv2
