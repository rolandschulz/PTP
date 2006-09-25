program GaussianElimination
! Solve a linear system of equations with Gaussian Elimination
!   and Back Substitution

! SUBROUTINES: mtxrd,mtxwrt,mtxmlt

! Always declare ALL variables
  implicit none
  INTEGER :: indx,jndx,kndx,lndx ! loop counters
  INTEGER :: nsize               ! # of equations (size of the A matrix)
  REAL :: amtx(10,10)            ! A matrix in Ax=B
  REAL :: bvct(10)               ! B vector in Ax=B
  REAL :: xvct(10)               ! x vector in Ax=B
  REAL :: scl(10)                ! Scale factors
  INTEGER :: irow(10)            ! Row numbers (for swapping rows)
  INTEGER :: ipivot              ! Pivot row
  INTEGER :: itemp               ! Temporary value while swapping
  REAL :: tmpval                 ! Temporary value while pivoting
  REAL :: pival                  ! Pivot value
  REAL :: fctr                   ! Multiplication factor for row reduction
  CHARACTER(len = 64) :: filein  ! Input file name
  INTEGER :: ieof                ! Flag to ensure the file opens correctly

! Initially, no row swaps
  do indx = 1, 10
    irow(indx) = indx
  end do

! Introduce the program
  write (*, *) &
    'Program to solve a linear system of equations using'
  write (*, *) &
    '  Gaussian Elimination and Back Substitution'
  write (*, *) ! A Blank Line

! Read from a file (we don't have to type all those numbers)
  write (*, *) 'Enter the input file name: '
  read (*, *) filein
  open (10, file = filein, status = 'old', iostat = ieof)

! If I have trouble opening the file, blow me out of the program!
  if (ieof .ne. 0) then
    write (*, *) 'File error!'
    stop
  end if

! Input the A matrix - write it out for the user
  write (*, *) 'A Matrix:'
  call mtxrd(amtx,nsize,nsize)
  call mtxwrt(amtx,irow,nsize,nsize)
! Input the B vector - write it out for the user
  write (*, *) ! Blank line
  write (*, *) 'B Vector:'
  call vctrd(bvct,nsize)
  call vctwrt(bvct,nsize)
  
! *** FIND SCALE FACTORS *** !

  do indx = 1,nsize
    scl(indx) = abs(amtx(indx,1))
    do jndx = 2, nsize
      if (abs(amtx(indx,jndx)) > scl(indx)) then
        scl(indx) = abs(amtx(indx,jndx))
      end if
    end do
  end do
  
  write (*, *) ! Blank line
  write (*, *) 'Scale factors:'
  call vctwrt(scl,nsize)
  
! *** BEGIN GAUSSIAN ELIMINATION (ROW REDUCTION) *** !

  do kndx = 1, nsize-1  ! For each column k...

    ! Choose the pivot element
    ipivot = kndx   ! WAS ipivot = irow(kndx)
    pival = abs(amtx(irow(ipivot), kndx) / scl(irow(ipivot)))
    do lndx = kndx+1, nsize
      tmpval = abs(amtx(irow(lndx), kndx) / scl(irow(lndx)))
      if (tmpval > pival) then
        pival = tmpval
        ipivot = lndx   ! WAS ipivot = irow(lndx)
      end if
    end do
    
    !write (*, *) ! Blank line
    !write (*, *) 'Swapping rows ', kndx, ' and ', ipivot, ' -- pival is ', pival
    
    ! Row swap
    itemp = irow(kndx)
    irow(kndx) = irow(ipivot)
    irow(ipivot) = itemp

    !call augwrt(amtx,irow,bvct,nsize)
    
    ! Row reduce
    do indx = kndx+1, nsize  ! For each row i > k...
      fctr = amtx(irow(indx),kndx) / amtx(irow(kndx),kndx)
      amtx(irow(indx),kndx) = 0.0      ! We're zeroing out a(i,k)
      do jndx = kndx+1,nsize     ! Scale & subtract the rest of the row
        amtx(irow(indx),jndx) = amtx(irow(indx),jndx) - fctr * amtx(irow(kndx),jndx)
      end do
      bvct(irow(indx)) = bvct(irow(indx)) - fctr * bvct(irow(kndx))
    end do
    
    !write (*, *) 'Reduction:'
    !call augwrt(amtx,irow,bvct,nsize)
    
  end do

! *** END GAUSSIAN ELIMINATION (ROW REDUCTION) *** !


  write (*, *) ! Blank line
  write (*, *) 'Reduced, Augmented Matrix:'
  call augwrt(amtx,irow,bvct,nsize)

! Back Substitute
  do indx = nsize, 1, -1  ! Start at lower right and work upwards
    xvct(indx) = bvct(irow(indx))
! Subtract the previous x-values times their respective coefficients
    do jndx = indx+1, nsize
      xvct(indx) = xvct(indx) - amtx(irow(indx),jndx) * xvct(jndx)
    end do
! Divide by the current coefficient
    xvct(indx) = xvct(indx) / amtx(irow(indx),indx)
  end do

  write (*, *) ! Blank line
  write (*, *) 'Solution Vector:'
  call vctwrt(xvct,nsize)

  stop

contains

  subroutine mtxrd(amtx,mrow,ncol)

! ABSTRACT: Subroutine to read an m x n matrix from a file
!   FORMAT: first line has the dimensions
!           each following line has a row of the matrix

! Always declare ALL variables
    implicit none
    INTEGER :: irow,jcol               ! row, column loop counters
    INTEGER, INTENT(OUT) :: mrow,ncol  ! # rows, columns
    REAL, INTENT(OUT) :: amtx(:,:)     ! matrix

! Read the size of the matrix
    read (10, *) mrow,ncol

! Read the matrix, one row at a time
    do irow = 1,mrow
      read (10, *) (amtx(irow,jcol),jcol=1,ncol)
    end do

    return
  end subroutine mtxrd

  subroutine mtxwrt(amtx,irow,mrow,ncol)

! ABSTRACT: Subroutine to write an m x n matrix to the screen

! Always declare ALL variables
    implicit none
    INTEGER :: indx,jcol              ! row, column loop counters
    INTEGER, INTENT(IN) :: mrow,ncol  ! # rows, columns
    INTEGER, INTENT(IN) :: irow(:)    ! For row swapping
    REAL, INTENT(IN) :: amtx(:,:)     ! matrix

! Write the matrix, one row at a time
    do indx = 1,mrow
      write (*, '('' '',10g10.4)') (amtx(irow(indx),jcol),jcol=1,ncol)
    end do
    return
  end subroutine mtxwrt

  subroutine augwrt(amtx,irow,bvct,nsize)

! ABSTRACT: Subroutine to write an n x n matrix with a vector augmented to it

! Always declare ALL variables
    implicit none
    INTEGER :: indx,jcol            ! row, column loop counters
    INTEGER, INTENT(IN) :: nsize   ! # rows
    INTEGER, INTENT(IN) :: irow(:)    ! For row swapping
    REAL, INTENT(IN) :: amtx(:,:)  ! matrix
    REAL, INTENT(IN) :: bvct(:)    ! vector

! Write the matrix, one row at a time
    do indx = 1,nsize
      write (*, '('' '',10g10.4)') &
        (amtx(irow(indx),jcol),jcol=1,nsize),bvct(irow(indx))
    end do
    return
  end subroutine augwrt

  subroutine vctrd(vctr,nsize)

! ABSTRACT: Subroutine to read a vector from a file
!   FORMAT: first line has the dimensions
!           each following line has a row of the vector

! Always declare ALL variables
    implicit none
    INTEGER :: irow               ! row loop counters
    INTEGER, INTENT(OUT) :: nsize ! # rows
    REAL, INTENT(OUT) :: vctr(:)  ! vector

! Read the size of the matrix
    read (10, *) nsize

! Read the matrix, one row at a time
    do irow = 1,nsize
      read (10, *) vctr(irow)
    end do

    return
  end subroutine vctrd

  subroutine vctwrt(vctr,nsize)

! ABSTRACT: Subroutine to write a vector to the screen

! Always declare ALL variables
    implicit none
    INTEGER :: irow               ! row loop counters
    INTEGER, INTENT(IN) :: nsize ! # rows
    REAL, INTENT(IN) :: vctr(:)  ! vector

! Write the matrix, one row at a time
    do irow = 1,nsize
      write (*, *) vctr(irow)
    end do
    return
  end subroutine vctwrt

end program GaussianElimination
