!          NOTE 4.41
!          The following example illustrates the use of an individual component access-spec to override the
!          default accessibility:

                 TYPE MIXED
                   PRIVATE
                   INTEGER :: I
                   INTEGER, PUBLIC :: J
                 END TYPE MIXED

                 TYPE (MIXED) :: M
end

!          The component M%J is accessible in any scoping unit where M is accessible; M%I is accessible
!          only within the module containing the TYPE MIXED definition, and within its descendants.
