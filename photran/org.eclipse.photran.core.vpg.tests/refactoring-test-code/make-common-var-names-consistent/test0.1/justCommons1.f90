program common1
    common /block/ a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z, &
                   a1, b1, c1, d1, e1, f1, g1, h1, i1, j1, k1, l1, m1, n1, o1, p1, q1, r1, s1, t1, u1, v1, w1, x1, y1, z1
end program common1

subroutine s
    common /block/ i, j !<<<<< 7, 13, 7, 18,, pass
end subroutine
