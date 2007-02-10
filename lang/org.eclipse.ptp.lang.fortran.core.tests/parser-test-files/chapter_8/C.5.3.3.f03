            DO I = 1, 10
               J=I
               DO 60, K = 5, 1           ! This inner loop is never executed
                  L=K
                  N=N+1
         60    CONTINUE                  ! Labeled DO inside a nonlabeled DO
            END DO
END
