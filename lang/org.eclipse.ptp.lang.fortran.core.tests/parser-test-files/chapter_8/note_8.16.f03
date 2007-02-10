           DO I = 1, M
              DO J = 1, N
                 C (I, J) = SUM (A (I, J, :) * B (:, I, J))
              END DO
           END DO
END