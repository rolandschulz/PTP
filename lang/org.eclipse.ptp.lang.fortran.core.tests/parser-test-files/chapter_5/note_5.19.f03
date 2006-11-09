! added the type definition to complete the example
type node
   integer :: x
end type node

type(node), target :: head
real, dimension (1000, 1000), target :: a, b

end
