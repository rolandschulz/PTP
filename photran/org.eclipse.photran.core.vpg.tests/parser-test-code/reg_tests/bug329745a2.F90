program test_preprocessor
    implicit none

#   ifdef __linux
     character, parameter :: sep = '/'
#   else
     character, parameter :: sep = '\'
#   endif

end program test_preprocessor
