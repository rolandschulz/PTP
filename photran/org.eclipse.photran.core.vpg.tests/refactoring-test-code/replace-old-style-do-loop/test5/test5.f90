program Test5 !<<<<< 1, 1, 46, 18, pass
! Shared Do Loop Termination
    do 100 j=1,10
    do 100 w=1,10
       100 i=j+1
! simple Do Loop
    do 110 i = 1,10
    110 j=i
! simple Do Loop with Continue
    do 120 i = 1,10
    120 continue
! simple f90 Do Loop
    do i =1,100
        w=i
    end do
! More Complex Do Loop
  DO 140 K=1,KM
      DO 140 I=1,IMT
        USAV(I,K)=UCLIN(I,K)
        VSAV(I,K)=VCLIN(I,K)
        UCLIN(I,K)=UP(I,K)
        VCLIN(I,K)=VP(I,K)
 140  CONTINUE
! More Complex Do Loop
     DO 170 K=1,KM
     DO 170 I=1,IMU
     IF(KMUP(I).GE.KAR(K)) THEN
     UBP(I,K)=UBP(I,K)+SFUB(I)
     VBP(I,K)=VBP(I,K)+SFVB(I)
     UP (I,K)=UP (I,K)+SFU (I)
     VP (I,K)=VP (I,K)+SFV (I)
     ENDIF
 170 CONTINUE
! More complex Shared Do Loop Termination
      DO 270 K=2,KM
      DO 270 I=1,IMT
      DPDX(I,K)=RHON(I,K-1)+RHON(I,K)
      DPDY(I,K)=RHOS(I,K-1)+RHOS(I,K)
 270  CONTINUE
! Complex Shared Do Loop Termination
  DO 150 K=1,KM
  DO 150 I=1,IMT
  DO 150 J=1,IMT
  DO 150 W=1,IMT
  150 DPDX(I,K)=RHON(I,K-1)+RHON(I,K)
end program Test5
