!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
!
! BstFit
!
! This program provides the user with three options:
! (1) Compute least-squares lines using vertical, horizontal, and
!     perpendicular distances
! (2) Show the distances between points and a given line
! (3) Compute linear, exponential, and power least-squares curves
! 
! Points are read from an ASCII text file, where each line contains a x-value
! followed by a space and a y-value.  Values are expected to be real numbers.
! The number of points is not specified; the program reads data from each line
! until the end of the file (or 1,000 points, whichever comes first).
!
! Output is displayed to the screen.
!
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

module Mdataset

  ! Declare all variables
  implicit none

  ! Declare a Tdataset type to hold data points, count, sums, etc.
  type :: Tdataset
    real             :: datapt(4,1000)    ! (x,y) data points from input file
                                          !    stored as (x, y, ln x, ln y)
    integer          :: npts              ! Number of points in input file
    real             :: valsum(4)         ! Sums of values of
                                          !    x, y, ln x, and ln y
    real             :: sqsum(4)          ! Sums of values of x**2, y**2,
                                          !    (ln x)**2, and (ln y)**2
    real             :: xysum(3)          ! Sums of products
                                          !    (xy, x ln y, ln x ln y)
    logical          :: has_np(2)         ! Are any x- or y-values nonpositive?
  end type Tdataset

end module Mdataset

program BstFitProj

  use Mdataset

  ! Declare all variables
  implicit none

  ! Type                Name(s)             Description
  integer			 :: if
  integer            :: ichoice           ! The user's choice from the menu
  type(Tdataset)     :: ds                ! Points and computed values
  real               :: aval, bval        ! Values of a and b (in y=ax+b)
  real               :: slopem, intrcpt   ! Slope and y-intercept of a line
                                          !   supplied by the user
  real               :: ssvd, sshd, sspd  ! Sum of squares of horiz, vert, and
                                          !   perpendicular distances
  logical            :: suces             ! Exponential or power fit success?
  integer            :: outu              ! Output file unit number

  ! Introduce the program
  write (*, *) 'This program performs least-squares fitting on ', &
                            'a set of n (x,y) pairs'
  write (*, *) ! Blank line

  ! Ask the user for the input file, and read the data from it  
  call LoadDataSet(ds)
  write (*, *) ! Blank line
  
  ! Show the list of points
  write (*, *) 'Data points are:'
  call ShowDataPoints(ds)
  write (*, *) ! Blank line
  
  ! Ask the user for the name of the output file, and open it
  outu = OpenOutput()
  write (*, *) ! Blank line
  
  ! The user hasn't chosen anything from the menu yet
  ichoice = 0
  
  ! Let the user continue choosing from the menu until s/he chooses to exit
  do while (ichoice .ne. 4)
  
    ! Display a list of options, and allow the user to select one
    write (*, *) ! Blank line
    write (*, *) '===================================================='
    write (*, *) 'MENU'
    write (*, *) '===================================================='
    write (*, *) '1. Compute three linear least squares lines'
    write (*, *) '2. Compute distances between points and a given line'
    write (*, *) '3. Find linear, exponential, and power least squares'
    write (*, *) '4. Exit'
    write (*, *) '===================================================='
    write (*, *) ! Blank line
    write (*, *) 'Choose an option: '
    read  (*, *) ichoice
    
    if (if .eq. 5) then
      print *, 'Why did you name a variable "if" anyway?'
    end if

    select case (ichoice)
      case (1) ! Option 1 - Three linear least squares lines

        write (outu, *) 'THREE LEAST SQUARES LINES:'

        ! Linear least squares, vertical distances
        call LinearLeastSquares(ds, 1, 2, aval, bval)
        write (outu, *) ! Blank line
        write (outu, *) 'Using vertical distances:'
        write (outu, *) 'y = ', trim(real2str(aval)), &
                                     'x + ', trim(real2str(bval))

        ! Linear least squares, horizontal distances
        call LinearLeastSquares(ds, 2, 1, aval, bval)
        write (outu, *) ! Blank line
        write (outu, *) 'Using horizontal distances:'
        write (outu, *) 'y = ', trim(real2str(aval)), &
                                     'x + ', trim(real2str(bval))

        ! Linear least squares, perpendicular distances
        call PerpLeastSquares(ds, 0, aval, bval)
        write (outu, *) ! Blank line
        write (outu, *) 'Using perpendicular distances:'
        write (outu, *) 'y = ', trim(real2str(aval)), &
                                     'x + ', trim(real2str(bval))
        
        write (outu, *) '--OR--'

        call PerpLeastSquares(ds, 1, aval, bval)
        write (outu, *) 'y = ', trim(real2str(aval)), &
                                     'x + ', trim(real2str(bval))

        write (outu, *) ! Blank line

      case (2) ! Option 2 - Distances between points and a given line

        write (outu, *) 'SUM OF SQUARES OF DISTANCES BETWEEN ', &
                                     'POINTS AND A GIVEN LINE:'
      
        write (*, *) ! Blank line
        write (*, *) 'Enter the line''s slope and y-intercept: '
        read (*, *) slopem, intrcpt
        
        write (outu, *) ! Blank line
        write (outu, *) 'Using f(x) = ', trim(real2str(slopem)), &
                                     'x + ', trim(real2str(intrcpt))

        write (outu, *) ! Blank line
        call SquareDistances(ds, slopem, intrcpt, ssvd, sshd, sspd)

        write (outu, *) 'Sum of squares of vertical distances:      ', &
                                      ssvd
        write (outu, *) ! Blank line
        write (outu, *) 'Sum of squares of horizontal distances:    ', &
                                      sshd
        write (outu, *) ! Blank line
        write (outu, *) 'Sum of squares of perpendicular distances: ', &
                                      sspd
        write (outu, *) ! Blank line

      case (3) ! Option 3 - Linear, exponential, and power least squares

        write (outu, *) 'LINEAR, EXPONENTIAL, AND POWER LEAST SQUARES:'
      
        call LinearLeastSquares(ds, 1, 2, aval, bval)
        write (outu, *) ! Blank line
        write (outu, *) 'Linear fit:       y = ', trim(real2str(aval)), &
                                     'x + ', trim(real2str(bval))

        call ExpLeastSquares(ds, aval, bval, suces)
        write (outu, *) ! Blank line
        if (suces .eqv. .true.) then
          write (outu, *) 'Exponential fit:  y = ', trim(real2str(bval)), &
                                       ' * e^(', trim(real2str(aval)), 'x)'
        else
          write (outu, *) 'Unable to perform exponential fit: ', &
                                       'there is at least one nonpositive y-value'
        end if

        call PowerLeastSquares(ds, aval, bval, suces)
        write (outu, *) ! Blank line
        if (suces .eqv. .true.) then
          write (outu, *) 'Power fit:        y = ', trim(real2str(bval)), &
                                       ' * x^', trim(real2str(aval))
        else
          write (outu, *) 'Unable to perform power fit: ', &
                                       'there is at least one nonpositive x- or y-value'
        end if

        write (outu, *) ! Blank line
        
      case (4) ! Option 4 - Exit
        ! Allow loop to stop

      case default
        write (*, *) 'The option you chose is not valid.'
    end select
    
  end do
  
  stop
  
contains
  
  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
  !
  ! LoadDataSet - Ask the user for an input filename, and read data points from
  !               that file into the ds%datapt array, storing the number
  !               of points in ds%npts and computing sums
  !
  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
  subroutine LoadDataSet(ds)
  
    ! Declare all variables
    implicit none
    ! Type                         Name(s)             Description
    type(Tdataset), intent(out) :: ds                ! Data points and sums
    character (len=64)          :: infile            ! Input filename
    integer                     :: ieof              ! File status
    integer                     :: indx              ! Loop counter
    integer                     :: inu               ! Input file unit number
    integer                     :: imaxpairs         ! Maximum number of pairs
    
    ! Default maximum number of pairs to array size
    imaxpairs = ubound(ds%datapt,2)
    
    ! Ask the user for the name of the input file
    write (*, *) 'Enter the input filename or - for keyboard input: '
    read (*, *) infile
    
    ! Determine the input unit number
    if (infile .eq. '-') then
    
      inu = 5     ! Read from keyboard (standard input)
      
      write (*, *) 'How many pairs? '
      read (*, *) imaxpairs
      
      ! Make sure this number is legal
      if (imaxpairs .lt. 1 .or. imaxpairs .gt. ubound(ds%datapt,2)) then
        write (*, *) 'Number of pairs must be between 1 and ', &
          trim(int2str(ubound(ds%datapt,2)))
        stop
      end if
      
      write (*, *) 'Enter pairs, one per line'
      write (*, *) ! End line before input
      
    else
    
      inu = 10    ! Read from file
    
      ! Open the file
      open (10, file = infile, status = 'old', iostat = ieof)
      if (ieof .ne. 0) then
        write (*, *) 'The specified file could not be opened.'
        stop
      end if
      
    end if

    ! Initialize the members of the dataset structure
    ds%npts      = 0
    ds%valsum(1) = 0.0
    ds%valsum(2) = 0.0
    ds%valsum(3) = 0.0
    ds%valsum(4) = 0.0
    ds%sqsum(1)  = 0.0
    ds%sqsum(2)  = 0.0
    ds%sqsum(3)  = 0.0
    ds%sqsum(4)  = 0.0
    ds%xysum(1)  = 0.0
    ds%xysum(2)  = 0.0
    ds%xysum(3)  = 0.0
    ds%has_np(1) = .false.
    ds%has_np(2) = .false.

    ! Read data points until EOF or imaxpairs reached
    do while (ds%npts < imaxpairs)
    
      ! Load the points into the datapt array, 
      read (inu, *, end = 110) &
        ds%datapt(1, ds%npts + 1), &
        ds%datapt(2, ds%npts + 1)
      
      ! Calculate ln x, or set has_np if the value is nonpositive
      if (ds%datapt(1, ds%npts + 1) .le. 0) then
        ds%datapt(3, ds%npts + 1) = 0
        ds%has_np(1) = .true.
      else
        ds%datapt(3, ds%npts + 1) = log(ds%datapt(1, ds%npts + 1))
      end if
      
      ! Calculate ln y, or set has_np if the value is nonpositive
      if (ds%datapt(2, ds%npts + 1) .le. 0) then
        ds%datapt(4, ds%npts + 1) = 0
        ds%has_np(2) = .true.
      else
        ds%datapt(4, ds%npts + 1) = log(ds%datapt(2, ds%npts + 1))
      end if
        
      ! Update the number of points
      ds%npts      = ds%npts      + 1

      ! Update the value sums and square sums
      do indx = 1,4
        ds%valsum(indx) = ds%valsum(indx) + ds%datapt(indx, ds%npts)
        ds%sqsum(indx)  = ds%sqsum(indx)  + ds%datapt(indx, ds%npts)**2
      end do
      
      ! Update the xy sums (xy, x ln y, ln x ln y)
      ds%xysum(1)  = ds%xysum(1) + &
        ds%datapt(1, ds%npts) * ds%datapt(2, ds%npts)
      ds%xysum(2)  = ds%xysum(2) + &
        ds%datapt(1, ds%npts) * ds%datapt(4, ds%npts)
      ds%xysum(3)  = ds%xysum(3) + &
        ds%datapt(3, ds%npts) * ds%datapt(4, ds%npts)
      
    end do
    
    ! If we get here and we're reading from a file, there are more than
    ! ubound(ds%datapt,2) points in the file, so display a warning
    if (inu .eq. 10) then
      write (*, *) 'WARNING: Only the first ', &
        trim(int2str(ubound(ds%datapt,2))), ' points will be used'
    end if

    ! We have read all points from the file; close it
110 if (inu .eq. 10) then
      close (10)
    end if
    
    return

  end subroutine LoadDataSet
  
  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
  !
  ! ShowDataPoints - Display the list of data points
  !
  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
  subroutine ShowDataPoints(ds)
  
    ! Declare all variables
    implicit none
    ! Type                         Name(s)      Description
    type(Tdataset), intent(in)  :: ds         ! Data points and sums
    integer                     :: indx       ! Loop counter
  
    ! Display a header
    write (*, '(a5,a2,2a10)') '=====', '==', '==========', &
                                                          '=========='
    write (*, '(a5,2x,2a10)') 'i', 'x_i', 'y_i'
    write (*, '(a5,a2,2a10)') '=====', '==', '==========', &
                                                          '=========='

    ! Display the list of points
    do indx = 1,ds%npts
      write (*, '(i5,a,2f10.4)') &
        indx, ': ', ds%datapt(1,indx), ds%datapt(2,indx)
    end do
    
    ! Display a footer
    write (*, '(a5,a2,2a10)') '=====', '==', '==========', &
                                                          '=========='
    
    return
    
  end subroutine ShowDataPoints
  
  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
  !
  ! SquareDistances - Calculate the sums of squares of distances of a set of
  !                   points from a given line f(x) = mx + b
  !
  ! finv denotes the inverse of the function f
  !
  ! Sum of squares of vertical distances is returned in ssvd
  ! Sum of squares of horizontal distances is returned in sshd
  ! Sum of squares of perpendicular distances is returned in sspd
  !
  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
  subroutine SquareDistances(ds, slopem, intrcpt, ssvd, sshd, sspd)
  
    ! Declare all variables
    implicit none
    ! Type                         Name(s)      Description
    type(Tdataset), intent(in)  :: ds         ! Data points and sums
    real, intent(in)            :: slopem     ! Slope of the line
    real, intent(in)            :: intrcpt    ! Line's y-intercept
    real                        :: fx         ! f(x) at a given x
    real                        :: finvy      ! finv(y) at a given y
    real                        :: perpx      ! x-coord where a perpendicular
                                              !   line through (x_i,y_i)
                                              !   intersects
    real                        :: perpy      ! y-coord of intersection
    real :: vdist, hdist, pdist               ! Distances of a particular point
    real, intent(out)           :: ssvd       ! Sum of squares of vert dist's
    real, intent(out)           :: sshd       ! Sum of squares of horiz dist's
    real, intent(out)           :: sspd       ! Sum of squares of perp dist's
    integer                     :: indx       ! Loop counter
    
    ! Initialize sums to 0.0
    ssvd = 0.0
    sshd = 0.0
    sspd = 0.0
  
    ! Add the squares of the distances
    do indx = 1,ds%npts
      ! Calculate f(x_i) = m x_i + b
      fx = slopem * ds%datapt(1,indx) + intrcpt
      
      ! Calculate finv(y_i) = (y_i - b) / m
      finvy = (ds%datapt(2,indx) - intrcpt) / slopem
      
      ! Vertical distance = |f(x_i) - y_i|
      vdist = abs(fx - ds%datapt(2,indx))
      
      ! Horizontal distance = |finv(y_i) - x_i|
      hdist = abs(finvy - ds%datapt(1,indx))
      
      ! Perpendicular distance is more complicated
      
      ! The perpendicular line through (x_i, y_i) will intersect
      ! at x-coordinate (y_i + x_i/m - b)/(m + 1/m)
      ! and y-coordinate f(perpx)
      perpx = (ds%datapt(2,indx) + ds%datapt(1,indx)/slopem - intrcpt) &
              / (slopem + 1.0/slopem)
      perpy = slopem * perpx + intrcpt
      
      ! Perpendicular distance is now given by the distance formula
      !    ________________________________
      !   /              2                2
      ! \/  (y_i - perpy)  + (x_i - perpx)
      !
      pdist = sqrt((ds%datapt(2,indx) - perpy)**2 + (ds%datapt(1,indx) - perpx)**2)
      
      ! Now add these distances squared to the sums
      ssvd = ssvd + vdist**2
      sshd = sshd + hdist**2
      sspd = sspd + pdist**2
    end do
    
    return
    
  end subroutine SquareDistances
  
  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
  !
  ! OpenOutput - Open an output file and return the unit number
  !
  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
  integer function OpenOutput() result(outu)

    ! Declare all variables
    implicit none
    ! Type                  Name(s)             Description
    character (len=64) :: outfile           ! Output filename
    integer            :: ieof              ! File status

    ! Ask the user for the name of the output file
    write (*, *) 'Enter the output filename or - for screen display: '
    read (*, *) outfile
    
    if (outfile .eq. '-') then
      outu = 6  ! Standard output
    else
    
      ! Open the file
      open (10, file = outfile, status = 'replace', iostat = ieof)
      if (ieof .ne. 0) then
        write (*, *) 'The specified file could not be opened.'
        stop
      end if
      
      outu = 10 ! File output
    end if
    
    return
    
  end function OpenOutput
  
  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
  !
  ! LinearLeastSquares - Compute a linear least squares line y = ax + b
  !                      using vertical or horizontal distances
  !
  ! For vertical distances, set xcol = 1 and ycol = 2
  ! For horizontal distances, set xcol = 2 and ycol = 1
  ! Other values of xcol and ycol are used by the power and exponential
  !   least squares functions (see below)
  ! Values of a and b will be stored in aval and bval
  !
  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
  subroutine LinearLeastSquares(ds, xcol, ycol, aval, bval)
  
    ! Declare all variables
    implicit none
    ! Type                         Name(s)      Description
    type(Tdataset), intent(in)  :: ds         ! Data points and sums
    integer, intent(in)         :: xcol       ! Which column contains x-values
                                              !   (independent data)?
    integer, intent(in)         :: ycol       ! Which column contains y-values
                                              !   (dependent data)?
    integer                     :: xycol      ! Which xyval to use?
    real, intent(out)           :: aval       ! Value of a
    real, intent(out)           :: bval       ! Value of b
    real                        :: dval       ! Value of delta (denominator)
    
    ! Determine which xyval to use (xy, x ln y, or ln x ln y)
    if (xcol .eq. 1 .and. ycol .eq. 2 .or. xcol .eq. 2 .and. ycol .eq. 1) then
      xycol = 1
    else if (xcol .eq. 1 .and. ycol .eq. 4 .or. xcol .eq. 4 .and. ycol .eq. 1) then
      xycol = 2
    else if (xcol .eq. 3 .and. ycol .eq. 4 .or. xcol .eq. 4 .and. ycol .eq. 3) then
      xycol = 3
    else
      write (*, *) 'ERROR: Invalid call to LinearLeastSquares'
      write (*, *) 'Unable to continue'
      stop
    end if
    
    ! Compute the denominator:
    !
    !             n                n        2
    !            ___    2      (  ___     )
    ! delta = n  \    x    -  (   \    x   )
    !            /__   i      (   /__   i  )
    !                          (          )
    !            i=1              i=1
    
    dval = ds%npts * ds%sqsum(xcol) - ds%valsum(xcol)**2
    
    ! Compute a:
    !
    !          n            n        n
    !         ___          ___      ___
    !      n  \    x y  -  \    x   \    y
    !         /__   i i    /__   i  /__   i
    !
    !         i=1          i=1      i=1
    ! a = ----------------------------------
    !                   delta
    
    aval = ( ds%npts * ds%xysum(xycol) -         &
             ds%valsum(xcol) * ds%valsum(ycol) ) &
           / dval
    
    
    ! Compute b:
    !
    !       n        n            n        n
    !      ___      ___    2     ___      ___
    !      \    y   \    x    -  \    x   \    x y
    !      /__   i  /__   i      /__   i  /__   i i
    !
    !      i=1      i=1          i=1      i=1
    ! b = ------------------------------------------
    !                   delta
    
    bval = ( ds%valsum(ycol) * ds%sqsum(xcol) -   &
             ds%valsum(xcol) * ds%xysum(xycol)  ) &
           / dval
           
    ! If we are calculating using horizontal distances, we need to
    ! solve x=ay+b for y (y = 1/a * x - b/a) and change a and b
    ! accordingly
    if (xcol .gt. ycol) then
      aval = 1.0 / aval
      bval = -bval * aval
    end if
    
    return
    
  end subroutine LinearLeastSquares
  
  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
  !
  ! PerpLeastSquares - Compute a linear least squares line y = ax + b
  !                    using perpendicular distances
  !
  ! To use a = -p + sqrt(...), set subtr to an even value
  ! To use a = -p - sqrt(...), set subtr to an odd value
  ! Values of a and b will be stored in aval and bval
  !
  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
  subroutine PerpLeastSquares(ds, subtr, aval, bval)
  
    ! Declare all variables
    implicit none
    ! Type                         Name(s)      Description
    type(Tdataset), intent(in)  :: ds         ! Data points and sums
    integer, intent(in)         :: subtr      ! Determines which of two values
                                              !   of a will be chosen
    real, intent(out)           :: aval       ! Value of a
    real, intent(out)           :: bval       ! Value of b
    real                        :: pval       ! Value of p
    
    ! Compute p:
    !
    !         (   n           n       )    [  (  n      )  2   (  n      )  2 ]
    !        (   ___    2    ___    2  )   [ (  ___      )    (  ___      )   ]
    !      n (   \    x   -  \    y    ) - [ (  \    x   )  - (  \    y   )   ]
    !        (   /__   k     /__   k   )   [ (  /__   k  )    (  /__   k  )   ]
    !        (                         )   [  (         )      (         )    ]
    !         (  k=1         k=1      )    [    k=1              k=1          ]  
    ! p = ----------------------------------------------------------------------
    !         (     n           n       n     )
    !        (     ___         ___     ___     )
    !      2 (  n  \   x y  -  \   x   \   y   )
    !        (     /__  k k    /__  k  /__  k  )
    !        (                                 )
    !         (    k=1         k=1     k=1    )
    
    pval = (ds%npts*(ds%sqsum(1)-ds%sqsum(2)) - (ds%valsum(1)**2 - ds%valsum(2)**2)) &
           / (2 * (ds%npts*ds%xysum(1) - ds%valsum(1)*ds%valsum(2)))
    
    !                        _______
    !                       / 2
    ! Compute a = -p +/-   / p  + 1
    !                    \/
    
    aval = -pval + (-1)**subtr * sqrt(pval**2 + 1)

    !               n           n
    !              ___         ___
    !              \    y  - a \    x
    !              /__   k     /__   k
    !
    !              k=1         k=1
    ! Compute b = ---------------------
    !                       n
    
    bval = ( ds%valsum(2) - aval * ds%valsum(1) ) / ds%npts
    
    return
    
  end subroutine PerpLeastSquares
  
  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
  !                                                                     ax
  ! ExpLeastSquares - Compute an exponential least squares curve y = b e
  !                   using vertical distances
  !
  ! Values of a and b will be stored in aval and bval
  !
  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
  subroutine ExpLeastSquares(ds, aval, bval, suces)
  
    ! Declare all variables
    implicit none
    ! Type                         Name(s)      Description
    type(Tdataset), intent(in)  :: ds         ! Data points and sums
    real, intent(out)           :: aval       ! Value of a
    real, intent(out)           :: bval       ! Value of b
    logical, intent(out)        :: suces      ! Success?

    ! Make sure all y-values are positive
    if (ds%has_np(2) .eqv. .true.) then
      suces = .false.
      return
    end if
    
    ! Exponential least squares is done by reducing the problem to a linear
    ! one, which is more easily solved.
    
    ! We want    y  = b e^(ax)
    ! ==>     ln(y) = ln(b e^(ax)
    ! ==>     ln(y) = ln(b) + ax
    ! ==>     ln(y) = ax + ln(b)
    
    ! Use linear least squares on ln(y) = ax + ln(b)
    call LinearLeastSquares(ds, 1, 4, aval, bval)
    ! The variable bval now contains ln(b); make it contain b instead
    bval = exp(bval)
    
    ! Now     ln(y) = ax + ln(b)
    ! So         y  = e^(ax + ln(b))
    ! ==>        y  = e^(ax) e^(ln(b))
    ! ==>        y  = e^(ax) b
    ! ==>        y  = b e^(ax)
    
    suces = .true.
    return
  
  end subroutine ExpLeastSquares
  
  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
  !                                                           a
  ! PowerLeastSquares - Compute an least squares curve y = b x
  !                     using vertical distances
  !
  ! Values of a and b will be stored in aval and bval
  !
  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
  subroutine PowerLeastSquares(ds, aval, bval, suces)
  
    ! Declare all variables
    implicit none
    ! Type                         Name(s)      Description
    type(Tdataset), intent(in)  :: ds         ! Data points and sums
    real, intent(out)           :: aval       ! Value of a
    real, intent(out)           :: bval       ! Value of b
    logical, intent(out)        :: suces      ! Success?

    ! Make sure all values are positive
    if ((ds%has_np(1) .eqv. .true.) .or. (ds%has_np(2) .eqv. .true.)) then
      suces = .false.
      return
    end if
    
    ! Exponential least squares is done by reducing the problem to a linear
    ! one, which is more easily solved.
    
    ! We want    y  = b x^a
    ! ==>     ln(y) = ln(b x^a)
    ! ==>     ln(y) = ln(b) + ln(x^a)
    ! ==>     ln(y) = ln(b) + a ln(x)
    ! ==>     ln(y) = a ln(x) + ln(b)
    
    ! Use linear least squares on ln(y) = a ln(x) + ln(b)
    call LinearLeastSquares(ds, 3, 4, aval, bval)
    ! The variable bval now contains ln(b); make it contain b instead
    bval = exp(bval)
    
    ! Now     ln(y) = a ln(x) + ln(b)
    !
    !                  a ln(x) + ln(b)
    ! ==>        y  = e
    !
    !                  a ln(x)  ln(b)
    ! ==>        y  = e        e
    !
    !                   a ln(x)
    ! ==>        y  = (e )      b
    !
    !                   ln(x) a
    ! ==>        y  = (e     )  b
    !
    !                  a
    ! ==>        y  = x  b
    !
    !                    a
    ! ==>        y  = b x
    
    suces = .true.
    return
  
  end subroutine PowerLeastSquares
  
  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
  !
  ! int2str - Convert an integer i to a string
  !
  ! The string will need to be trim()ed before displaying
  !
  ! Shouldn't something like this be built in?!
  !
  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
  character(len=32) function int2str(i) result(s)
  
    ! Declare all variables
    implicit none
    
    ! Type                 Name       Description
    integer, intent(in) :: i        ! The number to convert
    integer             :: ival     ! The number, with leading digits removed
    integer             :: ndigits  ! The number of digits in the number
    integer             :: indx     ! Loop counter
    integer             :: is_neg   ! 0 if negative, 1 if positive

    ! Start with a 0 string, overwriting the 0 if necessary
    s = '0'
    
    ! Handle the cases where i is <= 0
    if (i .eq. 0) then
      return
    else if (i .lt. 0) then
      s(1:1) = '-'  ! Prefix output with a minus sign
      is_neg = 1    ! There is one extra character at the start of the number
      ival = -i     ! Now write out the absolute value of i
    else
      is_neg = 0    ! It's not negative
      ival = i      ! We want to write out i
    end if
    
    ! The number of digits needed to write out an integer i in base b is
    ! |           |
    ! |  log (i)  | + 1
    ! |_    b    _|
    !
    ndigits = floor(log10(ival*1.0)) + 1
    
    ! Write out the digits in order, starting at the left
    do indx = 1, ndigits
      ! What is the first digit's ASCII code?  Append it to the string
      s(indx+is_neg:indx+is_neg) = char(48 + ival/10**(ndigits-indx))
      ! Now remove the first digit from ival
      ival = ival - (ival/10**(ndigits-indx))*10**(ndigits-indx)
    end do
  
    return
    
  end function int2str
  
  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
  !
  ! real2str - Convert a real number r to a string
  !
  ! The string will need to be trim()ed before displaying
  !
  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
  character(len=128) function real2str(r) result(s)
  
    ! Declare all variables
    implicit none
    
    ! Type                 Name       Description
    real, intent(in)    :: r        ! The number to convert
    real                :: rval     ! Value of |r|
    integer             :: intpt    ! The integer part of the number
    integer             :: ifracpt  ! The fractional part of the number
                                    !   (rounded somewhat)
    character(len=1)    :: is_neg   ! '-' if negative, ' ' otherwise
    character(len=32)   :: sintpt   ! The integer part, as a string
    character(len=32)   :: slead0   ! Leading 0s for fractional part
    character(len=32)   :: sfracpt  ! The fractional part, as a string
    integer             :: nfdigs=4 ! Number of fractional digits
    
    ! If the number's negative, remember this, but make it positive
    if (r .lt. 0) then
      is_neg = '-'
      rval = -r
    else
      is_neg = ' '
      rval = r
    end if

    ! Integer part = |_ r _|
    intpt = floor(rval)
    
    ! To get the fractional part, subtract off the integer part, multiply
    ! by 10**(# decimal places), and take the floor to drop off extra digits
    ifracpt = floor((rval-intpt) * 10**nfdigs)
    
    ! Now convert the integer and fractional parts to strings
    sintpt = int2str(intpt)
    sfracpt = int2str(ifracpt)
    slead0 = repeat('0', nfdigs-len_trim(sfracpt))
    
    ! Now just concatenate the number together
    s = trim(is_neg) // &  ! Negative sign (if any)
        trim(sintpt) // &  ! Integer part
        '.' // &           ! Decimal point
        trim(slead0) // &  ! Fractional part leading zeros
        int2str(ifracpt)   ! Fractional part
    
    return
    
  end function real2str
  
end program BstFitProj
