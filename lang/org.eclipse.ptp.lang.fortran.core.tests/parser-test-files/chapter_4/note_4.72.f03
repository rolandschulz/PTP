!   NOTE 4.72
!   An example of an array constructor that specifies a length type parameter:

x =      (/ CHARACTER(LEN=7) :: 'Takata', 'Tanaka', 'Hayashi' /)
end

!   In this constructor, without the type specification, it would have been necessary to specify all of
!   the constants with the same character length.
