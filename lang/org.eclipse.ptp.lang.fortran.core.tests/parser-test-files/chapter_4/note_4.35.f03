!          NOTE 4.35
!          Arrays of structures may be declared with elements that are partially or totally initialized by
!          default. Continuing the example of Note 4.38 :

          TYPE MEMBER (NAME_LEN)
             INTEGER, LEN :: NAME_LEN
             CHARACTER (LEN = NAME_LEN) NAME = ''
             INTEGER :: TEAM_NO, HANDICAP = 0
             TYPE (SINGLE_SCORE), POINTER :: HISTORY => NULL ( )
          END TYPE MEMBER

         TYPE (MEMBER(9)) LEAGUE (36)         ! Array of partially initialized elements
         TYPE (MEMBER(9)) :: ORGANIZER = MEMBER ("I. Manage",1,5,NULL ( ))

!         ORGANIZER is explicitly initialized, overriding the default initialization for an object of type
!         MEMBER.

!         Allocated objects may also be initialized partially or totally. For example:

         ALLOCATE (ORGANIZER % HISTORY)          ! A partially initialized object of type
                                                 ! SINGLE_SCORE is created.
end
