       READ (IUN, '(1X, G14.7)', IOSTAT = IOS) X
       DO WHILE (IOS == 0)
          IF (X >= 0.) THEN
             CALL SUBA (X)
             CALL SUBB (X)

             CALL SUBZ (X)
          ENDIF
          READ (IUN, '(1X, G14.7)', IOSTAT = IOS) X
       END DO
END
