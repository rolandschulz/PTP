!          NOTE 4.50
!          A deferred binding (4.5.5) defers the implementation of a type-bound procedure to extensions of
!          the type; it may appear only in an abstract type. The dynamic type of an object cannot be
!          abstract; therefore, a deferred binding cannot be invoked. An extension of an abstract type need
!          not be abstract if it has no deferred bindings. A short example of an abstract type is:

module mod
               TYPE, ABSTRACT :: FILE_HANDLE
               CONTAINS
                  PROCEDURE(OPEN_FILE), DEFERRED, PASS(HANDLE) :: OPEN
               END TYPE
end module

!          For a more elaborate example see C.1.3.

