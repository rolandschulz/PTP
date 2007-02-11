!          NOTE 4.34
!          The default initial value of a component of derived type may be overridden by default initialization
!          specified in the definition of the type. Continuing the example of Note 4.37:

          TYPE SINGLE_SCORE
              TYPE(DATE) :: PLAY_DAY = TODAY
              INTEGER SCORE
              TYPE(SINGLE_SCORE), POINTER :: NEXT => NULL ( )
          END TYPE SINGLE_SCORE

          TYPE(SINGLE_SCORE) SETUP
end

!          The PLAY DAY component of SETUP receives its initial value from TODAY, overriding the
!          initialization for the YEAR component.
