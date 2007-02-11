!          NOTE 4.57
!          Because no parent components appear in the defined component ordering, a value for a parent
!          component can be specified only with a component keyword. Examples of equivalent values using
!          types defined in Note 4.59:

          ! Create values with components x = 1.0, y =           2.0, color = 3.

          TYPE(POINT) :: PV = POINT(1.0, 2.0)        !           Assume components of TYPE(POINT)
                                                     !           are accessible here.

          COLOR_POINT( point=point(1,2), color=3)    !           Value for parent component
          COLOR_POINT( point=PV, color=3)            !           Available even if TYPE(point)
                                                     !           has private components
          COLOR_POINT( 1, 2, 3)                      !           All components of TYPE(point)
                                                     !           need to be accessible.
end
