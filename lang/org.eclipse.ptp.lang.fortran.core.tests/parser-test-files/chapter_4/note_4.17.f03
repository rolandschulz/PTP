!          NOTE 4.17

!          An example of a derived-type definition is:

          TYPE PERSON
             INTEGER AGE
             CHARACTER (LEN = 50) NAME
          END TYPE PERSON

!          An example of declaring a variable CHAIRMAN of type PERSON is:

          TYPE (PERSON) :: CHAIRMAN
end