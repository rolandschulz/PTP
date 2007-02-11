!          NOTE 4.67
!          A one-dimensional array may be reshaped into any allowable array shape using the RESHAPE
!          intrinsic function (13.7.146). An example is:

          X = (/ 3.2, 4.01, 6.5 /)
          Y = RESHAPE (SOURCE = [ 2.0, [ 4.5, 4.5 ], X ], SHAPE = [ 3, 2 ])

!          This results in Y having the 3 × 2 array of values:
!          2.0    3.2
!          4.5    4.01
!          4.5    6.5
end
