             SUM = 0.0
             READ (IUN) N
             OUTER: DO L = 1, N           ! A DO with a construct name
                READ (IUN) IQUAL, M, ARRAY (1:M)
                IF (IQUAL < IQUAL_MIN) CYCLE OUTER    ! Skip inner loop
                INNER: DO 40 I = 1, M     ! A DO with a label and a name

                  CALL CALCULATE (ARRAY (I), RESULT)
                  IF (RESULT < 0.0) CYCLE
                  SUM = SUM + RESULT
                  IF (SUM > SUM_MAX) EXIT OUTER
         40    END DO INNER
            END DO OUTER
END
