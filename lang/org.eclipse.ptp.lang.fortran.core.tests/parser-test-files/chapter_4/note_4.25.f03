!         NOTE 4.25
!         Given

                 TYPE :: t1(k1,k2)
                   INTEGER,KIND :: k1,k2
                   REAL(k1) a(k2)
                 END TYPE

                 TYPE,EXTENDS(t1) :: t2(k3)
                   INTEGER,KIND :: k3
                   LOGICAL(k3) flag
                 END TYPE
end

!          the type parameter order for type T1 is K1 then K2, and the type parameter order for type T2 is
!          K1 then K2 then K3.
