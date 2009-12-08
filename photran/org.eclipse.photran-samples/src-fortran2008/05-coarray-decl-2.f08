! Exercises the following rules for co-array declarations:
! R531-533
! J. Overbey - 7 Dec 2009

  implicit none

  integer, parameter :: FIVE = 5

  integer :: ar1, ar2, ar3, ar4, ar5, ar6, ar7, ar8, ar9, ar10, &
             deferred1, deferred2, deferred3, deferred4, deferred5, &
             explicit1, explicit2, explicit3, explicit4, explicit5, explicit6

  codimension :: ar1[*]
  codimension    ar2[*]

  contiguous :: ar3
  contiguous    ar4

  contiguous :: ar5, ar6, ar7
  contiguous    ar8, ar9, ar10

  codimension :: deferred1[:], deferred2[:,:], deferred3[:,:,:]
  codimension    deferred4[:,:,:,:], &
                 deferred5[:,:,:,:,:]

  codimension :: explicit1[3:5, 7, 9:*]
  codimension :: explicit2[3:5, 7,   *]
  codimension :: explicit3[  5, 7,   *]
  codimension :: explicit4[     7,   *]
  codimension :: explicit5[   3+7,   *], &
                 explicit6[(FIVE+1)*2, (FIVE+1)*2:*]

end program
