!          NOTE 4.37
!          It is not required that initialization be specified for each component of a derived type. For example:

          TYPE DATE
              INTEGER DAY
              CHARACTER (LEN = 5) MONTH
              INTEGER :: YEAR = 1994               ! Partial default initialization
          END TYPE DATE

!          In the following example, the default initial value for the YEAR component of TODAY is overridden
!          by explicit initialization in the type declaration statement:

          TYPE (DATE), PARAMETER :: TODAY = DATE (21, "Feb.", 1995)
end
