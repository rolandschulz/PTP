          INTEGER FUNCTION SIGNUM (N)
          SELECT CASE (N)
          CASE (:-1)
             SIGNUM = -1
          CASE (0)
             SIGNUM = 0
          CASE (1:)
             SIGNUM = 1
          END SELECT
          END

