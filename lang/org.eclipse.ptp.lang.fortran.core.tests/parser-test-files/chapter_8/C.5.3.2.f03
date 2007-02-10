            N=0
            DO 50, I = 1, 10
               J=I
               DO K = 1, 5
                  L=K
                  N = N + 1 ! This statement executes 50 times
               END DO        ! Nonlabeled DO inside a labeled DO
         50 CONTINUE
END
