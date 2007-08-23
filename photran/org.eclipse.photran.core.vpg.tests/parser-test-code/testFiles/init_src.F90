!******************************************************************************

!  Routine:     init_srcterms()

!  Description: Perform various initializations (apart from the problem-
!               dependent ones) for the source terms module.


        subroutine init_srcterms ()

!==============================================================================

!       use perfmon, ONLY: timer_create
       use perfmon
       implicit none
        
        integer ::  timer_source
        common /timer_source_c/ timer_source

!==============================================================================


        call timer_create("source terms", timer_source)


        call init_burn                  ! Nuclear burning
        call init_heat                  ! Heating source terms
        call init_cool                  ! Cooling source terms
        call init_stir                  ! Driving source terms
        call init_ioniz                 ! ionization module

!==============================================================================

        return
        end

