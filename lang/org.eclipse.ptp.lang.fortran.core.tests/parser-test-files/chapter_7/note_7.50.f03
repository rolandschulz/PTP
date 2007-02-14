type element
   real element_wt
   character (32), pointer :: name
end type element
type(element) chart(200)
real weights(1000)
character(32), target :: names(1000)

forall(i=1:200, weights(i+n-1) > .5)
   chart(i)%element_wt = weights(i+n-1)
   chart(i)%name=>names(i+n-1)
end forall

end

