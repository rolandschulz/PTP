module priv2
    implicit none
    double precision f
    real q
    private f !<<<<< 5, 13, 5, 14, pass
    public q
end module priv2

program p
	use priv2
end program p
