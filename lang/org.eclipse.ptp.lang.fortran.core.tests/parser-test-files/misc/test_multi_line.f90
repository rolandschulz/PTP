module my_module

contains
  subroutine subroutine1(integer1, real1)  & 
       & bind(c)
  end subroutine subroutine1

  subroutine subroutine2(integer1, real1, value1, function1, hello1, &
       & integer2, real2)  & 
       & bind(c)
  end subroutine subroutine2

end module my_module

inte&  
  &ger i

inte&
&ger j

i = &
& 3

end
