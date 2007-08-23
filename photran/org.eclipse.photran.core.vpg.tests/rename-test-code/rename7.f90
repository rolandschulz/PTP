!          12
block data bdn

implicit none
!          12  16  20  24  28
integer :: v1, v2, v3, v4, v5
!       9        18  22    28  32       41  45
common /common1/ v1, v2/zz/v3,          v4, v5 ! Replacing /zz/ with // causes parse error    Adding /common1/ at 31 causes semantic error
!     7
save /common1/
!    6   10
data v1, v2 /3, 4/
!              16
end block data bdn
end
