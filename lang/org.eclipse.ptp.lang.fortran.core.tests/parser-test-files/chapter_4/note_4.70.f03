!   NOTE 4.70
!   Using the type definition for LINE in Note 4.32, an example of the construction of a derived-type
!   scalar value with a rank-2 array component is:

x=   LINE (RESHAPE ( (/ 0.0, 0.0, 1.0, 2.0 /), (/ 2, 2 /) ), 0.1, 1)
end

!   The RESHAPE intrinsic function is used to construct a value that represents a solid line from (0,
!   0) to (1, 2) of width 0.1 centimeters.
