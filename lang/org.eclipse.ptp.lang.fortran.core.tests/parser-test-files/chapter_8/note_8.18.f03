       DO     ! A "DO WHILE + 1/2" loop
          READ (IUN, '(1X, G14.7)', IOSTAT = IOS) X
          IF (IOS /= 0) EXIT
          IF (X < 0.) CYCLE
          CALL SUBA (X)
          CALL SUBB (X)

          CALL SUBZ (X)
       END DO

END